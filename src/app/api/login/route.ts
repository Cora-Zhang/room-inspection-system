import { NextRequest, NextResponse } from 'next/server';

// Mock 用户数据
const MOCK_USERS = [
  {
    id: 1,
    username: 'admin',
    password: '123456',
    name: '系统管理员',
    role: 'admin',
  },
  {
    id: 2,
    username: 'user',
    password: '123456',
    name: '普通用户',
    role: 'user',
  },
];

// 生成简单的 token
function generateToken(userId: number): string {
  return `token_${userId}_${Date.now()}_${Math.random().toString(36).substring(2)}`;
}

export async function POST(request: NextRequest) {
  try {
    const body = await request.json();
    const { username, password } = body;

    // 参数验证
    if (!username || !password) {
      return NextResponse.json(
        { error: '用户名和密码不能为空' },
        { status: 400 }
      );
    }

    // Mock 数据验证
    const user = MOCK_USERS.find(
      (u) => u.username === username && u.password === password
    );

    if (!user) {
      return NextResponse.json(
        { error: '用户名或密码错误' },
        { status: 401 }
      );
    }

    // 生成 token
    const token = generateToken(user.id);

    // 返回成功响应
    const response = {
      success: true,
      message: '登录成功',
      token,
      user: {
        id: user.id,
        username: user.username,
        name: user.name,
        role: user.role,
      },
    };

    return NextResponse.json(response);
  } catch (error) {
    console.error('Login error:', error);
    return NextResponse.json(
      { error: '服务器内部错误' },
      { status: 500 }
    );
  }
}

// 支持 OPTIONS 请求（CORS）
export async function OPTIONS() {
  return new NextResponse(null, {
    status: 200,
    headers: {
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Methods': 'POST, OPTIONS',
      'Access-Control-Allow-Headers': 'Content-Type',
    },
  });
}
