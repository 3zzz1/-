<template>
  <div class="app-container role-home">
    <section class="home-header">
      <div>
        <div class="eyebrow">{{ currentHome.badge }}</div>
        <h1>{{ currentHome.title }}</h1>
        <p>{{ currentHome.subtitle }}</p>
      </div>
      <div class="role-panel">
        <span>当前首页</span>
        <strong>{{ currentHome.name }}</strong>
      </div>
    </section>

    <el-row :gutter="16" class="metric-row">
      <el-col v-for="item in currentHome.metrics" :key="item.key" :xs="24" :sm="12" :lg="6">
        <div class="metric-card" @click="go(item.path)">
          <div class="metric-icon" :class="item.tone">
            <el-icon :size="24"><component :is="item.icon" /></el-icon>
          </div>
          <div class="metric-content">
            <div class="metric-value">{{ displayValue(item.value) }}</div>
            <div class="metric-label">{{ item.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="16">
        <section class="panel">
          <div class="panel-header">
            <h2>{{ currentHome.workTitle }}</h2>
            <el-button type="primary" plain size="small" @click="go(currentHome.primaryAction.path)">
              {{ currentHome.primaryAction.text }}
            </el-button>
          </div>
          <el-row :gutter="12">
            <el-col v-for="item in currentHome.actions" :key="item.title" :xs="24" :sm="12">
              <button class="action-item" type="button" @click="go(item.path)">
                <span class="action-icon" :class="item.tone">
                  <el-icon :size="20"><component :is="item.icon" /></el-icon>
                </span>
                <span>
                  <strong>{{ item.title }}</strong>
                  <em>{{ item.desc }}</em>
                </span>
              </button>
            </el-col>
          </el-row>
        </section>
      </el-col>

      <el-col :xs="24" :lg="8">
        <section class="panel">
          <div class="panel-header">
            <h2>待关注事项</h2>
          </div>
          <div class="todo-list">
            <div v-for="item in currentHome.todos" :key="item.label" class="todo-item">
              <span>{{ item.label }}</span>
              <strong>{{ displayValue(item.value) }}</strong>
            </div>
          </div>
        </section>
      </el-col>
    </el-row>

    <section class="panel process-panel">
      <div class="panel-header">
        <h2>{{ currentHome.processTitle }}</h2>
      </div>
      <el-steps :active="currentHome.activeStep" finish-status="success" align-center>
        <el-step
          v-for="step in currentHome.steps"
          :key="step.title"
          :title="step.title"
          :description="step.desc"
        />
      </el-steps>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, unref } from 'vue'
import { useRouter } from 'vue-router'
import useUserStore from '@/store/modules/user'
import { getOverview } from '@/api/rectification/statistics'
import { listTask, listMyTask } from '@/api/rectification/task'
import { listClosure } from '@/api/rectification/closure'

const router = useRouter()
const userStore = useUserStore()

const overview = reactive({
  totalIssues: 0,
  inProgress: 0,
  completed: 0,
  overdue: 0,
  completionRate: '0%',
  overdueRate: '0%',
  recoveryAmount: '0.00'
})

const pending = reactive({
  tasks: 0,
  myTasks: 0,
  closures: 0
})

const roles = computed(() => userStore.roles || [])

const homeType = computed(() => {
  if (hasAnyRole(['admin', 'audit_director', 'audit_lead'])) return 'auditManager'
  if (hasAnyRole(['audited_unit_leader'])) return 'unitLeader'
  if (hasAnyRole(['audited_unit_liaison'])) return 'liaison'
  if (hasAnyRole(['rect_responsible'])) return 'rectifier'
  if (hasAnyRole(['social_audit', 'intermediary', 'agency_auditor', 'audit_agency'])) return 'agency'
  return 'auditor'
})

const currentHome = computed(() => homeConfigs[homeType.value] || homeConfigs.auditor)

const homeConfigs = {
  auditManager: {
    name: '审计处管理首页',
    badge: '全校整改监督',
    title: '整改数据分析与销号审核',
    subtitle: '集中查看全校问题整改、逾期预警、销号审核和高频风险领域。',
    workTitle: '审计处工作入口',
    processTitle: '审计处监督闭环',
    primaryAction: { text: '进入整改分析', path: '/rectification/statistics' },
    activeStep: 4,
    metrics: baseMetrics(),
    todos: [
      { label: '待审核销号申请', value: valueOf(() => pending.closures) },
      { label: '整改中任务', value: valueOf(() => overview.inProgress) },
      { label: '逾期问题', value: valueOf(() => overview.overdue) }
    ],
    actions: [
      { title: '问题台账', desc: '查看和维护全部审计整改问题', icon: 'List', path: '/rectification/issue', tone: 'blue' },
      { title: '整改任务', desc: '下发、跟踪和复核整改任务', icon: 'EditPen', path: '/rectification/task', tone: 'amber' },
      { title: '销号审核', desc: '审核整改材料和销号申请', icon: 'CircleCheck', path: '/rectification/closure', tone: 'green' },
      { title: '多维分析', desc: '查看完成率、逾期率和风险分布', icon: 'DataAnalysis', path: '/rectification/statistics', tone: 'gray' }
    ],
    steps: auditSteps()
  },
  auditor: {
    name: '审计人员首页',
    badge: '项目作业视图',
    title: '我的审计整改工作台',
    subtitle: '聚焦分配给自己的项目、问题跟踪、整改评价和资料复核。',
    workTitle: '审计作业入口',
    processTitle: '审计人员处理路径',
    primaryAction: { text: '查看整改任务', path: '/rectification/task' },
    activeStep: 2,
    metrics: baseMetrics(),
    todos: [
      { label: '当前整改任务', value: valueOf(() => pending.tasks) },
      { label: '整改中问题', value: valueOf(() => overview.inProgress) },
      { label: '逾期问题', value: valueOf(() => overview.overdue) }
    ],
    actions: [
      { title: '整改任务', desc: '查看项目整改任务和责任分工', icon: 'EditPen', path: '/rectification/task', tone: 'amber' },
      { title: '问题台账', desc: '检索审计发现问题和整改状态', icon: 'List', path: '/rectification/issue', tone: 'blue' },
      { title: '销号管理', desc: '查看销号申请和审核结果', icon: 'CircleCheck', path: '/rectification/closure', tone: 'green' },
      { title: '统计分析', desc: '查看整改成效和风险趋势', icon: 'DataAnalysis', path: '/rectification/statistics', tone: 'gray' }
    ],
    steps: auditSteps()
  },
  unitLeader: {
    name: '被审单位负责人首页',
    badge: '本单位整改审批',
    title: '本单位整改概览',
    subtitle: '查看本单位整改台账、待审批整改报告、复核结果和逾期风险。',
    workTitle: '负责人审批入口',
    processTitle: '单位负责人审批路径',
    primaryAction: { text: '处理我的任务', path: '/rectification/my-tasks' },
    activeStep: 3,
    metrics: unitMetrics(),
    todos: [
      { label: '我的待办任务', value: valueOf(() => pending.myTasks) },
      { label: '本单位整改中', value: valueOf(() => overview.inProgress) },
      { label: '本单位逾期', value: valueOf(() => overview.overdue) }
    ],
    actions: [
      { title: '我的任务', desc: '审批整改报告并查看复核结果', icon: 'User', path: '/rectification/my-tasks', tone: 'blue' },
      { title: '任务详情', desc: '查看本单位整改任务全过程', icon: 'Document', path: '/rectification/my-tasks', tone: 'amber' },
      { title: '销号记录', desc: '查看本单位销号申请处理情况', icon: 'CircleCheck', path: '/rectification/closure', tone: 'green' },
      { title: '整改统计', desc: '查看本单位整改成效', icon: 'DataAnalysis', path: '/rectification/statistics', tone: 'gray' }
    ],
    steps: unitSteps()
  },
  liaison: {
    name: '联络员首页',
    badge: '资料与分办协同',
    title: '本单位整改任务分办',
    subtitle: '接收整改任务、确认问题、分办责任人并跟踪材料提交进度。',
    workTitle: '联络员操作入口',
    processTitle: '联络员协同路径',
    primaryAction: { text: '进入我的任务', path: '/rectification/my-tasks' },
    activeStep: 2,
    metrics: unitMetrics(),
    todos: [
      { label: '待确认/分办任务', value: valueOf(() => pending.myTasks) },
      { label: '整改中任务', value: valueOf(() => overview.inProgress) },
      { label: '逾期预警', value: valueOf(() => overview.overdue) }
    ],
    actions: [
      { title: '我的任务', desc: '确认接收并分办整改责任人', icon: 'User', path: '/rectification/my-tasks', tone: 'blue' },
      { title: '任务台账', desc: '跟踪本单位整改任务状态', icon: 'List', path: '/rectification/my-tasks', tone: 'amber' },
      { title: '材料填报', desc: '协助上传整改佐证材料', icon: 'Upload', path: '/rectification/my-tasks', tone: 'green' },
      { title: '进度查看', desc: '查看整改报告和销号进展', icon: 'DataLine', path: '/rectification/my-tasks', tone: 'gray' }
    ],
    steps: unitSteps()
  },
  rectifier: {
    name: '整改责任人首页',
    badge: '个人整改执行',
    title: '我的整改任务',
    subtitle: '编制整改方案、填报整改材料、生成整改报告并发起销号申请。',
    workTitle: '整改人操作入口',
    processTitle: '整改责任人闭环路径',
    primaryAction: { text: '处理我的任务', path: '/rectification/my-tasks' },
    activeStep: 2,
    metrics: unitMetrics(),
    todos: [
      { label: '我的整改任务', value: valueOf(() => pending.myTasks) },
      { label: '整改中', value: valueOf(() => overview.inProgress) },
      { label: '逾期预警', value: valueOf(() => overview.overdue) }
    ],
    actions: [
      { title: '我的任务', desc: '进入方案、材料、报告和销号操作', icon: 'User', path: '/rectification/my-tasks', tone: 'blue' },
      { title: '整改方案', desc: '维护整改计划和整改措施', icon: 'Document', path: '/rectification/my-tasks', tone: 'amber' },
      { title: '佐证材料', desc: '上传合同、凭证、制度文件等附件', icon: 'Upload', path: '/rectification/my-tasks', tone: 'green' },
      { title: '整改报告', desc: '提交报告并等待单位负责人审批', icon: 'Tickets', path: '/rectification/my-tasks', tone: 'gray' }
    ],
    steps: unitSteps()
  },
  agency: {
    name: '中介机构首页',
    badge: '临时授权项目',
    title: '分配项目作业入口',
    subtitle: '仅查看授权期内分配项目的数据、资料和底稿，项目结束后权限自动回收。',
    workTitle: '中介作业入口',
    processTitle: '中介人员作业路径',
    primaryAction: { text: '查看项目任务', path: '/rectification/task' },
    activeStep: 1,
    metrics: baseMetrics(),
    todos: [
      { label: '授权项目任务', value: valueOf(() => pending.tasks) },
      { label: '整改中问题', value: valueOf(() => overview.inProgress) },
      { label: '需关注逾期', value: valueOf(() => overview.overdue) }
    ],
    actions: [
      { title: '分配项目', desc: '查看授权项目范围内任务', icon: 'FolderOpened', path: '/rectification/task', tone: 'blue' },
      { title: '问题资料', desc: '查看项目相关问题和材料', icon: 'Document', path: '/rectification/issue', tone: 'amber' },
      { title: '作业记录', desc: '维护授权范围内作业内容', icon: 'EditPen', path: '/rectification/task', tone: 'green' },
      { title: '授权说明', desc: '仅展示服务周期内可访问数据', icon: 'Lock', path: '/user/profile', tone: 'gray' }
    ],
    steps: [
      { title: '临时授权', desc: '按项目开通访问权限' },
      { title: '项目作业', desc: '录入和维护授权资料' },
      { title: '成果提交', desc: '提交项目作业成果' },
      { title: '权限回收', desc: '结项后自动回收权限' }
    ]
  }
}

function baseMetrics() {
  return [
    { key: 'total', label: '问题总数', value: valueOf(() => overview.totalIssues), icon: 'List', path: '/rectification/issue', tone: 'blue' },
    { key: 'doing', label: '整改中', value: valueOf(() => overview.inProgress), icon: 'EditPen', path: '/rectification/task', tone: 'amber' },
    { key: 'done', label: '已销号', value: valueOf(() => overview.completed), icon: 'CircleCheck', path: '/rectification/closure', tone: 'green' },
    { key: 'overdue', label: '逾期预警', value: valueOf(() => overview.overdue), icon: 'WarningFilled', path: '/rectification/statistics', tone: 'red' }
  ]
}

function unitMetrics() {
  return [
    { key: 'mine', label: '我的任务', value: valueOf(() => pending.myTasks), icon: 'User', path: '/rectification/my-tasks', tone: 'blue' },
    { key: 'doing', label: '整改中', value: valueOf(() => overview.inProgress), icon: 'EditPen', path: '/rectification/my-tasks', tone: 'amber' },
    { key: 'done', label: '已完成', value: valueOf(() => overview.completed), icon: 'CircleCheck', path: '/rectification/my-tasks', tone: 'green' },
    { key: 'overdue', label: '逾期预警', value: valueOf(() => overview.overdue), icon: 'WarningFilled', path: '/rectification/my-tasks', tone: 'red' }
  ]
}

function auditSteps() {
  return [
    { title: '问题入库', desc: '内源同步或外源录入' },
    { title: '任务下发', desc: '指定单位和整改期限' },
    { title: '整改跟踪', desc: '查看方案、材料和报告' },
    { title: '销号审核', desc: '通过销号或驳回二次整改' },
    { title: '统计分析', desc: '反向优化审计重点' }
  ]
}

function unitSteps() {
  return [
    { title: '接收任务', desc: '确认问题和整改要求' },
    { title: '方案整改', desc: '填报计划、措施和成效' },
    { title: '报告审批', desc: '负责人审批整改报告' },
    { title: '申请销号', desc: '提交销号附言和材料' },
    { title: '审计复核', desc: '通过归档或补充整改' }
  ]
}

function valueOf(getter) {
  return computed(getter)
}

function displayValue(value) {
  return unref(value)
}

function hasAnyRole(roleKeys) {
  return roleKeys.some(role => roles.value.includes(role))
}

function go(path) {
  if (!path) return
  router.push(path)
}

function normalizeOverview(data) {
  overview.totalIssues = data.totalIssues || data.issueTotal || data.total || 0
  overview.inProgress = data.inProgressCount || data.inProgress || data.rectifyingCount || 0
  overview.completed = data.completedCount || data.closedCount || data.completed || 0
  overview.overdue = data.overdueCount || data.overdue || 0
  overview.completionRate = `${data.completionRate || 0}%`
  overview.overdueRate = `${data.overdueRate || 0}%`
  overview.recoveryAmount = data.totalRecoveryAmount || data.recoveryAmount || '0.00'
}

function loadHomeData() {
  getOverview().then(res => normalizeOverview(res.data || {})).catch(() => {})

  if (['unitLeader', 'liaison', 'rectifier'].includes(homeType.value)) {
    listMyTask({ pageNum: 1, pageSize: 1 }).then(res => {
      pending.myTasks = res.total || 0
    }).catch(() => {})
  }

  if (['auditManager', 'auditor', 'agency'].includes(homeType.value)) {
    listTask({ pageNum: 1, pageSize: 1 }).then(res => {
      pending.tasks = res.total || 0
    }).catch(() => {})
  }

  if (homeType.value === 'auditManager') {
    listClosure({ pageNum: 1, pageSize: 1, status: '0' }).then(res => {
      pending.closures = res.total || 0
    }).catch(() => {})
  }
}

onMounted(loadHomeData)
</script>

<style scoped lang="scss">
.role-home {
  padding: 16px;
  background: #f5f7fb;
  min-height: calc(100vh - 84px);
}

.home-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: stretch;
  padding: 22px 24px;
  margin-bottom: 16px;
  color: #fff;
  background: linear-gradient(135deg, #174b7f 0%, #1f6f6a 100%);
  border-radius: 8px;

  h1 {
    margin: 6px 0 8px;
    font-size: 26px;
    font-weight: 700;
    letter-spacing: 0;
  }

  p {
    margin: 0;
    color: rgba(255, 255, 255, 0.82);
    line-height: 1.6;
  }
}

.eyebrow {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.75);
}

.role-panel {
  min-width: 180px;
  padding: 16px;
  border: 1px solid rgba(255, 255, 255, 0.24);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.12);
  display: flex;
  flex-direction: column;
  justify-content: center;

  span {
    font-size: 13px;
    opacity: 0.78;
  }

  strong {
    margin-top: 8px;
    font-size: 18px;
  }
}

.metric-row {
  margin-bottom: 16px;
}

.metric-card,
.panel {
  background: #fff;
  border: 1px solid #e6eaf0;
  border-radius: 8px;
}

.metric-card {
  min-height: 104px;
  padding: 18px;
  display: flex;
  align-items: center;
  gap: 14px;
  cursor: pointer;
  transition: border-color 0.18s, box-shadow 0.18s;

  &:hover {
    border-color: #91caff;
    box-shadow: 0 8px 20px rgba(32, 45, 64, 0.08);
  }
}

.metric-icon,
.action-icon {
  width: 46px;
  height: 46px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  flex-shrink: 0;
}

.blue {
  color: #1677ff;
  background: #eaf3ff;
}

.amber {
  color: #c97900;
  background: #fff4df;
}

.green {
  color: #2f8f4e;
  background: #eaf8ef;
}

.red {
  color: #d93026;
  background: #fff0ee;
}

.gray {
  color: #5f6b7a;
  background: #eef1f5;
}

.metric-value {
  font-size: 28px;
  font-weight: 700;
  color: #202d40;
  line-height: 1;
}

.metric-label {
  margin-top: 8px;
  color: #6b7785;
  font-size: 13px;
}

.panel {
  padding: 18px;
  margin-bottom: 16px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;

  h2 {
    margin: 0;
    font-size: 17px;
    color: #202d40;
  }
}

.action-item {
  width: 100%;
  min-height: 82px;
  padding: 14px;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 12px;
  border: 1px solid #e6eaf0;
  border-radius: 8px;
  background: #fff;
  text-align: left;
  cursor: pointer;
  transition: border-color 0.18s, background 0.18s;

  &:hover {
    border-color: #91caff;
    background: #fbfdff;
  }

  strong {
    display: block;
    color: #202d40;
    font-size: 14px;
    margin-bottom: 5px;
  }

  em {
    display: block;
    color: #7a8696;
    font-size: 12px;
    line-height: 1.5;
    font-style: normal;
  }
}

.todo-list {
  display: grid;
  gap: 10px;
}

.todo-item {
  height: 48px;
  padding: 0 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #f7f9fc;
  border-radius: 8px;
  color: #5f6b7a;

  strong {
    color: #202d40;
    font-size: 18px;
  }
}

.process-panel {
  overflow-x: auto;
}

:deep(.el-step__description) {
  max-width: 150px;
  line-height: 1.45;
}

@media (max-width: 768px) {
  .home-header {
    flex-direction: column;
    padding: 18px;

    h1 {
      font-size: 22px;
    }
  }

  .role-panel {
    min-width: 0;
  }
}
</style>
