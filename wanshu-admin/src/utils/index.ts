export const formatDate = (date: string | Date, format = 'YYYY-MM-DD HH:mm:ss'): string => {
  if (!date) return ''
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')
  
  return format
    .replace('YYYY', String(year))
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

export const generateCode = (prefix: string, length = 6): string => {
  const randomNum = Math.floor(Math.random() * Math.pow(10, length))
  return prefix + String(randomNum).padStart(length, '0')
}

export const validatePhone = (phone: string): boolean => {
  return /^1[3-9]\d{9}$/.test(phone)
}

export const validateEmail = (email: string): boolean => {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
}

export const debounce = <T extends (...args: any[]) => any>(
  func: T,
  wait: number
): ((...args: Parameters<T>) => void) => {
  let timeout: NodeJS.Timeout | null = null
  
  return function(...args: Parameters<T>) {
    if (timeout) clearTimeout(timeout)
    timeout = setTimeout(() => {
      func(...args)
    }, wait)
  }
}

export const throttle = <T extends (...args: any[]) => any>(
  func: T,
  wait: number
): ((...args: Parameters<T>) => void) => {
  let timeout: NodeJS.Timeout | null = null
  let previous = 0
  
  return function(...args: Parameters<T>) {
    const now = Date.now()
    const remaining = wait - (now - previous)
    
    if (remaining <= 0 || remaining > wait) {
      if (timeout) {
        clearTimeout(timeout)
        timeout = null
      }
      previous = now
      func(...args)
    } else if (!timeout) {
      timeout = setTimeout(() => {
        previous = Date.now()
        timeout = null
        func(...args)
      }, remaining)
    }
  }
}

export const downloadFile = (url: string, filename: string) => {
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

export const exportToExcel = (data: any[], filename: string) => {
  console.log('Export to Excel:', filename, data)
}

export const getOrganizationTypeLabel = (type: string): string => {
  const map: Record<string, string> = {
    'HEADQUARTERS': '总公司',
    'BRANCH': '分公司',
    'PRIMARY_HUB': '一级转运中心',
    'SECONDARY_HUB': '二级分拣中心',
    'STATION': '营业部'
  }
  return map[type] || type
}

export const getStatusLabel = (status: string): string => {
  const map: Record<string, string> = {
    'ACTIVE': '启用',
    'INACTIVE': '停用',
    'AVAILABLE': '可用',
    'DISABLED': '停用',
    'PENDING': '待处理',
    'CONFIRMED': '已确认',
    'PICKED_UP': '已取件',
    'CANCELLED': '已取消',
    'CLOSED': '已关闭',
    'PENDING_DISPATCH': '待调度',
    'DISPATCHED': '已调度',
    'DISPATCH_FAILED': '调度失败',
    'IN_TRANSIT': '运输中',
    'DELIVERED': '已签收',
    'REJECTED': '已拒收',
    'ASSIGNED': '已分配',
    'IN_PROGRESS': '进行中',
    'COMPLETED': '已完成',
    'TIMEOUT': '超时',
    'UNASSIGNED': '未分配'
  }
  return map[status] || status
}

export const getStatusType = (status: string): 'success' | 'warning' | 'danger' | 'info' | '' => {
  const successStatus = ['ACTIVE', 'AVAILABLE', 'COMPLETED', 'DELIVERED', 'PICKED_UP']
  const warningStatus = ['PENDING', 'ASSIGNED', 'IN_PROGRESS', 'IN_TRANSIT', 'DISPATCHED']
  const dangerStatus = ['INACTIVE', 'DISABLED', 'CANCELLED', 'CLOSED', 'DISPATCH_FAILED', 'REJECTED', 'TIMEOUT']
  
  if (successStatus.includes(status)) return 'success'
  if (warningStatus.includes(status)) return 'warning'
  if (dangerStatus.includes(status)) return 'danger'
  return 'info'
}
