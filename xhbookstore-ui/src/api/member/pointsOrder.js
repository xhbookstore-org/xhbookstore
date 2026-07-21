import request from '@/utils/request'

export function listPointsOrders(query) {
  return request({ url: '/member/points/order/list', method: 'get', params: query })
}

export function getPointsOrder(id) {
  return request({ url: '/member/points/order/' + id, method: 'get' })
}

export function listPointsOrderRules() {
  return request({ url: '/member/points/order/rules/options', method: 'get' })
}
