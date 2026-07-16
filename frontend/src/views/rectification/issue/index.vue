<template>
  <div class="app-container issue-page">
    <!-- Search Form -->
    <el-form class="desktop-query-form" :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
      <el-form-item label="问题编号" prop="issueNo">
        <el-input
          v-model="queryParams.issueNo"
          placeholder="请输入问题编号"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="问题标题" prop="issueTitle">
        <el-input
          v-model="queryParams.issueTitle"
          placeholder="请输入问题标题"
          clearable
          style="width: 200px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="来源类型" prop="sourceType">
        <el-select v-model="queryParams.sourceType" placeholder="请选择来源类型" clearable style="width: 200px">
          <el-option
            v-for="dict in sourceTypeOptions"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable style="width: 200px">
          <el-option
            v-for="dict in statusOptions"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="问题分类" prop="issueCategory">
        <el-select v-model="queryParams.issueCategory" placeholder="请选择问题分类" clearable style="width: 200px">
          <el-option
            v-for="dict in categoryOptions"
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
      v-model="queryParams.issueTitle"
      placeholder="搜索问题标题"
      :active-count="mobileFilterCount"
      @search="handleQuery"
      @reset="resetQuery"
    >
      <el-form label-position="top">
        <el-form-item label="问题编号">
          <el-input v-model="queryParams.issueNo" placeholder="请输入问题编号" clearable />
        </el-form-item>
        <el-form-item label="来源类型">
          <el-select v-model="queryParams.sourceType" placeholder="全部来源" clearable>
            <el-option v-for="dict in sourceTypeOptions" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="整改状态">
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable>
            <el-option v-for="dict in statusOptions" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="问题分类">
          <el-select v-model="queryParams.issueCategory" placeholder="全部分类" clearable>
            <el-option v-for="dict in categoryOptions" :key="dict.value" :label="dict.label" :value="dict.value" />
          </el-select>
        </el-form-item>
      </el-form>
    </MobileFilterBar>

    <!-- Action Bar -->
    <el-row :gutter="10" class="mb8 issue-toolbar">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleAdd"
          v-hasPermi="['rectification:issue:add']"
        >新增问题</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="info"
          plain
          icon="Upload"
          @click="handleSync"
          v-hasPermi="['rectification:issue:sync']"
        >同步问题</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="Download"
          @click="handleExport"
          v-hasPermi="['rectification:issue:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- Table -->
    <el-table class="issue-table" v-loading="loading" :data="issueList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="问题编号" align="center" prop="issueNo" width="140" />
      <el-table-column
        label="问题标题"
        align="center"
        prop="issueTitle"
        :show-overflow-tooltip="true"
        min-width="200"
      />
      <el-table-column label="来源类型" align="center" prop="sourceType" width="120">
        <template #default="scope">
          <el-tag :type="sourceTypeTag(scope.row.sourceType)">
            {{ sourceTypeLabel(scope.row.sourceType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="问题责任单位" align="center" width="150">
        <template #default="scope">
          {{ deptName(scope.row.responsibleDeptId) }}
        </template>
      </el-table-column>
      <el-table-column label="直接责任人" align="center" prop="responsiblePerson" width="100" />
      <el-table-column v-if="!isSchoolLeader" label="涉及金额" align="center" prop="issueAmount" width="120">
        <template #default="scope">
          <span v-if="scope.row.issueAmount">{{ scope.row.issueAmount.toLocaleString() }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="100">
        <template #default="scope">
          <el-tag :type="statusTag(scope.row.status)">
            {{ statusLabel(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="截止日期" align="center" prop="deadline" width="110" />
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="200">
        <template #default="scope">
          <el-button
            link
            type="primary"
            icon="View"
            @click="handleDetail(scope.row)"
            v-hasPermi="['rectification:issue:query']"
          >查看</el-button>
          <el-button
            link
            type="primary"
            icon="Edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['rectification:issue:edit']"
          >编辑</el-button>
          <el-button
            link
            type="primary"
            icon="Delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['rectification:issue:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div v-loading="loading" class="issue-mobile-list">
      <el-empty v-if="!loading && issueList.length === 0" description="暂无问题数据" />
      <section v-for="item in issueList" :key="item.issueId" class="issue-card">
        <div class="issue-card-header">
          <div class="issue-card-title">
            <strong>{{ item.issueTitle || '-' }}</strong>
            <span>{{ item.issueNo || '-' }}</span>
          </div>
          <el-tag :type="statusTag(item.status)">{{ statusLabel(item.status) }}</el-tag>
        </div>

        <div class="issue-card-meta">
          <span v-if="!isSchoolLeader">
            <em>来源</em>
            <b>{{ sourceTypeLabel(item.sourceType) }}</b>
          </span>
          <span>
            <em>问题责任单位</em>
            <b>{{ deptName(item.responsibleDeptId) || '-' }}</b>
          </span>
          <span>
            <em>直接责任人</em>
            <b>{{ item.responsiblePerson || '-' }}</b>
          </span>
          <span>
            <em>截止日期</em>
            <b>{{ item.deadline || '-' }}</b>
          </span>
          <span>
            <em>涉及金额</em>
            <b>{{ item.issueAmount ? item.issueAmount.toLocaleString() : '-' }}</b>
          </span>
        </div>

        <div class="issue-card-actions">
          <el-button type="primary" plain icon="View" @click="handleDetail(item)" v-hasPermi="['rectification:issue:query']">查看</el-button>
          <el-button type="primary" plain icon="Edit" @click="handleUpdate(item)" v-hasPermi="['rectification:issue:edit']">编辑</el-button>
          <el-button type="danger" plain icon="Delete" @click="handleDelete(item)" v-hasPermi="['rectification:issue:remove']">删除</el-button>
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

    <!-- Add/Edit Dialog -->
    <el-dialog class="issue-form-dialog" :title="title" v-model="open" width="750px" append-to-body>
      <el-form class="issue-form" ref="issueRef" :model="form" :rules="rules" label-width="110px">
        <el-row>
          <el-col :span="24">
            <el-form-item label="问题标题" prop="issueTitle">
              <el-input v-model="form.issueTitle" placeholder="请输入问题标题" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="来源类型" prop="sourceType">
              <el-select v-model="form.sourceType" placeholder="请选择来源类型" style="width: 100%" @change="onSourceTypeChange">
                <el-option
                  v-for="dict in sourceTypeOptions"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="问题分类" prop="issueCategory">
              <el-select v-model="form.issueCategory" placeholder="请选择问题分类" style="width: 100%">
                <el-option
                  v-for="dict in categoryOptions"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="涉及金额" prop="issueAmount">
              <el-input-number
                v-model="form.issueAmount"
                :precision="2"
                :min="0"
                :controls="false"
                placeholder="请输入金额"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="风险等级">
              <el-radio-group v-model="riskLevel">
                <el-radio label="1">低</el-radio>
                <el-radio label="2">中</el-radio>
                <el-radio label="3">高</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="来源描述" prop="sourceDesc" v-if="form.sourceType && form.sourceType !== '1'">
          <el-input v-model="form.sourceDesc" type="textarea" :rows="2" placeholder="请输入外源来源描述" />
        </el-form-item>
        <el-form-item label="问题描述" prop="issueDesc">
          <el-input v-model="form.issueDesc" type="textarea" :rows="3" placeholder="请输入问题详细描述" />
        </el-form-item>
        <el-form-item label="定性法规依据" prop="legalBasis">
          <el-input v-model="form.legalBasis" type="textarea" :rows="2" placeholder="请输入定性法规依据" />
        </el-form-item>
        <el-row>
          <el-col :span="12">
            <el-form-item label="问题责任单位" prop="responsibleDeptId">
              <el-select v-model="form.responsibleDeptId" placeholder="请选择问题责任单位" style="width: 100%" @change="handleResponsibleDeptChange">
                <el-option v-for="d in deptList" :key="d.deptId" :label="d.deptName" :value="d.deptId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="直接责任人">
              <el-select v-model="form.responsiblePerson" filterable allow-create clearable placeholder="选择或录入直接责任人" style="width: 100%" :disabled="!form.responsibleDeptId" @focus="loadDeptUsers">
                <el-option v-for="u in userList" :key="u.userId" :label="u.nickName || u.userName" :value="u.nickName || u.userName" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="截止日期" prop="deadline">
              <el-date-picker
                v-model="form.deadline"
                type="date"
                placeholder="请选择截止日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status" v-if="form.issueId">
              <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
                <el-option
                  v-for="dict in statusOptions"
                  :key="dict.value"
                  :label="dict.label"
                  :value="dict.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- Detail Dialog -->
    <el-dialog class="issue-detail-dialog" :title="detailTitle" v-model="detailOpen" width="700px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="问题编号">{{ detail.issueNo }}</el-descriptions-item>
        <el-descriptions-item label="来源类型">
          <el-tag :type="sourceTypeTag(detail.sourceType)">{{ sourceTypeLabel(detail.sourceType) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="问题标题" :span="2">{{ detail.issueTitle }}</el-descriptions-item>
        <el-descriptions-item label="问题分类">{{ detail.issueCategory }}</el-descriptions-item>
        <el-descriptions-item v-if="!isSchoolLeader" label="涉及金额">{{ detail.issueAmount ? detail.issueAmount.toLocaleString() : '-' }}</el-descriptions-item>
        <el-descriptions-item label="问题责任单位">{{ deptName(detail.responsibleDeptId) }}</el-descriptions-item>
        <el-descriptions-item label="直接责任人">{{ detail.responsiblePerson }}</el-descriptions-item>
        <el-descriptions-item label="风险等级">
          <el-tag v-if="detail.riskLevel" :type="riskLevelTag(detail.riskLevel)">{{ riskLevelLabel(detail.riskLevel) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTag(detail.status)">{{ statusLabel(detail.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="截止日期">{{ detail.deadline }}</el-descriptions-item>
        <el-descriptions-item label="来源描述" :span="2">{{ detail.sourceDesc || '-' }}</el-descriptions-item>
        <el-descriptions-item label="问题描述" :span="2">{{ detail.issueDesc || '-' }}</el-descriptions-item>
        <el-descriptions-item v-if="!isSchoolLeader" label="定性法规依据" :span="2">{{ detail.legalBasis || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="detailOpen = false">关 闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Issue">
import { ref, reactive, toRefs, computed } from 'vue'
import { listIssue, getIssue, addIssue, updateIssue, delIssue, syncIssue } from '@/api/rectification/issue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
import MobileFilterBar from '@/components/MobileFilterBar/index.vue'
import useUserStore from '@/store/modules/user'

const { proxy } = getCurrentInstance()
const userStore = useUserStore()
const isSchoolLeader = computed(() => (userStore.roles || []).includes('school_leader'))

const issueList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const total = ref(0)
const title = ref('')
const detailOpen = ref(false)
const detailTitle = ref('')
const detail = ref({})
const riskLevel = ref('')
const deptList = ref([])
const userList = ref([])

function loadDeptUsers() {
  if (!form.value.responsibleDeptId) return
  request({ url: '/rectification/issue/dept-users', method: 'get', params: { deptId: form.value.responsibleDeptId } })
    .then(res => { userList.value = res.data || [] })
    .catch(() => { userList.value = [] })
}

function handleResponsibleDeptChange() {
  form.value.responsiblePerson = undefined
  userList.value = []
  loadDeptUsers()
}

const data = reactive({
  form: {
    sourceType: undefined,
    issueCategory: undefined,
    status: '0'
  },
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    issueNo: undefined,
    issueTitle: undefined,
    sourceType: undefined,
    status: undefined,
    issueCategory: undefined
  },
  rules: {
    issueTitle: [{ required: true, message: '问题标题不能为空', trigger: 'blur' }],
    sourceType: [{ required: true, message: '来源类型不能为空', trigger: 'change' }],
    issueCategory: [{ required: true, message: '问题分类不能为空', trigger: 'change' }],
    responsibleDeptId: [{ required: true, message: '问题责任单位不能为空', trigger: 'blur' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

const mobileFilterCount = computed(() => [
  queryParams.value.issueNo,
  queryParams.value.sourceType,
  queryParams.value.status,
  queryParams.value.issueCategory
].filter(value => value !== undefined && value !== null && value !== '').length)

// 下拉选项
const sourceTypeOptions = ref([
  { label: '内源审计', value: '1' },
  { label: '外源巡视巡察', value: '2' },
  { label: '外部督查', value: '3' }
])

const statusOptions = ref([
  { label: '待下发', value: '0' },
  { label: '整改中', value: '1' },
  { label: '待审核', value: '2' },
  { label: '已销号', value: '3' },
  { label: '持续整改', value: '4' }
])

const categoryOptions = ref([
  { label: '资金类', value: 'FUND' },
  { label: '资产类', value: 'ASSET' },
  { label: '采购类', value: 'PURCHASE' },
  { label: '人事类', value: 'HR' },
  { label: '基建类', value: 'CONSTRUCTION' },
  { label: '其他', value: 'OTHER' }
])

// 辅助函数
function sourceTypeLabel(val) {
  const item = sourceTypeOptions.value.find(d => d.value === val)
  return item ? item.label : val
}

function sourceTypeTag(val) {
  const map = { inner: '', inspection: 'warning', supervision: 'danger' }
  return map[val] || ''
}

function statusLabel(val) {
  const item = statusOptions.value.find(d => d.value === val)
  return item ? item.label : val
}

function statusTag(val) {
  const map = { '0': 'info', '1': 'warning', '2': 'primary', '3': 'success', '4': 'danger' }
  return map[val] || ''
}

function riskLevelLabel(val) {
  const map = { '1': '低', '2': '中', '3': '高' }
  return map[val] || val
}

function riskLevelTag(val) {
  const map = { '1': 'success', '2': 'warning', '3': 'danger' }
  return map[val] || ''
}

/** 查询问题台账列表 */
function getList() {
  loading.value = true
  listIssue(queryParams.value).then(response => {
    issueList.value = response.rows
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
  ids.value = selection.map(item => item.issueId)
}

/** 表单重置 */
function reset() {
  form.value = {
    issueId: undefined,
    issueNo: undefined,
    issueTitle: undefined,
    sourceType: undefined,
    sourceDesc: undefined,
    issueDesc: undefined,
    issueCategory: undefined,
    issueAmount: undefined,
    legalBasis: undefined,
    responsibleDeptId: undefined,
    responsiblePerson: undefined,
    deadline: undefined,
    status: '0'
  }
  riskLevel.value = ''
  proxy.resetForm('issueRef')
}

/** 取消按钮 */
function cancel() {
  open.value = false
  reset()
}

/** 新增按钮操作 */
function handleAdd() {
  reset()
  open.value = true
  title.value = '新增问题'
}

/** 修改按钮操作 */
function handleUpdate(row) {
  reset()
  const issueId = row.issueId || ids.value[0]
  getIssue(issueId).then(response => {
    form.value = response.data
    riskLevel.value = response.data.riskLevel || '2'
    open.value = true
    title.value = '修改问题'
  })
}

/** 查看详情 */
function handleDetail(row) {
  detail.value = row
  detailTitle.value = '问题详情 - ' + row.issueNo
  detailOpen.value = true
}

function deptName(id) {
  const d = deptList.value.find(d => d.deptId == id)
  return d ? d.deptName : id
}

function onSourceTypeChange(val) {
  if (val === '1') form.value.sourceDesc = undefined
}

/** 提交按钮 */
function submitForm() {
  proxy.$refs['issueRef'].validate(valid => {
    if (valid) {
      if (form.value.issueId != undefined) {
        form.value.riskLevel = riskLevel.value
        updateIssue(form.value).then(() => {
          proxy.$modal.msgSuccess('修改成功')
          open.value = false
          getList()
        })
      } else {
        form.value.riskLevel = riskLevel.value
        addIssue(form.value).then(() => {
          proxy.$modal.msgSuccess('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 删除按钮操作 */
function handleDelete(row) {
  const issueIds = row.issueId || ids.value.join(',')
  proxy.$modal.confirm('是否确认删除问题编号为"' + issueIds + '"的数据项？').then(function () {
    return delIssue(issueIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

/** 同步问题 */
function handleSync() {
  ElMessageBox.prompt('请输入审计项目ID进行同步', '同步问题', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /^\d+$/,
    inputErrorMessage: '请输入正确的项目ID'
  }).then(({ value }) => {
    syncIssue(value).then(() => {
      proxy.$modal.msgSuccess('同步成功')
      getList()
    })
  }).catch(() => {})
}

/** 导出按钮操作 */
function handleExport() {
  proxy.$modal.confirm('是否确认导出所有问题台账数据项？').then(function () {
    proxy.download('rectification/issue/export', queryParams.value, `issue_${new Date().getTime()}.xlsx`)
  }).catch(() => {})
}

getList()
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
</script>

<style scoped lang="scss">
.issue-mobile-list {
  display: none;
}

.issue-card {
  padding: 14px;
  margin-bottom: 12px;
  border: 1px solid #e3eaf4;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 8px 22px rgba(36, 52, 75, 0.06);
}

.issue-card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
}

.issue-card-title {
  min-width: 0;

  strong {
    display: block;
    color: #1f2f46;
    font-size: 15px;
    line-height: 1.45;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  span {
    display: block;
    margin-top: 4px;
    color: #7a8798;
    font-size: 12px;
  }
}

.issue-card-meta {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px 12px;
  padding: 12px 0;

  span {
    min-width: 0;
  }

  em {
    display: block;
    color: #8a96a8;
    font-size: 12px;
    line-height: 1.4;
    font-style: normal;
  }

  b {
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
}

.issue-card-actions {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
  padding-top: 10px;
  border-top: 1px solid #eef2f7;

  .el-button {
    width: 100%;
    margin-left: 0;
  }
}

@media (max-width: 768px) {
  .issue-page {
    padding: 12px;
    background: #f5f7fb;
  }

  .desktop-query-form {
    display: none;
  }

  .issue-page :deep(.el-form--inline) {
    padding: 12px;
    margin-bottom: 12px;
    border: 1px solid #e3eaf4;
    border-radius: 8px;
    background: #ffffff;
  }

  .issue-page :deep(.el-form--inline .el-form-item) {
    display: block;
    margin-right: 0;
    margin-bottom: 10px;
  }

  .issue-page :deep(.el-form--inline .el-form-item__label) {
    display: block;
    height: auto;
    margin-bottom: 6px;
    line-height: 1.4;
    text-align: left;
  }

  .issue-page :deep(.el-form--inline .el-input),
  .issue-page :deep(.el-form--inline .el-select) {
    width: 100% !important;
  }

  .issue-page :deep(.el-form--inline .el-form-item:last-child .el-form-item__content) {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 8px;
  }

  .issue-page :deep(.el-form--inline .el-form-item:last-child .el-button) {
    width: 100%;
    margin-left: 0;
  }

  .issue-toolbar {
    display: flex;
    gap: 8px;
    margin-bottom: 12px;
  }

  .issue-toolbar :deep(.el-col) {
    flex: 1;
    max-width: none;
    width: auto;
  }

  .issue-toolbar :deep(.el-button) {
    width: 100%;
    margin-left: 0;
    padding-left: 8px;
    padding-right: 8px;
  }

  .issue-toolbar :deep(.right-toolbar) {
    display: none;
  }

  .issue-table {
    display: none;
  }

  .issue-mobile-list {
    display: block;
  }

  .issue-page :deep(.pagination-container) {
    margin-top: 12px;
    padding: 10px 0 0;
    background: transparent;
  }

  .issue-page :deep(.el-dialog) {
    width: 94vw !important;
    margin-top: 5vh !important;
  }

  .issue-page :deep(.el-dialog__body) {
    max-height: 72vh;
    overflow-y: auto;
    padding: 14px;
  }

  .issue-page :deep(.el-form .el-row) {
    display: block;
  }

  .issue-page :deep(.el-form .el-col) {
    max-width: 100%;
    width: 100%;
  }

  .issue-page :deep(.el-descriptions__table) {
    display: block;
  }

  .issue-page :deep(.el-descriptions__body),
  .issue-page :deep(.el-descriptions__table tbody),
  .issue-page :deep(.el-descriptions__table tr),
  .issue-page :deep(.el-descriptions__table th),
  .issue-page :deep(.el-descriptions__table td) {
    display: block;
    width: 100% !important;
  }
}

@media (max-width: 420px) {
  .issue-card-meta {
    grid-template-columns: 1fr;
  }

  .issue-card-actions {
    grid-template-columns: 1fr;
  }
}
</style>

<style lang="scss">
@media (max-width: 768px) {
  .issue-form-dialog,
  .issue-detail-dialog {
    border-radius: 10px;

    .el-dialog__header {
      padding: 14px 16px 10px;
      margin-right: 0;
      border-bottom: 1px solid #eef2f7;
    }

    .el-dialog__title {
      color: #1f2f46;
      font-size: 16px;
      font-weight: 600;
      line-height: 1.4;
    }

    .el-dialog__body {
      padding: 14px 16px;
      background: #f7f9fc;
    }

    .el-dialog__footer {
      padding: 10px 16px 16px;
      border-top: 1px solid #eef2f7;
      background: #ffffff;
    }
  }

  .issue-form {
    .el-row {
      display: block;
    }

    .el-col {
      max-width: 100%;
      width: 100%;
    }

    .el-form-item {
      padding: 12px;
      margin-bottom: 10px;
      border: 1px solid #edf1f7;
      border-radius: 8px;
      background: #ffffff;
    }

    .el-form-item__label {
      display: block;
      width: 100% !important;
      height: auto;
      margin-bottom: 7px;
      padding: 0;
      color: #53627a;
      font-size: 13px;
      line-height: 1.4;
      text-align: left;
    }

    .el-form-item__content {
      width: 100%;
      margin-left: 0 !important;
      line-height: 1.4;
    }

    .el-input,
    .el-select,
    .el-input-number,
    .el-date-editor,
    .el-textarea {
      width: 100% !important;
    }

    .el-input__wrapper,
    .el-textarea__inner {
      min-height: 38px;
      box-shadow: 0 0 0 1px #dfe5ef inset;
      border-radius: 6px;
    }

    .el-textarea__inner {
      padding-top: 8px;
    }

    .el-radio-group {
      display: grid;
      grid-template-columns: repeat(3, minmax(0, 1fr));
      gap: 8px;
      width: 100%;
    }

    .el-radio {
      height: 38px;
      margin-right: 0;
      padding: 0 10px;
      border: 1px solid #dfe5ef;
      border-radius: 6px;
      background: #ffffff;
    }
  }
}
</style>
