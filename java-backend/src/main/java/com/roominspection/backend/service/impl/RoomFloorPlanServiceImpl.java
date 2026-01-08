package com.roominspection.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.roominspection.backend.entity.RoomFloorPlan;
import com.roominspection.backend.mapper.RoomFloorPlanMapper;
import com.roominspection.backend.service.RoomFloorPlanService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 机房平面图Service实现
 */
@Service
public class RoomFloorPlanServiceImpl extends ServiceImpl<RoomFloorPlanMapper, RoomFloorPlan>
        implements RoomFloorPlanService {

    @Override
    public List<RoomFloorPlan> listByRoomId(String roomId) {
        return baseMapper.findByRoomId(roomId);
    }

    @Override
    public RoomFloorPlan getMainFloorPlan(String roomId) {
        return baseMapper.findMainByRoomId(roomId);
    }

    @Override
    @Transactional
    public boolean createFloorPlan(RoomFloorPlan floorPlan) {
        floorPlan.setCreatedAt(LocalDateTime.now());
        floorPlan.setUpdatedAt(LocalDateTime.now());

        // 如果是第一个平面图，自动设为主图
        LambdaQueryWrapper<RoomFloorPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoomFloorPlan::getRoomId, floorPlan.getRoomId());
        long count = count(wrapper);

        if (count == 0) {
            floorPlan.setIsMain(true);
        } else if (Boolean.TRUE.equals(floorPlan.getIsMain())) {
            // 如果设置为新的主图，将原有的主图取消
            LambdaQueryWrapper<RoomFloorPlan> mainWrapper = new LambdaQueryWrapper<>();
            mainWrapper.eq(RoomFloorPlan::getRoomId, floorPlan.getRoomId());
            mainWrapper.eq(RoomFloorPlan::getIsMain, true);
            RoomFloorPlan oldMain = getOne(mainWrapper);
            if (oldMain != null) {
                oldMain.setIsMain(false);
                updateById(oldMain);
            }
        }

        return save(floorPlan);
    }

    @Override
    @Transactional
    public boolean updateFloorPlan(RoomFloorPlan floorPlan) {
        floorPlan.setUpdatedAt(LocalDateTime.now());
        return updateById(floorPlan);
    }

    @Override
    @Transactional
    public boolean deleteFloorPlan(String id) {
        return removeById(id);
    }

    @Override
    @Transactional
    public boolean setAsMain(String id, String roomId) {
        // 取消原有的主图
        LambdaQueryWrapper<RoomFloorPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoomFloorPlan::getRoomId, roomId);
        wrapper.eq(RoomFloorPlan::getIsMain, true);
        RoomFloorPlan oldMain = getOne(wrapper);
        if (oldMain != null) {
            oldMain.setIsMain(false);
            updateById(oldMain);
        }

        // 设置新的主图
        RoomFloorPlan newMain = getById(id);
        if (newMain != null) {
            newMain.setIsMain(true);
            newMain.setUpdatedAt(LocalDateTime.now());
            return updateById(newMain);
        }
        return false;
    }

    @Override
    public Map<String, Object> getStatistics(String roomId) {
        LambdaQueryWrapper<RoomFloorPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(roomId != null, RoomFloorPlan::getRoomId, roomId);
        List<RoomFloorPlan> floorPlans = list(wrapper);

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", floorPlans.size());

        long activeCount = floorPlans.stream()
                .filter(fp -> "ACTIVE".equals(fp.getStatus()))
                .count();
        long mainCount = floorPlans.stream()
                .filter(RoomFloorPlan::getIsMain)
                .count();

        stats.put("active", activeCount);
        stats.put("main", mainCount);

        return stats;
    }
}
