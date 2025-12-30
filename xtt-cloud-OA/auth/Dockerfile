# 多阶段构建 Dockerfile for Auth Service
# 注意：此 Dockerfile 需要从 xtt-cloud-OA 目录执行构建
# 构建命令：docker build -f auth/Dockerfile -t auth:latest .

# 阶段1: 构建阶段
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# 复制父 POM 和所有模块的 POM（Maven 需要完整的项目结构）
COPY pom.xml /build/pom.xml
COPY common/pom.xml /build/common/pom.xml
COPY auth/pom.xml /build/auth/pom.xml

# 复制源代码（auth 依赖 common 模块）
COPY common/src /build/common/src
COPY auth/src /build/auth/src

# 构建应用
RUN mvn clean package -DskipTests -f /build/pom.xml -pl auth -am

# 阶段2: 运行阶段
FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="xtt-cloud"
LABEL description="Auth Service - 认证服务"

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

COPY --from=builder /build/auth/target/auth-*.jar /app/app.jar

RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8020

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8020/actuator/health || exit 1

ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

