import { request } from '@/utils/request'
import type { Route, RouteShift, TransportTask, PickupTask, DeliveryTask, PageResult } from '@/types'

export const dispatchApi = {
  getRouteList(params?: {
    code?: string
    name?: string
    type?: string
    startOrganizationId?: string
    endOrganizationId?: string
    pageNum?: number
    pageSize?: number
  }) {
    return request.get<PageResult<Route>>('/routes', { params })
  },

  getRouteDetail(id: string) {
    return request.get<Route>(`/routes/${id}`)
  },

  createRoute(data: Partial<Route>) {
    return request.post<Route>('/routes', data)
  },

  updateRoute(id: string, data: Partial<Route>) {
    return request.put<Route>(`/routes/${id}`, data)
  },

  deleteRoute(id: string) {
    return request.delete(`/routes/${id}`)
  },

  createShift(routeId: string, data: Partial<RouteShift>) {
    return request.post<RouteShift>(`/routes/${routeId}/shifts`, data)
  },

  updateShift(routeId: string, shiftId: string, data: Partial<RouteShift>) {
    return request.put<RouteShift>(`/routes/${routeId}/shifts/${shiftId}`, data)
  },

  deleteShift(routeId: string, shiftId: string) {
    return request.delete(`/routes/${routeId}/shifts/${shiftId}`)
  },

  assignVehicles(routeId: string, shiftId: string, vehicleIds: string[]) {
    return request.post(`/routes/${routeId}/shifts/${shiftId}/vehicles`, { vehicleIds })
  },

  removeVehicle(routeId: string, shiftId: string, vehicleId: string) {
    return request.delete(`/routes/${routeId}/shifts/${shiftId}/vehicles/${vehicleId}`)
  },

  getTransportTaskList(params?: {
    taskNo?: string
    status?: string
    routeId?: string
    startOrganizationId?: string
    endOrganizationId?: string
    scheduledDepartureTimeStart?: string
    scheduledDepartureTimeEnd?: string
    pageNum?: number
    pageSize?: number
  }) {
    return request.get<PageResult<TransportTask>>('/transport-tasks', { params })
  },

  getTransportTaskDetail(id: string) {
    return request.get<TransportTask>(`/transport-tasks/${id}`)
  },

  getTaskWaybills(id: string) {
    return request.get<any[]>(`/transport-tasks/${id}/waybills`)
  },

  getPickupTaskList(params?: {
    waybillNo?: string
    status?: string
    courierId?: string
    courierName?: string
    senderName?: string
    senderProvince?: string
    pageNum?: number
    pageSize?: number
  }) {
    return request.get<PageResult<PickupTask>>('/pickup-tasks', { params })
  },

  assignPickupCourier(taskIds: string[], courierId: string) {
    return request.post('/pickup-tasks/assign', { taskIds, courierId })
  },

  getDeliveryTaskList(params?: {
    waybillNo?: string
    status?: string
    courierId?: string
    courierName?: string
    receiverName?: string
    receiverProvince?: string
    pageNum?: number
    pageSize?: number
  }) {
    return request.get<PageResult<DeliveryTask>>('/delivery-tasks', { params })
  },

  assignDeliveryCourier(taskIds: string[], courierId: string) {
    return request.post('/delivery-tasks/assign', { taskIds, courierId })
  }
}
