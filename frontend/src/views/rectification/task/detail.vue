<template>
  <div class="app-container">
    <div class="detail-header">
      <el-page-header @back="goBack" :content="'任务详情 - ' + (taskInfo.taskNo || '')">
        <template #title>
          <el-button link icon="ArrowLeft" @click="goBack">返回任务列表</el-button>
        </template>
      </el-page-header>
      <div class="header-actions">
        <el-tag :type="taskStatusTag(taskInfo.status)" size="large">
          {{ taskStatusLabel(taskInfo.status) }}
        </el-tag>
      </div>
    </div>

    <el-tabs v-model="activeTab" type="border-card" class="detail-tabs">
      <el-tab-pane label="基本信息" name="basic">
        <div class="tab-content">
          <el-card shadow="never" class="mb20">
            <template #header>
              <div class="card-header">
                <span class="card-title">任务信息</span>
              </div>
            </template>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="任务编号">{{ taskInfo.taskNo || '-' }}</el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="taskStatusTag(taskInfo.status)">{{ taskStatusLabel(taskInfo.status) }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="整改单位">{{ taskInfo.rectifyDeptName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="联络人">{{ taskInfo.contactPerson || '-' }}</el-descriptions-item>
              <el-descriptions-item label="联系电话">{{ taskInfo.contactPhone || '-' }}</el-descriptions-item>
              <el-descriptions-item label="截止日期">{{ taskInfo.deadline || '-' }}</el-descriptions-item>
              <el-descriptions-item label="下发时间">{{ taskInfo.dispatchTime || '-' }}</el-descriptions-item>
              <el-descriptions-item label="接收时间">{{ taskInfo.confirmTime || '-' }}</el-descriptions-item>
              <el-descriptions-item label="整改要求" :span="2">{{ taskInfo.requirement || '-' }}</el-descriptions-item>
            </el-descriptions>
          </el-card>

          <el-card shadow="never">
            <template #header>
              <div class="card-header">
                <span class="card-title">关联问题信息</span>
              </div>
            </template>
            <el-descriptions v-if="issueInfo.issueNo" :column="2" border>
              <el-descriptions-item label="问题编号">{{ issueInfo.issueNo }}</el-descriptions-item>
              <el-descriptions-item label="来源类型">
                <el-tag :type="sourceTypeTag(issueInfo.sourceType)">
                  {{ sourceTypeLabel(issueInfo.sourceType) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="问题标题" :span="2">{{ issueInfo.issueTitle }}</el-descriptions-item>
              <el-descriptions-item label="问题分类">
                <el-tag>{{ categoryLabel(issueInfo.issueCategory) }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="涉及金额">{{ formatAmount(issueInfo.issueAmount) }}</el-descriptions-item>
              <el-descriptions-item label="责任单位">{{ issueInfo.responsibleDeptId || '-' }}</el-descriptions-item>
              <el-descriptions-item label="责任干部">{{ issueInfo.responsiblePerson || '-' }}</el-descriptions-item>
              <el-descriptions-item label="风险等级">
                <el-tag :type="riskLevelTag(issueInfo.riskLevel)">{{ riskLevelLabel(issueInfo.riskLevel) }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="issueStatusTag(issueInfo.status)">{{ issueStatusLabel(issueInfo.status) }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="问题描述" :span="2">{{ issueInfo.issueDesc || '-' }}</el-descriptions-item>
              <el-descriptions-item label="定性法规依据" :span="2">{{ issueInfo.legalBasis || '-' }}</el-descriptions-item>
            </el-descriptions>
            <el-empty v-else description="暂无关联问题信息" />
          </el-card>
        </div>
      </el-tab-pane>

      <el-tab-pane label="整改方案" name="plan">
        <div class="tab-content">
          <PlanEditor :task-id="taskId" />
        </div>
      </el-tab-pane>

      <el-tab-pane label="整改报告" name="report">
        <div class="tab-content">
          <ReportEditor :task-id="taskId" :task-status="taskInfo.status" />
          <el-divider />
          <div class="material-header">
            <span class="card-title">佐证材料附件</span>
            <el-button type="primary" icon="Upload" size="small" @click="uploadOpen = true">上传佐证</el-button>
          </div>
          <div v-if="materialList.length > 0" class="material-list">
            <el-tag
              v-for="m in materialList"
              :key="m.materialId"
              closable
              type="info"
              class="material-tag"
              @close="handleDeleteMaterial(m.materialId)"
            >
              {{ m.fileName }}
            </el-tag>
          </div>
          <el-empty v-else description="暂无佐证材料" />
          <MaterialUpload
            v-model="uploadOpen"
            :task-id="taskId"
            :issue-id="issueInfo.issueId"
            @success="handleMaterialUploaded"
          />
          <div v-if="reportData.unitApproveStatus" class="approval-info">
            <el-divider content-position="left">单位负责人审批信息</el-divider>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="审批结果">
                <el-tag v-if="reportData.unitApproveStatus === '1'" type="success">通过</el-tag>
                <el-tag v-else-if="reportData.unitApproveStatus === '2'" type="danger">驳回</el-tag>
                <el-tag v-else type="warning">待审批</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="审批时间">{{ reportData.unitApproveTime || '-' }}</el-descriptions-item>
              <el-descriptions-item :label="reportData.unitApproveStatus === '2' ? '驳回原因' : '审批意见'" :span="2">
                {{ reportData.unitApproveOpinion || '-' }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
          <el-button
            v-if="reportData.reportId && reportData.status === '1'"
            v-hasPermi="['rectification:report:approve']"
            type="warning"
            icon="Checked"
            @click="approvalOpen = true"
          >
            审批报告
          </el-button>
          <LeaderApproval
            v-model="approvalOpen"
            :report-id="reportData.reportId"
            :task-id="taskId"
            :content="reportData.reportContent || ''"
            :report="reportData"
            :materials="materialList"
            @success="loadReport"
          />
        </div>
      </el-tab-pane>

      <el-tab-pane label="进展时间线" name="timeline">
        <div class="tab-content">
          <TaskTimeline :task-id="taskId" />
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup name="TaskDetail">
import { defineAsyncComponent, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTask } from '@/api/rectification/task'
import { getIssue } from '@/api/rectification/issue'
import { listMaterial, delMaterial } from '@/api/rectification/material'
import { getReport } from '@/api/rectification/report'

const PlanEditor = defineAsyncComponent(() => import('../plan/PlanEditor.vue'))
const MaterialUpload = defineAsyncComponent(() => import('../material/MaterialUpload.vue'))
const ReportEditor = defineAsyncComponent(() => import('../report/ReportEditor.vue'))
const LeaderApproval = defineAsyncComponent(() => import('../report/LeaderApproval.vue'))
const TaskTimeline = defineAsyncComponent(() => import('./components/TaskTimeline.vue'))

const router = useRouter()
const route = useRoute()

const taskId = ref(route.params.taskId)
const activeTab = ref(route.query.tab || 'basic')
const uploadOpen = ref(false)
const approvalOpen = ref(false)

const taskInfo = ref({})
const issueInfo = ref({})
const materialList = ref([])
const reportData = ref({})
const loading = ref(false)

const taskStatusOptions = [
  { label: '待确认', value: '0' },
  { label: '整改中', value: '1' },
  { label: '已提交报告', value: '2' },
  { label: '待审核', value: '3' },
  { label: '已完成', value: '4' }
]

const issueStatusOptions = [
  { label: '待下发', value: '0' },
  { label: '整改中', value: '1' },
  { label: '待审核', value: '2' },
  { label: '已销号', value: '3' },
  { label: '持续整改', value: '4' }
]

const sourceTypeOptions = [
  { label: '内源审计', value: '1' },
  { label: '外源巡视巡察', value: '2' },
  { label: '外部督查', value: '3' }
]

const categoryOptions = [
  { label: '资金类', value: 'FUND' },
  { label: '资产类', value: 'ASSET' },
  { label: '采购类', value: 'PURCHASE' },
  { label: '人事类', value: 'HR' },
  { label: '基建类', value: 'CONSTRUCTION' },
  { label: '其他', value: 'OTHER' }
]

function optionLabel(options, val) {
  const item = options.find(d => d.value === val)
  return item ? item.label : (val || '-')
}

function taskStatusLabel(val) {
  return optionLabel(taskStatusOptions, val)
}

function taskStatusTag(val) {
  const map = { '0': 'info', '1': 'warning', '2': 'primary', '3': '', '4': 'success' }
  return map[val] || ''
}

function issueStatusLabel(val) {
  return optionLabel(issueStatusOptions, val)
}

function issueStatusTag(val) {
  const map = { '0': 'info', '1': 'warning', '2': 'primary', '3': 'success', '4': 'danger' }
  return map[val] || ''
}

function sourceTypeLabel(val) {
  return optionLabel(sourceTypeOptions, val)
}

function sourceTypeTag(val) {
  const map = { '1': '', '2': 'warning', '3': 'danger' }
  return map[val] || ''
}

function categoryLabel(val) {
  return optionLabel(categoryOptions, val)
}

function riskLevelLabel(val) {
  const map = { '1': '低', '2': '中', '3': '高' }
  return map[val] || val || '-'
}

function riskLevelTag(val) {
  const map = { '1': 'success', '2': 'warning', '3': 'danger' }
  return map[val] || ''
}

function formatAmount(amount) {
  return amount ? Number(amount).toLocaleString() : '-'
}

function parseFirstIssueId(issueIds) {
  try {
    const ids = JSON.parse((issueIds || '[]').replace(/'/g, '"'))
    return Array.isArray(ids) && ids.length > 0 ? ids[0] : null
  } catch (e) {
    return null
  }
}

function loadTaskInfo() {
  loading.value = true
  getTask(taskId.value).then(response => {
    taskInfo.value = response.data || {}
    const issueId = parseFirstIssueId(taskInfo.value.issueIds)
    if (issueId) {
      getIssue(issueId).then(res => {
        issueInfo.value = res.data || {}
        loadMaterials()
      }).catch(() => {})
    }
    loadReport()
  }).finally(() => {
    loading.value = false
  })
}

function loadMaterials() {
  const issueId = issueInfo.value?.issueId || route.query.issueId
  if (!issueId) {
    materialList.value = []
    return
  }
  listMaterial(issueId).then(response => {
    materialList.value = response.rows || []
  }).catch(() => {
    materialList.value = []
  })
}

function loadReport() {
  getReport(taskId.value).then(response => {
    reportData.value = response.data || {}
  }).catch(() => {
    reportData.value = {}
  })
}

function handleDeleteMaterial(materialId) {
  ElMessageBox.confirm('确认删除该材料吗？', '提示', { type: 'warning' }).then(() => {
    delMaterial(materialId).then(() => {
      ElMessage.success('已删除')
      materialList.value = materialList.value.filter(m => m.materialId !== materialId)
    })
  }).catch(() => {})
}

function handleMaterialUploaded() {
  loadMaterials()
}

function goBack() {
  router.push('/rectification/my-tasks')
}

onMounted(() => {
  loadTaskInfo()
})
</script>

<style scoped>
.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}
.detail-tabs {
  margin-top: 10px;
}
.tab-content {
  padding: 10px 0;
}
.card-header,
.material-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-title {
  font-size: 14px;
  font-weight: 600;
}
.material-list {
  margin-bottom: 10px;
}
.material-tag {
  margin-right: 8px;
  margin-bottom: 4px;
}
.mb20 {
  margin-bottom: 20px;
}
</style>
