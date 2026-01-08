package com.roominspection.backend.accesscontrol;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 门禁系统工厂类
 * 用于创建不同厂商的门禁系统适配器
 */
@Component
public class DoorAccessSystemFactory {

    /**
     * 创建门禁系统适配器
     *
     * @param manufacturer 门禁厂商
     * @return 门禁系统适配器
     */
    public DoorAccessSystem createDoorAccessSystem(String manufacturer) {
        if (manufacturer == null) {
            throw new IllegalArgumentException("门禁厂商不能为空");
        }

        switch (manufacturer.toUpperCase()) {
            case "HIKVISION":
            case "HIK":
                return new HikvisionDoorAccessSystem();
            case "DAHUA":
                return new DahuaDoorAccessSystem();
            case "UNIVIEW":
            case "UNV":
                return new UniviewDoorAccessSystem();
            default:
                throw new IllegalArgumentException("不支持的门禁厂商: " + manufacturer);
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
            case "HIKVISION":
            case "HIK":
            case "DAHUA":
            case "UNIVIEW":
            case "UNV":
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
                "HIKVISION",
                "DAHUA",
                "UNIVIEW"
        };
    }
}
