-- Insert admin user (password: 123456, BCrypt encoded)
INSERT IGNORE INTO sys_organization (id, parent_id, org_name, org_code, sort_order, status, created_at, updated_at)
VALUES (1, NULL, '总公司', 'HQ', 1, 1, NOW(), NOW());

INSERT IGNORE INTO sys_user (id, username, nickname, password, email, org_id, status, created_at, updated_at)
VALUES (1, 'admin', '系统管理员', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', 'admin@mangban.com', 1, 1, NOW(), NOW());

INSERT IGNORE INTO sys_role (id, role_name, role_code, description, status, created_at, updated_at)
VALUES (1, '超级管理员', 'admin', '系统超级管理员', 1, NOW(), NOW());

INSERT IGNORE INTO sys_user_role (id, user_id, role_id) VALUES (1, 1, 1);

-- Insert default menus
INSERT IGNORE INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, permission, icon, sort_order, created_at, updated_at) VALUES
(1, NULL, '系统管理', 0, '/system', NULL, NULL, 'Setting', 1, NOW(), NOW()),
(2, 1, '用户管理', 1, '/system/user', 'system/user/index', 'system:user:list', 'User', 1, NOW(), NOW()),
(3, 1, '角色管理', 1, '/system/role', 'system/role/index', 'system:role:list', 'UserFilled', 2, NOW(), NOW()),
(4, 1, '菜单管理', 1, '/system/menu', 'system/menu/index', 'system:menu:list', 'Menu', 3, NOW(), NOW()),
(5, 1, '组织机构', 1, '/system/org', 'system/org/index', 'system:org:list', 'Organization', 4, NOW(), NOW()),
(6, 1, '字典管理', 1, '/system/dict', 'system/dict/index', 'system:dict:list', 'List', 5, NOW(), NOW()),
(7, NULL, '首页', 1, '/dashboard', 'dashboard/index', NULL, 'HomeFilled', 0, NOW(), NOW());

-- Menu permissions
INSERT IGNORE INTO sys_menu (id, parent_id, menu_name, menu_type, permission, sort_order, created_at, updated_at) VALUES
(8, 2, '用户查询', 2, 'system:user:query', 1, NOW(), NOW()),
(9, 2, '用户新增', 2, 'system:user:create', 2, NOW(), NOW()),
(10, 2, '用户修改', 2, 'system:user:update', 3, NOW(), NOW()),
(11, 2, '用户删除', 2, 'system:user:delete', 4, NOW(), NOW()),
(12, 3, '角色查询', 2, 'system:role:query', 1, NOW(), NOW()),
(13, 3, '角色新增', 2, 'system:role:create', 2, NOW(), NOW()),
(14, 3, '角色修改', 2, 'system:role:update', 3, NOW(), NOW()),
(15, 3, '角色删除', 2, 'system:role:delete', 4, NOW(), NOW()),
(16, 4, '菜单查询', 2, 'system:menu:query', 1, NOW(), NOW()),
(17, 4, '菜单新增', 2, 'system:menu:create', 2, NOW(), NOW()),
(18, 4, '菜单修改', 2, 'system:menu:update', 3, NOW(), NOW()),
(19, 4, '菜单删除', 2, 'system:menu:delete', 4, NOW(), NOW()),
(20, 5, '机构查询', 2, 'system:org:query', 1, NOW(), NOW()),
(21, 5, '机构新增', 2, 'system:org:create', 2, NOW(), NOW()),
(22, 5, '机构修改', 2, 'system:org:update', 3, NOW(), NOW()),
(23, 5, '机构删除', 2, 'system:org:delete', 4, NOW(), NOW()),
(24, 6, '字典查询', 2, 'system:dict:query', 1, NOW(), NOW()),
(25, 6, '字典新增', 2, 'system:dict:create', 2, NOW(), NOW()),
(26, 6, '字典修改', 2, 'system:dict:update', 3, NOW(), NOW()),
(27, 6, '字典删除', 2, 'system:dict:delete', 4, NOW(), NOW());

-- Insert isolation point management menus
INSERT IGNORE INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, permission, icon, sort_order, created_at, updated_at) VALUES
(28, NULL, '隔离点管理', 0, '/process', NULL, NULL, 'Connection', 2, NOW(), NOW()),
(29, 28, '隔离点台账', 1, '/process/isolation-points', 'process/IsolationPointManage/index', 'process:isolation-point:list', 'List', 1, NOW(), NOW());

-- Menu permissions for isolation point management
INSERT IGNORE INTO sys_menu (id, parent_id, menu_name, menu_type, permission, sort_order, created_at, updated_at) VALUES
(32, 29, '装置层级查询', 2, 'process:location:query', 1, NOW(), NOW()),
(33, 29, '装置层级新增', 2, 'process:location:create', 2, NOW(), NOW()),
(34, 29, '装置层级修改', 2, 'process:location:update', 3, NOW(), NOW()),
(35, 29, '装置层级删除', 2, 'process:location:delete', 4, NOW(), NOW()),
(36, 29, '隔离点查询', 2, 'process:isolation-point:query', 1, NOW(), NOW()),
(37, 29, '隔离点新增', 2, 'process:isolation-point:create', 2, NOW(), NOW()),
(38, 29, '隔离点修改', 2, 'process:isolation-point:update', 3, NOW(), NOW()),
(39, 29, '隔离点删除', 2, 'process:isolation-point:delete', 4, NOW(), NOW()),
(40, 29, '隔离点状态切换', 2, 'process:isolation-point:status', 5, NOW(), NOW()),
(41, 29, '隔离点占用切换', 2, 'process:isolation-point:occupy', 6, NOW(), NOW());

-- Assign all menus to admin role
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu;