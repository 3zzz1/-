<template>
  <div class="navbar">
    <hamburger id="hamburger-container" :is-active="appStore.sidebar.opened" class="hamburger-container" @toggleClick="toggleSideBar" />
    <breadcrumb id="breadcrumb-container" class="breadcrumb-container" v-if="!settingsStore.topNav" />
    <top-nav id="topmenu-container" class="topmenu-container" v-if="settingsStore.topNav" />

    <div class="right-menu">
      <template v-if="appStore.device !== 'mobile'">
        <header-search id="header-search" class="right-menu-item" />

        <el-dropdown trigger="click" placement="bottom-end" @visible-change="handleNoticeVisible">
          <div class="notice-trigger right-menu-item hover-effect">
            <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
              <el-icon :size="19"><Bell /></el-icon>
            </el-badge>
          </div>
          <template #dropdown>
            <div class="notice-dropdown">
              <div class="notice-header">
                <strong>消息通知</strong>
                <el-button link type="primary" size="small" :disabled="unreadCount === 0" @click.stop="markAllRead">
                  全部已读
                </el-button>
              </div>
              <div v-if="noticeList.length" class="notice-list">
                <div
                  v-for="item in noticeList"
                  :key="item.notificationId"
                  class="notice-item"
                  :class="{ unread: item.readStatus !== '1' }"
                  @click="openNotice(item)"
                >
                  <div class="notice-title">{{ item.title || '消息提醒' }}</div>
                  <div class="notice-content">{{ item.content || '-' }}</div>
                  <div class="notice-time">{{ item.createTime || item.sendTime || '' }}</div>
                </div>
              </div>
              <el-empty v-else description="暂无消息" :image-size="72" />
            </div>
          </template>
        </el-dropdown>

        <screenfull id="screenfull" class="right-menu-item hover-effect" />

        <el-tooltip content="布局大小" effect="dark" placement="bottom">
          <size-select id="size-select" class="right-menu-item hover-effect" />
        </el-tooltip>
      </template>

      <div class="avatar-container">
        <el-dropdown @command="handleCommand" class="right-menu-item hover-effect" trigger="click">
          <div class="avatar-wrapper">
            <img :src="userStore.avatar" class="user-avatar" />
            <el-icon><caret-bottom /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <router-link to="/user/profile">
                <el-dropdown-item>个人中心</el-dropdown-item>
              </router-link>
              <el-dropdown-item command="setLayout" v-if="settingsStore.showSettings">
                <span>布局设置</span>
              </el-dropdown-item>
              <el-dropdown-item divided command="logout">
                <span>退出登录</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import Breadcrumb from '@/components/Breadcrumb'
import TopNav from '@/components/TopNav'
import Hamburger from '@/components/Hamburger'
import Screenfull from '@/components/Screenfull'
import SizeSelect from '@/components/SizeSelect'
import HeaderSearch from '@/components/HeaderSearch'
import useAppStore from '@/store/modules/app'
import useUserStore from '@/store/modules/user'
import useSettingsStore from '@/store/modules/settings'
import { getUnreadCount, listMyNotification, markAsRead } from '@/api/rectification/notification'

const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()
const settingsStore = useSettingsStore()
const unreadCount = ref(0)
const noticeList = ref([])
let noticeTimer = null

function toggleSideBar() {
  appStore.toggleSideBar()
}

function handleCommand(command) {
  switch (command) {
    case 'setLayout':
      setLayout()
      break
    case 'logout':
      logout()
      break
    default:
      break
  }
}

function logout() {
  ElMessageBox.confirm('确定注销并退出系统吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logOut().then(() => {
      location.href = '/index'
    })
  }).catch(() => {})
}

function loadUnreadCount() {
  getUnreadCount().then(res => {
    unreadCount.value = Number(res.data || 0)
  }).catch(() => {})
}

function loadNoticeList() {
  listMyNotification({ pageNum: 1, pageSize: 8 }).then(res => {
    noticeList.value = res.rows || []
  }).catch(() => {})
}

function handleNoticeVisible(visible) {
  if (visible) {
    loadNoticeList()
    loadUnreadCount()
  }
}

function openNotice(item) {
  const id = item.notificationId
  const taskId = item.taskId
  if (id && item.readStatus !== '1') {
    markAsRead(id).then(() => {
      item.readStatus = '1'
      loadUnreadCount()
    }).catch(() => {})
  }
  if (taskId) {
    router.push('/rectification/task-page/detail/' + taskId)
  }
}

function markAllRead() {
  const ids = noticeList.value
    .filter(item => item.readStatus !== '1')
    .map(item => item.notificationId)
    .filter(Boolean)
  if (!ids.length) return
  markAsRead(ids.join(',')).then(() => {
    noticeList.value.forEach(item => {
      item.readStatus = '1'
    })
    unreadCount.value = 0
  }).catch(() => {})
}

const emits = defineEmits(['setLayout'])
function setLayout() {
  emits('setLayout')
}

onMounted(() => {
  loadUnreadCount()
  noticeTimer = window.setInterval(loadUnreadCount, 60000)
})

onBeforeUnmount(() => {
  if (noticeTimer) {
    window.clearInterval(noticeTimer)
  }
})
</script>

<style lang="scss" scoped>
.navbar {
  height: 50px;
  overflow: hidden;
  position: relative;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);

  .hamburger-container {
    line-height: 46px;
    height: 100%;
    float: left;
    cursor: pointer;
    transition: background 0.3s;
    -webkit-tap-highlight-color: transparent;

    &:hover {
      background: rgba(0, 0, 0, 0.025);
    }
  }

  .breadcrumb-container {
    float: left;
  }

  .topmenu-container {
    position: absolute;
    left: 50px;
  }

  .right-menu {
    float: right;
    height: 100%;
    line-height: 50px;
    display: flex;

    &:focus {
      outline: none;
    }

    .right-menu-item {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      padding: 0 8px;
      height: 100%;
      font-size: 18px;
      color: #5a5e66;
      vertical-align: text-bottom;

      &.hover-effect {
        cursor: pointer;
        transition: background 0.3s;

        &:hover {
          background: rgba(0, 0, 0, 0.025);
        }
      }
    }

    .avatar-container {
      margin-right: 40px;

      .avatar-wrapper {
        margin-top: 5px;
        position: relative;

        .user-avatar {
          cursor: pointer;
          width: 40px;
          height: 40px;
          border-radius: 10px;
        }

        i {
          cursor: pointer;
          position: absolute;
          right: -20px;
          top: 25px;
          font-size: 12px;
        }
      }
    }
  }
}

.notice-trigger {
  width: 42px;
}

.notice-dropdown {
  width: 340px;
  max-height: 430px;
  overflow: hidden;
}

.notice-header {
  height: 42px;
  padding: 0 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #ebeef5;
}

.notice-list {
  max-height: 360px;
  overflow-y: auto;
}

.notice-item {
  padding: 10px 12px;
  border-bottom: 1px solid #f0f2f5;
  cursor: pointer;

  &:hover {
    background: #f7f9fc;
  }

  &.unread .notice-title::before {
    content: '';
    display: inline-block;
    width: 6px;
    height: 6px;
    margin-right: 6px;
    border-radius: 50%;
    background: #f56c6c;
    vertical-align: middle;
  }
}

.notice-title {
  color: #303133;
  font-size: 14px;
  font-weight: 600;
  line-height: 1.5;
}

.notice-content {
  margin-top: 4px;
  color: #606266;
  font-size: 12px;
  line-height: 1.5;
  word-break: break-all;
}

.notice-time {
  margin-top: 6px;
  color: #a8abb2;
  font-size: 12px;
}
</style>
