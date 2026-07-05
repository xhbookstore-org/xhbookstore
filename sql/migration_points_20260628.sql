-- ============================================================
-- 积分系统迁移脚本
-- 1. 为 member/member_ext/member_import_detail/member_import_log/member_level 表增加注释
-- 2. 删除 points_history 表
-- 3. 新增积分相关表
-- ============================================================

-- ----------------------------
-- 1.1 member 表增加注释
-- ----------------------------
ALTER TABLE member MODIFY COLUMN id INT NOT NULL AUTO_INCREMENT COMMENT '主键';
ALTER TABLE member MODIFY COLUMN id INT NOT NULL AUTO_INCREMENT COMMENT '主键';
ALTER TABLE member MODIFY COLUMN card_no VARCHAR(50) NOT NULL COMMENT '会员卡号';
ALTER TABLE member MODIFY COLUMN name VARCHAR(50) DEFAULT NULL COMMENT '会员姓名';
ALTER TABLE member MODIFY COLUMN phone VARCHAR(20) DEFAULT NULL COMMENT '手机号';
ALTER TABLE member MODIFY COLUMN card_type_id INT DEFAULT NULL COMMENT '卡类型ID，关联card_type表';
ALTER TABLE member MODIFY COLUMN level_id INT DEFAULT NULL COMMENT '会员等级ID，关联member_level表';
ALTER TABLE member MODIFY COLUMN dept_id BIGINT(20) DEFAULT NULL COMMENT '所属部门ID，关联sys_dept表';
ALTER TABLE member MODIFY COLUMN valid_date DATE DEFAULT NULL COMMENT '会员有效期';
ALTER TABLE member MODIFY COLUMN status TINYINT DEFAULT 0 COMMENT '状态：0-正常，1-删除';
ALTER TABLE member MODIFY COLUMN borrow_count_valid INT DEFAULT 0 COMMENT '可借阅数量';
ALTER TABLE member MODIFY COLUMN current_points INT DEFAULT 0 COMMENT '当前积分/书城币余额';
ALTER TABLE member MODIFY COLUMN remark TEXT DEFAULT NULL COMMENT '备注';
ALTER TABLE member MODIFY COLUMN last_operator VARCHAR(50) DEFAULT NULL COMMENT '最后操作人';
ALTER TABLE member MODIFY COLUMN source VARCHAR(20) DEFAULT 'manual' COMMENT '来源：manual-手动录入，import-批量导入';
ALTER TABLE member MODIFY COLUMN sync_erp TINYINT(1) DEFAULT 0 COMMENT '是否同步ERP：0-未同步，1-已同步';
ALTER TABLE member MODIFY COLUMN created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
ALTER TABLE member MODIFY COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间';
ALTER TABLE member COMMENT = '会员主表';

-- ----------------------------
-- 1.2 member_ext 表增加注释
-- ----------------------------
ALTER TABLE member_ext MODIFY COLUMN id INT NOT NULL AUTO_INCREMENT COMMENT '主键';
ALTER TABLE member_ext MODIFY COLUMN member_id INT NOT NULL COMMENT '会员ID，关联member表';
ALTER TABLE member_ext MODIFY COLUMN gender ENUM('男','女') DEFAULT NULL COMMENT '性别';
ALTER TABLE member_ext MODIFY COLUMN age INT DEFAULT NULL COMMENT '年龄';
ALTER TABLE member_ext MODIFY COLUMN unit_phone VARCHAR(20) DEFAULT NULL COMMENT '单位电话';
ALTER TABLE member_ext MODIFY COLUMN wechat VARCHAR(50) DEFAULT NULL COMMENT '微信号';
ALTER TABLE member_ext MODIFY COLUMN weibo VARCHAR(50) DEFAULT NULL COMMENT '微博号';
ALTER TABLE member_ext MODIFY COLUMN join_date DATE DEFAULT NULL COMMENT '入会日期';
ALTER TABLE member_ext MODIFY COLUMN total_points INT DEFAULT 0 COMMENT '累计积分';
ALTER TABLE member_ext MODIFY COLUMN level_points INT DEFAULT 0 COMMENT '等级积分';
ALTER TABLE member_ext MODIFY COLUMN discount DECIMAL(5,2) DEFAULT NULL COMMENT '折扣率';
ALTER TABLE member_ext MODIFY COLUMN total_purchase_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '累计消费金额';
ALTER TABLE member_ext MODIFY COLUMN total_purchase_count INT DEFAULT 0 COMMENT '累计消费数量';
ALTER TABLE member_ext MODIFY COLUMN total_purchase_times INT DEFAULT 0 COMMENT '累计消费次数';
ALTER TABLE member_ext MODIFY COLUMN superior_name VARCHAR(50) DEFAULT NULL COMMENT '上级推荐人姓名';
ALTER TABLE member_ext MODIFY COLUMN superior_points_ratio DECIMAL(5,2) DEFAULT NULL COMMENT '上级积分比例';
ALTER TABLE member_ext MODIFY COLUMN business_staff_name VARCHAR(50) DEFAULT NULL COMMENT '业务员姓名';
ALTER TABLE member_ext MODIFY COLUMN excel_raw_data LONGTEXT DEFAULT NULL COMMENT '导入时的原始Excel数据';
ALTER TABLE member_ext MODIFY COLUMN created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
ALTER TABLE member_ext MODIFY COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间';
ALTER TABLE member_ext COMMENT = '会员扩展信息表';

-- ----------------------------
-- 1.3 member_level 表增加注释
-- ----------------------------
ALTER TABLE member_level MODIFY COLUMN id INT NOT NULL AUTO_INCREMENT COMMENT '主键';
ALTER TABLE member_level MODIFY COLUMN level_name VARCHAR(50) NOT NULL COMMENT '等级名称';
ALTER TABLE member_level MODIFY COLUMN discount DECIMAL(5,2) DEFAULT 1.00 COMMENT '折扣率';
ALTER TABLE member_level MODIFY COLUMN status TINYINT DEFAULT 0 COMMENT '状态：0-正常，1-停用';
ALTER TABLE member_level COMMENT = '会员等级表';

-- ----------------------------
-- 1.4 member_import_log 表增加注释
-- ----------------------------
ALTER TABLE member_import_log MODIFY COLUMN id INT NOT NULL AUTO_INCREMENT COMMENT '主键';
ALTER TABLE member_import_log MODIFY COLUMN operator VARCHAR(50) NOT NULL COMMENT '操作人';
ALTER TABLE member_import_log MODIFY COLUMN import_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '导入时间';
ALTER TABLE member_import_log MODIFY COLUMN file_name VARCHAR(255) DEFAULT NULL COMMENT '导入文件名';
ALTER TABLE member_import_log MODIFY COLUMN total_records INT DEFAULT 0 COMMENT '总记录数';
ALTER TABLE member_import_log MODIFY COLUMN success_records INT DEFAULT 0 COMMENT '成功记录数';
ALTER TABLE member_import_log MODIFY COLUMN fail_records INT DEFAULT 0 COMMENT '失败记录数';
ALTER TABLE member_import_log MODIFY COLUMN original_content LONGTEXT DEFAULT NULL COMMENT '原始文件内容';
ALTER TABLE member_import_log MODIFY COLUMN error_log TEXT DEFAULT NULL COMMENT '错误日志';
ALTER TABLE member_import_log MODIFY COLUMN remark VARCHAR(255) DEFAULT NULL COMMENT '备注';
ALTER TABLE member_import_log MODIFY COLUMN created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
ALTER TABLE member_import_log COMMENT = '会员导入日志表';

-- ----------------------------
-- 1.5 member_import_detail 表增加注释
-- ----------------------------
ALTER TABLE member_import_detail MODIFY COLUMN id INT NOT NULL AUTO_INCREMENT COMMENT '主键';
ALTER TABLE member_import_detail MODIFY COLUMN log_id INT NOT NULL COMMENT '导入日志ID，关联member_import_log表';
ALTER TABLE member_import_detail MODIFY COLUMN row_index INT DEFAULT NULL COMMENT 'Excel行号';
ALTER TABLE member_import_detail MODIFY COLUMN card_no VARCHAR(50) DEFAULT NULL COMMENT '会员卡号';
ALTER TABLE member_import_detail MODIFY COLUMN name VARCHAR(50) DEFAULT NULL COMMENT '会员姓名';
ALTER TABLE member_import_detail MODIFY COLUMN phone VARCHAR(20) DEFAULT NULL COMMENT '手机号';
ALTER TABLE member_import_detail MODIFY COLUMN card_type_name VARCHAR(50) DEFAULT NULL COMMENT '卡类型名称';
ALTER TABLE member_import_detail MODIFY COLUMN level_name VARCHAR(50) DEFAULT NULL COMMENT '等级名称';
ALTER TABLE member_import_detail MODIFY COLUMN valid_date DATE DEFAULT NULL COMMENT '有效期';
ALTER TABLE member_import_detail MODIFY COLUMN update_time DATETIME DEFAULT NULL COMMENT '更新时间';
ALTER TABLE member_import_detail MODIFY COLUMN import_status TINYINT DEFAULT 0 COMMENT '导入状态：0-待导入，1-导入成功，2-导入失败';
ALTER TABLE member_import_detail MODIFY COLUMN member_id INT DEFAULT NULL COMMENT '导入成功后关联的会员ID';
ALTER TABLE member_import_detail MODIFY COLUMN error_msg TEXT DEFAULT NULL COMMENT '错误信息/提醒信息';
ALTER TABLE member_import_detail MODIFY COLUMN created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
ALTER TABLE member_import_detail COMMENT = '会员导入明细表';

-- ----------------------------
-- 2. 删除 points_history 表
-- ----------------------------
DROP TABLE IF EXISTS points_history;

-- ----------------------------
-- 3.1 书城币订单表
-- ----------------------------
CREATE TABLE `xhbs_points_order` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_number` varchar(50) NOT NULL COMMENT '订单号:系统生成，发钱用IN开头，核销用OT开头，过期用EX开头, 退回用BK开头',
  `open_id` varchar(50) NOT NULL COMMENT '用户Open_id',
  `card_no` varchar(50) NOT NULL COMMENT '会员码',
  `member_id` int(11) NOT NULL COMMENT '用户Id-member表的id',
  `app_order_number` varchar(50) NOT NULL COMMENT '外部平台订单号--预留字段',
  `amount` int(11) NOT NULL DEFAULT '0' COMMENT '订单金额',
  `description` varchar(255) NOT NULL DEFAULT '' COMMENT '订单描述',
  `type` int(11) NOT NULL DEFAULT '1' COMMENT '订单类型：1.虚拟,2.实体--预留字段',
  `custom_args` varchar(500) NOT NULL DEFAULT '' COMMENT '自定义参数:用于溯源，闭环',
  `completed_time` datetime NOT NULL DEFAULT '1970-01-01 00:00:00' COMMENT '订单完成时间',
  `order_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '订单下单时间',
  `client_ip` varchar(100) DEFAULT '0.0.0.0' COMMENT '用户ip',
  `item_id` varchar(50) NOT NULL DEFAULT '' COMMENT '商品唯一标识--预留字段',
  `item_name` varchar(50) NOT NULL DEFAULT '' COMMENT '商品名称--预留字段',
  `price` int(11) NOT NULL DEFAULT '0' COMMENT '原价',
  `discounted_price` int(11) DEFAULT '0' NOT NULL COMMENT '折扣价',
  `operation_device` varchar(20) DEFAULT NULL COMMENT '操作设备（PC/小程序/POS）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_del` int(2) NOT NULL DEFAULT '0' COMMENT '删除标识,0:正常,1:删除',
  `amount_type` int(11) NOT NULL DEFAULT '1' COMMENT '订单付款币种类型:1.人民币2.书城币--预留字段',
  `app_id` varchar(50) NOT NULL DEFAULT '' COMMENT '应用id--预留字段',
  `orgin_points` int(11) NOT NULL DEFAULT '0' COMMENT '操作前用户积分',
  `after_points` int(11) NOT NULL DEFAULT '0' COMMENT '操作后用户积分',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_order_number` (`order_number`) USING BTREE COMMENT '订单号唯一索引',
  KEY `idx_app_id_member_id_type` (`app_id`,`member_id`,`type`) USING BTREE COMMENT '应用id用户id订单类型索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=COMPRESSED KEY_BLOCK_SIZE=8 COMMENT='书城币订单表';

-- ----------------------------
-- 3.2 书城币入账待核销流水明细表
-- ----------------------------
CREATE TABLE `xhbs_points_user_into_bill_detail` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `member_id` int(11) NOT NULL DEFAULT '0' COMMENT '用户member表的id',
  `points` int(10) NOT NULL DEFAULT '0' COMMENT '书城币数量',
  `remaining_points` int(11) NOT NULL DEFAULT '0' COMMENT '书城币余额数量，出账扣减后的数量',
  `description` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '获取书城币的描述信息',
  `order_no_src` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT '' COMMENT '我方订单号——发放',
  `order_no_target` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT '' COMMENT '对方订单号——发放',
  `activity_key` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT '' COMMENT '活动Key--预留',
  `activity_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT '' COMMENT '活动名称--预留',
  `event_key` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT '' COMMENT '事件key--预留',
  `event_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT '' COMMENT '事件名称--预留',
  `account_type` tinyint(4) NOT NULL DEFAULT '0' COMMENT '账户类型，0：常规账户，1：创作者激励账户',
  `bill_status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '入账单状态，0：初始化，1：核销中，2：完全核销完毕',
  `is_white_order` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否是白名单账单 0:否，1:是 --预留账户',
  `expired_time` datetime DEFAULT '0000-00-00 00:00:00' COMMENT '白名单过期时间',
  `expired_timestamp` bigint(20) DEFAULT '0' COMMENT '白名单过期时间戳，13位时间戳',
  `is_del` tinyint(4) unsigned NOT NULL DEFAULT '0' COMMENT '是否删除；0-未删除，1-已删除，默认0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`,`created_at`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_member_id_bill_status_account_type_created_at` (`member_id`,`bill_status`,`account_type`,`created_at`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='书城币入账待核销流水明细,用户扣减核销追溯使用';

-- ----------------------------
-- 3.3 书城币出账待核销流水明细表
-- ----------------------------
CREATE TABLE `xhbs_points_user_out_bill_detail` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `member_id` int(11) NOT NULL DEFAULT '0' COMMENT '用户member表的id',
  `points` int(10) NOT NULL DEFAULT '0' COMMENT '书城币数量',
  `remaining_points` int(11) NOT NULL DEFAULT '0' COMMENT '账单书城币余额数量，核销入账单后剩余的数量',
  `description` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL COMMENT '获取书城币的描述信息',
  `channel` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT '' COMMENT '渠道号',
  `order_no_src` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT '' COMMENT '订单号——扣减',
  `activity_key` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT '' COMMENT '活动Key--预留',
  `activity_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT '' COMMENT '活动名称--预留',
  `event_key` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT '' COMMENT '事件key--预留',
  `event_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT '' COMMENT '事件名称--预留',
  `account_type` tinyint(4) NOT NULL DEFAULT '0' COMMENT '账户类型，0：常规账户，1：创作者激励账户--预留',
  `bill_status` tinyint(4) NOT NULL DEFAULT '0' COMMENT '出账单状态，0：初始化，1：核销中，2：完全核销完毕',
  `is_del` tinyint(4) unsigned NOT NULL DEFAULT '0' COMMENT '是否删除；0-未删除，1-已删除，默认0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`,`created_at`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_member_id_bill_status_account_type_created_at` (`member_id`,`bill_status`,`account_type`,`created_at`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='书城币入账待核销流水明细,用户扣减核销追溯使用';

-- ----------------------------
-- 3.4 书城币核销明细表
-- ----------------------------
CREATE TABLE `xhbs_points_user_bill_detail` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `member_id` int(11) NOT NULL DEFAULT '0' COMMENT '用户member表的id',
  `into_bill_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '入账单id',
  `out_bill_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '出账单id',
  `points` int(10) NOT NULL DEFAULT '0' COMMENT '核销书城币数量',
  `account_type` tinyint(4) NOT NULL DEFAULT '0' COMMENT '入账单账户类型，0：常规账户，1：创作者激励账户 --预留字段',
  `is_del` tinyint(4) unsigned NOT NULL DEFAULT '0' COMMENT '是否删除；0-未删除，1-已删除，默认0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`,`created_at`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_member_id_created_at` (`member_id`,`created_at`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='书城币入账待核销流水明细,用户扣减核销追溯使用';
