package com.roominspection.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.roominspection.backend.entity.Device;
import com.roominspection.backend.entity.DoorAccessPermission;
import com.roominspection.backend.entity.ShiftSchedule;
import com.roominspection.backend.entity.Staff;
import com.roominspection.backend.mapper.DeviceMapper;
import com.roominspection.backend.mapper.DoorAccessPermissionMapper;
import com.roominspection.backend.mapper.ShiftScheduleMapper;
import com.roominspection.backend.mapper.StaffMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 门禁权限服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DoorAccessPermissionService extends ServiceImpl<DoorAccessPermissionMapper, DoorAccessPermission> implements IService<DoorAccessPermission> {

    private final DoorAccessPermissionMapper doorAccessPermissionMapper;
    private final StaffMapper staffMapper;
    private final DeviceMapper deviceMapper;
    private final ShiftScheduleMapper shiftScheduleMapper;

    /**
     * 创建门禁权限
     *
     * @param staffId 值班人员ID
     * @param staffName 值班人员姓名
     * @param roomId 机房ID
     * @param roomName 机房名称
     * @param effectiveStartTime 生效开始时间
     * @param effectiveEndTime 生效结束时间
     * @return 是否创建成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean createPermission(String staffId, String staffName, Long roomId, String roomName,
                                    LocalDateTime effectiveStartTime, LocalDateTime effectiveEndTime) {
        try {
            // 查找值班人员
            Staff staff = staffMapper.selectById(staffId);
            if (staff == null) {
                throw new RuntimeException("值班人员不存在");
            }

            // 查找门禁设备
            Device doorDevice = deviceMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Device>()
                            .eq(Device::getRoomId, roomId.toString())
                            .eq(Device::getType, "DOOR_ACCESS")
            );

            if (doorDevice == null) {
                log.warn("机房{}未配置门禁设备", roomName);
                return false;
            }

            // 创建权限记录
            DoorAccessPermission permission = new DoorAccessPermission();
            permission.setStaffId(Long.parseLong(staffId));
            permission.setStaffName(staffName);
            permission.setStaffNo(staff.getEmployeeId());
            permission.setRoomId(roomId);
            permission.setRoomName(roomName);
            permission.setDeviceId(doorDevice.getAccessControlDeviceId());
            permission.setCardNo(staff.getEmployeeId()); // 使用工号作为卡号
            permission.setPermissionType(1); // 值班权限
            permission.setEffectiveStartTime(effectiveStartTime);
            permission.setEffectiveEndTime(effectiveEndTime);
            permission.setStatus(0); // 未生效
            permission.setSyncStatus(0); // 未下发
            permission.setDoorSystemType(doorDevice.getBrand().contains("海康") ? 1 : 2);
            permission.setCreateTime(LocalDateTime.now());
            permission.setUpdateTime(LocalDateTime.now());

            int result = doorAccessPermissionMapper.insert(permission);

            // 异步下发到门禁系统
            if (result > 0) {
                syncPermissionToDoorSystem(permission.getId());
            }

            return result > 0;

        } catch (Exception e) {
            log.error("创建门禁权限失败", e);
            return false;
        }
    }

    /**
     * 根据排班更新门禁权限
     *
     * @param schedule 排班信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void updatePermissionBySchedule(ShiftSchedule schedule) {
        try {
            // 查找关联的门禁权限
            DoorAccessPermission permission = doorAccessPermissionMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DoorAccessPermission>()
                            .eq(DoorAccessPermission::getStaffId, Long.parseLong(schedule.getStaffId()))
                            .eq(DoorAccessPermission::getRoomId, schedule.getRoomId())
                            .eq(DoorAccessPermission::getPermissionType, 1)
                            .eq(DoorAccessPermission::getStatus, 0)
            );

            if (permission != null) {
                // 更新权限时间
                LocalDateTime effectiveStartTime;
                LocalDateTime effectiveEndTime;

                if ("DAY".equals(schedule.getShift())) {
                    effectiveStartTime = schedule.getScheduleDate().atTime(8, 0);
                    effectiveEndTime = schedule.getScheduleDate().atTime(17, 0);
                } else {
                    effectiveStartTime = schedule.getScheduleDate().atTime(18, 0);
                    effectiveEndTime = schedule.getScheduleDate().plusDays(1).atTime(7, 0);
                }

                permission.setEffectiveStartTime(effectiveStartTime);
                permission.setEffectiveEndTime(effectiveEndTime);
                permission.setUpdateTime(LocalDateTime.now());

                doorAccessPermissionMapper.updateById(permission);

                // 重新下发权限
                syncPermissionToDoorSystem(permission.getId());
            }

        } catch (Exception e) {
            log.error("更新门禁权限失败", e);
        }
    }

    /**
     * 根据排班ID回收门禁权限
     *
     * @param scheduleId 排班ID
     * @param reason 回收原因
     */
    @Transactional(rollbackFor = Exception.class)
    public void revokePermissionBySchedule(String scheduleId, String reason) {
        try {
            ShiftSchedule schedule = shiftScheduleMapper.selectById(scheduleId);

            if (schedule != null && schedule.getRoomId() != null) {
                // 查找关联的门禁权限
                DoorAccessPermission permission = doorAccessPermissionMapper.selectOne(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DoorAccessPermission>()
                                .eq(DoorAccessPermission::getStaffId, Long.parseLong(schedule.getStaffId()))
                                .eq(DoorAccessPermission::getRoomId, schedule.getRoomId())
                                .eq(DoorAccessPermission::getPermissionType, 1)
                                .in(DoorAccessPermission::getStatus, 0, 1)
                );

                if (permission != null) {
                    revokePermission(permission.getId(), reason);
                }
            }

        } catch (Exception e) {
            log.error("回收门禁权限失败", e);
        }
    }

    /**
     * 回收门禁权限
     *
     * @param permissionId 权限ID
     * @param reason 回收原因
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean revokePermission(Long permissionId, String reason) {
        try {
            DoorAccessPermission permission = doorAccessPermissionMapper.selectById(permissionId);

            if (permission == null) {
                return false;
            }

            // 调用门禁系统API回收权限
            boolean revokeResult = revokePermissionFromDoorSystem(permission);

            if (revokeResult) {
                permission.setStatus(3); // 已回收
                permission.setRevokeTime(LocalDateTime.now());
                permission.setRevokeReason(reason);
                permission.setUpdateTime(LocalDateTime.now());
                doorAccessPermissionMapper.updateById(permission);
            }

            return revokeResult;

        } catch (Exception e) {
            log.error("回收门禁权限失败", e);
            return false;
        }
    }

    /**
     * 同步门禁权限到门禁系统
     *
     * @param permissionId 权限ID
     */
    @Async
    public void syncPermissionToDoorSystem(Long permissionId) {
        try {
            DoorAccessPermission permission = doorAccessPermissionMapper.selectById(permissionId);

            if (permission == null) {
                return;
            }

            boolean syncResult;

            // 根据门禁系统类型调用不同API
            if (permission.getDoorSystemType() == 1) {
                // 海康威视
                syncResult = syncToHikvision(permission);
            } else {
                // 大华
                syncResult = syncToDahua(permission);
            }

            // 更新同步状态
            if (syncResult) {
                permission.setSyncStatus(1); // 已下发
                permission.setSyncTime(LocalDateTime.now());
            } else {
                permission.setSyncStatus(2); // 下发失败
            }
            permission.setUpdateTime(LocalDateTime.now());
            doorAccessPermissionMapper.updateById(permission);

            log.info("门禁权限同步完成，权限ID={}，结果={}", permissionId, syncResult);

        } catch (Exception e) {
            log.error("同步门禁权限失败，权限ID={}", permissionId, e);

            // 更新为下发失败状态
            DoorAccessPermission permission = doorAccessPermissionMapper.selectById(permissionId);
            if (permission != null) {
                permission.setSyncStatus(2);
                permission.setUpdateTime(LocalDateTime.now());
                doorAccessPermissionMapper.updateById(permission);
            }
        }
    }

    /**
     * 同步到海康威视门禁系统
     *
     * @param permission 权限信息
     * @return 是否同步成功
     */
    private boolean syncToHikvision(DoorAccessPermission permission) {
        try {
            // 调用海康威视API
            // 示例代码，实际需要根据海康威视API文档实现

            log.info("调用海康威视API下发门禁权限，设备ID={}，卡号={}，时间段={}~{}",
                    permission.getDeviceId(), permission.getCardNo(),
                    permission.getEffectiveStartTime(), permission.getEffectiveEndTime());

            // TODO: 实际调用海康威视门禁API
            // HikvisionDoorClient client = new HikvisionDoorClient();
            // return client.addCardPermission(permission.getDeviceId(), permission.getCardNo(),
            //         permission.getEffectiveStartTime(), permission.getEffectiveEndTime());

            return true; // 模拟成功

        } catch (Exception e) {
            log.error("同步到海康威视门禁系统失败", e);
            return false;
        }
    }

    /**
     * 同步到大华门禁系统
     *
     * @param permission 权限信息
     * @return 是否同步成功
     */
    private boolean syncToDahua(DoorAccessPermission permission) {
        try {
            // 调用大华API
            // 示例代码，实际需要根据大华API文档实现

            log.info("调用大华API下发门禁权限，设备ID={}，卡号={}，时间段={}~{}",
                    permission.getDeviceId(), permission.getCardNo(),
                    permission.getEffectiveStartTime(), permission.getEffectiveEndTime());

            // TODO: 实际调用大华门禁API
            // DahuaDoorClient client = new DahuaDoorClient();
            // return client.addCardPermission(permission.getDeviceId(), permission.getCardNo(),
            //         permission.getEffectiveStartTime(), permission.getEffectiveEndTime());

            return true; // 模拟成功

        } catch (Exception e) {
            log.error("同步到大华门禁系统失败", e);
            return false;
        }
    }

    /**
     * 从门禁系统回收权限（海康威视）
     *
     * @param permission 权限信息
     * @return 是否回收成功
     */
    private boolean revokePermissionFromDoorSystem(DoorAccessPermission permission) {
        try {
            if (permission.getDoorSystemType() == 1) {
                // 海康威视
                log.info("调用海康威视API回收门禁权限，设备ID={}，卡号={}",
                        permission.getDeviceId(), permission.getCardNo());
                // TODO: 实际调用海康威视API
            } else {
                // 大华
                log.info("调用大华API回收门禁权限，设备ID={}，卡号={}",
                        permission.getDeviceId(), permission.getCardNo());
                // TODO: 实际调用大华API
            }

            return true; // 模拟成功

        } catch (Exception e) {
            log.error("从门禁系统回收权限失败", e);
            return false;
        }
    }

    /**
     * 查询有效的门禁权限
     *
     * @param staffId 值班人员ID
     * @return 权限列表
     */
    public List<DoorAccessPermission> getActivePermissionsByStaff(String staffId) {
        return doorAccessPermissionMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DoorAccessPermission>()
                        .eq(DoorAccessPermission::getStaffId, Long.parseLong(staffId))
                        .in(DoorAccessPermission::getStatus, 0, 1)
                        .orderByDesc(DoorAccessPermission::getCreateTime)
        );
    }

    /**
     * 查询所有门禁权限
     *
     * @param roomId 机房ID
     * @param status 状态
     * @return 权限列表
     */
    public List<DoorAccessPermission> getPermissionList(Long roomId, Integer status) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DoorAccessPermission> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();

        if (roomId != null) {
            wrapper.eq(DoorAccessPermission::getRoomId, roomId);
        }

        if (status != null) {
            wrapper.eq(DoorAccessPermission::getStatus, status);
        }

        wrapper.orderByDesc(DoorAccessPermission::getCreateTime);

        return doorAccessPermissionMapper.selectList(wrapper);
    }
}
