package com.pratyabhi.notification.router.registry;

import com.pratyabhi.notification.events.v1.Channel;

/** Resolved (channel, destination) tuple for a recipient (e.g. one row per device or email). */
public record RecipientChannel(Channel channel, String destination) {

  public RecipientChannel {
    if (channel == null) {
      throw new IllegalArgumentException("channel is required");
    }
    if (destination == null || destination.isBlank()) {
      throw new IllegalArgumentException("destination is required");
    }
  }
}
