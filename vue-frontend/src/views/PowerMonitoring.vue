<template>
  <div class="power-monitoring-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">电力系统监控</h1>
      <p class="page-subtitle">SNMP/Modbus实时采集 · 阈值告警 · 趋势预警</p>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: rgba(0, 212, 255, 0.1);">
            <el-icon :size="24" color="#00d4ff"><Monitor /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">设备总数</div>
            <div class="stat-value">{{ deviceStats.totalCount || 0 }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: rgba(34, 197, 94, 0.1);">
            <el-icon :size="24" color="#22c55e"><CircleCheck /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">在线设备</div>
            <div class="stat-value">{{ deviceStats.onlineCount || 0 }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: rgba(239, 68, 68, 0.1);">
            <el-icon :size="24" color="#ef4444"><CircleClose /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">离线设备</div>
            <div class="stat-value">{{ deviceStats.offlineCount || 0 }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: rgba(251, 146, 60, 0.1);">
            <el-icon :size="24" color="#fb923c"><Warning /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-label">告警设备</div>
            <div class="stat-value">{{ alertDeviceCount }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 操作栏 -->
    <div class="action-bar">
      <el-button type="primary" @click="handleBatchCollect" :loading="collecting">
        <el-icon><Refresh /></el-icon>
        批量采集数据
      </el-button>
      <el-button @click="loadDeviceList">
        <el-icon><Search /></el-icon>
        刷新列表
      </el-button>
      <el-select v-model="filter.deviceType" placeholder="设备类型" clearable style="width: 150px;">
        <el-option label="市电" value="main_power" />
        <el-option label="UPS" value="ups" />
        <el-option label="PDU" value="pdu" />
        <el-option label="发电机" value="generator" />
      </el-select>
      <el-select v-model="filter.status" placeholder="状态" clearable style="width: 120px;">
        <el-option label="在线" value="online" />
        <el-option label="离线" value="offline" />
      </el-select>
    </div>

    <!-- 设备列表 -->
    <el-table
      :data="deviceList"
      v-loading="loading"
      class="custom-table"
      stripe
      style="width: 100%"
    >
      <el-table-column prop="deviceCode" label="设备编号" width="140" />
      <el-table-column prop="deviceName" label="设备名称" width="180" />
      <el-table-column prop="deviceType" label="设备类型" width="100">
        <template #default="{ row }">
          <el-tag :type="getDeviceTypeColor(row.deviceType)">
            {{ getDeviceTypeText(row.deviceType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="roomName" label="机房位置" width="140" />
      <el-table-column prop="protocol" label="协议" width="80">
        <template #default="{ row }">
          <el-tag size="small">{{ row.protocol?.toUpperCase() }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="实时负载" width="120">
        <template #default="{ row }">
          <div v-if="row.latestMetric">
            <el-progress
              :percentage="Number(row.latestMetric.totalLoadPercent) || 0"
              :color="getLoadColor(row.latestMetric.totalLoadPercent)"
              :stroke-width="8"
            />
          </div>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 'online' ? 'success' : 'danger'">
            {{ row.status === 'online' ? '在线' : '离线' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="告警" width="80">
        <template #default="{ row }">
          <el-tag v-if="row.latestMetric?.isAlert" type="danger">告警</el-tag>
          <el-tag v-else type="success">正常</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleCollect(row)">
            采集
          </el-button>
          <el-button link type="primary" size="small" @click="viewMetrics(row)">
            指标
          </el-button>
          <el-button link type="primary" size="small" @click="viewTrend(row)">
            趋势
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      v-model:current-page="pagination.pageNum"
      v-model:page-size="pagination.pageSize"
      :total="pagination.total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="loadDeviceList"
      @current-change="loadDeviceList"
      class="pagination"
    />

    <!-- 指标详情对话框 -->
    <el-dialog
      v-model="metricDialogVisible"
      title="实时监控指标"
      width="800px"
      class="custom-dialog"
    >
      <div v-if="selectedDevice" class="metric-details">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="设备名称">{{ selectedDevice.deviceName }}</el-descriptions-item>
          <el-descriptions-item label="设备类型">
            {{ getDeviceTypeText(selectedDevice.deviceType) }}
          </el-descriptions-item>
          <el-descriptions-item label="采集时间">
            {{ selectedDevice.latestMetric?.collectTime }}
          </el-descriptions-item>
          <el-descriptions-item label="采集方式">
            {{ selectedDevice.latestMetric?.dataSource?.toUpperCase() }}
          </el-descriptions-item>

          <!-- 市电参数 -->
          <el-descriptions-item v-if="selectedDevice.deviceType === 'main_power'" label="输入电压A相">
            {{ selectedDevice.latestMetric?.inputVoltageA }} V
          </el-descriptions-item>
          <el-descriptions-item v-if="selectedDevice.deviceType === 'main_power'" label="输入电压B相">
            {{ selectedDevice.latestMetric?.inputVoltageB }} V
          </el-descriptions-item>
          <el-descriptions-item v-if="selectedDevice.deviceType === 'main_power'" label="输入电压C相">
            {{ selectedDevice.latestMetric?.inputVoltageC }} V
          </el-descriptions-item>
          <el-descriptions-item v-if="selectedDevice.deviceType === 'main_power'" label="输入频率">
            {{ selectedDevice.latestMetric?.inputFrequency }} Hz
          </el-descriptions-item>

          <!-- UPS参数 -->
          <el-descriptions-item v-if="selectedDevice.deviceType === 'ups'" label="负载百分比">
            {{ selectedDevice.latestMetric?.upsLoadPercent }}%
          </el-descriptions-item>
          <el-descriptions-item v-if="selectedDevice.deviceType === 'ups'" label="后备时间">
            {{ selectedDevice.latestMetric?.upsBackupTime }} 分钟
          </el-descriptions-item>
          <el-descriptions-item v-if="selectedDevice.deviceType === 'ups'" label="电池容量">
            {{ selectedDevice.latestMetric?.batteryCapacity }}%
          </el-descriptions-item>
          <el-descriptions-item v-if="selectedDevice.deviceType === 'ups'" label="电池状态">
            {{ selectedDevice.latestMetric?.batteryStatus }}
          </el-descriptions-item>

          <!-- PDU参数 -->
          <el-descriptions-item v-if="selectedDevice.deviceType === 'pdu'" label="总电流">
            {{ selectedDevice.latestMetric?.pduTotalCurrent }} A
          </el-descriptions-item>
          <el-descriptions-item v-if="selectedDevice.deviceType === 'pdu'" label="负载状态">
            <el-tag :type="getPduLoadStatusColor(selectedDevice.latestMetric?.pduLoadStatus)">
              {{ selectedDevice.latestMetric?.pduLoadStatus }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item v-if="selectedDevice.deviceType === 'pdu'" label="输出电压">
            {{ selectedDevice.latestMetric?.pduOutputVoltage }} V
          </el-descriptions-item>
          <el-descriptions-item label="总负载率">
            <el-progress
              :percentage="Number(selectedDevice.latestMetric?.totalLoadPercent) || 0"
              :color="getLoadColor(selectedDevice.latestMetric?.totalLoadPercent)"
            />
          </el-descriptions-item>

          <!-- 环境参数 -->
          <el-descriptions-item label="环境温度">
            {{ selectedDevice.latestMetric?.ambientTemperature }} ℃
          </el-descriptions-item>
          <el-descriptions-item label="环境湿度">
            {{ selectedDevice.latestMetric?.ambientHumidity }}%
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="metricDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Monitor, CircleCheck, CircleClose, Warning, Refresh, Search } from '@element-plus/icons-vue'
import axios from 'axios'

// 响应式数据
const loading = ref(false)
const collecting = ref(false)
const deviceList = ref<any[]>([])
const deviceStats = ref({
  totalCount: 0,
  onlineCount: 0,
  offlineCount: 0
})
const alertDeviceCount = ref(0)
const metricDialogVisible = ref(false)
const selectedDevice = ref<any>(null)

const filter = reactive({
  deviceType: '',
  status: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 方法
const loadDeviceList = async () => {
  loading.value = true
  try {
    const { data } = await axios.get('/api/power-monitoring/devices/page', {
      params: {
        ...filter,
        pageNum: pagination.pageNum,
        pageSize: pagination.pageSize
      }
    })
    deviceList.value = data.records || []
    pagination.total = data.total || 0
    
    // 查询每个设备的最新指标
    for (const device of deviceList.value) {
      try {
        const metricRes = await axios.get(`/api/power-monitoring/metric/latest/${device.id}`)
        device.latestMetric = metricRes.data
        if (device.latestMetric?.isAlert) {
          alertDeviceCount.value++
        }
      } catch (e) {
        // 忽略指标查询失败
      }
    }
  } catch (error: any) {
    ElMessage.error('加载设备列表失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

const loadDeviceStats = async () => {
  try {
    const { data } = await axios.get('/api/power-monitoring/devices/statistics')
    deviceStats.value = data
  } catch (error) {
    console.error('加载设备统计失败', error)
  }
}

const handleBatchCollect = async () => {
  collecting.value = true
  try {
    const { data } = await axios.post('/api/power-monitoring/collect/batch')
    ElMessage.success(`批量采集完成，成功：${data.successCount}，失败：${data.failCount}`)
    await loadDeviceList()
  } catch (error: any) {
    ElMessage.error('批量采集失败：' + (error.message || '未知错误'))
  } finally {
    collecting.value = false
  }
}

const handleCollect = async (row: any) => {
  try {
    if (row.protocol === 'snmp') {
      await axios.post(`/api/power-monitoring/collect/snmp/${row.id}`)
    } else if (row.protocol === 'modbus') {
      await axios.post(`/api/power-monitoring/collect/modbus/${row.id}`)
    }
    ElMessage.success('数据采集成功')
    await loadDeviceList()
  } catch (error: any) {
    ElMessage.error('数据采集失败：' + (error.message || '未知错误'))
  }
}

const viewMetrics = (row: any) => {
  selectedDevice.value = row
  metricDialogVisible.value = true
}

const viewTrend = (row: any) => {
  ElMessage.info('趋势分析功能开发中...')
}

// 工具方法
const getDeviceTypeText = (type: string) => {
  const map: any = {
    main_power: '市电',
    ups: 'UPS',
    pdu: 'PDU',
    generator: '发电机'
  }
  return map[type] || type
}

const getDeviceTypeColor = (type: string) => {
  const map: any = {
    main_power: 'primary',
    ups: 'warning',
    pdu: 'success',
    generator: 'info'
  }
  return map[type] || ''
}

const getLoadColor = (percent: number) => {
  if (percent >= 90) return '#ef4444'
  if (percent >= 80) return '#f97316'
  if (percent >= 60) return '#eab308'
  return '#22c55e'
}

const getPduLoadStatusColor = (status: string) => {
  const map: any = {
    normal: 'success',
    warning: 'warning',
    overload: 'danger'
  }
  return map[status] || ''
}

// 生命周期
onMounted(() => {
  loadDeviceList()
  loadDeviceStats()
})
</script>

<style scoped>
.power-monitoring-container {
  padding: 24px;
  min-height: 100vh;
  background: linear-gradient(135deg, #0a0e27 0%, #1a1f3a 100%);
}

.page-header {
  margin-bottom: 24px;
  padding: 24px;
  background: rgba(20, 30, 60, 0.8);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 212, 255, 0.1);
}

.page-title {
  font-size: 28px;
  font-weight: bold;
  background: linear-gradient(90deg, #00d4ff, #7c3aed);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin: 0 0 8px 0;
}

.page-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
  margin: 0;
}

.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  background: rgba(20, 30, 60, 0.8);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 8px;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0, 212, 255, 0.2);
}

.stat-icon {
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  margin-right: 16px;
}

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
  margin-bottom: 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #ffffff;
}

.action-bar {
  margin-bottom: 20px;
  display: flex;
  gap: 12px;
}

.custom-table {
  background: rgba(20, 30, 60, 0.6);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 8px;
}

:deep(.el-table__inner-wrapper) {
  background: transparent;
}

:deep(.el-table th.el-table__cell) {
  background: rgba(0, 212, 255, 0.1);
  color: #00d4ff;
  border-color: rgba(0, 212, 255, 0.2);
}

:deep(.el-table td.el-table__cell) {
  border-color: rgba(0, 212, 255, 0.1);
  color: rgba(255, 255, 255, 0.9);
}

:deep(.el-table__row:hover > td) {
  background: rgba(0, 212, 255, 0.05);
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

.custom-dialog {
  background: rgba(20, 30, 60, 0.95);
}

:deep(.el-dialog__header) {
  background: rgba(0, 212, 255, 0.1);
  border-bottom: 1px solid rgba(0, 212, 255, 0.2);
}

:deep(.el-dialog__title) {
  color: #00d4ff;
  font-weight: bold;
}

:deep(.el-descriptions__label) {
  background: rgba(0, 212, 255, 0.05);
  color: #00d4ff;
}

:deep(.el-descriptions__body) {
  background: transparent;
}

:deep(.el-descriptions__content) {
  color: rgba(255, 255, 255, 0.9);
  border-color: rgba(0, 212, 255, 0.1);
}
</style>
