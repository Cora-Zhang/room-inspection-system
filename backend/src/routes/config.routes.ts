import { Router } from 'express';
import { authenticate, requireAdmin } from '../middlewares/auth.middleware';
import prisma from '../utils/prisma';

const router = Router();

// 公开配置（无需认证）
router.get('/public', async (req, res, next) => {
  try {
    const configs = await prisma.systemConfig.findMany({
      where: { isPublic: true },
    });
    const publicConfig = configs.reduce((acc, c) => {
      acc[c.key] = c.value;
      return acc;
    }, {} as Record<string, string>);
    res.json({ success: true, data: publicConfig });
  } catch (error) {
    next(error);
  }
});

// 管理员配置
router.get('/', authenticate, requireAdmin, async (req, res, next) => {
  try {
    const configs = await prisma.systemConfig.findMany();
    res.json({ success: true, data: configs });
  } catch (error) {
    next(error);
  }
});

export default router;
