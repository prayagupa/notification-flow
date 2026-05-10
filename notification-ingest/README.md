# Notification ingest (Phase 0–1)

Implements **Phase 0 (foundations)** and **Phase 1 (contracts & ingest)** from [`docs/execution-plan.md`](../docs/execution-plan.md):

- **Maven multi-module** Java project (`notification-contracts` protobuf + `ingest-api` Spring Boot).
- **Apache Pulsar** via Docker Compose for local dev; topic default `persistent://public/default/activity-events`.
- **Prometheus** scrapes `/actuator/prometheus`.
- **Kubernetes** namespace manifest: `deploy/k8s/namespace.yaml`.
- **Buf** lint for protos; policy notes in [`docs/schema-evolution.md`](../docs/schema-evolution.md).
- **CI**: `.github/workflows/notification-ingest.yml`.

## Prerequisites

- **JDK 22+** (SDS targets **JDK 25**; use `-Pjdk25` on the parent POM when your toolchain supports it).
- **Maven 3.6.3+** (parent POM aligns with Spring Boot 3.4).
- Optional: **Buf CLI** for lint (`brew install bufbuild/buf/buf`).

## Local run (no Docker)

Start Pulsar separately, then:

```bash
export INGEST_API_KEYS=dev-local-key
export PULSAR_SERVICE_URL=pulsar://localhost:6650
cd notification-ingest
mvn -pl ingest-api spring-boot:run
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

```bash
cd notification-ingest
mvn verify
```

Integration tests use **`ingest.pulsar-enabled=false`** so no broker is required.
