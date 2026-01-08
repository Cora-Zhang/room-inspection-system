<template>
  <div class="efficiency-check-orders">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">效率核查工单</h1>
    </div>

    <!-- 工单统计卡片 -->
    <div class="stats-container">
      <div class="stat-card pending">
        <div class="stat-icon">📋</div>
        <div class="stat-content">
          <div class="stat-value">{{ statistics.pending }}</div>
          <div class="stat-label">待指派</div>
        </div>
      </div>
      <div class="stat-card assigned">
        <div class="stat-icon">👤</div>
        <div class="stat-content">
          <div class="stat-value">{{ statistics.assigned }}</div>
          <div class="stat-label">待处理</div>
        </div>
      </div>
      <div class="stat-card processing">
        <div class="stat-icon">⚙️</div>
        <div class="stat-content">
          <div class="stat-value">{{ statistics.processing }}</div>
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
    </div>

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
        <el-form-item label="空调">
          <el-select v-model="queryParams.acId" placeholder="请选择空调" clearable>
            <el-option
              v-for="ac in acList"
              :key="ac.id"
              :label="ac.acName"
              :value="ac.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="工单状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="待指派" :value="0" />
            <el-option label="待处理" :value="1" />
            <el-option label="处理中" :value="2" />
            <el-option label="已完成" :value="3" />
            <el-option label="已取消" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 工单列表 -->
    <el-table :data="orderList" border stripe style="width: 100%" v-loading="loading">
      <el-table-column prop="orderNo" label="工单编号" width="180" />
      <el-table-column prop="acCode" label="空调编号" width="120" />
      <el-table-column label="工单类型" width="120">
        <template #default="{ row }">
          <el-tag>{{ getOrderTypeName(row.orderType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="triggerReason" label="触发原因" width="150" />
      <el-table-column label="优先级" width="100">
        <template #default="{ row }">
          <el-tag :type="getPriorityType(row.priority)">
            {{ getPriorityName(row.priority) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusName(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="assignee" label="指派人" width="100" />
      <el-table-column prop="createTime" label="创建时间" width="160" />
      <el-table-column label="操作" width="300" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="row.status === 0"
            type="primary"
            size="small"
            @click="handleAssign(row)"
          >
            指派
          </el-button>
          <el-button
            v-if="row.status === 1"
            type="success"
            size="small"
            @click="handleStart(row)"
          >
            开始处理
          </el-button>
          <el-button
            v-if="row.status === 2"
            type="warning"
            size="small"
            @click="handleComplete(row)"
          >
            完成工单
          </el-button>
          <el-button
            v-if="row.status < 3"
            type="danger"
            size="small"
            @click="handleCancel(row)"
          >
            取消
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

    <!-- 指派对话框 -->
    <el-dialog v-model="assignDialogVisible" title="指派工单" width="500px">
      <el-form :model="assignForm" label-width="80px">
        <el-form-item label="指派人">
          <el-input v-model="assignForm.assignee" placeholder="请输入指派人姓名" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmAssign">确定</el-button>
      </template>
    </el-dialog>

    <!-- 完成对话框 -->
    <el-dialog v-model="completeDialogVisible" title="完成工单" width="600px">
      <el-form :model="completeForm" label-width="100px">
        <el-form-item label="处理结果">
          <el-input
            v-model="completeForm.handleResult"
            type="textarea"
            :rows="3"
            placeholder="请输入处理结果"
          />
        </el-form-item>
        <el-form-item label="处理描述">
          <el-input
            v-model="completeForm.handleDescription"
            type="textarea"
            :rows="3"
            placeholder="请输入处理描述"
          />
        </el-form-item>
        <el-form-item label="现场照片">
          <el-input
            v-model="completeForm.photos"
            placeholder="请输入照片URL（多个用逗号分隔）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="completeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmComplete">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

interface Order {
  id: number
  orderNo: string
  acId: number
  acCode: string
  roomId: number
  orderType: number
  triggerReason: string
  abnormalDetail: string
  priority: number
  status: number
  assignee: string
  createTime: string
}

interface Room {
  id: number
  name: string
}

interface AirConditioner {
  id: number
  acName: string
  acCode: string
}

// 数据
const loading = ref(false)
const orderList = ref<Order[]>([])
const roomList = ref<Room[]>([])
const acList = ref<AirConditioner[]>([])
const total = ref(0)
const statistics = ref({
  total: 0,
  pending: 0,
  assigned: 0,
  processing: 0,
  completed: 0,
  cancelled: 0
})

const queryParams = reactive({
  roomId: null as number | null,
  acId: null as number | null,
  status: null as number | null,
  current: 1,
  size: 10
})

const assignDialogVisible = ref(false)
const assignForm = reactive({
  orderId: 0,
  assignee: ''
})

const completeDialogVisible = ref(false)
const completeForm = reactive({
  orderId: 0,
  handleResult: '',
  handleDescription: '',
  photos: ''
})

// 方法
const getOrderTypeName = (type: number) => {
  const typeMap: Record<number, string> = {
    1: '制冷效率检查',
    2: '滤网清洁检查',
    3: '预防性保养'
  }
  return typeMap[type] || '未知'
}

const getPriorityName = (priority: number) => {
  const priorityMap: Record<number, string> = {
    1: '紧急',
    2: '高',
    3: '中',
    4: '低'
  }
  return priorityMap[priority] || '未知'
}

const getPriorityType = (priority: number) => {
  const typeMap: Record<number, string> = {
    1: 'danger',
    2: 'warning',
    3: 'info',
    4: 'info'
  }
  return typeMap[priority] || 'info'
}

const getStatusName = (status: number) => {
  const statusMap: Record<number, string> = {
    0: '待指派',
    1: '待处理',
    2: '处理中',
    3: '已完成',
    4: '已取消'
  }
  return statusMap[status] || '未知'
}

const getStatusType = (status: number) => {
  const typeMap: Record<number, string> = {
    0: 'info',
    1: 'warning',
    2: 'primary',
    3: 'success',
    4: 'info'
  }
  return typeMap[status] || 'info'
}

const loadRoomList = async () => {
  try {
    const response = await axios.get('/api/rooms/list')
    roomList.value = response.data.data
  } catch (error) {
    console.error('加载机房列表失败:', error)
  }
}

const loadAcList = async () => {
  try {
    const response = await axios.get('/api/air-conditioner/list')
    acList.value = response.data.data.records
  } catch (error) {
    console.error('加载空调列表失败:', error)
  }
}

const loadStatistics = async () => {
  try {
    const response = await axios.get('/api/air-conditioner/efficiencyOrders/statistics', {
      params: { roomId: queryParams.roomId }
    })
    statistics.value = response.data.data
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const loadOrderList = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/air-conditioner/efficiencyOrders/list', {
      params: queryParams
    })
    orderList.value = response.data.data.records
    total.value = response.data.data.total
  } catch (error) {
    ElMessage.error('加载工单列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.current = 1
  loadOrderList()
  loadStatistics()
}

const handleReset = () => {
  queryParams.roomId = null
  queryParams.acId = null
  queryParams.status = null
  queryParams.current = 1
  queryParams.size = 10
  loadOrderList()
  loadStatistics()
}

const handleAssign = (row: Order) => {
  assignForm.orderId = row.id
  assignForm.assignee = ''
  assignDialogVisible.value = true
}

const handleConfirmAssign = async () => {
  if (!assignForm.assignee) {
    ElMessage.warning('请输入指派人姓名')
    return
  }

  try {
    await axios.post('/api/air-conditioner/efficiencyOrders/assign', null, {
      params: {
        orderId: assignForm.orderId,
        assignee: assignForm.assignee
      }
    })
    ElMessage.success('指派成功')
    assignDialogVisible.value = false
    loadOrderList()
    loadStatistics()
  } catch (error) {
    ElMessage.error('指派失败')
  }
}

const handleStart = async (row: Order) => {
  try {
    await axios.post('/api/air-conditioner/efficiencyOrders/start', null, {
      params: { orderId: row.id }
    })
    ElMessage.success('开始处理成功')
    loadOrderList()
    loadStatistics()
  } catch (error) {
    ElMessage.error('开始处理失败')
  }
}

const handleComplete = (row: Order) => {
  completeForm.orderId = row.id
  completeForm.handleResult = ''
  completeForm.handleDescription = ''
  completeForm.photos = ''
  completeDialogVisible.value = true
}

const handleConfirmComplete = async () => {
  if (!completeForm.handleResult) {
    ElMessage.warning('请输入处理结果')
    return
  }

  try {
    await axios.post('/api/air-conditioner/efficiencyOrders/complete', null, {
      params: {
        orderId: completeForm.orderId,
        handleResult: completeForm.handleResult,
        handleDescription: completeForm.handleDescription,
        photos: completeForm.photos
      }
    })
    ElMessage.success('完成工单成功')
    completeDialogVisible.value = false
    loadOrderList()
    loadStatistics()
  } catch (error) {
    ElMessage.error('完成工单失败')
  }
}

const handleCancel = async (row: Order) => {
  const { value } = await ElMessageBox.prompt('请输入取消原因', '取消工单', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /\S/,
    inputErrorMessage: '取消原因不能为空'
  })

  try {
    await axios.post('/api/air-conditioner/efficiencyOrders/cancel', null, {
      params: {
        orderId: row.id,
        reason: value
      }
    })
    ElMessage.success('取消成功')
    loadOrderList()
    loadStatistics()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消失败')
    }
  }
}

// 初始化
onMounted(() => {
  loadRoomList()
  loadAcList()
  loadStatistics()
  loadOrderList()
})
</script>

<style scoped>
.efficiency-check-orders {
  padding: 20px;
  background: linear-gradient(135deg, #0d1b2a 0%, #1b263b 100%);
  min-height: 100vh;
  color: #fff;
}

.page-header {
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

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

:deep(.el-pagination) {
  color: #fff !important;
}

:deep(.el-dialog) {
  background: linear-gradient(135deg, #0d1b2a 0%, #1b263b 100%);
  border: 2px solid rgba(0, 240, 255, 0.5);
}

:deep(.el-dialog__title) {
  color: #00f0ff;
}
</style>
