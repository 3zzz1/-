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
