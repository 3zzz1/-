import request from '@/utils/request'

// 整改总览
export function getOverview(query) {
  return request({ url: '/rectification/statistics/home-overview', method: 'get', params: query })
}

// 问题类型分布
export function getByCategory() {
  return request({ url: '/rectification/statistics/by-category', method: 'get' })
}

// 整改状态分布
export function getByStatus() {
  return request({ url: '/rectification/statistics/by-status', method: 'get' })
}

// 逾期统计
export function getOverdue(query) {
  return request({ url: '/rectification/statistics/overdue', method: 'get', params: query })
}

// 资金挽回规模
export function getFundRecovery() {
  return request({ url: '/rectification/statistics/fund-recovery', method: 'get' })
}

// 重复问题识别
export function getRecurring() {
  return request({ url: '/rectification/statistics/recurring', method: 'get' })
}

// 整改趋势
export function getTrend(type) {
  return request({ url: '/rectification/statistics/trend', method: 'get', params: { type } })
}
