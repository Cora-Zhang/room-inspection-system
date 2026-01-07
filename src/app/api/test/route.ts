import { NextRequest, NextResponse } from 'next/server';

/**
 * 测试接口
 */
export async function GET() {
  return NextResponse.json({
    success: true,
    message: 'API is working',
    timestamp: Date.now(),
  });
}

export async function POST(request: NextRequest) {
  try {
    const body = await request.json();

    return NextResponse.json({
      success: true,
      message: 'POST request received',
      data: body,
      timestamp: Date.now(),
    });
  } catch (error) {
    return NextResponse.json(
      {
        success: false,
        error: 'Invalid request body',
      },
      { status: 400 }
    );
  }
}
