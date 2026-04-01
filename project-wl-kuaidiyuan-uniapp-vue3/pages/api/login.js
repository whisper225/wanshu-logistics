import { request } from '../../utils/request.js'

export const wxLogin = (params) =>
  request({
    url: '/auth/wx/login',
    method: 'POST',
    params: {
      code: params.code,
      role: params.role || 'courier'
    }
  })

export const logout = () =>
  request({
    url: '/auth/logout',
    method: 'POST',
    params: {}
  })
