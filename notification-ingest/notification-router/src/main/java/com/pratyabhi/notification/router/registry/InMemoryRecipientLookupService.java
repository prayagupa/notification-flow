package com.pratyabhi.notification.router.registry;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory recipient registry, used when {@code router.registry-mode=in-memory}
 * (tests, local smoke runs without PostgreSQL).
 */
public class InMemoryRecipientLookupService implements RecipientLookupService {

  private final Map<String, RecipientProfile> store = new ConcurrentHashMap<>();

  public void put(RecipientProfile profile) {
    store.put(profile.recipientId(), profile);
  }

  public void clear() {
    store.clear();
  }

  @Override
  public Optional<RecipientProfile> findByRecipientId(String recipientId) {
    return Optional.ofNullable(store.get(recipientId));
  }
}
