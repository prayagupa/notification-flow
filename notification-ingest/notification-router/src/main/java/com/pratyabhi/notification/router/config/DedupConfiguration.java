package com.pratyabhi.notification.router.config;

import com.pratyabhi.notification.router.dedup.DedupService;
import com.pratyabhi.notification.router.dedup.InMemoryDedupService;
import com.pratyabhi.notification.router.dedup.RedisDedupService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class DedupConfiguration {

  @Bean
  @ConditionalOnProperty(name = "router.dedup-mode", havingValue = "redis", matchIfMissing = true)
  DedupService redisDedupService(StringRedisTemplate redis, RouterProperties props) {
    return new RedisDedupService(redis, props.getDedupKeyPrefix());
  }

  @Bean
  @ConditionalOnProperty(name = "router.dedup-mode", havingValue = "in-memory")
  DedupService inMemoryDedupService() {
    return new InMemoryDedupService();
  }
}
