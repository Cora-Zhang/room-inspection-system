package com.roominspection.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.roominspection.backend.entity.Device;
import com.roominspection.backend.mapper.DeviceMapper;
import com.roominspection.backend.service.DeviceAssetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 设备资产管理服务实现类
 */
@Slf4j
@Service
public class DeviceAssetServiceImpl extends ServiceImpl<DeviceMapper, Device> implements DeviceAssetService {

    @Override
    public IPage<Device> queryDevicePage(Page<Device> page, String roomId, String type, String subType, String status, String deviceName, String ipAddress) {
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(roomId)) {
            wrapper.eq(Device::getRoomId, roomId);
        }
        if (StringUtils.hasText(type)) {
            wrapper.eq(Device::getType, type);
        }
        if (StringUtils.hasText(subType)) {
            wrapper.eq(Device::getSubType, subType);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Device::getStatus, status);
        }
        if (StringUtils.hasText(deviceName)) {
            wrapper.like(Device::getName, deviceName);
        }
        if (StringUtils.hasText(ipAddress)) {
            wrapper.like(Device::getIpAddress, ipAddress);
        }

        wrapper.orderByDesc(Device::getUpdatedAt);

        return page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importDevices(MultipartFile file, Long operatorId, String operatorName) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;

        try {
            // TODO: 使用EasyExcel解析Excel/CSV文件
            // 示例代码：
            // List<DeviceImportDTO> devices = EasyExcel.read(file.getInputStream()).head(DeviceImportDTO.class).sheet().doReadSync();

            // 临时返回模拟数据
            result.put("successCount", successCount);
            result.put("failCount", failCount);
            result.put("totalCount", 0);
            result.put("message", "设备导入功能待实现");

        } catch (Exception e) {
            log.error("导入设备失败", e);
            result.put("message", "导入失败: " + e.getMessage());
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> exportDevices(String deviceType, String roomId) {
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(deviceType)) {
            wrapper.eq(Device::getType, deviceType);
        }
        if (StringUtils.hasText(roomId)) {
            wrapper.eq(Device::getRoomId, roomId);
        }

        List<Device> devices = list(wrapper);
        return devices.stream().map(device -> {
            Map<String, Object> map = new HashMap<>();
            map.put("code", device.getCode());
            map.put("name", device.getName());
            map.put("type", device.getType());
            map.put("subType", device.getSubType());
            map.put("brand", device.getBrand());
            map.put("model", device.getModel());
            map.put("roomName", device.getRoomName());
            map.put("rackCode", device.getRackCode());
            map.put("uPosition", device.getUPosition());
            map.put("ipAddress", device.getIpAddress());
            map.put("status", device.getStatus());
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getDeviceStats(String roomId) {
        Map<String, Object> stats = new HashMap<>();

        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(roomId)) {
            wrapper.eq(Device::getRoomId, roomId);
        }

        List<Device> allDevices = list(wrapper);

        // 总数
        stats.put("total", allDevices.size());

        // 按类型统计
        Map<String, Long> typeStats = allDevices.stream()
                .collect(Collectors.groupingBy(Device::getType, Collectors.counting()));
        stats.put("typeStats", typeStats);

        // 按状态统计
        Map<String, Long> statusStats = allDevices.stream()
                .collect(Collectors.groupingBy(Device::getStatus, Collectors.counting()));
        stats.put("statusStats", statusStats);

        // 关键设备数量
        long keyDeviceCount = allDevices.stream()
                .filter(d -> d.getIsKeyDevice() != null && d.getIsKeyDevice() == 1)
                .count();
        stats.put("keyDeviceCount", keyDeviceCount);

        // 在线设备数量
        long onlineCount = allDevices.stream()
                .filter(d -> "ONLINE".equals(d.getStatus()))
                .count();
        stats.put("onlineCount", onlineCount);

        return stats;
    }

    @Override
    public List<Device> getKeyDevices(String roomId) {
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Device::getIsKeyDevice, 1);
        if (StringUtils.hasText(roomId)) {
            wrapper.eq(Device::getRoomId, roomId);
        }
        return list(wrapper);
    }

    @Override
    public Device getDeviceByIp(String ipAddress) {
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Device::getIpAddress, ipAddress);
        return getOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchUpdateStatus(List<String> deviceIds, String status) {
        for (String deviceId : deviceIds) {
            Device device = getById(deviceId);
            if (device != null) {
                device.setStatus(status);
                device.setUpdatedAt(LocalDateTime.now());
                updateById(device);
            }
        }
        return true;
    }
}
