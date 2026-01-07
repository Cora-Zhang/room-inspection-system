import { NextRequest, NextResponse } from 'next/server';
import type { UIConfig, ThemeConfig } from '@/types';

// Mock UI配置数据
let mockUIConfigs: UIConfig[] = [
  {
    id: 1,
    type: 'logo',
    key: 'system_logo',
    value: '/api/uploads/logo.png',
    description: '系统LOGO',
    category: 'brand',
    createTime: '2025-01-01 00:00:00',
    updateTime: '2025-01-01 00:00:00',
  },
  {
    id: 2,
    type: 'logo',
    key: 'login_logo',
    value: '/api/uploads/login-logo.png',
    description: '登录页LOGO',
    category: 'brand',
    createTime: '2025-01-01 00:00:00',
    updateTime: '2025-01-01 00:00:00',
  },
  {
    id: 3,
    type: 'background',
    key: 'login_background',
    value: '/api/uploads/login-bg.png',
    description: '登录页背景',
    category: 'background',
    createTime: '2025-01-01 00:00:00',
    updateTime: '2025-01-01 00:00:00',
  },
  {
    id: 4,
    type: 'background',
    key: 'dashboard_background',
    value: '/api/uploads/dashboard-bg.png',
    description: '首页背景',
    category: 'background',
    createTime: '2025-01-01 00:00:00',
    updateTime: '2025-01-01 00:00:00',
  },
  {
    id: 5,
    type: 'custom',
    key: 'system_name',
    value: '集团机房巡检系统',
    description: '系统名称',
    category: 'general',
    createTime: '2025-01-01 00:00:00',
    updateTime: '2025-01-01 00:00:00',
  },
  {
    id: 6,
    type: 'custom',
    key: 'system_slogan',
    value: '智能监控 · 实时预警 · 高效运维',
    description: '系统口号',
    category: 'general',
    createTime: '2025-01-01 00:00:00',
    updateTime: '2025-01-01 00:00:00',
  },
];

// Mock主题配置数据
let mockThemes: ThemeConfig[] = [
  {
    id: 1,
    name: '默认科幻蓝',
    primaryColor: '#06b6d4',
    secondaryColor: '#3b82f6',
    backgroundColor: '#0f172a',
    textColor: '#ffffff',
    accentColor: '#8b5cf6',
    isDefault: true,
  },
  {
    id: 2,
    name: '深邃紫',
    primaryColor: '#8b5cf6',
    secondaryColor: '#a855f7',
    backgroundColor: '#1a1025',
    textColor: '#ffffff',
    accentColor: '#ec4899',
    isDefault: false,
  },
  {
    id: 3,
    name: '森林绿',
    primaryColor: '#10b981',
    secondaryColor: '#059669',
    backgroundColor: '#064e3b',
    textColor: '#ffffff',
    accentColor: '#06b6d4',
    isDefault: false,
  },
];

// GET - 获取UI配置列表
export async function GET(request: NextRequest) {
  try {
    const searchParams = request.nextUrl.searchParams;
    const type = searchParams.get('type');
    const category = searchParams.get('category');

    let filtered = [...mockUIConfigs];

    if (type) {
      filtered = filtered.filter((item) => item.type === type);
    }
    if (category) {
      filtered = filtered.filter((item) => item.category === category);
    }

    return NextResponse.json({
      code: 200,
      message: '成功',
      data: filtered,
    });
  } catch (error) {
    console.error('Get UI configs error:', error);
    return NextResponse.json({ code: 500, message: '服务器内部错误' }, { status: 500 });
  }
}

// POST - 创建UI配置
export async function POST(request: NextRequest) {
  try {
    const body = await request.json();
    const newConfig: UIConfig = {
      id: Math.max(...mockUIConfigs.map((c) => c.id)) + 1,
      ...body,
      createTime: new Date().toISOString(),
      updateTime: new Date().toISOString(),
    };

    mockUIConfigs.push(newConfig);

    return NextResponse.json({
      code: 200,
      message: '创建成功',
      data: newConfig,
    });
  } catch (error) {
    console.error('Create UI config error:', error);
    return NextResponse.json({ code: 500, message: '服务器内部错误' }, { status: 500 });
  }
}

// PUT - 更新UI配置
export async function PUT(request: NextRequest) {
  try {
    const body = await request.json();
    const { id } = body;

    const index = mockUIConfigs.findIndex((c) => c.id === id);
    if (index === -1) {
      return NextResponse.json({ code: 404, message: '配置不存在' }, { status: 404 });
    }

    mockUIConfigs[index] = {
      ...mockUIConfigs[index],
      ...body,
      updateTime: new Date().toISOString(),
    };

    return NextResponse.json({
      code: 200,
      message: '更新成功',
      data: mockUIConfigs[index],
    });
  } catch (error) {
    console.error('Update UI config error:', error);
    return NextResponse.json({ code: 500, message: '服务器内部错误' }, { status: 500 });
  }
}

// DELETE - 删除UI配置
export async function DELETE(request: NextRequest) {
  try {
    const searchParams = request.nextUrl.searchParams;
    const id = parseInt(searchParams.get('id') || '0');

    const index = mockUIConfigs.findIndex((c) => c.id === id);
    if (index === -1) {
      return NextResponse.json({ code: 404, message: '配置不存在' }, { status: 404 });
    }

    mockUIConfigs.splice(index, 1);

    return NextResponse.json({
      code: 200,
      message: '删除成功',
      data: null,
    });
  } catch (error) {
    console.error('Delete UI config error:', error);
    return NextResponse.json({ code: 500, message: '服务器内部错误' }, { status: 500 });
  }
}

// 获取主题列表
export async function OPTIONS() {
  return NextResponse.json({
    code: 200,
    message: '成功',
    data: mockThemes,
  });
}
