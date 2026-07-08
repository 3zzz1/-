<template>
  <div class="app-container">
    <!-- Search Form -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
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

    <!-- Stats Cards -->
    <el-row :gutter="20" class="mb8">
      <el-col :span="6" v-for="card in statCards" :key="card.status">
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

    <!-- Table -->
    <el-table v-loading="loading" :data="taskList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="任务编号" align="center" prop="taskNo" width="140" />
      <el-table-column label="联系人" align="center" prop="contactPerson" width="90" />
      <el-table-column label="截止日期" align="center" prop="deadline" width="110">
        <template #default="scope">
          <span :style="{ color: isOverdue(scope.row.deadline) ? '#f56c6c' : '' }">
            {{ scope.row.deadline }}
            <el-tag v-if="isOverdue(scope.row.deadline)" type="danger" size="small" class="ml5">逾期</el-tag>
            <el-tag v-else-if="isNearDeadline(scope.row.deadline)" type="warning" size="small" class="ml5">临期</el-tag>
          </span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <el-tag :type="taskStatusTag(scope.row.status)">
            {{ taskStatusLabel(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="下发时间" align="center" prop="dispatchTime" width="160" />
      <el-table-column label="审批状态" align="center" width="100" v-if="isUnitLeader">
        <template #default="scope">
          <el-tag v-if="scope.row.approStatus === '1'" type="warning">待审批</el-tag>
          <el-tag v-else-if="scope.row.approStatus === '2'" type="success">已通过</el-tag>
          <el-tag v-else-if="scope.row.approStatus === '3'" type="danger">已驳回</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="确认" align="center" width="100">
        <template #default="scope">
          <el-button link type="primary" icon="Check" @click="handleConfirm(scope.row)" v-if="scope.row.status === '0'" v-hasPermi="['rectification:task:confirm']">确认接收</el-button>
          <el-tag type="success" v-else-if="scope.row.status !== '0'">已确认</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="分办" align="center" width="120" v-if="!isUnitLeader">
        <template #default="scope">
          <el-button link type="primary" icon="User" @click="handleAssign(scope.row)" v-if="scope.row.status === '1'" v-hasPermi="['rectification:task:assign']">分办责任人</el-button>
        </template>
      </el-table-column>
      <el-table-column label="执行操作" align="center" width="200">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleDetail(scope.row)" v-if="!isUnitLeader">详情</el-button>
          <el-button link type="primary" icon="Edit" @click="handleEditPlan(scope.row)" v-if="['1'].includes(scope.row.status)" v-hasPermi="['rectification:plan:edit']">方案</el-button>
          <el-button link type="danger" icon="CircleCheck" @click="handleApplyClosure(scope.row)" v-if="['1','2'].includes(scope.row.status)" v-hasPermi="['rectification:closure:apply']">销号</el-button>
          <el-button link type="warning" icon="Checked" @click="handleGoApproval(scope.row)" v-if="['1','2'].includes(scope.row.status)" v-hasPermi="['rectification:report:approve']">审批</el-button>
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

    <!-- 确认接收 Dialog -->
    <el-dialog title="确认接收任务" v-model="confirmOpen" width="500px" append-to-body>
      <div class="confirm-dialog-body">
        <el-alert
          title="确认接收后将正式开始整改工作，系统开始计时"
          type="warning"
          :closable="false"
          show-icon
          class="mb15"
        />
        <el-descriptions :column="1" border>
          <el-descriptions-item label="任务编号">{{ currentTask.taskNo }}</el-descriptions-item>
          <el-descriptions-item label="关联问题">{{ currentTask.issueTitle }}</el-descriptions-item>
          <el-descriptions-item label="截止日期">{{ currentTask.deadline }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitConfirm" :loading="submitLoading">确认接收</el-button>
          <el-button @click="confirmOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 申请销号 Dialog -->
    <el-dialog title="申请销号" v-model="closureOpen" width="700px" append-to-body>
      <div v-if="closureLoading" style="text-align:center;padding:20px">正在生成整改报告...</div>
      <template v-else>
        <h4 style="margin:0 0 5px">系统自动生成的整改报告预览</h4>
        <div style="max-height:280px;overflow-y:auto;background:#f5f7fa;padding:12px;border-radius:4px;white-space:pre-wrap;font-size:13px;margin-bottom:15px">{{ closureReportContent }}</div>
        <el-form ref="closureFormRef" :model="closureForm" label-width="100px">
          <el-form-item label="销号附言">
            <el-input v-model="closureForm.remark" type="textarea" :rows="2" placeholder="选填，补充说明" maxlength="300" show-word-limit />
          </el-form-item>
          <el-form-item>
            <el-checkbox v-model="closureForm.agreed" style="white-space:normal;line-height:1.6">本人承诺，本次提交的所有整改材料及佐证附件均真实、准确、有效。若存在弄虚作假，愿承担相应责任。</el-checkbox>
          </el-form-item>
        </el-form>
      </template>
      <template #footer>
        <el-button type="primary" @click="submitClosure" :loading="submitLoading" :disabled="!closureForm.agreed">确认销号</el-button>
        <el-button @click="closureOpen = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 申请延期 Dialog -->
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
        <div class="dialog-footer">
          <el-button type="primary" @click="submitExtension" :loading="submitLoading">提交申请</el-button>
          <el-button @click="extensionOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 分办责任人 Dialog -->
    <AssignDialog v-model="assignOpen" :task-id="assignTaskId" @success="onAssignSuccess" />

    <!-- 审批报告 Dialog -->
    <LeaderApproval v-model="approOpen" :report-id="approReportId" :content="approContent" :materials="approMaterials" @success="getList" />
  </div>
</template>

<script setup name="MyTasks">
import { ref, reactive, toRefs, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { listMyTask, confirmTask, getTask } from '@/api/rectification/task'
import { applyClosure } from '@/api/rectification/closure'
import { applyExtension } from '@/api/rectification/plan'
import request from '@/utils/request'
import useUserStore from '@/store/modules/user'
import AssignDialog from './components/AssignDialog.vue'
import LeaderApproval from '../report/LeaderApproval.vue'

const router = useRouter()
const isUnitLeader = computed(() => {
  const perms = useUserStore().permissions
  return perms.includes('rectification:report:approve') && !perms.includes('rectification:plan:edit')
})
const { proxy } = getCurrentInstance()

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
  { label: '待接收', value: '0' },
  { label: '整改中', value: '1' },
  { label: '待审核', value: '2' },
  { label: '已销号', value: '3' },
  { label: '持续整改', value: '4' }
])

const sourceTypeOptions = ref([
  { label: '内源审计', value: 'inner' },
  { label: '外源巡视巡察', value: 'inspection' },
  { label: '外部督查', value: 'supervision' }
])

// 统计卡片
const statCards = computed(() => {
  const all = taskList.value
  return [
    { status: 'all', label: '全部任务', count: total.value, icon: 'List', cardClass: '' },
    { status: '0', label: '待接收', count: all.filter(t => t.status === '0').length, icon: 'Clock', cardClass: 'stat-card-info' },
    { status: '1', label: '整改中', count: all.filter(t => t.status === '1').length, icon: 'Loading', cardClass: 'stat-card-warning' },
    { status: '3', label: '已销号', count: all.filter(t => t.status === '3').length, icon: 'CircleCheck', cardClass: 'stat-card-success' }
  ]
})

// 辅助函数
function taskStatusLabel(val) {
  const item = taskStatusOptions.value.find(d => d.value === val)
  return item ? item.label : val
}
function taskStatusTag(val) {
  const map = { '0': 'info', '1': 'warning', '2': 'primary', '3': 'success', '4': 'danger' }
  return map[val] || ''
}
function sourceTypeLabel(val) {
  const item = sourceTypeOptions.value.find(d => d.value === val)
  return item ? item.label : val
}
function sourceTypeTag(val) {
  const map = { inner: '', inspection: 'warning', supervision: 'danger' }
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

/** 查询我的任务列表 */
function getList() {
  loading.value = true
  listMyTask(queryParams.value).then(response => {
    const tasks = response.rows || []
    total.value = response.total
    if (tasks.length === 0) { taskList.value = []; loading.value = false; return }
    // 加载每个任务的报告审核状态，完成后再显示
    let loaded = 0
    tasks.forEach((t, i) => {
      t.approStatus = null
      request({ url: '/rectification/report/' + t.taskId, method: 'get' }).then(r => {
        const rpt = r.data || {}
        t.approStatus = rpt.unitApproveStatus || (rpt.status === '2' ? '2' : rpt.status === '1' ? '1' : null)
      }).catch(() => {}).finally(() => {
        loaded++
        if (loaded >= tasks.length) { taskList.value = [...tasks]; loading.value = false }
      })
    })
    taskList.value = [...tasks]
  })
}

/** 搜索按钮操作 */
function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
function resetQuery() {
  proxy.resetForm('queryRef')
  handleQuery()
}

/** 多选框选中数据 */
function handleSelectionChange(selection) {
  ids.value = selection.map(item => item.taskId)
}

/** 确认接收 */
const assignTaskId = ref(null)
const assignOpen = ref(false)
const approOpen = ref(false)
const approReportId = ref(0)
const approContent = ref('')
const approMaterials = ref([])
function handleAssign(row) { assignTaskId.value = row.taskId; assignOpen.value = true }
function onAssignSuccess() { assignOpen.value = false; getList() }

function handleConfirm(row) {
  currentTask.value = row
  confirmOpen.value = true
}

function submitConfirm() {
  submitLoading.value = true
  confirmTask(currentTask.value.taskId).then(() => {
    currentTask.value.status = '1'
    proxy.$modal.msgSuccess('任务已确认接收')
    confirmOpen.value = false
    getList()
  }).finally(() => {
    submitLoading.value = false
  })
}

/** 编制方案 - 跳转任务详情页 */
function handleGoApproval(row) {
  getTask(row.taskId).then(res => {
    const t = res.data || {}
    let iid = 0
    try { iid = JSON.parse((t.issueIds||'[0]').replace(/'/g,'"'))[0] } catch(e) {}
    const rptReq = request({ url: '/rectification/report/' + row.taskId, method: 'get' })
    const matReq = request({ url: '/rectification/material/list/' + iid, method: 'get' })
    Promise.all([rptReq, matReq]).then(([rptRes, matRes]) => {
      const rpt = rptRes.data || {}
      approReportId.value = rpt.reportId || 0
      approContent.value = rpt.reportContent || ''
      approMaterials.value = matRes.rows || []
      approOpen.value = true
    })
  })
}

function handleDetail(row) {
  router.push('/rectification/task-page/detail/' + row.taskId)
}
function handleGenerateReport(row) {
  proxy.$modal.confirm('系统将根据方案内容和佐证材料自动生成整改报告并提交审批，确认？').then(() => {
    getTask(row.taskId).then(res => {
      const t = res.data || {}
      let issueId = 0
      try { issueId = JSON.parse((t.issueIds||'[0]').replace(/'/g,'"'))[0] } catch(e) {}
      const planReq = request({ url: '/rectification/plan/' + row.taskId, method: 'get' })
      const matReq = request({ url: '/rectification/material/list/' + issueId, method: 'get' })
      Promise.all([planReq, matReq]).then(([planRes, matRes]) => {
        const plan = planRes.data || {}
        const mats = matRes.rows || []
        const content = '【整改报告】\n\n一、整改方案\n' + (plan.planContent || '待填报') + '\n\n二、佐证材料\n' + (mats.map(m => m.fileName).join('、') || '无') + '\n\n三、整改成效\n已按方案完成整改措施，相关佐证材料已上传。'
        request({ url: '/rectification/report', method: 'post', data: { taskId: row.taskId, reportContent: content } }).then(() => {
          proxy.$modal.msgSuccess('整改报告已生成，请前往任务详情审阅后提交审批')
          getList()
        })
      })
    })
  }).catch(() => {})
}

function handleEditPlan(row) {
  router.push('/rectification/task-page/detail/' + row.taskId + '?tab=plan')
}

/** 上传材料 - 跳转任务详情页 */
function handleUploadMaterial(row) {
  router.push('/rectification/task-page/detail/' + row.taskId + '?tab=material')
}

/** 提交报告 - 跳转任务详情页 */
function handleSubmitReport(row) {
  router.push('/rectification/task-page/detail/' + row.taskId + '?tab=report')
}

/** 申请销号 */
function handleApplyClosure(row) {
  currentTask.value = row
  closureForm.taskId = row.taskId
  try { closureForm.issueId = JSON.parse((row.issueIds || '[0]').replace(/'/g,'"'))[0] } catch(e) { closureForm.issueId = 0 }
  closureForm.remark = ''
  closureForm.agreed = false
  closureLoading.value = true
  closureReportContent.value = ''
  closureOpen.value = true
  // 加载方案 + 材料 + 报告，组成完整报告预览
  const pid = request({ url: '/rectification/plan/' + row.taskId, method: 'get' })
  const rid = request({ url: '/rectification/report/' + row.taskId, method: 'get' })
  const mid = request({ url: '/rectification/material/list/' + closureForm.issueId, method: 'get' })
  Promise.all([pid, rid, mid]).then(([pRes, rRes, mRes]) => {
    const plan = pRes.data || {}
    const rpt = rRes.data || {}
    const mats = mRes.rows || []
    closureReportContent.value = '【整改报告】\n\n一、整改方案\n' + (plan.planContent || '待填报') +
      '\n\n二、佐证材料\n' + (mats.map(m => m.fileName).join('、') || '无') +
      '\n\n三、整改报告\n' + (rpt.reportContent || '待填报') +
      '\n\n四、整改成效\n已按方案完成整改措施，相关佐证材料已上传。'
  }).catch(() => { closureReportContent.value = '加载失败，请重试' })
  .finally(() => { closureLoading.value = false })
}

function submitClosure() {
  if (!closureForm.agreed) { proxy.$modal.msgWarning('请勾选真实性承诺'); return }
  submitLoading.value = true
  applyClosure({
    taskId: closureForm.taskId,
    issueId: closureForm.issueId,
    applyContent: (closureForm.remark || '申请销号') + '\n\n---\n' + closureReportContent.value
  }).then(() => {
    proxy.$modal.msgSuccess('销号申请已提交')
    closureOpen.value = false
    getList()
  }).finally(() => {
    submitLoading.value = false
  })
}

/** 申请延期 */
function handleApplyExtension(row) {
  currentTask.value = row
  extensionForm.taskId = row.taskId
  extensionForm.newDeadline = undefined
  extensionForm.reason = undefined
  if (proxy.$refs['extensionFormRef']) {
    proxy.$refs['extensionFormRef'].resetFields()
  }
  extensionOpen.value = true
}

function submitExtension() {
  proxy.$refs['extensionFormRef'].validate(valid => {
    if (valid) {
      submitLoading.value = true
      applyExtension(extensionForm).then(() => {
        proxy.$modal.msgSuccess('延期申请已提交')
        extensionOpen.value = false
        getList()
      }).finally(() => {
        submitLoading.value = false
      })
    }
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
.stat-card-info .stat-card-right { color: #409eff; }
.stat-card-warning .stat-card-right { color: #e6a23c; }
.stat-card-success .stat-card-right { color: #67c23a; }

.mb8 {
  margin-bottom: 8px;
}
.mb15 {
  margin-bottom: 15px;
}
.ml5 {
  margin-left: 5px;
}
.confirm-dialog-body {
  padding: 10px 0;
}
</style>
