import { request } from '@/utils/request'
import type { Order, Waybill, DispatchConfig, PageResult } from '@/types'

export const businessApi = {
  getOrderList(params?: {
    orderNo?: string
    status?: string
    senderName?: string
    senderPhone?: string
    receiverName?: string
    receiverPhone?: string
    senderProvince?: string
    receiverProvince?: string
    pageNum?: number
    pageSize?: number
  }) {
    return request.get<PageResult<Order>>('/orders', { params })
  },

  getOrderDetail(id: string) {
    return request.get<Order>(`/orders/${id}`)
  },

  updateOrder(id: string, data: Partial<Order>) {
    return request.put<Order>(`/orders/${id}`, data)
  },

  cancelOrder(id: string, reason: string) {
    return request.post(`/orders/${id}/cancel`, { reason })
  },

  refundOrder(id: string) {
    return request.post(`/orders/${id}/refund`)
  },

  getWaybillList(params?: {
    waybillNo?: string
    status?: string
    senderName?: string
    senderPhone?: string
    receiverName?: string
    receiverPhone?: string
    senderProvince?: string
    receiverProvince?: string
    pageNum?: number
    pageSize?: number
  }) {
    return request.get<PageResult<Waybill>>('/waybills', { params })
  },

  getWaybillDetail(id: string) {
    return request.get<Waybill>(`/waybills/${id}`)
  },

  getDispatchConfig() {
    return request.get<DispatchConfig>('/waybills/dispatch-config')
  },

  updateDispatchConfig(data: DispatchConfig) {
    return request.put<DispatchConfig>('/waybills/dispatch-config', data)
  },

  dispatchWaybill(id: string) {
    return request.post(`/waybills/${id}/dispatch`)
  },

  batchDispatch(ids: string[]) {
    return request.post('/waybills/batch-dispatch', { ids })
  },

  getWaybillTrack(id: string) {
    return request.get<any[]>(`/waybills/${id}/track`)
  }
}
