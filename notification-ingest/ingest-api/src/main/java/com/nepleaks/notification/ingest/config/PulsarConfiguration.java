package com.nepleaks.notification.ingest.config;

import java.util.concurrent.TimeUnit;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.Schema;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "ingest.pulsar-enabled", havingValue = "true", matchIfMissing = true)
public class PulsarConfiguration {

  @Bean(destroyMethod = "close")
  public PulsarClient pulsarClient(PulsarProperties props) throws Exception {
    return PulsarClient.builder().serviceUrl(props.getServiceUrl()).build();
  }

  @Bean(destroyMethod = "close")
  @ConditionalOnProperty(name = "ingest.pulsar-enabled", havingValue = "true", matchIfMissing = true)
  public Producer<byte[]> activityProducer(PulsarClient client, PulsarProperties props)
      throws Exception {
    return client
        .newProducer(Schema.BYTES)
        .topic(props.getTopic())
        .batchingMaxPublishDelay(10, TimeUnit.MILLISECONDS)
        .create();
  }
}
