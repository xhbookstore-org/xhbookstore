-- Member and card type menu permissions
-- Safe to run repeatedly on MySQL 8+ because menu_id is the primary key.

insert into sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values
(5, '会员管理', 0, 5, 'member', null, '', '', 1, 0, 'M', '0', '0', '', 'peoples', 'admin', sysdate(), '', null, '会员管理目录'),
(118, '会员列表', 5, 1, 'member', 'member/list', '', '', 1, 0, 'C', '0', '0', 'member:member:list', 'peoples', 'admin', sysdate(), '', null, '会员列表菜单'),
(119, '卡类型配置', 5, 2, 'cardtype', 'system/cardtype/index', '', '', 1, 0, 'C', '0', '0', 'member:cardType:list', 'dict', 'admin', sysdate(), '', null, '卡类型配置菜单'),
(1061, '会员查询', 118, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'member:member:query', '#', 'admin', sysdate(), '', null, ''),
(1062, '会员新增', 118, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'member:member:add', '#', 'admin', sysdate(), '', null, ''),
(1063, '会员修改', 118, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'member:member:edit', '#', 'admin', sysdate(), '', null, ''),
(1064, '会员删除', 118, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'member:member:remove', '#', 'admin', sysdate(), '', null, ''),
(1065, '会员导出', 118, 5, '#', '', '', '', 1, 0, 'F', '0', '0', 'member:member:export', '#', 'admin', sysdate(), '', null, ''),
(1066, '会员导入', 118, 6, '#', '', '', '', 1, 0, 'F', '0', '0', 'member:member:import', '#', 'admin', sysdate(), '', null, ''),
(1067, '积分调整', 118, 7, '#', '', '', '', 1, 0, 'F', '0', '0', 'member:member:points', '#', 'admin', sysdate(), '', null, ''),
(1068, '卡类型查询', 119, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'member:cardType:query', '#', 'admin', sysdate(), '', null, ''),
(1069, '卡类型新增', 119, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'member:cardType:add', '#', 'admin', sysdate(), '', null, ''),
(1070, '卡类型修改', 119, 3, '#', '', '', '', 1, 0, 'F', '0', '0', 'member:cardType:edit', '#', 'admin', sysdate(), '', null, ''),
(1071, '卡类型删除', 119, 4, '#', '', '', '', 1, 0, 'F', '0', '0', 'member:cardType:remove', '#', 'admin', sysdate(), '', null, '')
on duplicate key update
  menu_name = values(menu_name), parent_id = values(parent_id), order_num = values(order_num), path = values(path),
  component = values(component), menu_type = values(menu_type), perms = values(perms), icon = values(icon),
  visible = values(visible), status = values(status), update_time = sysdate(), remark = values(remark);