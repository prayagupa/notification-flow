package com.pratyabhi.notification.delivery.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "delivery")
public class DeliveryWorkerProperties {

  private boolean pulsarEnabled = true;
  /** HTTP-level retries before escalating to the retry topic. */
  private int maxInlineRetries = 2;
  /** After this many topic-level attempts (dispatch + retries), send to DLQ. */
  private int maxTopicAttempts = 4;
  private long backoffBaseMs = 500L;
  private long backoffMaxMs = 30_000L;
  /** none | memory — memory suppresses duplicate sends after success until restart. */
  private String deliveredGuardMode = "none";
  /** none | log — optional blob archive hook (S3) can replace log impl later. */
  private String dlqArchiveMode = "none";

  public boolean isPulsarEnabled() {
    return pulsarEnabled;
  }

  public void setPulsarEnabled(boolean pulsarEnabled) {
    this.pulsarEnabled = pulsarEnabled;
  }

  public int getMaxInlineRetries() {
    return maxInlineRetries;
  }

  public void setMaxInlineRetries(int maxInlineRetries) {
    this.maxInlineRetries = maxInlineRetries;
  }

  public int getMaxTopicAttempts() {
    return maxTopicAttempts;
  }

  public void setMaxTopicAttempts(int maxTopicAttempts) {
    this.maxTopicAttempts = maxTopicAttempts;
  }

  public long getBackoffBaseMs() {
    return backoffBaseMs;
  }

  public void setBackoffBaseMs(long backoffBaseMs) {
    this.backoffBaseMs = backoffBaseMs;
  }

  public long getBackoffMaxMs() {
    return backoffMaxMs;
  }

  public void setBackoffMaxMs(long backoffMaxMs) {
    this.backoffMaxMs = backoffMaxMs;
  }

  public String getDeliveredGuardMode() {
    return deliveredGuardMode;
  }

  public void setDeliveredGuardMode(String deliveredGuardMode) {
    this.deliveredGuardMode = deliveredGuardMode;
  }

  public String getDlqArchiveMode() {
    return dlqArchiveMode;
  }

  public void setDlqArchiveMode(String dlqArchiveMode) {
    this.dlqArchiveMode = dlqArchiveMode;
  }
}
