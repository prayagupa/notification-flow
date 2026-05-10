package com.pratyabhi.notification.router.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "router")
public class RouterProperties {

  /** When false, skip Pulsar wiring (consumer + producer) — used for tests. */
  private boolean pulsarEnabled = true;

  /** {@code jdbc} (default) or {@code in-memory}. */
  private String registryMode = "jdbc";

  /** {@code redis} (default) or {@code in-memory}. */
  private String dedupMode = "redis";

  /** {@code redis} (default) or {@code in-memory}. */
  private String flagsMode = "redis";

  /** Dedup window in seconds (SDS §5.2 example: 24–48h). */
  private long dedupTtlSeconds = 24 * 60 * 60L;

  /** Redis prefix for dedup keys. */
  private String dedupKeyPrefix = "dedup:";

  /** Redis prefix for feature flag keys. */
  private String flagsKeyPrefix = "flag:";

  /** Feature flag toggling the entire routing pipeline. */
  private String routingFlag = "router.enabled";

  public boolean isPulsarEnabled() {
    return pulsarEnabled;
  }

  public void setPulsarEnabled(boolean pulsarEnabled) {
    this.pulsarEnabled = pulsarEnabled;
  }

  public String getRegistryMode() {
    return registryMode;
  }

  public void setRegistryMode(String registryMode) {
    this.registryMode = registryMode;
  }

  public String getDedupMode() {
    return dedupMode;
  }

  public void setDedupMode(String dedupMode) {
    this.dedupMode = dedupMode;
  }

  public String getFlagsMode() {
    return flagsMode;
  }

  public void setFlagsMode(String flagsMode) {
    this.flagsMode = flagsMode;
  }

  public long getDedupTtlSeconds() {
    return dedupTtlSeconds;
  }

  public void setDedupTtlSeconds(long dedupTtlSeconds) {
    this.dedupTtlSeconds = dedupTtlSeconds;
  }

  public String getDedupKeyPrefix() {
    return dedupKeyPrefix;
  }

  public void setDedupKeyPrefix(String dedupKeyPrefix) {
    this.dedupKeyPrefix = dedupKeyPrefix;
  }

  public String getFlagsKeyPrefix() {
    return flagsKeyPrefix;
  }

  public void setFlagsKeyPrefix(String flagsKeyPrefix) {
    this.flagsKeyPrefix = flagsKeyPrefix;
  }

  public String getRoutingFlag() {
    return routingFlag;
  }

  public void setRoutingFlag(String routingFlag) {
    this.routingFlag = routingFlag;
  }
}
