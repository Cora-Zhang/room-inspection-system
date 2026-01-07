import { NextResponse } from 'next/server';

/**
 * 获取已启用的SSO提供商列表
 */
export async function GET() {
  // Mock SSO提供商数据
  const providers = [
    {
      provider: 'iam-oauth2',
      name: 'IAM认证',
      type: 'oauth2',
    },
    {
      provider: 'enterprise-saml',
      name: '企业认证',
      type: 'saml',
    },
  ];

  return NextResponse.json({
    success: true,
    data: providers,
  });
}
