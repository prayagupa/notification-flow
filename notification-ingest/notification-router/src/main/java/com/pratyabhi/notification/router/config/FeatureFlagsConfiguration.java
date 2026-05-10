package com.pratyabhi.notification.router.config;

import com.pratyabhi.notification.router.flags.FeatureFlagService;
import com.pratyabhi.notification.router.flags.InMemoryFeatureFlagService;
import com.pratyabhi.notification.router.flags.RedisFeatureFlagService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class FeatureFlagsConfiguration {

  @Bean
  @ConditionalOnProperty(name = "router.flags-mode", havingValue = "redis", matchIfMissing = true)
  FeatureFlagService redisFeatureFlagService(StringRedisTemplate redis, RouterProperties props) {
    return new RedisFeatureFlagService(redis, props.getFlagsKeyPrefix());
  }

  @Bean
  @ConditionalOnProperty(name = "router.flags-mode", havingValue = "in-memory")
  FeatureFlagService inMemoryFeatureFlagService() {
    return new InMemoryFeatureFlagService();
  }
}
