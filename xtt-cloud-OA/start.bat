@echo off
chcp 65001 >nul
title OA系统启动脚本

echo 🚀 启动 OA 系统...

REM 检查是否安装了必要的工具
echo 📋 检查环境要求...

where node >nul 2>nul
if %errorlevel% neq 0 (
    echo ❌ Node.js 未安装，请先安装 Node.js
    pause
    exit /b 1
)

where npm >nul 2>nul
if %errorlevel% neq 0 (
    echo ❌ npm 未安装，请先安装 npm
    pause
    exit /b 1
)

where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo ❌ Maven 未安装，请先安装 Maven
    pause
    exit /b 1
)

where java >nul 2>nul
if %errorlevel% neq 0 (
    echo ❌ Java 未安装，请先安装 Java
    pause
    exit /b 1
)

echo ✅ 环境检查通过

REM 启动后端服务
echo 🔧 启动后端服务...
cd auth
start "Auth Service" cmd /k "mvn spring-boot:run"
cd ..

REM 等待后端服务启动
echo ⏳ 等待后端服务启动...
timeout /t 10 /nobreak >nul

REM 启动前端服务
echo 🎨 启动前端服务...
cd front
start "Frontend Service" cmd /k "npm run dev"
cd ..

REM 等待前端服务启动
echo ⏳ 等待前端服务启动...
timeout /t 5 /nobreak >nul

echo.
echo 🎉 OA 系统启动完成！
echo.
echo 📱 前端地址: http://localhost:3000
echo 🔧 Auth 服务: http://localhost:8020
echo.
echo 👤 测试账号:
echo    管理员: admin / password
echo    用户: user / password
echo    经理: manager / password
echo.
echo 🛑 关闭此窗口停止所有服务
echo.

pause
