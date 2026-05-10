package com.pratyabhi.notification.ingest.web;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Accepted activity ingested and published to Pulsar.")
public record ActivityAcceptedResponse(String eventId) {}
