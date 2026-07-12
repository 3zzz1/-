<template>
  <div class="report-editor">
    <el-card shadow="never" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>整改报告</span>
          <div>
            <el-button :loading="draftLoading" :disabled="!canEdit" @click="handleSaveDraft">保存草稿</el-button>
            <el-button type="success" :loading="genLoading" :disabled="!canEdit" @click="handleGenerate">生成报告</el-button>
            <el-button type="primary" plain icon="Download" @click="handleDownloadWord">下载Word报告</el-button>
            <el-button type="primary" :loading="submitLoading" :disabled="!canEdit" @click="handleSubmit">提交审批</el-button>
          </div>
        </div>
      </template>

      <div v-if="reportInfo.status" class="mb10">
        <el-alert v-if="showClosureRejectedTip" title="销号申请已驳回，请根据补充整改要求修改报告后重新提交" type="error" :closable="false" show-icon />
        <el-alert v-else-if="reportInfo.unitApproveStatus === '2'" title="报告已驳回，请修改后重新提交" type="error" :closable="false" show-icon />
        <el-alert v-else-if="reportInfo.status === '0'" title="草稿状态" type="info" :closable="false" show-icon />
        <el-alert v-else-if="reportInfo.status === '1'" title="已提交，待审批" type="warning" :closable="false" show-icon />
        <el-alert v-else-if="reportInfo.status === '2' && closureInfo.status !== '2'" title="单位负责人已审批通过" type="success" :closable="false" show-icon />
      </div>

      <el-input v-model="reportContent" type="textarea" :rows="18" placeholder="请输入整改报告内容..." :disabled="!canEdit" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, watch, computed, getCurrentInstance } from 'vue'
import { getReport, addReport, generateReport, downloadReportWord, submitReport } from '@/api/rectification/report'
import { saveAs } from 'file-saver'
import request from '@/utils/request'
import useUserStore from '@/store/modules/user'

const { proxy } = getCurrentInstance()
const userStore = useUserStore()
const props = defineProps({ taskId: { type: Number, required: true } })
const loading = ref(false), genLoading = ref(false), draftLoading = ref(false), submitLoading = ref(false)
const reportContent = ref('')
const reportInfo = ref({})
const closureInfo = ref({})
const showClosureRejectedTip = computed(() => {
  if (closureInfo.value.status !== '2') return false
  if (reportInfo.value.status === '1') return false
  return !isAfter(reportInfo.value.submitTime || reportInfo.value.updateTime, closureInfo.value.auditTime || closureInfo.value.updateTime || closureInfo.value.createTime)
})
const canEdit = computed(() => {
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
.card-header { display: flex; justify-content: space-between; align-items: center; }
.mb10 { margin-bottom: 10px; }
</style>
