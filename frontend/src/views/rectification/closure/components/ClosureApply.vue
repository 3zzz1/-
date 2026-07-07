<template>
  <el-dialog
    :title="title"
    v-model="visible"
    width="600px"
    append-to-body
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
      <el-form-item label="问题标题" prop="issueTitle">
        <el-input v-model="form.issueTitle" placeholder="问题标题" disabled />
      </el-form-item>
      <el-form-item label="整改完成情况说明" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="6"
          placeholder="请详细描述整改完成情况，包括已采取的措施、取得的成效、相关证明材料等"
          maxlength="2000"
          show-word-limit
        />
      </el-form-item>
      <el-form-item label="附件材料">
        <el-upload
          ref="uploadRef"
          :action="uploadUrl"
          :headers="uploadHeaders"
          :file-list="fileList"
          :on-success="handleUploadSuccess"
          :on-remove="handleUploadRemove"
          :before-upload="beforeUpload"
          multiple
        >
          <el-button type="primary" plain icon="Upload">上传附件</el-button>
          <template #tip>
            <div class="el-upload__tip">
              支持上传pdf、word、excel、图片等格式文件，单个文件不超过10MB
            </div>
          </template>
        </el-upload>
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">提 交</el-button>
        <el-button @click="handleClose">取 消</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup name="ClosureApply">
import { ref, reactive, watch, computed } from 'vue'
import { getToken } from '@/utils/auth'
import { applyClosure } from '@/api/rectification/closure'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  issueData: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const { proxy } = getCurrentInstance()
const formRef = ref(null)
const uploadRef = ref(null)
const submitLoading = ref(false)
const fileList = ref([])

const uploadUrl = computed(() => import.meta.env.VITE_APP_BASE_API + '/rectification/closure/upload')
const uploadHeaders = computed(() => ({ Authorization: 'Bearer ' + getToken() }))

const title = ref('申请销号')
const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const form = reactive({
  issueId: null,
  issueTitle: '',
  description: '',
  attachmentIds: []
})

const rules = {
  description: [
    { required: true, message: '请填写整改完成情况说明', trigger: 'blur' },
    { min: 10, message: '说明内容不能少于10个字', trigger: 'blur' }
  ]
}

watch(() => props.issueData, (val) => {
  if (val && val.issueId) {
    form.issueId = val.issueId
    form.issueTitle = val.issueTitle || val.title || ''
    form.description = ''
    form.attachmentIds = []
    fileList.value = []
  }
}, { immediate: true, deep: true })

function handleUploadSuccess(response, file) {
  if (response.code === 200) {
    form.attachmentIds.push(response.data?.id || response.data)
    proxy.$modal.msgSuccess('上传成功')
  } else {
    proxy.$modal.msgError(response.msg || '上传失败')
  }
}

function handleUploadRemove(file) {
  const id = file.response?.data?.id || file.response?.data
  if (id) {
    const idx = form.attachmentIds.indexOf(id)
    if (idx > -1) form.attachmentIds.splice(idx, 1)
  }
}

function beforeUpload(file) {
  const maxSize = 10 * 1024 * 1024
  if (file.size > maxSize) {
    proxy.$modal.msgError('单个文件大小不能超过10MB')
    return false
  }
  return true
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate((valid) => {
    if (!valid) return
    submitLoading.value = true
    applyClosure({
      issueId: form.issueId,
      description: form.description,
      attachmentIds: form.attachmentIds
    }).then(res => {
      proxy.$modal.msgSuccess('销号申请提交成功')
      emit('success')
      handleClose()
    }).catch(() => {}).finally(() => {
      submitLoading.value = false
    })
  })
}

function handleClose() {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  form.attachmentIds = []
  fileList.value = []
  visible.value = false
}
</script>

<style scoped lang="scss">
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.el-upload__tip {
  margin-top: 4px;
  color: #909399;
  font-size: 12px;
}
</style>
