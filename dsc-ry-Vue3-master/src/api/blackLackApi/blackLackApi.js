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
//开始报工
export function addOrReadInspectionMain(reportRecord) {
  console.log('开始报工', reportRecord)
  return request({
    url: '/blacklackUser/BlacklackUser/addOrReadInspectionMain',
    method: 'post',
    data: reportRecord 
  })
}
//不良品报工
export function reportBadItemOne(data) {
  console.log('开始报工', data);
  return request({
    url: '/blacklackUser/BlacklackUser/reportBadItemOne',
    method: 'post',
    data,
  });
}
//良品报工
export function reportBatch(reportRecord) {
  console.log('开始报工', reportRecord)
  return request({
    url: '/blacklackUser/BlacklackUser/reportBatch',
    method: 'post',
    data: reportRecord 
  })
}
export function updateStopTime(reportRecord) {
  return request({
    url: '/blacklackUser/BlacklackUser/updateStopTime',
    method: 'post',
    data: reportRecord 
  })
}