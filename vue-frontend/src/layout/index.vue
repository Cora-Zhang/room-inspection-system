<template>
  <div class="layout-container">
    <el-container>
      <!-- 左侧菜单 -->
      <el-aside width="240px">
        <div class="logo">
          <h2>机房巡检系统</h2>
        </div>
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          background-color="#1a1f3a"
          text-color="#ffffff"
          active-text-color="#00f2ff"
          router
        >
          <template v-for="route in menuRoutes" :key="route.path">
            <el-sub-menu v-if="route.children && route.children.length > 0" :index="route.path">
              <template #title>
                <el-icon><component :is="route.meta.icon" /></el-icon>
                <span>{{ route.meta.title }}</span>
              </template>
              <el-menu-item
                v-for="child in route.children"
                :key="child.path"
                :index="child.path"
              >
                <el-icon><component :is="child.meta.icon" /></el-icon>
                <span>{{ child.meta.title }}</span>
              </el-menu-item>
            </el-sub-menu>
            <el-menu-item v-else :index="route.path">
              <el-icon><component :is="route.meta.icon" /></el-icon>
              <span>{{ route.meta.title }}</span>
            </el-menu-item>
          </template>
        </el-menu>
      </el-aside>

      <!-- 主内容区 -->
      <el-container>
        <el-header>
          <div class="header-left">
            <el-icon
              class="collapse-btn"
              @click="toggleCollapse"
            >
              <Fold v-if="!isCollapse" />
              <Expand v-else />
            </el-icon>
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item v-if="currentRoute.meta.title">
                {{ currentRoute.meta.title }}
              </el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="header-right">
            <el-dropdown @command="handleCommand">
              <span class="user-info">
                <el-avatar :size="32" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
                <span class="username">管理员</span>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Fold, Expand } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const isCollapse = ref(false)

const activeMenu = computed(() => route.path)

const currentRoute = computed(() => route)

const menuRoutes = computed(() => {
  const routes = router.options.routes
  return routes.find(r => r.path === '/')?.children || []
})

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const handleCommand = (command: string) => {
  if (command === 'logout') {
    // 退出登录
    localStorage.removeItem('token')
    router.push('/login')
    ElMessage.success('退出成功')
  } else if (command === 'profile') {
    ElMessage.info('个人中心功能开发中')
  }
}
</script>

<style scoped lang="scss">
.layout-container {
  width: 100%;
  height: 100vh;
  background-color: #0a0e27;
}

.el-container {
  height: 100%;
}

.el-aside {
  background-color: #1a1f3a;
  border-right: 1px solid #3a4570;
  overflow-x: hidden;

  .logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-bottom: 1px solid #3a4570;

    h2 {
      color: #00f2ff;
      font-size: 18px;
      font-weight: 600;
      text-shadow: 0 0 10px rgba(0, 242, 255, 0.5);
    }
  }

  .el-menu {
    border-right: none;
  }
}

.el-header {
  background-color: #1a1f3a;
  border-bottom: 1px solid #3a4570;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;

  .header-left {
    display: flex;
    align-items: center;
    gap: 20px;

    .collapse-btn {
      font-size: 20px;
      cursor: pointer;
      color: #ffffff;
      transition: all 0.3s;

      &:hover {
        color: #00f2ff;
      }
    }

    :deep(.el-breadcrumb) {
      .el-breadcrumb__inner {
        color: #a0a0c0;

        &.is-link {
          &:hover {
            color: #00f2ff;
          }
        }
      }

      .el-breadcrumb__separator {
        color: #606080;
      }
    }
  }

  .header-right {
    .user-info {
      display: flex;
      align-items: center;
      gap: 10px;
      cursor: pointer;
      padding: 5px 10px;
      border-radius: 4px;
      transition: all 0.3s;

      &:hover {
        background-color: rgba(255, 255, 255, 0.1);
      }

      .username {
        color: #ffffff;
        font-size: 14px;
      }
    }
  }
}

.el-main {
  background-color: #0a0e27;
  padding: 20px;
  overflow-y: auto;
}
</style>
