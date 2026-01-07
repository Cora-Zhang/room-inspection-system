package com.roominspection.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.roominspection.backend.entity.ApiConfig;

import java.util.List;
import java.util.Map;

/**
 * 接口配置服务接口
 */
public interface ApiConfigService extends IService<ApiConfig> {

    /**
     * 分页查询接口配置
     *
     * @param page         分页对象
     * @param configType   配置类型
     * @param configGroup  配置分组
     * @param status       状态
     * @param configName   配置名称（模糊查询）
     * @return 分页结果
     */
    IPage<ApiConfig> queryApiConfigPage(Page<ApiConfig> page, String configType, String configGroup, Integer status, String configName);

    /**
     * 根据配置类型和配置键获取配置值
     *
     * @param configType 配置类型
     * @param configKey  配置键
     * @return 配置值
     */
    String getConfigValue(String configType, String configKey);

    /**
     * 根据配置类型获取所有配置（Map格式，key为configKey，value为configValue）
     *
     * @param configType 配置类型
     * @return 配置Map
     */
    Map<String, String> getConfigsByType(String configType);

    /**
     * 根据配置分组获取所有配置
     *
     * @param configGroup 配置分组
     * @return 配置列表
     */
    List<ApiConfig> getConfigsByGroup(String configGroup);

    /**
     * 更新配置值（支持敏感信息加密）
     *
     * @param id          配置ID
     * @param configValue 新的配置值
     * @param operatorId  操作人ID
     * @param operatorName 操作人姓名
     * @return 是否成功
     */
    boolean updateConfigValue(Long id, String configValue, Long operatorId, String operatorName);

    /**
     * 批量更新配置
     *
     * @param configs     配置列表
     * @param operatorId  操作人ID
     * @param operatorName 操作人姓名
     * @return 是否成功
     */
    boolean batchUpdateConfigs(List<ApiConfig> configs, Long operatorId, String operatorName);

    /**
     * 测试接口配置连接
     *
     * @param configType 配置类型
     * @return 测试结果（success-成功，fail-失败）
     */
    Map<String, Object> testConnection(String configType);

    /**
     * 导入配置（从Excel/JSON）
     *
     * @param configs     配置列表
     * @param operatorId  操作人ID
     * @param operatorName 操作人姓名
     * @return 导入结果
     */
    Map<String, Object> importConfigs(List<ApiConfig> configs, Long operatorId, String operatorName);

    /**
     * 导出配置（导出为JSON格式）
     *
     * @param configType 配置类型（可选）
     * @param configGroup 配置分组（可选）
     * @return 配置数据
     */
    List<Map<String, Object>> exportConfigs(String configType, String configGroup);
}
