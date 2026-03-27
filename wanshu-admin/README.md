# 万枢物流管理后台

基于 Vue 3 + TypeScript + Vite + Element Plus 的物流管理系统前端项目。

## 技术栈

- **框架**: Vue 3.4 + TypeScript 5.4
- **构建工具**: Vite 5.2
- **UI 组件库**: Element Plus 2.7
- **状态管理**: Pinia 2.1
- **路由**: Vue Router 4.3
- **HTTP 客户端**: Axios 1.6
- **图表**: ECharts 5.5

## 项目结构

```
wanshu-admin/
├── src/
│   ├── api/              # API 接口
│   │   ├── auth.ts       # 认证接口
│   │   ├── organization.ts # 机构管理
│   │   ├── pricing.ts    # 运费管理
│   │   ├── vehicle.ts    # 车辆管理
│   │   ├── employee.ts   # 员工管理
│   │   ├── business.ts   # 业务管理
│   │   ├── dispatch.ts   # 调度管理
│   │   └── system.ts     # 系统管理
│   ├── assets/           # 静态资源
│   ├── components/       # 公共组件
│   │   ├── PageContainer.vue
│   │   └── SearchForm.vue
│   ├── layouts/          # 布局组件
│   │   └── index.vue     # 主布局
│   ├── router/           # 路由配置
│   │   ├── index.ts
│   │   └── guard.ts      # 路由守卫
│   ├── stores/           # 状态管理
│   │   ├── user.ts       # 用户状态
│   │   └── app.ts        # 应用状态
│   ├── styles/           # 全局样式
│   ├── types/            # TypeScript 类型定义
│   ├── utils/            # 工具函数
│   │   ├── request.ts    # HTTP 请求封装
│   │   └── index.ts      # 通用工具
│   ├── views/            # 页面组件
│   │   ├── login/        # 登录页
│   │   ├── dashboard/    # 工作台
│   │   ├── basic/        # 基础数据管理
│   │   │   ├── organization/  # 机构管理
│   │   │   ├── service-area/  # 作业范围管理
│   │   │   └── pricing/       # 运费管理
│   │   ├── vehicle/      # 车辆管理
│   │   ├── employee/     # 员工管理
│   │   ├── business/     # 业务管理
│   │   ├── dispatch/     # 调度管理
│   │   ├── system/       # 系统管理
│   │   └── log/          # 日志管理
│   ├── App.vue
│   └── main.ts
├── .env.development      # 开发环境配置
├── .env.production       # 生产环境配置
├── index.html
├── package.json
├── tsconfig.json
├── vite.config.ts
└── README.md
```

## 功能模块

### 1. 登录模块
- 账号密码登录
- 图形验证码验证
- Token 认证

### 2. 工作台
- 机构概述（名称、类型、下属机构、员工数量）
- 今日数据（订单金额、订单数量、运输任务）
- 待办任务统计（取件、运输、派件）
- 近七日订单趋势图
- 全国订单分布地图

### 3. 基础数据管理
- **机构管理**: 树形结构展示，机构信息编辑，员工查看
- **机构作业范围管理**: 省市区三级选择，地图展示
- **运费管理**: 运费模板 CRUD，支持同城寄/省内寄/跨省寄/经济区互寄

### 4. 车辆管理
- **车型管理**: 车型 CRUD，载重/体积/尺寸配置
- **车辆管理**: 车辆 CRUD，状态管理，司机绑定
- **车辆详情**: 基本信息、行驶证信息、车次信息
- **回车登记**: 运输任务回车记录查看

### 5. 员工管理
- **快递员管理**: 快递员列表、详情查看、作业范围设置
- **司机管理**: 司机列表、详情查看、车辆绑定
- **排班管理**: 排班设置，支持连续制/礼拜制

### 6. 业务管理
- **运单管理**: 运单列表、详情、调度配置、状态跟踪
- **订单管理**: 订单列表、详情、编辑、退款、取消

### 7. 调度管理
- **线路管理**: 线路 CRUD，车次管理，车辆安排
- **运输任务管理**: 任务列表、详情、状态跟踪、运单查看
- **取件作业管理**: 取件任务分配、快递员指派
- **派件作业管理**: 派件任务分配、快递员指派

### 8. 后台管理
- **用户管理**: 用户 CRUD，角色分配
- **角色管理**: 角色 CRUD，权限配置，端登录限制
- **组织管理**: 组织树形结构管理

### 9. 日志管理
- **操作日志**: 记录用户操作行为
- **异常日志**: 记录系统异常信息

## 开发指南

### 安装依赖
```bash
npm install
```

### 开发环境运行
```bash
npm run dev
```
访问 http://localhost:3000

### 生产构建
```bash
npm run build
```

### 类型检查
```bash
npm run build
```

## 环境变量

### .env.development
```
VITE_API_BASE_URL=/api
```

### .env.production
```
VITE_API_BASE_URL=https://api.wanshu-logistics.com
```

## API 接口说明

所有 API 接口通过 Axios 统一封装，自动处理：
- Token 认证（请求头添加 Authorization）
- 错误处理（统一错误提示）
- 401 自动跳转登录
- 响应数据解包

## 权限控制

- 使用 Sa-Token 进行认证
- 支持菜单级和按钮级权限控制
- 通过 `useUserStore` 的 `hasPermission` 和 `hasRole` 方法判断权限

## 状态管理

### useUserStore
- token: 用户令牌
- userInfo: 用户信息
- setToken: 设置令牌
- setUserInfo: 设置用户信息
- logout: 退出登录
- hasPermission: 权限判断
- hasRole: 角色判断

### useAppStore
- sidebarCollapsed: 侧边栏折叠状态
- loading: 全局加载状态
- toggleSidebar: 切换侧边栏
- setLoading: 设置加载状态

## 路由守卫

- 未登录自动跳转登录页
- 已登录访问登录页自动跳转首页
- 自动获取用户信息
- 页面加载进度条

## 注意事项

1. 所有文件路径使用 `@/` 别名，指向 `src` 目录
2. 组件使用 `<script setup>` 语法
3. 使用 TypeScript 进行类型约束
4. Element Plus 组件按需自动导入
5. 图标使用 Element Plus Icons

## 待完成功能

由于项目规模较大，以下视图组件需要根据实际业务需求继续完善：

- 车辆管理详细页面（车型列表、车辆详情、回车登记）
- 员工管理详细页面（快递员详情、司机详情、排班设置）
- 业务管理详细页面（运单详情、订单详情）
- 调度管理详细页面（线路详情、运输任务详情）
- 系统管理详细页面（用户管理、角色管理、组织管理）
- 日志管理详细页面（操作日志、异常日志）

这些页面的基础结构、API 接口、类型定义已经完成，只需按照已有模式继续开发即可。

## 后端接口对接

后端 API 基础路径配置在 `.env` 文件中，开发环境代理到 `http://localhost:8080`。

所有接口遵循统一响应格式：
```typescript
{
  code: number      // 200 表示成功
  message: string   // 响应消息
  data: T          // 响应数据
}
```

分页接口响应格式：
```typescript
{
  list: T[]        // 数据列表
  total: number    // 总数
  pageNum: number  // 当前页
  pageSize: number // 每页大小
}
```
