<template>
  <el-dialog
    class="mobile-form-dialog closure-audit-dialog"
    v-model="visible"
    title="审核销号申请"
    width="650px"
    append-to-body
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <section class="audit-application-summary">
      <div class="audit-summary-heading">
        <span>销号申请</span>
        <el-tag type="warning" size="small">待审核</el-tag>
      </div>
      <h3>{{ form.issueTitle || '未命名问题' }}</h3>
      <div class="audit-summary-meta">
        <div><span>申请人</span><strong>{{ form.applicant || '-' }}</strong></div>
        <div><span>申请时间</span><strong>{{ form.applyTime || '-' }}</strong></div>
      </div>
      <div class="audit-remark">
        <span>销号附言</span>
        <p>{{ form.description || '-' }}</p>
      </div>
    </section>

    <section class="audit-files-section">
      <h4>整改材料</h4>
      <div class="audit-file-list">
        <el-button
          type="primary"
          plain
          icon="Document"
          :disabled="!form.taskId"
          @click="downloadReport"
        >
          Word整改报告
        </el-button>
        <el-button
          v-for="(file, idx) in form.attachments"
          :key="file.materialId || file.id || idx"
          plain
          icon="Paperclip"
          @click="downloadFile(file)"
        >
          {{ file.name || file.fileName || '附件' + (idx + 1) }}
        </el-button>
      </div>
      <span v-if="!form.attachments || form.attachments.length === 0" class="text-muted">暂无附件材料</span>
    </section>

    <el-form class="closure-audit-form" ref="formRef" :model="auditForm" :rules="rules" label-position="top">
      <el-form-item label="审核结论" prop="result">
        <el-radio-group v-model="auditForm.result" class="audit-result-options">
          <el-radio label="approved" border>
            <span class="approved-text">整改完成</span>
          </el-radio>
          <el-radio label="rejected" border>
            <span class="rejected-text">整改不到位</span>
          </el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item v-if="auditForm.result === 'approved'" label="审核意见" prop="opinion">
        <el-input
          v-model="auditForm.opinion"
          type="textarea"
          :rows="3"
          placeholder="可选，未填写时默认为“整改完成，同意销号”"
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
        <el-button :type="submitButtonType" :loading="submitLoading" @click="handleSubmit">
          {{ submitButtonText }}
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
import { downloadMaterial } from '@/api/rectification/material'

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

const submitButtonText = computed(() => {
  if (auditForm.result === 'approved') return '确认通过'
  if (auditForm.result === 'rejected') return '确认驳回'
  return '提交审核'
})

const submitButtonType = computed(() => {
  if (auditForm.result === 'approved') return 'success'
  if (auditForm.result === 'rejected') return 'danger'
  return 'primary'
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
  if (!auditForm.result) {
    proxy.$modal.msgWarning('请先选择“整改完成”或“整改不到位”')
    return
  }
  if (auditForm.result === 'rejected' && !auditForm.reworkRequirement.trim()) {
    proxy.$modal.msgWarning('请输入补充整改要求')
    return
  }

  try {
    await formRef.value.validate()
    const isApproved = auditForm.result === 'approved'
    const actionText = isApproved ? '通过' : '驳回'
    const auditOpinion = isApproved
      ? (auditForm.opinion.trim() || '整改完成，同意销号')
      : auditForm.reworkRequirement.trim()

    await proxy.$modal.confirm(
      `确认${actionText}该销号申请吗？`,
      '审核确认',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: isApproved ? 'success' : 'warning' }
    )
    submitLoading.value = true
    await auditClosure({
      closureId: form.id,
      auditResult: isApproved ? '1' : '2',
      auditOpinion,
      reRectRequired: isApproved ? null : auditForm.reworkRequirement
    })
    proxy.$modal.msgSuccess(`销号申请已${actionText}`)
    emit('success')
    handleClose()
  } catch (error) {
    // 取消确认或表单校验失败时保持弹窗开启。
  } finally {
    submitLoading.value = false
  }
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
  downloadMaterial(materialId).then(blob => {
    saveBlob(blob, file.fileName || file.name || ('material_' + materialId))
  })
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
  saveAs(new Blob([blob], { type: blob?.type || 'application/octet-stream' }), filename)
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

.audit-application-summary,
.audit-files-section,
.closure-audit-form {
  padding: 16px;
  border: 1px solid #e5eaf1;
  border-radius: 8px;
  background: #fff;
}

.audit-summary-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.audit-summary-heading > span {
  color: #7d8a9c;
  font-size: 12px;
}

.audit-application-summary h3 {
  margin: 8px 0 14px;
  color: #1f2f46;
  font-size: 17px;
  line-height: 1.45;
  overflow-wrap: anywhere;
}

.audit-summary-meta {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px 16px;
}

.audit-summary-meta span,
.audit-summary-meta strong,
.audit-remark > span {
  display: block;
  line-height: 1.4;
}

.audit-summary-meta span,
.audit-remark > span {
  color: #8a96a8;
  font-size: 11px;
}

.audit-summary-meta strong {
  margin-top: 3px;
  color: #34465e;
  font-size: 13px;
  font-weight: 500;
  overflow-wrap: anywhere;
}

.audit-remark {
  padding: 10px 12px;
  margin-top: 14px;
  border-radius: 6px;
  background: #f5f7fa;
}

.audit-remark p {
  margin: 4px 0 0;
  color: #34465e;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
}

.audit-files-section,
.closure-audit-form {
  margin-top: 12px;
}

.audit-files-section h4 {
  margin: 0 0 10px;
  color: #34465e;
  font-size: 14px;
}

.audit-file-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
}

.audit-file-list .el-button {
  width: 100%;
  min-width: 0;
  margin: 0;
  justify-content: flex-start;
  overflow: hidden;
}

.audit-file-list .el-button :deep(span) {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.closure-audit-form :deep(.el-form-item) {
  display: block;
  margin-bottom: 16px;
}

.closure-audit-form :deep(.el-form-item:last-child) {
  margin-bottom: 0;
}

.closure-audit-form :deep(.el-form-item__label) {
  display: block;
  height: auto;
  margin-bottom: 7px;
  padding: 0;
  color: #52657d;
  line-height: 1.4;
}

.audit-result-options {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  width: 100%;
  gap: 8px;
}

.audit-result-options :deep(.el-radio) {
  box-sizing: border-box;
  width: 100%;
  height: 42px;
  margin: 0;
  padding: 0 10px;
}

.closure-audit-form :deep(.el-textarea),
.closure-audit-form :deep(.el-textarea__inner) {
  width: 100%;
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
  font-size: 12px;
}

@media (max-width: 768px) {
  .audit-application-summary,
  .audit-files-section,
  .closure-audit-form {
    padding: 13px;
  }

  .audit-summary-meta,
  .audit-result-options,
  .audit-file-list {
    grid-template-columns: 1fr;
  }

  .dialog-footer {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 8px;
  }

  .dialog-footer .el-button {
    width: 100%;
    margin: 0;
  }
}
</style>
