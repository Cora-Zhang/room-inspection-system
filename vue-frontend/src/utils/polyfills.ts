/**
 * 浏览器兼容性Polyfills
 * 确保在主流浏览器（Chrome、Edge、Safari、Firefox）上正常运行
 */

// 1. Promise Polyfill
if (!window.Promise) {
  window.Promise = require('core-js/es/promise');
}

// 2. Fetch API Polyfill
if (!window.fetch) {
  window.fetch = require('whatwg-fetch');
}

// 3. Array.prototype.includes Polyfill
if (!Array.prototype.includes) {
  Array.prototype.includes = function(searchElement: any, fromIndex?: number): boolean {
    if (this == null) {
      throw new TypeError('"this" is null or not defined');
    }
    const O = Object(this);
    const len = O.length >>> 0;
    if (len === 0) {
      return false;
    }
    const n = fromIndex || 0;
    let k = Math.max(n >= 0 ? n : len - Math.abs(n), 0);
    while (k < len) {
      if (O[k] === searchElement) {
        return true;
      }
      k++;
    }
    return false;
  };
}

// 4. String.prototype.includes Polyfill
if (!String.prototype.includes) {
  String.prototype.includes = function(search: string, start?: number): boolean {
    if (typeof start !== 'number') {
      start = 0;
    }
    if (start + search.length > this.length) {
      return false;
    } else {
      return this.indexOf(search, start) !== -1;
    }
  };
}

// 5. Object.assign Polyfill
if (typeof Object.assign !== 'function') {
  Object.assign = function(target: any, ...sources: any[]): any {
    if (target == null) {
      throw new TypeError('Cannot convert undefined or null to object');
    }
    const to = Object(target);
    for (let index = 0; index < sources.length; index++) {
      const nextSource = sources[index];
      if (nextSource != null) {
        for (const nextKey in nextSource) {
          if (Object.prototype.hasOwnProperty.call(nextSource, nextKey)) {
            to[nextKey] = nextSource[nextKey];
          }
        }
      }
    }
    return to;
  };
}

// 6. Array.prototype.find Polyfill
if (!Array.prototype.find) {
  Array.prototype.find = function(predicate: any, thisArg?: any): any {
    if (this == null) {
      throw new TypeError('Array.prototype.find called on null or undefined');
    }
    if (typeof predicate !== 'function') {
      throw new TypeError('predicate must be a function');
    }
    const list = Object(this);
    const length = list.length >>> 0;
    for (let i = 0; i < length; i++) {
      if (predicate.call(thisArg, list[i], i, list)) {
        return list[i];
      }
    }
    return undefined;
  };
}

// 7. Array.prototype.findIndex Polyfill
if (!Array.prototype.findIndex) {
  Array.prototype.findIndex = function(predicate: any, thisArg?: any): number {
    if (this == null) {
      throw new TypeError('Array.prototype.findIndex called on null or undefined');
    }
    if (typeof predicate !== 'function') {
      throw new TypeError('predicate must be a function');
    }
    const list = Object(this);
    const length = list.length >>> 0;
    for (let i = 0; i < length; i++) {
      if (predicate.call(thisArg, list[i], i, list)) {
        return i;
      }
    }
    return -1;
  };
}

// 8. requestAnimationFrame Polyfill
let lastTime = 0;
if (!window.requestAnimationFrame) {
  window.requestAnimationFrame = function(callback: FrameRequestCallback): number {
    const currentTime = new Date().getTime();
    const timeToCall = Math.max(0, 16 - (currentTime - lastTime));
    const id = window.setTimeout(() => {
      callback(currentTime + timeToCall);
    }, timeToCall);
    lastTime = currentTime + timeToCall;
    return id;
  };
}

// 9. cancelAnimationFrame Polyfill
if (!window.cancelAnimationFrame) {
  window.cancelAnimationFrame = function(id: number): void {
    clearTimeout(id);
  };
}

// 10. IntersectionObserver Polyfill（用于懒加载）
if (!('IntersectionObserver' in window)) {
  require('intersection-observer');
}

// 11. ResizeObserver Polyfill（用于响应式布局）
if (!('ResizeObserver' in window)) {
  require('resize-observer-polyfill');
}

// 12. CustomEvent Polyfill
if (typeof window.CustomEvent !== 'function') {
  function CustomEvent(event: string, params: any = {}) {
    const evt = document.createEvent('CustomEvent');
    evt.initCustomEvent(event, params.bubbles === false ? false : true, params.cancelable === false ? false : true, params.detail);
    return evt;
  }
  (window as any).CustomEvent = CustomEvent;
}

// 13. URLSearchParams Polyfill
if (!window.URLSearchParams) {
  window.URLSearchParams = require('@ungap/url-search-params');
}

// 14. WebSocket Polyfill（在IE等旧浏览器中）
if (!window.WebSocket) {
  window.WebSocket = require('websocket-polyfill');
}

export default {
  // Polyfills已加载
  version: '1.0.0'
};
