<template>
  <el-dialog
    :title="title"
    v-model="visible"
    width="500px"
    append-to-body
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
      <el-form-item label="任务编号">
        <span class="info-text">{{ taskInfo.taskNo || '-' }}</span>
      </el-form-item>

      <el-form-item label="当前联络员">
        <span class="info-text">{{ taskInfo.contactPerson || '-' }}</span>
      </el-form-item>

      <el-form-item label="整改责任人" prop="assignUserId">
        <el-select
          v-model="form.assignUserId"
          filterable
          remote
          reserve-keyword
          placeholder="请输入姓名搜索选择责任人"
          :remote-method="searchUsers"
          :loading="userLoading"
          style="width: 100%"
        >
          <el-option
            v-for="user in userOptions"
            :key="user.value"
            :label="user.label"
            :value="user.value"
          >
            <span>{{ user.label }}</span>
            <span class="option-dept">{{ user.deptName || '' }}</span>
          </el-option>
        </el-select>
        <div class="form-item-tip">请选择具体负责该问题整改的责任人</div>
      </el-form-item>

      <el-form-item label="备注说明" prop="remark">
        <el-input
          v-model="form.remark"
          type="textarea"
          :rows="3"
          placeholder="可添加对责任人的相关说明或要求（选填）"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          确认分办
        </el-button>
        <el-button @click="handleClose">取 消</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup name="AssignDialog">
import { ref, reactive, computed, watch, getCurrentInstance } from 'vue'
import { getTask, assignTask } from '@/api/rectification/task'
import request from '@/utils/request'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  taskId: {
    type: Number,
    required: true
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const { proxy } = getCurrentInstance()
const formRef = ref(null)
const submitLoading = ref(false)
const userLoading = ref(false)
const userOptions = ref([])

const title = ref('分办责任人')

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const taskInfo = reactive({
  taskNo: '',
  contactPerson: ''
})

const form = reactive({
  taskId: props.taskId,
  assignUserId: undefined,
  remark: ''
})

const rules = reactive({
  assignUserId: [
    { required: true, message: '请选择整改责任人', trigger: 'change' }
  ]
})

// Watch dialog open to load task info
watch(
  () => props.modelValue,
  (val) => {
    if (val && props.taskId) {
      form.taskId = props.taskId
      loadTaskBrief()
    }
  }
)

function loadTaskBrief() {
  getTask(props.taskId)
    .then((response) => {
      const data = response.data
      if (data) {
        taskInfo.taskNo = data.taskNo || ''
        taskInfo.contactPerson = data.contactPerson || ''
      }
    })
    .catch(() => {})
}

/** Remote search for users */
function searchUsers(query) {
  if (!query || query.trim() === '') {
    userOptions.value = []
    return
  }
  userLoading.value = true
  request({
    url: '/system/user/list',
    method: 'get',
    params: { userName: query, pageNum: 1, pageSize: 20 }
  })
    .then((response) => {
      const rows = response.rows || response.data || []
      userOptions.value = rows.map((u) => ({
        value: u.userId,
        label: u.nickName || u.userName,
        deptName: u.dept?.deptName || u.deptName || ''
      }))
    })
    .catch(() => {
      userOptions.value = []
    })
    .finally(() => {
      userLoading.value = false
    })
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate((valid) => {
    if (!valid) return

    const selectedUser = userOptions.value.find((u) => u.value === form.assignUserId)
    const userName = selectedUser ? selectedUser.label : form.assignUserId

    proxy.$modal
      .confirm(
        `确认将整改任务分办给责任人"${userName}"吗？`,
        '分办确认',
        {
          confirmButtonText: '确定分办',
          cancelButtonText: '取消',
          type: 'info'
        }
      )
      .then(() => {
        submitLoading.value = true
        return assignTask(form.taskId, form.assignUserId)
      })
      .then(() => {
        proxy.$modal.msgSuccess('已分办给责任人')
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
  form.assignUserId = undefined
  form.remark = ''
  userOptions.value = []
  visible.value = false
}
</script>

<style scoped lang="scss">
.info-text {
  color: #606266;
  font-size: 14px;
}

.option-dept {
  float: right;
  color: #909399;
  font-size: 12px;
}

.form-item-tip {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
  margin-top: 2px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
