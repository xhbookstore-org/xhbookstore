-- 清理 ERP 会员导入测试/脏数据
-- 适用场景：
-- 1. 清理 member_import_log / member_import_detail 导入记录
-- 2. 清理通过导入创建的 member(source='import') 与 member_ext
-- 3. 清理导入会员卡队列产生的 ERPIMPORT 售卡订单、会员卡、退款单、业务日志
--
-- 注意：
-- - member / member_ext / member_import_* 当前表结构没有 is_del 字段，只能物理删除。
-- - member_card* 业务表有 is_del 字段，采用逻辑删除。
-- - 执行前会把命中的数据备份到 cleanup_bak_*_20260704 表。

SET @cleanup_tag := '20260704_member_import_cleanup';

-- =========================
-- 1. 执行前统计
-- =========================
SELECT 'member.source=import' AS item, COUNT(*) AS cnt FROM member WHERE source = 'import'
UNION ALL
SELECT 'member_ext of import member', COUNT(*)
FROM member_ext e
JOIN member m ON m.id = e.member_id
WHERE m.source = 'import'
UNION ALL
SELECT 'member_card ERPIMPORT active rows', COUNT(*)
FROM member_card
WHERE sale_order_no LIKE 'ERPIMPORT%' AND is_del = 0
UNION ALL
SELECT 'member_card_order ERPIMPORT active rows', COUNT(*)
FROM member_card_order
WHERE order_no LIKE 'ERPIMPORT%' AND is_del = 0
UNION ALL
SELECT 'member_card_refund_order ERPIMPORT active rows', COUNT(*)
FROM member_card_refund_order
WHERE sale_order_no LIKE 'ERPIMPORT%' AND is_del = 0
UNION ALL
SELECT 'member_card_biz_log ERPIMPORT active rows', COUNT(*)
FROM member_card_biz_log
WHERE sale_order_no LIKE 'ERPIMPORT%' AND is_del = 0
UNION ALL
SELECT 'member_import_log rows', COUNT(*) FROM member_import_log
UNION ALL
SELECT 'member_import_detail rows', COUNT(*) FROM member_import_detail;

-- =========================
-- 2. 备份命中数据
-- =========================
CREATE TABLE IF NOT EXISTS cleanup_bak_member_import_20260704 AS
SELECT m.*, @cleanup_tag AS cleanup_tag, NOW() AS backup_at
FROM member m
WHERE 1 = 0;

INSERT INTO cleanup_bak_member_import_20260704
SELECT m.*, @cleanup_tag AS cleanup_tag, NOW() AS backup_at
FROM member m
WHERE m.source = 'import';

CREATE TABLE IF NOT EXISTS cleanup_bak_member_ext_import_20260704 AS
SELECT e.*, @cleanup_tag AS cleanup_tag, NOW() AS backup_at
FROM member_ext e
WHERE 1 = 0;

INSERT INTO cleanup_bak_member_ext_import_20260704
SELECT e.*, @cleanup_tag AS cleanup_tag, NOW() AS backup_at
FROM member_ext e
JOIN member m ON m.id = e.member_id
WHERE m.source = 'import';

CREATE TABLE IF NOT EXISTS cleanup_bak_member_card_import_20260704 AS
SELECT c.*, @cleanup_tag AS cleanup_tag, NOW() AS backup_at
FROM member_card c
WHERE 1 = 0;

INSERT INTO cleanup_bak_member_card_import_20260704
SELECT c.*, @cleanup_tag AS cleanup_tag, NOW() AS backup_at
FROM member_card c
WHERE c.sale_order_no LIKE 'ERPIMPORT%' AND c.is_del = 0;

CREATE TABLE IF NOT EXISTS cleanup_bak_member_card_order_import_20260704 AS
SELECT o.*, @cleanup_tag AS cleanup_tag, NOW() AS backup_at
FROM member_card_order o
WHERE 1 = 0;

INSERT INTO cleanup_bak_member_card_order_import_20260704
SELECT o.*, @cleanup_tag AS cleanup_tag, NOW() AS backup_at
FROM member_card_order o
WHERE o.order_no LIKE 'ERPIMPORT%' AND o.is_del = 0;

CREATE TABLE IF NOT EXISTS cleanup_bak_member_card_refund_order_import_20260704 AS
SELECT r.*, @cleanup_tag AS cleanup_tag, NOW() AS backup_at
FROM member_card_refund_order r
WHERE 1 = 0;

INSERT INTO cleanup_bak_member_card_refund_order_import_20260704
SELECT r.*, @cleanup_tag AS cleanup_tag, NOW() AS backup_at
FROM member_card_refund_order r
WHERE r.sale_order_no LIKE 'ERPIMPORT%' AND r.is_del = 0;

CREATE TABLE IF NOT EXISTS cleanup_bak_member_card_biz_log_import_20260704 AS
SELECT l.*, @cleanup_tag AS cleanup_tag, NOW() AS backup_at
FROM member_card_biz_log l
WHERE 1 = 0;

INSERT INTO cleanup_bak_member_card_biz_log_import_20260704
SELECT l.*, @cleanup_tag AS cleanup_tag, NOW() AS backup_at
FROM member_card_biz_log l
WHERE l.sale_order_no LIKE 'ERPIMPORT%' AND l.is_del = 0;

CREATE TABLE IF NOT EXISTS cleanup_bak_member_import_log_20260704 AS
SELECT l.*, @cleanup_tag AS cleanup_tag, NOW() AS backup_at
FROM member_import_log l
WHERE 1 = 0;

INSERT INTO cleanup_bak_member_import_log_20260704
SELECT l.*, @cleanup_tag AS cleanup_tag, NOW() AS backup_at
FROM member_import_log l;

CREATE TABLE IF NOT EXISTS cleanup_bak_member_import_detail_20260704 AS
SELECT d.*, @cleanup_tag AS cleanup_tag, NOW() AS backup_at
FROM member_import_detail d
WHERE 1 = 0;

INSERT INTO cleanup_bak_member_import_detail_20260704
SELECT d.*, @cleanup_tag AS cleanup_tag, NOW() AS backup_at
FROM member_import_detail d;

-- =========================
-- 3. 清理数据
-- =========================
START TRANSACTION;

-- 会员卡相关表：有 is_del，走逻辑删除。
UPDATE member_card_biz_log
SET is_del = 1
WHERE sale_order_no LIKE 'ERPIMPORT%' AND is_del = 0;

UPDATE member_card_refund_order
SET is_del = 1, updated_at = NOW()
WHERE sale_order_no LIKE 'ERPIMPORT%' AND is_del = 0;

UPDATE member_card_order
SET is_del = 1, updated_at = NOW()
WHERE order_no LIKE 'ERPIMPORT%' AND is_del = 0;

UPDATE member_card
SET is_del = 1, updated_at = NOW()
WHERE sale_order_no LIKE 'ERPIMPORT%' AND is_del = 0;

-- 导入日志表：无 is_del，物理删除。
DELETE FROM member_import_detail;
DELETE FROM member_import_log;

-- 导入会员扩展与会员主表：无 is_del，按 source='import' 物理删除。
DELETE e
FROM member_ext e
JOIN member m ON m.id = e.member_id
WHERE m.source = 'import';

DELETE FROM member
WHERE source = 'import';

COMMIT;

-- =========================
-- 4. 执行后统计
-- =========================
SELECT 'member.source=import' AS item, COUNT(*) AS cnt FROM member WHERE source = 'import'
UNION ALL
SELECT 'member_ext of import member', COUNT(*)
FROM member_ext e
JOIN member m ON m.id = e.member_id
WHERE m.source = 'import'
UNION ALL
SELECT 'member_card ERPIMPORT active rows', COUNT(*)
FROM member_card
WHERE sale_order_no LIKE 'ERPIMPORT%' AND is_del = 0
UNION ALL
SELECT 'member_card_order ERPIMPORT active rows', COUNT(*)
FROM member_card_order
WHERE order_no LIKE 'ERPIMPORT%' AND is_del = 0
UNION ALL
SELECT 'member_card_refund_order ERPIMPORT active rows', COUNT(*)
FROM member_card_refund_order
WHERE sale_order_no LIKE 'ERPIMPORT%' AND is_del = 0
UNION ALL
SELECT 'member_card_biz_log ERPIMPORT active rows', COUNT(*)
FROM member_card_biz_log
WHERE sale_order_no LIKE 'ERPIMPORT%' AND is_del = 0
UNION ALL
SELECT 'member_import_log rows', COUNT(*) FROM member_import_log
UNION ALL
SELECT 'member_import_detail rows', COUNT(*) FROM member_import_detail;
