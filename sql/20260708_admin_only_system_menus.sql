-- 一级【系统管理】、【系统监控】、【系统工具】只授权给admin角色
-- 说明：
-- 1. role_id=1 为超级管理员/admin角色。
-- 2. 删除非admin角色对这三个一级菜单及其全部下级菜单/按钮的授权。
-- 3. 补齐admin角色对这三个一级菜单及其全部下级菜单/按钮的授权。
-- 4. MySQL 5.7临时表不能自引用，按固定层级收集菜单树。

START TRANSACTION;

DROP TEMPORARY TABLE IF EXISTS tmp_admin_only_menu;
CREATE TEMPORARY TABLE tmp_admin_only_menu (
  menu_id bigint(20) NOT NULL PRIMARY KEY
) ENGINE=MEMORY;

INSERT IGNORE INTO tmp_admin_only_menu(menu_id)
SELECT menu_id FROM sys_menu WHERE menu_id IN (1, 2, 3);

INSERT IGNORE INTO tmp_admin_only_menu(menu_id)
SELECT menu_id FROM sys_menu WHERE parent_id IN (1, 2, 3);

INSERT IGNORE INTO tmp_admin_only_menu(menu_id)
SELECT menu_id FROM sys_menu WHERE parent_id IN (
  SELECT menu_id FROM sys_menu WHERE parent_id IN (1, 2, 3)
);

INSERT IGNORE INTO tmp_admin_only_menu(menu_id)
SELECT menu_id FROM sys_menu WHERE parent_id IN (
  SELECT menu_id FROM sys_menu WHERE parent_id IN (
    SELECT menu_id FROM sys_menu WHERE parent_id IN (1, 2, 3)
  )
);

DELETE rm
FROM sys_role_menu rm
INNER JOIN tmp_admin_only_menu t ON rm.menu_id = t.menu_id
WHERE rm.role_id <> 1;

INSERT INTO sys_role_menu(role_id, menu_id)
SELECT 1, t.menu_id
FROM tmp_admin_only_menu t
WHERE NOT EXISTS (
  SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = 1 AND rm.menu_id = t.menu_id
);

COMMIT;
