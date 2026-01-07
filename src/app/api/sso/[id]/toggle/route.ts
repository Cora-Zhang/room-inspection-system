import { NextRequest, NextResponse } from 'next/server';

/**
 * 启用/禁用SSO配置
 */
export async function PATCH(
  req: NextRequest,
  { params }: { params: { id: string } }
) {
  try {
    const { id } = params;
    const body = await req.json();

    if (typeof body.enabled !== 'boolean') {
      return NextResponse.json(
        {
          success: false,
          error: {
            code: 'VALIDATION_ERROR',
            message: 'enabled字段必须是布尔值',
          },
        },
        { status: 400 }
      );
    }

    // TODO: 更新数据库中的enabled状态
    return NextResponse.json({
      success: true,
      message: body.enabled ? 'SSO配置已启用' : 'SSO配置已禁用',
    });
  } catch (error: any) {
    console.error('Toggle SSO config error:', error);
    return NextResponse.json(
      {
        success: false,
        error: {
          code: 'INTERNAL_ERROR',
          message: '服务器内部错误',
        },
      },
      { status: 500 }
    );
  }
}
