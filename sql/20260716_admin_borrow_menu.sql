-- Admin 借阅管理菜单、按钮权限和默认角色授权。
-- 可重复执行；数据范围由角色自身 data_scope 和 book_borrow_order.dept_id 控制。

-- 首次创建时为借阅管理预留会员管理之后的位置，并将后续一级菜单顺延一位。
SET @borrow_menu_exists := (SELECT COUNT(1) FROM sys_menu WHERE menu_id = 130);
SET @member_menu_order := COALESCE(
  (SELECT order_num FROM sys_menu WHERE parent_id = 0 AND menu_name = '会员管理' LIMIT 1),
  1
);
UPDATE sys_menu
SET order_num = order_num + 1
WHERE parent_id = 0
  AND menu_id <> 130
  AND order_num > @member_menu_order
  AND @borrow_menu_exists = 0;

INSERT INTO sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
 is_frame, is_cache, menu_type, visible, status, perms, icon,
 create_by, create_time, update_by, update_time, remark)
VALUES
(130, '借阅管理', 0, @member_menu_order + 1, 'borrow', NULL, '', 'BorrowManagement',
 1, 0, 'M', '0', '0', '', 'education',
 'admin', NOW(), '', NULL, '逐册借阅订单、明细和图片管理'),
(131, '借阅记录', 130, 1, 'record', 'borrow/record/index', '', 'BorrowRecord',
 1, 0, 'C', '0', '0', 'borrow:record:list', 'list',
 'admin', NOW(), '', NULL, '一张借阅单对应多条逐册明细'),
(1090, '借阅记录查询', 131, 1, '#', '', '', '',
 1, 0, 'F', '0', '0', 'borrow:record:list', '#',
 'admin', NOW(), '', NULL, ''),
(1091, '借阅详情查看', 131, 2, '#', '', '', '',
 1, 0, 'F', '0', '0', 'borrow:record:query', '#',
 'admin', NOW(), '', NULL, ''),
(1092, '借阅记录导出', 131, 3, '#', '', '', '',
 1, 0, 'F', '0', '0', 'borrow:record:export', '#',
 'admin', NOW(), '', NULL, '')
ON DUPLICATE KEY UPDATE
  menu_name = VALUES(menu_name), parent_id = VALUES(parent_id), order_num = VALUES(order_num),
  path = VALUES(path), component = VALUES(component), route_name = VALUES(route_name),
  menu_type = VALUES(menu_type), visible = VALUES(visible), status = VALUES(status),
  perms = VALUES(perms), icon = VALUES(icon), update_time = NOW(), remark = VALUES(remark);

-- 普通角色可查看本数据范围内的列表和详情，不默认授予导出。
INSERT IGNORE INTO sys_role_menu(role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
JOIN sys_menu m ON m.menu_id IN (130, 131, 1090, 1091)
WHERE r.role_key = 'common' AND r.del_flag = '0';

-- 经理、店长可查询并导出本数据范围内的记录。
INSERT IGNORE INTO sys_role_menu(role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
JOIN sys_menu m ON m.menu_id IN (130, 131, 1090, 1091, 1092)
WHERE r.role_key IN ('manager', 'shop_manager') AND r.del_flag = '0';

-- 管理员角色拥有完整借阅管理权限。
INSERT IGNORE INTO sys_role_menu(role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
JOIN sys_menu m ON m.menu_id IN (130, 131, 1090, 1091, 1092)
WHERE r.role_key = 'admin' AND r.del_flag = '0';
