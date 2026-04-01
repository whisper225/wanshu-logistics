import { baseUrl } from './env'

function trimBase() {
  return String(baseUrl || '').replace(/\/$/, '')
}

function omitEmpty(data) {
  if (!data || typeof data !== 'object') return undefined
  const out = {}
  for (const key of Object.keys(data)) {
    const v = data[key]
    if (v !== undefined && v !== null && v !== '') {
      out[key] = v
    }
  }
  return Object.keys(out).length ? out : undefined
}

export function request({ url = '', params = {}, method = 'GET', header = {} }) {
  const token = uni.getStorageSync('token')
  const isGet = !method || method.toUpperCase() === 'GET'
  const data = isGet ? omitEmpty(params) : params
  return new Promise((resolve, reject) => {
    uni.request({
      url: trimBase() + url,
      method,
      data,
      header: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: token } : {}),
        ...header
      },
      timeout: 20000,
      success: (res) => {
        if (res.statusCode === 401) {
          try {
            uni.removeStorageSync('token')
          } catch (e) {}
          reject(new Error('登录已失效，请重新登录'))
          return
        }
        const body = res.data
        if (body && typeof body === 'object' && 'code' in body) {
          if (body.code === 200) {
            resolve(body.data)
            return
          }
          reject(new Error(body.message || `请求失败(${body.code})`))
          return
        }
        resolve(body)
      },
      fail: (err) => reject(err)
    })
  })
}
