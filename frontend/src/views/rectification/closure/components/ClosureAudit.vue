<template>
  <el-dialog
    v-model="visible"
    title="审核销号申请"
    width="650px"
    append-to-body
    :close-on-click-modal="false"
    @close="handleClose"
  >
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
      <el-descriptions-item label="销号附言" label-align="right">
        <div class="description-text">{{ form.description || '-' }}</div>
      </el-descriptions-item>
      <el-descriptions-item label="整改报告文档" label-align="right">
        <el-button
          type="primary"
          plain
          size="small"
          icon="Download"
          :disabled="!form.taskId"
          @click="downloadReport"
        >
          下载Word整改报告
        </el-button>
      </el-descriptions-item>
      <el-descriptions-item label="附件材料" label-align="right">
        <div v-if="form.attachments && form.attachments.length > 0" class="attachment-actions">
          <el-button
            v-for="(file, idx) in form.attachments"
            :key="file.materialId || file.id || idx"
            type="primary"
            plain
            size="small"
            icon="Download"
            @click="downloadFile(file)"
          >
            {{ file.name || file.fileName || '附件' + (idx + 1) }}
          </el-button>
        </div>
        <span v-else class="text-muted">暂无附件</span>
      </el-descriptions-item>
    </el-descriptions>

    <el-form ref="formRef" :model="auditForm" :rules="rules" label-width="120px">
      <el-form-item label="审核结果" prop="result">
        <el-radio-group v-model="auditForm.result">
          <el-radio label="approved">
            <span class="approved-text">整改完成（通过）</span>
          </el-radio>
          <el-radio label="rejected">
            <span class="rejected-text">整改不到位（驳回）</span>
          </el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item v-if="auditForm.result === 'approved'" label="审核意见" prop="opinion">
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
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          {{ auditForm.result === 'approved' ? '确认通过' : '确认驳回' }}
        </el-button>
        <el-button @click="handleClose">取消</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup name="ClosureAudit">
import { ref, reactive, computed, watch, getCurrentInstance } from 'vue'
import { saveAs } from 'file-saver'
import { auditClosure } from '@/api/rectification/closure'
import { downloadReportWord } from '@/api/rectification/report'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  closureData: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:modelValue', 'success'])
const { proxy } = getCurrentInstance()

const formRef = ref(null)
const submitLoading = ref(false)

const visible = computed({
  get: () => props.modelValue,
  set: val => emit('update:modelValue', val)
})

const form = reactive({
  id: null,
  taskId: null,
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
  reworkRequirement: [{ required: true, message: '请输入补充整改要求', trigger: 'blur' }]
}

watch(() => props.closureData, val => {
  if (!val) return
  form.id = val.id || val.closureId || null
  form.taskId = val.taskId || null
  form.issueTitle = val.issueTitle || val.title || ''
  form.applicant = val.applicant || val.applyBy || val.createBy || ''
  form.applyTime = val.applyTime || val.createTime || ''
  form.description = closureRemark(val)
  form.attachments = val.attachments || val.files || []
  auditForm.result = ''
  auditForm.opinion = ''
  auditForm.reworkRequirement = ''
}, { immediate: true, deep: true })

watch(() => auditForm.result, val => {
  if (val === 'approved') {
    auditForm.reworkRequirement = ''
    formRef.value?.clearValidate('reworkRequirement')
  } else if (val === 'rejected') {
    auditForm.opinion = ''
    formRef.value?.clearValidate('opinion')
  }
})

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(valid => {
    if (!valid) return
    submitLoading.value = true
    const isApproved = auditForm.result === 'approved'
    const actionText = isApproved ? '通过' : '驳回'
    const auditOpinion = isApproved ? auditForm.opinion : auditForm.reworkRequirement

    proxy.$modal.confirm(
      `确认${actionText}该销号申请吗？`,
      '审核确认',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: isApproved ? 'success' : 'warning' }
    ).then(() => auditClosure({
      closureId: form.id,
      auditResult: isApproved ? '1' : '2',
      auditOpinion,
      reRectRequired: isApproved ? null : auditForm.reworkRequirement
    })).then(() => {
      proxy.$modal.msgSuccess(`销号申请已${actionText}`)
      emit('success')
      handleClose()
    }).catch(() => {}).finally(() => {
      submitLoading.value = false
    })
  })
}

function downloadReport() {
  if (!form.taskId) {
    proxy.$modal.msgWarning('任务ID不可用')
    return
  }
  downloadReportWord(form.taskId).then(response => {
    saveBlob(response.data, reportOwnerFileName())
  })
}

function downloadFile(file) {
  const materialId = file.materialId || file.id
  if (!materialId) {
    proxy.$modal.msgWarning('材料ID不可用')
    return
  }
  proxy.download('/rectification/material/download/' + materialId, {}, file.fileName || file.name || 'download')
}

async function saveBlob(blob, filename) {
  if (blob && blob.type === 'application/json') {
    const text = await blob.text()
    let data = {}
    try {
      data = JSON.parse(text)
    } catch (e) {
      data = {}
    }
    proxy.$modal.msgError(data.msg || '下载失败')
    return
  }
  saveAs(new Blob([blob]), filename)
}

function reportFileName(response, fallback) {
  const disposition = response && response.headers ? response.headers['content-disposition'] : ''
  const utf8Match = disposition && disposition.match(/filename\*=UTF-8''([^;]+)/i)
  if (utf8Match) {
    const decoded = decodeURIComponent(utf8Match[1])
    return isGenericReportFileName(decoded) ? fallback : decoded
  }
  const nameMatch = disposition && disposition.match(/filename="?([^"]+)"?/i)
  if (nameMatch) {
    const decoded = decodeURIComponent(nameMatch[1])
    return isGenericReportFileName(decoded) ? fallback : decoded
  }
  return fallback
}

function isGenericReportFileName(filename) {
  return /^report_\d+\.docx$/i.test(filename || '') || /^rectification_report_\d+\.docx$/i.test(filename || '')
}

function reportOwnerFileName() {
  return (form.applicant || '整改人') + '整改报告.docx'
}

function closureRemark(row) {
  const content = row?.description || row?.applyContent || row?.content || ''
  if (!content) return ''
  return String(content).split('\n\n报告文件：')[0] || ''
}

function handleClose() {
  formRef.value?.resetFields()
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

.attachment-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.approved-text {
  color: #67c23a;
  font-weight: 500;
}

.rejected-text {
  color: #f56c6c;
  font-weight: 500;
}

.text-muted {
  color: #c0c4cc;
}
</style>
