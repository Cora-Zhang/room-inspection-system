import crypto from 'crypto';
import prisma from '../utils/prisma';
import { ApiError } from '../utils/api-error';

// 用户数据同步接口定义
interface SyncUser {
  userId: string;
  username: string;
  realName: string;
  email?: string;
  phone?: string;
  departmentId?: string;
  position?: string;
  employeeId?: string;
  status: 'ACTIVE' | 'INACTIVE' | 'DELETED';
}

// 组织数据同步接口定义
interface SyncOrganization {
  orgId: string;
  orgCode: string;
  orgName: string;
  parentId?: string;
  level: number;
  path: string;
  status: 'ACTIVE' | 'INACTIVE';
}

// 同步数据请求体
interface SyncRequestBody {
  appId: string;
  timestamp: number;
  data: SyncUser[] | SyncOrganization[];
  signature: string;
  type: 'USER' | 'ORGANIZATION';
}

// 用户数据同步服务
export class SyncService {
  /**
   * 验证同步请求签名
   * 签名算法: MD5(appId + appSecret + timestamp)
   */
  static verifySignature(
    appId: string,
    appSecret: string,
    timestamp: number,
    signature: string
  ): boolean {
    const signString = `${appId}${appSecret}${timestamp}`;
    const calculatedSignature = crypto
      .createHash('md5')
      .update(signString)
      .digest('hex');

    return calculatedSignature === signature;
  }

  /**
   * 检查时间戳是否有效（5分钟内）
   */
  static isTimestampValid(timestamp: number): boolean {
    const now = Date.now();
    const diff = Math.abs(now - timestamp);
    return diff < 5 * 60 * 1000; // 5分钟
  }

  /**
   * 根据appId获取SSO配置
   */
  static async getSSOConfigByAppId(appId: string): Promise<any> {
    const configs = await prisma.sSOConfig.findMany();

    for (const config of configs) {
      const configData = JSON.parse(config.config);
      if (configData.appId === appId) {
        return { config, appSecret: configData.appSecret };
      }
    }

    throw new ApiError(401, 'INVALID_APPID', '无效的appId');
  }

  /**
   * 同步用户数据
   */
  static async syncUsers(users: SyncUser[]): Promise<{
    success: number;
    failed: number;
    errors: string[];
  }> {
    let success = 0;
    let failed = 0;
    const errors: string[] = [];

    for (const userData of users) {
      try {
        // 删除用户
        if (userData.status === 'DELETED') {
          await prisma.user.deleteMany({
            where: {
              externalId: userData.userId,
              source: 'SSO',
            },
          });
          success++;
          continue;
        }

        // 查找或创建用户
        const existingUser = await prisma.user.findFirst({
          where: {
            externalId: userData.userId,
            source: 'SSO',
          },
        });

        if (existingUser) {
          // 更新用户
          await prisma.user.update({
            where: { id: existingUser.id },
            data: {
              username: userData.username,
              email: userData.email || existingUser.email,
              realName: userData.realName,
              phone: userData.phone,
              position: userData.position,
              employeeId: userData.employeeId,
              status: userData.status as any,
              updatedAt: new Date(),
            },
          });
        } else {
          // 创建用户
          await prisma.user.create({
            data: {
              username: userData.username,
              email: userData.email || `${userData.username}@temp.com`,
              realName: userData.realName,
              phone: userData.phone,
              position: userData.position,
              employeeId: userData.employeeId,
              externalId: userData.userId,
              status: userData.status as any,
              source: 'SSO',
            },
          });
        }

        success++;
      } catch (error: any) {
        failed++;
        errors.push(`用户 ${userData.userId}: ${error.message}`);
      }
    }

    return { success, failed, errors };
  }

  /**
   * 同步组织数据
   */
  static async syncOrganizations(
    orgs: SyncOrganization[]
  ): Promise<{
    success: number;
    failed: number;
    errors: string[];
  }> {
    let success = 0;
    let failed = 0;
    const errors: string[] = [];

    // 按层级排序，先处理父级
    const sortedOrgs = orgs.sort((a, b) => a.level - b.level);

    for (const orgData of sortedOrgs) {
      try {
        // 删除组织
        if (orgData.status === 'INACTIVE') {
          await prisma.department.updateMany({
            where: { externalId: orgData.orgId },
            data: { status: 'INACTIVE' },
          });
          success++;
          continue;
        }

        // 查找或创建组织
        const existingOrg = await prisma.department.findFirst({
          where: { externalId: orgData.orgId },
        });

        if (existingOrg) {
          // 更新组织
          await prisma.department.update({
            where: { id: existingOrg.id },
            data: {
              code: orgData.orgCode,
              name: orgData.orgName,
              level: orgData.level,
              path: orgData.path,
              status: 'ACTIVE' as any,
              updatedAt: new Date(),
            },
          });
        } else {
          // 创建组织
          await prisma.department.create({
            data: {
              code: orgData.orgCode,
              name: orgData.orgName,
              level: orgData.level,
              path: orgData.path,
              status: 'ACTIVE' as any,
              externalId: orgData.orgId,
            },
          });
        }

        success++;
      } catch (error: any) {
        failed++;
        errors.push(`组织 ${orgData.orgId}: ${error.message}`);
      }
    }

    return { success, failed, errors };
  }

  /**
   * 处理同步请求
   */
  static async handleSyncRequest(
    body: SyncRequestBody
  ): Promise<{
    success: boolean;
    message: string;
    data?: {
      total: number;
      success: number;
      failed: number;
      errors?: string[];
    };
  }> {
    // 1. 验证时间戳
    if (!this.isTimestampValid(body.timestamp)) {
      throw new ApiError(401, 'INVALID_TIMESTAMP', '时间戳无效或已过期');
    }

    // 2. 获取SSO配置和appSecret
    const { config, appSecret } = await this.getSSOConfigByAppId(body.appId);

    // 3. 验证签名
    if (!this.verifySignature(body.appId, appSecret, body.timestamp, body.signature)) {
      throw new ApiError(401, 'INVALID_SIGNATURE', '签名验证失败');
    }

    // 4. 根据类型执行同步
    let result;
    if (body.type === 'USER') {
      result = await this.syncUsers(body.data as SyncUser[]);
    } else if (body.type === 'ORGANIZATION') {
      result = await this.syncOrganizations(body.data as SyncOrganization[]);
    } else {
      throw new ApiError(400, 'INVALID_TYPE', '无效的同步类型');
    }

    // 5. 记录同步日志
    await prisma.syncLog.create({
      data: {
        type: body.type === 'USER' ? 'USER' : 'ORGANIZATION',
        adapter: config.provider,
        status: result.failed === 0 ? 'SUCCESS' : 'PARTIAL',
        startTime: new Date(body.timestamp),
        endTime: new Date(),
        totalUsers: body.type === 'USER' ? body.data.length : 0,
        successUsers: body.type === 'USER' ? result.success : 0,
        failedUsers: body.type === 'USER' ? result.failed : 0,
        totalOrgs: body.type === 'ORGANIZATION' ? body.data.length : 0,
        successOrgs: body.type === 'ORGANIZATION' ? result.success : 0,
        failedOrgs: body.type === 'ORGANIZATION' ? result.failed : 0,
        errorMessage: result.errors.length > 0 ? result.errors.join('\n') : null,
        metadata: JSON.stringify({ appId: body.appId }),
      },
    });

    return {
      success: true,
      message: '同步成功',
      data: {
        total: body.data.length,
        success: result.success,
        failed: result.failed,
        errors: result.errors.length > 0 ? result.errors : undefined,
      },
    };
  }
}
