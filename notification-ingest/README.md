# Notification ingest (Phase 0–5)

Implements **Phase 0 (foundations)** through **Phase 5 (reliability hardening)** from [`docs/execution-plan.md`](../docs/execution-plan.md):

- **Gradle Kotlin DSL monorepo** (repo root): modules `notification-contracts` (protobuf) + `ingest-api` (Spring Boot) + `notification-router` (Spring Boot) + **`delivery-worker`** (Spring Boot).
- **Apache Pulsar** via Docker Compose for local dev; topics `persistent://public/default/activity-events`, `notification-dispatch`, **`notification-dispatch-retry`**, **`notification-dispatch-dlq`**.
- **PostgreSQL** for the recipient registry (preferences + devices), schema managed by **Flyway**.
- **Redis** for dedup (`SET NX` + TTL) and feature flag stubs.
- **Prometheus** scrapes `/actuator/prometheus` for both services.
- **Kubernetes** namespace manifest: `deploy/k8s/namespace.yaml`.
- **Buf** lint for protos; policy notes in [`docs/schema-evolution.md`](../docs/schema-evolution.md).
- **CI**: `.github/workflows/notification-ingest.yml`.

## Prerequisites

- **JDK 25** (toolchain default; see `gradle.properties` / `jdk.language.version`). For local override, set `jdk.language.version` in `gradle.properties` or pass `-Pjdk.language.version=22` if you must compile on an older JDK.
- **Gradle**: use the repo **wrapper** (`./gradlew` from repository root). **Gradle 9.1+** is required to *run* Gradle on JDK 25.
- Optional: **Buf CLI** for lint (`brew install bufbuild/buf/buf`).

## Local run (no Docker)

From the **repository root**, start Pulsar separately, then:

```bash
export INGEST_API_KEYS=dev-local-key
export PULSAR_SERVICE_URL=pulsar://localhost:6650
./gradlew :ingest-api:bootRun
```

OpenAPI UI: `http://localhost:8080/swagger-ui/index.html`

## Docker Compose (Pulsar + ingest + Prometheus)

From `notification-ingest/`:

```bash
docker compose up --build
```

Wait until Pulsar listens on `6650` (first start can take **1–2 minutes**), then:

```bash
chmod +x scripts/smoke.sh
./scripts/smoke.sh
```

Prometheus: `http://localhost:9090` (targets `ingest-api:8080` on the compose network).

## API

- `POST /v1/activities` — JSON body; requires header **`X-Api-Key`** (comma-separated keys in `INGEST_API_KEYS`).
- Optional **`Idempotency-Key`** header (or `idempotencyKey` in body).
- Payload maps to protobuf [`activity.proto`](notification-contracts/proto/com/pratyabhi/notification/events/v1/activity.proto); messages are published as **protobuf bytes** with Pulsar **message key** = `recipientId`.

## notification-router (Phase 2 + 3)

The `notification-router` Spring Boot service consumes `activity-events` with a Pulsar **`key_shared`** subscription (per-recipient ordering), enriches each event against the registry, suppresses duplicates via Redis, and produces a per-channel [`NotificationDispatch`](notification-contracts/proto/com/pratyabhi/notification/events/v1/notification_dispatch.proto) message keyed by `recipientId` to the dispatch topic.

Pluggable backends — switch with properties:

| Property | Default (prod) | Test value |
|----------|----------------|------------|
| `router.registry-mode` | `jdbc` (PostgreSQL) | `in-memory` |
| `router.dedup-mode`    | `redis` (`SET NX` + TTL) | `in-memory` |
| `router.flags-mode`    | `redis` | `in-memory` |
| `router.pulsar-enabled`| `true`  | `false` (uses in-memory dispatch publisher) |

Migrations live in `notification-router/src/main/resources/db/migration/`.

Metrics (Micrometer / Prometheus):
`router.event.consumed`, `router.event.dispatched`, `router.event.duplicate_suppressed`, `router.event.recipient_unknown`, `router.event.no_enabled_channels`, `router.event.flag_disabled`, `router.dispatch.published`.

## delivery-worker (Phase 4 + 5)

The **`delivery-worker`** service consumes **`notification-dispatch`** with a Pulsar **`key_shared`** subscription, sends through **FCM HTTP v1**, **APNs** (sandbox URL prefix by default), or an **email webhook** using **`java.net.http.HttpClient`** on a **virtual-thread** executor, applies **inline HTTP retries** with exponential backoff, then uses a dedicated **`notification-dispatch-retry`** topic and **`notification-dispatch-dlq`** for policy caps.

| Property / env | Purpose |
|----------------|---------|
| `delivery.pulsar-enabled` / `DELIVERY_PULSAR_ENABLED` | Set `false` for tests without a broker |
| `delivery.max-inline-retries` | HTTP attempts before escalating to retry topic |
| `delivery.max-topic-attempts` | Topic-level attempts before DLQ |
| `delivery.delivered-guard-mode` / `DELIVERY_DELIVERED_GUARD_MODE` | `none` or **`memory`** (suppress duplicate sends after success; useful with DLQ replay) |
| `delivery.dlq-archive-mode` / `DELIVERY_DLQ_ARCHIVE_MODE` | `none` or **`log`** (optional hook before S3/Blob archiver) |
| `FCM_SEND_URL`, `FCM_BEARER_TOKEN` | FCM HTTP v1 endpoint + OAuth bearer |
| `APNS_DEVICE_URL_PREFIX`, `APNS_TOPIC`, `APNS_AUTH_TOKEN` | APNs device POST + headers |
| `EMAIL_WEBHOOK_URL` | JSON POST `{to,subject,textBody}` |

**DLQ replay:** from repo root, `notification-ingest/scripts/replay-dlq.sh [limit]` or `./gradlew :delivery-worker:replayDlq -PdlqLimit=100`. Uses `PULSAR_SERVICE_URL`, `PULSAR_DISPATCH_DLQ_TOPIC`, `PULSAR_DISPATCH_TOPIC`, and subscription `PULSAR_DLQ_REPLAY_SUBSCRIPTION` (default `replay-dlq-cli`).

Docker Compose adds **`delivery-mocks`** (WireMock) plus **`delivery-worker`** wired to those URLs for a full **ingest → router → delivery** path without real providers.

## Tests

From the **repository root**:

```bash
./gradlew build
```

- `ingest-api` tests use `ingest.pulsar-enabled=false` (no broker required).
- `notification-router` tests use `router.pulsar-enabled=false`, `router.registry-mode=in-memory`, `router.dedup-mode=in-memory`, `router.flags-mode=in-memory` (no Pulsar/PostgreSQL/Redis required) and assert the Phase 2 dedup contract (`duplicateIdempotencyKeyYieldsSingleDispatchSet`) and the Phase 3 ordering contract (`preservesPerRecipientKeyOrdering`).
- `delivery-worker` tests use `delivery.pulsar-enabled=false` and WireMock for HTTP channel sends.
