import { request } from './request'

export interface AddressBook {
  id?: number
  userId?: number
  name: string
  phone: string
  provinceId?: number
  cityId?: number
  countyId?: number
  provinceName?: string
  cityName?: string
  countyName?: string
  address: string
  isDefault?: number
  createdTime?: string
}

export interface UserInfo {
  id: number
  name: string
  phone: string
  avatar: string
  gender: number
}

export function getUserInfo() {
  return request<UserInfo>({ url: '/app/user' })
}

// ====== 地址簿 ======

export function listAddress(keyword?: string) {
  return request<AddressBook[]>({
    url: '/app/address',
    data: keyword ? { keyword } : undefined
  })
}

export function getAddress(id: number) {
  return request<AddressBook>({ url: `/app/address/${id}` })
}

export function createAddress(data: AddressBook) {
  return request<void>({ url: '/app/address', method: 'POST', data: data as unknown as Record<string, unknown> })
}

export function updateAddress(id: number, data: AddressBook) {
  return request<void>({ url: `/app/address/${id}`, method: 'PUT', data: data as unknown as Record<string, unknown> })
}

export function deleteAddress(id: number) {
  return request<void>({ url: `/app/address/${id}`, method: 'DELETE' })
}

export function deleteAddressBatch(ids: number[]) {
  return request<void>({ url: '/app/address/batch', method: 'DELETE', data: ids as unknown as Record<string, unknown> })
}

export function setDefaultAddress(id: number) {
  return request<void>({ url: `/app/address/${id}/default`, method: 'PUT' })
}
