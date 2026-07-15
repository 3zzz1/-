import request from '@/utils/request'

// 查询整改任务列表（审计处视角）
export function listTask(query) {
  return request({ url: '/rectification/task/list', method: 'get', params: query })
}

// 查询我的整改任务（整改单位视角）
export function listMyTask(query) {
  return request({ url: '/rectification/task/my-list', method: 'get', params: query })
}

// 查询任务详情
export function getTask(taskId) {
  return request({ url: '/rectification/task/' + taskId, method: 'get' })
}

// 下发整改任务
export function addTask(data) {
  return request({ url: '/rectification/task', method: 'post', data: data })
}

// 批量下发整改任务
export function batchDispatchTask(data) {
  return request({ url: '/rectification/task/batch', method: 'post', data: data })
}

// 联络员确认接收任务
export function confirmTask(taskId) {
  return request({ url: '/rectification/task/confirm/' + taskId, method: 'put' })
}

// 联络员分办责任人
export function assignTask(taskId, assignUserId) {
  return request({ url: '/rectification/task/assign/' + taskId, method: 'put', data: { assignUserId } })
}

// 生成整改通知书Word
export function generateNotice(taskId) {
  return request({
    url: '/rectification/task/notice/' + taskId,
    method: 'post',
    responseType: 'blob',
    returnResponse: true
  })
}

// 查询逾期/临期预警
export function overdueAlert(query) {
  return request({ url: '/rectification/task/overdue-alert', method: 'get', params: query })
}

// 修改任务
export function updateTask(data) {
  return request({ url: '/rectification/task', method: 'put', data: data })
}

// 删除任务
export function delTask(taskIds) {
  return request({ url: '/rectification/task/' + taskIds, method: 'delete' })
}

// 查询任务进展时间轴
export function getProgressByTask(taskId) {
  return request({ url: '/rectification/progress/' + taskId, method: 'get' })
}
