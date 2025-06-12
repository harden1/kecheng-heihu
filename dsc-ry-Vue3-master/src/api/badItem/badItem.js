import request from '@/utils/request'

// 查询badItem列表
export function listBadItem(query) {
  return request({
    url: '/badItem/badItem/list',
    method: 'get',
    params: query
  })
}

// 查询badItem详细
export function getBadItem(id) {
  return request({
    url: '/badItem/badItem/' + id,
    method: 'get'
  })
}

// 新增badItem
export function addBadItem(data) {
  return request({
    url: '/badItem/badItem',
    method: 'post',
    data: data
  })
}

// 修改badItem
export function updateBadItem(data) {
  return request({
    url: '/badItem/badItem',
    method: 'put',
    data: data
  })
}

// 删除badItem
export function delBadItem(id) {
  return request({
    url: '/badItem/badItem/' + id,
    method: 'delete'
  })
}
// 后台黑湖接口 访问
export function updateHeihuApi(data) {
  return request({
    url: '/badItem/badItem',
    method: 'post',
    data: data
  })
}