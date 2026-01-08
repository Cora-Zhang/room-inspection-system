import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: {
      title: '登录',
      requiresAuth: false
    }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard',
    meta: {
      requiresAuth: true
    },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: {
          title: '首页',
          icon: 'DataAnalysis',
          requiresAuth: true
        }
      },
      {
        path: 'room',
        name: 'Room',
        component: () => import('@/views/room/index.vue'),
        meta: {
          title: '机房管理',
          icon: 'OfficeBuilding',
          requiresAuth: true
        }
      },
      {
        path: 'device',
        name: 'Device',
        component: () => import('@/views/device/index.vue'),
        meta: {
          title: '设备管理',
          icon: 'Monitor',
          requiresAuth: true
        }
      },
      {
        path: 'inspection',
        name: 'Inspection',
        component: () => import('@/views/inspection/index.vue'),
        meta: {
          title: '巡检管理',
          icon: 'DocumentChecked',
          requiresAuth: true
        },
        children: [
          {
            path: 'room-inspection',
            name: 'RoomInspection',
            component: () => import('@/views/inspection/index.vue'),
            meta: {
              title: '机房巡检',
              requiresAuth: true
            }
          },
          {
            path: 'device-inspection',
            name: 'DeviceInspection',
            component: () => import('@/views/DeviceInspection.vue'),
            meta: {
              title: '网络设备巡检',
              requiresAuth: true
            }
          },
          {
            path: 'verification',
            name: 'InspectionVerification',
            component: () => import('@/views/InspectionVerification.vue'),
            meta: {
              title: '巡检核验',
              requiresAuth: true
            }
          }
        ]
      },
      {
        path: 'schedule',
        name: 'Schedule',
        meta: {
          title: '值班管理',
          icon: 'User',
          requiresAuth: true
        },
        children: [
          {
            path: 'staff',
            name: 'ScheduleStaff',
            component: () => import('@/views/schedule/staff.vue'),
            meta: {
              title: '值班人员',
              requiresAuth: true
            }
          },
          {
            path: 'roster',
            name: 'ScheduleRoster',
            component: () => import('@/views/ShiftSchedule.vue'),
            meta: {
              title: '值班排班表',
              requiresAuth: true
            }
          },
          {
            path: 'handover',
            name: 'ScheduleHandover',
            component: () => import('@/views/schedule/handover.vue'),
            meta: {
              title: '值班交接',
              requiresAuth: true
            }
          },
          {
            path: 'door-access',
            name: 'DoorAccess',
            component: () => import('@/views/DoorAccess.vue'),
            meta: {
              title: '门禁权限',
              requiresAuth: true
            }
          }
        ]
      },
      {
        path: 'power-monitoring',
        name: 'PowerMonitoring',
        component: () => import('@/views/PowerMonitoring.vue'),
        meta: {
          title: '电力监控',
          icon: 'Lightning',
          requiresAuth: true
        }
      },
      {
        path: 'environment-monitoring',
        name: 'EnvironmentMonitoring',
        component: () => import('@/views/EnvironmentMonitoring.vue'),
        meta: {
          title: '环境监控',
          icon: 'Monitor',
          requiresAuth: true
        }
      },
      {
        path: 'energy-efficiency',
        name: 'EnergyEfficiency',
        component: () => import('@/views/EnergyEfficiency.vue'),
        meta: {
          title: '能效优化',
          icon: 'TrendCharts',
          requiresAuth: true
        }
      },
      {
        path: 'air-conditioner-monitoring',
        name: 'AirConditionerMonitoring',
        component: () => import('@/views/AirConditionerMonitoring.vue'),
        meta: {
          title: '精密空调监控',
          icon: 'WindPower',
          requiresAuth: true
        }
      },
      {
        path: 'fire-protection-monitoring',
        name: 'FireProtectionMonitoring',
        component: () => import('@/views/FireProtectionMonitoring.vue'),
        meta: {
          title: '消防保障系统',
          icon: 'Shield',
          requiresAuth: true
        }
      },
      {
        path: 'comprehensive-dashboard',
        name: 'ComprehensiveDashboard',
        component: () => import('@/views/ComprehensiveDashboard.vue'),
        meta: {
          title: '综合巡检看板',
          icon: 'Monitor',
          requiresAuth: true
        }
      },
      {
        path: 'work-order-management',
        name: 'WorkOrderManagement',
        component: () => import('@/views/WorkOrderManagement.vue'),
        meta: {
          title: '工单与任务管理',
          icon: 'Tickets',
          requiresAuth: true
        }
      },
      {
        path: 'system',
        name: 'System',
        meta: {
          title: '基础配置',
          icon: 'Setting',
          requiresAuth: true
        },
        children: [
          {
            path: 'user',
            name: 'SystemUser',
            component: () => import('@/views/system/user.vue'),
            meta: {
              title: '用户管理',
              requiresAuth: true
            }
          },
          {
            path: 'role',
            name: 'SystemRole',
            component: () => import('@/views/system/role.vue'),
            meta: {
              title: '角色管理',
              requiresAuth: true
            }
          },
          {
            path: 'permission',
            name: 'SystemPermission',
            component: () => import('@/views/system/permission.vue'),
            meta: {
              title: '权限管理',
              requiresAuth: true
            }
          },
          {
            path: 'dictionary',
            name: 'SystemDictionary',
            component: () => import('@/views/system/dictionary.vue'),
            meta: {
              title: '数据字典',
              requiresAuth: true
            }
          },
          {
            path: 'sso',
            name: 'SystemSSO',
            component: () => import('@/views/system/sso.vue'),
            meta: {
              title: 'SSO配置',
              requiresAuth: true
            }
          },
          {
            path: 'api-config',
            name: 'SystemApiConfig',
            component: () => import('@/views/ApiConfig.vue'),
            meta: {
              title: '接口管理',
              icon: 'Connection',
              requiresAuth: true
            }
          }
        ]
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  const token = authStore.token

  if (to.meta.requiresAuth && !token) {
    // 需要认证但未登录，跳转到登录页
    next({
      path: '/login',
      query: { redirect: to.fullPath }
    })
  } else if (to.path === '/login' && token) {
    // 已登录访问登录页，跳转到首页
    next({ path: '/' })
  } else {
    next()
  }
})

export default router
