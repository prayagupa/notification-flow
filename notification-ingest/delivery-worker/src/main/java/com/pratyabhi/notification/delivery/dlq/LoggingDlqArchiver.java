package com.pratyabhi.notification.delivery.dlq;

import java.util.Base64;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingDlqArchiver implements DlqArchiver {

  private static final Logger log = LoggerFactory.getLogger(LoggingDlqArchiver.class);

  @Override
  public void archive(
      byte[] dispatchPayload, String dispatchId, String errorMessage, Map<String, String> messageProps) {
    String b64 =
        dispatchPayload == null || dispatchPayload.length == 0
            ? ""
            : Base64.getEncoder().encodeToString(dispatchPayload);
    log.warn(
        "DLQ archive (log mode) dispatch_id={} error={} props={} payload_b64_len={}",
        dispatchId,
        errorMessage,
        messageProps,
        b64.length());
  }
}
