package com.roominspection.backend.service;

import com.roominspection.backend.entity.Job;
import com.roominspection.backend.entity.IAMUser;
import com.roominspection.backend.entity.Organization;

import java.util.Map;

/**
 * 数据同步服务接口
 */
public interface DataSyncService {

    /**
     * 同步组织数据
     *
     * @param orgData 组织数据
     * @return 同步结果
     */
    Map<String, Object> syncOrganization(Organization orgData);

    /**
     * 同步用户数据
     *
     * @param userData 用户数据
     * @return 同步结果
     */
    Map<String, Object> syncUser(IAMUser userData);

    /**
     * 同步职位数据
     *
     * @param jobData 职位数据
     * @return 同步结果
     */
    Map<String, Object> syncJob(Job jobData);
}
