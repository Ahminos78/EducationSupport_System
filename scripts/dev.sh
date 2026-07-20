#!/usr/bin/env bash
# ============================================================
# scripts/dev.sh — 开发环境管理器
# 取代 start-dev.sh / stop-dev.sh，提供更可靠的启动/停止/重启流程
#
# 使用方法:
#   ./scripts/dev.sh start             启动全部（基础设施→构建→运行）
#   ./scripts/dev.sh stop              停止全部服务（保留基础设施）
#   ./scripts/dev.sh restart [服务名]   构建并重启全部/单个服务
#   ./scripts/dev.sh build  [服务名]   构建全部/单个服务 (package -DskipTests)
#   ./scripts/dev.sh log    [服务名]   查看单个/全部服务的实时日志
#   ./scripts/dev.sh status            查看各服务运行状态
#   ./scripts/dev.sh ps                查看各服务进程和端口详情
# ============================================================
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

# ============================================================
# 配置
# ============================================================
LOG_DIR="$ROOT_DIR/logs/dev"
PID_DIR="$ROOT_DIR/.dev-pids"

# 服务定义: "简短名 端口 Maven模块路径"
SERVICES=(
  "user:8010:edu-backend/edu-user-service"
  "course:8020:edu-backend/edu-course-service"
  "enrollment:8030:edu-backend/edu-enrollment-service"
  "assessment:8050:edu-backend/edu-assessment-service"
  "ai:8060:edu-backend/edu-ai-service"
  "gateway:8080:edu-backend/edu-gateway"
)

# 完整服务名 → 简短名映射（支持 dev.sh restart edu-user-service）
SERVICE_ALIASES=(
  "edu-user-service:user"
  "edu-course-service:course"
  "edu-enrollment-service:enrollment"
  "edu-assessment-service:assessment"
  "edu-ai-service:ai"
  "edu-gateway:gateway"
)

FRONTEND_DIR="edu-frontend"
FRONTEND_PORT=5175

# 颜色
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

ok()   { echo -e "  ${GREEN}✓${NC} $1"; }
fail() { echo -e "  ${RED}✗${NC} $1"; }
warn() { echo -e "  ${YELLOW}⚠${NC} $1"; }
info() { echo -e "  ${CYAN}→${NC} $1"; }

# ============================================================
# 工具函数
# ============================================================

port_listening() { lsof -nP -iTCP:"$1" -sTCP:LISTEN >/dev/null 2>&1; }

get_pid() { cat "$PID_DIR/$1.pid" 2>/dev/null || echo ""; }

# 将完整服务名（如 edu-user-service）或简短名解析为简短名
resolve_name() {
  local input="$1"
  # 先检查是否是简短名（直接在 SERVICES 列表中匹配第一个字段）
  for svc in "${SERVICES[@]}"; do
    short="${svc%%:*}"
    if [[ "$short" == "$input" ]]; then
      echo "$short"
      return 0
    fi
  done
  # 检查别名映射
  for alias in "${SERVICE_ALIASES[@]}"; do
    full="${alias%%:*}"
    short="${alias##*:}"
    if [[ "$full" == "$input" ]]; then
      echo "$short"
      return 0
    fi
  done
  return 1
}

# 从简短名获取服务信息 (port module)
svc_info() {
  local short="$1"
  for svc in "${SERVICES[@]}"; do
    local name="${svc%%:*}"
    if [[ "$name" == "$short" ]]; then
      echo "${svc#*:}"
      return 0
    fi
  done
  return 1
}

svc_port() {
  local rest; rest=$(svc_info "$1") || return 1
  echo "${rest%%:*}"
}

svc_module() {
  local rest; rest=$(svc_info "$1") || return 1
  echo "${rest#*:}"
}

svc_full_name() {
  local short="$1"
  for alias in "${SERVICE_ALIASES[@]}"; do
    full="${alias%%:*}"
    s="${alias##*:}"
    if [[ "$s" == "$short" ]]; then
      echo "$full"
      return 0
    fi
  done
  # 没找到别名就用简短名自身
  echo "$short"
}

is_running() {
  local name; name=$(resolve_name "$1") || return 1
  local pid; pid=$(get_pid "$name")
  [[ -n "$pid" ]] && kill -0 "$pid" 2>/dev/null && return 0
  local port; port=$(svc_port "$name") || return 1
  port_listening "$port" && return 0
  return 1
}

# 更新所有失效 PID
clean_stale_pids() {
  if [[ ! -d "$PID_DIR" ]]; then
    mkdir -p "$PID_DIR"
    return
  fi
  for pid_file in "$PID_DIR"/*.pid; do
    [[ -e "$pid_file" ]] || continue
    local pid
    pid=$(cat "$pid_file" 2>/dev/null || echo "")
    if [[ -z "$pid" ]] || ! kill -0 "$pid" 2>/dev/null; then
      local name; name=$(basename "$pid_file" .pid)
      # 检查端口是否还在监听（可能 pid 变了但进程还在）
      local still_alive=false
      for svc in "${SERVICES[@]}"; do
        local short="${svc%%:*}"
        if [[ "$short" == "$name" ]]; then
          local port; port=$(svc_port "$name")
          if port_listening "$port"; then
            still_alive=true
            break
          fi
        fi
      done
      if ! $still_alive; then
        rm -f "$pid_file"
      fi
    fi
  done
}

# ============================================================
# 构建
# ============================================================

build_service() {
  local name="$1"
  local module; module=$(svc_module "$name") || { fail "unknown service: $name"; return 1; }
  local full; full=$(svc_full_name "$name")

  printf "  building ${full}..."
  if ./mvnw package -pl "$module" -am -DskipTests -q -T 4C 2>/dev/null; then
    echo -e " ${GREEN}done${NC}"
    return 0
  else
    echo -e " ${RED}FAILED${NC}"
    echo ""
    warn "Build failed for $full. Run again with verbose output:"
    info "./mvnw package -pl $module -am -DskipTests"
    return 1
  fi
}

build_all() {
  echo ""
  echo "=== Building all services (parallel) ==="
  local modules=()
  for svc in "${SERVICES[@]}"; do
    module; module=$(svc_module "${svc%%:*}")
    modules+=("$module")
  done
  # 用逗号拼接模块列表
  local module_list
  module_list=$(IFS=,; echo "${modules[*]}")
  if ./mvnw package -pl "$module_list" -am -DskipTests -q -T 4C 2>/dev/null; then
    ok "All services built successfully"
  else
    fail "Build failed. Try building individual services:"
    info "./scripts/dev.sh build <service>"
    return 1
  fi
}

# ============================================================
# 启动 / 停止 / 重启
# ============================================================

start_infra() {
  echo ""
  echo "=== Infrastructure (MySQL + Nacos) ==="
  if docker compose ps 2>/dev/null | grep -q "mysql.*running"; then
    ok "MySQL already running"
  else
    info "Starting MySQL..."
    if docker compose up -d mysql 2>/dev/null; then
      ok "MySQL started"
    else
      warn "Docker compose failed (is Docker running?)"
    fi
  fi

  if docker compose ps 2>/dev/null | grep -q "nacos.*running"; then
    ok "Nacos already running"
  else
    info "Starting Nacos..."
    if docker compose up -d nacos 2>/dev/null; then
      ok "Nacos started"
    else
      warn "Docker compose failed (is Docker running?)"
    fi
  fi

  # 等待 MySQL 就绪
  info "Waiting for MySQL to be ready..."
  local max=30
  local i=0
  while ! port_listening 3306 && [[ $i -lt $max ]]; do
    sleep 1
    i=$((i + 1))
  done
  if port_listening 3306; then
    ok "MySQL ready (port 3306)"
  else
    warn "MySQL not responding on port 3306 after ${max}s"
  fi
}

start_service() {
  local name="$1"
  local pid_file="$PID_DIR/$name.pid"
  local log_file="$LOG_DIR/$name.log"
  local port; port=$(svc_port "$name") || { fail "unknown service: $name"; return 1; }
  local module; module=$(svc_module "$name") || return 1
  local full; full=$(svc_full_name "$name")

  mkdir -p "$LOG_DIR" "$PID_DIR"

  # 检查是否已经在运行
  if is_running "$name"; then
    ok "$full already running (port $port)"
    return 0
  fi

  # 检查 JAR 是否存在
  local jar_path
  # shellcheck disable=SC2012
  jar_path=$(ls "$ROOT_DIR/$module/target/"*.jar 2>/dev/null | head -1 || echo "")
  if [[ -z "$jar_path" ]]; then
    warn "No JAR found for $full, building first..."
    build_service "$name" || return 1
    # shellcheck disable=SC2012
    jar_path=$(ls "$ROOT_DIR/$module/target/"*.jar 2>/dev/null | head -1 || echo "")
    if [[ -z "$jar_path" ]]; then
      fail "Build produced no JAR for $full"
      return 1
    fi
  fi

  printf "  starting ${full} (port ${port})..."
  # nohup + disown ensures the process survives even after the script exits
  nohup java -jar "$jar_path" > "$log_file" 2>&1 &
  disown
  local new_pid=$!
  echo "$new_pid" > "$pid_file"
  echo -e " ${GREEN}pid=${new_pid}${NC}"
}

start_frontend() {
  local pid_file="$PID_DIR/edu-frontend.pid"
  local log_file="$LOG_DIR/edu-frontend.log"

  if is_running "edu-frontend" 2>/dev/null || port_listening "$FRONTEND_PORT"; then
    ok "Frontend already running (port $FRONTEND_PORT)"
    return 0
  fi

  mkdir -p "$LOG_DIR" "$PID_DIR"
  printf "  starting frontend (port ${FRONTEND_PORT})..."
  nohup npm run dev --prefix "$ROOT_DIR/$FRONTEND_DIR" -- --host 0.0.0.0 --port "$FRONTEND_PORT" --strictPort > "$log_file" 2>&1 &
  local new_pid=$!
  echo "$new_pid" > "$pid_file"
  echo -e " ${GREEN}pid=${new_pid}${NC}"
}

stop_service() {
  local name="$1"
  local pid_file="$PID_DIR/$name.pid"
  local full; full=$(svc_full_name "$name")
  local port; port=$(svc_port "$name") 2>/dev/null || port="?"
  local stopped=false

  # 方法1：通过 PID 文件停止
  if [[ -f "$pid_file" ]]; then
    local pid
    pid=$(cat "$pid_file" 2>/dev/null || echo "")
    if [[ -n "$pid" ]] && kill -0 "$pid" 2>/dev/null; then
      printf "  stopping ${full} (pid ${pid})..."
      kill "$pid" 2>/dev/null || true
      i=0
      while kill -0 "$pid" 2>/dev/null && [[ $i -lt 8 ]]; do
        sleep 1
        i=$((i + 1))
      done
      if kill -0 "$pid" 2>/dev/null; then
        kill -9 "$pid" 2>/dev/null || true
        echo -e " ${YELLOW}force killed${NC}"
      else
        echo -e " ${GREEN}stopped${NC}"
      fi
      stopped=true
    fi
    rm -f "$pid_file"
  fi

  # 方法2：通过端口查找并停止（兜底）
  if ! $stopped && [[ "$port" != "?" ]] && port_listening "$port"; then
    pids=$(lsof -tiTCP:"$port" -sTCP:LISTEN 2>/dev/null || true)
    if [[ -n "$pids" ]]; then
      for pid in $pids; do
        printf "  stopping ${full} on port ${port} (pid ${pid})..."
        kill "$pid" 2>/dev/null || true
        sleep 1
        kill -0 "$pid" 2>/dev/null && kill -9 "$pid" 2>/dev/null || true
        echo -e " ${GREEN}stopped${NC}"
      done
    fi
    return 0
  fi

  if ! $stopped; then
    echo "  ${full} not running"
  fi
}

stop_frontend() {
  local pid_file="$PID_DIR/edu-frontend.pid"
  if [[ -f "$pid_file" ]]; then
    local pid
    pid=$(cat "$pid_file" 2>/dev/null || echo "")
    if [[ -n "$pid" ]] && kill -0 "$pid" 2>/dev/null; then
      printf "  stopping frontend (pid ${pid})..."
      kill "$pid" 2>/dev/null || true
      sleep 2
      kill -0 "$pid" 2>/dev/null && kill -9 "$pid" 2>/dev/null || true
      echo -e " ${GREEN}stopped${NC}"
    fi
    rm -f "$pid_file"
  fi
  # 端口兜底
  if port_listening "$FRONTEND_PORT"; then
    pids=$(lsof -tiTCP:"$FRONTEND_PORT" -sTCP:LISTEN 2>/dev/null || true)
    if [[ -n "$pids" ]]; then
      kill $pids 2>/dev/null || true
      ok "Frontend port $FRONTEND_PORT freed"
    fi
  fi
}

# ============================================================
# 日志查看
# ============================================================

tail_log() {
  local name="$1"
  local log_file="$LOG_DIR/$name.log"
  local full; full=$(svc_full_name "$name")

  if [[ ! -f "$log_file" ]]; then
    warn "No log file for $full (expected: $log_file)"
    return 1
  fi
  echo -e "${CYAN}=== $full log (Ctrl+C to stop) ===${NC}"
  tail -f "$log_file"
}

tail_all_logs() {
  echo -e "${CYAN}=== All service logs (Ctrl+C to stop) ===${NC}"
  local files=()
  for svc in "${SERVICES[@]}"; do
    local name="${svc%%:*}"
    lf="$LOG_DIR/$name.log"
    [[ -f "$lf" ]] && files+=("$lf")
  done
  if [[ ${#files[@]} -eq 0 ]]; then
    warn "No log files found"
    return
  fi
  tail -f "${files[@]}"
}

# ============================================================
# 状态
# ============================================================

show_status() {
  clean_stale_pids

  echo ""
  echo "============================================"
  echo "  Service Status"
  echo "============================================"
  echo ""

  local all_ok=true
  for svc in "${SERVICES[@]}"; do
    local name="${svc%%:*}"
    local full; full=$(svc_full_name "$name")
    local port; port=$(svc_port "$name")
    local pid; pid=$(get_pid "$name")

    if is_running "$name"; then
      if [[ -n "$pid" ]] && kill -0 "$pid" 2>/dev/null; then
        echo -e "  ${GREEN}RUNNING${NC}  ${full}  port=${port}  pid=${pid}"
      else
        local actual_pid
        actual_pid=$(lsof -tiTCP:"$port" -sTCP:LISTEN 2>/dev/null || echo "?")
        echo -e "  ${GREEN}RUNNING${NC}  ${full}  port=${port}  pid=${actual_pid} (no pid file)"
      fi
    else
      echo -e "  ${RED}STOPPED${NC}  ${full}  port=${port}"
      all_ok=false
    fi
  done

 # Frontend
 if port_listening "$FRONTEND_PORT"; then
    local fpid
    fpid=$(lsof -tiTCP:"$FRONTEND_PORT" -sTCP:LISTEN 2>/dev/null || echo "?")
    echo -e "  ${GREEN}RUNNING${NC}  edu-frontend  port=${FRONTEND_PORT}  pid=${fpid}"
  else
    echo -e "  ${RED}STOPPED${NC}  edu-frontend  port=${FRONTEND_PORT}"
    all_ok=false
  fi

  # Infrastructure
  for svc_name in mysql nacos; do
    if docker compose ps 2>/dev/null | grep -q "${svc_name}.*running"; then
      echo -e "  ${GREEN}RUNNING${NC}  infra-${svc_name}  (docker)"
    else
      echo -e "  ${YELLOW}STOPPED${NC}  infra-${svc_name}  (docker)"
    fi
  done

  echo ""
  if $all_ok; then
    ok "All services running"
  else
    warn "Some services are not running"
  fi
  echo ""
}

show_ps() {
  echo ""
  echo "============================================"
  echo "  Process Details (ps aux | grep java)"
  echo "============================================"
  echo ""
  local java_processes
  java_processes=$(ps aux | grep -E "java.*-jar" 2>/dev/null | grep -v grep || true)
  if [[ -z "$java_processes" ]]; then
    warn "No java -jar processes found"
  else
    echo "$java_processes" | while IFS= read -r line; do
      local pid
      pid=$(echo "$line" | awk '{print $2}')
      local cmd
      cmd=$(echo "$line" | awk '{$1=$2=$3=$4=$5=$6=$7=$8=$9=$10=""; print $0}' | xargs)
      local port_info=""
      port_info=$(lsof -nP -iTCP -sTCP:LISTEN -p "$pid" 2>/dev/null | awk 'NR>1{print $9}' | tr '\n' ' ' || true)
      if [[ -n "$port_info" ]]; then
        echo "  PID $pid  ports: $port_info"
      else
        echo "  PID $pid  (starting...)"
      fi
    done
  fi
  echo ""
}

# ============================================================
# 主命令
# ============================================================

usage() {
  echo ""
  echo "Usage: ./scripts/dev.sh <command> [service]"
  echo ""
  echo "Commands:"
  echo "  start                     启动全部（infra + build + services + frontend）"
  echo "  stop                      停止全部服务（保留 MySQL/Nacos）"
  echo "  restart   [service]       构建并重启指定服务（不指定则重启全部）"
  echo "  build     [service]       构建指定服务（不指定则构建全部）"
  echo "  log       [service]       查看指定服务日志（不指定则查看全部）"
  echo "  status                    查看各服务运行状态"
  echo "  ps                        查看 java 进程详情"
  echo "  start-service <service>   启动单个服务（不构建）"
  echo "  stop-service  <service>   停止单个服务"
  echo "  build-all                 构建全部服务"
  echo "  clean                     clean 所有模块"
  echo ""
  echo "Services:"
  for svc in "${SERVICES[@]}"; do
    local name="${svc%%:*}"
    local full; full=$(svc_full_name "$name")
    local port; port=$(svc_port "$name")
    echo "  ${full}  (${name})  → port ${port}"
  done
  echo "  edu-frontend  → port ${FRONTEND_PORT}"
  echo ""
  echo "Examples:"
  echo "  ./scripts/dev.sh start"
  echo "  ./scripts/dev.sh status"
  echo "  ./scripts/dev.sh restart user        # Rebuild + restart edu-user-service"
  echo "  ./scripts/dev.sh log course           # Tail edu-course-service logs"
  echo "  ./scripts/dev.sh build gateway        # Build edu-gateway only"
  echo "  ./scripts/dev.sh restart edu-user-service  # Full name also works"
  echo ""
  exit 0
}

main() {

CMD="${1:-help}"
shift || true
ARG="${1:-}"

case "$CMD" in
  start)
    clean_stale_pids
    start_infra
    echo ""
    echo "=== Building services ==="
    build_all || warn "Some services failed to build"

    echo ""
    echo "=== Starting backend services ==="
    for svc in "${SERVICES[@]}"; do
      name="${svc%%:*}"
      start_service "$name"
    done

    echo ""
    echo "=== Starting frontend ==="
    start_frontend

    echo ""
    echo "============================================"
    echo "  Dev Environment Summary"
    echo "============================================"
    echo ""
    echo "  Frontend:  http://localhost:${FRONTEND_PORT}/"
    echo "  Gateway:   http://localhost:8080/api"
    echo "  Nacos:     http://localhost:18848/nacos"
    echo ""
    echo "  View logs:  ./scripts/dev.sh log [service]"
    echo "  Status:     ./scripts/dev.sh status"
    echo "  Restart:    ./scripts/dev.sh restart <service>"
    echo "  Stop:       ./scripts/dev.sh stop"
    echo ""
    ;;

  stop)
    clean_stale_pids
    echo ""
    echo "=== Stopping all services ==="
    stop_frontend
    # 逆序停止
    for ((i = ${#SERVICES[@]} - 1; i >= 0; i--)); do
      name="${SERVICES[$i]%%:*}"
      stop_service "$name"
    done
    echo ""
    ok "All services stopped. Docker services (MySQL+Nacos) are still running."
    info "To stop Docker services: docker compose down"
    echo ""
    ;;

  restart)
    clean_stale_pids
    if [[ -z "$ARG" ]]; then
      # 重启全部
      echo ""
      echo "=== Rebuilding all services ==="
      build_all || true
      echo ""
      echo "=== Restarting all services ==="
      stop_frontend
      for ((i = ${#SERVICES[@]} - 1; i >= 0; i--)); do
        name="${SERVICES[$i]%%:*}"
        stop_service "$name"
      done
      for svc in "${SERVICES[@]}"; do
        name="${svc%%:*}"
        start_service "$name"
      done
      start_frontend
    else
      # 重启单个服务
      local resolved
      resolved=$(resolve_name "$ARG") || { fail "Unknown service: $ARG"; usage; exit 1; }
      local full; full=$(svc_full_name "$resolved")
      echo ""
      echo "=== Restarting ${full} ==="
      stop_service "$resolved"
      echo ""
      build_service "$resolved" || exit 1
      start_service "$resolved"
      echo ""
      ok "$full restarted"
    fi
    ;;

  build)
    if [[ -z "$ARG" ]]; then
      build_all
    else
      local resolved
      resolved=$(resolve_name "$ARG") || { fail "Unknown service: $ARG"; usage; exit 1; }
      local full; full=$(svc_full_name "$resolved")
      echo ""
      echo "=== Building ${full} ==="
      build_service "$resolved"
    fi
    ;;

  build-all)
    build_all
    ;;

  log)
    if [[ -z "$ARG" ]]; then
      tail_all_logs
    else
      local resolved
      resolved=$(resolve_name "$ARG") || { fail "Unknown service: $ARG"; usage; exit 1; }
      tail_log "$resolved"
    fi
    ;;

  status)
    clean_stale_pids
    show_status
    ;;

  ps)
    show_ps
    ;;

  start-service)
    [[ -z "$ARG" ]] && { fail "Usage: ./scripts/dev.sh start-service <service>"; exit 1; }
    local resolved
    resolved=$(resolve_name "$ARG") || { fail "Unknown service: $ARG"; exit 1; }
    clean_stale_pids
    start_service "$resolved"
    ;;

  stop-service)
    [[ -z "$ARG" ]] && { fail "Usage: ./scripts/dev.sh stop-service <service>"; exit 1; }
    local resolved
    resolved=$(resolve_name "$ARG") || { fail "Unknown service: $ARG"; exit 1; }
    clean_stale_pids
    stop_service "$resolved"
    ;;

  clean)
    echo ""
    echo "=== Cleaning all modules ==="
    ./mvnw clean -q 2>/dev/null && ok "Clean complete" || warn "Clean had issues"
    rm -rf "$PID_DIR"/*.pid 2>/dev/null || true
    rm -f "$LOG_DIR"/*.log 2>/dev/null || true
    info "PID files and logs cleaned"
    echo ""
    ;;

  help|--help|-h)
    usage
    ;;

  *)
    fail "Unknown command: $CMD"
    usage
    ;;
esac

}

# Uncomment to run main function automatically:
main "$@"
