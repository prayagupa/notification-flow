package com.pratyabhi.notification.delivery.dlq;

import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class NoOpDlqArchiver implements DlqArchiver {

  @Override
  public void archive(
      byte[] dispatchPayload, String dispatchId, String errorMessage, Map<String, String> messageProps) {
    // optional extension point for S3/Blob
  }
}
