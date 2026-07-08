import request from '@/utils/request'

// 查询材料列表
export function listMaterial(issueId) {
  return request({ url: '/rectification/material/list/' + issueId, method: 'get' })
}

// 上传材料
export function uploadMaterial(data) {
  return request({ url: '/rectification/material/upload', method: 'post', data: data, headers: { 'Content-Type': 'multipart/form-data', repeatSubmit: false } })
}

// 删除材料
export function delMaterial(materialIds) {
  return request({ url: '/rectification/material/' + materialIds, method: 'delete' })
}

// 下载材料
export function downloadMaterial(materialId) {
  return request({ url: '/rectification/material/download/' + materialId, method: 'get', responseType: 'blob' })
}
