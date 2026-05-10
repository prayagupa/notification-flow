package com.pratyabhi.notification.router.flags;

import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Feature flag stub: looks up {@code flag:<name>} in Redis. Treats values
 * "true"/"1"/"on" (case-insensitive) as enabled, everything else as disabled.
 * Missing keys default to enabled (fail-open) — matches Phase 3 deliverable
 * "feature flags stub (Redis)".
 */
public class RedisFeatureFlagService implements FeatureFlagService {

  private final StringRedisTemplate redis;
  private final String keyPrefix;

  public RedisFeatureFlagService(StringRedisTemplate redis, String keyPrefix) {
    this.redis = redis;
    this.keyPrefix = keyPrefix == null ? "flag:" : keyPrefix;
  }

  @Override
  public boolean isEnabled(String flag) {
    String value = redis.opsForValue().get(keyPrefix + flag);
    if (value == null) {
      return true;
    }
    return switch (value.toLowerCase()) {
      case "true", "1", "on", "enabled" -> true;
      default -> false;
    };
  }
}
