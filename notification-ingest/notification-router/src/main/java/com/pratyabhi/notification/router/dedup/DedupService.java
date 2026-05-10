package com.pratyabhi.notification.router.dedup;

import java.time.Duration;

/**
 * Idempotent dedup primitive: claim a key for a TTL window.
 * Implementations MUST be atomic (e.g. Redis SET NX + EX).
 */
public interface DedupService {

  /**
   * @return true if the caller acquired the key (first writer wins, proceed with delivery);
   *     false if the key was already set within the TTL window (duplicate, suppress).
   */
  boolean claim(String key, Duration ttl);
}
