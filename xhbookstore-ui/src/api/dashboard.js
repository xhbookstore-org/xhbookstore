import request from '@/utils/request'

export function getMemberDashboardOverview(deptId) {
  return request({
    url: '/dashboard/member/overview',
    method: 'get',
    params: { deptId }
  })
}

export function getMemberDashboardDeptOptions() {
  return request({
    url: '/dashboard/member/dept-options',
    method: 'get'
  })
}

export function refreshMemberDashboard() {
  return request({
    url: '/dashboard/member/refresh',
    method: 'post'
  })
}
