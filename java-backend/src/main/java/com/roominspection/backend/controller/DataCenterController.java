package com.roominspection.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.roominspection.backend.common.ApiVersion;
import com.roominspection.backend.common.Result;
import com.roominspection.backend.entity.DataCenter;
import com.roominspection.backend.service.DataCenterService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据中心管理控制器
 */
@RestController
@RequestMapping("/api/v1/datacenters")
@Api(tags = "数据中心管理")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DataCenterController {

    @Autowired
    private DataCenterService dataCenterService;

    /**
     * 分页查询数据中心列表
     */
    @GetMapping
    @ApiOperation(value = "分页查询数据中心列表", notes = "支持按名称和状态筛选")
    @ApiVersion("v1")
    public Result<Page<DataCenter>> listDataCenters(
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "每页数量", defaultValue = "10") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam(value = "数据中心名称") @RequestParam(required = false) String datacenterName,
            @ApiParam(value = "状态") @RequestParam(required = false) String status) {
        Page<DataCenter> result = dataCenterService.listDataCenters(page, size, datacenterName, status);
        return Result.page(result);
    }

    /**
     * 获取跨数据中心汇总数据
     */
    @GetMapping("/summary")
    @ApiOperation(value = "获取跨数据中心汇总数据", notes = "汇总所有数据中心的统计信息")
    @ApiVersion("v1")
    public Result<Map<String, Object>> getSummaryData() {
        Map<String, Object> summary = dataCenterService.getSummaryData();
        return Result.success(summary);
    }

    /**
     * 获取数据中心统计信息
     */
    @GetMapping("/statistics")
    @ApiOperation(value = "获取数据中心统计信息", notes = "统计数据中心的总体情况")
    @ApiVersion("v1")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> statistics = dataCenterService.getStatistics();
        return Result.success(statistics);
    }

    /**
     * 根据ID获取数据中心详情
     */
    @GetMapping("/{id}")
    @ApiOperation(value = "获取数据中心详情", notes = "获取数据中心的详细信息（含设备、告警等）")
    @ApiVersion("v1")
    public Result<Map<String, Object>> getDetail(
            @ApiParam(value = "数据中心ID", required = true) @PathVariable Long id) {
        Map<String, Object> detail = dataCenterService.getDetail(id);
        return Result.success(detail);
    }

    /**
     * 创建数据中心
     */
    @PostMapping
    @ApiOperation(value = "创建数据中心", notes = "注册新的数据中心")
    @ApiVersion("v1")
    public Result<String> createDataCenter(
            @ApiParam(value = "数据中心信息", required = true) @RequestBody DataCenter dataCenter) {
        boolean success = dataCenterService.createDataCenter(dataCenter);
        return success ? Result.success("创建成功") : Result.error("创建失败");
    }

    /**
     * 更新数据中心
     */
    @PutMapping("/{id}")
    @ApiOperation(value = "更新数据中心", notes = "更新数据中心信息")
    @ApiVersion("v1")
    public Result<String> updateDataCenter(
            @ApiParam(value = "数据中心ID", required = true) @PathVariable Long id,
            @ApiParam(value = "数据中心信息", required = true) @RequestBody DataCenter dataCenter) {
        dataCenter.setId(id);
        boolean success = dataCenterService.updateDataCenter(dataCenter);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    /**
     * 删除数据中心
     */
    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除数据中心", notes = "删除指定的数据中心")
    @ApiVersion("v1")
    public Result<String> deleteDataCenter(
            @ApiParam(value = "数据中心ID", required = true) @PathVariable Long id) {
        boolean success = dataCenterService.deleteDataCenter(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    /**
     * 同步数据中心状态
     */
    @PostMapping("/sync/{datacenterCode}")
    @ApiOperation(value = "同步数据中心状态", notes = "同步指定数据中心的最新状态")
    @ApiVersion("v1")
    public Result<String> syncDataCenterStatus(
            @ApiParam(value = "数据中心编码", required = true) @PathVariable String datacenterCode) {
        boolean success = dataCenterService.syncDataCenterStatus(datacenterCode);
        return success ? Result.success("同步成功") : Result.error("同步失败");
    }

    /**
     * 设置主数据中心
     */
    @PutMapping("/{id}/primary")
    @ApiOperation(value = "设置主数据中心", notes = "将指定数据中心设置为主数据中心")
    @ApiVersion("v1")
    public Result<String> setPrimaryDataCenter(
            @ApiParam(value = "数据中心ID", required = true) @PathVariable Long id) {
        boolean success = dataCenterService.setPrimaryDataCenter(id);
        return success ? Result.success("设置成功") : Result.error("设置失败");
    }

    /**
     * 获取所有激活的数据中心
     */
    @GetMapping("/active")
    @ApiOperation(value = "获取所有激活的数据中心", notes = "查询所有状态为激活的数据中心")
    @ApiVersion("v1")
    public Result<List<DataCenter>> getActiveDataCenters() {
        List<DataCenter> dataCenters = dataCenterService.getActiveDataCenters();
        return Result.success(dataCenters);
    }
}
