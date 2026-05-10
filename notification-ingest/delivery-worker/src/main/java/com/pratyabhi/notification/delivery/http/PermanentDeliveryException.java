package com.pratyabhi.notification.delivery.http;

/** Misconfiguration, schema, or HTTP 4xx responses that should not be retried indefinitely. */
public class PermanentDeliveryException extends Exception {

  public PermanentDeliveryException(String message) {
    super(message);
  }

  public PermanentDeliveryException(String message, Throwable cause) {
    super(message, cause);
  }
}
