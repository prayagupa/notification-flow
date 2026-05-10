package com.pratyabhi.notification.router.config;

import com.pratyabhi.notification.router.dedup.DedupService;
import com.pratyabhi.notification.router.flags.FeatureFlagService;
import com.pratyabhi.notification.router.pipeline.DispatchPublisher;
import com.pratyabhi.notification.router.pipeline.NotificationRouter;
import com.pratyabhi.notification.router.registry.RecipientLookupService;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouterPipelineConfiguration {

  @Bean
  NotificationRouter notificationRouter(
      RecipientLookupService recipientLookupService,
      DedupService dedupService,
      FeatureFlagService featureFlagService,
      DispatchPublisher dispatchPublisher,
      RouterProperties routerProperties,
      MeterRegistry meterRegistry) {
    return new NotificationRouter(
        recipientLookupService,
        dedupService,
        featureFlagService,
        dispatchPublisher,
        routerProperties,
        meterRegistry);
  }
}
