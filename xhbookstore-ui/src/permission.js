import router from './router'
import store from './store'
import { Message } from 'element-ui'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getToken } from '@/utils/auth'
import { isPathMatch } from '@/utils/validate'
import { isRelogin } from '@/utils/request'

NProgress.configure({ showSpinner: false })

const whiteList = ['/login', '/register']
const dashboardPath = '/index'
const memberListPath = '/member/list'
const allPermission = '*:*:*'
const dashboardPermission = 'dashboard:member:view'
const memberListPermission = 'member:member:list'

const isWhiteList = (path) => {
  return whiteList.some(pattern => isPathMatch(pattern, path))
}

const hasPermission = (permission) => {
  const permissions = store.getters && store.getters.permissions
  return permissions.some(item => item === allPermission || item === permission)
}

const getDefaultPath = () => {
  if (hasPermission(dashboardPermission)) {
    return dashboardPath
  }
  if (hasPermission(memberListPermission)) {
    return memberListPath
  }
  return dashboardPath
}

const shouldRedirectDashboard = (path) => {
  return path === dashboardPath && getDefaultPath() !== dashboardPath
}

router.beforeEach((to, from, next) => {
  NProgress.start()
  if (getToken()) {
    to.meta.title && store.dispatch('settings/setTitle', to.meta.title)
    const isLock = store.getters.isLock
    if (to.path === '/login') {
      next({ path: '/' })
      NProgress.done()
    } else if (isWhiteList(to.path)) {
      next()
    } else if (isLock && to.path !== '/lock') {
      next({ path: '/lock' })
      NProgress.done()
    } else if (!isLock && to.path === '/lock') {
      next({ path: '/' })
      NProgress.done()
    } else {
      if (store.getters.roles.length === 0) {
        isRelogin.show = true
        store.dispatch('GetInfo').then(() => {
          isRelogin.show = false
          store.dispatch('GenerateRoutes').then(accessRoutes => {
            router.addRoutes(accessRoutes)
            if (shouldRedirectDashboard(to.path)) {
              next({ path: getDefaultPath(), replace: true })
              return
            }
            next({ ...to, replace: true })
          })
        }).catch(err => {
          store.dispatch('LogOut').then(() => {
            Message.error(err)
            next({ path: '/login', query: { redirect: to.fullPath } })
          })
        })
      } else {
        if (shouldRedirectDashboard(to.path)) {
          next({ path: getDefaultPath(), replace: true })
          return
        }
        next()
      }
    }
  } else {
    if (isWhiteList(to.path)) {
      next()
    } else {
      next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
      NProgress.done()
    }
  }
})

router.afterEach(() => {
  NProgress.done()
})
