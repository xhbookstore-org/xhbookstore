import request from '@/utils/request'

export function listPointsRule(query) {
  return request({ url: '/system/points-rule/list', method: 'get', params: query })
}

export function getPointsRule(id) {
  return request({ url: '/system/points-rule/' + id, method: 'get' })
}

export function addPointsRule(data) {
  return request({ url: '/system/points-rule', method: 'post', data })
}

export function updatePointsRule(data) {
  return request({ url: '/system/points-rule', method: 'put', data })
}

export function delPointsRule(ids) {
  return request({ url: '/system/points-rule/' + ids, method: 'delete' })
}

export function updatePointsRuleValue(id, data) {
  return request({ url: '/system/points-rule/' + id + '/points', method: 'put', data })
}
