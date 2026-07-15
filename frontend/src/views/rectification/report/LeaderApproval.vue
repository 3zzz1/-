<template>
  <el-dialog
    class="mobile-form-dialog leader-approval-dialog"
    v-model="visible"
    title="单位负责人审批"
    :width="dialogWidth"
    append-to-body
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="approval-section">
      <h4 class="section-title">整改报告文件</h4>
      <el-button type="primary" icon="Download" :disabled="!taskId" @click="handleDownloadWord">
        下载Word整改报告
      </el-button>
    </div>

    <el-divider />

    <div class="approval-section" v-if="materials.length > 0">
      <h4 class="section-title">佐证材料</h4>
      <div class="material-actions">
        <el-button
          v-for="m in materials"
          :key="m.materialId || m.id"
          type="primary"
          plain
          size="small"
          icon="Download"
          @click="handleDownloadMaterial(m)"
        >
          {{ m.fileName || '附件' }}
        </el-button>
      </div>
    </div>

    <el-divider />

    <div v-if="approvalInfo.unitApproveStatus" class="approval-section">
      <h4 class="section-title">上次审批信息</h4>
      <el-descriptions :column="1" border size="small">
        <el-descriptions-item label="审批结果">
          <el-tag v-if="approvalInfo.unitApproveStatus === '1'" type="success">通过</el-tag>
          <el-tag v-else-if="approvalInfo.unitApproveStatus === '2'" type="danger">驳回</el-tag>
          <el-tag v-else type="warning">待审批</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="审批时间">{{ approvalInfo.unitApproveTime || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="approvalInfo.unitApproveStatus === '2' ? '驳回原因' : '审批意见'">
          {{ approvalInfo.unitApproveOpinion || '-' }}
        </el-descriptions-item>
      </el-descriptions>
    </div>

    <el-divider v-if="approvalInfo.unitApproveStatus" />

    <el-form class="mobile-dialog-form" ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="审批结果" prop="result">
        <el-radio-group v-model="form.result">
          <el-radio label="approved">
            <span class="approved-text">通过</span>
          </el-radio>
          <el-radio label="rejected">
            <span class="rejected-text">驳回</span>
          </el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item v-if="form.result === 'approved'" label="审批意见" prop="opinion">
        <el-input
          v-model="form.opinion"
          type="textarea"
          :rows="4"
          placeholder="可选，未填写时默认为“同意”"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <el-form-item v-if="form.result === 'rejected'" label="驳回原因" prop="rejectReason">
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
          :type="submitButtonType"
          :loading="submitLoading"
          native-type="button"
          @click="handleSubmit"
        >
          {{ submitButtonText }}
        </el-button>
        <el-button native-type="button" @click="handleClose">取消</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup name="LeaderApproval">
import { reactive, ref, computed, watch, getCurrentInstance } from 'vue'
import { saveAs } from 'file-saver'
import { leaderApprove, downloadReportWord } from '@/api/rectification/report'
import { downloadMaterial } from '@/api/rectification/material'
import useUserStore from '@/store/modules/user'
import useAppStore from '@/store/modules/app'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  reportId: { type: Number, required: true },
  taskId: { type: [Number, String], default: null },
  content: { type: String, default: '' },
  report: { type: Object, default: () => ({}) },
  materials: { type: Array, default: () => [] }
})

const emit = defineEmits(['update:modelValue', 'success'])
const { proxy } = getCurrentInstance()
const userStore = useUserStore()
const appStore = useAppStore()

const formRef = ref(null)
const submitLoading = ref(false)

const visible = computed({
  get: () => props.modelValue,
  set: val => emit('update:modelValue', val)
})

const taskId = computed(() => props.taskId)
const approvalInfo = computed(() => props.report || {})
const dialogWidth = computed(() => appStore.device === 'mobile' ? '94vw' : '750px')
const submitButtonText = computed(() => {
  if (form.result === 'approved') return '审批通过'
  if (form.result === 'rejected') return '审批驳回'
  return '审批'
})
const submitButtonType = computed(() => {
  if (form.result === 'approved') return 'success'
  if (form.result === 'rejected') return 'danger'
  return 'primary'
})

const form = reactive({
  reportId: props.reportId,
  result: '',
  opinion: '',
  rejectReason: ''
})

const rules = reactive({
  result: [{ required: true, message: '请选择审批结果', trigger: 'change' }],
  rejectReason: [
    { required: true, message: '请输入驳回原因', trigger: 'blur' }
  ]
})

watch(() => props.reportId, val => {
  form.reportId = val
})

watch(
  () => [visible.value, props.reportId, props.report?.unitApproveStatus, props.report?.unitApproveOpinion],
  () => {
    if (visible.value) {
      fillLastApproval()
    }
  },
  { immediate: true }
)

watch(() => form.result, val => {
  if (val === 'approved') {
    form.rejectReason = ''
    formRef.value?.clearValidate('rejectReason')
  }
})

function handleDownloadWord() {
  if (!props.taskId) return
  downloadReportWord(props.taskId).then(response => {
    saveReportWord(response.data, currentUserReportName())
  })
}

async function saveReportWord(blob, filename) {
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
  saveAs(new Blob([blob], {
    type: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
  }), filename)
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

function currentUserReportName() {
  return (userStore.name || '登录用户') + '整改报告.docx'
}

function fillLastApproval() {
  form.reportId = props.reportId
  form.result = ''
  form.opinion = ''
  form.rejectReason = ''
  formRef.value?.clearValidate()
}

function handleDownloadMaterial(row) {
  const materialId = row.materialId || row.id
  if (!materialId) {
    proxy.$modal.msgWarning('材料ID不可用')
    return
  }
  downloadMaterial(materialId).then(blob => {
    saveMaterialBlob(blob, row.fileName || ('material_' + materialId))
  })
}

async function saveMaterialBlob(blob, filename) {
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

async function handleSubmit() {
  if (!formRef.value) return
  if (!form.result) {
    proxy.$modal.msgWarning('请先选择“通过”或“驳回”')
    return
  }
  if (form.result === 'rejected' && !form.rejectReason.trim()) {
    proxy.$modal.msgWarning('请输入驳回原因')
    return
  }

  try {
    await formRef.value.validate()
    const isApproved = form.result === 'approved'
    const actionText = isApproved ? '通过' : '驳回'
    await proxy.$modal.confirm(`确认${actionText}该整改报告吗？`, '审批确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: isApproved ? 'success' : 'warning'
    })
    submitLoading.value = true
    await leaderApprove({
      reportId: form.reportId,
      approveResult: isApproved ? '1' : '2',
      approveOpinion: isApproved ? (form.opinion.trim() || '同意') : form.rejectReason.trim()
    })
    proxy.$modal.msgSuccess(`报告已${actionText}`)
    emit('success')
    handleClose()
  } catch (error) {
    // 取消确认框或表单校验失败时保持弹窗开启。
  } finally {
    submitLoading.value = false
  }
}

function handleClose() {
  formRef.value?.resetFields()
  form.result = ''
  form.opinion = ''
  form.rejectReason = ''
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

.material-actions {
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

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

@media (max-width: 768px) {
  .material-actions,
  .dialog-footer {
    align-items: stretch;
    flex-direction: column;
  }

  .material-actions .el-button,
  .dialog-footer .el-button {
    width: 100%;
    margin-left: 0;
  }
}
</style>
