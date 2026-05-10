package com.pratyabhi.notification.router.flags;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryFeatureFlagService implements FeatureFlagService {

  private final Map<String, Boolean> flags = new ConcurrentHashMap<>();

  public void set(String flag, boolean enabled) {
    flags.put(flag, enabled);
  }

  @Override
  public boolean isEnabled(String flag) {
    return flags.getOrDefault(flag, true);
  }
}
