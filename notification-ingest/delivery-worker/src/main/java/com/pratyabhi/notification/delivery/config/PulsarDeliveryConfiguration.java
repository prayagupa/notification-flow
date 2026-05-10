package com.pratyabhi.notification.delivery.config;

import com.pratyabhi.notification.delivery.dlq.DlqArchiver;
import com.pratyabhi.notification.delivery.guard.DeliveredGuard;
import com.pratyabhi.notification.delivery.http.MultiChannelHttpSender;
import com.pratyabhi.notification.delivery.pipeline.DeliveryProcessor;
import com.pratyabhi.notification.delivery.pulsar.PulsarDispatchDeliveryLoop;
import com.pratyabhi.notification.delivery.pulsar.PulsarRetryDeliveryLoop;
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
@ConditionalOnProperty(name = "delivery.pulsar-enabled", havingValue = "true", matchIfMissing = true)
public class PulsarDeliveryConfiguration {

  @Bean(destroyMethod = "close")
  PulsarClient pulsarDeliveryClient(PulsarDeliveryProperties props) throws Exception {
    return PulsarClient.builder().serviceUrl(props.getServiceUrl()).build();
  }

  @Bean(destroyMethod = "close")
  Producer<byte[]> deliveryRetryProducer(PulsarClient pulsarDeliveryClient, PulsarDeliveryProperties props)
      throws Exception {
    return pulsarDeliveryClient
        .newProducer(Schema.BYTES)
        .topic(props.getRetryTopic())
        .batchingMaxPublishDelay(10, TimeUnit.MILLISECONDS)
        .create();
  }

  @Bean(destroyMethod = "close")
  Producer<byte[]> deliveryDlqProducer(PulsarClient pulsarDeliveryClient, PulsarDeliveryProperties props)
      throws Exception {
    return pulsarDeliveryClient
        .newProducer(Schema.BYTES)
        .topic(props.getDlqTopic())
        .batchingMaxPublishDelay(10, TimeUnit.MILLISECONDS)
        .create();
  }

  @Bean(destroyMethod = "close")
  Consumer<byte[]> dispatchDeliveryConsumer(PulsarClient pulsarDeliveryClient, PulsarDeliveryProperties props)
      throws Exception {
    return pulsarDeliveryClient
        .newConsumer(Schema.BYTES)
        .topic(props.getDispatchTopic())
        .subscriptionName(props.getDispatchSubscription())
        .subscriptionType(SubscriptionType.Key_Shared)
        .subscribe();
  }

  @Bean(destroyMethod = "close")
  Consumer<byte[]> retryDeliveryConsumer(PulsarClient pulsarDeliveryClient, PulsarDeliveryProperties props)
      throws Exception {
    return pulsarDeliveryClient
        .newConsumer(Schema.BYTES)
        .topic(props.getRetryTopic())
        .subscriptionName(props.getRetrySubscription())
        .subscriptionType(SubscriptionType.Key_Shared)
        .subscribe();
  }

  @Bean
  DeliveryProcessor deliveryProcessor(
      DeliveryWorkerProperties workerProps,
      MultiChannelHttpSender httpSender,
      Producer<byte[]> deliveryRetryProducer,
      Producer<byte[]> deliveryDlqProducer,
      DeliveredGuard deliveredGuard,
      DlqArchiver dlqArchiver) {
    return new DeliveryProcessor(
        workerProps, httpSender, deliveryRetryProducer, deliveryDlqProducer, deliveredGuard, dlqArchiver);
  }

  @Bean
  PulsarDispatchDeliveryLoop pulsarDispatchDeliveryLoop(
      Consumer<byte[]> dispatchDeliveryConsumer, DeliveryProcessor deliveryProcessor) {
    return new PulsarDispatchDeliveryLoop(dispatchDeliveryConsumer, deliveryProcessor);
  }

  @Bean
  PulsarRetryDeliveryLoop pulsarRetryDeliveryLoop(
      Consumer<byte[]> retryDeliveryConsumer, DeliveryProcessor deliveryProcessor) {
    return new PulsarRetryDeliveryLoop(retryDeliveryConsumer, deliveryProcessor);
  }
}
