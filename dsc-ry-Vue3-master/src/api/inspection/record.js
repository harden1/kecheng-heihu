import request from '@/utils/request'

// 查询镜检单条记录列表
export function listRecord(query) {
  return request({
    url: '/inspection/record/list',
    method: 'get',
    params: query
  })
}

// 查询镜检单条记录详细
export function getRecord(id) {
  return request({
    url: '/inspection/record/' + id,
    method: 'get'
  })
}

// 新增镜检单条记录
export function addRecord(data) {
  return request({
    url: '/inspection/record',
    method: 'post',
    data: data
  })
}

// 修改镜检单条记录
export function updateRecord(data) {
  return request({
    url: '/inspection/record',
    method: 'put',
    data: data
  })
}

// 删除镜检单条记录
export function delRecord(id) {
  return request({
    url: '/inspection/record/' + id,
    method: 'delete'
  })
}
