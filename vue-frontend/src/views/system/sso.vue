<template>
  <div class="sso-container">
    <el-card class="sso-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>SSO配置管理</span>
          <el-button type="primary" @click="openConfigDialog()">新增配置</el-button>
        </div>
      </template>

      <el-table
        :data="ssoConfigList"
        style="width: 100%"
        v-loading="loading"
        :header-cell-style="{ background: '#1a1a2e', color: '#fff' }"
        :cell-style="{ background: '#16162a', color: '#fff' }"
      >
        <el-table-column prop="configName" label="配置名称" width="180" />
        <el-table-column prop="protocolType" label="协议类型" width="120">
          <template #default="{ row }">
            <el-tag :type="row.protocolType === 'OAUTH2' ? 'success' : 'warning'">
              {{ row.protocolType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="authServerUrl" label="认证服务器地址" min-width="200" />
        <el-table-column prop="enabled" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.enabled === 'Y' ? 'success' : 'info'">
              {{ row.enabled === 'Y' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.enabled !== 'Y'"
              type="success"
              size="small"
              @click="handleEnable(row)"
            >
              启用
            </el-button>
            <el-button
              v-if="row.enabled === 'Y'"
              type="warning"
              size="small"
              @click="handleDisable(row)"
            >
              禁用
            </el-button>
            <el-button type="primary" size="small" @click="openConfigDialog(row)">
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- SSO配置对话框 -->
    <el-dialog
      v-model="configDialogVisible"
      :title="isEdit ? '编辑SSO配置' : '新增SSO配置'"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="configFormRef"
        :model="configForm"
        :rules="configFormRules"
        label-width="180px"
        class="config-form"
      >
        <el-form-item label="配置名称" prop="configName">
          <el-input v-model="configForm.configName" placeholder="请输入配置名称" />
        </el-form-item>

        <el-form-item label="协议类型" prop="protocolType">
          <el-radio-group v-model="configForm.protocolType">
            <el-radio label="OAUTH2">OAuth2.0</el-radio>
            <el-radio label="SAML2">SAML2.0</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="认证服务器地址" prop="authServerUrl">
          <el-input
            v-model="configForm.authServerUrl"
            placeholder="例如：https://iam.example.com"
          />
        </el-form-item>

        <el-form-item label="客户端ID" prop="clientId">
          <el-input v-model="configForm.clientId" placeholder="请输入客户端ID" />
        </el-form-item>

        <el-form-item label="客户端密钥" prop="clientSecret">
          <el-input
            v-model="configForm.clientSecret"
            type="password"
            placeholder="请输入客户端密钥"
            show-password
          />
        </el-form-item>

        <el-form-item label="授权端点" prop="authorizeEndpoint">
          <el-input
            v-model="configForm.authorizeEndpoint"
            placeholder="例如：/esc-sso/oauth2.0/authorize"
          />
        </el-form-item>

        <el-form-item label="Token端点" prop="tokenEndpoint">
          <el-input
            v-model="configForm.tokenEndpoint"
            placeholder="例如：/env-201/open-apiportal/iamopen/oauth/accessToken"
          />
        </el-form-item>

        <el-form-item label="用户信息端点" prop="userInfoEndpoint">
          <el-input
            v-model="configForm.userInfoEndpoint"
            placeholder="例如：/env-201/open-apiportal/iamopen/oauth/profile"
          />
        </el-form-item>

        <el-form-item label="退出端点" prop="logoutEndpoint">
          <el-input
            v-model="configForm.logoutEndpoint"
            placeholder="例如：/esc-sso/logout"
          />
        </el-form-item>

        <el-form-item label="回调地址" prop="redirectUri">
          <el-input
            v-model="configForm.redirectUri"
            placeholder="例如：http://localhost:5000/api/sso/callback"
          />
        </el-form-item>

        <el-form-item label="应用入口地址" prop="entryUrl">
          <el-input
            v-model="configForm.entryUrl"
            placeholder="应用入口地址"
          />
        </el-form-item>

        <el-form-item label="Token有效期" prop="tokenExpireTime">
          <el-input-number
            v-model="configForm.tokenExpireTime"
            :min="60"
            :max="86400"
            :step="60"
          />
          <span class="unit-text">秒</span>
        </el-form-item>

        <el-form-item label="会话保持端点" prop="sessionKeepEndpoint">
          <el-input
            v-model="configForm.sessionKeepEndpoint"
            placeholder="可选，例如：/esc-sso/api/v1/loginLog/checkAccessToken"
          />
        </el-form-item>

        <el-form-item label="JWT Secret" prop="jwtSecret">
          <el-input
            v-model="configForm.jwtSecret"
            type="password"
            placeholder="用于数据同步接口鉴权"
            show-password
          />
        </el-form-item>

        <el-form-item label="时间偏差" prop="jwtTimeOffset">
          <el-input-number
            v-model="configForm.jwtTimeOffset"
            :min="1"
            :max="300"
            :step="1"
          />
          <span class="unit-text">秒</span>
        </el-form-item>

        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="configForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="configDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveConfig">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { getSsoLoginUrl } from '@/api/auth'

// 响应式数据
const loading = ref(false)
const ssoConfigList = ref<any[]>([])
const configDialogVisible = ref(false)
const isEdit = ref(false)
const configFormRef = ref<FormInstance>()

// 表单数据
const configForm = ref({
  id: '',
  configName: '',
  enabled: 'N',
  protocolType: 'OAUTH2',
  authServerUrl: '',
  clientId: '',
  clientSecret: '',
  authorizeEndpoint: '',
  tokenEndpoint: '',
  userInfoEndpoint: '',
  logoutEndpoint: '',
  redirectUri: '',
  entryUrl: '',
  tokenExpireTime: 86400,
  sessionKeepEndpoint: '',
  jwtSecret: '',
  jwtTimeOffset: 60,
  remark: ''
})

// 表单验证规则
const configFormRules: FormRules = {
  configName: [{ required: true, message: '请输入配置名称', trigger: 'blur' }],
  protocolType: [{ required: true, message: '请选择协议类型', trigger: 'change' }],
  authServerUrl: [{ required: true, message: '请输入认证服务器地址', trigger: 'blur' }],
  clientId: [{ required: true, message: '请输入客户端ID', trigger: 'blur' }],
  clientSecret: [{ required: true, message: '请输入客户端密钥', trigger: 'blur' }],
  authorizeEndpoint: [{ required: true, message: '请输入授权端点', trigger: 'blur' }],
  tokenEndpoint: [{ required: true, message: '请输入Token端点', trigger: 'blur' }],
  userInfoEndpoint: [{ required: true, message: '请输入用户信息端点', trigger: 'blur' }],
  logoutEndpoint: [{ required: true, message: '请输入退出端点', trigger: 'blur' }],
  redirectUri: [{ required: true, message: '请输入回调地址', trigger: 'blur' }]
}

// 加载SSO配置列表
const loadSsoConfigList = async () => {
  loading.value = true
  try {
    // 这里应该调用后端API获取SSO配置列表
    // 暂时使用模拟数据
    ssoConfigList.value = []
    ElMessage.success('加载成功')
  } catch (error) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

// 打开配置对话框
const openConfigDialog = (row?: any) => {
  if (row) {
    isEdit.value = true
    configForm.value = { ...row }
  } else {
    isEdit.value = false
    configForm.value = {
      id: '',
      configName: '',
      enabled: 'N',
      protocolType: 'OAUTH2',
      authServerUrl: '',
      clientId: '',
      clientSecret: '',
      authorizeEndpoint: '',
      tokenEndpoint: '',
      userInfoEndpoint: '',
      logoutEndpoint: '',
      redirectUri: '',
      entryUrl: '',
      tokenExpireTime: 86400,
      sessionKeepEndpoint: '',
      jwtSecret: '',
      jwtTimeOffset: 60,
      remark: ''
    }
  }
  configDialogVisible.value = true
}

// 保存配置
const handleSaveConfig = async () => {
  if (!configFormRef.value) return

  try {
    await configFormRef.value.validate()

    // 这里应该调用后端API保存配置
    ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
    configDialogVisible.value = false
    loadSsoConfigList()
  } catch (error: any) {
    if (error !== false) {
      ElMessage.error('保存失败')
    }
  }
}

// 启用配置
const handleEnable = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定启用此SSO配置吗？', '提示', {
      type: 'warning'
    })

    // 这里应该调用后端API启用配置
    ElMessage.success('启用成功')
    loadSsoConfigList()
  } catch (error) {
    // 用户取消
  }
}

// 禁用配置
const handleDisable = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定禁用此SSO配置吗？', '提示', {
      type: 'warning'
    })

    // 这里应该调用后端API禁用配置
    ElMessage.success('禁用成功')
    loadSsoConfigList()
  } catch (error) {
    // 用户取消
  }
}

// 删除配置
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定删除此SSO配置吗？', '提示', {
      type: 'warning'
    })

    // 这里应该调用后端API删除配置
    ElMessage.success('删除成功')
    loadSsoConfigList()
  } catch (error) {
    // 用户取消
  }
}

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}

// 页面加载时获取配置列表
onMounted(() => {
  loadSsoConfigList()
})
</script>

<style scoped lang="scss">
.sso-container {
  padding: 20px;

  .sso-card {
    background: #1a1a2e;
    border: 1px solid #2a2a4a;

    :deep(.el-card__header) {
      background: #1e1e36;
      border-bottom: 1px solid #2a2a4a;

      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;

        span {
          color: #fff;
          font-size: 18px;
          font-weight: 600;
        }
      }
    }

    :deep(.el-card__body) {
      padding: 20px;
    }
  }

  .config-form {
    :deep(.el-form-item__label) {
      color: #a0a0c0;
    }

    :deep(.el-input__wrapper),
    :deep(.el-textarea__inner) {
      background: #16162a;
      border: 1px solid #2a2a4a;
      color: #fff;

      &:hover,
      &:focus,
      &.is-focus {
        border-color: #4a9eff;
      }
    }

    :deep(.el-input__inner) {
      color: #fff;
    }

    :deep(.el-input-number) {
      width: 200px;

      .el-input__wrapper {
        background: #16162a;
        border: 1px solid #2a2a4a;
        color: #fff;
      }
    }

    .unit-text {
      margin-left: 10px;
      color: #a0a0c0;
    }
  }

  :deep(.el-dialog) {
    background: #1a1a2e;

    .el-dialog__header {
      border-bottom: 1px solid #2a2a4a;

      .el-dialog__title {
        color: #fff;
      }

      .el-dialog__close {
        color: #a0a0c0;

        &:hover {
          color: #fff;
        }
      }
    }

    .el-dialog__body {
      color: #fff;
    }
  }
}
</style>
