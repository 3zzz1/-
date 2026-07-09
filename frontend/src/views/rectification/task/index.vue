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
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- Action Bar -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleDispatch"
          v-hasPermi="['rectification:task:add']"
        >下发任务</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Operation"
          :disabled="!ids.length"
          @click="handleBatchDispatch"
          v-hasPermi="['rectification:task:add']"
        >批量下发</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- Table -->
    <el-table v-loading="loading" :data="taskList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="任务编号" align="center" prop="taskNo" width="140" />
      <el-table-column label="关联问题" align="center" prop="issueTitle" :show-overflow-tooltip="true" min-width="180" />
      <el-table-column label="整改单位" align="center" width="150">
        <template #default="scope">
          {{ deptName(scope.row.rectDeptId) }}
        </template>
      </el-table-column>
      <el-table-column label="联络人" align="center" prop="contactPerson" width="100" />
      <el-table-column label="截止日期" align="center" prop="deadline" width="110" />
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <el-tag :type="taskStatusTag(scope.row.status)">
            {{ taskStatusLabel(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="下发时间" align="center" prop="dispatchTime" width="160" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="220">
        <template #default="scope">
          <el-button
            link
            type="primary"
            icon="View"
            @click="handleDetail(scope.row)"
            v-hasPermi="['rectification:task:query']"
          >详情</el-button>
          <el-button
            link
            type="primary"
            icon="Check"
            @click="handleConfirm(scope.row)"
            v-if="scope.row.status === '0' && !isAdmin"
          >确认接收</el-button>
          <el-button
            link
            type="primary"
            icon="Document"
            @click="handleGenerateNotice(scope.row)"
            v-hasPermi="['rectification:task:notice']"
          >通知书</el-button>
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

    <!-- Dispatch Dialog (下发任务) -->
    <el-dialog :title="dispatchTitle" v-model="dispatchOpen" width="900px" append-to-body>
      <div class="dispatch-container">
        <!-- Step 1: Select issues to dispatch -->
        <div class="dispatch-section">
          <h4 class="dispatch-section-title">选择待下发问题</h4>
          <el-input v-model="issueSearch" placeholder="搜索问题编号或标题" clearable style="width: 280px; margin-bottom: 8px" />
          <el-table
            ref="issueSelectTableRef"
            v-loading="issueLoading"
            :data="filteredIssueList"
            @selection-change="handleDispatchIssueSelect"
            max-height="300"
            stripe
          >
            <el-table-column type="selection" width="55" align="center" />
            <el-table-column label="问题编号" prop="issueNo" width="140" />
            <el-table-column label="问题标题" prop="issueTitle" :show-overflow-tooltip="true" min-width="200" />
            <el-table-column label="来源类型" prop="sourceType" width="100">
              <template #default="scope">
                <el-tag :type="sourceTypeTag(scope.row.sourceType)" size="small">{{ sourceTypeLabel(scope.row.sourceType) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="问题分类" width="100">
                <template #default="scope">
                  <el-tag :type="categoryTag(scope.row.issueCategory)" size="small">{{ categoryLabel(scope.row.issueCategory) }}</el-tag>
                </template>
              </el-table-column>
            <el-table-column label="责任单位" width="140">
              <template #default="scope">{{ deptName(scope.row.responsibleDeptId) }}</template>
            </el-table-column>
          </el-table>
          <el-pagination
            v-if="issueTotal > 0"
            v-model:current-page="issuePage.pageNum"
            v-model:page-size="issuePage.pageSize"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            :total="issueTotal"
            @change="loadDispatchIssues"
            small
          />
        </div>

        <!-- Step 2: Fill task info -->
        <div class="dispatch-section">
          <h4 class="dispatch-section-title">任务信息</h4>
          <el-form ref="dispatchFormRef" :model="dispatchForm" :rules="dispatchRules" label-width="100px">
            <el-row>
              <el-col :span="12">
                <el-form-item label="整改单位" prop="rectDeptId">
                  <el-select v-model="dispatchForm.rectDeptId" placeholder="请选择整改单位" style="width: 100%">
                    <el-option v-for="d in deptList" :key="d.deptId" :label="d.deptName" :value="d.deptId" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="联络人" prop="contactPerson">
                  <el-select v-model="dispatchForm.contactPerson" filterable allow-create clearable placeholder="先选整改单位，再选联络人" style="width: 100%" :disabled="!dispatchForm.rectDeptId" @focus="loadContactUsers" @change="onContactChange">
                    <el-option v-for="u in contactUserList" :key="u.userId" :label="u.nickName || u.userName" :value="u.nickName || u.userName" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row>
              <el-col :span="12">
                <el-form-item label="联系电话" prop="contactPhone">
                  <el-input v-model="dispatchForm.contactPhone" placeholder="请输入联系电话" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="截止日期" prop="deadline">
                  <el-date-picker
                    v-model="dispatchForm.deadline"
                    type="date"
                    placeholder="请选择截止日期"
                    value-format="YYYY-MM-DD"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="整改要求" prop="requirement">
              <el-input v-model="dispatchForm.requirement" type="textarea" :rows="3" placeholder="请输入整改要求（可选）" />
            </el-form-item>
          </el-form>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitDispatch" :loading="submitLoading">确 定</el-button>
          <el-button @click="dispatchOpen = false">取 消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Task">
import { ref, reactive, toRefs, computed, getCurrentInstance } from 'vue'
import { useRouter } from 'vue-router'
import { listTask, getTask, addTask, batchDispatchTask, generateNotice, confirmTask } from '@/api/rectification/task'
import { listIssue } from '@/api/rectification/issue'
import request from '@/utils/request'

import useUserStore from '@/store/modules/user'
const router = useRouter()
const isAdmin = computed(() => useUserStore().roles.includes('admin'))
const { proxy } = getCurrentInstance()

const taskList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const total = ref(0)
const dispatchOpen = ref(false)
const dispatchTitle = ref('')
const dispatchIssueList = ref([])
const issueTotal = ref(0)
const issuePage = reactive({ pageNum: 1, pageSize: 20 })
const issueSearch = ref('')
const deptList = ref([])
const contactUserList = ref([])

function onContactChange(val) {
  const u = contactUserList.value.find(u => (u.nickName || u.userName) === val)
  if (u && u.phonenumber) dispatchForm.contactPhone = u.phonenumber
}

function loadContactUsers() {
  if (!dispatchForm.rectDeptId) return
  request({ url: '/system/user/list', method: 'get', params: { deptId: dispatchForm.rectDeptId, pageSize: 100 } })
    .then(res => { contactUserList.value = res.rows || [] })
    .catch(() => { contactUserList.value = [] })
}
request({ url: '/rectification/issue/depts', method: 'get' }).then(res => {
  const arr = Array.isArray(res.data) ? res.data : []
  if (arr.length === 0) arr.push(
    { deptId: 200, deptName: '审计处' }, { deptId: 201, deptName: '经济管理学院' },
    { deptId: 202, deptName: '信息工程学院' }, { deptId: 203, deptName: '后勤保障处' },
    { deptId: 204, deptName: '基建处' }, { deptId: 205, deptName: '校办企业集团' }
  )
  deptList.value = arr
}).catch(() => {
  deptList.value = [
    { deptId: 200, deptName: '审计处' }, { deptId: 201, deptName: '经济管理学院' },
    { deptId: 202, deptName: '信息工程学院' }, { deptId: 203, deptName: '后勤保障处' },
    { deptId: 204, deptName: '基建处' }, { deptId: 205, deptName: '校办企业集团' }
  ]
})
function deptName(id) {
  const d = deptList.value.find(d => d.deptId == id)
  return d ? d.deptName : id
}

const filteredIssueList = computed(() => {
  if (!issueSearch.value) return dispatchIssueList.value
  const kw = issueSearch.value.toLowerCase()
  return dispatchIssueList.value.filter(i =>
    (i.issueNo && i.issueNo.toLowerCase().includes(kw)) ||
    (i.issueTitle && i.issueTitle.toLowerCase().includes(kw))
  )
})
const issueLoading = ref(false)
const selectedIssues = ref([])
const submitLoading = ref(false)
const isBatchDispatch = ref(false)

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    taskNo: undefined,
    status: undefined
  }
})

const { queryParams } = toRefs(data)

const dispatchForm = reactive({
  issueIds: [],
  rectDeptId: undefined,
  contactPerson: undefined,
  contactPhone: undefined,
  deadline: undefined,
  requirement: undefined
})

const dispatchRules = reactive({
  rectDeptId: [{ required: true, message: '整改单位不能为空', trigger: 'blur' }],
  contactPerson: [{ required: true, message: '联络人不能为空', trigger: 'blur' }],
  contactPhone: [{ required: true, message: '联系电话不能为空', trigger: 'blur' }],
  deadline: [{ required: true, message: '截止日期不能为空', trigger: 'change' }]
})

// 状态下拉
const taskStatusOptions = ref([
  { label: '待接收', value: '0' },
  { label: '整改中', value: '1' },
  { label: '待审核', value: '2' },
  { label: '已销号', value: '3' },
  { label: '已完成', value: '4' }
])

const sourceTypeOptions = ref([
  { label: '内源审计', value: '1' },
  { label: '外源巡视巡察', value: '2' },
  { label: '外部督查', value: '3' }
])

const categoryOptions = [
  { label: '资金类', value: 'FUND' },
  { label: '资产类', value: 'ASSET' },
  { label: '采购类', value: 'PURCHASE' },
  { label: '人事类', value: 'HR' },
  { label: '基建类', value: 'CONSTRUCTION' },
  { label: '其他', value: 'OTHER' }
]

function taskStatusLabel(val) {
  const item = taskStatusOptions.value.find(d => d.value === val)
  return item ? item.label : val
}

function taskStatusTag(val) {
  const map = { '0': 'info', '1': 'warning', '2': 'primary', '3': '', '4': 'success' }
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

function categoryTag(val) {
  const map = { FUND: '', ASSET: 'warning', PURCHASE: 'primary', HR: 'danger', CONSTRUCTION: 'info', OTHER: '' }
  return map[val] || ''
}

/** 查询任务列表 */
function getList() {
  loading.value = true
  listTask(queryParams.value).then(response => {
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

/** 加载待下发问题列表（分页） */
function loadDispatchIssues() {
  issueLoading.value = true
  listIssue({ status: '0', pageNum: issuePage.pageNum, pageSize: issuePage.pageSize }).then(res => {
    dispatchIssueList.value = res.rows || []
    issueTotal.value = res.total || 0
    issueLoading.value = false
  })
}

/** 下发任务按钮 */
function handleDispatch() {
  isBatchDispatch.value = false
  dispatchTitle.value = '下发整改任务'
  resetDispatchForm()
  dispatchOpen.value = true
  loadDispatchIssues()
}

/** 批量下发 */
function handleBatchDispatch() {
  isBatchDispatch.value = true
  dispatchTitle.value = '批量下发整改任务'
  resetDispatchForm()
  dispatchOpen.value = true
  loadDispatchIssues()
}

/** 选择下发的问题 */
function handleDispatchIssueSelect(selection) {
  dispatchForm.issueIds = selection.map(item => item.issueId)
}

/** 重置下发表单 */
function resetDispatchForm() {
  dispatchForm.issueIds = []
  dispatchForm.rectDeptId = undefined
  dispatchForm.contactPerson = undefined
  dispatchForm.contactPhone = undefined
  dispatchForm.deadline = undefined
  dispatchForm.requirement = undefined
  selectedIssues.value = []
  if (proxy.$refs['dispatchFormRef']) {
    proxy.$refs['dispatchFormRef'].resetFields()
  }
}

/** 提交下发 */
function submitDispatch() {
  proxy.$refs['dispatchFormRef'].validate(valid => {
    if (valid) {
      if (!dispatchForm.issueIds || dispatchForm.issueIds.length === 0) {
        proxy.$modal.msgWarning('请至少选择一个待下发的问题')
        return
      }
      submitLoading.value = true
      const postData = { ...dispatchForm }
      if (isBatchDispatch.value) {
        batchDispatchTask(postData).then(() => {
          proxy.$modal.msgSuccess('批量下发成功')
          dispatchOpen.value = false
          getList()
        }).finally(() => {
          submitLoading.value = false
        })
      } else {
        // 单个下发时，取第一个问题
        postData.issueId = dispatchForm.issueIds[0]
        addTask(postData).then(() => {
          proxy.$modal.msgSuccess('任务下发成功')
          dispatchOpen.value = false
          getList()
        }).finally(() => {
          submitLoading.value = false
        })
      }
    }
  })
}

/** 查看详情 */
function handleDetail(row) {
  router.push('/rectification/task-page/detail/' + row.taskId)
}

/** 确认任务 */
function handleConfirm(row) {
  proxy.$modal.confirm('是否确认接收该整改任务？').then(function () {
    return confirmTask(row.taskId)
  }).then(() => {
    proxy.$modal.msgSuccess('确认成功')
    getList()
  }).catch(() => {})
}

/** 生成通知书 */
function handleGenerateNotice(row) {
  proxy.$modal.confirm('确认生成整改通知书？').then(function () {
    proxy.download('rectification/task/notice/' + row.taskId, {}, `notice_${row.taskNo}_${new Date().getTime()}.docx`)
  }).catch(() => {})
}

getList()
</script>

<style scoped>
.dispatch-container {
  padding: 0 10px;
}
.dispatch-section {
  margin-bottom: 20px;
}
.dispatch-section-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 12px 0;
  padding-left: 8px;
  border-left: 3px solid #409eff;
}
</style>
