import request from '@/utils/request'

// 查询方案
export function getPlan(taskId) {
  return request({ url: '/rectification/plan/' + taskId, method: 'get' })
}

// 提交整改方案
export function addPlan(data) {
  return request({ url: '/rectification/plan', method: 'post', data: data })
}

// 修改方案
export function updatePlan(data) {
  return request({ url: '/rectification/plan', method: 'put', data: data })
}

// 申请延期
export function applyExtension(data) {
  return request({ url: '/rectification/plan/extension', method: 'post', data: data })
}

// 审批延期
export function approveExtension(data) {
  return request({ url: '/rectification/plan/extension/approve', method: 'put', data: data })
}

// 申请转长期整改
export function applyLongTerm(data) {
  return request({ url: '/rectification/plan/long-term', method: 'post', data: data })
}

// 查询任务最新方案变更申请
export function getLatestPlanChange(taskId) {
  return request({ url: '/rectification/plan/change/latest/' + taskId, method: 'get' })
}

// 查询当前用户待审批的方案变更
export function listPendingPlanChanges() {
  return request({ url: '/rectification/plan/change/pending', method: 'get' })
}
