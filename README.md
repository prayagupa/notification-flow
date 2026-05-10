# notification-flow

[![notification-ingest CI](https://github.com/prayagupa/nepleaks-clj/actions/workflows/notification-ingest.yml/badge.svg)](https://github.com/prayagupa/nepleaks-clj/actions/workflows/notification-ingest.yml)

High-throughput, low-latency **notification platform** that turns product activity events (message sent, post liked, comment created, …) into push/email notifications with **per-recipient ordering**, **idempotent de-duplication**, **retry with backoff**, and **dead-letter** handling.

- **Backbone:** Apache Pulsar (`key_shared` per `recipient_id`)
- **Runtime:** Java 25, Spring Boot, Gradle Kotlin DSL monorepo
- **Contracts:** Protobuf (`notification-contracts`) — `ActivityEvent`, `NotificationDispatch`
- **Modules:** `ingest-api` (Phase 1), `notification-router` (Phase 2 + 3: registry + dedup + enrichment & routing), **`delivery-worker` (Phase 4 + 5: channel delivery + retry/DLQ/replay)**
- **State:** PostgreSQL (recipient prefs/devices, Flyway-managed) + Redis (`SET NX` + TTL dedup, feature flag stubs)
- **Architecture & roadmap:** [`docs/sds.md`](docs/sds.md), [`docs/execution-plan.md`](docs/execution-plan.md)
- **Service docs:** [`notification-ingest/README.md`](notification-ingest/README.md)

## System design

```mermaid
flowchart LR
  subgraph Product["Product services"]
    MSG["Messaging"]
    SOC["Social / feed"]
  end

  subgraph Ingest["Ingest"]
    API["Activity Ingest API<br/>(Spring Boot, Java 25)"]
    SCH["Schema registry<br/>(protobuf / Buf)"]
  end

  subgraph Bus["Event backbone"]
    PULSAR[("Apache Pulsar<br/>key_shared by recipient_id")]
  end

  subgraph Processing["Processing"]
    ENV["Enricher<br/>(Flink / Pulsar Functions)"]
    ORC["Router / policy engine"]
    WRK["Delivery worker pool<br/>(virtual threads)"]
  end

  subgraph State["State"]
    REDIS[("Redis<br/>dedup + rate-limit")]
    DB[("PostgreSQL / MongoDB<br/>prefs + devices")]
  end

  subgraph Delivery["Delivery"]
    PUSH["Push dispatcher"]
    MAIL["Email dispatcher"]
  end

  subgraph Reliability["Reliability"]
    RET[("Retry / delay topic")]
    DLQ[("Dead-letter topic + archive")]
  end

  subgraph Channels["Channel providers"]
    FCM["FCM"]
    APNs["APNs"]
    EMAIL["Email provider"]
  end

  U["End users"]

  MSG --> API
  SOC --> API
  API --> SCH
  API --> PULSAR
  PULSAR --> ENV
  ENV --> DB
  ENV --> ORC
  ORC --> REDIS
  ORC --> WRK
  WRK --> PUSH
  WRK --> MAIL
  WRK --> RET
  RET --> WRK
  WRK --> DLQ
  PUSH --> FCM
  PUSH --> APNs
  MAIL --> EMAIL
  FCM --> U
  APNs --> U
  EMAIL --> U
```
