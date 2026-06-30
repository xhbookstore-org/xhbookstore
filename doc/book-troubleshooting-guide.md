# 借书业务问题排查手册

> 适用版本：v1.0 | 关联SQL：`sql/book_tables_v1.sql` | 表数量：11张业务表 + 3张日志表 + 1张历史表

---

## 一、常用单号前缀速查

| 前缀 | 表 | 示例 |
|------|-----|------|
| `DY` | book_borrow_order | `DY20260630143000123456` |
| `HS` | book_return_detail | `HS20260630143000654321` |
| `JSDD` | dd_book_purchase_order | `JSDD20260630143000789012` |

## 二、表→日志→历史 映射

| 业务表 | 日志表 | 历史表 |
|--------|--------|--------|
| book_info | — | book_info_history |
| book_borrow_order | book_borrow_log | （日志即历史） |
| book_borrow_detail | book_borrow_log | （日志即历史） |
| book_return_detail | book_return_log | （日志即历史） |
| dd_book_purchase_order | book_purchase_log | （日志即历史） |
| book_image | — | — |
| book_borrow_detail_image | — | — |

---

## 三、问题排查速查表

### 📌 借阅类问题

| 编号 | 问题现象 | 可能原因 | 涉及业务表 | 涉及日志表 | 处置步骤 |
|------|---------|---------|-----------|-----------|---------|
| B01 | 会员声称没借过某本书 | ①记错会员 ②会员卡被冒用 ③系统数据错误 | borrow_order、borrow_detail、borrow_detail_image | borrow_log | ①按member_id查所有借书单 ②找到目标单查明细 ③查borrow_log看操作员工+时间 ④调取借书照片确认实物 |
| B02 | 借书单找不到 | ①单号记错 ②已被删除 ③跨天查询条件不对 | borrow_order、borrow_detail | borrow_log | ①用会员手机号反查 ②模糊匹配单号 ③查is_del=1的软删除记录 ④查borrow_log有无删除操作 |
| B03 | 借书数量与实际不符 | ①录入错误 ②中途修改 ③还书/转购后数量变化 | borrow_detail | borrow_log | ①查borrow_detail. borrow_qty ②查borrow_log的before_data→after_data看修改记录 ③对比borrow_detail_image照片中的实际数量 |
| B04 | 同本书被重复借出 | ①并发操作 ②库存未扣减 | borrow_detail×2、book_info | borrow_log×2 | ①查book_info.lendable_qty是否变负 ②按book_id+ borrow_time查同时间窗口的借书明细 ③查borrow_log确认操作时序 |
| B05 | 借书时提示库存不足但实际有书 | ①库存数据未更新 ②图书已下架 ③还书后库存未恢复 | book_info、book_info_history | — | ①查book_info.lendable_qty和book_status ②查book_info_history看最后一次库存变更 ③查还书return_detail是否都更新了库存 |

### 📌 还书类问题

| 编号 | 问题现象 | 可能原因 | 涉及业务表 | 涉及日志表 | 处置步骤 |
|------|---------|---------|-----------|-----------|---------|
| R01 | 还了书但系统显示未还 | ①还书操作未提交成功 ②还到了别的借书单 ③还书记录被误删 | return_detail、borrow_detail、borrow_order | return_log、borrow_log | ①用会员ID查所有return_detail ②查return_log确认创建操作 ③查borrow_detail.returned_qty ④通过trace_id关联return_log和borrow_log确认原子性 |
| R02 | 还书数量不对（还了2本只显示1本） | ①分次还书记录不全 ②部分还书流转为损坏/遗失 | return_detail、borrow_detail | return_log | ①按borrow_detail_id查所有return_detail ②SUM(return_qty) VS borrow_qty ③查borrow_log的before_data/after_data |
| R03 | 还书单重复 | ①网络超时重试 ②员工重复操作 | return_detail×2 | return_log | ①查return_order_no是否重复 ②按borrow_detail_id+return_time查相近时间的记录 ③查调用日志的requestId |
| R04 | 还书后图书库存未恢复 | ①还书事务未提交 ②并发冲突 ③book_info更新遗漏 | book_info、book_info_history | return_log | ①查return_detail.return_time后的book_info_history ②查source_order_no=HSxxx的history记录 ③对比return_detail创建时间与book_info.lendable_qty变化时间 |
| R05 | 损坏还书标记争议 | ①标记错误 ②还书时图书状态与借出时不一致 | return_detail、borrow_detail_image | return_log、borrow_log | ①查return_detail.return_type ②对比借书照片和还书照片（borrow_detail_image不同image_type） ③查return_log的before_data看操作前标记 |

### 📌 借转购类问题

| 编号 | 问题现象 | 可能原因 | 涉及业务表 | 涉及日志表 | 处置步骤 |
|------|---------|---------|-----------|-----------|---------|
| P01 | 借转购后借阅状态未变 | ①购书订单未付款 ②关联关系断裂 ③borrow_detail.purchase_order_no为空 | dd_purchase_order、borrow_detail、borrow_order | purchase_log、borrow_log | ①查dd_purchase_order. order_status是否=1 ②查borrow_detail. purchase_order_no是否关联正确 ③查purchase_log看付款操作是否完整 ④查borrow_log source_type=2的记录 |
| P02 | 借转购金额计算错误 | ①折扣未应用 ②积分抵扣未扣减 ③单价取错 | dd_purchase_order | purchase_log | ①查receivable_amount = qty×unit_price-discount_price ②查points_deduct是否正确 ③查purchase_log的before/after看金额变更 |
| P03 | 同一本书既归还又转购 | ①并发操作冲突 ②业务规则绕过 | return_detail、dd_purchase_order、borrow_detail | return_log、purchase_log | ①查borrow_detail的returned_qty+purchase_qty是否>borrow_qty ②按borrow_detail_id查return_detail和dd_purchase_order的创建时间 ③查两份日志的trace_id确认时序 |
| P04 | 直接购书（非借转购）被错误关联借书单 | ①录入时选错了borrow_order_id | dd_purchase_order | purchase_log | ①查dd_purchase_order.order_type=1但borrow_order_id非空 ②查borrow_order_id对应的借书单是否真实存在 |

### 📌 库存类问题

| 编号 | 问题现象 | 可能原因 | 涉及业务表 | 涉及日志表 | 处置步骤 |
|------|---------|---------|-----------|-----------|---------|
| S01 | 库存为负数 | ①借出时未校验库存 ②还书重复恢复库存 ③并发超借 | book_info、borrow_detail、return_detail | book_info_history | ①查book_info.lendable_qty ②查book_info_history按时间倒序 ③手工对账：初始+归还-借出=当前 |
| S02 | 库存和实际盘点不符 | ①图书遗失未登记 ②损坏未下架 ③系统外借出未录入 | book_info、book_info_history | — | ①导出book_info当前库存 ②对比盘点结果 ③查book_info_history看变更频率 ④标记差异并生成盘点调整单 |
| S03 | 图书下架后仍被借出 | ①下架前已借出 ②下架操作晚于借书操作 | book_info、borrow_detail、book_info_history | borrow_log | ①查borrow_detail. borrow_time VS book_info_history 下架时间 ②如果借出时间早于下架，属正常 ③如果晚于下架，查book_info_history和borrow_log确认操作顺序 |
| S04 | 新书上架后库存显示为0 | ①上架时stock_qty/lendable_qty未设置 ②初始化状态未改为上架 | book_info、book_info_history | — | ①查book_info.book_status是否=2 ②查book_info_history的after_data确认上架时的库存值 ③如果是bug，手动UPDATE并记录history |

### 📌 操作争议类问题

| 编号 | 问题现象 | 可能原因 | 涉及业务表 | 涉及日志表 | 处置步骤 |
|------|---------|---------|-----------|-----------|---------|
| O01 | "不是我操作的" — 员工否认 | ①账号共用 ②账号被盗 ③系统记录错误 | 全部业务表 | 全部日志表 | ①查日志的staff_id+staff_name+client_ip ②按单号UNION ALL三张日志表 ③对比操作时间+IP是否与该员工习惯一致 ④查api访问日志xhbs_api_log |
| O02 | 两条矛盾的操作记录 | ①并发未加锁 ②事务回滚不完整 | borrow_detail、return_detail | borrow_log、return_log | ①按borrow_detail_id查所有return_detail和borrow_log ②对比时间线 ③查trace_id确认是否同一事务 ④如果是并发冲突，查FOR UPDATE是否生效 |
| O03 | 操作时间对不上 | ①系统时间不准 ②客户端时间与服务端时间差异 ③时区问题 | — | 全部日志表 | ①对比log的created_at和业务表的created_at ②对比client_ip和服务器时区 ③查xhbs_api_log的cost_time看请求耗时 |
| O04 | 备注被修改/被删除 | ①后操作覆盖了前操作 ②有人误操作 | 目标表 | 对应log表 | ①查log表before_data.remark VS after_data.remark ②查change_fields是否包含remark ③查修改时间和操作人 |

### 📌 订单/财务类问题

| 编号 | 问题现象 | 可能原因 | 涉及业务表 | 涉及日志表 | 处置步骤 |
|------|---------|---------|-----------|-----------|---------|
| F01 | 购书已付款但订单显示待付款 | ①支付回调失败 ②手动标记遗漏 ③事务未提交 | dd_purchase_order | purchase_log | ①查purchase_log看是否有log_type=2(付款)的记录 ②查pay_time字段 ③如有支付平台交易号，与支付平台对账 |
| F02 | 日销售额报表与收银对不上 | ①部分订单未计入 ②退款未扣减 ③统计时间范围不一致 | dd_purchase_order | — | ①按DATE(created_at)和order_status=1统计 ②检查是否有order_status=3(已退款)的遗漏 ③对比日报SQL(3.3)的结果 |
| F03 | 会员积分抵扣金额不对 | ①积分规则变更 ②重复抵扣 ③积分余额不足 | dd_purchase_order、member | purchase_log | ①查dd_purchase_order. points_deduct ②查member.current_points变化 ③查purchase_log确认抵扣操作 |
| F04 | 借转购与直接购书统计混淆 | ①order_type标记错误 | dd_purchase_order | purchase_log | ①查dd_purchase_order.order_type ②order_type=1为直接购书，=2为借转购 ③按order_type分组统计对比 |

### 📌 图片/附件类问题

| 编号 | 问题现象 | 可能原因 | 涉及业务表 | 涉及日志表 | 处置步骤 |
|------|---------|---------|-----------|-----------|---------|
| I01 | 借书图片打不开/丢失 | ①OSS文件过期 ②URL路径变更 ③文件被误删 | borrow_detail_image | — | ①查image_url是否可访问 ②查image_status是否=1(已删除) ③联系运维恢复文件或标记为已删除 |
| I02 | 借书时没拍照但系统有照片 | ①记错 ②后补的照片 ③关联错误 | borrow_detail_image | — | ①查image的created_at是否在借书时间之后很久 ②查create_staff_name是否为经办人 ③对比其他同一借书单的图片时间 |
| I03 | 还书时需要对比借书照片 | — | borrow_detail_image (image_type=1) | — | `SELECT * FROM book_borrow_detail_image WHERE borrow_order_no=? AND image_type=1 AND image_status=0` |

### 📌 会员相关问题

| 编号 | 问题现象 | 可能原因 | 涉及业务表 | 涉及日志表 | 处置步骤 |
|------|---------|---------|-----------|-----------|---------|
| M01 | 会员下有未知借书记录 | ①会员卡被他人使用 ②会员信息被合并 | borrow_order、member | borrow_log | ①查borrow_order的first_staff_name和创建时间 ②查borrow_log的client_ip ③与会员确认是否曾在该时间段到店 |
| M02 | 会员已注销/过期但有在借图书 | ①注销前未清还 ②过期后仍可借 | borrow_order、member | — | ①查该会员所有borrow_status IN (1,3,4)的明细 ②通知会员归还或处理 ③如系统规则需修改，标记为待办 |

### 📌 系统/技术类问题

| 编号 | 问题现象 | 可能原因 | 涉及业务表 | 涉及日志表 | 处置步骤 |
|------|---------|---------|-----------|-----------|---------|
| T01 | 操作超时但数据已写入 | ①网络超时+重试 ②前端重复提交 | 目标表 | 对应log表 | ①按单号查是否有重复记录 ②查log的created_at看是否有毫秒级重复 ③如有重复，软删除多余的(is_del=1) |
| T02 | 日志表数据量过大 | ①正常积累 ②before_data/after_data JSON过大 | — | 全部日志表 | ①SELECT COUNT(*) FROM各日志表 ②制定归档策略（如保留6个月） ③历史数据导出到归档表 |
| T03 | trace_id无法关联 | ①应用层未传递trace_id ②并发问题 | — | 全部日志表 | ①查trace_id为空的记录 ②按borrow_detail_id+created_at手动关联 ③检查应用代码trace_id生成逻辑 |
| T04 | JSON字段查询慢 | ①JSON过大 ②未建虚拟列索引 | — | 全部日志表 | ①SET GLOBAL log_slow_queries=ON ②对常用JSON路径建虚拟列+索引 ③考虑before_data/after_data截断策略 |

---

## 四、快速排查流程图

### 会员投诉流程

```
会员投诉
  │
  ├─ "我没借过" ───→ [B01] 查 borrow_order → borrow_detail → borrow_detail_image → borrow_log
  │
  ├─ "我还过了" ───→ [R01] 查 return_detail → borrow_detail → return_log → borrow_log (trace_id)
  │
  ├─ "数量不对" ───→ [R02] 查 return_detail SUM → borrow_detail.borrow_qty → borrow_log
  │
  ├─ "钱不对"  ───→ [P02]/[F01] 查 dd_purchase_order → purchase_log
  │
  └─ "谁操作的" ───→ [O01] 查三张log表 UNION ALL → staff_name + client_ip

  ↓ 登记处理结果 → 如需修复 → [4.10]数据修复流程
```

### 库存对账流程

```
库存异常
  │
  ├─ book_info.stock_qty != 盘点数
  │     │
  │     └─→ [S02] 导出当前库存 → 对比盘点 → 标记差异
  │
  ├─ book_info.lendable_qty < 0
  │     │
  │     └─→ [S01] 查 book_info_history 时间线 → 对账计算
  │
  └─ 报表数据不准
        │
        └─→ [F02] 与收银系统交叉核对 → 检查退款/取消订单

  ↓ 确认差异原因 → book_info_history记录调整 → UPDATE book_info
```

### 数据修复流程

```
确定需要修复
  │
  ├─ 1. SELECT 当前数据 → 保存为 before_data
  ├─ 2. 分析影响范围（关联的order/detail）
  ├─ 3. 生成新 trace_id = UUID()
  ├─ 4. 执行 UPDATE
  ├─ 5. SELECT 修改后数据 → 保存为 after_data
  ├─ 6. INSERT 对应log表（trace_id, source_type=3, before/after）
  ├─ 7. 如果是库存修改 → INSERT book_info_history
  └─ 8. 记录修复原因到remark
```

---

## 五、常用排查SQL速查

### 5.1 按单号查完整链路

```sql
-- 借书单完整链路
SET @order_no = 'DY20260630143000123456';

-- 借书单+明细
SELECT * FROM book_borrow_order WHERE order_no = @order_no;
SELECT * FROM book_borrow_detail WHERE borrow_order_no = @order_no;

-- 还书记录
SELECT rd.* FROM book_return_detail rd
INNER JOIN book_borrow_detail bd ON rd.borrow_detail_id = bd.id
WHERE bd.borrow_order_no = @order_no;

-- 购书记录
SELECT * FROM dd_book_purchase_order WHERE borrow_order_no = @order_no;

-- 所有日志（时间线合并）
SELECT '借书日志' AS 来源, log_type, staff_name, client_ip, created_at
FROM book_borrow_log WHERE borrow_order_no = @order_no
UNION ALL
SELECT '还书日志', log_type, staff_name, client_ip, created_at
FROM book_return_log WHERE borrow_order_no = @order_no
UNION ALL
SELECT '购书日志', log_type, staff_name, client_ip, created_at
FROM book_purchase_log WHERE borrow_order_no = @order_no
ORDER BY created_at;
```

### 5.2 按会员查所有未完结借阅

```sql
SELECT bo.order_no, bo.borrow_time, bo.expected_return_time,
       bd.book_name, bd.borrow_qty, bd.returned_qty, bd.purchase_qty,
       bd.borrow_qty - bd.returned_qty - bd.purchase_qty AS 未处理数量,
       CASE bd.borrow_status
           WHEN 1 THEN '借阅中' WHEN 3 THEN '部分归还' WHEN 4 THEN '部分转购'
       END AS 状态,
       DATEDIFF(NOW(), bo.expected_return_time) AS 超期天数
FROM book_borrow_detail bd
INNER JOIN book_borrow_order bo ON bd.borrow_order_id = bo.id
WHERE bd.member_id = ? AND bd.borrow_status IN (1, 3, 4) AND bd.is_del = 0
ORDER BY bo.borrow_time DESC;
```

### 5.3 按单号查所有操作人员

```sql
SELECT '创建借书单' AS 操作, first_staff_name AS 员工, borrow_time AS 时间
FROM book_borrow_order WHERE order_no = ?
UNION ALL
SELECT '还书操作', staff_name, return_time
FROM book_return_detail WHERE borrow_order_no = ? AND is_del = 0
UNION ALL
SELECT '创建购书单', create_staff_name, created_at
FROM dd_book_purchase_order WHERE borrow_order_no = ? AND is_del = 0
ORDER BY 时间;
```

### 5.4 图书库存对账

```sql
SELECT bi.id, bi.book_name, bi.stock_qty AS 当前库存, bi.lendable_qty AS 可借,
       COALESCE(SUM(bd.borrow_qty), 0) AS 累计借出,
       COALESCE(SUM(bd.returned_qty), 0) AS 累计归还,
       COALESCE(SUM(bd.purchase_qty), 0) AS 累计转购,
       bi.stock_qty + COALESCE(SUM(bd.borrow_qty), 0)
         - COALESCE(SUM(bd.returned_qty), 0)
         - COALESCE(SUM(bd.purchase_qty), 0) AS 推算初始库存
FROM book_info bi
LEFT JOIN book_borrow_detail bd ON bi.id = bd.book_id AND bd.is_del = 0
WHERE bi.id = ? AND bi.is_del = 0
GROUP BY bi.id;
```

---

## 六、数量闭环校验

### 核心公式

```
borrow_qty = returned_qty + purchase_qty + 未处理
```

### 三层验证

| 层级 | 检查内容 | 不一致原因 |
|------|---------|-----------|
| ① 派生字段 vs 来源表 | `borrow_detail.returned_qty` = `SUM(return_detail.return_qty)` | 还书操作漏更新borrow_detail |
| ② 状态 vs 数量 | `borrow_status` 与实际处理量匹配 | 状态未随数量更新 |
| ③ 边界约束 | `returned_qty + purchase_qty <= borrow_qty` | 并发冲突 / 手动修改错误 |

### 校验场景

| 编号 | 问题现象 | 可能原因 | 涉及业务表 | 涉及日志表 | 处置步骤 |
|------|---------|---------|-----------|-----------|---------|
| C01 | returned_qty < SUM(return_detail) | 还书操作只INSERT了return_detail，没UPDATE borrow_detail | borrow_detail、return_detail | borrow_log、return_log | ①执行6.1SQL定位差异 ②查return_log找遗漏的UPDATE ③补UPDATE borrow_detail ④补borrow_log记录修复 |
| C02 | returned_qty > SUM(return_detail) | return_detail被软删除但returned_qty未扣减；或手动修改了returned_qty | borrow_detail、return_detail | borrow_log | ①查is_del=1的return_detail ②查borrow_log的source_type=3(手动修改)记录 ③回退或补录 |
| C03 | purchase_qty < SUM(purchase_order) | 借转购操作漏UPDATE borrow_detail | borrow_detail、dd_purchase_order | purchase_log、borrow_log | ①查purchase_log确认付款操作 ②查borrow_log有无source_type=2记录 ③补UPDATE+补日志 |
| C04 | purchase_qty > SUM(purchase_order WHERE paid) | 购书订单取消/退款但purchase_qty未回退 | borrow_detail、dd_purchase_order | purchase_log | ①查order_status=2/3的purchase_order ②确认是否应回退purchase_qty ③修复并记录 |
| C05 | returned_qty + purchase_qty > borrow_qty | 并发超量处理 或 手动修改错误 | borrow_detail、return_detail、dd_purchase_order | 三张log表 | ①执行6.7SQL定位 ②查borrow_log按时间线排序看before/after ③修复最大值为borrow_qty |
| C06 | status=2(已全部归还)但returned_qty < borrow_qty | 状态更新为2但数量未同步 | borrow_detail | borrow_log | ①查borrow_log的log_type=5看状态变更 ②对比before_data.returned_qty和after_data ③修复状态或数量 |
| C07 | total_book_count != SUM(明细.borrow_qty) | 借书单总数与明细汇总不一致 | borrow_order、borrow_detail | borrow_log | ①执行6.4SQL ②查borrow_log看修改记录 ③以明细汇总为准修复total_book_count |
| C08 | 超期天数计算异常 | expected_return_time为NULL或被修改 | borrow_order | borrow_log | ①查borrow_order.expected_return_time ②查borrow_log的log_type=2看修改 ③补设或修正预计归还时间 |

### 闭环校验SQL（每日自动执行）

```sql
-- 6.1 派生字段 vs 来源表
SELECT bd.id, bd.borrow_qty, bd.returned_qty,
       COALESCE(SUM(rd.return_qty),0) AS 实际还书,
       bd.purchase_qty,
       COALESCE(SUM(CASE WHEN dpo.order_status=1 THEN dpo.qty ELSE 0 END),0) AS 实际转购,
       bd.returned_qty - COALESCE(SUM(rd.return_qty),0) AS 还书差异,
       bd.purchase_qty - COALESCE(SUM(CASE WHEN dpo.order_status=1 THEN dpo.qty ELSE 0 END),0) AS 转购差异,
       CASE WHEN bd.returned_qty=COALESCE(SUM(rd.return_qty),0)
             AND bd.purchase_qty=COALESCE(SUM(CASE WHEN dpo.order_status=1 THEN dpo.qty ELSE 0 END),0)
            THEN '✅' ELSE '❌' END AS 闭环
FROM book_borrow_detail bd
LEFT JOIN book_return_detail rd ON bd.id=rd.borrow_detail_id AND rd.is_del=0
LEFT JOIN dd_book_purchase_order dpo ON bd.id=dpo.borrow_detail_id AND dpo.is_del=0
WHERE bd.is_del=0
GROUP BY bd.id
HAVING 还书差异!=0 OR 转购差异!=0;

-- 6.3 状态一致性
SELECT bd.id, bd.borrow_status, bd.borrow_qty, bd.returned_qty, bd.purchase_qty
FROM book_borrow_detail bd WHERE bd.is_del=0
AND ((bd.borrow_status=1 AND bd.returned_qty+bd.purchase_qty>0)
  OR (bd.borrow_status=2 AND (bd.returned_qty<bd.borrow_qty OR bd.purchase_qty>0))
  OR (bd.borrow_status=5 AND bd.purchase_qty<bd.borrow_qty)
  OR (bd.borrow_status IN(1,3,4) AND bd.returned_qty+bd.purchase_qty>=bd.borrow_qty));

-- 6.7 边界约束
SELECT bd.id, bd.borrow_qty,
       COALESCE(SUM(rd.return_qty),0)+COALESCE(SUM(CASE WHEN dpo.order_status=1 THEN dpo.qty ELSE 0 END),0) AS 已处理
FROM book_borrow_detail bd
LEFT JOIN book_return_detail rd ON bd.id=rd.borrow_detail_id AND rd.is_del=0
LEFT JOIN dd_book_purchase_order dpo ON bd.id=dpo.borrow_detail_id AND dpo.is_del=0
WHERE bd.is_del=0
GROUP BY bd.id
HAVING 已处理 > bd.borrow_qty;
```

### 数量闭环追踪图

```
借书 DY001, borrow_qty=3
 │
 │  book_borrow_log (log_type=4, after_data={borrow_qty:3})
 │
 ├─ 还书 HS001, return_qty=1
 │   │ book_return_log (trace_id=X, 创建)
 │   │ book_borrow_log (trace_id=X, log_type=5-还书触发,
 │   │   before={returned_qty:0}, after={returned_qty:1})
 │   └─ returned_qty: 0→1
 │
 ├─ 还书 HS002, return_qty=1
 │   │ book_return_log (trace_id=Y)
 │   │ book_borrow_log (trace_id=Y, log_type=5,
 │   │   before={returned_qty:1}, after={returned_qty:2})
 │   └─ returned_qty: 1→2
 │
 └─ 借转购 JSDD001, qty=1
     │ book_purchase_log (trace_id=Z, 创建)
     │ book_borrow_log (trace_id=Z, log_type=6-转购触发,
     │   before={purchase_qty:0}, after={purchase_qty:1})
     └─ purchase_qty: 0→1

 闭环: 3 = 2 + 1 + 0 ✅
 日志: 每条数量变化都有 before→after 可追溯
```

---

## 七、问题登记模板

每次处理完问题后，建议在日志系统或工单中记录：

| 字段 | 说明 | 示例 |
|------|------|------|
| 问题单号 | 唯一编号 | INC-20260630-001 |
| 发现时间 | 问题报告时间 | 2026-06-30 14:30 |
| 涉及单号 | DY/HS/JSDD | DY20260630143000123456 |
| 问题分类 | B/R/P/S/O/F/I/M/T | R01-还书数量不对 |
| 根因 | 排查后确认的原因 | 还书时网络超时，前端重试导致重复 |
| 涉及表 | 哪些表的数据需要关注 | return_detail, borrow_detail |
| 修复方式 | 直接UPDATE/软删除/补录 | UPDATE borrow_detail SET returned_qty=2 |
| trace_id | 修复操作的trace_id | uuid-xxx |
| 处理人 | 谁处理的 | 张三 |
| 处理时间 | 何时处理完 | 2026-06-30 15:00 |
| 是否需要代码修复 | Y/N | N |
