package com.pratyabhi.notification.router.dedup;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Map-based dedup with manual TTL expiry — used when {@code router.dedup-mode=in-memory}
 * (tests, local dev without Redis). Not suitable for production: not durable and not shared.
 */
public class InMemoryDedupService implements DedupService {

  private final Map<String, Instant> expirations = new ConcurrentHashMap<>();

  @Override
  public boolean claim(String key, Duration ttl) {
    Instant now = Instant.now();
    Instant expiresAt = now.plus(ttl);
    Instant existing = expirations.get(key);
    if (existing != null && existing.isAfter(now)) {
      return false;
    }
    Instant prior = expirations.put(key, expiresAt);
    return prior == null || !prior.isAfter(now);
  }

  void clear() {
    expirations.clear();
  }
}
