package com.pratyabhi.notification.router.config;

import com.pratyabhi.notification.router.pipeline.DispatchPublisher;
import com.pratyabhi.notification.router.pipeline.InMemoryDispatchPublisher;
import com.pratyabhi.notification.router.pipeline.NotificationRouter;
import com.pratyabhi.notification.router.pulsar.PulsarActivityConsumer;
import com.pratyabhi.notification.router.pulsar.PulsarDispatchPublisher;
import java.util.concurrent.TimeUnit;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.api.SubscriptionType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PulsarConfiguration {

  @Configuration
  @ConditionalOnProperty(name = "router.pulsar-enabled", havingValue = "true", matchIfMissing = true)
  static class Live {

    @Bean(destroyMethod = "close")
    PulsarClient pulsarClient(PulsarRouterProperties props) throws Exception {
      return PulsarClient.builder().serviceUrl(props.getServiceUrl()).build();
    }

    @Bean(destroyMethod = "close")
    Producer<byte[]> dispatchProducer(PulsarClient client, PulsarRouterProperties props)
        throws Exception {
      return client
          .newProducer(Schema.BYTES)
          .topic(props.getDispatchTopic())
          .batchingMaxPublishDelay(10, TimeUnit.MILLISECONDS)
          .create();
    }

    @Bean
    DispatchPublisher dispatchPublisher(Producer<byte[]> producer) {
      return new PulsarDispatchPublisher(producer);
    }

    @Bean(destroyMethod = "close")
    Consumer<byte[]> activityConsumer(PulsarClient client, PulsarRouterProperties props)
        throws Exception {
      return client
          .newConsumer(Schema.BYTES)
          .topic(props.getActivityTopic())
          .subscriptionName(props.getActivitySubscription())
          .subscriptionType(SubscriptionType.Key_Shared)
          .subscribe();
    }

    @Bean
    PulsarActivityConsumer activityConsumerRunner(
        Consumer<byte[]> consumer, NotificationRouter router) {
      return new PulsarActivityConsumer(consumer, router);
    }
  }

  @Configuration
  @ConditionalOnProperty(name = "router.pulsar-enabled", havingValue = "false")
  static class Offline {

    @Bean
    DispatchPublisher dispatchPublisher() {
      return new InMemoryDispatchPublisher();
    }
  }
}
