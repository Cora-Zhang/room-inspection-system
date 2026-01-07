import { NextRequest, NextResponse } from 'next/server';

/**
 * 获取SSO配置列表
 */
export async function GET() {
  // TODO: 从数据库获取SSO配置列表
  const configs = [
    {
      id: '1',
      provider: 'iam-oauth2',
      name: 'IAM认证',
      type: 'oauth2',
      enabled: false,
      config: {},
      description: '统一身份认证平台',
      sort: 0,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    },
  ];

  return NextResponse.json({
    success: true,
    data: configs,
  });
}

/**
 * 创建SSO配置
 */
export async function POST(req: NextRequest) {
  try {
    const body = await req.json();

    // 参数验证
    if (!body.provider || !body.name || !body.type) {
      return NextResponse.json(
        {
          success: false,
          error: {
            code: 'VALIDATION_ERROR',
            message: '参数验证失败',
          },
        },
        { status: 400 }
      );
    }

    // TODO: 保存到数据库
    const newConfig = {
      id: crypto.randomUUID(),
      ...body,
      enabled: body.enabled || false,
      config: JSON.stringify(body.config || {}),
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
    };

    return NextResponse.json(
      {
        success: true,
        data: newConfig,
        message: 'SSO配置创建成功',
      },
      { status: 201 }
    );
  } catch (error: any) {
    console.error('Create SSO config error:', error);
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
