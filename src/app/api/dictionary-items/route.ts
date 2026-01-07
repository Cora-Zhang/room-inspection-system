import { NextRequest, NextResponse } from 'next/server';
import type { DictionaryItem } from '@/types';

// Mock数据
let mockItems: DictionaryItem[] = [
  // 机房状态
  { id: 1, dictionaryId: 1, label: '正常', value: 'normal', sort: 1, status: 'active', createTime: '2025-01-01 00:00:00', updateTime: '2025-01-01 00:00:00' },
  { id: 2, dictionaryId: 1, label: '告警', value: 'warning', sort: 2, status: 'active', createTime: '2025-01-01 00:00:00', updateTime: '2025-01-01 00:00:00' },
  { id: 3, dictionaryId: 1, label: '维护中', value: 'maintenance', sort: 3, status: 'active', createTime: '2025-01-01 00:00:00', updateTime: '2025-01-01 00:00:00' },
  { id: 4, dictionaryId: 1, label: '离线', value: 'offline', sort: 4, status: 'active', createTime: '2025-01-01 00:00:00', updateTime: '2025-01-01 00:00:00' },

  // 设备类型
  { id: 5, dictionaryId: 2, label: '服务器', value: 'server', sort: 1, status: 'active', createTime: '2025-01-01 00:00:00', updateTime: '2025-01-01 00:00:00' },
  { id: 6, dictionaryId: 2, label: '网络设备', value: 'network', sort: 2, status: 'active', createTime: '2025-01-01 00:00:00', updateTime: '2025-01-01 00:00:00' },
  { id: 7, dictionaryId: 2, label: '环境设备', value: 'environment', sort: 3, status: 'active', createTime: '2025-01-01 00:00:00', updateTime: '2025-01-01 00:00:00' },
  { id: 8, dictionaryId: 2, label: '电源设备', value: 'power', sort: 4, status: 'active', createTime: '2025-01-01 00:00:00', updateTime: '2025-01-01 00:00:00' },

  // 巡检状态
  { id: 9, dictionaryId: 3, label: '待处理', value: 'pending', sort: 1, status: 'active', createTime: '2025-01-01 00:00:00', updateTime: '2025-01-01 00:00:00' },
  { id: 10, dictionaryId: 3, label: '进行中', value: 'processing', sort: 2, status: 'active', createTime: '2025-01-01 00:00:00', updateTime: '2025-01-01 00:00:00' },
  { id: 11, dictionaryId: 3, label: '已完成', value: 'completed', sort: 3, status: 'active', createTime: '2025-01-01 00:00:00', updateTime: '2025-01-01 00:00:00' },
  { id: 12, dictionaryId: 3, label: '已取消', value: 'cancelled', sort: 4, status: 'active', createTime: '2025-01-01 00:00:00', updateTime: '2025-01-01 00:00:00' },

  // 告警级别
  { id: 13, dictionaryId: 4, label: '提示', value: 'info', sort: 1, status: 'active', extra: 'color: blue', createTime: '2025-01-01 00:00:00', updateTime: '2025-01-01 00:00:00' },
  { id: 14, dictionaryId: 4, label: '警告', value: 'warning', sort: 2, status: 'active', extra: 'color: yellow', createTime: '2025-01-01 00:00:00', updateTime: '2025-01-01 00:00:00' },
  { id: 15, dictionaryId: 4, label: '严重', value: 'error', sort: 3, status: 'active', extra: 'color: orange', createTime: '2025-01-01 00:00:00', updateTime: '2025-01-01 00:00:00' },
  { id: 16, dictionaryId: 4, label: '紧急', value: 'critical', sort: 4, status: 'active', extra: 'color: red', createTime: '2025-01-01 00:00:00', updateTime: '2025-01-01 00:00:00' },

  // 用户状态
  { id: 17, dictionaryId: 5, label: '正常', value: 'active', sort: 1, status: 'active', createTime: '2025-01-01 00:00:00', updateTime: '2025-01-01 00:00:00' },
  { id: 18, dictionaryId: 5, label: '禁用', value: 'inactive', sort: 2, status: 'active', createTime: '2025-01-01 00:00:00', updateTime: '2025-01-01 00:00:00' },
  { id: 19, dictionaryId: 5, label: '锁定', value: 'locked', sort: 3, status: 'active', createTime: '2025-01-01 00:00:00', updateTime: '2025-01-01 00:00:00' },
];

// GET - 获取数据字典项列表
export async function GET(request: NextRequest) {
  try {
    const searchParams = request.nextUrl.searchParams;
    const page = parseInt(searchParams.get('page') || '1');
    const pageSize = parseInt(searchParams.get('pageSize') || '10');
    const dictionaryId = searchParams.get('dictionaryId');

    let filtered = [...mockItems];

    if (dictionaryId) {
      filtered = filtered.filter((item) => item.dictionaryId === parseInt(dictionaryId));
    }

    const total = filtered.length;
    const list = filtered.slice((page - 1) * pageSize, page * pageSize);

    return NextResponse.json({
      code: 200,
      message: '成功',
      data: { list, total, page, pageSize },
    });
  } catch (error) {
    console.error('Get dictionary items error:', error);
    return NextResponse.json({ code: 500, message: '服务器内部错误' }, { status: 500 });
  }
}

// POST - 创建数据字典项
export async function POST(request: NextRequest) {
  try {
    const body = await request.json();
    const newItem: DictionaryItem = {
      id: Math.max(...mockItems.map((item) => item.id)) + 1,
      ...body,
      createTime: new Date().toISOString(),
      updateTime: new Date().toISOString(),
    };

    mockItems.push(newItem);

    return NextResponse.json({
      code: 200,
      message: '创建成功',
      data: newItem,
    });
  } catch (error) {
    console.error('Create dictionary item error:', error);
    return NextResponse.json({ code: 500, message: '服务器内部错误' }, { status: 500 });
  }
}
