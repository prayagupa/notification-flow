package com.nepleaks.notification.ingest.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "HTTP mapping for nepleaks.events.v1.ActivityEvent (excluding server-generated event_id).")
public record ActivityIngestRequest(
    @Schema(description = "Idempotent dedup key (also accepted as Idempotency-Key header)", example = "msg-2026-001")
        String idempotencyKey,
    @NotBlank
        @Schema(description = "Notification recipient; used as Pulsar message key", example = "user-abc")
        String recipientId,
    @NotNull @Schema(description = "Domain activity type") ActivityTypeDto activityType,
    @Schema(description = "Epoch millis when the activity occurred", example = "1715340000000")
        Long occurredAtEpochMs,
    @NotBlank @Schema(description = "Originating product service", example = "messaging") String sourceService,
    @Schema(description = "Optional key/value metadata") java.util.Map<String, String> attributes) {}
