<template>
  <el-dialog
    v-model="visible"
    title="申请销号"
    width="650px"
    append-to-body
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
      <el-form-item label="问题标题">
        <el-input v-model="form.issueTitle" disabled />
      </el-form-item>

      <el-form-item label="整改报告">
        <el-button type="primary" plain icon="Download" :disabled="!form.taskId" @click="downloadWordReport">
          下载Word整改报告
        </el-button>
      </el-form-item>

      <el-form-item label="附件材料">
        <div class="closure-files">
          <el-button
            v-for="file in materialList"
            :key="file.materialId || file.id"
            type="primary"
            plain
            size="small"
            icon="Download"
            @click="downloadMaterialFile(file)"
          >
            {{ file.fileName || '附件' }}
          </el-button>
          <span v-if="materialList.length === 0" class="text-muted">
            暂无佐证附件，请先上传整改材料
          </span>
        </div>
      </el-form-item>

      <el-form-item label="完成情况说明" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="6"
          placeholder="请填写整改完成情况，包括已采取的措施、取得的成效、相关证明材料等"
          maxlength="2000"
          show-word-limit
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">提交</el-button>
        <el-button @click="handleClose">取消</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup name="ClosureApply">
import { ref, reactive, watch, computed, getCurrentInstance } from 'vue'
import { saveAs } from 'file-saver'
import { applyClosure } from '@/api/rectification/closure'
import { downloadReportWord } from '@/api/rectification/report'
import { listMaterial, downloadMaterial } from '@/api/rectification/material'
import useUserStore from '@/store/modules/user'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  issueData: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:modelValue', 'success'])
const { proxy } = getCurrentInstance()
const userStore = useUserStore()

const formRef = ref(null)
const submitLoading = ref(false)
const materialList = ref([])

const visible = computed({
  get: () => props.modelValue,
  set: val => emit('update:modelValue', val)
})

const form = reactive({
  issueId: null,
  taskId: null,
  issueTitle: '',
  description: ''
})

const rules = {
  description: [
    { required: true, message: '请填写整改完成情况说明', trigger: 'blur' }
  ]
}

watch(() => props.issueData, val => {
  if (!val) return
  form.issueId = val.issueId || null
  form.taskId = val.taskId || null
  form.issueTitle = val.issueTitle || val.title || ''
  form.description = ''
  loadMaterials()
}, { immediate: true, deep: true })

function loadMaterials() {
  if (!form.issueId) {
    materialList.value = []
    return
  }
  listMaterial(form.issueId).then(res => {
    materialList.value = res.rows || []
  }).catch(() => {
    materialList.value = []
  })
}

function downloadWordReport() {
  if (!form.taskId) return
  downloadReportWord(form.taskId).then(response => {
    saveBlob(response.data, closureFallbackReportName())
  })
}

function downloadMaterialFile(file) {
  const materialId = file.materialId || file.id
  if (!materialId) {
    proxy.$modal.msgWarning('材料ID不可用')
    return
  }
  downloadMaterial(materialId).then(blob => {
    saveBlob(blob, file.fileName || ('material_' + materialId))
  })
}

async function saveBlob(blob, filename) {
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
  saveAs(new Blob([blob]), filename)
}

function reportFileName(response, fallback) {
  const disposition = response && response.headers ? response.headers['content-disposition'] : ''
  const utf8Match = disposition && disposition.match(/filename\*=UTF-8''([^;]+)/i)
  if (utf8Match) {
    const decoded = decodeURIComponent(utf8Match[1])
    return isGenericReportFileName(decoded) ? fallback : decoded
  }
  const nameMatch = disposition && disposition.match(/filename="?([^"]+)"?/i)
  if (nameMatch) {
    const decoded = decodeURIComponent(nameMatch[1])
    return isGenericReportFileName(decoded) ? fallback : decoded
  }
  return fallback
}

function isGenericReportFileName(filename) {
  return /^report_\d+\.docx$/i.test(filename || '') || /^rectification_report_\d+\.docx$/i.test(filename || '')
}

function closureFallbackReportName() {
  return (userStore.name || '登录用户') + '整改报告.docx'
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(valid => {
    if (!valid) return

    submitLoading.value = true
    const materialNames = materialList.value.map(m => m.fileName).join('、') || '无'
    const reportName = form.taskId ? closureFallbackReportName() : '无'

    applyClosure({
      issueId: form.issueId,
      taskId: form.taskId,
      applyContent: form.description + '\n\n报告文件：' + reportName + '\n附件材料：' + materialNames
    }).then(() => {
      proxy.$modal.msgSuccess('销号申请提交成功')
      emit('success')
      handleClose()
    }).finally(() => {
      submitLoading.value = false
    })
  })
}

function handleClose() {
  formRef.value?.resetFields()
  materialList.value = []
  visible.value = false
}
</script>

<style scoped lang="scss">
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.closure-files {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.text-muted {
  color: #c0c4cc;
}
</style>
