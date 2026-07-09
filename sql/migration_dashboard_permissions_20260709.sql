-- 首页统计权限点：用于角色授权配置和后端接口鉴权
INSERT INTO sys_menu
    (menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
     is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 123, '首页', 0, 0, 'index', 'index', '', 'IndexPermission',
       1, 0, 'C', '1', '0', 'dashboard:member:view', 'dashboard', 'admin', sysdate(), '', null, '首页会员统计权限占位菜单'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 123);

INSERT INTO sys_menu
    (menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
     is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 124, '首页统计刷新', 123, 1, '#', '', '', '',
       1, 0, 'F', '0', '0', 'dashboard:member:refresh', '#', 'admin', sysdate(), '', null, '首页会员统计手动刷新权限'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'dashboard:member:refresh');

INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, m.menu_id
FROM sys_menu m
WHERE m.menu_id IN (123, 124)
  AND NOT EXISTS (
      SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 1 AND rm.menu_id = m.menu_id
  );
