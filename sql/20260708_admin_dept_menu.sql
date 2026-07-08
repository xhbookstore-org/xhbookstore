-- Admin菜单调整：新增一级【部门管理】，并将【系统管理】、【部门管理】移入其下
-- 说明：
-- 1. 新增一级目录 menu_id=1078，path=department。
-- 2. 原一级【系统管理】menu_id=1 改为新一级【部门管理】下的二级目录。
-- 3. 原【系统管理】下的【部门管理】menu_id=103 改为新一级【部门管理】下的二级菜单。
-- 4. 给拥有原【系统管理】或原【部门管理】权限的角色补充分配新一级目录。

START TRANSACTION;

SET @dept_menu_id := 1078;

INSERT INTO sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT @dept_menu_id, '部门管理', 0, 3, 'department', NULL, '', '', 1, 0, 'M', '0', '0', '', 'tree', 'admin', NOW(), '', NULL, '部门管理目录'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = @dept_menu_id);

UPDATE sys_menu
SET parent_id = @dept_menu_id, order_num = 1, update_by = 'admin', update_time = NOW()
WHERE menu_id = 1;

UPDATE sys_menu
SET parent_id = @dept_menu_id, order_num = 2, update_by = 'admin', update_time = NOW()
WHERE menu_id = 103;

INSERT INTO sys_role_menu(role_id, menu_id)
SELECT DISTINCT rm.role_id, @dept_menu_id
FROM sys_role_menu rm
WHERE rm.menu_id IN (1, 103)
  AND NOT EXISTS (
      SELECT 1 FROM sys_role_menu exists_rm
      WHERE exists_rm.role_id = rm.role_id AND exists_rm.menu_id = @dept_menu_id
  );

COMMIT;
