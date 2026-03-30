import { request } from './request'

export interface OrderItem {
  type: 'send' | 'receive'
  id: number
  /** 收件列表：关联订单 ID，用于拉取订单详情 */
  orderId?: number
  number: string
  status: number
  statusText: string
  senderName: string
  senderPhone: string
  receiverName: string
  receiverPhone: string
  goodsName: string
  /** 寄件列表：估算运费 */
  estimatedFee?: number
  /** 收件列表：运费字段名与后端一致 */
  freight?: number
  actualFee?: number
  createdTime: string
}

export interface OrderDetail {
  id: number
  orderNumber: string
  senderName: string
  senderPhone: string
  senderAddress: string
  receiverName: string
  receiverPhone: string
  receiverAddress: string
  goodsName: string
  goodsType: string
  weight: number
  volume: number
  paymentMethod: number
  estimatedFee: number
  actualFee: number
  pickupTime: string
  status: number
  cancelReason: string
  createdTime: string
}

export interface WaybillDetail {
  id: number
  waybillNumber: string
  orderId: number
  senderName: string
  senderPhone: string
  senderAddress: string
  receiverName: string
  receiverPhone: string
  receiverAddress: string
  goodsName: string
  weight: number
  freight: number
  status: number
  signTime: string
  createdTime: string
}

export interface CreateOrderParams {
  senderName: string
  senderPhone: string
  senderAddress: string
  senderProvinceId?: number
  senderCityId?: number
  senderCountyId?: number
  receiverName: string
  receiverPhone: string
  receiverAddress: string
  receiverProvinceId?: number
  receiverCityId?: number
  receiverCountyId?: number
  goodsName: string
  goodsType?: string
  weight?: number
  volume?: number
  paymentMethod: number
  estimatedFee?: number
  pickupTime?: string
}

export function listOrders(params?: { keyword?: string; type?: number; pageNum?: number; pageSize?: number }) {
  return request<{ list: OrderItem[]; total: number }>({
    url: '/app/order',
    data: params as unknown as Record<string, unknown>
  })
}

export function getOrderDetail(id: number) {
  return request<OrderDetail>({ url: `/app/order/${id}` })
}

export function getOrderWaybill(id: number) {
  return request<WaybillDetail | null>({ url: `/app/order/${id}/waybill` })
}

export function createOrder(data: CreateOrderParams) {
  return request<OrderDetail>({ url: '/app/order', method: 'POST', data: data as unknown as Record<string, unknown> })
}

export function cancelOrder(id: number, reason?: string) {
  return request<void>({
    url: `/app/order/${id}/cancel`,
    method: 'POST',
    data: { reason }
  })
}

export function deleteOrder(id: number) {
  return request<void>({ url: `/app/order/${id}`, method: 'DELETE' })
}

export function estimateFreight(weight: number, goodsType?: string) {
  return request<{ estimatedFee: number; note: string }>({
    url: '/app/freight/estimate',
    data: { weight, goodsType } as unknown as Record<string, unknown>
  })
}
