package com.pratyabhi.notification.delivery.dlq;

import java.util.Map;

/**
 * Optional hook to copy DLQ payloads to object storage (S3/Blob). Default implementation is
 * no-op; swap in a cloud SDK-backed bean when required.
 */
public interface DlqArchiver {

  void archive(byte[] dispatchPayload, String dispatchId, String errorMessage, Map<String, String> messageProps);
}
