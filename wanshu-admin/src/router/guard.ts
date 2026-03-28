import type { Router } from 'vue-router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { useUserStore } from '@/stores/user'

NProgress.configure({ showSpinner: false })

export function setupRouterGuard(router: Router) {
  router.beforeEach(async (to, _from, next) => {
    NProgress.start()
    
    const userStore = useUserStore()
    
    if (to.path === '/login') {
      if (userStore.token) {
        next('/')
      } else {
        next()
      }
      return
    }
    
    if (!userStore.token) {
      next('/login')
      return
    }
    
    if (!userStore.userInfo) {
      try {
        const { authApi } = await import('@/api/auth')
        const userInfo = await authApi.getUserInfo()
        userStore.setUserInfo(userInfo)
        next()
      } catch (error) {
        userStore.logout()
        next('/login')
      }
      return
    }
    
    next()
  })
  
  router.afterEach(() => {
    NProgress.done()
  })
}
