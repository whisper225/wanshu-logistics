import { request } from './request'

export type UserRole = 'user' | 'courier' | 'driver'

/**
 * 微信小程序登录
 * @param code  wx.login 获取的临时 code
 * @param role  登录端标识，后端根据此字段校验用户是否有对应角色
 */
export function wxLogin(code: string, role: UserRole = 'user') {
  return request<{ token: string; userInfo: Record<string, unknown>; mockNote?: string }>({
    url: '/auth/wx/login',
    method: 'POST',
    data: { code, role }
  })
}

export function logout() {
  return request<void>({ url: '/auth/logout', method: 'POST' })
}
