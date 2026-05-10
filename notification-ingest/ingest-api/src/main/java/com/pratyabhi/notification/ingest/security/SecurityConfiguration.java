package com.pratyabhi.notification.ingest.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http, ApiKeyAuthFilter apiKeyAuthFilter)
      throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        "/actuator/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**")
                    .permitAll()
                    .requestMatchers("/v1/**")
                    .authenticated()
                    .anyRequest()
                    .denyAll())
        .addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }
}
