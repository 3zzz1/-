import request from '@/utils/request'

// 查询销号申请列表
export function listClosure(query) {
  return request({ url: '/rectification/closure/list', method: 'get', params: query })
}

// 查询销号详情
export function getClosure(closureId) {
  return request({ url: '/rectification/closure/' + closureId, method: 'get' })
}

// Query the display name used in the closure workflow
export function getClosureUserName(userId) {
  return request({ url: '/rectification/closure/user-name/' + userId, method: 'get' })
}

// 申请销号
export function applyClosure(data) {
  return request({ url: '/rectification/closure/apply', method: 'post', data: data })
}

// 审计处审核销号
export function auditClosure(data) {
  return request({ url: '/rectification/closure/audit', method: 'put', data: data })
}
