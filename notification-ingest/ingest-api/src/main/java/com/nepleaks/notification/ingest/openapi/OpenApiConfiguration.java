package com.nepleaks.notification.ingest.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

  @Bean
  OpenAPI ingestOpenApi() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Notification activity ingest")
                .description(
                    "Phase 1 ingest API: publishes protobuf ActivityEvent to Apache Pulsar topic "
                        + "(see docs/execution-plan.md). Service auth via X-Api-Key.")
                .version("v1"))
        .components(
            new Components()
                .addSecuritySchemes(
                    "apiKey",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("X-Api-Key")));
  }
}
