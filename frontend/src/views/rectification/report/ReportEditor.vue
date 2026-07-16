<template>
  <div class="report-editor">
    <el-card shadow="never" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>整改报告</span>
          <div>
            <el-button v-if="!readOnly" :loading="draftLoading" :disabled="!canEdit" @click="handleSaveDraft">保存草稿</el-button>
            <el-button v-if="!readOnly" type="success" :loading="genLoading" :disabled="!canEdit" @click="handleGenerate">生成报告</el-button>
            <el-button type="primary" plain icon="Download" @click="handleDownloadWord">下载Word报告</el-button>
            <el-button v-if="!readOnly" type="primary" :loading="submitLoading" :disabled="!canEdit" @click="handleSubmit">提交审批</el-button>
          </div>
        </div>
      </template>

      <div v-if="reportInfo.status" class="mb10">
        <el-alert v-if="closureInfo.status === '1'" title="销号审核已通过，问题已正式销号" type="success" :closable="false" show-icon />
        <el-alert v-else-if="showClosureRejectedTip" :title="readOnly ? '销号申请已驳回' : '销号申请已驳回，请根据补充整改要求修改报告后重新提交'" type="error" :closable="false" show-icon />
        <el-alert v-else-if="reportInfo.unitApproveStatus === '2'" :title="readOnly ? '整改报告已被单位负责人驳回' : '报告已驳回，请修改后重新提交'" type="error" :closable="false" show-icon />
        <el-alert v-else-if="reportInfo.status === '0'" title="草稿状态" type="info" :closable="false" show-icon />
        <el-alert v-else-if="reportInfo.status === '1'" title="已提交，待审批" type="warning" :closable="false" show-icon />
        <el-alert v-else-if="reportInfo.status === '2' && closureInfo.status !== '2'" title="单位负责人已审批通过" type="success" :closable="false" show-icon />
      </div>

      <el-input v-if="!readOnly" v-model="reportContent" type="textarea" :rows="18" placeholder="请输入整改报告内容..." :disabled="!canEdit" />
      <article v-else-if="reportContent" class="report-document">
        <h3>整改报告</h3>
        <p v-for="(line, index) in reportLines" :key="index" :class="{ section: isSectionTitle(line) }">
          {{ line }}
        </p>
      </article>
      <el-empty v-else description="暂无可查看的整改报告" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, watch, computed, getCurrentInstance } from 'vue'
import { getReport, addReport, generateReport, downloadReportWord, submitReport } from '@/api/rectification/report'
import { saveAs } from 'file-saver'
import request from '@/utils/request'

const { proxy } = getCurrentInstance()
const props = defineProps({
  taskId: { type: Number, required: true },
  readOnly: { type: Boolean, default: false }
})
const readOnly = computed(() => props.readOnly)
const loading = ref(false), genLoading = ref(false), draftLoading = ref(false), submitLoading = ref(false)
const reportContent = ref('')
const reportInfo = ref({})
const closureInfo = ref({})
const reportLines = computed(() => String(reportContent.value || '').split(/\r?\n/).map(line => line.trim()).filter(Boolean))
const showClosureRejectedTip = computed(() => {
  if (closureInfo.value.status !== '2') return false
  if (reportInfo.value.status === '1') return false
  return !isAfter(reportInfo.value.submitTime || reportInfo.value.updateTime, closureInfo.value.auditTime || closureInfo.value.updateTime || closureInfo.value.createTime)
})
const canEdit = computed(() => {
  if (readOnly.value) return false
  if (reportInfo.value.unitApproveStatus === '2') return true
  if (showClosureRejectedTip.value) return true
  return reportInfo.value.status !== '1' && reportInfo.value.status !== '2'
})

function isAfter(left, right) {
  if (!right) return true
  if (!left) return false
  const leftTime = new Date(left).getTime()
  const rightTime = new Date(right).getTime()
  return Number.isFinite(leftTime) && Number.isFinite(rightTime) && leftTime > rightTime
}

function loadReport() {
  if (!props.taskId) return
  loading.value = true
  const reportReq = getReport(props.taskId).then(res => {
    const data = res.data
    if (data) {
      reportInfo.value = data
      reportContent.value = data.reportContent || ''
    }
  }).catch(() => {})
  const closureReq = request({ url: '/rectification/closure/task/' + props.taskId + '/latest', method: 'get' }).then(res => {
    closureInfo.value = res.data || {}
  }).catch(() => {
    closureInfo.value = {}
  })
  Promise.all([reportReq, closureReq]).finally(() => { loading.value = false })
}

function handleSaveDraft() {
  draftLoading.value = true
  addReport({ taskId: props.taskId, reportContent: reportContent.value }).then(() => {
    proxy.$modal.msgSuccess('保存成功')
    loadReport()
  }).finally(() => { draftLoading.value = false })
}

function handleGenerate() {
  genLoading.value = true
  generateReport(props.taskId).then(res => {
    reportContent.value = res.data || res.msg || reportContent.value
    proxy.$modal.msgSuccess('生成成功')
  }).finally(() => { genLoading.value = false })
}

function handleDownloadWord() {
  downloadReportWord(props.taskId).then(response => {
    saveReportWord(response.data, reportFileName(response, '整改报告.docx'))
  })
}

function isSectionTitle(line) {
  return /^[一二三四五六七八九十]+、/.test(line || '')
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

function handleSubmit() {
  proxy.$modal.confirm('确认提交整改报告？').then(() => {
    submitLoading.value = true
    // always save first, then submit
    addReport({ taskId: props.taskId, reportContent: reportContent.value }).then(() => {
      return getReport(props.taskId)
    }).then(res => {
      const rid = (res.data || {}).reportId
      if (rid) return submitReport(rid)
    }).then(() => {
      proxy.$modal.msgSuccess('已提交审批')
      reportInfo.value.status = '1'
      reportInfo.value.unitApproveStatus = '0'
    }).finally(() => { submitLoading.value = false })
  }).catch(() => {})
}

watch(() => props.taskId, loadReport, { immediate: true })
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.card-header > div {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.mb10 { margin-bottom: 10px; }

.report-document {
  min-height: 260px;
  padding: 24px 28px;
  border: 1px solid #e4e9f0;
  background: #fff;
  color: #303b4a;
  line-height: 1.8;
}

.report-document h3 {
  margin: 0 0 24px;
  text-align: center;
  font-size: 20px;
}

.report-document p {
  margin: 0 0 10px;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
}

.report-document p.section {
  margin-top: 18px;
  color: #1f2f46;
  font-weight: 600;
}

@media (max-width: 768px) {
  .report-editor :deep(.el-card__header),
  .report-editor :deep(.el-card__body) {
    padding: 12px;
  }

  .card-header {
    align-items: stretch;
    flex-direction: column;
  }

  .card-header > div {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 8px;
  }

  .card-header .el-button {
    width: 100%;
    margin-left: 0;
  }

  .report-editor :deep(.el-textarea__inner) {
    min-height: 260px !important;
    font-size: 14px;
    line-height: 1.7;
  }

  .report-document {
    min-height: 220px;
    padding: 18px 16px;
  }
}

@media (max-width: 420px) {
  .card-header > div {
    grid-template-columns: 1fr;
  }
}
</style>
