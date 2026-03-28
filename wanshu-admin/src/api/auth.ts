import { request } from '@/utils/request'
import type { LoginForm, UserInfo } from '@/types'

export const authApi = {
  login(data: LoginForm) {
    return request.post<{ token: string; userInfo: UserInfo }>('/auth/login', data)
  },

  getCaptcha() {
    return request.get<{ captchaId: string; captchaImage: string }>('/auth/captcha')
  },

  getUserInfo() {
    return request.get<UserInfo>('/auth/userInfo')
  },

  logout() {
    return request.post('/auth/logout')
  }
}
