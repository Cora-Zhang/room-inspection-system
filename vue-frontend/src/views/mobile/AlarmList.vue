<template>
  <div class="alarm-list">
    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-select v-model="filterLevel" placeholder="告警级别" size="small" style="width: 100px">
        <el-option label="全部" value="" />
        <el-option label="紧急" value="CRITICAL" />
        <el-option label="重要" value="MAJOR" />
        <el-option label="一般" value="MINOR" />
      </el-select>
      <el-select v-model="filterStatus" placeholder="处理状态" size="small" style="width: 100px">
        <el-option label="全部" value="" />
        <el-option label="待处理" value="PENDING" />
        <el-option label="处理中" value="PROCESSING" />
        <el-option label="已处理" value="RESOLVED" />
      </el-select>
      <el-button type="primary" size="small" @click="handleRefresh">刷新</el-button>
    </div>

    <!-- 告警列表 -->
    <div v-loading="loading" class="alarm-items">
      <el-card
        v-for="alarm in alarmList"
        :key="alarm.id"
        class="alarm-card"
        shadow="hover"
        @click="handleViewDetail(alarm)"
      >
        <div class="alarm-header">
          <el-tag
            :type="getLevelType(alarm.level)"
            size="small"
          >
            {{ getLevelText(alarm.level) }}
          </el-tag>
          <span class="alarm-time">{{ formatTime(alarm.createTime) }}</span>
        </div>
        <div class="alarm-content">
          <div class="alarm-title">{{ alarm.alarmName }}</div>
          <div class="alarm-device">设备：{{ alarm.deviceName }}</div>
          <div class="alarm-message">{{ alarm.message }}</div>
        </div>
        <div class="alarm-footer">
          <el-tag :type="getStatusType(alarm.status)" size="small">
            {{ getStatusText(alarm.status) }}
          </el-tag>
          <el-button
            v-if="alarm.status === 'PENDING'"
            type="primary"
            size="small"
            @click.stop="handleProcess(alarm)"
          >
            处理
          </el-button>
        </div>
      </el-card>

      <!-- 空状态 -->
      <el-empty
        v-if="!loading && alarmList.length === 0"
        description="暂无告警信息"
      />
    </div>

    <!-- 告警详情对话框 -->
    <el-dialog v-model="detailVisible" title="告警详情" width="90%" top="5vh">
      <div v-if="currentAlarm" class="alarm-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="告警名称">
            {{ currentAlarm.alarmName }}
          </el-descriptions-item>
          <el-descriptions-item label="告警级别">
            <el-tag :type="getLevelType(currentAlarm.level)">
              {{ getLevelText(currentAlarm.level) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="设备名称">
            {{ currentAlarm.deviceName }}
          </el-descriptions-item>
          <el-descriptions-item label="告警时间">
            {{ formatTime(currentAlarm.createTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="告警内容">
            {{ currentAlarm.message }}
          </el-descriptions-item>
          <el-descriptions-item label="处理状态">
            <el-tag :type="getStatusType(currentAlarm.status)">
              {{ getStatusText(currentAlarm.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="处理人" v-if="currentAlarm.handlerName">
            {{ currentAlarm.handlerName }}
          </el-descriptions-item>
          <el-descriptions-item label="处理时间" v-if="currentAlarm.handleTime">
            {{ formatTime(currentAlarm.handleTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="处理说明" v-if="currentAlarm.handleNote">
            {{ currentAlarm.handleNote }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button
          v-if="currentAlarm && currentAlarm.status === 'PENDING'"
          type="primary"
          @click="handleProcess(currentAlarm)"
        >
          处理
        </el-button>
      </template>
    </el-dialog>

    <!-- 处理对话框 -->
    <el-dialog v-model="processVisible" title="处理告警" width="90%">
      <el-form :model="processForm" label-width="80px">
        <el-form-item label="处理说明">
          <el-input
            v-model="processForm.note"
            type="textarea"
            :rows="4"
            placeholder="请输入处理说明"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="processVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitProcess">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import axios from 'axios'

const loading = ref(false)
const filterLevel = ref('')
const filterStatus = ref('')
const alarmList = ref([])
const detailVisible = ref(false)
const processVisible = ref(false)
const currentAlarm = ref(null)

const processForm = ref({
  note: ''
})

const getLevelType = (level: string) => {
  const map = {
    CRITICAL: 'danger',
    MAJOR: 'warning',
    MINOR: 'info'
  }
  return map[level] || 'info'
}

const getLevelText = (level: string) => {
  const map = {
    CRITICAL: '紧急',
    MAJOR: '重要',
    MINOR: '一般'
  }
  return map[level] || '未知'
}

const getStatusType = (status: string) => {
  const map = {
    PENDING: 'danger',
    PROCESSING: 'warning',
    RESOLVED: 'success'
  }
  return map[status] || 'info'
}

const getStatusText = (status: string) => {
  const map = {
    PENDING: '待处理',
    PROCESSING: '处理中',
    RESOLVED: '已处理'
  }
  return map[status] || '未知'
}

const formatTime = (time: string) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

const fetchAlarmList = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/alarm/list', {
      params: {
        level: filterLevel.value,
        status: filterStatus.value,
        page: 1,
        pageSize: 20
      }
    })
    alarmList.value = response.data.data || []
  } catch (error) {
    ElMessage.error('获取告警列表失败')
  } finally {
    loading.value = false
  }
}

const handleRefresh = () => {
  fetchAlarmList()
}

const handleViewDetail = (alarm: any) => {
  currentAlarm.value = alarm
  detailVisible.value = true
}

const handleProcess = (alarm: any) => {
  currentAlarm.value = alarm
  processForm.value.note = ''
  detailVisible.value = false
  processVisible.value = true
}

const handleSubmitProcess = async () => {
  if (!processForm.value.note.trim()) {
    ElMessage.warning('请输入处理说明')
    return
  }

  try {
    await axios.post('/api/alarm/process', {
      alarmId: currentAlarm.value.id,
      note: processForm.value.note
    })
    ElMessage.success('处理成功')
    processVisible.value = false
    fetchAlarmList()
  } catch (error) {
    ElMessage.error('处理失败')
  }
}

onMounted(() => {
  fetchAlarmList()
})
</script>

<style scoped lang="scss">
.alarm-list {
  .filter-bar {
    display: flex;
    gap: 8px;
    margin-bottom: 16px;
  }

  .alarm-items {
    .alarm-card {
      margin-bottom: 12px;

      .alarm-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 8px;

        .alarm-time {
          font-size: 12px;
          color: #909399;
        }
      }

      .alarm-content {
        margin-bottom: 12px;

        .alarm-title {
          font-size: 16px;
          font-weight: 600;
          margin-bottom: 8px;
        }

        .alarm-device {
          font-size: 14px;
          color: #606266;
          margin-bottom: 4px;
        }

        .alarm-message {
          font-size: 14px;
          color: #909399;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }

      .alarm-footer {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }
    }
  }

  .alarm-detail {
    padding: 16px 0;
  }
}
</style>
