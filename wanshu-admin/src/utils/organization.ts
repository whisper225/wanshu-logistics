import type { BaseOrganPayload } from '@/api/organization'

/** 雪花 ID 超过 JS 安全整数时须用字符串提交，避免 Number() 丢精度 */
export function toApiParentId(v: string | number | undefined | null): number | string {
  if (v == null || v === '') return 0
  const s = String(v).trim()
  if (s === '0') return 0
  return s
}

export function emptyOrganForm(parentId: string | number): BaseOrganPayload {
  const top = parentId === 0 || parentId === '0'
  return {
    parentId: top ? 0 : String(parentId),
    organName: '',
    organType: 3,
    provinceId: undefined,
    cityId: undefined,
    countyId: undefined,
    address: '',
    longitude: undefined,
    latitude: undefined,
    managerName: '',
    managerPhone: '',
    contactName: '',
    contactPhone: '',
    sortOrder: 0,
    status: 1
  }
}

export function organTypeLabel(t: number | undefined): string {
  const m: Record<number, string> = {
    1: '一级转运中心',
    2: '二级分拣中心',
    3: '营业部'
  }
  return t != null ? m[t] ?? String(t) : '—'
}
