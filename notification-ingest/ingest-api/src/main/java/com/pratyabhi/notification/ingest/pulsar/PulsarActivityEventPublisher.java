package com.pratyabhi.notification.ingest.pulsar;

import nepleaks.events.v1.ActivityEvent;
import org.apache.pulsar.client.api.Producer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "ingest.pulsar-enabled", havingValue = "true", matchIfMissing = true)
public class PulsarActivityEventPublisher implements ActivityEventPublisher {

  private static final Logger log = LoggerFactory.getLogger(PulsarActivityEventPublisher.class);

  private final Producer<byte[]> producer;

  public PulsarActivityEventPublisher(Producer<byte[]> producer) {
    this.producer = producer;
  }

  @Override
  public void publish(ActivityEvent event) throws Exception {
    byte[] payload = event.toByteArray();
    producer
        .newMessage()
        .key(event.getRecipientId())
        .value(payload)
        .send();
    log.debug("Published event_id={} recipient={}", event.getEventId(), event.getRecipientId());
  }
}
