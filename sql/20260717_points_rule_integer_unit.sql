-- 单位积分统一为整数，并移除已无意义的取整方式。
ALTER TABLE xhbs_points_rule
    MODIFY COLUMN points_per_unit INT NULL COMMENT '每单位积分（整数）';

SET @drop_rounding_mode_sql := (
    SELECT IF(COUNT(*) = 1,
        'ALTER TABLE xhbs_points_rule DROP COLUMN rounding_mode',
        'SELECT 1')
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'xhbs_points_rule'
      AND column_name = 'rounding_mode'
);
PREPARE drop_rounding_mode_stmt FROM @drop_rounding_mode_sql;
EXECUTE drop_rounding_mode_stmt;
DEALLOCATE PREPARE drop_rounding_mode_stmt;

-- 根据需求备注校准所有已明确的积分数值与冻结天数。
UPDATE xhbs_points_rule
SET fixed_points = CASE rule_code
        WHEN 'REGISTER_MEMBER' THEN 100
        WHEN 'BUY_PREMIUM_CARD' THEN 365
        WHEN 'BUY_ENJOY_CARD' THEN 188
        WHEN 'RENEW_PREMIUM_CARD' THEN 365
        WHEN 'RENEW_ENJOY_CARD' THEN 188
        WHEN 'REFUND_PREMIUM_CARD' THEN 365
        WHEN 'REFUND_ENJOY_CARD' THEN 188
        WHEN 'OFFLINE_EVENT_TEMPLATE' THEN NULL
        WHEN 'PAID_EVENT_TEMPLATE' THEN NULL
        WHEN 'GIFT_EXCHANGE' THEN NULL
        ELSE fixed_points
    END,
    points_per_unit = CASE rule_code
        WHEN 'BORROW_BOOK' THEN 10
        WHEN 'PURCHASE_BOOK' THEN 1
        WHEN 'PURCHASE_SELF_GOODS' THEN 1
        WHEN 'PURCHASE_STORED_VALUE' THEN 1
        ELSE points_per_unit
    END,
    manual_points_editable = CASE
        WHEN rule_code IN ('PURCHASE_BOOK', 'PURCHASE_SELF_GOODS', 'PURCHASE_STORED_VALUE') THEN 1
        ELSE manual_points_editable
    END,
    member_day_enabled = CASE
        WHEN rule_code IN ('PURCHASE_BOOK', 'PURCHASE_SELF_GOODS') THEN 1
        WHEN rule_code = 'PURCHASE_STORED_VALUE' THEN 0
        ELSE member_day_enabled
    END,
    member_day_days = CASE
        WHEN rule_code IN ('PURCHASE_BOOK', 'PURCHASE_SELF_GOODS') THEN JSON_ARRAY(6, 16, 26)
        WHEN rule_code = 'PURCHASE_STORED_VALUE' THEN NULL
        ELSE member_day_days
    END,
    member_day_multiplier = CASE
        WHEN rule_code IN ('PURCHASE_BOOK', 'PURCHASE_SELF_GOODS') THEN 2.000
        WHEN rule_code = 'PURCHASE_STORED_VALUE' THEN NULL
        ELSE member_day_multiplier
    END,
    freeze_days = CASE
        WHEN rule_code IN ('BUY_PREMIUM_CARD', 'BUY_ENJOY_CARD',
                           'RENEW_PREMIUM_CARD', 'RENEW_ENJOY_CARD') THEN 7
        ELSE 0
    END,
    status = CASE
        WHEN rule_code IN ('OFFLINE_EVENT_TEMPLATE', 'PAID_EVENT_TEMPLATE', 'GIFT_EXCHANGE') THEN 'DRAFT'
        ELSE status
    END,
    updated_at = NOW()
WHERE is_deleted = 0;
