package com.nepleaks.notification.ingest.web;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Serializable activity kinds aligned with protobuf ActivityType.")
public enum ActivityTypeDto {
  MESSAGE_SENT,
  POST_LIKED,
  COMMENT_CREATED
}
