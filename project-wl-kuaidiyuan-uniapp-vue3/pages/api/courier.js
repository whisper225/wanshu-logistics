import { request } from '../../utils/request.js'

/** 揽收任务列表 status: 0=待分配 1=已分配 2=已完成 3=已取消 */
export const getPickupTasks = (status) =>
  request({
    url: '/courier/pickup',
    method: 'GET',
    params: { status }
  })

/** 确认揽收 */
export const completePickup = (taskId, remark) =>
  request({
    url: `/courier/pickup/${taskId}/complete`,
    method: 'POST',
    params: remark ? { remark } : {}
  })

/** 派件任务列表 status: 0=待分配 1=已分配 2=已签收 3=已拒收 */
export const getDeliveryTasks = (status) =>
  request({
    url: '/courier/delivery',
    method: 'GET',
    params: { status }
  })

/** 确认签收 */
export const signDelivery = (taskId, signType = 0, signImage) =>
  request({
    url: `/courier/delivery/${taskId}/sign`,
    method: 'POST',
    params: { signType, ...(signImage ? { signImage } : {}) }
  })

/** 确认拒收 */
export const rejectDelivery = (taskId, reason) =>
  request({
    url: `/courier/delivery/${taskId}/reject`,
    method: 'POST',
    params: reason ? { reason } : {}
  })

/** 上报实时位置 */
export const reportLocation = (waybillId, longitude, latitude) =>
  request({
    url: '/courier/location',
    method: 'POST',
    params: { waybillId, longitude, latitude }
  })
