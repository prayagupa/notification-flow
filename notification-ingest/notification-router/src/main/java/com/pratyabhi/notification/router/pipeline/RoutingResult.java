package com.pratyabhi.notification.router.pipeline;

/** Outcome of a single ActivityEvent passing through the router. */
public enum RoutingResult {
  /** New event, fanned out to one or more dispatch messages. */
  DISPATCHED,
  /** Idempotency key was already claimed within the dedup window. */
  DUPLICATE_SUPPRESSED,
  /** Recipient lookup returned no profile. */
  RECIPIENT_UNKNOWN,
  /** Recipient profile had zero enabled channels (push/email all disabled or no devices). */
  NO_ENABLED_CHANNELS,
  /** Routing was disabled by a feature flag. */
  FLAG_DISABLED
}
