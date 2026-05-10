package com.pratyabhi.notification.router.flags;

public interface FeatureFlagService {

  /** Returns whether the named flag is enabled (defaults to true if the flag is unknown). */
  boolean isEnabled(String flag);
}
