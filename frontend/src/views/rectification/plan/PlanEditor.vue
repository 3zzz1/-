<template>
  <div class="plan-editor">
    <el-card shadow="never" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span class="card-title">整改方案</span>
          <div class="header-actions">
            <el-button type="primary" :loading="submitLoading" @click="handleSubmit">提交方案</el-button>
            <el-button :loading="draftLoading" @click="handleSaveDraft">保存草稿</el-button>
          </div>
        </div>
      </template>

      <el-alert
        v-if="closureRejectRequirement"
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

      <el-alert
        v-if="reportRejectOpinion"
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

      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px" size="default">
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

      <div v-if="rejectRecords.length > 0" class="reject-records">
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
      <div v-if="planInfo.updateTime" class="plan-meta">
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
  </div>
</template>

<script setup name="PlanEditor">
import { ref, reactive, computed, onMounted, watch, getCurrentInstance } from 'vue'
import { getPlan, addPlan, updatePlan } from '@/api/rectification/plan'
import { getReport } from '@/api/rectification/report'
import request from '@/utils/request'
const props = defineProps({
  taskId: { type: Number, required: true }
})

const { proxy } = getCurrentInstance()
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

const form = reactive({
  taskId: props.taskId,
  planContent: '',
  plannedCompletionDate: undefined,
  responsiblePerson: '',
  remark: ''
})

const planInfo = reactive({
  id: null,
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
}
</style>
