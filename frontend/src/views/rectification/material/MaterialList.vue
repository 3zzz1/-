<template>
  <div class="material-list">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">佐证材料列表</span>
          <div class="header-actions">
            <el-button type="primary" icon="Refresh" @click="loadList" :loading="loading">
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="materialList"
        size="default"
        stripe
        empty-text="暂无材料，请上传佐证材料"
      >
        <el-table-column label="序号" type="index" width="60" align="center" />

        <el-table-column
          label="文件名"
          prop="fileName"
          min-width="200"
          :show-overflow-tooltip="true"
        >
          <template #default="scope">
            <div class="file-name-cell">
              <el-icon :size="16" style="margin-right: 6px; color: #409eff">
                <component :is="getFileIcon(scope.row.fileType, scope.row.fileName)" />
              </el-icon>
              <el-link type="primary" :underline="false" @click="handlePreview(scope.row)">
                {{ scope.row.fileName }}
              </el-link>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="材料类型" prop="materialType" width="120" align="center">
          <template #default="scope">
            <el-tag :type="materialTypeTag(scope.row.materialType)" size="small">
              {{ materialTypeLabel(scope.row.materialType) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="文件大小" prop="fileSize" width="110" align="center">
          <template #default="scope">
            <span>{{ formatFileSize(scope.row.fileSize) }}</span>
          </template>
        </el-table-column>

        <el-table-column label="上传时间" prop="createTime" width="170" align="center">
          <template #default="scope">
            <span>{{ scope.row.createTime || scope.row.uploadTime || '-' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="上传人" prop="createBy" width="110" align="center">
          <template #default="scope">
            <span>{{ scope.row.createBy || scope.row.uploadBy || '-' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" align="center" fixed="right">
          <template #default="scope">
            <el-button
              link
              type="primary"
              icon="Download"
              size="small"
              @click="handleDownload(scope.row)"
              v-hasPermi="['rectification:material:download']"
            >
              下载
            </el-button>
            <el-button
              link
              type="primary"
              icon="View"
              size="small"
              @click="handlePreview(scope.row)"
            >
              预览
            </el-button>
            <el-button
              link
              type="danger"
              icon="Delete"
              size="small"
              @click="handleDelete(scope.row)"
              v-hasPermi="['rectification:material:remove']"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div class="pagination-wrapper" v-if="total > 0">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          :total="total"
          @size-change="loadList"
          @current-change="loadList"
          small
        />
      </div>
    </el-card>

    <!-- Preview Dialog -->
    <el-dialog
      v-model="previewVisible"
      title="文件预览"
      width="800px"
      append-to-body
      :close-on-click-modal="true"
    >
      <div class="preview-container">
        <div v-if="previewType === 'image'" class="image-preview">
          <el-image
            :src="previewUrl"
            fit="contain"
            style="max-width: 100%; max-height: 500px"
          />
        </div>
        <div v-else class="file-info-preview">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="文件名">{{ previewFile.fileName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="材料类型">
              <el-tag :type="materialTypeTag(previewFile.materialType)" size="small">
                {{ materialTypeLabel(previewFile.materialType) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="文件大小">
              {{ formatFileSize(previewFile.fileSize) }}
            </el-descriptions-item>
            <el-descriptions-item label="上传时间">
              {{ previewFile.createTime || previewFile.uploadTime || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="上传人">
              {{ previewFile.createBy || previewFile.uploadBy || '-' }}
            </el-descriptions-item>
          </el-descriptions>
          <div class="preview-actions">
            <el-alert
              title="此文件类型不支持在线预览"
              type="info"
              :closable="false"
              show-icon
              class="mt15"
            />
            <el-button type="primary" @click="handleDownload(previewFile)" class="mt15">
              <el-icon><Download /></el-icon>
              下载文件查看
            </el-button>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="previewVisible = false">关 闭</el-button>
          <el-button type="primary" @click="handleDownload(previewFile)">
            <el-icon><Download /></el-icon>
            下载文件
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="MaterialList">
import { ref, reactive, onMounted, watch, getCurrentInstance } from 'vue'
import { saveAs } from 'file-saver'
import { listMaterial, delMaterial, downloadMaterial } from '@/api/rectification/material'

const props = defineProps({
  issueId: {
    type: Number,
    required: true
  }
})

const { proxy } = getCurrentInstance()
const loading = ref(false)
const materialList = ref([])
const total = ref(0)

// Preview
const previewVisible = ref(false)
const previewType = ref('')
const previewUrl = ref('')
const previewFile = ref({})

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10
})

const materialTypeOptions = ref([
  { label: '合同', value: 'contract' },
  { label: '凭证', value: 'voucher' },
  { label: '制度文件', value: 'policy' },
  { label: '照片', value: 'photo' },
  { label: '其他', value: 'other' }
])

// Watch issueId change
watch(
  () => props.issueId,
  () => {
    queryParams.pageNum = 1
    loadList()
  }
)

onMounted(() => {
  loadList()
})

/** Load material list */
function loadList() {
  if (!props.issueId) return
  loading.value = true
  listMaterial(props.issueId)
    .then((response) => {
      const data = response.data || response
      if (Array.isArray(data)) {
        materialList.value = data
        total.value = data.length
      } else if (data.rows) {
        materialList.value = data.rows
        total.value = data.total || 0
      } else {
        materialList.value = []
        total.value = 0
      }
    })
    .catch(() => {
      materialList.value = []
      total.value = 0
    })
    .finally(() => {
      loading.value = false
    })
}

/** Material type tag color */
function materialTypeTag(val) {
  const map = {
    contract: 'primary',
    voucher: 'success',
    policy: 'warning',
    photo: 'info',
    other: ''
  }
  return map[val] || ''
}

function materialTypeLabel(val) {
  const item = materialTypeOptions.value.find((d) => d.value === val)
  return item ? item.label : val || '-'
}

/** Format file size */
function formatFileSize(size) {
  if (size == null) return '-'
  const num = Number(size)
  if (isNaN(num)) return String(size)
  if (num < 1024) return num + ' B'
  if (num < 1024 * 1024) return (num / 1024).toFixed(2) + ' KB'
  if (num < 1024 * 1024 * 1024) return (num / (1024 * 1024)).toFixed(2) + ' MB'
  return (num / (1024 * 1024 * 1024)).toFixed(2) + ' GB'
}

/** Get file icon by file extension */
function getFileIcon(fileType, fileName) {
  const name = (fileName || '').toLowerCase()
  const type = (fileType || '').toLowerCase()
  if (type === 'photo' || /\.(png|jpg|jpeg|gif|bmp|webp|svg)$/.test(name)) return 'Picture'
  if (/\.(doc|docx|docm|dot)$/.test(name)) return 'Document'
  if (/\.(xls|xlsx|xlsm|csv)$/.test(name)) return 'Grid'
  if (/\.(pdf)$/.test(name)) return 'Files'
  if (/\.(zip|rar|7z|gz|tar)$/.test(name)) return 'FolderOpened'
  return 'Document'
}

/** Preview material */
function handlePreview(row) {
  previewFile.value = row
  const fileName = (row.fileName || '').toLowerCase()

  if (/\.(png|jpg|jpeg|gif|bmp|webp|svg)$/.test(fileName)) {
    previewType.value = 'image'
    previewUrl.value = ''
    const materialId = row.id || row.materialId
    if (!materialId) {
      proxy.$modal.msgWarning('材料ID不可用')
      return
    }
    downloadMaterial(materialId).then(blob => {
      if (previewUrl.value) {
        window.URL.revokeObjectURL(previewUrl.value)
      }
      previewUrl.value = window.URL.createObjectURL(new Blob([blob], { type: imageMimeType(fileName) }))
    })
  } else {
    previewType.value = 'file'
    previewUrl.value = ''
  }
  previewVisible.value = true
}

/** Download material */
function handleDownload(row) {
  const materialId = row.id || row.materialId
  if (!materialId) {
    proxy.$modal.msgWarning('材料ID不可用')
    return
  }
  downloadMaterial(materialId)
    .then((blob) => {
      saveFileBlob(blob, row.fileName || `material_${materialId}`)
    })
    .catch(() => {})
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

function imageMimeType(fileName) {
  const name = (fileName || '').toLowerCase()
  if (name.endsWith('.png')) return 'image/png'
  if (name.endsWith('.jpg') || name.endsWith('.jpeg')) return 'image/jpeg'
  if (name.endsWith('.gif')) return 'image/gif'
  if (name.endsWith('.bmp')) return 'image/bmp'
  if (name.endsWith('.webp')) return 'image/webp'
  if (name.endsWith('.svg')) return 'image/svg+xml'
  return 'image/*'
}

/** Delete material */
function handleDelete(row) {
  const materialId = row.id || row.materialId
  if (!materialId) {
    proxy.$modal.msgWarning('材料ID不可用')
    return
  }
  proxy.$modal
    .confirm(`确认删除材料"${row.fileName}"吗？删除后不可恢复。`, '删除确认', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    .then(() => {
      return delMaterial(materialId)
    })
    .then(() => {
      proxy.$modal.msgSuccess('材料已删除')
      loadList()
    })
    .catch(() => {})
}
</script>

<style scoped lang="scss">
.material-list {
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

  .file-name-cell {
    display: flex;
    align-items: center;
  }

  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 16px;
  }

  .preview-container {
    min-height: 200px;
  }

  .image-preview {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 300px;
    background: #f5f7fa;
    border-radius: 4px;
    padding: 20px;
  }

  .file-info-preview {
    .preview-actions {
      text-align: center;
    }
  }

  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 8px;
  }

  .mt15 {
    margin-top: 15px;
  }
}
</style>
