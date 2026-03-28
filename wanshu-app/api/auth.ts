import { request } from './request'

export function wxLogin(code: string) {
  return request<{ token: string; userInfo: Record<string, unknown>; mockNote?: string }>({
    url: '/auth/wx/login',
    method: 'POST',
    data: { code }
  })
}

export function sendSms(phone: string) {
  return request<void>({
    url: '/auth/sms/send',
    method: 'POST',
    data: { phone }
  })
}

export function smsLogin(phone: string, code: string) {
  return request<{ token: string; userInfo: Record<string, unknown> }>({
    url: '/auth/sms/login',
    method: 'POST',
    data: { phone, code }
  })
}

/** 与管理端相同账号密码；不传验证码时后端不校验图形码 */
export function passwordLogin(account: string, password: string) {
  return request<{ token: string; userInfo: Record<string, unknown> }>({
    url: '/auth/login',
    method: 'POST',
    data: { account, password }
  })
}

export function logout() {
  return request<void>({ url: '/auth/logout', method: 'POST' })
}
