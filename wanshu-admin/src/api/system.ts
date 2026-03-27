import { request } from '@/utils/request'
import type { User, Role, Permission, OperationLog, ExceptionLog, DashboardStats, PageResult } from '@/types'

export const systemApi = {
  getUserList(params?: {
    account?: string
    name?: string
    phone?: string
    organizationId?: string
    status?: string
    pageNum?: number
    pageSize?: number
  }) {
    return request.get<PageResult<User>>('/system/users', { params })
  },

  getUserDetail(id: string) {
    return request.get<User>(`/system/users/${id}`)
  },

  createUser(data: Partial<User>) {
    return request.post<User>('/system/users', data)
  },

  updateUser(id: string, data: Partial<User>) {
    return request.put<User>(`/system/users/${id}`, data)
  },

  deleteUser(id: string) {
    return request.delete(`/system/users/${id}`)
  },

  assignRoles(userId: string, roleIds: string[]) {
    return request.post(`/system/users/${userId}/roles`, { roleIds })
  },

  getRoleList(params?: {
    name?: string
    code?: string
    status?: string
    pageNum?: number
    pageSize?: number
  }) {
    return request.get<PageResult<Role>>('/system/roles', { params })
  },

  getRoleDetail(id: string) {
    return request.get<Role>(`/system/roles/${id}`)
  },

  createRole(data: Partial<Role>) {
    return request.post<Role>('/system/roles', data)
  },

  updateRole(id: string, data: Partial<Role>) {
    return request.put<Role>(`/system/roles/${id}`, data)
  },

  deleteRole(id: string) {
    return request.delete(`/system/roles/${id}`)
  },

  getPermissionTree() {
    return request.get<Permission[]>('/system/permissions/tree')
  },

  assignPermissions(roleId: string, permissionIds: string[]) {
    return request.post(`/system/roles/${roleId}/permissions`, { permissionIds })
  },

  getOperationLogs(params?: {
    userId?: string
    module?: string
    startTime?: string
    endTime?: string
    pageNum?: number
    pageSize?: number
  }) {
    return request.get<PageResult<OperationLog>>('/system/logs/operations', { params })
  },

  getExceptionLogs(params?: {
    module?: string
    startTime?: string
    endTime?: string
    pageNum?: number
    pageSize?: number
  }) {
    return request.get<PageResult<ExceptionLog>>('/system/logs/exceptions', { params })
  },

  getDashboardStats() {
    return request.get<DashboardStats>('/system/dashboard/stats')
  }
}
