-- ============================================================
-- 新华书店 - 借书业务表结构设计
-- 表前缀: book_ / dd_
-- 订单号规则: DY=借书单, HS=还书单, JSDD=购书订单
-- ============================================================

-- ----------------------------
-- 1. 图书主表
-- ----------------------------
DROP TABLE IF EXISTS book_info;
CREATE TABLE book_info (
    `id`                BIGINT(20)      NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `book_name`         VARCHAR(200)    NOT NULL                 COMMENT '图书名称',
    `isbn`              VARCHAR(30)     DEFAULT ''               COMMENT 'ISBN编号',
    `author`            VARCHAR(100)    DEFAULT ''               COMMENT '作者',
    `publisher`         VARCHAR(200)    DEFAULT ''               COMMENT '出版社',
    `price`             DECIMAL(10,2)   NOT NULL DEFAULT 0.00    COMMENT '定价/原价',
    `sale_price`        DECIMAL(10,2)   NOT NULL DEFAULT 0.00    COMMENT '售价',
    `stock_qty`         INT             NOT NULL DEFAULT 0        COMMENT '库存数量',
    `lendable_qty`      INT             NOT NULL DEFAULT 0        COMMENT '可借数量',
    `book_status`       TINYINT         NOT NULL DEFAULT 1        COMMENT '状态：1-初始化，2-上架，3-下架',
    `cover_url`         VARCHAR(500)    DEFAULT ''               COMMENT '封面图URL',
    `description`       VARCHAR(1000)   DEFAULT ''               COMMENT '图书简介',
    `remark`            VARCHAR(500)    DEFAULT ''               COMMENT '备注',
    `create_staff_id`   VARCHAR(50)     DEFAULT ''               COMMENT '创建员工ID',
    `create_staff_name` VARCHAR(50)     DEFAULT ''               COMMENT '创建员工姓名',
    `update_staff_id`   VARCHAR(50)     DEFAULT ''               COMMENT '最后修改员工ID',
    `update_staff_name` VARCHAR(50)     DEFAULT ''               COMMENT '最后修改员工姓名',
    `created_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `is_del`            TINYINT         NOT NULL DEFAULT 0        COMMENT '删除标识：0-正常，1-删除',
    PRIMARY KEY (`id`),
    KEY `idx_book_name` (`book_name`(100)),
    KEY `idx_book_status` (`book_status`),
    KEY `idx_isbn` (`isbn`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书主表';


-- ----------------------------
-- 2. 图书图片表
-- ----------------------------
DROP TABLE IF EXISTS book_image;
CREATE TABLE book_image (
    `id`                BIGINT(20)      NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `book_id`           BIGINT(20)      NOT NULL                 COMMENT '图书ID，关联book_info.id',
    `image_id`          VARCHAR(64)     NOT NULL                 COMMENT '图片唯一标识(UUID)',
    `image_name`        VARCHAR(200)    DEFAULT ''               COMMENT '图片名称',
    `image_url`         VARCHAR(500)    NOT NULL                 COMMENT '图片URL',
    `thumb_url`         VARCHAR(500)    DEFAULT ''               COMMENT '缩略图URL',
    `sort_order`        INT             DEFAULT 0                COMMENT '排序号，越小越靠前',
    `image_type`        TINYINT         DEFAULT 1                COMMENT '图片类型：1-封面，2-内页，3-破损图，4-其他',
    `image_status`      TINYINT         NOT NULL DEFAULT 0        COMMENT '状态：0-正常，1-删除',
    `create_staff_id`   VARCHAR(50)     DEFAULT ''               COMMENT '创建员工ID',
    `create_staff_name` VARCHAR(50)     DEFAULT ''               COMMENT '创建员工姓名',
    `update_staff_id`   VARCHAR(50)     DEFAULT ''               COMMENT '最后修改员工ID',
    `update_staff_name` VARCHAR(50)     DEFAULT ''               COMMENT '最后修改员工姓名',
    `created_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    KEY `idx_book_id` (`book_id`),
    KEY `idx_image_id` (`image_id`),
    KEY `idx_image_status` (`image_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书图片表';


-- ----------------------------
-- 3. 借书单主表
-- ----------------------------
DROP TABLE IF EXISTS book_borrow_order;
CREATE TABLE book_borrow_order (
    `id`                    BIGINT(20)      NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `order_no`              VARCHAR(50)     NOT NULL                 COMMENT '借书单号：DY+yyyyMMddHHmmss+6位随机数，唯一',
    `member_id`             INT             NOT NULL                 COMMENT '会员ID，关联member.id',
    `member_card_no`        VARCHAR(50)     NOT NULL                 COMMENT '会员卡号(冗余，便于查询)',
    `member_name`           VARCHAR(50)     DEFAULT ''               COMMENT '会员姓名(冗余，便于查询)',
    `member_phone`          VARCHAR(20)     DEFAULT ''               COMMENT '会员手机号(冗余，便于查询)',
    `total_book_count`      INT             NOT NULL DEFAULT 0        COMMENT '借书总数量',
    `is_finished`           TINYINT         NOT NULL DEFAULT 0        COMMENT '是否完结：0-未完结，1-已完结',
    `borrow_status`         TINYINT         NOT NULL DEFAULT 1        COMMENT '借阅状态：1-已借阅，2-已全部归还，3-部分归还，4-部分借转购，5-全部借转购',
    `borrow_time`           DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '借书时间',
    `return_all_time`       DATETIME        DEFAULT NULL             COMMENT '全部还书时间',
    `expected_return_time`  DATETIME        DEFAULT NULL             COMMENT '预计还书时间',
    `remark`                VARCHAR(500)    DEFAULT ''               COMMENT '备注',
    `dept_id`               BIGINT(20)      DEFAULT NULL             COMMENT '操作门店ID',
    `first_staff_id`        VARCHAR(50)     DEFAULT ''               COMMENT '首次操作员工ID',
    `first_staff_name`      VARCHAR(50)     DEFAULT ''               COMMENT '首次操作员工姓名',
    `last_staff_id`         VARCHAR(50)     DEFAULT ''               COMMENT '最后操作员工ID',
    `last_staff_name`       VARCHAR(50)     DEFAULT ''               COMMENT '最后操作员工姓名',
    `created_at`            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `is_del`                TINYINT         NOT NULL DEFAULT 0        COMMENT '删除标识：0-正常，1-删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`) USING BTREE COMMENT '借书单号唯一索引',
    KEY `idx_member_id` (`member_id`),
    KEY `idx_member_card_no` (`member_card_no`),
    KEY `idx_borrow_status` (`borrow_status`),
    KEY `idx_borrow_time` (`borrow_time`),
    KEY `idx_dept_id` (`dept_id`),
    KEY `idx_first_staff_id` (`first_staff_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='借书单主表';


-- ----------------------------
-- 4. 借书单明细表
-- ----------------------------
DROP TABLE IF EXISTS book_borrow_detail;
CREATE TABLE book_borrow_detail (
    `id`                    BIGINT(20)      NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `borrow_order_id`       BIGINT(20)      NOT NULL                 COMMENT '借书单ID，关联book_borrow_order.id',
    `borrow_order_no`       VARCHAR(50)     NOT NULL                 COMMENT '借书单号(冗余，便于查询)',
    `member_id`             INT             NOT NULL                 COMMENT '会员ID，关联member.id',
    `book_id`               BIGINT(20)      NOT NULL                 COMMENT '图书ID，关联book_info.id',
    `book_name`             VARCHAR(200)    DEFAULT ''               COMMENT '图书名称(冗余)',
    `borrow_qty`            INT             NOT NULL DEFAULT 1        COMMENT '借书数量',
    `returned_qty`          INT             NOT NULL DEFAULT 0        COMMENT '已还书数量',
    `purchase_qty`          INT             NOT NULL DEFAULT 0        COMMENT '借转购数量',
    `borrow_status`         TINYINT         NOT NULL DEFAULT 1        COMMENT '借阅状态：1-已借阅，2-已全部归还，3-部分归还，4-部分借转购，5-全部借转购',
    `borrow_time`           DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '借书时间',
    `return_all_time`       DATETIME        DEFAULT NULL             COMMENT '全部还书时间',
    `purchase_order_no`     VARCHAR(50)     DEFAULT ''               COMMENT '借转购订单号(JSDD开头)，关联dd_book_purchase_order.order_no',
    `remark`                VARCHAR(500)    DEFAULT ''               COMMENT '备注',
    `first_staff_id`        VARCHAR(50)     DEFAULT ''               COMMENT '首次操作员工ID',
    `first_staff_name`      VARCHAR(50)     DEFAULT ''               COMMENT '首次操作员工姓名',
    `last_staff_id`         VARCHAR(50)     DEFAULT ''               COMMENT '最后操作员工ID',
    `last_staff_name`       VARCHAR(50)     DEFAULT ''               COMMENT '最后操作员工姓名',
    `created_at`            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `is_del`                TINYINT         NOT NULL DEFAULT 0        COMMENT '删除标识：0-正常，1-删除',
    PRIMARY KEY (`id`),
    KEY `idx_borrow_order_id` (`borrow_order_id`),
    KEY `idx_borrow_order_no` (`borrow_order_no`),
    KEY `idx_member_id` (`member_id`),
    KEY `idx_book_id` (`book_id`),
    KEY `idx_borrow_status` (`borrow_status`),
    KEY `idx_purchase_order_no` (`purchase_order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='借书单明细表';


-- ----------------------------
-- 5. 借书明细图片表（借书时拍摄的图书照片）
-- ----------------------------
DROP TABLE IF EXISTS book_borrow_detail_image;
CREATE TABLE book_borrow_detail_image (
    `id`                    BIGINT(20)      NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `borrow_detail_id`      BIGINT(20)      NOT NULL                 COMMENT '借书明细ID，关联book_borrow_detail.id',
    `borrow_order_id`       BIGINT(20)      NOT NULL                 COMMENT '借书单ID，关联book_borrow_order.id',
    `borrow_order_no`       VARCHAR(50)     NOT NULL                 COMMENT '借书单号(冗余)',
    `image_id`              VARCHAR(64)     NOT NULL                 COMMENT '图片唯一标识(UUID)',
    `image_name`            VARCHAR(200)    DEFAULT ''               COMMENT '图片名称',
    `image_url`             VARCHAR(500)    NOT NULL                 COMMENT '图片URL',
    `thumb_url`             VARCHAR(500)    DEFAULT ''               COMMENT '缩略图URL',
    `sort_order`            INT             DEFAULT 0                COMMENT '排序号',
    `image_type`            TINYINT         DEFAULT 1                COMMENT '图片类型：1-借书时拍摄，2-还书时拍摄，3-损坏记录',
    `image_status`          TINYINT         NOT NULL DEFAULT 0        COMMENT '状态：0-正常，1-删除',
    `create_staff_id`       VARCHAR(50)     DEFAULT ''               COMMENT '创建员工ID',
    `create_staff_name`     VARCHAR(50)     DEFAULT ''               COMMENT '创建员工姓名',
    `created_at`            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    KEY `idx_borrow_detail_id` (`borrow_detail_id`),
    KEY `idx_borrow_order_id` (`borrow_order_id`),
    KEY `idx_borrow_order_no` (`borrow_order_no`),
    KEY `idx_image_id` (`image_id`),
    KEY `idx_image_status` (`image_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='借书明细图片表（借/还书时拍摄的图书照片）';


-- ----------------------------
-- 6. 还书明细表
-- ----------------------------
DROP TABLE IF EXISTS book_return_detail;
CREATE TABLE book_return_detail (
    `id`                    BIGINT(20)      NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `return_order_no`       VARCHAR(50)     NOT NULL                 COMMENT '还书单号：HS+yyyyMMddHHmmss+6位随机数，唯一',
    `borrow_order_id`       BIGINT(20)      NOT NULL                 COMMENT '借书单ID，关联book_borrow_order.id',
    `borrow_order_no`       VARCHAR(50)     NOT NULL                 COMMENT '借书单号(冗余)',
    `borrow_detail_id`      BIGINT(20)      NOT NULL                 COMMENT '借书明细ID，关联book_borrow_detail.id',
    `member_id`             INT             NOT NULL                 COMMENT '会员ID，关联member.id',
    `book_id`               BIGINT(20)      NOT NULL                 COMMENT '图书ID，关联book_info.id',
    `book_name`             VARCHAR(200)    DEFAULT ''               COMMENT '图书名称(冗余)',
    `return_qty`            INT             NOT NULL DEFAULT 1        COMMENT '本次还书数量',
    `return_time`           DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '还书时间',
    `return_type`           TINYINT         NOT NULL DEFAULT 1        COMMENT '还书类型：1-正常还书，2-损坏还书，3-遗失赔偿',
    `remark`                VARCHAR(500)    DEFAULT ''               COMMENT '备注（损坏/遗失原因等）',
    `dept_id`               BIGINT(20)      DEFAULT NULL             COMMENT '操作门店ID',
    `staff_id`              VARCHAR(50)     DEFAULT ''               COMMENT '操作员工ID',
    `staff_name`            VARCHAR(50)     DEFAULT ''               COMMENT '操作员工姓名',
    `created_at`            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `is_del`                TINYINT         NOT NULL DEFAULT 0        COMMENT '删除标识：0-正常，1-删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_return_order_no` (`return_order_no`) USING BTREE COMMENT '还书单号唯一索引',
    KEY `idx_borrow_order_id` (`borrow_order_id`),
    KEY `idx_borrow_order_no` (`borrow_order_no`),
    KEY `idx_borrow_detail_id` (`borrow_detail_id`),
    KEY `idx_member_id` (`member_id`),
    KEY `idx_book_id` (`book_id`),
    KEY `idx_return_time` (`return_time`),
    KEY `idx_staff_id` (`staff_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='还书明细表';


-- ----------------------------
-- 7. 购书订单表（dd_前缀，JSDD订单号开头）
-- ----------------------------
DROP TABLE IF EXISTS dd_book_purchase_order;
CREATE TABLE dd_book_purchase_order (
    `id`                    BIGINT(20)      NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `order_no`              VARCHAR(50)     NOT NULL                 COMMENT '购书订单号：JSDD+yyyyMMddHHmmss+6位随机数，唯一',
    `borrow_order_id`       BIGINT(20)      DEFAULT NULL             COMMENT '关联借书单ID（借转购时关联），关联book_borrow_order.id',
    `borrow_order_no`       VARCHAR(50)     DEFAULT ''               COMMENT '关联借书单号(冗余)',
    `borrow_detail_id`      BIGINT(20)      DEFAULT NULL             COMMENT '关联借书明细ID（借转购时关联），关联book_borrow_detail.id',
    `member_id`             INT             NOT NULL                 COMMENT '会员ID，关联member.id',
    `member_card_no`        VARCHAR(50)     NOT NULL                 COMMENT '会员卡号(冗余)',
    `member_name`           VARCHAR(50)     DEFAULT ''               COMMENT '会员姓名(冗余)',
    `order_type`            TINYINT         NOT NULL DEFAULT 1        COMMENT '订单类型：1-购书，2-借转购',
    `payment_type`          TINYINT         NOT NULL DEFAULT 1        COMMENT '付款类型：1-现金，2-微信，3-支付宝，4-积分，5-混合支付',
    `book_id`               BIGINT(20)      NOT NULL                 COMMENT '图书ID，关联book_info.id',
    `book_name`             VARCHAR(200)    DEFAULT ''               COMMENT '图书名称(冗余)',
    `qty`                   INT             NOT NULL DEFAULT 1        COMMENT '购买数量',
    `unit_price`            DECIMAL(10,2)   NOT NULL DEFAULT 0.00    COMMENT '单价',
    `discount_price`        DECIMAL(10,2)   NOT NULL DEFAULT 0.00    COMMENT '折扣金额',
    `receivable_amount`     DECIMAL(10,2)   NOT NULL DEFAULT 0.00    COMMENT '应收金额 = qty * unit_price - discount_price',
    `paid_amount`           DECIMAL(10,2)   NOT NULL DEFAULT 0.00    COMMENT '实付金额',
    `points_deduct`         INT             NOT NULL DEFAULT 0        COMMENT '积分抵扣金额（分）',
    `order_status`          TINYINT         NOT NULL DEFAULT 0        COMMENT '状态：0-待付款，1-已付款，2-已取消，3-已退款',
    `pay_time`              DATETIME        DEFAULT NULL             COMMENT '付款时间',
    `cancel_time`           DATETIME        DEFAULT NULL             COMMENT '取消时间',
    `remark`                VARCHAR(500)    DEFAULT ''               COMMENT '备注',
    `dept_id`               BIGINT(20)      DEFAULT NULL             COMMENT '操作门店ID',
    `create_staff_id`       VARCHAR(50)     DEFAULT ''               COMMENT '创建员工ID',
    `create_staff_name`     VARCHAR(50)     DEFAULT ''               COMMENT '创建员工姓名',
    `update_staff_id`       VARCHAR(50)     DEFAULT ''               COMMENT '最后修改员工ID',
    `update_staff_name`     VARCHAR(50)     DEFAULT ''               COMMENT '最后修改员工姓名',
    `created_at`            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`            DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `is_del`                TINYINT         NOT NULL DEFAULT 0        COMMENT '删除标识：0-正常，1-删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`) USING BTREE COMMENT '购书订单号唯一索引',
    KEY `idx_borrow_order_id` (`borrow_order_id`),
    KEY `idx_borrow_detail_id` (`borrow_detail_id`),
    KEY `idx_member_id` (`member_id`),
    KEY `idx_member_card_no` (`member_card_no`),
    KEY `idx_book_id` (`book_id`),
    KEY `idx_order_type` (`order_type`),
    KEY `idx_order_status` (`order_status`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购书订单表（dd_前缀，JSDD订单号）';


-- ----------------------------
-- 8. 图书变更历史表
-- ----------------------------
DROP TABLE IF EXISTS book_info_history;
CREATE TABLE book_info_history (
    `id`                BIGINT(20)      NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `book_id`           BIGINT(20)      NOT NULL                 COMMENT '图书ID，关联book_info.id',
    `change_type`       TINYINT         NOT NULL                 COMMENT '变更类型：1-新增，2-修改，3-上架，4-下架',
    `before_data`       JSON            DEFAULT NULL             COMMENT '变更前完整数据(JSON)',
    `after_data`        JSON            DEFAULT NULL             COMMENT '变更后完整数据(JSON)',
    `change_fields`     VARCHAR(500)    DEFAULT ''               COMMENT '变更字段列表，逗号分隔',
    `change_reason`     VARCHAR(500)    DEFAULT ''               COMMENT '变更原因',
    `source_order_no`   VARCHAR(50)     DEFAULT ''               COMMENT '来源单号（借书单/还书单/购书单）',
    `source_type`       TINYINT         DEFAULT 0                COMMENT '来源类型：1-借书，2-还书，3-购书，4-手动修改',
    `staff_id`          VARCHAR(50)     DEFAULT ''               COMMENT '操作员工ID',
    `staff_name`        VARCHAR(50)     DEFAULT ''               COMMENT '操作员工姓名',
    `created_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    KEY `idx_book_id` (`book_id`),
    KEY `idx_change_type` (`change_type`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书变更历史表';


-- ----------------------------
-- 9.1 借书操作日志表
-- ----------------------------
DROP TABLE IF EXISTS book_borrow_log;
CREATE TABLE book_borrow_log (
    `id`                BIGINT(20)      NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `trace_id`          VARCHAR(64)     NOT NULL                 COMMENT '全链路追踪ID(UUID)，关联同一操作产生的多表日志',
    `borrow_order_id`   BIGINT(20)      NOT NULL                 COMMENT '借书单ID',
    `borrow_order_no`   VARCHAR(50)     NOT NULL                 COMMENT '借书单号',
    `borrow_detail_id`  BIGINT(20)      DEFAULT NULL             COMMENT '借书明细ID（明细级操作时记录）',
    `source_order_no`   VARCHAR(50)     DEFAULT ''               COMMENT '触发本日志的来源单号（如还书单HSxxx、购书单JSDDxxx）',
    `source_type`       TINYINT         DEFAULT 0                COMMENT '触发来源类型：0-借书操作，1-还书触发，2-借转购触发，3-手动修改',
    `log_type`          TINYINT         NOT NULL                 COMMENT '日志类型：1-创建借书单，2-修改借书单，3-完结借书单，4-新增借书明细，5-还书触发明细变更，6-借转购触发明细变更，7-手动修改明细',
    `before_data`       JSON            DEFAULT NULL             COMMENT '操作前数据(JSON)',
    `after_data`        JSON            DEFAULT NULL             COMMENT '操作后数据(JSON)',
    `change_fields`     VARCHAR(500)    DEFAULT ''               COMMENT '变更字段',
    `staff_id`          VARCHAR(50)     DEFAULT ''               COMMENT '操作员工ID',
    `staff_name`        VARCHAR(50)     DEFAULT ''               COMMENT '操作员工姓名',
    `client_ip`         VARCHAR(100)    DEFAULT ''               COMMENT '客户端IP',
    `created_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    KEY `idx_trace_id` (`trace_id`),
    KEY `idx_borrow_order_id` (`borrow_order_id`),
    KEY `idx_borrow_order_no` (`borrow_order_no`),
    KEY `idx_borrow_detail_id` (`borrow_detail_id`),
    KEY `idx_source_order_no` (`source_order_no`),
    KEY `idx_log_type` (`log_type`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_staff_id` (`staff_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='借书操作日志表';


-- ----------------------------
-- 9.2 还书操作日志表
-- ----------------------------
DROP TABLE IF EXISTS book_return_log;
CREATE TABLE book_return_log (
    `id`                BIGINT(20)      NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `trace_id`          VARCHAR(64)     NOT NULL                 COMMENT '全链路追踪ID(UUID)，关联同一操作产生的多表日志',
    `return_detail_id`  BIGINT(20)      NOT NULL                 COMMENT '还书明细ID，关联book_return_detail.id',
    `return_order_no`   VARCHAR(50)     NOT NULL                 COMMENT '还书单号',
    `borrow_order_id`   BIGINT(20)      NOT NULL                 COMMENT '借书单ID',
    `borrow_order_no`   VARCHAR(50)     NOT NULL                 COMMENT '借书单号',
    `borrow_detail_id`  BIGINT(20)      NOT NULL                 COMMENT '借书明细ID',
    `log_type`          TINYINT         NOT NULL                 COMMENT '日志类型：1-创建还书，2-修改还书信息，3-撤销还书',
    `before_data`       JSON            DEFAULT NULL             COMMENT '操作前数据(JSON)：还书单+借书明细变更前',
    `after_data`        JSON            DEFAULT NULL             COMMENT '操作后数据(JSON)：还书单+借书明细变更后',
    `change_fields`     VARCHAR(500)    DEFAULT ''               COMMENT '变更字段',
    `staff_id`          VARCHAR(50)     DEFAULT ''               COMMENT '操作员工ID',
    `staff_name`        VARCHAR(50)     DEFAULT ''               COMMENT '操作员工姓名',
    `client_ip`         VARCHAR(100)    DEFAULT ''               COMMENT '客户端IP',
    `created_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    KEY `idx_trace_id` (`trace_id`),
    KEY `idx_return_detail_id` (`return_detail_id`),
    KEY `idx_return_order_no` (`return_order_no`),
    KEY `idx_borrow_order_id` (`borrow_order_id`),
    KEY `idx_borrow_detail_id` (`borrow_detail_id`),
    KEY `idx_log_type` (`log_type`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_staff_id` (`staff_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='还书操作日志表';


-- ----------------------------
-- 9.3 购书操作日志表
-- ----------------------------
DROP TABLE IF EXISTS book_purchase_log;
CREATE TABLE book_purchase_log (
    `id`                BIGINT(20)      NOT NULL AUTO_INCREMENT  COMMENT '主键',
    `trace_id`          VARCHAR(64)     NOT NULL                 COMMENT '全链路追踪ID(UUID)，关联同一操作产生的多表日志',
    `purchase_order_id` BIGINT(20)      NOT NULL                 COMMENT '购书订单ID，关联dd_book_purchase_order.id',
    `purchase_order_no` VARCHAR(50)     NOT NULL                 COMMENT '购书订单号',
    `borrow_order_id`   BIGINT(20)      DEFAULT NULL             COMMENT '关联借书单ID（借转购时）',
    `borrow_detail_id`  BIGINT(20)      DEFAULT NULL             COMMENT '关联借书明细ID（借转购时）',
    `log_type`          TINYINT         NOT NULL                 COMMENT '日志类型：1-创建订单，2-付款，3-取消，4-退款，5-修改',
    `before_data`       JSON            DEFAULT NULL             COMMENT '操作前数据(JSON)：购书单+借书明细变更前',
    `after_data`        JSON            DEFAULT NULL             COMMENT '操作后数据(JSON)：购书单+借书明细变更后',
    `change_fields`     VARCHAR(500)    DEFAULT ''               COMMENT '变更字段',
    `staff_id`          VARCHAR(50)     DEFAULT ''               COMMENT '操作员工ID',
    `staff_name`        VARCHAR(50)     DEFAULT ''               COMMENT '操作员工姓名',
    `client_ip`         VARCHAR(100)    DEFAULT ''               COMMENT '客户端IP',
    `created_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    KEY `idx_trace_id` (`trace_id`),
    KEY `idx_purchase_order_id` (`purchase_order_id`),
    KEY `idx_purchase_order_no` (`purchase_order_no`),
    KEY `idx_borrow_order_id` (`borrow_order_id`),
    KEY `idx_log_type` (`log_type`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_staff_id` (`staff_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购书操作日志表';


-- ============================================================
-- 表关系说明
-- ============================================================
--
-- ┌──────────┐     ┌──────────────┐     ┌─────────────────────┐
-- │ member   │──<  │borrow_order  │──<  │borrow_detail        │
-- │ (会员)    │     │(借书单,DY)    │     │(借书明细,每本书一条)   │
-- └──────────┘     └──────────────┘     └──────┬──────┬───────┘
--                                              │      │
--                         ┌────────────────────┘      └──────────────┐
--                         ▼                                           ▼
--              ┌─────────────────┐                      ┌──────────────────────┐
--              │ return_detail   │                      │ dd_purchase_order    │
--              │ (还书单,HS)      │                      │ (购书订单,JSDD)       │
--              └────────┬────────┘                      └──────────┬───────────┘
--                       │                                          │
--    ┌──────────────────┼──────────────────┐                       │
--    ▼                  ▼                  ▼                       ▼
-- ┌──────────┐  ┌──────────────┐  ┌────────────────┐  ┌──────────────────┐
-- │borrow_log│  │ return_log   │  │ purchase_log   │  │ book_info_history│
-- │(借书日志) │  │(还书日志)     │  │(购书日志)       │  │(图书变更历史)      │
-- └──────────┘  └──────────────┘  └────────────────┘  └──────────────────┘
--    trace_id      trace_id          trace_id           source_order_no
--       └──────────────┴─────────────────┘                    │
--                    同一trace_id串联                          │
--                                                            ▼
--                                                   book_info / book_image
--                                                   (图书 / 图片)
--
-- JOIN 关系速查:
--   borrow_order  ← borrow_detail     ON borrow_order.id = borrow_detail.borrow_order_id
--   borrow_detail ← borrow_detail_img ON borrow_detail.id = borrow_detail_image.borrow_detail_id
--   borrow_detail ← return_detail     ON borrow_detail.id = return_detail.borrow_detail_id
--   borrow_detail ← dd_purchase_order ON borrow_detail.id = dd_book_purchase_order.borrow_detail_id
--   borrow_order  ← borrow_log        ON borrow_order.id = borrow_log.borrow_order_id
--   return_detail ← return_log        ON return_detail.id = return_log.return_detail_id
--   purchase_order← purchase_log      ON dd_book_purchase_order.id = purchase_log.purchase_order_id
--   book_info     ← book_info_history ON book_info.id = book_info_history.book_id
--   book_info     ← book_image        ON book_info.id = book_image.book_id
--   member        ← borrow_order      ON member.id = borrow_order.member_id
--   book_info     ← borrow_detail     ON book_info.id = borrow_detail.book_id
--


-- ============================================================
-- 二、常用数据查询
-- ============================================================

-- 2.1 查询会员当前借阅中/未还的图书
-- ================================
-- SELECT
--     bo.order_no              AS 借书单号,
--     bo.member_name           AS 会员姓名,
--     bo.member_phone          AS 会员手机,
--     bd.book_name             AS 图书名称,
--     bd.borrow_qty            AS 借书数量,
--     bd.returned_qty          AS 已还数量,
--     bd.purchase_qty          AS 转购数量,
--     bd.borrow_qty - bd.returned_qty - bd.purchase_qty AS 未还数量,
--     CASE bd.borrow_status
--         WHEN 1 THEN '已借阅'
--         WHEN 2 THEN '已全部归还'
--         WHEN 3 THEN '部分归还'
--         WHEN 4 THEN '部分借转购'
--         WHEN 5 THEN '全部借转购'
--     END AS 借阅状态,
--     bo.borrow_time           AS 借书时间,
--     bo.expected_return_time  AS 预计还书时间,
--     DATEDIFF(NOW(), bo.borrow_time) AS 已借天数
-- FROM book_borrow_order bo
-- INNER JOIN book_borrow_detail bd ON bo.id = bd.borrow_order_id
-- WHERE bo.is_del = 0
--   AND bd.is_del = 0
--   AND bd.borrow_status IN (1, 3, 4)  -- 未完全归还/未完全转购
-- ORDER BY bo.borrow_time DESC;


-- 2.2 查询会员借阅历史（已完结）
-- ================================
-- SELECT
--     bo.order_no        AS 借书单号,
--     bo.member_name     AS 会员姓名,
--     bo.member_phone    AS 会员手机,
--     bo.total_book_count AS 借书本数,
--     CASE bo.borrow_status
--         WHEN 2 THEN '已全部归还'
--         WHEN 5 THEN '全部借转购'
--     END AS 最终状态,
--     bo.borrow_time     AS 借书时间,
--     bo.return_all_time AS 还清时间,
--     bo.expected_return_time AS 预计还书时间,
--     DATEDIFF(bo.return_all_time, bo.expected_return_time) AS 超期天数
-- FROM book_borrow_order bo
-- WHERE bo.is_del = 0
--   AND bo.is_finished = 1
--   AND bo.member_id = ?   -- 传入会员ID
-- ORDER BY bo.borrow_time DESC;


-- 2.3 查询某本图书的当前在借情况
-- ================================
-- SELECT
--     bi.book_name       AS 图书名称,
--     bi.lendable_qty     AS 可借数量,
--     bi.stock_qty        AS 库存总量,
--     bd.borrow_qty       AS 本次借出,
--     bd.returned_qty     AS 已还,
--     bo.order_no         AS 借书单号,
--     bo.member_name      AS 借阅人,
--     bo.member_phone     AS 借阅人手机,
--     bo.borrow_time      AS 借书时间,
--     DATEDIFF(NOW(), bo.borrow_time) AS 已借天数
-- FROM book_info bi
-- INNER JOIN book_borrow_detail bd ON bi.id = bd.book_id
-- INNER JOIN book_borrow_order bo ON bd.borrow_order_id = bo.id
-- WHERE bi.id = ?        -- 传入图书ID
--   AND bi.is_del = 0
--   AND bd.is_del = 0
--   AND bd.borrow_status IN (1, 3, 4)  -- 未完全归还
-- ORDER BY bo.borrow_time;


-- 2.4 查询某个会员的详细借阅明细（含还书/购书状态）
-- ================================
-- SELECT
--     bo.order_no        AS 借书单号,
--     bo.member_name     AS 会员,
--     bd.book_name       AS 图书,
--     bd.borrow_qty      AS 借书数量,
--     bd.returned_qty    AS 已还,
--     bd.purchase_qty    AS 转购,
--     bd.borrow_qty - bd.returned_qty - bd.purchase_qty AS 待处理,
--     CASE bd.borrow_status
--         WHEN 1 THEN '借阅中'
--         WHEN 2 THEN '已还清'
--         WHEN 3 THEN '部分还'
--         WHEN 4 THEN '部分转购'
--         WHEN 5 THEN '已转购'
--     END AS 状态,
--     bd.borrow_time     AS 借书时间,
--     bd.return_all_time AS 还清时间,
--     bo.first_staff_name AS 经办人,
--     GROUP_CONCAT(DISTINCT rd.return_order_no) AS 还书单号,
--     bd.purchase_order_no AS 购书单号
-- FROM book_borrow_detail bd
-- INNER JOIN book_borrow_order bo ON bd.borrow_order_id = bo.id
-- LEFT JOIN book_return_detail rd ON bd.id = rd.borrow_detail_id AND rd.is_del = 0
-- WHERE bd.member_id = ?    -- 传入会员ID
--   AND bd.is_del = 0
-- GROUP BY bd.id
-- ORDER BY bd.borrow_time DESC;


-- 2.5 还书操作查询（某天的还书记录）
-- ================================
-- SELECT
--     rd.return_order_no  AS 还书单号,
--     rd.borrow_order_no  AS 借书单号,
--     rd.book_name        AS 图书,
--     rd.return_qty        AS 还书数量,
--     CASE rd.return_type
--         WHEN 1 THEN '正常还书'
--         WHEN 2 THEN '损坏还书'
--         WHEN 3 THEN '遗失赔偿'
--     END AS 还书类型,
--     rd.return_time      AS 还书时间,
--     rd.staff_name       AS 操作员工,
--     rd.remark           AS 备注
-- FROM book_return_detail rd
-- WHERE rd.is_del = 0
--   AND DATE(rd.return_time) = ?  -- 传入日期 '2026-06-29'
-- ORDER BY rd.return_time DESC;


-- 2.6 购书订单查询
-- ================================
-- SELECT
--     dpo.order_no        AS 订单号,
--     CASE dpo.order_type
--         WHEN 1 THEN '购书'
--         WHEN 2 THEN '借转购'
--     END AS 订单类型,
--     dpo.book_name       AS 图书,
--     dpo.qty             AS 数量,
--     dpo.unit_price      AS 单价,
--     dpo.receivable_amount AS 应收,
--     dpo.paid_amount     AS 实付,
--     CASE dpo.payment_type
--         WHEN 1 THEN '现金' WHEN 2 THEN '微信'
--         WHEN 3 THEN '支付宝' WHEN 4 THEN '积分' WHEN 5 THEN '混合'
--     END AS 付款方式,
--     CASE dpo.order_status
--         WHEN 0 THEN '待付款' WHEN 1 THEN '已付款'
--         WHEN 2 THEN '已取消' WHEN 3 THEN '已退款'
--     END AS 状态,
--     dpo.member_name     AS 会员,
--     dpo.create_staff_name AS 操作员工,
--     dpo.created_at      AS 创建时间
-- FROM dd_book_purchase_order dpo
-- WHERE dpo.is_del = 0
-- ORDER BY dpo.created_at DESC;


-- 2.7 图书库存快速查询
-- ================================
-- SELECT
--     id, book_name, isbn, publisher,
--     stock_qty            AS 库存,
--     lendable_qty         AS 可借,
--     stock_qty - lendable_qty AS 在借中,
--     CASE book_status
--         WHEN 1 THEN '初始化' WHEN 2 THEN '上架' WHEN 3 THEN '下架'
--     END AS 状态,
--     price, sale_price,
--     updated_at           AS 最后修改时间
-- FROM book_info
-- WHERE is_del = 0
-- ORDER BY book_status ASC, lendable_qty DESC;


-- ============================================================
-- 三、常用报表查询
-- ============================================================

-- 3.1 借阅日报（某日借书统计）
-- ================================
-- SELECT
--     DATE(bo.borrow_time)       AS 日期,
--     bo.dept_id                 AS 门店ID,
--     COUNT(DISTINCT bo.id)      AS 借书单数,
--     COUNT(DISTINCT bo.member_id) AS 借书人数,
--     SUM(bd.borrow_qty)         AS 借出册数
-- FROM book_borrow_order bo
-- INNER JOIN book_borrow_detail bd ON bo.id = bd.borrow_order_id AND bd.is_del = 0
-- WHERE bo.is_del = 0
--   AND DATE(bo.borrow_time) = ?  -- 传入日期
-- GROUP BY DATE(bo.borrow_time), bo.dept_id;


-- 3.2 还书日报（某日还书统计）
-- ================================
-- SELECT
--     DATE(rd.return_time)      AS 日期,
--     rd.dept_id                AS 门店ID,
--     COUNT(DISTINCT rd.id)     AS 还书笔数,
--     COUNT(DISTINCT rd.borrow_order_id) AS 涉及借书单数,
--     SUM(rd.return_qty)        AS 还书册数,
--     SUM(CASE WHEN rd.return_type = 2 THEN rd.return_qty ELSE 0 END) AS 损坏册数,
--     SUM(CASE WHEN rd.return_type = 3 THEN rd.return_qty ELSE 0 END) AS 遗失册数
-- FROM book_return_detail rd
-- WHERE rd.is_del = 0
--   AND DATE(rd.return_time) = ?  -- 传入日期
-- GROUP BY DATE(rd.return_time), rd.dept_id;


-- 3.3 购书日报（某日购书统计，含借转购）
-- ================================
-- SELECT
--     DATE(dpo.created_at)      AS 日期,
--     dpo.dept_id               AS 门店ID,
--     COUNT(DISTINCT dpo.id)    AS 订单数,
--     SUM(CASE WHEN dpo.order_type = 1 THEN 1 ELSE 0 END) AS 购书单数,
--     SUM(CASE WHEN dpo.order_type = 2 THEN 1 ELSE 0 END) AS 借转购单数,
--     SUM(dpo.qty)              AS 售出册数,
--     SUM(dpo.receivable_amount) AS 应收总额,
--     SUM(dpo.paid_amount)       AS 实收总额,
--     SUM(dpo.discount_price)    AS 折扣总额,
--     SUM(dpo.points_deduct)     AS 积分抵扣
-- FROM dd_book_purchase_order dpo
-- WHERE dpo.is_del = 0
--   AND dpo.order_status = 1     -- 已付款
--   AND DATE(dpo.created_at) = ? -- 传入日期
-- GROUP BY DATE(dpo.created_at), dpo.dept_id;


-- 3.4 会员借阅排行榜（TopN）
-- ================================
-- SELECT
--     bo.member_id,
--     bo.member_name              AS 会员姓名,
--     bo.member_phone             AS 手机号,
--     COUNT(DISTINCT bo.id)       AS 借阅次数,
--     SUM(bd.borrow_qty)          AS 借阅册数,
--     SUM(bd.returned_qty)        AS 已还册数,
--     SUM(bd.purchase_qty)        AS 转购册数,
--     MAX(bo.borrow_time)         AS 最近借书时间
-- FROM book_borrow_order bo
-- INNER JOIN book_borrow_detail bd ON bo.id = bd.borrow_order_id AND bd.is_del = 0
-- WHERE bo.is_del = 0
--   AND bo.borrow_time >= ?       -- 统计起始时间
--   AND bo.borrow_time <  ?       -- 统计截止时间
-- GROUP BY bo.member_id, bo.member_name, bo.member_phone
-- ORDER BY 借阅册数 DESC
-- LIMIT 20;


-- 3.5 图书借阅排行榜（TopN）
-- ================================
-- SELECT
--     bd.book_id,
--     bd.book_name                AS 图书名称,
--     bi.publisher                AS 出版社,
--     bi.price                    AS 定价,
--     COUNT(DISTINCT bd.id)       AS 被借次数,
--     SUM(bd.borrow_qty)          AS 借出总册数,
--     SUM(bd.returned_qty)        AS 归还册数,
--     SUM(bd.purchase_qty)        AS 转购册数,
--     bi.lendable_qty             AS 当前可借
-- FROM book_borrow_detail bd
-- LEFT JOIN book_info bi ON bd.book_id = bi.id AND bi.is_del = 0
-- WHERE bd.is_del = 0
--   AND bd.borrow_time >= ?
--   AND bd.borrow_time <  ?
-- GROUP BY bd.book_id, bd.book_name, bi.publisher, bi.price, bi.lendable_qty
-- ORDER BY 借出总册数 DESC
-- LIMIT 20;


-- 3.6 员工工作量统计
-- ================================
-- SELECT
--     t.staff_id,
--     t.staff_name                AS 员工姓名,
--     COUNT(DISTINCT t.order_no)   AS 借书单数,
--     SUM(t.borrow_qty)           AS 借出册数
-- FROM (
--     -- 借书
--     SELECT bo.first_staff_id AS staff_id, bo.first_staff_name AS staff_name,
--            bo.order_no, bd.borrow_qty
--     FROM book_borrow_order bo
--     INNER JOIN book_borrow_detail bd ON bo.id = bd.borrow_order_id AND bd.is_del = 0
--     WHERE bo.is_del = 0
--       AND bo.borrow_time >= ? AND bo.borrow_time < ?
--     UNION ALL
--     -- 还书
--     SELECT rd.staff_id, rd.staff_name, rd.return_order_no, rd.return_qty
--     FROM book_return_detail rd
--     WHERE rd.is_del = 0
--       AND rd.return_time >= ? AND rd.return_time < ?
--     UNION ALL
--     -- 购书
--     SELECT dpo.create_staff_id, dpo.create_staff_name, dpo.order_no, dpo.qty
--     FROM dd_book_purchase_order dpo
--     WHERE dpo.is_del = 0 AND dpo.order_status = 1
--       AND dpo.created_at >= ? AND dpo.created_at < ?
-- ) t
-- GROUP BY t.staff_id, t.staff_name
-- ORDER BY 借出册数 DESC;


-- 3.7 超期未还图书报表
-- ================================
-- SELECT
--     bo.order_no              AS 借书单号,
--     bo.member_name           AS 会员,
--     bo.member_phone          AS 手机,
--     bd.book_name             AS 图书,
--     bo.borrow_time           AS 借书时间,
--     bo.expected_return_time  AS 预计还书时间,
--     DATEDIFF(NOW(), bo.expected_return_time) AS 超期天数,
--     bo.first_staff_name      AS 经办人
-- FROM book_borrow_order bo
-- INNER JOIN book_borrow_detail bd ON bo.id = bd.borrow_order_id AND bd.is_del = 0
-- WHERE bo.is_del = 0
--   AND bd.borrow_status IN (1, 3, 4)  -- 未还清
--   AND bo.expected_return_time IS NOT NULL
--   AND bo.expected_return_time < NOW() -- 已超期
-- ORDER BY 超期天数 DESC;


-- 3.8 月度经营汇总报表
-- ================================
-- SELECT
--     DATE_FORMAT(t.date, '%Y-%m') AS 月份,
--     COUNT(DISTINCT t.order_no)    AS 业务单数,
--     SUM(t.borrow_qty)             AS 借出总册数,
--     SUM(t.return_qty)            AS 还书总册数,
--     SUM(t.purchase_qty)          AS 售出总册数,
--     SUM(t.purchase_amount)       AS 销售总额
-- FROM (
--     -- 借书
--     SELECT DATE(bo.borrow_time) AS date, bo.order_no,
--            bd.borrow_qty, 0 AS return_qty, 0 AS purchase_qty, 0 AS purchase_amount
--     FROM book_borrow_order bo
--     INNER JOIN book_borrow_detail bd ON bo.id = bd.borrow_order_id AND bd.is_del = 0
--     WHERE bo.is_del = 0
--     UNION ALL
--     -- 还书
--     SELECT DATE(rd.return_time), rd.return_order_no,
--            0, rd.return_qty, 0, 0
--     FROM book_return_detail rd
--     WHERE rd.is_del = 0
--     UNION ALL
--     -- 购书
--     SELECT DATE(dpo.created_at), dpo.order_no,
--            0, 0, dpo.qty, dpo.paid_amount
--     FROM dd_book_purchase_order dpo
--     WHERE dpo.is_del = 0 AND dpo.order_status = 1
-- ) t
-- WHERE t.date >= ? AND t.date < ?
-- GROUP BY DATE_FORMAT(t.date, '%Y-%m')
-- ORDER BY 月份;


-- 3.9 图书库存变动明细报表
-- ================================
-- SELECT
--     bih.book_id,
--     bi.book_name           AS 图书名称,
--     CASE bih.change_type
--         WHEN 1 THEN '新增' WHEN 2 THEN '修改'
--         WHEN 3 THEN '上架' WHEN 4 THEN '下架'
--     END AS 变更类型,
--     bih.change_reason      AS 变更原因,
--     bih.source_order_no    AS 来源单号,
--     CASE bih.source_type
--         WHEN 1 THEN '借书' WHEN 2 THEN '还书'
--         WHEN 3 THEN '购书' WHEN 4 THEN '手动'
--     END AS 来源类型,
--     bih.staff_name         AS 操作人,
--     bih.created_at         AS 变更时间
-- FROM book_info_history bih
-- LEFT JOIN book_info bi ON bih.book_id = bi.id
-- WHERE bih.created_at >= ? AND bih.created_at < ?
-- ORDER BY bih.created_at DESC;


-- 3.10 全链路追踪报表（按trace_id查询某次操作的完整过程）
-- ================================
-- SELECT '借书日志' AS 来源, log_type, before_data, after_data, created_at
-- FROM book_borrow_log WHERE trace_id = ?
-- UNION ALL
-- SELECT '还书日志', log_type, before_data, after_data, created_at
-- FROM book_return_log WHERE trace_id = ?
-- UNION ALL
-- SELECT '购书日志', log_type, before_data, after_data, created_at
-- FROM book_purchase_log WHERE trace_id = ?
-- ORDER BY created_at;


-- ============================================================
-- 四、常见问题排查指南
-- ============================================================
-- 格式: 问题描述 → 涉及表 → 日志表 → 排查SQL

-- 4.1 「会员说没借过这本书」
-- ================================
-- 涉及表: book_borrow_order + book_borrow_detail + book_borrow_detail_image
-- 日志表: book_borrow_log
-- 排查步骤:
--   ① 查该会员的所有借书单
--   ② 找到目标借书单后查明细
--   ③ 通过日志确认操作时的 before/after 数据
--   ④ 查看借书时拍摄的照片确认图书实物
-- SELECT bo.order_no, bo.borrow_time, bo.first_staff_name,
--        bd.book_name, bd.borrow_qty,
--        bdi.image_url
-- FROM book_borrow_order bo
-- INNER JOIN book_borrow_detail bd ON bo.id = bd.borrow_order_id
-- LEFT JOIN book_borrow_detail_image bdi ON bd.id = bdi.borrow_detail_id AND bdi.image_status = 0
-- WHERE bo.member_id = ? AND bo.is_del = 0 AND bd.is_del = 0
-- ORDER BY bo.borrow_time DESC;
-- -- 查日志: SELECT * FROM book_borrow_log WHERE borrow_detail_id = ? ORDER BY created_at;


-- 4.2 「还了书但系统显示没还 / 还书数量不对」
-- ================================
-- 涉及表: book_return_detail + book_borrow_detail + book_borrow_order
-- 日志表: book_return_log + book_borrow_log
-- 排查步骤:
--   ① 查该借书明细的所有还书记录
--   ② 对比借书数量 vs 累计还书数量 vs 借转购数量
--   ③ 查还书日志的 before_data/after_data 看当时数据快照
--   ④ 通过 trace_id 关联还书日志和借书日志确认原子性
-- SELECT bd.id AS 明细ID, bd.book_name, bd.borrow_qty, bd.returned_qty,
--        bd.purchase_qty, bd.borrow_qty - bd.returned_qty - bd.purchase_qty AS 差异,
--        rd.return_order_no, rd.return_qty, rd.return_time, rd.staff_name
-- FROM book_borrow_detail bd
-- LEFT JOIN book_return_detail rd ON bd.id = rd.borrow_detail_id AND rd.is_del = 0
-- WHERE bd.id = ? AND bd.is_del = 0
-- ORDER BY rd.return_time;
-- -- 查还书操作原子性:
-- SELECT '还书日志' AS src, trace_id, log_type, before_data, after_data, created_at
-- FROM book_return_log WHERE borrow_detail_id = ?
-- UNION ALL
-- SELECT '借书日志', trace_id, log_type, before_data, after_data, created_at
-- FROM book_borrow_log WHERE borrow_detail_id = ? AND source_type = 1
-- ORDER BY created_at;


-- 4.3 「借转购后为什么还显示借阅中」
-- ================================
-- 涉及表: dd_book_purchase_order + book_borrow_detail + book_borrow_order
-- 日志表: book_purchase_log + book_borrow_log
-- 排查步骤:
--   ① 查借书明细的 borrow_status 和 purchase_order_no
--   ② 查购书订单的 order_status 是否已付款
--   ③ 查购书日志 + 借书日志确认借转购操作链路
-- SELECT bd.id, bd.book_name, bd.borrow_status, bd.purchase_qty,
--        bd.purchase_order_no,
--        dpo.order_status, dpo.order_type, dpo.pay_time,
--        dpo.receivable_amount, dpo.paid_amount
-- FROM book_borrow_detail bd
-- LEFT JOIN dd_book_purchase_order dpo ON bd.purchase_order_no = dpo.order_no AND dpo.is_del = 0
-- WHERE bd.id = ?
--   AND bd.is_del = 0;
-- -- 查转购操作日志:
-- SELECT '购书日志' AS src, trace_id, log_type, before_data, after_data
-- FROM book_purchase_log WHERE borrow_detail_id = ?
-- UNION ALL
-- SELECT '借书日志', trace_id, log_type, before_data, after_data
-- FROM book_borrow_log WHERE borrow_detail_id = ? AND source_type = 2
-- ORDER BY created_at;


-- 4.4 「图书库存对不上」
-- ================================
-- 涉及表: book_info + book_info_history
-- 日志表: 无独立日志，book_info_history 本身就是变更历史
-- 排查步骤:
--   ① 查当前库存
--   ② 查所有变更历史（含 source_order_no 追溯来源）
--   ③ 手工计算: 初始库存 - 借出 + 归还 + 购入 = 当前库存
-- SELECT '当前' AS 节点, NOW() AS 时间, stock_qty, lendable_qty,
--        stock_qty - lendable_qty AS 在借中, NULL AS 来源单号
-- FROM book_info WHERE id = ?
-- UNION ALL
-- SELECT CASE change_type WHEN 1 THEN '新增' WHEN 3 THEN '上架' WHEN 4 THEN '下架' END,
--        created_at,
--        JSON_EXTRACT(after_data, '$.stock_qty'),
--        JSON_EXTRACT(after_data, '$.lendable_qty'),
--        NULL, source_order_no
-- FROM book_info_history
-- WHERE book_id = ? AND change_type IN (1, 3, 4)
-- ORDER BY 时间;
-- -- 完整库存对账:
-- SELECT
--     bi.id, bi.book_name, bi.stock_qty AS 当前库存, bi.lendable_qty AS 可借,
--     COALESCE(SUM(bd.borrow_qty), 0) AS 累计借出,
--     COALESCE(SUM(bd.returned_qty), 0) AS 累计归还,
--     COALESCE(SUM(bd.purchase_qty), 0) AS 累计转购,
--     COALESCE(SUM(dpo.qty), 0) AS 直接购书,
--     bi.stock_qty + COALESCE(SUM(bd.borrow_qty), 0)
--       - COALESCE(SUM(bd.returned_qty), 0)
--       - COALESCE(SUM(dpo.qty), 0) AS 推算初始库存
-- FROM book_info bi
-- LEFT JOIN book_borrow_detail bd ON bi.id = bd.book_id AND bd.is_del = 0
-- LEFT JOIN dd_book_purchase_order dpo ON bi.id = dpo.book_id
--   AND dpo.order_type = 1 AND dpo.order_status = 1 AND dpo.is_del = 0
-- WHERE bi.id = ? AND bi.is_del = 0
-- GROUP BY bi.id, bi.book_name, bi.stock_qty, bi.lendable_qty;


-- 4.5 「员工操作争议 — 谁动了这条记录」
-- ================================
-- 涉及表: book_borrow_order / book_return_detail / dd_book_purchase_order
-- 日志表: book_borrow_log + book_return_log + book_purchase_log
-- 排查步骤:
--   ① 按单号从日志表查所有操作记录
--   ② 每条记录都有 staff_id + staff_name + client_ip
--   ③ before_data / after_data 可还原每步操作前后的完整数据
-- SELECT '借书' AS 操作, log_type, staff_name, client_ip, created_at
-- FROM book_borrow_log WHERE borrow_order_no = ?
-- UNION ALL
-- SELECT '还书', log_type, staff_name, client_ip, created_at
-- FROM book_return_log WHERE borrow_order_no = ?
-- UNION ALL
-- SELECT '购书', log_type, staff_name, client_ip, created_at
-- FROM book_purchase_log WHERE borrow_order_id = ?
-- ORDER BY created_at;
-- -- 查看某次操作的具体变更内容:
-- SELECT log_type,
--        JSON_EXTRACT(before_data, '$.returned_qty') AS 还前数量,
--        JSON_EXTRACT(after_data, '$.returned_qty')  AS 还后数量,
--        JSON_EXTRACT(after_data, '$.borrow_status') AS 变更后状态,
--        staff_name, created_at
-- FROM book_borrow_log
-- WHERE borrow_detail_id = ? AND source_type = 1  -- 还书触发
-- ORDER BY created_at;


-- 4.6 「某本图书的完整生命周期」
-- ================================
-- 涉及表: book_info + book_borrow_detail + book_return_detail + dd_book_purchase_order
-- 日志表: book_info_history + book_borrow_log + book_return_log + book_purchase_log
-- 排查步骤:
--   ① 查该图书的库存变更历史
--   ② 查该图书的所有借阅明细（含还书、转购）
--   ③ 按时间线合并所有操作
-- -- 库存变更:
-- SELECT created_at, change_type, change_reason, source_order_no, staff_name
-- FROM book_info_history WHERE book_id = ? ORDER BY created_at;
-- -- 借阅流转:
-- SELECT bd.borrow_time AS 时间, '借出' AS 操作, bo.order_no AS 单号,
--        bd.borrow_qty AS 数量, bo.member_name AS 会员
-- FROM book_borrow_detail bd
-- INNER JOIN book_borrow_order bo ON bd.borrow_order_id = bo.id
-- WHERE bd.book_id = ? AND bd.is_del = 0
-- UNION ALL
-- SELECT rd.return_time, '归还', rd.return_order_no, rd.return_qty, ''
-- FROM book_return_detail rd WHERE rd.book_id = ? AND rd.is_del = 0
-- UNION ALL
-- SELECT dpo.created_at, '购书', dpo.order_no, dpo.qty, dpo.member_name
-- FROM dd_book_purchase_order dpo WHERE dpo.book_id = ? AND dpo.is_del = 0
-- ORDER BY 时间;


-- 4.7 「借书单状态异常 — 显示未完结但明细都还了」
-- ================================
-- 涉及表: book_borrow_order + book_borrow_detail
-- 日志表: book_borrow_log
-- 排查步骤:
--   ① 对比借书单的 borrow_status 和所有明细的 borrow_status
--   ② 查日志看是否有遗漏的完结操作
--   ③ 计算: 所有明细都完结→ 借书单应完结
-- SELECT bo.order_no, bo.borrow_status AS 单状态, bo.is_finished AS 完结,
--        bd.id AS 明细ID, bd.book_name, bd.borrow_status AS 明细状态,
--        bd.borrow_qty, bd.returned_qty, bd.purchase_qty
-- FROM book_borrow_order bo
-- INNER JOIN book_borrow_detail bd ON bo.id = bd.borrow_order_id AND bd.is_del = 0
-- WHERE bo.order_no = ? AND bo.is_del = 0;
-- -- 查是否漏记完结日志:
-- SELECT * FROM book_borrow_log
-- WHERE borrow_order_no = ? AND log_type = 3  -- log_type=3 是完结操作
-- ORDER BY created_at;


-- 4.8 「购书订单付款了但状态还是待付款 / 金额对不上」
-- ================================
-- 涉及表: dd_book_purchase_order
-- 日志表: book_purchase_log
-- 排查步骤:
--   ① 查当前订单状态和金额
--   ② 查日志还原每次变更前后的金额和状态
-- SELECT order_no, order_type, payment_type, order_status,
--        qty, unit_price, discount_price,
--        receivable_amount, paid_amount, points_deduct,
--        receivable_amount - paid_amount AS 差额,
--        pay_time, create_staff_name
-- FROM dd_book_purchase_order
-- WHERE order_no = ? AND is_del = 0;
-- -- 金额变更追溯:
-- SELECT log_type,
--        JSON_EXTRACT(before_data, '$.order_status')      AS 前状态,
--        JSON_EXTRACT(after_data, '$.order_status')       AS 后状态,
--        JSON_EXTRACT(before_data, '$.receivable_amount')  AS 前应收,
--        JSON_EXTRACT(after_data, '$.receivable_amount')   AS 后应收,
--        JSON_EXTRACT(before_data, '$.paid_amount')        AS 前实付,
--        JSON_EXTRACT(after_data, '$.paid_amount')         AS 后实付,
--        staff_name, created_at
-- FROM book_purchase_log
-- WHERE purchase_order_no = ?
-- ORDER BY created_at;


-- 4.9 「图片丢失 / 借书图片与还书图片不一致」
-- ================================
-- 涉及表: book_borrow_detail_image + book_image
-- 日志表: 无独立日志（图片表无log表）
-- 排查步骤:
--   ① 按借书单号查所有关联的借书/还书图片
--   ② 结合借书时间对比图片上传时间
-- SELECT bdi.image_id, bdi.image_name, bdi.image_url,
--        CASE bdi.image_type
--            WHEN 1 THEN '借书拍摄' WHEN 2 THEN '还书拍摄' WHEN 3 THEN '损坏记录'
--        END AS 图片类型,
--        bdi.image_status, bdi.created_at, bdi.create_staff_name,
--        bd.book_name, bo.order_no AS 借书单号
-- FROM book_borrow_detail_image bdi
-- INNER JOIN book_borrow_detail bd ON bdi.borrow_detail_id = bd.id
-- INNER JOIN book_borrow_order bo ON bdi.borrow_order_id = bo.id
-- WHERE bdi.borrow_order_no = ?
--   AND bdi.image_status = 0
-- ORDER BY bdi.created_at;


-- 4.10 「数据修复 — 如何安全地修改一条记录并保留完整的审计轨迹」
-- ================================
-- 涉及表: 目标业务表 + 对应的log表
-- 日志表: 按操作类型写入对应log表
-- 修复原则:
--   ① SELECT 当前数据 → 存入 before_data
--   ② UPDATE 目标表
--   ③ SELECT 修改后数据 → 存入 after_data
--   ④ INSERT 对应log表 (trace_id=新UUID, log_type=7/3/5-手动修改,
--       source_type=3-手动, before_data+after_data+change_fields+staff信息)
--   ⑤ 如果是图书库存修改，额外 INSERT book_info_history
-- 示例: 手动修改借书明细的已还数量
--   SET @trace = UUID();
--   -- Step1: 记录修改前数据
--   SELECT * FROM book_borrow_detail WHERE id = ?;  -- 存入 before_data
--   -- Step2: 执行修改
--   UPDATE book_borrow_detail SET returned_qty = ?, borrow_status = ?, last_staff_id = ?, last_staff_name = ? WHERE id = ?;
--   -- Step3: 记录修改后数据
--   SELECT * FROM book_borrow_detail WHERE id = ?;  -- 存入 after_data
--   -- Step4: 写入日志
--   INSERT INTO book_borrow_log (trace_id, borrow_order_id, borrow_order_no, borrow_detail_id,
--       source_type, log_type, before_data, after_data, change_fields, staff_id, staff_name, client_ip)
--   VALUES (@trace, ?, ?, ?, 3, 7, '{...}', '{...}', 'returned_qty,borrow_status', ?, ?, ?);


-- ============================================================
-- 五、表→日志表映射速查
-- ============================================================
--
-- 业务表                    → 日志表                  → 历史表
-- ───────────────────────────────────────────────────────────
-- book_info                → (无独立日志)             → book_info_history
-- book_image               → (无独立日志)             → (无)
-- book_borrow_order        → book_borrow_log          → (日志表即历史)
-- book_borrow_detail       → book_borrow_log          → (日志表即历史)
-- book_borrow_detail_image → (无独立日志)             → (无)
-- book_return_detail       → book_return_log          → (日志表即历史)
-- dd_book_purchase_order   → book_purchase_log        → (日志表即历史)
--
-- 操作场景         业务表(写)            日志表(记)                  关键关联字段
-- ──────────────────────────────────────────────────────────────────────────
-- 新增图书         book_info             book_info_history          book_id
-- 修改图书         book_info             book_info_history          book_id
-- 上架/下架        book_info             book_info_history          book_id, source_order_no
-- 上传图片         book_image            (无)                       book_id
-- 创建借书单       book_borrow_order     book_borrow_log            borrow_order_id, trace_id
--                  book_borrow_detail    (log_type=1,4)
--                  book_info             book_info_history          source_order_no=DYxxx
-- 还书             book_return_detail    book_return_log            return_detail_id, trace_id
--                  book_borrow_detail    book_borrow_log            source_order_no=HSxxx
--                  book_borrow_order     (log_type=5, source_type=1)
--                  book_info             book_info_history          source_order_no=HSxxx
-- 借转购           dd_book_purchase_order book_purchase_log         purchase_order_id, trace_id
--                  book_borrow_detail    book_borrow_log            source_order_no=JSDDxxx
--                  book_borrow_order     (log_type=6, source_type=2)
-- 购书(直接购买)   dd_book_purchase_order book_purchase_log         purchase_order_id
--                  book_info             book_info_history          source_order_no=JSDDxxx
-- 手动修改         (目标表)              (对应log表)                  trace_id=新UUID
--                                        (log_type=7/3/5, source_type=3)


-- ============================================================
-- 订单号规则
-- ============================================================
-- DY   + yyyyMMddHHmmss + 6位随机数 → 借书单号   (book_borrow_order.order_no)
-- HS   + yyyyMMddHHmmss + 6位随机数 → 还书单号   (book_return_detail.return_order_no)
-- JSDD + yyyyMMddHHmmss + 6位随机数 → 购书订单号 (dd_book_purchase_order.order_no)


-- ============================================================
-- 六、数量闭环验证（数据一致性检查SQL）
-- ============================================================
-- 核心公式：borrow_qty = returned_qty + purchase_qty + 未处理
-- 派生字段(borrow_detail) 必须与 来源表(return_detail + purchase_order) 一致

-- 6.1 单条明细闭环验证（返回不一致的记录）
-- ==========================================
-- SELECT
--     bd.id AS 明细ID,
--     bd.book_name AS 图书,
--     bd.borrow_qty AS 借书数量,
--     bd.returned_qty AS 明细已还,
--     bd.purchase_qty AS 明细已转购,
--     bd.borrow_qty - bd.returned_qty - bd.purchase_qty AS 未处理,
--     COALESCE(SUM(rd.return_qty), 0) AS 还书表反算,
--     COALESCE(SUM(CASE WHEN dpo.order_status = 1 THEN dpo.qty ELSE 0 END), 0) AS 购书表反算,
--     bd.returned_qty - COALESCE(SUM(rd.return_qty), 0) AS 还书差异,
--     bd.purchase_qty - COALESCE(SUM(CASE WHEN dpo.order_status = 1 THEN dpo.qty ELSE 0 END), 0) AS 转购差异,
--     CASE WHEN bd.returned_qty = COALESCE(SUM(rd.return_qty), 0)
--           AND bd.purchase_qty = COALESCE(SUM(CASE WHEN dpo.order_status = 1 THEN dpo.qty ELSE 0 END), 0)
--          THEN '✅' ELSE '❌' END AS 闭环
-- FROM book_borrow_detail bd
-- LEFT JOIN book_return_detail rd ON bd.id = rd.borrow_detail_id AND rd.is_del = 0
-- LEFT JOIN dd_book_purchase_order dpo ON bd.id = dpo.borrow_detail_id AND dpo.is_del = 0
-- WHERE bd.is_del = 0
-- GROUP BY bd.id
-- HAVING 还书差异 != 0 OR 转购差异 != 0;

-- 6.2 全系统闭环率
-- ==========================
-- SELECT COUNT(*) AS 总明细, SUM(CASE WHEN t.闭环='✅' THEN 1 ELSE 0 END) AS 闭环数,
--        SUM(CASE WHEN t.闭环='❌' THEN 1 ELSE 0 END) AS 不一致数
-- FROM ( ... 同6.1 ... ) t;

-- 6.3 borrow_status 与实际数量一致性
-- ======================================
-- 状态规则:
--   1=已借阅    → returned_qty + purchase_qty == 0
--   2=已全部归还 → returned_qty == borrow_qty AND purchase_qty == 0
--   3=部分归还   → 0 < returned_qty < borrow_qty AND purchase_qty == 0
--   4=部分借转购 → 0 < purchase_qty < borrow_qty
--   5=全部借转购 → purchase_qty == borrow_qty
-- SELECT bd.id, bd.borrow_status, bd.borrow_qty, bd.returned_qty, bd.purchase_qty,
--        CASE
--            WHEN bd.borrow_status=1 AND bd.returned_qty+bd.purchase_qty>0 THEN '❌ 有处理但状态=已借阅'
--            WHEN bd.borrow_status=2 AND bd.returned_qty<bd.borrow_qty THEN '❌ 未全还但状态=已全部归还'
--            WHEN bd.borrow_status=2 AND bd.purchase_qty>0 THEN '❌ 有转购但状态=已全部归还'
--            WHEN bd.borrow_status=5 AND bd.purchase_qty<bd.borrow_qty THEN '❌ 未全转购但状态=全部借转购'
--            WHEN bd.borrow_status IN(1,3,4) AND bd.returned_qty+bd.purchase_qty>=bd.borrow_qty
--                THEN '❌ 已全处理但状态未完结'
--            ELSE '✅'
--        END AS 校验
-- FROM book_borrow_detail bd WHERE bd.is_del = 0
-- HAVING 校验 != '✅';

-- 6.4 借书单total_book_count与明细汇总一致性
-- ==============================================
-- SELECT bo.order_no, bo.total_book_count AS 单总数,
--        SUM(bd.borrow_qty) AS 明细汇总,
--        bo.total_book_count - SUM(bd.borrow_qty) AS 差异
-- FROM book_borrow_order bo
-- INNER JOIN book_borrow_detail bd ON bo.id = bd.borrow_order_id AND bd.is_del = 0
-- WHERE bo.is_del = 0
-- GROUP BY bo.id HAVING 差异 != 0;

-- 6.5 还书数量不超借书数量
-- ==========================
-- SELECT bd.id, bd.borrow_qty,
--        COALESCE(SUM(rd.return_qty), 0) AS 累计还书,
--        COALESCE(SUM(rd.return_qty), 0) - bd.borrow_qty AS 超还数量
-- FROM book_borrow_detail bd
-- INNER JOIN book_return_detail rd ON bd.id = rd.borrow_detail_id AND rd.is_del = 0
-- WHERE bd.is_del = 0
-- GROUP BY bd.id
-- HAVING 超还数量 > 0;

-- 6.6 转购数量不超借书数量
-- ==========================
-- SELECT bd.id, bd.borrow_qty,
--        COALESCE(SUM(dpo.qty), 0) AS 累计转购,
--        COALESCE(SUM(dpo.qty), 0) - bd.borrow_qty AS 超转数量
-- FROM book_borrow_detail bd
-- INNER JOIN dd_book_purchase_order dpo ON bd.id = dpo.borrow_detail_id
--        AND dpo.is_del = 0 AND dpo.order_status = 1
-- WHERE bd.is_del = 0
-- GROUP BY bd.id
-- HAVING 超转数量 > 0;

-- 6.7 还书+转购不超过借书数量
-- ============================
-- SELECT bd.id, bd.borrow_qty,
--        COALESCE(SUM(rd.return_qty), 0) + COALESCE(SUM(CASE WHEN dpo.order_status=1 THEN dpo.qty ELSE 0 END), 0) AS 已处理,
--        (COALESCE(SUM(rd.return_qty), 0) + COALESCE(SUM(CASE WHEN dpo.order_status=1 THEN dpo.qty ELSE 0 END), 0))
--        - bd.borrow_qty AS 超量
-- FROM book_borrow_detail bd
-- LEFT JOIN book_return_detail rd ON bd.id = rd.borrow_detail_id AND rd.is_del = 0
-- LEFT JOIN dd_book_purchase_order dpo ON bd.id = dpo.borrow_detail_id AND dpo.is_del = 0
-- WHERE bd.is_del = 0
-- GROUP BY bd.id
-- HAVING 超量 > 0;
