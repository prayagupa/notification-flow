package com.pratyabhi.notification.ingest.pulsar;

import com.pratyabhi.notification.events.v1.ActivityEvent;

public interface ActivityEventPublisher {

  void publish(ActivityEvent event) throws Exception;
}
