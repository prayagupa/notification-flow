package com.pratyabhi.notification.delivery;

import com.pratyabhi.notification.delivery.config.ChannelHttpProperties;
import com.pratyabhi.notification.delivery.config.DeliveryWorkerProperties;
import com.pratyabhi.notification.delivery.config.PulsarDeliveryProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
  DeliveryWorkerProperties.class,
  PulsarDeliveryProperties.class,
  ChannelHttpProperties.class,
})
public class DeliveryWorkerApplication {

  public static void main(String[] args) {
    SpringApplication.run(DeliveryWorkerApplication.class, args);
  }
}
