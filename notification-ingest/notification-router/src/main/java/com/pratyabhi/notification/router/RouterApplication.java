package com.pratyabhi.notification.router;

import com.pratyabhi.notification.router.config.PulsarRouterProperties;
import com.pratyabhi.notification.router.config.RouterProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({RouterProperties.class, PulsarRouterProperties.class})
public class RouterApplication {

  public static void main(String[] args) {
    SpringApplication.run(RouterApplication.class, args);
  }
}
