# Notification ingest (Phase 0–1)

Implements **Phase 0 (foundations)** and **Phase 1 (contracts & ingest)** from [`docs/execution-plan.md`](../docs/execution-plan.md):

- **Gradle Kotlin DSL monorepo** (repo root): modules `notification-contracts` (protobuf) + `ingest-api` (Spring Boot).
- **Apache Pulsar** via Docker Compose for local dev; topic default `persistent://public/default/activity-events`.
- **Prometheus** scrapes `/actuator/prometheus`.
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
- Payload maps to protobuf [`activity.proto`](notification-contracts/proto/nepleaks/events/v1/activity.proto); messages are published as **protobuf bytes** with Pulsar **message key** = `recipientId`.

## Tests

From the **repository root**:

```bash
./gradlew build
```

Integration tests use **`ingest.pulsar-enabled=false`** so no broker is required.
