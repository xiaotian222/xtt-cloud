# 多阶段构建 Dockerfile for Gateway Service
# 注意：此 Dockerfile 需要从 xtt-cloud-OA 目录执行构建
# 构建命令：docker build -f gateway/Dockerfile -t gateway:latest .

# 阶段1: 构建阶段
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# 复制父 POM 和所有模块的 POM
COPY pom.xml /build/pom.xml
COPY gateway/pom.xml /build/gateway/pom.xml

# 复制源代码
COPY gateway/src /build/gateway/src

# 构建应用
RUN mvn clean package -DskipTests -f /build/pom.xml -pl gateway -am

# 阶段2: 运行阶段
FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="xtt-cloud"
LABEL description="Gateway Service - 网关服务"

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

COPY --from=builder /build/gateway/target/gateway-*.jar /app/app.jar

RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 30010

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:30010/actuator/health || exit 1

ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]