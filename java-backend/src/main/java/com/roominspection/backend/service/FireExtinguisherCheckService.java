package com.roominspection.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.roominspection.backend.entity.FireExtinguisherCheck;
import java.time.LocalDate;
import java.util.List;

/**
 * 灭火器检查记录Service
 */
public interface FireExtinguisherCheckService extends IService<FireExtinguisherCheck> {

    /**
     * 根据灭火器ID查询检查记录列表
     */
    List<FireExtinguisherCheck> listByExtinguisherId(Long extinguisherId);

    /**
     * 根据机房ID查询检查记录列表
     */
    List<FireExtinguisherCheck> listByRoomId(Long roomId);

    /**
     * 根据检查类型查询检查记录列表
     */
    List<FireExtinguisherCheck> listByCheckType(Integer checkType);

    /**
     * 检查是否有未处理工单
     */
    boolean hasOpenOrder(Long extinguisherId, Integer checkType);

    /**
     * 检查本月是否已有月度检查
     */
    boolean hasMonthlyCheck(Long extinguisherId, LocalDate date);

    /**
     * 完成检查
     */
    void completeCheck(Long checkId, String checker, Double pressureValue, Double weightValue,
                       Integer pressureStatus, Integer weightStatus, Integer appearanceStatus,
                       String appearanceDescription, String photos, Integer needRefill,
                       String refillRemark, Integer checkResult, String handlingMeasures);
}
