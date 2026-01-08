package com.roominspection.backend.protocol;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 设备协议工厂类
 * 用于创建不同厂商的设备协议适配器
 */
@Slf4j
@Component
public class DeviceProtocolFactory {

    /**
     * 创建设备协议适配器
     *
     * @param manufacturer 设备厂商
     * @return 设备协议适配器
     */
    public DeviceProtocol createProtocol(String manufacturer) {
        if (manufacturer == null) {
            throw new IllegalArgumentException("设备厂商不能为空");
        }

        switch (manufacturer.toUpperCase()) {
            case "APC":
                return new APCProtocolAdapter();
            case "SCHNEIDER":
                return new SchneiderProtocolAdapter();
            case "EMERSON":
                return new EmersonProtocolAdapter();
            case "HUAWEI":
                return new HuaweiProtocolAdapter();
            case "VERTIV":
                return new VertivProtocolAdapter();
            case "RITTAL":
                return new RittalProtocolAdapter();
            default:
                throw new IllegalArgumentException("不支持的设备厂商: " + manufacturer);
        }
    }

    /**
     * 检查是否支持指定厂商
     */
    public boolean isSupported(String manufacturer) {
        if (manufacturer == null) {
            return false;
        }

        switch (manufacturer.toUpperCase()) {
            case "APC":
            case "SCHNEIDER":
            case "EMERSON":
            case "HUAWEI":
            case "VERTIV":
            case "RITTAL":
                return true;
            default:
                return false;
        }
    }

    /**
     * 获取支持的厂商列表
     */
    public String[] getSupportedManufacturers() {
        return new String[]{
                "APC",
                "SCHNEIDER",
                "EMERSON",
                "HUAWEI",
                "VERTIV",
                "RITTAL"
        };
    }
}
