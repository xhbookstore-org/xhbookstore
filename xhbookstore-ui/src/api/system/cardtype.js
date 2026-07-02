import request from '@/utils/request'

// 查询卡类型列表
export function listCardType(query) {
  return request({ url: '/member/card-type/list', method: 'get', params: query })
}

// 查询卡类型详情
export function getCardType(id) {
  return request({ url: '/member/card-type/' + id, method: 'get' })
}

// 新增卡类型
export function addCardType(data) {
  return request({ url: '/member/card-type', method: 'post', data: data })
}

// 修改卡类型
export function updateCardType(id, data) {
  return request({ url: '/member/card-type/' + id, method: 'put', data: data })
}

// 删除卡类型
export function delCardType(id) {
  return request({ url: '/member/card-type/' + id, method: 'delete' })
}

// 查询操作日志
export function listCardTypeLog(id) {
  return request({ url: '/member/card-type/' + id + '/log', method: 'get' })
}
