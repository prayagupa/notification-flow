package com.nepleaks.notification.ingest.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ingest")
public class IngestProperties {

  /** Comma-separated API keys for service-to-service auth (header X-Api-Key). */
  private String apiKeys = "dev-local-key";

  /** When false, skip creating Pulsar clients and log-only publisher (for tests / local without broker). */
  private boolean pulsarEnabled = true;

  public Set<String> parsedApiKeys() {
    if (apiKeys == null || apiKeys.isBlank()) {
      return Collections.emptySet();
    }
    return new HashSet<>(Arrays.asList(apiKeys.split(",")));
  }

  public String getApiKeys() {
    return apiKeys;
  }

  public void setApiKeys(String apiKeys) {
    this.apiKeys = apiKeys;
  }

  public boolean isPulsarEnabled() {
    return pulsarEnabled;
  }

  public void setPulsarEnabled(boolean pulsarEnabled) {
    this.pulsarEnabled = pulsarEnabled;
  }
}
