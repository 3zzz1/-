import request from '@/utils/request'

// 查询问题台账列表
export function listIssue(query) {
  return request({ url: '/rectification/issue/list', method: 'get', params: query })
}

// 查询问题详情
export function getIssue(issueId) {
  return request({ url: '/rectification/issue/' + issueId, method: 'get' })
}

// 新增问题（手动录入外源问题）
export function addIssue(data) {
  return request({ url: '/rectification/issue', method: 'post', data: data })
}

// 修改问题
export function updateIssue(data) {
  return request({ url: '/rectification/issue', method: 'put', data: data })
}

// 删除问题
export function delIssue(issueIds) {
  return request({ url: '/rectification/issue/' + issueIds, method: 'delete' })
}

// 从审计项目同步问题
export function syncIssue(projectId) {
  return request({ url: '/rectification/issue/sync/' + projectId, method: 'post' })
}

// 导出问题台账Excel
export function exportIssue(query) {
  return request({ 
    url: '/rectification/issue/export', 
    method: 'post', 
    data: query,          
    responseType: 'blob'  
  })
}
