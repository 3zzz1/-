<template>
  <el-dialog
    class="mobile-form-dialog material-upload-dialog"
    :title="title"
    v-model="visible"
    width="550px"
    append-to-body
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form class="mobile-dialog-form" ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="材料类型" prop="materialType">
        <el-select
          v-model="form.materialType"
          placeholder="请选择材料类型"
          style="width: 100%"
        >
          <el-option
            v-for="item in materialTypeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="文件上传" prop="files">
        <el-upload
          ref="uploadRef"
          class="material-upload"
          v-model:file-list="fileList"
          :drag="!isMobile"
          multiple
          :action="uploadUrl"
          :headers="uploadHeaders"
          :data="uploadData"
          :auto-upload="false"
          :on-change="handleFileChange"
          :on-remove="handleFileRemove"
          :on-success="handleUploadSuccess"
          :on-error="handleUploadError"
          :before-upload="beforeUpload"
          :limit="10"
          accept=".pdf,.doc,.docx,.xls,.xlsx,.jpg,.jpeg,.png,.gif,.bmp,.zip,.rar"
        >
          <template v-if="isMobile">
            <el-button native-type="button" type="primary" plain icon="Upload">
              选择佐证文件
            </el-button>
          </template>
          <template v-else>
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">
              将文件拖到此处，或<em>点击上传</em>
            </div>
          </template>
          <div v-if="isMobile" class="mobile-upload-text">
            支持多选，选中文件后点击下方“开始上传”
          </div>
          <template #tip>
            <div class="el-upload__tip">
              支持 PDF、Word、Excel、图片、压缩包等格式，单个文件不超过 20MB，最多上传 10 个文件
            </div>
          </template>
        </el-upload>
      </el-form-item>

      <!-- File list summary -->
      <div v-if="pendingFiles.length > 0" class="pending-files">
        <el-alert
          :title="`已选择 ${pendingFiles.length} 个文件待上传`"
          type="info"
          :closable="false"
          show-icon
        />
        <ul class="file-summary-list">
          <li v-for="(file, idx) in pendingFiles" :key="idx">
            <el-icon><Document /></el-icon>
            <span>{{ file.name }}</span>
            <span class="file-size">{{ formatFileSize(file.size) }}</span>
          </li>
        </ul>
      </div>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button
          type="primary"
          native-type="button"
          :loading="uploadLoading"
          @click.prevent="handleUpload"
          :disabled="pendingFiles.length === 0"
        >
          开始上传
        </el-button>
        <el-button native-type="button" @click.prevent="handleClose">取 消</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup name="MaterialUpload">
import { ref, reactive, computed, getCurrentInstance } from 'vue'
import { getToken } from '@/utils/auth'
import { uploadMaterial } from '@/api/rectification/material'
import useAppStore from '@/store/modules/app'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  issueId: {
    type: Number,
    required: true
  },
  taskId: {
    type: Number,
    required: true
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const { proxy } = getCurrentInstance()
const appStore = useAppStore()
const formRef = ref(null)
const uploadRef = ref(null)
const uploadLoading = ref(false)
const fileList = ref([])
const pendingFiles = ref([])

const title = ref('上传材料')
const isMobile = computed(() => appStore.device === 'mobile')

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const uploadUrl = computed(
  () => (import.meta.env.VITE_APP_BASE_API || '') + '/rectification/material/upload'
)

const uploadHeaders = computed(() => ({
  Authorization: 'Bearer ' + getToken()
}))

const uploadData = computed(() => ({
  issueId: props.issueId,
  taskId: props.taskId,
  materialType: form.materialType
}))

const form = reactive({
  materialType: 'other'
})

const rules = reactive({
  materialType: [{ required: true, message: '请选择材料类型', trigger: 'change' }]
})

const materialTypeOptions = ref([
  { label: '合同', value: 'contract' },
  { label: '凭证', value: 'voucher' },
  { label: '制度文件', value: 'policy' },
  { label: '照片', value: 'photo' },
  { label: '其他', value: 'other' }
])

function handleFileChange(file, fileListNew) {
  pendingFiles.value = fileListNew.filter((f) => f.status === 'ready')
}

function handleFileRemove(file, fileListNew) {
  pendingFiles.value = fileListNew.filter((f) => f.status === 'ready')
}

function beforeUpload(file) {
  const maxSize = 20 * 1024 * 1024 // 20MB
  if (file.size > maxSize) {
    proxy.$modal.msgError(`文件"${file.name}"大小超过20MB，请压缩后上传`)
    return false
  }

  const allowedExts = [
    '.pdf', '.doc', '.docx', '.xls', '.xlsx',
    '.jpg', '.jpeg', '.png', '.gif', '.bmp',
    '.zip', '.rar'
  ]
  const ext = '.' + file.name.split('.').pop().toLowerCase()
  if (!allowedExts.includes(ext)) {
    proxy.$modal.msgError(`文件"${file.name}"格式不支持，支持的格式：PDF、Word、Excel、图片、压缩包`)
    return false
  }

  return true
}

function handleUploadSuccess(response, file) {
  if (response.code === 200) {
    proxy.$modal.msgSuccess(`文件"${file.name}"上传成功`)
  } else {
    proxy.$modal.msgError(response.msg || `文件"${file.name}"上传失败`)
  }
}

function handleUploadError(error, file) {
  proxy.$modal.msgError(`文件"${file.name}"上传失败：${error.message || '网络错误'}`)
}

async function handleUpload() {
  if (!formRef.value) return

  await formRef.value.validate((valid) => {
    if (!valid) return

    if (pendingFiles.value.length === 0) {
      proxy.$modal.msgWarning('请先选择要上传的文件')
      return
    }

    proxy.$modal
      .confirm(
        `确认上传 ${pendingFiles.value.length} 个文件吗？材料类型为：${
          materialTypeOptions.value.find((d) => d.value === form.materialType)?.label || form.materialType
        }`,
        '上传确认',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'info'
        }
      )
      .then(() => {
        uploadLoading.value = true
        const promises = pendingFiles.value.map(fileWrapper => {
          const fd = new FormData()
          fd.append('issueId', props.issueId)
          fd.append('taskId', props.taskId)
          fd.append('materialType', form.materialType)
          fd.append('file', fileWrapper.raw)
          return uploadMaterial(fd)
        })
        return Promise.all(promises)
      })
      .then((results) => {
        proxy.$modal.msgSuccess(`成功上传 ${results.length} 个文件`)
        emit('success')
        handleClose()
      })
      .catch(() => {})
      .finally(() => {
        uploadLoading.value = false
      })
  })
}

function formatFileSize(size) {
  if (size == null) return '-'
  const num = Number(size)
  if (isNaN(num)) return String(size)
  if (num < 1024) return num + ' B'
  if (num < 1024 * 1024) return (num / 1024).toFixed(1) + ' KB'
  return (num / (1024 * 1024)).toFixed(1) + ' MB'
}

function handleClose() {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  form.materialType = 'other'
  fileList.value = []
  pendingFiles.value = []
  visible.value = false
}
</script>

<style scoped lang="scss">
.material-upload {
  width: 100%;

  :deep(.el-upload-dragger) {
    padding: 30px 20px;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.pending-files {
  margin-top: 16px;
}

.file-summary-list {
  list-style: none;
  padding: 0;
  margin: 8px 0 0 0;
  max-height: 160px;
  overflow-y: auto;

  li {
    display: flex;
    align-items: center;
    padding: 4px 8px;
    font-size: 13px;
    color: #606266;
    border-bottom: 1px solid #f0f0f0;

    .el-icon {
      margin-right: 6px;
      color: #409eff;
      flex-shrink: 0;
    }

    span:nth-child(2) {
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .file-size {
      margin-left: 8px;
      color: #909399;
      font-size: 12px;
      flex-shrink: 0;
    }
  }
}

.el-upload__tip {
  margin-top: 8px;
  color: #909399;
  font-size: 12px;
  line-height: 1.5;
}

.mobile-upload-text {
  margin-top: 8px;
  color: #64748b;
  font-size: 12px;
  line-height: 1.5;
}

@media (max-width: 768px) {
  .material-upload {
    :deep(.el-upload) {
      width: 100%;
    }

    :deep(.el-button) {
      width: 100%;
      min-height: 40px;
    }
  }

  .pending-files {
    margin-top: 12px;
  }

  .file-summary-list {
    max-height: 180px;

    li {
      align-items: flex-start;
      gap: 6px;
      padding: 8px;

      span:nth-child(2) {
        white-space: normal;
        word-break: break-word;
      }
    }
  }
}
</style>
