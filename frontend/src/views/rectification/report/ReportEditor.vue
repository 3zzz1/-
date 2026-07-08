<template>
  <div class="report-editor">
    <el-card shadow="never" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span>整改报告</span>
          <div>
            <el-button :loading="draftLoading" @click="handleSaveDraft">保存草稿</el-button>
            <el-button type="primary" :loading="submitLoading" @click="handleSubmit">提交审批</el-button>
          </div>
        </div>
      </template>

      <div v-if="reportInfo.status" class="mb10">
        <el-alert v-if="reportInfo.status === '0'" title="草稿状态" type="info" :closable="false" show-icon />
        <el-alert v-else-if="reportInfo.status === '1'" title="已提交，待审批" type="warning" :closable="false" show-icon />
        <el-alert v-else-if="reportInfo.status === '2'" title="领导已审批通过" type="success" :closable="false" show-icon />
      </div>

      <el-input v-model="reportContent" type="textarea" :rows="18" placeholder="请输入整改报告内容..." :disabled="reportInfo.status === '1' || reportInfo.status === '2'" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, watch, getCurrentInstance } from 'vue'
import { getReport, addReport, generateReport, submitReport } from '@/api/rectification/report'

const { proxy } = getCurrentInstance()
const props = defineProps({ taskId: { type: Number, required: true } })
const loading = ref(false), genLoading = ref(false), draftLoading = ref(false), submitLoading = ref(false)
const reportContent = ref('')
const reportInfo = ref({})

function loadReport() {
  if (!props.taskId) return
  loading.value = true
  getReport(props.taskId).then(res => {
    const data = res.data
    if (data) {
      reportInfo.value = data
      reportContent.value = data.reportContent || ''
    }
  }).catch(() => {}).finally(() => { loading.value = false })
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

function handleSubmit() {
  proxy.$modal.confirm('确认提交整改报告？').then(() => {
    submitLoading.value = true
    if (!reportInfo.value.reportId) {
      addReport({ taskId: props.taskId, reportContent: reportContent.value }).then(res => {
        const id = res.data?.reportId
        if (id) return submitReport(id)
      }).then(() => { proxy.$modal.msgSuccess('提交成功'); loadReport() }).finally(() => { submitLoading.value = false })
    } else {
      submitReport(reportInfo.value.reportId).then(() => {
        proxy.$modal.msgSuccess('提交成功'); loadReport()
      }).finally(() => { submitLoading.value = false })
    }
  }).catch(() => {})
}

watch(() => props.taskId, loadReport, { immediate: true })
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
.mb10 { margin-bottom: 10px; }
</style>
