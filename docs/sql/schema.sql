-- ============================================================
-- 万枢物流 MySQL 数据库建表脚本
-- 数据库: wanshu-logistics
-- ============================================================

CREATE DATABASE IF NOT EXISTS `wanshu_logistics` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `wanshu_logistics`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- -----------------------------------------------------------
-- 1. 系统管理
-- -----------------------------------------------------------

-- 用户表
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id              BIGINT       NOT NULL COMMENT '主键ID',
    username        VARCHAR(50)  NOT NULL COMMENT '用户名/账号',
    password        VARCHAR(200) NOT NULL COMMENT '密码（BCrypt加密）',
    real_name       VARCHAR(20)  DEFAULT NULL COMMENT '真实姓名',
    phone           VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    avatar          VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    gender          TINYINT      DEFAULT 0 COMMENT '性别: 0=未知 1=男 2=女',
    birthday        DATE         DEFAULT NULL COMMENT '生日',
    organ_id        BIGINT       DEFAULT NULL COMMENT '所属机构ID',
    status          TINYINT      DEFAULT 1 COMMENT '状态: 0=禁用 1=启用',
    last_login_time DATETIME     DEFAULT NULL COMMENT '最后登录时间',
    last_login_ip   VARCHAR(50)  DEFAULT NULL COMMENT '最后登录IP',
    deleted         TINYINT      DEFAULT 0 COMMENT '逻辑删除: 0=未删除 1=已删除',
    created_time    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 角色表
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id           BIGINT       NOT NULL COMMENT '主键ID',
    role_name    VARCHAR(50)  NOT NULL COMMENT '角色名称',
    role_code    VARCHAR(50)  NOT NULL COMMENT '角色编码',
    description  VARCHAR(200) DEFAULT NULL COMMENT '描述',
    login_scope  VARCHAR(100) DEFAULT NULL COMMENT '可登录端: admin,courier,driver',
    status       TINYINT      DEFAULT 1 COMMENT '状态: 0=禁用 1=启用',
    deleted      TINYINT      DEFAULT 0 COMMENT '逻辑删除',
    created_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 用户角色关联表
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    id      BIGINT NOT NULL COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 菜单表
DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu (
    id           BIGINT       NOT NULL COMMENT '主键ID',
    parent_id    BIGINT       DEFAULT 0 COMMENT '父菜单ID（0=顶级目录）',
    menu_name    VARCHAR(50)  NOT NULL COMMENT '菜单名称',
    menu_type    TINYINT      NOT NULL COMMENT '类型: 1=目录 2=菜单',
    path         VARCHAR(200) DEFAULT NULL COMMENT '路由路径',
    component    VARCHAR(200) DEFAULT NULL COMMENT '组件路径',
    icon         VARCHAR(100) DEFAULT NULL COMMENT '图标',
    sort_order   INT          DEFAULT 0 COMMENT '排序',
    visible      TINYINT      DEFAULT 1 COMMENT '是否可见: 0=隐藏 1=显示',
    status       TINYINT      DEFAULT 1 COMMENT '状态: 0=禁用 1=启用',
    deleted      TINYINT      DEFAULT 0 COMMENT '逻辑删除',
    created_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- 角色菜单关联表
DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE sys_role_menu (
    id      BIGINT NOT NULL COMMENT '主键ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (id),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- 操作日志表
DROP TABLE IF EXISTS sys_operation_log;
CREATE TABLE sys_operation_log (
    id             BIGINT        NOT NULL COMMENT '主键ID',
    module         VARCHAR(50)   DEFAULT NULL COMMENT '操作模块',
    operation      VARCHAR(100)  DEFAULT NULL COMMENT '操作描述',
    method         VARCHAR(200)  DEFAULT NULL COMMENT '请求方法',
    request_url    VARCHAR(500)  DEFAULT NULL COMMENT '请求URL',
    request_params TEXT          DEFAULT NULL COMMENT '请求参数',
    response_data  TEXT          DEFAULT NULL COMMENT '响应数据',
    operator_id    BIGINT        DEFAULT NULL COMMENT '操作人ID',
    operator_name  VARCHAR(50)   DEFAULT NULL COMMENT '操作人姓名',
    operator_ip    VARCHAR(50)   DEFAULT NULL COMMENT '操作人IP',
    cost_time      BIGINT        DEFAULT NULL COMMENT '耗时(ms)',
    status         TINYINT       DEFAULT 1 COMMENT '状态: 0=失败 1=成功',
    created_time   DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_operator_id (operator_id),
    KEY idx_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 异常日志表
DROP TABLE IF EXISTS sys_exception_log;
CREATE TABLE sys_exception_log (
    id             BIGINT        NOT NULL COMMENT '主键ID',
    module         VARCHAR(50)   DEFAULT NULL COMMENT '异常模块',
    request_url    VARCHAR(500)  DEFAULT NULL COMMENT '请求URL',
    request_params TEXT          DEFAULT NULL COMMENT '请求参数',
    exception_name VARCHAR(200)  DEFAULT NULL COMMENT '异常类名',
    exception_msg  TEXT          DEFAULT NULL COMMENT '异常信息',
    stack_trace    TEXT          DEFAULT NULL COMMENT '堆栈信息',
    operator_id    BIGINT        DEFAULT NULL COMMENT '操作人ID',
    operator_name  VARCHAR(50)   DEFAULT NULL COMMENT '操作人姓名',
    operator_ip    VARCHAR(50)   DEFAULT NULL COMMENT '操作人IP',
    created_time   DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='异常日志表';

-- -----------------------------------------------------------
-- 2. 基础数据管理
-- -----------------------------------------------------------

-- 机构表
DROP TABLE IF EXISTS base_organ;
CREATE TABLE base_organ (
    id             BIGINT       NOT NULL COMMENT '主键ID',
    parent_id      BIGINT       DEFAULT 0 COMMENT '上级机构ID（0=顶级）',
    organ_name     VARCHAR(100) NOT NULL COMMENT '机构名称',
    organ_type     TINYINT      NOT NULL COMMENT '类型: 1=一级转运中心(OLT) 2=二级分拣中心(TLT) 3=营业部(AGENCY)',
    province_id    BIGINT       DEFAULT NULL COMMENT '省ID',
    city_id        BIGINT       DEFAULT NULL COMMENT '市ID',
    county_id      BIGINT       DEFAULT NULL COMMENT '区/县ID',
    address        VARCHAR(500) DEFAULT NULL COMMENT '详细地址',
    longitude      DECIMAL(10,6) DEFAULT NULL COMMENT '经度',
    latitude       DECIMAL(10,6) DEFAULT NULL COMMENT '纬度',
    manager_name   VARCHAR(20)  DEFAULT NULL COMMENT '负责人姓名',
    manager_phone  VARCHAR(20)  DEFAULT NULL COMMENT '负责人电话',
    contact_name   VARCHAR(20)  DEFAULT NULL COMMENT '对接人姓名',
    contact_phone  VARCHAR(20)  DEFAULT NULL COMMENT '对接人电话',
    sort_order     INT          DEFAULT 0 COMMENT '排序',
    status         TINYINT      DEFAULT 1 COMMENT '状态: 0=禁用 1=启用',
    deleted        TINYINT      DEFAULT 0 COMMENT '逻辑删除',
    created_time   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_parent_id (parent_id),
    KEY idx_organ_type (organ_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='机构表';

-- 机构作业范围表
DROP TABLE IF EXISTS base_organ_scope;
CREATE TABLE base_organ_scope (
    id          BIGINT NOT NULL COMMENT '主键ID',
    organ_id    BIGINT NOT NULL COMMENT '机构ID',
    province_id BIGINT DEFAULT NULL COMMENT '省ID',
    city_id     BIGINT DEFAULT NULL COMMENT '市ID',
    county_id   BIGINT DEFAULT NULL COMMENT '区/县ID',
    PRIMARY KEY (id),
    KEY idx_organ_id (organ_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='机构作业范围表';

-- 运费模板表
DROP TABLE IF EXISTS base_freight_template;
CREATE TABLE base_freight_template (
    id              BIGINT        NOT NULL COMMENT '主键ID',
    template_name   VARCHAR(100)  NOT NULL COMMENT '模板名称',
    template_type   TINYINT       NOT NULL COMMENT '类型: 1=同城寄 2=省内寄 3=跨省寄 4=经济区互寄',
    first_weight    DECIMAL(10,2) DEFAULT 1.00 COMMENT '首重(kg)',
    first_weight_price DECIMAL(10,2) DEFAULT 0.00 COMMENT '首重价格(元)',
    extra_weight    DECIMAL(10,2) DEFAULT 1.00 COMMENT '续重(kg)',
    extra_weight_price DECIMAL(10,2) DEFAULT 0.00 COMMENT '续重价格(元)',
    light_throw_ratio  INT        DEFAULT 6000 COMMENT '轻抛比(cm³/kg)',
    status          TINYINT       DEFAULT 1 COMMENT '状态: 0=禁用 1=启用',
    deleted         TINYINT       DEFAULT 0 COMMENT '逻辑删除',
    created_time    DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time    DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运费模板表';

-- 经济区定义表
DROP TABLE IF EXISTS base_economic_zone;
CREATE TABLE base_economic_zone (
    id        BIGINT      NOT NULL COMMENT '主键ID',
    zone_name VARCHAR(50) NOT NULL COMMENT '经济区名称(京津翼/江沪浙皖/川渝/黑吉辽)',
    provinces VARCHAR(200) NOT NULL COMMENT '包含省份(逗号分隔)',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='经济区定义表';

-- 车型表
DROP TABLE IF EXISTS base_vehicle_type;
CREATE TABLE base_vehicle_type (
    id            BIGINT       NOT NULL COMMENT '主键ID',
    type_number   VARCHAR(20)  NOT NULL COMMENT '车型编号（如 AB000001）',
    type_name     VARCHAR(30)  NOT NULL COMMENT '车辆类型名称',
    load_weight   INT          DEFAULT NULL COMMENT '实载重量(吨)',
    load_volume   INT          DEFAULT NULL COMMENT '实载体积(m³)',
    length        DECIMAL(5,1) DEFAULT NULL COMMENT '长(m)',
    width         DECIMAL(5,1) DEFAULT NULL COMMENT '宽(m)',
    height        DECIMAL(5,1) DEFAULT NULL COMMENT '高(m)',
    deleted       TINYINT      DEFAULT 0 COMMENT '逻辑删除',
    created_time  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_type_number (type_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车型表';

-- 车辆表
DROP TABLE IF EXISTS base_vehicle;
CREATE TABLE base_vehicle (
    id                BIGINT       NOT NULL COMMENT '主键ID',
    vehicle_number    VARCHAR(20)  NOT NULL COMMENT '车辆编号（如 CL000001）',
    vehicle_type_id   BIGINT       NOT NULL COMMENT '车型ID',
    license_plate     VARCHAR(20)  NOT NULL COMMENT '车牌号码',
    load_weight       INT          DEFAULT NULL COMMENT '实载重量(吨)',
    load_volume       INT          DEFAULT NULL COMMENT '实载体积(m³)',
    organ_id          BIGINT       DEFAULT NULL COMMENT '所属机构ID',
    status            TINYINT      DEFAULT 0 COMMENT '状态: 0=停用 1=可用',
    license_image     VARCHAR(500) DEFAULT NULL COMMENT '行驶证照片URL',
    deleted           TINYINT      DEFAULT 0 COMMENT '逻辑删除',
    created_time      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_vehicle_number (vehicle_number),
    UNIQUE KEY uk_license_plate (license_plate),
    KEY idx_vehicle_type_id (vehicle_type_id),
    KEY idx_organ_id (organ_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆表';

-- 车辆司机关联表（车辆:司机 = 1:N，最多10个）
DROP TABLE IF EXISTS base_vehicle_driver;
CREATE TABLE base_vehicle_driver (
    id         BIGINT NOT NULL COMMENT '主键ID',
    vehicle_id BIGINT NOT NULL COMMENT '车辆ID',
    driver_id  BIGINT NOT NULL COMMENT '司机(用户)ID',
    PRIMARY KEY (id),
    KEY idx_vehicle_id (vehicle_id),
    KEY idx_driver_id (driver_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车辆司机关联表';

-- -----------------------------------------------------------
-- 3. 员工管理（快递员/司机扩展信息）
-- -----------------------------------------------------------

-- 快递员扩展表
DROP TABLE IF EXISTS emp_courier;
CREATE TABLE emp_courier (
    id           BIGINT   NOT NULL COMMENT '主键ID（=sys_user.id）',
    organ_id     BIGINT   DEFAULT NULL COMMENT '所属营业部ID',
    work_status  TINYINT  DEFAULT 1 COMMENT '工作状态: 0=休息 1=上班',
    deleted      TINYINT  DEFAULT 0 COMMENT '逻辑删除',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='快递员扩展表';

-- 快递员作业范围表
DROP TABLE IF EXISTS emp_courier_scope;
CREATE TABLE emp_courier_scope (
    id          BIGINT NOT NULL COMMENT '主键ID',
    courier_id  BIGINT NOT NULL COMMENT '快递员ID',
    province_id BIGINT DEFAULT NULL COMMENT '省ID',
    city_id     BIGINT DEFAULT NULL COMMENT '市ID',
    county_id   BIGINT DEFAULT NULL COMMENT '区/县ID',
    PRIMARY KEY (id),
    KEY idx_courier_id (courier_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='快递员作业范围表';

-- 司机扩展表
DROP TABLE IF EXISTS emp_driver;
CREATE TABLE emp_driver (
    id            BIGINT       NOT NULL COMMENT '主键ID（=sys_user.id）',
    organ_id      BIGINT       DEFAULT NULL COMMENT '所属机构ID（转运中心/分拣中心）',
    vehicle_types VARCHAR(200) DEFAULT NULL COMMENT '擅长车型(逗号分隔)',
    license_image VARCHAR(500) DEFAULT NULL COMMENT '驾驶证照片URL',
    work_status   TINYINT      DEFAULT 1 COMMENT '工作状态: 0=休息 1=上班',
    deleted       TINYINT      DEFAULT 0 COMMENT '逻辑删除',
    created_time  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='司机扩展表';

-- -----------------------------------------------------------
-- 4. 业务管理
-- -----------------------------------------------------------

-- 订单表
DROP TABLE IF EXISTS biz_order;
CREATE TABLE biz_order (
    id                BIGINT        NOT NULL COMMENT '主键ID',
    order_number      VARCHAR(30)   NOT NULL COMMENT '订单编号',
    -- 寄件人
    sender_name       VARCHAR(50)   DEFAULT NULL COMMENT '寄件人姓名',
    sender_phone      VARCHAR(20)   DEFAULT NULL COMMENT '寄件人电话',
    sender_province_id BIGINT       DEFAULT NULL COMMENT '寄件省ID',
    sender_city_id    BIGINT        DEFAULT NULL COMMENT '寄件市ID',
    sender_county_id  BIGINT        DEFAULT NULL COMMENT '寄件区ID',
    sender_address    VARCHAR(500)  DEFAULT NULL COMMENT '寄件人详细地址',
    -- 收件人
    receiver_name     VARCHAR(50)   DEFAULT NULL COMMENT '收件人姓名',
    receiver_phone    VARCHAR(20)   DEFAULT NULL COMMENT '收件人电话',
    receiver_province_id BIGINT     DEFAULT NULL COMMENT '收件省ID',
    receiver_city_id  BIGINT        DEFAULT NULL COMMENT '收件市ID',
    receiver_county_id BIGINT       DEFAULT NULL COMMENT '收件区ID',
    receiver_address  VARCHAR(500)  DEFAULT NULL COMMENT '收件人详细地址',
    -- 物品信息
    goods_name        VARCHAR(100)  DEFAULT NULL COMMENT '物品名称',
    goods_type        VARCHAR(50)   DEFAULT NULL COMMENT '物品类型',
    weight            DECIMAL(10,2) DEFAULT NULL COMMENT '预估重量(kg)',
    volume            DECIMAL(10,2) DEFAULT NULL COMMENT '预估体积(cm³)',
    -- 费用
    payment_method    TINYINT       DEFAULT 1 COMMENT '付款方式: 1=寄付 2=到付',
    estimated_fee     DECIMAL(10,2) DEFAULT NULL COMMENT '估算运费(元)',
    actual_fee        DECIMAL(10,2) DEFAULT NULL COMMENT '实际运费(元)',
    -- 取件
    pickup_time       DATETIME      DEFAULT NULL COMMENT '预约取件时间',
    privacy_flag      TINYINT       DEFAULT 0 COMMENT '隐私寄件: 0=否 1=是',
    -- 状态
    status            TINYINT       DEFAULT 0 COMMENT '订单状态: 0=已下单 1=已接单 2=已揽收 3=运输中 4=派送中 5=已签收 6=已拒收 7=已取消',
    cancel_reason     VARCHAR(200)  DEFAULT NULL COMMENT '取消原因',
    -- 来源
    user_id           BIGINT        DEFAULT NULL COMMENT '下单用户ID',
    source            TINYINT       DEFAULT 1 COMMENT '来源: 1=用户端 2=快递员端 3=管理端',
    deleted           TINYINT       DEFAULT 0 COMMENT '逻辑删除',
    created_time      DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time      DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_number (order_number),
    KEY idx_user_id (user_id),
    KEY idx_status (status),
    KEY idx_sender_phone (sender_phone),
    KEY idx_receiver_phone (receiver_phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 运单表
DROP TABLE IF EXISTS biz_waybill;
CREATE TABLE biz_waybill (
    id                BIGINT        NOT NULL COMMENT '主键ID',
    waybill_number    VARCHAR(30)   NOT NULL COMMENT '运单编号',
    order_id          BIGINT        NOT NULL COMMENT '关联订单ID',
    -- 寄/收件信息(冗余)
    sender_name       VARCHAR(50)   DEFAULT NULL COMMENT '寄件人姓名',
    sender_phone      VARCHAR(20)   DEFAULT NULL COMMENT '寄件人电话',
    sender_address    VARCHAR(500)  DEFAULT NULL COMMENT '寄件人地址',
    receiver_name     VARCHAR(50)   DEFAULT NULL COMMENT '收件人姓名',
    receiver_phone    VARCHAR(20)   DEFAULT NULL COMMENT '收件人电话',
    receiver_address  VARCHAR(500)  DEFAULT NULL COMMENT '收件人地址',
    -- 物品
    goods_name        VARCHAR(100)  DEFAULT NULL COMMENT '物品名称',
    weight            DECIMAL(10,2) DEFAULT NULL COMMENT '实际重量(kg)',
    volume            DECIMAL(10,2) DEFAULT NULL COMMENT '实际体积(cm³)',
    -- 费用
    freight           DECIMAL(10,2) DEFAULT NULL COMMENT '运费(元)',
    -- 调度
    send_organ_id     BIGINT        DEFAULT NULL COMMENT '发件营业部ID',
    receive_organ_id  BIGINT        DEFAULT NULL COMMENT '收件营业部ID',
    -- 状态
    status            TINYINT       DEFAULT 0 COMMENT '运单状态: 0=待调度 1=已调度 2=待提货 3=在途 4=到达 5=派送中 6=已签收 7=已拒签 8=异常件',
    sign_image        VARCHAR(500)  DEFAULT NULL COMMENT '签收签名图片',
    sign_time         DATETIME      DEFAULT NULL COMMENT '签收时间',
    deleted           TINYINT       DEFAULT 0 COMMENT '逻辑删除',
    created_time      DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time      DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_waybill_number (waybill_number),
    KEY idx_order_id (order_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运单表';

-- 取件作业表
DROP TABLE IF EXISTS biz_pickup_task;
CREATE TABLE biz_pickup_task (
    id             BIGINT       NOT NULL COMMENT '主键ID',
    task_number    VARCHAR(30)  NOT NULL COMMENT '任务单号',
    order_id       BIGINT       NOT NULL COMMENT '订单ID',
    waybill_id     BIGINT       DEFAULT NULL COMMENT '运单ID',
    courier_id     BIGINT       DEFAULT NULL COMMENT '分配的快递员ID',
    assign_time    DATETIME     DEFAULT NULL COMMENT '分配时间',
    pickup_time    DATETIME     DEFAULT NULL COMMENT '实际取件时间',
    status         TINYINT      DEFAULT 0 COMMENT '状态: 0=待分配 1=已分配 2=已取件 3=已取消',
    cancel_reason  VARCHAR(200) DEFAULT NULL COMMENT '取消原因',
    remark         VARCHAR(500) DEFAULT NULL COMMENT '备注',
    deleted        TINYINT      DEFAULT 0 COMMENT '逻辑删除',
    created_time   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_task_number (task_number),
    KEY idx_order_id (order_id),
    KEY idx_courier_id (courier_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='取件作业表';

-- 派件作业表
DROP TABLE IF EXISTS biz_delivery_task;
CREATE TABLE biz_delivery_task (
    id             BIGINT       NOT NULL COMMENT '主键ID',
    task_number    VARCHAR(30)  NOT NULL COMMENT '任务单号',
    waybill_id     BIGINT       NOT NULL COMMENT '运单ID',
    courier_id     BIGINT       DEFAULT NULL COMMENT '分配的快递员ID',
    assign_time    DATETIME     DEFAULT NULL COMMENT '分配时间',
    delivery_time  DATETIME     DEFAULT NULL COMMENT '实际派件时间',
    sign_type      TINYINT      DEFAULT NULL COMMENT '签收方式: 1=本人签收 2=代签 3=拒收',
    sign_image     VARCHAR(500) DEFAULT NULL COMMENT '签收签名图片',
    status         TINYINT      DEFAULT 0 COMMENT '状态: 0=待分配 1=已分配 2=派送中 3=已签收 4=已拒收 5=已取消',
    reject_reason  VARCHAR(200) DEFAULT NULL COMMENT '拒收原因',
    remark         VARCHAR(500) DEFAULT NULL COMMENT '备注',
    deleted        TINYINT      DEFAULT 0 COMMENT '逻辑删除',
    created_time   DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_task_number (task_number),
    KEY idx_waybill_id (waybill_id),
    KEY idx_courier_id (courier_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='派件作业表';

-- -----------------------------------------------------------
-- 5. 调度管理
-- -----------------------------------------------------------

-- 线路表（MySQL管理端CRUD用，图计算用Neo4j）
DROP TABLE IF EXISTS dispatch_line;
CREATE TABLE dispatch_line (
    id              BIGINT        NOT NULL COMMENT '主键ID',
    line_number     VARCHAR(30)   NOT NULL COMMENT '线路编号（XL+缩写+6位）',
    line_name       VARCHAR(50)   NOT NULL COMMENT '线路名称',
    line_type       TINYINT       NOT NULL COMMENT '线路类型: 1=干线 2=支线 3=专线 4=临时线路 5=接驳路线',
    start_organ_id  BIGINT        NOT NULL COMMENT '起始机构ID',
    end_organ_id    BIGINT        NOT NULL COMMENT '目的地机构ID',
    distance        DECIMAL(12,2) DEFAULT NULL COMMENT '距离(米)',
    cost            DECIMAL(10,2) DEFAULT NULL COMMENT '成本(元)',
    estimated_time  BIGINT        DEFAULT NULL COMMENT '预计耗时(秒)',
    status          TINYINT       DEFAULT 1 COMMENT '状态: 0=禁用 1=启用',
    deleted         TINYINT       DEFAULT 0 COMMENT '逻辑删除',
    created_time    DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time    DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_line_number (line_number),
    KEY idx_start_organ (start_organ_id),
    KEY idx_end_organ (end_organ_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='线路表';

-- 车次表
DROP TABLE IF EXISTS dispatch_trip;
CREATE TABLE dispatch_trip (
    id             BIGINT      NOT NULL COMMENT '主键ID',
    trip_number    VARCHAR(30) NOT NULL COMMENT '车次编号',
    trip_name      VARCHAR(50) NOT NULL COMMENT '车次名称',
    line_id        BIGINT      NOT NULL COMMENT '所属线路ID',
    period_type    TINYINT     NOT NULL COMMENT '发车周期: 1=天 2=周 3=月 4=一次',
    depart_day     VARCHAR(20) DEFAULT NULL COMMENT '发车日(周: 1-7，月: 1-31，一次: yyyy-MM-dd)',
    depart_time    TIME        NOT NULL COMMENT '发车时间',
    status         TINYINT     DEFAULT 1 COMMENT '状态: 0=禁用 1=启用',
    deleted        TINYINT     DEFAULT 0 COMMENT '逻辑删除',
    created_time   DATETIME    DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time   DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_trip_number (trip_number),
    KEY idx_line_id (line_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车次表';

-- 车次车辆关联表
DROP TABLE IF EXISTS dispatch_trip_vehicle;
CREATE TABLE dispatch_trip_vehicle (
    id         BIGINT NOT NULL COMMENT '主键ID',
    trip_id    BIGINT NOT NULL COMMENT '车次ID',
    vehicle_id BIGINT NOT NULL COMMENT '车辆ID',
    PRIMARY KEY (id),
    KEY idx_trip_id (trip_id),
    KEY idx_vehicle_id (vehicle_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车次车辆关联表';

-- 运输任务表
DROP TABLE IF EXISTS dispatch_transport_task;
CREATE TABLE dispatch_transport_task (
    id                BIGINT       NOT NULL COMMENT '主键ID',
    task_number       VARCHAR(30)  NOT NULL COMMENT '任务编号',
    trip_id           BIGINT       DEFAULT NULL COMMENT '车次ID',
    line_id           BIGINT       DEFAULT NULL COMMENT '线路ID',
    vehicle_id        BIGINT       DEFAULT NULL COMMENT '车辆ID',
    driver_id         BIGINT       DEFAULT NULL COMMENT '司机ID',
    start_organ_id    BIGINT       DEFAULT NULL COMMENT '起始机构ID',
    end_organ_id      BIGINT       DEFAULT NULL COMMENT '目的地机构ID',
    plan_depart_time  DATETIME     DEFAULT NULL COMMENT '计划发车时间',
    actual_depart_time DATETIME    DEFAULT NULL COMMENT '实际发车时间',
    plan_arrive_time  DATETIME     DEFAULT NULL COMMENT '计划到达时间',
    actual_arrive_time DATETIME    DEFAULT NULL COMMENT '实际到达时间',
    load_weight       DECIMAL(10,2) DEFAULT NULL COMMENT '实载重量(吨)',
    load_volume       DECIMAL(10,2) DEFAULT NULL COMMENT '实载体积(m³)',
    waybill_count     INT          DEFAULT 0 COMMENT '运单数量',
    status            TINYINT      DEFAULT 0 COMMENT '状态: 0=待分配 1=已分配 2=进行中 3=已完成 4=超时',
    deleted           TINYINT      DEFAULT 0 COMMENT '逻辑删除',
    created_time      DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_task_number (task_number),
    KEY idx_driver_id (driver_id),
    KEY idx_vehicle_id (vehicle_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运输任务表';

-- 运输任务运单关联表
DROP TABLE IF EXISTS dispatch_task_waybill;
CREATE TABLE dispatch_task_waybill (
    id              BIGINT NOT NULL COMMENT '主键ID',
    transport_task_id BIGINT NOT NULL COMMENT '运输任务ID',
    waybill_id      BIGINT NOT NULL COMMENT '运单ID',
    PRIMARY KEY (id),
    KEY idx_task_id (transport_task_id),
    KEY idx_waybill_id (waybill_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运输任务运单关联表';

-- 回车登记表
DROP TABLE IF EXISTS dispatch_return_register;
CREATE TABLE dispatch_return_register (
    id              BIGINT       NOT NULL COMMENT '主键ID',
    transport_task_id BIGINT     NOT NULL COMMENT '运输任务ID',
    vehicle_id      BIGINT       DEFAULT NULL COMMENT '车辆ID',
    driver_id       BIGINT       DEFAULT NULL COMMENT '司机ID',
    register_time   DATETIME     DEFAULT NULL COMMENT '登记时间',
    register_date   DATE         DEFAULT NULL COMMENT '登记日期',
    description     VARCHAR(500) DEFAULT NULL COMMENT '登记说明',
    images          TEXT         DEFAULT NULL COMMENT '照片URL(JSON数组)',
    created_time    DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_transport_task_id (transport_task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='回车登记表';

-- 调度配置表
DROP TABLE IF EXISTS dispatch_config;
CREATE TABLE dispatch_config (
    id                     BIGINT  NOT NULL COMMENT '主键ID',
    latest_dispatch_hour   INT     DEFAULT 1 COMMENT '最晚派单时间(小时)',
    max_assign_time        INT     DEFAULT 0 COMMENT '最长分配时间(0=当日)',
    priority_first         TINYINT DEFAULT 1 COMMENT '一级优先: 1=转运次数最少 2=成本最低',
    priority_second        TINYINT DEFAULT 2 COMMENT '二级优先: 1=转运次数最少 2=成本最低',
    organ_id               BIGINT  DEFAULT NULL COMMENT '所属机构ID',
    updated_time           DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='调度配置表';

-- -----------------------------------------------------------
-- 6. 用户端
-- -----------------------------------------------------------

-- 微信用户表
DROP TABLE IF EXISTS app_wx_user;
CREATE TABLE app_wx_user (
    id           BIGINT       NOT NULL COMMENT '主键ID',
    openid       VARCHAR(100) NOT NULL COMMENT '微信openid',
    unionid      VARCHAR(100) DEFAULT NULL COMMENT '微信unionid',
    nickname     VARCHAR(50)  DEFAULT NULL COMMENT '微信昵称',
    avatar       VARCHAR(500) DEFAULT NULL COMMENT '微信头像',
    phone        VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    gender       TINYINT      DEFAULT 0 COMMENT '性别: 0=未知 1=男 2=女',
    birthday     DATE         DEFAULT NULL COMMENT '生日',
    deleted      TINYINT      DEFAULT 0 COMMENT '逻辑删除',
    created_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_openid (openid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='微信用户表';

-- 地址簿表
DROP TABLE IF EXISTS app_address_book;
CREATE TABLE app_address_book (
    id           BIGINT       NOT NULL COMMENT '主键ID',
    user_id      BIGINT       NOT NULL COMMENT '用户ID',
    name         VARCHAR(50)  NOT NULL COMMENT '联系人姓名',
    phone        VARCHAR(20)  NOT NULL COMMENT '联系人电话',
    province_id  BIGINT       DEFAULT NULL COMMENT '省ID',
    city_id      BIGINT       DEFAULT NULL COMMENT '市ID',
    county_id    BIGINT       DEFAULT NULL COMMENT '区ID',
    address      VARCHAR(500) DEFAULT NULL COMMENT '详细地址',
    is_default   TINYINT      DEFAULT 0 COMMENT '是否默认: 0=否 1=是',
    deleted      TINYINT      DEFAULT 0 COMMENT '逻辑删除',
    created_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地址簿表';

-- -----------------------------------------------------------
-- 7. 消息表
-- -----------------------------------------------------------
DROP TABLE IF EXISTS sys_message;
CREATE TABLE sys_message (
    id           BIGINT       NOT NULL COMMENT '主键ID',
    title        VARCHAR(100) DEFAULT NULL COMMENT '消息标题',
    content      VARCHAR(1000) DEFAULT NULL COMMENT '消息内容',
    msg_type     TINYINT      NOT NULL COMMENT '类型: 1=公告 2=寄件通知 3=派件通知 4=取消通知 5=签收提醒',
    target_id    BIGINT       DEFAULT NULL COMMENT '目标用户ID',
    is_read      TINYINT      DEFAULT 0 COMMENT '是否已读: 0=未读 1=已读',
    created_time DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_target_id (target_id),
    KEY idx_is_read (is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

SET FOREIGN_KEY_CHECKS = 1;
