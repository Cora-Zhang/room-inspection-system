package com.roominspection.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.roominspection.backend.entity.AirConditioner;
import com.roominspection.backend.entity.AirConditionerData;
import com.roominspection.backend.entity.EfficiencyCheckOrder;
import com.roominspection.backend.mapper.AirConditionerMapper;
import com.roominspection.backend.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 精密空调管理Service实现
 */
@Service
public class AirConditionerServiceImpl extends ServiceImpl<AirConditionerMapper, AirConditioner>
        implements AirConditionerService {

    @Autowired
    private AirConditionerDataService acDataService;

    @Autowired
    private EfficiencyCheckOrderService efficiencyCheckOrderService;

    @Override
    public List<AirConditioner> listByRoomId(Long roomId) {
        LambdaQueryWrapper<AirConditioner> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(roomId != null, AirConditioner::getRoomId, roomId);
        wrapper.orderByAsc(AirConditioner::getAcCode);
        return list(wrapper);
    }

    @Override
    @Transactional
    public void batchCollectData() {
        // 查询所有在线空调
        LambdaQueryWrapper<AirConditioner> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AirConditioner::getStatus, 0); // 0-正常在线
        List<AirConditioner> acList = list(wrapper);

        for (AirConditioner ac : acList) {
            try {
                collectData(ac.getId());
                // 检查是否达到保养阈值
                checkMaintenanceThreshold(ac.getId());
            } catch (Exception e) {
                log.error("采集空调数据失败: acCode={}, error={}", ac.getAcCode(), e.getMessage());
            }
        }
    }

    @Override
    @Transactional
    public void collectData(Long acId) {
        AirConditioner ac = getById(acId);
        if (ac == null) {
            throw new RuntimeException("空调不存在");
        }

        // 模拟采集数据（实际应通过SNMP/Modbus等协议采集）
        AirConditionerData data = new AirConditionerData();
        data.setAcId(acId);
        data.setAcCode(ac.getAcCode());

        // 运行模式
        int runMode = (int) (Math.random() * 2); // 0-关闭，1-制冷
        data.setRunMode(runMode);

        // 设定温度
        data.setSetTemperature(ac.getSetTemperature() != null ? ac.getSetTemperature() : 22.0);

        // 回风温度（模拟波动）
        double baseTemp = data.getSetTemperature();
        double tempFluctuation = (Math.random() - 0.5) * 3.0; // ±1.5度波动
        double returnTemp = baseTemp + tempFluctuation;
        data.setReturnTemperature(returnTemp);

        // 回风湿度
        double humidity = 50.0 + (Math.random() - 0.5) * 20.0; // 40%-60%
        data.setReturnHumidity(humidity);

        // 送风温度（比回风低5-8度）
        double supplyTemp = returnTemp - 5.0 - Math.random() * 3.0;
        data.setSupplyTemperature(supplyTemp);

        // 压缩机状态
        data.setCompressor1Status(runMode == 1 ? 1 : 0);
        data.setCompressor2Status(runMode == 1 && Math.random() > 0.5 ? 1 : 0);

        // 风机状态
        data.setFan1Status(runMode == 1 ? 1 : 0);
        data.setFan2Status(runMode == 1 ? 1 : 0);

        // 加湿器状态
        data.setHumidifierStatus(humidity < 45.0 ? 1 : 0);

        // 电加热器状态
        data.setHeaterStatus(0);

        // 当前功率（模拟）
        double power = runMode == 1 ? 15.0 + Math.random() * 10.0 : 0.5;
        data.setCurrentPower(power);

        // 计算温差
        double tempDiff = returnTemp - data.getSetTemperature();
        data.setTemperatureDiff(tempDiff);

        // 判断温差异常（温差超过3度算异常）
        boolean isDiffAbnormal = Math.abs(tempDiff) > 3.0;
        data.setIsDiffAbnormal(isDiffAbnormal ? 1 : 0);

        data.setCollectTime(LocalDateTime.now());

        // 保存数据
        acDataService.save(data);

        // 更新空调状态
        ac.setRunMode(runMode);
        ac.setCurrentReturnTemp(returnTemp);
        ac.setCurrentReturnHumidity(humidity);
        ac.setLastCollectTime(LocalDateTime.now());

        // 如果压缩机运行，更新运行时长
        if (data.getCompressor1Status() == 1 || data.getCompressor2Status() == 1) {
            ac.setCompressorRuntimeHours(ac.getCompressorRuntimeHours() + 0.0167); // 假设每分钟采集一次，加1分钟时长
        }

        // 判断状态
        int status = 0; // 正常
        if (isDiffAbnormal) {
            status = 1; // 告警
        }
        ac.setStatus(status);

        updateById(ac);

        // 如果温差异常，触发效率核查工单
        if (isDiffAbnormal) {
            triggerEfficiencyCheckOrder(ac, data, tempDiff);
        }
    }

    /**
     * 触发效率核查工单
     */
    private void triggerEfficiencyCheckOrder(AirConditioner ac, AirConditionerData data, double tempDiff) {
        // 检查是否已有未处理的温差异常工单
        boolean hasOpenOrder = efficiencyCheckOrderService.hasOpenOrder(ac.getId(), 1); // 1-制冷效率检查
        if (hasOpenOrder) {
            return;
        }

        // 创建工单
        EfficiencyCheckOrder order = new EfficiencyCheckOrder();
        order.setOrderNo("EFF-" + System.currentTimeMillis());
        order.setAcId(ac.getId());
        order.setAcCode(ac.getAcCode());
        order.setRoomId(ac.getRoomId());
        order.setOrderType(1); // 1-制冷效率检查
        order.setTriggerReason("回风与设定温差持续异常");
        order.setAbnormalDetail(String.format(
                "空调 %s 温差异常：回风温度 %.1f℃ 与设定温度 %.1f℃ 差值 %.1f℃，超过正常范围",
                ac.getAcName(), data.getReturnTemperature(),
                data.getSetTemperature(), tempDiff));
        order.setPriority(2); // 2-高
        order.setStatus(0); // 0-待指派
        order.setCreateTime(LocalDateTime.now());

        efficiencyCheckOrderService.save(order);

        log.info("自动触发效率核查工单: orderNo={}, acCode={}", order.getOrderNo(), ac.getAcCode());
    }

    @Override
    public Map<String, Object> getStatistics(Long roomId) {
        LambdaQueryWrapper<AirConditioner> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(roomId != null, AirConditioner::getRoomId, roomId);
        List<AirConditioner> acList = list(wrapper);

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", acList.size());

        long onlineCount = acList.stream().filter(ac -> ac.getStatus() == 0).count();
        long alarmCount = acList.stream().filter(ac -> ac.getStatus() == 1).count();
        long offlineCount = acList.stream().filter(ac -> ac.getStatus() == 2).count();

        long runningCount = acList.stream().filter(ac -> ac.getRunMode() == 1).count();

        stats.put("online", onlineCount);
        stats.put("alarm", alarmCount);
        stats.put("offline", offlineCount);
        stats.put("running", runningCount);

        return stats;
    }

    @Override
    @Transactional
    public void updateRuntime(Long acId, Double additionalHours) {
        AirConditioner ac = getById(acId);
        if (ac == null) {
            throw new RuntimeException("空调不存在");
        }

        ac.setCompressorRuntimeHours(ac.getCompressorRuntimeHours() + additionalHours);
        updateById(ac);

        // 检查是否达到保养阈值
        checkMaintenanceThreshold(acId);
    }

    @Override
    @Transactional
    public void checkMaintenanceThreshold(Long acId) {
        AirConditioner ac = getById(acId);
        if (ac == null) {
            return;
        }

        // 检查是否达到保养阈值
        if (ac.getCompressorRuntimeHours() >= ac.getMaintenanceThreshold()) {
            // 检查是否已有未处理的保养工单
            boolean hasOpenOrder = efficiencyCheckOrderService.hasOpenOrder(ac.getId(), 3); // 3-预防性保养
            if (hasOpenOrder) {
                return;
            }

            // 创建保养工单
            EfficiencyCheckOrder order = new EfficiencyCheckOrder();
            order.setOrderNo("EFF-" + System.currentTimeMillis());
            order.setAcId(ac.getId());
            order.setAcCode(ac.getAcCode());
            order.setRoomId(ac.getRoomId());
            order.setOrderType(3); // 3-预防性保养
            order.setTriggerReason("压缩机运行时长达到保养阈值");
            order.setAbnormalDetail(String.format(
                    "空调 %s 压缩机累计运行时长 %.1f 小时，已达到保养阈值 %.1f 小时",
                    ac.getAcName(), ac.getCompressorRuntimeHours(), ac.getMaintenanceThreshold()));
            order.setPriority(2); // 2-高
            order.setStatus(0); // 0-待指派
            order.setCreateTime(LocalDateTime.now());

            efficiencyCheckOrderService.save(order);

            log.info("自动触发预防性保养工单: orderNo={}, acCode={}", order.getOrderNo(), ac.getAcCode());

            // 更新下次保养时间
            ac.setLastMaintenanceTime(LocalDateTime.now());
            ac.setNextMaintenanceTime(LocalDateTime.now().plusHours((long) ac.getMaintenanceThreshold().doubleValue()));
            updateById(ac);
        }
    }
}
