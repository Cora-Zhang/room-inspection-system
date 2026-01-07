import jwt from 'jsonwebtoken';
import bcrypt from 'bcryptjs';
import { config } from '../config';
import prisma from '../utils/prisma';
import { ApiError } from '../utils/api-error';

// 登录响应DTO
export interface LoginResponse {
  token: string;
  refreshToken: string;
  user: {
    id: string;
    username: string;
    email?: string;
    realName: string;
    avatar?: string;
    roles: string[];
    permissions: string[];
  };
}

// 登录请求DTO
export interface LoginDto {
  username: string;
  password: string;
}

// 注册请求DTO
export interface RegisterDto {
  username: string;
  password: string;
  email?: string;
  realName: string;
}

// 令牌刷新DTO
export interface RefreshTokenDto {
  refreshToken: string;
}

/**
 * 本地登录服务
 */
export const localLogin = async (dto: LoginDto, ip?: string): Promise<LoginResponse> => {
  // 查询用户
  const user = await prisma.user.findUnique({
    where: { username: dto.username },
    include: {
      roles: {
        include: {
          role: {
            include: {
              permissions: {
                include: {
                  permission: true,
                },
              },
            },
          },
        },
      },
    },
  });

  if (!user) {
    throw ApiError.unauthorized('用户名或密码错误');
  }

  // 检查用户状态
  if (user.status === 'INACTIVE') {
    throw ApiError.forbidden('账号已被禁用');
  }

  if (user.status === 'DELETED') {
    throw ApiError.forbidden('账号已删除');
  }

  // 检查账号是否锁定
  if (user.lockedUntil && user.lockedUntil > new Date()) {
    const remainingMinutes = Math.ceil((user.lockedUntil.getTime() - Date.now()) / 60000);
    throw ApiError.forbidden(`账号已被锁定，请在 ${remainingMinutes} 分钟后重试`);
  }

  // 验证密码
  if (!user.password) {
    throw ApiError.unauthorized('该账号不支持密码登录');
  }

  const isPasswordValid = await bcrypt.compare(dto.password, user.password);

  if (!isPasswordValid) {
    // 增加失败次数
    const failedCount = user.failedLoginCount + 1;
    const updateData: any = { failedLoginCount: failedCount };

    // 超过最大失败次数，锁定账号
    if (failedCount >= config.account.maxFailedAttempts) {
      updateData.lockedUntil = new Date(
        Date.now() + config.account.lockDurationMinutes * 60000
      );
    }

    await prisma.user.update({
      where: { id: user.id },
      data: updateData,
    });

    throw ApiError.unauthorized('用户名或密码错误');
  }

  // 重置失败次数
  if (user.failedLoginCount > 0) {
    await prisma.user.update({
      where: { id: user.id },
      data: {
        failedLoginCount: 0,
        lockedUntil: null,
      },
    });
  }

  // 更新登录信息
  await prisma.user.update({
    where: { id: user.id },
    data: {
      lastLoginAt: new Date(),
      lastLoginIp: ip,
    },
  });

  // 生成JWT令牌
  const token = generateAccessToken(user);
  const refreshToken = generateRefreshToken(user);

  // 提取用户信息
  const roles = user.roles.map((ur) => ur.role.code);
  const permissions = user.roles.flatMap((ur) =>
    ur.role.permissions.map((rp) => rp.permission.code)
  );

  return {
    token,
    refreshToken,
    user: {
      id: user.id,
      username: user.username,
      email: user.email,
      realName: user.realName,
      avatar: user.avatar,
      roles,
      permissions,
    },
  };
};

/**
 * 生成访问令牌
 */
const generateAccessToken = (user: any): string => {
  const payload = {
    userId: user.id,
    username: user.username,
  };

  return jwt.sign(payload, config.jwt.secret, {
    expiresIn: config.jwt.expiresIn,
  });
};

/**
 * 生成刷新令牌
 */
const generateRefreshToken = (user: any): string => {
  const payload = {
    userId: user.id,
    username: user.username,
    type: 'refresh',
  };

  return jwt.sign(payload, config.jwt.secret, {
    expiresIn: config.jwt.refreshExpiresIn,
  });
};

/**
 * 刷新令牌
 */
export const refreshAccessToken = async (dto: RefreshTokenDto): Promise<{ token: string }> => {
  try {
    const decoded = jwt.verify(dto.refreshToken, config.jwt.secret) as any;

    if (decoded.type !== 'refresh') {
      throw ApiError.unauthorized('无效的刷新令牌');
    }

    const user = await prisma.user.findUnique({
      where: { id: decoded.userId },
    });

    if (!user || user.status !== 'ACTIVE') {
      throw ApiError.unauthorized('用户不存在或已被禁用');
    }

    const token = generateAccessToken(user);

    return { token };
  } catch (error) {
    throw ApiError.unauthorized('刷新令牌无效或已过期');
  }
};

/**
 * 验证令牌
 */
export const verifyToken = (token: string): any => {
  try {
    return jwt.verify(token, config.jwt.secret);
  } catch (error) {
    throw ApiError.unauthorized('令牌无效或已过期');
  }
};

/**
 * SSO登录（自动创建用户）
 */
export const ssoLogin = async (
  profile: {
    id: string;
    username: string;
    email?: string;
    realName?: string;
    externalId: string;
  },
  source: 'LDAP' | 'SSO' | 'HR_SYSTEM'
): Promise<LoginResponse> => {
  // 查找或创建用户
  let user = await prisma.user.findUnique({
    where: { externalId: profile.externalId },
    include: {
      roles: {
        include: {
          role: {
            include: {
              permissions: {
                include: {
                  permission: true,
                },
              },
            },
          },
        },
      },
    },
  });

  if (!user) {
    // 创建新用户
    const defaultRole = await prisma.role.findFirst({
      where: { code: 'user' },
    });

    user = await prisma.user.create({
      data: {
        username: profile.username,
        email: profile.email,
        realName: profile.realName || profile.username,
        source: source as any,
        externalId: profile.externalId,
        status: 'ACTIVE',
        lastLoginAt: new Date(),
        roles: defaultRole
          ? {
              create: {
                role: {
                  connect: { id: defaultRole.id },
                },
              },
            }
          : undefined,
      },
      include: {
        roles: {
          include: {
            role: {
              include: {
                permissions: {
                  include: {
                    permission: true,
                  },
                },
              },
            },
          },
        },
      },
    });
  } else {
    // 更新最后登录时间
    user = await prisma.user.update({
      where: { id: user.id },
      data: {
        lastLoginAt: new Date(),
        email: user.email || profile.email,
        realName: user.realName || profile.realName,
      },
      include: {
        roles: {
          include: {
            role: {
              include: {
                permissions: {
                  include: {
                    permission: true,
                  },
                },
              },
            },
          },
        },
      },
    });
  }

  // 生成令牌
  const token = generateAccessToken(user);
  const refreshToken = generateRefreshToken(user);

  const roles = user.roles.map((ur) => ur.role.code);
  const permissions = user.roles.flatMap((ur) =>
    ur.role.permissions.map((rp) => rp.permission.code)
  );

  return {
    token,
    refreshToken,
    user: {
      id: user.id,
      username: user.username,
      email: user.email,
      realName: user.realName,
      avatar: user.avatar,
      roles,
      permissions,
    },
  };
};

/**
 * 修改密码
 */
export const changePassword = async (
  userId: string,
  oldPassword: string,
  newPassword: string
): Promise<void> => {
  const user = await prisma.user.findUnique({
    where: { id: userId },
  });

  if (!user || !user.password) {
    throw ApiError.notFound('用户不存在');
  }

  const isPasswordValid = await bcrypt.compare(oldPassword, user.password);

  if (!isPasswordValid) {
    throw ApiError.badRequest('原密码错误');
  }

  // 验证密码强度
  validatePassword(newPassword);

  // 加密新密码
  const hashedPassword = await bcrypt.hash(newPassword, 10);

  await prisma.user.update({
    where: { id: userId },
    data: { password: hashedPassword },
  });
};

/**
 * 重置密码（管理员）
 */
export const resetPassword = async (userId: string, newPassword: string): Promise<void> => {
  validatePassword(newPassword);

  const hashedPassword = await bcrypt.hash(newPassword, 10);

  await prisma.user.update({
    where: { id: userId },
    data: {
      password: hashedPassword,
      failedLoginCount: 0,
      lockedUntil: null,
    },
  });
};

/**
 * 密码强度验证
 */
const validatePassword = (password: string): void => {
  if (password.length < config.password.minLength) {
    throw ApiError.badRequest(`密码长度不能少于 ${config.password.minLength} 位`);
  }

  if (config.password.requireUppercase && !/[A-Z]/.test(password)) {
    throw ApiError.badRequest('密码必须包含大写字母');
  }

  if (config.password.requireLowercase && !/[a-z]/.test(password)) {
    throw ApiError.badRequest('密码必须包含小写字母');
  }

  if (config.password.requireNumber && !/\d/.test(password)) {
    throw ApiError.badRequest('密码必须包含数字');
  }

  if (config.password.requireSpecialChar && !/[!@#$%^&*(),.?":{}|<>]/.test(password)) {
    throw ApiError.badRequest('密码必须包含特殊字符');
  }
};
