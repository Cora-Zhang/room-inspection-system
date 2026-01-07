<template>
  <div class="shift-schedule-container">
    <el-card class="page-header">
      <div class="header-content">
        <h2>值班排班管理</h2>
        <div class="header-actions">
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            新增排班
          </el-button>
          <el-button type="success" @click="handleImport">
            <el-icon><Upload /></el-icon>
            Excel导入
          </el-button>
          <el-button type="info" @click="handlePeriodic">
            <el-icon><Calendar /></el-icon>
            周期性排班
          </el-button>
          <el-button @click="handleDingTalkSync">
            <el-icon><Connection /></el-icon>
            钉钉同步
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm" class="filter-form">
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="机房">
          <el-select v-model="queryForm.roomId" placeholder="选择机房" clearable>
            <el-option
              v-for="room in roomList"
              :key="room.id"
              :label="room.name"
              :value="room.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="值班人员">
          <el-input v-model="queryForm.staffId" placeholder="输入工号" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="schedule-table">
      <el-table :data="scheduleList" stripe border style="width: 100%">
        <el-table-column prop="scheduleDate" label="值班日期" width="120" />
        <el-table-column prop="shift" label="班次" width="100">
          <template #default="{ row }">
            <el-tag :type="row.shift === 'DAY' ? 'success' : 'warning'">
              {{ row.shift === 'DAY' ? '白班' : '夜班' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="staffName" label="值班人员" width="120" />
        <el-table-column prop="roomName" label="机房" width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="dataSource" label="来源" width="100">
          <template #default="{ row }">
            {{ getDataSourceText(row.dataSource) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
    >
      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="值班日期" prop="scheduleDate">
          <el-date-picker
            v-model="formData.scheduleDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="班次" prop="shift">
          <el-radio-group v-model="formData.shift">
            <el-radio label="DAY">白班（08:00-17:00）</el-radio>
            <el-radio label="NIGHT">夜班（18:00-次日07:00）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="值班人员" prop="staffId">
          <el-select v-model="formData.staffId" placeholder="选择值班人员" style="width: 100%">
            <el-option
              v-for="staff in staffList"
              :key="staff.id"
              :label="staff.name"
              :value="staff.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="机房" prop="roomId">
          <el-select v-model="formData.roomId" placeholder="选择机房" style="width: 100%">
            <el-option
              v-for="room in roomList"
              :key="room.id"
              :label="room.name"
              :value="room.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- Excel导入对话框 -->
    <el-dialog v-model="importDialogVisible" title="Excel导入" width="500px">
      <el-upload
        ref="uploadRef"
        :auto-upload="false"
        :on-change="handleFileChange"
        :limit="1"
        accept=".xlsx,.xls,.csv"
        drag
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            支持.xlsx、.xls、.csv格式，文件大小不超过10MB
          </div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleImportSubmit">导入</el-button>
      </template>
    </el-dialog>

    <!-- 周期性排班对话框 -->
    <el-dialog v-model="periodicDialogVisible" title="周期性排班" width="600px">
      <el-form :model="periodicForm" :rules="periodicRules" ref="periodicFormRef" label-width="120px">
        <el-form-item label="值班人员" prop="staffId">
          <el-select v-model="periodicForm.staffId" placeholder="选择值班人员" style="width: 100%">
            <el-option
              v-for="staff in staffList"
              :key="staff.id"
              :label="staff.name"
              :value="staff.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="机房" prop="roomId">
          <el-select v-model="periodicForm.roomId" placeholder="选择机房" style="width: 100%">
            <el-option
              v-for="room in roomList"
              :key="room.id"
              :label="room.name"
              :value="room.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="班次" prop="shift">
          <el-radio-group v-model="periodicForm.shift">
            <el-radio label="DAY">白班（08:00-17:00）</el-radio>
            <el-radio label="NIGHT">夜班（18:00-次日07:00）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="开始日期" prop="startDate">
          <el-date-picker
            v-model="periodicForm.startDate"
            type="date"
            placeholder="选择开始日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束日期" prop="endDate">
          <el-date-picker
            v-model="periodicForm.endDate"
            type="date"
            placeholder="选择结束日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="排班周期" prop="scheduleType">
          <el-radio-group v-model="periodicForm.scheduleType">
            <el-radio :label="1">每周轮换</el-radio>
            <el-radio :label="2">每月轮换</el-radio>
            <el-radio :label="3">季度轮换</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="periodicDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePeriodicSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type UploadFile } from 'element-plus'
import { Plus, Upload, Calendar, Connection, UploadFilled } from '@element-plus/icons-vue'
import axios from 'axios'

interface Schedule {
  id: string
  scheduleDate: string
  shift: string
  staffId: string
  staffName: string
  roomId: number
  roomName: string
  status: string
  dataSource: number
}

interface Staff {
  id: string
  name: string
  employeeId: string
}

interface Room {
  id: number
  name: string
}

// 数据
const scheduleList = ref<Schedule[]>([])
const staffList = ref<Staff[]>([])
const roomList = ref<Room[]>([])
const dateRange = ref<[string, string] | null>(null)

// 查询表单
const queryForm = reactive({
  startDate: '',
  endDate: '',
  roomId: null as number | null,
  staffId: ''
})

// 对话框
const dialogVisible = ref(false)
const dialogTitle = ref('新增排班')
const formRef = ref<FormInstance>()
const formData = reactive({
  id: '',
  scheduleDate: '',
  shift: 'DAY',
  staffId: '',
  roomId: null as number | null
})

const formRules = {
  scheduleDate: [{ required: true, message: '请选择值班日期', trigger: 'change' }],
  shift: [{ required: true, message: '请选择班次', trigger: 'change' }],
  staffId: [{ required: true, message: '请选择值班人员', trigger: 'change' }],
  roomId: [{ required: true, message: '请选择机房', trigger: 'change' }]
}

// Excel导入
const importDialogVisible = ref(false)
const uploadRef = ref()
const importFile = ref<File | null>(null)

// 周期性排班
const periodicDialogVisible = ref(false)
const periodicFormRef = ref<FormInstance>()
const periodicForm = reactive({
  staffId: '',
  roomId: null as number | null,
  shift: 'DAY',
  startDate: '',
  endDate: '',
  scheduleType: 1
})

const periodicRules = {
  staffId: [{ required: true, message: '请选择值班人员', trigger: 'change' }],
  roomId: [{ required: true, message: '请选择机房', trigger: 'change' }],
  shift: [{ required: true, message: '请选择班次', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }],
  scheduleType: [{ required: true, message: '请选择排班周期', trigger: 'change' }]
}

// 方法
const loadScheduleList = async () => {
  try {
    const params: any = {}
    if (dateRange.value) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    if (queryForm.roomId) {
      params.roomId = queryForm.roomId
    }
    if (queryForm.staffId) {
      params.staffId = queryForm.staffId
    }

    const res = await axios.get('/api/shift/schedule/list', { params })
    if (res.data.success) {
      scheduleList.value = res.data.data
    }
  } catch (error) {
    ElMessage.error('加载排班列表失败')
  }
}

const loadStaffList = async () => {
  try {
    const res = await axios.get('/api/staff/list')
    if (res.data.success) {
      staffList.value = res.data.data
    }
  } catch (error) {
    ElMessage.error('加载值班人员列表失败')
  }
}

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

const handleQuery = () => {
  loadScheduleList()
}

const handleReset = () => {
  dateRange.value = null
  queryForm.roomId = null
  queryForm.staffId = ''
  loadScheduleList()
}

const handleCreate = () => {
  dialogTitle.value = '新增排班'
  dialogVisible.value = true
  formData.id = ''
  formData.scheduleDate = ''
  formData.shift = 'DAY'
  formData.staffId = ''
  formData.roomId = null
}

const handleEdit = (row: Schedule) => {
  dialogTitle.value = '编辑排班'
  dialogVisible.value = true
  Object.assign(formData, row)
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (formData.id) {
          await axios.put('/api/shift/schedule/update', formData)
          ElMessage.success('更新成功')
        } else {
          await axios.post('/api/shift/schedule/create', formData)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        loadScheduleList()
      } catch (error) {
        ElMessage.error('操作失败')
      }
    }
  })
}

const handleDelete = async (id: string) => {
  try {
    await ElMessageBox.confirm('确认删除该排班吗？', '提示', {
      type: 'warning'
    })
    await axios.delete(`/api/shift/schedule/${id}`)
    ElMessage.success('删除成功')
    loadScheduleList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleImport = () => {
  importDialogVisible.value = true
}

const handleFileChange = (file: UploadFile) => {
  importFile.value = file.raw as File
}

const handleImportSubmit = async () => {
  if (!importFile.value) {
    ElMessage.warning('请选择要导入的文件')
    return
  }

  try {
    const formData = new FormData()
    formData.append('file', importFile.value)

    const res = await axios.post('/api/shift/schedule/import', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })

    if (res.data.success) {
      ElMessage.success(`导入成功：成功${res.data.data.successCount}条，失败${res.data.data.failCount}条`)
      importDialogVisible.value = false
      loadScheduleList()
    }
  } catch (error) {
    ElMessage.error('导入失败')
  }
}

const handlePeriodic = () => {
  periodicDialogVisible.value = true
}

const handlePeriodicSubmit = async () => {
  if (!periodicFormRef.value) return
  await periodicFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const res = await axios.post('/api/shift/schedule/periodic', periodicForm)
        if (res.data.success) {
          ElMessage.success(res.data.data.message)
          periodicDialogVisible.value = false
          loadScheduleList()
        }
      } catch (error) {
        ElMessage.error('创建周期性排班失败')
      }
    }
  })
}

const handleDingTalkSync = async () => {
  try {
    await ElMessageBox.confirm('确认从钉钉多维表同步排班数据吗？', '提示', {
      type: 'info'
    })

    const res = await axios.post('/api/shift/schedule/sync/dingtalk', {
      appId: '',
      tableId: ''
    })

    if (res.data.success) {
      ElMessage.success(`同步成功：成功${res.data.data.successCount}条，失败${res.data.data.failCount}条`)
      loadScheduleList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('同步失败')
    }
  }
}

// 辅助方法
const getStatusType = (status: string) => {
  const map: Record<string, any> = {
    SCHEDULED: 'info',
    IN_PROGRESS: 'primary',
    COMPLETED: 'success',
    MISSED: 'danger'
  }
  return map[status] || 'info'
}

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    SCHEDULED: '已排班',
    IN_PROGRESS: '值班中',
    COMPLETED: '已完成',
    MISSED: '缺勤'
  }
  return map[status] || status
}

const getDataSourceText = (dataSource: number) => {
  const map: Record<number, string> = {
    1: '手动',
    2: 'Excel导入',
    3: '钉钉同步'
  }
  return map[dataSource] || '未知'
}

onMounted(() => {
  loadScheduleList()
  loadStaffList()
  loadRoomList()
})
</script>

<style scoped lang="scss">
.shift-schedule-container {
  padding: 20px;

  .page-header {
    margin-bottom: 20px;

    .header-content {
      display: flex;
      justify-content: space-between;
      align-items: center;

      h2 {
        margin: 0;
        color: #00ff88;
        text-shadow: 0 0 10px rgba(0, 255, 136, 0.3);
      }

      .header-actions {
        display: flex;
        gap: 10px;
      }
    }
  }

  .filter-card {
    margin-bottom: 20px;

    .filter-form {
      margin: 0;
    }
  }

  .schedule-table {
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
}
</style>
