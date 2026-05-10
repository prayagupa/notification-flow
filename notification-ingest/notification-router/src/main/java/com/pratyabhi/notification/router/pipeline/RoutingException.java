package com.pratyabhi.notification.router.pipeline;

/** Wraps unrecoverable errors during routing so the consumer loop can NACK / retry. */
public class RoutingException extends RuntimeException {

  public RoutingException(String message, Throwable cause) {
    super(message, cause);
  }
}
