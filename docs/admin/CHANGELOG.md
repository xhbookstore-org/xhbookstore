# CHANGELOG — 变更记录

## v20260630

### 已实现
- **会员管理**：CRUD + 11位卡号自动生成 + 卡类型关联 + 门店归属
- **积分系统**：增加积分（悲观锁+事务）、积分流水查询、订单追溯
- **管理后台前端**：会员列表页（搜索/表格/分页）、积分管理入口
- **API基础设施**：JWT认证 + AOP日志 + 统一错误码 + CORS + 全局异常
- **API认证模块**：微信登录骨架 + 登录态校验 + 退出 + 注销
- **API积分模块**：增加/消耗积分 + 积分列表/详情
- **文件上传**：腾讯云COS上传 + 图书附件图片

### 已设计（表结构完成，代码未实现）
- 借书业务11张表（book_info, book_image, book_borrow_order, book_borrow_detail, book_borrow_detail_image, book_return_detail, dd_book_purchase_order, book_info_history, book_borrow_log, book_return_log, book_purchase_log）
- 订单号规则：DY(借书) / HS(还书) / JSDD(购书) / IN(积分入账) / OT(积分出账)

### 数据库变更
- `sql/migration_points_20260628.sql`：增量迁移
- `sql/book_tables_v1.sql`：借书业务11张表DDL + 闭环校验SQL

---

## v20260417

- 系统基础框架搭建（RuoYi-Vue）
- 会员主表 + 扩展表 + 级别表 + 卡类型表
- 积分订单表 + 入账单 + 出账单 + 核销明细
- 导入日志表 + 导入明细表
- API请求日志表
- 系统管理全部功能
