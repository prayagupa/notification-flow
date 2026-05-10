package com.pratyabhi.notification.router.pipeline;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import com.pratyabhi.notification.events.v1.NotificationDispatch;

/** Thread-safe collector used when {@code router.pulsar-enabled=false}. */
public class InMemoryDispatchPublisher implements DispatchPublisher {

  private final List<NotificationDispatch> messages = new CopyOnWriteArrayList<>();

  @Override
  public void publish(NotificationDispatch dispatch) {
    messages.add(dispatch);
  }

  public List<NotificationDispatch> messages() {
    return List.copyOf(messages);
  }

  public void clear() {
    messages.clear();
  }
}
