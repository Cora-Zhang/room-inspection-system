import { Request, Response, NextFunction } from 'express';
import { ApiError } from '../utils/api-error';

// 权限检查中间件
export const authorize = (...requiredPermissions: string[]) => {
  return (req: Request, res: Response, next: NextFunction): void => {
    // 确保用户已认证
    if (!req.user) {
      throw ApiError.unauthorized('用户未登录');
    }

    // 如果没有指定权限要求，通过
    if (!requiredPermissions || requiredPermissions.length === 0) {
      next();
      return;
    }

    // 检查用户是否拥有所需权限
    const userPermissions = req.user.permissions || [];
    const hasPermission = requiredPermissions.every((permission) =>
      userPermissions.includes(permission)
    );

    if (!hasPermission) {
      throw ApiError.forbidden('权限不足');
    }

    next();
  };
};

// 角色检查中间件
export const requireRole = (...requiredRoles: string[]) => {
  return (req: Request, res: Response, next: NextFunction): void => {
    if (!req.user) {
      throw ApiError.unauthorized('用户未登录');
    }

    if (!requiredRoles || requiredRoles.length === 0) {
      next();
      return;
    }

    const userRoles = req.user.roles || [];
    const hasRole = requiredRoles.some((role) => userRoles.includes(role));

    if (!hasRole) {
      throw ApiError.forbidden('角色权限不足');
    }

    next();
  };
};

// 管理员权限检查
export const requireAdmin = (req: Request, res: Response, next: NextFunction): void => {
  if (!req.user) {
    throw ApiError.unauthorized('用户未登录');
  }

  const userRoles = req.user.roles || [];
  const isAdmin = userRoles.includes('admin') || userRoles.includes('super_admin');

  if (!isAdmin) {
    throw ApiError.forbidden('需要管理员权限');
  }

  next();
};

// 检查是否是本人或管理员
export const isSelfOrAdmin = (userIdParam: string = 'userId') => {
  return (req: Request, res: Response, next: NextFunction): void => {
    if (!req.user) {
      throw ApiError.unauthorized('用户未登录');
    }

    const targetUserId = req.params[userIdParam] || req.body[userIdParam];
    const userRoles = req.user.roles || [];
    const isAdmin = userRoles.includes('admin') || userRoles.includes('super_admin');

    if (req.user.id === targetUserId || isAdmin) {
      next();
      return;
    }

    throw ApiError.forbidden('无权访问其他用户的数据');
  };
};
