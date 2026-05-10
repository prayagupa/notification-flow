package com.pratyabhi.notification.delivery.pipeline;

import com.google.protobuf.InvalidProtocolBufferException;
import com.pratyabhi.notification.delivery.config.DeliveryWorkerProperties;
import com.pratyabhi.notification.delivery.dlq.DlqArchiver;
import com.pratyabhi.notification.delivery.guard.DeliveredGuard;
import com.pratyabhi.notification.delivery.http.MultiChannelHttpSender;
import com.pratyabhi.notification.delivery.http.PermanentDeliveryException;
import com.pratyabhi.notification.delivery.http.TransientDeliveryException;
import com.pratyabhi.notification.events.v1.NotificationDispatch;
import java.util.HashMap;
import java.util.Map;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Wired from {@link com.pratyabhi.notification.delivery.config.PulsarDeliveryConfiguration} when Pulsar is enabled. */
public class DeliveryProcessor {

  public static final String PROP_TOPIC_ATTEMPT = "topic_attempt";
  public static final String PROP_LAST_ERROR = "last_error";

  private static final Logger log = LoggerFactory.getLogger(DeliveryProcessor.class);

  private final DeliveryWorkerProperties workerProps;
  private final MultiChannelHttpSender httpSender;
  private final Producer<byte[]> retryProducer;
  private final Producer<byte[]> dlqProducer;
  private final DeliveredGuard deliveredGuard;
  private final DlqArchiver dlqArchiver;

  public DeliveryProcessor(
      DeliveryWorkerProperties workerProps,
      MultiChannelHttpSender httpSender,
      Producer<byte[]> retryProducer,
      Producer<byte[]> dlqProducer,
      DeliveredGuard deliveredGuard,
      DlqArchiver dlqArchiver) {
    this.workerProps = workerProps;
    this.httpSender = httpSender;
    this.retryProducer = retryProducer;
    this.dlqProducer = dlqProducer;
    this.deliveredGuard = deliveredGuard;
    this.dlqArchiver = dlqArchiver;
  }

  /**
   * @param fromRetryTopic when true, exponential backoff is applied from {@link
   *     DeliveryWorkerProperties#getBackoffBaseMs()} using the current topic attempt.
   */
  public void process(Message<byte[]> pulsarMessage, boolean fromRetryTopic) throws Exception {
    NotificationDispatch dispatch;
    try {
      dispatch = NotificationDispatch.parseFrom(pulsarMessage.getValue());
    } catch (InvalidProtocolBufferException e) {
      log.error("Malformed NotificationDispatch — acknowledging to drop poison pill", e);
      return;
    }

    int topicAttempt = parseTopicAttempt(pulsarMessage.getProperty(PROP_TOPIC_ATTEMPT));
    if (fromRetryTopic && topicAttempt > 0) {
      long backoff =
          Math.min(
              workerProps.getBackoffMaxMs(),
              workerProps.getBackoffBaseMs() * (1L << (topicAttempt - 1)));
      try {
        Thread.sleep(backoff);
      } catch (InterruptedException ie) {
        Thread.currentThread().interrupt();
        throw ie;
      }
    }

    if (deliveredGuard.wasAlreadyDelivered(dispatch.getDispatchId())) {
      log.info(
          "Skipping duplicate dispatch_id={} (delivered guard — replay or at-least-once duplicate)",
          dispatch.getDispatchId());
      return;
    }

    try {
      sendWithInlineRetries(dispatch);
    } catch (PermanentDeliveryException e) {
      publishDlq(pulsarMessage, dispatch, topicAttempt, e.getMessage());
      return;
    } catch (TransientDeliveryException e) {
      int next = topicAttempt + 1;
      if (next > workerProps.getMaxTopicAttempts()) {
        publishDlq(pulsarMessage, dispatch, topicAttempt, e.getMessage());
      } else {
        publishRetry(pulsarMessage.getValue(), dispatch, next, e.getMessage());
      }
      return;
    }

    deliveredGuard.markDelivered(dispatch.getDispatchId());
    log.info(
        "Delivered dispatch_id={} recipient_id={} channel={}",
        dispatch.getDispatchId(),
        dispatch.getRecipientId(),
        dispatch.getChannel());
  }

  private void sendWithInlineRetries(NotificationDispatch dispatch)
      throws TransientDeliveryException, PermanentDeliveryException {
    int maxExtra = Math.max(0, workerProps.getMaxInlineRetries());
    TransientDeliveryException lastTransient = null;
    for (int i = 0; i <= maxExtra; i++) {
      try {
        httpSender.sendOnce(dispatch);
        return;
      } catch (PermanentDeliveryException e) {
        throw e;
      } catch (TransientDeliveryException e) {
        lastTransient = e;
        if (i < maxExtra) {
          long sleep = Math.min(workerProps.getBackoffMaxMs(), workerProps.getBackoffBaseMs() * (1L << i));
          try {
            Thread.sleep(sleep);
          } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw e;
          }
        }
      }
    }
    if (lastTransient != null) {
      throw lastTransient;
    }
  }

  private void publishRetry(byte[] rawPayload, NotificationDispatch dispatch, int nextAttempt, String error)
      throws Exception {
    retryProducer
        .newMessage()
        .key(dispatch.getRecipientId())
        .value(rawPayload)
        .property(PROP_TOPIC_ATTEMPT, Integer.toString(nextAttempt))
        .property(PROP_LAST_ERROR, truncate(error, 1024))
        .send();
    log.warn(
        "Published to retry topic dispatch_id={} next_topic_attempt={} error={}",
        dispatch.getDispatchId(),
        nextAttempt,
        error);
  }

  private void publishDlq(Message<byte[]> pulsarMessage, NotificationDispatch dispatch, int topicAttempt, String error)
      throws Exception {
    Map<String, String> props = new HashMap<>();
    props.put("dlq_final_error", truncate(error, 2048));
    props.put("dlq_topic_attempt", Integer.toString(topicAttempt));
    props.put("dlq_source_message_id", pulsarMessage.getMessageId().toString());
    dlqArchiver.archive(pulsarMessage.getValue(), dispatch.getDispatchId(), error, props);
    dlqProducer
        .newMessage()
        .key(dispatch.getRecipientId())
        .value(pulsarMessage.getValue())
        .property("dlq_final_error", truncate(error, 2048))
        .property("dlq_topic_attempt", Integer.toString(topicAttempt))
        .send();
    log.error(
        "Sent to DLQ dispatch_id={} recipient_id={} topic_attempt={} error={}",
        dispatch.getDispatchId(),
        dispatch.getRecipientId(),
        topicAttempt,
        error);
  }

  private static int parseTopicAttempt(String raw) {
    if (raw == null || raw.isBlank()) {
      return 0;
    }
    try {
      return Integer.parseInt(raw.trim());
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  private static String truncate(String s, int max) {
    if (s == null) {
      return "";
    }
    return s.length() <= max ? s : s.substring(0, max);
  }
}
