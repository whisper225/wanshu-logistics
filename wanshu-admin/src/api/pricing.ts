import { request } from '@/utils/request'
import type { EconomicZoneItem, PricingTemplate, PageResult } from '@/types'

export const pricingApi = {
  getList(params?: {
    name?: string
    type?: string
    pageNum?: number
    pageSize?: number
  }) {
    const typeNum =
      params?.type && /^\d+$/.test(String(params.type)) ? Number(params.type) : undefined
    return request.get<PageResult<PricingTemplate>>('/base/freight/page', {
      params: {
        pageNum: params?.pageNum,
        pageSize: params?.pageSize,
        name: params?.name,
        type: typeNum
      }
    })
  },

  getEconomicZones() {
    return request.get<EconomicZoneItem[]>('/base/freight/economic-zones')
  },

  getDetail(id: string) {
    return request.get<PricingTemplate>(`/base/freight/${id}`)
  },

  create(data: Partial<PricingTemplate>) {
    return request.post<void>('/base/freight', data)
  },

  update(id: string, data: Partial<PricingTemplate>) {
    return request.put<void>(`/base/freight/${id}`, data)
  },

  delete(id: string) {
    return request.delete<void>(`/base/freight/${id}`)
  },

  calculate(params: {
    templateId: string
    weight: number
    volume?: number
    length?: number
    width?: number
    height?: number
  }) {
    return request.get<Record<string, unknown>>('/base/freight/calculate', {
      params: {
        templateId: params.templateId,
        weight: params.weight,
        volume: params.volume,
        length: params.length,
        width: params.width,
        height: params.height
      }
    })
  }
}
