import { request } from '@/utils/request'
import type { PageResult, Vehicle } from '@/types'

/** 与后端 EmpCourierAdmin 列表/详情 VO 一致 */
export interface EmpCourierVO {
  id: string | number
  username?: string
  realName?: string
  phone?: string
  organId?: string | number
  organName?: string
  employeeNo?: string
  workStatus?: number
  createdTime?: string
}

/** 与后端 EmpDriverAdmin VO 一致 */
export interface EmpDriverVO {
  id: string | number
  username?: string
  realName?: string
  phone?: string
  organId?: string | number
  organName?: string
  vehicleTypes?: string
  licenseImage?: string
  workStatus?: number
  createdTime?: string
}

/** 快递员作业范围（与 emp_courier_scope / OrganScope 字段一致） */
export interface EmpCourierScopeRow {
  id?: number
  courierId?: number
  provinceId?: number
  cityId?: number
  countyId?: number
}

function toOrganIdParam(v: string | number | undefined): number | undefined {
  if (v === undefined || v === null || v === '') return undefined
  const n = typeof v === 'number' ? v : Number(v)
  return Number.isFinite(n) ? n : undefined
}

function toIdNum(id: string | number): number {
  const n = typeof id === 'number' ? id : Number(id)
  return n
}

export const employeeApi = {
  getCourierList(params?: {
    keyword?: string
    organId?: string | number
    pageNum?: number
    pageSize?: number
    workStatus?: number
  }) {
    return request.get<PageResult<EmpCourierVO>>('/emp/courier/page', {
      params: {
        pageNum: params?.pageNum,
        pageSize: params?.pageSize,
        organId: toOrganIdParam(params?.organId),
        workStatus: params?.workStatus,
        keyword: params?.keyword
      }
    })
  },

  getCourierDetail(id: string | number) {
    return request.get<EmpCourierVO>(`/emp/courier/${id}`)
  },

  getCourierScopes(id: string | number) {
    return request.get<EmpCourierScopeRow[]>(`/emp/courier/${id}/scopes`)
  },

  updateCourierScopes(id: string | number, scopes: EmpCourierScopeRow[]) {
    return request.put<void>(`/emp/courier/${id}/scopes`, scopes)
  },

  getDriverList(params?: {
    keyword?: string
    organId?: string | number
    pageNum?: number
    pageSize?: number
    workStatus?: number
  }) {
    return request.get<PageResult<EmpDriverVO>>('/emp/driver/page', {
      params: {
        pageNum: params?.pageNum,
        pageSize: params?.pageSize,
        organId: toOrganIdParam(params?.organId),
        workStatus: params?.workStatus,
        keyword: params?.keyword
      }
    })
  },

  getDriverDetail(id: string | number) {
    return request.get<EmpDriverVO>(`/emp/driver/${id}`)
  },

  /** 司机已绑定的车辆 */
  getDriverBoundVehicles(driverId: string | number) {
    return request.get<Vehicle[]>('/emp/driver/bound-vehicles', {
      params: { driverId: toIdNum(driverId) }
    })
  },

  /**
   * 绑定车辆（后端校验：资料完整、上班/排班、车辆须为停用非可用）
   */
  bindDriverVehicle(driverId: string | number, vehicleId: string | number) {
    return request.post<void>('/emp/driver/bind-vehicle', {
      driverId: toIdNum(driverId),
      vehicleId: toIdNum(vehicleId)
    })
  },

  unbindDriverVehicle(driverId: string | number, vehicleId: string | number) {
    return request.delete<void>('/emp/driver/unbind-vehicle', {
      params: { driverId: toIdNum(driverId), vehicleId: toIdNum(vehicleId) }
    })
  }
}
