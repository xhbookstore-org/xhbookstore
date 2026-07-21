-- 积分规则有效期（默认360天）、积分流水导出权限。
-- 代码部署前执行；脚本可重复执行。

SET @add_points_valid_days_sql := (
    SELECT IF(COUNT(*) = 0,
        'ALTER TABLE xhbs_points_rule ADD COLUMN points_valid_days INT NOT NULL DEFAULT 360 COMMENT ''发放积分有效天数，从入账或解冻时间起算'' AFTER freeze_days',
        'SELECT 1')
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'xhbs_points_rule'
      AND column_name = 'points_valid_days'
);
PREPARE add_points_valid_days_stmt FROM @add_points_valid_days_sql;
EXECUTE add_points_valid_days_stmt;
DEALLOCATE PREPARE add_points_valid_days_stmt;

UPDATE xhbs_points_rule
SET points_valid_days = 360
WHERE points_valid_days IS NULL OR points_valid_days <= 0;

SET @points_menu_id := (
    SELECT menu_id FROM sys_menu
    WHERE component = 'member/points/index' OR perms = 'member:points:list'
    ORDER BY menu_id DESC LIMIT 1
);

INSERT INTO sys_menu
(menu_name, parent_id, order_num, path, component, query, route_name,
 is_frame, is_cache, menu_type, visible, status, perms, icon,
 create_by, create_time, update_by, update_time, remark)
SELECT '积分流水导出', @points_menu_id, 3, '#', '', '', '',
       1, 0, 'F', '0', '0', 'member:points:export', '#',
       'admin', NOW(), '', NULL, ''
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu WHERE perms = 'member:points:export'
);

INSERT IGNORE INTO sys_role_menu(role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
JOIN sys_menu m ON m.perms = 'member:points:export'
WHERE r.role_key IN ('common', 'shop_manager', 'manager', 'admin')
  AND r.del_flag = '0';
