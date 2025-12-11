@echo off
title XTT Cloud OA Startup Script

echo ================================
echo XTT Cloud OA 启动脚本
echo ================================

REM 检查Maven是否安装
where mvn >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未检测到Maven，请先安装Maven
    pause
    exit /b 1
)

REM 检查Node.js是否安装
node --version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未检测到Node.js，请先安装Node.js
    pause
    exit /b 1
)

REM 定义服务端口
set GATEWAY_PORT=30010
set AUTH_PORT=8020
set PLATFORM_PORT=8085
set DOCUMENT_PORT=8086
set FRONT_PORT=3000

REM 检查端口是否被占用
:check_port
REM 这里简化处理，实际项目中可能需要更复杂的端口检查逻辑

echo.
echo 选择要执行的操作:
echo 1. 启动所有服务
echo 2. 启动网关服务
echo 3. 启动认证服务
echo 4. 启动平台服务
echo 5. 启动公文服务
echo 6. 启动前端服务
echo 7. 停止所有服务
echo 8. 退出
echo.

set /p choice=请输入选项 (1-8): 

if "%choice%"=="1" (
    echo 启动所有服务...
    start "Gateway" cmd /c "cd gateway && mvn spring-boot:run"
    timeout /t 5 /nobreak >nul
    start "Auth" cmd /c "cd auth && mvn spring-boot:run"
    timeout /t 5 /nobreak >nul
    start "Platform" cmd /c "cd platform && mvn spring-boot:run"
    timeout /t 5 /nobreak >nul
    start "Document" cmd /c "cd document && mvn spring-boot:run"
    timeout /t 5 /nobreak >nul
    start "Frontend" cmd /c "cd front && npm run dev"
    echo 所有服务启动命令已执行
) else if "%choice%"=="2" (
    echo 启动网关服务...
    start "Gateway" cmd /c "cd gateway && mvn spring-boot:run"
) else if "%choice%"=="3" (
    echo 启动认证服务...
    start "Auth" cmd /c "cd auth && mvn spring-boot:run"
) else if "%choice%"=="4" (
    echo 启动平台服务...
    start "Platform" cmd /c "cd platform && mvn spring-boot:run"
) else if "%choice%"=="5" (
    echo 启动公文服务...
    start "Document" cmd /c "cd document && mvn spring-boot:run"
) else if "%choice%"=="6" (
    echo 启动前端服务...
    start "Frontend" cmd /c "cd front && npm run dev"
) else if "%choice%"=="7" (
    echo 停止所有服务...
    taskkill /f /im java.exe 2>nul
    taskkill /f /im node.exe 2>nul
    echo 所有服务已停止
) else if "%choice%"=="8" (
    echo 退出脚本
    exit /b 0
) else (
    echo 无效选项，请重新选择
)

echo.
echo 按任意键返回主菜单...
pause >nul
goto :check_port