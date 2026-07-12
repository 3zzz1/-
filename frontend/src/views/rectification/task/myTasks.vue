<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true">
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

    <el-row :gutter="20" class="mb8">
      <el-col v-for="card in statCards" :key="card.status" :span="6">
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

    <el-table v-loading="loading" :data="taskList" @selection-change="handleSelectionChange">
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
            :content="scope.row.reRectRequired"
            placement="top"
          >
            <el-tag type="danger">已驳回</el-tag>
          </el-tooltip>
          <el-tag v-else-if="scope.row.closureStatus === '0'" type="warning">待审核</el-tag>
          <el-tag v-else-if="scope.row.closureStatus === '1'" type="success">已销号</el-tag>
          <el-tag v-else-if="scope.row.closureStatus === '2'" type="danger">已驳回</el-tag>
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
      <el-table-column label="执行操作" align="center" width="260" fixed="right">
        <template #default="scope">
          <el-button v-if="!isUnitLeader" link type="primary" icon="View" @click="handleDetail(scope.row)">详情</el-button>
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

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <el-dialog title="确认接收任务" v-model="confirmOpen" width="500px" append-to-body>
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

    <el-dialog title="申请销号" v-model="closureOpen" width="700px" append-to-body>
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
        <el-form ref="closureFormRef" :model="closureForm" label-width="100px">
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


    <el-dialog title="申请延期" v-model="extensionOpen" width="500px" append-to-body>
      <el-form ref="extensionFormRef" :model="extensionForm" :rules="extensionRules" label-width="100px">
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
      @success="getList"
    />
  </div>
</template>

<script setup name="MyTasks">
import { ref, reactive, toRefs, computed, onMounted, getCurrentInstance } from 'vue'
import { useRouter } from 'vue-router'
import { listMyTask, confirmTask, getTask } from '@/api/rectification/task'
import { applyClosure } from '@/api/rectification/closure'
import { applyExtension } from '@/api/rectification/plan'
import { downloadReportWord } from '@/api/rectification/report'
import { downloadMaterial } from '@/api/rectification/material'
import request from '@/utils/request'
import useUserStore from '@/store/modules/user'
import AssignDialog from './components/AssignDialog.vue'
import LeaderApproval from '../report/LeaderApproval.vue'
import { saveAs } from 'file-saver'

const router = useRouter()
const { proxy } = getCurrentInstance()
const userStore = useUserStore()

const isUnitLeader = computed(() => {
  const perms = userStore.permissions || []
  return perms.includes('rectification:report:approve') && !perms.includes('rectification:plan:edit')
})
const isLiaison = computed(() => (userStore.roles || []).includes('audited_unit_liaison'))

const taskList = ref([])
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
    overdue: undefined
  }
})

const { queryParams } = toRefs(data)

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

function taskStatusLabel(val) {
  const item = taskStatusOptions.value.find(d => d.value === val)
  return item ? item.label : (val || '-')
}

function taskStatusTag(val) {
  const map = { '0': 'info', '1': 'warning', '2': 'primary', '3': '', '4': 'success' }
  return map[val] || ''
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
  loading.value = true
  listMyTask(queryParams.value).then(response => {
    const tasks = response.rows || []
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
  proxy.resetForm('queryRef')
  handleQuery()
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
  saveAs(new Blob([blob]), filename)
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
</style>
