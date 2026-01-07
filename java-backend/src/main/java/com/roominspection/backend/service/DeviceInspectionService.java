package com.roominspection.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.roominspection.backend.entity.DeviceInspectionRecord;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

/**
 * 设备巡检服务接口
 */
public interface DeviceInspectionService extends IService<DeviceInspectionRecord> {

    /**
     * 分页查询巡检记录
     *
     * @param page         分页对象
     * @param deviceId     设备ID
     * @param roomId       机房ID
     * @param result       巡检结果
     * @param inspectionType 巡检类型
     * @return 分页结果
     */
    IPage<DeviceInspectionRecord> queryInspectionPage(Page<DeviceInspectionRecord> page, String deviceId, String roomId, String result, String inspectionType);

    /**
     * 执行自动巡检
     *
     * @param deviceId 设备ID
     * @param inspectorId 巡检人ID
     * @return 巡检记录
     */
    DeviceInspectionRecord executeAutoInspection(String deviceId, String inspectorId);

    /**
     * 执行SNMP巡检
     *
     * @param deviceId 设备ID
     * @param inspectorId 巡检人ID
     * @return 巡检记录
     */
    DeviceInspectionRecord executeSnmpInspection(String deviceId, String inspectorId);

    /**
     * 提交拍照巡检
     *
     * @param deviceId     设备ID
     * @param inspectorId  巡检人ID
     * @param photoUrls    照片URL列表
     * @param remark       备注
     * @return 巡检记录
     */
    DeviceInspectionRecord submitPhotoInspection(String deviceId, String inspectorId, List<String> photoUrls, String remark);

    /**
     * OCR识别设备标签
     *
     * @param recordId 巡检记录ID
     * @return 识别结果
     */
    Map<String, Object> ocrRecognize(Long recordId);

    /**
     * 为照片添加水印
     *
     * @param photoUrl     照片URL
     * @param watermarkInfo 水印信息
     * @return 处理后的照片URL
     */
    String addWatermark(String photoUrl, Map<String, String> watermarkInfo);

    /**
     * 获取待巡检设备列表
     *
     * @param inspectorId 巡检人ID
     * @return 设备列表
     */
    List<Map<String, Object>> getPendingInspectionDevices(String inspectorId);

    /**
     * 批量执行巡检
     *
     * @param deviceIds   设备ID列表
     * @param inspectorId 巡检人ID
     * @return 巡检结果
     */
    Map<String, Object> batchExecuteInspection(List<String> deviceIds, String inspectorId);
}
