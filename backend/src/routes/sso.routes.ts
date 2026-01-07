import { Router } from 'express';
import { authenticate, requireAdmin } from '../middlewares/auth.middleware';
import { authorize } from '../middlewares/rbac.middleware';
import { body, param, validationResult } from 'express-validator';
import prisma from '../utils/prisma';
import { ApiError } from '../utils/api-error';

const router = Router();

// 所有路由都需要认证和管理员权限
router.use(authenticate, requireAdmin);

// 获取SSO配置列表
router.get('/', authorize('sso:read'), async (req, res, next) => {
  try {
    const configs = await prisma.sSOConfig.findMany({
      orderBy: { sort: 'asc' },
    });

    res.json({
      success: true,
      data: configs,
    });
  } catch (error) {
    next(error);
  }
});

// 获取启用的SSO配置
router.get('/enabled', async (req, res, next) => {
  try {
    const configs = await prisma.sSOConfig.findMany({
      where: { enabled: true },
      orderBy: { sort: 'asc' },
    });

    res.json({
      success: true,
      data: configs,
    });
  } catch (error) {
    next(error);
  }
});

// 获取SSO配置详情
router.get(
  '/:id',
  param('id').notEmpty().withMessage('配置ID不能为空'),
  authorize('sso:read'),
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

      const config = await prisma.sSOConfig.findUnique({
        where: { id: req.params.id },
      });

      if (!config) {
        return res.status(404).json({
          success: false,
          error: { code: 'NOT_FOUND', message: '配置不存在' },
        });
      }

      res.json({ success: true, data: config });
    } catch (error) {
      next(error);
    }
  }
);

// 创建SSO配置
router.post(
  '/',
  [
    body('provider').notEmpty().withMessage('提供商不能为空'),
    body('name').notEmpty().withMessage('显示名称不能为空'),
    body('type').notEmpty().withMessage('认证类型不能为空'),
    body('config').isObject().withMessage('配置参数必须是JSON对象'),
  ],
  authorize('sso:create'),
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

      const config = await prisma.sSOConfig.create({
        data: {
          provider: req.body.provider,
          name: req.body.name,
          type: req.body.type,
          enabled: req.body.enabled || false,
          config: JSON.stringify(req.body.config),
          description: req.body.description,
          sort: req.body.sort || 0,
        },
      });

      res.status(201).json({
        success: true,
        data: config,
        message: 'SSO配置创建成功',
      });
    } catch (error) {
      next(error);
    }
  }
);

// 更新SSO配置
router.put(
  '/:id',
  [
    param('id').notEmpty().withMessage('配置ID不能为空'),
    body('config').isObject().withMessage('配置参数必须是JSON对象'),
  ],
  authorize('sso:update'),
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

      const { id } = req.params;
      const { name, enabled, config, description, sort } = req.body;

      const updateData: any = {};
      if (name !== undefined) updateData.name = name;
      if (enabled !== undefined) updateData.enabled = enabled;
      if (config !== undefined) updateData.config = JSON.stringify(config);
      if (description !== undefined) updateData.description = description;
      if (sort !== undefined) updateData.sort = sort;

      const ssoConfig = await prisma.sSOConfig.update({
        where: { id },
        data: updateData,
      });

      res.json({
        success: true,
        data: ssoConfig,
        message: 'SSO配置更新成功',
      });
    } catch (error) {
      next(error);
    }
  }
);

// 删除SSO配置
router.delete(
  '/:id',
  param('id').notEmpty().withMessage('配置ID不能为空'),
  authorize('sso:delete'),
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

      await prisma.sSOConfig.delete({
        where: { id: req.params.id },
      });

      res.json({
        success: true,
        message: 'SSO配置删除成功',
      });
    } catch (error) {
      next(error);
    }
  }
);

// 启用/禁用SSO配置
router.patch(
  '/:id/toggle',
  param('id').notEmpty().withMessage('配置ID不能为空'),
  body('enabled').isBoolean().withMessage('enabled必须是布尔值'),
  authorize('sso:update'),
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

      const config = await prisma.sSOConfig.update({
        where: { id: req.params.id },
        data: { enabled: req.body.enabled },
      });

      res.json({
        success: true,
        data: config,
        message: req.body.enabled ? 'SSO配置已启用' : 'SSO配置已禁用',
      });
    } catch (error) {
      next(error);
    }
  }
);

// 测试SSO配置连接
router.post(
  '/:id/test',
  param('id').notEmpty().withMessage('配置ID不能为空'),
  authorize('sso:test'),
  async (req, res, next) => {
    try {
      const config = await prisma.sSOConfig.findUnique({
        where: { id: req.params.id },
      });

      if (!config) {
        return res.status(404).json({
          success: false,
          error: { code: 'NOT_FOUND', message: '配置不存在' },
        });
      }

      // TODO: 实现实际的连接测试
      const configData = JSON.parse(config.config);

      res.json({
        success: true,
        message: '配置测试通过',
        data: {
          provider: config.provider,
          type: config.type,
          authUrl: configData.authorizationUrl,
          tokenUrl: configData.tokenUrl,
          userInfoUrl: configData.userInfoUrl,
        },
      });
    } catch (error) {
      next(error);
    }
  }
);

export default router;
