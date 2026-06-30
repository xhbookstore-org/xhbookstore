# 新华书店小程序 API 接口文档

> 版本：v1.0 | 基础路径：`http://localhost:8091/api/mp/v1` | 更新时间：2026-06-30

---

## 通用说明

### 响应格式

所有接口统一返回：

```json
{
  "code": 0,          // 0=成功, 400=参数错误, 401=未认证, 500=服务端错误, 其他见错误码表
  "message": "操作成功",
  "data": {},         // 业务数据，无数据时为 null
  "requestId": null   // 请求追踪ID（AOP日志自动生成）
}
```

### 认证方式

除白名单接口外，所有请求需携带 Token：

```
Authorization: Bearer <accessToken>
```

Token 有效期 30 天。**先调用登录接口获取 Token，再将其填入后续请求：**

```bash
# 第1步：获取 Token
TOKEN=$(curl -s -X POST http://localhost:8091/api/mp/v1/auth/wechat-phone-login \
  -H "Content-Type: application/json;charset=UTF-8" \
  -d '{"code":"test"}' | python3 -c "import sys,json; print(json.load(sys.stdin)['data']['accessToken'])")

echo "Token: $TOKEN"

# 第2步：后续请求都带上这个 Token
curl http://localhost:8091/api/mp/v1/staff/home \
  -H "Authorization: Bearer $TOKEN"
```

> **下文所有示例中的 `<token>` 均指第1步获取到的 `$TOKEN` 值，使用时直接替换即可。**

### 分页格式

```json
{
  "list": [],       // 当前页数据
  "pageNo": 1,      // 当前页码
  "pageSize": 20,   // 每页数量
  "total": 100,     // 总条数
  "hasMore": true   // 是否还有下一页
}
```

### 错误码

| code | 说明 |
|------|------|
| 0 | 操作成功 |
| 400 | 参数无效 |
| 401 | 未登录或登录已过期 |
| 500 | 服务器内部错误 |
| 10004 | 无效的访问令牌 |
| 20001 | 会员不存在 |
| 30001 | 积分不足 |
| 30003 | 无效的积分事项 |
| 40001 | 不允许办理借阅 |
| 40002 | 至少需要一本图书 |
| 40004 | 不允许办理还书 |
| 50001 | 文件上传失败 |
| 50002 | 文件大小超出限制 |
| 50003 | 不支持的文件类型 |

---

## 一、认证接口

### 1.1 微信手机号登录

> 白名单接口，无需Token。当前为Mock实现，后续对接微信 `getPhoneNumber`。

```
POST /api/mp/v1/auth/wechat-phone-login
```

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `code` | string | 是 | 微信手机号授权组件返回的手机号获取凭证 |

**响应 `data`**

| 字段 | 类型 | 说明 |
|------|------|------|
| `accessToken` | string | 访问令牌，后续请求携带 |
| `expiresIn` | number | 有效秒数，2592000（30天） |
| `isStaff` | boolean | 是否员工身份 |
| `userId` | string | 账号编号 |
| `memberId` | string\|null | 关联会员ID（如已绑定） |

**调用示例**

```bash
curl -X POST http://localhost:8091/api/mp/v1/auth/wechat-phone-login \
  -H "Content-Type: application/json;charset=UTF-8" \
  -d '{"code":"wechat_phone_code_xxx"}'
```

**响应示例**

```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "expiresIn": 2592000,
    "isStaff": false,
    "userId": "a4c0f8dc-d36a-484e-a218-a5ee3f3018c4",
    "memberId": null
  },
  "requestId": null
}
```

---

### 1.2 校验登录态

> 白名单接口，无Token时可调用。小程序启动时检查登录是否有效。

```
GET /api/mp/v1/auth/session
```

**响应 `data`**

| 字段 | 类型 | 说明 |
|------|------|------|
| `valid` | boolean | 登录态是否有效 |
| `isStaff` | boolean\|null | 有效时返回 |
| `userId` | string\|null | 有效时返回 |

**调用示例**

```bash
# 带Token
curl http://localhost:8091/api/mp/v1/auth/session \
  -H "Authorization: Bearer <token>"

# 不带Token（白名单）
curl http://localhost:8091/api/mp/v1/auth/session
```

**响应示例**

```json
{
  "code": 0,
  "message": "操作成功",
  "data": { "valid": true, "isStaff": false, "userId": "xxx" },
  "requestId": null
}
```

---

### 1.3 退出登录

```
POST /api/mp/v1/auth/logout
Authorization: Bearer <token>
```

**响应 `data`**

| 字段 | 类型 | 说明 |
|------|------|------|
| `success` | boolean | 是否退出成功 |

**调用示例**

```bash
curl -X POST http://localhost:8091/api/mp/v1/auth/logout \
  -H "Authorization: Bearer <token>"
```

**响应示例**

```json
{
  "code": 0,
  "message": "操作成功",
  "data": { "success": true },
  "requestId": null
}
```

---

## 二、账号接口

### 2.1 查询注销前置状态

```
GET /api/mp/v1/account/cancel-eligibility
Authorization: Bearer <token>
```

**响应 `data`**

| 字段 | 类型 | 说明 |
|------|------|------|
| `canCancel` | boolean | 是否可注销 |
| `blockedReason` | string\|null | 不可注销原因 |
| `hasUnreturnedBooks` | boolean | 是否有未还图书 |
| `isStaffActive` | boolean | 员工身份是否有效 |
| `currentPoints` | number | 当前积分 |

**调用示例**

```bash
curl http://localhost:8091/api/mp/v1/account/cancel-eligibility \
  -H "Authorization: Bearer <token>"
```

---

### 2.2 注销账号

```
POST /api/mp/v1/account/cancel
Authorization: Bearer <token>
```

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `confirmed` | boolean | 是 | 用户确认注销 |

**响应 `data`**

| 字段 | 类型 | 说明 |
|------|------|------|
| `success` | boolean | 是否注销成功 |
| `cancelledAt` | number | 注销时间戳(毫秒) |

**调用示例**

```bash
curl -X POST http://localhost:8091/api/mp/v1/account/cancel \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json;charset=UTF-8" \
  -d '{"confirmed":true}'
```

---

## 三、文件上传接口

### 3.1 上传图书附件图片

> 上传到腾讯云COS，同时写入 book_image 或 book_borrow_detail_image 表。

```
POST /api/mp/v1/files/book-attachment-images
Content-Type: multipart/form-data
Authorization: Bearer <token>
```

**请求参数 (FormData)**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `file` | File | 是 | 图片文件，最大10MB，仅限 image/* |
| `memberId` | string | 是 | 会员ID |
| `borrowOrderNo` | string | 否 | 借书单号，传入则写入 borrow_detail_image |
| `borrowDetailId` | number | 否 | 借书明细ID |
| `borrowOrderId` | number | 否 | 借书单ID |
| `bookId` | number | 否 | 图书ID，传入则写入 book_image |
| `imageType` | number | 否 | 1=借书拍摄 2=还书拍摄 3=损坏记录，默认1 |

**响应 `data`**

| 字段 | 类型 | 说明 |
|------|------|------|
| `imageId` | string | 图片唯一标识(UUID) |
| `url` | string | COS原始图片地址 |
| `thumbUrl` | string | 缩略图地址 (400x) |
| `size` | number | 文件大小(byte) |
| `fileName` | string | 原始文件名 |

**调用示例**

```bash
curl -X POST http://localhost:8091/api/mp/v1/files/book-attachment-images \
  -H "Authorization: Bearer <token>" \
  -F "file=@/path/to/photo.jpg" \
  -F "memberId=2" \
  -F "borrowOrderNo=DY20260630095251323627" \
  -F "borrowDetailId=1" \
  -F "imageType=1"
```

**响应示例**

```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "imageId": "a1b2c3d4e5f6",
    "url": "https://xhbookstore-xxx.cos.ap-guangzhou.myqcloud.com/bookstore/2/a1b2c3d4e5f6.jpg",
    "thumbUrl": "...?imageMogr2/thumbnail/400x",
    "size": 123456,
    "fileName": "photo.jpg"
  },
  "requestId": null
}
```

---

## 四、用户端接口

### 4.1 查询用户首页

```
GET /api/mp/v1/user/home
Authorization: Bearer <token>
```

**响应 `data`**

| 字段 | 类型 | 说明 |
|------|------|------|
| `phoneDisplay` | string | 脱敏手机号 |
| `member` | object | 会员概要 |
| `member.memberId` | string | 会员ID |
| `member.memberNo` | string | 会员编号 |
| `member.memberName` | string | 会员姓名 |
| `member.phoneDisplay` | string | 脱敏手机号 |
| `member.currentPoints` | number | 当前积分 |
| `member.currentBorrowingCount` | number | 当前借阅中数量 |
| `member.yearBorrowCount` | number | 年度借阅量 |

**调用示例**

```bash
curl http://localhost:8091/api/mp/v1/user/home \
  -H "Authorization: Bearer <token>"
```

---

### 4.2 生成动态会员码

```
POST /api/mp/v1/user/member-code
Authorization: Bearer <token>
```

**响应 `data`**

| 字段 | 类型 | 说明 |
|------|------|------|
| `memberNo` | string | 会员编号 |
| `codeId` | string | 动态码编号(UUID) |
| `codeContent` | string | 二维码载荷 |
| `expiresAt` | number | 过期时间戳(毫秒) |
| `ttlSeconds` | number | 剩余有效秒数(30秒) |

**调用示例**

```bash
curl -X POST http://localhost:8091/api/mp/v1/user/member-code \
  -H "Authorization: Bearer <token>"
```

---

### 4.3 查询本人借阅记录

```
GET /api/mp/v1/user/borrows?pageNo=1&pageSize=20
Authorization: Bearer <token>
```

**请求参数**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `pageNo` | number | 否 | 页码，默认1 |
| `pageSize` | number | 否 | 每页数量，默认20 |

**响应 `data`**

| 字段 | 类型 | 说明 |
|------|------|------|
| `memberDisplay` | string | 会员展示名 |
| `yearBorrowCount` | number | 年度借阅量 |
| `currentBorrowingCount` | number | 当前借阅中数量 |
| `page` | Page | 借阅记录分页 |
| `page.list[].orderNo` | string | 借书单号(DY开头) |
| `page.list[].totalBookCount` | number | 借书总数 |
| `page.list[].borrowStatus` | number | 1=借阅中 2=已归还 3=部分归还 4=部分转购 5=全部转购 |
| `page.list[].borrowTime` | number | 借书时间戳 |
| `page.list[].details` | array | 借书明细列表 |

**调用示例**

```bash
curl "http://localhost:8091/api/mp/v1/user/borrows?pageNo=1&pageSize=10" \
  -H "Authorization: Bearer <token>"
```

---

### 4.4 查询借阅详情

```
GET /api/mp/v1/user/borrows/{borrowId}
Authorization: Bearer <token>
```

**路径参数**

| 字段 | 类型 | 说明 |
|------|------|------|
| `borrowId` | string | 借书单号(DY开头) |

**响应 `data`**

| 字段 | 类型 | 说明 |
|------|------|------|
| `order` | object | 借书单信息 |
| `details` | array | 借书明细列表 |
| `returns` | array | 还书记录列表 |

**调用示例**

```bash
curl http://localhost:8091/api/mp/v1/user/borrows/DY20260630095251323627 \
  -H "Authorization: Bearer <token>"
```

---

### 4.5 查询本人积分记录

```
GET /api/mp/v1/user/points-records?pageNo=1&pageSize=20
Authorization: Bearer <token>
```

**响应 `data`**

| 字段 | 类型 | 说明 |
|------|------|------|
| `memberDisplay` | string | 会员展示名 |
| `currentPoints` | number | 当前总积分 |
| `yearEarnedPoints` | number | 年度累计获得 |
| `page.list[].pointsRecordId` | string | 积分记录编号(IN/OT开头) |
| `page.list[].direction` | string | add=增加 deduct=消耗 |
| `page.list[].pointsDelta` | number | 积分变化值 |
| `page.list[].beforePoints` | number | 操作前积分 |
| `page.list[].afterPoints` | number | 操作后积分 |
| `page.list[].operatedAt` | number | 操作时间戳 |

**调用示例**

```bash
curl "http://localhost:8091/api/mp/v1/user/points-records?pageNo=1&pageSize=10" \
  -H "Authorization: Bearer <token>"
```

---

### 4.6 查询积分详情

```
GET /api/mp/v1/user/points-records/{pointsRecordId}
Authorization: Bearer <token>
```

**路径参数**

| 字段 | 类型 | 说明 |
|------|------|------|
| `pointsRecordId` | string | 积分记录编号(IN开头) |

**调用示例**

```bash
curl http://localhost:8091/api/mp/v1/user/points-records/IN20260628120000123456 \
  -H "Authorization: Bearer <token>"
```

---

## 五、员工端接口

### 5.1 查询员工首页

```
GET /api/mp/v1/staff/home
Authorization: Bearer <token>
```

**响应 `data`**

| 字段 | 类型 | 说明 |
|------|------|------|
| `storeName` | string | 所属门店名称 |
| `todayStoreBorrowCount` | number | 本店今日借阅量 |
| `todayStaffBorrowCount` | number | 本人今日借阅量 |

**调用示例**

```bash
curl http://localhost:8091/api/mp/v1/staff/home \
  -H "Authorization: Bearer <token>"
```

---

### 5.2 解析会员码

```
POST /api/mp/v1/staff/member-code/scan
Authorization: Bearer <token>
```

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `scanResult` | string | 是 | 微信 wx.scanCode 返回的原始内容 |
| `scanType` | string | 否 | 码类型（QR_CODE等） |

**响应 `data`**

| 字段 | 类型 | 说明 |
|------|------|------|
| `memberId` | string | 解析出的会员ID |

**调用示例**

```bash
curl -X POST http://localhost:8091/api/mp/v1/staff/member-code/scan \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json;charset=UTF-8" \
  -d '{"scanResult":"MEMBER:65000000001:TIMESTAMP:1700000000000","scanType":"QR_CODE"}'
```

---

### 5.3 查询扫码会员概要

```
GET /api/mp/v1/staff/members/{memberId}/overview
Authorization: Bearer <token>
```

**路径参数**

| 字段 | 类型 | 说明 |
|------|------|------|
| `memberId` | string | 会员ID |

**响应 `data`**

| 字段 | 类型 | 说明 |
|------|------|------|
| `member` | MemberProfile | 会员概要（同4.1 member结构 + card信息） |
| `member.card` | object | 会员卡信息（memberNo/cardName/cardStatus/expiredAt等） |
| `availability` | OperationAvailability | 可执行操作状态 |
| `availability.canBorrow` | boolean | 是否可借阅 |
| `availability.canReturn` | boolean | 是否可还书 |
| `availability.canAddPoints` | boolean | 是否可增加积分 |
| `availability.canDeductPoints` | boolean | 是否可消耗积分 |
| `availability.maxAddPoints` | number | 单次最大增加积分 |

**调用示例**

```bash
curl http://localhost:8091/api/mp/v1/staff/members/2/overview \
  -H "Authorization: Bearer <token>"
```

---

### 5.4 办理借阅 ★

> 事务保证：验会员 → 生成DY单号 → INSERT order → INSERT details → 任一步失败全部回滚

```
POST /api/mp/v1/staff/members/{memberId}/borrows
Authorization: Bearer <token>
```

**路径参数**

| 字段 | 类型 | 说明 |
|------|------|------|
| `memberId` | string | 会员ID |

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `books` | array | 是 | 借阅图书列表，至少1本 |
| `books[].bookName` | string | 是 | 图书名称 |
| `books[].borrowQty` | number | 否 | 借书数量，默认1 |
| `books[].bookId` | number | 否 | 图书ID（如已录入book_info表） |
| `remark` | string | 否 | 备注 |

**响应 `data`**

| 字段 | 类型 | 说明 |
|------|------|------|
| `orderNo` | string | 借书单号(DY开头) |
| `totalBookCount` | number | 借书总数 |
| `detailCount` | number | 明细条数 |

**调用示例**

```bash
curl -X POST http://localhost:8091/api/mp/v1/staff/members/2/borrows \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json;charset=UTF-8" \
  -d '{
    "books": [
      {"bookName": "红楼梦", "borrowQty": 2},
      {"bookName": "西游记", "borrowQty": 1}
    ],
    "remark": "借书测试"
  }'
```

**响应示例**

```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "orderNo": "DY20260630102919699538",
    "totalBookCount": 3,
    "detailCount": 2
  },
  "requestId": null
}
```

---

### 5.5 办理还书 ★

> 事务保证：验借书单 → 验可还数量 → INSERT return_detail → UPDATE detail → UPDATE order

```
POST /api/mp/v1/staff/borrow-returns
Authorization: Bearer <token>
```

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `borrowOrderNo` | string | 是 | 借书单号(DY开头) |
| `returnItems` | array | 是 | 还书项列表 |
| `returnItems[].borrowDetailId` | number | 是 | 借书明细ID |
| `returnItems[].returnQty` | number | 是 | 本次还书数量 |
| `returnItems[].returnType` | number | 否 | 1=正常 2=损坏 3=遗失，默认1 |
| `returnItems[].remark` | string | 否 | 备注 |

**响应 `data`**

| 字段 | 类型 | 说明 |
|------|------|------|
| `returnOrderNos` | array | 还书单号列表(HS开头) |
| `totalReturned` | number | 本次还书总数 |

**调用示例**

```bash
curl -X POST http://localhost:8091/api/mp/v1/staff/borrow-returns \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json;charset=UTF-8" \
  -d '{
    "borrowOrderNo": "DY20260630095251323627",
    "returnItems": [
      {"borrowDetailId": 1, "returnQty": 1, "returnType": 1, "remark": "正常还书"}
    ]
  }'
```

**响应示例**

```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "returnOrderNos": ["HS20260630095251372444"],
    "totalReturned": 1
  },
  "requestId": null
}
```

---

### 5.6 查询借阅详情

```
GET /api/mp/v1/staff/borrows/{borrowId}
Authorization: Bearer <token>
```

**路径参数**

| 字段 | 类型 | 说明 |
|------|------|------|
| `borrowId` | string | 借书单号(DY开头) |

**响应 `data`**

| 字段 | 类型 | 说明 |
|------|------|------|
| `order` | object | 借书单（含 memberCardNo/memberName/borrowStatus 等） |
| `details` | array | 借书明细（含 returnedQty/purchaseQty/borrowStatus） |
| `returns` | array | 还书记录（含 returnOrderNo(HS)/returnQty/returnTime） |

**调用示例**

```bash
curl http://localhost:8091/api/mp/v1/staff/borrows/DY20260630095251323627 \
  -H "Authorization: Bearer <token>"
```

---

### 5.7 查询全市借阅列表

```
GET /api/mp/v1/staff/borrows?phone=&status=&pageNo=1&pageSize=20
Authorization: Bearer <token>
```

**请求参数**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `phone` | string | 否 | 手机号搜索 |
| `status` | number | 否 | 借阅状态筛选 |
| `pageNo` | number | 否 | 页码 |
| `pageSize` | number | 否 | 每页数量 |

---

### 5.8 查询指定会员借阅记录

```
GET /api/mp/v1/staff/members/{memberId}/borrows?pageNo=1&pageSize=20
Authorization: Bearer <token>
```

**路径参数**

| 字段 | 类型 | 说明 |
|------|------|------|
| `memberId` | string | 会员ID |

**响应 `data`**

| 字段 | 类型 | 说明 |
|------|------|------|
| `member` | object | 会员概要 |
| `page.list[].orderNo` | string | 借书单号 |
| `page.list[].totalBookCount` | number | 借书总数 |
| `page.list[].borrowStatus` | number | 借阅状态 |
| `page.list[].borrowTime` | number | 借书时间戳 |
| `page.list[].details` | array | 借书明细 |

**调用示例**

```bash
curl "http://localhost:8091/api/mp/v1/staff/members/2/borrows?pageNo=1&pageSize=10" \
  -H "Authorization: Bearer <token>"
```

---

### 5.9 查询积分事项

```
GET /api/mp/v1/staff/points-reasons?direction=add&memberId=2
Authorization: Bearer <token>
```

**请求参数**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `direction` | string | 是 | add=增加 deduct=消耗 |
| `memberId` | string | 否 | 会员ID |

**响应 `data`**

| 字段 | 类型 | 说明 |
|------|------|------|
| `list` | array | 事项列表 |
| `list[].reasonId` | string | 事项编号 |
| `list[].reasonName` | string | 事项名称 |
| `list[].enabled` | boolean | 是否启用 |
| `list[].defaultPoints` | number | 默认积分值 |
| `maxPoints` | number | 单次最大操作积分 |

**调用示例**

```bash
curl "http://localhost:8091/api/mp/v1/staff/points-reasons?direction=add" \
  -H "Authorization: Bearer <token>"
```

---

### 5.10 增加积分 ★

> 悲观锁 + 事务：`SELECT FOR UPDATE` → 记操作前后积分 → INSERT order → INSERT into_bill → UPDATE member

```
POST /api/mp/v1/staff/members/{memberId}/points/add
Authorization: Bearer <token>
```

**路径参数**

| 字段 | 类型 | 说明 |
|------|------|------|
| `memberId` | string | 会员ID |

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `reasonId` | string | 是 | 积分事项编号 |
| `points` | number | 是 | 增加积分值，正整数 |
| `remark` | string | 否 | 备注 |

**响应 `data`**

| 字段 | 类型 | 说明 |
|------|------|------|
| `success` | boolean | 是否成功 |
| `pointsRecordId` | string | 积分记录编号(IN开头) |
| `beforePoints` | number | 操作前积分 |
| `pointsDelta` | number | 本次增加(正数) |
| `afterPoints` | number | 操作后积分 |
| `operatedAt` | number | 操作时间戳 |

**调用示例**

```bash
curl -X POST http://localhost:8091/api/mp/v1/staff/members/2/points/add \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json;charset=UTF-8" \
  -d '{"reasonId":"1","points":50,"remark":"活动赠送"}'
```

**响应示例**

```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "success": true,
    "pointsRecordId": "IN20260630120000123456",
    "beforePoints": 500,
    "pointsDelta": 50,
    "afterPoints": 550,
    "operatedAt": 1782785568000
  },
  "requestId": null
}
```

---

### 5.11 消耗积分

```
POST /api/mp/v1/staff/members/{memberId}/points/deduct
Authorization: Bearer <token>
```

**路径参数**

| 字段 | 类型 | 说明 |
|------|------|------|
| `memberId` | string | 会员ID |

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `reasonId` | string | 是 | 积分事项编号 |
| `points` | number | 是 | 消耗积分值，正整数，不得超过当前积分 |
| `remark` | string | 否 | 备注 |

**响应 `data`** — 同 5.10，`pointsDelta` 为负数

**调用示例**

```bash
curl -X POST http://localhost:8091/api/mp/v1/staff/members/2/points/deduct \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json;charset=UTF-8" \
  -d '{"reasonId":"2","points":30,"remark":"兑换礼品"}'
```

---

### 5.12 查询全市积分列表

```
GET /api/mp/v1/staff/points-records?phone=&memberId=&direction=&pageNo=1&pageSize=20
Authorization: Bearer <token>
```

**请求参数**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `phone` | string | 否 | 手机号搜索 |
| `memberId` | string | 否 | 指定会员 |
| `direction` | string | 否 | add/deduct |
| `pageNo` | number | 否 | 页码 |
| `pageSize` | number | 否 | 每页数量 |

**调用示例**

```bash
curl "http://localhost:8091/api/mp/v1/staff/points-records?memberId=2&pageNo=1" \
  -H "Authorization: Bearer <token>"
```

---

### 5.13 查询积分详情

```
GET /api/mp/v1/staff/points-records/{pointsRecordId}
Authorization: Bearer <token>
```

---

## 六、接口汇总

| 编号 | 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|------|
| 1.1 | POST | /auth/wechat-phone-login | 否 | 微信登录 |
| 1.2 | GET | /auth/session | 否 | 校验登录态 |
| 1.3 | POST | /auth/logout | 是 | 退出登录 |
| 2.1 | GET | /account/cancel-eligibility | 是 | 注销前置状态 |
| 2.2 | POST | /account/cancel | 是 | 注销账号 |
| 3.1 | POST | /files/book-attachment-images | 是 | 上传图片(COS) |
| 4.1 | GET | /user/home | 是 | 用户首页 |
| 4.2 | POST | /user/member-code | 是 | 动态会员码 |
| 4.3 | GET | /user/borrows | 是 | 我的借阅 |
| 4.4 | GET | /user/borrows/{id} | 是 | 借阅详情 |
| 4.5 | GET | /user/points-records | 是 | 我的积分 |
| 4.6 | GET | /user/points-records/{id} | 是 | 积分详情 |
| 5.1 | GET | /staff/home | 是 | 员工首页 |
| 5.2 | POST | /staff/member-code/scan | 是 | 扫码解析 |
| 5.3 | GET | /staff/members/{id}/overview | 是 | 会员概要 |
| 5.4 | POST | /staff/members/{id}/borrows | 是 | ★ 办理借阅 |
| 5.5 | POST | /staff/borrow-returns | 是 | ★ 办理还书 |
| 5.6 | GET | /staff/borrows/{id} | 是 | 借阅详情 |
| 5.7 | GET | /staff/borrows | 是 | 全市借阅列表 |
| 5.8 | GET | /staff/members/{id}/borrows | 是 | 会员借阅记录 |
| 5.9 | GET | /staff/points-reasons | 是 | 积分事项 |
| 5.10 | POST | /staff/members/{id}/points/add | 是 | ★ 增加积分 |
| 5.11 | POST | /staff/members/{id}/points/deduct | 是 | 消耗积分 |
| 5.12 | GET | /staff/points-records | 是 | 积分列表 |
| 5.13 | GET | /staff/points-records/{id} | 是 | 积分详情 |

**共 25 个接口** | ★ = 含事务 | 认证 = Bearer Token (除白名单外)
