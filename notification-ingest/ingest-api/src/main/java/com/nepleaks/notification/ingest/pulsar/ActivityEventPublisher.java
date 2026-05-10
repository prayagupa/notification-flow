package com.nepleaks.notification.ingest.pulsar;

import nepleaks.events.v1.ActivityEvent;

public interface ActivityEventPublisher {

  void publish(ActivityEvent event) throws Exception;
}
