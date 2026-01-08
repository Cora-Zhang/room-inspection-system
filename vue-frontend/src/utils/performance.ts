// 性能监控工具函数

export interface PerformanceMetrics {
  fps: number
  memory: number
  loadTime: number
  renderTime: number
  resourceCount: number
  timestamp: number
}

// 计算FPS
let frameCount = 0
let lastTime = performance.now()

export function calculateFPS(): number {
  frameCount++
  const currentTime = performance.now()

  if (currentTime - lastTime >= 1000) {
    const fps = frameCount
    frameCount = 0
    lastTime = currentTime
    return fps
  }

  return 0
}

// 获取内存使用情况
export function getMemoryUsage(): number {
  if ('memory' in performance) {
    const memory = (performance as any).memory
    return memory.usedJSHeapSize / (1024 * 1024) // MB
  }
  return 0
}

// 测量函数执行时间
export function measureTime<T>(fn: () => T, label = 'Execution time'): { result: T; duration: number } {
  const startTime = performance.now()
  const result = fn()
  const duration = performance.now() - startTime

  console.log(`${label}: ${duration.toFixed(2)}ms`)

  return { result, duration }
}

// 异步测量函数执行时间
export async function measureAsyncTime<T>(
  fn: () => Promise<T>,
  label = 'Async execution time'
): Promise<{ result: T; duration: number }> {
  const startTime = performance.now()
  const result = await fn()
  const duration = performance.now() - startTime

  console.log(`${label}: ${duration.toFixed(2)}ms`)

  return { result, duration }
}

// 获取页面性能指标
export function getPagePerformanceMetrics(): PerformanceMetrics {
  const navigation = performance.getEntriesByType('navigation')[0] as PerformanceNavigationTiming

  return {
    fps: calculateFPS(),
    memory: getMemoryUsage(),
    loadTime: navigation ? navigation.loadEventEnd - navigation.fetchStart : 0,
    renderTime: navigation ? navigation.domComplete - navigation.domInteractive : 0,
    resourceCount: performance.getEntriesByType('resource').length,
    timestamp: Date.now()
  }
}

// 资源加载性能
export function getResourceMetrics() {
  const resources = performance.getEntriesByType('resource')

  const metrics = {
    total: resources.length,
    byType: {} as Record<string, number>,
    slowResources: [] as Array<{ name: string; duration: number }>,
    totalSize: 0
  }

  resources.forEach((resource: any) => {
    const type = resource.initiatorType
    metrics.byType[type] = (metrics.byType[type] || 0) + 1

    if (resource.duration > 1000) {
      metrics.slowResources.push({
        name: resource.name,
        duration: resource.duration
      })
    }

    if (resource.transferSize) {
      metrics.totalSize += resource.transferSize
    }
  })

  return metrics
}

// 性能监控类
class PerformanceMonitor {
  private metrics: PerformanceMetrics[] = []
  private maxMetrics = 100

  record() {
    const metrics = getPagePerformanceMetrics()
    this.metrics.push(metrics)

    if (this.metrics.length > this.maxMetrics) {
      this.metrics.shift()
    }
  }

  getMetrics(): PerformanceMetrics[] {
    return [...this.metrics]
  }

  getAverageFPS(): number {
    if (this.metrics.length === 0) return 0
    const sum = this.metrics.reduce((acc, m) => acc + m.fps, 0)
    return sum / this.metrics.length
  }

  getAverageMemory(): number {
    if (this.metrics.length === 0) return 0
    const sum = this.metrics.reduce((acc, m) => acc + m.memory, 0)
    return sum / this.metrics.length
  }

  clear() {
    this.metrics = []
  }
}

export const performanceMonitor = new PerformanceMonitor()

// Vue性能监控Hook
import { ref, onMounted, onUnmounted } from 'vue'

export function usePerformanceMonitor(interval = 1000) {
  const metrics = ref<PerformanceMetrics | null>(null)
  const averageFPS = ref(0)
  const averageMemory = ref(0)
  let timer: NodeJS.Timeout | null = null

  const updateMetrics = () => {
    const currentMetrics = getPagePerformanceMetrics()
    metrics.value = currentMetrics
    performanceMonitor.record()
    averageFPS.value = performanceMonitor.getAverageFPS()
    averageMemory.value = performanceMonitor.getAverageMemory()
  }

  onMounted(() => {
    updateMetrics()
    timer = setInterval(updateMetrics, interval)
  })

  onUnmounted(() => {
    if (timer) {
      clearInterval(timer)
    }
  })

  return {
    metrics,
    averageFPS,
    averageMemory,
    resourceMetrics: getResourceMetrics()
  }
}

// 防抖函数
export function debounce<T extends (...args: any[]) => any>(
  func: T,
  wait: number
): (...args: Parameters<T>) => void {
  let timeout: NodeJS.Timeout | null = null

  return function (this: any, ...args: Parameters<T>) {
    if (timeout) {
      clearTimeout(timeout)
    }

    timeout = setTimeout(() => {
      func.apply(this, args)
    }, wait)
  }
}

// 节流函数
export function throttle<T extends (...args: any[]) => any>(
  func: T,
  wait: number
): (...args: Parameters<T>) => void {
  let inThrottle = false

  return function (this: any, ...args: Parameters<T>) {
    if (!inThrottle) {
      func.apply(this, args)
      inThrottle = true

      setTimeout(() => {
        inThrottle = false
      }, wait)
    }
  }
}
