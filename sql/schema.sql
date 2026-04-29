CREATE DATABASE IF NOT EXISTS shipping_management
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

USE shipping_management;

SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS sys_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
  role_name VARCHAR(30) NOT NULL COMMENT '角色名称',
  role_code VARCHAR(30) NOT NULL COMMENT '角色编码，如 ADMIN、BUSINESS、VIEWER',
  description VARCHAR(255) DEFAULT NULL COMMENT '角色说明',
  built_in TINYINT NOT NULL DEFAULT 0 COMMENT '是否内置角色：0否，1是',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_sys_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

CREATE TABLE IF NOT EXISTS sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
  username VARCHAR(30) NOT NULL COMMENT '用户名',
  password VARCHAR(30) NOT NULL COMMENT '明文密码，课程设计简化使用，不进行加密或哈希处理',
  real_name VARCHAR(30) NOT NULL COMMENT '真实姓名',
  phone VARCHAR(30) DEFAULT NULL COMMENT '联系电话',
  email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  role_id BIGINT NOT NULL COMMENT '角色ID',
  status VARCHAR(20) NOT NULL DEFAULT 'ENABLED' COMMENT '用户状态：ENABLED启用，DISABLED禁用',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_sys_user_username (username),
  KEY idx_sys_user_role_id (role_id),
  KEY idx_sys_user_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE IF NOT EXISTS ship (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '船舶ID',
  ship_no VARCHAR(30) NOT NULL COMMENT '船舶编号',
  ship_name VARCHAR(50) NOT NULL COMMENT '船名',
  ship_type VARCHAR(50) NOT NULL COMMENT '船舶类型',
  load_capacity DECIMAL(12,2) NOT NULL COMMENT '载重量，单位：吨',
  home_port VARCHAR(50) DEFAULT NULL COMMENT '所属港口',
  status VARCHAR(20) NOT NULL DEFAULT 'IDLE' COMMENT '船舶状态：IDLE空闲，SAILING运输中，MAINTENANCE维修中，DISABLED停用',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_ship_no (ship_no),
  KEY idx_ship_name (ship_name),
  KEY idx_ship_type (ship_type),
  KEY idx_ship_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='船舶表';

CREATE TABLE IF NOT EXISTS crew_member (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '船员ID',
  crew_no VARCHAR(30) NOT NULL COMMENT '船员编号',
  name VARCHAR(30) NOT NULL COMMENT '姓名',
  gender VARCHAR(10) DEFAULT NULL COMMENT '性别',
  phone VARCHAR(30) DEFAULT NULL COMMENT '联系电话',
  certificate_no VARCHAR(50) NOT NULL COMMENT '证件编号',
  position VARCHAR(50) NOT NULL COMMENT '岗位',
  ship_id BIGINT DEFAULT NULL COMMENT '所属船舶ID',
  status VARCHAR(20) NOT NULL DEFAULT 'UNASSIGNED' COMMENT '船员状态：ON_DUTY在岗，ON_LEAVE休假，UNASSIGNED待分配，RESIGNED离职',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_crew_member_no (crew_no),
  UNIQUE KEY uk_crew_member_certificate_no (certificate_no),
  KEY idx_crew_member_name (name),
  KEY idx_crew_member_ship_id (ship_id),
  KEY idx_crew_member_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='船员表';

CREATE TABLE IF NOT EXISTS transport_order (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '运输任务ID',
  order_no VARCHAR(40) NOT NULL COMMENT '运输任务编号',
  cargo_name VARCHAR(100) NOT NULL COMMENT '货物名称',
  cargo_type VARCHAR(50) NOT NULL COMMENT '货物类型',
  cargo_weight DECIMAL(12,2) NOT NULL COMMENT '货物重量，单位：吨',
  origin_port VARCHAR(50) NOT NULL COMMENT '起运港',
  destination_port VARCHAR(50) NOT NULL COMMENT '目的港',
  ship_id BIGINT NOT NULL COMMENT '运输船舶ID',
  customer_name VARCHAR(100) NOT NULL COMMENT '客户名称',
  customer_phone VARCHAR(30) DEFAULT NULL COMMENT '客户联系电话',
  planned_departure_time DATETIME NOT NULL COMMENT '预计出发时间',
  planned_arrival_time DATETIME NOT NULL COMMENT '预计到达时间',
  actual_departure_time DATETIME DEFAULT NULL COMMENT '实际出发时间',
  actual_arrival_time DATETIME DEFAULT NULL COMMENT '实际到达时间',
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '运输状态：PENDING待出发，IN_TRANSIT运输中，ARRIVED已到达，CANCELLED已取消',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_transport_order_no (order_no),
  KEY idx_transport_order_ship_id (ship_id),
  KEY idx_transport_order_status (status),
  KEY idx_transport_order_customer_name (customer_name),
  KEY idx_transport_order_planned_departure_time (planned_departure_time),
  KEY idx_transport_order_route (origin_port, destination_port)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运输任务表';

CREATE TABLE IF NOT EXISTS settlement (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '结算ID',
  settlement_no VARCHAR(40) NOT NULL COMMENT '结算编号',
  transport_order_id BIGINT NOT NULL COMMENT '运输任务ID',
  customer_name VARCHAR(100) NOT NULL COMMENT '客户名称',
  freight_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '运费金额',
  additional_fee DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '附加费用',
  discount_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '优惠金额',
  receivable_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '应收金额',
  received_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '实收金额',
  status VARCHAR(20) NOT NULL DEFAULT 'UNSETTLED' COMMENT '结算状态：UNSETTLED未结算，PARTIAL部分结算，SETTLED已结算',
  settled_at DATETIME DEFAULT NULL COMMENT '结算完成时间',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_settlement_no (settlement_no),
  UNIQUE KEY uk_settlement_transport_order_id (transport_order_id),
  KEY idx_settlement_customer_name (customer_name),
  KEY idx_settlement_status (status),
  KEY idx_settlement_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='财务结算表';

CREATE TABLE IF NOT EXISTS dictionary_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '字典项ID',
  dict_type VARCHAR(50) NOT NULL COMMENT '字典类型，如 SHIP_TYPE、CARGO_TYPE、PORT、CREW_POSITION',
  label VARCHAR(50) NOT NULL COMMENT '显示名称',
  value VARCHAR(50) NOT NULL COMMENT '字典值',
  sort INT NOT NULL DEFAULT 0 COMMENT '排序号',
  enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：0否，1是',
  remark VARCHAR(255) DEFAULT NULL COMMENT '备注',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY uk_dictionary_item_type_value (dict_type, value),
  KEY idx_dictionary_item_type (dict_type),
  KEY idx_dictionary_item_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典项表';

INSERT IGNORE INTO sys_role (id, role_name, role_code, description, built_in)
VALUES
  (1, '管理员', 'ADMIN', '拥有系统主要管理权限', 1),
  (2, '业务用户', 'BUSINESS', '负责船舶、船员、运输和结算等业务数据维护', 1),
  (3, '查看用户', 'VIEWER', '仅允许查看业务数据', 1);

INSERT IGNORE INTO sys_user (id, username, password, real_name, phone, email, role_id, status)
VALUES
  (1, 'admin', '123456', '系统管理员', '13800000000', 'admin@example.com', 1, 'ENABLED'),
  (2, 'business01', '123456', '业务员一号', '13800000001', 'business01@example.com', 2, 'ENABLED');

INSERT IGNORE INTO dictionary_item (dict_type, label, value, sort, enabled)
VALUES
  ('SHIP_TYPE', '集装箱船', 'CONTAINER_SHIP', 1, 1),
  ('SHIP_TYPE', '散货船', 'BULK_CARRIER', 2, 1),
  ('SHIP_TYPE', '油轮', 'OIL_TANKER', 3, 1),
  ('CARGO_TYPE', '集装箱货物', 'CONTAINER_CARGO', 1, 1),
  ('CARGO_TYPE', '散装货物', 'BULK_CARGO', 2, 1),
  ('CARGO_TYPE', '液体货物', 'LIQUID_CARGO', 3, 1),
  ('PORT', '上海港', 'SHANGHAI_PORT', 1, 1),
  ('PORT', '深圳港', 'SHENZHEN_PORT', 2, 1),
  ('PORT', '宁波舟山港', 'NINGBO_ZHOUSHAN_PORT', 3, 1),
  ('CREW_POSITION', '船长', 'CAPTAIN', 1, 1),
  ('CREW_POSITION', '大副', 'CHIEF_OFFICER', 2, 1),
  ('CREW_POSITION', '轮机长', 'CHIEF_ENGINEER', 3, 1);

INSERT IGNORE INTO ship (ship_no, ship_name, ship_type, load_capacity, home_port, status, remark)
VALUES
  ('SHIP-001', '远航一号', '集装箱船', 50000.00, '上海港', 'IDLE', '主力运输船舶'),
  ('SHIP-002', '海运二号', '散货船', 30000.00, '深圳港', 'IDLE', '常规散货运输');

INSERT IGNORE INTO crew_member (crew_no, name, gender, phone, certificate_no, position, ship_id, status, remark)
VALUES
  ('CREW-001', '张三', '男', '13800000002', 'CERT-001', '船长', 1, 'ON_DUTY', '经验丰富'),
  ('CREW-002', '李四', '男', '13800000003', 'CERT-002', '大副', 1, 'ON_DUTY', '负责航行协助');

UPDATE sys_role
SET role_name = '管理员', description = '拥有系统主要管理权限', built_in = 1
WHERE role_code = 'ADMIN';

UPDATE sys_role
SET role_name = '业务用户', description = '负责船舶、船员、运输和结算等业务数据维护', built_in = 1
WHERE role_code = 'BUSINESS';

UPDATE sys_role
SET role_name = '查看用户', description = '仅允许查看业务数据', built_in = 1
WHERE role_code = 'VIEWER';

UPDATE sys_user
SET password = '123456', real_name = '系统管理员', phone = '13800000000',
    email = 'admin@example.com', role_id = 1, status = 'ENABLED'
WHERE username = 'admin';

UPDATE sys_user
SET password = '123456', real_name = '业务员一号', phone = '13800000001',
    email = 'business01@example.com', role_id = 2, status = 'ENABLED'
WHERE username = 'business01';

UPDATE dictionary_item SET label = '集装箱船' WHERE dict_type = 'SHIP_TYPE' AND value = 'CONTAINER_SHIP';
UPDATE dictionary_item SET label = '散货船' WHERE dict_type = 'SHIP_TYPE' AND value = 'BULK_CARRIER';
UPDATE dictionary_item SET label = '油轮' WHERE dict_type = 'SHIP_TYPE' AND value = 'OIL_TANKER';
UPDATE dictionary_item SET label = '集装箱货物' WHERE dict_type = 'CARGO_TYPE' AND value = 'CONTAINER_CARGO';
UPDATE dictionary_item SET label = '散装货物' WHERE dict_type = 'CARGO_TYPE' AND value = 'BULK_CARGO';
UPDATE dictionary_item SET label = '液体货物' WHERE dict_type = 'CARGO_TYPE' AND value = 'LIQUID_CARGO';
UPDATE dictionary_item SET label = '上海港' WHERE dict_type = 'PORT' AND value = 'SHANGHAI_PORT';
UPDATE dictionary_item SET label = '深圳港' WHERE dict_type = 'PORT' AND value = 'SHENZHEN_PORT';
UPDATE dictionary_item SET label = '宁波舟山港' WHERE dict_type = 'PORT' AND value = 'NINGBO_ZHOUSHAN_PORT';
UPDATE dictionary_item SET label = '船长' WHERE dict_type = 'CREW_POSITION' AND value = 'CAPTAIN';
UPDATE dictionary_item SET label = '大副' WHERE dict_type = 'CREW_POSITION' AND value = 'CHIEF_OFFICER';
UPDATE dictionary_item SET label = '轮机长' WHERE dict_type = 'CREW_POSITION' AND value = 'CHIEF_ENGINEER';

UPDATE ship
SET ship_name = '远航一号', ship_type = '集装箱船', load_capacity = 50000.00,
    home_port = '上海港', status = 'IDLE', remark = '主力运输船舶'
WHERE ship_no = 'SHIP-001';

UPDATE ship
SET ship_name = '海运二号', ship_type = '散货船', load_capacity = 30000.00,
    home_port = '深圳港', status = 'IDLE', remark = '常规散货运输'
WHERE ship_no = 'SHIP-002';

UPDATE crew_member
SET name = '张三', gender = '男', phone = '13800000002', certificate_no = 'CERT-001',
    position = '船长', ship_id = 1, status = 'ON_DUTY', remark = '经验丰富'
WHERE crew_no = 'CREW-001';

UPDATE crew_member
SET name = '李四', gender = '男', phone = '13800000003', certificate_no = 'CERT-002',
    position = '大副', ship_id = 1, status = 'ON_DUTY', remark = '负责航行协助'
WHERE crew_no = 'CREW-002';
