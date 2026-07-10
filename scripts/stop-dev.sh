#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PID_DIR="$ROOT_DIR/.dev-pids"

DEV_PORTS=(5173 5175 8010 8020 8030 8040 8050 8060 8080)

stop_pid() {
  local pid="$1"
  local label="$2"

  if [[ -z "$pid" ]]; then
    return
  fi

  if kill -0 "$pid" >/dev/null 2>&1; then
    echo "stop $label pid=$pid"
    kill "$pid" >/dev/null 2>&1 || true
  fi
}

force_stop_pid() {
  local pid="$1"
  local label="$2"

  if [[ -z "$pid" ]]; then
    return
  fi

  if kill -0 "$pid" >/dev/null 2>&1; then
    echo "force stop $label pid=$pid"
    kill -9 "$pid" >/dev/null 2>&1 || true
  fi
}

echo "stopping recorded dev processes..."
if [[ -d "$PID_DIR" ]]; then
  for pid_file in "$PID_DIR"/*.pid; do
    [[ -e "$pid_file" ]] || continue
    name="$(basename "$pid_file" .pid)"
    pid="$(cat "$pid_file")"
    stop_pid "$pid" "$name"
    rm -f "$pid_file"
  done
else
  echo "no dev pid directory found"
fi

sleep 2

echo "checking known dev ports..."
for port in "${DEV_PORTS[@]}"; do
  pids="$(lsof -tiTCP:"$port" -sTCP:LISTEN 2>/dev/null || true)"
  if [[ -z "$pids" ]]; then
    echo "port $port is free"
    continue
  fi

  for pid in $pids; do
    stop_pid "$pid" "port-$port"
  done
done

sleep 2

echo "verifying ports after graceful stop..."
remaining=()
for port in "${DEV_PORTS[@]}"; do
  pids="$(lsof -tiTCP:"$port" -sTCP:LISTEN 2>/dev/null || true)"
  if [[ -n "$pids" ]]; then
    for pid in $pids; do
      remaining+=("$port:$pid")
    done
  fi
done

if (( ${#remaining[@]} > 0 )); then
  echo "some dev ports are still occupied, forcing stop..."
  for item in "${remaining[@]}"; do
    port="${item%%:*}"
    pid="${item##*:}"
    force_stop_pid "$pid" "port-$port"
  done
fi

echo "final port status:"
for port in "${DEV_PORTS[@]}"; do
  pids="$(lsof -tiTCP:"$port" -sTCP:LISTEN 2>/dev/null || true)"
  if [[ -z "$pids" ]]; then
    echo "  $port free"
  else
    echo "  $port still occupied by pid(s): $pids"
  fi
done

echo "dev processes stopped. Docker services are still running."
echo "run 'docker compose down' if you also want to stop MySQL and Nacos."
