package com.pratyabhi.notification.delivery;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "delivery.pulsar-enabled=false")
class DeliveryWorkerApplicationTest {

  @Test
  void contextLoads() {}
}
