<template>
  <div class="api-config-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-title">
        <el-icon class="title-icon"><Setting /></el-icon>
        <h2>接口配置管理</h2>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="handleAdd" class="sci-fi-button">
          <el-icon><Plus /></el-icon>
          新增配置
        </el-button>
        <el-button @click="handleExport" class="sci-fi-button">
          <el-icon><Download /></el-icon>
          导出配置
        </el-button>
      </div>
    </div>

    <!-- 配置类型标签页 -->
    <div class="config-tabs-wrapper">
      <el-tabs v-model="activeConfigType" @tab-change="handleTabChange" class="sci-fi-tabs">
        <el-tab-pane label="全部配置" name=""></el-tab-pane>
        <el-tab-pane label="钉钉配置" name="DINGTALK">
          <template #label>
            <span class="tab-item">
              <el-icon><ChatLineSquare /></el-icon>
              钉钉配置
            </span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="短信配置" name="SMS">
          <template #label>
            <span class="tab-item">
              <el-icon><Message /></el-icon>
              短信配置
            </span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="海康门禁" name="HIKVISION">
          <template #label>
            <span class="tab-item">
              <el-icon><Lock /></el-icon>
              海康门禁
            </span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="大华门禁" name="DAHUA">
          <template #label>
            <span class="tab-item">
              <el-icon><Lock /></el-icon>
              大华门禁
            </span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="SNMP监控" name="SNMP">
          <template #label>
            <span class="tab-item">
              <el-icon><Monitor /></el-icon>
              SNMP监控
            </span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="Modbus监控" name="MODBUS">
          <template #label>
            <span class="tab-item">
              <el-icon><Connection /></el-icon>
              Modbus监控
            </span>
          </template>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="配置名称">
          <el-input
            v-model="queryParams.configName"
            placeholder="请输入配置名称"
            clearable
            class="sci-fi-input"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable class="sci-fi-select">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery" class="sci-fi-button">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset" class="sci-fi-button">
            <el-icon><RefreshLeft /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 配置表格 -->
    <div class="config-table-wrapper">
      <el-table
        :data="tableData"
        v-loading="loading"
        border
        class="sci-fi-table"
        stripe
        height="calc(100vh - 400px)"
      >
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="configType" label="配置类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getConfigTypeTagType(row.configType)" size="small">
              {{ getConfigTypeLabel(row.configType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="configName" label="配置名称" width="180" show-overflow-tooltip />
        <el-table-column prop="configKey" label="配置键" width="200" show-overflow-tooltip />
        <el-table-column prop="configValue" label="配置值" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.isSensitive" class="sensitive-value">******</span>
            <span v-else>{{ row.configValue || '-' }}</span>
            <el-button
              v-if="row.isSensitive"
              type="primary"
              link
              size="small"
              @click="handleShowValue(row)"
            >
              查看
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="configGroup" label="配置分组" width="120" align="center" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="180" show-overflow-tooltip />
        <el-table-column label="操作" width="240" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button type="success" link size="small" @click="handleTest(row)">
              <el-icon><Connection /></el-icon>
              测试
            </el-button>
            <el-button
              :type="row.status === 1 ? 'warning' : 'success'"
              link
              size="small"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryParams.current"
          v-model:page-size="queryParams.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleQuery"
          @current-change="handleQuery"
          class="sci-fi-pagination"
        />
      </div>
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      class="sci-fi-dialog"
      :close-on-click-modal="false"
    >
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="120px">
        <el-form-item label="配置类型" prop="configType">
          <el-select
            v-model="form.configType"
            placeholder="请选择配置类型"
            class="sci-fi-select"
            :disabled="isEdit"
          >
            <el-option label="钉钉" value="DINGTALK" />
            <el-option label="短信" value="SMS" />
            <el-option label="邮件" value="EMAIL" />
            <el-option label="海康门禁" value="HIKVISION" />
            <el-option label="大华门禁" value="DAHUA" />
            <el-option label="SNMP监控" value="SNMP" />
            <el-option label="Modbus监控" value="MODBUS" />
            <el-option label="BMS接口" value="BMS" />
            <el-option label="传感器网络" value="SENSOR" />
            <el-option label="消防主机" value="FIRE" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="配置名称" prop="configName">
          <el-input v-model="form.configName" placeholder="请输入配置名称" class="sci-fi-input" />
        </el-form-item>
        <el-form-item label="配置键" prop="configKey">
          <el-input v-model="form.configKey" placeholder="请输入配置键" class="sci-fi-input" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="配置值" prop="configValue">
          <el-input
            v-model="form.configValue"
            type="textarea"
            :rows="3"
            placeholder="请输入配置值"
            class="sci-fi-input"
          />
        </el-form-item>
        <el-form-item label="敏感配置" prop="isSensitive">
          <el-switch v-model="form.isSensitive" :active-value="1" :inactive-value="0" />
          <span class="form-tip">敏感信息将加密存储，列表中显示为******</span>
        </el-form-item>
        <el-form-item label="配置分组" prop="configGroup">
          <el-input v-model="form.configGroup" placeholder="请输入配置分组" class="sci-fi-input" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="2"
            placeholder="请输入描述"
            class="sci-fi-input"
          />
        </el-form-item>
        <el-form-item label="排序序号" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" class="sci-fi-input-number" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false" class="sci-fi-button">取消</el-button>
        <el-button type="primary" @click="handleSubmit" class="sci-fi-button">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看敏感信息对话框 -->
    <el-dialog v-model="valueDialogVisible" title="查看敏感信息" width="500px" class="sci-fi-dialog">
      <el-input v-model="sensitiveValue" type="textarea" :rows="4" readonly class="sci-fi-input" />
      <template #footer>
        <el-button @click="valueDialogVisible = false" class="sci-fi-button">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Setting,
  Plus,
  Download,
  ChatLineSquare,
  Message,
  Lock,
  Monitor,
  Connection,
  Search,
  RefreshLeft,
  Edit,
  Delete
} from '@element-plus/icons-vue'
import request from '@/utils/request'

// 响应式数据
const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const activeConfigType = ref('')

// 查询参数
const queryParams = reactive({
  current: 1,
  size: 20,
  configType: '',
  configGroup: '',
  status: undefined,
  configName: ''
})

// 对话框相关
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const formRef = ref()
const form = reactive({
  id: undefined,
  configType: '',
  configName: '',
  configKey: '',
  configValue: '',
  isSensitive: 0,
  configGroup: '',
  status: 1,
  description: '',
  sortOrder: 0
})

// 表单验证规则
const formRules = {
  configType: [{ required: true, message: '请选择配置类型', trigger: 'change' }],
  configName: [{ required: true, message: '请输入配置名称', trigger: 'blur' }],
  configKey: [{ required: true, message: '请输入配置键', trigger: 'blur' }],
  configValue: [{ required: true, message: '请输入配置值', trigger: 'blur' }]
}

// 查看敏感信息
const valueDialogVisible = ref(false)
const sensitiveValue = ref('')

// 获取配置类型标签类型
const getConfigTypeTagType = (type: string) => {
  const typeMap: Record<string, string> = {
    DINGTALK: 'primary',
    SMS: 'success',
    EMAIL: 'info',
    HIKVISION: 'warning',
    DAHUA: 'warning',
    SNMP: 'danger',
    MODBUS: 'danger',
    BMS: '',
    SENSOR: '',
    FIRE: '',
    OTHER: ''
  }
  return typeMap[type] || ''
}

// 获取配置类型标签
const getConfigTypeLabel = (type: string) => {
  const labelMap: Record<string, string> = {
    DINGTALK: '钉钉',
    SMS: '短信',
    EMAIL: '邮件',
    HIKVISION: '海康门禁',
    DAHUA: '大华门禁',
    SNMP: 'SNMP监控',
    MODBUS: 'Modbus监控',
    BMS: 'BMS接口',
    SENSOR: '传感器网络',
    FIRE: '消防主机',
    OTHER: '其他'
  }
  return labelMap[type] || type
}

// 查询配置列表
const handleQuery = async () => {
  loading.value = true
  try {
    queryParams.configType = activeConfigType.value
    const res = await request.get('/api/api-config/page', { params: queryParams })
    if (res.code === 200) {
      tableData.value = res.data.records
      total.value = res.data.total
    }
  } catch (error) {
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

// 重置查询
const handleReset = () => {
  queryParams.current = 1
  queryParams.configType = ''
  queryParams.configGroup = ''
  queryParams.status = undefined
  queryParams.configName = ''
  activeConfigType.value = ''
  handleQuery()
}

// 切换标签页
const handleTabChange = (tabName: string) => {
  activeConfigType.value = tabName
  queryParams.current = 1
  handleQuery()
}

// 新增配置
const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增接口配置'
  Object.assign(form, {
    id: undefined,
    configType: activeConfigType.value || '',
    configName: '',
    configKey: '',
    configValue: '',
    isSensitive: 0,
    configGroup: '',
    status: 1,
    description: '',
    sortOrder: 0
  })
  dialogVisible.value = true
}

// 编辑配置
const handleEdit = (row: any) => {
  isEdit.value = true
  dialogTitle.value = '编辑接口配置'
  Object.assign(form, {
    id: row.id,
    configType: row.configType,
    configName: row.configName,
    configKey: row.configKey,
    configValue: row.configValue,
    isSensitive: row.isSensitive,
    configGroup: row.configGroup,
    status: row.status,
    description: row.description,
    sortOrder: row.sortOrder
  })
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        const url = isEdit.value ? `/api/api-config/${form.id}` : '/api/api-config'
        const method = isEdit.value ? 'put' : 'post'
        const res = await request[method](url, form)
        if (res.code === 200) {
          ElMessage.success(isEdit.value ? '更新成功' : '新增成功')
          dialogVisible.value = false
          handleQuery()
        }
      } catch (error) {
        ElMessage.error('操作失败')
      }
    }
  })
}

// 删除配置
const handleDelete = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除该配置吗？', '提示', {
      type: 'warning'
    })
    const res = await request.delete(`/api/api-config/${row.id}`)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      handleQuery()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 切换状态
const handleToggleStatus = async (row: any) => {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    const res = await request.put(`/api/api-config/${row.id}/status`, null, {
      params: { status: newStatus }
    })
    if (res.code === 200) {
      ElMessage.success('状态更新成功')
      handleQuery()
    }
  } catch (error) {
    ElMessage.error('更新失败')
  }
}

// 测试连接
const handleTest = async (row: any) => {
  try {
    const res = await request.post(`/api/api-config/test/${row.configType}`)
    if (res.code === 200) {
      if (res.data.success) {
        ElMessage.success(res.data.message || '测试成功')
      } else {
        ElMessage.error(res.data.message || '测试失败')
      }
    }
  } catch (error) {
    ElMessage.error('测试失败')
  }
}

// 查看敏感信息
const handleShowValue = async (row: any) => {
  try {
    const res = await request.get(`/api/api-config/${row.id}`)
    if (res.code === 200) {
      sensitiveValue.value = res.data.configValue
      valueDialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('获取失败')
  }
}

// 导出配置
const handleExport = async () => {
  try {
    const res = await request.get('/api/api-config/export', {
      params: {
        configType: activeConfigType.value
      }
    })
    if (res.code === 200) {
      // 导出为JSON文件
      const blob = new Blob([JSON.stringify(res.data, null, 2)], { type: 'application/json' })
      const url = window.URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `api-config-${Date.now()}.json`
      a.click()
      window.URL.revokeObjectURL(url)
      ElMessage.success('导出成功')
    }
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

// 页面初始化
onMounted(() => {
  handleQuery()
})
</script>

<style scoped>
.api-config-container {
  padding: 20px;
  background: linear-gradient(135deg, #0a0e27 0%, #1a1f3a 100%);
  min-height: calc(100vh - 60px);
  color: #fff;
}

/* 页面标题 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px;
  background: rgba(26, 31, 58, 0.6);
  border: 1px solid rgba(100, 200, 255, 0.3);
  border-radius: 8px;
  box-shadow: 0 0 20px rgba(0, 150, 255, 0.2);
}

.header-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  font-size: 32px;
  color: #00c6ff;
  text-shadow: 0 0 10px rgba(0, 198, 255, 0.6);
}

.header-title h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  background: linear-gradient(90deg, #00c6ff, #92fe9d);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.header-actions {
  display: flex;
  gap: 12px;
}

/* 标签页 */
.config-tabs-wrapper {
  margin-bottom: 20px;
}

.tab-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

/* 搜索栏 */
.search-bar {
  margin-bottom: 20px;
  padding: 16px;
  background: rgba(26, 31, 58, 0.4);
  border: 1px solid rgba(100, 200, 255, 0.2);
  border-radius: 8px;
}

.search-form {
  display: flex;
  align-items: center;
}

/* 表格区域 */
.config-table-wrapper {
  background: rgba(26, 31, 58, 0.4);
  border: 1px solid rgba(100, 200, 255, 0.2);
  border-radius: 8px;
  padding: 16px;
}

.sensitive-value {
  color: #ff6b6b;
  font-family: monospace;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

/* 表单提示 */
.form-tip {
  margin-left: 10px;
  color: #999;
  font-size: 12px;
}

/* 科幻风格组件 */
.sci-fi-button {
  background: linear-gradient(135deg, #00c6ff 0%, #0072ff 100%);
  border: none;
  color: #fff;
  font-weight: 600;
  transition: all 0.3s;
  box-shadow: 0 0 15px rgba(0, 198, 255, 0.4);
}

.sci-fi-button:hover {
  box-shadow: 0 0 25px rgba(0, 198, 255, 0.7);
  transform: translateY(-2px);
}

.sci-fi-input :deep(.el-input__wrapper) {
  background: rgba(10, 14, 39, 0.6);
  border: 1px solid rgba(100, 200, 255, 0.3);
  border-radius: 4px;
  box-shadow: none;
}

.sci-fi-input :deep(.el-input__inner),
.sci-fi-input :deep(.el-textarea__inner) {
  color: #fff;
  background: transparent;
}

.sci-fi-select :deep(.el-input__wrapper) {
  background: rgba(10, 14, 39, 0.6);
  border: 1px solid rgba(100, 200, 255, 0.3);
  border-radius: 4px;
}

.sci-fi-select :deep(.el-input__inner) {
  color: #fff;
  background: transparent;
}

.sci-fi-table :deep(.el-table__header-wrapper) {
  background: rgba(0, 198, 255, 0.1);
}

.sci-fi-table :deep(.el-table__header th) {
  background: rgba(0, 198, 255, 0.15);
  color: #00c6ff;
  font-weight: 600;
  border-color: rgba(100, 200, 255, 0.3);
}

.sci-fi-table :deep(.el-table__body td) {
  border-color: rgba(100, 200, 255, 0.2);
  color: #e0e0e0;
}

.sci-fi-table :deep(.el-table__row:hover td) {
  background: rgba(0, 198, 255, 0.1);
}

.sci-fi-pagination :deep(.el-pagination.is-background .btn-prev),
.sci-fi-pagination :deep(.el-pagination.is-background .btn-next),
.sci-fi-pagination :deep(.el-pagination.is-background .el-pager li) {
  background: rgba(10, 14, 39, 0.6);
  border: 1px solid rgba(100, 200, 255, 0.3);
  color: #00c6ff;
}

.sci-fi-pagination :deep(.el-pagination.is-background .el-pager li.is-active) {
  background: linear-gradient(135deg, #00c6ff 0%, #0072ff 100%);
  color: #fff;
}

.sci-fi-tabs :deep(.el-tabs__item) {
  color: #999;
}

.sci-fi-tabs :deep(.el-tabs__item.is-active) {
  color: #00c6ff;
}

.sci-fi-tabs :deep(.el-tabs__active-bar) {
  background: linear-gradient(90deg, #00c6ff, #92fe9d);
}

.sci-fi-dialog :deep(.el-dialog) {
  background: rgba(26, 31, 58, 0.95);
  border: 1px solid rgba(100, 200, 255, 0.4);
  box-shadow: 0 0 40px rgba(0, 198, 255, 0.3);
}

.sci-fi-dialog :deep(.el-dialog__title) {
  color: #00c6ff;
}

.sci-fi-dialog :deep(.el-dialog__body) {
  color: #e0e0e0;
}

.sci-fi-dialog :deep(.el-form-item__label) {
  color: #00c6ff;
}

.sci-fi-input-number :deep(.el-input__wrapper) {
  background: rgba(10, 14, 39, 0.6);
  border: 1px solid rgba(100, 200, 255, 0.3);
  border-radius: 4px;
}

.sci-fi-input-number :deep(.el-input__inner) {
  color: #fff;
  background: transparent;
}
</style>
