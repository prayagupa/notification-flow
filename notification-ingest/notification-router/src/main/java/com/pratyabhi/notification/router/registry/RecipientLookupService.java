package com.pratyabhi.notification.router.registry;

import java.util.Optional;

public interface RecipientLookupService {

  /** Returns the recipient profile, or empty if the recipient is unknown. */
  Optional<RecipientProfile> findByRecipientId(String recipientId);
}
