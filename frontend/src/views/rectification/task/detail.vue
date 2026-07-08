<template>
  <div class="app-container">
    <!-- Header -->
    <div class="detail-header">
      <el-page-header @back="goBack" :content="'任务详情 - ' + taskInfo.taskNo">
        <template #title>
          <el-button link icon="ArrowLeft" @click="goBack">返回任务列表</el-button>
        </template>
      </el-page-header>
      <div class="header-actions">
        <el-tag :type="taskStatusTag(taskInfo.status)" size="large">{{ taskStatusLabel(taskInfo.status) }}</el-tag>
      </div>
    </div>

    <!-- Tabs -->
    <el-tabs v-model="activeTab" type="border-card" class="detail-tabs">
      <!-- Tab 1: 基本信息 -->
      <el-tab-pane label="基本信息" name="basic">
        <div class="tab-content">
          <el-card shadow="never" class="mb20">
            <template #header>
              <div class="card-header">
                <span class="card-title">任务信息</span>
              </div>
            </template>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="任务编号">{{ taskInfo.taskNo }}</el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="taskStatusTag(taskInfo.status)">{{ taskStatusLabel(taskInfo.status) }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="整改单位">{{ taskInfo.rectifyDeptName }}</el-descriptions-item>
              <el-descriptions-item label="联络人">{{ taskInfo.contactPerson }}</el-descriptions-item>
              <el-descriptions-item label="联系电话">{{ taskInfo.contactPhone }}</el-descriptions-item>
              <el-descriptions-item label="截止日期">{{ taskInfo.deadline }}</el-descriptions-item>
              <el-descriptions-item label="下发时间">{{ taskInfo.dispatchTime }}</el-descriptions-item>
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
            <el-descriptions :column="2" border v-if="issueInfo.issueNo">
              <el-descriptions-item label="问题编号">{{ issueInfo.issueNo }}</el-descriptions-item>
              <el-descriptions-item label="来源类型">
                <el-tag :type="sourceTypeTag(issueInfo.sourceType)">{{ sourceTypeLabel(issueInfo.sourceType) }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="问题标题" :span="2">{{ issueInfo.issueTitle }}</el-descriptions-item>
              <el-descriptions-item label="问题分类">
                <el-tag>{{ categoryLabel(issueInfo.issueCategory) }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="涉及金额">{{ issueInfo.issueAmount ? issueInfo.issueAmount.toLocaleString() : '-' }}</el-descriptions-item>
              <el-descriptions-item label="责任单位">{{ issueInfo.responsibleDeptId }}</el-descriptions-item>
              <el-descriptions-item label="责任干部">{{ issueInfo.responsiblePerson }}</el-descriptions-item>
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

      <!-- Tab 2: 整改方案 -->
      <el-tab-pane label="整改方案" name="plan">
        <div class="tab-content">
          <PlanEditor :task-id="taskId" />
        </div>
      </el-tab-pane>

      <!-- Tab 3: 整改报告（含佐证材料上传） -->
      <el-tab-pane label="整改报告" name="report">
        <div class="tab-content">
          <ReportEditor :task-id="taskId" :task-status="taskInfo.status" />
          <el-divider />
          <div style="display:flex;align-items:center;gap:10px;margin-bottom:10px">
            <span style="font-weight:600">佐证材料附件：</span>
            <el-button type="primary" icon="Upload" @click="uploadOpen = true" size="small">上传佐证</el-button>
          </div>
          <div v-if="materialList.length > 0" style="margin-bottom:10px">
            <el-tag v-for="m in materialList" :key="m.materialId" closable type="info" style="margin-right:8px;margin-bottom:4px" @close="handleDeleteMaterial(m.materialId)">{{ m.fileName }}</el-tag>
          </div>
          <MaterialUpload v-model="uploadOpen" :task-id="taskId" :issue-id="issueInfo.issueId" @success="handleMaterialUploaded" />
          <el-button type="warning" icon="Checked" @click="approvalOpen = true" v-if="reportData.reportId && reportData.status === '1'" v-hasPermi="['rectification:report:approve']">审批报告</el-button>
          <LeaderApproval v-model="approvalOpen" :report-id="(reportData || {}).reportId" :content="reportData.reportContent || ''" @success="loadReport" />
        </div>
      </el-tab-pane>

      <!-- Tab 4: 进展时间轴 -->
      <el-tab-pane label="进展时间轴" name="timeline">
        <div class="tab-content">
          <TaskTimeline :task-id="taskId" />
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup name="TaskDetail">
import { ref, reactive, onMounted, watch, getCurrentInstance } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getTask, confirmTask } from '@/api/rectification/task'
import { getIssue } from '@/api/rectification/issue'
import { getPlan } from '@/api/rectification/plan'
import { listMaterial, delMaterial } from '@/api/rectification/material'
import { getReport } from '@/api/rectification/report'
import { getOverview } from '@/api/rectification/statistics'

// Sub-components (lazy loaded to avoid Quill init issues)
import { defineAsyncComponent } from 'vue'
const PlanEditor = defineAsyncComponent(() => import('../plan/PlanEditor.vue'))
const MaterialList = defineAsyncComponent(() => import('../material/MaterialList.vue'))
const MaterialUpload = defineAsyncComponent(() => import('../material/MaterialUpload.vue'))
const ReportEditor = defineAsyncComponent(() => import('../report/ReportEditor.vue'))
const LeaderApproval = defineAsyncComponent(() => import('../report/LeaderApproval.vue'))
const TaskTimeline = defineAsyncComponent(() => import('./components/TaskTimeline.vue'))

const router = useRouter()
const route = useRoute()
const { proxy } = getCurrentInstance()

const taskId = ref(route.params.taskId)
const activeTab = ref(route.query.tab || 'basic')
const uploadOpen = ref(false)
const approvalOpen = ref(false)

const taskInfo = ref({})
const issueInfo = ref({})
const planData = ref({})
const materialList = ref([])
const reportData = ref({})
const loading = ref(false)

// 状态下拉
const taskStatusOptions = ref([
  { label: '待接收', value: '0' },
  { label: '整改中', value: '1' },
  { label: '待审核', value: '2' },
  { label: '已销号', value: '3' },
  { label: '持续整改', value: '4' }
])

const issueStatusOptions = ref([
  { label: '待下发', value: '0' },
  { label: '整改中', value: '1' },
  { label: '待审核', value: '2' },
  { label: '已销号', value: '3' },
  { label: '持续整改', value: '4' }
])

const sourceTypeOptions = ref([
  { label: '内源审计', value: '1' },
  { label: '外源巡视巡察', value: '2' },
  { label: '外部督查', value: '3' }
])
const categoryOptions = [
  { label: '资金类', value: 'FUND' }, { label: '资产类', value: 'ASSET' },
  { label: '采购类', value: 'PURCHASE' }, { label: '人事类', value: 'HR' },
  { label: '基建类', value: 'CONSTRUCTION' }, { label: '其他', value: 'OTHER' }
]

function taskStatusLabel(val) {
  const item = taskStatusOptions.value.find(d => d.value === val)
  return item ? item.label : val
}
function taskStatusTag(val) {
  const map = { '0': 'info', '1': 'warning', '2': 'primary', '3': 'success', '4': 'danger' }
  return map[val] || ''
}
function issueStatusLabel(val) {
  const item = issueStatusOptions.value.find(d => d.value === val)
  return item ? item.label : val
}
function issueStatusTag(val) {
  const map = { '0': 'info', '1': 'warning', '2': 'primary', '3': 'success', '4': 'danger' }
  return map[val] || ''
}
function sourceTypeLabel(val) {
  const item = sourceTypeOptions.value.find(d => d.value === val)
  return item ? item.label : val
}
function sourceTypeTag(val) {
  const map = { '1': '', '2': 'warning', '3': 'danger' }
  return map[val] || ''
}
function categoryLabel(val) {
  const item = categoryOptions.find(d => d.value === val)
  return item ? item.label : val
}
function riskLevelLabel(val) {
  const map = { '1': '低', '2': '中', '3': '高' }
  return map[val] || val
}
function riskLevelTag(val) {
  const map = { '1': 'success', '2': 'warning', '3': 'danger' }
  return map[val] || ''
}

/** 加载任务信息 */
function loadTaskInfo() {
  loading.value = true
  getTask(taskId.value).then(response => {
    taskInfo.value = response.data || {}
    try {
      const ids = JSON.parse(taskInfo.value.issueIds || '[]')
      if (ids.length > 0) {
        getIssue(ids[0]).then(res => {
          issueInfo.value = res.data || {}
          loadMaterials()
          loadReport()
        })
      }
    } catch(e) {}
    loading.value = false
  }).catch(() => { loading.value = false })
}

/** 加载方案数据 */
function loadPlan() {
  getPlan(taskId.value).then(response => {
    planData.value = response.data || {}
  }).catch(() => {})
}

/** 加载材料列表 */
function loadMaterials() {
  const iid = issueInfo.value?.issueId || route.query.issueId
  if (iid) {
    listMaterial(iid).then(response => {
      materialList.value = response.rows || []
    }).catch(() => {})
  } else {
    setTimeout(loadMaterials, 500)
  }
}

/** 加载报告数据 */
function loadReport() {
  getReport(taskId.value).then(response => {
    reportData.value = response.data || {}
  }).catch(() => {})
}

/** 材料上传回调 */
function handleDeleteMaterial(mid) {
  proxy.$modal.confirm('确认删除该材料？').then(() => {
    delMaterial(mid).then(() => {
      proxy.$modal.msgSuccess('已删除')
      materialList.value = materialList.value.filter(m => m.materialId !== mid)
    })
  }).catch(() => {})
}

function handleMaterialUploaded() {
  const iid = issueInfo.value?.issueId
  if (iid) {
    listMaterial(iid).then(response => {
      materialList.value = response.rows || []
    })
  }
}

/** 返回列表 */
function goBack() {
  router.push('/rectification/my-tasks')
}

onMounted(() => {
  loadTaskInfo()
  loadPlan()
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
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-title {
  font-size: 14px;
  font-weight: 600;
}
.mb20 {
  margin-bottom: 20px;
}
.mt20 {
  margin-top: 20px;
}
</style>
