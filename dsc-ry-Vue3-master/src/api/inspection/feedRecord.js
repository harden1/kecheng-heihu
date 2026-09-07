import request from '@/utils/request'

// 查询投料记录列表
export function listFeedRecord(query) {
  return request({
    url: '/inspection/feedRecord/list',
    method: 'get',
    params: query
  })
}

// 查询投料记录详细
export function getFeedRecord(id) {
  return request({
    url: '/inspection/feedRecord/' + id,
    method: 'get'
  })
}

// 手动重传投料记录
export function retryFeedRecord(id) {
  return request({
    url: '/inspection/feedRecord/' + id + '/retry',
    method: 'post'
  })
}
