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

      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px" size="default">
        <el-form-item label="方案内容" prop="planContent">
          <div class="editor-wrapper">
            <QuillEditor
              ref="quillRef"
              v-model:content="form.planContent"
              contentType="html"
              theme="snow"
              :toolbar="toolbarOptions"
              :modules="editorModules"
              placeholder="请输入整改方案详细内容，包括整改措施、责任人、时间节点等..."
              style="height: 420px"
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

        <el-form-item label="整改责任人" prop="responsiblePerson">
          <el-input
            v-model="form.responsiblePerson"
            placeholder="请输入整改责任人"
            style="width: 300px"
            maxlength="50"
            show-word-limit
          />
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

      <!-- 方案状态信息 -->
      <div v-if="planInfo.updateTime" class="plan-meta">
        <el-divider />
        <el-descriptions :column="3" size="small">
          <el-descriptions-item label="创建人">{{ planInfo.createBy || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ planInfo.createTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="最后更新">{{ planInfo.updateTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="方案状态">
            <el-tag v-if="planInfo.status === 'draft'" type="info" size="small">草稿</el-tag>
            <el-tag v-else-if="planInfo.status === 'submitted'" type="warning" size="small">已提交</el-tag>
            <el-tag v-else-if="planInfo.status === 'approved'" type="success" size="small">已通过</el-tag>
            <el-tag v-else-if="planInfo.status === 'rejected'" type="danger" size="small">已驳回</el-tag>
            <span v-else>未提交</span>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>
  </div>
</template>

<script setup name="PlanEditor">
import { ref, reactive, onMounted, watch, getCurrentInstance } from 'vue'
import { getPlan, addPlan, updatePlan } from '@/api/rectification/plan'
import { QuillEditor } from '@vueup/vue-quill'
import '@vueup/vue-quill/dist/vue-quill.snow.css'

const props = defineProps({
  taskId: {
    type: Number,
    required: true
  }
})

const { proxy } = getCurrentInstance()
const formRef = ref(null)
const quillRef = ref(null)
const loading = ref(false)
const submitLoading = ref(false)
const draftLoading = ref(false)

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
  getPlan(props.taskId)
    .then((response) => {
      const data = response.data
      if (data) {
        form.planContent = data.planContent || data.content || ''
        form.plannedCompletionDate = data.plannedCompletionDate || data.completionDate || undefined
        form.responsiblePerson = data.responsiblePerson || data.rectifyPerson || ''
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

// Save as draft
async function handleSaveDraft() {
  if (!formRef.value) return
  await formRef.value.validate((valid) => {
    if (!valid) return
    draftLoading.value = true
    const postData = {
      taskId: form.taskId,
      planContent: form.planContent,
      plannedCompletionDate: form.plannedCompletionDate,
      responsiblePerson: form.responsiblePerson,
      remark: form.remark,
      status: 'draft'
    }
    const apiCall = planInfo.id ? updatePlan({ ...postData, id: planInfo.id }) : addPlan(postData)
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
          planContent: form.planContent,
          plannedCompletionDate: form.plannedCompletionDate,
          responsiblePerson: form.responsiblePerson,
          remark: form.remark,
          status: 'submitted'
        }
        const apiCall = planInfo.id
          ? updatePlan({ ...postData, id: planInfo.id })
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

  .plan-meta {
    margin-top: 10px;

    :deep(.el-descriptions__label) {
      color: #909399;
    }
  }
}
</style>
