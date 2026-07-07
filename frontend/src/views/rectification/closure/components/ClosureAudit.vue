<template>
  <el-dialog
    :title="title"
    v-model="visible"
    width="650px"
    append-to-body
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <!-- Application Details (read-only) -->
    <el-descriptions :column="1" border size="default" style="margin-bottom: 20px">
      <el-descriptions-item label="问题标题" label-align="right" min-width="120">
        {{ form.issueTitle || '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="申请人" label-align="right">
        {{ form.applicant || '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="申请时间" label-align="right">
        {{ form.applyTime || '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="整改完成情况说明" label-align="right">
        <div class="description-text">{{ form.description || '-' }}</div>
      </el-descriptions-item>
      <el-descriptions-item label="附件材料" label-align="right">
        <div v-if="form.attachments && form.attachments.length > 0">
          <el-link
            v-for="(file, idx) in form.attachments"
            :key="idx"
            type="primary"
            :underline="false"
            style="margin-right: 12px"
            @click="downloadFile(file)"
          >
            <el-icon><Document /></el-icon>
            {{ file.name || file.fileName || '附件' + (idx + 1) }}
          </el-link>
        </div>
        <span v-else class="text-muted">无附件</span>
      </el-descriptions-item>
    </el-descriptions>

    <!-- Audit Form -->
    <el-form ref="formRef" :model="auditForm" :rules="rules" label-width="120px">
      <el-form-item label="审核结果" prop="result">
        <el-radio-group v-model="auditForm.result">
          <el-radio label="approved">
            <span style="color: #67C23A; font-weight: 500">整改完成（通过）</span>
          </el-radio>
          <el-radio label="rejected">
            <span style="color: #F56C6C; font-weight: 500">整改不到位（驳回）</span>
          </el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="审核意见" prop="opinion">
        <el-input
          v-model="auditForm.opinion"
          type="textarea"
          :rows="3"
          placeholder="请输入审核意见"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <el-form-item
        v-if="auditForm.result === 'rejected'"
        label="补充整改要求"
        prop="reworkRequirement"
      >
        <el-input
          v-model="auditForm.reworkRequirement"
          type="textarea"
          :rows="4"
          placeholder="请明确说明需要补充整改的具体内容和要求"
          maxlength="1000"
          show-word-limit
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          {{ auditForm.result === 'approved' ? '确认通过' : '确认驳回' }}
        </el-button>
        <el-button @click="handleClose">取 消</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup name="ClosureAudit">
import { ref, reactive, computed, watch } from 'vue'
import { auditClosure } from '@/api/rectification/closure'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  closureData: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const { proxy } = getCurrentInstance()
const formRef = ref(null)
const submitLoading = ref(false)

const title = ref('审核销号申请')
const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const form = reactive({
  id: null,
  issueTitle: '',
  applicant: '',
  applyTime: '',
  description: '',
  attachments: []
})

const auditForm = reactive({
  result: '',
  opinion: '',
  reworkRequirement: ''
})

const rules = {
  result: [{ required: true, message: '请选择审核结果', trigger: 'change' }],
  opinion: [{ required: true, message: '请输入审核意见', trigger: 'blur' }],
  reworkRequirement: [
    { required: true, message: '请输入补充整改要求', trigger: 'blur' },
    { min: 10, message: '补充整改要求不能少于10个字', trigger: 'blur' }
  ]
}

watch(() => props.closureData, (val) => {
  if (val && val.id) {
    form.id = val.id
    form.issueTitle = val.issueTitle || val.title || ''
    form.applicant = val.applicant || val.applyBy || val.createBy || ''
    form.applyTime = val.applyTime || val.createTime || ''
    form.description = val.description || val.applyContent || ''
    form.attachments = val.attachments || val.files || []
  }
  // Reset audit form
  auditForm.result = ''
  auditForm.opinion = ''
  auditForm.reworkRequirement = ''
}, { immediate: true, deep: true })

// Clear rework requirement when switching to approved
watch(() => auditForm.result, (val) => {
  if (val === 'approved') {
    auditForm.reworkRequirement = ''
    if (formRef.value) {
      formRef.value.clearValidate('reworkRequirement')
    }
  }
})

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate((valid) => {
    if (!valid) return
    submitLoading.value = true

    const isApproved = auditForm.result === 'approved'
    const actionText = isApproved ? '通过' : '驳回'

    proxy.$modal.confirm(
      `确认${actionText}该销号申请吗？`,
      '审核确认',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: isApproved ? 'success' : 'warning' }
    ).then(() => {
      return auditClosure({
        id: form.id,
        approved: isApproved,
        opinion: auditForm.opinion,
        reworkRequirement: isApproved ? null : auditForm.reworkRequirement
      })
    }).then(() => {
      proxy.$modal.msgSuccess(`销号申请已${actionText}`)
      emit('success')
      handleClose()
    }).catch(() => {}).finally(() => {
      submitLoading.value = false
    })
  })
}

function downloadFile(file) {
  const url = file.url || file.fileUrl || file.path
  if (url) {
    window.open(url, '_blank')
  } else {
    proxy.$modal.msgWarning('文件地址不可用')
  }
}

function handleClose() {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  auditForm.result = ''
  auditForm.opinion = ''
  auditForm.reworkRequirement = ''
  visible.value = false
}
</script>

<style scoped lang="scss">
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.description-text {
  white-space: pre-wrap;
  word-break: break-all;
  line-height: 1.6;
  color: #303133;
}

.text-muted {
  color: #c0c4cc;
}
</style>
