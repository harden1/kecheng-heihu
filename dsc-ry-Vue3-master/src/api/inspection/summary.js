import request from '@/utils/request'

// 查询镜检统计主列表
export function listSummary(query) {
  return request({
    url: '/inspection/summary/list',
    method: 'get',
    params: query
  })
}

// 查询镜检统计主详细
export function getSummary(id) {
  return request({
    url: '/inspection/summary/' + id,
    method: 'get'
  })
}

// 新增镜检统计主
export function addSummary(data) {
  return request({
    url: '/inspection/summary',
    method: 'post',
    data: data
  })
}

// 修改镜检统计主
export function updateSummary(data) {
  return request({
    url: '/inspection/summary',
    method: 'put',
    data: data
  })
}

// 删除镜检统计主
export function delSummary(id) {
  return request({
    url: '/inspection/summary/' + id,
    method: 'delete'
  })
}
