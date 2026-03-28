import { request } from '@/utils/request'
import type { Order, Waybill, PageResult } from '@/types'

export const businessApi = {
  getOrderList(params?: {
    keyword?: string
    orderNo?: string
    status?: string | number
    senderName?: string
    senderPhone?: string
    receiverName?: string
    receiverPhone?: string
    senderProvince?: string
    receiverProvince?: string
    pageNum?: number
    pageSize?: number
  }) {
    const keyword =
      params?.keyword ||
      [params?.orderNo, params?.senderName, params?.senderPhone, params?.receiverName, params?.receiverPhone]
        .filter(Boolean)
        .join(' ') ||
      undefined
    return request.get<PageResult<Order>>('/biz/order/page', {
      params: {
        pageNum: params?.pageNum,
        pageSize: params?.pageSize,
        keyword,
        status: params?.status !== undefined && params?.status !== '' ? Number(params.status) : undefined
      }
    })
  },

  getOrderDetail(id: string) {
    return request.get<Order>(`/biz/order/${id}`)
  },

  updateOrder(id: string, data: Partial<Order>) {
    return request.put<void>(`/biz/order/${id}`, data)
  },

  cancelOrder(id: string, reason: string) {
    return request.post<void>(`/biz/order/${id}/cancel`, { reason })
  },

  refundOrder(_id: string) {
    return Promise.reject(new Error('退款接口未在后端实现'))
  },

  getWaybillList(params?: {
    keyword?: string
    waybillNo?: string
    status?: string | number
    senderName?: string
    senderPhone?: string
    receiverName?: string
    receiverPhone?: string
    senderProvince?: string
    receiverProvince?: string
    pageNum?: number
    pageSize?: number
  }) {
    const keyword =
      params?.keyword ||
      [params?.waybillNo, params?.senderName, params?.senderPhone, params?.receiverName, params?.receiverPhone]
        .filter(Boolean)
        .join(' ') ||
      undefined
    return request.get<PageResult<Waybill>>('/biz/waybill/page', {
      params: {
        pageNum: params?.pageNum,
        pageSize: params?.pageSize,
        keyword,
        status: params?.status !== undefined && params?.status !== '' ? Number(params.status) : undefined
      }
    })
  },

  getWaybillDetail(id: string) {
    return request.get<Waybill>(`/biz/waybill/${id}`)
  },

  updateWaybill(id: string, data: Partial<Waybill>) {
    return request.put<void>(`/biz/waybill/${id}`, data)
  },

  getDispatchConfig() {
    return request.get<unknown[]>('/dispatch/config')
  },

  updateDispatchConfig(data: { id?: number } & Record<string, unknown>) {
    return request.post<void>('/dispatch/config', data)
  },

  dispatchWaybill(_id: string) {
    return Promise.reject(new Error('单运单调度接口未在后端实现，请使用调度配置'))
  },

  batchDispatch(_ids: string[]) {
    return Promise.reject(new Error('批量调度接口未在后端实现'))
  },

  getWaybillTrack(id: string) {
    return request.get<{ nodeCode?: string; description?: string; eventTime?: string }[]>(
      `/biz/waybill/${id}/track`
    )
  }
}
