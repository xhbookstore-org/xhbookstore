-- 修复会员管理菜单权限码与后端 @PreAuthorize 注解不一致的问题
-- 影响：
-- 1. 会员列表页面 /member/list 需要 member:member:list
-- 2. 会员详情、卡类型下拉等接口需要 member:member:query
-- 3. 卡类型配置接口需要 member:cardType:*，不是旧的 system:cardtype:*
-- 4. 会员页面门店下拉会调用部门列表，需要 system:dept:list / system:dept:query

START TRANSACTION;

UPDATE sys_menu
SET perms = 'member:member:list', update_time = NOW()
WHERE menu_id = 1063;

UPDATE sys_menu
SET parent_id = 1062,
    perms = 'member:cardType:list',
    update_time = NOW()
WHERE menu_id = 1064;

UPDATE sys_menu SET perms = 'member:cardType:query', update_time = NOW() WHERE menu_id = 1065;
UPDATE sys_menu SET perms = 'member:cardType:add', update_time = NOW() WHERE menu_id = 1066;
UPDATE sys_menu SET perms = 'member:cardType:edit', update_time = NOW() WHERE menu_id = 1067;
UPDATE sys_menu SET perms = 'member:cardType:remove', update_time = NOW() WHERE menu_id = 1068;

INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 1079, '会员查询', 1063, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'member:member:query', '#', 'admin', NOW(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 1079 OR perms = 'member:member:query');

INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 1080, '会员新增', 1063, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'member:member:add', '#', 'admin', NOW(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 1080 OR perms = 'member:member:add');

INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 1081, '会员修改', 1063, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'member:member:edit', '#', 'admin', NOW(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 1081 OR perms = 'member:member:edit');

INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 1082, '会员删除', 1063, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'member:member:remove', '#', 'admin', NOW(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 1082 OR perms = 'member:member:remove');

INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 1083, '会员导出', 1063, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'member:member:export', '#', 'admin', NOW(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 1083 OR perms = 'member:member:export');

INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 1084, '会员导入', 1063, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'member:member:import', '#', 'admin', NOW(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 1084 OR perms = 'member:member:import');

INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 1085, '积分调整', 1063, 7, '#', '', '', '', 1, 0, 'F', '0', '0', 'member:member:points', '#', 'admin', NOW(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 1085 OR perms = 'member:member:points');

INSERT INTO sys_menu(menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 1086, '会员卡导出', 120, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'member:card:export', '#', 'admin', NOW(), '', NULL, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 1086 OR perms = 'member:card:export');

-- 超级管理员角色应拥有全部启用菜单和按钮。否则非 user_id=1 的 admin 角色用户仍会遇到 403。
INSERT INTO sys_role_menu(role_id, menu_id)
SELECT 1, m.menu_id
FROM sys_menu m
WHERE m.status = '0'
  AND NOT EXISTS (
      SELECT 1 FROM sys_role_menu rm
      WHERE rm.role_id = 1 AND rm.menu_id = m.menu_id
  );

-- 会员相关页面初始化会调用卡类型下拉和门店下拉。
-- 凡是能进入会员列表/会员卡/售卡订单/退卡订单的角色，都需要这些基础查询权限。
INSERT INTO sys_role_menu(role_id, menu_id)
SELECT DISTINCT rm.role_id, m.menu_id
FROM sys_role_menu rm
JOIN sys_menu owned ON owned.menu_id = rm.menu_id
JOIN sys_menu m ON m.menu_id IN (1079, 103, 1016)
WHERE owned.menu_id IN (1063, 120, 121, 122)
  AND NOT EXISTS (
      SELECT 1 FROM sys_role_menu x
      WHERE x.role_id = rm.role_id AND x.menu_id = m.menu_id
  );

COMMIT;
