package com.roominspection.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.roominspection.backend.common.ApiVersion;
import com.roominspection.backend.common.Result;
import com.roominspection.backend.entity.CollectorNode;
import com.roominspection.backend.entity.CollectionTask;
import com.roominspection.backend.service.CollectionTaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据采集管理控制器
 */
@RestController
@RequestMapping("/api/v1/collection")
@Api(tags = "数据采集管理")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CollectionTaskController {

    @Autowired
    private CollectionTaskService collectionTaskService;

    /**
     * 分页查询采集任务
     */
    @GetMapping("/tasks")
    @ApiOperation(value = "分页查询采集任务", notes = "支持按状态和采集节点筛选")
    @ApiVersion("v1")
    public Result<Page<CollectionTask>> listTasks(
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "每页数量", defaultValue = "10") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam(value = "任务状态") @RequestParam(required = false) String status,
            @ApiParam(value = "采集节点ID") @RequestParam(required = false) String collectorId) {
        Page<CollectionTask> result = collectionTaskService.listTasks(page, size, status, collectorId);
        return Result.page(result);
    }

    /**
     * 创建采集任务
     */
    @PostMapping("/tasks")
    @ApiOperation(value = "创建采集任务", notes = "创建新的采集任务")
    @ApiVersion("v1")
    public Result<String> createTask(
            @ApiParam(value = "采集任务信息", required = true) @RequestBody CollectionTask task) {
        boolean success = collectionTaskService.createTask(task);
        return success ? Result.success("创建成功") : Result.error("创建失败");
    }

    /**
     * 分配采集任务
     */
    @PostMapping("/tasks/{id}/assign")
    @ApiOperation(value = "分配采集任务", notes = "将任务分配到最优采集节点")
    @ApiVersion("v1")
    public Result<String> assignTask(
            @ApiParam(value = "任务ID", required = true) @PathVariable Long id) {
        boolean success = collectionTaskService.assignTask(id);
        return success ? Result.success("分配成功") : Result.error("分配失败");
    }

    /**
     * 执行采集任务
     */
    @PostMapping("/tasks/{id}/execute")
    @ApiOperation(value = "执行采集任务", notes = "立即执行指定的采集任务")
    @ApiVersion("v1")
    public Result<Map<String, Object>> executeTask(
            @ApiParam(value = "任务ID", required = true) @PathVariable Long id) {
        Map<String, Object> result = collectionTaskService.executeTask(id);
        return Result.success(result);
    }

    /**
     * 取消采集任务
     */
    @PutMapping("/tasks/{id}/cancel")
    @ApiOperation(value = "取消采集任务", notes = "取消指定的采集任务")
    @ApiVersion("v1")
    public Result<String> cancelTask(
            @ApiParam(value = "任务ID", required = true) @PathVariable Long id) {
        boolean success = collectionTaskService.cancelTask(id);
        return success ? Result.success("取消成功") : Result.error("取消失败");
    }

    /**
     * 重新执行失败任务
     */
    @PostMapping("/tasks/{id}/retry")
    @ApiOperation(value = "重新执行失败任务", notes = "重新执行失败的采集任务")
    @ApiVersion("v1")
    public Result<String> retryTask(
            @ApiParam(value = "任务ID", required = true) @PathVariable Long id) {
        boolean success = collectionTaskService.retryTask(id);
        return success ? Result.success("重试成功") : Result.error("重试失败");
    }

    /**
     * 获取采集任务统计信息
     */
    @GetMapping("/tasks/statistics")
    @ApiOperation(value = "获取采集任务统计信息", notes = "统计采集任务的总体情况")
    @ApiVersion("v1")
    public Result<Map<String, Object>> getTaskStatistics() {
        Map<String, Object> statistics = collectionTaskService.getTaskStatistics();
        return Result.success(statistics);
    }

    /**
     * 获取采集节点列表
     */
    @GetMapping("/nodes")
    @ApiOperation(value = "获取采集节点列表", notes = "查询所有采集节点")
    @ApiVersion("v1")
    public Result<List<CollectorNode>> getCollectorNodes() {
        List<CollectorNode> nodes = collectionTaskService.getCollectorNodes();
        return Result.success(nodes);
    }

    /**
     * 注册采集节点
     */
    @PostMapping("/nodes/register")
    @ApiOperation(value = "注册采集节点", notes = "注册新的采集节点")
    @ApiVersion("v1")
    public Result<String> registerNode(
            @ApiParam(value = "采集节点信息", required = true) @RequestBody CollectorNode node) {
        boolean success = collectionTaskService.registerNode(node);
        return success ? Result.success("注册成功") : Result.error("注册失败");
    }

    /**
     * 采集节点心跳
     */
    @PostMapping("/nodes/{nodeId}/heartbeat")
    @ApiOperation(value = "采集节点心跳", notes = "节点定期发送心跳保持在线状态")
    @ApiVersion("v1")
    public Result<String> nodeHeartbeat(
            @ApiParam(value = "节点ID", required = true) @PathVariable String nodeId) {
        boolean success = collectionTaskService.nodeHeartbeat(nodeId);
        return success ? Result.success("心跳成功") : Result.error("心跳失败");
    }

    /**
     * 节点负载上报
     */
    @PostMapping("/nodes/{nodeId}/load")
    @ApiOperation(value = "节点负载上报", notes = "节点上报当前负载情况")
    @ApiVersion("v1")
    public Result<String> reportLoad(
            @ApiParam(value = "节点ID", required = true) @PathVariable String nodeId,
            @ApiParam(value = "负载信息", required = true) @RequestBody Map<String, Object> loadInfo) {
        boolean success = collectionTaskService.reportLoad(nodeId, loadInfo);
        return success ? Result.success("上报成功") : Result.error("上报失败");
    }

    /**
     * 创建定时采集任务
     */
    @PostMapping("/scheduled-tasks")
    @ApiOperation(value = "创建定时采集任务", notes = "创建定时执行的采集任务")
    @ApiVersion("v1")
    public Result<String> createScheduledTask(
            @ApiParam(value = "采集任务信息", required = true) @RequestBody CollectionTask task) {
        boolean success = collectionTaskService.createScheduledTask(task);
        return success ? Result.success("创建成功") : Result.error("创建失败");
    }

    /**
     * 删除定时采集任务
     */
    @DeleteMapping("/scheduled-tasks/{id}")
    @ApiOperation(value = "删除定时采集任务", notes = "删除定时采集任务")
    @ApiVersion("v1")
    public Result<String> deleteScheduledTask(
            @ApiParam(value = "任务ID", required = true) @PathVariable Long id) {
        boolean success = collectionTaskService.deleteScheduledTask(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }
}
