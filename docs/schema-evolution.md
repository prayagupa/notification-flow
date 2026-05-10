# Schema evolution (protobuf)

This project uses **[Buf](https://buf.build)** against `notification-ingest/notification-contracts/proto` (see `buf.yaml`).

## Rules

1. **Additive changes first:** Prefer adding new fields with new field numbers; never reuse field numbers.
2. **Breaking changes:** Run `buf breaking` against the previous Git revision before merging (CI should enforce this once `buf.lock` / base branch is wired).
3. **Enums:** Do not renumber enum values; add new values with new ordinals only.
4. **HTTP JSON mapping:** `ingest-api` maps DTO enums (`MESSAGE_SENT`, …) to protobuf `ActivityType`; when adding activity kinds, extend **both** `activity.proto` and `ActivityTypeDto` in lockstep.
5. **Consumers:** Downstream Flink/Pulsar consumers must tolerate unknown fields (protobuf default).

## Commands

```bash
cd notification-ingest/notification-contracts
buf lint
# buf breaking --against '.git#branch=main'   # enable when repo policy is set
```
