/**
 * 浏览器检测工具
 * 用于检测当前浏览器类型、版本和兼容性
 */

export interface BrowserInfo {
  name: string;
  version: string;
  os: string;
  isSupported: boolean;
  features: {
    webp: boolean;
    webSocket: boolean;
    localStorage: boolean;
    sessionStorage: boolean;
    canvas: boolean;
    webGL: boolean;
    geolocation: boolean;
    notification: boolean;
  };
}

/**
 * 检测浏览器信息
 */
export function detectBrowser(): BrowserInfo {
  const ua = navigator.userAgent;
  let browserName = 'Unknown';
  let browserVersion = '0.0';
  let os = 'Unknown';

  // 检测操作系统
  if (ua.indexOf('Win') !== -1) os = 'Windows';
  else if (ua.indexOf('Mac') !== -1) os = 'MacOS';
  else if (ua.indexOf('Linux') !== -1) os = 'Linux';
  else if (ua.indexOf('Android') !== -1) os = 'Android';
  else if (ua.indexOf('iPhone') !== -1 || ua.indexOf('iPad') !== -1) os = 'iOS';

  // 检测浏览器类型和版本
  if (ua.indexOf('Chrome') !== -1 && ua.indexOf('Edg') === -1) {
    browserName = 'Chrome';
    const match = ua.match(/Chrome\/(\d+\.\d+\.\d+\.\d+)/);
    if (match) browserVersion = match[1];
  } else if (ua.indexOf('Edg') !== -1) {
    browserName = 'Edge';
    const match = ua.match(/Edg\/(\d+\.\d+\.\d+\.\d+)/);
    if (match) browserVersion = match[1];
  } else if (ua.indexOf('Safari') !== -1 && ua.indexOf('Chrome') === -1) {
    browserName = 'Safari';
    const match = ua.match(/Version\/(\d+\.\d+)/);
    if (match) browserVersion = match[1];
  } else if (ua.indexOf('Firefox') !== -1) {
    browserName = 'Firefox';
    const match = ua.match(/Firefox\/(\d+\.\d+)/);
    if (match) browserVersion = match[1];
  }

  // 检测特性支持
  const features = {
    webp: checkWebPSupport(),
    webSocket: checkWebSocketSupport(),
    localStorage: checkLocalStorageSupport(),
    sessionStorage: checkSessionStorageSupport(),
    canvas: checkCanvasSupport(),
    webGL: checkWebGLSupport(),
    geolocation: checkGeolocationSupport(),
    notification: checkNotificationSupport()
  };

  // 判断是否支持
  const isSupported = checkBrowserSupport(browserName, browserVersion);

  return {
    name: browserName,
    version: browserVersion,
    os,
    isSupported,
    features
  };
}

/**
 * 检查WebP支持
 */
function checkWebPSupport(): boolean {
  const canvas = document.createElement('canvas');
  if (canvas.getContext && canvas.getContext('2d')) {
    return canvas.toDataURL('image/webp').indexOf('data:image/webp') === 0;
  }
  return false;
}

/**
 * 检查WebSocket支持
 */
function checkWebSocketSupport(): boolean {
  return 'WebSocket' in window || 'MozWebSocket' in window;
}

/**
 * 检查LocalStorage支持
 */
function checkLocalStorageSupport(): boolean {
  try {
    const test = '__localStorageTest__';
    localStorage.setItem(test, test);
    localStorage.removeItem(test);
    return true;
  } catch (e) {
    return false;
  }
}

/**
 * 检查SessionStorage支持
 */
function checkSessionStorageSupport(): boolean {
  try {
    const test = '__sessionStorageTest__';
    sessionStorage.setItem(test, test);
    sessionStorage.removeItem(test);
    return true;
  } catch (e) {
    return false;
  }
}

/**
 * 检查Canvas支持
 */
function checkCanvasSupport(): boolean {
  const canvas = document.createElement('canvas');
  return !!(canvas.getContext && canvas.getContext('2d'));
}

/**
 * 检查WebGL支持
 */
function checkWebGLSupport(): boolean {
  try {
    const canvas = document.createElement('canvas');
    return !!(canvas.getContext('webgl') || canvas.getContext('experimental-webgl'));
  } catch (e) {
    return false;
  }
}

/**
 * 检查Geolocation支持
 */
function checkGeolocationSupport(): boolean {
  return 'geolocation' in navigator;
}

/**
 * 检查Notification支持
 */
function checkNotificationSupport(): boolean {
  return 'Notification' in window;
}

/**
 * 检查浏览器是否支持
 */
function checkBrowserSupport(name: string, version: string): boolean {
  const v = parseFloat(version);

  switch (name) {
    case 'Chrome':
      return v >= 90;
    case 'Edge':
      return v >= 90;
    case 'Safari':
      return v >= 14;
    case 'Firefox':
      return v >= 88;
    default:
      return false;
  }
}

/**
 * 显示浏览器兼容性警告
 */
export function showBrowserWarning(): void {
  const browserInfo = detectBrowser();

  if (!browserInfo.isSupported) {
    const warningHtml = `
      <div style="position: fixed; top: 0; left: 0; right: 0; bottom: 0;
                  background: rgba(0, 0, 0, 0.9); z-index: 999999;
                  display: flex; align-items: center; justify-content: center;">
        <div style="background: #1a1a2e; padding: 40px; border-radius: 10px;
                    max-width: 600px; text-align: center; color: #fff;">
          <h2 style="color: #ff6b6b; margin-bottom: 20px;">
            ⚠️ 浏览器兼容性警告
          </h2>
          <p style="margin-bottom: 20px; line-height: 1.6;">
            您正在使用的浏览器（${browserInfo.name} ${browserInfo.version}）可能无法完全支持本系统。
          </p>
          <p style="margin-bottom: 20px; line-height: 1.6;">
            建议使用以下最新版本的浏览器：
          </p>
          <ul style="text-align: left; margin-bottom: 30px; line-height: 2;">
            <li>🔵 Google Chrome 90+</li>
            <li>🟢 Microsoft Edge 90+</li>
            <li>🟠 Safari 14+</li>
            <li>🟤 Firefox 88+</li>
          </ul>
          <a href="https://www.google.com/chrome/"
             style="background: #4e73df; color: #fff; padding: 12px 30px;
                    border-radius: 5px; text-decoration: none; margin-right: 10px;">
            下载Chrome
          </a>
          <a href="https://www.microsoft.com/edge"
             style="background: #00a4ef; color: #fff; padding: 12px 30px;
                    border-radius: 5px; text-decoration: none;">
            下载Edge
          </a>
        </div>
      </div>
    `;

    const warningDiv = document.createElement('div');
    warningDiv.innerHTML = warningHtml;
    document.body.appendChild(warningDiv);
  }
}

/**
 * 获取浏览器推荐信息
 */
export function getBrowserRecommendation(): { message: string; actions: string[] } {
  const browserInfo = detectBrowser();

  if (browserInfo.isSupported) {
    return {
      message: '您的浏览器完全支持本系统，可以正常使用所有功能。',
      actions: []
    };
  }

  const actions: string[] = [];

  if (!browserInfo.features.webSocket) {
    actions.push('建议升级浏览器以支持实时通讯功能');
  }

  if (!browserInfo.features.localStorage) {
    actions.push('建议启用Cookie和本地存储以保存用户偏好');
  }

  if (!browserInfo.features.notification) {
    actions.push('建议启用通知权限以接收告警信息');
  }

  return {
    message: `检测到您使用的是 ${browserInfo.name} ${browserInfo.version}，建议升级到最新版本以获得最佳体验。`,
    actions
  };
}
