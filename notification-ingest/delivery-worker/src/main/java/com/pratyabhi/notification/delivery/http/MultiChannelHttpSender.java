package com.pratyabhi.notification.delivery.http;

import com.pratyabhi.notification.delivery.config.ChannelHttpProperties;
import com.pratyabhi.notification.events.v1.NotificationDispatch;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Sends dispatch payloads to FCM HTTP v1, APNs HTTP/2 sandbox, or a simple email webhook using
 * {@link HttpClient} on a virtual-thread-per-task executor (Java 21+).
 */
@Component
public class MultiChannelHttpSender {

  private static final Logger log = LoggerFactory.getLogger(MultiChannelHttpSender.class);

  private final ChannelHttpProperties httpProps;
  private final HttpClient httpClient;

  public MultiChannelHttpSender(ChannelHttpProperties httpProps) {
    this.httpProps = httpProps;
    this.httpClient =
        HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(httpProps.getConnectTimeoutMs()))
            .executor(Executors.newVirtualThreadPerTaskExecutor())
            .build();
  }

  /**
   * Performs one HTTP attempt. Callers implement inline retry / retry-topic policy.
   *
   * @throws TransientDeliveryException for I/O errors, HTTP 5xx, 429, 408
   * @throws PermanentDeliveryException for misconfiguration or non-retryable HTTP responses
   */
  public void sendOnce(NotificationDispatch dispatch) throws TransientDeliveryException, PermanentDeliveryException {
    switch (dispatch.getChannel()) {
      case CHANNEL_PUSH_FCM -> sendFcm(dispatch);
      case CHANNEL_PUSH_APNS -> sendApns(dispatch);
      case CHANNEL_EMAIL -> sendEmail(dispatch);
      case CHANNEL_UNSPECIFIED, UNRECOGNIZED ->
          throw new PermanentDeliveryException("Unspecified or unknown channel: " + dispatch.getChannel());
    }
  }

  private void sendFcm(NotificationDispatch dispatch)
      throws TransientDeliveryException, PermanentDeliveryException {
    String url = httpProps.getFcmSendUrl();
    if (url == null || url.isBlank()) {
      throw new PermanentDeliveryException("FCM send URL not configured (delivery.http.fcm-send-url / FCM_SEND_URL)");
    }
    if (httpProps.getFcmBearerToken() == null || httpProps.getFcmBearerToken().isBlank()) {
      throw new PermanentDeliveryException("FCM bearer token not configured (FCM_BEARER_TOKEN)");
    }
    String token = escapeJson(dispatch.getDestination());
    String title = escapeJson(shortTitle(dispatch));
    String body = escapeJson(shortBody(dispatch));
    String json =
        "{\"message\":{\"token\":\""
            + token
            + "\",\"notification\":{\"title\":\""
            + title
            + "\",\"body\":\""
            + body
            + "\"}}}";
    HttpRequest request =
        HttpRequest.newBuilder(URI.create(url))
            .timeout(Duration.ofMillis(httpProps.getRequestTimeoutMs()))
            .header("Authorization", "Bearer " + httpProps.getFcmBearerToken())
            .header("Content-Type", "application/json; charset=UTF-8")
            .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
            .build();
    execute(dispatch, request, "FCM");
  }

  private void sendApns(NotificationDispatch dispatch)
      throws TransientDeliveryException, PermanentDeliveryException {
    String prefix = httpProps.getApnsDeviceUrlPrefix();
    if (prefix == null || prefix.isBlank()) {
      throw new PermanentDeliveryException("APNs URL prefix not configured");
    }
    if (httpProps.getApnsTopic() == null || httpProps.getApnsTopic().isBlank()) {
      throw new PermanentDeliveryException("APNs topic not configured (delivery.http.apns-topic / APNS_TOPIC)");
    }
    if (httpProps.getApnsAuthToken() == null || httpProps.getApnsAuthToken().isBlank()) {
      throw new PermanentDeliveryException("APNs auth JWT not configured (APNS_AUTH_TOKEN)");
    }
    String url = prefix.endsWith("/") ? prefix + dispatch.getDestination() : prefix + "/" + dispatch.getDestination();
    String payload = buildApnsPayload(dispatch);
    HttpRequest request =
        HttpRequest.newBuilder(URI.create(url))
            .timeout(Duration.ofMillis(httpProps.getRequestTimeoutMs()))
            .header("authorization", "bearer " + httpProps.getApnsAuthToken())
            .header("apns-topic", httpProps.getApnsTopic())
            .header("apns-push-type", "alert")
            .header("content-type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
            .build();
    execute(dispatch, request, "APNs");
  }

  private static String buildApnsPayload(NotificationDispatch dispatch) {
    String title = escapeJson(shortTitle(dispatch));
    String body = escapeJson(shortBody(dispatch));
    return "{\"aps\":{\"alert\":{\"title\":\"" + title + "\",\"body\":\"" + body + "\"}}}";
  }

  private void sendEmail(NotificationDispatch dispatch)
      throws TransientDeliveryException, PermanentDeliveryException {
    String url = httpProps.getEmailWebhookUrl();
    if (url == null || url.isBlank()) {
      throw new PermanentDeliveryException(
          "Email webhook URL not configured (delivery.http.email-webhook-url / EMAIL_WEBHOOK_URL)");
    }
    String to = escapeJson(dispatch.getDestination());
    String subject = escapeJson("Notification: " + dispatch.getActivityType().name());
    String text = escapeJson(shortBody(dispatch));
    String json =
        "{\"to\":\"" + to + "\",\"subject\":\"" + subject + "\",\"textBody\":\"" + text + "\"}";
    HttpRequest request =
        HttpRequest.newBuilder(URI.create(url))
            .timeout(Duration.ofMillis(httpProps.getRequestTimeoutMs()))
            .header("Content-Type", "application/json; charset=UTF-8")
            .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
            .build();
    execute(dispatch, request, "EMAIL");
  }

  private void execute(NotificationDispatch dispatch, HttpRequest request, String label)
      throws TransientDeliveryException, PermanentDeliveryException {
    try {
      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      int code = response.statusCode();
      if (code >= 200 && code < 300) {
        log.debug("{} delivery ok dispatch_id={} status={}", label, dispatch.getDispatchId(), code);
        return;
      }
      if (isTransientHttp(code)) {
        throw new TransientDeliveryException(
            label + " HTTP " + code + " body=" + truncate(response.body(), 256), code);
      }
      throw new PermanentDeliveryException(
          label + " HTTP " + code + " body=" + truncate(response.body(), 512));
    } catch (IOException e) {
      throw new TransientDeliveryException(label + " I/O: " + e.getMessage(), -1, e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new TransientDeliveryException(label + " interrupted", -1, e);
    }
  }

  private static boolean isTransientHttp(int code) {
    return code >= 500 || code == 429 || code == 408;
  }

  private static String shortTitle(NotificationDispatch d) {
    return d.getActivityType().name();
  }

  private static String shortBody(NotificationDispatch d) {
    String b = d.getAttributesMap().getOrDefault("preview", d.getActivityType().name());
    return b.length() > 200 ? b.substring(0, 200) : b;
  }

  private static String escapeJson(String raw) {
    if (raw == null) {
      return "";
    }
    return raw.replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t");
  }

  private static String truncate(String s, int max) {
    if (s == null) {
      return "";
    }
    return s.length() <= max ? s : s.substring(0, max) + "...";
  }
}
