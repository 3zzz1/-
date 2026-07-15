<template>
  <el-dialog class="mobile-form-dialog assign-dialog" :title="'分办责任人 - ' + taskInfo.taskNo" v-model="visible" :width="dialogWidth" append-to-body :close-on-click-modal="false" @close="handleClose">
    <el-alert title="请为每一项问题指派具体的整改责任人" type="info" :closable="false" show-icon class="mb15" />
    <el-table v-if="!isMobile" :data="issues" stripe max-height="400">
      <el-table-column label="问题编号" prop="issueNo" width="150" />
      <el-table-column label="问题标题" prop="issueTitle" min-width="200" show-overflow-tooltip />
      <el-table-column label="问题分类" width="90">
        <template #default="s"><el-tag size="small">{{ catLabel(s.row.issueCategory) }}</el-tag></template>
      </el-table-column>
      <el-table-column label="指派责任人" width="200">
        <template #default="s">
          <el-select v-model="s.row.assignTo" filterable :disabled="s.row.assigned" placeholder="选择责任人" size="small" style="width: 100%">
            <el-option v-for="u in deptUsers" :key="u.userId" :label="u.nickName || u.userName" :value="u.userId" />
          </el-select>
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
          <div class="assign-card-label">整改责任人</div>
          <el-select
            v-model="item.assignTo"
            filterable
            :disabled="item.assigned"
            placeholder="选择责任人"
            size="large"
            style="width: 100%"
          >
            <el-option v-for="u in deptUsers" :key="u.userId" :label="u.nickName || u.userName" :value="u.userId" />
          </el-select>
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

watch(() => props.modelValue, val => { if (val) loadData() })

function loadData() {
  getTask(props.taskId).then(res => {
    const t = res.data || {}
    taskInfo.taskNo = t.taskNo || ''
    // 加载部门用户
    if (t.rectDeptId) {
      request({ url: '/system/user/list', method: 'get', params: { deptId: t.rectDeptId, pageSize: 100 } })
        .then(r => { deptUsers.value = r.rows || [] })
    }
    // 加载关联问题
    const ids = JSON.parse((t.issueIds || '[]').replace(/'/g, '"'))
    const saved = JSON.parse(localStorage.getItem('assign_' + props.taskId) || '{}')
    issues.value = []
    ids.forEach(id => {
      getIssue(id).then(r => {
        const iss = r.data || {}
        if (iss) {
          iss.assignTo = saved[id] || undefined
          iss.assigned = false
          issues.value.push(iss)
        }
      })
    })
  })
}

function handleSubmit() {
  const unassigned = issues.value.filter(i => !i.assignTo)
  if (unassigned.length > 0) { proxy.$modal.msgWarning('请为所有问题指派责任人'); return }
  submitting.value = true
  const today = new Date().toISOString().slice(0, 10)
  const plans = issues.value.map(i => ({
    taskId: props.taskId, issueId: i.issueId, planContent: '待编制整改方案',
    responsibleUserId: i.assignTo, planDeadline: today, planType: '1'
  }))
  const saved = {}
  issues.value.forEach(i => { if (i.assignTo) saved[i.issueId] = i.assignTo })
  localStorage.setItem('assign_' + props.taskId, JSON.stringify(saved))
  Promise.all(plans.map(p => addPlan(p)))
    .then(() => { proxy.$modal.msgSuccess('分办完成'); emit('success'); handleClose() })
    .catch(() => { proxy.$modal.msgError('分办失败') })
    .finally(() => { submitting.value = false })
}

function handleClose() { issues.value = []; visible.value = false }
</script>

<style scoped>
.mb15 { margin-bottom: 15px; }

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
