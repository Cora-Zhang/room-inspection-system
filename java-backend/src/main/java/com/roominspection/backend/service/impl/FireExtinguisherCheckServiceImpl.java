package com.roominspection.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.roominspection.backend.entity.FireExtinguisherCheck;
import com.roominspection.backend.mapper.FireExtinguisherCheckMapper;
import com.roominspection.backend.service.FireExtinguisherCheckService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * 灭火器检查记录Service实现
 */
@Service
public class FireExtinguisherCheckServiceImpl extends ServiceImpl<FireExtinguisherCheckMapper, FireExtinguisherCheck>
        implements FireExtinguisherCheckService {

    @Override
    public List<FireExtinguisherCheck> listByExtinguisherId(Long extinguisherId) {
        LambdaQueryWrapper<FireExtinguisherCheck> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FireExtinguisherCheck::getExtinguisherId, extinguisherId);
        wrapper.orderByDesc(FireExtinguisherCheck::getCheckTime);
        return list(wrapper);
    }

    @Override
    public List<FireExtinguisherCheck> listByRoomId(Long roomId) {
        LambdaQueryWrapper<FireExtinguisherCheck> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(roomId != null, FireExtinguisherCheck::getRoomId, roomId);
        wrapper.orderByDesc(FireExtinguisherCheck::getCheckTime);
        return list(wrapper);
    }

    @Override
    public List<FireExtinguisherCheck> listByCheckType(Integer checkType) {
        LambdaQueryWrapper<FireExtinguisherCheck> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(checkType != null, FireExtinguisherCheck::getCheckType, checkType);
        wrapper.orderByDesc(FireExtinguisherCheck::getCheckTime);
        return list(wrapper);
    }

    @Override
    public boolean hasOpenOrder(Long extinguisherId, Integer checkType) {
        LambdaQueryWrapper<FireExtinguisherCheck> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FireExtinguisherCheck::getExtinguisherId, extinguisherId);
        wrapper.eq(checkType != null, FireExtinguisherCheck::getCheckType, checkType);
        wrapper.eq(FireExtinguisherCheck::getCheckResult, 0); // 0-待检查
        wrapper.orderByDesc(FireExtinguisherCheck::getCheckTime);
        wrapper.last("LIMIT 1");
        return count(wrapper) > 0;
    }

    @Override
    public boolean hasMonthlyCheck(Long extinguisherId, LocalDate date) {
        YearMonth yearMonth = YearMonth.from(date);
        LocalDate startOfMonth = yearMonth.atDay(1);
        LocalDate endOfMonth = yearMonth.atEndOfMonth();

        LambdaQueryWrapper<FireExtinguisherCheck> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FireExtinguisherCheck::getExtinguisherId, extinguisherId);
        wrapper.eq(FireExtinguisherCheck::getCheckType, 1); // 1-月度检查
        wrapper.ge(FireExtinguisherCheck::getCheckTime, startOfMonth.atStartOfDay());
        wrapper.le(FireExtinguisherCheck::getCheckTime, endOfMonth.atTime(23, 59, 59));
        wrapper.last("LIMIT 1");
        return count(wrapper) > 0;
    }

    @Override
    @Transactional
    public void completeCheck(Long checkId, String checker, Double pressureValue, Double weightValue,
                              Integer pressureStatus, Integer weightStatus, Integer appearanceStatus,
                              String appearanceDescription, String photos, Integer needRefill,
                              String refillRemark, Integer checkResult, String handlingMeasures) {
        FireExtinguisherCheck check = getById(checkId);
        if (check == null) {
            throw new RuntimeException("检查记录不存在");
        }

        check.setChecker(checker);
        check.setCheckTime(java.time.LocalDateTime.now());
        check.setPressureValue(pressureValue);
        check.setPressureStatus(pressureStatus);
        check.setWeightValue(weightValue);
        check.setWeightStatus(weightStatus);
        check.setAppearanceStatus(appearanceStatus);
        check.setAppearanceDescription(appearanceDescription);
        check.setPhotos(photos);
        check.setNeedRefill(needRefill);
        check.setRefillRemark(refillRemark);
        check.setCheckResult(checkResult);
        check.setHandlingMeasures(handlingMeasures);
        check.setUpdateTime(java.time.LocalDateTime.now());

        updateById(check);
    }
}
