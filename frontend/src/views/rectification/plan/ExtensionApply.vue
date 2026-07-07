<template>
  <el-dialog
    :title="title"
    v-model="visible"
    width="550px"
    append-to-body
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
      <el-form-item label="原截止日期">
        <el-input v-model="originalDeadline" disabled />
      </el-form-item>

      <el-form-item label="延期天数" prop="extensionDays">
        <el-input-number
          v-model="form.extensionDays"
          :min="1"
          :max="365"
          :step="1"
          step-strictly
          placeholder="请输入延期天数"
          style="width: 100%"
          @change="onExtensionDaysChange"
        />
        <div class="form-item-tip">请输入1-365之间的整数天数</div>
      </el-form-item>

      <el-form-item label="新截止日期">
        <el-input v-model="newDeadlineText" disabled>
          <template #prefix>
            <el-icon><Calendar /></el-icon>
          </template>
        </el-input>
      </el-form-item>

      <el-form-item label="延期原因" prop="reason">
        <el-input
          v-model="form.reason"
          type="textarea"
          :rows="4"
          placeholder="请详细说明延期原因及当前的整改进展情况"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <!-- Warning alert -->
      <el-alert
        title="提示"
        type="warning"
        :closable="false"
        show-icon
        class="mb10"
      >
        <template #default>
          <div class="alert-text">
            延期申请提交后需经审批方可生效，请合理评估延期时长并如实说明延期原因。
          </div>
        </template>
      </el-alert>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          提交申请
        </el-button>
        <el-button @click="handleClose">取 消</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup name="ExtensionApply">
import { ref, reactive, computed, watch, getCurrentInstance } from 'vue'
import { applyExtension } from '@/api/rectification/plan'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  taskId: {
    type: Number,
    required: true
  },
  issueId: {
    type: Number,
    required: true
  },
  deadline: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const { proxy } = getCurrentInstance()
const formRef = ref(null)
const submitLoading = ref(false)

const title = ref('申请延期')

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const originalDeadline = computed(() => {
  return props.deadline || '-'
})

const form = reactive({
  taskId: props.taskId,
  issueId: props.issueId,
  extensionDays: undefined,
  newDeadline: '',
  reason: ''
})

const rules = reactive({
  extensionDays: [
    { required: true, message: '请输入延期天数', trigger: 'blur' },
    {
      type: 'number',
      min: 1,
      max: 365,
      message: '延期天数范围为1-365天',
      trigger: 'blur'
    }
  ],
  reason: [
    { required: true, message: '请输入延期原因', trigger: 'blur' },
    { min: 10, message: '延期原因不能少于10个字', trigger: 'blur' }
  ]
})

const newDeadlineText = computed(() => {
  if (!form.extensionDays || !props.deadline) return '-'
  const baseDate = new Date(props.deadline)
  if (isNaN(baseDate.getTime())) return '-'
  const newDate = new Date(baseDate.getTime() + form.extensionDays * 24 * 60 * 60 * 1000)
  const y = newDate.getFullYear()
  const m = String(newDate.getMonth() + 1).padStart(2, '0')
  const d = String(newDate.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
})

// Watch to sync newDeadline
watch(newDeadlineText, (val) => {
  form.newDeadline = val !== '-' ? val : ''
})

// Watch taskId/issueId changes
watch(
  () => [props.taskId, props.deadline],
  () => {
    form.taskId = props.taskId
    form.issueId = props.issueId
    form.extensionDays = undefined
    form.newDeadline = ''
    form.reason = ''
  }
)

function onExtensionDaysChange() {
  // Trigger reactive update via computed
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate((valid) => {
    if (!valid) return

    proxy.$modal
      .confirm(
        `确认申请延期 ${form.extensionDays} 天（至 ${newDeadlineText.value}）？`,
        '延期确认',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
      .then(() => {
        submitLoading.value = true
        const postData = {
          taskId: form.taskId,
          issueId: form.issueId,
          extensionDays: form.extensionDays,
          newDeadline: form.newDeadline,
          reason: form.reason
        }
        return applyExtension(postData)
      })
      .then(() => {
        proxy.$modal.msgSuccess('延期申请已提交')
        emit('success')
        handleClose()
      })
      .catch(() => {})
      .finally(() => {
        submitLoading.value = false
      })
  })
}

function handleClose() {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  form.extensionDays = undefined
  form.newDeadline = ''
  form.reason = ''
  visible.value = false
}
</script>

<style scoped lang="scss">
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.form-item-tip {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
  margin-top: 2px;
}

.mb10 {
  margin-top: 10px;
}

.alert-text {
  font-size: 13px;
  line-height: 1.6;
}
</style>
