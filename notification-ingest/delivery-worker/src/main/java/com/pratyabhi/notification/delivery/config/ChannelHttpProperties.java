package com.pratyabhi.notification.delivery.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "delivery.http")
public class ChannelHttpProperties {

  private int connectTimeoutMs = 5000;
  private int requestTimeoutMs = 15000;
  /** Full FCM HTTP v1 send URL, e.g. https://fcm.googleapis.com/v1/projects/{id}/messages:send */
  private String fcmSendUrl = "";
  private String fcmBearerToken = "";
  /** Base path before device token, e.g. https://api.sandbox.push.apple.com/3/device/ */
  private String apnsDeviceUrlPrefix = "https://api.sandbox.push.apple.com/3/device/";
  private String apnsTopic = "";
  private String apnsAuthToken = "";
  private String emailWebhookUrl = "";

  public int getConnectTimeoutMs() {
    return connectTimeoutMs;
  }

  public void setConnectTimeoutMs(int connectTimeoutMs) {
    this.connectTimeoutMs = connectTimeoutMs;
  }

  public int getRequestTimeoutMs() {
    return requestTimeoutMs;
  }

  public void setRequestTimeoutMs(int requestTimeoutMs) {
    this.requestTimeoutMs = requestTimeoutMs;
  }

  public String getFcmSendUrl() {
    return fcmSendUrl;
  }

  public void setFcmSendUrl(String fcmSendUrl) {
    this.fcmSendUrl = fcmSendUrl;
  }

  public String getFcmBearerToken() {
    return fcmBearerToken;
  }

  public void setFcmBearerToken(String fcmBearerToken) {
    this.fcmBearerToken = fcmBearerToken;
  }

  public String getApnsDeviceUrlPrefix() {
    return apnsDeviceUrlPrefix;
  }

  public void setApnsDeviceUrlPrefix(String apnsDeviceUrlPrefix) {
    this.apnsDeviceUrlPrefix = apnsDeviceUrlPrefix;
  }

  public String getApnsTopic() {
    return apnsTopic;
  }

  public void setApnsTopic(String apnsTopic) {
    this.apnsTopic = apnsTopic;
  }

  public String getApnsAuthToken() {
    return apnsAuthToken;
  }

  public void setApnsAuthToken(String apnsAuthToken) {
    this.apnsAuthToken = apnsAuthToken;
  }

  public String getEmailWebhookUrl() {
    return emailWebhookUrl;
  }

  public void setEmailWebhookUrl(String emailWebhookUrl) {
    this.emailWebhookUrl = emailWebhookUrl;
  }
}
