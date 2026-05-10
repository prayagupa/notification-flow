package com.pratyabhi.notification.router;

import static org.assertj.core.api.Assertions.assertThat;

import com.pratyabhi.notification.router.pipeline.DispatchPublisher;
import com.pratyabhi.notification.router.pipeline.InMemoryDispatchPublisher;
import com.pratyabhi.notification.router.pipeline.NotificationRouter;
import com.pratyabhi.notification.router.pipeline.RoutingResult;
import com.pratyabhi.notification.router.registry.InMemoryRecipientLookupService;
import com.pratyabhi.notification.router.registry.RecipientChannel;
import com.pratyabhi.notification.router.registry.RecipientLookupService;
import com.pratyabhi.notification.router.registry.RecipientProfile;
import java.util.List;
import com.pratyabhi.notification.events.v1.ActivityEvent;
import com.pratyabhi.notification.events.v1.ActivityType;
import com.pratyabhi.notification.events.v1.Channel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RouterContextTest {

  @Autowired private NotificationRouter router;
  @Autowired private DispatchPublisher dispatchPublisher;
  @Autowired private RecipientLookupService recipientLookupService;

  @Test
  void contextLoadsAndRoutesEndToEnd() {
    InMemoryRecipientLookupService inMemRegistry =
        (InMemoryRecipientLookupService) recipientLookupService;
    InMemoryDispatchPublisher inMemPublisher = (InMemoryDispatchPublisher) dispatchPublisher;
    inMemRegistry.put(
        new RecipientProfile(
            "user-ctx",
            "en-US",
            "UTC",
            true,
            false,
            List.of(new RecipientChannel(Channel.CHANNEL_PUSH_FCM, "ctx-token"))));

    ActivityEvent event =
        ActivityEvent.newBuilder()
            .setEventId("evt-1")
            .setIdempotencyKey("ctx-idem-1")
            .setRecipientId("user-ctx")
            .setActivityType(ActivityType.ACTIVITY_TYPE_MESSAGE_SENT)
            .setOccurredAtEpochMs(System.currentTimeMillis())
            .setSourceService("messaging")
            .build();

    assertThat(router.route(event)).isEqualTo(RoutingResult.DISPATCHED);
    assertThat(inMemPublisher.messages()).hasSize(1);
    assertThat(inMemPublisher.messages().get(0).getDestination()).isEqualTo("ctx-token");
  }
}
