#!/bin/sh
set -eu

SPRING_PORT="${SERVER_PORT:-8081}"
MAX_WAIT="${STARTUP_MAX_WAIT_SECONDS:-120}"
HEALTH_URL="http://127.0.0.1:${SPRING_PORT}/api/actuator/health"

java ${JAVA_OPTS:-} -jar /app/app.jar &
JAVA_PID=$!

i=0
while [ "$i" -lt "$MAX_WAIT" ]; do
  if wget -q -O /dev/null "$HEALTH_URL" 2>/dev/null; then
    exec nginx -g 'daemon off;'
  fi
  i=$((i + 1))
  sleep 1
done

echo "Spring Boot did not become healthy at ${HEALTH_URL} within ${MAX_WAIT}s" >&2
kill "$JAVA_PID" 2>/dev/null || true
wait "$JAVA_PID" 2>/dev/null || true
exit 1
