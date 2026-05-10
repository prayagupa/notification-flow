package com.pratyabhi.notification.delivery.guard;

/** Suppresses duplicate user-visible sends when the same dispatch_id is replayed from DLQ. */
public interface DeliveredGuard {

  boolean wasAlreadyDelivered(String dispatchId);

  void markDelivered(String dispatchId);
}
