import { Router } from 'express';
import { body, validationResult } from 'express-validator';
import { authenticate } from '../middlewares/auth.middleware';
import {
  localLogin,
  refreshAccessToken,
  changePassword,
  resetPassword,
} from '../services/auth.service';
import { requireAdmin } from '../middlewares/rbac.middleware';
import prisma from '../utils/prisma';

const router = Router();

// 本地登录
router.post(
  '/login',
  [
    body('username').notEmpty().withMessage('用户名不能为空'),
    body('password').notEmpty().withMessage('密码不能为空'),
  ],
  async (req, res, next) => {
    try {
      const errors = validationResult(req);
      if (!errors.isEmpty()) {
        return res.status(400).json({
          success: false,
          error: {
            code: 'VALIDATION_ERROR',
            message: '参数验证失败',
            details: errors.array(),
          },
        });
      }

      const ip = req.ip;
      const result = await localLogin(req.body, ip);

      res.json({
        success: true,
        data: result,
        message: '登录成功',
      });
    } catch (error) {
      next(error);
    }
  }
);

// 刷新令牌
router.post(
  '/refresh',
  [body('refreshToken').notEmpty().withMessage('刷新令牌不能为空')],
  async (req, res, next) => {
    try {
      const errors = validationResult(req);
      if (!errors.isEmpty()) {
        return res.status(400).json({
          success: false,
          error: {
            code: 'VALIDATION_ERROR',
            message: '参数验证失败',
            details: errors.array(),
          },
        });
      }

      const result = await refreshAccessToken(req.body);

      res.json({
        success: true,
        data: result,
      });
    } catch (error) {
      next(error);
    }
  }
);

// 获取当前用户信息
router.get('/me', authenticate, async (req, res, next) => {
  try {
    if (!req.user) {
      return res.status(401).json({
        success: false,
        error: { code: 'UNAUTHORIZED', message: '未登录' },
      });
    }

    const user = await prisma.user.findUnique({
      where: { id: req.user.id },
      include: {
        department: true,
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
      return res.status(404).json({
        success: false,
        error: { code: 'NOT_FOUND', message: '用户不存在' },
      });
    }

    const roles = user.roles.map((ur) => ur.role.code);
    const permissions = user.roles.flatMap((ur) =>
      ur.role.permissions.map((rp) => rp.permission.code)
    );

    res.json({
      success: true,
      data: {
        id: user.id,
        username: user.username,
        email: user.email,
        realName: user.realName,
        avatar: user.avatar,
        phone: user.phone,
        employeeId: user.employeeId,
        department: user.department,
        roles,
        permissions,
      },
    });
  } catch (error) {
    next(error);
  }
});

// 修改密码
router.post(
  '/change-password',
  authenticate,
  [
    body('oldPassword').notEmpty().withMessage('原密码不能为空'),
    body('newPassword').isLength({ min: 8 }).withMessage('新密码不能少于8位'),
  ],
  async (req, res, next) => {
    try {
      const errors = validationResult(req);
      if (!errors.isEmpty()) {
        return res.status(400).json({
          success: false,
          error: {
            code: 'VALIDATION_ERROR',
            message: '参数验证失败',
            details: errors.array(),
          },
        });
      }

      if (!req.user) {
        return res.status(401).json({
          success: false,
          error: { code: 'UNAUTHORIZED', message: '未登录' },
        });
      }

      await changePassword(req.user.id, req.body.oldPassword, req.body.newPassword);

      res.json({
        success: true,
        message: '密码修改成功',
      });
    } catch (error) {
      next(error);
    }
  }
);

// 重置密码（管理员）
router.post(
  '/reset-password',
  authenticate,
  requireAdmin,
  [body('userId').notEmpty().withMessage('用户ID不能为空'), body('newPassword').isLength({ min: 8 }).withMessage('新密码不能少于8位')],
  async (req, res, next) => {
    try {
      const errors = validationResult(req);
      if (!errors.isEmpty()) {
        return res.status(400).json({
          success: false,
          error: {
            code: 'VALIDATION_ERROR',
            message: '参数验证失败',
            details: errors.array(),
          },
        });
      }

      await resetPassword(req.body.userId, req.body.newPassword);

      res.json({
        success: true,
        message: '密码重置成功',
      });
    } catch (error) {
      next(error);
    }
  }
);

// 登出（客户端删除token即可，无需后端特殊处理）
router.post('/logout', authenticate, (req, res) => {
  res.json({
    success: true,
    message: '登出成功',
  });
});

export default router;
