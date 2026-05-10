package com.pratyabhi.notification.router.pulsar;

import com.pratyabhi.notification.router.pipeline.DispatchPublisher;
import com.pratyabhi.notification.events.v1.NotificationDispatch;
import org.apache.pulsar.client.api.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Publishes the enriched dispatch message to Pulsar with key = recipient_id. */
public class PulsarDispatchPublisher implements DispatchPublisher {

  private static final Logger log = LoggerFactory.getLogger(PulsarDispatchPublisher.class);

  private final Producer<byte[]> producer;

  public PulsarDispatchPublisher(Producer<byte[]> producer) {
    this.producer = producer;
  }

  @Override
  public void publish(NotificationDispatch dispatch) throws Exception {
    producer
        .newMessage()
        .key(dispatch.getRecipientId())
        .value(dispatch.toByteArray())
        .send();
    log.debug(
        "Published dispatch_id={} recipient_id={} channel={}",
        dispatch.getDispatchId(),
        dispatch.getRecipientId(),
        dispatch.getChannel());
  }
}
