package com.pratyabhi.notification.router.pulsar;

import com.google.protobuf.InvalidProtocolBufferException;
import com.pratyabhi.notification.router.pipeline.NotificationRouter;
import com.pratyabhi.notification.router.pipeline.RoutingException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import com.pratyabhi.notification.events.v1.ActivityEvent;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Drives the router on a background thread by polling a Pulsar {@code key_shared} subscription on
 * the activity-events topic. Per-recipient ordering is preserved by Pulsar (single consumer thread
 * per key), and we use a single-threaded receive loop here so message acks happen in order.
 */
public class PulsarActivityConsumer {

  private static final Logger log = LoggerFactory.getLogger(PulsarActivityConsumer.class);

  private final Consumer<byte[]> consumer;
  private final NotificationRouter router;
  private volatile boolean running = true;
  private Thread loop;

  public PulsarActivityConsumer(Consumer<byte[]> consumer, NotificationRouter router) {
    this.consumer = consumer;
    this.router = router;
  }

  @PostConstruct
  public void start() {
    loop = new Thread(this::runLoop, "router-pulsar-consumer");
    loop.setDaemon(true);
    loop.start();
    log.info("Started Pulsar activity consumer subscription");
  }

  @PreDestroy
  public void stop() throws Exception {
    running = false;
    if (loop != null) {
      loop.interrupt();
    }
    consumer.close();
  }

  void runLoop() {
    while (running && !Thread.currentThread().isInterrupted()) {
      Message<byte[]> msg = null;
      try {
        msg = consumer.receive();
        ActivityEvent event = ActivityEvent.parseFrom(msg.getValue());
        router.route(event);
        consumer.acknowledge(msg);
      } catch (InvalidProtocolBufferException e) {
        log.error("Malformed ActivityEvent — sending to DLQ via negative ack", e);
        if (msg != null) {
          consumer.negativeAcknowledge(msg);
        }
      } catch (RoutingException e) {
        log.warn("Routing failed; negative-acknowledging for retry", e);
        if (msg != null) {
          consumer.negativeAcknowledge(msg);
        }
      } catch (Exception e) {
        if (e instanceof InterruptedException) {
          Thread.currentThread().interrupt();
          return;
        }
        log.error("Unexpected consumer error", e);
        if (msg != null) {
          consumer.negativeAcknowledge(msg);
        }
      }
    }
  }
}
