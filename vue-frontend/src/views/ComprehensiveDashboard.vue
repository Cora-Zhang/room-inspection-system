<template>
  <div class="comprehensive-dashboard">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">综合巡检看板</h1>
      <div class="page-actions">
        <el-button type="primary" @click="handleRefresh" :icon="Refresh">
          刷新数据
        </el-button>
      </div>
    </div>

    <!-- 统计概览 -->
    <div class="stats-container">
      <div class="stat-card alarm">
        <div class="stat-icon">🚨</div>
        <div class="stat-content">
          <div class="stat-value critical">{{ alarmStats.critical }}</div>
          <div class="stat-label">紧急告警</div>
        </div>
      </div>
      <div class="stat-card workorder">
        <div class="stat-icon">📋</div>
        <div class="stat-content">
          <div class="stat-value urgent">{{ workOrderStats.pending + workOrderStats.assigned + workOrderStats.inProgress }}</div>
          <div class="stat-label">待处理工单</div>
        </div>
      </div>
      <div class="stat-card inspection">
        <div class="stat-icon">✅</div>
        <div class="stat-content">
          <div class="stat-value">{{ inspectionStats.completionRate }}%</div>
          <div class="stat-label">巡检完成率</div>
        </div>
      </div>
      <div class="stat-card device">
        <div class="stat-icon">🖥️</div>
        <div class="stat-content">
          <div class="stat-value">{{ deviceStats.availabilityRate }}%</div>
          <div class="stat-label">设备可用率</div>
        </div>
      </div>
    </div>

    <!-- 主体内容 -->
    <div class="dashboard-content">
      <!-- 左侧：机房平面图 -->
      <div class="left-panel">
        <div class="panel-header">
          <h3>机房平面图</h3>
          <el-select v-model="selectedRoomId" placeholder="选择机房" @change="handleRoomChange" style="width: 200px">
            <el-option
              v-for="room in roomList"
              :key="room.id"
              :label="room.name"
              :value="room.id"
            />
          </el-select>
        </div>

        <div class="floor-plan-container" v-loading="floorPlanLoading">
          <div v-if="floorPlan && floorPlan.imageUrl" class="floor-plan-wrapper">
            <img :src="floorPlan.imageUrl" alt="机房平面图" class="floor-plan-image" />

            <!-- 设备标记 -->
            <div
              v-for="device in deviceLocations"
              :key="device.id"
              class="device-marker"
              :class="getDeviceStatusClass(device.status)"
              :style="{
                left: device.xCoordinate + 'px',
                top: device.yCoordinate + 'px'
              }"
              @click="handleDeviceClick(device)"
            >
              <span class="device-label">{{ device.deviceName }}</span>
            </div>

            <!-- 值班人员位置标记（基于门禁记录） -->
            <div
              v-for="staff in staffLocations"
              :key="staff.id"
              class="staff-marker"
              :style="{
                left: staff.xCoordinate + 'px',
                top: staff.yCoordinate + 'px'
              }"
            >
              <el-tooltip :content="staff.name + ' - ' + staff.role">
                <span class="staff-icon">👤</span>
              </el-tooltip>
            </div>
          </div>
          <el-empty v-else description="暂无平面图数据" />
        </div>
      </div>

      <!-- 右侧：实时监控和告警 -->
      <div class="right-panel">
        <el-tabs v-model="activeTab">
          <!-- 告警汇总 -->
          <el-tab-pane label="告警汇总" name="alarms">
            <div class="alarm-filters">
              <el-radio-group v-model="alarmFilter" size="small" @change="handleAlarmFilterChange">
                <el-radio-button label="all">全部</el-radio-button>
                <el-radio-button label="critical">紧急</el-radio-button>
                <el-radio-button label="major">重要</el-radio-button>
                <el-radio-button label="minor">一般</el-radio-button>
              </el-radio-group>
            </div>

            <div class="alarm-list" v-loading="alarmLoading">
              <div
                v-for="alarm in filteredAlarms"
                :key="alarm.id"
                class="alarm-item"
                :class="alarm.level.toLowerCase()"
              >
                <div class="alarm-header">
                  <el-tag :type="getAlarmTagType(alarm.level)" size="small">
                    {{ getAlarmLevelName(alarm.level) }}
                  </el-tag>
                  <span class="alarm-time">{{ formatTime(alarm.alarmTime) }}</span>
                </div>
                <div class="alarm-title">{{ alarm.title }}</div>
                <div class="alarm-content">{{ alarm.content }}</div>
                <div class="alarm-actions">
                  <el-button
                    v-if="alarm.status === 'ACTIVE'"
                    type="primary"
                    size="small"
                    link
                    @click="handleAcknowledge(alarm)"
                  >
                    确认告警
                  </el-button>
                  <el-button
                    type="success"
                    size="small"
                    link
                    @click="handleCreateWorkOrder(alarm)"
                  >
                    生成工单
                  </el-button>
                </div>
              </div>
              <el-empty v-if="!filteredAlarms.length" description="暂无告警数据" />
            </div>
          </el-tab-pane>

          <!-- 巡检进度 -->
          <el-tab-pane label="巡检进度" name="inspection">
            <div class="inspection-progress">
              <div class="progress-item">
                <div class="progress-label">今日巡检进度</div>
                <el-progress :percentage="inspectionStats.completionRate" :stroke-width="12" />
                <div class="progress-detail">
                  已完成 {{ inspectionStats.completed }} / {{ inspectionStats.total }}
                </div>
              </div>

              <div class="inspection-list" v-loading="inspectionLoading">
                <div
                  v-for="inspection in inspectionList"
                  :key="inspection.id"
                  class="inspection-item"
                  :class="inspection.status.toLowerCase()"
                >
                  <div class="inspection-header">
                    <span class="inspection-title">{{ inspection.title }}</span>
                    <el-tag :type="getInspectionTagType(inspection.status)" size="small">
                      {{ getInspectionStatusName(inspection.status) }}
                    </el-tag>
                  </div>
                  <div class="inspection-info">
                    <span>负责人: {{ inspection.ownerName }}</span>
                    <span>预计时间: {{ formatDateTime(inspection.expectedTime) }}</span>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

// 数据状态
const loading = ref(false)
const floorPlanLoading = ref(false)
const alarmLoading = ref(false)
const inspectionLoading = ref(false)

// 统计数据
const alarmStats = reactive({
  critical: 0,
  major: 0,
  minor: 0,
  active: 0
})

const workOrderStats = reactive({
  pending: 0,
  assigned: 0,
  inProgress: 0,
  completed: 0,
  overdue: 0
})

const inspectionStats = reactive({
  total: 0,
  completed: 0,
  completionRate: 0
})

const deviceStats = reactive({
  total: 0,
  normal: 0,
  warning: 0,
  error: 0,
  availabilityRate: 0
})

// 机房数据
const roomList = ref<any[]>([])
const selectedRoomId = ref('')

// 平面图数据
const floorPlan = ref<any>(null)
const deviceLocations = ref<any[]>([])
const staffLocations = ref<any[]>([])

// 告警数据
const activeTab = ref('alarms')
const alarmFilter = ref('all')
const alarmList = ref<any[]>([])

const filteredAlarms = computed(() => {
  if (alarmFilter.value === 'all') {
    return alarmList.value
  }
  return alarmList.value.filter(alarm => alarm.level.toLowerCase() === alarmFilter.value)
})

// 巡检数据
const inspectionList = ref<any[]>([])

// 初始化
onMounted(() => {
  initData()
})

// 初始化数据
const initData = () => {
  fetchRoomList()
  fetchStatistics()
  fetchAlarms()
  fetchInspections()
}

// 获取机房列表
const fetchRoomList = async () => {
  try {
    // 模拟数据
    roomList.value = [
      { id: 'R001', name: '数据中心机房A' },
      { id: 'R002', name: '数据中心机房B' }
    ]
    if (roomList.value.length > 0) {
      selectedRoomId.value = roomList.value[0].id
      handleRoomChange(selectedRoomId.value)
    }
  } catch (error) {
    console.error('获取机房列表失败:', error)
  }
}

// 获取统计数据
const fetchStatistics = async () => {
  try {
    // 模拟数据
    Object.assign(alarmStats, {
      critical: 2,
      major: 5,
      minor: 8,
      active: 15
    })

    Object.assign(workOrderStats, {
      pending: 3,
      assigned: 5,
      inProgress: 4,
      completed: 25,
      overdue: 2
    })

    Object.assign(inspectionStats, {
      total: 8,
      completed: 6,
      completionRate: 75
    })

    Object.assign(deviceStats, {
      total: 50,
      normal: 46,
      warning: 3,
      error: 1,
      availabilityRate: 98
    })
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 获取告警列表
const fetchAlarms = async () => {
  alarmLoading.value = true
  try {
    const response = await axios.get('/api/alarms/unresolved')
    alarmList.value = response.data.data || []
  } catch (error) {
    console.error('获取告警列表失败:', error)
    // 模拟数据
    alarmList.value = [
      {
        id: 'ALM001',
        level: 'CRITICAL',
        title: '服务器CPU温度过高',
        content: '服务器001 CPU温度达到85℃，超过阈值75℃',
        alarmTime: new Date(),
        status: 'ACTIVE'
      },
      {
        id: 'ALM002',
        level: 'MAJOR',
        title: '空调回风温度异常',
        content: '空调001回风温度为26℃，设定温度22℃',
        alarmTime: new Date(),
        status: 'ACTIVE'
      }
    ]
  } finally {
    alarmLoading.value = false
  }
}

// 获取巡检进度
const fetchInspections = async () => {
  inspectionLoading.value = true
  try {
    // 模拟数据
    inspectionList.value = [
      {
        id: 'INS001',
        title: '服务器区域例行巡检',
        status: 'COMPLETED',
        ownerName: '张三',
        expectedTime: new Date()
      },
      {
        id: 'INS002',
        title: '网络设备巡检',
        status: 'IN_PROGRESS',
        ownerName: '李四',
        expectedTime: new Date()
      }
    ]
  } catch (error) {
    console.error('获取巡检进度失败:', error)
  } finally {
    inspectionLoading.value = false
  }
}

// 机房切换
const handleRoomChange = async (roomId: string) => {
  await fetchFloorPlan(roomId)
  await fetchDeviceLocations(roomId)
}

// 获取平面图
const fetchFloorPlan = async (roomId: string) => {
  floorPlanLoading.value = true
  try {
    const response = await axios.get('/api/dashboard/main-floor-plan', {
      params: { roomId }
    })
    floorPlan.value = response.data.data

    // 模拟值班人员位置
    staffLocations.value = [
      { id: 'S001', name: '张三', role: '值班员', xCoordinate: 300, yCoordinate: 200 }
    ]
  } catch (error) {
    console.error('获取平面图失败:', error)
  } finally {
    floorPlanLoading.value = false
  }
}

// 获取设备位置
const fetchDeviceLocations = async (roomId: string) => {
  try {
    const response = await axios.get('/api/device-locations', {
      params: { roomId }
    })
    deviceLocations.value = response.data.data || []
  } catch (error) {
    console.error('获取设备位置失败:', error)
  }
}

// 工具函数
const getDeviceStatusClass = (status: string) => {
  const statusMap: Record<string, string> = {
    NORMAL: 'normal',
    WARNING: 'warning',
    ERROR: 'error',
    OFFLINE: 'offline'
  }
  return statusMap[status] || 'normal'
}

const getAlarmTagType = (level: string) => {
  const levelMap: Record<string, string> = {
    CRITICAL: 'danger',
    MAJOR: 'warning',
    MINOR: 'info'
  }
  return levelMap[level] || 'info'
}

const getAlarmLevelName = (level: string) => {
  const levelMap: Record<string, string> = {
    CRITICAL: '紧急',
    MAJOR: '重要',
    MINOR: '一般'
  }
  return levelMap[level] || level
}

const getInspectionTagType = (status: string) => {
  const statusMap: Record<string, string> = {
    COMPLETED: 'success',
    IN_PROGRESS: 'primary',
    PENDING: 'warning'
  }
  return statusMap[status] || 'info'
}

const getInspectionStatusName = (status: string) => {
  const statusMap: Record<string, string> = {
    COMPLETED: '已完成',
    IN_PROGRESS: '进行中',
    PENDING: '待开始'
  }
  return statusMap[status] || status
}

const formatTime = (time: any) => {
  if (!time) return ''
  return new Date(time).toLocaleTimeString()
}

const formatDateTime = (time: any) => {
  if (!time) return ''
  return new Date(time).toLocaleString()
}

// 事件处理
const handleRefresh = () => {
  initData()
}

const handleDeviceClick = (device: any) => {
  console.log('点击设备:', device)
  ElMessage.info(`设备: ${device.deviceName}`)
}

const handleAcknowledge = async (alarm: any) => {
  try {
    await axios.put(`/api/alarms/${alarm.id}/acknowledge`, {
      acknowledgedBy: 'U001',
      acknowledgedByName: '张三'
    })
    ElMessage.success('告警已确认')
    fetchAlarms()
  } catch (error) {
    console.error('确认告警失败:', error)
    ElMessage.error('确认告警失败')
  }
}

const handleCreateWorkOrder = async (alarm: any) => {
  try {
    const response = await axios.post('/api/work-orders/auto-create', {
      alarmId: alarm.id
    })
    ElMessage.success('工单创建成功')
    fetchStatistics()
  } catch (error) {
    console.error('创建工单失败:', error)
    ElMessage.error('创建工单失败')
  }
}

const handleAlarmFilterChange = () => {
  // 过滤逻辑在 computed 中处理
}
</script>

<style scoped lang="scss">
.comprehensive-dashboard {
  padding: 20px;
  background: linear-gradient(135deg, #0c1929 0%, #1a2332 100%);
  min-height: 100vh;
  color: #e0e6ed;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  background: linear-gradient(90deg, #00f0ff, #00d4ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.stats-container {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 32px rgba(0, 240, 255, 0.2);
  }
}

.stat-icon {
  font-size: 32px;
  margin-right: 16px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 4px;
}

.stat-value.critical {
  color: #ff4d4f;
}

.stat-value.urgent {
  color: #faad14;
}

.stat-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
}

.dashboard-content {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
}

.left-panel, .right-panel {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 20px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.panel-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #00f0ff;
}

.floor-plan-container {
  min-height: 500px;
  position: relative;
}

.floor-plan-wrapper {
  position: relative;
  width: 100%;
  border-radius: 8px;
  overflow: hidden;
}

.floor-plan-image {
  width: 100%;
  height: auto;
  display: block;
}

.device-marker {
  position: absolute;
  transform: translate(-50%, -50%);
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  z-index: 10;

  &.normal {
    background: rgba(0, 255, 128, 0.3);
    border: 2px solid #00ff80;
    box-shadow: 0 0 10px rgba(0, 255, 128, 0.5);
  }

  &.warning {
    background: rgba(255, 165, 0, 0.3);
    border: 2px solid #ffa500;
    box-shadow: 0 0 10px rgba(255, 165, 0, 0.5);
  }

  &.error {
    background: rgba(255, 77, 79, 0.3);
    border: 2px solid #ff4d4f;
    box-shadow: 0 0 10px rgba(255, 77, 79, 0.5);
    animation: pulse 2s infinite;
  }

  &:hover {
    transform: translate(-50%, -50%) scale(1.2);
    z-index: 20;
  }
}

@keyframes pulse {
  0%, 100% {
    box-shadow: 0 0 10px rgba(255, 77, 79, 0.5);
  }
  50% {
    box-shadow: 0 0 20px rgba(255, 77, 79, 0.8);
  }
}

.device-label {
  font-size: 10px;
  color: #fff;
  text-align: center;
  white-space: nowrap;
}

.staff-marker {
  position: absolute;
  transform: translate(-50%, -50%);
  font-size: 24px;
  cursor: pointer;
  z-index: 15;
  animation: bounce 2s infinite;
}

@keyframes bounce {
  0%, 100% {
    transform: translate(-50%, -50%);
  }
  50% {
    transform: translate(-50%, -60%);
  }
}

.alarm-filters {
  margin-bottom: 16px;
}

.alarm-list {
  max-height: 600px;
  overflow-y: auto;
}

.alarm-item {
  padding: 16px;
  margin-bottom: 12px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  transition: all 0.3s ease;

  &:hover {
    background: rgba(255, 255, 255, 0.08);
  }

  &.critical {
    border-left: 4px solid #ff4d4f;
  }

  &.major {
    border-left: 4px solid #faad14;
  }

  &.minor {
    border-left: 4px solid #1890ff;
  }
}

.alarm-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.alarm-time {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
}

.alarm-title {
  font-weight: 600;
  margin-bottom: 8px;
}

.alarm-content {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 12px;
}

.alarm-actions {
  display: flex;
  gap: 12px;
}

.inspection-progress {
  padding: 16px;
}

.progress-item {
  margin-bottom: 24px;
}

.progress-label {
  margin-bottom: 8px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
}

.progress-detail {
  margin-top: 8px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
}

.inspection-list {
  max-height: 500px;
  overflow-y: auto;
}

.inspection-item {
  padding: 16px;
  margin-bottom: 12px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
}

.inspection-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.inspection-title {
  font-weight: 600;
}

.inspection-info {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
}

// 滚动条样式
.alarm-list::-webkit-scrollbar,
.inspection-list::-webkit-scrollbar {
  width: 6px;
}

.alarm-list::-webkit-scrollbar-thumb,
.inspection-list::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 3px;
}

.alarm-list::-webkit-scrollbar-track,
.inspection-list::-webkit-scrollbar-track {
  background: transparent;
}
</style>
