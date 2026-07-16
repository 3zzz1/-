<template>
  <div class="plan-editor">
    <el-card shadow="never" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span class="card-title">{{ isApprovalOnlyView ? '方案变更审批' : '整改方案' }}</span>
          <div class="header-actions">
            <template v-if="isResponsible">
              <el-button type="primary" :loading="submitLoading" @click="handleSubmit">提交方案</el-button>
              <el-button :loading="draftLoading" @click="handleSaveDraft">保存草稿</el-button>
              <el-button type="warning" plain :disabled="hasPendingChange" @click="openChangeApply('1')">申请延期</el-button>
              <el-button type="danger" plain :disabled="hasPendingChange || planInfo.planType === '2'" @click="openChangeApply('2')">持续整改</el-button>
            </template>
          </div>
        </div>
      </template>

      <el-alert
        v-if="!isApprovalOnlyView && closureRejectRequirement"
        type="error"
        show-icon
        :closable="false"
        class="reject-alert"
      >
        <template #title>
          销号申请已被审计处驳回，请根据补充整改要求修改整改方案后重新提交报告
        </template>
        <div class="reject-requirement">
          <div class="requirement-label">补充整改要求</div>
          <div class="requirement-content">{{ closureRejectRequirement }}</div>
        </div>
      </el-alert>

      <section v-if="latestChange.extensionId" class="plan-change-panel">
        <div class="change-panel-head">
          <div>
            <span>方案变更申请</span>
            <strong>{{ changeTypeLabel(latestChange.applyType) }}</strong>
          </div>
          <el-tag :type="changeStatusTag(latestChange.status)">{{ changeStatusLabel(latestChange.status) }}</el-tag>
        </div>
        <div class="change-info-grid">
          <div><span>申请人</span><strong>{{ latestChange.applyUserName || latestChange.createBy || '-' }}</strong></div>
          <div><span>申请时间</span><strong>{{ latestChange.applyTime || '-' }}</strong></div>
          <div><span>申请原因</span><strong>{{ latestChange.reason || '-' }}</strong></div>
          <div v-if="latestChange.applyType === '1'"><span>原截止日期</span><strong>{{ latestChange.originalDeadline || '-' }}</strong></div>
          <div v-if="latestChange.applyType === '1'"><span>申请延期至</span><strong>{{ latestChange.newDeadline || '-' }}</strong></div>
          <div v-if="latestChange.applyType === '2'"><span>阶段整改目标</span><strong>{{ latestChange.stageGoal || '-' }}</strong></div>
          <div v-if="latestChange.applyType === '2'"><span>下次复核日期</span><strong>{{ latestChange.reviewDate || '-' }}</strong></div>
          <div v-if="latestChange.applyType === '2'"><span>预计完成日期</span><strong>{{ latestChange.expectedFinishDate || '-' }}</strong></div>
          <div v-if="latestChange.approveOpinion"><span>单位审批意见</span><strong>{{ latestChange.approveOpinion }}</strong></div>
          <div v-if="latestChange.auditApproveOpinion"><span>审计处审批意见</span><strong>{{ latestChange.auditApproveOpinion }}</strong></div>
        </div>
        <div v-if="canApproveChange" class="change-approve-actions">
          <el-button type="success" icon="Check" @click="openChangeApproval('1')">通过</el-button>
          <el-button type="danger" plain icon="Close" @click="openChangeApproval('2')">驳回</el-button>
        </div>
      </section>
      <el-empty v-else-if="isApprovalOnlyView" description="暂无待审批的延期或持续整改申请" />

      <el-alert
        v-if="!isApprovalOnlyView && reportRejectOpinion"
        type="error"
        show-icon
        :closable="false"
        class="reject-alert"
      >
        <template #title>
          整改报告已被单位负责人驳回，请根据驳回原因修改整改方案后重新提交报告
        </template>
        <div class="reject-requirement">
          <div class="requirement-label">驳回原因</div>
          <div class="requirement-content">{{ reportRejectOpinion }}</div>
        </div>
      </el-alert>

      <el-form v-if="!isApprovalOnlyView" ref="formRef" :model="form" :rules="rules" label-width="110px" size="default">
        <el-form-item label="方案内容" prop="planContent">
          <div class="editor-wrapper">
            <el-input
              v-model="form.planContent"
              type="textarea"
              :rows="15"
              placeholder="请输入整改方案详细内容，包括整改措施、责任人、时间节点等..."
            />
          </div>
        </el-form-item>

        <el-form-item label="计划完成日期" prop="plannedCompletionDate">
          <el-date-picker
            v-model="form.plannedCompletionDate"
            type="date"
            placeholder="请选择计划完成日期"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
        </el-form-item>

        <el-form-item label="整改责任人">
          <el-input :model-value="assignedUserName" readonly placeholder="已由联络员分办时指定" style="width: 300px" />
        </el-form-item>

        <el-form-item label="备注说明" prop="remark">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="3"
            placeholder="其他需要说明的事项（选填）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <div v-if="!isApprovalOnlyView && rejectRecords.length > 0" class="reject-records">
        <el-divider content-position="left">历史驳回记录</el-divider>
        <el-timeline>
          <el-timeline-item
            v-for="item in rejectRecords"
            :key="item.progressId || item.id"
            :timestamp="item.operateTime || item.createTime || '-'"
            type="danger"
            color="#F56C6C"
            placement="top"
          >
            <div class="record-card">
              <div class="record-header">
                <el-tag type="danger" size="small">{{ rejectTypeLabel(item.progressType) }}</el-tag>
                <span class="record-operator">{{ item.operatorName || item.createBy || '系统' }}</span>
              </div>
              <div class="record-content">{{ item.content || '-' }}</div>
            </div>
          </el-timeline-item>
        </el-timeline>
      </div>

      <!-- 方案状态信息 -->
      <div v-if="!isApprovalOnlyView && planInfo.updateTime" class="plan-meta">
        <el-divider />
        <el-descriptions :column="3" size="small">
          <el-descriptions-item label="责任人">{{ assignedUserName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ planInfo.createTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="最后更新">{{ planInfo.updateTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="方案状态">
            <el-tag v-if="isPlanRejected" type="danger" size="small">已驳回</el-tag>
            <el-tag v-else-if="planInfo.status === '0'" type="info" size="small">草稿</el-tag>
            <el-tag v-else-if="planInfo.status === '1'" type="warning" size="small">已提交</el-tag>
            <el-tag v-else-if="planInfo.status === '2'" type="success" size="small">已通过</el-tag>
            <el-tag v-else-if="planInfo.status === '3'" type="danger" size="small">已驳回</el-tag>
            <span v-else>未提交</span>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>

    <el-dialog
      v-model="changeApplyOpen"
      class="mobile-form-dialog plan-change-dialog"
      :title="changeForm.applyType === '1' ? '申请延期' : '申请长期持续整改'"
      width="620px"
      append-to-body
      :close-on-click-modal="false"
    >
      <el-form ref="changeFormRef" :model="changeForm" :rules="changeRules" label-position="top">
        <template v-if="changeForm.applyType === '1'">
          <el-form-item label="原截止日期">
            <el-input :model-value="taskDeadline" disabled />
          </el-form-item>
          <el-form-item label="延期至" prop="newDeadline">
            <el-date-picker v-model="changeForm.newDeadline" type="date" value-format="YYYY-MM-DD" placeholder="请选择新的完成日期" style="width: 100%" />
          </el-form-item>
        </template>
        <el-form-item :label="changeForm.applyType === '1' ? '延期原因' : '持续整改原因'" prop="reason">
          <el-input v-model="changeForm.reason" type="textarea" :rows="3" placeholder="请说明申请原因和当前整改进展" />
        </el-form-item>
        <template v-if="changeForm.applyType === '2'">
          <el-form-item label="阶段整改目标" prop="stageGoal">
            <el-input v-model="changeForm.stageGoal" type="textarea" :rows="3" placeholder="请填写下一阶段需要完成的具体目标" />
          </el-form-item>
          <el-form-item label="下一次复核日期" prop="reviewDate">
            <el-date-picker v-model="changeForm.reviewDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
          </el-form-item>
          <el-form-item label="预计最终完成日期" prop="expectedFinishDate">
            <el-date-picker v-model="changeForm.expectedFinishDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" :loading="changeSubmitting" @click="submitChangeApply">提交申请</el-button>
          <el-button @click="changeApplyOpen = false">取消</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="changeApproveOpen" class="mobile-form-dialog" title="方案变更审批" width="520px" append-to-body>
      <el-alert
        :title="approvalResult === '1' ? '确认通过该方案变更申请' : '确认驳回该方案变更申请'"
        :type="approvalResult === '1' ? 'success' : 'warning'"
        :closable="false"
        show-icon
        class="mb15"
      />
      <el-form label-position="top">
        <el-form-item :label="approvalResult === '1' ? '审批意见' : '驳回原因'">
          <el-input v-model="approvalOpinion" type="textarea" :rows="4" placeholder="请输入审批意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button :type="approvalResult === '1' ? 'success' : 'danger'" :loading="changeSubmitting" @click="submitChangeApproval">确认提交</el-button>
          <el-button @click="changeApproveOpen = false">取消</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="PlanEditor">
import { ref, reactive, computed, onMounted, watch, getCurrentInstance } from 'vue'
import { getPlan, addPlan, updatePlan, applyExtension, applyLongTerm, approveExtension, getLatestPlanChange } from '@/api/rectification/plan'
import { getReport } from '@/api/rectification/report'
import request from '@/utils/request'
import useUserStore from '@/store/modules/user'
const props = defineProps({
  taskId: { type: Number, required: true }
})
const emit = defineEmits(['change-approved'])

const { proxy } = getCurrentInstance()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const submitLoading = ref(false)
const draftLoading = ref(false)
const assignedUserName = ref('')
const reportApproveStatus = ref('')
const reportApproveTime = ref('')
const reportApproveOpinion = ref('')
const closureInfo = ref({})
const rejectRecords = ref([])
const taskDeadline = ref('')
const changeApplyOpen = ref(false)
const changeApproveOpen = ref(false)
const changeSubmitting = ref(false)
const changeFormRef = ref(null)
const approvalResult = ref('')
const approvalOpinion = ref('')

const isResponsible = computed(() => (userStore.roles || []).includes('rect_responsible'))
const isUnitLeader = computed(() => (userStore.roles || []).includes('audited_unit_leader'))
const isAuditManager = computed(() => {
  const roles = userStore.roles || []
  return roles.includes('admin') || roles.includes('audit_director') || roles.includes('audit_lead')
})

const latestChange = reactive({
  extensionId: null,
  applyType: '',
  status: '',
  reason: '',
  originalDeadline: '',
  newDeadline: '',
  stageGoal: '',
  reviewDate: '',
  expectedFinishDate: '',
  approveOpinion: '',
  auditApproveOpinion: '',
  applyTime: '',
  createBy: '',
  applyUserName: ''
})

const changeForm = reactive({
  applyType: '1',
  newDeadline: '',
  reason: '',
  stageGoal: '',
  reviewDate: '',
  expectedFinishDate: ''
})

const hasPendingChange = computed(() => ['0', '1'].includes(String(latestChange.status)))
const canApproveChange = computed(() => {
  return (isUnitLeader.value && latestChange.status === '0')
    || (isAuditManager.value && latestChange.status === '1')
})
const isApprovalOnlyView = computed(() => {
  return isUnitLeader.value
    || (isAuditManager.value && latestChange.extensionId && latestChange.status === '1')
})

const changeRules = {
  newDeadline: [{ validator: validateNewDeadline, trigger: 'change' }],
  reason: [{ required: true, message: '申请原因不能为空', trigger: 'blur' }],
  stageGoal: [{ validator: validateLongTermField, trigger: 'blur' }],
  reviewDate: [{ validator: validateLongTermField, trigger: 'change' }],
  expectedFinishDate: [{ validator: validateLongTermField, trigger: 'change' }]
}

const form = reactive({
  taskId: props.taskId,
  planContent: '',
  plannedCompletionDate: undefined,
  responsiblePerson: '',
  remark: ''
})

const planInfo = reactive({
  id: null,
  issueId: null,
  planType: '1',
  status: '',
  createBy: '',
  createTime: '',
  updateTime: ''
})

const isPlanRejected = computed(() => {
  if (closureInfo.value.status === '2') return true
  if (reportApproveStatus.value !== '2') return false
  const planChangeTime = planInfo.updateTime || planInfo.createTime
  if (!reportApproveTime.value || !planChangeTime) return true
  return new Date(planChangeTime).getTime() <= new Date(reportApproveTime.value).getTime()
})

const closureRejectRequirement = computed(() => {
  if (closureInfo.value.status !== '2') return ''
  return closureInfo.value.reRectRequired || ''
})

const reportRejectOpinion = computed(() => {
  if (reportApproveStatus.value !== '2') return ''
  return reportApproveOpinion.value || ''
})

const rules = reactive({
  planContent: [
    { required: true, message: '整改方案内容不能为空', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (!value || value.replace(/<[^>]*>/g, '').trim() === '') {
          callback(new Error('整改方案内容不能为空'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  plannedCompletionDate: [
    { required: true, message: '请选择计划完成日期', trigger: 'change' }
  ],
  responsiblePerson: [
    { required: true, message: '请填写整改责任人', trigger: 'blur' }
  ]
})

// Rich text editor toolbar config
const toolbarOptions = [
  [{ header: [1, 2, 3, false] }],
  ['bold', 'italic', 'underline', 'strike'],
  [{ color: [] }, { background: [] }],
  [{ list: 'ordered' }, { list: 'bullet' }, { list: 'check' }],
  [{ indent: '-1' }, { indent: '+1' }, { align: [] }],
  ['blockquote', 'code-block'],
  ['link', 'image'],
  ['clean']
]

const editorModules = {
  clipboard: {
    matchVisual: false
  }
}

// Load existing plan on mount
onMounted(() => {
  loadPlan()
})

// Reload when taskId changes
watch(() => props.taskId, () => {
  form.taskId = props.taskId
  loadPlan()
})

function loadPlan() {
  if (!props.taskId) return
  loading.value = true
  reportApproveStatus.value = ''
  reportApproveTime.value = ''
  reportApproveOpinion.value = ''
  closureInfo.value = {}
  rejectRecords.value = []
  const taskRequest = request({ url: '/rectification/task/' + props.taskId, method: 'get' }).then(res => {
    taskDeadline.value = (res.data || {}).deadline || ''
  }).catch(() => {})
  const changeRequest = loadLatestChange()
  if (isUnitLeader.value) {
    Promise.all([taskRequest, changeRequest]).finally(() => {
      loading.value = false
    })
    return
  }
  getReport(props.taskId).then(res => {
    const report = res.data || {}
    reportApproveStatus.value = report.unitApproveStatus || ''
    reportApproveTime.value = report.unitApproveTime || report.updateTime || ''
    reportApproveOpinion.value = report.unitApproveOpinion || ''
  }).catch(() => {})
  request({ url: '/rectification/closure/task/' + props.taskId + '/latest', method: 'get' }).then(res => {
    closureInfo.value = res.data || {}
  }).catch(() => {
    closureInfo.value = {}
  })
  loadRejectRecords()
  getPlan(props.taskId)
    .then((response) => {
      const data = response.data
      if (data) {
        form.planContent = data.planContent || data.content || ''
        form.plannedCompletionDate = data.planDeadline || data.plannedCompletionDate || undefined
        form.responsiblePerson = data.responsiblePerson || data.rectifyPerson || ''
        if (data.responsibleUserId) {
          request({ url: '/system/user/list', method: 'get', params: { userId: data.responsibleUserId } })
            .then(res => { const u = (res.rows||[])[0]; if (u) assignedUserName.value = u.nickName || u.userName })
        }
        form.remark = data.remark || ''
        planInfo.id = data.id || data.planId || null
        planInfo.issueId = data.issueId || null
        planInfo.planType = data.planType || '1'
        planInfo.status = data.status || ''
        planInfo.createBy = data.createBy || ''
        planInfo.createTime = data.createTime || ''
        planInfo.updateTime = data.updateTime || ''
      }
    })
    .catch(() => {})
    .finally(() => {
      loading.value = false
    })
}

function loadLatestChange() {
  return getLatestPlanChange(props.taskId).then(res => {
    const data = res.data || {}
    Object.assign(latestChange, {
      extensionId: data.extensionId || null,
      applyType: data.applyType || '',
      status: data.status || '',
      reason: data.reason || '',
      originalDeadline: data.originalDeadline || '',
      newDeadline: data.newDeadline || '',
      stageGoal: data.stageGoal || '',
      reviewDate: data.reviewDate || '',
      expectedFinishDate: data.expectedFinishDate || '',
      approveOpinion: data.approveOpinion || '',
      auditApproveOpinion: data.auditApproveOpinion || '',
      applyTime: data.applyTime || data.createTime || '',
      createBy: data.createBy || '',
      applyUserName: data.applyUserName || ''
    })
  }).catch(() => {
    latestChange.extensionId = null
    latestChange.status = ''
  })
}

function changeTypeLabel(type) {
  return type === '2' ? '长期持续整改申请' : '延期申请'
}

function changeStatusLabel(status) {
  const map = { '0': '单位审批中', '1': '审计处审批中', '2': '已通过', '3': '单位已驳回', '4': '审计处已驳回' }
  return map[status] || '-'
}

function changeStatusTag(status) {
  const map = { '0': 'warning', '1': 'warning', '2': 'success', '3': 'danger', '4': 'danger' }
  return map[status] || 'info'
}

function validateNewDeadline(rule, value, callback) {
  if (changeForm.applyType !== '1') return callback()
  if (!value) return callback(new Error('请选择新的完成日期'))
  if (taskDeadline.value && new Date(value).getTime() <= new Date(taskDeadline.value).getTime()) {
    return callback(new Error('延期日期必须晚于原截止日期'))
  }
  callback()
}

function validateLongTermField(rule, value, callback) {
  if (changeForm.applyType === '2' && !value) return callback(new Error('该项不能为空'))
  callback()
}

function openChangeApply(type) {
  if (!planInfo.id) {
    proxy.$modal.msgWarning('请先保存整改方案')
    return
  }
  changeForm.applyType = type
  changeForm.newDeadline = ''
  changeForm.reason = ''
  changeForm.stageGoal = ''
  changeForm.reviewDate = ''
  changeForm.expectedFinishDate = ''
  changeApplyOpen.value = true
}

function submitChangeApply() {
  changeFormRef.value?.validate(async valid => {
    if (!valid) return
    const issueId = planInfo.issueId
    if (!issueId) {
      proxy.$modal.msgError('整改方案未关联具体问题，无法提交申请')
      return
    }
    changeSubmitting.value = true
    try {
      if (changeForm.applyType === '1') {
        const days = Math.ceil((new Date(changeForm.newDeadline) - new Date(taskDeadline.value)) / 86400000)
        await applyExtension({
          taskId: props.taskId,
          issueId,
          originalDeadline: taskDeadline.value,
          newDeadline: changeForm.newDeadline,
          extensionDays: days,
          reason: changeForm.reason
        })
      } else {
        await applyLongTerm({
          taskId: props.taskId,
          issueId,
          reason: changeForm.reason,
          stageGoal: changeForm.stageGoal,
          reviewDate: changeForm.reviewDate,
          expectedFinishDate: changeForm.expectedFinishDate
        })
      }
      proxy.$modal.msgSuccess('方案变更申请已提交，等待单位负责人审批')
      changeApplyOpen.value = false
      loadLatestChange()
    } finally {
      changeSubmitting.value = false
    }
  })
}

function openChangeApproval(result) {
  approvalResult.value = result
  approvalOpinion.value = result === '1' ? '同意' : ''
  changeApproveOpen.value = true
}

async function submitChangeApproval() {
  if (approvalResult.value === '2' && !approvalOpinion.value.trim()) {
    proxy.$modal.msgWarning('请输入驳回原因')
    return
  }
  changeSubmitting.value = true
  try {
    await approveExtension({
      extensionId: latestChange.extensionId,
      status: approvalResult.value,
      opinion: approvalOpinion.value.trim()
    })
    proxy.$modal.msgSuccess(approvalResult.value === '1' ? '审批已通过' : '申请已驳回')
    changeApproveOpen.value = false
    emit('change-approved')
  } finally {
    changeSubmitting.value = false
  }
}

function loadRejectRecords() {
  request({ url: '/rectification/progress/' + props.taskId, method: 'get' }).then(res => {
    const rows = Array.isArray(res.data) ? res.data : (res.rows || res.data?.rows || res.data?.records || [])
    rejectRecords.value = rows.filter(item => isRejectProgress(item.progressType))
  }).catch(() => {
    rejectRecords.value = []
  })
}

function isRejectProgress(type) {
  return ['LEADER_REJECT', 'leader_reject', 'CLOSURE_REJECTED', 'closure_rejected', 'PLAN_REJECT', 'plan_reject', 'REJECT', 'reject'].includes(type)
}

function rejectTypeLabel(type) {
  const map = {
    LEADER_REJECT: '单位负责人驳回报告',
    leader_reject: '单位负责人驳回报告',
    CLOSURE_REJECTED: '审计处驳回销号',
    closure_rejected: '审计处驳回销号',
    PLAN_REJECT: '方案驳回',
    plan_reject: '方案驳回',
    REJECT: '驳回',
    reject: '驳回'
  }
  return map[type] || '驳回'
}

// Save as draft
async function handleSaveDraft() {
  if (!formRef.value) return
  await formRef.value.validate((valid) => {
    if (!valid) return
    draftLoading.value = true
    const postData = {
      taskId: form.taskId,
      issueId: form.taskId,
      planContent: form.planContent,
      planDeadline: form.plannedCompletionDate,
      status: '0',
      planType: '1',
      remark: form.remark
    }
    const apiCall = planInfo.id ? updatePlan({ ...postData, planId: planInfo.id }) : addPlan(postData)
    apiCall
      .then(() => {
        proxy.$modal.msgSuccess('方案草稿已保存')
        loadPlan()
      })
      .catch(() => {})
      .finally(() => {
        draftLoading.value = false
      })
  })
}

// Submit plan
async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate((valid) => {
    if (!valid) return

    // Confirm submission
    proxy.$modal
      .confirm('确认提交整改方案吗？提交后将进入审核流程。', '提交确认', {
        confirmButtonText: '确定提交',
        cancelButtonText: '取消',
        type: 'warning'
      })
      .then(() => {
        submitLoading.value = true
        const postData = {
          taskId: form.taskId,
          issueId: form.taskId,
          planContent: form.planContent,
          planDeadline: form.plannedCompletionDate,
          status: '1',
          planType: '1',
          remark: form.remark
        }
        const apiCall = planInfo.id
          ? updatePlan({ ...postData, planId: planInfo.id })
          : addPlan(postData)
        return apiCall
      })
      .then(() => {
        proxy.$modal.msgSuccess('整改方案已提交')
        loadPlan()
      })
      .catch(() => {})
      .finally(() => {
        submitLoading.value = false
      })
  })
}
</script>

<style scoped lang="scss">
.plan-editor {
  min-height: 500px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .card-title {
    font-size: 15px;
    font-weight: 600;
    color: #303133;
  }

  .header-actions {
    display: flex;
    gap: 8px;
  }

  .editor-wrapper {
    width: 100%;
    margin-bottom: 20px;

    :deep(.ql-editor) {
      min-height: 360px;
      font-size: 14px;
      line-height: 1.8;
    }

    :deep(.ql-toolbar) {
      border-radius: 4px 4px 0 0;
      border-color: #dcdfe6;
    }

    :deep(.ql-container) {
      border-radius: 0 0 4px 4px;
      border-color: #dcdfe6;
    }
  }

  .reject-alert {
    margin-bottom: 16px;

    .reject-requirement {
      margin-top: 8px;
      line-height: 1.7;
    }

    .requirement-label {
      font-weight: 600;
      color: #303133;
      margin-bottom: 4px;
    }

    .requirement-content {
      white-space: pre-wrap;
      color: #606266;
    }
  }

  .reject-records {
    margin-top: 8px;

    :deep(.el-timeline) {
      padding-left: 20px;
    }

    .record-card {
      border: 1px solid #f5c2c7;
      background: #fff8f8;
      border-radius: 4px;
      padding: 10px 12px;
    }

    .record-header {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 6px;
    }

    .record-operator {
      font-size: 12px;
      color: #909399;
    }

    .record-content {
      color: #303133;
      line-height: 1.7;
      white-space: pre-wrap;
      word-break: break-all;
    }
  }

  .plan-meta {
    margin-top: 10px;

    :deep(.el-descriptions__label) {
      color: #909399;
    }
  }

  .plan-change-panel {
    padding: 14px;
    margin-bottom: 16px;
    border: 1px solid #e3eaf4;
    border-radius: 8px;
    background: #f8fafc;
  }

  .change-panel-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
  }

  .change-panel-head span,
  .change-info-grid span {
    display: block;
    color: #8492a6;
    font-size: 12px;
  }

  .change-panel-head strong {
    display: block;
    margin-top: 3px;
    color: #26384f;
    font-size: 15px;
  }

  .change-info-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 10px 16px;
    padding-top: 14px;
  }

  .change-info-grid strong {
    display: block;
    margin-top: 3px;
    color: #34465e;
    font-size: 13px;
    font-weight: 500;
    line-height: 1.5;
    white-space: pre-wrap;
    overflow-wrap: anywhere;
  }

  .change-approve-actions {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
    padding-top: 14px;
    margin-top: 14px;
    border-top: 1px solid #e5eaf1;
  }

  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
  }

  @media (max-width: 768px) {
    :deep(.el-card__header),
    :deep(.el-card__body) {
      padding: 12px;
    }

    .card-header {
      align-items: stretch;
      flex-direction: column;
      gap: 10px;
    }

    .header-actions {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 8px;

      .el-button {
        width: 100%;
        margin-left: 0;
      }
    }

    .change-info-grid {
      grid-template-columns: 1fr;
    }

    .change-approve-actions,
    .dialog-footer {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }

    .change-approve-actions .el-button,
    .dialog-footer .el-button {
      width: 100%;
      margin: 0;
    }

    :deep(.el-form-item) {
      display: block;
      padding: 12px;
      margin-bottom: 10px;
      border: 1px solid #edf1f7;
      border-radius: 8px;
      background: #ffffff;
    }

    :deep(.el-form-item__label) {
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

    :deep(.el-form-item__content) {
      width: 100%;
      margin-left: 0 !important;
    }

    :deep(.el-input),
    :deep(.el-date-editor),
    :deep(.el-textarea) {
      width: 100% !important;
    }

    .editor-wrapper {
      margin-bottom: 0;

      :deep(.el-textarea__inner) {
        min-height: 220px !important;
      }
    }
  }
}
</style>
