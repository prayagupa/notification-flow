package com.nepleaks.notification.ingest.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pulsar")
public class PulsarProperties {

  private String serviceUrl = "pulsar://localhost:6650";
  /** Fully qualified topic name, e.g. persistent://public/default/activity-events */
  private String topic = "persistent://public/default/activity-events";

  public String getServiceUrl() {
    return serviceUrl;
  }

  public void setServiceUrl(String serviceUrl) {
    this.serviceUrl = serviceUrl;
  }

  public String getTopic() {
    return topic;
  }

  public void setTopic(String topic) {
    this.topic = topic;
  }
}
