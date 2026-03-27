# 万枢物流 (Wanshu Logistics)

一站式物流快递管理平台，涵盖管理后台、用户端、快递员端、司机端。

## 项目结构

```
wanshu-logistics/
├── docs/                  # 业务功能清单文档
├── wanshu-backend/        # Spring Boot 3 后端服务 (多模块 Maven)
│   ├── wanshu-common/     # 公共模块 (工具类、常量、异常、统一响应)
│   ├── wanshu-model/      # 数据模型 (实体类、DTO、VO)
│   ├── wanshu-system/     # 系统管理 (用户、角色、组织、日志)
│   ├── wanshu-base/       # 基础数据 (机构、运费、车型、车辆)
│   ├── wanshu-business/   # 业务管理 (运单、订单、取件/派件)
│   ├── wanshu-dispatch/   # 调度管理 (线路、运输任务、快递员/司机/排班)
│   └── wanshu-web/        # Web 启动模块 (聚合入口)
├── wanshu-admin/          # Vue 3 + Element Plus 管理后台前端
└── wanshu-app/            # Uni-app 移动微信小程序端 (用户/快递员/司机)
```

## 技术栈

### 后端 (wanshu-backend)
- **Java 17** + **Spring Boot 3.2**
- **MyBatis-Plus** 持久层
- **Sa-Token** 权限认证
- **Knife4j** API 文档
- **MySQL 8** + **Redis**

### 管理后台 (wanshu-admin)
- **Vue 3** + **TypeScript**
- **Vite 5** 构建
- **Element Plus** UI 组件
- **Pinia** 状态管理
- **ECharts** 数据可视化

### 移动端 (wanshu-app)
- **Uni-app** (Vue 3 + TypeScript)
- **微信小程序** 为主要目标平台
- **Pinia** 状态管理
- 包含三个子端：用户端、快递员端、司机端

## 快速开始

### 后端
```bash
cd wanshu-backend
mvn clean install
mvn spring-boot:run -pl wanshu-web
```

### 管理后台
```bash
cd wanshu-admin
npm install
npm run dev
```

### 移动端
```bash
cd wanshu-app
npm install
npm run dev:mp-weixin
```

## 环境要求
- JDK 17+
- Maven 3.9+
- Node.js 18+
- MySQL 8.0+
- Redis 7+
