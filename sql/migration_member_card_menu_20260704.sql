-- Member card record menu and button permissions.
-- Safe to run repeatedly on MySQL 8+ because menu_id is the primary key.

insert into sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values
(120, '会员卡记录', 1062, 3, '/member/card', 'member/card/index', '', '', 1, 0, 'C', '0', '0', 'member:card:list', 'money', 'admin', sysdate(), '', null, '会员卡售卖、状态、退卡和对账记录'),
(1072, '会员卡查询', 120, 1, '#', '', '', '', 1, 0, 'F', '0', '0', 'member:card:query', '#', 'admin', sysdate(), '', null, ''),
(1073, '会员卡退卡', 120, 2, '#', '', '', '', 1, 0, 'F', '0', '0', 'member:card:refund', '#', 'admin', sysdate(), '', null, ''),
(1074, '会员列表查看会员卡', 1063, 8, '#', '', '', '', 1, 0, 'F', '0', '0', 'member:card:list', '#', 'admin', sysdate(), '', null, ''),
(1075, '会员列表退卡', 1063, 9, '#', '', '', '', 1, 0, 'F', '0', '0', 'member:card:refund', '#', 'admin', sysdate(), '', null, '')
on duplicate key update
  menu_name = values(menu_name), parent_id = values(parent_id), order_num = values(order_num), path = values(path),
  component = values(component), menu_type = values(menu_type), perms = values(perms), icon = values(icon),
  visible = values(visible), status = values(status), update_time = sysdate(), remark = values(remark);
