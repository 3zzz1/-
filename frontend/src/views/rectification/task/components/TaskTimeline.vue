<template>
  <div class="task-timeline" v-loading="loading">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">任务进展时间轴</span>
          <el-button type="primary" icon="Refresh" size="small" @click="loadProgress" :loading="loading">
            刷新
          </el-button>
        </div>
      </template>

      <!-- Timeline -->
      <div v-if="progressList.length > 0">
        <el-timeline>
          <el-timeline-item
            v-for="item in progressList"
            :key="item.progressId || item.id"
            :timestamp="item.operateTime || item.createTime || '-'"
            :color="getTimelineColor(item.progressType)"
            :type="getTimelineType(item.progressType)"
            :icon="getTimelineIcon(item.progressType)"
            placement="top"
          >
            <el-card shadow="hover" class="timeline-card">
              <div class="timeline-item-header">
                <el-tag
                  :color="getTimelineColor(item.progressType)"
                  effect="dark"
                  size="small"
                  :disable-transitions="true"
                >
                  {{ getProgressTypeLabel(item.progressType) }}
                </el-tag>
              </div>

              <div class="timeline-item-content">
                <p>{{ item.content || item.description || '-' }}</p>
              </div>

              <div class="timeline-item-footer">
                <span class="operator-info">
                  <el-icon><User /></el-icon>
                  {{ item.operatorName || item.operator || item.createBy || '系统' }}
                </span>
                <span class="timestamp-info">
                  <el-icon><Clock /></el-icon>
                  {{ item.operateTime || item.createTime || '-' }}
                </span>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>

      <el-empty v-else description="暂无进展记录" />

      <!-- Progress statistics -->
      <div v-if="progressList.length > 0" class="timeline-summary">
        <el-divider content-position="left">进展统计</el-divider>
        <el-row :gutter="16">
          <el-col :span="6" v-for="stat in progressStats" :key="stat.label">
            <div class="stat-item" :style="{ borderLeftColor: stat.color }">
              <div class="stat-count">{{ stat.count }}</div>
              <div class="stat-label">{{ stat.label }}</div>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>
  </div>
</template>

<script setup name="TaskTimeline">
import { ref, computed, watch } from 'vue'
import request from '@/utils/request'

const props = defineProps({
  taskId: {
    type: Number,
    required: true
  }
})

const loading = ref(false)
const progressList = ref([])

// Progress type map: key to Chinese label
const progressTypeMap = {
  'DISPATCH': '任务下发',
  'dispatch': '任务下发',
  'CONFIRM': '确认接收',
  'confirm': '确认接收',
  'ASSIGN': '分办责任人',
  'assign': '分办责任人',
  'PLAN_SUBMIT': '方案提交',
  'plan_submit': '方案提交',
  'PLAN_APPROVE': '方案审核通过',
  'plan_approve': '方案审核通过',
  'PLAN_REJECT': '方案驳回',
  'plan_reject': '方案驳回',
  'MATERIAL_UPLOAD': '材料上传',
  'material_upload': '材料上传',
  'REPORT_SUBMIT': '报告提交',
  'report_submit': '报告提交',
  'LEADER_APPROVE': '单位领导通过',
  'leader_approve': '单位领导通过',
  'LEADER_REJECT': '单位领导驳回',
  'leader_reject': '单位领导驳回',
  'EXTENSION_APPLY': '延期申请',
  'extension_apply': '延期申请',
  'EXTENSION_APPROVE': '延期审批通过',
  'extension_approve': '延期审批通过',
  'EXTENSION_REJECT': '延期审批驳回',
  'extension_reject': '延期审批驳回',
  'CLOSURE_APPLY': '销号申请',
  'closure_apply': '销号申请',
  'CLOSURE_APPROVED': '销号通过',
  'closure_approved': '销号通过',
  'CLOSURE_REJECTED': '销号驳回',
  'closure_rejected': '销号驳回',
  'COMPLETED': '整改完成',
  'completed': '整改完成',
  'REJECT': '退回',
  'reject': '退回'
}

// Color mapping by progress type
const timelineColors = {
  'DISPATCH': '#409EFF',
  'dispatch': '#409EFF',
  'CONFIRM': '#67C23A',
  'confirm': '#67C23A',
  'ASSIGN': '#409EFF',
  'assign': '#409EFF',
  'PLAN_SUBMIT': '#E6A23C',
  'plan_submit': '#E6A23C',
  'PLAN_APPROVE': '#67C23A',
  'plan_approve': '#67C23A',
  'PLAN_REJECT': '#F56C6C',
  'plan_reject': '#F56C6C',
  'MATERIAL_UPLOAD': '#909399',
  'material_upload': '#909399',
  'REPORT_SUBMIT': '#E6A23C',
  'report_submit': '#E6A23C',
  'LEADER_APPROVE': '#67C23A',
  'leader_approve': '#67C23A',
  'LEADER_REJECT': '#F56C6C',
  'leader_reject': '#F56C6C',
  'EXTENSION_APPLY': '#E6A23C',
  'extension_apply': '#E6A23C',
  'EXTENSION_APPROVE': '#67C23A',
  'extension_approve': '#67C23A',
  'EXTENSION_REJECT': '#F56C6C',
  'extension_reject': '#F56C6C',
  'CLOSURE_APPLY': '#E6A23C',
  'closure_apply': '#E6A23C',
  'CLOSURE_APPROVED': '#67C23A',
  'closure_approved': '#67C23A',
  'CLOSURE_REJECTED': '#F56C6C',
  'closure_rejected': '#F56C6C',
  'COMPLETED': '#67C23A',
  'completed': '#67C23A',
  'REJECT': '#F56C6C',
  'reject': '#F56C6C'
}

// Icon mapping by progress type
const timelineIcons = {
  'DISPATCH': 'Promotion',
  'dispatch': 'Promotion',
  'CONFIRM': 'CircleCheck',
  'confirm': 'CircleCheck',
  'ASSIGN': 'UserFilled',
  'assign': 'UserFilled',
  'PLAN_SUBMIT': 'EditPen',
  'plan_submit': 'EditPen',
  'PLAN_APPROVE': 'Checked',
  'plan_approve': 'Checked',
  'PLAN_REJECT': 'CircleClose',
  'plan_reject': 'CircleClose',
  'MATERIAL_UPLOAD': 'Upload',
  'material_upload': 'Upload',
  'REPORT_SUBMIT': 'Document',
  'report_submit': 'Document',
  'LEADER_APPROVE': 'Checked',
  'leader_approve': 'Checked',
  'LEADER_REJECT': 'CircleClose',
  'leader_reject': 'CircleClose',
  'EXTENSION_APPLY': 'Clock',
  'extension_apply': 'Clock',
  'EXTENSION_APPROVE': 'Clock',
  'extension_approve': 'Clock',
  'EXTENSION_REJECT': 'CircleClose',
  'extension_reject': 'CircleClose',
  'CLOSURE_APPLY': 'Finished',
  'closure_apply': 'Finished',
  'CLOSURE_APPROVED': 'CircleCheckFilled',
  'closure_approved': 'CircleCheckFilled',
  'CLOSURE_REJECTED': 'CircleClose',
  'closure_rejected': 'CircleClose',
  'COMPLETED': 'CircleCheckFilled',
  'completed': 'CircleCheckFilled',
  'REJECT': 'CircleClose',
  'reject': 'CircleClose'
}

// Computed statistics
const progressStats = computed(() => {
  const total = progressList.value.length
  const completed = progressList.value.filter(
    (t) => t.progressType === 'COMPLETED' || t.progressType === 'completed' ||
           t.progressType === 'CLOSURE_APPROVED' || t.progressType === 'closure_approved'
  ).length
  const rejected = progressList.value.filter(
    (t) => t.progressType === 'REJECT' || t.progressType === 'reject' ||
           t.progressType === 'LEADER_REJECT' || t.progressType === 'leader_reject' ||
           t.progressType === 'CLOSURE_REJECTED' || t.progressType === 'closure_rejected' ||
           t.progressType === 'PLAN_REJECT' || t.progressType === 'plan_reject'
  ).length
  const inProgress = total - completed - rejected

  return [
    { label: '全部记录', count: total, color: '#909399' },
    { label: '进行中', count: inProgress, color: '#409EFF' },
    { label: '已完成', count: completed, color: '#67C23A' },
    { label: '退回', count: rejected, color: '#F56C6C' }
  ]
})

function getProgressTypeLabel(type) {
  return progressTypeMap[type] || (type || '未知')
}

function getTimelineColor(type) {
  return timelineColors[type] || '#909399'
}

function getTimelineType(type) {
  const upperType = (type || '').toUpperCase()
  if (upperType.includes('REJECTED') || upperType === 'REJECT' || upperType.includes('REJECT')) {
    return 'danger'
  }
  if (upperType.includes('APPROVED') || upperType === 'CONFIRM' || upperType.includes('LEADER_APPROVE') ||
      upperType === 'COMPLETED' || upperType.includes('COMPLETED')) {
    return 'success'
  }
  if (upperType.includes('APPLY') || upperType.includes('SUBMIT')) {
    return 'warning'
  }
  return 'primary'
}

function getTimelineIcon(type) {
  return timelineIcons[type] || 'MoreFilled'
}

function loadProgress() {
  if (!props.taskId) return
  loading.value = true
  request({
    url: '/rectification/task/progress/' + props.taskId,
    method: 'get'
  })
    .then((res) => {
      const data = res.data || res
      if (Array.isArray(data)) {
        progressList.value = data
      } else if (data.rows) {
        progressList.value = data.rows
      } else if (data.records) {
        progressList.value = data.records
      } else {
        progressList.value = []
      }
    })
    .catch(() => {
      progressList.value = []
    })
    .finally(() => {
      loading.value = false
    })
}

watch(() => props.taskId, loadProgress, { immediate: true })
</script>

<style scoped lang="scss">
.task-timeline {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .card-title {
    font-size: 15px;
    font-weight: 600;
    color: #303133;
  }

  :deep(.el-timeline) {
    padding-left: 30px;
    padding-top: 10px;
  }

  :deep(.el-timeline-item__timestamp) {
    font-size: 12px;
    color: #909399;
  }

  .timeline-card {
    margin-bottom: 4px;

    :deep(.el-card__body) {
      padding: 12px 16px;
    }
  }

  .timeline-item-header {
    margin-bottom: 6px;
  }

  .timeline-item-content {
    margin-bottom: 8px;

    p {
      margin: 0;
      line-height: 1.6;
      color: #303133;
      font-size: 14px;
      white-space: pre-wrap;
      word-break: break-all;
    }
  }

  .timeline-item-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 12px;
    color: #909399;

    .operator-info,
    .timestamp-info {
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }

  .timeline-summary {
    margin-top: 24px;

    .stat-item {
      padding: 12px 16px;
      border-left: 3px solid #409eff;
      background: #fafafa;
      border-radius: 4px;
      text-align: center;

      .stat-count {
        font-size: 24px;
        font-weight: 700;
        color: #303133;
        line-height: 1.2;
      }

      .stat-label {
        font-size: 12px;
        color: #909399;
        margin-top: 4px;
      }
    }
  }
}
</style>
