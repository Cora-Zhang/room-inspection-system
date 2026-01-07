<template>
  <div class="inspection-verification-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">巡检核验体系</h1>
      <p class="page-subtitle">门禁日志核验 · 照片质量检查 · 完整性分析</p>
    </div>

    <!-- Tab切换 -->
    <el-tabs v-model="activeTab" class="custom-tabs">
      <!-- 门禁日志核验 -->
      <el-tab-pane label="门禁日志核验" name="access">
        <AccessVerification />
      </el-tab-pane>

      <!-- 照片核验 -->
      <el-tab-pane label="照片核验" name="photo">
        <PhotoVerification />
      </el-tab-pane>

      <!-- 巡检完整性分析 -->
      <el-tab-pane label="完整性分析" name="completeness">
        <CompletenessAnalysis />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import AccessVerification from './components/AccessVerification.vue'
import PhotoVerification from './components/PhotoVerification.vue'
import CompletenessAnalysis from './components/CompletenessAnalysis.vue'

const activeTab = ref('access')
</script>

<style scoped>
.inspection-verification-container {
  padding: 24px;
  min-height: 100vh;
  background: linear-gradient(135deg, #0a0e27 0%, #1a1f3a 100%);
}

.page-header {
  margin-bottom: 24px;
  padding: 24px;
  background: rgba(20, 30, 60, 0.8);
  border: 1px solid rgba(0, 212, 255, 0.3);
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 212, 255, 0.1);
}

.page-title {
  font-size: 28px;
  font-weight: bold;
  background: linear-gradient(90deg, #00d4ff, #7c3aed);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin: 0 0 8px 0;
}

.page-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
  margin: 0;
}

.custom-tabs {
  background: rgba(20, 30, 60, 0.6);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 8px;
  padding: 16px;
}

:deep(.el-tabs__nav-wrap::after) {
  background: transparent;
}

:deep(.el-tabs__item) {
  color: rgba(255, 255, 255, 0.6);
  font-size: 15px;
}

:deep(.el-tabs__item.is-active) {
  color: #00d4ff;
  font-weight: bold;
}

:deep(.el-tabs__active-bar) {
  background: linear-gradient(90deg, #00d4ff, #7c3aed);
  height: 3px;
}
</style>
