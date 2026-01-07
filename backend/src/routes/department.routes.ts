import { Router } from 'express';
import { authenticate } from '../middlewares/auth.middleware';
import { authorize } from '../middlewares/rbac.middleware';
import prisma from '../utils/prisma';

const router = Router();
router.use(authenticate);

router.get('/', authorize('department:read'), async (req, res, next) => {
  try {
    const departments = await prisma.department.findMany({
      include: {
        children: true,
        _count: { select: { users: true } },
      },
      orderBy: { sort: 'asc' },
    });
    const tree = buildTree(departments);
    res.json({ success: true, data: tree });
  } catch (error) {
    next(error);
  }
});

function buildTree(departments: any[], parentId: string | null = null) {
  return departments
    .filter(d => d.parentId === parentId)
    .map(d => ({
      ...d,
      children: buildTree(departments, d.id),
    }));
}

export default router;
