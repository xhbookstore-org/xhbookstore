-- Borrow V2: manual per-copy book entry, detail-bound images and rule-based points.
-- Run once after backing up the database. Deploy the matching application version immediately after this migration.

ALTER TABLE book_borrow_order
    ADD COLUMN member_card_type_name VARCHAR(100) NULL AFTER member_phone,
    ADD COLUMN member_valid_date DATE NULL AFTER member_card_type_name;
UPDATE book_borrow_order o
LEFT JOIN member m ON m.id = o.member_id
LEFT JOIN util_card_type ct ON ct.id = m.card_type_id AND ct.is_del = 0
SET o.member_card_type_name = ct.type_name,
    o.member_valid_date = m.valid_date
WHERE o.member_card_type_name IS NULL AND o.member_valid_date IS NULL;

ALTER TABLE book_borrow_detail
    ADD COLUMN book_code VARCHAR(64) NULL AFTER book_id;
UPDATE book_borrow_detail d
LEFT JOIN book_info b ON b.id = d.book_id
SET d.book_code = COALESCE(NULLIF(TRIM(b.isbn), ''), CONCAT('LEGACY-', d.id))
WHERE d.book_code IS NULL OR d.book_code = '';
ALTER TABLE book_borrow_detail
    MODIFY COLUMN book_id BIGINT(20) NULL COMMENT 'Legacy book_info id; new borrowing does not use it',
    MODIFY COLUMN book_code VARCHAR(64) NOT NULL COMMENT 'Employee-entered book/copy code';
CREATE INDEX idx_book_code ON book_borrow_detail(book_code);

ALTER TABLE book_return_detail
    ADD COLUMN book_code VARCHAR(64) NULL AFTER book_id;
UPDATE book_return_detail r
JOIN book_borrow_detail d ON d.id = r.borrow_detail_id
SET r.book_code = d.book_code
WHERE r.book_code IS NULL OR r.book_code = '';
ALTER TABLE book_return_detail
    MODIFY COLUMN book_id BIGINT(20) NULL COMMENT 'Legacy book_info id',
    MODIFY COLUMN book_code VARCHAR(64) NOT NULL COMMENT 'Book/copy code snapshot';

ALTER TABLE dd_book_purchase_order
    ADD COLUMN book_code VARCHAR(64) NULL AFTER book_id;
UPDATE dd_book_purchase_order p
JOIN book_borrow_detail d ON d.id = p.borrow_detail_id
SET p.book_code = d.book_code
WHERE p.borrow_detail_id IS NOT NULL AND (p.book_code IS NULL OR p.book_code = '');
UPDATE dd_book_purchase_order
SET book_code = CONCAT('LEGACY-', id)
WHERE book_code IS NULL OR book_code = '';
ALTER TABLE dd_book_purchase_order
    MODIFY COLUMN book_id BIGINT(20) NULL COMMENT 'Legacy book_info id',
    MODIFY COLUMN book_code VARCHAR(64) NOT NULL COMMENT 'Book/copy code snapshot';

ALTER TABLE book_borrow_detail_image
    ADD COLUMN member_id INT NULL AFTER id,
    ADD COLUMN bind_status VARCHAR(10) NOT NULL DEFAULT 'BOUND' AFTER image_status,
    MODIFY COLUMN borrow_detail_id BIGINT(20) NULL,
    MODIFY COLUMN borrow_order_id BIGINT(20) NULL,
    MODIFY COLUMN borrow_order_no VARCHAR(50) NULL;
UPDATE book_borrow_detail_image i
JOIN book_borrow_detail d ON d.id = i.borrow_detail_id
SET i.member_id = d.member_id, i.bind_status = 'BOUND';
ALTER TABLE book_borrow_detail_image
    MODIFY COLUMN member_id INT NOT NULL COMMENT 'Member owning the temporary or bound image',
    ADD UNIQUE KEY uk_borrow_image_id(image_id),
    ADD KEY idx_borrow_image_member_bind(member_id, bind_status, created_at);

-- New compact statuses: order 1=active, 2=partially settled, 3=completed;
-- detail 1=active, 2=returned, 5=purchased.
UPDATE book_borrow_order
SET borrow_status = CASE
    WHEN is_finished = 1 OR borrow_status IN (2, 5) THEN 3
    WHEN borrow_status IN (3, 4) THEN 2
    ELSE 1 END;
UPDATE book_borrow_detail
SET borrow_status = CASE
    WHEN returned_qty >= borrow_qty THEN 2
    WHEN purchase_qty >= borrow_qty THEN 5
    ELSE 1 END;

CREATE TABLE xhbs_points_rule (
    id BIGINT NOT NULL AUTO_INCREMENT,
    rule_code VARCHAR(64) NOT NULL,
    rule_name VARCHAR(100) NOT NULL,
    scene_code VARCHAR(50) NOT NULL,
    rule_source VARCHAR(20) NOT NULL DEFAULT 'SYSTEM',
    direction VARCHAR(10) NOT NULL,
    trigger_mode VARCHAR(10) NOT NULL,
    trigger_event VARCHAR(50) NOT NULL,
    calculation_mode VARCHAR(20) NOT NULL,
    fixed_points INT NULL,
    points_per_unit INT NULL,
    unit_type VARCHAR(20) NULL,
    manual_points_editable TINYINT NOT NULL DEFAULT 0,
    member_day_enabled TINYINT NOT NULL DEFAULT 0,
    member_day_days JSON NULL,
    member_day_multiplier DECIMAL(6,3) NULL,
    effective_from DATETIME NULL,
    effective_to DATETIME NULL,
    dept_ids JSON NULL,
    card_type_ids JSON NULL,
    member_limit INT NULL,
    total_limit INT NULL,
    used_count INT NOT NULL DEFAULT 0,
    budget_points BIGINT NULL,
    used_points BIGINT NOT NULL DEFAULT 0,
    max_points_per_order INT NULL,
    require_biz_order TINYINT NOT NULL DEFAULT 1,
    require_evidence TINYINT NOT NULL DEFAULT 0,
    exclude_bulk_purchase TINYINT NOT NULL DEFAULT 0,
    freeze_days INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    sort_order INT NOT NULL DEFAULT 0,
    remark VARCHAR(500) NULL,
    lock_version INT NOT NULL DEFAULT 0,
    operator_user_id BIGINT NULL,
    operator_name VARCHAR(64) NOT NULL,
    operation_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_points_rule_code(rule_code),
    KEY idx_points_rule_scene(scene_code, direction, status),
    KEY idx_points_rule_effective(status, effective_from, effective_to),
    KEY idx_points_rule_operator(operator_user_id, operation_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Points grant and deduction rules';

INSERT INTO xhbs_points_rule (
    rule_code, rule_name, scene_code, direction, trigger_mode, trigger_event,
    calculation_mode, points_per_unit, unit_type,
    status, operator_name, remark
) VALUES (
    'BORROW_BOOK', '借阅图书积分', 'BORROW_BOOK', 'ADD', 'AUTO', 'BORROW_COMPLETED',
    'PER_ITEM', 10, 'ITEM',
    'ENABLED', 'SYSTEM', '借阅成功后按逐册明细数发放，一册 10 分'
);

ALTER TABLE xhbs_points_order
    ADD COLUMN rule_id BIGINT NULL,
    ADD COLUMN rule_code VARCHAR(64) NOT NULL DEFAULT 'LEGACY',
    ADD COLUMN rule_name VARCHAR(100) NOT NULL DEFAULT '历史积分',
    ADD COLUMN scene_code VARCHAR(50) NOT NULL DEFAULT 'LEGACY',
    ADD COLUMN operation_kind VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    ADD COLUMN direction VARCHAR(10) NOT NULL DEFAULT 'ADD',
    ADD COLUMN trigger_mode VARCHAR(10) NOT NULL DEFAULT 'MANUAL',
    ADD COLUMN trigger_event VARCHAR(50) NULL,
    ADD COLUMN calculation_mode VARCHAR(20) NULL,
    ADD COLUMN base_amount DECIMAL(12,2) NULL,
    ADD COLUMN base_quantity DECIMAL(12,2) NULL,
    ADD COLUMN base_points INT NOT NULL DEFAULT 0,
    ADD COLUMN multiplier DECIMAL(6,3) NOT NULL DEFAULT 1.000,
    ADD COLUMN balance_bucket VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    ADD COLUMN before_frozen_points INT NOT NULL DEFAULT 0,
    ADD COLUMN after_frozen_points INT NOT NULL DEFAULT 0,
    ADD COLUMN availability_status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    ADD COLUMN available_at DATETIME NULL,
    ADD COLUMN unfrozen_at DATETIME NULL,
    ADD COLUMN business_type VARCHAR(50) NOT NULL DEFAULT 'LEGACY',
    ADD COLUMN business_order_no VARCHAR(64) NOT NULL DEFAULT '',
    ADD COLUMN business_key VARCHAR(180) NULL,
    ADD COLUMN original_order_no VARCHAR(50) NULL,
    ADD COLUMN operator_type VARCHAR(20) NOT NULL DEFAULT 'SYSTEM',
    ADD COLUMN operator_user_id BIGINT NULL,
    ADD COLUMN operator_name VARCHAR(64) NOT NULL DEFAULT 'SYSTEM',
    ADD COLUMN operation_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN order_status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    ADD COLUMN idempotency_key VARCHAR(100) NULL,
    ADD COLUMN evidence_urls JSON NULL,
    ADD COLUMN calculation_snapshot JSON NULL,
    ADD COLUMN error_code VARCHAR(40) NULL,
    ADD COLUMN error_message VARCHAR(500) NULL;
UPDATE xhbs_points_order
SET direction = CASE WHEN amount < 0 THEN 'DEDUCT' ELSE 'ADD' END,
    business_order_no = order_number,
    business_key = CONCAT('LEGACY:', id),
    idempotency_key = CONCAT('LEGACY:', id),
    base_points = ABS(amount);
ALTER TABLE xhbs_points_order
    MODIFY COLUMN business_key VARCHAR(180) NOT NULL,
    MODIFY COLUMN idempotency_key VARCHAR(100) NOT NULL,
    ADD UNIQUE KEY uk_points_order_business_key(business_key),
    ADD UNIQUE KEY uk_points_order_idempotency(idempotency_key),
    ADD KEY idx_points_order_rule(rule_id, order_status, operation_time),
    ADD KEY idx_points_order_member(member_id, order_status, operation_time),
    ADD KEY idx_points_order_operator(operator_user_id, operation_time),
    ADD KEY idx_points_order_business(business_type, business_order_no),
    ADD KEY idx_points_order_original(original_order_no),
    ADD KEY idx_points_order_availability(availability_status, available_at);
