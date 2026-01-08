package com.roominspection.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.roominspection.backend.entity.DataCenter;

import java.util.List;
import java.util.Map;

/**
 * 数据中心管理服务接口
 */
public interface DataCenterService {

    /**
     * 分页查询数据中心列表
     * @param page 页码
     * @param size 每页数量
     * @param datacenterName 数据中心名称（可选）
     * @param status 状态（可选）
     * @return 分页结果
     */
    Page<DataCenter> listDataCenters(Integer page, Integer size, String datacenterName, String status);

    /**
     * 根据ID获取数据中心
     * @param id 数据中心ID
     * @return 数据中心
     */
    DataCenter getDataCenterById(Long id);

    /**
     * 根据编码获取数据中心
     * @param datacenterCode 数据中心编码
     * @return 数据中心
     */
    DataCenter getDataCenterByCode(String datacenterCode);

    /**
     * 创建数据中心
     * @param dataCenter 数据中心
     * @return 创建是否成功
     */
    boolean createDataCenter(DataCenter dataCenter);

    /**
     * 更新数据中心
     * @param dataCenter 数据中心
     * @return 更新是否成功
     */
    boolean updateDataCenter(DataCenter dataCenter);

    /**
     * 删除数据中心
     * @param id 数据中心ID
     * @return 删除是否成功
     */
    boolean deleteDataCenter(Long id);

    /**
     * 获取跨数据中心汇总数据
     * @return 汇总数据
     */
    Map<String, Object> getSummaryData();

    /**
     * 获取数据中心统计信息
     * @return 统计信息
     */
    Map<String, Object> getStatistics();

    /**
     * 获取数据中心详情（含设备、告警等）
     * @param id 数据中心ID
     * @return 详情数据
     */
    Map<String, Object> getDetail(Long id);

    /**
     * 同步数据中心状态
     * @param datacenterCode 数据中心编码
     * @return 同步是否成功
     */
    boolean syncDataCenterStatus(String datacenterCode);

    /**
     * 设置主数据中心
     * @param id 数据中心ID
     * @return 设置是否成功
     */
    boolean setPrimaryDataCenter(Long id);

    /**
     * 获取所有激活的数据中心
     * @return 数据中心列表
     */
    List<DataCenter> getActiveDataCenters();
}
