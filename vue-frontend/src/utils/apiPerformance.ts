import { ref } from 'vue'

// 简单的内存缓存
class ApiCache {
  private cache = new Map<string, { data: any; timestamp: number; ttl: number }>()

  set(key: string, data: any, ttl: number = 300000) {
    // 默认缓存5分钟
    this.cache.set(key, {
      data,
      timestamp: Date.now(),
      ttl
    })
  }

  get(key: string): any | null {
    const item = this.cache.get(key)

    if (!item) {
      return null
    }

    if (Date.now() - item.timestamp > item.ttl) {
      this.cache.delete(key)
      return null
    }

    return item.data
  }

  has(key: string): boolean {
    return this.get(key) !== null
  }

  clear() {
    this.cache.clear()
  }

  clearPrefix(prefix: string) {
    for (const key of this.cache.keys()) {
      if (key.startsWith(prefix)) {
        this.cache.delete(key)
      }
    }
  }
}

export const apiCache = new ApiCache()

// 带缓存的API请求Hook
export function useCachedRequest<T = any>(
  key: string,
  fetcher: () => Promise<T>,
  ttl: number = 300000
) {
  const data = ref<T | null>(null)
  const loading = ref(false)
  const error = ref<Error | null>(null)

  const fetch = async (forceRefresh = false) => {
    // 如果不强制刷新且缓存存在，直接返回缓存数据
    if (!forceRefresh && apiCache.has(key)) {
      data.value = apiCache.get(key)
      return data.value
    }

    loading.value = true
    error.value = null

    try {
      const result = await fetcher()
      data.value = result
      apiCache.set(key, result, ttl)
      return result
    } catch (err) {
      error.value = err as Error
      throw err
    } finally {
      loading.value = false
    }
  }

  return {
    data,
    loading,
    error,
    fetch,
    clear: () => {
      apiCache.set(key, null, 0)
      data.value = null
    }
  }
}

// 请求取消Hook
export function useAbortController() {
  const controller = ref<AbortController | null>(null)

  const createController = () => {
    if (controller.value) {
      controller.value.abort()
    }
    controller.value = new AbortController()
    return controller.value
  }

  const abort = () => {
    if (controller.value) {
      controller.value.abort()
      controller.value = null
    }
  }

  return {
    controller,
    createController,
    abort
  }
}

// 并发请求限制器
class RequestLimiter {
  private pendingRequests = new Map<string, AbortController>()
  private maxConcurrent = 5
  private currentConcurrent = 0

  async execute<T>(key: string, fn: (signal: AbortSignal) => Promise<T>): Promise<T> {
    // 如果已有相同请求，返回该请求
    if (this.pendingRequests.has(key)) {
      const controller = this.pendingRequests.get(key)!
      return new Promise((resolve, reject) => {
        const originalThen = (controller as any).promise?.then
        if (originalThen) {
          originalThen.call((controller as any).promise, resolve, reject)
        }
      })
    }

    // 等待直到有可用槽位
    while (this.currentConcurrent >= this.maxConcurrent) {
      await new Promise(resolve => setTimeout(resolve, 100))
    }

    // 创建新请求
    const controller = new AbortController()
    this.pendingRequests.set(key, controller)
    this.currentConcurrent++

    try {
      const result = await fn(controller.signal)
      return result
    } finally {
      this.pendingRequests.delete(key)
      this.currentConcurrent--
    }
  }

  cancel(key: string) {
    const controller = this.pendingRequests.get(key)
    if (controller) {
      controller.abort()
      this.pendingRequests.delete(key)
      this.currentConcurrent--
    }
  }

  cancelAll() {
    this.pendingRequests.forEach((controller) => {
      controller.abort()
    })
    this.pendingRequests.clear()
    this.currentConcurrent = 0
  }
}

export const requestLimiter = new RequestLimiter()

// 请求重试Hook
export async function useRetry<T>(
  fn: () => Promise<T>,
  maxRetries: number = 3,
  delay: number = 1000
): Promise<T> {
  let lastError: Error

  for (let i = 0; i <= maxRetries; i++) {
    try {
      return await fn()
    } catch (error) {
      lastError = error as Error

      if (i < maxRetries) {
        console.warn(`请求失败，第${i + 1}次重试:`, error)
        await new Promise(resolve => setTimeout(resolve, delay * (i + 1)))
      }
    }
  }

  throw lastError!
}

// 分页请求Hook
export function usePaginationRequest<T = any>(
  fetcher: (page: number, pageSize: number) => Promise<{ data: T[]; total: number }>
) {
  const data = ref<T[]>([])
  const total = ref(0)
  const page = ref(1)
  const pageSize = ref(20)
  const loading = ref(false)
  const error = ref<Error | null>(null)

  const fetch = async () => {
    loading.value = true
    error.value = null

    try {
      const result = await fetcher(page.value, pageSize.value)
      data.value = result.data
      total.value = result.total
    } catch (err) {
      error.value = err as Error
    } finally {
      loading.value = false
    }
  }

  const nextPage = () => {
    if (page.value * pageSize.value < total.value) {
      page.value++
      fetch()
    }
  }

  const prevPage = () => {
    if (page.value > 1) {
      page.value--
      fetch()
    }
  }

  const goToPage = (p: number) => {
    page.value = p
    fetch()
  }

  const refresh = () => {
    fetch()
  }

  return {
    data,
    total,
    page,
    pageSize,
    loading,
    error,
    fetch,
    nextPage,
    prevPage,
    goToPage,
    refresh
  }
}

// 批量请求Hook
export async function useBatchRequest<T = any>(
  items: any[],
  fetcher: (item: any) => Promise<T>,
  batchSize: number = 10,
  delay: number = 100
): Promise<(T | Error)[]> {
  const results: (T | Error)[] = []

  for (let i = 0; i < items.length; i += batchSize) {
    const batch = items.slice(i, i + batchSize)

    const batchResults = await Promise.allSettled(
      batch.map(item => fetcher(item))
    )

    batchResults.forEach(result => {
      if (result.status === 'fulfilled') {
        results.push(result.value)
      } else {
        results.push(result.reason)
      }
    })

    // 延迟以避免服务器过载
    if (i + batchSize < items.length) {
      await new Promise(resolve => setTimeout(resolve, delay))
    }
  }

  return results
}
