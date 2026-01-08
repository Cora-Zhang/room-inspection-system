package com.roominspection.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.roominspection.backend.entity.FireInspectionReminder;
import java.time.LocalDate;
import java.util.List;

/**
 * 消防设施审验提醒Service
 */
public interface FireInspectionReminderService extends IService<FireInspectionReminder> {

    /**
     * 根据机房ID查询提醒列表
     */
    List<FireInspectionReminder> listByRoomId(Long roomId);

    /**
     * 根据设施类型查询提醒列表
     */
    List<FireInspectionReminder> listByFacilityType(Integer facilityType);

    /**
     * 根据提醒日期查询提醒列表
     */
    List<FireInspectionReminder> listByReminderDate(LocalDate reminderDate);

    /**
     * 查询未处理的提醒
     */
    List<FireInspectionReminder> listUnhandledReminders();

    /**
     * 生成审验提醒（定时任务调用）
     */
    void generateInspectionReminders();

    /**
     * 处理提醒
     */
    void handleReminder(Long reminderId, String handler, String handleResult);

    /**
     * 添加消防设施审验信息
     */
    void addFacilityInspection(Long facilityId, String facilityName, Integer facilityType,
                               Long roomId, String facilityCode, String facilityLocation,
                               Integer inspectionType, LocalDate lastInspectionDate,
                               Integer inspectionPeriod);
}
