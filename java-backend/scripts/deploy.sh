#!/bin/bash
# 机房巡检系统一键部署脚本
# 支持开发、测试、生产环境部署

set -e

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 打印函数
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查环境
check_environment() {
    print_info "检查运行环境..."

    # 检查Java
    if ! command -v java &> /dev/null; then
        print_error "Java未安装，请先安装JDK 8+"
        exit 1
    fi

    # 检查Maven
    if ! command -v mvn &> /dev/null; then
        print_error "Maven未安装，请先安装Maven"
        exit 1
    fi

    # 检查MySQL
    if ! command -v mysql &> /dev/null; then
        print_warn "MySQL客户端未安装，跳过数据库检查"
    fi

    # 检查Redis
    if ! command -v redis-cli &> /dev/null; then
        print_warn "Redis客户端未安装，跳过Redis检查"
    fi

    print_info "环境检查完成"
}

# 构建应用
build_application() {
    print_info "开始构建应用..."
    cd "$(dirname "$0")/.."

    mvn clean package -DskipTests

    if [ $? -eq 0 ]; then
        print_info "应用构建成功"
    else
        print_error "应用构建失败"
        exit 1
    fi
}

# 初始化数据库
init_database() {
    print_info "初始化数据库..."

    read -p "请输入数据库主机: " DB_HOST
    read -p "请输入数据库端口 (默认3306): " DB_PORT
    DB_PORT=${DB_PORT:-3306}
    read -sp "请输入数据库root密码: " DB_PASSWORD
    echo
    read -p "请输入数据库名称 (默认room_inspection): " DB_NAME
    DB_NAME=${DB_NAME:-room_inspection}

    # 创建数据库
    mysql -h ${DB_HOST} -P ${DB_PORT} -u root -p${DB_PASSWORD} << EOF
CREATE DATABASE IF NOT EXISTS ${DB_NAME} DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EOF

    # 导入初始化脚本
    if [ -f "sql/init.sql" ]; then
        mysql -h ${DB_HOST} -P ${DB_PORT} -u root -p${DB_PASSWORD} ${DB_NAME} < sql/init.sql
        print_info "数据库初始化完成"
    else
        print_warn "未找到初始化脚本，跳过数据库导入"
    fi
}

# 部署应用
deploy_application() {
    print_info "部署应用..."

    DEPLOY_DIR="/opt/room-inspection"
    SERVICE_USER="room-inspection"

    # 创建部署目录
    sudo mkdir -p ${DEPLOY_DIR}
    sudo mkdir -p /var/log/room-inspection
    sudo mkdir -p /opt/uploads

    # 创建用户
    if ! id -u ${SERVICE_USER} &>/dev/null; then
        sudo useradd -r -s /bin/false ${SERVICE_USER}
        print_info "创建服务用户: ${SERVICE_USER}"
    fi

    # 复制JAR包
    sudo cp target/room-inspection-*.jar ${DEPLOY_DIR}/room-inspection.jar
    sudo chown -R ${SERVICE_USER}:${SERVICE_USER} ${DEPLOY_DIR}
    sudo chown -R ${SERVICE_USER}:${SERVICE_USER} /var/log/room-inspection
    sudo chown -R ${SERVICE_USER}:${SERVICE_USER} /opt/uploads

    print_info "应用部署完成"
}

# 创建systemd服务
create_service() {
    print_info "创建systemd服务..."

    sudo tee /etc/systemd/system/room-inspection.service > /dev/null << EOF
[Unit]
Description=Room Inspection System Backend
After=network.target mysql.service redis.service

[Service]
Type=simple
User=room-inspection
Group=room-inspection
WorkingDirectory=/opt/room-inspection
Environment="SPRING_PROFILES_ACTIVE=${PROFILE}"
Environment="DB_HOST=${DB_HOST}"
Environment="DB_PORT=${DB_PORT}"
Environment="DB_NAME=${DB_NAME}"
Environment="DB_PASSWORD=${DB_PASSWORD}"
Environment="REDIS_HOST=${REDIS_HOST}"
Environment="REDIS_PORT=${REDIS_PORT}"
Environment="REDIS_PASSWORD=${REDIS_PASSWORD}"
ExecStart=/usr/bin/java -Xms2g -Xmx4g -XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/var/log/room-inspection/heap_dump.hprof -jar /opt/room-inspection/room-inspection.jar
ExecStop=/bin/kill -15 \$MAINPID
Restart=on-failure
RestartSec=10
SuccessExitStatus=143

[Install]
WantedBy=multi-user.target
EOF

    sudo systemctl daemon-reload
    print_info "systemd服务创建完成"
}

# 启动服务
start_service() {
    print_info "启动服务..."

    sudo systemctl enable room-inspection
    sudo systemctl start room-inspection

    sleep 5

    if systemctl is-active --quiet room-inspection; then
        print_info "服务启动成功"
    else
        print_error "服务启动失败，请查看日志: sudo journalctl -u room-inspection -n 50"
        exit 1
    fi
}

# 验证部署
verify_deployment() {
    print_info "验证部署..."

    # 检查HTTP健康检查
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health || echo "000")

    if [ "${HTTP_CODE}" -eq 200 ]; then
        print_info "健康检查通过"
    else
        print_error "健康检查失败，HTTP状态码: ${HTTP_CODE}"
        exit 1
    fi

    print_info "部署验证完成"
}

# 打印部署信息
print_deployment_info() {
    print_info "部署完成！"
    echo ""
    echo "========================================"
    echo "  部署信息"
    echo "========================================"
    echo "部署目录: /opt/room-inspection"
    echo "日志目录: /var/log/room-inspection"
    echo "上传目录: /opt/uploads"
    echo "服务名称: room-inspection"
    echo ""
    echo "常用命令:"
    echo "  查看状态: sudo systemctl status room-inspection"
    echo "  启动服务: sudo systemctl start room-inspection"
    echo "  停止服务: sudo systemctl stop room-inspection"
    echo "  重启服务: sudo systemctl restart room-inspection"
    echo "  查看日志: sudo journalctl -u room-inspection -f"
    echo "  查看应用日志: sudo tail -f /var/log/room-inspection/application.log"
    echo "========================================"
}

# Docker部署
deploy_docker() {
    print_info "Docker部署模式..."

    if ! command -v docker &> /dev/null; then
        print_error "Docker未安装，请先安装Docker"
        exit 1
    fi

    if ! command -v docker-compose &> /dev/null; then
        print_error "Docker Compose未安装，请先安装Docker Compose"
        exit 1
    fi

    # 构建Docker镜像
    docker-compose build

    # 启动服务
    docker-compose up -d

    print_info "Docker部署完成"
    print_info "查看日志: docker-compose logs -f"
}

# 主函数
main() {
    print_info "机房巡检系统部署脚本"
    print_info "========================================"

    # 选择环境
    echo "请选择部署环境:"
    echo "1) 开发环境"
    echo "2) 测试环境"
    echo "3) 生产环境"
    echo "4) Docker部署"
    read -p "请输入选项 (1-4): " OPTION

    case ${OPTION} in
        1)
            PROFILE="dev"
            print_info "选择开发环境"
            ;;
        2)
            PROFILE="test"
            print_info "选择测试环境"
            ;;
        3)
            PROFILE="prod"
            print_info "选择生产环境"
            ;;
        4)
            deploy_docker
            exit 0
            ;;
        *)
            print_error "无效的选项"
            exit 1
            ;;
    esac

    # 执行部署步骤
    check_environment
    build_application
    init_database
    deploy_application
    create_service
    start_service
    verify_deployment
    print_deployment_info
}

# 执行主函数
main "$@"
