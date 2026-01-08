/**
 * PWA Service Worker
 * 用于离线缓存和后台同步
 */

const CACHE_NAME = 'room-inspection-v1'
const urlsToCache = [
  '/',
  '/index.html',
  '/manifest.json',
  '/api/availability/overview'
]

// 安装Service Worker
self.addEventListener('install', event => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then(cache => {
        console.log('Opened cache')
        return cache.addAll(urlsToCache)
      })
  )
})

// 激活Service Worker
self.addEventListener('activate', event => {
  event.waitUntil(
    caches.keys().then(cacheNames => {
      return Promise.all(
        cacheNames.map(cacheName => {
          if (cacheName !== CACHE_NAME) {
            console.log('Deleting old cache:', cacheName)
            return caches.delete(cacheName)
          }
        })
      )
    })
  )
})

// 拦截网络请求
self.addEventListener('fetch', event => {
  event.respondWith(
    caches.match(event.request)
      .then(response => {
        // 缓存命中，返回缓存资源
        if (response) {
          return response
        }

        // 缓存未命中，发起网络请求
        return fetch(event.request).then(response => {
          // 检查是否为有效响应
          if (!response || response.status !== 200 || response.type !== 'basic') {
            return response
          }

          // 克隆响应
          const responseToCache = response.clone()

          // 将响应添加到缓存
          caches.open(CACHE_NAME)
            .then(cache => {
              cache.put(event.request, responseToCache)
            })

          return response
        }).catch(() => {
          // 网络请求失败，返回离线页面
          return caches.match('/offline.html')
        })
      })
  )
})

// 后台同步
self.addEventListener('sync', event => {
  if (event.tag === 'sync-alarm') {
    event.waitUntil(syncAlarms())
  } else if (event.tag === 'sync-workorder') {
    event.waitUntil(syncWorkOrders())
  }
})

// 推送通知
self.addEventListener('push', event => {
  const options = {
    body: event.data ? event.data.text() : '新消息',
    icon: '/icon-192.png',
    badge: '/icon-192.png',
    vibrate: [200, 100, 200],
    data: {
      dateOfArrival: Date.now(),
      primaryKey: 1
    }
  }

  event.waitUntil(
    self.registration.showNotification('机房巡检系统', options)
  )
})

// 通知点击
self.addEventListener('notificationclick', event => {
  event.notification.close()

  event.waitUntil(
    clients.openWindow('/')
  )
})

// 同步告警
async function syncAlarms() {
  // 实现告警同步逻辑
  console.log('Syncing alarms...')
}

// 同步工单
async function syncWorkOrders() {
  // 实现工单同步逻辑
  console.log('Syncing work orders...')
}
