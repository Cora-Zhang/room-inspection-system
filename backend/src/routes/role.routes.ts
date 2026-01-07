import { Router } from 'express';
import { authenticate } from '../middlewares/auth.middleware';
import { authorize } from '../middlewares/rbac.middleware';
import prisma from '../utils/prisma';

const router = Router();
router.use(authenticate);

router.get('/', authorize('role:read'), async (req, res, next) => {
  try {
    const roles = await prisma.role.findMany({
      include: {
        permissions: {
          include: { permission: true },
        },
      },
    });
    res.json({ success: true, data: roles });
  } catch (error) {
    next(error);
  }
});

router.post('/', authorize('role:create'), async (req, res, next) => {
  try {
    const { permissionIds, ...roleData } = req.body;

    const role = await prisma.role.create({
      data: roleData,
    });

    if (permissionIds && permissionIds.length > 0) {
      await prisma.rolePermission.createMany({
        data: permissionIds.map((pid: string) => ({
          roleId: role.id,
          permissionId: pid,
        })),
      });
    }

    res.status(201).json({ success: true, data: role, message: '角色创建成功' });
  } catch (error) {
    next(error);
  }
});

router.put('/:id', authorize('role:update'), async (req, res, next) => {
  try {
    const role = await prisma.role.update({
      where: { id: req.params.id },
      data: req.body,
    });
    res.json({ success: true, data: role, message: '角色更新成功' });
  } catch (error) {
    next(error);
  }
});

router.delete('/:id', authorize('role:delete'), async (req, res, next) => {
  try {
    await prisma.role.delete({
      where: { id: req.params.id },
    });
    res.json({ success: true, message: '角色删除成功' });
  } catch (error) {
    next(error);
  }
});

export default router;
