import { request } from '@/utils/request'
import type { Courier, Driver, Schedule, PageResult } from '@/types'

export const employeeApi = {
  getCourierList(params?: {
    account?: string
    name?: string
    phone?: string
    organizationId?: string
    pageNum?: number
    pageSize?: number
  }) {
    return request.get<PageResult<Courier>>('/employees/couriers', { params })
  },

  getCourierDetail(id: string) {
    return request.get<Courier>(`/employees/couriers/${id}`)
  },

  updateCourierServiceArea(id: string, serviceArea: any) {
    return request.put(`/employees/couriers/${id}/service-area`, serviceArea)
  },

  getDriverList(params?: {
    account?: string
    name?: string
    phone?: string
    organizationId?: string
    pageNum?: number
    pageSize?: number
  }) {
    return request.get<PageResult<Driver>>('/employees/drivers', { params })
  },

  getDriverDetail(id: string) {
    return request.get<Driver>(`/employees/drivers/${id}`)
  },

  updateDriver(id: string, data: Partial<Driver>) {
    return request.put<Driver>(`/employees/drivers/${id}`, data)
  },

  bindVehicle(driverId: string, vehicleId: string) {
    return request.post(`/employees/drivers/${driverId}/vehicle`, { vehicleId })
  },

  unbindVehicle(driverId: string) {
    return request.delete(`/employees/drivers/${driverId}/vehicle`)
  },

  getScheduleList(params?: {
    employeeName?: string
    mode?: string
    date?: string
    pageNum?: number
    pageSize?: number
  }) {
    return request.get<PageResult<Schedule>>('/employees/schedules', { params })
  },

  createSchedule(data: Partial<Schedule>) {
    return request.post<Schedule>('/employees/schedules', data)
  },

  updateSchedule(id: string, data: Partial<Schedule>) {
    return request.put<Schedule>(`/employees/schedules/${id}`, data)
  },

  deleteSchedule(id: string) {
    return request.delete(`/employees/schedules/${id}`)
  }
}
