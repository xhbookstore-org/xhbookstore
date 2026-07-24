-- 修复借阅单号与积分业务单号比较时的排序规则冲突。
-- 执行前请备份 xhbs_points_order；本迁移可重复执行。
-- 两侧统一使用 utf8mb4_unicode_ci：
--   book_borrow_order.order_no         已为 utf8mb4_unicode_ci
--   xhbs_points_order.business_order_no 原为 utf8mb4_general_ci

ALTER TABLE xhbs_points_order
    MODIFY COLUMN business_order_no VARCHAR(64)
        CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci
        NOT NULL DEFAULT '';
