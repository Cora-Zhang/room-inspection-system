<template>
  <div class="workorder-list">
    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-select v-model="filterType" placeholder="工单类型" size="small" style="width: 100px">
        <el-option label="全部" value="" />
        <el-option label="巡检" value="INSPECTION" />
        <el-option label="维修" value="REPAIR" />
        <el-option label="保养" value="MAINTENANCE" />
        <el-option label="应急" value="EMERGENCY" />
      </el-select>
      <el-select v-model="filterStatus" placeholder="工单状态" size="small" style="width: 100px">
        <el-option label="全部" value="" />
        <el-option label="待处理" value="PENDING" />
        <el-option label="处理中" value="PROCESSING" />
        <el-option label="已完成" value="COMPLETED" />
      </el-select>
      <el-button type="primary" size="small" @click="handleRefresh">刷新</el-button>
    </div>

    <!-- 工单列表 -->
    <div v-loading="loading" class="workorder-items">
      <el-card
        v-for="workOrder in workOrderList"
        :key="workOrder.id"
        class="workorder-card"
        shadow="hover"
        @click="handleViewDetail(workOrder)"
      >
        <div class="workorder-header">
          <el-tag :type="getTypeType(workOrder.type)" size="small">
            {{ getTypeText(workOrder.type) }}
          </el-tag>
          <span class="workorder-code">{{ workOrder.orderCode }}</span>
        </div>
        <div class="workorder-content">
          <div class="workorder-title">{{ workOrder.title }}</div>
          <div class="workorder-device">设备：{{ workOrder.deviceName }}</div>
          <div class="workorder-priority">
            优先级：<el-tag :type="getPriorityType(workOrder.priority)" size="small">
              {{ getPriorityText(workOrder.priority) }}
            </el-tag>
          </div>
          <div class="workorder-assignee">
            责任人：{{ workOrder.assigneeName }}
          </div>
          <div class="workorder-deadline">
            截止时间：{{ formatTime(workOrder.deadline) }}
          </div>
        </div>
        <div class="workorder-footer">
          <el-tag :type="getStatusType(workOrder.status)" size="small">
            {{ getStatusText(workOrder.status) }}
          </el-tag>
          <el-button
            v-if="workOrder.status === 'PENDING' || workOrder.status === 'PROCESSING'"
            type="primary"
            size="small"
            @click.stop="handleProcess(workOrder)"
          >
            处理
          </el-button>
        </div>
      </el-card>

      <!-- 空状态 -->
      <el-empty
        v-if="!loading && workOrderList.length === 0"
        description="暂无工单信息"
      />
    </div>

    <!-- 工单详情对话框 -->
    <el-dialog v-model="detailVisible" title="工单详情" width="90%" top="5vh">
      <div v-if="currentWorkOrder" class="workorder-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="工单编号">
            {{ currentWorkOrder.orderCode }}
          </el-descriptions-item>
          <el-descriptions-item label="工单标题">
            {{ currentWorkOrder.title }}
          </el-descriptions-item>
          <el-descriptions-item label="工单类型">
            <el-tag :type="getTypeType(currentWorkOrder.type)">
              {{ getTypeText(currentWorkOrder.type) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="优先级">
            <el-tag :type="getPriorityType(currentWorkOrder.priority)">
              {{ getPriorityText(currentWorkOrder.priority) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="设备名称">
            {{ currentWorkOrder.deviceName }}
          </el-descriptions-item>
          <el-descriptions-item label="责任人">
            {{ currentWorkOrder.assigneeName }}
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ formatTime(currentWorkOrder.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="截止时间">
            {{ formatTime(currentWorkOrder.deadline) }}
          </el-descriptions-item>
          <el-descriptions-item label="工单状态">
            <el-tag :type="getStatusType(currentWorkOrder.status)">
              {{ getStatusText(currentWorkOrder.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="工单描述" :span="2">
            {{ currentWorkOrder.description }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button
          v-if="currentWorkOrder && (currentWorkOrder.status === 'PENDING' || currentWorkOrder.status === 'PROCESSING')"
          type="primary"
          @click="handleProcess(currentWorkOrder)"
        >
          处理
        </el-button>
      </template>
    </el-dialog>

    <!-- 处理对话框 -->
    <el-dialog v-model="processVisible" title="处理工单" width="90%">
      <el-form :model="processForm" label-width="80px">
        <el-form-item label="处理结果">
          <el-radio-group v-model="processForm.result">
            <el-radio label="COMPLETED">完成</el-radio>
            <el-radio label="REJECTED">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="处理说明">
          <el-input
            v-model="processForm.note"
            type="textarea"
            :rows="4"
            placeholder="请输入处理说明"
          />
        </el-form-item>
        <el-form-item label="现场照片">
          <el-upload
            v-model:file-list="processForm.photos"
            :action="uploadUrl"
            :headers="uploadHeaders"
            list-type="picture-card"
            :limit="6"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="processVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitProcess">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import axios from 'axios'

const loading = ref(false)
const filterType = ref('')
const filterStatus = ref('')
const workOrderList = ref([])
const detailVisible = ref(false)
const processVisible = ref(false)
const currentWorkOrder = ref(null)

const uploadUrl = import.meta.env.VITE_API_URL + '/api/file/upload'
const uploadHeaders = {
  'Authorization': 'Bearer ' + localStorage.getItem('token')
}

const processForm = ref({
  result: 'COMPLETED',
  note: '',
  photos: []
})

const getTypeType = (type: string) => {
  const map = {
    INSPECTION: 'info',
    REPAIR: 'warning',
    MAINTENANCE: 'primary',
    EMERGENCY: 'danger'
  }
  return map[type] || 'info'
}

const getTypeText = (type: string) => {
  const map = {
    INSPECTION: '巡检',
    REPAIR: '维修',
    MAINTENANCE: '保养',
    EMERGENCY: '应急'
  }
  return map[type] || '未知'
}

const getPriorityType = (priority: string) => {
  const map = {
    LOW: 'info',
    MEDIUM: 'warning',
    HIGH: 'danger',
    URGENT: 'danger'
  }
  return map[priority] || 'info'
}

const getPriorityText = (priority: string) => {
  const map = {
    LOW: '低',
    MEDIUM: '中',
    HIGH: '高',
    URGENT: '紧急'
  }
  return map[priority] || '未知'
}

const getStatusType = (status: string) => {
  const map = {
    PENDING: 'info',
    PROCESSING: 'warning',
    COMPLETED: 'success',
    REJECTED: 'danger'
  }
  return map[status] || 'info'
}

const getStatusText = (status: string) => {
  const map = {
    PENDING: '待处理',
    PROCESSING: '处理中',
    COMPLETED: '已完成',
    REJECTED: '已驳回'
  }
  return map[status] || '未知'
}

const formatTime = (time: string) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

const fetchWorkOrderList = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/workorder/my-list', {
      params: {
        type: filterType.value,
        status: filterStatus.value,
        page: 1,
        pageSize: 20
      }
    })
    workOrderList.value = response.data.data || []
  } catch (error) {
    ElMessage.error('获取工单列表失败')
  } finally {
    loading.value = false
  }
}

const handleRefresh = () => {
  fetchWorkOrderList()
}

const handleViewDetail = (workOrder: any) => {
  currentWorkOrder.value = workOrder
  detailVisible.value = true
}

const handleProcess = (workOrder: any) => {
  currentWorkOrder.value = workOrder
  processForm.value = {
    result: 'COMPLETED',
    note: '',
    photos: []
  }
  detailVisible.value = false
  processVisible.value = true
}

const handleSubmitProcess = async () => {
  if (!processForm.value.note.trim()) {
    ElMessage.warning('请输入处理说明')
    return
  }

  try {
    await axios.post('/api/workorder/process', {
      workOrderId: currentWorkOrder.value.id,
      result: processForm.value.result,
      note: processForm.value.note,
      photos: processForm.value.photos.map((f: any) => f.response?.data?.url || f.url)
    })
    ElMessage.success('提交成功')
    processVisible.value = false
    fetchWorkOrderList()
  } catch (error) {
    ElMessage.error('提交失败')
  }
}

onMounted(() => {
  fetchWorkOrderList()
})
</script>

<style scoped lang="scss">
.workorder-list {
  .filter-bar {
    display: flex;
    gap: 8px;
    margin-bottom: 16px;
  }

  .workorder-items {
    .workorder-card {
      margin-bottom: 12px;

      .workorder-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 8px;

        .workorder-code {
          font-size: 12px;
          color: #909399;
        }
      }

      .workorder-content {
        margin-bottom: 12px;

        .workorder-title {
          font-size: 16px;
          font-weight: 600;
          margin-bottom: 8px;
        }

        .workorder-device,
        .workorder-assignee,
        .workorder-deadline {
          font-size: 14px;
          color: #606266;
          margin-bottom: 4px;
        }

        .workorder-priority {
          font-size: 14px;
          color: #606266;
          margin-bottom: 4px;
          display: flex;
          align-items: center;
          gap: 8px;
        }
      }

      .workorder-footer {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }
    }
  }

  .workorder-detail {
    padding: 16px 0;
  }
}
</style>
