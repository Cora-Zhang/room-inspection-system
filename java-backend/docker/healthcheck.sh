#!/bin/sh

# 健康检查脚本

# 检查应用是否在运行
if ! pgrep -f "app.jar" > /dev/null; then
    echo "Application is not running"
    exit 1
fi

# 检查HTTP健康检查端点
HEALTH_URL="http://localhost:8080/actuator/health"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" --max-time 5 "${HEALTH_URL}")

if [ "${HTTP_CODE}" -eq 200 ]; then
    echo "Health check passed"
    exit 0
else
    echo "Health check failed with HTTP code: ${HTTP_CODE}"
    exit 1
fi
