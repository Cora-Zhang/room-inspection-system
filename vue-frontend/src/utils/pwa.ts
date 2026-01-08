/**
 * PWA工具类
 * 用于注册Service Worker和管理应用生命周期
 */

export function registerServiceWorker() {
  if ('serviceWorker' in navigator) {
    window.addEventListener('load', () => {
      navigator.serviceWorker.register('/service-worker.js')
        .then(registration => {
          console.log('ServiceWorker registered with scope:', registration.scope)

          // 检查更新
          registration.addEventListener('updatefound', () => {
            const newWorker = registration.installing
            if (newWorker) {
              newWorker.addEventListener('statechange', () => {
                if (newWorker.state === 'installed' && navigator.serviceWorker.controller) {
                  // 新版本可用，提示用户刷新
                  showUpdateNotification()
                }
              })
            }
          })
        })
        .catch(error => {
          console.log('ServiceWorker registration failed:', error)
        })
    })
  }
}

export function unregisterServiceWorker() {
  if ('serviceWorker' in navigator) {
    navigator.serviceWorker.ready
      .then(registration => {
        registration.unregister()
      })
      .catch(error => {
        console.log(error.message)
      })
  }
}

export function requestNotificationPermission() {
  if ('Notification' in window && Notification.permission !== 'granted') {
    Notification.requestPermission().then(permission => {
      if (permission === 'granted') {
        new Notification('机房巡检系统', {
          body: '通知权限已开启',
          icon: '/icon-192.png'
        })
      }
    })
  }
}

export function showNotification(title: string, body: string, data?: any) {
  if ('Notification' in window && Notification.permission === 'granted') {
    const options = {
      body,
      icon: '/icon-192.png',
      badge: '/icon-192.png',
      tag: 'room-inspection',
      renotify: true,
      requireInteraction: false,
      data: data || {}
    }

    new Notification(title, options)
  }
}

function showUpdateNotification() {
  if ('Notification' in window && Notification.permission === 'granted') {
    const notification = new Notification('有新版本可用', {
      body: '点击刷新页面获取最新版本',
      icon: '/icon-192.png',
      tag: 'app-update',
      requireInteraction: true
    })

    notification.onclick = () => {
      window.location.reload()
    }
  }
}

export function checkOnlineStatus(): boolean {
  return navigator.onLine
}

export function addOnlineStatusListener(callback: (online: boolean) => void) {
  window.addEventListener('online', () => callback(true))
  window.addEventListener('offline', () => callback(false))
}

export async function subscribePushNotifications() {
  if ('serviceWorker' in navigator && 'PushManager' in window) {
    try {
      const registration = await navigator.serviceWorker.ready
      const subscription = await registration.pushManager.subscribe({
        userVisibleOnly: true,
        applicationServerKey: urlBase64ToUint8Array('YOUR_PUBLIC_KEY')
      })

      // 将订阅信息发送到服务器
      await fetch('/api/push/subscribe', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(subscription)
      })

      console.log('Push notification subscribed')
      return subscription
    } catch (error) {
      console.error('Push notification subscription failed:', error)
      return null
    }
  }
  return null
}

function urlBase64ToUint8Array(base64String: string) {
  const padding = '='.repeat((4 - base64String.length % 4) % 4)
  const base64 = (base64String + padding)
    .replace(/-/g, '+')
    .replace(/_/g, '/')

  const rawData = window.atob(base64)
  const outputArray = new Uint8Array(rawData.length)

  for (let i = 0; i < rawData.length; ++i) {
    outputArray[i] = rawData.charCodeAt(i)
  }

  return outputArray
}
