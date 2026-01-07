import { NextRequest, NextResponse } from 'next/server';
import type { RolePermission } from '@/types';

// Mock数据
let mockRolePermissions: RolePermission[] = [
  // 超级管理员拥有所有权限
  ...Array.from({ length: 50 }, (_, i) => ({
    roleId: 1,
    permissionId: i + 1,
  })),

  // 管理员权限
  { roleId: 2, permissionId: 2 },
  { roleId: 2, permissionId: 3 },
  { roleId: 2, permissionId: 6 },
  { roleId: 2, permissionId: 7 },
  { roleId: 2, permissionId: 8 },

  // 巡检员权限
  { roleId: 3, permissionId: 2 },

  // 访客权限（只读）
  { roleId: 4, permissionId: 2 },
];

// GET - 获取角色权限
export async function GET(request: NextRequest) {
  try {
    const searchParams = request.nextUrl.searchParams;
    const roleId = searchParams.get('roleId');

    if (roleId) {
      const permissions = mockRolePermissions
        .filter((rp) => rp.roleId === parseInt(roleId))
        .map((rp) => rp.permissionId);

      return NextResponse.json({
        code: 200,
        message: '成功',
        data: permissions,
      });
    }

    return NextResponse.json({
      code: 200,
      message: '成功',
      data: mockRolePermissions,
    });
  } catch (error) {
    console.error('Get role permissions error:', error);
    return NextResponse.json({ code: 500, message: '服务器内部错误' }, { status: 500 });
  }
}

// POST - 设置角色权限
export async function POST(request: NextRequest) {
  try {
    const body = await request.json();
    const { roleId, permissionIds } = body;

    // 删除该角色原有权限
    mockRolePermissions = mockRolePermissions.filter((rp) => rp.roleId !== roleId);

    // 添加新权限
    permissionIds.forEach((permissionId: number) => {
      mockRolePermissions.push({ roleId, permissionId });
    });

    return NextResponse.json({
      code: 200,
      message: '设置成功',
      data: { roleId, permissionIds },
    });
  } catch (error) {
    console.error('Set role permissions error:', error);
    return NextResponse.json({ code: 500, message: '服务器内部错误' }, { status: 500 });
  }
}
