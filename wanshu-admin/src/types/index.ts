export interface LoginForm {
  account: string
  password: string
  captcha: string
}

export interface UserInfo {
  id: string
  account: string
  name: string
  phone: string
  avatar?: string
  organizationId: string
  organizationName: string
  roles: string[]
  permissions: string[]
}

export interface Organization {
  id: string
  name: string
  type: 'HEADQUARTERS' | 'BRANCH' | 'PRIMARY_HUB' | 'SECONDARY_HUB' | 'STATION'
  parentId?: string
  address: {
    province: string
    city: string
    district: string
    detail: string
  }
  manager: string
  managerPhone: string
  contact: string
  contactPhone: string
  latitude: number
  latitudeDirection: 'N' | 'S'
  longitude: number
  longitudeDirection: 'E' | 'W'
  status: 'ACTIVE' | 'INACTIVE'
  children?: Organization[]
}

export interface ServiceArea {
  organizationId: string
  areas: {
    province: string
    cities: {
      city: string
      districts: string[]
    }[]
  }[]
}

/** 与后端 BaseFreightTemplate + economicZoneIds 一致 */
export interface PricingTemplate {
  id?: string
  templateName: string
  /** 1同城 2省内 3跨省 4经济区互寄 */
  templateType: number
  firstWeight: number
  firstWeightPrice: number
  extraWeight: number
  extraWeightPrice: number
  /** 可选；不填则按类型/经济区默认轻抛 */
  lightThrowRatio?: number | null
  status?: number
  economicZoneIds?: string[]
  createdTime?: string
  updatedTime?: string
}

export interface EconomicZoneItem {
  id: string
  zoneName: string
  provinces: string
  lightThrowRatio?: number
}

/** 与后端 BaseVehicleType 一致 */
export interface VehicleType {
  id?: string | number
  typeNumber?: string
  typeName?: string
  loadWeight?: number
  loadVolume?: number
  length?: number
  width?: number
  height?: number
  createdTime?: string
  updatedTime?: string
}

/** 与后端 BaseVehicle 一致 */
export interface Vehicle {
  id?: string | number
  vehicleNumber?: string
  vehicleTypeId?: string | number
  licensePlate?: string
  loadWeight?: number
  loadVolume?: number
  organId?: string | number
  /** 0=停用 1=可用 */
  status?: number
  licenseImage?: string
  createdTime?: string
  updatedTime?: string
}

export interface VehicleDetail extends Vehicle {
  registrationInfo?: {
    owner?: string
    vehicleModel?: string
    engineNumber?: string
    vinNumber?: string
    registrationDate?: string
    issueDate?: string
    images?: string[]
  }
}

export interface Courier {
  id: string
  account: string
  name: string
  phone: string
  age: number
  avatar?: string
  organizationId: string
  organizationName: string
  serviceArea?: ServiceArea
  status: 'ACTIVE' | 'INACTIVE'
  createdAt: string
  /** 与后端 emp_courier.workStatus 一致 */
  workStatus?: number
}

export interface Driver {
  id: string
  account: string
  name: string
  phone: string
  age: number
  avatar?: string
  organizationId: string
  organizationName: string
  vehicleTypes: string[]
  vehicleId?: string
  vehiclePlate?: string
  status: 'ACTIVE' | 'INACTIVE'
  /** 与后端 emp_driver.workStatus 一致 */
  workStatus?: number
  licenseInfo?: {
    licenseNumber: string
    licenseType: string
    issueDate: string
    expiryDate: string
    images: string[]
  }
  createdAt: string
}

export interface Route {
  id: string
  code: string
  name: string
  type: 'TRUNK' | 'BRANCH' | 'DEDICATED' | 'TEMPORARY' | 'DISTRIBUTION'
  startOrganizationId: string
  startOrganizationName: string
  endOrganizationId: string
  endOrganizationName: string
  distance: number
  estimatedDuration: number
  status: 'ACTIVE' | 'INACTIVE'
  shifts: RouteShift[]
  createdAt: string
}

export interface RouteShift {
  id: string
  routeId: string
  code: string
  name: string
  departureTime: string
  arrivalTime: string
  frequency: 'DAILY' | 'WEEKLY' | 'MONTHLY' | 'ONCE'
  frequencyDetail?: string
  vehicles: Vehicle[]
  status: 'ACTIVE' | 'INACTIVE'
}

export interface Order {
  id: string
  orderNo: string
  senderName: string
  senderPhone: string
  senderAddress: {
    province: string
    city: string
    district: string
    detail: string
  }
  receiverName: string
  receiverPhone: string
  receiverAddress: {
    province: string
    city: string
    district: string
    detail: string
  }
  goodsInfo: {
    name: string
    category: string
    weight: number
    volume: number
  }
  pickupTime: string
  paymentMethod: 'PREPAID' | 'COLLECT'
  estimatedFee: number
  actualFee?: number
  status: 'PENDING' | 'CONFIRMED' | 'PICKED_UP' | 'CANCELLED' | 'CLOSED'
  createdAt: string
  updatedAt: string
}

export interface Waybill {
  id: string
  waybillNo: string
  orderId: string
  orderNo: string
  senderName: string
  senderPhone: string
  senderAddress: {
    province: string
    city: string
    district: string
    detail: string
  }
  receiverName: string
  receiverPhone: string
  receiverAddress: {
    province: string
    city: string
    district: string
    detail: string
  }
  goodsInfo: {
    name: string
    category: string
    weight: number
    volume: number
  }
  fee: number
  status: 'PENDING_DISPATCH' | 'DISPATCHED' | 'DISPATCH_FAILED' | 'IN_TRANSIT' | 'DELIVERED' | 'REJECTED'
  dispatchConfig?: DispatchConfig
  route?: string[]
  currentLocation?: string
  createdAt: string
  updatedAt: string
}

export interface DispatchConfig {
  maxDispatchHours: number
  maxAllocationTime: 'SAME_DAY' | 'NEXT_DAY'
  priority: 'MIN_TRANSFER' | 'MIN_COST'
  secondaryPriority?: 'MIN_TRANSFER' | 'MIN_COST'
}

export interface TransportTask {
  id: string
  taskNo: string
  routeId: string
  routeName: string
  shiftId: string
  shiftName: string
  vehicleId: string
  vehiclePlate: string
  driverId: string
  driverName: string
  waybillCount: number
  totalWeight: number
  totalVolume: number
  scheduledDepartureTime: string
  actualDepartureTime?: string
  scheduledArrivalTime: string
  actualArrivalTime?: string
  status: 'ASSIGNED' | 'IN_PROGRESS' | 'COMPLETED' | 'TIMEOUT'
  pickupImages?: string[]
  deliveryImages?: string[]
  createdAt: string
  updatedAt: string
}

export interface PickupTask {
  id: string
  waybillId: string
  waybillNo: string
  courierId?: string
  courierName?: string
  senderName: string
  senderPhone: string
  senderAddress: {
    province: string
    city: string
    district: string
    detail: string
  }
  goodsInfo: {
    name: string
    category: string
    weight: number
    volume: number
  }
  scheduledTime: string
  actualTime?: string
  status: 'UNASSIGNED' | 'ASSIGNED' | 'PICKED_UP' | 'CANCELLED'
  createdAt: string
}

export interface DeliveryTask {
  id: string
  waybillId: string
  waybillNo: string
  courierId?: string
  courierName?: string
  receiverName: string
  receiverPhone: string
  receiverAddress: {
    province: string
    city: string
    district: string
    detail: string
  }
  goodsInfo: {
    name: string
    category: string
    weight: number
    volume: number
  }
  status: 'UNASSIGNED' | 'ASSIGNED' | 'DELIVERED' | 'REJECTED' | 'CANCELLED'
  deliveryTime?: string
  signature?: string
  createdAt: string
}

export interface User {
  id: string
  account: string
  name: string
  phone: string
  email?: string
  avatar?: string
  organizationId: string
  organizationName: string
  roles: Role[]
  status: 'ACTIVE' | 'INACTIVE'
  createdAt: string
  updatedAt: string
}

export interface Role {
  id: string
  name: string
  code: string
  description?: string
  permissions: Permission[]
  allowedPlatforms: ('ADMIN' | 'COURIER_APP' | 'DRIVER_APP')[]
  status: 'ACTIVE' | 'INACTIVE'
  createdAt: string
}

export interface Permission {
  id: string
  name: string
  code: string
  type: 'MENU' | 'BUTTON'
  parentId?: string
  path?: string
  children?: Permission[]
}

/** 与后端 SysOperationLog 一致 */
export interface OperationLog {
  id?: string
  module?: string
  operation?: string
  method?: string
  requestUrl?: string
  requestParams?: string
  responseData?: string
  operatorId?: string
  operatorName?: string
  operatorIp?: string
  costTime?: number
  status?: number
  createdTime?: string
}

/** 与后端 SysExceptionLog 一致 */
export interface ExceptionLog {
  id?: string
  module?: string
  requestUrl?: string
  requestParams?: string
  exceptionName?: string
  exceptionMsg?: string
  stackTrace?: string
  operatorId?: string
  operatorName?: string
  operatorIp?: string
  createdTime?: string
}

export interface DashboardStats {
  organizationInfo: {
    name: string
    type: string
    subOrganizationCount: number
    employeeCount: number
  }
  todayData: {
    orderAmount: number
    orderCount: number
    transportTaskCount: number
  }
  pickupTasks: {
    unassigned: number
    assigned: number
    completed: number
  }
  transportTasks: {
    pending: number
    inProgress: number
    completed: number
  }
  deliveryTasks: {
    unassigned: number
    assigned: number
    completed: number
  }
  recentOrders: {
    dates: string[]
    counts: number[]
  }
  orderDistribution: {
    province: string
    count: number
  }[]
}

export interface PageResult<T> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
}

export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}
