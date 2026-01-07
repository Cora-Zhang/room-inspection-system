import { NextRequest, NextResponse } from 'next/server';
import type { Permission } from '@/types';

// Mock数据
let mockPermissions: Permission[] = [
  // 系统管理
  { id: 1, name: '系统管理', code: 'system', type: 'menu', sort: 1, status: 'active', createTime: '2025-01-01 00:00:00', updateTime: '2025-01-01 00:00:00' },
  { id: 2, name: '用户管理', code: 'system:user', type: 'menu', parentId: 1, path: '/system/user', sort: 1, status: 'active', createTime: '2025-01-01 00:00:00', updateTime: '2025-01-01 00:00:00' },
  { id: 3, name: '角色管理', code: 'system:role', type: 'menu', parentId: 1, path: '/system/role', sort: 2, status: 'active', createTime: '2025-01-01 00:00:00', updateTime: '2025-01-01 00:00:00' },
  { id: 4, name: '权限管理', code: 'system:permission', type: 'menu', parentId: 1, path: '/system/permission', sort: 3, status: 'active', createTime: '2025-01-01 00:00:00', updateTime: '2025-01-01 00:00:00' },
  { id: 5, name: '菜单管理', code: 'system:menu', type: 'menu', parentId: 1, path: '/system/menu', sort: 4, status: 'active', createTime: '2025-01-01 00:00:00', updateTime: '2025-01-01 00:00:00' },

  // 基础配置
  { id: 6, name: '基础配置', code: 'config', type: 'menu', sort: 2, status: 'active', createTime: '2025-01-01 00:00:00', updateTime: '2025-01-01 00:00:00' },
  { id: 7, name: '数据字典', code: 'config:dictionary', type: 'menu', parentId: 6, path: '/config/dictionary', sort: 1, status: 'active', createTime: '2025-01-01 00:00:00', updateTime: '2025-01-01 00:00:00' },
  { id: 8, name: '参数配置', code: 'config:parameter', type: 'menu', parentId: 6, path: '/config/parameter', sort: 2, status: 'active', createTime: '2025-01-01 00:00:00', updateTime: '2025-01-01 00:00:00' },
  { id: 9, name: '接口管理', code: 'config:api', type: 'menu', parentId: 6, path: '/config/api', sort: 3, status: 'active', createTime: '2025-01-01 00:00:00', updateTime: '2025-01-01 00:00:00' },
  { id: 10, name: '报表配置', code: 'config:report', type: 'menu', parentId: 6, path: '/config/report', sort: 4, status: 'active', createTime: '2025-01-01 00:00:00', updateTime: '2025-01-01 00:00:00' },
  { id: 11, name: '元数据管理', code: 'config:metadata', type: 'menu', parentId: 6, path: '/config/metadata', sort: 5, status: 'active', createTime: '2025-01-01 00:00:00', updateTime: '2025-01-01 00:00:00' },

  // 系统设置
  { id: 12, name: '系统设置', code: 'settings', type: 'menu', sort: 3, status: 'active', createTime: '2025-01-01 00:00:00', updateTime: '2025-01-01 00:00:00' },
  { id: 13, name: 'UI配置', code: 'settings:ui', type: 'menu', parentId: 12, path: '/settings/ui', sort: 1, status: 'active', createTime: '2025-01-01 00:00:00', updateTime: '2025-01-01 00:00:00' },
  { id: 14, name: '主题配置', code: 'settings:theme', type: 'menu', parentId: 12, path: '/settings/theme', sort: 2, status: 'active', createTime: '2025-01-01 00:00:00', updateTime: '2025-01-01 00:00:00' },
  { id: 15, name: '日志管理', code: 'settings:log', type: 'menu', parentId: 12, path: '/settings/log', sort: 3, status: 'active', createTime: '2025-01-01 00:00:00', updateTime: '2025-01-01 00:00:00' },

  // 按钮权限
  { id: 100, name: '添加用户', code: 'system:user:add', type: 'button', parentId: 2, sort: 1, status: 'active', createTime: '2025-01-01 00:00:00', updateTime: '2025-01-01 00:00:00' },
  { id: 101, name: '编辑用户', code: 'system:user:edit', type: 'button', parentId: 2, sort: 2, status: 'active', createTime: '2025-01-01 00:00:00', updateTime: '2025-01-01 00:00:00' },
  { id: 102, name: '删除用户', code: 'system:user:delete', type: 'button', parentId: 2, sort: 3, status: 'active', createTime: '2025-01-01 00:00:00', updateTime: '2025-01-01 00:00:00' },
  { id: 103, name: '重置密码', code: 'system:user:reset', type: 'button', parentId: 2, sort: 4, status: 'active', createTime: '2025-01-01 00:00:00', updateTime: '2025-01-01 00:00:00' },

  // 数据权限
  { id: 200, name: '全部数据', code: 'data:all', type: 'api', sort: 1, status: 'active', createTime: '2025-01-01 00:00:00', updateTime: '2025-01-01 00:00:00' },
  { id: 201, name: '部门数据', code: 'data:department', type: 'api', sort: 2, status: 'active', createTime: '2025-01-01 00:00:00', updateTime: '2025-01-01 00:00:00' },
  { id: 202, name: '个人数据', code: 'data:self', type: 'api', sort: 3, status: 'active', createTime: '2025-01-01 00:00:00', updateTime: '2025-01-01 00:00:00' },
  { id: 203, name: '自定义数据', code: 'data:custom', type: 'api', sort: 4, status: 'active', createTime: '2025-01-01 00:00:00', updateTime: '2025-01-01 00:00:00' },
];

// GET - 获取权限列表（树形结构）
export async function GET(request: NextRequest) {
  try {
    const searchParams = request.nextUrl.searchParams;
    const type = searchParams.get('type');
    const status = searchParams.get('status');

    let filtered = [...mockPermissions];

    if (type) {
      filtered = filtered.filter((item) => item.type === type);
    }
    if (status) {
      filtered = filtered.filter((item) => item.status === status);
    }

    // 构建树形结构
    const buildTree = (parentId?: number): any[] => {
      return filtered
        .filter((item) => {
          if (parentId === undefined) {
            return !item.parentId;
          }
          return item.parentId === parentId;
        })
        .sort((a, b) => (a.sort || 0) - (b.sort || 0))
        .map((item) => ({
          ...item,
          children: buildTree(item.id),
        }));
    };

    const tree = buildTree();

    return NextResponse.json({
      code: 200,
      message: '成功',
      data: tree,
    });
  } catch (error) {
    console.error('Get permissions error:', error);
    return NextResponse.json({ code: 500, message: '服务器内部错误' }, { status: 500 });
  }
}

// POST - 创建权限
export async function POST(request: NextRequest) {
  try {
    const body = await request.json();
    const newPermission: Permission = {
      id: Math.max(...mockPermissions.map((p) => p.id)) + 1,
      ...body,
      createTime: new Date().toISOString(),
      updateTime: new Date().toISOString(),
    };

    mockPermissions.push(newPermission);

    return NextResponse.json({
      code: 200,
      message: '创建成功',
      data: newPermission,
    });
  } catch (error) {
    console.error('Create permission error:', error);
    return NextResponse.json({ code: 500, message: '服务器内部错误' }, { status: 500 });
  }
}
