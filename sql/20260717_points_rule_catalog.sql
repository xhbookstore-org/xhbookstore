-- 积分获取/消耗规则目录及后台管理菜单。
-- 可重复执行；重复执行不会覆盖后台已经修改过的积分数值。

-- 测试/生产当前使用 MySQL 5.7，不支持 ADD COLUMN IF NOT EXISTS。
-- 通过 information_schema 生成条件 DDL，保证脚本可重复执行。
SET @points_rule_column_sql := (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE xhbs_points_rule ADD COLUMN implementation_status VARCHAR(20) NOT NULL DEFAULT ''NOT_STARTED'' COMMENT ''业务开发状态：EXISTING/IN_PROGRESS/NOT_STARTED'' AFTER rule_source',
        'SELECT 1')
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'xhbs_points_rule'
      AND column_name = 'implementation_status'
);
PREPARE points_rule_column_stmt FROM @points_rule_column_sql;
EXECUTE points_rule_column_stmt;
DEALLOCATE PREPARE points_rule_column_stmt;

INSERT INTO xhbs_points_rule (
    rule_code, rule_name, scene_code, rule_source, implementation_status,
    direction, trigger_mode, trigger_event, calculation_mode,
    fixed_points, points_per_unit, unit_type,
    manual_points_editable, member_day_enabled, member_day_days, member_day_multiplier,
    require_biz_order, require_evidence, exclude_bulk_purchase, freeze_days,
    status, sort_order, remark, operator_name
) VALUES
('REGISTER_MEMBER', '注册会员', 'REGISTER_MEMBER', 'SYSTEM', 'EXISTING',
 'ADD', 'AUTO', 'MEMBER_REGISTERED', 'FIXED',
 100, NULL, NULL, 0, 0, NULL, NULL,
 1, 0, 1, 0, 'ENABLED', 10, '普通会员首次注册获得100积分；批量采购不发放', 'SYSTEM'),
('BUY_PREMIUM_CARD', '购买尊享会员', 'BUY_PREMIUM_CARD', 'SYSTEM', 'EXISTING',
 'ADD', 'AUTO', 'CARD_PURCHASE_PAID', 'FIXED',
 365, NULL, NULL, 0, 0, NULL, NULL,
 1, 0, 1, 7, 'ENABLED', 20, '购买尊享会员获得365积分，入账可见并冻结7天', 'SYSTEM'),
('BUY_ENJOY_CARD', '购买畅享会员', 'BUY_ENJOY_CARD', 'SYSTEM', 'EXISTING',
 'ADD', 'AUTO', 'CARD_PURCHASE_PAID', 'FIXED',
 188, NULL, NULL, 0, 0, NULL, NULL,
 1, 0, 1, 7, 'ENABLED', 30, '购买畅享会员获得188积分，入账可见并冻结7天', 'SYSTEM'),
('RENEW_PREMIUM_CARD', '续费尊享会员', 'RENEW_PREMIUM_CARD', 'SYSTEM', 'EXISTING',
 'ADD', 'AUTO', 'CARD_RENEWAL_PAID', 'FIXED',
 365, NULL, NULL, 0, 0, NULL, NULL,
 1, 0, 1, 7, 'ENABLED', 40, '续费尊享会员获得365积分，入账可见并冻结7天', 'SYSTEM'),
('RENEW_ENJOY_CARD', '续费畅享会员', 'RENEW_ENJOY_CARD', 'SYSTEM', 'EXISTING',
 'ADD', 'AUTO', 'CARD_RENEWAL_PAID', 'FIXED',
 188, NULL, NULL, 0, 0, NULL, NULL,
 1, 0, 1, 7, 'ENABLED', 50, '续费畅享会员获得188积分，入账可见并冻结7天', 'SYSTEM'),
('OFFLINE_EVENT_TEMPLATE', '线下参与活动核销', 'OFFLINE_EVENT', 'CUSTOM', 'NOT_STARTED',
 'ADD', 'AUTO', 'OFFLINE_EVENT_VERIFIED', 'FIXED',
 NULL, NULL, NULL, 0, 0, NULL, NULL,
 1, 1, 0, 0, 'DRAFT', 60, '自定义模板；创建具体活动规则时配置积分额度和有效期', 'SYSTEM'),
('BORROW_BOOK', '借阅图书', 'BORROW_BOOK', 'SYSTEM', 'IN_PROGRESS',
 'ADD', 'AUTO', 'BORROW_COMPLETED', 'PER_ITEM',
 NULL, 10, 'ITEM', 0, 0, NULL, NULL,
 1, 0, 0, 0, 'ENABLED', 70, '完成一次借阅按逐册明细数发放，一册10积分', 'SYSTEM'),
('PURCHASE_BOOK', '购买书籍', 'PURCHASE_BOOK', 'SYSTEM', 'IN_PROGRESS',
 'ADD', 'MANUAL', 'BOOK_PURCHASE_RECORDED', 'PER_YUAN',
 NULL, 1, 'YUAN', 1, 1, JSON_ARRAY(6, 16, 26), 2.000,
 1, 1, 0, 0, 'ENABLED', 80, '员工扫码并录入实付金额；每月6、16、26日双倍积分', 'SYSTEM'),
('PURCHASE_SELF_GOODS', '购买店内自营商品', 'PURCHASE_SELF_GOODS', 'SYSTEM', 'IN_PROGRESS',
 'ADD', 'MANUAL', 'SELF_GOODS_PURCHASE_RECORDED', 'PER_YUAN',
 NULL, 1, 'YUAN', 1, 1, JSON_ARRAY(6, 16, 26), 2.000,
 1, 1, 0, 0, 'ENABLED', 90, '员工扫码并录入实付金额；每月6、16、26日双倍积分', 'SYSTEM'),
('PURCHASE_STORED_VALUE', '购买储值卡', 'PURCHASE_STORED_VALUE', 'SYSTEM', 'IN_PROGRESS',
 'ADD', 'MANUAL', 'STORED_VALUE_PURCHASE_RECORDED', 'PER_YUAN',
 NULL, 1, 'YUAN', 1, 0, NULL, NULL,
 1, 1, 0, 0, 'ENABLED', 100, '员工扫码并录入实付金额；1元1积分，不参加会员日倍增', 'SYSTEM'),
('PAID_EVENT_TEMPLATE', '付费活动抵扣', 'PAID_EVENT', 'CUSTOM', 'NOT_STARTED',
 'DEDUCT', 'AUTO', 'PAID_EVENT_CONFIRMED', 'FIXED',
 NULL, NULL, NULL, 0, 0, NULL, NULL,
 1, 1, 0, 0, 'DRAFT', 110, '自定义模板；创建具体付费活动规则时配置扣减积分和有效期', 'SYSTEM'),
('GIFT_EXCHANGE', '兑换礼品', 'GIFT_EXCHANGE', 'SYSTEM', 'NOT_STARTED',
 'DEDUCT', 'MANUAL', 'GIFT_EXCHANGE_CONFIRMED', 'FIXED',
 NULL, NULL, NULL, 0, 0, NULL, NULL,
 1, 1, 0, 0, 'DRAFT', 120, '员工扫码核销礼品；需先配置单次扣减积分', 'SYSTEM'),
('REFUND_PREMIUM_CARD', '尊享会员卡退订', 'REFUND_PREMIUM_CARD', 'SYSTEM', 'EXISTING',
 'DEDUCT', 'AUTO', 'CARD_REFUNDED', 'FIXED',
 365, NULL, NULL, 0, 0, NULL, NULL,
 1, 0, 1, 0, 'ENABLED', 130, '7天退卡时冲正原购卡冻结积分，不占用其他可用积分', 'SYSTEM'),
('REFUND_ENJOY_CARD', '畅享会员卡退订', 'REFUND_ENJOY_CARD', 'SYSTEM', 'EXISTING',
 'DEDUCT', 'AUTO', 'CARD_REFUNDED', 'FIXED',
 188, NULL, NULL, 0, 0, NULL, NULL,
 1, 0, 1, 0, 'ENABLED', 140, '7天退卡时冲正原购卡冻结积分，不占用其他可用积分', 'SYSTEM'),
('RETURN_ORDER', '退书或商品', 'RETURN_ORDER', 'SYSTEM', 'IN_PROGRESS',
 'DEDUCT', 'MANUAL', 'RETURN_ORDER_CONFIRMED', 'ORIGINAL_ORDER',
 NULL, NULL, NULL, 0, 0, NULL, NULL,
 1, 1, 0, 0, 'ENABLED', 150, '必须关联原积分订单，按原订单实际发放积分扣回', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    rule_name = VALUES(rule_name),
    scene_code = VALUES(scene_code),
    rule_source = VALUES(rule_source),
    implementation_status = VALUES(implementation_status),
    direction = VALUES(direction),
    trigger_mode = VALUES(trigger_mode),
    trigger_event = VALUES(trigger_event),
    calculation_mode = VALUES(calculation_mode),
    fixed_points = COALESCE(xhbs_points_rule.fixed_points, VALUES(fixed_points)),
    points_per_unit = COALESCE(xhbs_points_rule.points_per_unit, VALUES(points_per_unit)),
    unit_type = VALUES(unit_type),
    manual_points_editable = VALUES(manual_points_editable),
    member_day_enabled = VALUES(member_day_enabled),
    member_day_days = VALUES(member_day_days),
    member_day_multiplier = VALUES(member_day_multiplier),
    require_biz_order = VALUES(require_biz_order),
    require_evidence = VALUES(require_evidence),
    exclude_bulk_purchase = VALUES(exclude_bulk_purchase),
    freeze_days = VALUES(freeze_days),
    sort_order = VALUES(sort_order),
    remark = VALUES(remark),
    updated_at = NOW();

-- 没有积分数值的固定规则只能作为草稿模板，避免被业务误选。
UPDATE xhbs_points_rule
SET status = 'DRAFT', updated_at = NOW()
WHERE rule_code IN ('OFFLINE_EVENT_TEMPLATE', 'PAID_EVENT_TEMPLATE', 'GIFT_EXCHANGE')
  AND fixed_points IS NULL AND used_count = 0 AND status = 'ENABLED';

SET @system_menu_id := (
    SELECT menu_id FROM sys_menu
    WHERE parent_id = 0 AND menu_name = '系统管理'
    ORDER BY menu_id DESC LIMIT 1
);

INSERT INTO sys_menu
(menu_id, menu_name, parent_id, order_num, path, component, query, route_name,
 is_frame, is_cache, menu_type, visible, status, perms, icon,
 create_by, create_time, update_by, update_time, remark)
VALUES
(132, '积分规则管理', @system_menu_id, 8, 'points-rule', 'member/pointsRule/index', '', 'SystemPointsRule',
 1, 0, 'C', '0', '0', 'system:pointsRule:list', 'star',
 'admin', NOW(), '', NULL, '积分获取与消耗规则增删改查'),
(1093, '积分规则查询', 132, 1, '#', '', '', '',
 1, 0, 'F', '0', '0', 'system:pointsRule:query', '#',
 'admin', NOW(), '', NULL, ''),
(1094, '积分规则修改', 132, 2, '#', '', '', '',
 1, 0, 'F', '0', '0', 'system:pointsRule:edit', '#',
 'admin', NOW(), '', NULL, ''),
(1095, '积分规则新增', 132, 3, '#', '', '', '',
 1, 0, 'F', '0', '0', 'system:pointsRule:add', '#',
 'admin', NOW(), '', NULL, ''),
(1096, '积分规则删除', 132, 4, '#', '', '', '',
 1, 0, 'F', '0', '0', 'system:pointsRule:remove', '#',
 'admin', NOW(), '', NULL, ''),
(1097, '积分规则列表', 132, 5, '#', '', '', '',
 1, 0, 'F', '0', '0', 'system:pointsRule:list', '#',
 'admin', NOW(), '', NULL, '')
ON DUPLICATE KEY UPDATE
    menu_name = VALUES(menu_name), parent_id = VALUES(parent_id), order_num = VALUES(order_num),
    path = VALUES(path), component = VALUES(component), route_name = VALUES(route_name),
    visible = VALUES(visible), status = VALUES(status), perms = VALUES(perms),
    icon = VALUES(icon), update_time = NOW(), remark = VALUES(remark);

INSERT IGNORE INTO sys_role_menu(role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
JOIN sys_menu m ON m.menu_id IN (132, 1093, 1094, 1095, 1096, 1097)
WHERE r.role_key = 'admin' AND r.del_flag = '0';
