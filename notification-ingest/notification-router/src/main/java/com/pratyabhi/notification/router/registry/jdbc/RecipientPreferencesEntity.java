package com.pratyabhi.notification.router.registry.jdbc;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "recipient_preferences")
public class RecipientPreferencesEntity {

  @Id
  @Column(name = "recipient_id", nullable = false)
  private String recipientId;

  @Column(name = "locale", nullable = false)
  private String locale = "en-US";

  @Column(name = "timezone", nullable = false)
  private String timezone = "UTC";

  @Column(name = "push_enabled", nullable = false)
  private boolean pushEnabled = true;

  @Column(name = "email_enabled", nullable = false)
  private boolean emailEnabled = true;

  @Column(name = "email_address")
  private String emailAddress;

  public String getRecipientId() {
    return recipientId;
  }

  public void setRecipientId(String recipientId) {
    this.recipientId = recipientId;
  }

  public String getLocale() {
    return locale;
  }

  public void setLocale(String locale) {
    this.locale = locale;
  }

  public String getTimezone() {
    return timezone;
  }

  public void setTimezone(String timezone) {
    this.timezone = timezone;
  }

  public boolean isPushEnabled() {
    return pushEnabled;
  }

  public void setPushEnabled(boolean pushEnabled) {
    this.pushEnabled = pushEnabled;
  }

  public boolean isEmailEnabled() {
    return emailEnabled;
  }

  public void setEmailEnabled(boolean emailEnabled) {
    this.emailEnabled = emailEnabled;
  }

  public String getEmailAddress() {
    return emailAddress;
  }

  public void setEmailAddress(String emailAddress) {
    this.emailAddress = emailAddress;
  }
}
