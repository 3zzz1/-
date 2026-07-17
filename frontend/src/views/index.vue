<template>
  <div class="app-container role-home">
    <section class="home-header">
      <div>
        <h1>{{ currentHome.title }}</h1>
        <p>{{ currentHome.desc }}</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="go(currentHome.primary.path)">
          {{ currentHome.primary.text }}
        </el-button>
        <el-button plain @click="go(currentHome.secondary.path)">
          {{ currentHome.secondary.text }}
        </el-button>
      </div>
    </section>

    <el-row :gutter="16" class="metric-row">
      <el-col v-for="item in metrics" :key="item.key" :xs="12" :sm="12" :lg="6">
        <button class="metric-card" type="button" @click="go(item.path)">
          <span class="metric-icon" :class="item.tone">
            <el-icon :size="22"><component :is="item.icon" /></el-icon>
          </span>
          <span class="metric-copy">
            <strong>{{ item.value }}</strong>
            <em>{{ item.label }}</em>
          </span>
        </button>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :lg="16">
        <section class="panel">
          <div class="panel-header">
            <div>
              <h2>{{ currentHome.workTitle }}</h2>
              <p>{{ currentHome.workDesc }}</p>
            </div>
          </div>

          <div class="action-grid">
            <button v-for="item in currentHome.actions" :key="item.title" class="action-item" type="button" @click="go(item.path)">
              <span class="action-icon" :class="item.tone">
                <el-icon :size="20"><component :is="item.icon" /></el-icon>
              </span>
              <span>
                <strong>{{ item.title }}</strong>
                <em>{{ item.desc }}</em>
              </span>
            </button>
          </div>
        </section>
      </el-col>

      <el-col :xs="24" :lg="8">
        <section class="panel todo-panel">
          <div class="panel-header compact">
            <div>
              <h2>{{ currentHome.todoTitle }}</h2>
              <p>{{ currentHome.todoDesc }}</p>
            </div>
          </div>

          <div class="todo-list">
            <button v-for="item in currentTodos" :key="item.label" class="todo-item" type="button" @click="go(item.path)">
              <span>{{ item.label }}</span>
              <strong>{{ item.value }}</strong>
            </button>
          </div>

          <div v-if="recentTasks.length" class="recent-list">
            <div class="recent-title">最近任务</div>
            <button v-for="task in recentTasks" :key="task.taskId" class="recent-item" type="button" @click="goTaskDetail(task)">
              <span>{{ task.taskNo || ('任务' + task.taskId) }}</span>
              <em>{{ task.statusLabel || '-' }}</em>
            </button>
          </div>
        </section>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import useUserStore from '@/store/modules/user'
import { getOverview } from '@/api/rectification/statistics'

const router = useRouter()
const userStore = useUserStore()

const overview = reactive({
  totalIssues: 0,
  inProgress: 0,
  completed: 0,
  overdue: 0,
  tasks: 0,
  myTasks: 0,
  closures: 0
})
const recentTasks = ref([])

const roles = computed(() => userStore.roles || [])
const homeType = computed(() => {
  if (hasAnyRole(['admin'])) return 'systemAdmin'
  if (hasAnyRole(['audit_director', 'audit_lead'])) return 'auditManager'
  if (hasAnyRole(['audit_staff'])) return 'auditStaff'
  if (hasAnyRole(['school_leader'])) return 'schoolLeader'
  if (hasAnyRole(['external_auditor'])) return 'externalAuditor'
  if (hasAnyRole(['audited_unit_leader'])) return 'unitLeader'
  if (hasAnyRole(['audited_unit_liaison'])) return 'liaison'
  if (hasAnyRole(['rect_responsible'])) return 'rectifier'
  return 'auditor'
})

const currentHome = computed(() => homes[homeType.value] || homes.auditor)
const metrics = computed(() => currentHome.value.metrics())
const currentTodos = computed(() => currentHome.value.todos())

const homes = {
  systemAdmin: {
    title: '系统管理工作台',
    desc: '维护系统用户、角色权限、组织机构和运行参数。',
    workTitle: '系统管理入口',
    workDesc: '处理系统配置、安全授权和运行维护事项。',
    todoTitle: '管理入口',
    todoDesc: '进入对应模块执行系统管理操作。',
    primary: { text: '用户管理', path: '/system/user' },
    secondary: { text: '角色管理', path: '/system/role' },
    metrics: () => [
      metric('users', '用户管理', '用户', 'User', '/system/user', 'blue'),
      metric('roles', '角色权限', '角色', 'Lock', '/system/role', 'amber'),
      metric('departments', '组织机构', '部门', 'OfficeBuilding', '/system/dept', 'green'),
      metric('settings', '参数设置', '参数', 'Setting', '/system/config', 'violet')
    ],
    todos: () => [
      { label: '用户与账号管理', value: '进入', path: '/system/user' },
      { label: '角色与数据权限', value: '进入', path: '/system/role' },
      { label: '系统运行日志', value: '进入', path: '/monitor/operlog' }
    ],
    actions: [
      { title: '用户管理', desc: '维护账号、状态和所属部门。', icon: 'User', path: '/system/user', tone: 'blue' },
      { title: '角色管理', desc: '配置角色菜单和数据权限。', icon: 'Lock', path: '/system/role', tone: 'amber' },
      { title: '部门管理', desc: '维护学校组织机构信息。', icon: 'OfficeBuilding', path: '/system/dept', tone: 'green' },
      { title: '参数设置', desc: '维护系统运行参数。', icon: 'Setting', path: '/system/config', tone: 'violet' }
    ]
  },
  auditManager: {
    title: '审计整改监督工作台',
    desc: '集中处理待下发问题、整改任务跟踪、销号审核和整改成效分析。',
    workTitle: '审计处待办入口',
    workDesc: '审计处长和项目组长/主审在这里处理全校整改监督事项。',
    todoTitle: '审计待办',
    todoDesc: '优先处理需要审计处操作的事项。',
    primary: { text: '审计待办', path: '/rectification/my-tasks' },
    secondary: { text: '整改分析', path: '/rectification/statistics' },
    metrics: () => [
      metric('issues', '问题总数', overview.totalIssues, 'List', '/rectification/issue', 'blue'),
      metric('tasks', '整改任务', overview.tasks, 'EditPen', '/rectification/task', 'amber'),
      metric('closures', '待审销号', overview.closures, 'CircleCheck', '/rectification/closure', 'green'),
      metric('overdue', '逾期预警', overview.overdue, 'WarningFilled', '/rectification/statistics', 'red')
    ],
    todos: () => [
      { label: '待审销号申请', value: overview.closures, path: '/rectification/closure' },
      { label: '整改任务跟踪', value: overview.tasks, path: '/rectification/task' },
      { label: '逾期问题预警', value: overview.overdue, path: '/rectification/statistics' }
    ],
    actions: [
      { title: '问题台账', desc: '维护审计发现问题，并下发整改任务。', icon: 'List', path: '/rectification/issue', tone: 'blue' },
      { title: '整改任务', desc: '查看任务下发、确认、分办和整改进度。', icon: 'EditPen', path: '/rectification/task', tone: 'amber' },
      { title: '销号审核', desc: '审核整改材料和销号申请。', icon: 'CircleCheck', path: '/rectification/closure', tone: 'green' },
      { title: '整改分析', desc: '查看完成率、逾期率和风险分布。', icon: 'DataAnalysis', path: '/rectification/statistics', tone: 'violet' }
    ]
  },
  auditStaff: {
    title: '普通审计人员工作台',
    desc: '查看审计问题、整改任务、佐证材料和整改结果。',
    workTitle: '审计查询入口',
    workDesc: '查询整改业务资料和进展，不提供下发、编辑及审批操作。',
    todoTitle: '重点关注',
    todoDesc: '查看整改进度和逾期情况。',
    primary: { text: '问题台账', path: '/rectification/issue' },
    secondary: { text: '整改分析', path: '/rectification/statistics' },
    metrics: () => baseMetrics('/rectification/task'),
    todos: () => [
      { label: '整改中任务', value: overview.inProgress, path: '/rectification/task' },
      { label: '已完成问题', value: overview.completed, path: '/rectification/closure' },
      { label: '逾期问题', value: overview.overdue, path: '/rectification/statistics' }
    ],
    actions: [
      { title: '问题台账', desc: '查看审计发现问题及当前整改状态。', icon: 'List', path: '/rectification/issue', tone: 'blue' },
      { title: '整改任务', desc: '查看整改任务、责任分工和办理进度。', icon: 'EditPen', path: '/rectification/task', tone: 'amber' },
      { title: '销号记录', desc: '查看销号申请及审计复核结果。', icon: 'CircleCheck', path: '/rectification/closure', tone: 'green' },
      { title: '整改分析', desc: '查看整改完成率、逾期率和风险分布。', icon: 'DataAnalysis', path: '/rectification/statistics', tone: 'violet' }
    ]
  },
  schoolLeader: {
    title: '全校整改监督工作台',
    desc: '查看全校整改概况、风险分布、整改进展和最终整改结果。',
    workTitle: '整改监督入口',
    workDesc: '以汇总和只读方式掌握全校整改情况，不参与具体业务办理。',
    todoTitle: '监督关注',
    todoDesc: '重点关注整改进度、逾期问题和销号结果。',
    primary: { text: '整改数据分析', path: '/rectification/statistics' },
    secondary: { text: '整改任务进展', path: '/rectification/task' },
    metrics: () => baseMetrics('/rectification/task'),
    todos: () => [
      { label: '整改中问题', value: overview.inProgress, path: '/rectification/task' },
      { label: '已销号问题', value: overview.completed, path: '/rectification/closure' },
      { label: '逾期问题', value: overview.overdue, path: '/rectification/statistics' }
    ],
    actions: [
      { title: '整改总体情况', desc: '查看全校整改完成率、逾期率和成效汇总。', icon: 'DataAnalysis', path: '/rectification/statistics', tone: 'violet' },
      { title: '整改任务进展', desc: '查看任务状态和全流程进展时间线。', icon: 'EditPen', path: '/rectification/task', tone: 'blue' },
      { title: '逾期风险监督', desc: '关注逾期问题、高风险领域和重复问题。', icon: 'WarningFilled', path: '/rectification/statistics', tone: 'amber' },
      { title: '销号结果', desc: '查看最终整改报告和销号复核结果。', icon: 'CircleCheck', path: '/rectification/closure', tone: 'green' }
    ]
  },
  externalAuditor: {
    title: '项目整改跟踪工作台',
    desc: '查看授权项目发现的问题及后续整改进展。',
    workTitle: '授权项目入口',
    workDesc: '仅展示当前服务期内已授权项目的整改信息。',
    todoTitle: '项目跟踪',
    todoDesc: '进入对应模块查看问题和任务进展。',
    primary: { text: '项目问题', path: '/rectification/issue' },
    secondary: { text: '整改进展', path: '/rectification/task' },
    metrics: () => [
      metric('projectIssues', '项目问题', '查看', 'List', '/rectification/issue', 'blue'),
      metric('projectTasks', '整改进展', '查看', 'EditPen', '/rectification/task', 'amber')
    ],
    todos: () => [
      { label: '授权项目问题', value: '查看', path: '/rectification/issue' },
      { label: '关联整改任务', value: '查看', path: '/rectification/task' }
    ],
    actions: [
      { title: '项目问题', desc: '查看授权项目发现的问题及整改状态。', icon: 'List', path: '/rectification/issue', tone: 'blue' },
      { title: '整改进展', desc: '查看关联整改任务和进展时间线。', icon: 'EditPen', path: '/rectification/task', tone: 'amber' }
    ]
  },
  auditor: {
    title: '审计整改工作台',
    desc: '查看整改任务、问题台账和整改分析。',
    workTitle: '审计作业入口',
    workDesc: '查看整改任务和相关问题进展。',
    todoTitle: '待关注事项',
    todoDesc: '关注整改中和逾期事项。',
    primary: { text: '整改任务', path: '/rectification/task' },
    secondary: { text: '整改分析', path: '/rectification/statistics' },
    metrics: () => baseMetrics('/rectification/task'),
    todos: () => baseTodos('/rectification/task'),
    actions: auditActions()
  },
  unitLeader: {
    title: '本单位整改审批工作台',
    desc: '审批整改报告，查看本单位整改台账和复核结果。',
    workTitle: '负责人审批入口',
    workDesc: '处理整改报告审批，并跟踪本单位整改进展。',
    todoTitle: '我的待办',
    todoDesc: '只显示本单位负责人需要处理的事项。',
    primary: { text: '处理我的任务', path: '/rectification/my-tasks' },
    secondary: { text: '整改分析', path: '/rectification/statistics' },
    metrics: () => unitMetrics(),
    todos: () => baseTodos('/rectification/my-tasks'),
    actions: unitActions('审批整改报告并查看复核结果。')
  },
  liaison: {
    title: '整改任务接收与分办工作台',
    desc: '接收审计处下发任务，确认问题并分办责任人。',
    workTitle: '联络员操作入口',
    workDesc: '负责任务确认、责任分办和材料协同。',
    todoTitle: '我的待办',
    todoDesc: '只显示联络员需要处理的任务。',
    primary: { text: '进入我的任务', path: '/rectification/my-tasks' },
    secondary: { text: '整改分析', path: '/rectification/statistics' },
    metrics: () => unitMetrics(),
    todos: () => baseTodos('/rectification/my-tasks'),
    actions: unitActions('确认接收并分办整改责任人。')
  },
  rectifier: {
    title: '我的整改任务工作台',
    desc: '处理整改方案、佐证材料、整改报告和销号申请。',
    workTitle: '整改人操作入口',
    workDesc: '围绕个人整改任务完成整改闭环。',
    todoTitle: '我的待办',
    todoDesc: '只显示整改责任人需要处理的任务。',
    primary: { text: '处理我的任务', path: '/rectification/my-tasks' },
    secondary: { text: '整改分析', path: '/rectification/statistics' },
    metrics: () => unitMetrics(),
    todos: () => baseTodos('/rectification/my-tasks'),
    actions: unitActions('进入方案、材料、报告和销号操作。')
  }
}

function metric(key, label, value, icon, path, tone) {
  return { key, label, value, icon, path, tone }
}

function baseMetrics(taskPath) {
  return [
    metric('issues', '问题总数', overview.totalIssues, 'List', '/rectification/issue', 'blue'),
    metric('doing', '整改中', overview.inProgress, 'EditPen', taskPath, 'amber'),
    metric('done', '已销号', overview.completed, 'CircleCheck', '/rectification/closure', 'green'),
    metric('overdue', '逾期预警', overview.overdue, 'WarningFilled', '/rectification/statistics', 'red')
  ]
}

function unitMetrics() {
  return [
    metric('mine', '我的任务', overview.myTasks, 'User', '/rectification/my-tasks', 'blue'),
    metric('doing', '整改中', overview.inProgress, 'EditPen', '/rectification/my-tasks', 'amber'),
    metric('done', '已完成', overview.completed, 'CircleCheck', '/rectification/my-tasks', 'green'),
    metric('overdue', '逾期预警', overview.overdue, 'WarningFilled', '/rectification/statistics', 'red')
  ]
}

function baseTodos(path) {
  return [
    { label: '我的任务', value: overview.myTasks, path },
    { label: '整改中任务', value: overview.inProgress, path },
    { label: '逾期预警', value: overview.overdue, path: '/rectification/statistics' }
  ]
}

function auditActions() {
  return [
    { title: '整改任务', desc: '查看整改任务和责任分工。', icon: 'EditPen', path: '/rectification/task', tone: 'amber' },
    { title: '问题台账', desc: '检索审计发现问题和整改状态。', icon: 'List', path: '/rectification/issue', tone: 'blue' },
    { title: '销号管理', desc: '查看销号申请和审核结果。', icon: 'CircleCheck', path: '/rectification/closure', tone: 'green' },
    { title: '统计分析', desc: '查看整改成效和风险趋势。', icon: 'DataAnalysis', path: '/rectification/statistics', tone: 'violet' }
  ]
}

function unitActions(firstDesc) {
  return [
    { title: '我的任务', desc: firstDesc, icon: 'User', path: '/rectification/my-tasks', tone: 'blue' },
    { title: '整改方案', desc: '查看或维护整改计划和措施。', icon: 'Document', path: '/rectification/my-tasks', tone: 'amber' },
    { title: '佐证材料', desc: '查看或上传整改附件材料。', icon: 'Upload', path: '/rectification/my-tasks', tone: 'green' },
    { title: '整改统计', desc: '查看整改完成率和逾期情况。', icon: 'DataAnalysis', path: '/rectification/statistics', tone: 'violet' }
  ]
}

function hasAnyRole(roleKeys) {
  return roleKeys.some(role => roles.value.includes(role))
}

function go(path) {
  if (path) router.push(path)
}

function goTaskDetail(task) {
  if (task?.taskId) router.push('/rectification/task-page/detail/' + task.taskId)
}

function normalizeOverview(data) {
  overview.totalIssues = data.totalIssues || data.issueTotal || data.total || 0
  overview.inProgress = data.inProgressCount || data.inProgress || data.rectifyingCount || 0
  overview.completed = data.completedCount || data.closedCount || data.completed || 0
  overview.overdue = data.overdueCount || data.overdue || 0
  overview.tasks = data.tasks || data.taskTotal || 0
  overview.myTasks = data.myTasks || data.myTaskTotal || 0
  overview.closures = data.closures || data.closurePending || 0
  recentTasks.value = Array.isArray(data.recentTasks) ? data.recentTasks : []
}

function loadHomeData() {
  if (['systemAdmin', 'externalAuditor'].includes(homeType.value)) return
  getOverview().then(res => normalizeOverview(res.data || {})).catch(() => {})
}

onMounted(loadHomeData)
</script>

<style scoped lang="scss">
.role-home {
  min-height: calc(100vh - 84px);
  padding: 18px;
  background: #f5f7fb;
}

.home-header,
.panel,
.metric-card {
  border: 1px solid #e1e8f2;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 12px 30px rgba(65, 84, 109, 0.06);
}

.home-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 18px 20px;
  margin-bottom: 18px;

  h1 {
    margin: 0;
    color: #20324d;
    font-size: 22px;
    line-height: 1.3;
  }

  p {
    margin: 6px 0 0;
    color: #718096;
    font-size: 13px;
    line-height: 1.5;
  }
}

.header-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.metric-row {
  margin-bottom: 18px;
  row-gap: 12px;
}

.metric-card {
  width: 100%;
  min-height: 108px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  color: inherit;
  text-align: left;
  cursor: pointer;
}

.metric-icon,
.action-icon {
  width: 46px;
  height: 46px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  border-radius: 8px;
}

.blue { color: #2f6fe4; background: #edf4ff; }
.amber { color: #b66d00; background: #fff6e5; }
.green { color: #28845a; background: #ebf8f1; }
.red { color: #cf4a3b; background: #fff0ed; }
.violet { color: #6d5bd0; background: #f1efff; }

.metric-copy {
  min-width: 0;

  strong {
    display: block;
    color: #1f2f46;
    font-size: 28px;
    line-height: 1;
  }

  em {
    display: block;
    margin-top: 8px;
    color: #6f7e93;
    font-size: 13px;
    font-style: normal;
  }
}

.panel {
  padding: 20px;
  margin-bottom: 18px;
}

.panel-header {
  margin-bottom: 16px;

  h2 {
    margin: 0;
    color: #1f2f46;
    font-size: 18px;
  }

  p {
    margin: 6px 0 0;
    color: #7a879a;
    font-size: 13px;
    line-height: 1.6;
  }
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.action-item {
  min-height: 94px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 13px;
  border: 1px solid #e4ebf5;
  border-radius: 8px;
  background: #fbfdff;
  text-align: left;
  cursor: pointer;

  strong {
    display: block;
    color: #22334c;
    font-size: 15px;
  }

  em {
    display: block;
    margin-top: 6px;
    color: #758397;
    font-size: 13px;
    line-height: 1.5;
    font-style: normal;
  }
}

.todo-list,
.recent-list {
  display: grid;
  gap: 10px;
}

.recent-list {
  margin-top: 16px;
  padding-top: 14px;
  border-top: 1px solid #e6edf6;
}

.recent-title {
  color: #6f7e93;
  font-size: 13px;
  font-weight: 600;
}

.todo-item,
.recent-item {
  width: 100%;
  min-height: 50px;
  padding: 0 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  border: 1px solid #e6edf6;
  border-radius: 8px;
  background: #f8fbff;
  cursor: pointer;
}

.todo-item span,
.recent-item span {
  min-width: 0;
  color: #5f6f86;
  font-size: 14px;
  text-align: left;
}

.todo-item strong {
  color: #1f2f46;
  font-size: 20px;
}

.recent-item span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.recent-item em {
  flex-shrink: 0;
  color: #6f7e93;
  font-size: 12px;
  font-style: normal;
}

@media (max-width: 768px) {
  .role-home {
    padding: 12px;
  }

  .home-header {
    display: block;
    padding: 16px;

    h1 {
      font-size: 20px;
    }
  }

  .header-actions {
    margin-top: 14px;

    .el-button {
      flex: 1;
      margin-left: 0;
    }
  }

  .metric-card {
    min-height: 96px;
    padding: 12px;
  }

  .metric-icon {
    width: 38px;
    height: 38px;
  }

  .metric-copy strong {
    font-size: 22px;
  }

  .panel {
    padding: 14px;
  }

  .action-grid {
    grid-template-columns: 1fr;
  }
}
</style>
