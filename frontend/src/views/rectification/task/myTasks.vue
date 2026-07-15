<template>
  <div class="app-container my-task-page">
    <template v-if="isAuditManager">
      <section class="audit-todo-header">
        <div>
          <h2>审计处待办</h2>
          <p>聚合待下发、待审核、整改不到位、临期和逾期事项，点击后进入对应业务模块处理。</p>
        </div>
        <el-button type="primary" icon="Refresh" :loading="loading" @click="getList">刷新待办</el-button>
      </section>

      <el-row :gutter="16" class="audit-summary">
        <el-col v-for="card in auditStatCards" :key="card.type" :xs="12" :sm="12" :md="6">
          <button class="audit-summary-card" type="button" :class="card.tone" @click="filterAuditType(card.type)">
            <span>{{ card.label }}</span>
            <strong>{{ card.count }}</strong>
          </button>
        </el-col>
      </el-row>

      <el-form class="audit-filter desktop-query-form" :model="queryParams" :inline="true">
        <el-form-item label="待办类型">
          <el-select v-model="queryParams.auditType" placeholder="全部类型" clearable style="width: 180px" @change="handleQuery">
            <el-option label="待下发问题" value="issue_pending" />
            <el-option label="待审销号" value="closure_pending" />
            <el-option label="整改不到位" value="closure_rejected" />
            <el-option label="逾期未整改" value="task_overdue" />
            <el-option label="即将到期" value="task_soon" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input
            v-model="queryParams.taskNo"
            placeholder="任务编号/问题标题"
            clearable
            style="width: 220px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <MobileFilterBar
        v-model="queryParams.taskNo"
        placeholder="搜索编号或事项名称"
        :active-count="queryParams.auditType ? 1 : 0"
        @search="handleQuery"
        @reset="resetQuery"
      >
        <el-form label-position="top">
          <el-form-item label="待办类型">
            <el-select v-model="queryParams.auditType" placeholder="全部类型" clearable>
              <el-option label="待下发问题" value="issue_pending" />
              <el-option label="待审销号" value="closure_pending" />
              <el-option label="整改不到位" value="closure_rejected" />
              <el-option label="逾期未整改" value="task_overdue" />
              <el-option label="即将到期" value="task_soon" />
            </el-select>
          </el-form-item>
        </el-form>
      </MobileFilterBar>

      <el-table class="audit-todo-table" v-loading="loading" :data="auditTodoList" stripe>
        <el-table-column label="待办类型" width="120">
          <template #default="scope">
            <el-tag :type="auditTodoTag(scope.row.type)">{{ scope.row.typeLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="事项名称" prop="title" min-width="220" show-overflow-tooltip />
        <el-table-column label="编号" prop="bizNo" width="150" show-overflow-tooltip />
        <el-table-column label="责任/联系信息" prop="owner" min-width="150" show-overflow-tooltip />
        <el-table-column label="截止/申请时间" width="160">
          <template #default="scope">
            <span :class="{ 'audit-date-danger': scope.row.type === 'task_overdue', 'audit-date-warning': scope.row.type === 'task_soon' }">
              {{ scope.row.time || '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="说明" prop="desc" min-width="240" show-overflow-tooltip />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button link type="primary" icon="Position" @click="goAuditTodo(scope.row)">去处理</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="audit-todo-mobile-list">
        <div v-if="loading" class="my-task-mobile-loading">
          <el-skeleton :rows="4" animated />
        </div>
        <template v-else>
        <el-empty v-if="auditTodoList.length === 0" description="暂无审计待办" />
        <section v-for="item in auditTodoList" :key="item.key" class="audit-todo-card">
          <div class="audit-todo-card-head">
            <el-tag :type="auditTodoTag(item.type)" size="small">{{ item.typeLabel }}</el-tag>
            <span :class="{ danger: item.type === 'task_overdue', warning: item.type === 'task_soon' }">{{ item.time || '-' }}</span>
          </div>
          <strong>{{ item.title }}</strong>
          <div class="audit-todo-meta">
            <span><em>编号</em><b>{{ item.bizNo || '-' }}</b></span>
            <span><em>责任/联系</em><b>{{ item.owner || '-' }}</b></span>
          </div>
          <p>{{ item.desc || '请进入对应业务模块处理。' }}</p>
          <el-button type="primary" plain icon="Position" @click="goAuditTodo(item)">去处理</el-button>
        </section>
        </template>
      </div>
    </template>

    <template v-else>
    <el-form class="desktop-query-form" v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true">
      <el-form-item label="任务编号" prop="taskNo">
        <el-input
          v-model="queryParams.taskNo"
          placeholder="请输入任务编号"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 200px">
          <el-option
            v-for="dict in taskStatusOptions"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="截止日期" prop="overdue">
        <el-select v-model="queryParams.overdue" placeholder="临期/逾期筛选" clearable style="width: 200px">
          <el-option label="全部" value="" />
          <el-option label="临期（7天内）" value="soon" />
          <el-option label="已逾期" value="overdue" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <MobileFilterBar
      v-model="queryParams.taskNo"
      placeholder="搜索任务编号"
      :active-count="myTaskFilterCount"
      @search="handleQuery"
      @reset="resetQuery"
    >
      <el-form label-position="top">
        <el-form-item label="任务状态">
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable>
            <el-option v-for="dict in taskStatusOptions" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="截止日期">
          <el-select v-model="queryParams.overdue" placeholder="全部日期" clearable>
            <el-option label="临期（7天内）" value="soon" />
            <el-option label="已逾期" value="overdue" />
          </el-select>
        </el-form-item>
      </el-form>
    </MobileFilterBar>

    <el-row :gutter="20" class="mb8">
      <el-col v-for="card in statCards" :key="card.status" :xs="12" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card" :class="card.cardClass">
          <div class="stat-card-content">
            <div class="stat-card-left">
              <div class="stat-card-value">{{ card.count }}</div>
              <div class="stat-card-label">{{ card.label }}</div>
            </div>
            <div class="stat-card-right">
              <el-icon :size="36"><component :is="card.icon" /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-table class="my-task-table" v-loading="loading" :data="taskList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="任务编号" align="center" prop="taskNo" width="150" />
      <el-table-column label="联系人" align="center" prop="contactPerson" width="100" />
      <el-table-column label="截止日期" align="center" prop="deadline" width="130">
        <template #default="scope">
          <span :style="{ color: isOverdue(scope.row.deadline) ? '#f56c6c' : '' }">
            {{ scope.row.deadline || '-' }}
            <el-tag v-if="isOverdue(scope.row.deadline)" type="danger" size="small" class="ml5">逾期</el-tag>
            <el-tag v-else-if="isNearDeadline(scope.row.deadline)" type="warning" size="small" class="ml5">临期</el-tag>
          </span>
        </template>
      </el-table-column>
      <el-table-column label="任务状态" align="center" prop="status" width="110">
        <template #default="scope">
          <el-tag :type="taskStatusTag(scope.row.status)">
            {{ taskStatusLabel(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="单位审批" align="center" width="110">
        <template #default="scope">
          <el-tag v-if="scope.row.approStatus === '0'" type="warning">待审批</el-tag>
          <el-tag v-else-if="scope.row.approStatus === '1'" type="success">已通过</el-tag>
          <el-tag v-else-if="scope.row.approStatus === '2'" type="danger">已驳回</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="销号状态" align="center" width="140">
        <template #default="scope">
          <el-tooltip
            v-if="scope.row.closureStatus === '2' && scope.row.reRectRequired"
            :content="closureRejectMessage(scope.row)"
            placement="top"
          >
            <el-tag type="danger">审计处驳回</el-tag>
          </el-tooltip>
          <el-tag v-else-if="scope.row.closureStatus === '0'" type="warning">待审核</el-tag>
          <el-tag v-else-if="scope.row.closureStatus === '1'" type="success">已销号</el-tag>
          <el-tag v-else-if="scope.row.closureStatus === '2'" type="danger">审计处驳回</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="下发时间" align="center" prop="dispatchTime" min-width="160" />
      <el-table-column v-if="isLiaison" label="确认" align="center" width="110">
        <template #default="scope">
          <el-button
            v-if="scope.row.status === '0'"
            v-hasPermi="['rectification:task:confirm']"
            link
            type="primary"
            icon="Check"
            @click="handleConfirm(scope.row)"
          >
            确认接收
          </el-button>
          <el-tag v-else type="success">已确认</el-tag>
        </template>
      </el-table-column>
      <el-table-column v-if="isLiaison" label="分办" align="center" width="130">
        <template #default="scope">
          <el-button
            v-if="scope.row.status === '1'"
            v-hasPermi="['rectification:task:assign']"
            link
            type="primary"
            icon="User"
            @click="handleAssign(scope.row)"
          >
            分办责任人
          </el-button>
        </template>
      </el-table-column>
      <el-table-column label="执行操作" align="center" :width="isMobile ? 210 : 260" :fixed="isMobile ? false : 'right'">
        <template #default="scope">
          <el-button v-if="!isUnitLeader" link type="primary" icon="View" @click="handleDetail(scope.row)">详情</el-button>
          <el-button
            v-if="isLiaison || isResponsible"
            link
            type="warning"
            icon="Document"
            @click="handleDownloadNotice(scope.row)"
          >通知书</el-button>
          <el-button
            v-if="!isLiaison && ['1', '2'].includes(scope.row.status)"
            v-hasPermi="['rectification:plan:edit']"
            link
            type="primary"
            icon="Edit"
            @click="handleEditPlan(scope.row)"
          >
            方案
          </el-button>
          <el-button
            v-if="!isLiaison && canModifyReport(scope.row)"
            v-hasPermi="['rectification:report:add']"
            link
            type="warning"
            icon="DocumentChecked"
            @click="handleSubmitReport(scope.row)"
          >
            修改报告
          </el-button>
          <el-button
            v-else-if="!isLiaison && ['1', '2'].includes(scope.row.status)"
            v-hasPermi="['rectification:report:submit']"
            link
            type="primary"
            icon="Document"
            @click="handleSubmitReport(scope.row)"
          >
            报告
          </el-button>
          <el-button
            v-if="!isLiaison && canApplyClosure(scope.row)"
            v-hasPermi="['rectification:closure:apply']"
            link
            type="danger"
            icon="CircleCheck"
            @click="handleApplyClosure(scope.row)"
          >
            销号
          </el-button>
          <el-button
            v-if="['1', '2', '3'].includes(scope.row.status)"
            v-hasPermi="['rectification:report:approve']"
            link
            type="warning"
            icon="Checked"
            @click="handleGoApproval(scope.row)"
          >
            审批
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="my-task-mobile-list">
      <div v-if="loading" class="my-task-mobile-loading">
        <el-skeleton :rows="4" animated />
      </div>
      <template v-else>
      <el-empty v-if="taskList.length === 0" description="暂无我的任务" />
      <section
        v-for="item in taskList"
        :key="item.taskId"
        class="my-task-card"
        :class="{ 'notice-target-task': isNoticeTarget(item.taskId) }"
      >
        <div class="my-task-card-header">
          <div class="my-task-card-title">
            <strong>{{ item.issueTitle || '整改任务' }}</strong>
            <span>{{ item.taskNo || '-' }}</span>
          </div>
          <el-tag :type="taskStatusTag(item.status)">{{ taskStatusLabel(item.status) }}</el-tag>
        </div>

        <div class="my-task-card-meta">
          <span><em>联系人</em><b>{{ item.contactPerson || '-' }}</b></span>
          <span>
            <em>截止日期</em>
            <b :class="{ danger: isOverdue(item.deadline), warning: !isOverdue(item.deadline) && isNearDeadline(item.deadline) }">
              {{ item.deadline || '-' }}
            </b>
          </span>
          <span>
            <em>单位审批</em>
            <b>{{ approvalStatusLabel(item.approStatus) }}</b>
          </span>
          <span>
            <em>销号状态</em>
            <b>{{ closureStatusLabel(item.closureStatus) }}</b>
          </span>
        </div>

        <div v-if="item.closureStatus === '2' && item.reRectRequired" class="my-task-warning">
          <strong>{{ closureRejectActor(item) }}驳回销号申请</strong>
          <span>补充整改要求：{{ item.reRectRequired }}</span>
        </div>

        <div class="my-task-card-actions">
          <el-button v-if="!isUnitLeader" type="primary" plain icon="View" @click="handleDetail(item)">详情</el-button>
          <el-button
            v-if="isLiaison || isResponsible"
            type="warning"
            plain
            icon="Document"
            @click="handleDownloadNotice(item)"
          >通知书</el-button>
          <el-button
            v-if="isLiaison && item.status === '0'"
            v-hasPermi="['rectification:task:confirm']"
            type="success"
            plain
            icon="Check"
            @click="handleConfirm(item)"
          >接收</el-button>
          <el-button
            v-if="isLiaison && item.status === '1'"
            v-hasPermi="['rectification:task:assign']"
            type="primary"
            plain
            icon="User"
            @click="handleAssign(item)"
          >分办</el-button>
          <el-button
            v-if="!isLiaison && ['1', '2'].includes(item.status)"
            v-hasPermi="['rectification:plan:edit']"
            type="primary"
            plain
            icon="Edit"
            @click="handleEditPlan(item)"
          >方案</el-button>
          <el-button
            v-if="!isLiaison && canModifyReport(item)"
            v-hasPermi="['rectification:report:add']"
            type="warning"
            plain
            icon="DocumentChecked"
            @click="handleSubmitReport(item)"
          >修改报告</el-button>
          <el-button
            v-else-if="!isLiaison && ['1', '2'].includes(item.status)"
            v-hasPermi="['rectification:report:submit']"
            type="primary"
            plain
            icon="Document"
            @click="handleSubmitReport(item)"
          >报告</el-button>
          <el-button
            v-if="!isLiaison && canApplyClosure(item)"
            v-hasPermi="['rectification:closure:apply']"
            type="danger"
            plain
            icon="CircleCheck"
            @click="handleApplyClosure(item)"
          >销号</el-button>
          <el-button
            v-if="['1', '2', '3'].includes(item.status)"
            v-hasPermi="['rectification:report:approve']"
            type="warning"
            plain
            icon="Checked"
            @click="handleGoApproval(item)"
          >审批</el-button>
        </div>
      </section>
      </template>
    </div>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <el-dialog class="mobile-form-dialog task-confirm-dialog" title="确认接收任务" v-model="confirmOpen" :width="dialogWidth('500px')" append-to-body>
      <el-alert
        title="确认接收后将正式开始整改工作，系统开始计时。"
        type="warning"
        :closable="false"
        show-icon
        class="mb15"
      />
      <el-descriptions :column="1" border>
        <el-descriptions-item label="任务编号">{{ currentTask.taskNo }}</el-descriptions-item>
        <el-descriptions-item label="关联问题">{{ currentTask.issueTitle || '-' }}</el-descriptions-item>
        <el-descriptions-item label="截止日期">{{ currentTask.deadline || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button type="primary" :loading="submitLoading" @click="submitConfirm">确认接收</el-button>
        <el-button @click="confirmOpen = false">取消</el-button>
      </template>
    </el-dialog>

    <el-dialog class="mobile-form-dialog task-closure-dialog" title="申请销号" v-model="closureOpen" :width="dialogWidth('700px')" append-to-body>
      <div v-show="closureLoading" class="loading-box">正在生成整改报告...</div>
      <div v-show="!closureLoading">
        <h4 class="preview-title">整改报告文件</h4>
        <div class="closure-file-row">
          <el-button type="primary" plain icon="Download" @click="downloadClosureReport">
            下载Word整改报告
          </el-button>
        </div>
        <h4 class="preview-title">佐证附件材料</h4>
        <div v-if="closureMaterials.length > 0" class="closure-file-list">
          <el-button
            v-for="m in closureMaterials"
            :key="m.materialId || m.id"
            type="primary"
            plain
            size="small"
            icon="Download"
            @click="downloadClosureMaterial(m)"
          >
            {{ m.fileName || '附件' }}
          </el-button>
        </div>
        <el-alert v-else title="暂无佐证附件，请先在任务详情中上传整改材料附件" type="warning" :closable="false" show-icon class="mb15" />
        <el-form class="mobile-dialog-form" ref="closureFormRef" :model="closureForm" label-width="100px">
          <el-form-item label="销号附言">
            <el-input
              v-model="closureForm.remark"
              type="textarea"
              :rows="2"
              placeholder="选填，补充说明"
              maxlength="300"
              show-word-limit
            />
          </el-form-item>
          <el-form-item>
            <el-checkbox v-model="closureForm.agreed" class="agreement-text">
              本人承诺，本次提交的所有整改材料及佐证附件均真实、准确、有效。
            </el-checkbox>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button type="primary" :loading="submitLoading" :disabled="!closureForm.agreed" @click="submitClosure">确认销号</el-button>
        <el-button @click="closureOpen = false">取消</el-button>
      </template>
    </el-dialog>


    <el-dialog class="mobile-form-dialog task-extension-dialog" title="申请延期" v-model="extensionOpen" :width="dialogWidth('500px')" append-to-body>
      <el-form class="mobile-dialog-form" ref="extensionFormRef" :model="extensionForm" :rules="extensionRules" label-width="100px">
        <el-form-item label="任务编号">
          <span>{{ currentTask.taskNo }}</span>
        </el-form-item>
        <el-form-item label="原截止日期">
          <span>{{ currentTask.deadline }}</span>
        </el-form-item>
        <el-form-item label="延期至" prop="newDeadline">
          <el-date-picker
            v-model="extensionForm.newDeadline"
            type="date"
            placeholder="请选择新截止日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="延期原因" prop="reason">
          <el-input v-model="extensionForm.reason" type="textarea" :rows="3" placeholder="请说明延期原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" :loading="submitLoading" @click="submitExtension">提交申请</el-button>
        <el-button @click="extensionOpen = false">取消</el-button>
      </template>
    </el-dialog>

    <AssignDialog v-model="assignOpen" :task-id="assignTaskId" @success="onAssignSuccess" />
    <LeaderApproval
      v-model="approOpen"
      :report-id="approReportId"
      :task-id="approTaskId"
      :content="approContent"
      :report="approReport"
      :materials="approMaterials"
      @success="handleApprovalSuccess"
    />
    </template>
  </div>
</template>

<script setup name="MyTasks">
import { ref, reactive, toRefs, computed, onMounted, getCurrentInstance, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { listTask, listMyTask, confirmTask, getTask, generateNotice } from '@/api/rectification/task'
import { listIssue } from '@/api/rectification/issue'
import { applyClosure } from '@/api/rectification/closure'
import { listClosure } from '@/api/rectification/closure'
import { applyExtension } from '@/api/rectification/plan'
import { downloadReportWord } from '@/api/rectification/report'
import { downloadMaterial } from '@/api/rectification/material'
import request from '@/utils/request'
import useUserStore from '@/store/modules/user'
import useAppStore from '@/store/modules/app'
import AssignDialog from './components/AssignDialog.vue'
import LeaderApproval from '../report/LeaderApproval.vue'
import { saveAs } from 'file-saver'
import MobileFilterBar from '@/components/MobileFilterBar/index.vue'

const router = useRouter()
const route = useRoute()
const { proxy } = getCurrentInstance()
const userStore = useUserStore()
const appStore = useAppStore()
const isMobile = computed(() => appStore.device === 'mobile')

const isUnitLeader = computed(() => {
  const perms = userStore.permissions || []
  return perms.includes('rectification:report:approve') && !perms.includes('rectification:plan:edit')
})
const isLiaison = computed(() => (userStore.roles || []).includes('audited_unit_liaison'))
const isResponsible = computed(() => (userStore.roles || []).includes('rect_responsible'))
const isAuditManager = computed(() => {
  const roles = userStore.roles || []
  return roles.includes('admin') || roles.includes('audit_director') || roles.includes('audit_lead')
})

const taskList = ref([])
const auditRawTodos = ref([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const total = ref(0)
const confirmOpen = ref(false)
const closureOpen = ref(false)
const extensionOpen = ref(false)
const submitLoading = ref(false)
const currentTask = ref({})

const assignTaskId = ref(null)
const assignOpen = ref(false)
const approOpen = ref(false)
const approReportId = ref(0)
const approTaskId = ref(null)
const approContent = ref('')
const approReport = ref({})
const approMaterials = ref([])

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    taskNo: undefined,
    status: undefined,
    overdue: undefined,
    auditType: undefined
  }
})

const { queryParams } = toRefs(data)

const myTaskFilterCount = computed(() => [
  queryParams.value.status,
  queryParams.value.overdue
].filter(Boolean).length)

const closureForm = reactive({
  taskId: undefined,
  issueId: undefined,
  remark: '',
  agreed: false
})
const closureReportContent = ref('')
const closureMaterials = ref([])
const closureResponsibleName = ref('')
const closureLoading = ref(false)

const extensionForm = reactive({
  taskId: undefined,
  newDeadline: undefined,
  reason: undefined
})

const extensionRules = reactive({
  newDeadline: [{ required: true, message: '新截止日期不能为空', trigger: 'change' }],
  reason: [{ required: true, message: '延期原因不能为空', trigger: 'blur' }]
})

const taskStatusOptions = ref([
  { label: '待确认', value: '0' },
  { label: '整改中', value: '1' },
  { label: '已提交报告', value: '2' },
  { label: '待审核', value: '3' },
  { label: '已完成', value: '4' }
])

const statCards = computed(() => {
  const all = taskList.value
  return [
    { status: 'all', label: '全部任务', count: total.value, icon: 'List', cardClass: '' },
    { status: '0', label: '待接收', count: all.filter(t => t.status === '0').length, icon: 'Clock', cardClass: 'stat-card-info' },
    { status: '1', label: '整改中', count: all.filter(t => t.status === '1').length, icon: 'Loading', cardClass: 'stat-card-warning' },
    { status: '4', label: '已完成', count: all.filter(t => t.status === '4').length, icon: 'CircleCheck', cardClass: 'stat-card-success' }
  ]
})

const auditTodoList = computed(() => {
  const keyword = (queryParams.value.taskNo || '').trim().toLowerCase()
  const type = queryParams.value.auditType
  return auditRawTodos.value.filter(item => {
    const matchesType = !type || item.type === type
    const matchesKeyword = !keyword
      || String(item.title || '').toLowerCase().includes(keyword)
      || String(item.bizNo || '').toLowerCase().includes(keyword)
      || String(item.owner || '').toLowerCase().includes(keyword)
    return matchesType && matchesKeyword
  })
})

const auditStatCards = computed(() => {
  const list = auditRawTodos.value
  return [
    { type: undefined, label: '全部待办', count: list.length, tone: 'all' },
    { type: 'issue_pending', label: '待下发问题', count: countAuditTodo('issue_pending'), tone: 'blue' },
    { type: 'closure_pending', label: '待审销号', count: countAuditTodo('closure_pending'), tone: 'green' },
    { type: 'task_overdue', label: '逾期未整改', count: countAuditTodo('task_overdue'), tone: 'red' },
    { type: 'task_soon', label: '即将到期', count: countAuditTodo('task_soon'), tone: 'amber' }
  ]
})

function countAuditTodo(type) {
  return auditRawTodos.value.filter(item => item.type === type).length
}

function auditTodoTag(type) {
  const map = {
    issue_pending: 'info',
    closure_pending: 'success',
    closure_rejected: 'danger',
    task_overdue: 'danger',
    task_soon: 'warning'
  }
  return map[type] || ''
}

function filterAuditType(type) {
  queryParams.value.auditType = type
}

function taskStatusLabel(val) {
  const item = taskStatusOptions.value.find(d => d.value === val)
  return item ? item.label : (val || '-')
}

function taskStatusTag(val) {
  const map = { '0': 'info', '1': 'warning', '2': 'primary', '3': '', '4': 'success' }
  return map[val] || ''
}

function approvalStatusLabel(val) {
  const map = { '0': '待审批', '1': '已通过', '2': '已驳回' }
  return map[val] || '-'
}

function closureStatusLabel(val) {
  const map = { '0': '待审核', '1': '已销号', '2': '审计处已驳回' }
  return map[val] || '-'
}

function closureRejectActor(row) {
  return row.closureAuditor ? `审计处审核人（${row.closureAuditor}）` : '审计处'
}

function closureRejectMessage(row) {
  return `${closureRejectActor(row)}驳回销号申请。补充整改要求：${row.reRectRequired || '-'}`
}

function isOverdue(deadline) {
  if (!deadline) return false
  return new Date(deadline) < new Date()
}

function isNearDeadline(deadline) {
  if (!deadline) return false
  const now = new Date()
  const d = new Date(deadline)
  const diff = d.getTime() - now.getTime()
  return diff > 0 && diff < 7 * 24 * 60 * 60 * 1000
}

function canApplyClosure(row) {
  const reportApprovedAfterClosureReject = String(row.closureStatus) !== '2'
    || isAfter(row.reportApproveTime, row.closureAuditTime || row.closureUpdateTime || row.closureCreateTime)
  return row.reportChecked === true
    && row.reportId
    && ['1', '2'].includes(String(row.status))
    && String(row.approStatus) === '1'
    && String(row.reportStatus) === '2'
    && String(row.closureStatus) !== '0'
    && reportApprovedAfterClosureReject
}

function canModifyReport(row) {
  return String(row.approStatus) === '2' || isClosureRejectPending(row)
}

function isClosureRejectPending(row) {
  if (String(row.closureStatus) !== '2') return false
  return !isAfter(row.reportSubmitTime || row.reportUpdateTime, row.closureAuditTime || row.closureUpdateTime || row.closureCreateTime)
}

function isAfter(left, right) {
  if (!right) return true
  if (!left) return false
  const leftTime = new Date(left).getTime()
  const rightTime = new Date(right).getTime()
  return Number.isFinite(leftTime) && Number.isFinite(rightTime) && leftTime > rightTime
}

function parseFirstIssueId(issueIds) {
  try {
    const parsed = JSON.parse((issueIds || '[0]').replace(/'/g, '"'))
    return Array.isArray(parsed) && parsed.length > 0 ? parsed[0] : 0
  } catch (e) {
    return 0
  }
}

function getList() {
  if (isAuditManager.value) {
    getAuditTodoList()
    return
  }
  loading.value = true
  listMyTask(queryParams.value).then(response => {
    const tasks = prioritizeNoticeTask(response.rows || [])
    total.value = response.total || 0
    if (tasks.length === 0) {
      taskList.value = []
      return
    }
    return Promise.all(tasks.map(t => {
      t.approStatus = null
      t.reportStatus = null
      t.reportId = null
      t.reportChecked = false
      t.reportApproveTime = null
      t.reportSubmitTime = null
      t.reportUpdateTime = null
      t.closureStatus = null
      t.closureId = null
      t.closureAuditTime = null
      t.closureUpdateTime = null
      t.closureCreateTime = null
      t.closureAuditor = ''
      t.reRectRequired = ''
      const reportReq = request({ url: '/rectification/report/' + t.taskId, method: 'get' }).then(r => {
        const rpt = r.data || {}
        t.approStatus = rpt.unitApproveStatus || null
        t.reportStatus = rpt.status || null
        t.reportId = rpt.reportId || null
        t.reportApproveTime = rpt.unitApproveTime || null
        t.reportSubmitTime = rpt.submitTime || null
        t.reportUpdateTime = rpt.updateTime || null
        t.reportChecked = true
      }).catch(() => {})
      const closureReq = request({ url: '/rectification/closure/task/' + t.taskId + '/latest', method: 'get' }).then(r => {
        const closure = r.data || {}
        t.closureStatus = closure.status || null
        t.closureId = closure.closureId || null
        t.closureAuditTime = closure.auditTime || null
        t.closureUpdateTime = closure.updateTime || null
        t.closureCreateTime = closure.createTime || null
        t.closureAuditor = closure.updateBy || closure.auditBy || ''
        t.reRectRequired = closure.reRectRequired || ''
      }).catch(() => {})
      return Promise.all([reportReq, closureReq])
    })).then(() => {
      taskList.value = [...tasks]
    })
  }).finally(() => {
    loading.value = false
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  if (isAuditManager.value) {
    queryParams.value.taskNo = undefined
    queryParams.value.auditType = undefined
    handleQuery()
    return
  }
  proxy.resetForm('queryRef')
  handleQuery()
}

function prioritizeNoticeTask(tasks) {
  const taskId = route.query.taskId
  if (!taskId) return tasks
  return [...tasks].sort((left, right) => {
    if (String(left.taskId) === String(taskId)) return -1
    if (String(right.taskId) === String(taskId)) return 1
    return 0
  })
}

function isNoticeTarget(taskId) {
  return !!route.query.taskId && String(route.query.taskId) === String(taskId)
}

function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.taskId)
}

function handleAssign(row) {
  assignTaskId.value = row.taskId
  assignOpen.value = true
}

function onAssignSuccess() {
  assignOpen.value = false
  getList()
}

function handleConfirm(row) {
  currentTask.value = row
  confirmOpen.value = true
}

function submitConfirm() {
  submitLoading.value = true
  confirmTask(currentTask.value.taskId).then(() => {
    proxy.$modal.msgSuccess('任务已确认接收')
    confirmOpen.value = false
    getList()
  }).finally(() => {
    submitLoading.value = false
  })
}

function handleGoApproval(row) {
  getTask(row.taskId).then(res => {
    const task = res.data || {}
    const issueId = parseFirstIssueId(task.issueIds)
    const reportReq = request({ url: '/rectification/report/' + row.taskId, method: 'get' })
    const materialReq = request({ url: '/rectification/material/list/' + issueId, method: 'get' })
    Promise.all([reportReq, materialReq]).then(([reportRes, materialRes]) => {
      const report = reportRes.data || {}
      approReportId.value = report.reportId || 0
      approTaskId.value = row.taskId
      approContent.value = report.reportContent || ''
      approReport.value = report
      approMaterials.value = materialRes.rows || []
      approOpen.value = true
    })
  })
}

function handleDetail(row) {
  router.push('/rectification/task-page/detail/' + row.taskId)
}

async function handleDownloadNotice(row) {
  try {
    const response = await generateNotice(row.taskId)
    const blob = response && response.data
    if (!blob) throw new Error('通知书内容为空')
    if (blob.type && blob.type.includes('application/json')) {
      const result = JSON.parse(await blob.text())
      throw new Error(result.msg || '通知书下载失败')
    }
    saveAs(new Blob([blob], {
      type: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
    }), `整改通知书_${row.taskNo || row.taskId}.docx`)
  } catch (error) {
    proxy.$modal.msgError(error?.message || '通知书下载失败')
  }
}

function handleEditPlan(row) {
  router.push('/rectification/task-page/detail/' + row.taskId + '?tab=plan')
}

function handleSubmitReport(row) {
  const tab = canModifyReport(row) ? 'plan' : 'report'
  router.push('/rectification/task-page/detail/' + row.taskId + '?tab=' + tab)
}

function handleApplyClosure(row) {
  if (!canApplyClosure(row)) {
    proxy.$modal.msgWarning('整改报告需经被审单位负责人审批通过后，方可申请销号')
    return
  }
  currentTask.value = row
  closureForm.taskId = row.taskId
  closureForm.issueId = parseFirstIssueId(row.issueIds)
  closureForm.remark = ''
  closureForm.agreed = false
  closureLoading.value = true
  closureReportContent.value = ''
  closureMaterials.value = []
  closureResponsibleName.value = ''
  closureOpen.value = true

  const materialReq = request({ url: '/rectification/material/list/' + closureForm.issueId, method: 'get' })
  const planReq = request({ url: '/rectification/plan/' + row.taskId, method: 'get' })

  planReq.then(planRes => {
    const plan = planRes.data || {}
    if (plan.responsibleUserId) {
      request({ url: '/system/user/list', method: 'get', params: { userId: plan.responsibleUserId } }).then(userRes => {
        const user = (userRes.rows || [])[0]
        closureResponsibleName.value = user ? (user.nickName || user.userName || '') : ''
      }).catch(() => {})
    }
    closureResponsibleName.value = closureResponsibleName.value || plan.responsiblePerson || plan.rectifyPerson || ''
  }).catch(() => {})

  materialReq.then(materialRes => {
    const materials = materialRes.rows || []
    closureMaterials.value = materials
    closureReportContent.value = ''
  }).catch(() => {
    closureReportContent.value = '加载失败，请重试'
  }).finally(() => {
    closureLoading.value = false
  })
}

function handleApprovalSuccess() {
  approOpen.value = false
  nextTick(() => getList())
}

function getAuditTodoList() {
  loading.value = true
  const commonParams = { pageNum: 1, pageSize: 999 }
  Promise.all([
    listIssue({ ...commonParams, status: '0' }).catch(() => ({ rows: [] })),
    listTask(commonParams).catch(() => ({ rows: [] })),
    listClosure(commonParams).catch(() => ({ rows: [] }))
  ]).then(([issueRes, taskRes, closureRes]) => {
    const issues = issueRes.rows || []
    const tasks = taskRes.rows || []
    const closures = closureRes.rows || []
    const todos = []

    issues.forEach(issue => {
      todos.push({
        key: 'issue-' + issue.issueId,
        type: 'issue_pending',
        typeLabel: '待下发问题',
        title: issue.issueTitle || '待下发问题',
        bizNo: issue.issueNo,
        owner: issue.responsiblePerson || '-',
        time: issue.deadline || issue.createTime,
        desc: '问题已进入台账，尚未下发整改任务。',
        path: '/rectification/task'
      })
    })

    closures.filter(item => String(item.status) === '0').forEach(item => {
      todos.push({
        key: 'closure-' + item.closureId,
        type: 'closure_pending',
        typeLabel: '待审销号',
        title: item.issueTitle || '销号申请待审核',
        bizNo: item.closureNo || item.taskNo || item.closureId,
        owner: item.applicant || item.applyBy || item.createBy || '-',
        time: item.applyTime || item.createTime,
        desc: closureRemarkText(item) || '整改单位已提交销号申请，请审核整改报告和佐证材料。',
        path: '/rectification/closure'
      })
    })

    closures.filter(item => String(item.status) === '2').forEach(item => {
      todos.push({
        key: 'closure-reject-' + item.closureId,
        type: 'closure_rejected',
        typeLabel: '整改不到位',
        title: item.issueTitle || '整改不到位待跟踪',
        bizNo: item.closureNo || item.taskNo || item.closureId,
        owner: item.applicant || item.applyBy || item.createBy || '-',
        time: item.auditTime || item.updateTime || item.createTime,
        desc: item.reRectRequired || item.auditOpinion || '销号已驳回，请跟踪二次整改进展。',
        path: item.taskId ? ('/rectification/task-page/detail/' + item.taskId) : '/rectification/closure'
      })
    })

    tasks.filter(task => isActiveTask(task) && isOverdue(task.deadline)).forEach(task => {
      todos.push(taskTodo(task, 'task_overdue', '逾期未整改', '整改任务已超过截止日期，请催办或跟踪处理。'))
    })

    tasks.filter(task => isActiveTask(task) && !isOverdue(task.deadline) && isNearDeadline(task.deadline)).forEach(task => {
      todos.push(taskTodo(task, 'task_soon', '即将到期', '整改任务将在7天内到期，请提前关注进度。'))
    })

    auditRawTodos.value = sortAuditTodos(todos)
    total.value = auditRawTodos.value.length
  }).finally(() => {
    loading.value = false
  })
}

function taskTodo(task, type, typeLabel, desc) {
  return {
    key: type + '-' + task.taskId,
    type,
    typeLabel,
    title: task.issueTitle || '整改任务',
    bizNo: task.taskNo || task.taskId,
    owner: task.contactPerson || task.responsiblePerson || '-',
    time: task.deadline,
    desc,
    path: '/rectification/task-page/detail/' + task.taskId
  }
}

function isActiveTask(task) {
  return !['4', '5'].includes(String(task.status))
}

function closureRemarkText(row) {
  const content = row.applyContent || row.description || row.remark || ''
  if (!content) return ''
  return String(content).split('\n')[0]
}

function sortAuditTodos(list) {
  const order = {
    task_overdue: 1,
    closure_pending: 2,
    closure_rejected: 3,
    issue_pending: 4,
    task_soon: 5
  }
  return list.sort((a, b) => {
    const orderDiff = (order[a.type] || 99) - (order[b.type] || 99)
    if (orderDiff !== 0) return orderDiff
    return String(b.time || '').localeCompare(String(a.time || ''))
  })
}

function goAuditTodo(row) {
  if (row.path) router.push(row.path)
}

function dialogWidth(desktopWidth) {
  return isMobile.value ? '94vw' : desktopWidth
}

function downloadClosureReport() {
  if (!closureForm.taskId) return
  downloadReportWord(closureForm.taskId).then(response => {
    saveClosureBlob(response.data, closureFallbackReportName())
  })
}

function downloadClosureMaterial(row) {
  const materialId = row.materialId || row.id
  if (!materialId) {
    proxy.$modal.msgWarning('材料ID不可用')
    return
  }
  downloadMaterial(materialId).then(blob => {
    saveClosureBlob(blob, row.fileName || ('material_' + materialId))
  })
}

async function saveClosureBlob(blob, filename) {
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

function closureFallbackReportName() {
  const name = userStore.name || '登录用户'
  return name + '整改报告.docx'
}

function submitClosure() {
  if (!closureForm.agreed) {
    proxy.$modal.msgWarning('请勾选真实性承诺')
    return
  }
  submitLoading.value = true
  applyClosure({
    taskId: closureForm.taskId,
    issueId: closureForm.issueId,
    applyContent: (closureForm.remark || '申请销号')
      + '\n\n报告文件：' + closureFallbackReportName()
      + '\n附件材料：' + (closureMaterials.value.map(m => m.fileName).join('、') || '无')
  }).then(() => {
    proxy.$modal.msgSuccess('销号申请已提交')
    closureOpen.value = false
    window.setTimeout(() => {
      getList()
    }, 300)
  }).finally(() => {
    submitLoading.value = false
  })
}

function handleApplyExtension(row) {
  currentTask.value = row
  extensionForm.taskId = row.taskId
  extensionForm.newDeadline = undefined
  extensionForm.reason = undefined
  if (proxy.$refs.extensionFormRef) {
    proxy.$refs.extensionFormRef.resetFields()
  }
  extensionOpen.value = true
}

function submitExtension() {
  proxy.$refs.extensionFormRef.validate(valid => {
    if (!valid) return
    submitLoading.value = true
    applyExtension(extensionForm).then(() => {
      proxy.$modal.msgSuccess('延期申请已提交')
      extensionOpen.value = false
      getList()
    }).finally(() => {
      submitLoading.value = false
    })
  })
}

onMounted(() => {
  getList()
})

watch(() => route.query.taskId, (next, previous) => {
  if (next !== previous) getList()
})
</script>

<style scoped>
.stat-card {
  cursor: pointer;
  transition: all 0.3s;
}
.stat-card:hover {
  transform: translateY(-2px);
}
.stat-card-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.stat-card-left {
  flex: 1;
}
.stat-card-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}
.stat-card-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}
.stat-card-right {
  color: #c0c4cc;
}
.stat-card-info .stat-card-right {
  color: #409eff;
}
.stat-card-warning .stat-card-right {
  color: #e6a23c;
}
.stat-card-success .stat-card-right {
  color: #67c23a;
}
.mb8 {
  margin-bottom: 8px;
}
.mb15 {
  margin-bottom: 15px;
}
.ml5 {
  margin-left: 5px;
}
.loading-box {
  text-align: center;
  padding: 20px;
}
.preview-title {
  margin: 0 0 5px;
}
.report-preview {
  max-height: 280px;
  overflow-y: auto;
  background: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  white-space: pre-wrap;
  font-size: 13px;
  margin-bottom: 15px;
}
.closure-file-row,
.closure-file-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 15px;
}
.agreement-text {
  white-space: normal;
  line-height: 1.6;
}

.my-task-mobile-list {
  display: none;
}

.my-task-mobile-loading {
  padding: 16px;
  border: 1px solid #e3eaf4;
  border-radius: 8px;
  background: #fff;
}

.audit-todo-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px;
  margin-bottom: 16px;
  border: 1px solid #e1e8f2;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 10px 24px rgba(45, 67, 96, 0.06);
}

.audit-todo-header h2 {
  margin: 0;
  color: #1f2f46;
  font-size: 20px;
}

.audit-todo-header p {
  margin: 6px 0 0;
  color: #6f7e93;
  font-size: 13px;
  line-height: 1.5;
}

.audit-summary {
  margin-bottom: 14px;
  row-gap: 12px;
}

.audit-summary-card {
  width: 100%;
  min-height: 92px;
  padding: 14px;
  border: 1px solid #e4ebf5;
  border-radius: 8px;
  background: #fff;
  text-align: left;
  cursor: pointer;
  box-shadow: 0 8px 20px rgba(45, 67, 96, 0.05);
}

.audit-summary-card span {
  display: block;
  color: #6f7e93;
  font-size: 13px;
}

.audit-summary-card strong {
  display: block;
  margin-top: 8px;
  color: #1f2f46;
  font-size: 28px;
  line-height: 1;
}

.audit-summary-card.blue { background: #f3f7ff; }
.audit-summary-card.green { background: #f0fbf5; }
.audit-summary-card.red { background: #fff4f2; }
.audit-summary-card.amber { background: #fff8eb; }

.audit-filter {
  padding: 14px 16px 2px;
  margin-bottom: 14px;
  border: 1px solid #e1e8f2;
  border-radius: 8px;
  background: #fff;
}

.audit-date-danger {
  color: #f56c6c;
  font-weight: 600;
}

.audit-date-warning {
  color: #e6a23c;
  font-weight: 600;
}

.audit-todo-mobile-list {
  display: none;
}

.audit-todo-card {
  padding: 14px;
  margin-bottom: 12px;
  border: 1px solid #e3eaf4;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 8px 22px rgba(36, 52, 75, 0.06);
}

.audit-todo-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 10px;
}

.audit-todo-card-head span {
  color: #7a8798;
  font-size: 12px;
}

.audit-todo-card-head span.danger {
  color: #f56c6c;
}

.audit-todo-card-head span.warning {
  color: #e6a23c;
}

.audit-todo-card > strong {
  display: block;
  color: #1f2f46;
  font-size: 15px;
  line-height: 1.45;
}

.audit-todo-meta {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  padding: 12px 0 8px;
}

.audit-todo-meta em {
  display: block;
  color: #8a96a8;
  font-size: 12px;
  font-style: normal;
}

.audit-todo-meta b {
  display: block;
  margin-top: 3px;
  color: #2c3e57;
  font-size: 13px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.audit-todo-card p {
  margin: 0 0 12px;
  color: #69788d;
  font-size: 13px;
  line-height: 1.5;
}

.audit-todo-card .el-button {
  width: 100%;
  margin-left: 0;
}

.my-task-card {
  padding: 14px;
  margin-bottom: 12px;
  border: 1px solid #e3eaf4;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 8px 22px rgba(36, 52, 75, 0.06);
}

.my-task-card.notice-target-task {
  border-color: #409eff;
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.16);
}

.my-task-card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
}

.my-task-card-title {
  min-width: 0;
}

.my-task-card-title strong {
  display: block;
  color: #1f2f46;
  font-size: 15px;
  line-height: 1.45;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.my-task-card-title span {
  display: block;
  margin-top: 4px;
  color: #7a8798;
  font-size: 12px;
}

.my-task-card-meta {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px 12px;
  padding: 12px 0;
}

.my-task-card-meta span {
  min-width: 0;
}

.my-task-card-meta em {
  display: block;
  color: #8a96a8;
  font-size: 12px;
  line-height: 1.4;
  font-style: normal;
}

.my-task-card-meta b {
  display: block;
  margin-top: 3px;
  color: #2c3e57;
  font-size: 13px;
  font-weight: 500;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.my-task-card-meta b.danger {
  color: #f56c6c;
}

.my-task-card-meta b.warning {
  color: #e6a23c;
}

.my-task-warning {
  padding: 8px 10px;
  margin-bottom: 10px;
  color: #b42318;
  font-size: 12px;
  line-height: 1.5;
  border-radius: 6px;
  background: #fff1f0;
}

.my-task-warning strong,
.my-task-warning span {
  display: block;
}

.my-task-warning strong {
  margin-bottom: 3px;
  font-size: 13px;
}

.my-task-card-actions {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
  padding-top: 10px;
  border-top: 1px solid #eef2f7;
}

.my-task-card-actions .el-button {
  width: 100%;
  margin-left: 0;
}

@media (max-width: 768px) {
  .my-task-page {
    padding: 12px;
    padding-bottom: calc(32px + env(safe-area-inset-bottom));
    min-height: 100%;
    background: #f5f7fb;
  }

  .desktop-query-form {
    display: none;
  }

  .my-task-page :deep(.el-card__body) {
    padding: 12px;
  }

  .stat-card-value {
    font-size: 22px;
  }

  .stat-card-right {
    display: none;
  }

  .my-task-table {
    display: none;
  }

  .my-task-mobile-list {
    display: block;
    min-height: 120px;
    padding-bottom: 8px;
  }

  .my-task-page :deep(.pagination-container) {
    margin-bottom: env(safe-area-inset-bottom);
  }

  .audit-todo-header {
    display: block;
    padding: 16px;
  }

  .audit-todo-header .el-button {
    width: 100%;
    margin-top: 12px;
  }

  .audit-filter {
    padding: 12px 12px 0;
  }

  .audit-filter :deep(.el-form-item) {
    display: block;
    margin-right: 0;
  }

  .audit-filter :deep(.el-form-item__label) {
    display: block;
    text-align: left;
  }

  .audit-filter :deep(.el-input),
  .audit-filter :deep(.el-select),
  .audit-filter :deep(.el-button) {
    width: 100% !important;
  }

  .audit-filter :deep(.el-button + .el-button) {
    margin-top: 8px;
    margin-left: 0;
  }

  .audit-todo-table {
    display: none;
  }

  .audit-todo-mobile-list {
    display: block;
  }

  .my-task-page :deep(.el-dialog) {
    width: 94vw !important;
    margin-top: 5vh !important;
  }

  .my-task-page :deep(.el-dialog__body) {
    max-height: 72vh;
    overflow-y: auto;
    padding: 14px;
  }
}

@media (max-width: 420px) {
  .my-task-card-meta,
  .my-task-card-actions,
  .audit-todo-meta {
    grid-template-columns: 1fr;
  }
}
</style>
