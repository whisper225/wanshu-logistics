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
    return request.get<PageResult<VehicleType>>('/base/vehicle/type/page', {
      params: {
        pageNum: params?.pageNum,
        pageSize: params?.pageSize,
        keyword: params?.name || params?.code
      }
    })
  },

  createType(data: Partial<VehicleType>) {
    return request.post<void>('/base/vehicle/type', data)
  },

  updateType(id: string, data: Partial<VehicleType>) {
    return request.put<void>(`/base/vehicle/type/${id}`, data)
  },

  deleteType(id: string) {
    return request.delete<void>(`/base/vehicle/type/${id}`)
  },

  /** 车型下拉：拉取一页全量 */
  getAllTypes() {
    return request.get<PageResult<VehicleType>>('/base/vehicle/type/page', {
      params: { pageNum: 1, pageSize: 500 }
    }).then((res) => res.list)
  },

  getList(params?: {
    keyword?: string
    licensePlate?: string
    status?: string | number
    pageNum?: number
    pageSize?: number
  }) {
    const kw = params?.keyword ?? params?.licensePlate
    return request.get<PageResult<Vehicle>>('/base/vehicle/page', {
      params: {
        pageNum: params?.pageNum,
        pageSize: params?.pageSize,
        keyword: kw,
        status: params?.status !== undefined && params?.status !== '' ? Number(params.status) : undefined
      }
    })
  },

  getDetail(id: string) {
    return request.get<VehicleDetail>(`/base/vehicle/${id}`)
  },

  create(data: Partial<Vehicle>) {
    return request.post<void>('/base/vehicle', data)
  },

  update(id: string, data: Partial<Vehicle>) {
    return request.put<void>(`/base/vehicle/${id}`, data)
  },

  delete(id: string) {
    return request.delete<void>(`/base/vehicle/${id}`)
  },

  /** 仅更新 status（后端 Lambda 更新，不误伤其它字段） */
  updateVehicleStatus(id: string, status: number) {
    return request.put<void>(`/base/vehicle/${id}/status`, { status })
  },

  /** 已绑定司机用户 ID 列表（与 sys_user.id 一致） */
  getDriverIds(vehicleId: string) {
    return request.get<(string | number)[]>(`/base/vehicle/${vehicleId}/drivers`)
  },

  bindDrivers(id: string, driverIds: string[]) {
    return Promise.all(driverIds.map((driverId) => request.post<void>(`/base/vehicle/${id}/drivers/${driverId}`)))
  },

  unbindDriver(id: string, driverId: string) {
    return request.delete<void>(`/base/vehicle/${id}/drivers/${driverId}`)
  }
}
