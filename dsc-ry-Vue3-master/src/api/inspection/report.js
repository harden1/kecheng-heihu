import request from '@/utils/request'

// 查询报工记录列表
export function listReport(query) {
  return request({
    url: '/inspection/report/list',
    method: 'get',
    params: query
  })
}

// 查询报工记录详细
export function getReport(id) {
  return request({
    url: '/inspection/report/' + id,
    method: 'get'
  })
}

// 新增报工记录
export function addReport(data) {
  return request({
    url: '/inspection/report',
    method: 'post',
    data: data
  })
}

// 修改报工记录
export function updateReport(data) {
  return request({
    url: '/inspection/report',
    method: 'put',
    data: data
  })
}

// 删除报工记录
export function delReport(id) {
  return request({
    url: '/inspection/report/' + id,
    method: 'delete'
  })
}
