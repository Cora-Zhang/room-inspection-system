package com.roominspection.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.roominspection.backend.entity.Device;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

/**
 * 设备资产管理服务接口
 */
public interface DeviceAssetService extends IService<Device> {

    /**
     * 分页查询设备
     *
     * @param page       分页对象
     * @param roomId     机房ID
     * @param type       设备类型
     * @param subType    设备子类型
     * @param status     设备状态
     * @param deviceName 设备名称（模糊查询）
     * @param ipAddress  IP地址
     * @return 分页结果
     */
    IPage<Device> queryDevicePage(Page<Device> page, String roomId, String type, String subType, String status, String deviceName, String ipAddress);

    /**
     * 批量导入设备
     *
     * @param file Excel/CSV文件
     * @param operatorId  操作人ID
     * @param operatorName 操作人姓名
     * @return 导入结果
     */
    Map<String, Object> importDevices(MultipartFile file, Long operatorId, String operatorName);

    /**
     * 导出设备
     *
     * @param deviceType 设备类型
     * @param roomId     机房ID
     * @return 设备数据
     */
    List<Map<String, Object>> exportDevices(String deviceType, String roomId);

    /**
     * 获取设备统计信息
     *
     * @param roomId 机房ID
     * @return 统计信息
     */
    Map<String, Object> getDeviceStats(String roomId);

    /**
     * 获取关键设备列表
     *
     * @param roomId 机房ID
     * @return 关键设备列表
     */
    List<Device> getKeyDevices(String roomId);

    /**
     * 根据IP地址获取设备
     *
     * @param ipAddress IP地址
     * @return 设备信息
     */
    Device getDeviceByIp(String ipAddress);

    /**
     * 批量更新设备状态
     *
     * @param deviceIds 设备ID列表
     * @param status    状态
     * @return 是否成功
     */
    boolean batchUpdateStatus(List<String> deviceIds, String status);
}
