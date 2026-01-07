import { Request, Response, NextFunction } from 'express';
import { logger } from '../utils/logger';
import { ApiError } from '../utils/api-error';

export const errorHandler = (
  err: Error | ApiError,
  req: Request,
  res: Response,
  next: NextFunction
): void => {
  // 记录错误日志
  logger.error('Error occurred', {
    message: err.message,
    stack: err.stack,
    url: req.url,
    method: req.method,
    ip: req.ip,
  });

  // 处理自定义API错误
  if (err instanceof ApiError) {
    res.status(err.statusCode).json({
      success: false,
      error: {
        code: err.code,
        message: err.message,
        details: err.details,
      },
    });
    return;
  }

  // 处理Prisma错误
  if (err.name === 'PrismaClientKnownRequestError') {
    // @ts-ignore
    const prismaError = err as Prisma.PrismaClientKnownRequestError;

    if (prismaError.code === 'P2002') {
      // 唯一约束冲突
      res.status(409).json({
        success: false,
        error: {
          code: 'DUPLICATE_ENTRY',
          message: '数据已存在',
        },
      });
      return;
    }

    if (prismaError.code === 'P2025') {
      // 记录不存在
      res.status(404).json({
        success: false,
        error: {
          code: 'NOT_FOUND',
          message: '数据不存在',
        },
      });
      return;
    }
  }

  // 处理JWT错误
  if (err.name === 'JsonWebTokenError') {
    res.status(401).json({
      success: false,
      error: {
        code: 'INVALID_TOKEN',
        message: '无效的令牌',
      },
    });
    return;
  }

  if (err.name === 'TokenExpiredError') {
    res.status(401).json({
      success: false,
      error: {
        code: 'TOKEN_EXPIRED',
        message: '令牌已过期',
      },
    });
    return;
  }

  // 处理验证错误
  if (err.name === 'ValidationError') {
    res.status(400).json({
      success: false,
      error: {
        code: 'VALIDATION_ERROR',
        message: '参数验证失败',
        // @ts-ignore
        details: err.details,
      },
    });
    return;
  }

  // 默认服务器错误
  res.status(500).json({
    success: false,
    error: {
      code: 'INTERNAL_SERVER_ERROR',
      message: process.env.NODE_ENV === 'development' ? err.message : '服务器内部错误',
    },
  });
};
