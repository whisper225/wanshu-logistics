import { request } from '@/utils/request'
import type { Organization, ServiceArea, PageResult } from '@/types'

export const organizationApi = {
  getTree(params?: { keyword?: string }) {
    return request.get<Organization[]>('/organizations/tree', { params })
  },

  getDetail(id: string) {
    return request.get<Organization>(`/organizations/${id}`)
  },

  create(data: Partial<Organization>) {
    return request.post<Organization>('/organizations', data)
  },

  update(id: string, data: Partial<Organization>) {
    return request.put<Organization>(`/organizations/${id}`, data)
  },

  delete(id: string) {
    return request.delete(`/organizations/${id}`)
  },

  getServiceArea(id: string) {
    return request.get<ServiceArea>(`/organizations/${id}/service-area`)
  },

  updateServiceArea(id: string, data: ServiceArea) {
    return request.put<ServiceArea>(`/organizations/${id}/service-area`, data)
  },

  getEmployees(id: string, params?: { pageNum?: number; pageSize?: number }) {
    return request.get<PageResult<any>>(`/organizations/${id}/employees`, { params })
  }
}
