package com.pratyabhi.notification.router.pipeline;

import com.pratyabhi.notification.events.v1.NotificationDispatch;

public interface DispatchPublisher {

  void publish(NotificationDispatch dispatch) throws Exception;
}
