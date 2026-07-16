<template>
  <div class="app-container task-page">
    <!-- Search Form -->
    <el-form class="desktop-task-search" :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
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

    <MobileFilterBar
      v-model="queryParams.taskNo"
      placeholder="搜索任务编号"
      :active-count="queryParams.status ? 1 : 0"
      @search="handleQuery"
      @reset="resetQuery"
    >
      <el-form label-position="top">
        <el-form-item label="任务状态">
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable>
            <el-option v-for="dict in taskStatusOptions" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>
      </el-form>
    </MobileFilterBar>

    <!-- Action Bar -->
    <el-row :gutter="10" class="mb8 task-toolbar">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleDispatch"
          v-hasPermi="['rectification:task:dispatch']"
        >下发任务</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="Operation"
          @click="handleBatchDispatch"
          v-hasPermi="['rectification:task:batchDispatch']"
        >批量下发</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- Table -->
    <el-table class="task-table" v-loading="loading" :data="taskList" @selection-change="handleSelectionChange">
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
            v-if="scope.row.status === '0' && isLiaison"
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

    <div v-loading="loading" class="task-mobile-list">
      <el-empty v-if="!loading && taskList.length === 0" description="暂无任务数据" />
      <section v-for="item in taskList" :key="item.taskId" class="task-card">
        <div class="task-card-header">
          <div class="task-card-title">
            <strong>{{ item.issueTitle || '整改任务' }}</strong>
            <span>{{ item.taskNo || '-' }}</span>
          </div>
          <el-tag :type="taskStatusTag(item.status)">{{ taskStatusLabel(item.status) }}</el-tag>
        </div>
        <div class="task-card-meta">
          <span><em>整改单位</em><b>{{ deptName(item.rectDeptId) || '-' }}</b></span>
          <span><em>联络人</em><b>{{ item.contactPerson || '-' }}</b></span>
          <span><em>截止日期</em><b>{{ item.deadline || '-' }}</b></span>
          <span><em>下发时间</em><b>{{ item.dispatchTime || '-' }}</b></span>
        </div>
        <div class="task-card-actions">
          <el-button type="primary" plain icon="View" @click="handleDetail(item)" v-hasPermi="['rectification:task:query']">详情</el-button>
          <el-button v-if="item.status === '0' && isLiaison" type="success" plain icon="Check" @click="handleConfirm(item)">接收</el-button>
          <el-button type="warning" plain icon="Document" @click="handleGenerateNotice(item)" v-hasPermi="['rectification:task:notice']">通知书</el-button>
        </div>
      </section>
    </div>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- Dispatch Dialog (下发任务) -->
    <el-dialog class="mobile-form-dialog task-dispatch-dialog" :title="dispatchTitle" v-model="dispatchOpen" width="900px" append-to-body>
      <div class="dispatch-container">
        <!-- Step 1: Select issues to dispatch -->
        <div class="dispatch-section">
          <h4 class="dispatch-section-title">选择待下发问题</h4>
          <el-input class="dispatch-issue-search" v-model="issueSearch" placeholder="搜索问题编号或标题" clearable />
          <el-table
            class="dispatch-issue-table"
            ref="issueSelectTableRef"
            v-loading="issueLoading"
            :data="filteredIssueList"
            row-key="issueId"
            @selection-change="handleDispatchIssueSelect"
            max-height="300"
            stripe
          >
            <el-table-column type="selection" width="55" align="center" :reserve-selection="true" />
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

          <div v-loading="issueLoading" class="dispatch-issue-mobile">
            <div class="dispatch-mobile-toolbar">
              <el-checkbox
                v-if="isBatchDispatch"
                :model-value="allVisibleIssuesSelected"
                :indeterminate="someVisibleIssuesSelected"
                @change="toggleVisibleIssues"
              >全选当前页</el-checkbox>
              <span v-else>请选择 1 个待下发问题</span>
              <el-button v-if="dispatchForm.issueIds.length" link type="primary" @click="clearIssueSelection">清空</el-button>
            </div>

            <el-empty v-if="!issueLoading && filteredIssueList.length === 0" description="暂无待下发问题" />
            <section
              v-for="item in filteredIssueList"
              :key="item.issueId"
              class="dispatch-issue-card"
              :class="{ selected: isIssueSelected(item.issueId) }"
              @click="toggleMobileIssue(item, !isIssueSelected(item.issueId))"
            >
              <div class="dispatch-issue-card-head">
                <el-checkbox
                  :model-value="isIssueSelected(item.issueId)"
                  @click.stop
                  @change="checked => toggleMobileIssue(item, checked)"
                />
                <div>
                  <strong>{{ item.issueTitle || '未命名问题' }}</strong>
                  <span>{{ item.issueNo || '-' }}</span>
                </div>
              </div>
              <div class="dispatch-issue-tags">
                <el-tag :type="sourceTypeTag(item.sourceType)" size="small">{{ sourceTypeLabel(item.sourceType) }}</el-tag>
                <el-tag :type="categoryTag(item.issueCategory)" size="small">{{ categoryLabel(item.issueCategory) }}</el-tag>
              </div>
              <div class="dispatch-issue-meta">
                <span><em>责任单位</em><b>{{ deptName(item.responsibleDeptId) || '-' }}</b></span>
                <span><em>责任人</em><b>{{ item.responsiblePerson || '-' }}</b></span>
                <span><em>截止日期</em><b>{{ item.deadline || '-' }}</b></span>
              </div>
            </section>
          </div>

          <el-pagination
            class="dispatch-issue-pagination"
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
          <el-form class="mobile-dialog-form" ref="dispatchFormRef" :model="dispatchForm" :rules="dispatchRules" label-width="100px">
            <el-row>
              <el-col :span="12">
                <el-form-item label="整改单位" prop="rectDeptId">
                  <el-select v-model="dispatchForm.rectDeptId" placeholder="请选择整改单位" style="width: 100%" @change="handleRectDeptChange">
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
          <span class="dispatch-selected-count">已选择 {{ dispatchForm.issueIds.length }} 项</span>
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
import MobileFilterBar from '@/components/MobileFilterBar/index.vue'
import { saveAs } from 'file-saver'

import useUserStore from '@/store/modules/user'
const router = useRouter()
const isLiaison = computed(() => useUserStore().roles.includes('audited_unit_liaison'))
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
  request({ url: '/rectification/task/liaisons', method: 'get', params: { deptId: dispatchForm.rectDeptId } })
    .then(res => { contactUserList.value = res.data || [] })
    .catch(() => { contactUserList.value = [] })
}

function handleRectDeptChange() {
  dispatchForm.contactPerson = undefined
  dispatchForm.contactPhone = undefined
  contactUserList.value = []
  loadContactUsers()
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

const visibleIssueIds = computed(() => filteredIssueList.value.map(item => String(item.issueId)))
const selectedIssueIds = computed(() => dispatchForm.issueIds.map(id => String(id)))
const allVisibleIssuesSelected = computed(() => {
  return visibleIssueIds.value.length > 0 && visibleIssueIds.value.every(id => selectedIssueIds.value.includes(id))
})
const someVisibleIssuesSelected = computed(() => {
  const selectedCount = visibleIssueIds.value.filter(id => selectedIssueIds.value.includes(id)).length
  return selectedCount > 0 && selectedCount < visibleIssueIds.value.length
})

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
  if (window.matchMedia('(max-width: 768px)').matches) return
  dispatchForm.issueIds = selection.map(item => item.issueId)
}

function isIssueSelected(issueId) {
  return selectedIssueIds.value.includes(String(issueId))
}

function toggleMobileIssue(item, checked) {
  const issueId = item.issueId
  if (!isBatchDispatch.value) {
    dispatchForm.issueIds = checked ? [issueId] : []
    return
  }
  if (checked && !isIssueSelected(issueId)) {
    dispatchForm.issueIds.push(issueId)
  } else if (!checked) {
    dispatchForm.issueIds = dispatchForm.issueIds.filter(id => String(id) !== String(issueId))
  }
}

function toggleVisibleIssues(checked) {
  const visibleSet = new Set(visibleIssueIds.value)
  const retained = dispatchForm.issueIds.filter(id => !visibleSet.has(String(id)))
  dispatchForm.issueIds = checked
    ? [...retained, ...filteredIssueList.value.map(item => item.issueId)]
    : retained
}

function clearIssueSelection() {
  dispatchForm.issueIds = []
  proxy.$refs.issueSelectTableRef?.clearSelection()
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
      const baseData = {
        rectDeptId: dispatchForm.rectDeptId,
        contactPerson: dispatchForm.contactPerson,
        contactPhone: dispatchForm.contactPhone,
        deadline: dispatchForm.deadline,
        taskRequirement: dispatchForm.requirement
      }
      if (isBatchDispatch.value) {
        const postData = dispatchForm.issueIds.map(issueId => ({
          ...baseData,
          issueIds: [issueId]
        }))
        batchDispatchTask(postData).then(() => {
          proxy.$modal.msgSuccess('批量下发成功')
          dispatchOpen.value = false
          getList()
        }).finally(() => {
          submitLoading.value = false
        })
      } else {
        const postData = {
          ...baseData,
          issueIds: [dispatchForm.issueIds[0]]
        }
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
    return generateNotice(row.taskId)
  }).then(async response => {
    const blob = response && response.data
    if (!blob) {
      throw new Error('通知书内容为空')
    }
    if (blob.type && blob.type.includes('application/json')) {
      const result = JSON.parse(await blob.text())
      throw new Error(result.msg || '通知书生成失败')
    }
    const wordBlob = new Blob([blob], {
      type: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
    })
    saveAs(wordBlob, `整改通知书_${row.taskNo || row.taskId}.docx`)
    proxy.$modal.msgSuccess('通知书已下载')
  }).catch(error => {
    if (error && error.message && error.message !== 'cancel') {
      proxy.$modal.msgError(error.message)
    }
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

.task-mobile-list {
  display: none;
}

.task-card {
  padding: 14px;
  margin-bottom: 12px;
  border: 1px solid #e3eaf4;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 8px 22px rgba(36, 52, 75, 0.06);
}

.task-card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
}

.task-card-title {
  min-width: 0;
}

.task-card-title strong {
  display: block;
  color: #1f2f46;
  font-size: 15px;
  line-height: 1.45;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.task-card-title span {
  display: block;
  margin-top: 4px;
  color: #7a8798;
  font-size: 12px;
}

.task-card-meta {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px 12px;
  padding: 12px 0;
}

.task-card-meta span {
  min-width: 0;
}

.task-card-meta em {
  display: block;
  color: #8a96a8;
  font-size: 12px;
  line-height: 1.4;
  font-style: normal;
}

.task-card-meta b {
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

.task-card-actions {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
  padding-top: 10px;
  border-top: 1px solid #eef2f7;
}

.task-card-actions .el-button {
  width: 100%;
  margin-left: 0;
}

.dispatch-issue-search {
  width: 280px;
  margin-bottom: 8px;
}

.dispatch-issue-mobile {
  display: none;
}

.dispatch-selected-count {
  margin-right: auto;
  color: #52657d;
  font-size: 13px;
}

@media (max-width: 768px) {
  .task-page {
    padding: 12px;
    background: #f5f7fb;
  }

  .desktop-task-search {
    display: none;
  }

  .task-toolbar {
    display: flex;
    gap: 8px;
    margin-bottom: 12px;
  }

  .task-toolbar :deep(.el-col) {
    flex: 1;
    max-width: none;
    width: auto;
  }

  .task-toolbar :deep(.el-button) {
    width: 100%;
    margin-left: 0;
    padding-left: 8px;
    padding-right: 8px;
  }

  .task-toolbar :deep(.right-toolbar) {
    display: none;
  }

  .task-table {
    display: none;
  }

  .task-mobile-list {
    display: block;
  }

  .dispatch-container {
    padding: 0;
  }

  .dispatch-section {
    overflow: visible;
  }

  .dispatch-issue-search {
    width: 100%;
    margin-bottom: 10px;
  }

  .dispatch-issue-table {
    display: none;
  }

  .dispatch-issue-mobile {
    display: block;
    min-height: 120px;
  }

  .dispatch-mobile-toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    min-height: 36px;
    padding: 0 2px 8px;
    color: #66768c;
    font-size: 13px;
  }

  .dispatch-issue-card {
    padding: 12px;
    margin-bottom: 10px;
    border: 1px solid #dfe7f1;
    border-radius: 8px;
    background: #fff;
    transition: border-color 0.15s ease, background-color 0.15s ease;
  }

  .dispatch-issue-card.selected {
    border-color: #409eff;
    background: #f4f8ff;
  }

  .dispatch-issue-card-head {
    display: flex;
    align-items: flex-start;
    gap: 10px;
  }

  .dispatch-issue-card-head > div {
    min-width: 0;
    flex: 1;
  }

  .dispatch-issue-card-head strong {
    display: block;
    color: #1f2f46;
    font-size: 14px;
    line-height: 1.45;
    overflow-wrap: anywhere;
  }

  .dispatch-issue-card-head span {
    display: block;
    margin-top: 3px;
    color: #8390a2;
    font-size: 12px;
  }

  .dispatch-issue-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
    padding: 10px 0;
  }

  .dispatch-issue-meta {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 8px 12px;
    padding-top: 9px;
    border-top: 1px solid #edf1f6;
  }

  .dispatch-issue-meta span:last-child {
    grid-column: 1 / -1;
  }

  .dispatch-issue-meta em,
  .dispatch-issue-meta b {
    display: block;
    min-width: 0;
    font-style: normal;
    line-height: 1.4;
  }

  .dispatch-issue-meta em {
    color: #8a96a8;
    font-size: 11px;
  }

  .dispatch-issue-meta b {
    margin-top: 2px;
    color: #34465e;
    font-size: 12px;
    font-weight: 500;
    overflow-wrap: anywhere;
  }

  .dispatch-issue-pagination {
    justify-content: center;
    width: 100%;
    margin-top: 10px;
    white-space: normal;
  }

  .dispatch-issue-pagination :deep(.el-pagination__total),
  .dispatch-issue-pagination :deep(.el-pagination__sizes) {
    display: none;
  }

  .task-dispatch-dialog :deep(.el-dialog__footer) {
    position: sticky;
    bottom: 0;
    z-index: 2;
    padding: 10px 14px;
    border-top: 1px solid #e8edf4;
    background: #fff;
  }

  .task-dispatch-dialog .dialog-footer {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    align-items: center;
    gap: 8px;
  }

  .task-dispatch-dialog .dispatch-selected-count {
    grid-column: 1 / -1;
    margin-right: 0;
    padding-bottom: 2px;
    text-align: left;
  }

  .task-dispatch-dialog .dialog-footer .el-button {
    width: 100%;
    margin-left: 0;
  }

  .task-page :deep(.el-dialog) {
    width: 94vw !important;
    margin-top: 5vh !important;
  }

  .task-page :deep(.el-dialog__body) {
    max-height: 72vh;
    overflow-y: auto;
    padding: 14px;
  }

  .task-page :deep(.el-form .el-row) {
    display: block;
  }

  .task-page :deep(.el-form .el-col) {
    max-width: 100%;
    width: 100%;
  }
}

@media (max-width: 420px) {
  .task-card-meta,
  .task-card-actions {
    grid-template-columns: 1fr;
  }
}
</style>
