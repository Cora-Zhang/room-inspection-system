import { Request, Response, NextFunction } from 'express';
import { config } from '../config';

// 简单的内存存储（生产环境建议使用Redis）
const requestCounts = new Map<string, { count: number; resetTime: number }>();

export const rateLimiter = (req: Request, res: Response, next: NextFunction): void => {
  const key = req.ip || 'unknown';
  const now = Date.now();
  const windowMs = config.rateLimit.windowMs;
  const maxRequests = config.rateLimit.maxRequests;

  // 获取或创建计数器
  let data = requestCounts.get(key);

  if (!data || now > data.resetTime) {
    data = {
      count: 1,
      resetTime: now + windowMs,
    };
    requestCounts.set(key, data);
  } else {
    data.count++;
  }

  // 设置响应头
  res.setHeader('X-RateLimit-Limit', maxRequests);
  res.setHeader('X-RateLimit-Remaining', Math.max(0, maxRequests - data.count));
  res.setHeader('X-RateLimit-Reset', new Date(data.resetTime).toISOString());

  // 检查是否超限
  if (data.count > maxRequests) {
    const retryAfter = Math.ceil((data.resetTime - now) / 1000);
    res.setHeader('Retry-After', retryAfter);

    res.status(429).json({
      success: false,
      error: {
        code: 'RATE_LIMIT_EXCEEDED',
        message: `请求过于频繁，请在 ${retryAfter} 秒后重试`,
      },
    });
    return;
  }

  next();
};
