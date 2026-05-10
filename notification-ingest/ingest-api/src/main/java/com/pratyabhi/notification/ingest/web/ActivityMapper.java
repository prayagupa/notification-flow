package com.pratyabhi.notification.ingest.web;

import static nepleaks.events.v1.ActivityType.ACTIVITY_TYPE_COMMENT_CREATED;
import static nepleaks.events.v1.ActivityType.ACTIVITY_TYPE_MESSAGE_SENT;
import static nepleaks.events.v1.ActivityType.ACTIVITY_TYPE_POST_LIKED;
import static nepleaks.events.v1.ActivityType.ACTIVITY_TYPE_UNSPECIFIED;

import java.util.UUID;
import nepleaks.events.v1.ActivityEvent;
import nepleaks.events.v1.ActivityType;

final class ActivityMapper {

  private ActivityMapper() {}

  static ActivityEvent toProto(String eventId, String idempotencyKey, ActivityIngestRequest req) {
    long occurred =
        req.occurredAtEpochMs() != null ? req.occurredAtEpochMs() : System.currentTimeMillis();
    ActivityEvent.Builder b =
        ActivityEvent.newBuilder()
            .setEventId(eventId)
            .setIdempotencyKey(idempotencyKey)
            .setRecipientId(req.recipientId())
            .setActivityType(mapType(req.activityType()))
            .setOccurredAtEpochMs(occurred)
            .setSourceService(req.sourceService() != null ? req.sourceService() : "");
    if (req.attributes() != null) {
      b.putAllAttributes(req.attributes());
    }
    return b.build();
  }

  private static ActivityType mapType(ActivityTypeDto dto) {
    if (dto == null) {
      return ACTIVITY_TYPE_UNSPECIFIED;
    }
    return switch (dto) {
      case MESSAGE_SENT -> ACTIVITY_TYPE_MESSAGE_SENT;
      case POST_LIKED -> ACTIVITY_TYPE_POST_LIKED;
      case COMMENT_CREATED -> ACTIVITY_TYPE_COMMENT_CREATED;
    };
  }

  static String newEventId() {
    return UUID.randomUUID().toString();
  }
}
