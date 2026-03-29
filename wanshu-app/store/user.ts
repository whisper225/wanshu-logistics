import { defineStore } from 'pinia'
import { getUserInfo, type UserInfo } from '@/api/user'

/** 选项式 Store，避免在 .ts 中 `import { ref } from 'vue'`，兼容 HBuilderX 内置 ts-loader */
export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: null as UserInfo | null,
    token: (uni.getStorageSync('token') as string) || '',
  }),
  actions: {
    setToken(t: string) {
      this.token = t
      uni.setStorageSync('token', t)
    },
    clearToken() {
      this.token = ''
      uni.removeStorageSync('token')
    },
    async fetchUserInfo() {
      try {
        this.userInfo = await getUserInfo()
      } catch {
        this.userInfo = null
      }
    },
    logout() {
      this.clearToken()
      this.userInfo = null
      uni.reLaunch({ url: '/pages/login/index' })
    },
  },
})
