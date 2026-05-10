#!/usr/bin/env bash
# Replay protobuf payloads from the DLQ topic back onto the dispatch topic.
# Requires a running Pulsar broker and topics created (e.g. via docker compose).
# Usage: ./scripts/replay-dlq.sh [limit]
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
REPO="$(cd "$ROOT/.." && pwd)"
LIMIT="${1:-50}"
cd "$REPO"
chmod +x gradlew
./gradlew :delivery-worker:replayDlq -PdlqLimit="$LIMIT" --no-daemon -q
