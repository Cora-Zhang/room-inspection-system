package com.roominspection.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.roominspection.backend.entity.*;
import com.roominspection.backend.mapper.*;
import com.roominspection.backend.service.DataSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据同步服务实现类
 */
@Service
public class DataSyncServiceImpl implements DataSyncService {

    private static final Logger logger = LoggerFactory.getLogger(DataSyncServiceImpl.class);

    @Autowired
    private OrganizationMapper organizationMapper;

    @Autowired
    private IAMUserMapper iamUserMapper;

    @Autowired
    private JobMapper jobMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DepartmentMapper departmentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> syncOrganization(Organization orgData) {
        Map<String, Object> result = new HashMap<>();

        try {
            logger.info("Syncing organization: {}, action: {}", orgData.getOrgCode(), orgData.getActionFlag());

            // 根据组织编码查询
            LambdaQueryWrapper<Organization> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Organization::getOrgCode, orgData.getOrgCode());
            Organization existingOrg = organizationMapper.selectOne(wrapper);

            if (orgData.getActionFlag() == 0) {
                // 新增
                if (existingOrg != null) {
                    result.put("code", "-1");
                    result.put("msg", "组织已存在");
                    return result;
                }
                orgData.setSyncTime(LocalDateTime.now());
                orgData.setCreatedAt(LocalDateTime.now());
                orgData.setUpdatedAt(LocalDateTime.now());
                organizationMapper.insert(orgData);

                // 同步到部门表
                syncToDepartment(orgData);

                logger.info("Organization created: {}", orgData.getOrgCode());
            } else if (orgData.getActionFlag() == 1) {
                // 修改
                if (existingOrg == null) {
                    result.put("code", "-1");
                    result.put("msg", "组织不存在");
                    return result;
                }
                orgData.setId(existingOrg.getId());
                orgData.setSyncTime(LocalDateTime.now());
                orgData.setUpdatedAt(LocalDateTime.now());
                organizationMapper.updateById(orgData);

                // 同步到部门表
                syncToDepartment(orgData);

                logger.info("Organization updated: {}", orgData.getOrgCode());
            } else if (orgData.getActionFlag() == 2) {
                // 删除
                if (existingOrg == null) {
                    result.put("code", "-1");
                    result.put("msg", "组织不存在");
                    return result;
                }
                organizationMapper.deleteById(existingOrg.getId());

                // 删除部门表中的数据
                deleteDepartment(orgData.getOrgCode());

                logger.info("Organization deleted: {}", orgData.getOrgCode());
            } else {
                result.put("code", "-1");
                result.put("msg", "无效的操作标识");
                return result;
            }

            result.put("code", "0");
            result.put("msg", "同步成功");
            return result;

        } catch (Exception e) {
            logger.error("Sync organization error", e);
            result.put("code", "-1");
            result.put("msg", "同步失败: " + e.getMessage());
            return result;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> syncUser(IAMUser userData) {
        Map<String, Object> result = new HashMap<>();

        try {
            logger.info("Syncing user: {}, action: {}", userData.getAccountNo(), userData.getActionFlag());

            // 根据账号查询
            LambdaQueryWrapper<IAMUser> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(IAMUser::getAccountNo, userData.getAccountNo());
            IAMUser existingUser = iamUserMapper.selectOne(wrapper);

            if (userData.getActionFlag() == 0) {
                // 新增
                if (existingUser != null) {
                    result.put("code", "-1");
                    result.put("msg", "账号已存在");
                    return result;
                }
                userData.setSyncTime(LocalDateTime.now());
                userData.setCreatedAt(LocalDateTime.now());
                userData.setUpdatedAt(LocalDateTime.now());
                iamUserMapper.insert(userData);

                // 同步到用户表
                syncToUser(userData);

                logger.info("User created: {}", userData.getAccountNo());
            } else if (userData.getActionFlag() == 1) {
                // 修改
                if (existingUser == null) {
                    result.put("code", "-1");
                    result.put("msg", "账号不存在");
                    return result;
                }
                userData.setId(existingUser.getId());
                userData.setSyncTime(LocalDateTime.now());
                userData.setUpdatedAt(LocalDateTime.now());
                iamUserMapper.updateById(userData);

                // 同步到用户表
                syncToUser(userData);

                logger.info("User updated: {}", userData.getAccountNo());
            } else if (userData.getActionFlag() == 2) {
                // 删除（停用）
                if (existingUser == null) {
                    result.put("code", "-1");
                    result.put("msg", "账号不存在");
                    return result;
                }
                existingUser.setStatus(0); // 停用
                existingUser.setUpdatedAt(LocalDateTime.now());
                iamUserMapper.updateById(existingUser);

                // 停用用户表中的账号
                disableUser(userData.getAccountNo());

                logger.info("User disabled: {}", userData.getAccountNo());
            } else {
                result.put("code", "-1");
                result.put("msg", "无效的操作标识");
                return result;
            }

            result.put("code", "0");
            result.put("msg", "同步成功");
            return result;

        } catch (Exception e) {
            logger.error("Sync user error", e);
            result.put("code", "-1");
            result.put("msg", "同步失败: " + e.getMessage());
            return result;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> syncJob(Job jobData) {
        Map<String, Object> result = new HashMap<>();

        try {
            logger.info("Syncing job: {}, action: {}", jobData.getCode(), jobData.getActionFlag());

            // 根据职位编码查询
            LambdaQueryWrapper<Job> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Job::getCode, jobData.getCode());
            Job existingJob = jobMapper.selectOne(wrapper);

            if (jobData.getActionFlag() == 0) {
                // 新增
                if (existingJob != null) {
                    result.put("code", "-1");
                    result.put("msg", "职位已存在");
                    return result;
                }
                jobData.setSyncTime(LocalDateTime.now());
                jobData.setCreatedAt(LocalDateTime.now());
                jobData.setUpdatedAt(LocalDateTime.now());
                jobMapper.insert(jobData);

                logger.info("Job created: {}", jobData.getCode());
            } else if (jobData.getActionFlag() == 1) {
                // 修改
                if (existingJob == null) {
                    result.put("code", "-1");
                    result.put("msg", "职位不存在");
                    return result;
                }
                jobData.setId(existingJob.getId());
                jobData.setSyncTime(LocalDateTime.now());
                jobData.setUpdatedAt(LocalDateTime.now());
                jobMapper.updateById(jobData);

                logger.info("Job updated: {}", jobData.getCode());
            } else if (jobData.getActionFlag() == 2) {
                // 删除
                if (existingJob == null) {
                    result.put("code", "-1");
                    result.put("msg", "职位不存在");
                    return result;
                }
                jobMapper.deleteById(existingJob.getId());

                logger.info("Job deleted: {}", jobData.getCode());
            } else {
                result.put("code", "-1");
                result.put("msg", "无效的操作标识");
                return result;
            }

            result.put("code", "0");
            result.put("msg", "同步成功");
            return result;

        } catch (Exception e) {
            logger.error("Sync job error", e);
            result.put("code", "-1");
            result.put("msg", "同步失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 同步到部门表
     */
    private void syncToDepartment(Organization orgData) {
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Department::getExternalId, orgData.getOrgCode());
        Department department = departmentMapper.selectOne(wrapper);

        if (department == null) {
            department = new Department();
            department.setExternalId(orgData.getOrgCode());
            department.setName(orgData.getOrgName());
            department.setCode(orgData.getOrgCode());
            department.setParentId(orgData.getParentCode());
            department.setStatus(orgData.getStatus() == 1 ? "ACTIVE" : "INACTIVE");
            department.setCreatedAt(LocalDateTime.now());
            department.setUpdatedAt(LocalDateTime.now());
            departmentMapper.insert(department);
        } else {
            department.setName(orgData.getOrgName());
            department.setParentId(orgData.getParentCode());
            department.setStatus(orgData.getStatus() == 1 ? "ACTIVE" : "INACTIVE");
            department.setUpdatedAt(LocalDateTime.now());
            departmentMapper.updateById(department);
        }
    }

    /**
     * 删除部门
     */
    private void deleteDepartment(String orgCode) {
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Department::getExternalId, orgCode);
        departmentMapper.delete(wrapper);
    }

    /**
     * 同步到用户表
     */
    private void syncToUser(IAMUser userData) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getExternalId, userData.getAccountNo())
                .or()
                .eq(User::getUsername, userData.getAccountNo());
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            user = new User();
            user.setUsername(userData.getAccountNo());
            user.setRealName(userData.getUserName());
            user.setEmail(userData.getEmail());
            user.setPhone(userData.getMobile());
            user.setExternalId(userData.getAccountNo());
            user.setSource("IAM");
            user.setStatus(userData.getStatus() == 1 ? "ACTIVE" : "INACTIVE");

            // 设置初始密码（根据IAM规范，不同步密码，使用默认密码）
            user.setPassword("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH");

            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.insert(user);
        } else {
            user.setRealName(userData.getUserName());
            user.setEmail(userData.getEmail());
            user.setPhone(userData.getMobile());
            user.setSource("IAM");
            user.setStatus(userData.getStatus() == 1 ? "ACTIVE" : "INACTIVE");
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.updateById(user);
        }
    }

    /**
     * 停用用户
     */
    private void disableUser(String accountNo) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getExternalId, accountNo)
                .or()
                .eq(User::getUsername, accountNo);
        User user = userMapper.selectOne(wrapper);
        if (user != null) {
            user.setStatus("INACTIVE");
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.updateById(user);
        }
    }
}
