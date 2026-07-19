import request from '@/utils/request'

export function listBorrowRecords(query) {
  return request({ url: '/borrow/record/list', method: 'get', params: query })
}

export function getBorrowRecord(orderId) {
  return request({ url: '/borrow/record/' + orderId, method: 'get' })
}

export function listBorrowDepts() {
  return request({ url: '/borrow/record/depts/options', method: 'get' })
}
