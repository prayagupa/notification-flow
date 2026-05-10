package com.pratyabhi.notification.router.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pulsar")
public class PulsarRouterProperties {

  private String serviceUrl = "pulsar://localhost:6650";
  private String activityTopic = "persistent://public/default/activity-events";
  private String dispatchTopic = "persistent://public/default/notification-dispatch";
  private String activitySubscription = "router-activity";

  public String getServiceUrl() {
    return serviceUrl;
  }

  public void setServiceUrl(String serviceUrl) {
    this.serviceUrl = serviceUrl;
  }

  public String getActivityTopic() {
    return activityTopic;
  }

  public void setActivityTopic(String activityTopic) {
    this.activityTopic = activityTopic;
  }

  public String getDispatchTopic() {
    return dispatchTopic;
  }

  public void setDispatchTopic(String dispatchTopic) {
    this.dispatchTopic = dispatchTopic;
  }

  public String getActivitySubscription() {
    return activitySubscription;
  }

  public void setActivitySubscription(String activitySubscription) {
    this.activitySubscription = activitySubscription;
  }
}
