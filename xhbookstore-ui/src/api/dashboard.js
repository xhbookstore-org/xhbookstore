import request from '@/utils/request'

export function getMemberDashboardOverview(deptId) {
  return request({
    url: '/dashboard/member/overview',
    method: 'get',
    params: deptId ? { deptId } : undefined
  })
}

export function getMemberDashboardDepts() {
  return request({ url: '/dashboard/member/depts', method: 'get' })
}

export function refreshMemberDashboard() {
  return request({
    url: '/dashboard/member/refresh',
    method: 'post'
  })
}
