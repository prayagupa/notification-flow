#!/usr/bin/env bash
set -euo pipefail
BASE="${BASE_URL:-http://localhost:8080}"
KEY="${INGEST_API_KEYS:-dev-local-key}"

curl -sf "${BASE}/actuator/health" | head -c 200
echo

curl -sS -o /dev/null -w "%{http_code}" \
  -X POST "${BASE}/v1/activities" \
  -H "Content-Type: application/json" \
  -H "X-Api-Key: ${KEY}" \
  -H "Idempotency-Key: smoke-$(date +%s)" \
  -d '{
    "recipientId": "user-smoke-1",
    "activityType": "MESSAGE_SENT",
    "sourceService": "smoke-script",
    "occurredAtEpochMs": 1715340000000
  }'
echo
