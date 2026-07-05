-- 会员售卡订单、会员退卡订单菜单和权限
-- 父菜单：会员管理，当前项目中为 menu_id=1062。

INSERT INTO sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type,
 visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 121, '会员售卡订单', 1062, 4, '/member/card-order', 'member/cardOrder/index', '', '', 1, 0, 'C',
       '0', '0', 'member:cardOrder:list', 'list', 'admin', sysdate(), '', null, '会员开卡、续卡、导入开卡售卡订单对账'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 121);

INSERT INTO sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type,
 visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 122, '会员退卡订单', 1062, 5, '/member/refund-order', 'member/refundOrder/index', '', '', 1, 0, 'C',
       '0', '0', 'member:cardRefund:list', 'money', 'admin', sysdate(), '', null, '会员退卡退款订单对账'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 122);

UPDATE sys_menu
SET icon = 'money'
WHERE menu_id = 122;

INSERT INTO sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type,
 visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 1076, '会员售卡订单查询', 121, 1, '#', '', '', '', 1, 0, 'F',
       '0', '0', 'member:cardOrder:list', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 1076);

INSERT INTO sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type,
 visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT 1077, '会员退卡订单查询', 122, 1, '#', '', '', '', 1, 0, 'F',
       '0', '0', 'member:cardRefund:list', '#', 'admin', sysdate(), '', null, ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 1077);
