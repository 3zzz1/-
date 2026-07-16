<template>
  <div class="app-container closure-page">
    <!-- Search Form -->
    <el-form class="desktop-query-form" :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="80px">
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

    <MobileFilterBar
      v-model="queryParams.issueTitle"
      placeholder="搜索问题标题"
      :active-count="mobileFilterCount"
      @search="handleQuery"
      @reset="resetQuery"
    >
      <el-form label-position="top">
        <el-form-item label="申请人">
          <el-input v-model="queryParams.applicant" placeholder="请输入申请人" clearable />
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable>
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
          />
        </el-form-item>
      </el-form>
    </MobileFilterBar>

    <!-- Toolbar -->
    <el-row :gutter="10" class="mb8 closure-toolbar">
      <el-col :span="1.5">
        <el-button
          v-if="canApplyClosure"
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
    <el-table class="closure-table" v-loading="loading" :data="closureList" @selection-change="handleSelectionChange" stripe>
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="问题标题" align="center" prop="issueTitle" min-width="200" show-overflow-tooltip />
      <el-table-column label="销号附言" align="center" prop="description" min-width="240" show-overflow-tooltip>
        <template #default="scope">
          <span>{{ closureRemark(scope.row) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="申请人" align="center" prop="applicant" width="100">
        <template #default="scope">
          {{ scope.row.applyUserName || scope.row.applicant || scope.row.applyBy || scope.row.createBy || '-' }}
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

    <div v-loading="loading" class="closure-mobile-list">
      <el-empty v-if="!loading && closureList.length === 0" description="暂无销号申请" />
      <section v-for="item in closureList" :key="item.id || item.closureId" class="closure-card">
        <div class="closure-card-header">
          <div class="closure-card-title">
            <strong>{{ item.issueTitle || '销号申请' }}</strong>
            <span>{{ closureRemark(item) }}</span>
          </div>
          <el-tag :type="getStatusTagType(item.status)" size="small">{{ getStatusLabel(item.status) }}</el-tag>
        </div>
        <div class="closure-card-meta">
          <span><em>申请人</em><b>{{ item.applyUserName || item.applicant || item.applyBy || item.createBy || '-' }}</b></span>
          <span><em>申请时间</em><b>{{ item.applyTime || item.createTime || '-' }}</b></span>
          <span><em>审核人</em><b>{{ item.auditor || item.auditBy || '-' }}</b></span>
          <span><em>审核时间</em><b>{{ item.auditTime || item.updateTime || '-' }}</b></span>
        </div>
        <div class="closure-card-actions">
          <el-button
            v-if="item.status === '0'"
            type="primary"
            plain
            icon="Check"
            @click="handleAudit(item)"
            v-hasPermi="['rectification:closure:audit']"
          >审核</el-button>
          <el-button type="primary" plain icon="View" @click="handleView(item)">查看</el-button>
        </div>
      </section>
    </div>

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
    <el-dialog class="closure-detail-dialog" :title="viewTitle" v-model="viewOpen" width="720px" append-to-body>
      <section class="closure-detail-summary">
        <div class="closure-detail-heading">
          <span>销号申请</span>
          <el-tag :type="getStatusTagType(viewData.status)" size="small">
            {{ getStatusLabel(viewData.status) }}
          </el-tag>
        </div>
        <h3>{{ viewData.issueTitle || '未命名问题' }}</h3>
        <div class="closure-detail-meta">
          <div><span>申请人</span><strong>{{ viewData.applyUserName || viewData.applicant || viewData.applyBy || viewData.createBy || '-' }}</strong></div>
          <div><span>申请时间</span><strong>{{ viewData.applyTime || viewData.createTime || '-' }}</strong></div>
          <div><span>审核人</span><strong>{{ viewData.auditor || viewData.auditUserId || '-' }}</strong></div>
          <div><span>审核时间</span><strong>{{ viewData.auditTime || viewData.updateTime || '-' }}</strong></div>
        </div>
        <div class="closure-detail-text">
          <span>销号附言</span>
          <p>{{ closureRemark(viewData) || '-' }}</p>
        </div>
      </section>

      <section class="closure-detail-section">
        <h4>整改文件</h4>
        <div class="closure-detail-files">
          <el-button
            type="primary"
            plain
            icon="Document"
            :disabled="!viewData.taskId"
            @click="downloadReport(viewData)"
          >整改报告</el-button>
          <el-button
            v-for="file in viewData.attachments || []"
            :key="file.materialId || file.id"
            plain
            icon="Paperclip"
            @click="downloadAttachment(file)"
          >{{ file.fileName || file.name || '附件' }}</el-button>
        </div>
        <span v-if="!viewData.attachments || viewData.attachments.length === 0" class="text-muted">暂无附件材料</span>
      </section>

      <section
        v-if="viewData.auditOpinion || viewData.opinion || viewData.reRectRequired || viewData.reworkRequirement"
        class="closure-detail-section"
      >
        <h4>审核记录</h4>
        <div v-if="viewData.auditOpinion || viewData.opinion" class="closure-detail-text">
          <span>审核意见</span>
          <p>{{ viewData.auditOpinion || viewData.opinion }}</p>
        </div>
        <div v-if="viewData.reRectRequired || viewData.reworkRequirement" class="closure-detail-text danger">
          <span>补充整改要求</span>
          <p>{{ viewData.reRectRequired || viewData.reworkRequirement }}</p>
        </div>
      </section>
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
import { ref, reactive, toRefs, getCurrentInstance, computed } from 'vue'
import { saveAs } from 'file-saver'
import request from '@/utils/request'
import { listClosure, getClosure } from '@/api/rectification/closure'
import { downloadReportWord } from '@/api/rectification/report'
import { downloadMaterial } from '@/api/rectification/material'
import ClosureApply from './components/ClosureApply.vue'
import ClosureAudit from './components/ClosureAudit.vue'
import MobileFilterBar from '@/components/MobileFilterBar/index.vue'
import useUserStore from '@/store/modules/user'

const { proxy } = getCurrentInstance()
const userStore = useUserStore()
const canApplyClosure = computed(() => (userStore.roles || []).includes('rect_responsible'))

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

const mobileFilterCount = computed(() => {
  return [queryParams.value.applicant, queryParams.value.status].filter(Boolean).length
    + (dateRange.value && dateRange.value.length === 2 ? 1 : 0)
})

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
      if (item.applyUserId) {
        request({ url: '/system/user/list', method: 'get', params: { userId: item.applyUserId } }).then(r => {
          const u = ((r.rows || [])[0] || {})
          item.applicant = u.nickName || u.userName || item.applicant || ''
        }).catch(() => {})
      }
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
        request({ url: '/rectification/material/list/' + iid, method: 'get' }).then(r => {
          viewData.value.attachments = r.rows || []
        }).catch(() => {
          viewData.value.attachments = []
        })
      }
      // 加载审核人姓名
      if (viewData.value.auditUserId) {
        request({ url: '/system/user/list', method: 'get', params: { userId: viewData.value.auditUserId } }).then(r => {
          const u = ((r.rows || [])[0] || {})
          viewData.value.auditor = u.nickName || u.userName || ''
        })
      }
      if (viewData.value.applyUserId) {
        request({ url: '/system/user/list', method: 'get', params: { userId: viewData.value.applyUserId } }).then(r => {
          const u = ((r.rows || [])[0] || {})
          viewData.value.applicant = u.nickName || u.userName || viewData.value.applicant || ''
        })
      }
      viewOpen.value = true
    }).catch(() => { viewData.value = row; viewOpen.value = true })
  } else {
    viewData.value = row; viewOpen.value = true
  }
}

function downloadAttachment(file) {
  const mid = file.materialId || file.id
  if (!mid) {
    proxy.$modal.msgWarning('材料ID不可用')
    return
  }
  downloadMaterial(mid).then(blob => {
    saveFileBlob(blob, file.fileName || file.name || ('material_' + mid))
  })
}

function closureRemark(row) {
  const content = row?.description || row?.applyContent || row?.content || ''
  if (!content) return '-'
  return String(content).split('\n\n报告文件：')[0] || '-'
}

function downloadReport(row) {
  if (!row?.taskId) {
    proxy.$modal.msgWarning('任务ID不可用')
    return
  }
  downloadReportWord(row.taskId).then(response => {
    saveReportBlob(response.data, reportOwnerFileName(row))
  })
}

async function saveReportBlob(blob, filename) {
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
  saveAs(new Blob([blob], {
    type: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
  }), filename)
}

async function saveFileBlob(blob, filename) {
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

function reportOwnerFileName(row) {
  const name = row?.applicant || row?.applyBy || row?.createBy || '整改人'
  return name + '整改报告.docx'
}

/** Audit */
function handleAudit(row) {
  const closureId = row.id || row.closureId
  if (!closureId) { auditData.value = row; auditOpen.value = true; return }
  getClosure(closureId).then(res => {
    const c = res.data || res || row
    c.applicant = c.applyBy || c.createBy || ''
    c.description = closureRemark(c)
    const promises = []
    if (c.issueId) {
      promises.push(request({ url: '/rectification/issue/' + c.issueId, method: 'get' }).then(r => {
        c.issueTitle = (r.data || {}).issueTitle || ''
      }))
      promises.push(request({ url: '/rectification/material/list/' + c.issueId, method: 'get' }).then(r => {
        c.attachments = r.rows || []
      }))
    }
    if (c.applyUserId) {
      promises.push(request({ url: '/system/user/list', method: 'get', params: { userId: c.applyUserId } }).then(r => {
        const u = ((r.rows || [])[0] || {})
        c.applicant = u.nickName || u.userName || c.applicant || ''
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

.attachment-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.closure-detail-summary,
.closure-detail-section {
  padding: 16px;
  border: 1px solid #e5eaf1;
  border-radius: 8px;
  background: #fff;
}

.closure-detail-section {
  margin-top: 12px;
}

.closure-detail-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.closure-detail-heading > span,
.closure-detail-meta span,
.closure-detail-text > span {
  color: #8492a6;
  font-size: 12px;
}

.closure-detail-summary h3 {
  margin: 8px 0 16px;
  color: #1f2f46;
  font-size: 17px;
  line-height: 1.5;
  overflow-wrap: anywhere;
}

.closure-detail-meta {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px 20px;
}

.closure-detail-meta div {
  min-width: 0;
}

.closure-detail-meta span,
.closure-detail-meta strong {
  display: block;
}

.closure-detail-meta strong {
  margin-top: 4px;
  color: #34465e;
  font-size: 13px;
  font-weight: 500;
  line-height: 1.45;
  overflow-wrap: anywhere;
}

.closure-detail-text {
  padding: 10px 12px;
  margin-top: 14px;
  border-radius: 6px;
  background: #f5f7fa;
}

.closure-detail-text p {
  max-height: 180px;
  margin: 5px 0 0;
  overflow-y: auto;
  color: #34465e;
  font-size: 13px;
  line-height: 1.65;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
}

.closure-detail-text.danger {
  background: #fff2f0;
}

.closure-detail-text.danger p {
  color: #c0362c;
}

.closure-detail-section h4 {
  margin: 0 0 10px;
  color: #34465e;
  font-size: 14px;
}

.closure-detail-files {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
}

.closure-detail-files .el-button {
  width: 100%;
  min-width: 0;
  margin: 0;
  justify-content: flex-start;
  overflow: hidden;
}

.closure-detail-files .el-button :deep(span) {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.content-box {
  white-space: pre-wrap;
  line-height: 1.6;
  max-height: 200px;
  overflow-y: auto;
  background: #f5f7fa;
  padding: 8px;
  border-radius: 4px;
}

.text-muted {
  color: #c0c4cc;
}

.closure-mobile-list {
  display: none;
}

.closure-card {
  padding: 14px;
  margin-bottom: 12px;
  border: 1px solid #e3eaf4;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 8px 22px rgba(36, 52, 75, 0.06);
}

.closure-card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
}

.closure-card-title {
  min-width: 0;
}

.closure-card-title strong {
  display: block;
  color: #1f2f46;
  font-size: 15px;
  line-height: 1.45;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.closure-card-title span {
  display: block;
  margin-top: 4px;
  color: #7a8798;
  font-size: 12px;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.closure-card-meta {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px 12px;
  padding: 12px 0;
}

.closure-card-meta span {
  min-width: 0;
}

.closure-card-meta em {
  display: block;
  color: #8a96a8;
  font-size: 12px;
  line-height: 1.4;
  font-style: normal;
}

.closure-card-meta b {
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

.closure-card-actions {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
  padding-top: 10px;
  border-top: 1px solid #eef2f7;
}

.closure-card-actions .el-button {
  width: 100%;
  margin-left: 0;
}

@media (max-width: 768px) {
  .closure-page {
    padding: 12px;
    background: #f5f7fb;
  }

  .desktop-query-form {
    display: none;
  }

  .closure-toolbar {
    display: flex;
    gap: 8px;
    margin-bottom: 12px;
  }

  .closure-toolbar :deep(.el-col) {
    flex: 1;
    max-width: none;
    width: auto;
  }

  .closure-toolbar :deep(.el-button) {
    width: 100%;
    margin-left: 0;
  }

  .closure-toolbar :deep(.right-toolbar) {
    display: none;
  }

  .closure-table {
    display: none;
  }

  .closure-mobile-list {
    display: block;
  }

  .attachment-actions {
    display: grid;
    grid-template-columns: 1fr;
  }

  .attachment-actions .el-button {
    width: 100%;
    margin-left: 0;
  }

  .closure-detail-summary,
  .closure-detail-section {
    padding: 13px;
  }

  .closure-detail-meta,
  .closure-detail-files {
    grid-template-columns: 1fr;
  }

  .closure-page :deep(.el-dialog) {
    width: 94vw !important;
    margin-top: 5vh !important;
  }

  .closure-page :deep(.el-dialog__body) {
    max-height: 72vh;
    overflow-y: auto;
    padding: 14px;
  }
}

@media (max-width: 420px) {
  .closure-card-meta,
  .closure-card-actions {
    grid-template-columns: 1fr;
  }
}
</style>
