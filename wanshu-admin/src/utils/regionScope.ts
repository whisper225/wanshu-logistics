import type { OrganScope } from '@/api/organization'

/** 取用于地图边界检索的最深层级 adcode（区 > 市 > 省） */
export function scopeToDistrictAdcode(s: OrganScope): string {
  if (s.countyId != null && s.countyId !== 0) return String(s.countyId)
  if (s.cityId != null && s.cityId !== 0) return String(s.cityId)
  if (s.provinceId != null && s.provinceId !== 0) return String(s.provinceId)
  return ''
}

export function organScopeToPath(s: OrganScope): string[] {
  const p = s.provinceId != null && s.provinceId !== 0 ? String(s.provinceId) : ''
  if (!p) return []
  const c = s.cityId != null && s.cityId !== 0 ? String(s.cityId) : ''
  const co = s.countyId != null && s.countyId !== 0 ? String(s.countyId) : ''
  if (co && c && p) return [p, c, co]
  if (c && p) return [p, c]
  return [p]
}

export function scopesToPaths(scopes: OrganScope[]): string[][] {
  return scopes.map(organScopeToPath).filter((p) => p.length > 0)
}

/** 级联多选路径 → 后端作业范围行（国标 adcode，存为 number） */
export function pathsToOrganScopes(paths: string[][]): OrganScope[] {
  const rows: OrganScope[] = []
  for (const path of paths) {
    if (!path?.length) continue
    const provinceId = Number(path[0])
    if (!Number.isFinite(provinceId)) continue
    const row: OrganScope = { provinceId }
    if (path.length >= 2) row.cityId = Number(path[1])
    if (path.length >= 3) row.countyId = Number(path[2])
    rows.push(row)
  }
  return rows
}
