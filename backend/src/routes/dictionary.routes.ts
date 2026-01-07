import { Router } from 'express';
import { authenticate } from '../middlewares/auth.middleware';
import prisma from '../utils/prisma';

const router = Router();
router.use(authenticate);

router.get('/', async (req, res, next) => {
  try {
    const dictionaries = await prisma.dictionary.findMany({
      include: { items: { orderBy: { sort: 'asc' } } },
      orderBy: { sort: 'asc' },
    });
    res.json({ success: true, data: dictionaries });
  } catch (error) {
    next(error);
  }
});

export default router;
