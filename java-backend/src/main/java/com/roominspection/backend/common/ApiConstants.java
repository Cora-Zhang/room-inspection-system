package com.roominspection.backend.common;

/**
 * API通用常量
 */
public class ApiConstants {

    /**
     * API版本
     */
    public static final String API_V1 = "v1";
    public static final String API_V2 = "v2";
    public static final String DEFAULT_VERSION = API_V1;

    /**
     * API路径前缀
     */
    public static final String API_PREFIX = "/api";

    /**
     * 请求头常量
     */
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_API_VERSION = "X-API-Version";
    public static final String HEADER_REQUEST_ID = "X-Request-ID";
    public static final String HEADER_TENANT_ID = "X-Tenant-ID";
    public static final String HEADER_APP_ID = "X-App-ID";
    public static final String HEADER_SIGNATURE = "X-Signature";
    public static final String HEADER_TIMESTAMP = "X-Timestamp";

    /**
     * Token前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String TOKEN_TYPE_BEARER = "Bearer";

    /**
     * 时间格式
     */
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "HH:mm:ss";
    public static final String ISO_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    /**
     * 分页默认值
     */
    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_SIZE = 10;
    public static final int MAX_SIZE = 100;

    /**
     * 限流配置
     */
    public static final int RATE_LIMIT_IP_PER_MINUTE = 1000;
    public static final int RATE_LIMIT_USER_PER_MINUTE = 500;
    public static final int RATE_LIMIT_API_PER_MINUTE = 200;

    /**
     * Token有效期
     */
    public static final long ACCESS_TOKEN_EXPIRE_TIME = 2 * 60 * 60 * 1000; // 2小时
    public static final long REFRESH_TOKEN_EXPIRE_TIME = 7 * 24 * 60 * 60 * 1000; // 7天

    /**
     * 文件上传限制
     */
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    public static final String[] ALLOWED_FILE_TYPES = {
        "image/jpeg",
        "image/png",
        "image/gif",
        "application/pdf"
    };

    /**
     * 监控协议类型
     */
    public static final String PROTOCOL_SNMP = "SNMP";
    public static final String PROTOCOL_MODBUS = "Modbus";
    public static final String PROTOCOL_BMS = "BMS";
    public static final String PROTOCOL_SENSOR = "Sensor";
    public static final String PROTOCOL_FIRE = "FireHost";
    public static final String PROTOCOL_CUSTOM = "Custom";

    /**
     * 设备类型
     */
    public static final String DEVICE_TYPE_AC = "AC";
    public static final String DEVICE_TYPE_UPS = "UPS";
    public static final String DEVICE_TYPE_PDU = "PDU";
    public static final String DEVICE_TYPE_FIRE = "FIRE";
    public static final String DEVICE_TYPE_ENV = "ENV";
    public static final String DEVICE_TYPE_SWITCH = "SWITCH";
    public static final String DEVICE_TYPE_SERVER = "SERVER";
    public static final String DEVICE_TYPE_STORAGE = "STORAGE";

    /**
     * 告警级别
     */
    public static final String ALARM_LEVEL_INFO = "info";
    public static final String ALARM_LEVEL_WARNING = "warning";
    public static final String ALARM_LEVEL_ERROR = "error";
    public static final String ALARM_LEVEL_CRITICAL = "critical";

    /**
     * 告警状态
     */
    public static final String ALARM_STATUS_PENDING = "pending";
    public static final String ALARM_STATUS_ACKNOWLEDGED = "acknowledged";
    public static final String ALARM_STATUS_RESOLVED = "resolved";

    /**
     * 设备状态
     */
    public static final String DEVICE_STATUS_ONLINE = "online";
    public static final String DEVICE_STATUS_OFFLINE = "offline";
    public static final String DEVICE_STATUS_WARNING = "warning";
    public static final String DEVICE_STATUS_ERROR = "error";

    /**
     * 巡检计划类型
     */
    public static final String INSPECTION_TYPE_DAILY = "daily";
    public static final String INSPECTION_TYPE_WEEKLY = "weekly";
    public static final String INSPECTION_TYPE_MONTHLY = "monthly";

    /**
     * Redis缓存键前缀
     */
    public static final String REDIS_PREFIX_TOKEN = "token:";
    public static final String REDIS_PREFIX_USER = "user:";
    public static final String REDIS_PREFIX_DEVICE = "device:";
    public static final String REDIS_PREFIX_ALARM = "alarm:";
    public static final String REDIS_PREFIX_CONFIG = "config:";
    public static final String REDIS_PREFIX_RATE_LIMIT = "rate_limit:";

    /**
     * 监控数据采样间隔
     */
    public static final String INTERVAL_1S = "1s";
    public static final String INTERVAL_5S = "5s";
    public static final String INTERVAL_1M = "1m";
    public static final String INTERVAL_5M = "5m";
    public static final String INTERVAL_1H = "1h";

    private ApiConstants() {}
}
