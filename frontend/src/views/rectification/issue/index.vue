<template>
  <div class="app-container">
    <!-- Search Form -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch">
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

    <!-- Action Bar -->
    <el-row :gutter="10" class="mb8">
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
    <el-table v-loading="loading" :data="issueList" @selection-change="handleSelectionChange">
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
      <el-table-column label="责任单位" align="center" width="150">
        <template #default="scope">
          {{ deptName(scope.row.responsibleDeptId) }}
        </template>
      </el-table-column>
      <el-table-column label="责任干部" align="center" prop="responsiblePerson" width="100" />
      <el-table-column label="涉及金额" align="center" prop="issueAmount" width="120">
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

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- Add/Edit Dialog -->
    <el-dialog :title="title" v-model="open" width="750px" append-to-body>
      <el-form ref="issueRef" :model="form" :rules="rules" label-width="110px">
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
            <el-form-item label="责任单位" prop="responsibleDeptId">
              <el-select v-model="form.responsibleDeptId" placeholder="请选择责任单位" style="width: 100%">
                <el-option v-for="d in deptList" :key="d.deptId" :label="d.deptName" :value="d.deptId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="责任干部">
              <el-select v-model="form.responsiblePerson" filterable allow-create clearable placeholder="先选责任单位，再选干部" style="width: 100%" :disabled="!form.responsibleDeptId" @focus="loadDeptUsers">
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
    <el-dialog :title="detailTitle" v-model="detailOpen" width="700px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="问题编号">{{ detail.issueNo }}</el-descriptions-item>
        <el-descriptions-item label="来源类型">
          <el-tag :type="sourceTypeTag(detail.sourceType)">{{ sourceTypeLabel(detail.sourceType) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="问题标题" :span="2">{{ detail.issueTitle }}</el-descriptions-item>
        <el-descriptions-item label="问题分类">{{ detail.issueCategory }}</el-descriptions-item>
        <el-descriptions-item label="涉及金额">{{ detail.issueAmount ? detail.issueAmount.toLocaleString() : '-' }}</el-descriptions-item>
        <el-descriptions-item label="责任单位">{{ deptName(detail.responsibleDeptId) }}</el-descriptions-item>
        <el-descriptions-item label="责任干部">{{ detail.responsiblePerson }}</el-descriptions-item>
        <el-descriptions-item label="风险等级">
          <el-tag v-if="detail.riskLevel" :type="riskLevelTag(detail.riskLevel)">{{ riskLevelLabel(detail.riskLevel) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTag(detail.status)">{{ statusLabel(detail.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="截止日期">{{ detail.deadline }}</el-descriptions-item>
        <el-descriptions-item label="来源描述" :span="2">{{ detail.sourceDesc || '-' }}</el-descriptions-item>
        <el-descriptions-item label="问题描述" :span="2">{{ detail.issueDesc || '-' }}</el-descriptions-item>
        <el-descriptions-item label="定性法规依据" :span="2">{{ detail.legalBasis || '-' }}</el-descriptions-item>
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
import { ref, reactive, toRefs } from 'vue'
import { listIssue, getIssue, addIssue, updateIssue, delIssue, syncIssue } from '@/api/rectification/issue'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const { proxy } = getCurrentInstance()

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
  request({ url: '/system/user/list', method: 'get', params: { deptId: form.value.responsibleDeptId, pageSize: 100 } })
    .then(res => { userList.value = res.rows || [] })
    .catch(() => { userList.value = [] })
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
    responsibleDeptId: [{ required: true, message: '责任单位不能为空', trigger: 'blur' }]
  }
})

const { queryParams, form, rules } = toRefs(data)

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
