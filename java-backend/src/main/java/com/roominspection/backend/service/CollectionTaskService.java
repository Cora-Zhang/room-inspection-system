package com.roominspection.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.roominspection.backend.entity.CollectorNode;
import com.roominspection.backend.entity.CollectionTask;

import java.util.List;
import java.util.Map;

/**
 * 分布式数据采集服务接口
 */
public interface CollectionTaskService {

    /**
     * 分页查询采集任务
     * @param page 页码
     * @param size 每页数量
     * @param status 任务状态（可选）
     * @param collectorId 采集节点ID（可选）
     * @return 分页结果
     */
    Page<CollectionTask> listTasks(Integer page, Integer size, String status, String collectorId);

    /**
     * 创建采集任务
     * @param task 采集任务
     * @return 创建是否成功
     */
    boolean createTask(CollectionTask task);

    /**
     * 分配采集任务到采集节点
     * @param taskId 任务ID
     * @return 分配是否成功
     */
    boolean assignTask(Long taskId);

    /**
     * 执行采集任务
     * @param taskId 任务ID
     * @return 执行结果
     */
    Map<String, Object> executeTask(Long taskId);

    /**
     * 取消采集任务
     * @param taskId 任务ID
     * @return 取消是否成功
     */
    boolean cancelTask(Long taskId);

    /**
     * 重新执行失败任务
     * @param taskId 任务ID
     * @return 重新执行是否成功
     */
    boolean retryTask(Long taskId);

    /**
     * 获取采集任务统计信息
     * @return 统计信息
     */
    Map<String, Object> getTaskStatistics();

    /**
     * 获取采集节点列表
     * @return 采集节点列表
     */
    List<CollectorNode> getCollectorNodes();

    /**
     * 注册采集节点
     * @param node 采集节点信息
     * @return 注册是否成功
     */
    boolean registerNode(CollectorNode node);

    /**
     * 采集节点心跳
     * @param nodeId 节点ID
     * @return 心跳是否成功
     */
    boolean nodeHeartbeat(String nodeId);

    /**
     * 节点负载上报
     * @param nodeId 节点ID
     * @param loadInfo 负载信息
     * @return 上报是否成功
     */
    boolean reportLoad(String nodeId, Map<String, Object> loadInfo);

    /**
     * 获取最优采集节点
     * @return 最优节点ID
     */
    String getBestCollector();

    /**
     * 创建定时采集任务
     * @param task 采集任务
     * @return 创建是否成功
     */
    boolean createScheduledTask(CollectionTask task);

    /**
     * 删除定时采集任务
     * @param taskId 任务ID
     * @return 删除是否成功
     */
    boolean deleteScheduledTask(Long taskId);
}
