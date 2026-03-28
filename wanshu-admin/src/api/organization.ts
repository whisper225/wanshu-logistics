import { request } from '@/utils/request'

/** 与后端 BaseOrgan 一致，用于 create/update/getDetail */
export interface BaseOrganPayload {
  id?: string | number
  /** 0 为顶级；大雪花建议用 string，避免 JSON 转 number 丢精度 */
  parentId?: number | string
  /** 详情接口返回：是否存在下级（用于禁用删除） */
  hasChildren?: boolean
  organName: string
  organType: number
  provinceId?: number | null
  cityId?: number | null
  countyId?: number | null
  address?: string | null
  longitude?: number | null
  latitude?: number | null
  managerName?: string | null
  managerPhone?: string | null
  contactName?: string | null
  contactPhone?: string | null
  sortOrder?: number
  status?: number
  deleted?: number
  createdTime?: string
  updatedTime?: string
}

/**
 * 机构作业范围（与后端 BaseOrganScope 一致）。
 * provinceId / cityId / countyId 约定为 **国标 GB/T 2260 行政区划代码（adcode）**，
 * 与 `element-china-area-data`、高德行政区检索一致；若历史数据曾用内部编码需先迁移。
 */
export interface OrganScope {
  id?: number
  organId?: number
  provinceId?: number
  cityId?: number
  countyId?: number
}

/** 分页行（较 BaseOrgan 多 parentName） */
export interface OrganTableRow extends BaseOrganPayload {
  parentName?: string
}

export interface OrganPageResult {
  list: OrganTableRow[]
  total: number
  pageNum: number
  pageSize: number
}

export const organizationApi = {
  getPage(params: { pageNum: number; pageSize: number; keyword?: string }) {
    return request.get<OrganPageResult>('/base/organ/page', { params })
  },

  /** 树节点字段与后端一致：id、parentId、name、type(1/2/3)、children 等 */
  getTree(params?: { keyword?: string }) {
    return request.get<Record<string, unknown>[]>('/base/organ/tree', { params })
  },

  getDetail(id: string) {
    return request.get<BaseOrganPayload>(`/base/organ/${id}`)
  },

  create(data: BaseOrganPayload) {
    return request.post<void>('/base/organ', data)
  },

  update(id: string, data: BaseOrganPayload) {
    return request.put<void>(`/base/organ/${id}`, data)
  },

  delete(id: string) {
    return request.delete<void>(`/base/organ/${id}`)
  },

  getScopes(id: string) {
    return request.get<OrganScope[]>(`/base/organ/${id}/scopes`)
  },

  /** @deprecated 使用 getScopes */
  getServiceArea(id: string) {
    return organizationApi.getScopes(id)
  },

  updateScopes(id: string, scopes: OrganScope[]) {
    return request.put<void>(`/base/organ/${id}/scopes`, scopes)
  },

  /** @deprecated 使用 updateScopes */
  updateServiceArea(id: string, _data: { organizationId?: string; areas?: unknown[] }) {
    return organizationApi.updateScopes(id, (_data.areas as OrganScope[]) || [])
  }
}
