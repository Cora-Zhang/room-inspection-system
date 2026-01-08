<template>
  <div class="work-order-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">工单与任务管理</h1>
      <div class="page-actions">
        <el-button type="primary" @click="handleCreateWorkOrder" :icon="Plus">
          新建工单
        </el-button>
        <el-button @click="handleCheckOverdue" :icon="Warning">
          检查超时
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-container">
      <div class="stat-card pending">
        <div class="stat-icon">⏳</div>
        <div class="stat-content">
          <div class="stat-value">{{ statistics.pending }}</div>
          <div class="stat-label">待处理</div>
        </div>
      </div>
      <div class="stat-card assigned">
        <div class="stat-icon">📋</div>
        <div class="stat-content">
          <div class="stat-value">{{ statistics.assigned }}</div>
          <div class="stat-label">已指派</div>
        </div>
      </div>
      <div class="stat-card in-progress">
        <div class="stat-icon">🔧</div>
        <div class="stat-content">
          <div class="stat-value">{{ statistics.inProgress }}</div>
          <div class="stat-label">处理中</div>
        </div>
      </div>
      <div class="stat-card completed">
        <div class="stat-icon">✅</div>
        <div class="stat-content">
          <div class="stat-value">{{ statistics.completed }}</div>
          <div class="stat-label">已完成</div>
        </div>
      </div>
      <div class="stat-card overdue">
        <div class="stat-icon">⚠️</div>
        <div class="stat-content">
          <div class="stat-value urgent">{{ statistics.overdue }}</div>
          <div class="stat-label">超时工单</div>
        </div>
      </div>
    </div>

    <!-- 标签页 -->
    <el-tabs v-model="activeTab" class="work-order-tabs">
      <!-- 工单列表 -->
      <el-tab-pane label="工单列表" name="list">
        <!-- 查询表单 -->
        <div class="search-form">
          <el-form :inline="true" :model="queryParams">
            <el-form-item label="工单编号">
              <el-input v-model="queryParams.orderCode" placeholder="请输入工单编号" clearable />
            </el-form-item>
            <el-form-item label="工单类型">
              <el-select v-model="queryParams.type" placeholder="请选择类型" clearable>
                <el-option label="巡检工单" value="INSPECTION" />
                <el-option label="维修工单" value="MAINTENANCE" />
                <el-option label="保养工单" value="MAINTENANCE_PREVENTIVE" />
                <el-option label="应急工单" value="EMERGENCY" />
                <el-option label="效率核查" value="EFFICIENCY_CHECK" />
                <el-option label="能效优化" value="ENERGY_OPTIMIZATION" />
              </el-select>
            </el-form-item>
            <el-form-item label="优先级">
              <el-select v-model="queryParams.priority" placeholder="请选择优先级" clearable>
                <el-option label="紧急" value="URGENT" />
                <el-option label="高" value="HIGH" />
                <el-option label="中" value="MEDIUM" />
                <el-option label="低" value="LOW" />
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
                <el-option label="待处理" value="PENDING" />
                <el-option label="已指派" value="ASSIGNED" />
                <el-option label="处理中" value="IN_PROGRESS" />
                <el-option label="等待中" value="WAITING" />
                <el-option label="已完成" value="COMPLETED" />
                <el-option label="已取消" value="CANCELLED" />
                <el-option label="已关闭" value="CLOSED" />
              </el-select>
            </el-form-item>
            <el-form-item label="负责人">
              <el-input v-model="queryParams.ownerName" placeholder="请输入负责人" clearable />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">查询</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 数据表格 -->
        <el-table :data="workOrderList" border stripe style="width: 100%" v-loading="loading">
          <el-table-column prop="orderCode" label="工单编号" width="140" fixed />
          <el-table-column label="工单类型" width="120">
            <template #default="{ row }">
              <el-tag :type="getWorkOrderTypeTag(row.type)" size="small">
                {{ getWorkOrderTypeName(row.type) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="title" label="工单标题" min-width="200" show-overflow-tooltip />
          <el-table-column label="优先级" width="80">
            <template #default="{ row }">
              <el-tag :type="getPriorityTag(row.priority)" size="small">
                {{ getPriorityName(row.priority) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getStatusTag(row.status)" size="small">
                {{ getStatusName(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="roomName" label="机房" width="120" />
          <el-table-column prop="deviceName" label="设备" width="120" />
          <el-table-column prop="ownerName" label="负责人" width="100" />
          <el-table-column label="超时" width="80">
            <template #default="{ row }">
              <el-tag v-if="row.isOverdue" type="danger" size="small">是</el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="160" />
          <el-table-column label="操作" width="280" fixed="right">
            <template #default="{ row }">
              <el-button
                v-if="row.status === 'PENDING'"
                type="primary"
                size="small"
                link
                @click="handleAssign(row)"
              >
                指派
              </el-button>
              <el-button
                v-if="row.status === 'ASSIGNED'"
                type="success"
                size="small"
                link
                @click="handleStart(row)"
              >
                开始
              </el-button>
              <el-button
                v-if="row.status === 'IN_PROGRESS'"
                type="primary"
                size="small"
                link
                @click="handleComplete(row)"
              >
                完成
              </el-button>
              <el-button
                v-if="['PENDING', 'ASSIGNED', 'IN_PROGRESS'].includes(row.status)"
                type="warning"
                size="small"
                link
                @click="handleCancel(row)"
              >
                取消
              </el-button>
              <el-button
                v-if="row.status === 'COMPLETED'"
                type="success"
                size="small"
                link
                @click="handleClose(row)"
              >
                关闭
              </el-button>
              <el-button
                type="info"
                size="small"
                link
                @click="handleViewFlows(row)"
              >
                流转
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination">
          <el-pagination
            v-model:current-page="pagination.current"
            v-model:page-size="pagination.size"
            :total="pagination.total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </el-tab-pane>

      <!-- 工单看板 -->
      <el-tab-pane label="工单看板" name="kanban">
        <div class="kanban-board">
          <div class="kanban-column" v-for="status in kanbanColumns" :key="status.key">
            <div class="kanban-header">
              <h4>{{ status.label }}</h4>
              <span class="count">{{ getKanbanCount(status.key) }}</span>
            </div>
            <div class="kanban-cards">
              <div
                v-for="order in getKanbanOrders(status.key)"
                :key="order.id"
                class="kanban-card"
                :class="order.priority.toLowerCase()"
                @click="handleViewDetail(order)"
              >
                <div class="card-header">
                  <el-tag :type="getPriorityTag(order.priority)" size="small">
                    {{ getPriorityName(order.priority) }}
                  </el-tag>
                  <span class="card-code">{{ order.orderCode }}</span>
                </div>
                <div class="card-title">{{ order.title }}</div>
                <div class="card-info">
                  <span>{{ order.roomName }}</span>
                  <span>{{ order.ownerName || '未指派' }}</span>
                </div>
                <div v-if="order.isOverdue" class="card-overdue">超时 {{ order.overdueMinutes }} 分钟</div>
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <!-- 报表统计 -->
      <el-tab-pane label="报表统计" name="reports">
        <div class="reports-container">
          <!-- 统计图表 -->
          <div class="charts-row">
            <div class="chart-card">
              <h4>工单类型分布</h4>
              <div class="chart-placeholder">
                <div v-for="item in typeStats" :key="item.type" class="stat-bar">
                  <span class="bar-label">{{ getWorkOrderTypeName(item.type) }}</span>
                  <div class="bar-track">
                    <div class="bar-fill" :style="{ width: (item.count / maxTypeCount * 100) + '%' }"></div>
                  </div>
                  <span class="bar-value">{{ item.count }}</span>
                </div>
              </div>
            </div>

            <div class="chart-card">
              <h4>优先级分布</h4>
              <div class="chart-placeholder">
                <div v-for="item in priorityStats" :key="item.priority" class="stat-bar">
                  <span class="bar-label">{{ getPriorityName(item.priority) }}</span>
                  <div class="bar-track">
                    <div class="bar-fill" :class="item.priority.toLowerCase()" :style="{ width: (item.count / maxPriorityCount * 100) + '%' }"></div>
                  </div>
                  <span class="bar-value">{{ item.count }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 绩效统计 -->
          <div class="performance-stats">
            <h4>值班人员绩效统计</h4>
            <el-table :data="staffPerformance" border stripe>
              <el-table-column prop="staffName" label="姓名" />
              <el-table-column prop="totalOrders" label="工单数" />
              <el-table-column prop="completedOrders" label="完成数" />
              <el-table-column label="完成率">
                <template #default="{ row }">
                  {{ ((row.completedOrders / row.totalOrders) * 100).toFixed(1) }}%
                </template>
              </el-table-column>
              <el-table-column prop="avgDuration" label="平均时长(小时)" />
              <el-table-column prop="overdueCount" label="超时次数" />
            </el-table>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 工单指派对话框 -->
    <el-dialog v-model="assignDialogVisible" title="指派工单" width="500px">
      <el-form :model="assignForm" label-width="80px">
        <el-form-item label="负责人">
          <el-select v-model="assignForm.assignedTo" placeholder="请选择负责人">
            <el-option
              v-for="staff in staffList"
              :key="staff.id"
              :label="staff.name"
              :value="staff.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="assignForm.priority">
            <el-option label="紧急" value="URGENT" />
            <el-option label="高" value="HIGH" />
            <el-option label="中" value="MEDIUM" />
            <el-option label="低" value="LOW" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAssignSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 工单完成对话框 -->
    <el-dialog v-model="completeDialogVisible" title="完成工单" width="600px">
      <el-form :model="completeForm" label-width="100px">
        <el-form-item label="处理结果">
          <el-input v-model="completeForm.handleResult" placeholder="请输入处理结果" />
        </el-form-item>
        <el-form-item label="处理说明">
          <el-input
            v-model="completeForm.handleDescription"
            type="textarea"
            :rows="4"
            placeholder="请输入处理说明"
          />
        </el-form-item>
        <el-form-item label="工作时长">
          <el-input-number v-model="completeForm.duration" :min="0" :step="0.5" />
          <span style="margin-left: 8px">小时</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="completeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCompleteSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 工单流转记录对话框 -->
    <el-dialog v-model="flowsDialogVisible" title="工单流转记录" width="700px">
      <el-timeline>
        <el-timeline-item
          v-for="flow in workOrderFlows"
          :key="flow.id"
          :timestamp="formatDateTime(flow.operatedAt)"
        >
          <div class="flow-content">
            <div class="flow-header">
              <span class="flow-action">{{ getActionTypeName(flow.actionType) }}</span>
              <span class="flow-operator">{{ flow.operatorName }}</span>
            </div>
            <div class="flow-content-text">{{ flow.content }}</div>
            <div v-if="flow.comment" class="flow-comment">{{ flow.comment }}</div>
          </div>
        </el-timeline-item>
      </el-timeline>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { Plus, Warning } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

// 数据状态
const loading = ref(false)
const activeTab = ref('list')

// 统计数据
const statistics = reactive({
  pending: 0,
  assigned: 0,
  inProgress: 0,
  completed: 0,
  overdue: 0
})

// 查询参数
const queryParams = reactive({
  orderCode: '',
  type: '',
  priority: '',
  status: '',
  ownerName: ''
})

// 工单列表
const workOrderList = ref<any[]>([])

// 分页
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 看板数据
const kanbanColumns = [
  { key: 'PENDING', label: '待处理' },
  { key: 'ASSIGNED', label: '已指派' },
  { key: 'IN_PROGRESS', label: '处理中' },
  { key: 'WAITING', label: '等待中' },
  { key: 'COMPLETED', label: '已完成' }
]

const getKanbanOrders = (status: string) => {
  return workOrderList.value.filter(order => order.status === status)
}

const getKanbanCount = (status: string) => {
  return workOrderList.value.filter(order => order.status === status).length
}

// 统计数据
const typeStats = ref<any[]>([])
const priorityStats = ref<any[]>([])
const staffPerformance = ref<any[]>([])

const maxTypeCount = computed(() => {
  return Math.max(...typeStats.value.map(item => item.count), 1)
})

const maxPriorityCount = computed(() => {
  return Math.max(...priorityStats.value.map(item => item.count), 1)
})

// 对话框
const assignDialogVisible = ref(false)
const completeDialogVisible = ref(false)
const flowsDialogVisible = ref(false)

const assignForm = reactive({
  orderId: '',
  assignedTo: '',
  priority: 'MEDIUM'
})

const completeForm = reactive({
  orderId: '',
  handleResult: '',
  handleDescription: '',
  duration: 0
})

const workOrderFlows = ref<any[]>([])

// 员工列表
const staffList = ref<any[]>([])

// 初始化
onMounted(() => {
  initData()
})

// 初始化数据
const initData = () => {
  fetchStatistics()
  fetchWorkOrders()
  fetchStats()
  fetchStaffList()
}

// 获取统计数据
const fetchStatistics = async () => {
  try {
    const response = await axios.get('/api/work-orders/statistics')
    Object.assign(statistics, response.data.data)
  } catch (error) {
    console.error('获取统计数据失败:', error)
    // 模拟数据
    Object.assign(statistics, {
      pending: 3,
      assigned: 5,
      inProgress: 4,
      completed: 25,
      overdue: 2
    })
  }
}

// 获取工单列表
const fetchWorkOrders = async () => {
  loading.value = true
  try {
    const params = {
      ...queryParams,
      current: pagination.current,
      size: pagination.size
    }
    const response = await axios.get('/api/work-orders/list', { params })
    workOrderList.value = response.data.data || []
    pagination.total = response.data.data?.length || 0
  } catch (error) {
    console.error('获取工单列表失败:', error)
    // 模拟数据
    workOrderList.value = [
      {
        id: 'WO001',
        orderCode: 'WO-20250120-001',
        type: 'MAINTENANCE',
        title: '服务器温度告警处理',
        priority: 'URGENT',
        status: 'ASSIGNED',
        roomName: '数据中心机房A',
        deviceName: '服务器001',
        ownerName: '李四',
        isOverdue: false,
        createdAt: '2025-01-20 10:30:00'
      },
      {
        id: 'WO002',
        orderCode: 'WO-20250120-002',
        type: 'MAINTENANCE',
        title: '空调温差异常检查',
        priority: 'HIGH',
        status: 'IN_PROGRESS',
        roomName: '数据中心机房A',
        deviceName: '空调001',
        ownerName: '王五',
        isOverdue: false,
        createdAt: '2025-01-20 10:25:00'
      }
    ]
  } finally {
    loading.value = false
  }
}

// 获取统计报表数据
const fetchStats = async () => {
  try {
    // 模拟数据
    typeStats.value = [
      { type: 'MAINTENANCE', count: 15 },
      { type: 'MAINTENANCE_PREVENTIVE', count: 8 },
      { type: 'INSPECTION', count: 10 },
      { type: 'EMERGENCY', count: 2 },
      { type: 'EFFICIENCY_CHECK', count: 2 }
    ]

    priorityStats.value = [
      { priority: 'URGENT', count: 3 },
      { priority: 'HIGH', count: 8 },
      { priority: 'MEDIUM', count: 20 },
      { priority: 'LOW', count: 6 }
    ]

    staffPerformance.value = [
      { staffName: '李四', totalOrders: 15, completedOrders: 12, avgDuration: 2.5, overdueCount: 1 },
      { staffName: '王五', totalOrders: 12, completedOrders: 10, avgDuration: 2.0, overdueCount: 0 },
      { staffName: '张三', totalOrders: 8, completedOrders: 7, avgDuration: 1.8, overdueCount: 0 }
    ]
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 获取员工列表
const fetchStaffList = async () => {
  try {
    // 模拟数据
    staffList.value = [
      { id: 'U002', name: '李四' },
      { id: 'U003', name: '王五' },
      { id: 'U004', name: '赵六' }
    ]
  } catch (error) {
    console.error('获取员工列表失败:', error)
  }
}

// 工具函数
const getWorkOrderTypeTag = (type: string) => {
  const tagMap: Record<string, string> = {
    MAINTENANCE: 'warning',
    MAINTENANCE_PREVENTIVE: 'success',
    INSPECTION: 'primary',
    EMERGENCY: 'danger',
    EFFICIENCY_CHECK: 'info',
    ENERGY_OPTIMIZATION: 'info'
  }
  return tagMap[type] || ''
}

const getWorkOrderTypeName = (type: string) => {
  const nameMap: Record<string, string> = {
    MAINTENANCE: '维修工单',
    MAINTENANCE_PREVENTIVE: '保养工单',
    INSPECTION: '巡检工单',
    EMERGENCY: '应急工单',
    EFFICIENCY_CHECK: '效率核查',
    ENERGY_OPTIMIZATION: '能效优化'
  }
  return nameMap[type] || type
}

const getPriorityTag = (priority: string) => {
  const tagMap: Record<string, string> = {
    URGENT: 'danger',
    HIGH: 'warning',
    MEDIUM: 'primary',
    LOW: 'info'
  }
  return tagMap[priority] || ''
}

const getPriorityName = (priority: string) => {
  const nameMap: Record<string, string> = {
    URGENT: '紧急',
    HIGH: '高',
    MEDIUM: '中',
    LOW: '低'
  }
  return nameMap[priority] || priority
}

const getStatusTag = (status: string) => {
  const tagMap: Record<string, string> = {
    PENDING: 'warning',
    ASSIGNED: 'primary',
    IN_PROGRESS: 'success',
    WAITING: 'info',
    COMPLETED: 'success',
    CANCELLED: 'danger',
    CLOSED: 'info'
  }
  return tagMap[status] || ''
}

const getStatusName = (status: string) => {
  const nameMap: Record<string, string> = {
    PENDING: '待处理',
    ASSIGNED: '已指派',
    IN_PROGRESS: '处理中',
    WAITING: '等待中',
    COMPLETED: '已完成',
    CANCELLED: '已取消',
    CLOSED: '已关闭'
  }
  return nameMap[status] || status
}

const getActionTypeName = (actionType: string) => {
  const nameMap: Record<string, string> = {
    CREATE: '创建',
    ASSIGN: '指派',
    START: '开始',
    PAUSE: '暂停',
    RESUME: '恢复',
    COMPLETE: '完成',
    CANCEL: '取消',
    CLOSE: '关闭',
    REASSIGN: '重新指派'
  }
  return nameMap[actionType] || actionType
}

const formatDateTime = (time: any) => {
  if (!time) return ''
  return new Date(time).toLocaleString()
}

// 事件处理
const handleSearch = () => {
  pagination.current = 1
  fetchWorkOrders()
}

const handleReset = () => {
  Object.assign(queryParams, {
    orderCode: '',
    type: '',
    priority: '',
    status: '',
    ownerName: ''
  })
  fetchWorkOrders()
}

const handleSizeChange = (size: number) => {
  pagination.size = size
  fetchWorkOrders()
}

const handleCurrentChange = (current: number) => {
  pagination.current = current
  fetchWorkOrders()
}

const handleCreateWorkOrder = () => {
  ElMessage.info('打开新建工单表单')
}

const handleCheckOverdue = async () => {
  try {
    const response = await axios.post('/api/work-orders/check-overdue')
    ElMessage.success(`检查完成，发现 ${response.data.data} 个超时工单`)
    fetchWorkOrders()
    fetchStatistics()
  } catch (error) {
    console.error('检查超时失败:', error)
    ElMessage.error('检查超时失败')
  }
}

const handleAssign = (row: any) => {
  assignForm.orderId = row.id
  assignForm.assignedTo = ''
  assignForm.priority = row.priority
  assignDialogVisible.value = true
}

const handleAssignSubmit = async () => {
  try {
    const staff = staffList.value.find(s => s.id === assignForm.assignedTo)
    await axios.put(`/api/work-orders/${assignForm.orderId}/assign`, {
      assignedTo: assignForm.assignedTo,
      assignedToName: staff?.name,
      priority: assignForm.priority
    })
    ElMessage.success('指派成功')
    assignDialogVisible.value = false
    fetchWorkOrders()
    fetchStatistics()
  } catch (error) {
    console.error('指派失败:', error)
    ElMessage.error('指派失败')
  }
}

const handleStart = async (row: any) => {
  try {
    await axios.put(`/api/work-orders/${row.id}/start`, {
      ownerId: row.assignedTo,
      ownerName: row.assignedToName
    })
    ElMessage.success('开始处理')
    fetchWorkOrders()
    fetchStatistics()
  } catch (error) {
    console.error('开始处理失败:', error)
    ElMessage.error('开始处理失败')
  }
}

const handleComplete = (row: any) => {
  completeForm.orderId = row.id
  completeForm.handleResult = ''
  completeForm.handleDescription = ''
  completeForm.duration = 0
  completeDialogVisible.value = true
}

const handleCompleteSubmit = async () => {
  try {
    await axios.put(`/api/work-orders/${completeForm.orderId}/complete`, {
      handleResult: completeForm.handleResult,
      handleDescription: completeForm.handleDescription,
      duration: completeForm.duration
    })
    ElMessage.success('工单已完成')
    completeDialogVisible.value = false
    fetchWorkOrders()
    fetchStatistics()
  } catch (error) {
    console.error('完成工单失败:', error)
    ElMessage.error('完成工单失败')
  }
}

const handleCancel = async (row: any) => {
  try {
    await ElMessageBox.prompt('请输入取消原因', '取消工单', {
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    const cancelReason = document.querySelector('.el-message-box__input')?.value || ''
    await axios.put(`/api/work-orders/${row.id}/cancel`, { cancelReason })
    ElMessage.success('工单已取消')
    fetchWorkOrders()
    fetchStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消工单失败:', error)
      ElMessage.error('取消工单失败')
    }
  }
}

const handleClose = async (row: any) => {
  try {
    await ElMessageBox.prompt('请输入关闭原因', '关闭工单', {
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    const closeReason = document.querySelector('.el-message-box__input')?.value || ''
    await axios.put(`/api/work-orders/${row.id}/close`, {
      closeReason,
      closedBy: 'U001',
      closedByName: '张三'
    })
    ElMessage.success('工单已关闭')
    fetchWorkOrders()
    fetchStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('关闭工单失败:', error)
      ElMessage.error('关闭工单失败')
    }
  }
}

const handleViewFlows = async (row: any) => {
  try {
    const response = await axios.get(`/api/work-orders/${row.id}/flows`)
    workOrderFlows.value = response.data.data || []
    flowsDialogVisible.value = true
  } catch (error) {
    console.error('获取流转记录失败:', error)
    ElMessage.error('获取流转记录失败')
  }
}

const handleViewDetail = (row: any) => {
  ElMessage.info(`工单详情: ${row.title}`)
}
</script>

<style scoped lang="scss">
.work-order-management {
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
  grid-template-columns: repeat(5, 1fr);
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

.stat-value.urgent {
  color: #ff4d4f;
}

.stat-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
}

.search-form {
  margin-bottom: 20px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 8px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

// 看板样式
.kanban-board {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 20px;
  overflow-x: auto;
}

.kanban-column {
  min-width: 280px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 16px;
  max-height: 70vh;
  overflow-y: auto;
}

.kanban-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.kanban-header h4 {
  font-size: 16px;
  font-weight: 600;
  color: #00f0ff;
}

.count {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
}

.kanban-cards {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.kanban-card {
  padding: 16px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(0, 240, 255, 0.2);
  }

  &.urgent {
    border-left: 4px solid #ff4d4f;
  }

  &.high {
    border-left: 4px solid #faad14;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.card-code {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
}

.card-title {
  font-weight: 600;
  margin-bottom: 8px;
  font-size: 14px;
}

.card-info {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
}

.card-overdue {
  margin-top: 8px;
  padding: 4px 8px;
  background: rgba(255, 77, 79, 0.2);
  color: #ff4d4f;
  border-radius: 4px;
  font-size: 12px;
  text-align: center;
}

// 报表样式
.reports-container {
  padding: 16px;
}

.charts-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.chart-card {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 20px;
}

.chart-card h4 {
  font-size: 16px;
  font-weight: 600;
  color: #00f0ff;
  margin-bottom: 20px;
}

.chart-placeholder {
  min-height: 200px;
}

.stat-bar {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.bar-label {
  width: 100px;
  font-size: 14px;
}

.bar-track {
  flex: 1;
  margin: 0 12px;
  height: 24px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #00f0ff, #00d4ff);
  transition: width 0.5s ease;
}

.bar-fill.urgent {
  background: linear-gradient(90deg, #ff4d4f, #ff7875);
}

.bar-fill.high {
  background: linear-gradient(90deg, #faad14, #ffc53d);
}

.bar-value {
  width: 40px;
  text-align: right;
  font-weight: 600;
}

.performance-stats {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 20px;
}

.performance-stats h4 {
  font-size: 16px;
  font-weight: 600;
  color: #00f0ff;
  margin-bottom: 16px;
}

// 流转记录样式
.flow-content {
  padding: 8px;
}

.flow-header {
  display: flex;
  gap: 12px;
  margin-bottom: 8px;
}

.flow-action {
  font-weight: 600;
  color: #00f0ff;
}

.flow-operator {
  color: rgba(255, 255, 255, 0.7);
}

.flow-content-text {
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 4px;
}

.flow-comment {
  padding: 8px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 4px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
}
</style>
