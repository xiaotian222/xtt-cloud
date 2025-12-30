.PHONY: help build build-all up down restart logs ps clean

# 默认目标
.DEFAULT_GOAL := help

# 颜色定义
GREEN  := $(shell tput -Txterm setaf 2)
YELLOW := $(shell tput -Txterm setaf 3)
RESET  := $(shell tput -Txterm sgr0)

help: ## 显示帮助信息
	@echo "$(GREEN)XTT Cloud OA - Docker 管理命令$(RESET)"
	@echo ""
	@echo "可用命令:"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  $(YELLOW)%-20s$(RESET) %s\n", $$1, $$2}'

build: ## 构建所有服务镜像
	@echo "$(GREEN)构建所有服务镜像...$(RESET)"
	docker-compose build

build-all: ## 构建所有服务镜像（不缓存）
	@echo "$(GREEN)构建所有服务镜像（不缓存）...$(RESET)"
	docker-compose build --no-cache

build-gateway: ## 构建 Gateway 服务镜像
	@echo "$(GREEN)构建 Gateway 服务镜像...$(RESET)"
	docker build -f gateway/Dockerfile -t xtt-cloud-oa/gateway:latest .

build-auth: ## 构建 Auth 服务镜像
	@echo "$(GREEN)构建 Auth 服务镜像...$(RESET)"
	docker build -f auth/Dockerfile -t xtt-cloud-oa/auth:latest .

build-platform: ## 构建 Platform 服务镜像
	@echo "$(GREEN)构建 Platform 服务镜像...$(RESET)"
	docker build -f platform/Dockerfile -t xtt-cloud-oa/platform:latest .

build-workflow: ## 构建 Workflow 服务镜像
	@echo "$(GREEN)构建 Workflow 服务镜像...$(RESET)"
	docker build -f workflow/Dockerfile -t xtt-cloud-oa/workflow:latest .

up: ## 启动所有服务
	@echo "$(GREEN)启动所有服务...$(RESET)"
	docker-compose up -d

up-infra: ## 只启动基础设施服务（MySQL, Redis, Nacos）
	@echo "$(GREEN)启动基础设施服务...$(RESET)"
	docker-compose up -d mysql redis nacos

down: ## 停止所有服务
	@echo "$(GREEN)停止所有服务...$(RESET)"
	docker-compose down

down-v: ## 停止所有服务并删除数据卷
	@echo "$(YELLOW)警告: 这将删除所有数据卷！$(RESET)"
	docker-compose down -v

restart: ## 重启所有服务
	@echo "$(GREEN)重启所有服务...$(RESET)"
	docker-compose restart

restart-gateway: ## 重启 Gateway 服务
	docker-compose restart gateway

restart-auth: ## 重启 Auth 服务
	docker-compose restart auth

restart-platform: ## 重启 Platform 服务
	docker-compose restart platform

restart-workflow: ## 重启 Workflow 服务
	docker-compose restart workflow

restart-file: ## 重启 File 服务
	docker-compose restart file

logs: ## 查看所有服务日志
	docker-compose logs -f

logs-gateway: ## 查看 Gateway 服务日志
	docker-compose logs -f gateway

logs-auth: ## 查看 Auth 服务日志
	docker-compose logs -f auth

logs-platform: ## 查看 Platform 服务日志
	docker-compose logs -f platform

logs-workflow: ## 查看 Workflow 服务日志
	docker-compose logs -f workflow

logs-file: ## 查看 File 服务日志
	docker-compose logs -f file

ps: ## 查看服务状态
	docker-compose ps

clean: ## 清理未使用的 Docker 资源
	@echo "$(GREEN)清理未使用的 Docker 资源...$(RESET)"
	docker system prune -f

clean-all: ## 清理所有 Docker 资源（包括镜像和卷）
	@echo "$(YELLOW)警告: 这将删除所有未使用的 Docker 资源！$(RESET)"
	docker system prune -a -f --volumes

health: ## 检查所有服务健康状态
	@echo "$(GREEN)检查服务健康状态...$(RESET)"
	@echo "Gateway:"
	@curl -s http://localhost:30010/actuator/health || echo "  ❌ 未运行"
	@echo "Auth:"
	@curl -s http://localhost:8020/actuator/health || echo "  ❌ 未运行"
	@echo "Platform:"
	@curl -s http://localhost:8085/actuator/health || echo "  ❌ 未运行"
	@echo "Workflow:"
	@curl -s http://localhost:8091/actuator/health || echo "  ❌ 未运行"
	@echo "File:"
	@curl -s http://localhost:8090/actuator/health || echo "  ❌ 未运行"

backup-db: ## 备份 MySQL 数据库
	@echo "$(GREEN)备份 MySQL 数据库...$(RESET)"
	@mkdir -p backups
	docker exec xtt-cloud-oa-mysql mysqldump -u root -proot123456 --all-databases > backups/backup-$$(date +%Y%m%d-%H%M%S).sql
	@echo "$(GREEN)备份完成: backups/backup-$$(date +%Y%m%d-%H%M%S).sql$(RESET)"

restore-db: ## 恢复 MySQL 数据库（需要指定文件: make restore-db FILE=backups/backup.sql）
	@if [ -z "$(FILE)" ]; then \
		echo "$(YELLOW)错误: 请指定备份文件，例如: make restore-db FILE=backups/backup.sql$(RESET)"; \
		exit 1; \
	fi
	@echo "$(GREEN)恢复 MySQL 数据库: $(FILE)...$(RESET)"
	docker exec -i xtt-cloud-oa-mysql mysql -u root -proot123456 < $(FILE)
	@echo "$(GREEN)恢复完成$(RESET)"

prod-up: ## 启动生产环境服务
	@echo "$(GREEN)启动生产环境服务...$(RESET)"
	docker-compose -f docker-compose.prod.yml up -d

prod-down: ## 停止生产环境服务
	@echo "$(GREEN)停止生产环境服务...$(RESET)"
	docker-compose -f docker-compose.prod.yml down

prod-logs: ## 查看生产环境服务日志
	docker-compose -f docker-compose.prod.yml logs -f

