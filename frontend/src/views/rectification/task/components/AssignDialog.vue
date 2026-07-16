<template>
  <el-dialog class="mobile-form-dialog assign-dialog" :title="'分办整改执行人 - ' + taskInfo.taskNo" v-model="visible" :width="dialogWidth" append-to-body :close-on-click-modal="false" @close="handleClose">
    <el-alert title="请为每一项问题指派具体的整改执行人" type="info" :closable="false" show-icon class="mb15" />
    <el-table v-if="!isMobile" :data="issues" stripe max-height="400">
      <el-table-column label="问题编号" prop="issueNo" width="150" />
      <el-table-column label="问题标题" prop="issueTitle" min-width="200" show-overflow-tooltip />
      <el-table-column label="问题分类" width="90">
        <template #default="s"><el-tag size="small">{{ catLabel(s.row.issueCategory) }}</el-tag></template>
      </el-table-column>
      <el-table-column label="整改执行人" width="200">
        <template #default="s">
          <el-select v-model="s.row.assignTo" filterable :disabled="s.row.assigned" placeholder="选择整改执行人" size="small" style="width: 100%">
            <el-option v-for="u in deptUsers" :key="u.userId" :label="responsibleLabel(u)" :value="u.userId" />
          </el-select>
          <div v-if="s.row.assignment" class="assign-record">{{ assignmentLabel(s.row.assignment) }}</div>
        </template>
      </el-table-column>
    </el-table>
    <div v-else class="assign-mobile-list">
      <div v-for="item in issues" :key="item.issueId" class="assign-mobile-card">
        <div class="assign-card-head">
          <span class="assign-issue-no">{{ item.issueNo || '-' }}</span>
          <el-tag size="small">{{ catLabel(item.issueCategory) }}</el-tag>
        </div>
        <div class="assign-issue-title">{{ item.issueTitle || '未命名问题' }}</div>
        <div class="assign-card-field">
          <div class="assign-card-label">整改执行人</div>
          <el-select
            v-model="item.assignTo"
            filterable
            :disabled="item.assigned"
            placeholder="选择整改执行人"
            size="large"
            style="width: 100%"
          >
            <el-option v-for="u in deptUsers" :key="u.userId" :label="responsibleLabel(u)" :value="u.userId" />
          </el-select>
          <div v-if="item.assignment" class="assign-record">{{ assignmentLabel(item.assignment) }}</div>
        </div>
      </div>
      <el-empty v-if="!issues.length" description="暂无待分办问题" />
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确认分办</el-button>
        <el-button @click="handleClose">取消</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch, getCurrentInstance } from 'vue'
import { getTask } from '@/api/rectification/task'
import { getIssue } from '@/api/rectification/issue'
import { addPlan } from '@/api/rectification/plan'
import request from '@/utils/request'
import useAppStore from '@/store/modules/app'

const props = defineProps({ modelValue: Boolean, taskId: { type: Number, required: true } })
const emit = defineEmits(['update:modelValue', 'success'])
const { proxy } = getCurrentInstance()
const appStore = useAppStore()
const visible = computed({ get: () => props.modelValue, set: v => emit('update:modelValue', v) })
const dialogWidth = computed(() => appStore.device === 'mobile' ? '94vw' : '800px')
const isMobile = computed(() => appStore.device === 'mobile')
const submitting = ref(false)
const deptUsers = ref([])
const taskInfo = reactive({ taskNo: '' })
const issues = ref([])
const cats = { FUND: '资金类', ASSET: '资产类', PURCHASE: '采购类', HR: '人事类', CONSTRUCTION: '基建类', OTHER: '其他' }
function catLabel(v) { return cats[v] || v }
function responsibleLabel(user) {
  return [user.nickName || user.userName, user.remark].filter(Boolean).join(' · ')
}
function assignmentLabel(assignment) {
  const person = [assignment.responsibleName, assignment.responsibilityCategory].filter(Boolean).join(' · ')
  return ['已分办', person || assignment.responsibleUserId, assignment.assignTime].filter(Boolean).join(' · ')
}

watch(() => props.modelValue, val => { if (val) loadData() })

function loadData() {
  getTask(props.taskId).then(res => {
    const t = res.data || {}
    taskInfo.taskNo = t.taskNo || ''
    // 加载部门用户
    if (t.rectDeptId) {
      request({ url: '/rectification/task/executors', method: 'get', params: { deptId: t.rectDeptId } })
        .then(r => { deptUsers.value = r.data || [] })
        .catch(() => { deptUsers.value = [] })
    }
    // 从数据库加载已分办记录，重新打开时回显执行人。
    const ids = JSON.parse((t.issueIds || '[]').replace(/'/g, '"'))
    localStorage.removeItem('assign_' + props.taskId)
    issues.value = []
    request({ url: '/rectification/task/assignments/' + props.taskId, method: 'get' })
      .then(assignRes => {
        const assignedMap = new Map((assignRes.data || []).map(item => [String(item.issueId), item]))
        return Promise.all(ids.map(id => getIssue(id).then(r => {
          const iss = r.data || {}
          if (iss) {
            const assignment = assignedMap.get(String(id))
            iss.assignTo = assignment?.responsibleUserId
            iss.assigned = !!assignment
            iss.assignment = assignment
          }
          return iss
        })))
      })
      .then(list => { issues.value = list.filter(Boolean) })
      .catch(() => { issues.value = [] })
  })
}

function handleSubmit() {
  const unassigned = issues.value.filter(i => !i.assignTo)
  if (unassigned.length > 0) { proxy.$modal.msgWarning('请为所有问题指派整改执行人'); return }
  submitting.value = true
  const today = new Date().toISOString().slice(0, 10)
  const plans = issues.value.filter(i => !i.assigned).map(i => ({
    taskId: props.taskId, issueId: i.issueId, planContent: '待编制整改方案',
    responsibleUserId: i.assignTo, planDeadline: today, planType: '1'
  }))
  if (plans.length === 0) {
    proxy.$modal.msgInfo('当前问题均已完成分办')
    submitting.value = false
    return
  }
  Promise.all(plans.map(p => addPlan(p)))
    .then(() => { proxy.$modal.msgSuccess('分办完成'); emit('success'); handleClose() })
    .catch(() => { proxy.$modal.msgError('分办失败') })
    .finally(() => { submitting.value = false })
}

function handleClose() { issues.value = []; visible.value = false }
</script>

<style scoped>
.mb15 { margin-bottom: 15px; }
.assign-record {
  margin-top: 5px;
  color: #7a899d;
  font-size: 12px;
  line-height: 1.4;
}

@media (max-width: 768px) {
  :deep(.el-dialog__body) {
    padding: 12px;
  }

  .assign-mobile-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
    max-height: min(62vh, 520px);
    overflow-y: auto;
    padding: 2px 2px 4px;
  }

  .assign-mobile-card {
    border: 1px solid #e5e7eb;
    border-radius: 8px;
    background: #ffffff;
    padding: 12px;
    box-shadow: 0 6px 18px rgba(15, 23, 42, 0.06);
  }

  .assign-card-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
    margin-bottom: 8px;
  }

  .assign-issue-no {
    min-width: 0;
    color: #64748b;
    font-size: 12px;
    font-weight: 600;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .assign-issue-title {
    color: #111827;
    font-size: 15px;
    font-weight: 600;
    line-height: 1.45;
    margin-bottom: 12px;
    word-break: break-word;
  }

  .assign-card-field {
    display: flex;
    flex-direction: column;
    gap: 6px;
  }

  .assign-card-label {
    color: #475569;
    font-size: 13px;
  }
}
</style>
