package com.roominspection.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.roominspection.backend.entity.FireExtinguisher;
import com.roominspection.backend.entity.FireExtinguisherCheck;
import com.roominspection.backend.mapper.FireExtinguisherMapper;
import com.roominspection.backend.service.FireExtinguisherCheckService;
import com.roominspection.backend.service.FireExtinguisherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 灭火器管理Service实现
 */
@Service
public class FireExtinguisherServiceImpl extends ServiceImpl<FireExtinguisherMapper, FireExtinguisher>
        implements FireExtinguisherService {

    @Autowired
    private FireExtinguisherCheckService checkService;

    @Override
    public List<FireExtinguisher> listByRoomId(Long roomId) {
        LambdaQueryWrapper<FireExtinguisher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(roomId != null, FireExtinguisher::getRoomId, roomId);
        wrapper.orderByAsc(FireExtinguisher::getExtinguisherCode);
        return list(wrapper);
    }

    @Override
    @Transactional
    public void batchCollectData() {
        // 查询所有在线灭火器
        LambdaQueryWrapper<FireExtinguisher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FireExtinguisher::getStatus, 0); // 0-正常在线
        List<FireExtinguisher> extinguisherList = list(wrapper);

        for (FireExtinguisher fe : extinguisherList) {
            try {
                collectData(fe.getId());
                // 检查压力是否异常
                checkPressureAbnormal(fe.getId());
            } catch (Exception e) {
                log.error("采集灭火器数据失败: extinguisherCode={}, error={}", fe.getExtinguisherCode(), e.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public void collectData(Long extinguisherId) {
        FireExtinguisher fe = getById(extinguisherId);
        if (fe == null) {
            throw new RuntimeException("灭火器不存在");
        }

        // 模拟采集压力数据（如果是数字压力表）
        if (fe.getPressureMeterType() == 1) { // 1-数字压力表
            double pressure = fe.getRatedPressure() * (0.95 + Math.random() * 0.08); // 额定压力的95%-103%
            updatePressure(extinguisherId, pressure);
        }

        // 模拟采集重量数据（如果支持智能称重）
        if (fe.getSupportWeighing() == 1) { // 1-支持智能称重
            double weight = fe.getWeight() * (0.98 + Math.random() * 0.04); // 额定重量的98%-102%
            updateWeight(extinguisherId, weight);
        }

        fe.setLastCollectTime(LocalDateTime.now());
        updateById(fe);
    }

    @Override
    @Transactional
    public void updatePressure(Long extinguisherId, Double pressure) {
        FireExtinguisher fe = getById(extinguisherId);
        if (fe == null) {
            throw new RuntimeException("灭火器不存在");
        }

        fe.setCurrentPressure(pressure);

        // 判断压力状态
        if (pressure < fe.getPressureAbnormalThreshold()) {
            fe.setPressureStatus(3); // 3-异常
            fe.setStatus(1); // 1-告警
        } else if (pressure < fe.getPressureAlertThreshold()) {
            fe.setPressureStatus(2); // 2-偏低预警
            fe.setStatus(0); // 0-正常
        } else if (pressure < fe.getRatedPressure() * 0.9) {
            fe.setPressureStatus(1); // 1-偏低
            fe.setStatus(0); // 0-正常
        } else {
            fe.setPressureStatus(0); // 0-正常
            fe.setStatus(0); // 0-正常
        }

        updateById(fe);
    }

    @Override
    @Transactional
    public void updateWeight(Long extinguisherId, Double weight) {
        FireExtinguisher fe = getById(extinguisherId);
        if (fe == null) {
            throw new RuntimeException("灭火器不存在");
        }

        fe.setCurrentWeight(weight);

        // 判断重量状态
        double alertThreshold = fe.getWeightAlertThreshold() != null ? fe.getWeightAlertThreshold() : fe.getWeight() * 0.95;
        double abnormalThreshold = fe.getWeightAbnormalThreshold() != null ? fe.getWeightAbnormalThreshold() : fe.getWeight() * 0.9;

        if (weight < abnormalThreshold) {
            fe.setWeightStatus(3); // 3-异常
        } else if (weight < alertThreshold) {
            fe.setWeightStatus(2); // 2-偏低预警
        } else if (weight < fe.getWeight() * 0.98) {
            fe.setWeightStatus(1); // 1-偏低
        } else {
            fe.setWeightStatus(0); // 0-正常
        }

        updateById(fe);
    }

    @Override
    @Transactional
    public void checkPressureAbnormal(Long extinguisherId) {
        FireExtinguisher fe = getById(extinguisherId);
        if (fe == null || fe.getPressureStatus() != 3) { // 3-异常
            return;
        }

        // 检查是否已有未处理的紧急确认工单
        boolean hasOpenOrder = checkService.hasOpenOrder(extinguisherId, 2); // 2-紧急确认
        if (hasOpenOrder) {
            return;
        }

        // 创建紧急确认工单
        FireExtinguisherCheck check = new FireExtinguisherCheck();
        check.setCheckNo("FEC-" + System.currentTimeMillis());
        check.setExtinguisherId(fe.getId());
        check.setExtinguisherCode(fe.getExtinguisherCode());
        check.setRoomId(fe.getRoomId());
        check.setCheckType(2); // 2-紧急确认
        check.setPressureValue(fe.getCurrentPressure());
        check.setPressureStatus(fe.getPressureStatus());
        check.setCheckResult(1); // 1-不合格
        check.setCreateTime(LocalDateTime.now());

        checkService.save(check);

        log.info("自动触发灭火器紧急确认工单: checkNo={}, extinguisherCode={}", check.getCheckNo(), fe.getExtinguisherCode());
    }

    @Override
    @Transactional
    public void generateMonthlyCheckOrder() {
        // 检查是否为每月首日
        LocalDate today = LocalDate.now();
        if (today.getDayOfMonth() != 1) {
            return;
        }

        // 查询所有在线灭火器
        LambdaQueryWrapper<FireExtinguisher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FireExtinguisher::getStatus, 0); // 0-正常在线
        List<FireExtinguisher> extinguisherList = list(wrapper);

        for (FireExtinguisher fe : extinguisherList) {
            // 检查本月是否已有月度检查工单
            boolean hasMonthlyCheck = checkService.hasMonthlyCheck(fe.getId(), today);
            if (hasMonthlyCheck) {
                continue;
            }

            // 创建月度检查工单
            FireExtinguisherCheck check = new FireExtinguisherCheck();
            check.setCheckNo("FEC-" + System.currentTimeMillis());
            check.setExtinguisherId(fe.getId());
            check.setExtinguisherCode(fe.getExtinguisherCode());
            check.setRoomId(fe.getRoomId());
            check.setCheckType(1); // 1-月度称重与外观检查
            check.setCheckTime(today.atStartOfDay());
            check.setCheckResult(0); // 0-待检查
            check.setCreateTime(LocalDateTime.now());

            checkService.save(check);

            log.info("自动生成月度检查工单: checkNo={}, extinguisherCode={}", check.getCheckNo(), fe.getExtinguisherCode());
        }
    }

    @Override
    public Map<String, Object> getStatistics(Long roomId) {
        LambdaQueryWrapper<FireExtinguisher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(roomId != null, FireExtinguisher::getRoomId, roomId);
        List<FireExtinguisher> feList = list(wrapper);

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", feList.size());

        long onlineCount = feList.stream().filter(fe -> fe.getStatus() == 0).count();
        long alarmCount = feList.stream().filter(fe -> fe.getStatus() == 1).count();
        long offlineCount = feList.stream().filter(fe -> fe.getStatus() == 2).count();

        long pressureAbnormalCount = feList.stream().filter(fe -> fe.getPressureStatus() == 3).count();
        long weightAbnormalCount = feList.stream().filter(fe -> fe.getWeightStatus() == 3).count();

        stats.put("online", onlineCount);
        stats.put("alarm", alarmCount);
        stats.put("offline", offlineCount);
        stats.put("pressureAbnormal", pressureAbnormalCount);
        stats.put("weightAbnormal", weightAbnormalCount);

        return stats;
    }
}
