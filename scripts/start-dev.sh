#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LOG_DIR="$ROOT_DIR/logs/dev"
PID_DIR="$ROOT_DIR/.dev-pids"

mkdir -p "$LOG_DIR" "$PID_DIR"
cd "$ROOT_DIR"

is_running() {
  local pid_file="$1"
  [[ -f "$pid_file" ]] && kill -0 "$(cat "$pid_file")" >/dev/null 2>&1
}

start_process() {
  local name="$1"
  local workdir="$2"
  shift 2
  local pid_file="$PID_DIR/$name.pid"
  local log_file="$LOG_DIR/$name.log"

  if is_running "$pid_file"; then
    echo "skip $name, already running pid=$(cat "$pid_file")"
    return
  fi

  echo "start $name -> $log_file"
  (
    cd "$workdir"
    "$@"
  ) >"$log_file" 2>&1 &
  echo $! >"$pid_file"
}

# 清理超过 10MB 的旧日志（防止再次撑爆磁盘）
find "$LOG_DIR" -name "*.log" -size +10M -delete 2>/dev/null || true

echo "starting docker services..."
docker compose up -d

echo "installing common module..."
./mvnw -q -pl edu-backend/edu-common -am install -DskipTests

start_process "edu-user-service" "$ROOT_DIR" env NACOS_ENABLED=true ./mvnw -pl edu-backend/edu-user-service spring-boot:run
start_process "edu-course-service" "$ROOT_DIR" env NACOS_ENABLED=true ./mvnw -pl edu-backend/edu-course-service spring-boot:run
start_process "edu-enrollment-service" "$ROOT_DIR" env NACOS_ENABLED=true ./mvnw -pl edu-backend/edu-enrollment-service spring-boot:run
start_process "edu-interaction-service" "$ROOT_DIR" env NACOS_ENABLED=true ./mvnw -pl edu-backend/edu-interaction-service spring-boot:run
start_process "edu-assessment-service" "$ROOT_DIR" env NACOS_ENABLED=true ./mvnw -pl edu-backend/edu-assessment-service spring-boot:run
start_process "edu-gateway" "$ROOT_DIR" env NACOS_ENABLED=true EDU_USER_SERVICE_URI=lb://edu-user-service ./mvnw -pl edu-backend/edu-gateway spring-boot:run
start_process "edu-frontend" "$ROOT_DIR/edu-frontend" npm run dev -- --host 0.0.0.0 --port 5175 --strictPort

echo
echo "started. Useful URLs:"
echo "  frontend: http://localhost:5175/"
echo "  gateway:  http://localhost:8080/api"
echo "  nacos:    http://localhost:18848/nacos"
echo
echo "logs:"
echo "  tail -f logs/dev/edu-gateway.log"
echo "  tail -f logs/dev/edu-frontend.log"
echo
echo "stop:"
echo "  ./scripts/stop-dev.sh"
