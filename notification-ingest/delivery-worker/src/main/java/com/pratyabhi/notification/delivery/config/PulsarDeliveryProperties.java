package com.pratyabhi.notification.delivery.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pulsar")
public class PulsarDeliveryProperties {

  private String serviceUrl = "pulsar://localhost:6650";
  private String dispatchTopic = "persistent://public/default/notification-dispatch";
  private String dispatchSubscription = "delivery-worker-dispatch";
  private String retryTopic = "persistent://public/default/notification-dispatch-retry";
  private String retrySubscription = "delivery-worker-retry";
  private String dlqTopic = "persistent://public/default/notification-dispatch-dlq";

  public String getServiceUrl() {
    return serviceUrl;
  }

  public void setServiceUrl(String serviceUrl) {
    this.serviceUrl = serviceUrl;
  }

  public String getDispatchTopic() {
    return dispatchTopic;
  }

  public void setDispatchTopic(String dispatchTopic) {
    this.dispatchTopic = dispatchTopic;
  }

  public String getDispatchSubscription() {
    return dispatchSubscription;
  }

  public void setDispatchSubscription(String dispatchSubscription) {
    this.dispatchSubscription = dispatchSubscription;
  }

  public String getRetryTopic() {
    return retryTopic;
  }

  public void setRetryTopic(String retryTopic) {
    this.retryTopic = retryTopic;
  }

  public String getRetrySubscription() {
    return retrySubscription;
  }

  public void setRetrySubscription(String retrySubscription) {
    this.retrySubscription = retrySubscription;
  }

  public String getDlqTopic() {
    return dlqTopic;
  }

  public void setDlqTopic(String dlqTopic) {
    this.dlqTopic = dlqTopic;
  }
}
