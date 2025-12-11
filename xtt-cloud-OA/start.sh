#!/bin/bash

# XTT Cloud OA 启动脚本

# 检查是否在Windows环境下
if [[ -n "$WINDIR" ]]; then
    echo "检测到Windows环境，请使用 start.bat 脚本启动"
    exit 1
fi

# 定义服务列表
SERVICES=("nacos" "gateway" "auth" "platform" "document" "front")

# 定义服务端口
declare -A SERVICE_PORTS
SERVICE_PORTS["gateway"]=30010
SERVICE_PORTS["auth"]=8020
SERVICE_PORTS["platform"]=8085
SERVICE_PORTS["document"]=8086
SERVICE_PORTS["front"]=3000

# 定义服务启动命令
declare -A SERVICE_COMMANDS
SERVICE_COMMANDS["gateway"]="cd gateway && mvn spring-boot:run"
SERVICE_COMMANDS["auth"]="cd auth && mvn spring-boot:run"
SERVICE_COMMANDS["platform"]="cd platform && mvn spring-boot:run"
SERVICE_COMMANDS["document"]="cd document && mvn spring-boot:run"
SERVICE_COMMANDS["front"]="cd front && npm run dev"

# 检查端口是否被占用
check_port() {
    local port=$1
    if command -v netstat >/dev/null 2>&1; then
        if netstat -tuln | grep :$port >/dev/null 2>&1; then
            return 0
        fi
    elif command -v ss >/dev/null 2>&1; then
        if ss -tuln | grep :$port >/dev/null 2>&1; then
            return 0
        fi
    fi
    return 1
}

# 启动单个服务
start_service() {
    local service=$1
    local port=${SERVICE_PORTS[$service]}
    
    echo "正在启动 $service 服务..."
    
    # 检查端口是否被占用
    if [[ -n "$port" ]] && check_port $port; then
        echo "警告: 端口 $port 已被占用，$service 服务可能已在运行"
        return 1
    fi
    
    # 启动服务
    if [[ -n "${SERVICE_COMMANDS[$service]}" ]]; then
        eval "${SERVICE_COMMANDS[$service]}" &
        echo "$service 服务启动命令已执行"
    else
        echo "未找到 $service 服务的启动命令"
        return 1
    fi
}

# 停止单个服务
stop_service() {
    local service=$1
    echo "正在停止 $service 服务..."
    pkill -f "$service"
}

# 显示帮助信息
show_help() {
    echo "用法: $0 [选项] [服务名]"
    echo "选项:"
    echo "  start [服务名]   启动指定服务或所有服务"
    echo "  stop [服务名]    停止指定服务或所有服务"
    echo "  restart [服务名] 重启指定服务或所有服务"
    echo "  status          查看服务状态"
    echo "  help            显示帮助信息"
    echo ""
    echo "服务列表: ${SERVICES[*]}"
}

# 查看服务状态
show_status() {
    echo "服务状态:"
    for service in "${SERVICES[@]}"; do
        local port=${SERVICE_PORTS[$service]}
        if [[ -n "$port" ]] && check_port $port; then
            echo "  $service: 运行中 (端口: $port)"
        else
            echo "  $service: 未运行"
        fi
    done
}

# 主逻辑
case "${1:-start}" in
    start)
        if [[ -n "$2" ]]; then
            # 启动指定服务
            start_service "$2"
        else
            # 启动所有服务
            echo "启动所有服务..."
            for service in "${SERVICES[@]}"; do
                start_service "$service"
                sleep 2
            done
            echo "所有服务启动命令已执行，请稍候查看服务状态"
        fi
        ;;
    stop)
        if [[ -n "$2" ]]; then
            # 停止指定服务
            stop_service "$2"
        else
            # 停止所有服务
            echo "停止所有服务..."
            for service in "${SERVICES[@]}"; do
                stop_service "$service"
            done
        fi
        ;;
    restart)
        $0 stop "$2"
        sleep 3
        $0 start "$2"
        ;;
    status)
        show_status
        ;;
    help)
        show_help
        ;;
    *)
        echo "未知选项: $1"
        show_help
        exit 1
        ;;
esac