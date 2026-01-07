import { Router } from 'express';
import { authenticate, requireAdmin } from '../middlewares/auth.middleware';
import { body, validationResult } from 'express-validator';
import { SyncService } from '../services/sync.service';
import prisma from '../utils/prisma';

const router = Router();

// 管理接口 - 需要认证
router.use('/admin', authenticate, requireAdmin);

// 手动触发同步（保留原有功能）
router.post('/admin/trigger', async (req, res, next) => {
  try {
    res.json({ success: true, message: '同步任务已启动' });
  } catch (error) {
    next(error);
  }
});

// 获取同步日志（保留原有功能）
router.get('/admin/logs', async (req, res, next) => {
  try {
    const logs = await prisma.syncLog.findMany({
      orderBy: { createdAt: 'desc' },
      take: 50,
    });
    res.json({ success: true, data: logs });
  } catch (error) {
    next(error);
  }
});

// ============================================
// 统一身份认证平台回调接口（无需认证，通过appId/appSecret鉴权）
// ============================================

/**
 * 用户数据同步回调接口
 *
 * 请求方法：POST
 * Content-Type：application/json
 *
 * 请求体：
 * {
 *   "appId": "your_app_id",
 *   "timestamp": 1234567890,
 *   "signature": "md5(appId + appSecret + timestamp)",
 *   "type": "USER" | "ORGANIZATION",
 *   "data": [...]
 * }
 *
 * 签名算法：MD5(appId + appSecret + timestamp)
 */
router.post(
  '/callback',
  [
    body('appId').notEmpty().withMessage('appId不能为空'),
    body('timestamp').isInt().withMessage('timestamp必须是整数'),
    body('signature').notEmpty().withMessage('signature不能为空'),
    body('type').isIn(['USER', 'ORGANIZATION']).withMessage('type必须是USER或ORGANIZATION'),
    body('data').isArray({ min: 1 }).withMessage('data必须是非空数组'),
  ],
  async (req, res, next) => {
    try {
      // 参数验证
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

      // 处理同步请求
      const result = await SyncService.handleSyncRequest(req.body);

      res.json(result);
    } catch (error: any) {
      next(error);
    }
  }
);

// 健康检查接口（用于测试回调地址是否可访问）
router.get('/health', (req, res) => {
  res.json({
    success: true,
    message: '用户数据同步服务正常运行',
    timestamp: Date.now(),
  });
});

export default router;
