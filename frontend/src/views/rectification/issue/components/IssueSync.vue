<template>
  <el-dialog
    :title="title"
    v-model="visible"
    width="600px"
    append-to-body
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-alert
      title="仅可选择已结项的审计项目同步，已同步过的问题不会重复导入"
      type="info"
      :closable="false"
      show-icon
      class="mb15"
    />

    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="选择项目" prop="projectId">
        <el-select
          v-model="form.projectId"
          filterable
          remote
          reserve-keyword
          placeholder="请输入项目名称搜索"
          :remote-method="searchProjects"
          :loading="projectLoading"
          style="width: 100%"
          @change="handleProjectChange"
        >
          <el-option
            v-for="p in projectList"
            :key="p.value"
            :label="p.label"
            :value="p.value"
          >
            <span>{{ p.label }}</span>
            <span class="option-tag">
              <el-tag :type="p.projectType === 'audit' ? '' : 'warning'" size="small">
                {{ p.projectTypeLabel }}
              </el-tag>
            </span>
          </el-option>
        </el-select>
      </el-form-item>

      <!-- Selected Project Info -->
      <div v-if="selectedProject.id" class="project-info-section">
        <el-divider content-position="left">项目信息</el-divider>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="项目名称" :span="2">
            {{ selectedProject.name || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="项目编号">
            {{ selectedProject.projectNo || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="项目类型">
            {{ selectedProject.projectTypeLabel || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="审计期间">
            {{ selectedProject.auditPeriod || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="被审计单位">
            {{ selectedProject.auditeeName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="发现问题数">
            <el-tag type="primary" size="small">{{ selectedProject.issueCount || 0 }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="结项时间">
            {{ selectedProject.completionTime || selectedProject.endTime || '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- Sync Preview hint -->
      <div v-if="selectedProject.id" class="sync-hint">
        <el-alert
          :title="`系统将从该项目中同步 ${selectedProject.issueCount || 0} 个问题到整改台账，已存在的重复问题将被自动跳过。`"
          type="warning"
          :closable="false"
          show-icon
          class="mt15"
        />
      </div>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button
          type="primary"
          :loading="submitLoading"
          :disabled="!selectedProject.id"
          @click="handleSync"
        >
          开始同步
        </el-button>
        <el-button @click="handleClose">取 消</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup name="IssueSync">
import { ref, reactive, computed, watch, getCurrentInstance } from 'vue'
import { syncIssue } from '@/api/rectification/issue'
import request from '@/utils/request'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const { proxy } = getCurrentInstance()
const formRef = ref(null)
const projectLoading = ref(false)
const submitLoading = ref(false)
const projectList = ref([])

const title = ref('从审计项目同步问题')

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const form = reactive({
  projectId: null
})

const rules = reactive({
  projectId: [{ required: true, message: '请选择审计项目', trigger: 'change' }]
})

const selectedProject = reactive({
  id: null,
  name: '',
  projectNo: '',
  projectType: '',
  projectTypeLabel: '',
  auditPeriod: '',
  auditeeName: '',
  issueCount: 0,
  completionTime: '',
  endTime: ''
})

// Load projects when dialog opens
watch(
  () => props.modelValue,
  (val) => {
    if (val) {
      loadProjects()
    }
  }
)

/** Search projects */
function searchProjects(query) {
  if (!query || query.trim() === '') {
    projectList.value = []
    return
  }
  projectLoading.value = true
  request({
    url: '/rectification/issue/projects',
    method: 'get',
    params: { projectName: query, pageNum: 1, pageSize: 20, status: 'completed' }
  })
    .then((response) => {
      const rows = response.rows || response.data || []
      projectList.value = rows.map((p) => ({
        value: p.id || p.projectId,
        label: p.projectName || p.name || '',
        projectType: p.projectType || '',
        projectTypeLabel:
          p.projectType === 'audit'
            ? '审计项目'
            : p.projectType === 'inspection'
              ? '巡察项目'
              : p.projectType === 'supervision'
                ? '督查项目'
                : p.projectType || '其他',
        issueCount: p.issueCount || p.unSyncIssueCount || 0,
        completionTime: p.completionTime || ''
      }))
    })
    .catch(() => {
      projectList.value = []
    })
    .finally(() => {
      projectLoading.value = false
    })
}

function loadProjects() {
  projectLoading.value = true
  request({
    url: '/rectification/issue/projects',
    method: 'get',
    params: { status: 'completed', pageNum: 1, pageSize: 50 }
  })
    .then((response) => {
      const rows = response.rows || response.data || []
      projectList.value = rows.map((p) => ({
        value: p.id || p.projectId,
        label: p.projectName || p.name || '',
        projectType: p.projectType || '',
        projectTypeLabel:
          p.projectType === 'audit'
            ? '审计项目'
            : p.projectType === 'inspection'
              ? '巡察项目'
              : p.projectType === 'supervision'
                ? '督查项目'
                : p.projectType || '其他',
        issueCount: p.issueCount || p.unSyncIssueCount || 0,
        completionTime: p.completionTime || ''
      }))
    })
    .catch(() => {
      // Fallback mock data
      projectList.value = [
        {
          value: 1,
          label: '2024年度经济责任审计项目',
          projectType: 'audit',
          projectTypeLabel: '审计项目',
          issueCount: 12
        },
        {
          value: 2,
          label: '2024年度财务收支审计项目',
          projectType: 'audit',
          projectTypeLabel: '审计项目',
          issueCount: 8
        },
        {
          value: 3,
          label: '2024年预算执行审计项目',
          projectType: 'audit',
          projectTypeLabel: '审计项目',
          issueCount: 15
        }
      ]
    })
    .finally(() => {
      projectLoading.value = false
    })
}

/** When a project is selected, load its detail */
function handleProjectChange(projectId) {
  if (!projectId) {
    resetSelectedProject()
    return
  }

  const project = projectList.value.find((p) => p.value === projectId)
  if (project) {
    selectedProject.id = project.value
    selectedProject.name = project.label
    selectedProject.projectNo = project.projectNo || ''
    selectedProject.projectType = project.projectType || ''
    selectedProject.projectTypeLabel = project.projectTypeLabel || ''
    selectedProject.auditPeriod = project.auditPeriod || ''
    selectedProject.auditeeName = project.auditeeName || ''
    selectedProject.issueCount = project.issueCount || 0
    selectedProject.completionTime = project.completionTime || ''
  }

  // Try to load full project detail
  request({
    url: '/rectification/issue/project/' + projectId,
    method: 'get'
  })
    .then((response) => {
      const data = response.data
      if (data) {
        selectedProject.name = data.projectName || data.name || selectedProject.name
        selectedProject.projectNo = data.projectNo || selectedProject.projectNo
        selectedProject.auditPeriod = data.auditPeriod || data.period || selectedProject.auditPeriod
        selectedProject.auditeeName = data.auditeeName || data.auditee || selectedProject.auditeeName
        selectedProject.issueCount =
          data.issueCount || data.unSyncIssueCount || selectedProject.issueCount
        selectedProject.completionTime =
          data.completionTime || data.endTime || selectedProject.completionTime
      }
    })
    .catch(() => {})
}

function resetSelectedProject() {
  selectedProject.id = null
  selectedProject.name = ''
  selectedProject.projectNo = ''
  selectedProject.projectType = ''
  selectedProject.projectTypeLabel = ''
  selectedProject.auditPeriod = ''
  selectedProject.auditeeName = ''
  selectedProject.issueCount = 0
  selectedProject.completionTime = ''
}

async function handleSync() {
  if (!formRef.value) return
  await formRef.value.validate((valid) => {
    if (!valid) return

    proxy.$modal
      .confirm(
        `确认从项目"${selectedProject.name}"同步问题吗？\n预计将同步 ${selectedProject.issueCount} 个问题，已存在的重复问题将自动跳过。`,
        '同步确认',
        {
          confirmButtonText: '确定同步',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
      .then(() => {
        submitLoading.value = true
        return syncIssue(form.projectId)
      })
      .then((res) => {
        const syncCount = res.data?.count || res.data || 0
        proxy.$modal.msgSuccess(`同步完成，共导入 ${syncCount} 个问题`)
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
  form.projectId = null
  resetSelectedProject()
  projectList.value = []
  visible.value = false
}
</script>

<style scoped lang="scss">
.mb15 {
  margin-bottom: 15px;
}

.option-tag {
  float: right;
}

.project-info-section {
  margin-top: 8px;
}

.sync-hint {
  margin-bottom: 8px;
}

.mt15 {
  margin-top: 15px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
