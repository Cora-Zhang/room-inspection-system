import { Request, Response, NextFunction } from 'express';
import { v4 as uuidv4 } from 'uuid';
import { createRequestLogger } from '../utils/logger';

export const requestLogger = (req: Request, res: Response, next: NextFunction): void => {
  // 生成请求ID
  const requestId = uuidv4();
  req.requestId = requestId;

  // 创建带请求ID的logger
  const logger = createRequestLogger(requestId);

  // 记录请求信息
  logger.info('Incoming request', {
    method: req.method,
    url: req.url,
    ip: req.ip,
    userAgent: req.get('user-agent'),
  });

  // 记录响应时间
  const startTime = Date.now();
  const originalSend = res.send;

  res.send = function (this: Response, ...args: any[]): Response {
    const duration = Date.now() - startTime;

    logger.info('Request completed', {
      method: req.method,
      url: req.url,
      statusCode: res.statusCode,
      duration: `${duration}ms`,
    });

    return originalSend.apply(this, args);
  };

  next();
};

// 扩展Express Request类型
declare global {
  namespace Express {
    interface Request {
      requestId?: string;
      user?: {
        id: string;
        username: string;
        email?: string;
        roles: string[];
      };
    }
  }
}
