import request from '@/utils/request'

// 我的消息列表
export function listMyNotification(query) {
  return request({ url: '/rectification/notification/my-list', method: 'get', params: query })
}

// 标记已读
export function markAsRead(ids) {
  return request({ url: '/rectification/notification/read/' + ids, method: 'put' })
}

// 未读消息数
export function getUnreadCount() {
  return request({ url: '/rectification/notification/unread-count', method: 'get' })
}
