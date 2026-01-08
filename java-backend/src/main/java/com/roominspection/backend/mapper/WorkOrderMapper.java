package com.roominspection.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.roominspection.backend.entity.WorkOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 工单Mapper
 */
@Mapper
public interface WorkOrderMapper extends BaseMapper<WorkOrder> {

    /**
     * 查询指定时间范围的工单列表
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 工单列表
     */
    @Select("SELECT * FROM work_orders WHERE created_at BETWEEN #{startTime} AND #{endTime} AND deleted = 0 ORDER BY created_at DESC")
    List<WorkOrder> findByTimeRange(@Param("startTime") LocalDateTime startTime,
                                       @Param("endTime") LocalDateTime endTime);

    /**
     * 查询指定状态的工单列表
     *
     * @param status 状态
     * @return 工单列表
     */
    @Select("SELECT * FROM work_orders WHERE status = #{status} AND deleted = 0 ORDER BY created_at DESC")
    List<WorkOrder> findByStatus(@Param("status") String status);

    /**
     * 查询指定类型的工单列表
     *
     * @param type 类型
     * @return 工单列表
     */
    @Select("SELECT * FROM work_orders WHERE type = #{type} AND deleted = 0 ORDER BY created_at DESC")
    List<WorkOrder> findByType(@Param("type") String type);

    /**
     * 查询指定负责人的工单列表
     *
     * @param ownerId 负责人ID
     * @return 工单列表
     */
    @Select("SELECT * FROM work_orders WHERE owner_id = #{ownerId} AND deleted = 0 ORDER BY created_at DESC")
    List<WorkOrder> findByOwnerId(@Param("ownerId") String ownerId);

    /**
     * 查询指定机房的工单列表
     *
     * @param roomId 机房ID
     * @return 工单列表
     */
    @Select("SELECT * FROM work_orders WHERE room_id = #{roomId} AND deleted = 0 ORDER BY created_at DESC")
    List<WorkOrder> findByRoomId(@Param("roomId") String roomId);

    /**
     * 查询超时工单列表
     *
     * @return 工单列表
     */
    @Select("SELECT * FROM work_orders WHERE is_overdue = 1 AND status NOT IN ('COMPLETED', 'CANCELLED', 'CLOSED') AND deleted = 0 ORDER BY created_at DESC")
    List<WorkOrder> findOverdue();

    /**
     * 查询待处理工单列表
     *
     * @return 工单列表
     */
    @Select("SELECT * FROM work_orders WHERE status IN ('PENDING', 'ASSIGNED', 'IN_PROGRESS') AND deleted = 0 ORDER BY priority DESC, created_at ASC")
    List<WorkOrder> findPending();

    /**
     * 查询紧急工单列表
     *
     * @return 工单列表
     */
    @Select("SELECT * FROM work_orders WHERE priority = 'URGENT' AND status NOT IN ('COMPLETED', 'CANCELLED', 'CLOSED') AND deleted = 0 ORDER BY created_at ASC")
    List<WorkOrder> findUrgent();

    /**
     * 统计工单数量（按状态）
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    @Select("SELECT status, COUNT(*) as count FROM work_orders WHERE created_at BETWEEN #{startTime} AND #{endTime} AND deleted = 0 GROUP BY status")
    List<Map<String, Object>> countByStatus(@Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime);

    /**
     * 统计工单数量（按类型）
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    @Select("SELECT type, COUNT(*) as count FROM work_orders WHERE created_at BETWEEN #{startTime} AND #{endTime} AND deleted = 0 GROUP BY type")
    List<Map<String, Object>> countByType(@Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime);

    /**
     * 统计工单数量（按优先级）
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    @Select("SELECT priority, COUNT(*) as count FROM work_orders WHERE created_at BETWEEN #{startTime} AND #{endTime} AND deleted = 0 GROUP BY priority")
    List<Map<String, Object>> countByPriority(@Param("startTime") LocalDateTime startTime,
                                                @Param("endTime") LocalDateTime endTime);
}
