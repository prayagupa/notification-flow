package com.pratyabhi.notification.delivery.pulsar;

import com.pratyabhi.notification.delivery.pipeline.DeliveryProcessor;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Consumes the primary notification dispatch topic (per-recipient {@code key_shared} ordering). */
public class PulsarDispatchDeliveryLoop {

  private static final Logger log = LoggerFactory.getLogger(PulsarDispatchDeliveryLoop.class);

  private final Consumer<byte[]> consumer;
  private final DeliveryProcessor deliveryProcessor;
  private volatile boolean running = true;
  private Thread loop;

  public PulsarDispatchDeliveryLoop(Consumer<byte[]> consumer, DeliveryProcessor deliveryProcessor) {
    this.consumer = consumer;
    this.deliveryProcessor = deliveryProcessor;
  }

  @PostConstruct
  public void start() {
    loop = new Thread(this::runLoop, "delivery-dispatch-consumer");
    loop.setDaemon(true);
    loop.start();
    log.info("Started Pulsar dispatch delivery consumer");
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
        deliveryProcessor.process(msg, false);
        consumer.acknowledge(msg);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return;
      } catch (Exception e) {
        if (msg != null) {
          log.warn("Delivery failed; negative-acknowledging for broker redelivery", e);
          consumer.negativeAcknowledge(msg);
        } else {
          log.error("Unexpected delivery loop error without message", e);
        }
      }
    }
  }
}
