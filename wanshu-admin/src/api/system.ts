import { request } from '@/utils/request'
import type { User, Role, Permission, OperationLog, ExceptionLog, DashboardStats, PageResult } from '@/types'

export const systemApi = {
  getUserList(params?: {
    account?: string
    name?: string
    phone?: string
    organizationId?: string
    keyword?: string
    status?: string | number
    pageNum?: number
    pageSize?: number
  }) {
    return request.get<PageResult<User>>('/system/user/page', {
      params: {
        pageNum: params?.pageNum,
        pageSize: params?.pageSize,
        keyword: params?.keyword || params?.account || params?.name || params?.phone,
        status:
          params?.status !== undefined && params?.status !== '' ? Number(params.status) : undefined
      }
    })
  },

  getUserDetail(id: string) {
    return request.get<User>(`/system/user/${id}`)
  },

  createUser(data: Partial<User>) {
    return request.post<void>('/system/user', data)
  },

  updateUser(id: string, data: Partial<User>) {
    return request.put<void>(`/system/user/${id}`, data)
  },

  deleteUser(id: string) {
    return request.delete<void>(`/system/user/${id}`)
  },

  getUserRoles(id: string | number) {
    return request.get<number[]>(`/system/user/${id}/roles`)
  },

  assignRoles(userId: string | number, roleIds: number[]) {
    return request.put<void>(`/system/user/${userId}/roles`, roleIds)
  },

  getRoleList(params?: {
    name?: string
    code?: string
    keyword?: string
    status?: string
    pageNum?: number
    pageSize?: number
  }) {
    return request.get<PageResult<Role>>('/system/role/page', {
      params: {
        pageNum: params?.pageNum,
        pageSize: params?.pageSize,
        keyword: params?.keyword || params?.name || params?.code
      }
    })
  },

  getRoleDetail(id: string) {
    return request.get<Role>(`/system/role/${id}`)
  },

  createRole(data: Partial<Role>) {
    return request.post<void>('/system/role', data)
  },

  updateRole(id: string, data: Partial<Role>) {
    return request.put<void>(`/system/role/${id}`, data)
  },

  deleteRole(id: string) {
    return request.delete<void>(`/system/role/${id}`)
  },

  /** 菜单树，用于角色分配权限 */
  getPermissionTree() {
    return request.get<Permission[]>('/system/menu/tree')
  },

  getRolePermissions(roleId: string | number) {
    return request.get<number[]>(`/system/role/${roleId}/menus`)
  },

  assignPermissions(roleId: string | number, menuIds: number[]) {
    return request.put<void>(`/system/role/${roleId}/menus`, menuIds)
  },

  getOperationLogs(params?: {
    keyword?: string
    startTime?: string
    endTime?: string
    pageNum?: number
    pageSize?: number
  }) {
    return request.get<PageResult<OperationLog>>('/system/log/operation/page', {
      params: {
        pageNum: params?.pageNum,
        pageSize: params?.pageSize,
        keyword: params?.keyword,
        startTime: params?.startTime,
        endTime: params?.endTime
      }
    })
  },

  getOperationLogDetail(id: string) {
    return request.get<OperationLog>(`/system/log/operation/${id}`)
  },

  getExceptionLogs(params?: {
    keyword?: string
    startTime?: string
    endTime?: string
    pageNum?: number
    pageSize?: number
  }) {
    return request.get<PageResult<ExceptionLog>>('/system/log/exception/page', {
      params: {
        pageNum: params?.pageNum,
        pageSize: params?.pageSize,
        keyword: params?.keyword,
        startTime: params?.startTime,
        endTime: params?.endTime
      }
    })
  },

  getExceptionLogDetail(id: string) {
    return request.get<ExceptionLog>(`/system/log/exception/${id}`)
  },

  getDashboardStats() {
    return request.get<DashboardStats>('/system/dashboard/stats')
  }
}
