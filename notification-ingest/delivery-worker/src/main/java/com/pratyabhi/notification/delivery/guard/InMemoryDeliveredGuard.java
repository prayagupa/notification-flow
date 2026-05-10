package com.pratyabhi.notification.delivery.guard;

import java.util.concurrent.ConcurrentHashMap;

/** Process-local idempotency for replay drills; use Redis at the edge for multi-instance workers. */
public class InMemoryDeliveredGuard implements DeliveredGuard {

  private final ConcurrentHashMap<String, Boolean> delivered = new ConcurrentHashMap<>();

  @Override
  public boolean wasAlreadyDelivered(String dispatchId) {
    return Boolean.TRUE.equals(delivered.get(dispatchId));
  }

  @Override
  public void markDelivered(String dispatchId) {
    delivered.put(dispatchId, Boolean.TRUE);
  }
}
