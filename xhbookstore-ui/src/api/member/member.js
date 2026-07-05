import request from '@/utils/request'

// Member list
export function listMember(query) {
  return request({ url: '/member/list', method: 'get', params: query })
}

// Member detail
export function getMember(id) {
  return request({ url: '/member/' + id, method: 'get' })
}

// Add member
export function addMember(data) {
  return request({ url: '/member', method: 'post', data: data })
}

// Update member
export function updateMember(id, data) {
  return request({ url: '/member/' + id, method: 'put', data: data })
}

// Delete member
export function delMember(id) {
  return request({ url: '/member/' + id, method: 'delete' })
}

// Generate member card number
export function generateCardNo(deptId) {
  return request({ url: '/member/generateCardNo', method: 'get', params: { deptId } })
}

// Card types
export function listCardTypes() {
  return request({ url: '/member/cardTypes', method: 'get' })
}

// Points records
export function listPoints(memberId) {
  return request({ url: '/member/' + memberId + '/points', method: 'get' })
}

// Add points
export function addPoints(memberId, data) {
  return request({ url: '/member/' + memberId + '/points', method: 'post', data: data })
}

// Member cards
export function getMemberCards(memberId) {
  return request({ url: '/member/card/member/' + memberId, method: 'get' })
}

// Member card records
export function listMemberCards(query) {
  return request({ url: '/member/card/list', method: 'get', params: query })
}

// Member card sale orders
export function listMemberCardOrders(query) {
  return request({ url: '/member/card/order/list', method: 'get', params: query })
}

// Member card refund orders
export function listMemberCardRefundOrders(query) {
  return request({ url: '/member/card/refund-order/list', method: 'get', params: query })
}

// Member card logs
export function listMemberCardLogs(memberCardId) {
  return request({ url: '/member/card/' + memberCardId + '/log', method: 'get' })
}

// Refund member card
export function refundMemberCard(memberCardId, data) {
  return request({ url: '/member/card/' + memberCardId + '/refund', method: 'post', data: data })
}

// Import ERP members
export function importMember(data, config = {}) {
  return request({
    url: '/member/importData',
    method: 'post',
    data,
    headers: { 'Content-Type': 'multipart/form-data', repeatSubmit: false },
    timeout: 60000,
    ...config
  })
}

// Export members
export function exportMember(query) {
  return request({ url: '/member/export', method: 'get', params: query })
}
