<template>
  <div class="door-access-container">
    <el-card class="page-header">
      <h2>门禁权限管理</h2>
    </el-card>

    <el-tabs v-model="activeTab" class="access-tabs">
      <!-- 门禁权限列表 -->
      <el-tab-pane label="权限列表" name="permission">
        <el-card class="filter-card">
          <el-form :inline="true" :model="permissionQuery" class="filter-form">
            <el-form-item label="机房">
              <el-select v-model="permissionQuery.roomId" placeholder="选择机房" clearable>
                <el-option
                  v-for="room in roomList"
                  :key="room.id"
                  :label="room.name"
                  :value="room.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="permissionQuery.status" placeholder="选择状态" clearable>
                <el-option label="未生效" :value="0" />
                <el-option label="生效中" :value="1" />
                <el-option label="已过期" :value="2" />
                <el-option label="已回收" :value="3" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadPermissionList">查询</el-button>
              <el-button @click="handlePermissionReset">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card class="table-card">
          <el-table :data="permissionList" stripe border style="width: 100%">
            <el-table-column prop="staffName" label="值班人员" width="120" />
            <el-table-column prop="staffNo" label="工号" width="120" />
            <el-table-column prop="roomName" label="机房" width="150" />
            <el-table-column prop="effectiveStartTime" label="生效开始时间" width="180" />
            <el-table-column prop="effectiveEndTime" label="生效结束时间" width="180" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getPermissionStatusType(row.status)">
                  {{ getPermissionStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="syncStatus" label="下发状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getSyncStatusType(row.syncStatus)">
                  {{ getSyncStatusText(row.syncStatus) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="doorSystemType" label="门禁系统" width="100">
              <template #default="{ row }">
                {{ row.doorSystemType === 1 ? '海康' : '大华' }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button
                  v-if="row.status < 2"
                  link
                  type="danger"
                  @click="handleRevokePermission(row)"
                >
                  回收
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- 临时权限申请 -->
      <el-tab-pane label="临时申请" name="request">
        <el-card class="header-actions">
          <el-button type="primary" @click="handleRequestCreate">
            <el-icon><Plus /></el-icon>
            申请临时权限
          </el-button>
        </el-card>

        <el-card class="filter-card">
          <el-form :inline="true" :model="requestQuery" class="filter-form">
            <el-form-item label="审批状态">
              <el-select v-model="requestQuery.approvalStatus" placeholder="选择状态" clearable>
                <el-option label="待审批" :value="0" />
                <el-option label="已通过" :value="1" />
                <el-option label="已拒绝" :value="2" />
                <el-option label="已撤销" :value="3" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadRequestList">查询</el-button>
              <el-button @click="handleRequestReset">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card class="table-card">
          <el-table :data="requestList" stripe border style="width: 100%">
            <el-table-column prop="applicantName" label="申请人" width="120" />
            <el-table-column prop="department" label="部门" width="150" />
            <el-table-column prop="roomName" label="访问机房" width="150" />
            <el-table-column prop="accessStartTime" label="访问开始时间" width="180" />
            <el-table-column prop="accessEndTime" label="访问结束时间" width="180" />
            <el-table-column prop="reason" label="访问事由" min-width="200" show-overflow-tooltip />
            <el-table-column prop="approvalStatus" label="审批状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getApprovalStatusType(row.approvalStatus)">
                  {{ getApprovalStatusText(row.approvalStatus) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="effectiveStatus" label="生效状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getEffectiveStatusType(row.effectiveStatus)">
                  {{ getEffectiveStatusText(row.effectiveStatus) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="250" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="handleRequestView(row)">查看</el-button>
                <el-button
                  v-if="row.approvalStatus === 0"
                  link
                  type="primary"
                  @click="handleRequestRevoke(row)"
                >
                  撤销
                </el-button>
                <el-button
                  v-if="row.approvalStatus === 0"
                  link
                  type="success"
                  @click="handleRequestApprove(row)"
                >
                  审批
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 新增临时权限申请对话框 -->
    <el-dialog
      v-model="requestDialogVisible"
      title="申请临时门禁权限"
      width="600px"
    >
      <el-form :model="requestForm" :rules="requestRules" ref="requestFormRef" label-width="120px">
        <el-form-item label="访问机房" prop="roomId">
          <el-select v-model="requestForm.roomId" placeholder="选择机房" style="width: 100%">
            <el-option
              v-for="room in roomList"
              :key="room.id"
              :label="room.name"
              :value="room.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="访问开始时间" prop="accessStartTime">
          <el-date-picker
            v-model="requestForm.accessStartTime"
            type="datetime"
            placeholder="选择开始时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="访问结束时间" prop="accessEndTime">
          <el-date-picker
            v-model="requestForm.accessEndTime"
            type="datetime"
            placeholder="选择结束时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DDTHH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="访问事由" prop="reason">
          <el-input
            v-model="requestForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入访问事由"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="requestDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleRequestSubmit">提交申请</el-button>
      </template>
    </el-dialog>

    <!-- 审批对话框 -->
    <el-dialog
      v-model="approveDialogVisible"
      title="审批临时权限申请"
      width="500px"
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="申请人">{{ approveRequest?.applicantName }}</el-descriptions-item>
        <el-descriptions-item label="部门">{{ approveRequest?.department }}</el-descriptions-item>
        <el-descriptions-item label="访问机房">{{ approveRequest?.roomName }}</el-descriptions-item>
        <el-descriptions-item label="访问时间">
          {{ approveRequest?.accessStartTime }} ~ {{ approveRequest?.accessEndTime }}
        </el-descriptions-item>
        <el-descriptions-item label="访问事由">{{ approveRequest?.reason }}</el-descriptions-item>
      </el-descriptions>
      <el-divider />
      <el-form :model="approveForm" ref="approveFormRef" label-width="100px">
        <el-form-item label="审批结果" required>
          <el-radio-group v-model="approveForm.approvalStatus">
            <el-radio :label="1">通过</el-radio>
            <el-radio :label="2">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审批意见">
          <el-input
            v-model="approveForm.comment"
            type="textarea"
            :rows="3"
            placeholder="请输入审批意见"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleApproveSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import axios from 'axios'

interface Room {
  id: number
  name: string
}

interface DoorAccessPermission {
  id: number
  staffName: string
  staffNo: string
  roomName: string
  effectiveStartTime: string
  effectiveEndTime: string
  status: number
  syncStatus: number
  doorSystemType: number
}

interface TempAccessRequest {
  id: number
  applicantName: string
  department: string
  roomId: number
  roomName: string
  accessStartTime: string
  accessEndTime: string
  reason: string
  approvalStatus: number
  effectiveStatus: number
}

// 数据
const activeTab = ref('permission')
const roomList = ref<Room[]>([])
const permissionList = ref<DoorAccessPermission[]>([])
const requestList = ref<TempAccessRequest[]>([])

// 权限查询
const permissionQuery = reactive({
  roomId: null as number | null,
  status: null as number | null
})

// 申请查询
const requestQuery = reactive({
  approvalStatus: null as number | null
})

// 申请对话框
const requestDialogVisible = ref(false)
const requestFormRef = ref<FormInstance>()
const requestForm = reactive({
  roomId: null as number | null,
  accessStartTime: '',
  accessEndTime: '',
  reason: ''
})

const requestRules = {
  roomId: [{ required: true, message: '请选择访问机房', trigger: 'change' }],
  accessStartTime: [{ required: true, message: '请选择访问开始时间', trigger: 'change' }],
  accessEndTime: [{ required: true, message: '请选择访问结束时间', trigger: 'change' }],
  reason: [{ required: true, message: '请输入访问事由', trigger: 'blur' }]
}

// 审批对话框
const approveDialogVisible = ref(false)
const approveRequest = ref<TempAccessRequest | null>(null)
const approveFormRef = ref<FormInstance>()
const approveForm = reactive({
  approvalStatus: 1,
  comment: ''
})

// 方法
const loadRoomList = async () => {
  try {
    const res = await axios.get('/api/room/list')
    if (res.data.success) {
      roomList.value = res.data.data
    }
  } catch (error) {
    ElMessage.error('加载机房列表失败')
  }
}

const loadPermissionList = async () => {
  try {
    const params: any = {}
    if (permissionQuery.roomId) {
      params.roomId = permissionQuery.roomId
    }
    if (permissionQuery.status !== null) {
      params.status = permissionQuery.status
    }

    const res = await axios.get('/api/door/access/permission/list', { params })
    if (res.data.success) {
      permissionList.value = res.data.data
    }
  } catch (error) {
    ElMessage.error('加载权限列表失败')
  }
}

const handlePermissionReset = () => {
  permissionQuery.roomId = null
  permissionQuery.status = null
  loadPermissionList()
}

const handleRevokePermission = async (row: DoorAccessPermission) => {
  try {
    await ElMessageBox.prompt('请输入回收原因', '回收门禁权限', {
      inputPattern: /.+/,
      inputErrorMessage: '回收原因不能为空'
    })

    const reason = '用户手动回收'

    await axios.post(`/api/door/access/permission/revoke/${row.id}`, null, {
      params: { reason }
    })

    ElMessage.success('回收成功')
    loadPermissionList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('回收失败')
    }
  }
}

const loadRequestList = async () => {
  try {
    const params: any = {}
    if (requestQuery.approvalStatus !== null) {
      params.approvalStatus = requestQuery.approvalStatus
    }

    const res = await axios.get('/api/door/access/temp-request/list', { params })
    if (res.data.success) {
      requestList.value = res.data.data
    }
  } catch (error) {
    ElMessage.error('加载申请列表失败')
  }
}

const handleRequestReset = () => {
  requestQuery.approvalStatus = null
  loadRequestList()
}

const handleRequestCreate = () => {
  requestDialogVisible.value = true
}

const handleRequestSubmit = async () => {
  if (!requestFormRef.value) return
  await requestFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await axios.post('/api/door/access/temp-request/create', requestForm)
        ElMessage.success('申请提交成功')
        requestDialogVisible.value = false
        loadRequestList()
      } catch (error) {
        ElMessage.error('申请提交失败')
      }
    }
  })
}

const handleRequestView = (row: TempAccessRequest) => {
  approveRequest.value = row
  approveDialogVisible.value = true
}

const handleRequestRevoke = async (row: TempAccessRequest) => {
  try {
    await ElMessageBox.confirm('确认撤销该申请吗？', '提示', {
      type: 'warning'
    })

    await ElMessageBox.prompt('请输入撤销原因', '撤销申请', {
      inputPattern: /.+/,
      inputErrorMessage: '撤销原因不能为空'
    })

    const reason = '用户手动撤销'

    await axios.post(`/api/door/access/temp-request/revoke/${row.id}`, null, {
      params: { reason }
    })

    ElMessage.success('撤销成功')
    loadRequestList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('撤销失败')
    }
  }
}

const handleRequestApprove = (row: TempAccessRequest) => {
  approveRequest.value = row
  approveForm.approvalStatus = 1
  approveForm.comment = ''
  approveDialogVisible.value = true
}

const handleApproveSubmit = async () => {
  if (!approveRequest.value) return

  try {
    await axios.post(`/api/door/access/temp-request/approve/${approveRequest.value.id}`, null, {
      params: {
        approverId: 1,
        approverName: '管理员',
        approvalStatus: approveForm.approvalStatus,
        comment: approveForm.comment
      }
    })

    ElMessage.success('审批成功')
    approveDialogVisible.value = false
    loadRequestList()
  } catch (error) {
    ElMessage.error('审批失败')
  }
}

// 辅助方法
const getPermissionStatusType = (status: number) => {
  const map: Record<number, any> = {
    0: 'info',
    1: 'success',
    2: 'warning',
    3: 'danger'
  }
  return map[status] || 'info'
}

const getPermissionStatusText = (status: number) => {
  const map: Record<number, string> = {
    0: '未生效',
    1: '生效中',
    2: '已过期',
    3: '已回收'
  }
  return map[status] || '未知'
}

const getSyncStatusType = (status: number) => {
  const map: Record<number, any> = {
    0: 'info',
    1: 'success',
    2: 'danger'
  }
  return map[status] || 'info'
}

const getSyncStatusText = (status: number) => {
  const map: Record<number, string> = {
    0: '未下发',
    1: '已下发',
    2: '下发失败'
  }
  return map[status] || '未知'
}

const getApprovalStatusType = (status: number) => {
  const map: Record<number, any> = {
    0: 'warning',
    1: 'success',
    2: 'danger',
    3: 'info'
  }
  return map[status] || 'info'
}

const getApprovalStatusText = (status: number) => {
  const map: Record<number, string> = {
    0: '待审批',
    1: '已通过',
    2: '已拒绝',
    3: '已撤销'
  }
  return map[status] || '未知'
}

const getEffectiveStatusType = (status: number) => {
  const map: Record<number, any> = {
    0: 'info',
    1: 'success',
    2: 'warning',
    3: 'danger'
  }
  return map[status] || 'info'
}

const getEffectiveStatusText = (status: number) => {
  const map: Record<number, string> = {
    0: '未生效',
    1: '生效中',
    2: '已过期',
    3: '已回收'
  }
  return map[status] || '未知'
}

onMounted(() => {
  loadRoomList()
  loadPermissionList()
  loadRequestList()
})
</script>

<style scoped lang="scss">
.door-access-container {
  padding: 20px;

  .page-header {
    margin-bottom: 20px;

    h2 {
      margin: 0;
      color: #00ff88;
      text-shadow: 0 0 10px rgba(0, 255, 136, 0.3);
    }
  }

  .access-tabs {
    :deep(.el-tabs__header) {
      margin-bottom: 20px;

      .el-tabs__nav-wrap::after {
        background-color: #333;
      }

      .el-tabs__item {
        color: #999;

        &.is-active {
          color: #00ff88;
        }

        &:hover {
          color: #00ff88;
        }
      }

      .el-tabs__active-bar {
        background-color: #00ff88;
      }
    }
  }

  .filter-card {
    margin-bottom: 20px;

    .filter-form {
      margin: 0;
    }
  }

  .header-actions {
    margin-bottom: 20px;
  }

  .table-card {
    :deep(.el-table) {
      background-color: transparent;

      th, td {
        border-color: #333;
      }

      th {
        background-color: rgba(0, 255, 136, 0.1);
        color: #00ff88;
      }

      tr:hover > td {
        background-color: rgba(0, 255, 136, 0.05) !important;
      }
    }
  }

  .el-divider {
    border-top-color: #333;
  }

  :deep(.el-descriptions) {
    .el-descriptions__body {
      background-color: rgba(0, 255, 136, 0.05);

      .el-descriptions__table {
        .el-descriptions__cell {
          border-color: #333;
        }
      }
    }

    .el-descriptions__label {
      color: #00ff88;
    }
  }
}
</style>
