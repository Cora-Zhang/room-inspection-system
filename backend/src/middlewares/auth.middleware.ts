import { Request, Response, NextFunction } from 'express';
import jwt from 'jsonwebtoken';
import { config } from '../config';
import { ApiError } from '../utils/api-error';
import prisma from '../utils/prisma';

// JWT载荷接口
interface JwtPayload {
  userId: string;
  username: string;
  iat: number;
  exp: number;
}

// 验证JWT令牌
export const authenticate = async (
  req: Request,
  res: Response,
  next: NextFunction
): Promise<void> => {
  try {
    // 从Authorization头获取token
    const authHeader = req.headers.authorization;

    if (!authHeader || !authHeader.startsWith('Bearer ')) {
      throw ApiError.unauthorized('缺少认证令牌');
    }

    const token = authHeader.substring(7); // 移除 'Bearer ' 前缀

    // 验证JWT
    const decoded = jwt.verify(token, config.jwt.secret) as JwtPayload;

    // 查询用户信息
    const user = await prisma.user.findUnique({
      where: { id: decoded.userId },
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
      throw ApiError.unauthorized('用户不存在');
    }

    // 检查用户状态
    if (user.status !== 'ACTIVE') {
      throw ApiError.forbidden('账号已被禁用或锁定');
    }

    // 检查账号是否锁定
    if (user.lockedUntil && user.lockedUntil > new Date()) {
      throw ApiError.forbidden('账号已被锁定，请稍后再试');
    }

    // 将用户信息附加到请求对象
    req.user = {
      id: user.id,
      username: user.username,
      email: user.email,
      roles: user.roles.map((ur) => ur.role.code),
      permissions: user.roles.flatMap((ur) =>
        ur.role.permissions.map((rp) => rp.permission.code)
      ),
    };

    next();
  } catch (error) {
    if (error instanceof ApiError) {
      next(error);
      return;
    }

    if (error instanceof jwt.JsonWebTokenError) {
      next(ApiError.unauthorized('无效的认证令牌'));
      return;
    }

    if (error instanceof jwt.TokenExpiredError) {
      next(ApiError.unauthorized('认证令牌已过期'));
      return;
    }

    next(error);
  }
};

// 可选认证（不强制要求登录）
export const optionalAuth = async (
  req: Request,
  res: Response,
  next: NextFunction
): Promise<void> => {
  try {
    const authHeader = req.headers.authorization;

    if (!authHeader || !authHeader.startsWith('Bearer ')) {
      next();
      return;
    }

    const token = authHeader.substring(7);
    const decoded = jwt.verify(token, config.jwt.secret) as JwtPayload;

    const user = await prisma.user.findUnique({
      where: { id: decoded.userId },
    });

    if (user && user.status === 'ACTIVE') {
      req.user = {
        id: user.id,
        username: user.username,
        email: user.email,
        roles: [],
        permissions: [],
      };
    }

    next();
  } catch (error) {
    // 静默失败，继续请求
    next();
  }
};
