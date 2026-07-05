-- 会员导入明细失败原因可能包含卡类型未匹配、手机号冲突、底层数据库异常等较长文本。
-- 使用 TEXT 避免记录失败原因时再次触发 Data too long。
ALTER TABLE member_import_detail
    MODIFY COLUMN error_msg TEXT DEFAULT NULL COMMENT '错误信息/提醒信息';
