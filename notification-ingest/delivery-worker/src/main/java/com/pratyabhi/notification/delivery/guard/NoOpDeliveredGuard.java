package com.pratyabhi.notification.delivery.guard;

public class NoOpDeliveredGuard implements DeliveredGuard {

  @Override
  public boolean wasAlreadyDelivered(String dispatchId) {
    return false;
  }

  @Override
  public void markDelivered(String dispatchId) {
    // no-op
  }
}
