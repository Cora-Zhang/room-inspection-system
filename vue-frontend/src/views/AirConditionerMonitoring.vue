<template>
  <div class="air-conditioner-monitoring">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">精密空调监控</h1>
      <div class="page-actions">
        <el-button type="primary" @click="handleBatchCollect" :icon="Refresh">
          批量采集数据
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-container">
      <div class="stat-card total">
        <div class="stat-icon">🖥️</div>
        <div class="stat-content">
          <div class="stat-value">{{ statistics.total }}</div>
          <div class="stat-label">空调总数</div>
        </div>
      </div>
      <div class="stat-card online">
        <div class="stat-icon">🟢</div>
        <div class="stat-content">
          <div class="stat-value">{{ statistics.online }}</div>
          <div class="stat-label">在线运行</div>
        </div>
      </div>
      <div class="stat-card running">
        <div class="stat-icon">❄️</div>
        <div class="stat-content">
          <div class="stat-value">{{ statistics.running }}</div>
          <div class="stat-label">制冷中</div>
        </div>
      </div>
      <div class="stat-card alarm">
        <div class="stat-icon">🚨</div>
        <div class="stat-content">
          <div class="stat-value">{{ statistics.alarm }}</div>
          <div class="stat-label">告警状态</div>
        </div>
      </div>
    </div>

    <!-- 标签页 -->
    <el-tabs v-model="activeTab" class="monitoring-tabs">
      <el-tab-pane label="空调列表" name="list">
        <!-- 查询表单 -->
        <div class="search-form">
          <el-form :inline="true" :model="queryParams">
            <el-form-item label="机房">
              <el-select v-model="queryParams.roomId" placeholder="请选择机房" clearable>
                <el-option
                  v-for="room in roomList"
                  :key="room.id"
                  :label="room.name"
                  :value="room.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">查询</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 数据表格 -->
        <el-table :data="acList" border stripe style="width: 100%" v-loading="loading">
          <el-table-column prop="acCode" label="空调编号" width="120" />
          <el-table-column prop="acName" label="空调名称" width="150" />
          <el-table-column prop="location" label="安装位置" width="150" />
          <el-table-column prop="brand" label="品牌" width="100" />
          <el-table-column prop="model" label="型号" width="120" />
          <el-table-column label="运行模式" width="100">
            <template #default="{ row }">
              <el-tag :type="row.runMode === 1 ? 'success' : 'info'">
                {{ getRunModeName(row.runMode) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="设定温度(℃)" width="100" prop="setTemperature" />
          <el-table-column label="回风温度(℃)" width="100" prop="currentReturnTemp" />
          <el-table-column label="回风湿度(%)" width="100" prop="currentReturnHumidity" />
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 0 ? 'success' : 'danger'">
                {{ row.status === 0 ? '正常' : '告警' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="250" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" @click="handleViewDetail(row)">
                查看详情
              </el-button>
              <el-button type="success" size="small" @click="handleCollect(row)">
                采集数据
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <el-pagination
          v-model:current-page="queryParams.current"
          v-model:page-size="queryParams.size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSearch"
          @current-change="handleSearch"
          class="pagination"
        />
      </el-tab-pane>

      <el-tab-pane label="效率核查工单" name="orders">
        <EfficiencyCheckOrders />
      </el-tab-pane>
    </el-tabs>

    <!-- 空调详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="空调详情"
      width="80%"
      class="detail-dialog"
    >
      <div v-if="currentAc" class="detail-content">
        <!-- 基本信息 -->
        <div class="detail-section">
          <h3 class="section-title">基本信息</h3>
          <el-descriptions :column="3" border>
            <el-descriptions-item label="空调编号">
              {{ currentAc.acCode }}
            </el-descriptions-item>
            <el-descriptions-item label="空调名称">
              {{ currentAc.acName }}
            </el-descriptions-item>
            <el-descriptions-item label="安装位置">
              {{ currentAc.location }}
            </el-descriptions-item>
            <el-descriptions-item label="品牌">
              {{ currentAc.brand }}
            </el-descriptions-item>
            <el-descriptions-item label="型号">
              {{ currentAc.model }}
            </el-descriptions-item>
            <el-descriptions-item label="制冷量(kW)">
              {{ currentAc.coolingCapacity }}
            </el-descriptions-item>
            <el-descriptions-item label="设定温度(℃)">
              {{ currentAc.setTemperature }}
            </el-descriptions-item>
            <el-descriptions-item label="当前回风温度(℃)">
              {{ currentAc.currentReturnTemp }}
            </el-descriptions-item>
            <el-descriptions-item label="当前回风湿度(%)">
              {{ currentAc.currentReturnHumidity }}
            </el-descriptions-item>
            <el-descriptions-item label="压缩机运行时长(h)">
              {{ currentAc.compressorRuntimeHours }}
            </el-descriptions-item>
            <el-descriptions-item label="保养阈值(h)">
              {{ currentAc.maintenanceThreshold }}
            </el-descriptions-item>
            <el-descriptions-item label="上次保养时间">
              {{ currentAc.lastMaintenanceTime }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 温度趋势图 -->
        <div class="detail-section">
          <h3 class="section-title">温度趋势</h3>
          <div ref="temperatureChartRef" class="chart-container"></div>
        </div>

        <!-- 功率趋势图 -->
        <div class="detail-section">
          <h3 class="section-title">功率趋势</h3>
          <div ref="powerChartRef" class="chart-container"></div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import axios from 'axios'
import EfficiencyCheckOrders from './EfficiencyCheckOrders.vue'

interface AirConditioner {
  id: number
  acCode: string
  acName: string
  roomId: number
  location: string
  brand: string
  model: string
  coolingCapacity: number
  runMode: number
  setTemperature: number
  currentReturnTemp: number
  currentReturnHumidity: number
  compressorRuntimeHours: number
  maintenanceThreshold: number
  lastMaintenanceTime: string
  status: number
}

interface Room {
  id: number
  name: string
}

// 数据
const activeTab = ref('list')
const loading = ref(false)
const acList = ref<AirConditioner[]>([])
const roomList = ref<Room[]>([])
const total = ref(0)
const statistics = ref({
  total: 0,
  online: 0,
  running: 0,
  alarm: 0
})

const queryParams = reactive({
  roomId: null as number | null,
  current: 1,
  size: 10
})

const detailDialogVisible = ref(false)
const currentAc = ref<AirConditioner | null>(null)
const temperatureChartRef = ref()
const powerChartRef = ref()

// 方法
const getRunModeName = (mode: number) => {
  const modeMap: Record<number, string> = {
    0: '关闭',
    1: '制冷',
    2: '制热',
    3: '除湿',
    4: '通风'
  }
  return modeMap[mode] || '未知'
}

const loadRoomList = async () => {
  try {
    const response = await axios.get('/api/rooms/list')
    roomList.value = response.data.data
  } catch (error) {
    console.error('加载机房列表失败:', error)
  }
}

const loadStatistics = async () => {
  try {
    const response = await axios.get('/api/air-conditioner/statistics')
    statistics.value = response.data.data
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const loadAcList = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/air-conditioner/list', {
      params: queryParams
    })
    acList.value = response.data.data.records
    total.value = response.data.data.total
  } catch (error) {
    ElMessage.error('加载空调列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.current = 1
  loadAcList()
}

const handleReset = () => {
  queryParams.roomId = null
  queryParams.current = 1
  queryParams.size = 10
  loadAcList()
}

const handleBatchCollect = async () => {
  try {
    await axios.post('/api/air-conditioner/batchCollect')
    ElMessage.success('批量采集数据成功')
    loadAcList()
    loadStatistics()
  } catch (error) {
    ElMessage.error('批量采集数据失败')
  }
}

const handleCollect = async (row: AirConditioner) => {
  try {
    await axios.post('/api/air-conditioner/collect', null, {
      params: { acId: row.id }
    })
    ElMessage.success('采集数据成功')
    loadAcList()
    loadStatistics()
  } catch (error) {
    ElMessage.error('采集数据失败')
  }
}

const handleViewDetail = async (row: AirConditioner) => {
  currentAc.value = row
  detailDialogVisible.value = true
  await nextTick()

  // 加载温度趋势
  loadTemperatureTrend(row.id)
  // 加载功率趋势
  loadPowerTrend(row.id)
}

const loadTemperatureTrend = async (acId: number) => {
  try {
    const endTime = new Date()
    const startTime = new Date(endTime.getTime() - 24 * 60 * 60 * 1000) // 最近24小时

    const response = await axios.get('/api/air-conditioner/temperatureTrend', {
      params: {
        acId,
        startTime: startTime.toISOString(),
        endTime: endTime.toISOString()
      }
    })

    const trend = response.data.data
    renderTemperatureChart(trend)
  } catch (error) {
    console.error('加载温度趋势失败:', error)
  }
}

const loadPowerTrend = async (acId: number) => {
  try {
    const endTime = new Date()
    const startTime = new Date(endTime.getTime() - 24 * 60 * 60 * 1000) // 最近24小时

    const response = await axios.get('/api/air-conditioner/powerTrend', {
      params: {
        acId,
        startTime: startTime.toISOString(),
        endTime: endTime.toISOString()
      }
    })

    const trend = response.data.data
    renderPowerChart(trend)
  } catch (error) {
    console.error('加载功率趋势失败:', error)
  }
}

const renderTemperatureChart = (data: any[]) => {
  if (!temperatureChartRef.value) return

  const chart = echarts.init(temperatureChartRef.value)

  const option = {
    backgroundColor: 'transparent',
    title: {
      text: '温度趋势',
      textStyle: {
        color: '#00f0ff',
        fontSize: 16
      }
    },
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['设定温度', '回风温度', '送风温度'],
      textStyle: {
        color: '#fff'
      }
    },
    xAxis: {
      type: 'category',
      data: data.map(item => item.time),
      axisLabel: {
        color: '#fff'
      }
    },
    yAxis: {
      type: 'value',
      name: '温度(℃)',
      nameTextStyle: {
        color: '#fff'
      },
      axisLabel: {
        color: '#fff'
      },
      splitLine: {
        lineStyle: {
          color: 'rgba(255, 255, 255, 0.1)'
        }
      }
    },
    series: [
      {
        name: '设定温度',
        type: 'line',
        data: data.map(item => item.setTemperature),
        itemStyle: {
          color: '#00f0ff'
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(0, 240, 255, 0.3)' },
              { offset: 1, color: 'rgba(0, 240, 255, 0.05)' }
            ]
          }
        }
      },
      {
        name: '回风温度',
        type: 'line',
        data: data.map(item => item.returnTemperature),
        itemStyle: {
          color: '#ff00ff'
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(255, 0, 255, 0.3)' },
              { offset: 1, color: 'rgba(255, 0, 255, 0.05)' }
            ]
          }
        }
      },
      {
        name: '送风温度',
        type: 'line',
        data: data.map(item => item.supplyTemperature),
        itemStyle: {
          color: '#00ff00'
        }
      }
    ]
  }

  chart.setOption(option)
}

const renderPowerChart = (data: any[]) => {
  if (!powerChartRef.value) return

  const chart = echarts.init(powerChartRef.value)

  const option = {
    backgroundColor: 'transparent',
    title: {
      text: '功率趋势',
      textStyle: {
        color: '#00f0ff',
        fontSize: 16
      }
    },
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['功率'],
      textStyle: {
        color: '#fff'
      }
    },
    xAxis: {
      type: 'category',
      data: data.map(item => item.time),
      axisLabel: {
        color: '#fff'
      }
    },
    yAxis: {
      type: 'value',
      name: '功率(kW)',
      nameTextStyle: {
        color: '#fff'
      },
      axisLabel: {
        color: '#fff'
      },
      splitLine: {
        lineStyle: {
          color: 'rgba(255, 255, 255, 0.1)'
        }
      }
    },
    series: [
      {
        name: '功率',
        type: 'line',
        data: data.map(item => item.power),
        itemStyle: {
          color: '#ff6b00'
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(255, 107, 0, 0.3)' },
              { offset: 1, color: 'rgba(255, 107, 0, 0.05)' }
            ]
          }
        }
      }
    ]
  }

  chart.setOption(option)
}

// 初始化
onMounted(() => {
  loadRoomList()
  loadStatistics()
  loadAcList()
})
</script>

<style scoped>
.air-conditioner-monitoring {
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
  border-image: linear-gradient(to right, #00f0ff, #ff00ff) 1;
}

.page-title {
  font-size: 28px;
  font-weight: bold;
  background: linear-gradient(to right, #00f0ff, #ff00ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin: 0;
  text-shadow: 0 0 10px rgba(0, 240, 255, 0.3);
}

.stats-container {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(0, 240, 255, 0.3);
  border-radius: 10px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 0 20px rgba(0, 240, 255, 0.2);
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 0 30px rgba(0, 240, 255, 0.4);
}

.stat-icon {
  font-size: 36px;
}

.stat-card.total .stat-icon {
  filter: drop-shadow(0 0 10px rgba(255, 255, 255, 0.5));
}

.stat-card.online .stat-icon {
  filter: drop-shadow(0 0 10px rgba(0, 255, 0, 0.5));
}

.stat-card.running .stat-icon {
  filter: drop-shadow(0 0 10px rgba(0, 240, 255, 0.5));
}

.stat-card.alarm .stat-icon {
  filter: drop-shadow(0 0 10px rgba(255, 0, 0, 0.5));
}

.stat-value {
  font-size: 36px;
  font-weight: bold;
  background: linear-gradient(to right, #00f0ff, #ff00ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.stat-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  margin-top: 5px;
}

.search-form {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(0, 240, 255, 0.3);
  border-radius: 10px;
  padding: 20px;
  margin-bottom: 20px;
}

.monitoring-tabs {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(0, 240, 255, 0.3);
  border-radius: 10px;
  padding: 20px;
}

:deep(.el-tabs__item) {
  color: rgba(255, 255, 255, 0.7) !important;
}

:deep(.el-tabs__item.is-active) {
  color: #00f0ff !important;
}

:deep(.el-tabs__nav-wrap::after) {
  background: rgba(255, 255, 255, 0.1) !important;
}

:deep(.el-table) {
  background: transparent !important;
  color: #fff !important;
}

:deep(.el-table th) {
  background: rgba(0, 240, 255, 0.1) !important;
  color: #00f0ff !important;
  border-color: rgba(0, 240, 255, 0.3) !important;
}

:deep(.el-table td) {
  border-color: rgba(255, 255, 255, 0.1) !important;
}

:deep(.el-table tr:hover > td) {
  background: rgba(0, 240, 255, 0.1) !important;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

:deep(.el-pagination) {
  color: #fff !important;
}

:deep(.el-pagination .el-pager li) {
  background: rgba(255, 255, 255, 0.05) !important;
  border-color: rgba(0, 240, 255, 0.3) !important;
  color: #fff !important;
}

:deep(.el-pagination .el-pager li.is-active) {
  background: linear-gradient(135deg, #00f0ff, #ff00ff) !important;
  border-color: transparent !important;
}

.detail-section {
  margin-bottom: 30px;
}

.section-title {
  font-size: 20px;
  font-weight: bold;
  color: #00f0ff;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid rgba(0, 240, 255, 0.3);
}

.chart-container {
  height: 400px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(0, 240, 255, 0.3);
  border-radius: 10px;
}

:deep(.el-dialog) {
  background: linear-gradient(135deg, #0d1b2a 0%, #1b263b 100%);
  border: 2px solid rgba(0, 240, 255, 0.5);
}

:deep(.el-dialog__title) {
  color: #00f0ff;
}

:deep(.el-descriptions__label) {
  background: rgba(0, 240, 255, 0.1) !important;
  color: #00f0ff !important;
}

:deep(.el-descriptions__content) {
  background: transparent !important;
  color: #fff !important;
  border-color: rgba(255, 255, 255, 0.1) !important;
}
</style>
