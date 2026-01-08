<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h1>机房巡检系统</h1>
        <p>Room Inspection System</p>
      </div>
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>
        <el-form-item>
          <el-button
            size="large"
            class="sso-btn"
            :loading="ssoLoading"
            @click="handleSsoLogin"
          >
            <span>SSO单点登录</span>
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getSsoLoginUrl } from '@/api/auth'

const router = useRouter()
const route = useRoute()
const loginFormRef = ref<FormInstance>()
const loading = ref(false)
const ssoLoading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const loginRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate((valid) => {
    if (valid) {
      loading.value = true

      // 模拟登录请求
      setTimeout(() => {
        localStorage.setItem('token', 'mock-token-' + Date.now())
        loading.value = false
        ElMessage.success('登录成功')
        router.push('/')
      }, 1000)
    }
  })
}

// SSO单点登录
const handleSsoLogin = async () => {
  ssoLoading.value = true
  try {
    const state = btoa(JSON.stringify({
      redirect: route.query.redirect || '/',
      timestamp: Date.now()
    }))

    const res = await getSsoLoginUrl(undefined, state)
    if (res.data) {
      // 跳转到IAM认证服务器
      window.location.href = res.data
    }
  } catch (error: any) {
    ElMessage.error(error.message || '获取SSO登录URL失败')
  } finally {
    ssoLoading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-container {
  width: 100%;
  height: 100vh;
  background: linear-gradient(135deg, #0a0e27 0%, #1a1f3a 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background:
      radial-gradient(circle at 20% 50%, rgba(0, 242, 255, 0.1) 0%, transparent 50%),
      radial-gradient(circle at 80% 50%, rgba(123, 47, 247, 0.1) 0%, transparent 50%);
    animation: pulse 8s ease-in-out infinite;
  }
}

.login-box {
  position: relative;
  width: 400px;
  padding: 60px 40px;
  background: rgba(26, 31, 58, 0.9);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);

  .login-header {
    text-align: center;
    margin-bottom: 40px;

    h1 {
      color: #00f2ff;
      font-size: 32px;
      font-weight: 600;
      text-shadow: 0 0 20px rgba(0, 242, 255, 0.5);
      margin-bottom: 8px;
    }

    p {
      color: #a0a0c0;
      font-size: 14px;
      letter-spacing: 2px;
    }
  }

  .login-form {
    :deep(.el-form-item) {
      margin-bottom: 24px;
    }

    :deep(.el-input) {
      .el-input__wrapper {
        background-color: rgba(255, 255, 255, 0.05);
        border: 1px solid rgba(255, 255, 255, 0.1);
        box-shadow: none;

        &:hover {
          border-color: rgba(0, 242, 255, 0.3);
        }

        &.is-focus {
          border-color: #00f2ff;
          box-shadow: 0 0 10px rgba(0, 242, 255, 0.3);
        }

        .el-input__inner {
          color: #ffffff;
          &::placeholder {
            color: #606080;
          }
        }
      }
    }

    .login-btn {
      width: 100%;
      background: linear-gradient(135deg, #00f2ff 0%, #7b2ff7 100%);
      border: none;
      color: #ffffff;
      font-size: 16px;
      font-weight: 500;
      height: 44px;
      border-radius: 6px;
      box-shadow: 0 4px 15px rgba(0, 242, 255, 0.3);
      transition: all 0.3s;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba(0, 242, 255, 0.5);
      }

      &:active {
        transform: translateY(0);
      }
    }

    .sso-btn {
      width: 100%;
      background: transparent;
      border: 2px solid #7b2ff7;
      color: #7b2ff7;
      font-size: 16px;
      font-weight: 500;
      height: 44px;
      border-radius: 6px;
      transition: all 0.3s;

      &:hover {
        background: rgba(123, 47, 247, 0.1);
        border-color: #9b4ff7;
        color: #9b4ff7;
      }

      &:active {
        transform: scale(0.98);
      }
    }
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}
</style>
