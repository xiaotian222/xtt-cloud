#!/bin/bash

# OA系统启动脚本
# 用于同时启动前端和后端服务

echo "🚀 启动 OA 系统..."

# 检查是否安装了必要的工具
check_requirements() {
    echo "📋 检查环境要求..."
    
    if ! command -v node &> /dev/null; then
        echo "❌ Node.js 未安装，请先安装 Node.js"
        exit 1
    fi
    
    if ! command -v npm &> /dev/null; then
        echo "❌ npm 未安装，请先安装 npm"
        exit 1
    fi
    
    if ! command -v mvn &> /dev/null; then
        echo "❌ Maven 未安装，请先安装 Maven"
        exit 1
    fi
    
    if ! command -v java &> /dev/null; then
        echo "❌ Java 未安装，请先安装 Java"
        exit 1
    fi
    
    echo "✅ 环境检查通过"
}

# 启动后端服务
start_backend() {
    echo "🔧 启动后端服务..."
    cd auth
    mvn spring-boot:run &
    AUTH_PID=$!
    echo "✅ Auth 服务已启动 (PID: $AUTH_PID)"
    cd ..
}

# 启动前端服务
start_frontend() {
    echo "🎨 启动前端服务..."
    cd front
    npm run dev &
    FRONTEND_PID=$!
    echo "✅ 前端服务已启动 (PID: $FRONTEND_PID)"
    cd ..
}

# 等待服务启动
wait_for_services() {
    echo "⏳ 等待服务启动..."
    sleep 10
    
    # 检查 Auth 服务
    if curl -s http://localhost:8020/test/health > /dev/null; then
        echo "✅ Auth 服务运行正常"
    else
        echo "❌ Auth 服务启动失败"
    fi
    
    # 检查前端服务
    if curl -s http://localhost:3000 > /dev/null; then
        echo "✅ 前端服务运行正常"
    else
        echo "❌ 前端服务启动失败"
    fi
}

# 显示服务信息
show_info() {
    echo ""
    echo "🎉 OA 系统启动完成！"
    echo ""
    echo "📱 前端地址: http://localhost:3000"
    echo "🔧 Auth 服务: http://localhost:8020"
    echo ""
    echo "👤 测试账号:"
    echo "   管理员: admin / password"
    echo "   用户: user / password"
    echo "   经理: manager / password"
    echo ""
    echo "🛑 按 Ctrl+C 停止所有服务"
    echo ""
}

# 清理函数
cleanup() {
    echo ""
    echo "🛑 正在停止服务..."
    
    if [ ! -z "$AUTH_PID" ]; then
        kill $AUTH_PID 2>/dev/null
        echo "✅ Auth 服务已停止"
    fi
    
    if [ ! -z "$FRONTEND_PID" ]; then
        kill $FRONTEND_PID 2>/dev/null
        echo "✅ 前端服务已停止"
    fi
    
    echo "👋 再见！"
    exit 0
}

# 设置信号处理
trap cleanup SIGINT SIGTERM

# 主函数
main() {
    check_requirements
    start_backend
    start_frontend
    wait_for_services
    show_info
    
    # 保持脚本运行
    wait
}

# 运行主函数
main
