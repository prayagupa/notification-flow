package com.nepleaks.notification.ingest.web;

import com.nepleaks.notification.ingest.pulsar.ActivityEventPublisher;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import nepleaks.events.v1.ActivityEvent;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/v1", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
@SecurityRequirement(name = "apiKey")
public class ActivityController {

  private final ActivityEventPublisher publisher;
  private final Counter publishSuccess;

  public ActivityController(ActivityEventPublisher publisher, MeterRegistry registry) {
    this.publisher = publisher;
    this.publishSuccess =
        Counter.builder("ingest.activity.publish.success")
            .description("Activities successfully published to Pulsar")
            .register(registry);
  }

  @PostMapping(path = "/activities", consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.ACCEPTED)
  public ActivityAcceptedResponse ingest(
      @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyHeader,
      @Valid @NotNull @RequestBody ActivityIngestRequest body)
      throws Exception {
    String idempotency =
        idempotencyHeader != null && !idempotencyHeader.isBlank()
            ? idempotencyHeader
            : body.idempotencyKey();
    if (idempotency == null || idempotency.isBlank()) {
      throw new BadRequestException("idempotencyKey or Idempotency-Key header is required");
    }
    String eventId = ActivityMapper.newEventId();
    ActivityEvent event = ActivityMapper.toProto(eventId, idempotency, body);
    publisher.publish(event);
    publishSuccess.increment();
    return new ActivityAcceptedResponse(eventId);
  }
}
