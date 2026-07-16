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
            <el-icon :size="20"><Bell /></el-icon>
            <span v-if="unreadCount > 0" class="notice-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
          </div>
          <template #dropdown>
            <div class="notice-dropdown">
              <div class="notice-header">
                <div>
                  <strong>消息通知</strong>
                  <span v-if="unreadCount"> {{ unreadCount }} 条未读</span>
                </div>
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
                  <div class="notice-time">{{ item.sendTime || item.createTime || '' }}</div>
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

      <div
        v-if="appStore.device === 'mobile'"
        class="mobile-notice-trigger right-menu-item hover-effect"
        @click="openMobileNotice"
      >
        <el-icon :size="21"><Bell /></el-icon>
        <span v-if="unreadCount > 0" class="notice-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
      </div>

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

    <el-drawer
      v-model="mobileNoticeOpen"
      title="消息通知"
      direction="btt"
      size="86%"
      append-to-body
      class="mobile-notice-drawer"
      @open="loadMobileNotice"
    >
      <div class="mobile-notice-panel">
        <div class="mobile-notice-summary">
          <span>{{ unreadCount > 0 ? unreadCount + ' 条未读消息' : '暂无未读消息' }}</span>
          <el-button link type="primary" :disabled="unreadCount === 0" @click="markAllRead">
            全部已读
          </el-button>
        </div>
        <div v-if="noticeList.length" class="mobile-notice-list">
          <div
            v-for="item in noticeList"
            :key="item.notificationId"
            class="mobile-notice-item"
            :class="{ unread: item.readStatus !== '1' }"
            @click="openNotice(item)"
          >
            <div class="mobile-notice-title">{{ item.title || '消息提醒' }}</div>
            <div class="mobile-notice-content">{{ item.content || '-' }}</div>
            <div class="mobile-notice-time">{{ item.sendTime || item.createTime || '' }}</div>
          </div>
        </div>
        <el-empty v-else description="暂无消息" :image-size="86" />
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElNotification } from 'element-plus'
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
const mobileNoticeOpen = ref(false)
let noticeTimer = null
let initializedNotice = false
let latestNoticeId = null

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

function loadUnreadCount(showTip = false) {
  getUnreadCount().then(res => {
    const nextCount = Number(res.data || 0)
    unreadCount.value = nextCount

    if (!initializedNotice) {
      initializedNotice = true
      if (nextCount > 0 && showTip) {
        showUnreadTip(nextCount)
      }
      return
    }

    // 新消息的桌面提醒由 refreshNoticeState 按通知 ID 统一触发，
    // 避免同一条消息因“未读数增加”和“最新通知变化”重复弹出。
  }).catch(() => {})
}

function refreshNoticeState() {
  loadUnreadCount(true)

  listMyNotification({ pageNum: 1, pageSize: 8 }).then(res => {
    const rows = res.rows || []
    const newest = rows[0]
    noticeList.value = rows

    if (!newest || !newest.notificationId) {
      return
    }

    if (latestNoticeId === null) {
      latestNoticeId = newest.notificationId
      return
    }

    if (Number(newest.notificationId) > Number(latestNoticeId)) {
      latestNoticeId = newest.notificationId
      showLatestNoticeTip(newest)
    }
  }).catch(() => {})
}

function showUnreadTip(count) {
  ElNotification({
    title: '站内消息提醒',
    message: `你有 ${count} 条未读消息，请及时处理。`,
    type: 'info',
    duration: 5000,
    position: 'top-right'
  })
}

function showLatestNoticeTip(item) {
  ElNotification({
    title: item?.title || '新消息提醒',
    message: item?.content || '你收到一条新的站内消息，请及时处理。',
    type: 'info',
    duration: 6000,
    position: 'top-right',
    onClick: () => {
      if (item) {
        openNotice(item)
      }
    }
  })
}

function loadNoticeList(showLatestTip = false) {
  listMyNotification({ pageNum: 1, pageSize: 8 }).then(res => {
    noticeList.value = res.rows || []
    if (noticeList.value.length && noticeList.value[0].notificationId) {
      latestNoticeId = noticeList.value[0].notificationId
    }
    if (showLatestTip && noticeList.value.length) {
      showLatestNoticeTip(noticeList.value[0])
    }
  }).catch(() => {})
}

function handleNoticeVisible(visible) {
  if (visible) {
    loadNoticeList()
    loadUnreadCount()
  }
}

function openMobileNotice() {
  mobileNoticeOpen.value = true
}

function loadMobileNotice() {
  loadNoticeList()
  loadUnreadCount()
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
    mobileNoticeOpen.value = false
    const isPlanChange = String(item.title || '').includes('方案变更')
      || String(item.title || '').includes('延期')
      || String(item.title || '').includes('持续整改')
    if (isPlanChange) {
      router.push({ path: '/rectification/task-page/detail/' + taskId, query: { tab: 'plan' } }).catch(() => {})
    } else {
      router.push({ path: '/rectification/my-tasks', query: { taskId } }).catch(() => {})
    }
  } else if (item.issueId) {
    mobileNoticeOpen.value = false
    router.push('/rectification/my-tasks').catch(() => {})
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
  loadUnreadCount(true)
  loadNoticeList()
  noticeTimer = window.setInterval(refreshNoticeState, 5000)
  document.addEventListener('visibilitychange', handleVisibilityChange)
})

onBeforeUnmount(() => {
  if (noticeTimer) {
    window.clearInterval(noticeTimer)
  }
  document.removeEventListener('visibilitychange', handleVisibilityChange)
})

function handleVisibilityChange() {
  if (!document.hidden) {
    refreshNoticeState()
  }
}
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
  position: relative;
  color: #4b5563;
}

.mobile-notice-trigger {
  width: 44px;
  position: relative;
  color: #334155;
}

.notice-badge {
  position: absolute;
  top: 7px;
  right: 4px;
  min-width: 17px;
  height: 17px;
  padding: 0 5px;
  border-radius: 999px;
  background: #f56c6c;
  border: 2px solid #fff;
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  line-height: 13px;
  text-align: center;
  box-sizing: border-box;
  box-shadow: 0 2px 6px rgba(245, 108, 108, 0.32);
  pointer-events: none;
}

.notice-dropdown {
  width: 360px;
  max-height: 450px;
  overflow: hidden;
}

.notice-header {
  height: 46px;
  padding: 0 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #ebeef5;

  strong {
    color: #303133;
    font-size: 14px;
  }

  span {
    color: #909399;
    font-size: 12px;
  }
}

.notice-list {
  max-height: 370px;
  overflow-y: auto;
}

.notice-item {
  padding: 12px 14px;
  border-bottom: 1px solid #f0f2f5;
  cursor: pointer;
  transition: background 0.18s ease;

  &:hover {
    background: #f7f9fc;
  }

  &.unread {
    background: #f8fbff;
  }

  &.unread .notice-title::before {
    content: '';
    display: inline-block;
    width: 7px;
    height: 7px;
    margin-right: 7px;
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
  margin-top: 5px;
  color: #606266;
  font-size: 12px;
  line-height: 1.6;
  word-break: break-all;
}

.notice-time {
  margin-top: 7px;
  color: #a8abb2;
  font-size: 12px;
}

:global(.mobile-notice-drawer) {
  border-radius: 14px 14px 0 0;
}

:global(.mobile-notice-drawer .el-drawer__header) {
  margin-bottom: 0;
  padding: 16px 16px 10px;
  color: #111827;
  font-weight: 700;
}

:global(.mobile-notice-drawer .el-drawer__body) {
  padding: 0 12px 16px;
  background: #f8fafc;
  overflow: hidden;
}

.mobile-notice-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.mobile-notice-summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
  padding: 8px 4px 12px;
  color: #64748b;
  font-size: 13px;
}

.mobile-notice-list {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding-bottom: 8px;
}

.mobile-notice-item {
  position: relative;
  padding: 13px 13px 12px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #ffffff;
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.05);

  &.unread {
    border-color: #bfdbfe;
    background: #f8fbff;
  }

  &.unread::before {
    content: '';
    position: absolute;
    top: 16px;
    right: 14px;
    width: 8px;
    height: 8px;
    border-radius: 50%;
    background: #ef4444;
  }
}

.mobile-notice-title {
  padding-right: 18px;
  color: #111827;
  font-size: 15px;
  font-weight: 700;
  line-height: 1.45;
}

.mobile-notice-content {
  margin-top: 6px;
  color: #475569;
  font-size: 13px;
  line-height: 1.6;
  word-break: break-word;
}

.mobile-notice-time {
  margin-top: 8px;
  color: #94a3b8;
  font-size: 12px;
}

@media (max-width: 768px) {
  .navbar .right-menu .avatar-container {
    margin-right: 12px;
  }

  .navbar .right-menu .avatar-container .avatar-wrapper .user-avatar {
    width: 34px;
    height: 34px;
    border-radius: 8px;
  }
}
</style>
