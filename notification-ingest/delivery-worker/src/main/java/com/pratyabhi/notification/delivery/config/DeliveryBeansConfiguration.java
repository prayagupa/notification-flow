package com.pratyabhi.notification.delivery.config;

import com.pratyabhi.notification.delivery.dlq.DlqArchiver;
import com.pratyabhi.notification.delivery.dlq.LoggingDlqArchiver;
import com.pratyabhi.notification.delivery.dlq.NoOpDlqArchiver;
import com.pratyabhi.notification.delivery.guard.DeliveredGuard;
import com.pratyabhi.notification.delivery.guard.InMemoryDeliveredGuard;
import com.pratyabhi.notification.delivery.guard.NoOpDeliveredGuard;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DeliveryBeansConfiguration {

  @Bean
  DeliveredGuard deliveredGuard(DeliveryWorkerProperties props) {
    if ("memory".equalsIgnoreCase(props.getDeliveredGuardMode())) {
      return new InMemoryDeliveredGuard();
    }
    return new NoOpDeliveredGuard();
  }

  @Bean
  DlqArchiver dlqArchiver(DeliveryWorkerProperties props, LoggingDlqArchiver logging, NoOpDlqArchiver noop) {
    if ("log".equalsIgnoreCase(props.getDlqArchiveMode())) {
      return logging;
    }
    return noop;
  }
}
