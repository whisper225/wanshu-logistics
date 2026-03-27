import { request } from '@/utils/request'
import type { PricingTemplate, PageResult } from '@/types'

export const pricingApi = {
  getList(params?: {
    name?: string
    type?: string
    pageNum?: number
    pageSize?: number
  }) {
    return request.get<PageResult<PricingTemplate>>('/pricing/templates', { params })
  },

  getDetail(id: string) {
    return request.get<PricingTemplate>(`/pricing/templates/${id}`)
  },

  create(data: Partial<PricingTemplate>) {
    return request.post<PricingTemplate>('/pricing/templates', data)
  },

  update(id: string, data: Partial<PricingTemplate>) {
    return request.put<PricingTemplate>(`/pricing/templates/${id}`, data)
  },

  delete(id: string) {
    return request.delete(`/pricing/templates/${id}`)
  },

  calculate(params: {
    fromProvince: string
    fromCity: string
    toProvince: string
    toCity: string
    weight: number
    volume: number
  }) {
    return request.post<{ fee: number; templateName: string }>('/pricing/calculate', params)
  }
}
