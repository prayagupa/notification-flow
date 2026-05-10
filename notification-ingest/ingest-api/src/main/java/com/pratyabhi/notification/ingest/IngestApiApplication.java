package com.pratyabhi.notification.ingest;

import com.pratyabhi.notification.ingest.config.IngestProperties;
import com.pratyabhi.notification.ingest.config.PulsarProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
@EnableConfigurationProperties({IngestProperties.class, PulsarProperties.class})
public class IngestApiApplication {

  public static void main(String[] args) {
    SpringApplication.run(IngestApiApplication.class, args);
  }
}
