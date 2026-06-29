import request from '@/utils/request'

// 会员列表
export function listMember(query) {
  return request({ url: '/member/list', method: 'get', params: query })
}

// 会员详情
export function getMember(id) {
  return request({ url: '/member/' + id, method: 'get' })
}

// 新增会员
export function addMember(data) {
  return request({ url: '/member', method: 'post', data: data })
}

// 修改会员
export function updateMember(id, data) {
  return request({ url: '/member/' + id, method: 'put', data: data })
}

// 删除会员
export function delMember(id) {
  return request({ url: '/member/' + id, method: 'delete' })
}

// 生成卡号
export function generateCardNo(deptId) {
  return request({ url: '/member/generateCardNo', method: 'get', params: { deptId } })
}

// 卡类型列表
export function listCardTypes() {
  return request({ url: '/member/cardTypes', method: 'get' })
}

// 积分流水
export function listPoints(memberId) {
  return request({ url: '/member/' + memberId + '/points', method: 'get' })
}

// 添加积分
export function addPoints(memberId, data) {
  return request({ url: '/member/' + memberId + '/points', method: 'post', data: data })
}

// 导出
export function exportMember(query) {
  return request({ url: '/member/export', method: 'get', params: query })
}
