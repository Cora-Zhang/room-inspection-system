package com.roominspection.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.roominspection.backend.entity.FireInspectionReminder;
import com.roominspection.backend.mapper.FireInspectionReminderMapper;
import com.roominspection.backend.service.FireInspectionReminderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 消防设施审验提醒Service实现
 */
@Service
public class FireInspectionReminderServiceImpl extends ServiceImpl<FireInspectionReminderMapper, FireInspectionReminder>
        implements FireInspectionReminderService {

    @Override
    public List<FireInspectionReminder> listByRoomId(Long roomId) {
        LambdaQueryWrapper<FireInspectionReminder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(roomId != null, FireInspectionReminder::getRoomId, roomId);
        wrapper.orderByDesc(FireInspectionReminder::getReminderDate);
        return list(wrapper);
    }

    @Override
    public List<FireInspectionReminder> listByFacilityType(Integer facilityType) {
        LambdaQueryWrapper<FireInspectionReminder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(facilityType != null, FireInspectionReminder::getFacilityType, facilityType);
        wrapper.orderByDesc(FireInspectionReminder::getReminderDate);
        return list(wrapper);
    }

    @Override
    public List<FireInspectionReminder> listByReminderDate(LocalDate reminderDate) {
        LambdaQueryWrapper<FireInspectionReminder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(reminderDate != null, FireInspectionReminder::getReminderDate, reminderDate);
        wrapper.orderByDesc(FireInspectionReminder::getReminderDate);
        return list(wrapper);
    }

    @Override
    public List<FireInspectionReminder> listUnhandledReminders() {
        LambdaQueryWrapper<FireInspectionReminder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FireInspectionReminder::getHandled, 0); // 0-未处理
        wrapper.le(FireInspectionReminder::getReminderDate, LocalDate.now());
        wrapper.orderByAsc(FireInspectionReminder::getReminderDate);
        return list(wrapper);
    }

    @Override
    @Transactional
    public void generateInspectionReminders() {
        LocalDate today = LocalDate.now();

        // 查询所有消防设施（实际应用中应该有消防设施表，这里简化处理）
        // 示例：查询所有灭火器、气体灭火系统、消防主机等设施

        // TODO: 从相关表中查询所有需要审验的消防设施
        // 这里简化处理，假设从其他地方获取到设施列表

        // 对每个设施生成审验提醒
        // 提前30天提醒
        generateReminderForDay(30, 1);
        // 提前7天提醒
        generateReminderForDay(7, 2);
        // 提前1天提醒
        generateReminderForDay(1, 3);
    }

    /**
     * 为特定天数生成提醒
     */
    private void generateReminderForDay(int daysBefore, int reminderType) {
        LocalDate reminderDate = LocalDate.now().plusDays(daysBefore);

        // 查询即将到期需要提醒的设施
        // TODO: 根据审验周期计算下次审验日期，如果等于提醒日期，则生成提醒

        // 示例：查询下次审验日期等于reminderDate的设施
        // 实际应用中需要从相关表中查询并判断

        // 这里简化处理，不做具体实现
        log.debug("生成审验提醒: daysBefore={}, reminderType={}, reminderDate={}",
                daysBefore, reminderType, reminderDate);
    }

    @Override
    @Transactional
    public void handleReminder(Long reminderId, String handler, String handleResult) {
        FireInspectionReminder reminder = getById(reminderId);
        if (reminder == null) {
            throw new RuntimeException("提醒记录不存在");
        }

        reminder.setHandled(1); // 1-已处理
        reminder.setHandler(handler);
        reminder.setHandleTime(java.time.LocalDateTime.now());
        reminder.setHandleResult(handleResult);
        reminder.setUpdateTime(java.time.LocalDateTime.now());

        updateById(reminder);
    }

    @Override
    @Transactional
    public void addFacilityInspection(Long facilityId, String facilityName, Integer facilityType,
                                      Long roomId, String facilityCode, String facilityLocation,
                                      Integer inspectionType, LocalDate lastInspectionDate,
                                      Integer inspectionPeriod) {
        if (lastInspectionDate == null || inspectionPeriod == null) {
            throw new RuntimeException("上次审验日期和审验周期不能为空");
        }

        // 计算下次审验日期
        LocalDate nextInspectionDate = lastInspectionDate.plusMonths(inspectionPeriod);

        // 生成提前30天、7天、1天的提醒
        createReminder(facilityId, facilityName, facilityType, roomId, facilityCode, facilityLocation,
                inspectionType, lastInspectionDate, inspectionPeriod, nextInspectionDate,
                30, 1, nextInspectionDate.minusDays(30));
        createReminder(facilityId, facilityName, facilityType, roomId, facilityCode, facilityLocation,
                inspectionType, lastInspectionDate, inspectionPeriod, nextInspectionDate,
                7, 2, nextInspectionDate.minusDays(7));
        createReminder(facilityId, facilityName, facilityType, roomId, facilityCode, facilityLocation,
                inspectionType, lastInspectionDate, inspectionPeriod, nextInspectionDate,
                1, 3, nextInspectionDate.minusDays(1));
    }

    /**
     * 创建提醒记录
     */
    private void createReminder(Long facilityId, String facilityName, Integer facilityType,
                               Long roomId, String facilityCode, String facilityLocation,
                               Integer inspectionType, LocalDate lastInspectionDate,
                               Integer inspectionPeriod, LocalDate nextInspectionDate,
                               int daysBefore, int reminderType, LocalDate reminderDate) {
        // 检查是否已存在该提醒
        LambdaQueryWrapper<FireInspectionReminder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FireInspectionReminder::getFacilityId, facilityId);
        wrapper.eq(FireInspectionReminder::getReminderType, reminderType);
        wrapper.eq(FireInspectionReminder::getNextInspectionDate, nextInspectionDate);
        if (count(wrapper) > 0) {
            return; // 已存在，不重复创建
        }

        FireInspectionReminder reminder = new FireInspectionReminder();
        reminder.setReminderNo("FIR-" + System.currentTimeMillis());
        reminder.setFacilityId(facilityId);
        reminder.setFacilityName(facilityName);
        reminder.setFacilityType(facilityType);
        reminder.setRoomId(roomId);
        reminder.setFacilityCode(facilityCode);
        reminder.setFacilityLocation(facilityLocation);
        reminder.setInspectionType(inspectionType);
        reminder.setLastInspectionDate(lastInspectionDate);
        reminder.setInspectionPeriod(inspectionPeriod);
        reminder.setNextInspectionDate(nextInspectionDate);
        reminder.setReminderType(reminderType);

        // 生成提醒内容
        String daysDesc = daysBefore == 1 ? "明天" : daysBefore + "天后";
        String inspectionTypeName = getInspectionTypeName(inspectionType);
        reminder.setReminderContent(String.format("%s设施审验即将到期：%s（%s），请预约服务。",
                daysDesc, facilityName, inspectionTypeName));

        reminder.setReminderDate(reminderDate);
        reminder.setHandled(0); // 0-未处理
        reminder.setCreateTime(java.time.LocalDateTime.now());

        save(reminder);

        log.info("生成消防设施审验提醒: facilityName={}, reminderDate={}, reminderType={}",
                facilityName, reminderDate, reminderType);
    }

    private String getInspectionTypeName(Integer inspectionType) {
        switch (inspectionType) {
            case 1: return "年度检查";
            case 2: return "充装检查";
            case 3: return "专业检测";
            case 4: return "法定审验";
            default: return "审验";
        }
    }
}
