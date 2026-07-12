import request from '@/utils/request'

// 查询整改报告
export function getReport(taskId) {
  return request({ url: '/rectification/report/' + taskId, method: 'get' })
}

// 新增或更新整改报告
export function addReport(data) {
  return request({ url: '/rectification/report', method: 'post', data: data })
}

// 生成整改报告
export function generateReport(taskId) {
  return request({ url: '/rectification/report/generate/' + taskId, method: 'post' })
}

// 下载整改报告 Word
export function downloadReportWord(taskId) {
  return request({ url: '/rectification/report/word/' + taskId, method: 'get', responseType: 'blob', returnResponse: true })
}

// 提交报告给单位负责人审批
export function submitReport(reportId) {
  return request({ url: '/rectification/report/submit/' + reportId, method: 'put' })
}

// 单位负责人审批报告
export function leaderApprove(data) {
  return request({ url: '/rectification/report/leader-approve', method: 'put', data: data })
}
