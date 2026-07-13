-- 修复普通角色、经理、店长的菜单与按钮权限。
-- 角色键：common=普通角色，manager=经理，shop_manager=店长。

START TRANSACTION;

-- 首页权限节点必须出现在角色授权树中；首页路由本身由前端静态路由提供。
INSERT INTO sys_menu
    (menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
     is_frame, is_cache, menu_type, visible, status, perms, icon,
     create_by, create_time, update_by, update_time, remark)
SELECT 123, '首页', 0, 0, 'index', 'index', '', 'IndexPermission',
       1, 0, 'C', '1', '0', 'dashboard:member:view', 'dashboard',
       'admin', NOW(), '', NULL, '首页权限配置节点'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'dashboard:member:view');

INSERT INTO sys_menu
    (menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
     is_frame, is_cache, menu_type, visible, status, perms, icon,
     create_by, create_time, update_by, update_time, remark)
SELECT 124, '首页统计刷新', 123, 1, '#', '', '', '',
       1, 0, 'F', '0', '0', 'dashboard:member:refresh', '#',
       'admin', NOW(), '', NULL, '首页统计刷新按钮'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'dashboard:member:refresh');

-- 普通角色仅保留会员列表和页面初始化所需的基础查询权限。
DELETE rm
FROM sys_role_menu rm
JOIN sys_role r ON r.role_id = rm.role_id
WHERE r.role_key = 'common';

INSERT IGNORE INTO sys_role_menu(role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
JOIN sys_menu m ON m.menu_id IN (1062, 1063, 1079)
WHERE r.role_key = 'common' AND r.del_flag = '0';

-- 经理补齐会员删除按钮、售卡订单及其页面依赖权限。
INSERT IGNORE INTO sys_role_menu(role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
JOIN sys_menu m ON m.menu_id IN (1062, 1063, 1079, 1082, 121, 1076, 103, 1016)
WHERE r.role_key = 'manager' AND r.del_flag = '0';

-- 店长补齐员工管理入口、会员售卡订单及页面依赖权限。
INSERT IGNORE INTO sys_role_menu(role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
JOIN sys_menu m ON m.menu_id IN (1061, 100, 1062, 1063, 1079, 121, 1076, 103, 1016)
WHERE r.role_key = 'shop_manager' AND r.del_flag = '0';

-- 超级管理员始终拥有全部启用菜单和按钮。
INSERT IGNORE INTO sys_role_menu(role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
CROSS JOIN sys_menu m
WHERE r.role_key = 'admin' AND r.del_flag = '0' AND m.status = '0';

COMMIT;
