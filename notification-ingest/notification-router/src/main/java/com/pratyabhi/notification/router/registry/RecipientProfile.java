package com.pratyabhi.notification.router.registry;

import java.util.List;
import java.util.Optional;

/**
 * View of a recipient that the router needs to fan out a notification:
 * preferences (locale/timezone, channel allow-list) + active channels (devices/email).
 */
public record RecipientProfile(
    String recipientId,
    String locale,
    String timezone,
    boolean pushEnabled,
    boolean emailEnabled,
    List<RecipientChannel> channels) {

  public RecipientProfile {
    channels = channels == null ? List.of() : List.copyOf(channels);
    locale = locale == null ? "en-US" : locale;
    timezone = timezone == null ? "UTC" : timezone;
  }

  public static Optional<RecipientProfile> empty() {
    return Optional.empty();
  }
}
