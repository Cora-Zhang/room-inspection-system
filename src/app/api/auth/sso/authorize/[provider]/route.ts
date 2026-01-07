import { NextRequest, NextResponse } from 'next/server';

/**
 * 获取SSO授权URL
 */
export async function GET(
  request: NextRequest,
  { params }: { params: { provider: string } }
) {
  const { provider } = params;

  // 模拟生成授权URL
  // 实际实现中，这里应该根据provider配置生成OAuth2.0/SAML授权URL
  const mockAuthUrl = `https://sso.example.com/oauth/authorize?client_id=test_client&redirect_uri=${encodeURIComponent(process.env.NEXT_PUBLIC_BASE_URL || 'http://localhost:5000')}/auth/callback&response_type=code&scope=openid profile email&state=${Date.now()}`;

  return NextResponse.json({
    success: true,
    data: {
      authUrl: mockAuthUrl,
      provider,
    },
  });
}
