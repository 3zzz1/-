<template>
  <div class="app-container task-detail-page">
    <header class="detail-header">
      <div class="detail-header-bar">
        <el-button link icon="ArrowLeft" @click="goBack">返回</el-button>
        <el-tag :type="taskStatusTag(taskInfo.status)" effect="light">
          {{ taskStatusLabel(taskInfo.status) }}
        </el-tag>
      </div>
      <div class="detail-heading">
        <span>整改任务</span>
        <h1>{{ issueInfo.issueTitle || '任务详情' }}</h1>
        <p>{{ taskInfo.taskNo || '-' }}</p>
      </div>
      <div class="detail-summary">
        <div>
          <span>整改单位</span>
          <strong>{{ rectifyDeptName }}</strong>
        </div>
        <div>
          <span>联络人</span>
          <strong>{{ taskInfo.contactPerson || '-' }}</strong>
        </div>
        <div>
          <span>截止日期</span>
          <strong :class="{ overdue: isTaskOverdue }">{{ taskInfo.deadline || '-' }}</strong>
        </div>
      </div>
    </header>

    <el-tabs v-model="activeTab" type="border-card" class="detail-tabs">
      <el-tab-pane label="基本信息" name="basic">
        <div class="tab-content">
          <el-card shadow="never" class="mb20">
            <template #header>
              <div class="card-header">
                <span class="card-title">任务信息</span>
              </div>
            </template>
            <el-descriptions v-if="!isMobile" :column="2" border>
              <el-descriptions-item label="任务编号">{{ taskInfo.taskNo || '-' }}</el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="taskStatusTag(taskInfo.status)">{{ taskStatusLabel(taskInfo.status) }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="整改单位">{{ rectifyDeptName }}</el-descriptions-item>
              <el-descriptions-item label="联络人">{{ taskInfo.contactPerson || '-' }}</el-descriptions-item>
              <el-descriptions-item label="联系电话">{{ taskInfo.contactPhone || '-' }}</el-descriptions-item>
              <el-descriptions-item label="截止日期">{{ taskInfo.deadline || '-' }}</el-descriptions-item>
              <el-descriptions-item label="下发时间">{{ taskInfo.dispatchTime || '-' }}</el-descriptions-item>
              <el-descriptions-item label="接收时间">{{ taskInfo.confirmTime || '-' }}</el-descriptions-item>
              <el-descriptions-item label="整改要求" :span="2">{{ taskInfo.requirement || '-' }}</el-descriptions-item>
            </el-descriptions>
            <div v-else class="compact-info-grid">
              <div class="compact-info-wide"><span>联系电话</span><strong>{{ taskInfo.contactPhone || '-' }}</strong></div>
              <div><span>下发时间</span><strong>{{ taskInfo.dispatchTime || '-' }}</strong></div>
              <div><span>接收时间</span><strong>{{ taskInfo.confirmTime || '尚未接收' }}</strong></div>
              <div class="compact-info-wide"><span>整改要求</span><strong>{{ taskInfo.requirement || '-' }}</strong></div>
            </div>
          </el-card>

          <el-card shadow="never">
            <template #header>
              <div class="card-header">
                <span class="card-title">关联问题信息</span>
              </div>
            </template>
            <el-descriptions v-if="issueInfo.issueNo && !isMobile" :column="2" border>
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
              <el-descriptions-item label="责任单位">{{ deptName(issueInfo.responsibleDeptId) }}</el-descriptions-item>
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
            <template v-else-if="issueInfo.issueNo">
              <div class="compact-info-grid">
                <div><span>问题编号</span><strong>{{ issueInfo.issueNo }}</strong></div>
                <div><span>来源类型</span><strong>{{ sourceTypeLabel(issueInfo.sourceType) }}</strong></div>
                <div><span>问题分类</span><strong>{{ categoryLabel(issueInfo.issueCategory) }}</strong></div>
                <div><span>涉及金额</span><strong>{{ formatAmount(issueInfo.issueAmount) }}</strong></div>
                <div><span>责任干部</span><strong>{{ issueInfo.responsiblePerson || '-' }}</strong></div>
                <div><span>风险等级</span><strong>{{ riskLevelLabel(issueInfo.riskLevel) }}</strong></div>
                <div><span>问题状态</span><strong>{{ issueStatusLabel(issueInfo.status) }}</strong></div>
              </div>
              <el-collapse v-if="issueInfo.issueDesc || issueInfo.legalBasis" class="issue-detail-collapse">
                <el-collapse-item title="查看问题说明与定性依据" name="issue-detail">
                  <div class="collapsed-detail">
                    <span>问题描述</span>
                    <p>{{ issueInfo.issueDesc || '-' }}</p>
                    <span>定性法规依据</span>
                    <p>{{ issueInfo.legalBasis || '-' }}</p>
                  </div>
                </el-collapse-item>
              </el-collapse>
            </template>
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
            <el-button type="primary" icon="Upload" size="small" native-type="button" @click.prevent="uploadOpen = true">上传佐证</el-button>
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
            <el-descriptions :column="descriptionColumns" border>
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
import { computed, defineAsyncComponent, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTask } from '@/api/rectification/task'
import { getIssue } from '@/api/rectification/issue'
import { listMaterial, delMaterial } from '@/api/rectification/material'
import { getReport } from '@/api/rectification/report'
import useAppStore from '@/store/modules/app'
import request from '@/utils/request'

const PlanEditor = defineAsyncComponent(() => import('../plan/PlanEditor.vue'))
const MaterialUpload = defineAsyncComponent(() => import('../material/MaterialUpload.vue'))
const ReportEditor = defineAsyncComponent(() => import('../report/ReportEditor.vue'))
const LeaderApproval = defineAsyncComponent(() => import('../report/LeaderApproval.vue'))
const TaskTimeline = defineAsyncComponent(() => import('./components/TaskTimeline.vue'))

const router = useRouter()
const route = useRoute()
const appStore = useAppStore()
const isMobile = computed(() => appStore.device === 'mobile')
const descriptionColumns = computed(() => isMobile.value ? 1 : 2)

const taskId = ref(route.params.taskId)
const activeTab = ref(route.query.tab || 'basic')
const uploadOpen = ref(false)
const approvalOpen = ref(false)

const taskInfo = ref({})
const issueInfo = ref({})
const materialList = ref([])
const reportData = ref({})
const loading = ref(false)
const deptList = ref([])

const rectifyDeptName = computed(() => deptName(taskInfo.value.rectDeptId))
const isTaskOverdue = computed(() => {
  if (!taskInfo.value.deadline || ['3', '4'].includes(String(taskInfo.value.status))) return false
  return new Date(taskInfo.value.deadline).getTime() < new Date().setHours(0, 0, 0, 0)
})

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

function deptName(deptId) {
  if (!deptId) return '-'
  const dept = deptList.value.find(item => String(item.deptId) === String(deptId))
  return dept ? dept.deptName : String(deptId)
}

function loadDeptList() {
  request({ url: '/rectification/issue/depts', method: 'get' }).then(response => {
    deptList.value = Array.isArray(response.data) ? response.data : []
  }).catch(() => {
    deptList.value = []
  })
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
  loadDeptList()
  loadTaskInfo()
})
</script>

<style scoped>
.detail-header {
  padding: 18px 20px;
  margin-bottom: 16px;
  border: 1px solid #e2e9f2;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 8px 22px rgba(42, 60, 84, 0.05);
}

.detail-header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  min-height: 28px;
}

.detail-header-bar .el-button {
  margin-left: 0;
  padding: 0;
}

.detail-heading {
  padding: 15px 0 16px;
}

.detail-heading > span {
  color: #7b899b;
  font-size: 12px;
}

.detail-heading h1 {
  margin: 5px 0 4px;
  color: #1f2f46;
  font-size: 21px;
  line-height: 1.4;
  overflow-wrap: anywhere;
}

.detail-heading p {
  margin: 0;
  color: #8290a3;
  font-size: 12px;
}

.detail-summary {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  border-top: 1px solid #edf1f6;
}

.detail-summary > div {
  min-width: 0;
  padding: 13px 14px 0 0;
}

.detail-summary span,
.detail-summary strong {
  display: block;
  line-height: 1.4;
}

.detail-summary span {
  color: #8995a6;
  font-size: 11px;
}

.detail-summary strong {
  margin-top: 3px;
  color: #33465e;
  font-size: 13px;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.detail-summary strong.overdue {
  color: #d94a45;
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

.compact-info-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 1px;
  overflow: hidden;
  border: 1px solid #e5eaf1;
  border-radius: 8px;
  background: #e5eaf1;
}

.compact-info-grid > div {
  min-width: 0;
  padding: 10px 12px;
  background: #fff;
}

.compact-info-grid span,
.compact-info-grid strong {
  display: block;
  line-height: 1.4;
}

.compact-info-grid span {
  color: #8995a6;
  font-size: 11px;
}

.compact-info-grid strong {
  margin-top: 3px;
  color: #33465e;
  font-size: 13px;
  font-weight: 500;
  overflow-wrap: anywhere;
}

.compact-info-wide {
  grid-column: 1 / -1;
}

.compact-info-grid > div:last-child:nth-child(odd) {
  grid-column: 1 / -1;
}

.issue-detail-collapse {
  margin-top: 10px;
  border: 1px solid #e5eaf1;
  border-radius: 8px;
}

.issue-detail-collapse :deep(.el-collapse-item__header) {
  min-height: 42px;
  height: auto;
  padding: 0 12px;
  border: 0;
  color: #52657d;
  line-height: 1.4;
}

.issue-detail-collapse :deep(.el-collapse-item__wrap) {
  border: 0;
}

.issue-detail-collapse :deep(.el-collapse-item__content) {
  padding: 0 12px 12px;
}

.collapsed-detail span {
  color: #7f8d9f;
  font-size: 12px;
}

.collapsed-detail p {
  margin: 4px 0 12px;
  color: #34465e;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
}

@media (max-width: 768px) {
  .task-detail-page {
    padding: 12px;
    padding-bottom: calc(28px + env(safe-area-inset-bottom));
    background: #f5f7fb;
  }

  .detail-header {
    padding: 14px;
    margin-bottom: 12px;
  }

  .card-header,
  .material-header {
    width: 100%;
    align-items: flex-start;
    flex-wrap: wrap;
    gap: 8px;
  }

  .detail-tabs {
    margin-top: 0;
    border-radius: 8px;
    overflow: hidden;
  }

  .detail-tabs :deep(.el-tabs__header) {
    overflow-x: auto;
    overflow-y: hidden;
  }

  .detail-tabs :deep(.el-tabs__nav) {
    display: flex;
    float: none;
    width: max-content;
  }

  .detail-tabs :deep(.el-tabs__item) {
    flex: 0 0 auto;
    padding: 0 14px;
  }

  .tab-content {
    padding: 4px 0;
  }

  .material-tag {
    max-width: 100%;
    white-space: normal;
    height: auto;
    line-height: 1.5;
  }

  .detail-summary {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .detail-summary > div:last-child {
    grid-column: 1 / -1;
  }
}

</style>
