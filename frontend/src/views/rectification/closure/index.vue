<template>
  <div class="app-container">
    <!-- Search Form -->
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="问题标题" prop="issueTitle">
        <el-input
          v-model="queryParams.issueTitle"
          placeholder="请输入问题标题"
          clearable
          style="width: 220px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="申请人" prop="applicant">
        <el-input
          v-model="queryParams.applicant"
          placeholder="请输入申请人"
          clearable
          style="width: 180px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select
          v-model="queryParams.status"
          placeholder="审核状态"
          clearable
          style="width: 160px"
        >
          <el-option label="待审核" value="0" />
          <el-option label="已销号" value="1" />
          <el-option label="已驳回" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="申请时间">
        <el-date-picker
          v-model="dateRange"
          value-format="YYYY-MM-DD"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          style="width: 240px"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- Toolbar -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="Plus"
          @click="handleApply"
          v-hasPermi="['rectification:closure:apply']"
        >申请销号</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- Table -->
    <el-table v-loading="loading" :data="closureList" @selection-change="handleSelectionChange" stripe>
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="问题标题" align="center" prop="issueTitle" min-width="200" show-overflow-tooltip />
      <el-table-column label="申请内容" align="center" prop="description" min-width="240" show-overflow-tooltip>
        <template #default="scope">
          <span>{{ scope.row.description || scope.row.applyContent || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="申请人" align="center" prop="applicant" width="100">
        <template #default="scope">
          {{ scope.row.applicant || scope.row.applyBy || scope.row.createBy || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="申请时间" align="center" prop="applyTime" width="160">
        <template #default="scope">
          <span>{{ scope.row.applyTime || scope.row.createTime || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template #default="scope">
          <el-tag
            :type="getStatusTagType(scope.row.status)"
            size="small"
          >
            {{ getStatusLabel(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="审核人" align="center" prop="auditor" width="100">
        <template #default="scope">
          {{ scope.row.auditor || scope.row.auditBy || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="审核时间" align="center" prop="auditTime" width="160">
        <template #default="scope">
          {{ scope.row.auditTime || scope.row.updateTime || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="150" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button
            v-if="scope.row.status === '0'"
            link
            type="primary"
            icon="Check"
            @click="handleAudit(scope.row)"
            v-hasPermi="['rectification:closure:audit']"
          >审核</el-button>
          <el-button
            link
            type="primary"
            icon="View"
            @click="handleView(scope.row)"
          >查看</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- Pagination -->
    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- Apply Dialog -->
    <closure-apply
      v-model="applyOpen"
      :issueData="currentIssue"
      @success="handleApplySuccess"
    />

    <!-- View Dialog -->
    <el-dialog :title="viewTitle" v-model="viewOpen" width="650px" append-to-body>
      <el-descriptions :column="1" border size="default">
        <el-descriptions-item label="问题标题" label-align="right" min-width="120">
          {{ viewData.issueTitle || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="申请内容" label-align="right">
          <div style="white-space: pre-wrap; line-height: 1.6; max-height: 200px; overflow-y: auto; background: #f5f7fa; padding: 8px; border-radius: 4px">
            {{ viewData.description || viewData.applyContent || viewData.content || '-' }}
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="申请人" label-align="right">
          {{ viewData.applicant || viewData.applyBy || viewData.createBy || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="申请时间" label-align="right">
          {{ viewData.applyTime || viewData.createTime || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="状态" label-align="right">
          <el-tag :type="getStatusTagType(viewData.status)" size="small">
            {{ getStatusLabel(viewData.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="审核人" label-align="right">
          {{ viewData.auditor || viewData.auditUserId || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="审核时间" label-align="right">
          {{ viewData.auditTime || viewData.updateTime || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="审核意见" label-align="right" v-if="viewData.opinion">
          <div style="white-space: pre-wrap; line-height: 1.6">
            {{ viewData.auditOpinion || viewData.opinion || '-' }}
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="补充整改要求" label-align="right" v-if="viewData.reworkRequirement">
          <div style="white-space: pre-wrap; line-height: 1.6; color: #F56C6C">
            {{ viewData.reRectRequired || viewData.reworkRequirement || '-' }}
          </div>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="viewOpen = false">关 闭</el-button>
          <el-button
            v-if="viewData.status === '0'"
            type="primary"
            @click="viewOpen = false; handleAudit(viewData)"
            v-hasPermi="['rectification:closure:audit']"
          >去审核</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- Audit Dialog -->
    <closure-audit
      v-model="auditOpen"
      :closureData="auditData"
      @success="handleAuditSuccess"
    />
  </div>
</template>

<script setup name="Closure">
import { ref, reactive, toRefs } from 'vue'
import request from '@/utils/request'
import { listClosure, getClosure } from '@/api/rectification/closure'
import ClosureApply from './components/ClosureApply.vue'
import ClosureAudit from './components/ClosureAudit.vue'

const { proxy } = getCurrentInstance()

const closureList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const dateRange = ref([])

// Apply dialog
const applyOpen = ref(false)
const currentIssue = ref({})

// View dialog
const viewOpen = ref(false)
const viewTitle = ref('销号详情')
const viewData = ref({})

// Audit dialog
const auditOpen = ref(false)
const auditData = ref({})

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    issueTitle: undefined,
    applicant: undefined,
    status: undefined
  }
})

const { queryParams } = toRefs(data)

/** Status helpers */
function getStatusTagType(status) {
  const map = { '0': 'warning', '1': 'success', '2': 'danger' }
  return map[status] || 'info'
}

function getStatusLabel(status) {
  const map = { '0': '待审核', '1': '已销号', '2': '已驳回' }
  return map[status] || status || '-'
}
function getStatusType(status) {
  const map = { '0': 'warning', '1': 'success', '2': 'danger' }
  return map[status] || 'info'
}

/** Query list */
function getList() {
  loading.value = true
  const params = { ...queryParams.value }
  if (dateRange.value && dateRange.value.length === 2) {
    params.beginTime = dateRange.value[0]
    params.endTime = dateRange.value[1]
  }
  listClosure(params).then(res => {
    const list = res.rows || res.data || []
    total.value = res.total || 0
    if (list.length === 0) { closureList.value = []; loading.value = false; return }
    let loaded = 0
    list.forEach((item) => {
      let done = 0
      const checkDone = () => { done++; if (done >= 2) { loaded++; if (loaded >= list.length) { closureList.value = [...list]; loading.value = false } } }
      request({ url: '/rectification/issue/' + item.issueId, method: 'get' }).then(r => {
        item.issueTitle = (r.data || {}).issueTitle || ''
      }).catch(() => {}).finally(checkDone)
      if (item.auditUserId) {
        request({ url: '/system/user/list', method: 'get', params: { userId: item.auditUserId } }).then(r => {
          const u = ((r.rows || [])[0] || {})
          item.auditor = u.nickName || u.userName || ''
        }).catch(() => {}).finally(checkDone)
      } else { checkDone() }
    })
    closureList.value = [...list]
  }).catch(() => { loading.value = false })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  dateRange.value = []
  proxy.resetForm('queryRef')
  handleQuery()
}

function handleSelectionChange() {}

/** Apply */
function handleApply(row) {
  currentIssue.value = row || {}
  applyOpen.value = true
}

function handleApplySuccess() {
  getList()
}

/** View */
function handleView(row) {
  const closureId = row.id || row.closureId
  const data = closureId ? row : row
  if (closureId) {
    getClosure(closureId).then(res => {
      viewData.value = res.data || res || row
      const iid = viewData.value.issueId
      if (iid) {
        request({ url: '/rectification/issue/' + iid, method: 'get' }).then(r => {
          viewData.value.issueTitle = (r.data || {}).issueTitle || ''
        })
      }
      // 加载审核人姓名
      if (viewData.value.auditUserId) {
        request({ url: '/system/user/list', method: 'get', params: { userId: viewData.value.auditUserId } }).then(r => {
          const u = ((r.rows || [])[0] || {})
          viewData.value.auditor = u.nickName || u.userName || ''
        })
      }
      viewOpen.value = true
    }).catch(() => { viewData.value = row; viewOpen.value = true })
  } else {
    viewData.value = row; viewOpen.value = true
  }
}

/** Audit */
function handleAudit(row) {
  const closureId = row.id || row.closureId
  if (!closureId) { auditData.value = row; auditOpen.value = true; return }
  getClosure(closureId).then(res => {
    const c = res.data || res || row
    c.applicant = c.applyBy || c.createBy || ''
    c.description = c.applyContent || ''
    const promises = []
    if (c.issueId) {
      promises.push(request({ url: '/rectification/issue/' + c.issueId, method: 'get' }).then(r => {
        c.issueTitle = (r.data || {}).issueTitle || ''
      }))
      promises.push(request({ url: '/rectification/material/list/' + c.issueId, method: 'get' }).then(r => {
        c.attachments = r.rows || []
      }))
    }
    Promise.all(promises).then(() => {
      auditData.value = c
      auditOpen.value = true
    })
  }).catch(() => { auditData.value = row; auditOpen.value = true })
}

function handleAuditSuccess() {
  getList()
}

getList()
</script>

<style scoped lang="scss">
.app-container {
  padding: 16px;
}

.mb8 {
  margin-bottom: 8px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
