import { Request, Response } from 'express';
import { ApiError } from '../utils/api-error';

export const notFoundHandler = (req: Request, res: Response): void => {
  throw ApiError.notFound(`路由 ${req.method} ${req.url} 不存在`);
};
