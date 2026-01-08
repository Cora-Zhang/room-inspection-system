package com.roominspection.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.roominspection.backend.entity.ProtocolPlugin;
import com.roominspection.backend.mapper.ProtocolPluginMapper;
import com.roominspection.backend.plugin.MonitorProtocol;
import com.roominspection.backend.plugin.ProtocolRegistry;
import com.roominspection.backend.service.ProtocolService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 协议管理服务实现类
 */
@Service
public class ProtocolServiceImpl implements ProtocolService {

    @Autowired
    private ProtocolPluginMapper protocolPluginMapper;

    @Autowired
    private ProtocolRegistry protocolRegistry;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * JAR文件存储目录
     */
    private static final String JAR_STORAGE_DIR = "/tmp/plugins/";

    @Override
    public Page<ProtocolPlugin> listPlugins(Integer page, Integer size, String protocolName, String status) {
        Page<ProtocolPlugin> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ProtocolPlugin> queryWrapper = new LambdaQueryWrapper<>();

        if (protocolName != null && !protocolName.isEmpty()) {
            queryWrapper.like(ProtocolPlugin::getProtocolName, protocolName);
        }

        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(ProtocolPlugin::getStatus, status);
        }

        queryWrapper.orderByDesc(ProtocolPlugin::getCreateTime);
        return protocolPluginMapper.selectPage(pageParam, queryWrapper);
    }

    @Override
    public ProtocolPlugin getPluginById(Long id) {
        return protocolPluginMapper.selectById(id);
    }

    @Override
    public ProtocolPlugin getPluginByName(String protocolName) {
        LambdaQueryWrapper<ProtocolPlugin> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProtocolPlugin::getProtocolName, protocolName);
        return protocolPluginMapper.selectOne(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean registerPlugin(ProtocolPlugin plugin) {
        plugin.setCreateTime(LocalDateTime.now());
        plugin.setUpdateTime(LocalDateTime.now());
        plugin.setStatus("enabled");

        int result = protocolPluginMapper.insert(plugin);

        // 如果启用状态，则加载协议
        if (result > 0 && "enabled".equals(plugin.getStatus())) {
            loadProtocol(plugin);
        }

        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean uploadAndRegisterPlugin(ProtocolPlugin plugin, byte[] jarBytes) {
        try {
            // 创建存储目录
            Path storagePath = Paths.get(JAR_STORAGE_DIR);
            if (!Files.exists(storagePath)) {
                Files.createDirectories(storagePath);
            }

            // 保存JAR文件
            String fileName = plugin.getProtocolName() + "-" + UUID.randomUUID().toString() + ".jar";
            String filePath = JAR_STORAGE_DIR + fileName;
            Path jarPath = Paths.get(filePath);
            Files.write(jarPath, jarBytes);

            plugin.setJarFilePath(filePath);
            plugin.setCreateTime(LocalDateTime.now());
            plugin.setUpdateTime(LocalDateTime.now());
            plugin.setStatus("enabled");

            int result = protocolPluginMapper.insert(plugin);

            // 加载协议
            if (result > 0) {
                loadProtocol(plugin);
            }

            return result > 0;
        } catch (IOException e) {
            throw new RuntimeException("上传JAR文件失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePlugin(ProtocolPlugin plugin) {
        plugin.setUpdateTime(LocalDateTime.now());
        int result = protocolPluginMapper.updateById(plugin);

        // 重新加载协议
        if (result > 0) {
            loadProtocol(plugin);
        }

        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePlugin(Long id) {
        ProtocolPlugin plugin = protocolPluginMapper.selectById(id);
        if (plugin == null) {
            return false;
        }

        // 注销协议
        protocolRegistry.unregister(plugin.getProtocolName());

        // 删除JAR文件
        if (plugin.getJarFilePath() != null) {
            try {
                Files.deleteIfExists(Paths.get(plugin.getJarFilePath()));
            } catch (IOException e) {
                // 记录日志，但不影响删除操作
            }
        }

        return protocolPluginMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean enablePlugin(Long id) {
        ProtocolPlugin plugin = protocolPluginMapper.selectById(id);
        if (plugin == null) {
            return false;
        }

        plugin.setStatus("enabled");
        plugin.setUpdateTime(LocalDateTime.now());
        int result = protocolPluginMapper.updateById(plugin);

        // 加载协议
        if (result > 0) {
            loadProtocol(plugin);
        }

        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean disablePlugin(Long id) {
        ProtocolPlugin plugin = protocolPluginMapper.selectById(id);
        if (plugin == null) {
            return false;
        }

        plugin.setStatus("disabled");
        plugin.setUpdateTime(LocalDateTime.now());
        int result = protocolPluginMapper.updateById(plugin);

        // 注销协议
        if (result > 0) {
            protocolRegistry.unregister(plugin.getProtocolName());
        }

        return result > 0;
    }

    @Override
    public Map<String, Object> getPluginStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        long totalCount = protocolPluginMapper.selectCount(null);
        long enabledCount = protocolPluginMapper.selectCount(
                new LambdaQueryWrapper<ProtocolPlugin>().eq(ProtocolPlugin::getStatus, "enabled")
        );
        long disabledCount = totalCount - enabledCount;

        statistics.put("totalCount", totalCount);
        statistics.put("enabledCount", enabledCount);
        statistics.put("disabledCount", disabledCount);
        statistics.put("registeredCount", protocolRegistry.getAllProtocols().size());

        return statistics;
    }

    @Override
    public List<ProtocolPlugin> getEnabledPlugins() {
        LambdaQueryWrapper<ProtocolPlugin> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProtocolPlugin::getStatus, "enabled");
        return protocolPluginMapper.selectList(queryWrapper);
    }

    /**
     * 加载协议
     * @param plugin 协议插件信息
     */
    private void loadProtocol(ProtocolPlugin plugin) {
        try {
            // 解析配置Schema
            Map<String, Object> config = objectMapper.readValue(plugin.getConfigSchema(), Map.class);

            // 动态加载协议类（需要实现自定义类加载器）
            // 这里简化处理，实际需要实现URLClassLoader动态加载JAR
            MonitorProtocol protocol = instantiateProtocol(plugin.getImplementationClass());

            // 注册协议
            protocolRegistry.register(protocol, config);
        } catch (Exception e) {
            throw new RuntimeException("加载协议失败: " + plugin.getProtocolName(), e);
        }
    }

    /**
     * 实例化协议类
     * @param className 类全限定名
     * @return 协议实例
     */
    private MonitorProtocol instantiateProtocol(String className) throws Exception {
        // TODO: 实现自定义类加载器，动态加载JAR文件中的类
        // 这里简化处理，直接通过反射实例化
        Class<?> clazz = Class.forName(className);
        return (MonitorProtocol) clazz.getDeclaredConstructor().newInstance();
    }
}
