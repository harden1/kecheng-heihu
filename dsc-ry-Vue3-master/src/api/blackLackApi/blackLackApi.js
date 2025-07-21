import request from '@/utils/request'
import { parseStrEmpty } from '@/utils/ruoyi'
//查询黑湖数据是否存在此用户：登陆时候验证，新注册的时候验证
export function checkUserToBlackLack(username) {
  let query = { username: username }
  console.log('查询接口', query)
  return request({
    url: '/system/user/checkUserToBlackLack/',
    method: 'get',
    params: query
  })
}
// 获取用户详细信息
export function queryScanTaskResult(taskCode) {
  let query = { taskCode: taskCode }
  console.log('查询接口', query)
  return request({
    url: '/blacklackUser/BlacklackUser/queryScanTaskResult',
    method: 'post',
    params: query
  })
}
