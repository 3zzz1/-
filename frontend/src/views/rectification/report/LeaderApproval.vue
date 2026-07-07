<template>
  <el-dialog
    :title="title"
    v-model="visible"
    width="750px"
    append-to-body
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <!-- Report content (readonly) -->
    <div class="approval-section">
      <h4 class="section-title">整改报告内容</h4>
      <div class="report-content-preview" v-if="reportContent">
        <div class="ql-snow">
          <div class="ql-editor report-display" v-html="reportContent"></div>
        </div>
      </div>
      <el-empty v-else description="暂无报告内容" />
    </div>

    <el-divider />

    <!-- Approval form -->
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="审批结果" prop="result">
        <el-radio-group v-model="form.result" size="default">
          <el-radio value="approved">
            <span style="color: #67c23a; font-weight: 500">通过</span>
          </el-radio>
          <el-radio value="rejected">
            <span style="color: #f56c6c; font-weight: 500">驳回</span>
          </el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="审批意见" prop="opinion">
        <el-input
          v-model="form.opinion"
          type="textarea"
          :rows="4"
          placeholder="请输入审批意见"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <!-- Rejected reason (conditional) -->
      <el-form-item
        v-if="form.result === 'rejected'"
        label="驳回原因"
        prop="rejectReason"
      >
        <el-input
          v-model="form.rejectReason"
          type="textarea"
          :rows="3"
          placeholder="请说明驳回的具体原因和需要修改的内容"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button
          :type="form.result === 'approved' ? 'success' : 'danger'"
          :loading="submitLoading"
          @click="handleSubmit"
        >
          {{ form.result === 'approved' ? '审批通过' : '审批驳回' }}
        </el-button>
        <el-button @click="handleClose">取 消</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup name="LeaderApproval">
import { ref, reactive, computed, watch, getCurrentInstance } from 'vue'
import { getReport, leaderApprove } from '@/api/rectification/report'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  reportId: {
    type: Number,
    required: true
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const { proxy } = getCurrentInstance()
const formRef = ref(null)
const submitLoading = ref(false)
const reportContent = ref('')
const loading = ref(false)

const title = ref('单位领导审批')

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const form = reactive({
  reportId: props.reportId,
  result: '',
  opinion: '',
  rejectReason: ''
})

const rules = reactive({
  result: [{ required: true, message: '请选择审批结果', trigger: 'change' }],
  opinion: [{ required: true, message: '请输入审批意见', trigger: 'blur' }],
  rejectReason: [
    { required: true, message: '请输入驳回原因', trigger: 'blur' },
    { min: 10, message: '驳回原因不能少于10个字', trigger: 'blur' }
  ]
})

// Load report content when dialog opens
watch(
  () => props.modelValue,
  (val) => {
    if (val && props.reportId) {
      loadReportContent()
    }
  }
)

// Watch reportId changes
watch(
  () => props.reportId,
  (val) => {
    if (val) {
      form.reportId = val
    }
  }
)

// Clear reject reason when switching to approved
watch(
  () => form.result,
  (val) => {
    if (val === 'approved') {
      form.rejectReason = ''
      if (formRef.value) {
        formRef.value.clearValidate('rejectReason')
      }
    }
  }
)

function loadReportContent() {
  loading.value = true
  getReport(props.reportId)
    .then((response) => {
      const data = response.data
      if (data) {
        reportContent.value = data.reportContent || data.content || ''
      }
    })
    .catch(() => {
      reportContent.value = ''
    })
    .finally(() => {
      loading.value = false
    })
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate((valid) => {
    if (!valid) return

    const isApproved = form.result === 'approved'
    const actionText = isApproved ? '通过' : '驳回'

    proxy.$modal
      .confirm(
        `确认${actionText}该整改报告吗？`,
        '审批确认',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: isApproved ? 'success' : 'warning'
        }
      )
      .then(() => {
        submitLoading.value = true
        const postData = {
          reportId: form.reportId,
          approved: isApproved,
          leaderOpinion: form.opinion,
          rejectReason: isApproved ? null : form.rejectReason
        }
        return leaderApprove(postData)
      })
      .then(() => {
        proxy.$modal.msgSuccess(`报告已${actionText}`)
        emit('success')
        handleClose()
      })
      .catch(() => {})
      .finally(() => {
        submitLoading.value = false
      })
  })
}

function handleClose() {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  form.result = ''
  form.opinion = ''
  form.rejectReason = ''
  reportContent.value = ''
  visible.value = false
}
</script>

<style scoped lang="scss">
.approval-section {
  margin-bottom: 10px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 12px 0;
  padding-left: 8px;
  border-left: 3px solid #409eff;
}

.report-content-preview {
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid #ebeef5;
  border-radius: 4px;

  .report-display {
    min-height: 80px;
    padding: 16px;
    font-size: 14px;
    line-height: 1.8;
    color: #303133;
    white-space: normal;

    :deep(p) {
      margin-bottom: 8px;
    }

    :deep(table) {
      border-collapse: collapse;
      width: 100%;
      margin: 8px 0;

      td, th {
        border: 1px solid #dcdfe6;
        padding: 6px 10px;
      }
    }

    :deep(ul), :deep(ol) {
      padding-left: 24px;
    }

    :deep(img) {
      max-width: 100%;
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
