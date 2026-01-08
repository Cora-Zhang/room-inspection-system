package com.roominspection.backend.plugin;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 协议注册器
 * 管理所有已注册的监控协议
 */
@Component
public class ProtocolRegistry {

    /**
     * 协议实例缓存
     * Key: 协议名称
     * Value: 协议实例
     */
    private final Map<String, MonitorProtocol> protocolMap = new ConcurrentHashMap<>();

    /**
     * 协议配置缓存
     * Key: 协议名称
     * Value: 协议配置
     */
    private final Map<String, Map<String, Object>> configMap = new ConcurrentHashMap<>();

    /**
     * 注册协议
     * @param protocol 协议实例
     * @param config 协议配置
     * @throws Exception 注册异常
     */
    public void register(MonitorProtocol protocol, Map<String, Object> config) throws Exception {
        String protocolName = protocol.getProtocolName();

        // 初始化协议
        protocol.init(config);

        // 注册协议
        protocolMap.put(protocolName, protocol);
        configMap.put(protocolName, config);
    }

    /**
     * 注销协议
     * @param protocolName 协议名称
     */
    public void unregister(String protocolName) {
        MonitorProtocol protocol = protocolMap.remove(protocolName);
        if (protocol != null) {
            protocol.destroy();
        }
        configMap.remove(protocolName);
    }

    /**
     * 获取协议实例
     * @param protocolName 协议名称
     * @return 协议实例
     */
    public MonitorProtocol getProtocol(String protocolName) {
        return protocolMap.get(protocolName);
    }

    /**
     * 检查协议是否已注册
     * @param protocolName 协议名称
     * @return 是否已注册
     */
    public boolean isRegistered(String protocolName) {
        return protocolMap.containsKey(protocolName);
    }

    /**
     * 获取所有已注册的协议名称
     * @return 协议名称列表
     */
    public java.util.Set<String> getAllProtocols() {
        return protocolMap.keySet();
    }

    /**
     * 获取协议配置
     * @param protocolName 协议名称
     * @return 协议配置
     */
    public Map<String, Object> getConfig(String protocolName) {
        return configMap.get(protocolName);
    }

    /**
     * 更新协议配置
     * @param protocolName 协议名称
     * @param config 新配置
     * @throws Exception 更新异常
     */
    public void updateConfig(String protocolName, Map<String, Object> config) throws Exception {
        MonitorProtocol protocol = protocolMap.get(protocolName);
        if (protocol != null) {
            protocol.destroy();
            protocol.init(config);
            configMap.put(protocolName, config);
        }
    }
}
