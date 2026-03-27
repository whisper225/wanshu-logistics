import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { setupRouterGuard } from './guard'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/layouts/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '工作台', icon: 'DataAnalysis' },
      },
      {
        path: 'basic',
        name: 'Basic',
        meta: { title: '基础数据管理', icon: 'Setting' },
        children: [
          {
            path: 'organization',
            name: 'Organization',
            component: () => import('@/views/basic/organization/index.vue'),
            meta: { title: '机构管理' },
          },
          {
            path: 'service-area',
            name: 'ServiceArea',
            component: () => import('@/views/basic/service-area/index.vue'),
            meta: { title: '机构作业范围管理' },
          },
          {
            path: 'pricing',
            name: 'Pricing',
            component: () => import('@/views/basic/pricing/index.vue'),
            meta: { title: '运费管理' },
          },
        ],
      },
      {
        path: 'vehicle',
        name: 'Vehicle',
        meta: { title: '车辆管理', icon: 'Van' },
        children: [
          {
            path: 'type',
            name: 'VehicleType',
            component: () => import('@/views/vehicle/type/index.vue'),
            meta: { title: '车型管理' },
          },
          {
            path: 'list',
            name: 'VehicleList',
            component: () => import('@/views/vehicle/list/index.vue'),
            meta: { title: '车辆管理' },
          },
          {
            path: 'detail/:id',
            name: 'VehicleDetail',
            component: () => import('@/views/vehicle/detail/index.vue'),
            meta: { title: '车辆详情', hidden: true },
          },
          {
            path: 'return-log',
            name: 'ReturnLog',
            component: () => import('@/views/vehicle/return-log/index.vue'),
            meta: { title: '回车登记' },
          },
        ],
      },
      {
        path: 'employee',
        name: 'Employee',
        meta: { title: '员工管理', icon: 'User' },
        children: [
          {
            path: 'courier',
            name: 'Courier',
            component: () => import('@/views/employee/courier/index.vue'),
            meta: { title: '快递员管理' },
          },
          {
            path: 'courier/:id',
            name: 'CourierDetail',
            component: () => import('@/views/employee/courier/detail.vue'),
            meta: { title: '快递员详情', hidden: true },
          },
          {
            path: 'driver',
            name: 'Driver',
            component: () => import('@/views/employee/driver/index.vue'),
            meta: { title: '司机管理' },
          },
          {
            path: 'driver/:id',
            name: 'DriverDetail',
            component: () => import('@/views/employee/driver/detail.vue'),
            meta: { title: '司机详情', hidden: true },
          },
          {
            path: 'schedule',
            name: 'Schedule',
            component: () => import('@/views/employee/schedule/index.vue'),
            meta: { title: '排班管理' },
          },
        ],
      },
      {
        path: 'business',
        name: 'Business',
        meta: { title: '业务管理', icon: 'Document' },
        children: [
          {
            path: 'waybill',
            name: 'Waybill',
            component: () => import('@/views/business/waybill/index.vue'),
            meta: { title: '运单管理' },
          },
          {
            path: 'waybill/:id',
            name: 'WaybillDetail',
            component: () => import('@/views/business/waybill/detail.vue'),
            meta: { title: '运单详情', hidden: true },
          },
          {
            path: 'order',
            name: 'Order',
            component: () => import('@/views/business/order/index.vue'),
            meta: { title: '订单管理' },
          },
          {
            path: 'order/:id',
            name: 'OrderDetail',
            component: () => import('@/views/business/order/detail.vue'),
            meta: { title: '订单详情', hidden: true },
          },
        ],
      },
      {
        path: 'dispatch',
        name: 'Dispatch',
        meta: { title: '调度管理', icon: 'Position' },
        children: [
          {
            path: 'route',
            name: 'Route',
            component: () => import('@/views/dispatch/route/index.vue'),
            meta: { title: '线路管理' },
          },
          {
            path: 'transport-task',
            name: 'TransportTask',
            component: () => import('@/views/dispatch/transport-task/index.vue'),
            meta: { title: '运输任务管理' },
          },
          {
            path: 'transport-task/:id',
            name: 'TransportTaskDetail',
            component: () => import('@/views/dispatch/transport-task/detail.vue'),
            meta: { title: '运输任务详情', hidden: true },
          },
          {
            path: 'pickup-task',
            name: 'PickupTask',
            component: () => import('@/views/dispatch/pickup-task/index.vue'),
            meta: { title: '取件作业管理' },
          },
          {
            path: 'delivery-task',
            name: 'DeliveryTask',
            component: () => import('@/views/dispatch/delivery-task/index.vue'),
            meta: { title: '派件作业管理' },
          },
        ],
      },
      {
        path: 'system',
        name: 'System',
        meta: { title: '后台管理', icon: 'Tools' },
        children: [
          {
            path: 'user',
            name: 'User',
            component: () => import('@/views/system/user/index.vue'),
            meta: { title: '用户管理' },
          },
          {
            path: 'role',
            name: 'Role',
            component: () => import('@/views/system/role/index.vue'),
            meta: { title: '角色管理' },
          },
          {
            path: 'organization-manage',
            name: 'OrganizationManage',
            component: () => import('@/views/system/organization/index.vue'),
            meta: { title: '组织管理' },
          },
        ],
      },
      {
        path: 'log',
        name: 'Log',
        meta: { title: '日志管理', icon: 'Document' },
        children: [
          {
            path: 'operation',
            name: 'OperationLog',
            component: () => import('@/views/log/operation/index.vue'),
            meta: { title: '操作日志' },
          },
          {
            path: 'exception',
            name: 'ExceptionLog',
            component: () => import('@/views/log/exception/index.vue'),
            meta: { title: '异常日志' },
          },
        ],
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

setupRouterGuard(router)

export default router
