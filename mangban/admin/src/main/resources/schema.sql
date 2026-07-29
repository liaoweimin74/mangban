CREATE TABLE IF NOT EXISTS sys_organization (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT,
    org_name VARCHAR(100) NOT NULL,
    org_code VARCHAR(50) NOT NULL UNIQUE,
    sort_order INT DEFAULT 0,
    status INT DEFAULT 1,
    is_deleted INT DEFAULT 0,
    created_by VARCHAR(50),
    created_at DATETIME,
    updated_by VARCHAR(50),
    updated_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    nickname VARCHAR(50),
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    avatar VARCHAR(255),
    org_id BIGINT,
    status INT DEFAULT 1,
    is_deleted INT DEFAULT 0,
    created_by VARCHAR(50),
    created_at DATETIME,
    updated_by VARCHAR(50),
    updated_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(100) NOT NULL,
    role_code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    status INT DEFAULT 1,
    is_deleted INT DEFAULT 0,
    created_by VARCHAR(50),
    created_at DATETIME,
    updated_by VARCHAR(50),
    updated_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT,
    menu_name VARCHAR(100) NOT NULL,
    menu_type INT NOT NULL COMMENT '0-目录 1-菜单 2-按钮',
    path VARCHAR(200),
    component VARCHAR(255),
    permission VARCHAR(100),
    icon VARCHAR(50),
    sort_order INT DEFAULT 0,
    status INT DEFAULT 1,
    is_deleted INT DEFAULT 0,
    created_by VARCHAR(50),
    created_at DATETIME,
    updated_by VARCHAR(50),
    updated_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_dict_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dict_name VARCHAR(100) NOT NULL,
    dict_code VARCHAR(50) NOT NULL UNIQUE,
    remark VARCHAR(255),
    status INT DEFAULT 1,
    is_deleted INT DEFAULT 0,
    created_by VARCHAR(50),
    created_at DATETIME,
    updated_by VARCHAR(50),
    updated_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_dict_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dict_code VARCHAR(50) NOT NULL,
    label VARCHAR(100) NOT NULL,
    value VARCHAR(100) NOT NULL,
    sort_order INT DEFAULT 0,
    status INT DEFAULT 1,
    is_deleted INT DEFAULT 0,
    created_by VARCHAR(50),
    created_at DATETIME,
    updated_by VARCHAR(50),
    updated_at DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_role_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    UNIQUE KEY uk_role_menu (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 装置层级表
CREATE TABLE IF NOT EXISTS sys_location (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT COMMENT '父级ID，NULL=根节点',
    name VARCHAR(100) NOT NULL COMMENT '名称',
    code VARCHAR(50) NOT NULL COMMENT '编码',
    type VARCHAR(20) NOT NULL COMMENT '类型：FACTORY/PLANT/UNIT',
    sort_order INT DEFAULT 0 COMMENT '排序',
    remark VARCHAR(500) COMMENT '备注',
    is_deleted INT DEFAULT 0,
    created_by VARCHAR(50),
    created_at DATETIME,
    updated_by VARCHAR(50),
    updated_at DATETIME,
    UNIQUE KEY uk_location_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='装置层级';

-- 隔离点台账表
CREATE TABLE IF NOT EXISTS sys_isolation_point (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    unit_id BIGINT NOT NULL COMMENT '所属单元ID',
    code VARCHAR(50) NOT NULL COMMENT '编码',
    name VARCHAR(100) NOT NULL COMMENT '名称',
    medium VARCHAR(50) COMMENT '介质',
    pressure_rating VARCHAR(50) COMMENT '压力等级',
    temperature_rating VARCHAR(50) COMMENT '温度等级',
    hazard_level VARCHAR(20) COMMENT '危害等级',
    point_type VARCHAR(50) COMMENT '点位类型',
    blind_spec VARCHAR(100) COMMENT '适配盲板规格',
    equipment_tag VARCHAR(50) COMMENT '关联设备位号',
    pipeline_no VARCHAR(50) COMMENT '关联管线号',
    status VARCHAR(20) DEFAULT 'OPEN' COMMENT '通盲状态：OPEN/BLIND',
    occupy_status VARCHAR(20) DEFAULT 'FREE' COMMENT '占用状态：OCCUPIED/FREE',
    remark VARCHAR(500) COMMENT '备注',
    is_deleted INT DEFAULT 0,
    created_by VARCHAR(50),
    created_at DATETIME,
    updated_by VARCHAR(50),
    updated_at DATETIME,
    UNIQUE KEY uk_ip_code (code),
    KEY idx_unit_id (unit_id),
    KEY idx_ip_status (status),
    KEY idx_hazard_level (hazard_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='隔离点台账';