#!/bin/bash
# ============================================
# 机房巡检系统 - 一键部署脚本
# 适用于 Docker Compose 部署
# ============================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查命令是否存在
check_command() {
    if ! command -v $1 &> /dev/null; then
        log_error "$1 未安装，请先安装 $1"
        exit 1
    fi
}

# 检查Docker和Docker Compose
check_dependencies() {
    log_info "检查依赖..."
    check_command docker
    check_command docker-compose
    log_success "依赖检查完成"
}

# 创建必要的目录
create_directories() {
    log_info "创建必要的目录..."
    sudo mkdir -p /var/lib/room-inspection/uploads
    sudo mkdir -p /var/log/room-inspection
    sudo mkdir -p ./nginx/ssl
    sudo chown -R $USER:$USER /var/lib/room-inspection
    sudo chown -R $USER:$USER /var/log/room-inspection
    log_success "目录创建完成"
}

# 配置环境变量
setup_env() {
    log_info "配置环境变量..."
    
    if [ ! -f .env.production ]; then
        log_warning ".env.production 不存在，从模板创建..."
        cp .env.production.example .env.production
        log_warning "请编辑 .env.production 文件，修改相关配置"
        read -p "是否现在编辑环境变量文件? (y/n): " edit_env
        if [ "$edit_env" = "y" ]; then
            ${EDITOR:-nano} .env.production
        fi
    else
        log_success "环境变量文件已存在"
    fi
}

# 停止旧服务
stop_services() {
    log_info "停止旧服务..."
    docker-compose -f docker-compose.full.yml down
    log_success "旧服务已停止"
}

# 构建镜像
build_images() {
    log_info "构建 Docker 镜像..."
    docker-compose -f docker-compose.full.yml build --no-cache
    log_success "镜像构建完成"
}

# 启动服务
start_services() {
    log_info "启动服务..."
    docker-compose -f docker-compose.full.yml up -d
    log_success "服务启动完成"
}

# 等待服务就绪
wait_for_services() {
    log_info "等待服务就绪..."
    
    # 等待 MySQL
    log_info "等待 MySQL 就绪..."
    max_attempts=30
    attempt=0
    while [ $attempt -lt $max_attempts ]; do
        if docker-compose -f docker-compose.full.yml exec -T mysql mysqladmin ping -h localhost -u root -p${MYSQL_ROOT_PASSWORD:-root123456} &> /dev/null; then
            log_success "MySQL 已就绪"
            break
        fi
        attempt=$((attempt+1))
        sleep 2
    done
    
    if [ $attempt -eq $max_attempts ]; then
        log_error "MySQL 启动超时"
        exit 1
    fi
    
    # 等待 Redis
    log_info "等待 Redis 就绪..."
    max_attempts=30
    attempt=0
    while [ $attempt -lt $max_attempts ]; do
        if docker-compose -f docker-compose.full.yml exec -T redis redis-cli ping &> /dev/null; then
            log_success "Redis 已就绪"
            break
        fi
        attempt=$((attempt+1))
        sleep 2
    done
    
    if [ $attempt -eq $max_attempts ]; then
        log_error "Redis 启动超时"
        exit 1
    fi
    
    # 等待后端
    log_info "等待后端服务就绪..."
    max_attempts=60
    attempt=0
    while [ $attempt -lt $max_attempts ]; do
        if curl -s http://localhost:${BACKEND_PORT:-8080}/api/health &> /dev/null; then
            log_success "后端服务已就绪"
            break
        fi
        attempt=$((attempt+1))
        sleep 3
    done
    
    if [ $attempt -eq $max_attempts ]; then
        log_warning "后端服务可能未完全就绪，请检查日志"
    fi
    
    sleep 5
    log_success "所有服务已就绪"
}

# 检查服务状态
check_services() {
    log_info "检查服务状态..."
    docker-compose -f docker-compose.full.yml ps
}

# 显示访问信息
show_access_info() {
    log_success "=========================================="
    log_success "部署完成！"
    log_success "=========================================="
    echo ""
    echo "前端访问地址: http://localhost:${FRONTEND_PORT:-5000}"
    echo "后端API地址:  http://localhost:${BACKEND_PORT:-8080}/api"
    echo "Grafana地址:   http://localhost:${GRAFANA_PORT:-3000}"
    echo "Prometheus:    http://localhost:${PROMETHEUS_PORT:-9090}"
    echo ""
    echo "默认管理员账号: admin"
    echo "默认管理员密码: Admin@123"
    echo ""
    echo "查看日志: docker-compose -f docker-compose.full.yml logs -f"
    echo "停止服务: docker-compose -f docker-compose.full.yml down"
    echo ""
    log_success "=========================================="
}

# 主函数
main() {
    log_info "=========================================="
    log_info "机房巡检系统 - 开始部署"
    log_info "=========================================="
    echo ""
    
    check_dependencies
    create_directories
    setup_env
    
    read -p "是否重新构建镜像? (y/n): " rebuild
    if [ "$rebuild" = "y" ]; then
        stop_services
        build_images
    fi
    
    start_services
    wait_for_services
    check_services
    show_access_info
    
    log_success "部署完成！"
}

# 执行主函数
main "$@"
