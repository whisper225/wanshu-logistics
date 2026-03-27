import { request } from '@/utils/request'
import type { VehicleType, Vehicle, VehicleDetail, PageResult } from '@/types'

export const vehicleApi = {
  getTypeList(params?: {
    code?: string
    name?: string
    loadWeight?: number
    loadVolume?: number
    pageNum?: number
    pageSize?: number
  }) {
    return request.get<PageResult<VehicleType>>('/vehicles/types', { params })
  },

  createType(data: Partial<VehicleType>) {
    return request.post<VehicleType>('/vehicles/types', data)
  },

  updateType(id: string, data: Partial<VehicleType>) {
    return request.put<VehicleType>(`/vehicles/types/${id}`, data)
  },

  deleteType(id: string) {
    return request.delete(`/vehicles/types/${id}`)
  },

  getList(params?: {
    typeId?: string
    licensePlate?: string
    status?: string
    pageNum?: number
    pageSize?: number
  }) {
    return request.get<PageResult<Vehicle>>('/vehicles', { params })
  },

  getDetail(id: string) {
    return request.get<VehicleDetail>(`/vehicles/${id}`)
  },

  create(data: Partial<Vehicle>) {
    return request.post<Vehicle>('/vehicles', data)
  },

  update(id: string, data: Partial<Vehicle>) {
    return request.put<Vehicle>(`/vehicles/${id}`, data)
  },

  delete(id: string) {
    return request.delete(`/vehicles/${id}`)
  },

  updateStatus(id: string, status: 'AVAILABLE' | 'DISABLED') {
    return request.put(`/vehicles/${id}/status`, { status })
  },

  bindDrivers(id: string, driverIds: string[]) {
    return request.post(`/vehicles/${id}/drivers`, { driverIds })
  },

  unbindDriver(id: string, driverId: string) {
    return request.delete(`/vehicles/${id}/drivers/${driverId}`)
  }
}
