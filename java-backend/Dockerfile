# 多阶段构建Dockerfile

# 第一阶段：构建
FROM maven:3.8.6-openjdk-8-slim AS builder

WORKDIR /app

# 复制pom.xml并下载依赖（利用Docker缓存）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源代码并构建
COPY src ./src
RUN mvn clean package -DskipTests -B

# 第二阶段：运行
FROM openjdk:8u342-jre-slim

LABEL maintainer="support@roominspection.com"
LABEL version="1.0.0"
LABEL description="Room Inspection System Backend"

WORKDIR /app

# 安装必要的工具
RUN apt-get update && apt-get install -y \
    curl \
    netcat-openbsd \
    && rm -rf /var/lib/apt/lists/*

# 从构建阶段复制jar包
COPY --from=builder /app/target/*.jar app.jar

# 创建日志目录
RUN mkdir -p /var/log/room-inspection \
    && mkdir -p /opt/uploads

# 添加非root用户
RUN groupadd -r room-inspection && useradd -r -g room-inspection room-inspection \
    && chown -R room-inspection:room-inspection /app /var/log/room-inspection /opt/uploads

USER room-inspection

# JVM参数优化
ENV JAVA_OPTS="-Xms2g -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/var/log/room-inspection/heap_dump.hprof -Djava.security.egd=file:/dev/./urandom"

# 健康检查脚本
COPY docker/healthcheck.sh /healthcheck.sh
RUN chmod +x /healthcheck.sh

# 暴露端口
EXPOSE 8080 8081

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD /healthcheck.sh

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
