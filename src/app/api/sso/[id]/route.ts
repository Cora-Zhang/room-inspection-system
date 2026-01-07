import { NextRequest, NextResponse } from 'next/server';

/**
 * 获取SSO配置详情
 */
export async function GET(
  req: NextRequest,
  { params }: { params: Promise<{ id: string }> }
) {
  const { id } = await params;

  // TODO: 从数据库获取配置详情
  const config = {
    id,
    provider: 'iam-oauth2',
    name: 'IAM认证',
    type: 'oauth2',
    enabled: false,
    config: {},
    description: '统一身份认证平台',
    sort: 0,
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
  };

  return NextResponse.json({
    success: true,
    data: config,
  });
}

/**
 * 更新SSO配置
 */
export async function PUT(
  req: NextRequest,
  { params }: { params: Promise<{ id: string }> }
) {
  try {
    const { id } = await params;
    const body = await req.json();

    // TODO: 更新数据库
    const updatedConfig = {
      id,
      ...body,
      updatedAt: new Date().toISOString(),
    };

    return NextResponse.json({
      success: true,
      data: updatedConfig,
      message: 'SSO配置更新成功',
    });
  } catch (error: any) {
    console.error('Update SSO config error:', error);
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

/**
 * 删除SSO配置
 */
export async function DELETE(
  req: NextRequest,
  { params }: { params: Promise<{ id: string }> }
) {
  const { id } = await params;

  // TODO: 从数据库删除
  return NextResponse.json({
    success: true,
    message: 'SSO配置删除成功',
  });
}
