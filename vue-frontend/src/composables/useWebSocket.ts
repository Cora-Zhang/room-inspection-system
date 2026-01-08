import { ref, onMounted, onUnmounted, Ref } from 'vue'

interface WebSocketOptions {
  url: string
  reconnectInterval?: number
  maxReconnectAttempts?: number
  onMessage?: (data: any) => void
  onOpen?: () => void
  onClose?: () => void
  onError?: (error: Event) => void
}

export function useWebSocket(options: WebSocketOptions) {
  const {
    url,
    reconnectInterval = 3000,
    maxReconnectAttempts = 5,
    onMessage,
    onOpen,
    onClose,
    onError
  } = options

  const ws = ref<WebSocket | null>(null) as Ref<WebSocket | null>
  const isConnected = ref(false)
  const reconnectAttempts = ref(0)
  const reconnectTimer = ref<NodeJS.Timeout | null>(null)

  const connect = () => {
    try {
      ws.value = new WebSocket(url)

      ws.value.onopen = () => {
        isConnected.value = true
        reconnectAttempts.value = 0
        console.log('WebSocket连接成功:', url)
        onOpen?.()
      }

      ws.value.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data)
          onMessage?.(data)
        } catch (error) {
          console.error('解析WebSocket消息失败:', error)
        }
      }

      ws.value.onclose = () => {
        isConnected.value = false
        console.log('WebSocket连接关闭:', url)
        onClose?.()

        // 自动重连
        if (reconnectAttempts.value < maxReconnectAttempts) {
          reconnectTimer.value = setTimeout(() => {
            reconnectAttempts.value++
            console.log(`尝试重连WebSocket (${reconnectAttempts.value}/${maxReconnectAttempts})`)
            connect()
          }, reconnectInterval)
        }
      }

      ws.value.onerror = (error) => {
        console.error('WebSocket错误:', error)
        onError?.(error)
      }
    } catch (error) {
      console.error('WebSocket连接失败:', error)
    }
  }

  const disconnect = () => {
    if (reconnectTimer.value) {
      clearTimeout(reconnectTimer.value)
      reconnectTimer.value = null
    }

    if (ws.value) {
      ws.value.close()
      ws.value = null
    }

    isConnected.value = false
  }

  const send = (data: any) => {
    if (ws.value && isConnected.value) {
      try {
        const message = typeof data === 'string' ? data : JSON.stringify(data)
        ws.value.send(message)
      } catch (error) {
        console.error('发送WebSocket消息失败:', error)
      }
    } else {
      console.warn('WebSocket未连接，无法发送消息')
    }
  }

  onMounted(() => {
    connect()
  })

  onUnmounted(() => {
    disconnect()
  })

  return {
    ws,
    isConnected,
    connect,
    disconnect,
    send
  }
}

// 告警WebSocket Hook
export function useAlarmWebSocket(onMessage: (data: any) => void) {
  const wsUrl = import.meta.env.VITE_WS_URL || 'ws://localhost:8080/api/ws/alarms'

  return useWebSocket({
    url: wsUrl,
    onMessage,
    onOpen: () => {
      console.log('告警WebSocket已连接')
    },
    onClose: () => {
      console.log('告警WebSocket已断开')
    }
  })
}

// 监控数据WebSocket Hook
export function useMonitorWebSocket(onMessage: (data: any) => void) {
  const wsUrl = import.meta.env.VITE_WS_URL || 'ws://localhost:8080/api/ws/monitor'

  return useWebSocket({
    url: wsUrl,
    onMessage,
    onOpen: () => {
      console.log('监控数据WebSocket已连接')
    },
    onClose: () => {
      console.log('监控数据WebSocket已断开')
    }
  })
}
