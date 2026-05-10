package com.pratyabhi.notification.delivery.http;

/** Provider or network failure that may succeed on retry (HTTP 5xx, 429, timeouts, I/O). */
public class TransientDeliveryException extends Exception {

  private final int httpStatus;

  public TransientDeliveryException(String message, int httpStatus) {
    super(message);
    this.httpStatus = httpStatus;
  }

  public TransientDeliveryException(String message, int httpStatus, Throwable cause) {
    super(message, cause);
    this.httpStatus = httpStatus;
  }

  public int getHttpStatus() {
    return httpStatus;
  }
}
