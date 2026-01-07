<template>
  <div class="device-inspection-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-title">
        <el-icon class="title-icon"><Monitor /></el-icon>
        <h2>网络设备与服务器巡检</h2>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="handleImport" class="sci-fi-button">
          <el-icon><Upload /></el-icon>
          批量导入
        </el-button>
        <el-button @click="handleExport" class="sci-fi-button">
          <el-icon><Download /></el-icon>
          导出设备
        </el-button>
      </div>
    </div>

    <!-- 设备统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card" v-for="stat in stats" :key="stat.label">
        <div class="stat-icon" :style="{ background: stat.bgColor }">
          <el-icon><component :is="stat.icon" /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-value">{{ stat.value }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </div>
      </div>
    </div>

    <!-- 设备列表标签页 -->
    <div class="tabs-wrapper">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="sci-fi-tabs">
        <el-tab-pane label="全部设备" name="all"></el-tab-pane>
        <el-tab-pane label="服务器" name="SERVER">
          <template #label>
            <span class="tab-item">
              <el-icon><Server /></el-icon>
              服务器
            </span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="交换机" name="SWITCH">
          <template #label>
            <span class="tab-item">
              <el-icon><Switch /></el-icon>
              交换机
            </span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="路由器" name="ROUTER">
          <template #label>
            <span class="tab-item">
              <el-icon><Connection /></el-icon>
              路由器
            </span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="防火墙" name="FIREWALL">
          <template #label>
            <span class="tab-item">
              <el-icon><Lock /></el-icon>
              防火墙
            </span>
          </template>
        </el-tab-pane>
        <el-tab-pane label="关键设备" name="key"></el-tab-pane>
      </el-tabs>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="机房">
          <el-select v-model="queryParams.roomId" placeholder="全部" clearable class="sci-fi-select">
            <el-option v-for="room in roomList" :key="room.id" :label="room.name" :value="room.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="设备名称">
          <el-input
            v-model="queryParams.deviceName"
            placeholder="请输入设备名称"
            clearable
            class="sci-fi-input"
          />
        </el-form-item>
        <el-form-item label="IP地址">
          <el-input
            v-model="queryParams.ipAddress"
            placeholder="请输入IP地址"
            clearable
            class="sci-fi-input"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="全部" clearable class="sci-fi-select">
            <el-option label="在线" value="ONLINE" />
            <el-option label="离线" value="OFFLINE" />
            <el-option label="故障" value="FAULT" />
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

    <!-- 设备表格 -->
    <div class="table-wrapper">
      <el-table
        :data="tableData"
        v-loading="loading"
        border
        class="sci-fi-table"
        stripe
        height="calc(100vh - 580px)"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="code" label="设备编号" width="140" />
        <el-table-column prop="name" label="设备名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="type" label="设备类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getDeviceTypeTagType(row.type)" size="small">
              {{ getDeviceTypeLabel(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="subType" label="子类型" width="120" align="center" />
        <el-table-column prop="roomName" label="机房" width="120" />
        <el-table-column prop="rackCode" label="机柜" width="100" align="center" />
        <el-table-column prop="uPosition" label="U位" width="60" align="center" />
        <el-table-column prop="ipAddress" label="IP地址" width="140" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)" size="small">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="监控指标" width="180" align="center">
          <template #default="{ row }">
            <div class="metric-item">
              <span class="metric-label">CPU:</span>
              <span :class="getMetricClass(row.currentCpuUsage, row.cpuThreshold)">
                {{ row.currentCpuUsage || '-' }}%
              </span>
            </div>
            <div class="metric-item">
              <span class="metric-label">MEM:</span>
              <span :class="getMetricClass(row.currentMemoryUsage, row.memoryThreshold)">
                {{ row.currentMemoryUsage || '-' }}%
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="isKeyDevice" label="关键设备" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.isKeyDevice === 1" type="danger" size="small">是</el-tag>
            <el-tag v-else type="info" size="small">否</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastInspectionTime" label="上次巡检" width="160" />
        <el-table-column label="操作" width="240" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleInspect(row)">
              <el-icon><View /></el-icon>
              巡检
            </el-button>
            <el-button type="success" link size="small" @click="handleSnmpCollect(row)">
              <el-icon><Refresh /></el-icon>
              采集
            </el-button>
            <el-button type="warning" link size="small" @click="handleViewHistory(row)">
              <el-icon><Document /></el-icon>
              历史
            </el-button>
            <el-button type="info" link size="small" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              编辑
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

    <!-- 批量操作栏 -->
    <div class="batch-actions">
      <el-button type="success" @click="handleBatchInspect" class="sci-fi-button">
        <el-icon><Check /></el-icon>
        批量巡检
      </el-button>
      <el-button type="warning" @click="handleBatchUpdateStatus('MAINTENANCE')" class="sci-fi-button">
        批量设为维护
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Monitor,
  Upload,
  Download,
  Search,
  RefreshLeft,
  View,
  Refresh,
  Document,
  Edit,
  Check,
  Server,
  Switch,
  Connection,
  Lock
} from '@element-plus/icons-vue'
import request from '@/utils/request'

// 响应式数据
const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const activeTab = ref('all')
const roomList = ref<any[]>([])

// 统计数据
const stats = ref([
  { label: '设备总数', value: 0, icon: Monitor, bgColor: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' },
  { label: '在线设备', value: 0, icon: Connection, bgColor: 'linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%)' },
  { label: '关键设备', value: 0, icon: Lock, bgColor: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' },
  { label: '异常设备', value: 0, icon: Document, bgColor: 'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)' }
])

// 查询参数
const queryParams = reactive({
  current: 1,
  size: 20,
  roomId: '',
  type: '',
  subType: '',
  status: '',
  deviceName: '',
  ipAddress: ''
})

// 获取设备类型标签类型
const getDeviceTypeTagType = (type: string) => {
  const typeMap: Record<string, string> = {
    SERVER: 'primary',
    SWITCH: 'success',
    ROUTER: 'warning',
    FIREWALL: 'danger',
    STORAGE: 'info'
  }
  return typeMap[type] || ''
}

// 获取设备类型标签
const getDeviceTypeLabel = (type: string) => {
  const labelMap: Record<string, string> = {
    SERVER: '服务器',
    SWITCH: '交换机',
    ROUTER: '路由器',
    FIREWALL: '防火墙',
    STORAGE: '存储'
  }
  return labelMap[type] || type
}

// 获取状态标签类型
const getStatusTagType = (status: string) => {
  const statusMap: Record<string, string> = {
    ONLINE: 'success',
    OFFLINE: 'info',
    FAULT: 'danger',
    MAINTENANCE: 'warning'
  }
  return statusMap[status] || ''
}

// 获取状态标签
const getStatusLabel = (status: string) => {
  const labelMap: Record<string, string> = {
    ONLINE: '在线',
    OFFLINE: '离线',
    FAULT: '故障',
    MAINTENANCE: '维护中'
  }
  return labelMap[status] || status
}

// 获取指标样式类
const getMetricClass = (value: number, threshold: number) => {
  if (!value || !threshold) return ''
  return value >= threshold ? 'metric-warning' : 'metric-normal'
}

// 查询设备列表
const handleQuery = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/device-inspection/devices/page', { params: queryParams })
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
  Object.assign(queryParams, {
    current: 1,
    size: 20,
    roomId: '',
    type: '',
    subType: '',
    status: '',
    deviceName: '',
    ipAddress: ''
  })
  activeTab.value = 'all'
  handleQuery()
}

// 切换标签页
const handleTabChange = (tabName: string) => {
  if (tabName === 'key') {
    // 关键设备需要单独处理
    queryParams.type = ''
    // TODO: 调用关键设备接口
  } else {
    queryParams.type = tabName === 'all' ? '' : tabName
  }
  queryParams.current = 1
  handleQuery()
}

// 批量导入
const handleImport = () => {
  ElMessage.info('批量导入功能待实现')
}

// 导出设备
const handleExport = async () => {
  try {
    const res = await request.get('/api/device-inspection/devices/export', {
      params: { deviceType: queryParams.type, roomId: queryParams.roomId }
    })
    if (res.code === 200) {
      const blob = new Blob([JSON.stringify(res.data, null, 2)], { type: 'application/json' })
      const url = window.URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `devices-${Date.now()}.json`
      a.click()
      window.URL.revokeObjectURL(url)
      ElMessage.success('导出成功')
    }
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

// 巡检
const handleInspect = async (row: any) => {
  try {
    const res = await request.post(`/api/device-inspection/execute/${row.id}`)
    if (res.code === 200) {
      ElMessage.success('巡检成功')
      handleQuery()
    }
  } catch (error) {
    ElMessage.error('巡检失败')
  }
}

// SNMP采集
const handleSnmpCollect = async (row: any) => {
  try {
    const res = await request.post(`/api/device-inspection/snmp/collect/${row.id}`)
    if (res.code === 200) {
      ElMessage.success('采集成功')
      handleQuery()
    }
  } catch (error) {
    ElMessage.error('采集失败')
  }
}

// 查看历史
const handleViewHistory = (row: any) => {
  ElMessage.info('查看巡检历史')
}

// 编辑
const handleEdit = (row: any) => {
  ElMessage.info('编辑设备')
}

// 批量巡检
const handleBatchInspect = () => {
  ElMessage.info('批量巡检功能待实现')
}

// 批量更新状态
const handleBatchUpdateStatus = (status: string) => {
  ElMessage.info('批量更新状态功能待实现')
}

// 获取统计数据
const fetchStats = async () => {
  try {
    const res = await request.get('/api/device-inspection/devices/stats')
    if (res.code === 200) {
      const data = res.data
      stats.value[0].value = data.total || 0
      stats.value[1].value = data.onlineCount || 0
      stats.value[2].value = data.keyDeviceCount || 0
      // 异常设备数 = 总数 - 在线数
      stats.value[3].value = (data.total || 0) - (data.onlineCount || 0)
    }
  } catch (error) {
    console.error('获取统计数据失败', error)
  }
}

// 页面初始化
onMounted(() => {
  handleQuery()
  fetchStats()
  // TODO: 获取机房列表
})
</script>

<style scoped>
.device-inspection-container {
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

/* 统计卡片 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  background: rgba(26, 31, 58, 0.6);
  border: 1px solid rgba(100, 200, 255, 0.2);
  border-radius: 8px;
  box-shadow: 0 0 15px rgba(0, 150, 255, 0.1);
}

.stat-icon {
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  margin-right: 15px;
}

.stat-icon :deep(.el-icon) {
  font-size: 24px;
  color: #fff;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #00c6ff;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #999;
}

/* 标签页 */
.tabs-wrapper {
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
.table-wrapper {
  background: rgba(26, 31, 58, 0.4);
  border: 1px solid rgba(100, 200, 255, 0.2);
  border-radius: 8px;
  padding: 16px;
}

.metric-item {
  display: flex;
  align-items: center;
  font-size: 12px;
  margin-bottom: 4px;
}

.metric-label {
  margin-right: 5px;
  color: #999;
}

.metric-normal {
  color: #67c23a;
}

.metric-warning {
  color: #f56c6c;
  font-weight: 600;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

/* 批量操作栏 */
.batch-actions {
  margin-top: 20px;
  padding: 16px;
  background: rgba(26, 31, 58, 0.4);
  border: 1px solid rgba(100, 200, 255, 0.2);
  border-radius: 8px;
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

.sci-fi-input :deep(.el-input__inner) {
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
</style>
