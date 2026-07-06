-- 会员手机号唯一索引
-- 执行前请先处理重复手机号数据，否则唯一索引会创建失败。

ALTER TABLE member DROP INDEX idx_phone;
ALTER TABLE member ADD UNIQUE KEY uk_member_phone (phone);
