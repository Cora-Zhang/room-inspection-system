import winston from 'winston';
import path from 'path';
import fs from 'fs';
import { config } from '../config';

// 确保日志目录存在
const logDir = path.join(process.cwd(), 'logs');
if (!fs.existsSync(logDir)) {
  fs.mkdirSync(logDir, { recursive: true });
}

// 自定义日志格式
const logFormat = winston.format.combine(
  winston.format.timestamp({ format: 'YYYY-MM-DD HH:mm:ss' }),
  winston.format.errors({ stack: true }),
  winston.format.splat(),
  winston.format.json()
);

// 控制台输出格式（开发环境）
const consoleFormat = winston.format.combine(
  winston.format.colorize(),
  winston.format.timestamp({ format: 'YYYY-MM-DD HH:mm:ss' }),
  winston.format.printf(({ timestamp, level, message, ...metadata }) => {
    let msg = `${timestamp} [${level}]: ${message}`;
    if (Object.keys(metadata).length > 0) {
      msg += ` ${JSON.stringify(metadata)}`;
    }
    return msg;
  })
);

// 创建日志传输器
const transports: winston.transport[] = [
  // 控制台输出
  new winston.transports.Console({
    format: config.nodeEnv === 'production' ? logFormat : consoleFormat,
    level: config.log.level,
  }),
];

// 文件输出（生产环境）
if (config.nodeEnv === 'production') {
  transports.push(
    // 所有日志
    new winston.transports.File({
      filename: path.join(logDir, 'combined.log'),
      format: logFormat,
      level: 'info',
    }),
    // 错误日志
    new winston.transports.File({
      filename: path.join(logDir, 'error.log'),
      level: 'error',
      format: logFormat,
    })
  );
}

// 创建logger实例
export const logger = winston.createLogger({
  level: config.log.level,
  format: logFormat,
  transports,
  exitOnError: false,
});

// 创建子logger（带请求ID）
export const createRequestLogger = (requestId: string): winston.Logger => {
  return logger.child({ requestId });
};

// 审计日志专用logger
export const auditLogger = winston.createLogger({
  level: 'info',
  format: winston.format.combine(
    winston.format.timestamp(),
    winston.format.json()
  ),
  transports: [
    new winston.transports.File({
      filename: path.join(logDir, 'audit.log'),
    }),
  ],
});

export default logger;
