import request from '@/utils/request'

export function getMemberDashboardOverview() {
  return request({
    url: '/dashboard/member/overview',
    method: 'get'
  })
}

export function refreshMemberDashboard() {
  return request({
    url: '/dashboard/member/refresh',
    method: 'post'
  })
}
