import { NextRequest, NextResponse } from 'next/server';
import type { User } from '@/types';

// Mock用户数据
let mockUsers: User[] = [
  {
    id: 1,
    username: 'admin',
    name: '系统管理员',
    email: 'admin@example.com',
    phone: '13800138000',
    avatar: '/api/uploads/avatars/admin.png',
    status: 'active',
    departmentId: 1,
    position: 'IT经理',
    lastLoginTime: '2025-01-07 14:30:00',
    createTime: '2025-01-01 00:00:00',
    updateTime: '2025-01-07 14:30:00',
    creator: 'system',
  },
  {
    id: 2,
    username: 'zhangsan',
    name: '张三',
    email: 'zhangsan@example.com',
    phone: '13800138001',
    avatar: '/api/uploads/avatars/zhangsan.png',
    status: 'active',
    departmentId: 2,
    position: '巡检员',
    lastLoginTime: '2025-01-07 12:15:00',
    createTime: '2025-01-01 00:00:00',
    updateTime: '2025-01-07 12:15:00',
    creator: 'admin',
  },
  {
    id: 3,
    username: 'lisi',
    name: '李四',
    email: 'lisi@example.com',
    phone: '13800138002',
    avatar: '/api/uploads/avatars/lisi.png',
    status: 'active',
    departmentId: 2,
    position: '巡检员',
    lastLoginTime: '2025-01-07 10:00:00',
    createTime: '2025-01-01 00:00:00',
    updateTime: '2025-01-07 10:00:00',
    creator: 'admin',
  },
  {
    id: 4,
    username: 'wangwu',
    name: '王五',
    email: 'wangwu@example.com',
    phone: '13800138003',
    avatar: '/api/uploads/avatars/wangwu.png',
    status: 'inactive',
    departmentId: 3,
    position: '技术员',
    lastLoginTime: '2025-01-05 16:20:00',
    createTime: '2025-01-01 00:00:00',
    updateTime: '2025-01-06 09:00:00',
    creator: 'admin',
  },
  {
    id: 5,
    username: 'zhaoliu',
    name: '赵六',
    email: 'zhaoliu@example.com',
    phone: '13800138004',
    avatar: '/api/uploads/avatars/zhaoliu.png',
    status: 'active',
    departmentId: 2,
    position: '巡检组长',
    lastLoginTime: '2025-01-07 09:30:00',
    createTime: '2025-01-01 00:00:00',
    updateTime: '2025-01-07 09:30:00',
    creator: 'admin',
  },
];

// GET - 获取用户列表
export async function GET(request: NextRequest) {
  try {
    const searchParams = request.nextUrl.searchParams;
    const page = parseInt(searchParams.get('page') || '1');
    const pageSize = parseInt(searchParams.get('pageSize') || '10');
    const name = searchParams.get('name');
    const username = searchParams.get('username');
    const status = searchParams.get('status');
    const departmentId = searchParams.get('departmentId');

    let filtered = [...mockUsers];

    if (name) {
      filtered = filtered.filter((item) => item.name.includes(name));
    }
    if (username) {
      filtered = filtered.filter((item) => item.username.includes(username));
    }
    if (status) {
      filtered = filtered.filter((item) => item.status === status);
    }
    if (departmentId) {
      filtered = filtered.filter((item) => item.departmentId === parseInt(departmentId));
    }

    const total = filtered.length;
    const list = filtered.slice((page - 1) * pageSize, page * pageSize);

    // 移除密码字段
    const safeList = list.map(({ password, ...user }) => user);

    return NextResponse.json({
      code: 200,
      message: '成功',
      data: { list: safeList, total, page, pageSize },
    });
  } catch (error) {
    console.error('Get users error:', error);
    return NextResponse.json({ code: 500, message: '服务器内部错误' }, { status: 500 });
  }
}

// POST - 创建用户
export async function POST(request: NextRequest) {
  try {
    const body = await request.json();

    // 检查用户名是否已存在
    if (mockUsers.some((u) => u.username === body.username)) {
      return NextResponse.json({ code: 400, message: '用户名已存在' }, { status: 400 });
    }

    const newUser: User = {
      id: Math.max(...mockUsers.map((u) => u.id)) + 1,
      ...body,
      password: body.password || '123456', // 默认密码
      avatar: body.avatar || '/api/uploads/avatars/default.png',
      status: body.status || 'active',
      createTime: new Date().toISOString(),
      updateTime: new Date().toISOString(),
      creator: 'admin',
    };

    mockUsers.push(newUser);

    // 移除密码字段
    const { password, ...safeUser } = newUser;

    return NextResponse.json({
      code: 200,
      message: '创建成功',
      data: safeUser,
    });
  } catch (error) {
    console.error('Create user error:', error);
    return NextResponse.json({ code: 500, message: '服务器内部错误' }, { status: 500 });
  }
}

// PUT - 更新用户
export async function PUT(request: NextRequest) {
  try {
    const body = await request.json();
    const { id } = body;

    const index = mockUsers.findIndex((u) => u.id === id);
    if (index === -1) {
      return NextResponse.json({ code: 404, message: '用户不存在' }, { status: 404 });
    }

    mockUsers[index] = {
      ...mockUsers[index],
      ...body,
      updateTime: new Date().toISOString(),
      updater: 'admin',
    };

    // 移除密码字段
    const { password, ...safeUser } = mockUsers[index];

    return NextResponse.json({
      code: 200,
      message: '更新成功',
      data: safeUser,
    });
  } catch (error) {
    console.error('Update user error:', error);
    return NextResponse.json({ code: 500, message: '服务器内部错误' }, { status: 500 });
  }
}

// DELETE - 删除用户
export async function DELETE(request: NextRequest) {
  try {
    const searchParams = request.nextUrl.searchParams;
    const id = parseInt(searchParams.get('id') || '0');

    const index = mockUsers.findIndex((u) => u.id === id);
    if (index === -1) {
      return NextResponse.json({ code: 404, message: '用户不存在' }, { status: 404 });
    }

    // 不允许删除管理员
    if (mockUsers[index].username === 'admin') {
      return NextResponse.json({ code: 400, message: '不允许删除管理员账号' }, { status: 400 });
    }

    mockUsers.splice(index, 1);

    return NextResponse.json({
      code: 200,
      message: '删除成功',
      data: null,
    });
  } catch (error) {
    console.error('Delete user error:', error);
    return NextResponse.json({ code: 500, message: '服务器内部错误' }, { status: 500 });
  }
}
