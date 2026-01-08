#!/bin/bash
# 机房巡检系统防火墙配置脚本

set -e

echo "开始配置防火墙规则..."

# 获取主网卡
INTERFACE=$(ip route | grep default | awk '{print $5}')
echo "主网卡: ${INTERFACE}"

# 清除现有规则
iptables -F
iptables -X
iptables -Z

# 设置默认策略
iptables -P INPUT DROP
iptables -P FORWARD DROP
iptables -P OUTPUT ACCEPT

# 允许本地回环
iptables -A INPUT -i lo -j ACCEPT
iptables -A OUTPUT -o lo -j ACCEPT

# 允许已建立和相关的连接
iptables -A INPUT -m state --state ESTABLISHED,RELATED -j ACCEPT

# 允许ICMP（ping）
iptables -A INPUT -p icmp --icmp-type echo-request -j ACCEPT

# ==================== DMZ区域规则 ====================

# 允许HTTP (80)
iptables -A INPUT -i ${INTERFACE} -p tcp --dport 80 -m state --state NEW -j ACCEPT

# 允许HTTPS (443)
iptables -A INPUT -i ${INTERFACE} -p tcp --dport 443 -m state --state NEW -j ACCEPT

# 允许SSH (仅内网)
iptables -A INPUT -s 10.0.0.0/8 -p tcp --dport 22 -m state --state NEW -j ACCEPT
iptables -A INPUT -s 172.16.0.0/12 -p tcp --dport 22 -m state --state NEW -j ACCEPT
iptables -A INPUT -s 192.168.0.0/16 -p tcp --dport 22 -m state --state NEW -j ACCEPT

# ==================== 业务网络规则 ====================

# 允许后端服务端口 (8080) - 仅DMZ访问
iptables -A INPUT -s 10.0.1.0/24 -p tcp --dport 8080 -m state --state NEW -j ACCEPT

# 允许管理端口 (8081) - 仅内网管理
iptables -A INPUT -s 10.0.1.0/24 -p tcp --dport 8081 -m state --state NEW -j ACCEPT

# ==================== 数据库规则 ====================

# MySQL端口 (3306) - 仅后端服务访问
iptables -A INPUT -s 10.0.1.0/24 -p tcp --dport 3306 -m state --state NEW -j ACCEPT

# Redis端口 (6379) - 仅后端服务访问
iptables -A INPUT -s 10.0.1.0/24 -p tcp --dport 6379 -m state --state NEW -j ACCEPT

# ==================== 监控网络规则 ====================

# SNMP端口 (161/162)
iptables -A INPUT -p udp --dport 161 -j ACCEPT
iptables -A INPUT -p udp --dport 162 -j ACCEPT

# Modbus TCP端口 (502) - 仅监控网络
iptables -A INPUT -s 10.0.3.0/24 -p tcp --dport 502 -m state --state NEW -j ACCEPT

# MQTT端口 (1883)
iptables -A INPUT -p tcp --dport 1883 -m state --state NEW -j ACCEPT

# 门禁API端口 (8443) - 仅特定IP
iptables -A INPUT -s 192.168.100.10 -p tcp --dport 8443 -m state --state NEW -j ACCEPT
iptables -A INPUT -s 192.168.100.20 -p tcp --dport 8443 -m state --state NEW -j ACCEPT

# Prometheus端口 (9090) - 仅内网
iptables -A INPUT -s 10.0.1.0/24 -p tcp --dport 9090 -m state --state NEW -j ACCEPT

# Grafana端口 (3000) - 仅内网
iptables -A INPUT -s 10.0.1.0/24 -p tcp --dport 3000 -m state --state NEW -j ACCEPT

# ==================== 防护规则 ====================

# 限制连接频率
iptables -A INPUT -p tcp --dport 80 -m limit --limit 100/second --limit-burst 200 -j ACCEPT
iptables -A INPUT -p tcp --dport 443 -m limit --limit 100/second --limit-burst 200 -j ACCEPT

# 防止SYN洪水攻击
iptables -A INPUT -p tcp --syn -m limit --limit 1/s --limit-burst 3 -j ACCEPT

# 防止ICMP洪水攻击
iptables -A INPUT -p icmp --icmp-type echo-request -m limit --limit 1/s --limit-burst 2 -j ACCEPT

# 记录并丢弃非法访问
iptables -A INPUT -j LOG --log-prefix "[FIREWALL DROP] " --log-level 4

# 保存规则
iptables-save > /etc/iptables/rules.v4

# 安装iptables-persistent（如果未安装）
if ! command -v iptables-persistent &> /dev/null; then
    apt-get update
    apt-get install -y iptables-persistent
fi

echo "防火墙配置完成！"
echo "当前规则："
iptables -L -n -v

# 重启iptables-persistent服务
systemctl restart netfilter-persistent

echo "防火墙规则已保存并应用。"
