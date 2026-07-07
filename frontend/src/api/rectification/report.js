import request from '@/utils/request'

// 查询整改报告
export function getReport(taskId) {
  return request({ url: '/rectification/report/' + taskId, method: 'get' })
}

// 编辑/保存报告
export function addReport(data) {
  return request({ url: '/rectification/report', method: 'post', data: data })
}

// 自动生成Word报告
export function generateReport(taskId) {
  return request({ url: '/rectification/report/generate/' + taskId, method: 'post' })
}

// 提交单位审批
export function submitReport(reportId) {
  return request({ url: '/rectification/report/submit/' + reportId, method: 'put' })
}

// 单位领导审批
export function leaderApprove(data) {
  return request({ url: '/rectification/report/leader-approve', method: 'put', data: data })
}
