package com.roominspection.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.roominspection.backend.entity.ProtocolPlugin;

import java.util.List;
import java.util.Map;

/**
 * 协议管理服务接口
 */
public interface ProtocolService {

    /**
     * 分页查询协议插件列表
     * @param page 页码
     * @param size 每页数量
     * @param protocolName 协议名称（可选）
     * @param status 状态（可选）
     * @return 分页结果
     */
    Page<ProtocolPlugin> listPlugins(Integer page, Integer size, String protocolName, String status);

    /**
     * 根据ID获取协议插件
     * @param id 插件ID
     * @return 协议插件
     */
    ProtocolPlugin getPluginById(Long id);

    /**
     * 根据名称获取协议插件
     * @param protocolName 协议名称
     * @return 协议插件
     */
    ProtocolPlugin getPluginByName(String protocolName);

    /**
     * 注册协议插件
     * @param plugin 协议插件信息
     * @return 注册是否成功
     */
    boolean registerPlugin(ProtocolPlugin plugin);

    /**
     * 上传并注册协议插件
     * @param plugin 协议插件信息
     * @param jarBytes JAR文件字节数组
     * @return 注册是否成功
     */
    boolean uploadAndRegisterPlugin(ProtocolPlugin plugin, byte[] jarBytes);

    /**
     * 更新协议插件
     * @param plugin 协议插件信息
     * @return 更新是否成功
     */
    boolean updatePlugin(ProtocolPlugin plugin);

    /**
     * 删除协议插件
     * @param id 插件ID
     * @return 删除是否成功
     */
    boolean deletePlugin(Long id);

    /**
     * 启用协议插件
     * @param id 插件ID
     * @return 启用是否成功
     */
    boolean enablePlugin(Long id);

    /**
     * 禁用协议插件
     * @param id 插件ID
     * @return 禁用是否成功
     */
    boolean disablePlugin(Long id);

    /**
     * 获取协议插件统计信息
     * @return 统计信息
     */
    Map<String, Object> getPluginStatistics();

    /**
     * 获取已启用的协议列表
     * @return 协议列表
     */
    List<ProtocolPlugin> getEnabledPlugins();
}
