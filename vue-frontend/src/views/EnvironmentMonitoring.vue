<template>
  <div class="environment-monitoring-container">
    <!-- 头部统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <div class="stat-card temperature">
          <div class="stat-icon">🌡️</div>
          <div class="stat-info">
            <div class="stat-label">平均温度</div>
            <div class="stat-value">{{ stats.avgTemperature }}°C</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card humidity">
          <div class="stat-icon">💧</div>
          <div class="stat-info">
            <div class="stat-label">平均湿度</div>
            <div class="stat-value">{{ stats.avgHumidity }}%</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card normal">
          <div class="stat-icon">✅</div>
          <div class="stat-info">
            <div class="stat-label">正常传感器</div>
            <div class="stat-value">{{ stats.normalCount }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card alarm">
          <div class="stat-icon">⚠️</div>
          <div class="stat-info">
            <div class="stat-label">告警传感器</div>
            <div class="stat-value">{{ stats.alarmCount }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 主要内容区域 -->
    <el-row :gutter="20" class="main-content">
      <!-- 左侧：热力图 -->
      <el-col :span="16">
        <div class="panel heatmap-panel">
          <div class="panel-header">
            <h3>温湿度热力图</h3>
            <div class="header-actions">
              <el-radio-group v-model="heatmapType" size="small" @change="generateHeatmap">
                <el-radio-button label="TEMPERATURE">温度</el-radio-button>
                <el-radio-button label="HUMIDITY">湿度</el-radio-button>
              </el-radio-group>
              <el-button type="primary" size="small" @click="generateHeatmap">刷新</el-button>
            </div>
          </div>
          <div class="panel-body">
            <div ref="heatmapContainer" class="heatmap-container"></div>
            <div class="heatmap-legend">
              <div class="legend-title">{{ heatmapType === 'TEMPERATURE' ? '温度 (°C)' : '湿度 (%)' }}</div>
              <div class="legend-gradient"></div>
              <div class="legend-values">
                <span>{{ heatmapType === 'TEMPERATURE' ? '18' : '40' }}</span>
                <span>{{ heatmapType === 'TEMPERATURE' ? '24' : '55' }}</span>
                <span>{{ heatmapType === 'TEMPERATURE' ? '30' : '70' }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 传感器列表 -->
        <div class="panel sensor-list-panel">
          <div class="panel-header">
            <h3>传感器列表</h3>
            <div class="header-actions">
              <el-button type="primary" size="small" @click="handleAddSensor">添加传感器</el-button>
              <el-button size="small" @click="batchCollectData">批量采集</el-button>
            </div>
          </div>
          <div class="panel-body">
            <el-table :data="sensorList" style="width: 100%" :header-cell-style="{ background: '#1a1a2e', color: '#00d4ff' }">
              <el-table-column prop="sensorName" label="传感器名称" width="150"></el-table-column>
              <el-table-column prop="sensorType" label="类型" width="100">
                <template #default="scope">
                  <el-tag v-if="scope.row.sensorType === 'TEMPERATURE'" type="success">温湿度</el-tag>
                  <el-tag v-else-if="scope.row.sensorType === 'WATER'" type="warning">漏水</el-tag>
                  <el-tag v-else-if="scope.row.sensorType === 'SMOKE'" type="danger">烟感</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="location" label="位置" width="150"></el-table-column>
              <el-table-column label="当前值" width="120">
                <template #default="scope">
                  <span :class="{'value-abnormal': scope.row.currentValue && scope.row.isAlarm}">
                    {{ scope.row.currentValue }}{{ scope.row.unit }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="100">
                <template #default="scope">
                  <el-tag v-if="scope.row.status === 'NORMAL'" type="success">正常</el-tag>
                  <el-tag v-else-if="scope.row.status === 'ALARM'" type="danger">告警</el-tag>
                  <el-tag v-else type="info">离线</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="200">
                <template #default="scope">
                  <el-button type="text" size="small" @click="handleViewDetail(scope.row)">详情</el-button>
                  <el-button type="text" size="small" @click="handleEditSensor(scope.row)">编辑</el-button>
                  <el-button type="text" size="small" @click="handleCollectData(scope.row)">采集</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </el-col>

      <!-- 右侧：异常区域与趋势图 -->
      <el-col :span="8">
        <!-- 异常区域 -->
        <div class="panel abnormal-panel">
          <div class="panel-header">
            <h3>异常区域</h3>
          </div>
          <div class="panel-body">
            <div v-if="abnormalAreas.length === 0" class="no-data">
              <div class="no-data-icon">✓</div>
              <div class="no-data-text">暂无异常区域</div>
            </div>
            <div v-else class="abnormal-list">
              <div v-for="area in abnormalAreas" :key="area.id" class="abnormal-item">
                <div class="abnormal-icon">{{ area.abnormalType === 'HIGH_TEMP' ? '🔥' : area.abnormalType === 'LOW_TEMP' ? '❄️' : area.abnormalType === 'HIGH_HUMIDITY' ? '💧' : '🌫️' }}</div>
                <div class="abnormal-info">
                  <div class="abnormal-type">{{ getAbnormalTypeName(area.abnormalType) }}</div>
                  <div class="abnormal-value">坐标: ({{ area.coordinateX }}, {{ area.coordinateY }})</div>
                  <div class="abnormal-value">数值: {{ area.value }}{{ heatmapType === 'TEMPERATURE' ? '°C' : '%' }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 数据趋势图 -->
        <div class="panel trend-panel">
          <div class="panel-header">
            <h3>数据趋势</h3>
            <el-select v-model="trendDataType" size="small" @change="loadTrendData">
              <el-option label="温度" value="TEMPERATURE"></el-option>
              <el-option label="湿度" value="HUMIDITY"></el-option>
            </el-select>
          </div>
          <div class="panel-body">
            <div ref="trendChart" class="trend-chart"></div>
          </div>
        </div>

        <!-- 告警记录 -->
        <div class="panel alarm-panel">
          <div class="panel-header">
            <h3>告警记录</h3>
            <el-button type="text" size="small" @click="loadAlarmData">更多</el-button>
          </div>
          <div class="panel-body">
            <div v-if="alarmList.length === 0" class="no-data">
              <div class="no-data-icon">✓</div>
              <div class="no-data-text">暂无告警记录</div>
            </div>
            <div v-else class="alarm-list">
              <div v-for="alarm in alarmList" :key="alarm.id" class="alarm-item">
                <div class="alarm-time">{{ formatDate(alarm.collectTime) }}</div>
                <div class="alarm-message">{{ alarm.alarmMessage }}</div>
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 传感器对话框 -->
    <el-dialog v-model="sensorDialogVisible" :title="sensorDialogTitle" width="600px" class="sci-fi-dialog">
      <el-form :model="sensorForm" label-width="120px">
        <el-form-item label="传感器名称">
          <el-input v-model="sensorForm.sensorName" placeholder="请输入传感器名称"></el-input>
        </el-form-item>
        <el-form-item label="传感器类型">
          <el-select v-model="sensorForm.sensorType" placeholder="请选择类型">
            <el-option label="温湿度传感器" value="TEMPERATURE"></el-option>
            <el-option label="漏水检测器" value="WATER"></el-option>
            <el-option label="烟感传感器" value="SMOKE"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="机房">
          <el-select v-model="sensorForm.roomId" placeholder="请选择机房">
            <el-option label="机房A-101" :value="1"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="位置">
          <el-input v-model="sensorForm.location" placeholder="请输入安装位置"></el-input>
        </el-form-item>
        <el-form-item label="坐标X">
          <el-input-number v-model="sensorForm.coordinateX" :min="0" :max="1000"></el-input-number>
        </el-form-item>
        <el-form-item label="坐标Y">
          <el-input-number v-model="sensorForm.coordinateY" :min="0" :max="1000"></el-input-number>
        </el-form-item>
        <el-form-item label="IP地址">
          <el-input v-model="sensorForm.ipAddress" placeholder="192.168.1.200"></el-input>
        </el-form-item>
        <el-form-item label="协议类型">
          <el-select v-model="sensorForm.protocolType" placeholder="请选择协议">
            <el-option label="Modbus" value="MODBUS"></el-option>
            <el-option label="SNMP" value="SNMP"></el-option>
            <el-option label="BACnet" value="BACNET"></el-option>
            <el-option label="HTTP" value="HTTP"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="采集间隔(秒)">
          <el-input-number v-model="sensorForm.collectInterval" :min="10" :max="3600"></el-input-number>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="sensorDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveSensor">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

// 响应式数据
const stats = reactive({
  avgTemperature: 22.5,
  avgHumidity: 55.0,
  normalCount: 4,
  alarmCount: 1
})

const heatmapType = ref('TEMPERATURE')
const sensorList = ref([])
const abnormalAreas = ref([])
const alarmList = ref([])
const trendDataType = ref('TEMPERATURE')

// 对话框
const sensorDialogVisible = ref(false)
const sensorDialogTitle = ref('添加传感器')
const sensorForm = reactive({
  id: null,
  sensorName: '',
  sensorType: 'TEMPERATURE',
  roomId: 1,
  location: '',
  coordinateX: 0,
  coordinateY: 0,
  ipAddress: '',
  protocolType: 'MODBUS',
  collectInterval: 300
})

// 引用
const heatmapContainer = ref(null)
const trendChart = ref(null)

// 生成热力图
const generateHeatmap = async () => {
  try {
    const roomId = 1
    const response = await axios.get(`/api/environment/heatmap/generate?roomId=${roomId}&heatmapType=${heatmapType.value}`)
    if (response.data.success) {
      const data = response.data.data
      renderHeatmap(data)
      
      // 同时加载异常区域
      loadAbnormalAreas()
    }
  } catch (error) {
    ElMessage.error('生成热力图失败')
  }
}

// 加载异常区域
const loadAbnormalAreas = async () => {
  try {
    const roomId = 1
    const response = await axios.get(`/api/environment/heatmap/abnormal?roomId=${roomId}&heatmapType=${heatmapType.value}`)
    if (response.data.success) {
      abnormalAreas.value = response.data.data
    }
  } catch (error) {
    console.error('加载异常区域失败', error)
  }
}

// 渲染热力图（简化版，实际项目中可使用ECharts或专门的Heatmap库）
const renderHeatmap = (data: any[]) => {
  // 这里简化处理，实际项目应使用ECharts Heatmap或其他热力图库
  const container = heatmapContainer.value
  if (!container) return
  
  container.innerHTML = `
    <div class="heatmap-placeholder">
      <div class="heatmap-text">热力图数据已加载 (${data.length} 个数据点)</div>
      <div class="heatmap-visual">
        ${data.map(point => `
          <div class="heatmap-point" style="
            left: ${point.coordinateX}px;
            top: ${point.coordinateY}px;
            background-color: ${getHeatmapColor(point.heatValue)};
            opacity: 0.6;
          "></div>
        `).join('')}
      </div>
    </div>
  `
}

// 获取热力图颜色
const getHeatmapColor = (value: number) => {
  if (heatmapType.value === 'TEMPERATURE') {
    // 温度：蓝 -> 绿 -> 黄 -> 红
    if (value < 0.25) return `rgba(0, 100, 255, ${0.4 + value})`
    if (value < 0.5) return `rgba(0, 255, 0, ${0.4 + value})`
    if (value < 0.75) return `rgba(255, 255, 0, ${0.4 + value})`
    return `rgba(255, 0, 0, ${0.4 + value})`
  } else {
    // 湿度：蓝 -> 浅蓝 -> 白
    if (value < 0.5) return `rgba(0, 100, 255, ${0.3 + value})`
    return `rgba(135, 206, 250, ${0.3 + value})`
  }
}

// 加载传感器列表
const loadSensorList = async () => {
  try {
    const response = await axios.get('/api/environment/sensor/list')
    if (response.data.success) {
      sensorList.value = response.data.data.map((sensor: any) => ({
        ...sensor,
        currentValue: null,
        unit: sensor.sensorType === 'TEMPERATURE' ? '°C' : '%',
        isAlarm: false
      }))
      
      // 加载各传感器最新数据
      loadSensorsLatestData()
    }
  } catch (error) {
    ElMessage.error('加载传感器列表失败')
  }
}

// 加载传感器最新数据
const loadSensorsLatestData = async () => {
  for (const sensor of sensorList.value) {
    try {
      const response = await axios.get(`/api/environment/sensor/${sensor.id}/latest-data`)
      if (response.data.success) {
        const data = response.data.data.dataList || []
        const tempData = data.find((d: any) => d.dataType === 'TEMPERATURE')
        if (tempData) {
          sensor.currentValue = tempData.value.toFixed(1)
          sensor.unit = tempData.unit
          sensor.isAlarm = tempData.isAlarm === 1
        }
      }
    } catch (error) {
      console.error(`加载传感器 ${sensor.id} 数据失败`, error)
    }
  }
}

// 批量采集数据
const batchCollectData = async () => {
  try {
    const response = await axios.post('/api/environment/sensor/batch-collect')
    if (response.data.success) {
      ElMessage.success('批量采集成功')
      setTimeout(() => {
        loadSensorList()
        generateHeatmap()
      }, 1000)
    }
  } catch (error) {
    ElMessage.error('批量采集失败')
  }
}

// 单个采集数据
const handleCollectData = async (sensor: any) => {
  try {
    const response = await axios.post(`/api/environment/sensor/collect/${sensor.id}`)
    if (response.data.success) {
      ElMessage.success('采集成功')
      setTimeout(() => loadSensorsLatestData(), 1000)
    }
  } catch (error) {
    ElMessage.error('采集失败')
  }
}

// 添加传感器
const handleAddSensor = () => {
  sensorDialogTitle.value = '添加传感器'
  Object.assign(sensorForm, {
    id: null,
    sensorName: '',
    sensorType: 'TEMPERATURE',
    roomId: 1,
    location: '',
    coordinateX: 0,
    coordinateY: 0,
    ipAddress: '',
    protocolType: 'MODBUS',
    collectInterval: 300
  })
  sensorDialogVisible.value = true
}

// 编辑传感器
const handleEditSensor = (sensor: any) => {
  sensorDialogTitle.value = '编辑传感器'
  Object.assign(sensorForm, sensor)
  sensorDialogVisible.value = true
}

// 保存传感器
const handleSaveSensor = async () => {
  try {
    const response = sensorForm.id
      ? await axios.put('/api/environment/sensor', sensorForm)
      : await axios.post('/api/environment/sensor', sensorForm)
    
    if (response.data.success) {
      ElMessage.success(sensorForm.id ? '更新成功' : '添加成功')
      sensorDialogVisible.value = false
      loadSensorList()
    }
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

// 查看详情
const handleViewDetail = (sensor: any) => {
  ElMessage.info('功能开发中...')
}

// 加载告警记录
const loadAlarmData = async () => {
  try {
    const roomId = 1
    const endTime = new Date()
    const startTime = new Date(endTime.getTime() - 24 * 60 * 60 * 1000)
    
    const response = await axios.get('/api/environment/data/alarm', {
      params: {
        roomId,
        startTime: startTime.toISOString().slice(0, 19).replace('T', ' '),
        endTime: endTime.toISOString().slice(0, 19).replace('T', ' ')
      }
    })
    
    if (response.data.success) {
      alarmList.value = response.data.data.slice(0, 5)
    }
  } catch (error) {
    console.error('加载告警记录失败', error)
  }
}

// 加载趋势数据
const loadTrendData = async () => {
  // TODO: 实现趋势图渲染
  console.log('加载趋势数据', trendDataType.value)
}

// 格式化日期
const formatDate = (dateStr: string) => {
  const date = new Date(dateStr)
  return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

// 获取异常类型名称
const getAbnormalTypeName = (type: string) => {
  const map: Record<string, string> = {
    HIGH_TEMP: '高温',
    LOW_TEMP: '低温',
    HIGH_HUMIDITY: '高湿',
    LOW_HUMIDITY: '低湿'
  }
  return map[type] || type
}

// 定时刷新
let refreshTimer: number | null = null

onMounted(() => {
  loadSensorList()
  generateHeatmap()
  loadAlarmData()
  
  // 定时刷新（每30秒）
  refreshTimer = window.setInterval(() => {
    loadSensorsLatestData()
    generateHeatmap()
  }, 30000)
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style scoped>
.environment-monitoring-container {
  padding: 20px;
  background: linear-gradient(135deg, #0f0c29 0%, #302b63 50%, #24243e 100%);
  min-height: calc(100vh - 120px);
}

/* 统计卡片 */
.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  background: rgba(26, 26, 46, 0.8);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.3);
}

.stat-card.temperature {
  border-left: 3px solid #ff6b6b;
}

.stat-card.humidity {
  border-left: 3px solid #4ecdc4;
}

.stat-card.normal {
  border-left: 3px solid #51cf66;
}

.stat-card.alarm {
  border-left: 3px solid #ffd93d;
}

.stat-icon {
  font-size: 36px;
  margin-right: 15px;
}

.stat-info {
  flex: 1;
}

.stat-label {
  color: #a0a0c0;
  font-size: 14px;
  margin-bottom: 5px;
}

.stat-value {
  color: #00d4ff;
  font-size: 24px;
  font-weight: bold;
  text-shadow: 0 0 10px rgba(0, 212, 255, 0.5);
}

/* 面板样式 */
.main-content {
  margin-top: 20px;
}

.panel {
  background: rgba(26, 26, 46, 0.8);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 8px;
  margin-bottom: 20px;
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.3);
}

.panel-header {
  padding: 15px 20px;
  border-bottom: 1px solid rgba(0, 212, 255, 0.2);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.panel-header h3 {
  margin: 0;
  color: #00d4ff;
  font-size: 16px;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.panel-body {
  padding: 20px;
}

/* 热力图容器 */
.heatmap-container {
  height: 400px;
  position: relative;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 4px;
  overflow: hidden;
}

.heatmap-placeholder {
  width: 100%;
  height: 100%;
  position: relative;
}

.heatmap-visual {
  width: 100%;
  height: 100%;
  position: relative;
}

.heatmap-point {
  position: absolute;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  filter: blur(10px);
}

.heatmap-legend {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 15px;
  padding: 10px;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 4px;
}

.legend-title {
  color: #a0a0c0;
  font-size: 12px;
  margin-bottom: 8px;
}

.legend-gradient {
  width: 200px;
  height: 10px;
  background: linear-gradient(to right, #0064ff, #00ff00, #ffff00, #ff0000);
  border-radius: 5px;
}

.legend-values {
  display: flex;
  justify-content: space-between;
  width: 200px;
  margin-top: 5px;
  color: #a0a0c0;
  font-size: 11px;
}

/* 异常区域 */
.abnormal-list {
  max-height: 300px;
  overflow-y: auto;
}

.abnormal-item {
  display: flex;
  align-items: center;
  padding: 12px;
  margin-bottom: 10px;
  background: rgba(255, 107, 107, 0.1);
  border: 1px solid rgba(255, 107, 107, 0.3);
  border-radius: 6px;
}

.abnormal-icon {
  font-size: 28px;
  margin-right: 12px;
}

.abnormal-info {
  flex: 1;
}

.abnormal-type {
  color: #ff6b6b;
  font-weight: bold;
  margin-bottom: 4px;
}

.abnormal-value {
  color: #a0a0c0;
  font-size: 12px;
  margin-bottom: 2px;
}

/* 趋势图 */
.trend-chart {
  height: 250px;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #a0a0c0;
}

/* 告警记录 */
.alarm-list {
  max-height: 250px;
  overflow-y: auto;
}

.alarm-item {
  padding: 10px;
  margin-bottom: 8px;
  background: rgba(255, 217, 61, 0.1);
  border-left: 3px solid #ffd93d;
  border-radius: 4px;
}

.alarm-time {
  color: #a0a0c0;
  font-size: 12px;
  margin-bottom: 4px;
}

.alarm-message {
  color: #ffd93d;
  font-size: 13px;
}

/* 空数据 */
.no-data {
  text-align: center;
  padding: 40px 20px;
  color: #51cf66;
}

.no-data-icon {
  font-size: 48px;
  margin-bottom: 10px;
  opacity: 0.5;
}

.no-data-text {
  color: #a0a0c0;
  font-size: 14px;
}

/* 值异常样式 */
.value-abnormal {
  color: #ff6b6b !important;
  font-weight: bold;
}

/* 对话框样式 */
:deep(.sci-fi-dialog) {
  background: rgba(26, 26, 46, 0.95);
  border: 1px solid rgba(0, 212, 255, 0.5);
}

:deep(.sci-fi-dialog .el-dialog__header) {
  background: linear-gradient(90deg, rgba(0, 212, 255, 0.2), transparent);
  border-bottom: 1px solid rgba(0, 212, 255, 0.3);
}

:deep(.sci-fi-dialog .el-dialog__title) {
  color: #00d4ff;
}

:deep(.sci-fi-dialog .el-form-item__label) {
  color: #a0a0c0;
}

/* 滚动条样式 */
:deep(.el-table__body-wrapper::-webkit-scrollbar),
.abnormal-list::-webkit-scrollbar,
.alarm-list::-webkit-scrollbar {
  width: 6px;
}

:deep(.el-table__body-wrapper::-webkit-scrollbar-thumb),
.abnormal-list::-webkit-scrollbar-thumb,
.alarm-list::-webkit-scrollbar-thumb {
  background: rgba(0, 212, 255, 0.3);
  border-radius: 3px;
}

:deep(.el-table__body-wrapper::-webkit-scrollbar-track),
.abnormal-list::-webkit-scrollbar-track,
.alarm-list::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.2);
}
</style>
