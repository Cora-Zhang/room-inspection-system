package com.roominspection.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.roominspection.backend.dto.ShiftScheduleImportDTO;
import com.roominspection.backend.entity.Room;
import com.roominspection.backend.entity.ShiftSchedule;
import com.roominspection.backend.entity.Staff;
import com.roominspection.backend.mapper.RoomMapper;
import com.roominspection.backend.mapper.ShiftScheduleMapper;
import com.roominspection.backend.mapper.StaffMapper;
import com.roominspection.backend.util.DingTalkSyncUtil;
import com.roominspection.backend.util.ExcelImportUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 值班排班服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShiftScheduleService extends ServiceImpl<ShiftScheduleMapper, ShiftSchedule> implements IService<ShiftSchedule> {

    private final ShiftScheduleMapper shiftScheduleMapper;
    private final StaffMapper staffMapper;
    private final RoomMapper roomMapper;
    private final ExcelImportUtil excelImportUtil;
    private final DingTalkSyncUtil dingTalkSyncUtil;

    /**
     * 查询排班列表
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param roomId 机房ID
     * @param staffId 值班人员ID
     * @return 排班列表
     */
    public List<ShiftSchedule> getScheduleList(LocalDate startDate, LocalDate endDate, Long roomId, String staffId) {
        List<ShiftSchedule> result = new ArrayList<>();

        if (startDate != null && endDate != null) {
            result = shiftScheduleMapper.findByDateRange(startDate, endDate);
        } else if (startDate != null) {
            result = shiftScheduleMapper.findByScheduleDate(startDate);
        } else {
            // 查询未来30天的排班
            LocalDate today = LocalDate.now();
            LocalDate futureDate = today.plusDays(30);
            result = shiftScheduleMapper.findByDateRange(today, futureDate);
        }

        // 按机房过滤
        if (roomId != null) {
            result = result.stream()
                    .filter(s -> roomId.equals(s.getRoomId()))
                    .collect(Collectors.toList());
        }

        // 按值班人员过滤
        if (staffId != null && !staffId.isEmpty()) {
            result = result.stream()
                    .filter(s -> staffId.equals(s.getStaffId()))
                    .collect(Collectors.toList());
        }

        return result;
    }

    /**
     * 创建排班
     *
     * @param schedule 排班信息
     * @return 是否创建成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean createSchedule(ShiftSchedule schedule) {
        try {
            // 验证值班人员是否存在
            Staff staff = staffMapper.selectById(schedule.getStaffId());
            if (staff == null) {
                throw new RuntimeException("值班人员不存在");
            }

            // 验证机房是否存在
            if (schedule.getRoomId() != null) {
                Room room = roomMapper.selectById(schedule.getRoomId());
                if (room == null) {
                    throw new RuntimeException("机房不存在");
                }
                schedule.setRoomName(room.getName());
            }

            // 设置默认值
            schedule.setId(UUID.randomUUID().toString());
            schedule.setStaffName(staff.getName());
            schedule.setStatus("SCHEDULED");
            schedule.setDataSource(1); // 手动创建
            schedule.setCreateTime(LocalDateTime.now());
            schedule.setUpdateTime(LocalDateTime.now());

            int result = shiftScheduleMapper.insert(schedule);

            // 自动创建门禁权限
            if (result > 0 && schedule.getRoomId() != null) {
                createDoorAccessPermission(schedule);
            }

            return result > 0;
        } catch (Exception e) {
            log.error("创建排班失败", e);
            throw new RuntimeException("创建排班失败: " + e.getMessage());
        }
    }

    /**
     * 批量创建排班
     *
     * @param schedules 排班列表
     * @return 是否创建成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean batchCreateSchedules(List<ShiftSchedule> schedules) {
        try {
            for (ShiftSchedule schedule : schedules) {
                createSchedule(schedule);
            }
            return true;
        } catch (Exception e) {
            log.error("批量创建排班失败", e);
            throw new RuntimeException("批量创建排班失败: " + e.getMessage());
        }
    }

    /**
     * 更新排班
     *
     * @param schedule 排班信息
     * @return 是否更新成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSchedule(ShiftSchedule schedule) {
        try {
            schedule.setUpdateTime(LocalDateTime.now());
            int result = shiftScheduleMapper.updateById(schedule);

            // 更新门禁权限
            if (result > 0 && schedule.getRoomId() != null) {
                updateDoorAccessPermission(schedule);
            }

            return result > 0;
        } catch (Exception e) {
            log.error("更新排班失败", e);
            throw new RuntimeException("更新排班失败: " + e.getMessage());
        }
    }

    /**
     * 删除排班
     *
     * @param id 排班ID
     * @return 是否删除成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSchedule(String id) {
        try {
            // 回收门禁权限
            revokeDoorAccessPermissionBySchedule(id);

            return shiftScheduleMapper.deleteById(id) > 0;
        } catch (Exception e) {
            log.error("删除排班失败", e);
            throw new RuntimeException("删除排班失败: " + e.getMessage());
        }
    }

    /**
     * Excel导入排班
     *
     * @param file Excel文件
     * @return 导入结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importFromExcel(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;

        try {
            String importBatch = UUID.randomUUID().toString();
            List<ShiftScheduleImportDTO> dataList = excelImportUtil.parseShiftScheduleExcel(file);

            for (ShiftScheduleImportDTO dto : dataList) {
                try {
                    // 查找值班人员
                    Staff staff = staffMapper.selectOne(
                            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Staff>()
                                    .eq(Staff::getEmployeeId, dto.getStaffNo())
                    );

                    if (staff == null) {
                        errors.add(String.format("值班人员工号%s不存在", dto.getStaffNo()));
                        failCount++;
                        continue;
                    }

                    // 查找机房
                    Room room = roomMapper.selectOne(
                            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Room>()
                                    .eq(Room::getName, dto.getRoomName())
                    );

                    if (room == null) {
                        errors.add(String.format("机房%s不存在", dto.getRoomName()));
                        failCount++;
                        continue;
                    }

                    // 创建排班记录
                    ShiftSchedule schedule = new ShiftSchedule();
                    schedule.setId(UUID.randomUUID().toString());
                    schedule.setScheduleDate(dto.getScheduleDate());
                    schedule.setShift(dto.getShift());
                    schedule.setStaffId(staff.getId());
                    schedule.setStaffName(staff.getName());
                    schedule.setRoomId(room.getId());
                    schedule.setRoomName(room.getName());
                    schedule.setStatus("SCHEDULED");
                    schedule.setDataSource(2); // Excel导入
                    schedule.setImportBatch(importBatch);
                    schedule.setSyncDingtalkStatus(0);
                    schedule.setCreateTime(LocalDateTime.now());
                    schedule.setUpdateTime(LocalDateTime.now());

                    shiftScheduleMapper.insert(schedule);

                    // 自动创建门禁权限
                    createDoorAccessPermission(schedule);

                    successCount++;

                } catch (Exception e) {
                    errors.add(String.format("导入第%d条数据失败: %s", successCount + failCount + 1, e.getMessage()));
                    failCount++;
                }
            }

            result.put("success", true);
            result.put("total", dataList.size());
            result.put("successCount", successCount);
            result.put("failCount", failCount);
            result.put("errors", errors);
            result.put("importBatch", importBatch);

        } catch (Exception e) {
            log.error("Excel导入失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * 周期性排班
     *
     * @param staffId 值班人员ID
     * @param roomId 机房ID
     * @param shift 班次
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param scheduleType 排班类型（1-每周轮换 2-每月轮换 3-季度轮换）
     * @return 排班结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> createPeriodicSchedule(String staffId, Long roomId, String shift,
                                                        LocalDate startDate, LocalDate endDate, Integer scheduleType) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 验证值班人员
            Staff staff = staffMapper.selectById(staffId);
            if (staff == null) {
                throw new RuntimeException("值班人员不存在");
            }

            // 验证机房
            Room room = roomMapper.selectById(roomId);
            if (room == null) {
                throw new RuntimeException("机房不存在");
            }

            List<ShiftSchedule> schedules = new ArrayList<>();
            LocalDate currentDate = startDate;

            // 根据排班类型生成排班
            int intervalDays = 7; // 默认每周
            if (scheduleType == 2) {
                intervalDays = 30; // 每月
            } else if (scheduleType == 3) {
                intervalDays = 90; // 每季度
            }

            while (!currentDate.isAfter(endDate)) {
                ShiftSchedule schedule = new ShiftSchedule();
                schedule.setId(UUID.randomUUID().toString());
                schedule.setScheduleDate(currentDate);
                schedule.setShift(shift);
                schedule.setStaffId(staffId);
                schedule.setStaffName(staff.getName());
                schedule.setRoomId(roomId);
                schedule.setRoomName(room.getName());
                schedule.setStatus("SCHEDULED");
                schedule.setDataSource(1); // 手动创建
                schedule.setScheduleType(scheduleType);
                schedule.setCreateTime(LocalDateTime.now());
                schedule.setUpdateTime(LocalDateTime.now());

                schedules.add(schedule);
                currentDate = currentDate.plusDays(intervalDays);
            }

            // 批量插入
            for (ShiftSchedule schedule : schedules) {
                shiftScheduleMapper.insert(schedule);
                createDoorAccessPermission(schedule);
            }

            result.put("success", true);
            result.put("count", schedules.size());
            result.put("message", String.format("成功创建%d条周期性排班记录", schedules.size()));

        } catch (Exception e) {
            log.error("创建周期性排班失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * 从钉钉多维表同步排班
     *
     * @param appId 钉钉多维表应用ID
     * @param tableId 表ID
     * @return 同步结果
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> syncFromDingTalk(String appId, String tableId) {
        Map<String, Object> result = new HashMap<>();

        try {
            Map<String, Object> syncResult = dingTalkSyncUtil.syncFromDingTalk(appId, tableId);

            if (!(Boolean) syncResult.get("success")) {
                result.put("success", false);
                result.put("error", syncResult.get("error"));
                return result;
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> shiftList = (List<Map<String, Object>>) syncResult.get("data");

            int successCount = 0;
            int failCount = 0;

            for (Map<String, Object> shiftData : shiftList) {
                try {
                    // 解析钉钉数据并创建排班
                    ShiftSchedule schedule = parseDingTalkShiftData(shiftData);
                    shiftScheduleMapper.insert(schedule);
                    createDoorAccessPermission(schedule);
                    successCount++;

                } catch (Exception e) {
                    log.error("同步钉钉数据失败", e);
                    failCount++;
                }
            }

            result.put("success", true);
            result.put("total", shiftList.size());
            result.put("successCount", successCount);
            result.put("failCount", failCount);
            result.put("syncTime", syncResult.get("syncTime"));

        } catch (Exception e) {
            log.error("钉钉同步失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * 解析钉钉排班数据
     *
     * @param shiftData 钉钉数据
     * @return 排班对象
     */
    private ShiftSchedule parseDingTalkShiftData(Map<String, Object> shiftData) {
        ShiftSchedule schedule = new ShiftSchedule();

        schedule.setId(UUID.randomUUID().toString());
        schedule.setScheduleDate(LocalDate.parse((String) shiftData.get("scheduleDate")));
        schedule.setShift((String) shiftData.get("shift"));

        // 查找值班人员
        Staff staff = staffMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Staff>()
                        .eq(Staff::getEmployeeId, shiftData.get("staffNo"))
        );

        if (staff != null) {
            schedule.setStaffId(staff.getId());
            schedule.setStaffName(staff.getName());
        }

        // 查找机房
        Room room = roomMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Room>()
                        .eq(Room::getName, shiftData.get("roomName"))
        );

        if (room != null) {
            schedule.setRoomId(room.getId());
            schedule.setRoomName(room.getName());
        }

        schedule.setStatus("SCHEDULED");
        schedule.setDataSource(3); // 钉钉同步
        schedule.setSyncDingtalkStatus(1); // 已同步
        schedule.setDingtalkTaskId(UUID.randomUUID().toString());
        schedule.setCreateTime(LocalDateTime.now());
        schedule.setUpdateTime(LocalDateTime.now());

        return schedule;
    }

    /**
     * 创建门禁权限
     *
     * @param schedule 排班信息
     */
    private void createDoorAccessPermission(ShiftSchedule schedule) {
        try {
            DoorAccessPermissionService doorAccessPermissionService =
                    SpringContextHolder.getBean(DoorAccessPermissionService.class);

            // 计算值班时间
            LocalDateTime effectiveStartTime;
            LocalDateTime effectiveEndTime;

            if ("DAY".equals(schedule.getShift())) {
                effectiveStartTime = schedule.getScheduleDate().atTime(8, 0);
                effectiveEndTime = schedule.getScheduleDate().atTime(17, 0);
            } else {
                // 夜班（跨天）
                effectiveStartTime = schedule.getScheduleDate().atTime(18, 0);
                effectiveEndTime = schedule.getScheduleDate().plusDays(1).atTime(7, 0);
            }

            doorAccessPermissionService.createPermission(
                    schedule.getStaffId(),
                    schedule.getStaffName(),
                    schedule.getRoomId(),
                    schedule.getRoomName(),
                    effectiveStartTime,
                    effectiveEndTime
            );

        } catch (Exception e) {
            log.error("创建门禁权限失败", e);
        }
    }

    /**
     * 更新门禁权限
     *
     * @param schedule 排班信息
     */
    private void updateDoorAccessPermission(ShiftSchedule schedule) {
        try {
            DoorAccessPermissionService doorAccessPermissionService =
                    SpringContextHolder.getBean(DoorAccessPermissionService.class);

            doorAccessPermissionService.updatePermissionBySchedule(schedule);

        } catch (Exception e) {
            log.error("更新门禁权限失败", e);
        }
    }

    /**
     * 回收门禁权限
     *
     * @param scheduleId 排班ID
     */
    private void revokeDoorAccessPermissionBySchedule(String scheduleId) {
        try {
            DoorAccessPermissionService doorAccessPermissionService =
                    SpringContextHolder.getBean(DoorAccessPermissionService.class);

            doorAccessPermissionService.revokePermissionBySchedule(scheduleId, "排班已删除");

        } catch (Exception e) {
            log.error("回收门禁权限失败", e);
        }
    }
}
