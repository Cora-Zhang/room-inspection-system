import { NextResponse } from 'next/server';

/**
 * 用户数据同步服务健康检查接口
 * 用于测试回调地址是否可访问
 */
export async function GET() {
  return NextResponse.json({
    success: true,
    message: '用户数据同步服务正常运行',
    timestamp: Date.now(),
  });
}
