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
      <el-table-column label="关联问题" align="center" prop="issueTitle" :show-overflow-tooltip="true" min-width="180" />
      <el-table-column label="问题分类" align="center" prop="issueCategory" width="90" />
      <el-table-column label="来源类型" align="center" prop="sourceType" width="100">
        <template #default="scope">
          <el-tag :type="sourceTypeTag(scope.row.sourceType)" size="small">
            {{ sourceTypeLabel(scope.row.sourceType) }}
          </el-tag>
        </template>
      </el-table-column>
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
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="280">
        <template #default="scope">
          <el-button
            link
            type="primary"
            icon="Check"
            @click="handleConfirm(scope.row)"
            v-if="scope.row.status === '0'"
            v-hasPermi="['rectification:task:confirm']"
          >确认接收</el-button>
          <el-button
            link
            type="primary"
            icon="Edit"
            @click="handleEditPlan(scope.row)"
            v-if="['0', '1'].includes(scope.row.status)"
            v-hasPermi="['rectification:plan:edit']"
          >编制方案</el-button>
          <el-button
            link
            type="primary"
            icon="Upload"
            @click="handleUploadMaterial(scope.row)"
            v-if="['1'].includes(scope.row.status)"
            v-hasPermi="['rectification:material:upload']"
          >上传材料</el-button>
          <el-button
            link
            type="primary"
            icon="Document"
            @click="handleSubmitReport(scope.row)"
            v-if="['1'].includes(scope.row.status)"
            v-hasPermi="['rectification:report:submit']"
          >提交报告</el-button>
          <el-button
            link
            type="success"
            icon="CircleCheck"
            @click="handleApplyClosure(scope.row)"
            v-if="['1', '2'].includes(scope.row.status)"
            v-hasPermi="['rectification:closure:apply']"
          >申请销号</el-button>
          <el-button
            link
            type="warning"
            icon="Timer"
            @click="handleApplyExtension(scope.row)"
            v-if="['1'].includes(scope.row.status)"
            v-hasPermi="['rectification:plan:extension']"
          >申请延期</el-button>
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
    <el-dialog title="申请销号" v-model="closureOpen" width="600px" append-to-body>
      <el-form ref="closureFormRef" :model="closureForm" :rules="closureRules" label-width="100px">
        <el-form-item label="任务编号">
          <span>{{ currentTask.taskNo }}</span>
        </el-form-item>
        <el-form-item label="整改完成情况" prop="completionDesc">
          <el-input v-model="closureForm.completionDesc" type="textarea" :rows="4" placeholder="请描述整改完成情况" />
        </el-form-item>
        <el-form-item label="佐证说明" prop="evidenceDesc">
          <el-input v-model="closureForm.evidenceDesc" type="textarea" :rows="3" placeholder="请说明已提交的佐证材料" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitClosure" :loading="submitLoading">提交申请</el-button>
          <el-button @click="closureOpen = false">取 消</el-button>
        </div>
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
  </div>
</template>

<script setup name="MyTasks">
import { ref, reactive, toRefs, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { listMyTask, confirmTask } from '@/api/rectification/task'
import { applyClosure } from '@/api/rectification/closure'
import { applyExtension } from '@/api/rectification/plan'

const router = useRouter()
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
  completionDesc: undefined,
  evidenceDesc: undefined
})

const closureRules = reactive({
  completionDesc: [{ required: true, message: '整改完成情况不能为空', trigger: 'blur' }],
  evidenceDesc: [{ required: true, message: '佐证说明不能为空', trigger: 'blur' }]
})

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
    taskList.value = response.rows
    total.value = response.total
    loading.value = false
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

/** 编制方案 - 跳转任务详情页 */
function handleEditPlan(row) {
  router.push('/rectification/task/detail/' + row.taskId + '?tab=plan')
}

/** 上传材料 - 跳转任务详情页 */
function handleUploadMaterial(row) {
  router.push('/rectification/task/detail/' + row.taskId + '?tab=material')
}

/** 提交报告 - 跳转任务详情页 */
function handleSubmitReport(row) {
  router.push('/rectification/task/detail/' + row.taskId + '?tab=report')
}

/** 申请销号 */
function handleApplyClosure(row) {
  currentTask.value = row
  closureForm.taskId = row.taskId
  closureForm.completionDesc = undefined
  closureForm.evidenceDesc = undefined
  if (proxy.$refs['closureFormRef']) {
    proxy.$refs['closureFormRef'].resetFields()
  }
  closureOpen.value = true
}

function submitClosure() {
  proxy.$refs['closureFormRef'].validate(valid => {
    if (valid) {
      submitLoading.value = true
      applyClosure(closureForm).then(() => {
        proxy.$modal.msgSuccess('销号申请已提交')
        closureOpen.value = false
        getList()
      }).finally(() => {
        submitLoading.value = false
      })
    }
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
