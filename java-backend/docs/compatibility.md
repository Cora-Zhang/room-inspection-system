# 机房巡检系统 - 兼容性功能

## 功能概述

本系统实现了完整的兼容性功能，包括浏览器兼容性、IDC设备厂商协议对接、主流门禁系统接口和移动端适配。确保系统在主流浏览器上正常运行，支持常见IDC设备厂商的协议，支持主流门禁系统接口，并支持移动端访问。

## 核心功能

### 1. 浏览器兼容性

**功能说明：**
- 支持主流浏览器（Chrome、Edge、Safari、Firefox）
- 自动浏览器检测和兼容性提示
- Polyfills支持旧版本浏览器
- 响应式设计适配不同屏幕尺寸

**实现文件：**
- `vue-frontend/.browserslistrc` - 浏览器兼容性配置
- `vue-frontend/src/utils/polyfills.ts` - Polyfills集合
- `vue-frontend/src/utils/browserDetector.ts` - 浏览器检测工具

**支持的浏览器版本：**
- Chrome >= 90
- Edge >= 90
- Safari >= 14
- Firefox >= 88
- iOS >= 14
- Android >= 10

**Polyfills功能：**
- Promise
- Fetch API
- Array.includes/find/findIndex
- String.includes
- Object.assign
- requestAnimationFrame/cancelAnimationFrame
- IntersectionObserver
- ResizeObserver
- CustomEvent
- URLSearchParams
- WebSocket

**使用说明：**
```typescript
import { detectBrowser, showBrowserWarning, getBrowserRecommendation } from '@/utils/browserDetector'

// 检测浏览器信息
const browserInfo = detectBrowser()
console.log(browserInfo.name, browserInfo.version, browserInfo.isSupported)

// 显示浏览器兼容性警告（不支持的浏览器会自动显示）
showBrowserWarning()

// 获取浏览器推荐信息
const recommendation = getBrowserRecommendation()
console.log(recommendation.message, recommendation.actions)
```

### 2. IDC设备协议对接

**功能说明：**
- 支持主流IDC设备厂商的协议对接
- 统一的协议接口，便于扩展
- 支持SNMP、Modbus等协议
- 支持批量数据采集和实时监控

**支持的设备厂商：**
| 厂商 | 设备类型 | 协议 | 状态 |
|------|---------|------|------|
| APC | UPS、PDU、环境监控 | SNMP | ✅ 已实现 |
| Schneider | PDU、UPS、环境监控 | Modbus TCP | ✅ 已实现 |
| Emerson | Liebert UPS、PDU、环境监控 | SNMP | ✅ 已实现 |
| Huawei | UPS、PDU | SNMP | ✅ 已实现 |
| Vertiv | UPS、PDU | SNMP | ✅ 已实现 |
| Rittal | CMC环境监控 | SNMP | ✅ 已实现 |

**核心接口：**
```java
public interface DeviceProtocol {
    void connect(String host, int port, Map<String, Object> params);
    void disconnect();
    boolean isConnected();
    Map<String, Object> readData(String address, String dataType);
    void writeData(String address, String dataType, Object value);
    Map<String, Object> readDeviceStatus();
    Map<String, Object> batchRead(Map<String, String> addressMap);
    void setAlarmThreshold(String address, String alarmType, double threshold);
    Map<String, Object> getDeviceInfo();
}
```

**协议工厂：**
```java
@Component
public class DeviceProtocolFactory {
    public DeviceProtocol createProtocol(String manufacturer) {
        // 根据厂商创建对应的协议适配器
    }
}
```

**使用示例：**
```java
@Autowired
private DeviceProtocolFactory protocolFactory;

// 创建APC协议适配器
DeviceProtocol apcProtocol = protocolFactory.createProtocol("APC");

// 连接设备
Map<String, Object> params = new HashMap<>();
params.put("community", "public");
apcProtocol.connect("192.168.1.100", 161, params);

// 读取设备状态
Map<String, Object> status = apcProtocol.readDeviceStatus();

// 读取温度传感器
Map<String, Object> temperatures = ((APCProtocolAdapter) apcProtocol).readTemperatureSensors();

// 断开连接
apcProtocol.disconnect();
```

**APC协议特性：**
- 支持APC NetBotz环境监控系统
- 支持APC UPS设备
- 支持温度、湿度、告警状态监控
- 支持批量数据采集

**Schneider协议特性：**
- 支持施耐德Modbus设备
- 支持PDU、UPS、环境监控
- 支持电压、电流、功率、频率采集
- 支持多路设备监控

**Emerson协议特性：**
- 支持Emerson Liebert UPS
- 支持Emerson PDU
- 支持环境监控设备
- 支持电池状态监控

### 3. 门禁系统接口

**功能说明：**
- 支持主流门禁系统API对接
- 统一的门禁系统接口
- 支持权限下发和撤销
- 支持门禁日志查询
- 支持实时事件监听

**支持的门禁厂商：**
| 厂商 | 设备类型 | 协议 | 状态 |
|------|---------|------|------|
| Hikvision | iVMS-4200、DS-K系列 | REST API | ✅ 已实现 |
| Dahua | DH-ASC系列 | REST API | ✅ 已实现 |
| Uniview | UNV系列 | REST API | ✅ 已实现 |

**核心接口：**
```java
public interface DoorAccessSystem {
    void connect(String host, int port, Map<String, Object> params);
    void disconnect();
    boolean isConnected();
    boolean authenticate(String userId, String password);
    boolean openDoor(String doorId, String userId);
    boolean closeDoor(String doorId, String userId);
    boolean grantPermission(String userId, List<String> doorIds, Map<String, Object> schedule);
    boolean revokePermission(String userId, List<String> doorIds);
    List<Map<String, Object>> getUserPermissions(String userId);
    List<Map<String, Object>> getAccessLogs(String doorId, String startTime, String endTime);
    void registerEventListener(DoorAccessEventListener listener);
    Map<String, Object> getDoorStatus(String doorId);
}
```

**门禁系统工厂：**
```java
@Component
public class DoorAccessSystemFactory {
    public DoorAccessSystem createDoorAccessSystem(String manufacturer) {
        // 根据厂商创建对应的门禁系统适配器
    }
}
```

**使用示例：**
```java
@Autowired
private DoorAccessSystemFactory doorAccessFactory;

// 创建海康威视门禁系统
DoorAccessSystem hikvisionSystem = doorAccessFactory.createDoorAccessSystem("Hikvision");

// 连接系统
Map<String, Object> params = new HashMap<>();
params.put("username", "admin");
params.put("password", "admin123");
hikvisionSystem.connect("192.168.1.100", 80, params);

// 开门
boolean success = hikvisionSystem.openDoor("DOOR001", "USER001");

// 下发权限
List<String> doorIds = Arrays.asList("DOOR001", "DOOR002");
Map<String, Object> schedule = new HashMap<>();
schedule.put("startTime", "08:00");
schedule.put("endTime", "18:00");
hikvisionSystem.grantPermission("USER001", doorIds, schedule);

// 获取门禁日志
List<Map<String, Object>> logs = hikvisionSystem.getAccessLogs("DOOR001", "2024-01-01 00:00:00", "2024-01-02 00:00:00");

// 注册事件监听器
hikvisionSystem.registerEventListener(event -> {
    System.out.println("门禁事件: " + event.getEventType());
});

// 断开连接
hikvisionSystem.disconnect();
```

**海康威视门禁特性：**
- 支持iVMS-4200平台
- 支持DS-K系列门禁控制器
- 支持权限精细化控制（精确到分钟）
- 支持门禁日志查询
- 支持实时事件监听

**大华门禁特性：**
- 支持DH-ASC系列门禁控制器
- 支持用户认证和权限管理
- 支持门禁日志查询
- 支持实时事件监听

**宇视门禁特性：**
- 支持UNV系列门禁控制器
- 支持用户认证和权限管理
- 支持门禁日志查询
- 支持实时事件监听

### 4. 移动端适配

**功能说明：**
- 响应式设计，适配各种屏幕尺寸
- 移动端专用布局
- 移动端告警查看
- 移动端工单处理
- PWA支持，可离线访问
- 推送通知支持

**移动端布局：**
- `vue-frontend/src/layout/MobileLayout.vue` - 移动端布局组件
- 顶部导航栏
- 侧边菜单
- 底部标签栏
- 响应式主内容区

**移动端页面：**
- `/mobile/dashboard` - 首页大屏
- `/mobile/alarm` - 告警列表
- `/mobile/workorder` - 工单列表
- `/mobile/device` - 设备监控
- `/mobile/inspection` - 巡检管理
- `/mobile/profile` - 个人中心

**移动端告警查看功能：**
```vue
<template>
  <div class="alarm-list">
    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-select v-model="filterLevel" placeholder="告警级别" />
      <el-select v-model="filterStatus" placeholder="处理状态" />
      <el-button @click="handleRefresh">刷新</el-button>
    </div>
    
    <!-- 告警列表 -->
    <div class="alarm-items">
      <el-card v-for="alarm in alarmList" @click="handleViewDetail(alarm)">
        <el-tag :type="getLevelType(alarm.level)">{{ getLevelText(alarm.level) }}</el-tag>
        <div class="alarm-title">{{ alarm.alarmName }}</div>
        <el-button @click.stop="handleProcess(alarm)">处理</el-button>
      </el-card>
    </div>
  </div>
</template>
```

**移动端工单处理功能：**
- 支持工单列表查看
- 支持工单筛选（类型、状态）
- 支持工单详情查看
- 支持工单处理（完成/驳回）
- 支持上传现场照片
- 支持填写处理说明

**移动端UI特性：**
- 适合触屏操作的大按钮
- 优化的字体大小和间距
- 流畅的动画效果
- 适配不同屏幕尺寸
- 科幻风格主题

### 5. PWA支持

**功能说明：**
- 支持安装到主屏幕
- 支持离线访问
- 支持后台同步
- 支持推送通知
- 支持自动更新

**PWA配置：**
```json
{
  "name": "机房巡检系统",
  "short_name": "机房巡检",
  "description": "机房巡检系统移动端",
  "theme_color": "#667eea",
  "background_color": "#ffffff",
  "display": "standalone",
  "orientation": "portrait",
  "start_url": "/",
  "scope": "/",
  "icons": [
    {
      "src": "/icon-192.png",
      "sizes": "192x192",
      "type": "image/png"
    },
    {
      "src": "/icon-512.png",
      "sizes": "512x512",
      "type": "image/png"
    }
  ]
}
```

**Service Worker功能：**
- 缓存静态资源
- 离线访问支持
- 后台同步（告警、工单）
- 推送通知支持
- 自动更新检测

**PWA使用：**
```typescript
import { registerServiceWorker, requestNotificationPermission, showNotification } from '@/utils/pwa'

// 注册Service Worker
registerServiceWorker()

// 请求通知权限
requestNotificationPermission()

// 显示通知
showNotification('新告警', '机房温度超过阈值')

// 检查在线状态
import { checkOnlineStatus, addOnlineStatusListener } from '@/utils/pwa'

const isOnline = checkOnlineStatus()

addOnlineStatusListener((online) => {
  console.log('网络状态:', online ? '在线' : '离线')
})
```

**PWA特性：**
- 可安装到手机主屏幕
- 支持离线访问缓存的内容
- 支持推送通知（新告警、新工单）
- 支持后台数据同步
- 支持应用自动更新
- 支持分享功能

## 兼容性测试

### 浏览器兼容性测试

| 浏览器 | 版本 | 兼容性 | 说明 |
|--------|------|--------|------|
| Chrome | 90+ | ✅ 完全兼容 | 推荐使用 |
| Edge | 90+ | ✅ 完全兼容 | 推荐使用 |
| Safari | 14+ | ✅ 完全兼容 | iOS/macOS推荐 |
| Firefox | 88+ | ✅ 完全兼容 | Linux推荐 |
| IE 11 | ❌ 不兼容 | 需升级浏览器 |

### 设备兼容性测试

| 设备类型 | 分辨率 | 兼容性 | 说明 |
|----------|--------|--------|------|
| PC端 | 1920x1080 | ✅ 完全兼容 | 桌面浏览器 |
| 笔记本 | 1366x768 | ✅ 完全兼容 | 桌面浏览器 |
| 平板 | 768x1024 | ✅ 完全兼容 | 响应式布局 |
| 手机 | 375x667 | ✅ 完全兼容 | 移动端PWA |
| 大屏 | 3840x2160 | ✅ 完全兼容 | 4K显示器 |

### 协议兼容性测试

| 协议 | 版本 | 兼容性 | 说明 |
|------|------|--------|------|
| SNMP | v2c/v3 | ✅ 完全兼容 | 大部分设备 |
| Modbus TCP | - | ✅ 完全兼容 | Schneider等 |
| REST API | v1.0 | ✅ 完全兼容 | 门禁系统 |

## 部署配置

### 浏览器兼容性配置

```bash
# 安装依赖
pnpm install core-js whatwg-fetch intersection-observer resize-observer-polyfill @ungap/url-search-params
```

### IDC设备协议配置

```yaml
# application.yml
monitor:
  apc:
    enabled: true
    timeout: 5000
    community: public
  
  schneider:
    enabled: true
    timeout: 5000
    slave-id: 1
  
  emerson:
    enabled: true
    timeout: 5000
    community: public
```

### 门禁系统配置

```yaml
# application.yml
access-control:
  hikvision:
    enabled: true
    api-url: http://hikvision.example.com/api
    username: admin
    password: admin123
  
  dahua:
    enabled: true
    api-url: http://dahua.example.com/api
    username: admin
    password: admin123
  
  uniview:
    enabled: true
    api-url: http://uniview.example.com/api
    username: admin
    password: admin123
```

### PWA配置

```typescript
// main.ts
import { registerServiceWorker, requestNotificationPermission } from '@/utils/pwa'

registerServiceWorker()
requestNotificationPermission()
```

## 最佳实践

### 浏览器兼容性
1. 推荐使用Chrome 90+或Edge 90+
2. iOS推荐使用Safari 14+
3. 不支持IE 11，需要升级
4. 定期更新浏览器版本

### IDC设备协议
1. 根据设备厂商选择对应的协议适配器
2. 确保网络连通性
3. 合理设置超时时间
4. 定期检查设备连接状态

### 门禁系统接口
1. 配置正确的API地址和凭证
2. 定期同步用户和权限数据
3. 监听门禁事件进行实时告警
4. 定期备份门禁日志

### 移动端适配
1. 使用PWA提升用户体验
2. 开启推送通知及时接收告警
3. 优化网络请求减少流量
4. 合理使用离线缓存

## 常见问题

### Q1: 在IE 11上无法访问系统？

A: IE 11不支持现代Web技术，请升级到Chrome、Edge或其他现代浏览器。

### Q2: 移动端无法安装PWA？

A: 确保浏览器支持PWA功能，推荐使用Chrome for Android或Safari for iOS。

### Q3: 设备协议对接失败？

A: 检查网络连接、设备地址、端口、用户名密码等配置是否正确。

### Q4: 门禁系统无法连接？

A: 检查API地址是否正确，网络是否通畅，认证信息是否正确。

### Q5: 推送通知不生效？

A: 确保已授予通知权限，Service Worker正常运行。

## 总结

本系统实现了完整的兼容性功能：
- ✅ 支持主流浏览器（Chrome、Edge、Safari、Firefox）
- ✅ 支持常见IDC设备厂商协议对接（APC、Schneider、Emerson等）
- ✅ 支持主流门禁系统接口（Hikvision、Dahua、Uniview）
- ✅ 移动端适配（响应式布局、移动端页面）
- ✅ PWA支持（离线访问、推送通知）

通过这些兼容性功能，确保系统能够在各种环境下稳定运行，提供良好的用户体验。
