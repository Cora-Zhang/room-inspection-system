<template>
  <div class="energy-efficiency-container">
    <!-- 头部统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <div class="stat-card pending">
          <div class="stat-icon">📋</div>
          <div class="stat-info">
            <div class="stat-label">待处理</div>
            <div class="stat-value">{{ orderStats.pendingCount }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card processing">
          <div class="stat-icon">⚙️</div>
          <div class="stat-info">
            <div class="stat-label">处理中</div>
            <div class="stat-value">{{ orderStats.processingCount }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card completed">
          <div class="stat-icon">✅</div>
          <div class="stat-info">
            <div class="stat-label">已完成</div>
            <div class="stat-value">{{ orderStats.completedCount }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card urgent">
          <div class="stat-icon">🚨</div>
          <div class="stat-info">
            <div class="stat-label">紧急工单</div>
            <div class="stat-value">{{ orderStats.urgentCount }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 主要内容区域 -->
    <el-row :gutter="20" class="main-content">
      <!-- 左侧：工单列表 -->
      <el-col :span="16">
        <div class="panel order-list-panel">
          <div class="panel-header">
            <h3>能效优化工单</h3>
            <div class="header-actions">
              <el-select v-model="filterForm.status" placeholder="工单状态" size="small" clearable @change="loadOrderList">
                <el-option label="待处理" value="PENDING"></el-option>
                <el-option label="处理中" value="PROCESSING"></el-option>
                <el-option label="已完成" value="COMPLETED"></el-option>
                <el-option label="已关闭" value="CLOSED"></el-option>
              </el-select>
              <el-select v-model="filterForm.orderType" placeholder="工单类型" size="small" clearable @change="loadOrderList">
                <el-option label="高温检查" value="HIGH_TEMP"></el-option>
                <el-option label="湿度排查" value="HUMIDITY_RISE"></el-option>
                <el-option label="冷通道检查" value="COLD_AIR"></el-option>
                <el-option label="风道检查" value="AIR_DUCT"></el-option>
              </el-select>
              <el-select v-model="filterForm.priority" placeholder="优先级" size="small" clearable @change="loadOrderList">
                <el-option label="低" value="LOW"></el-option>
                <el-option label="中" value="MEDIUM"></el-option>
                <el-option label="高" value="HIGH"></el-option>
                <el-option label="紧急" value="URGENT"></el-option>
              </el-select>
            </div>
          </div>
          <div class="panel-body">
            <el-table :data="orderList" style="width: 100%" :header-cell-style="{ background: '#1a1a2e', color: '#00d4ff' }">
              <el-table-column prop="orderNo" label="工单编号" width="130"></el-table-column>
              <el-table-column prop="orderType" label="类型" width="110">
                <template #default="scope">
                  <el-tag v-if="scope.row.orderType === 'HIGH_TEMP'" type="danger">高温检查</el-tag>
                  <el-tag v-else-if="scope.row.orderType === 'HUMIDITY_RISE'" type="warning">湿度排查</el-tag>
                  <el-tag v-else-if="scope.row.orderType === 'COLD_AIR'" type="info">冷通道检查</el-tag>
                  <el-tag v-else type="primary">风道检查</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="title" label="标题" width="180" show-overflow-tooltip></el-table-column>
              <el-table-column prop="abnormalArea" label="异常区域" width="120" show-overflow-tooltip></el-table-column>
              <el-table-column prop="priority" label="优先级" width="90">
                <template #default="scope">
                  <el-tag v-if="scope.row.priority === 'URGENT'" type="danger">紧急</el-tag>
                  <el-tag v-else-if="scope.row.priority === 'HIGH'" type="warning">高</el-tag>
                  <el-tag v-else-if="scope.row.priority === 'MEDIUM'" type="info">中</el-tag>
                  <el-tag v-else type="success">低</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="90">
                <template #default="scope">
                  <el-tag v-if="scope.row.status === 'PENDING'" type="warning">待处理</el-tag>
                  <el-tag v-else-if="scope.row.status === 'PROCESSING'" type="primary">处理中</el-tag>
                  <el-tag v-else-if="scope.row.status === 'COMPLETED'" type="success">已完成</el-tag>
                  <el-tag v-else type="info">已关闭</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="assigneeName" label="负责人" width="90"></el-table-column>
              <el-table-column label="操作" width="150">
                <template #default="scope">
                  <el-button type="text" size="small" @click="handleViewDetail(scope.row)">详情</el-button>
                  <el-button v-if="scope.row.status === 'PENDING'" type="text" size="small" @click="handleAssignOrder(scope.row)">指派</el-button>
                  <el-button v-if="scope.row.status === 'PROCESSING'" type="text" size="small" @click="handleCompleteOrder(scope.row)">完成</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </el-col>

      <!-- 右侧：趋势图与优化建议 -->
      <el-col :span="8">
        <!-- 工单趋势图 -->
        <div class="panel trend-panel">
          <div class="panel-header">
            <h3>工单趋势</h3>
            <el-select v-model="trendDays" size="small" @change="loadOrderTrend">
              <el-option label="近7天" :value="7"></el-option>
              <el-option label="近15天" :value="15"></el-option>
              <el-option label="近30天" :value="30"></el-option>
            </el-select>
          </div>
          <div class="panel-body">
            <div ref="trendChart" class="trend-chart"></div>
          </div>
        </div>

        <!-- 优化建议 -->
        <div class="panel suggestion-panel">
          <div class="panel-header">
            <h3>优化建议</h3>
          </div>
          <div class="panel-body">
            <div class="suggestion-list">
              <div class="suggestion-item">
                <div class="suggestion-icon">💡</div>
                <div class="suggestion-content">
                  <div class="suggestion-title">定期检查冷通道封闭</div>
                  <div class="suggestion-desc">建议每月对冷通道封闭板进行检查，确保无破损和冷气泄漏</div>
                </div>
              </div>
              <div class="suggestion-item">
                <div class="suggestion-icon">🌡️</div>
                <div class="suggestion-content">
                  <div class="suggestion-title">优化空调温度设置</div>
                  <div class="suggestion-desc">根据环境监测数据，动态调整空调温度设置，提高能效</div>
                </div>
              </div>
              <div class="suggestion-item">
                <div class="suggestion-icon">💧</div>
                <div class="suggestion-content">
                  <div class="suggestion-title">预防性除湿措施</div>
                  <div class="suggestion-desc">在湿度缓升时及时开启除湿设备，防止冷凝水产生</div>
                </div>
              </div>
              <div class="suggestion-item">
                <div class="suggestion-icon">🔄</div>
                <div class="suggestion-content">
                  <div class="suggestion-title">建立定期巡检机制</div>
                  <div class="suggestion-desc">每周对重点区域进行环境巡检，及时发现异常</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 触发记录 -->
        <div class="panel trigger-panel">
          <div class="panel-header">
            <h3>自动触发记录</h3>
            <el-button type="primary" size="small" @click="autoCheckAndTrigger">立即检查</el-button>
          </div>
          <div class="panel-body">
            <div v-if="triggerRecords.length === 0" class="no-data">
              <div class="no-data-icon">📝</div>
              <div class="no-data-text">暂无触发记录</div>
            </div>
            <div v-else class="trigger-list">
              <div v-for="record in triggerRecords" :key="record.id" class="trigger-item">
                <div class="trigger-time">{{ formatDate(record.triggerTime) }}</div>
                <div class="trigger-message">{{ record.orderType }} - {{ record.triggerCondition }}</div>
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 工单详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="工单详情" width="700px" class="sci-fi-dialog">
      <div v-if="currentOrder" class="order-detail">
        <div class="detail-section">
          <h4>基本信息</h4>
          <div class="detail-row">
            <span class="label">工单编号：</span>
            <span class="value">{{ currentOrder.orderNo }}</span>
          </div>
          <div class="detail-row">
            <span class="label">工单类型：</span>
            <span class="value">{{ getOrderTypeName(currentOrder.orderType) }}</span>
          </div>
          <div class="detail-row">
            <span class="label">标题：</span>
            <span class="value">{{ currentOrder.title }}</span>
          </div>
          <div class="detail-row">
            <span class="label">异常区域：</span>
            <span class="value">{{ currentOrder.abnormalArea || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="label">优先级：</span>
            <el-tag v-if="currentOrder.priority === 'URGENT'" type="danger">紧急</el-tag>
            <el-tag v-else-if="currentOrder.priority === 'HIGH'" type="warning">高</el-tag>
            <el-tag v-else-if="currentOrder.priority === 'MEDIUM'" type="info">中</el-tag>
            <el-tag v-else type="success">低</el-tag>
          </div>
          <div class="detail-row">
            <span class="label">状态：</span>
            <el-tag v-if="currentOrder.status === 'PENDING'" type="warning">待处理</el-tag>
            <el-tag v-else-if="currentOrder.status === 'PROCESSING'" type="primary">处理中</el-tag>
            <el-tag v-else-if="currentOrder.status === 'COMPLETED'" type="success">已完成</el-tag>
            <el-tag v-else type="info">已关闭</el-tag>
          </div>
        </div>
        <div class="detail-section">
          <h4>触发信息</h4>
          <div class="detail-row">
            <span class="label">触发条件：</span>
            <span class="value">{{ currentOrder.triggerCondition || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="label">触发值：</span>
            <span class="value">{{ currentOrder.triggerValue || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="label">触发时间：</span>
            <span class="value">{{ formatDateTime(currentOrder.triggerTime) }}</span>
          </div>
        </div>
        <div class="detail-section">
          <h4>负责人信息</h4>
          <div class="detail-row">
            <span class="label">负责人：</span>
            <span class="value">{{ currentOrder.assigneeName || '未指派' }}</span>
          </div>
          <div class="detail-row">
            <span class="label">指派时间：</span>
            <span class="value">{{ formatDateTime(currentOrder.assignTime) }}</span>
          </div>
          <div class="detail-row">
            <span class="label">计划完成时间：</span>
            <span class="value">{{ formatDateTime(currentOrder.planCompleteTime) }}</span>
          </div>
        </div>
        <div class="detail-section">
          <h4>描述</h4>
          <div class="detail-content">{{ currentOrder.description }}</div>
        </div>
        <div class="detail-section" v-if="currentOrder.suggestion">
          <h4>处理建议</h4>
          <div class="detail-content suggestion-text">{{ currentOrder.suggestion }}</div>
        </div>
        <div class="detail-section" v-if="currentOrder.status === 'COMPLETED'">
          <h4>处理结果</h4>
          <div class="detail-content">{{ currentOrder.result }}</div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 指派工单对话框 -->
    <el-dialog v-model="assignDialogVisible" title="指派工单" width="500px" class="sci-fi-dialog">
      <el-form :model="assignForm" label-width="100px">
        <el-form-item label="负责人">
          <el-select v-model="assignForm.assigneeId" placeholder="请选择负责人" filterable>
            <el-option label="张三" :value="1"></el-option>
            <el-option label="李四" :value="2"></el-option>
            <el-option label="王五" :value="3"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="计划完成时间">
          <el-date-picker
            v-model="assignForm.planCompleteTime"
            type="datetime"
            placeholder="选择日期时间"
            style="width: 100%">
          </el-date-picker>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAssign">确定</el-button>
      </template>
    </el-dialog>

    <!-- 完成工单对话框 -->
    <el-dialog v-model="completeDialogVisible" title="完成工单" width="600px" class="sci-fi-dialog">
      <el-form :model="completeForm" label-width="100px">
        <el-form-item label="处理结果">
          <el-input v-model="completeForm.result" type="textarea" :rows="3" placeholder="请输入处理结果"></el-input>
        </el-form-item>
        <el-form-item label="改进建议">
          <el-input v-model="completeForm.suggestion" type="textarea" :rows="3" placeholder="请输入改进建议（可选）"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="completeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmComplete">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

// 响应式数据
const orderStats = reactive({
  pendingCount: 3,
  processingCount: 2,
  completedCount: 15,
  urgentCount: 1
})

const filterForm = reactive({
  status: '',
  orderType: '',
  priority: ''
})

const orderList = ref([])
const trendDays = ref(7)
const triggerRecords = ref([])

// 对话框
const detailDialogVisible = ref(false)
const assignDialogVisible = ref(false)
const completeDialogVisible = ref(false)
const currentOrder = ref(null)

const assignForm = reactive({
  assigneeId: null,
  assigneeName: '',
  planCompleteTime: null
})

const completeForm = reactive({
  result: '',
  suggestion: ''
})

// 引用
const trendChart = ref(null)

// 加载工单列表
const loadOrderList = async () => {
  try {
    const params: any = {}
    if (filterForm.status) params.status = filterForm.status
    if (filterForm.orderType) params.orderType = filterForm.orderType
    if (filterForm.priority) params.priority = filterForm.priority
    
    const response = await axios.get('/api/energy-efficiency/order/list', { params })
    if (response.data.success) {
      orderList.value = response.data.data
    }
  } catch (error) {
    ElMessage.error('加载工单列表失败')
  }
}

// 查看详情
const handleViewDetail = async (order: any) => {
  try {
    const response = await axios.get(`/api/energy-efficiency/order/${order.id}`)
    if (response.data.success) {
      currentOrder.value = response.data.data
      detailDialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('加载工单详情失败')
  }
}

// 指派工单
const handleAssignOrder = (order: any) => {
  currentOrder.value = order
  assignForm.assigneeId = null
  assignForm.assigneeName = ''
  assignForm.planCompleteTime = null
  assignDialogVisible.value = true
}

// 确认指派
const confirmAssign = async () => {
  if (!assignForm.assigneeId) {
    ElMessage.warning('请选择负责人')
    return
  }
  
  try {
    const assigneeMap: Record<number, string> = {
      1: '张三',
      2: '李四',
      3: '王五'
    }
    assignForm.assigneeName = assigneeMap[assignForm.assigneeId as number]
    
    const response = await axios.post(`/api/energy-efficiency/order/${currentOrder.value.id}/assign`, null, {
      params: {
        assigneeId: assignForm.assigneeId,
        assigneeName: assignForm.assigneeName
      }
    })
    
    if (response.data.success) {
      ElMessage.success('指派成功')
      assignDialogVisible.value = false
      loadOrderList()
    }
  } catch (error) {
    ElMessage.error('指派失败')
  }
}

// 完成工单
const handleCompleteOrder = (order: any) => {
  currentOrder.value = order
  completeForm.result = ''
  completeForm.suggestion = ''
  completeDialogVisible.value = true
}

// 确认完成
const confirmComplete = async () => {
  if (!completeForm.result) {
    ElMessage.warning('请输入处理结果')
    return
  }
  
  try {
    const response = await axios.post(`/api/energy-efficiency/order/${currentOrder.value.id}/complete`, null, {
      params: completeForm
    })
    
    if (response.data.success) {
      ElMessage.success('完成工单成功')
      completeDialogVisible.value = false
      loadOrderList()
    }
  } catch (error) {
    ElMessage.error('完成工单失败')
  }
}

// 加载工单趋势
const loadOrderTrend = async () => {
  try {
    const response = await axios.get('/api/energy-efficiency/order/trend', {
      params: { days: trendDays.value }
    })
    
    if (response.data.success) {
      const data = response.data.data
      // TODO: 使用ECharts渲染趋势图
      console.log('工单趋势数据', data)
    }
  } catch (error) {
    console.error('加载工单趋势失败', error)
  }
}

// 自动检查并触发工单
const autoCheckAndTrigger = async () => {
  try {
    const response = await axios.post('/api/energy-efficiency/auto/check-trigger')
    if (response.data.success) {
      ElMessage.success('自动检查完成')
      loadOrderList()
    }
  } catch (error) {
    ElMessage.error('自动检查失败')
  }
}

// 格式化日期
const formatDate = (dateStr: string) => {
  const date = new Date(dateStr)
  return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

// 格式化日期时间
const formatDateTime = (dateStr: string | null) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
}

// 获取工单类型名称
const getOrderTypeName = (type: string) => {
  const map: Record<string, string> = {
    HIGH_TEMP: '高温检查',
    HUMIDITY_RISE: '湿度排查',
    COLD_AIR: '冷通道检查',
    AIR_DUCT: '风道检查'
  }
  return map[type] || type
}

onMounted(() => {
  loadOrderList()
  loadOrderTrend()
})
</script>

<style scoped>
.energy-efficiency-container {
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

.stat-card.pending {
  border-left: 3px solid #ffd93d;
}

.stat-card.processing {
  border-left: 3px solid #4ecdc4;
}

.stat-card.completed {
  border-left: 3px solid #51cf66;
}

.stat-card.urgent {
  border-left: 3px solid #ff6b6b;
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

/* 优化建议 */
.suggestion-list {
  max-height: 350px;
  overflow-y: auto;
}

.suggestion-item {
  display: flex;
  align-items: flex-start;
  padding: 12px;
  margin-bottom: 10px;
  background: rgba(0, 212, 255, 0.05);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 6px;
}

.suggestion-icon {
  font-size: 24px;
  margin-right: 12px;
}

.suggestion-content {
  flex: 1;
}

.suggestion-title {
  color: #00d4ff;
  font-weight: bold;
  margin-bottom: 5px;
}

.suggestion-desc {
  color: #a0a0c0;
  font-size: 13px;
  line-height: 1.5;
}

/* 触发记录 */
.trigger-list {
  max-height: 250px;
  overflow-y: auto;
}

.trigger-item {
  padding: 10px;
  margin-bottom: 8px;
  background: rgba(78, 205, 196, 0.1);
  border-left: 3px solid #4ecdc4;
  border-radius: 4px;
}

.trigger-time {
  color: #a0a0c0;
  font-size: 12px;
  margin-bottom: 4px;
}

.trigger-message {
  color: #4ecdc4;
  font-size: 13px;
}

/* 空数据 */
.no-data {
  text-align: center;
  padding: 40px 20px;
  color: #a0a0c0;
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

/* 工单详情 */
.order-detail {
  max-height: 500px;
  overflow-y: auto;
}

.detail-section {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(0, 212, 255, 0.1);
}

.detail-section:last-child {
  border-bottom: none;
}

.detail-section h4 {
  color: #00d4ff;
  margin: 0 0 10px 0;
  font-size: 14px;
}

.detail-row {
  display: flex;
  margin-bottom: 8px;
  align-items: center;
}

.detail-row .label {
  color: #a0a0c0;
  width: 100px;
  font-size: 13px;
}

.detail-row .value {
  color: #fff;
  flex: 1;
  font-size: 13px;
}

.detail-content {
  color: #a0a0c0;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
}

.suggestion-text {
  color: #4ecdc4;
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
.suggestion-list::-webkit-scrollbar,
.trigger-list::-webkit-scrollbar,
.order-detail::-webkit-scrollbar {
  width: 6px;
}

:deep(.el-table__body-wrapper::-webkit-scrollbar-thumb),
.suggestion-list::-webkit-scrollbar-thumb,
.trigger-list::-webkit-scrollbar-thumb,
.order-detail::-webkit-scrollbar-thumb {
  background: rgba(0, 212, 255, 0.3);
  border-radius: 3px;
}

:deep(.el-table__body-wrapper::-webkit-scrollbar-track),
.suggestion-list::-webkit-scrollbar-track,
.trigger-list::-webkit-scrollbar-track,
.order-detail::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.2);
}
</style>
