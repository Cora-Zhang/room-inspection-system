import { NextRequest, NextResponse } from 'next/server';
import type { Role } from '@/types';

// Mock数据
let mockRoles: Role[] = [
  {
    id: 1,
    name: '超级管理员',
    code: 'super_admin',
    description: '系统最高权限，可管理所有功能',
    status: 'active',
    sort: 1,
    createTime: '2025-01-01 00:00:00',
    updateTime: '2025-01-01 00:00:00',
  },
  {
    id: 2,
    name: '管理员',
    code: 'admin',
    description: '常规管理权限',
    status: 'active',
    sort: 2,
    createTime: '2025-01-01 00:00:00',
    updateTime: '2025-01-01 00:00:00',
  },
  {
    id: 3,
    name: '巡检员',
    code: 'inspector',
    description: '负责机房巡检工作',
    status: 'active',
    sort: 3,
    createTime: '2025-01-01 00:00:00',
    updateTime: '2025-01-01 00:00:00',
  },
  {
    id: 4,
    name: '访客',
    code: 'guest',
    description: '只读权限',
    status: 'active',
    sort: 4,
    createTime: '2025-01-01 00:00:00',
    updateTime: '2025-01-01 00:00:00',
  },
];

// GET - 获取角色列表
export async function GET(request: NextRequest) {
  try {
    const searchParams = request.nextUrl.searchParams;
    const page = parseInt(searchParams.get('page') || '1');
    const pageSize = parseInt(searchParams.get('pageSize') || '10');
    const name = searchParams.get('name');
    const code = searchParams.get('code');
    const status = searchParams.get('status');

    let filtered = [...mockRoles];

    if (name) {
      filtered = filtered.filter((item) => item.name.includes(name));
    }
    if (code) {
      filtered = filtered.filter((item) => item.code.includes(code));
    }
    if (status) {
      filtered = filtered.filter((item) => item.status === status);
    }

    const total = filtered.length;
    const list = filtered.slice((page - 1) * pageSize, page * pageSize);

    return NextResponse.json({
      code: 200,
      message: '成功',
      data: { list, total, page, pageSize },
    });
  } catch (error) {
    console.error('Get roles error:', error);
    return NextResponse.json({ code: 500, message: '服务器内部错误' }, { status: 500 });
  }
}

// POST - 创建角色
export async function POST(request: NextRequest) {
  try {
    const body = await request.json();
    const newRole: Role = {
      id: Math.max(...mockRoles.map((r) => r.id)) + 1,
      ...body,
      createTime: new Date().toISOString(),
      updateTime: new Date().toISOString(),
    };

    mockRoles.push(newRole);

    return NextResponse.json({
      code: 200,
      message: '创建成功',
      data: newRole,
    });
  } catch (error) {
    console.error('Create role error:', error);
    return NextResponse.json({ code: 500, message: '服务器内部错误' }, { status: 500 });
  }
}
