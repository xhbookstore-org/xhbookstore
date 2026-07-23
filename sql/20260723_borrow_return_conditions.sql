-- 借阅 V3：图书编号可选、还书按单册四态处理，并记录员工填写的积分金额。
-- 执行前请备份相关表；本迁移需与对应应用版本同时部署。

ALTER TABLE book_borrow_detail
    MODIFY COLUMN book_code VARCHAR(64) NULL COMMENT '员工录入的图书编号；未录入时为空';

ALTER TABLE book_return_detail
    MODIFY COLUMN book_code VARCHAR(64) NULL COMMENT '图书编号快照；未录入时为空',
    ADD COLUMN return_condition VARCHAR(32) NOT NULL DEFAULT 'intact' COMMENT '还书状态：intact/slight_wear/damaged/purchase' AFTER return_type,
    ADD COLUMN points INT NULL COMMENT '破损或购买时员工录入的实付金额' AFTER return_condition;

-- 历史 return_type：1=正常、2=损坏、3=遗失；统一映射到新的四态。
UPDATE book_return_detail
SET return_condition = CASE return_type
    WHEN 1 THEN 'intact'
    WHEN 2 THEN 'damaged'
    WHEN 3 THEN 'damaged'
    WHEN 4 THEN 'purchase'
    ELSE 'intact'
END,
return_type = CASE return_type
    WHEN 1 THEN 1
    WHEN 2 THEN 3
    WHEN 3 THEN 3
    WHEN 4 THEN 4
    ELSE 1
END
WHERE return_condition IS NULL OR return_condition = 'intact';
