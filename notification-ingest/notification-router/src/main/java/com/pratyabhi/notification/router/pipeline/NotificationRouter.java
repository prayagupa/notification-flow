package com.pratyabhi.notification.router.pipeline;

import com.pratyabhi.notification.router.config.RouterProperties;
import com.pratyabhi.notification.router.dedup.DedupService;
import com.pratyabhi.notification.router.flags.FeatureFlagService;
import com.pratyabhi.notification.router.registry.RecipientLookupService;
import com.pratyabhi.notification.router.registry.RecipientProfile;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.util.UUID;
import com.pratyabhi.notification.events.v1.ActivityEvent;
import com.pratyabhi.notification.events.v1.NotificationDispatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Phase 2 + 3 orchestrator: dedup → recipient lookup → static fan-out → publish to dispatch topic.
 *
 * <p>Invoked from the Pulsar consumer loop (or directly from tests). Stateless; safe to share
 * across threads as long as the injected {@link DispatchPublisher} is thread-safe.
 */
public class NotificationRouter {

  private static final Logger log = LoggerFactory.getLogger(NotificationRouter.class);

  private final RecipientLookupService recipientLookup;
  private final DedupService dedup;
  private final FeatureFlagService flags;
  private final DispatchPublisher dispatchPublisher;
  private final RouterProperties props;
  private final Counter consumed;
  private final Counter dispatched;
  private final Counter duplicateSuppressed;
  private final Counter recipientUnknown;
  private final Counter noChannels;
  private final Counter flagDisabled;
  private final Counter dispatchPublished;

  public NotificationRouter(
      RecipientLookupService recipientLookup,
      DedupService dedup,
      FeatureFlagService flags,
      DispatchPublisher dispatchPublisher,
      RouterProperties props,
      MeterRegistry meterRegistry) {
    this.recipientLookup = recipientLookup;
    this.dedup = dedup;
    this.flags = flags;
    this.dispatchPublisher = dispatchPublisher;
    this.props = props;
    this.consumed = Counter.builder("router.event.consumed").register(meterRegistry);
    this.dispatched = Counter.builder("router.event.dispatched").register(meterRegistry);
    this.duplicateSuppressed =
        Counter.builder("router.event.duplicate_suppressed").register(meterRegistry);
    this.recipientUnknown =
        Counter.builder("router.event.recipient_unknown").register(meterRegistry);
    this.noChannels = Counter.builder("router.event.no_enabled_channels").register(meterRegistry);
    this.flagDisabled = Counter.builder("router.event.flag_disabled").register(meterRegistry);
    this.dispatchPublished = Counter.builder("router.dispatch.published").register(meterRegistry);
  }

  public RoutingResult route(ActivityEvent event) {
    consumed.increment();

    if (!flags.isEnabled(props.getRoutingFlag())) {
      flagDisabled.increment();
      log.debug("Routing disabled by flag={} event_id={}", props.getRoutingFlag(), event.getEventId());
      return RoutingResult.FLAG_DISABLED;
    }

    String dedupKey = buildDedupKey(event);
    if (!dedup.claim(dedupKey, Duration.ofSeconds(props.getDedupTtlSeconds()))) {
      duplicateSuppressed.increment();
      log.debug("Duplicate suppressed dedup_key={} event_id={}", dedupKey, event.getEventId());
      return RoutingResult.DUPLICATE_SUPPRESSED;
    }

    var profileOpt = recipientLookup.findByRecipientId(event.getRecipientId());
    if (profileOpt.isEmpty()) {
      recipientUnknown.increment();
      log.warn(
          "Recipient unknown recipient_id={} event_id={}",
          event.getRecipientId(),
          event.getEventId());
      return RoutingResult.RECIPIENT_UNKNOWN;
    }

    RecipientProfile profile = profileOpt.get();
    if (profile.channels().isEmpty()) {
      noChannels.increment();
      return RoutingResult.NO_ENABLED_CHANNELS;
    }

    long dispatchedAt = System.currentTimeMillis();
    int publishedCount = 0;
    for (var channel : profile.channels()) {
      NotificationDispatch dispatch =
          NotificationDispatch.newBuilder()
              .setDispatchId(UUID.randomUUID().toString())
              .setSourceEventId(event.getEventId())
              .setSourceIdempotencyKey(event.getIdempotencyKey())
              .setRecipientId(event.getRecipientId())
              .setActivityType(event.getActivityType())
              .setChannel(channel.channel())
              .setDestination(channel.destination())
              .setLocale(profile.locale())
              .setTimezone(profile.timezone())
              .setOccurredAtEpochMs(event.getOccurredAtEpochMs())
              .setDispatchedAtEpochMs(dispatchedAt)
              .putAllAttributes(event.getAttributesMap())
              .build();
      try {
        dispatchPublisher.publish(dispatch);
        dispatchPublished.increment();
        publishedCount++;
      } catch (Exception e) {
        log.error(
            "Failed to publish dispatch event_id={} channel={} dest={}",
            event.getEventId(),
            channel.channel(),
            channel.destination(),
            e);
        throw new RoutingException("dispatch publish failed", e);
      }
    }
    dispatched.increment();
    log.debug(
        "Dispatched event_id={} recipient_id={} channels={}",
        event.getEventId(),
        event.getRecipientId(),
        publishedCount);
    return RoutingResult.DISPATCHED;
  }

  private String buildDedupKey(ActivityEvent event) {
    String idem = event.getIdempotencyKey();
    if (idem != null && !idem.isBlank()) {
      return idem;
    }
    return event.getRecipientId() + ":" + event.getActivityType().name() + ":" + event.getEventId();
  }
}
