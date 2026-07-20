-- Admin 积分流水列表、详情菜单与只读权限。
-- 可重复执行；通过菜单名称定位会员管理，兼容不同环境的 menu_id。

SET @member_menu_id := (
    SELECT menu_id FROM sys_menu
    WHERE parent_id = 0 AND menu_name = '会员管理'
    ORDER BY menu_id DESC LIMIT 1
);

INSERT INTO sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
 is_frame, is_cache, menu_type, visible, status, perms, icon,
 create_by, create_time, update_by, update_time, remark)
VALUES
(133, '积分流水', @member_menu_id, 6, 'points', 'member/points/index', '', 'MemberPointsOrder',
 1, 0, 'C', '0', '0', 'member:points:list', 'chart',
 'admin', NOW(), '', NULL, '积分总流水、入账批次、出账单和核销明细只读查询'),
(1098, '积分流水查询', 133, 1, '#', '', '', '',
 1, 0, 'F', '0', '0', 'member:points:list', '#',
 'admin', NOW(), '', NULL, ''),
(1099, '积分流水详情', 133, 2, '#', '', '', '',
 1, 0, 'F', '0', '0', 'member:points:query', '#',
 'admin', NOW(), '', NULL, '')
ON DUPLICATE KEY UPDATE
    menu_name = VALUES(menu_name), parent_id = VALUES(parent_id), order_num = VALUES(order_num),
    path = VALUES(path), component = VALUES(component), route_name = VALUES(route_name),
    menu_type = VALUES(menu_type), visible = VALUES(visible), status = VALUES(status),
    perms = VALUES(perms), icon = VALUES(icon), update_time = NOW(), remark = VALUES(remark);

-- 店员、店长、经理和管理员均可在各自数据范围内只读查询。
INSERT IGNORE INTO sys_role_menu(role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
JOIN sys_menu m ON m.menu_id IN (133, 1098, 1099)
WHERE r.role_key IN ('common', 'shop_manager', 'manager', 'admin')
  AND r.del_flag = '0';
