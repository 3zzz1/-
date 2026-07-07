<template>
  <div class="report-editor">
    <el-card shadow="never" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span class="card-title">整改报告</span>
          <div class="header-actions">
            <el-button
              type="success"
              :loading="generateLoading"
              icon="MagicStick"
              @click="handleGenerateReport"
            >
              自动生成报告
            </el-button>
            <el-button :loading="draftLoading" icon="EditPen" @click="handleSaveDraft">
              保存草稿
            </el-button>
            <el-button
              type="primary"
              :loading="submitLoading"
              icon="Finished"
              @click="handleSubmitReport"
            >
              提交审批
            </el-button>
          </div>
        </div>
      </template>

      <!-- Report Status Banner -->
      <div v-if="reportInfo.status" class="status-banner">
        <el-alert
          v-if="reportInfo.status === 'draft'"
          title="当前为草稿状态，您可以继续编辑并提交审批"
          type="info"
          :closable="false"
          show-icon
        />
        <el-alert
          v-else-if="reportInfo.status === 'submitted'"
          title="报告已提交，等待单位领导审批中"
          type="warning"
          :closable="false"
          show-icon
        />
        <el-alert
          v-else-if="reportInfo.status === 'leader_approved'"
          title="单位领导已审批通过，已报送审计处"
          type="success"
          :closable="false"
          show-icon
        />
        <el-alert
          v-else-if="reportInfo.status === 'leader_rejected'"
          title="单位领导审批驳回，请修改后重新提交"
          type="error"
          :closable="false"
          show-icon
        />
      </div>

      <!-- Rich text editor -->
      <div class="editor-wrapper">
        <QuillEditor
          ref="quillRef"
          v-model:content="reportContent"
          contentType="html"
          theme="snow"
          :toolbar="toolbarOptions"
          :modules="editorModules"
          :readonly="isSubmitted"
          placeholder="请输入整改报告内容，或点击"自动生成报告"按钮根据整改方案和材料自动生成报告..."
          style="height: 480px"
        />
      </div>

      <!-- Report metadata -->
      <div v-if="reportInfo.updateTime" class="report-meta">
        <el-divider />
        <el-descriptions :column="4" size="small">
          <el-descriptions-item label="创建人">{{ reportInfo.createBy || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ reportInfo.createTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="最后更新">{{ reportInfo.updateTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="报告状态">
            <el-tag v-if="reportInfo.status === 'draft'" type="info" size="small">草稿</el-tag>
            <el-tag v-else-if="reportInfo.status === 'submitted'" type="warning" size="small">已提交</el-tag>
            <el-tag v-else-if="reportInfo.status === 'leader_approved'" type="success" size="small">领导已通过</el-tag>
            <el-tag v-else-if="reportInfo.status === 'leader_rejected'" type="danger" size="small">领导驳回</el-tag>
            <span v-else>未撰写</span>
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- Rejection feedback -->
      <div v-if="reportInfo.status === 'leader_rejected' && reportInfo.rejectOpinion" class="reject-feedback">
        <el-divider content-position="left">
          <span style="color: #f56c6c">驳回意见</span>
        </el-divider>
        <el-alert
          :title="reportInfo.rejectOpinion"
          type="error"
          :closable="false"
        >
          <template #default>
            <div class="reject-content">{{ reportInfo.rejectOpinion }}</div>
          </template>
        </el-alert>
      </div>
    </el-card>
  </div>
</template>

<script setup name="ReportEditor">
import { ref, reactive, computed, onMounted, watch, getCurrentInstance } from 'vue'
import { getReport, addReport, generateReport, submitReport } from '@/api/rectification/report'
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'

const props = defineProps({
  taskId: {
    type: Number,
    required: true
  }
})

const { proxy } = getCurrentInstance()
const quillRef = ref(null)
const loading = ref(false)
const generateLoading = ref(false)
const draftLoading = ref(false)
const submitLoading = ref(false)
const reportContent = ref('')

const reportInfo = reactive({
  id: null,
  status: '',
  createBy: '',
  createTime: '',
  updateTime: '',
  rejectOpinion: ''
})

const isSubmitted = computed(() => {
  return reportInfo.status === 'submitted' || reportInfo.status === 'leader_approved'
})

const toolbarOptions = [
  [{ header: [1, 2, 3, 4, false] }],
  ['bold', 'italic', 'underline', 'strike'],
  [{ color: [] }, { background: [] }],
  [{ font: [] }, { size: ['small', false, 'large', 'huge'] }],
  [{ list: 'ordered' }, { list: 'bullet' }, { list: 'check' }],
  [{ indent: '-1' }, { indent: '+1' }, { align: [] }],
  ['blockquote', 'code-block'],
  [{ table: [] }],
  ['link', 'image'],
  ['clean']
]

const editorModules = {
  clipboard: {
    matchVisual: false
  }
}

onMounted(() => {
  loadReport()
})

watch(
  () => props.taskId,
  () => {
    loadReport()
  }
)

function loadReport() {
  if (!props.taskId) return
  loading.value = true
  getReport(props.taskId)
    .then((response) => {
      const data = response.data
      if (data) {
        reportContent.value = data.reportContent || data.content || ''
        reportInfo.id = data.id || data.reportId || null
        reportInfo.status = data.status || ''
        reportInfo.createBy = data.createBy || ''
        reportInfo.createTime = data.createTime || ''
        reportInfo.updateTime = data.updateTime || ''
        reportInfo.rejectOpinion = data.rejectOpinion || data.leaderOpinion || ''
      }
    })
    .catch(() => {})
    .finally(() => {
      loading.value = false
    })
}

/** Auto-generate report from plan and materials */
async function handleGenerateReport() {
  proxy.$modal
    .confirm(
      '系统将根据整改方案和佐证材料自动生成整改报告草稿，生成后当前未保存的内容将被覆盖，确认继续？',
      '生成确认',
      {
        confirmButtonText: '确认生成',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    .then(() => {
      generateLoading.value = true
      return generateReport(props.taskId)
    })
    .then((response) => {
      const data = response.data
      if (data) {
        reportContent.value = data.reportContent || data.content || reportContent.value
        proxy.$modal.msgSuccess('报告已自动生成，请检查并编辑后保存')
      } else {
        proxy.$modal.msgWarning('报告生成失败，请手动撰写')
      }
    })
    .catch(() => {})
    .finally(() => {
      generateLoading.value = false
    })
}

/** Save draft */
async function handleSaveDraft() {
  if (!reportContent.value || reportContent.value.replace(/<[^>]*>/g, '').trim() === '') {
    proxy.$modal.msgWarning('报告内容不能为空')
    return
  }

  draftLoading.value = true
  const postData = {
    taskId: props.taskId,
    reportContent: reportContent.value,
    status: 'draft'
  }
  const apiCall = reportInfo.id
    ? addReport({ ...postData, id: reportInfo.id })
    : addReport(postData)

  apiCall
    .then(() => {
      proxy.$modal.msgSuccess('草稿已保存')
      loadReport()
    })
    .catch(() => {})
    .finally(() => {
      draftLoading.value = false
    })
}

/** Submit for approval */
async function handleSubmitReport() {
  if (!reportContent.value || reportContent.value.replace(/<[^>]*>/g, '').trim() === '') {
    proxy.$modal.msgWarning('报告内容不能为空')
    return
  }

  proxy.$modal
    .confirm('确认提交整改报告进行单位领导审批吗？提交后将不可修改。', '提交确认', {
      confirmButtonText: '确认提交',
      cancelButtonText: '取消',
      type: 'warning'
    })
    .then(() => {
      submitLoading.value = true
      // First save the report
      const postData = {
        taskId: props.taskId,
        reportContent: reportContent.value,
        status: 'submitted'
      }
      const saveCall = reportInfo.id
        ? addReport({ ...postData, id: reportInfo.id })
        : addReport(postData)

      return saveCall.then((saveRes) => {
        const savedId = saveRes.data?.id || saveRes.data?.reportId || reportInfo.id
        if (savedId) {
          return submitReport(savedId)
        }
        // If no ID returned, attempt submit with taskId directly
        return submitReport(reportInfo.id)
      })
    })
    .then(() => {
      proxy.$modal.msgSuccess('报告已提交，等待单位领导审批')
      loadReport()
    })
    .catch(() => {})
    .finally(() => {
      submitLoading.value = false
    })
}
</script>

<style scoped lang="scss">
.report-editor {
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

  .header-actions {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
  }

  .status-banner {
    margin-bottom: 16px;
  }

  .editor-wrapper {
    width: 100%;

    :deep(.ql-editor) {
      min-height: 420px;
      font-size: 14px;
      line-height: 1.8;
    }

    :deep(.ql-toolbar) {
      border-radius: 4px 4px 0 0;
      border-color: #dcdfe6;
    }

    :deep(.ql-container) {
      border-radius: 0 0 4px 4px;
      border-color: #dcdfe6;
    }
  }

  .report-meta {
    margin-top: 10px;
  }

  .reject-feedback {
    .reject-content {
      white-space: pre-wrap;
      line-height: 1.6;
      font-size: 13px;
    }
  }
}
</style>
