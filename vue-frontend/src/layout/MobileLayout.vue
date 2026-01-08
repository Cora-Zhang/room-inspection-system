<template>
  <div class="mobile-layout">
    <!-- 顶部导航栏 -->
    <div class="mobile-header">
      <div class="header-left">
        <el-icon v-if="showBack" @click="goBack" class="back-icon">
          <ArrowLeft />
        </el-icon>
        <span class="header-title">{{ title }}</span>
      </div>
      <div class="header-right">
        <el-icon @click="toggleMenu" class="menu-icon">
          <Menu />
        </el-icon>
      </div>
    </div>

    <!-- 侧边菜单 -->
    <el-drawer v-model="drawerVisible" direction="rtl" size="70%">
      <div class="mobile-menu">
        <div class="user-info">
          <el-avatar :size="60" src="/default-avatar.png" />
          <div class="user-name">{{ userName }}</div>
          <div class="user-role">{{ userRole }}</div>
        </div>
        <el-menu
          :default-active="activeMenu"
          @select="handleMenuSelect"
          class="menu-list"
        >
          <el-menu-item index="/mobile/dashboard">
            <el-icon><Odometer /></el-icon>
            <span>首页</span>
          </el-menu-item>
          <el-menu-item index="/mobile/alarm">
            <el-icon><Bell /></el-icon>
            <span>告警</span>
            <el-badge v-if="alarmCount > 0" :value="alarmCount" class="menu-badge" />
          </el-menu-item>
          <el-menu-item index="/mobile/workorder">
            <el-icon><Document /></el-icon>
            <span>工单</span>
          </el-menu-item>
          <el-menu-item index="/mobile/device">
            <el-icon><Monitor /></el-icon>
            <span>设备</span>
          </el-menu-item>
          <el-menu-item index="/mobile/inspection">
            <el-icon><View /></el-icon>
            <span>巡检</span>
          </el-menu-item>
          <el-menu-item index="/mobile/profile">
            <el-icon><User /></el-icon>
            <span>我的</span>
          </el-menu-item>
        </el-menu>
      </div>
    </el-drawer>

    <!-- 主内容区 -->
    <div class="mobile-content">
      <router-view />
    </div>

    <!-- 底部标签栏 -->
    <div class="mobile-tabbar">
      <div
        v-for="tab in tabs"
        :key="tab.path"
        class="tab-item"
        :class="{ active: activeTab === tab.path }"
        @click="handleTabClick(tab.path)"
      >
        <el-icon class="tab-icon">
          <component :is="tab.icon" />
        </el-icon>
        <span class="tab-label">{{ tab.label }}</span>
        <el-badge v-if="tab.badge > 0" :value="tab.badge" class="tab-badge" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowLeft, Menu, Odometer, Bell, Document, Monitor, View, User, House, Setting } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const title = ref('机房巡检')
const drawerVisible = ref(false)
const activeMenu = ref('')
const activeTab = ref('/mobile/dashboard')
const showBack = computed(() => route.meta.showBack)

const userName = ref('管理员')
const userRole = ref('运维人员')
const alarmCount = ref(5)

const tabs = ref([
  { path: '/mobile/dashboard', label: '首页', icon: Odometer, badge: 0 },
  { path: '/mobile/alarm', label: '告警', icon: Bell, badge: 5 },
  { path: '/mobile/workorder', label: '工单', icon: Document, badge: 0 },
  { path: '/mobile/profile', label: '我的', icon: User, badge: 0 }
])

const toggleMenu = () => {
  drawerVisible.value = !drawerVisible.value
}

const goBack = () => {
  router.back()
}

const handleMenuSelect = (index: string) => {
  drawerVisible.value = false
  router.push(index)
}

const handleTabClick = (path: string) => {
  activeTab.value = path
  router.push(path)
}

// 监听路由变化
watch(() => route.path, (newPath) => {
  activeMenu.value = newPath
  activeTab.value = newPath
})
</script>

<style scoped lang="scss">
.mobile-layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f5f5;
}

.mobile-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;

    .back-icon {
      font-size: 20px;
      cursor: pointer;
    }

    .header-title {
      font-size: 18px;
      font-weight: 600;
    }
  }

  .header-right {
    .menu-icon {
      font-size: 20px;
      cursor: pointer;
    }
  }
}

.mobile-menu {
  .user-info {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 40px 20px 20px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;

    .user-name {
      margin-top: 12px;
      font-size: 18px;
      font-weight: 600;
    }

    .user-role {
      margin-top: 4px;
      font-size: 14px;
      opacity: 0.9;
    }
  }

  .menu-list {
    border: none;

    .menu-badge {
      margin-left: auto;
    }
  }
}

.mobile-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.mobile-tabbar {
  display: flex;
  align-items: center;
  padding: 8px 0;
  background: #fff;
  border-top: 1px solid #e4e7ed;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.05);

  .tab-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 4px;
    color: #909399;
    cursor: pointer;
    position: relative;

    &.active {
      color: #667eea;
    }

    .tab-icon {
      font-size: 24px;
    }

    .tab-label {
      font-size: 12px;
    }

    .tab-badge {
      position: absolute;
      top: 4px;
      right: 30%;
    }
  }
}
</style>
