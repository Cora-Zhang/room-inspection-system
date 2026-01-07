import { NextRequest, NextResponse } from 'next/server';
import type { Dictionary, PageResponse } from '@/types';

// Mock数据
let mockDictionaries: Dictionary[] = [
  {
    id: 1,
    name: '机房状态',
    code: 'room_status',
    description: '机房运行状态',
    status: 'active',
    sort: 1,
    createTime: '2025-01-01 00:00:00',
    updateTime: '2025-01-01 00:00:00',
  },
  {
    id: 2,
    name: '设备类型',
    code: 'device_type',
    description: '设备类型分类',
    status: 'active',
    sort: 2,
    createTime: '2025-01-01 00:00:00',
    updateTime: '2025-01-01 00:00:00',
  },
  {
    id: 3,
    name: '巡检状态',
    code: 'inspection_status',
    description: '巡检任务状态',
    status: 'active',
    sort: 3,
    createTime: '2025-01-01 00:00:00',
    updateTime: '2025-01-01 00:00:00',
  },
  {
    id: 4,
    name: '告警级别',
    code: 'alarm_level',
    description: '告警严重级别',
    status: 'active',
    sort: 4,
    createTime: '2025-01-01 00:00:00',
    updateTime: '2025-01-01 00:00:00',
  },
  {
    id: 5,
    name: '用户状态',
    code: 'user_status',
    description: '用户账号状态',
    status: 'active',
    sort: 5,
    createTime: '2025-01-01 00:00:00',
    updateTime: '2025-01-01 00:00:00',
  },
];

// GET - 获取数据字典列表
export async function GET(request: NextRequest) {
  try {
    const searchParams = request.nextUrl.searchParams;
    const page = parseInt(searchParams.get('page') || '1');
    const pageSize = parseInt(searchParams.get('pageSize') || '10');
    const name = searchParams.get('name');
    const code = searchParams.get('code');
    const status = searchParams.get('status');

    let filtered = [...mockDictionaries];

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
    console.error('Get dictionaries error:', error);
    return NextResponse.json({ code: 500, message: '服务器内部错误' }, { status: 500 });
  }
}

// POST - 创建数据字典
export async function POST(request: NextRequest) {
  try {
    const body = await request.json();
    const newDictionary: Dictionary = {
      id: Math.max(...mockDictionaries.map((d) => d.id)) + 1,
      ...body,
      createTime: new Date().toISOString(),
      updateTime: new Date().toISOString(),
    };

    mockDictionaries.push(newDictionary);

    return NextResponse.json({
      code: 200,
      message: '创建成功',
      data: newDictionary,
    });
  } catch (error) {
    console.error('Create dictionary error:', error);
    return NextResponse.json({ code: 500, message: '服务器内部错误' }, { status: 500 });
  }
}
