package com.pratyabhi.notification.delivery.http;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.pratyabhi.notification.delivery.config.ChannelHttpProperties;
import com.pratyabhi.notification.events.v1.Channel;
import com.pratyabhi.notification.events.v1.NotificationDispatch;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class MultiChannelHttpSenderWireMockTest {

  @RegisterExtension
  static final WireMockExtension wm = WireMockExtension.newInstance().options(com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig().dynamicPort()).build();

  @Test
  void fcm_success_on_200() {
    wm.stubFor(com.github.tomakehurst.wiremock.client.WireMock.post("/fcm").willReturn(com.github.tomakehurst.wiremock.client.WireMock.aResponse().withStatus(200).withBody("{}")));

    ChannelHttpProperties p = new ChannelHttpProperties();
    p.setFcmSendUrl(wm.getRuntimeInfo().getHttpBaseUrl() + "/fcm");
    p.setFcmBearerToken("test-token");
    MultiChannelHttpSender sender = new MultiChannelHttpSender(p);

    NotificationDispatch dispatch =
        NotificationDispatch.newBuilder()
            .setDispatchId("d1")
            .setSourceEventId("e1")
            .setSourceIdempotencyKey("k1")
            .setRecipientId("r1")
            .setChannel(Channel.CHANNEL_PUSH_FCM)
            .setDestination("device-token")
            .setOccurredAtEpochMs(1)
            .setDispatchedAtEpochMs(2)
            .build();

    assertThatCode(() -> sender.sendOnce(dispatch)).doesNotThrowAnyException();
  }

  @Test
  void fcm_transient_on_503() {
    wm.stubFor(com.github.tomakehurst.wiremock.client.WireMock.post("/fcm").willReturn(com.github.tomakehurst.wiremock.client.WireMock.aResponse().withStatus(503)));

    ChannelHttpProperties p = new ChannelHttpProperties();
    p.setFcmSendUrl(wm.getRuntimeInfo().getHttpBaseUrl() + "/fcm");
    p.setFcmBearerToken("t");
    MultiChannelHttpSender sender = new MultiChannelHttpSender(p);

    NotificationDispatch dispatch =
        NotificationDispatch.newBuilder()
            .setDispatchId("d1")
            .setSourceEventId("e1")
            .setSourceIdempotencyKey("k1")
            .setRecipientId("r1")
            .setChannel(Channel.CHANNEL_PUSH_FCM)
            .setDestination("tok")
            .setOccurredAtEpochMs(1)
            .setDispatchedAtEpochMs(2)
            .build();

    assertThatThrownBy(() -> sender.sendOnce(dispatch)).isInstanceOf(TransientDeliveryException.class);
  }
}
