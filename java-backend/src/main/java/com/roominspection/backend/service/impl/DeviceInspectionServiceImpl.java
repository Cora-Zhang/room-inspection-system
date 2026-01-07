package com.roominspection.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.roominspection.backend.entity.*;
import com.roominspection.backend.mapper.DeviceInspectionRecordMapper;
import com.roominspection.backend.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 设备巡检服务实现类
 */
@Slf4j
@Service
public class DeviceInspectionServiceImpl extends ServiceImpl<DeviceInspectionRecordMapper, DeviceInspectionRecord> implements DeviceInspectionService {

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private SnmpMonitorService snmpMonitorService;

    @Autowired
    private DeviceMetricService deviceMetricService;

    @Override
    public IPage<DeviceInspectionRecord> queryInspectionPage(Page<DeviceInspectionRecord> page, String deviceId, String roomId, String result, String inspectionType) {
        LambdaQueryWrapper<DeviceInspectionRecord> wrapper = new LambdaQueryWrapper<>();

        if (deviceId != null && !deviceId.isEmpty()) {
            wrapper.eq(DeviceInspectionRecord::getDeviceId, deviceId);
        }
        if (roomId != null && !roomId.isEmpty()) {
            wrapper.eq(DeviceInspectionRecord::getRoomId, roomId);
        }
        if (result != null && !result.isEmpty()) {
            wrapper.eq(DeviceInspectionRecord::getResult, result);
        }
        if (inspectionType != null && !inspectionType.isEmpty()) {
            wrapper.eq(DeviceInspectionRecord::getInspectionType, inspectionType);
        }

        wrapper.orderByDesc(DeviceInspectionRecord::getStartTime);

        return page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeviceInspectionRecord executeAutoInspection(String deviceId, String inspectorId) {
        Device device = deviceService.getById(deviceId);
        if (device == null) {
            throw new RuntimeException("设备不存在");
        }

        DeviceInspectionRecord record = new DeviceInspectionRecord();
        record.setCode("INS-" + System.currentTimeMillis());
        record.setDeviceId(deviceId);
        record.setDeviceCode(device.getCode());
        record.setDeviceName(device.getName());
        record.setDeviceType(device.getType());
        record.setRoomId(device.getRoomId());
        record.setRoomName(device.getRoomName());
        record.setInspectionType("AUTO");
        record.setInspectionMethod("SNMP");
        record.setStartTime(LocalDateTime.now());
        record.setInspectorId(inspectorId);

        // 采集SNMP指标
        List<DeviceMetric> metrics = snmpMonitorService.collectMetrics(device);

        // 保存指标
        deviceMetricService.saveBatch(metrics);

        // 填充巡检记录
        Double cpuUsage = metrics.stream()
                .filter(m -> "CPU_USAGE".equals(m.getMetricType()))
                .map(DeviceMetric::getMetricValue)
                .findFirst()
                .orElse(null);

        Double memoryUsage = metrics.stream()
                .filter(m -> "MEMORY_USAGE".equals(m.getMetricType()))
                .map(DeviceMetric::getMetricValue)
                .findFirst()
                .orElse(null);

        record.setCpuUsage(cpuUsage);
        record.setMemoryUsage(memoryUsage);
        record.setEndTime(LocalDateTime.now());

        // 判断结果
        String result = "NORMAL";
        if (cpuUsage != null && cpuUsage > device.getCpuThreshold()) {
            result = "WARNING";
        }
        if (memoryUsage != null && memoryUsage > device.getMemoryThreshold()) {
            result = "WARNING";
        }
        record.setResult(result);

        // 更新设备状态
        device.setCurrentCpuUsage(cpuUsage);
        device.setCurrentMemoryUsage(memoryUsage);
        device.setLastInspectionTime(LocalDateTime.now());
        deviceService.updateById(device);

        save(record);
        return record;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeviceInspectionRecord executeSnmpInspection(String deviceId, String inspectorId) {
        return executeAutoInspection(deviceId, inspectorId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeviceInspectionRecord submitPhotoInspection(String deviceId, String inspectorId, List<String> photoUrls, String remark) {
        Device device = deviceService.getById(deviceId);
        if (device == null) {
            throw new RuntimeException("设备不存在");
        }

        DeviceInspectionRecord record = new DeviceInspectionRecord();
        record.setCode("INS-PHOTO-" + System.currentTimeMillis());
        record.setDeviceId(deviceId);
        record.setDeviceCode(device.getCode());
        record.setDeviceName(device.getName());
        record.setDeviceType(device.getType());
        record.setRoomId(device.getRoomId());
        record.setRoomName(device.getRoomName());
        record.setInspectionType("MANUAL");
        record.setInspectionMethod("PHOTO");
        record.setStartTime(LocalDateTime.now());
        record.setEndTime(LocalDateTime.now());
        record.setInspectorId(inspectorId);
        record.setPhotoUrls(String.join(",", photoUrls));
        record.setRemark(remark);
        record.setResult("NORMAL");

        // TODO: OCR识别设备标签
        record.setOcrRecognized(0);

        // TODO: 为照片添加水印
        record.setHasWatermark(0);

        save(record);
        return record;
    }

    @Override
    public Map<String, Object> ocrRecognize(Long recordId) {
        Map<String, Object> result = new HashMap<>();
        DeviceInspectionRecord record = getById(recordId);

        if (record == null) {
            result.put("success", false);
            result.put("message", "巡检记录不存在");
            return result;
        }

        try {
            // TODO: 集成OCR服务识别设备标签
            // 示例代码：
            // String ocrResult = ocrService.recognize(record.getPhotoUrls());

            result.put("success", true);
            result.put("message", "OCR识别成功");
            result.put("ocrResult", "{}");

        } catch (Exception e) {
            log.error("OCR识别失败", e);
            result.put("success", false);
            result.put("message", "OCR识别失败: " + e.getMessage());
        }

        return result;
    }

    @Override
    public String addWatermark(String photoUrl, Map<String, String> watermarkInfo) {
        // TODO: 为照片添加水印
        return photoUrl;
    }

    @Override
    public List<Map<String, Object>> getPendingInspectionDevices(String inspectorId) {
        // TODO: 根据值班人员获取待巡检设备列表
        return new ArrayList<>();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchExecuteInspection(List<String> deviceIds, String inspectorId) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;

        for (String deviceId : deviceIds) {
            try {
                executeAutoInspection(deviceId, inspectorId);
                successCount++;
            } catch (Exception e) {
                log.error("批量巡检失败: deviceId={}", deviceId, e);
                failCount++;
            }
        }

        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("totalCount", deviceIds.size());
        return result;
    }
}
