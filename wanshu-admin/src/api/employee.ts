import { request } from '@/utils/request'
import type { Courier, Driver, PageResult } from '@/types'

export const employeeApi = {
  getCourierList(params?: {
    account?: string
    name?: string
    phone?: string
    organizationId?: string
    organId?: number
    pageNum?: number
    pageSize?: number
    workStatus?: number
  }) {
    return request.get<PageResult<Courier>>('/emp/courier/page', {
      params: {
        pageNum: params?.pageNum,
        pageSize: params?.pageSize,
        organId: params?.organId ?? (params?.organizationId ? Number(params.organizationId) : undefined),
        workStatus: params?.workStatus
      }
    })
  },

  getCourierDetail(id: string) {
    return request.get<Courier>(`/emp/courier/${id}`)
  },

  updateCourier(id: string, data: Partial<Courier>) {
    return request.put<void>(`/emp/courier/${id}`, data)
  },

  createCourier(data: Partial<Courier>) {
    return request.post<void>('/emp/courier', data)
  },

  deleteCourier(id: string) {
    return request.delete<void>(`/emp/courier/${id}`)
  },

  updateCourierServiceArea(id: string, scopes: { provinceId?: number; cityId?: number; countyId?: number }[]) {
    return request.put<void>(`/emp/courier/${id}/scopes`, scopes)
  },

  getDriverList(params?: {
    account?: string
    name?: string
    phone?: string
    organizationId?: string
    organId?: number
    pageNum?: number
    pageSize?: number
    workStatus?: number
  }) {
    return request.get<PageResult<Driver>>('/emp/driver/page', {
      params: {
        pageNum: params?.pageNum,
        pageSize: params?.pageSize,
        organId: params?.organId ?? (params?.organizationId ? Number(params.organizationId) : undefined),
        workStatus: params?.workStatus
      }
    })
  },

  getDriverDetail(id: string) {
    return request.get<Driver>(`/emp/driver/${id}`)
  },

  updateDriver(id: string, data: Partial<Driver>) {
    return request.put<void>(`/emp/driver/${id}`, data)
  },

  createDriver(data: Partial<Driver>) {
    return request.post<void>('/emp/driver', data)
  },

  deleteDriver(id: string) {
    return request.delete<void>(`/emp/driver/${id}`)
  },

  bindVehicle(driverId: string, vehicleId: string) {
    return request.post<void>(`/base/vehicle/${vehicleId}/drivers/${driverId}`)
  },

  unbindVehicle(driverId: string, vehicleId: string) {
    return request.delete<void>(`/base/vehicle/${vehicleId}/drivers/${driverId}`)
  }
}
