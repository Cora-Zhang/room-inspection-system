package com.roominspection.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.roominspection.backend.entity.WorkOrderFlow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 工单流转记录Mapper
 */
@Mapper
public interface WorkOrderFlowMapper extends BaseMapper<WorkOrderFlow> {

    /**
     * 查询指定工单的流转记录
     *
     * @param workOrderId 工单ID
     * @return 流转记录列表
     */
    @Select("SELECT * FROM work_order_flows WHERE work_order_id = #{workOrderId} AND deleted = 0 ORDER BY operated_at ASC")
    List<WorkOrderFlow> findByWorkOrderId(@Param("workOrderId") String workOrderId);

    /**
     * 查询指定操作类型的流转记录
     *
     * @param actionType 操作类型
     * @return 流转记录列表
     */
    @Select("SELECT * FROM work_order_flows WHERE action_type = #{actionType} AND deleted = 0 ORDER BY operated_at DESC")
    List<WorkOrderFlow> findByActionType(@Param("actionType") String actionType);

    /**
     * 查询指定操作人的流转记录
     *
     * @param operatorId 操作人ID
     * @return 流转记录列表
     */
    @Select("SELECT * FROM work_order_flows WHERE operator_id = #{operatorId} AND deleted = 0 ORDER BY operated_at DESC")
    List<WorkOrderFlow> findByOperatorId(@Param("operatorId") String operatorId);
}
