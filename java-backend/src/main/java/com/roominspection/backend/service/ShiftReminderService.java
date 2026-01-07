package com.roominspection.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.roominspection.backend.entity.ShiftHandover;
import com.roominspection.backend.entity.ShiftSchedule;
import com.roominspection.backend.entity.Staff;
import com.roominspection.backend.mapper.ShiftHandoverMapper;
import com.roominspection.backend.mapper.ShiftScheduleMapper;
import com.roominspection.backend.mapper.StaffMapper;
import com.roominspection.backend.util.DingTalkSyncUtil;
import com.roominspection.backend.util.SmsUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 值班提醒服务
 * 负责值班前提醒、交接提醒、异常告警等定时任务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShiftReminderService extends ServiceImpl<ShiftHandoverMapper, ShiftHandover> implements IService<ShiftHandover> {

    private final ShiftScheduleMapper shiftScheduleMapper;
    private final ShiftHandoverMapper shiftHandoverMapper;
    private final StaffMapper staffMapper;
    private final DingTalkSyncUtil dingTalkSyncUtil;
    private final SmsUtil smsUtil;

    /**
     * 定时任务：值班前1小时发送提醒
     * 每10分钟执行一次
     */
    @Scheduled(cron = "0 */10 * * * ?")
    public void sendShiftPreReminder() {
        try {
            log.info("开始执行值班前提醒任务");

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime oneHourLater = now.plusHours(1);

            // 查询1小时后开始的值班
            java.time.LocalDate scheduleDate = oneHourLater.toLocalDate();
            List<ShiftSchedule> schedules = shiftScheduleMapper.findByScheduleDate(scheduleDate);

            for (ShiftSchedule schedule : schedules) {
                try {
                    // 判断是否需要发送提醒（根据班次开始时间）
                    boolean shouldRemind = false;

                    if ("DAY".equals(schedule.getShift())) {
                        // 白班8:00开始
                        LocalDateTime shiftStartTime = schedule.getScheduleDate().atTime(8, 0);
                        if (now.isAfter(shiftStartTime.minusHours(1)) && now.isBefore(shiftStartTime)) {
                            shouldRemind = true;
                        }
                    } else {
                        // 夜班18:00开始
                        LocalDateTime shiftStartTime = schedule.getScheduleDate().atTime(18, 0);
                        if (now.isAfter(shiftStartTime.minusHours(1)) && now.isBefore(shiftStartTime)) {
                            shouldRemind = true;
                        }
                    }

                    if (shouldRemind) {
                        // 查询值班人员信息
                        Staff staff = staffMapper.selectById(schedule.getStaffId());

                        if (staff != null && staff.getPhone() != null) {
                            // 发送钉钉提醒
                            dingTalkSyncUtil.sendShiftReminder(
                                    List.of(staff.getId()),
                                    staff.getName(),
                                    schedule.getScheduleDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                                    schedule.getShift(),
                                    schedule.getRoomName()
                            );

                            // 发送短信提醒
                            smsUtil.sendShiftReminder(
                                    List.of(staff.getPhone()),
                                    staff.getName(),
                                    schedule.getScheduleDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                                    schedule.getShift(),
                                    schedule.getRoomName()
                            );

                            log.info("值班前提醒发送成功，值班人员={}", staff.getName());
                        }
                    }

                } catch (Exception e) {
                    log.error("发送值班前提醒失败，排班ID={}", schedule.getId(), e);
                }
            }

            log.info("值班前提醒任务执行完成");

        } catch (Exception e) {
            log.error("值班前提醒任务执行异常", e);
        }
    }

    /**
     * 定时任务：交接前30分钟发送提醒
     * 每5分钟执行一次
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void sendHandoverReminder() {
        try {
            log.info("开始执行交接前提醒任务");

            LocalDateTime now = LocalDateTime.now();

            // 查询今日需要交接的排班
            java.time.LocalDate today = now.toLocalDate();
            List<ShiftSchedule> todaySchedules = shiftScheduleMapper.findByScheduleDate(today);

            for (ShiftSchedule schedule : todaySchedules) {
                try {
                    LocalDateTime handoverTime;

                    // 计算交接时间
                    if ("DAY".equals(schedule.getShift())) {
                        // 白班17:00交接
                        handoverTime = schedule.getScheduleDate().atTime(17, 0);
                    } else {
                        // 夜班次日07:00交接
                        handoverTime = schedule.getScheduleDate().plusDays(1).atTime(7, 0);
                    }

                    // 判断是否在交接前30分钟内
                    if (now.isAfter(handoverTime.minusMinutes(30)) && now.isBefore(handoverTime)) {
                        // 查询交接记录
                        ShiftHandover handover = shiftHandoverMapper.selectOne(
                                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ShiftHandover>()
                                        .eq(ShiftHandover::getHandoverDate, today)
                                        .eq(ShiftHandover::getShift, schedule.getShift())
                                        .eq(ShiftHandover::getOutgoingStaffId, schedule.getStaffId())
                        );

                        if (handover != null && !handover.getIsReminded()) {
                            // 查询值班人员信息
                            Staff staff = staffMapper.selectById(schedule.getStaffId());

                            if (staff != null && staff.getPhone() != null) {
                                // 发送钉钉提醒
                                dingTalkSyncUtil.sendHandoverReminder(
                                        List.of(staff.getId()),
                                        staff.getName()
                                );

                                // 发送短信提醒
                                smsUtil.sendHandoverReminder(
                                        List.of(staff.getPhone()),
                                        staff.getName()
                                );

                                // 更新提醒状态
                                handover.setIsReminded(true);
                                handover.setRemindTime(now);
                                shiftHandoverMapper.updateById(handover);

                                log.info("交接前提醒发送成功，值班人员={}", staff.getName());
                            }
                        }
                    }

                } catch (Exception e) {
                    log.error("发送交接前提醒失败，排班ID={}", schedule.getId(), e);
                }
            }

            log.info("交接前提醒任务执行完成");

        } catch (Exception e) {
            log.error("交接前提醒任务执行异常", e);
        }
    }

    /**
     * 定时任务：检测值班异常（未按时到岗）
     * 每15分钟执行一次
     */
    @Scheduled(cron = "0 */15 * * * ?")
    public void detectShiftAbnormal() {
        try {
            log.info("开始执行值班异常检测任务");

            LocalDateTime now = LocalDateTime.now();
            java.time.LocalDate today = now.toLocalDate();

            // 查询今日排班
            List<ShiftSchedule> todaySchedules = shiftScheduleMapper.findByScheduleDate(today);

            for (ShiftSchedule schedule : todaySchedules) {
                try {
                    LocalDateTime shiftStartTime;

                    // 计算值班开始时间
                    if ("DAY".equals(schedule.getShift())) {
                        shiftStartTime = schedule.getScheduleDate().atTime(8, 0);
                    } else {
                        shiftStartTime = schedule.getScheduleDate().atTime(18, 0);
                    }

                    // 判断是否已超过值班开始时间30分钟
                    if (now.isAfter(shiftStartTime.plusMinutes(30))) {
                        // 查询交接记录
                        ShiftHandover handover = shiftHandoverMapper.selectOne(
                                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ShiftHandover>()
                                        .eq(ShiftHandover::getHandoverDate, today)
                                        .eq(ShiftHandover::getShift, schedule.getShift())
                                        .eq(ShiftHandover::getIncomingStaffId, schedule.getStaffId())
                        );

                        if (handover != null && !handover.getIsAbnormal() && handover.getIncomingTime() == null) {
                            // 标记为异常
                            handover.setIsAbnormal(true);
                            handover.setAbnormalReason("未按时到岗，超过30分钟未刷卡进入机房");
                            shiftHandoverMapper.updateById(handover);

                            // 查询值班人员信息
                            Staff staff = staffMapper.selectById(schedule.getStaffId());

                            if (staff != null) {
                                // 查询管理员（发送告警）
                                List<Staff> admins = staffMapper.selectList(
                                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Staff>()
                                                .eq(Staff::getPosition, "管理员")
                                );

                                List<String> adminIds = admins.stream()
                                        .map(Staff::getId)
                                        .collect(Collectors.toList());

                                List<String> adminPhones = admins.stream()
                                        .filter(s -> s.getPhone() != null)
                                        .map(Staff::getPhone)
                                        .collect(Collectors.toList());

                                // 发送钉钉告警
                                if (!adminIds.isEmpty()) {
                                    dingTalkSyncUtil.sendShiftAbnormalAlert(
                                            adminIds,
                                            staff.getName(),
                                            handover.getAbnormalReason()
                                    );
                                }

                                // 发送短信告警
                                if (!adminPhones.isEmpty()) {
                                    smsUtil.sendShiftAbnormalAlert(
                                            adminPhones,
                                            staff.getName(),
                                            handover.getAbnormalReason()
                                    );
                                }

                                log.info("值班异常告警发送成功，值班人员={}，原因={}",
                                        staff.getName(), handover.getAbnormalReason());
                            }
                        }
                    }

                } catch (Exception e) {
                    log.error("检测值班异常失败，排班ID={}", schedule.getId(), e);
                }
            }

            log.info("值班异常检测任务执行完成");

        } catch (Exception e) {
            log.error("值班异常检测任务执行异常", e);
        }
    }

    /**
     * 定时任务：自动回收过期门禁权限
     * 每小时执行一次
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void autoRevokeExpiredPermissions() {
        try {
            log.info("开始执行过期门禁权限回收任务");

            DoorAccessPermissionService permissionService =
                    com.roominspection.backend.util.SpringContextHolder.getBean(DoorAccessPermissionService.class);

            // TODO: 查询所有生效中但已过期的权限并回收

            log.info("过期门禁权限回收任务执行完成");

        } catch (Exception e) {
            log.error("过期门禁权限回收任务执行异常", e);
        }
    }

    /**
     * 定时任务：更新权限状态（未生效 -> 生效中 -> 已过期）
     * 每小时执行一次
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void updatePermissionStatus() {
        try {
            log.info("开始执行门禁权限状态更新任务");

            LocalDateTime now = LocalDateTime.now();

            // TODO: 更新门禁权限状态
            // 1. 当前时间在有效期内，状态从0(未生效)更新为1(生效中)
            // 2. 当前时间超过结束时间，状态从1(生效中)更新为2(已过期)

            log.info("门禁权限状态更新任务执行完成");

        } catch (Exception e) {
            log.error("门禁权限状态更新任务执行异常", e);
        }
    }
}
