<template>
  <div class="virtual-scroll-container" :style="{ height: containerHeight }" ref="containerRef">
    <div
      class="virtual-scroll-content"
      :style="{
        height: `${totalHeight}px`,
        transform: `translateY(${offsetY}px)`
      }"
    >
      <div
        v-for="item in visibleItems"
        :key="getItemKey(item)"
        class="virtual-scroll-item"
        :style="{ height: `${itemHeight}px` }"
      >
        <slot :item="item" :index="item._index"></slot>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'

interface Props {
  items: any[]
  itemHeight: number
  containerHeight?: string
  buffer?: number
  getItemKey?: (item: any) => string | number
}

const props = withDefaults(defineProps<Props>(), {
  containerHeight: '100%',
  buffer: 5,
  getItemKey: (item: any) => item.id || item._index
})

const containerRef = ref<HTMLElement>()
const scrollTop = ref(0)

// 计算总高度
const totalHeight = computed(() => props.items.length * props.itemHeight)

// 计算可视区域内的项目
const visibleItems = computed(() => {
  const startIndex = Math.max(
    0,
    Math.floor(scrollTop.value / props.itemHeight) - props.buffer
  )
  const endIndex = Math.min(
    props.items.length - 1,
    startIndex + Math.ceil(parseFloat(props.containerHeight) / props.itemHeight) + props.buffer * 2
  )

  const items = []
  for (let i = startIndex; i <= endIndex; i++) {
    items.push({
      ...props.items[i],
      _index: i
    })
  }

  return items
})

// 计算偏移量
const offsetY = computed(() => {
  const startIndex = Math.max(
    0,
    Math.floor(scrollTop.value / props.itemHeight) - props.buffer
  )
  return startIndex * props.itemHeight
})

// 滚动事件处理
const handleScroll = () => {
  if (containerRef.value) {
    scrollTop.value = containerRef.value.scrollTop
  }
}

onMounted(() => {
  if (containerRef.value) {
    containerRef.value.addEventListener('scroll', handleScroll)
  }
})

onUnmounted(() => {
  if (containerRef.value) {
    containerRef.value.removeEventListener('scroll', handleScroll)
  }
})

// 暴露方法
defineExpose({
  scrollToTop: () => {
    if (containerRef.value) {
      containerRef.value.scrollTop = 0
      scrollTop.value = 0
    }
  }
})
</script>

<style scoped>
.virtual-scroll-container {
  overflow-y: auto;
  overflow-x: hidden;
  position: relative;
}

.virtual-scroll-content {
  position: relative;
  width: 100%;
}

.virtual-scroll-item {
  position: absolute;
  left: 0;
  right: 0;
  box-sizing: border-box;
}

/* 滚动条样式优化 */
.virtual-scroll-container::-webkit-scrollbar {
  width: 6px;
}

.virtual-scroll-container::-webkit-scrollbar-track {
  background: #1a1a2e;
}

.virtual-scroll-container::-webkit-scrollbar-thumb {
  background: #4a4a6a;
  border-radius: 3px;
}

.virtual-scroll-container::-webkit-scrollbar-thumb:hover {
  background: #5a5a8a;
}
</style>
