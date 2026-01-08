<template>
  <div class="fire-protection-monitoring">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">消防保障系统监控</h1>
      <div class="page-actions">
        <el-button type="danger" @click="handleBatchCollect" :icon="Refresh">
          批量采集数据
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-container">
      <div class="stat-card total">
        <div class="stat-icon">🧯</div>
        <div class="stat-content">
          <div class="stat-value">{{ statistics.total }}</div>
          <div class="stat-label">灭火器总数</div>
        </div>
      </div>
      <div class="stat-card online">
        <div class="stat-icon">🟢</div>
        <div class="stat-content">
          <div class="stat-value">{{ statistics.online }}</div>
          <div class="stat-label">在线正常</div>
        </div>
      </div>
      <div class="stat-card alarm">
        <div class="stat-icon">🔴</div>
        <div class="stat-content">
          <div class="stat-value">{{ statistics.pressureAbnormal }}</div>
          <div class="stat-label">压力异常</div>
        </div>
      </div>
      <div class="stat-card weight">
        <div class="stat-icon">⚠️</div>
        <div class="stat-content">
          <div class="stat-value">{{ statistics.weightAbnormal }}</div>
          <div class="stat-label">重量异常</div>
        </div>
      </div>
    </div>

    <!-- 标签页 -->
    <el-tabs v-model="activeTab" class="monitoring-tabs">
      <el-tab-pane label="灭火器管理" name="extinguishers">
        <!-- 灭火器列表 -->
        <el-table
          :data="extinguisherList"
          border
          stripe
          style="width: 100%"
          v-loading="loading"
        >
          <el-table-column prop="extinguisherCode" label="编号" width="120" />
          <el-table-column prop="extinguisherName" label="名称" width="150" />
          <el-table-column prop="location" label="位置" width="150" />
          <el-table-column prop="specification" label="规格" width="100" />
          <el-table-column label="压力(MPa)" width="100">
            <template #default="{ row }">
              <span :class="getPressureClass(row.pressureStatus)">
                {{ row.currentPressure }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="重量(kg)" width="100">
            <template #default="{ row }">
              <span :class="getWeightClass(row.weightStatus)">
                {{ row.currentWeight }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 0 ? 'success' : 'danger'">
                {{ row.status === 0 ? '正常' : '告警' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="消防主机日志" name="fireHostLogs">
        <!-- 查询表单 -->
        <div class="search-form">
          <el-form :inline="true" :model="logQueryParams">
            <el-form-item label="信号类型">
              <el-select v-model="logQueryParams.signalType" placeholder="请选择" clearable>
                <el-option label="一般信号" :value="1" />
                <el-option label="火警信号" :value="2" />
                <el-option label="故障信号" :value="3" />
                <el-option label="故障恢复" :value="4" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadFireHostLogs">查询</el-button>
              <el-button @click="handleLogReset">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 日志列表 -->
        <el-table :data="fireHostLogList" border stripe style="width: 100%" v-loading="loading">
          <el-table-column prop="hostCode" label="主机编号" width="120" />
          <el-table-column label="信号类型" width="100">
            <template #default="{ row }">
              <el-tag :type="getSignalTypeTag(row.signalType)">
                {{ getSignalTypeName(row.signalType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="detectorLocation" label="位置" width="200" />
          <el-table-column prop="signalDescription" label="描述" width="200" />
          <el-table-column prop="signalTime" label="时间" width="160" />
          <el-table-column label="确认状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.confirmed ? 'success' : 'warning'">
                {{ row.confirmed ? '已确认' : '未确认' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150">
            <template #default="{ row }">
              <el-button
                v-if="!row.confirmed"
                type="primary"
                size="small"
                @click="handleConfirmSignal(row)"
              >
                确认
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="气体压力监控" name="gasPressure">
        <!-- 气体压力监控列表 -->
        <el-table
          :data="gasPressureList"
          border
          stripe
          style="width: 100%"
          v-loading="loading"
        >
          <el-table-column prop="systemCode" label="系统编号" width="120" />
          <el-table-column prop="systemName" label="系统名称" width="150" />
          <el-table-column prop="bottleCode" label="钢瓶编号" width="120" />
          <el-table-column label="气体类型" width="100">
            <template #default="{ row }">
              {{ getGasTypeName(row.gasType) }}
            </template>
          </el-table-column>
          <el-table-column label="当前压力(MPa)" width="120">
            <template #default="{ row }">
              <span :class="getPressureClass(row.pressureStatus)">
                {{ row.currentPressure }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="ratedPressure" label="额定压力(MPa)" width="120" />
          <el-table-column label="压力状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getPressureStatusTag(row.pressureStatus)">
                {{ getPressureStatusName(row.pressureStatus) }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="审验提醒" name="reminders">
        <!-- 审验提醒列表 -->
        <el-table :data="reminderList" border stripe style="width: 100%" v-loading="loading">
          <el-table-column prop="reminderNo" label="提醒编号" width="180" />
          <el-table-column prop="facilityName" label="设施名称" width="150" />
          <el-table-column label="设施类型" width="100">
            <template #default="{ row }">
              {{ getFacilityTypeName(row.facilityType) }}
            </template>
          </el-table-column>
          <el-table-column prop="reminderContent" label="提醒内容" width="300" />
          <el-table-column prop="reminderDate" label="提醒日期" width="120" />
          <el-table-column label="处理状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.handled ? 'success' : 'warning'">
                {{ row.handled ? '已处理' : '未处理' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import axios from 'axios'

interface FireExtinguisher {
  id: number
  extinguisherCode: string
  extinguisherName: string
  location: string
  specification: string
  currentPressure: number
  pressureStatus: number
  currentWeight: number
  weightStatus: number
  status: number
}

interface FireHostLog {
  id: number
  hostCode: string
  signalType: number
  detectorLocation: string
  signalDescription: string
  signalTime: string
  confirmed: boolean
}

interface GasPressureMonitor {
  id: number
  systemCode: string
  systemName: string
  bottleCode: string
  gasType: number
  currentPressure: number
  ratedPressure: number
  pressureStatus: number
}

interface FireInspectionReminder {
  id: number
  reminderNo: string
  facilityName: string
  facilityType: number
  reminderContent: string
  reminderDate: string
  handled: boolean
}

// 数据
const activeTab = ref('extinguishers')
const loading = ref(false)
const extinguisherList = ref<FireExtinguisher[]>([])
const fireHostLogList = ref<FireHostLog[]>([])
const gasPressureList = ref<GasPressureMonitor[]>([])
const reminderList = ref<FireInspectionReminder[]>([])

const statistics = ref({
  total: 0,
  online: 0,
  pressureAbnormal: 0,
  weightAbnormal: 0
})

const logQueryParams = reactive({
  signalType: null as number | null
})

// 方法
const getPressureClass = (status: number) => {
  if (status === 0) return 'normal'
  if (status === 1) return 'low'
  if (status === 2) return 'alert'
  return 'abnormal'
}

const getWeightClass = (status: number) => {
  if (status === 0) return 'normal'
  if (status === 1) return 'low'
  if (status === 2) return 'alert'
  return 'abnormal'
}

const getSignalTypeName = (type: number) => {
  const typeMap: Record<number, string> = {
    1: '一般信号',
    2: '火警信号',
    3: '故障信号',
    4: '故障恢复'
  }
  return typeMap[type] || '未知'
}

const getSignalTypeTag = (type: number) => {
  if (type === 2) return 'danger'
  if (type === 3) return 'warning'
  return 'info'
}

const getGasTypeName = (type: number) => {
  const typeMap: Record<number, string> = {
    1: '七氟丙烷',
    2: 'IG541',
    3: '二氧化碳'
  }
  return typeMap[type] || '未知'
}

const getPressureStatusName = (status: number) => {
  const statusMap: Record<number, string> = {
    0: '正常',
    1: '偏低',
    2: '预警',
    3: '异常'
  }
  return statusMap[status] || '未知'
}

const getPressureStatusTag = (status: number) => {
  if (status === 0) return 'success'
  if (status === 1) return 'warning'
  if (status === 2) return 'warning'
  return 'danger'
}

const getFacilityTypeName = (type: number) => {
  const typeMap: Record<number, string> = {
    1: '灭火器',
    2: '气体灭火系统',
    3: '消防主机',
    4: '消防栓',
    5: '喷淋系统',
    6: '应急照明'
  }
  return typeMap[type] || '未知'
}

const loadStatistics = async () => {
  try {
    const response = await axios.get('/api/fire-protection/extinguishers/statistics')
    statistics.value = response.data.data
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const loadExtinguishers = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/fire-protection/extinguishers/list')
    extinguisherList.value = response.data.data.records
  } catch (error) {
    ElMessage.error('加载灭火器列表失败')
  } finally {
    loading.value = false
  }
}

const loadFireHostLogs = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/fire-protection/fireHostLogs/list', {
      params: logQueryParams
    })
    fireHostLogList.value = response.data.data.records
  } catch (error) {
    ElMessage.error('加载消防主机日志失败')
  } finally {
    loading.value = false
  }
}

const loadGasPressureMonitors = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/fire-protection/gasPressureMonitors/list')
    gasPressureList.value = response.data.data.records
  } catch (error) {
    ElMessage.error('加载气体压力监控失败')
  } finally {
    loading.value = false
  }
}

const loadReminders = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/fire-protection/inspectionReminders/list')
    reminderList.value = response.data.data.records
  } catch (error) {
    ElMessage.error('加载审验提醒失败')
  } finally {
    loading.value = false
  }
}

const handleBatchCollect = async () => {
  try {
    await axios.post('/api/fire-protection/extinguishers/batchCollect')
    ElMessage.success('批量采集数据成功')
    loadExtinguishers()
    loadStatistics()
  } catch (error) {
    ElMessage.error('批量采集数据失败')
  }
}

const handleLogReset = () => {
  logQueryParams.signalType = null
  loadFireHostLogs()
}

const handleConfirmSignal = async (row: FireHostLog) => {
  const { value } = await ElMessageBox.prompt('请输入确认备注', '确认信号', {
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  })

  try {
    await axios.post('/api/fire-protection/fireHostLogs/confirm', null, {
      params: {
        logId: row.id,
        confirmer: 'admin',
        confirmRemark: value
      }
    })
    ElMessage.success('确认成功')
    loadFireHostLogs()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('确认失败')
    }
  }
}

// 监听标签页切换
watch(activeTab, (newTab) => {
  if (newTab === 'extinguishers') {
    loadExtinguishers()
  } else if (newTab === 'fireHostLogs') {
    loadFireHostLogs()
  } else if (newTab === 'gasPressure') {
    loadGasPressureMonitors()
  } else if (newTab === 'reminders') {
    loadReminders()
  }
})

// 初始化
onMounted(() => {
  loadStatistics()
  loadExtinguishers()
})
</script>

<style scoped>
.fire-protection-monitoring {
  padding: 20px;
  background: linear-gradient(135deg, #0d1b2a 0%, #1b263b 100%);
  min-height: 100vh;
  color: #fff;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 2px solid;
  border-image: linear-gradient(to right, #ff0000, #ff6600) 1;
}

.page-title {
  font-size: 28px;
  font-weight: bold;
  background: linear-gradient(to right, #ff0000, #ff6600);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
  text-shadow: 0 0 10px rgba(255, 0, 0, 0.3);
}

.stats-container {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 0, 0, 0.3);
  border-radius: 10px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 0 20px rgba(255, 0, 0, 0.2);
}

.stat-value {
  font-size: 36px;
  font-weight: bold;
  background: linear-gradient(to right, #ff0000, #ff6600);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.stat-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  margin-top: 5px;
}

.normal {
  color: #00ff00;
  font-weight: bold;
}

.low {
  color: #ffff00;
  font-weight: bold;
}

.alert {
  color: #ff9900;
  font-weight: bold;
}

.abnormal {
  color: #ff0000;
  font-weight: bold;
}

.monitoring-tabs {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 0, 0, 0.3);
  border-radius: 10px;
  padding: 20px;
}

.search-form {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 0, 0, 0.3);
  border-radius: 10px;
  padding: 20px;
  margin-bottom: 20px;
}

:deep(.el-table) {
  background: transparent !important;
  color: #fff !important;
}

:deep(.el-table th) {
  background: rgba(255, 0, 0, 0.1) !important;
  color: #ff0000 !important;
  border-color: rgba(255, 0, 0, 0.3) !important;
}

:deep(.el-table td) {
  border-color: rgba(255, 255, 255, 0.1) !important;
}

:deep(.el-tabs__item) {
  color: rgba(255, 255, 255, 0.7) !important;
}

:deep(.el-tabs__item.is-active) {
  color: #ff0000 !important;
}
</style>
