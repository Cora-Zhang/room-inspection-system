package com.roominspection.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.roominspection.backend.entity.Room;
import com.roominspection.backend.entity.TempAccessRequest;
import com.roominspection.backend.mapper.RoomMapper;
import com.roominspection.backend.mapper.TempAccessRequestMapper;
import com.roominspection.backend.util.DingTalkSyncUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 临时门禁权限申请服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TempAccessRequestService extends ServiceImpl<TempAccessRequestMapper, TempAccessRequest> implements IService<TempAccessRequest> {

    private final TempAccessRequestMapper tempAccessRequestMapper;
    private final RoomMapper roomMapper;
    private final DoorAccessPermissionService doorAccessPermissionService;
    private final DingTalkSyncUtil dingTalkSyncUtil;

    /**
     * 创建临时权限申请
     *
     * @param request 申请信息
     * @return 是否创建成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean createRequest(TempAccessRequest request) {
        try {
            // 验证机房是否存在
            Room room = roomMapper.selectById(request.getRoomId());
            if (room == null) {
                throw new RuntimeException("机房不存在");
            }

            // 设置默认值
            request.setRoomName(room.getName());
            request.setApprovalStatus(0); // 待审批
            request.setSyncStatus(0); // 未下发
            request.setEffectiveStatus(0); // 未生效
            request.setCreateTime(LocalDateTime.now());
            request.setUpdateTime(LocalDateTime.now());

            int result = tempAccessRequestMapper.insert(request);

            // 发送审批通知给管理员
            if (result > 0) {
                sendApprovalNotification(request);
            }

            return result > 0;

        } catch (Exception e) {
            log.error("创建临时权限申请失败", e);
            return false;
        }
    }

    /**
     * 审批临时权限申请
     *
     * @param requestId 申请ID
     * @param approverId 审批人ID
     * @param approverName 审批人姓名
     * @param approvalStatus 审批状态（1-通过 2-拒绝）
     * @param comment 审批意见
     * @return 是否审批成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean approveRequest(Long requestId, Long approverId, String approverName,
                                   Integer approvalStatus, String comment) {
        try {
            TempAccessRequest request = tempAccessRequestMapper.selectById(requestId);

            if (request == null) {
                throw new RuntimeException("申请不存在");
            }

            if (request.getApprovalStatus() != 0) {
                throw new RuntimeException("申请已审批");
            }

            // 更新审批信息
            request.setApproverId(approverId);
            request.setApproverName(approverName);
            request.setApprovalStatus(approvalStatus);
            request.setApprovalTime(LocalDateTime.now());
            request.setApprovalComment(comment);
            request.setUpdateTime(LocalDateTime.now());

            int result = tempAccessRequestMapper.updateById(request);

            // 如果审批通过，自动创建门禁权限
            if (result > 0 && approvalStatus == 1) {
                createDoorPermission(request);
            }

            // 发送审批结果通知
            sendApprovalResultNotification(request);

            return result > 0;

        } catch (Exception e) {
            log.error("审批临时权限申请失败", e);
            return false;
        }
    }

    /**
     * 撤销临时权限申请
     *
     * @param requestId 申请ID
     * @param reason 撤销原因
     * @return 是否撤销成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean revokeRequest(Long requestId, String reason) {
        try {
            TempAccessRequest request = tempAccessRequestMapper.selectById(requestId);

            if (request == null) {
                throw new RuntimeException("申请不存在");
            }

            if (request.getApprovalStatus() != 0) {
                throw new RuntimeException("已审批的申请不能撤销");
            }

            // 更新状态
            request.setApprovalStatus(3); // 已撤销
            request.setApprovalComment(reason);
            request.setUpdateTime(LocalDateTime.now());

            return tempAccessRequestMapper.updateById(request) > 0;

        } catch (Exception e) {
            log.error("撤销临时权限申请失败", e);
            return false;
        }
    }

    /**
     * 创建门禁权限（审批通过后）
     *
     * @param request 申请信息
     */
    @Async
    @Transactional(rollbackFor = Exception.class)
    public void createDoorPermission(TempAccessRequest request) {
        try {
            boolean success = doorAccessPermissionService.createPermission(
                    request.getApplicantNo(),
                    request.getApplicantName(),
                    request.getRoomId(),
                    request.getRoomName(),
                    request.getAccessStartTime(),
                    request.getAccessEndTime()
            );

            if (success) {
                request.setSyncStatus(1); // 已下发
            } else {
                request.setSyncStatus(2); // 下发失败
            }
            request.setUpdateTime(LocalDateTime.now());
            tempAccessRequestMapper.updateById(request);

        } catch (Exception e) {
            log.error("创建临时门禁权限失败", e);
            request.setSyncStatus(2); // 下发失败
            request.setUpdateTime(LocalDateTime.now());
            tempAccessRequestMapper.updateById(request);
        }
    }

    /**
     * 发送审批通知
     *
     * @param request 申请信息
     */
    @Async
    public void sendApprovalNotification(TempAccessRequest request) {
        try {
            // TODO: 查找管理员并发送审批通知

            log.info("发送临时权限审批通知，申请人={}", request.getApplicantName());

        } catch (Exception e) {
            log.error("发送审批通知失败", e);
        }
    }

    /**
     * 发送审批结果通知
     *
     * @param request 申请信息
     */
    @Async
    public void sendApprovalResultNotification(TempAccessRequest request) {
        try {
            // 发送钉钉通知
            dingTalkSyncUtil.sendTempAccessApproval(
                    List.of(request.getApplicantNo()),
                    request.getApplicantName(),
                    request.getApprovalStatus(),
                    request.getApprovalComment()
            );

            log.info("发送临时权限审批结果通知，申请人={}，结果={}",
                    request.getApplicantName(),
                    request.getApprovalStatus() == 1 ? "通过" : "拒绝");

        } catch (Exception e) {
            log.error("发送审批结果通知失败", e);
        }
    }

    /**
     * 查询临时权限申请列表
     *
     * @param approvalStatus 审批状态
     * @return 申请列表
     */
    public java.util.List<TempAccessRequest> getRequestList(Integer approvalStatus) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TempAccessRequest> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();

        if (approvalStatus != null) {
            wrapper.eq(TempAccessRequest::getApprovalStatus, approvalStatus);
        }

        wrapper.orderByDesc(TempAccessRequest::getCreateTime);

        return tempAccessRequestMapper.selectList(wrapper);
    }
}
