package com.pratyabhi.notification.delivery.guard;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class InMemoryDeliveredGuardTest {

  @Test
  void suppresses_second_mark() {
    InMemoryDeliveredGuard g = new InMemoryDeliveredGuard();
    assertThat(g.wasAlreadyDelivered("a")).isFalse();
    g.markDelivered("a");
    assertThat(g.wasAlreadyDelivered("a")).isTrue();
  }
}
