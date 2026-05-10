package com.pratyabhi.notification.delivery.tools;

import com.pratyabhi.notification.events.v1.NotificationDispatch;
import java.util.concurrent.TimeUnit;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.api.SubscriptionInitialPosition;

/**
 * Standalone replay: reads up to N protobuf payloads from the DLQ topic and republishes them to the
 * dispatch topic with the same routing key (recipient_id) parsed from the payload. Run via
 * {@code ./gradlew :delivery-worker:replayDlq -PdlqLimit=100} from the repo root.
 */
public final class ReplayDlqMain {

  private ReplayDlqMain() {}

  public static void main(String[] args) throws Exception {
    int limit = args.length > 0 ? Integer.parseInt(args[0]) : 50;
    String serviceUrl = env("PULSAR_SERVICE_URL", "pulsar://localhost:6650");
    String dlqTopic = env("PULSAR_DISPATCH_DLQ_TOPIC", "persistent://public/default/notification-dispatch-dlq");
    String dispatchTopic =
        env("PULSAR_DISPATCH_TOPIC", "persistent://public/default/notification-dispatch");
    String subName = env("PULSAR_DLQ_REPLAY_SUBSCRIPTION", "replay-dlq-cli");

    try (PulsarClient client = PulsarClient.builder().serviceUrl(serviceUrl).build();
        Consumer<byte[]> consumer =
            client
                .newConsumer(Schema.BYTES)
                .topic(dlqTopic)
                .subscriptionName(subName)
                .subscriptionInitialPosition(SubscriptionInitialPosition.Earliest)
                .subscribe();
        Producer<byte[]> producer =
            client
                .newProducer(Schema.BYTES)
                .topic(dispatchTopic)
                .batchingMaxPublishDelay(10, TimeUnit.MILLISECONDS)
                .create()) {

      int replayed = 0;
      while (replayed < limit) {
        Message<byte[]> msg = consumer.receive(5, TimeUnit.SECONDS);
        if (msg == null) {
          break;
        }
        String key = extractRecipientKey(msg.getValue());
        producer.newMessage().key(key).value(msg.getValue()).send();
        consumer.acknowledge(msg);
        replayed++;
      }
      System.out.println("Replayed " + replayed + " message(s) from DLQ to " + dispatchTopic);
    }
  }

  private static String extractRecipientKey(byte[] payload) {
    try {
      return NotificationDispatch.parseFrom(payload).getRecipientId();
    } catch (Exception e) {
      return "";
    }
  }

  private static String env(String key, String def) {
    String v = System.getenv(key);
    return v == null || v.isBlank() ? def : v;
  }
}
