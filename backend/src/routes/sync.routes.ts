import { Router } from 'express';
import { authenticate, requireAdmin } from '../middlewares/auth.middleware';
import prisma from '../utils/prisma';

const router = Router();
router.use(authenticate, requireAdmin);

router.post('/trigger', async (req, res, next) => {
  try {
    res.json({ success: true, message: '同步任务已启动' });
  } catch (error) {
    next(error);
  }
});

router.get('/logs', async (req, res, next) => {
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

export default router;
