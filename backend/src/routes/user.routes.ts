import { Router } from 'express';
import { authenticate, isSelfOrAdmin } from '../middlewares/auth.middleware';
import { requireAdmin } from '../middlewares/rbac.middleware';
import { authorize } from '../middlewares/rbac.middleware';
import prisma from '../utils/prisma';

const router = Router();

// 所有路由都需要认证
router.use(authenticate);

// 获取用户列表
router.get('/', authorize('user:read'), async (req, res, next) => {
  try {
    const { page = 1, pageSize = 20, status, departmentId } = req.query;
    const skip = (Number(page) - 1) * Number(pageSize);

    const where: any = {};
    if (status) where.status = status;
    if (departmentId) where.departmentId = departmentId;

    const [users, total] = await Promise.all([
      prisma.user.findMany({
        where,
        skip,
        take: Number(pageSize),
        include: {
          department: true,
          roles: {
            include: { role: true },
          },
        },
        orderBy: { createdAt: 'desc' },
      }),
      prisma.user.count({ where }),
    ]);

    res.json({
      success: true,
      data: users,
      pagination: {
        page: Number(page),
        pageSize: Number(pageSize),
        total,
        totalPages: Math.ceil(total / Number(pageSize)),
      },
    });
  } catch (error) {
    next(error);
  }
});

// 获取用户详情
router.get('/:id', isSelfOrAdmin('id'), async (req, res, next) => {
  try {
    const user = await prisma.user.findUnique({
      where: { id: req.params.id },
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

    res.json({ success: true, data: user });
  } catch (error) {
    next(error);
  }
});

// 创建用户
router.post('/', authorize('user:create'), async (req, res, next) => {
  try {
    const { roleId, ...userData } = req.body;
    const hashedPassword = await import('bcryptjs').then(bcrypt => bcrypt.hash(userData.password, 10));

    const user = await prisma.user.create({
      data: {
        ...userData,
        password: hashedPassword,
      },
    });

    // 分配角色
    if (roleId) {
      await prisma.userRole.create({
        data: {
          userId: user.id,
          roleId,
        },
      });
    }

    res.status(201).json({ success: true, data: user, message: '用户创建成功' });
  } catch (error) {
    next(error);
  }
});

// 更新用户
router.put('/:id', isSelfOrAdmin('id'), async (req, res, next) => {
  try {
    const { password, roles, ...updateData } = req.body;

    const user = await prisma.user.update({
      where: { id: req.params.id },
      data: updateData,
    });

    res.json({ success: true, data: user, message: '用户更新成功' });
  } catch (error) {
    next(error);
  }
});

// 删除用户
router.delete('/:id', requireAdmin, async (req, res, next) => {
  try {
    await prisma.user.delete({
      where: { id: req.params.id },
    });

    res.json({ success: true, message: '用户删除成功' });
  } catch (error) {
    next(error);
  }
});

export default router;
