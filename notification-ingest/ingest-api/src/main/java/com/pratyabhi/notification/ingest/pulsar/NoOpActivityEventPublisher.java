package com.pratyabhi.notification.ingest.pulsar;

import com.pratyabhi.notification.events.v1.ActivityEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Used when {@code ingest.pulsar-enabled=false} (e.g. unit tests without a broker).
 */
@Component
@ConditionalOnProperty(name = "ingest.pulsar-enabled", havingValue = "false")
public class NoOpActivityEventPublisher implements ActivityEventPublisher {

  private static final Logger log = LoggerFactory.getLogger(NoOpActivityEventPublisher.class);

  @Override
  public void publish(ActivityEvent event) {
    log.info("Pulsar disabled; would publish event_id={} recipient={}", event.getEventId(), event.getRecipientId());
  }
}
