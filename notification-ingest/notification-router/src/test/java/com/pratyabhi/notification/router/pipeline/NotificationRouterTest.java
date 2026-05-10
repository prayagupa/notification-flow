package com.pratyabhi.notification.router.pipeline;

import static org.assertj.core.api.Assertions.assertThat;

import com.pratyabhi.notification.router.config.RouterProperties;
import com.pratyabhi.notification.router.dedup.InMemoryDedupService;
import com.pratyabhi.notification.router.flags.InMemoryFeatureFlagService;
import com.pratyabhi.notification.router.registry.InMemoryRecipientLookupService;
import com.pratyabhi.notification.router.registry.RecipientChannel;
import com.pratyabhi.notification.router.registry.RecipientProfile;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.List;
import com.pratyabhi.notification.events.v1.ActivityEvent;
import com.pratyabhi.notification.events.v1.ActivityType;
import com.pratyabhi.notification.events.v1.Channel;
import com.pratyabhi.notification.events.v1.NotificationDispatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NotificationRouterTest {

  private InMemoryRecipientLookupService registry;
  private InMemoryDedupService dedup;
  private InMemoryFeatureFlagService flags;
  private InMemoryDispatchPublisher publisher;
  private NotificationRouter router;

  @BeforeEach
  void setUp() {
    registry = new InMemoryRecipientLookupService();
    dedup = new InMemoryDedupService();
    flags = new InMemoryFeatureFlagService();
    publisher = new InMemoryDispatchPublisher();
    RouterProperties props = new RouterProperties();
    router =
        new NotificationRouter(registry, dedup, flags, publisher, props, new SimpleMeterRegistry());
  }

  @Test
  void duplicateIdempotencyKeyYieldsSingleDispatchSet() {
    registry.put(
        new RecipientProfile(
            "user-1",
            "en-US",
            "UTC",
            true,
            true,
            List.of(
                new RecipientChannel(Channel.CHANNEL_PUSH_FCM, "fcm-token"),
                new RecipientChannel(Channel.CHANNEL_EMAIL, "user1@example.com"))));

    ActivityEvent first = newEvent("user-1", "idem-1");
    ActivityEvent duplicate = newEvent("user-1", "idem-1");

    RoutingResult firstResult = router.route(first);
    RoutingResult dupResult = router.route(duplicate);

    assertThat(firstResult).isEqualTo(RoutingResult.DISPATCHED);
    assertThat(dupResult).isEqualTo(RoutingResult.DUPLICATE_SUPPRESSED);
    assertThat(publisher.messages()).hasSize(2);
    assertThat(publisher.messages())
        .extracting(NotificationDispatch::getChannel)
        .containsExactlyInAnyOrder(Channel.CHANNEL_PUSH_FCM, Channel.CHANNEL_EMAIL);
  }

  @Test
  void unknownRecipientIsNotDispatched() {
    ActivityEvent event = newEvent("ghost", "idem-2");

    RoutingResult result = router.route(event);

    assertThat(result).isEqualTo(RoutingResult.RECIPIENT_UNKNOWN);
    assertThat(publisher.messages()).isEmpty();
  }

  @Test
  void preservesPerRecipientKeyOrdering() {
    registry.put(
        new RecipientProfile(
            "user-A",
            "en-US",
            "UTC",
            true,
            false,
            List.of(new RecipientChannel(Channel.CHANNEL_PUSH_FCM, "tokenA"))));
    registry.put(
        new RecipientProfile(
            "user-B",
            "en-US",
            "UTC",
            true,
            false,
            List.of(new RecipientChannel(Channel.CHANNEL_PUSH_FCM, "tokenB"))));

    router.route(newEvent("user-A", "a-1"));
    router.route(newEvent("user-B", "b-1"));
    router.route(newEvent("user-A", "a-2"));
    router.route(newEvent("user-A", "a-3"));
    router.route(newEvent("user-B", "b-2"));

    var perUserKeys =
        publisher.messages().stream()
            .collect(
                java.util.stream.Collectors.groupingBy(
                    NotificationDispatch::getRecipientId,
                    java.util.stream.Collectors.mapping(
                        NotificationDispatch::getSourceIdempotencyKey,
                        java.util.stream.Collectors.toList())));
    assertThat(perUserKeys.get("user-A")).containsExactly("a-1", "a-2", "a-3");
    assertThat(perUserKeys.get("user-B")).containsExactly("b-1", "b-2");
  }

  @Test
  void featureFlagDisablesRouting() {
    flags.set("router.enabled", false);
    registry.put(
        new RecipientProfile(
            "user-1",
            "en-US",
            "UTC",
            true,
            true,
            List.of(new RecipientChannel(Channel.CHANNEL_PUSH_FCM, "fcm-token"))));

    RoutingResult result = router.route(newEvent("user-1", "idem-flag"));

    assertThat(result).isEqualTo(RoutingResult.FLAG_DISABLED);
    assertThat(publisher.messages()).isEmpty();
  }

  @Test
  void noEnabledChannelsIsRecorded() {
    registry.put(
        new RecipientProfile(
            "user-quiet",
            "en-US",
            "UTC",
            false,
            false,
            List.of()));

    RoutingResult result = router.route(newEvent("user-quiet", "idem-quiet"));

    assertThat(result).isEqualTo(RoutingResult.NO_ENABLED_CHANNELS);
    assertThat(publisher.messages()).isEmpty();
  }

  private ActivityEvent newEvent(String recipientId, String idempotencyKey) {
    return ActivityEvent.newBuilder()
        .setEventId(java.util.UUID.randomUUID().toString())
        .setIdempotencyKey(idempotencyKey)
        .setRecipientId(recipientId)
        .setActivityType(ActivityType.ACTIVITY_TYPE_MESSAGE_SENT)
        .setOccurredAtEpochMs(System.currentTimeMillis())
        .setSourceService("messaging")
        .build();
  }
}
