-- 修复会员菜单历史迁移中错误复用菜单 ID 的问题。
-- 以权限码和组件路径定位，避免不同环境的菜单主键差异；可重复执行。

START TRANSACTION;

-- 统一会员菜单层级：会员管理(目录) -> 会员列表 / 会员卡记录 / 售卡订单 / 退卡订单。
SET @member_root_id := (
    SELECT menu_id FROM sys_menu
    WHERE menu_type = 'M' AND menu_name = '会员管理'
    ORDER BY menu_id LIMIT 1
);
SET @member_list_id := (
    SELECT menu_id FROM sys_menu
    WHERE component = 'member/list'
    ORDER BY menu_id LIMIT 1
);

UPDATE sys_menu
SET parent_id = @member_root_id, menu_type = 'C', perms = 'member:member:list',
    path = 'member', component = 'member/list', status = '0', update_time = NOW()
WHERE menu_id = @member_list_id;

UPDATE sys_menu
SET parent_id = @member_root_id, menu_type = 'C', perms = 'member:card:list', status = '0', update_time = NOW()
WHERE component = 'member/card/index';

UPDATE sys_menu
SET parent_id = @member_root_id, menu_type = 'C', perms = 'member:cardOrder:list', status = '0', update_time = NOW()
WHERE component = 'member/cardOrder/index';

UPDATE sys_menu
SET parent_id = @member_root_id, menu_type = 'C', perms = 'member:cardRefund:list', status = '0', update_time = NOW()
WHERE component = 'member/refundOrder/index';

-- 所有会员列表按钮挂到真正的会员列表页面，而非历史上被错误复用的按钮 ID。
UPDATE sys_menu
SET parent_id = @member_list_id, menu_type = 'F', update_time = NOW()
WHERE perms IN ('member:member:query', 'member:member:add', 'member:member:edit',
                'member:member:remove', 'member:member:export', 'member:member:import',
                'member:member:points');

-- 普通员工只保留会员列表以及查看所需查询权限，其他菜单/按钮全部移除。
DELETE rm
FROM sys_role_menu rm
JOIN sys_role r ON r.role_id = rm.role_id
WHERE r.role_key = 'common';

INSERT IGNORE INTO sys_role_menu(role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
JOIN sys_menu m ON m.menu_id IN (@member_root_id, @member_list_id)
WHERE r.role_key = 'common' AND r.del_flag = '0';

INSERT IGNORE INTO sys_role_menu(role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
JOIN sys_menu m ON m.perms = 'member:member:query'
WHERE r.role_key = 'common' AND r.del_flag = '0';

-- 经理：补齐会员删除按钮和售卡订单页面（含查询按钮）。
INSERT IGNORE INTO sys_role_menu(role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
JOIN sys_menu m ON m.menu_id = @member_root_id
   OR m.perms IN ('member:member:list', 'member:member:query', 'member:member:remove',
                  'member:cardOrder:list')
WHERE r.role_key = 'manager' AND r.del_flag = '0';

-- 店长：补齐员工管理目录和员工列表；售卡订单使用与经理相同的页面权限。
INSERT IGNORE INTO sys_role_menu(role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
JOIN sys_menu m ON (m.menu_type = 'M' AND m.menu_name = '系统管理')
   OR m.perms IN ('system:user:list', 'member:member:list', 'member:member:query', 'member:cardOrder:list')
   OR m.menu_id = @member_root_id
WHERE r.role_key = 'shop_manager' AND r.del_flag = '0';

-- 退卡后售卡订单应展示“已退款”；对已有历史数据做一次对账回填。
UPDATE member_card_order o
LEFT JOIN member_card c ON c.id = o.member_card_id
LEFT JOIN member_card_refund_order r ON r.member_card_id = o.member_card_id AND r.is_del = 0
SET o.order_status = 2, o.updated_at = NOW()
WHERE o.is_del = 0
  AND o.order_status <> 2
  AND (c.status = 3 OR r.member_card_id IS NOT NULL);

-- 历史记录中曾写入登录账号或员工 ID；可匹配到 sys_user 的数据统一展示为员工姓名。
UPDATE member m
JOIN sys_user u ON (BINARY m.last_operator = BINARY u.user_name OR m.last_operator = CAST(u.user_id AS CHAR))
SET m.last_operator = u.nick_name, m.updated_at = NOW()
WHERE u.nick_name IS NOT NULL AND u.nick_name <> '';

UPDATE member_card c
JOIN sys_user u ON (c.create_staff_id = CAST(u.user_id AS CHAR) OR BINARY c.create_staff_name = BINARY u.user_name)
SET c.create_staff_name = u.nick_name, c.updated_at = NOW()
WHERE u.nick_name IS NOT NULL AND u.nick_name <> '';

UPDATE member_card_order o
JOIN sys_user u ON (o.create_staff_id = CAST(u.user_id AS CHAR) OR BINARY o.create_staff_name = BINARY u.user_name)
SET o.create_staff_name = u.nick_name, o.updated_at = NOW()
WHERE u.nick_name IS NOT NULL AND u.nick_name <> '';

COMMIT;
