import { request } from '../../utils/request.js'

/** 运输任务列表 status: 0=待出发 1=运输中 2=已完成 */
export const getTransportTasks = (status) =>
  request({
    url: '/driver/transport',
    method: 'GET',
    params: { status }
  })

/** 运输任务详情 */
export const getTransportDetail = (taskId) =>
  request({
    url: `/driver/transport/${taskId}`,
    method: 'GET',
    params: {}
  })

/** 出库确认（开始运输） */
export const confirmDeparture = (taskId) =>
  request({
    url: `/driver/transport/${taskId}/depart`,
    method: 'POST',
    params: {}
  })

/** 入库确认（完成运输） */
export const confirmArrival = (taskId) =>
  request({
    url: `/driver/transport/${taskId}/arrive`,
    method: 'POST',
    params: {}
  })

/** 回车登记 */
export const createReturnRegister = (taskId, description, images) =>
  request({
    url: `/driver/transport/${taskId}/return`,
    method: 'POST',
    params: {
      ...(description ? { description } : {}),
      ...(images ? { images } : {})
    }
  })

/** 上报实时位置 */
export const reportLocation = (waybillId, longitude, latitude) =>
  request({
    url: '/driver/location',
    method: 'POST',
    params: { waybillId, longitude, latitude }
  })
