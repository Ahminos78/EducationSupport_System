#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PID_DIR="$ROOT_DIR/.dev-pids"

if [[ ! -d "$PID_DIR" ]]; then
  echo "no dev pid directory found"
  exit 0
fi

for pid_file in "$PID_DIR"/*.pid; do
  [[ -e "$pid_file" ]] || continue
  name="$(basename "$pid_file" .pid)"
  pid="$(cat "$pid_file")"

  if kill -0 "$pid" >/dev/null 2>&1; then
    echo "stop $name pid=$pid"
    kill "$pid" >/dev/null 2>&1 || true
  else
    echo "skip $name, pid is not running"
  fi

  rm -f "$pid_file"
done

echo "dev processes stopped. Docker services are still running."
echo "run 'docker compose down' if you also want to stop MySQL and Nacos."
