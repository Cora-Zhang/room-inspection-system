import { NextResponse } from 'next/server';

export async function GET() {
  return NextResponse.json({
    success: true,
    message: 'Test API is working',
  });
}

export async function POST(request: Request) {
  const body = await request.json();
  return NextResponse.json({
    success: true,
    message: 'Test POST received',
    data: body,
  });
}
