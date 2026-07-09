<template>
  <el-dialog
    :title="title"
    v-model="visible"
    width="700px"
    append-to-body
    :close-on-click-modal="false"
    @open="handleOpen"
    @close="handleClose"
  >
    <div class="notice-preview" v-loading="loading">
      <!-- Task basic info -->
      <el-card shadow="never" class="notice-card">
        <template #header>
          <div class="card-header">
            <span class="section-title">整改任务基本信息</span>
          </div>
        </template>
        <el-descriptions :column="2" border size="default">
          <el-descriptions-item label="任务编号" :span="1">
            {{ taskInfo.taskNo || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="任务状态" :span="1">
            <el-tag :type="statusTag(taskInfo.status)" size="small">
              {{ statusLabel(taskInfo.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="整改单位" :span="2">
            {{ taskInfo.rectifyDeptName || taskInfo.rectifyDept || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="联络人" :span="1">
            {{ taskInfo.contactPerson || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="联系电话" :span="1">
            {{ taskInfo.contactPhone || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="截止日期" :span="1">
            <span :style="{ color: isOverdue(taskInfo.deadline) ? '#f56c6c' : '', fontWeight: isOverdue(taskInfo.deadline) ? '600' : '' }">
              {{ taskInfo.deadline || '-' }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="下发时间" :span="1">
            {{ taskInfo.dispatchTime || taskInfo.createTime || '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- Issue Summary -->
      <el-card shadow="never" class="notice-card" v-if="issueInfo.issueNo">
        <template #header>
          <div class="card-header">
            <span class="section-title">问题摘要</span>
          </div>
        </template>
        <el-descriptions :column="2" border size="default">
          <el-descriptions-item label="问题编号" :span="2">
            {{ issueInfo.issueNo || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="问题标题" :span="2">
            {{ issueInfo.issueTitle || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="问题分类" :span="1">
            {{ issueInfo.issueCategory || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="风险等级" :span="1">
            <el-tag v-if="issueInfo.riskLevel" :type="riskLevelTag(issueInfo.riskLevel)" size="small">
              {{ riskLevelLabel(issueInfo.riskLevel) }}
            </el-tag>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="涉及金额" :span="1">
            {{ issueInfo.issueAmount ? issueInfo.issueAmount.toLocaleString() : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="来源类型" :span="1">
            <el-tag :type="sourceTypeTag(issueInfo.sourceType)" size="small">
              {{ sourceTypeLabel(issueInfo.sourceType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="问题描述" :span="2">
            <div class="content-text">{{ issueInfo.issueDesc || '-' }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="法规依据" :span="2">
            <div class="content-text">{{ issueInfo.legalBasis || '-' }}</div>
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- Rectification Requirement -->
      <el-card shadow="never" class="notice-card" v-if="taskInfo.requirement">
        <template #header>
          <div class="card-header">
            <span class="section-title">整改要求</span>
          </div>
        </template>
        <div class="content-text requirement-text">{{ taskInfo.requirement }}</div>
      </el-card>

      <!-- Notice content (if available from API) -->
      <el-card shadow="never" class="notice-card" v-if="noticeContent">
        <template #header>
          <div class="card-header">
            <span class="section-title">通知书内容</span>
          </div>
        </template>
        <div class="notice-content-preview">
          <div class="ql-snow">
            <div class="ql-editor" v-html="noticeContent"></div>
          </div>
        </div>
      </el-card>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button
          type="primary"
          icon="Download"
          :loading="downloadLoading"
          @click="handleDownloadNotice"
        >
          下载Word通知书
        </el-button>
        <el-button @click="handleClose">关 闭</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup name="NoticePreview">
import { ref, reactive, computed, watch, getCurrentInstance } from 'vue'
import { getTask, generateNotice } from '@/api/rectification/task'
import { getIssue } from '@/api/rectification/issue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  taskId: {
    type: Number,
    required: true
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const { proxy } = getCurrentInstance()
const loading = ref(false)
const downloadLoading = ref(false)
const noticeContent = ref('')

const title = ref('整改通知书预览')

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const taskInfo = reactive({
  taskNo: '',
  status: '',
  rectifyDeptName: '',
  rectifyDept: '',
  contactPerson: '',
  contactPhone: '',
  deadline: '',
  dispatchTime: '',
  createTime: '',
  requirement: ''
})

const issueInfo = reactive({
  issueNo: '',
  issueTitle: '',
  issueCategory: '',
  riskLevel: '',
  issueAmount: 0,
  sourceType: '',
  issueDesc: '',
  legalBasis: ''
})

const statusOptions = ref([
  { label: '待确认', value: '0' },
  { label: '整改中', value: '1' },
  { label: '已提交报告', value: '2' },
  { label: '待审核', value: '3' },
  { label: '已完成', value: '4' }
])

const sourceTypeOptions = ref([
  { label: '内源审计', value: 'inner' },
  { label: '外源巡视巡察', value: 'inspection' },
  { label: '外部督查', value: 'supervision' }
])

function statusLabel(val) {
  const item = statusOptions.value.find((d) => d.value === val)
  return item ? item.label : val
}

function statusTag(val) {
  const map = { '0': 'info', '1': 'warning', '2': 'primary', '3': 'success', '4': 'danger' }
  return map[val] || ''
}

function sourceTypeLabel(val) {
  const item = sourceTypeOptions.value.find((d) => d.value === val)
  return item ? item.label : val
}

function sourceTypeTag(val) {
  const map = { inner: '', inspection: 'warning', supervision: 'danger' }
  return map[val] || ''
}

function riskLevelLabel(val) {
  const map = { '1': '低', '2': '中', '3': '高' }
  return map[val] || val
}

function riskLevelTag(val) {
  const map = { '1': 'success', '2': 'warning', '3': 'danger' }
  return map[val] || ''
}

function isOverdue(deadline) {
  if (!deadline) return false
  return new Date(deadline) < new Date()
}

function handleOpen() {
  loadTaskInfo()
}

function loadTaskInfo() {
  if (!props.taskId) return
  loading.value = true

  getTask(props.taskId)
    .then((response) => {
      const data = response.data
      if (data) {
        taskInfo.taskNo = data.taskNo || ''
        taskInfo.status = data.status || ''
        taskInfo.rectifyDeptName = data.rectifyDeptName || data.rectifyDept || ''
        taskInfo.rectifyDept = data.rectifyDept || ''
        taskInfo.contactPerson = data.contactPerson || ''
        taskInfo.contactPhone = data.contactPhone || ''
        taskInfo.deadline = data.deadline || ''
        taskInfo.dispatchTime = data.dispatchTime || data.createTime || ''
        taskInfo.createTime = data.createTime || ''
        taskInfo.requirement = data.requirement || ''

        // Load related issue
        if (data.issueId) {
          loadIssueInfo(data.issueId)
        }
      }
    })
    .catch(() => {})
    .finally(() => {
      loading.value = false
    })
}

function loadIssueInfo(issueId) {
  getIssue(issueId)
    .then((response) => {
      const data = response.data
      if (data) {
        issueInfo.issueNo = data.issueNo || ''
        issueInfo.issueTitle = data.issueTitle || ''
        issueInfo.issueCategory = data.issueCategory || ''
        issueInfo.riskLevel = data.riskLevel || ''
        issueInfo.issueAmount = data.issueAmount || 0
        issueInfo.sourceType = data.sourceType || ''
        issueInfo.issueDesc = data.issueDesc || ''
        issueInfo.legalBasis = data.legalBasis || ''
      }
    })
    .catch(() => {})
}

/** Download Word notice */
function handleDownloadNotice() {
  downloadLoading.value = true
  generateNotice(props.taskId)
    .then((response) => {
      const blob = response instanceof Blob ? response : new Blob([response.data] || [response], {
        type: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
      })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `notice_${taskInfo.taskNo || props.taskId}_${new Date().getTime()}.docx`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
      proxy.$modal.msgSuccess('通知书已下载')
    })
    .catch(() => {
      proxy.$modal.msgError('通知书下载失败')
    })
    .finally(() => {
      downloadLoading.value = false
    })
}

function handleClose() {
  // Reset all data
  taskInfo.taskNo = ''
  taskInfo.status = ''
  taskInfo.rectifyDeptName = ''
  taskInfo.rectifyDept = ''
  taskInfo.contactPerson = ''
  taskInfo.contactPhone = ''
  taskInfo.deadline = ''
  taskInfo.dispatchTime = ''
  taskInfo.createTime = ''
  taskInfo.requirement = ''

  issueInfo.issueNo = ''
  issueInfo.issueTitle = ''
  issueInfo.issueCategory = ''
  issueInfo.riskLevel = ''
  issueInfo.issueAmount = 0
  issueInfo.sourceType = ''
  issueInfo.issueDesc = ''
  issueInfo.legalBasis = ''

  noticeContent.value = ''
  visible.value = false
}
</script>

<style scoped lang="scss">
.notice-preview {
  max-height: 60vh;
  overflow-y: auto;

  .notice-card {
    margin-bottom: 16px;

    &:last-child {
      margin-bottom: 0;
    }
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .section-title {
    font-size: 14px;
    font-weight: 600;
    color: #303133;
    padding-left: 8px;
    border-left: 3px solid #409eff;
  }

  .content-text {
    white-space: pre-wrap;
    word-break: break-all;
    line-height: 1.7;
    color: #303133;
    font-size: 14px;
  }

  .requirement-text {
    padding: 8px 0;
    line-height: 1.8;
  }

  .notice-content-preview {
    border: 1px solid #ebeef5;
    border-radius: 4px;

    :deep(.ql-editor) {
      min-height: 100px;
      padding: 12px 16px;
      font-size: 14px;
      line-height: 1.8;
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
