import { request } from '@/utils/request'
import type { TransportTask, PickupTask, DeliveryTask, PageResult, Vehicle } from '@/types'

/** 线路（与后端 DispatchLine 字段一致） */
export interface DispatchLineDto {
  id?: number
  lineNumber?: string
  lineName?: string
  lineType?: number
  startOrganId?: number | string
  endOrganId?: number | string
  /** 距离（米），新增时后端自动计算，编辑时可手动修改 */
  distance?: number
  /** 成本（元），新增时后端自动计算，编辑时可手动修改 */
  cost?: number
  estimatedTime?: number
  status?: number
  startOrganName?: string
  endOrganName?: string
}

export interface DispatchTripDto {
  id?: number
  tripNumber?: string
  tripName?: string
  lineId?: number
  periodType?: number
  departDay?: string
  departTime?: string
  status?: number
}

export interface TripVehicleRow {
  id: string
  tripId: string
  vehicleId: string
  licensePlate?: string
  vehicleNumber?: string
  organId?: number | string
  vehicleTypeId?: number | string
  status?: number
}

/** 全局调度配置（dispatch_config 表 organ_id 为空的一条） */
export interface GlobalDispatchConfigDto {
  id?: string | number
  /** 最晚任务下发时间（小时前） */
  latestDispatchHour?: number
  /** 优先匹配方式：1=转运次数最少 2=成本最低 */
  priorityFirst?: number
  /** 干线(类型1)默认每公里成本（元） */
  costPerKmType1?: number
  /** 支线(类型2)默认每公里成本（元） */
  costPerKmType2?: number
  /** 接驳路线(类型3)默认每公里成本（元） */
  costPerKmType3?: number
  organId?: number | null
}

export const dispatchApi = {
  getLineList(params?: {
    keyword?: string
    code?: string
    name?: string
    type?: string
    startOrganizationId?: string
    endOrganizationId?: string
    status?: number
    pageNum?: number
    pageSize?: number
  }) {
    return request.get<PageResult<DispatchLineDto>>('/dispatch/line/page', {
      params: {
        pageNum: params?.pageNum,
        pageSize: params?.pageSize,
        keyword: params?.keyword || params?.name || params?.code,
        status: params?.status
      }
    })
  },

  getLineDetail(id: string) {
    return request.get<DispatchLineDto>(`/dispatch/line/${id}`)
  },

  createLine(data: Partial<DispatchLineDto>) {
    return request.post<void>('/dispatch/line', normalizeLineBody(data))
  },

  updateLine(id: string, data: Partial<DispatchLineDto>) {
    return request.put<void>(`/dispatch/line/${id}`, normalizeLineBody(data))
  },

  deleteLine(id: string) {
    return request.delete<void>(`/dispatch/line/${id}`)
  },

  getLineTrips(lineId: string | number) {
    return request.get<unknown[]>(`/dispatch/line/${lineId}/trips`)
  },

  createTrip(lineId: string | number, data: Record<string, unknown>) {
    return request.post<void>(`/dispatch/line/${lineId}/trips`, data)
  },

  updateTrip(tripId: string | number, data: Record<string, unknown>) {
    return request.put<void>(`/dispatch/line/trips/${tripId}`, data)
  },

  deleteTrip(tripId: string | number) {
    return request.delete<void>(`/dispatch/line/trips/${tripId}`)
  },

  getTripVehicles(tripId: string | number) {
    return request.get<TripVehicleRow[]>(`/dispatch/line/trips/${tripId}/vehicles`)
  },

  getEligibleTripVehicles(tripId: string | number, keyword?: string) {
    return request.get<Vehicle[]>(`/dispatch/line/trips/${tripId}/eligible-vehicles`, {
      params: { keyword }
    })
  },

  addTripVehicle(tripId: string | number, vehicleId: string | number) {
    return request.post<void>(`/dispatch/line/trips/${tripId}/vehicles`, {}, {
      params: { vehicleId }
    })
  },

  removeTripVehicle(tripId: string | number, vehicleId: string | number) {
    return request.delete<void>(`/dispatch/line/trips/${tripId}/vehicles/${vehicleId}`)
  },

  /** 全局：路线类型成本 + 调度规则 */
  getGlobalDispatchConfig() {
    return request.get<GlobalDispatchConfigDto>('/dispatch/config/global')
  },

  saveGlobalDispatchConfig(data: GlobalDispatchConfigDto) {
    return request.put<void>('/dispatch/config/global', data)
  },

  getTransportTaskList(params?: {
    taskNo?: string
    keyword?: string
    status?: string | number
    routeId?: string
    startOrganizationId?: string
    endOrganizationId?: string
    scheduledDepartureTimeStart?: string
    scheduledDepartureTimeEnd?: string
    pageNum?: number
    pageSize?: number
  }) {
    return request.get<PageResult<TransportTask>>('/dispatch/transport/page', {
      params: {
        pageNum: params?.pageNum,
        pageSize: params?.pageSize,
        keyword: params?.keyword || params?.taskNo,
        status:
          params?.status !== undefined && params?.status !== '' ? Number(params.status) : undefined
      }
    })
  },

  getTransportTaskDetail(id: string) {
    return request.get<TransportTask>(`/dispatch/transport/${id}`)
  },

  updateTransportTaskStatus(id: string, status: number) {
    return request.put<void>(`/dispatch/transport/${id}/status`, null, { params: { status } })
  },

  getPickupTaskList(params?: {
    waybillNo?: string
    keyword?: string
    status?: string | number
    courierId?: string
    courierName?: string
    senderName?: string
    senderProvince?: string
    pageNum?: number
    pageSize?: number
  }) {
    return request.get<PageResult<PickupTask>>('/biz/task/pickup/page', {
      params: {
        pageNum: params?.pageNum,
        pageSize: params?.pageSize,
        keyword: params?.keyword || params?.waybillNo,
        status:
          params?.status !== undefined && params?.status !== '' ? Number(params.status) : undefined
      }
    })
  },

  assignPickupTask(taskId: string | number, courierId: number) {
    return request.put<void>(`/biz/task/pickup/${taskId}/assign`, null, { params: { courierId } })
  },

  cancelPickupTask(taskId: string | number, reason: string) {
    return request.post<void>(`/biz/task/pickup/${taskId}/cancel`, { reason })
  },

  getDeliveryTaskList(params?: {
    waybillNo?: string
    keyword?: string
    status?: string | number
    courierId?: string
    courierName?: string
    receiverName?: string
    receiverProvince?: string
    pageNum?: number
    pageSize?: number
  }) {
    return request.get<PageResult<DeliveryTask>>('/biz/task/delivery/page', {
      params: {
        pageNum: params?.pageNum,
        pageSize: params?.pageSize,
        keyword: params?.keyword || params?.waybillNo,
        status:
          params?.status !== undefined && params?.status !== '' ? Number(params.status) : undefined
      }
    })
  },

  assignDeliveryTask(taskId: string | number, courierId: number) {
    return request.put<void>(`/biz/task/delivery/${taskId}/assign`, null, { params: { courierId } })
  },

  /** 回车登记分页 */
  getReturnRegisterPage(params?: { pageNum?: number; pageSize?: number }) {
    return request.get<PageResult<Record<string, unknown>>>('/dispatch/transport/return/page', {
      params
    })
  }
}

function normalizeLineBody(data: Partial<DispatchLineDto>) {
  const body = { ...data } as Record<string, unknown>
  if (body.startOrganId !== undefined && body.startOrganId !== '') {
    body.startOrganId = Number(body.startOrganId)
  }
  if (body.endOrganId !== undefined && body.endOrganId !== '') {
    body.endOrganId = Number(body.endOrganId)
  }
  return body
}
