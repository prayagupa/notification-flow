package com.pratyabhi.notification.router.config;

import com.pratyabhi.notification.router.registry.InMemoryRecipientLookupService;
import com.pratyabhi.notification.router.registry.RecipientLookupService;
import com.pratyabhi.notification.router.registry.jdbc.JdbcRecipientLookupService;
import com.pratyabhi.notification.router.registry.jdbc.RecipientDeviceRepository;
import com.pratyabhi.notification.router.registry.jdbc.RecipientPreferencesRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
public class RegistryConfiguration {

  @Configuration
  @ConditionalOnProperty(name = "router.registry-mode", havingValue = "jdbc", matchIfMissing = true)
  @EntityScan("com.pratyabhi.notification.router.registry.jdbc")
  @EnableJpaRepositories("com.pratyabhi.notification.router.registry.jdbc")
  static class JdbcRegistry {

    @Bean
    RecipientLookupService recipientLookupService(
        RecipientPreferencesRepository preferences, RecipientDeviceRepository devices) {
      return new JdbcRecipientLookupService(preferences, devices);
    }
  }

  @Configuration
  @ConditionalOnProperty(name = "router.registry-mode", havingValue = "in-memory")
  static class InMemoryRegistry {

    @Bean
    RecipientLookupService recipientLookupService() {
      return new InMemoryRecipientLookupService();
    }
  }
}
