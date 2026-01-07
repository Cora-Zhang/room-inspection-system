import { Router } from 'express';
import { authenticate } from '../middlewares/auth.middleware';
import { authorize } from '../middlewares/rbac.middleware';
import prisma from '../utils/prisma';

const router = Router();
router.use(authenticate);

router.get('/', authorize('permission:read'), async (req, res, next) => {
  try {
    const permissions = await prisma.permission.findMany({
      orderBy: [{ resource: 'asc' }, { action: 'asc' }],
    });
    res.json({ success: true, data: permissions });
  } catch (error) {
    next(error);
  }
});

router.get('/tree', authorize('permission:read'), async (req, res, next) => {
  try {
    const permissions = await prisma.permission.findMany();
    const tree = buildPermissionTree(permissions);
    res.json({ success: true, data: tree });
  } catch (error) {
    next(error);
  }
});

router.post('/', authorize('permission:create'), async (req, res, next) => {
  try {
    const permission = await prisma.permission.create({
      data: req.body,
    });
    res.status(201).json({ success: true, data: permission, message: '权限创建成功' });
  } catch (error) {
    next(error);
  }
});

function buildPermissionTree(permissions: any[]) {
  const map = new Map();
  permissions.forEach(p => map.set(p.id, { ...p, children: [] }));
  const roots: any[] = [];
  permissions.forEach(p => {
    const node = map.get(p.id);
    roots.push(node);
  });
  return roots;
}

export default router;
