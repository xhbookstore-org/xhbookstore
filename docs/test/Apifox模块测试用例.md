# Apifox 模块测试用例

> 适用范围：`xhbookstore-admin` 后台接口、`xhbookstore-api` 小程序接口。
> 建议环境：测试服务器。
> 更新时间：2026-07-05。

## 一、Apifox 环境变量

### 1. admin 后台环境

| 变量名 | 示例 | 说明 |
|---|---|---|
| `adminBaseUrl` | `http://152.136.127.168` | 后台接口域名；如直接测端口可填 `http://152.136.127.168:8090` |
| `adminToken` | 自动提取 | 后台登录 token |
| `captchaUuid` | 自动提取 | 验证码 uuid |
| `captchaCode` | 手动填写 | 验证码答案，测试时从页面或 Redis 获取 |
| `deptId` | `101` | 测试门店/部门 ID |
| `memberId` | `1` | 测试会员 ID |
| `memberCardId` | `1` | 测试会员卡 ID |
| `cardTypeId` | `1` | 年卡/半年卡类型 ID |

后台通用 Header：

```http
Authorization: Bearer {{adminToken}}
Content-Type: application/json
```

### 2. 小程序 API 环境

| 变量名 | 示例 | 说明 |
|---|---|---|
| `mpBaseUrl` | `http://152.136.127.168/api/mp/v1` | 小程序 API 基础地址 |
| `memberAccessToken` | 自动提取 | 会员登录 token |
| `staffAccessToken` | 自动提取 | 员工登录 token |
| `refreshToken` | 自动提取 | 刷新 token |
| `memberId` | `1` | 会员 ID |
| `detailId` | `1` | 借阅明细 ID |
| `bookId` | `1` | 图书 ID |
| `memberCodeToken` | 自动提取 | 用户动态会员码 token |
| `pointsRecordId` | `IN202607050001` | 积分流水编号 |

小程序通用 Header：

```http
Authorization: Bearer {{memberAccessToken}}
Content-Type: application/json
```

员工端接口把 `Authorization` 换成：

```http
Authorization: Bearer {{staffAccessToken}}
```

## 二、测试数据准备

| 数据 | 要求 |
|---|---|
| 管理员账号 | 可登录 admin，拥有系统、会员、会员卡权限 |
| 门店 | 至少 1 个正常状态门店；有效门店总数不能超过 20 个 |
| 员工账号 | 绑定系统用户，并能登录小程序员工端 |
| 会员 | 至少 1 个正常会员，绑定手机号或可通过测试登录返回 |
| 卡类型 | 至少有年卡、半年卡等系统内存在的卡类型 |
| 图书 | 至少 1 本可借图书，库存充足 |
| 积分事项 | 至少有增加、消耗积分可用事项 |

## 三、通用断言

### admin 后台接口

| 场景 | 断言 |
|---|---|
| 成功 | HTTP 状态为 `200`，响应 `code = 200` |
| 未登录 | 响应 `code = 401` 或提示认证失败 |
| 无权限 | 响应无权限提示，不应返回目标数据 |
| 列表 | 返回 `rows` 和 `total`，分页参数生效 |
| 写操作 | 返回 `code = 200`，操作日志应有记录 |

### 小程序 API

| 场景 | 断言 |
|---|---|
| 成功 | HTTP 状态为 `200`，业务成功码按接口返回规则判断 |
| 未登录 | 不允许访问受保护接口 |
| 员工接口会员 token 调用 | 应失败 |
| 会员接口员工 token 调用 | 应失败或只能返回员工兼容数据 |
| 写操作 | 业务表、日志表、积分/库存/订单数据闭环 |

## 四、admin 后台测试用例

### A01 后台登录

#### A01-01 获取验证码

```http
GET {{adminBaseUrl}}/captchaImage
```

断言：

- `code = 200`
- 返回 `uuid`
- 返回 `img`
- 提取 `uuid` 到 `captchaUuid`

#### A01-02 登录成功

```http
POST {{adminBaseUrl}}/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123",
  "code": "{{captchaCode}}",
  "uuid": "{{captchaUuid}}"
}
```

断言：

- `code = 200`
- 返回 `token`
- 提取 `token` 到 `adminToken`

#### A01-03 获取登录人信息

```http
GET {{adminBaseUrl}}/getInfo
Authorization: Bearer {{adminToken}}
```

断言：

- `code = 200`
- 返回 `user`
- 返回 `roles`、`permissions`

#### A01-04 获取路由

```http
GET {{adminBaseUrl}}/getRouters
Authorization: Bearer {{adminToken}}
```

断言：

- `code = 200`
- 返回菜单树
- 会员管理相关菜单可见

### A02 首页会员数据看板

#### A02-01 查询首页统计

```http
GET {{adminBaseUrl}}/dashboard/member/overview
Authorization: Bearer {{adminToken}}
```

断言：

- `code = 200`
- `data.stats.totalMembers` 存在
- `data.stats.borrowCardMembers` 存在
- `data.loginStats.totalLoginCount` 存在
- `data.visibleDeptCount >= 1`
- `data.refreshStatus = SUCCESS`

业务校验：

- 当前账号只能看到自己数据权限范围内的部门。
- `累计会员 = 借阅卡会员 + 普通会员`。
- 会员码展示量按会员所属门店纳入数据权限。
- 登录量当前按全局登录成功次数统计。

#### A02-02 手动刷新首页统计

```http
POST {{adminBaseUrl}}/dashboard/member/refresh
Authorization: Bearer {{adminToken}}
Content-Type: application/json

{}
```

断言：

- `code = 200` 或锁占用提示
- 再调用 A02-01，`refreshedAt` 更新或保持最近一次成功刷新时间

#### A02-03 未登录访问首页统计

```http
GET {{adminBaseUrl}}/dashboard/member/overview
```

断言：

- 返回未认证/无法访问

### A03 部门/门店管理

#### A03-01 查询部门列表

```http
GET {{adminBaseUrl}}/system/dept/list
Authorization: Bearer {{adminToken}}
```

断言：

- `code = 200`
- 返回部门数组
- 正常部门 `status = 0`

#### A03-02 新增正常门店

```http
POST {{adminBaseUrl}}/system/dept
Authorization: Bearer {{adminToken}}
Content-Type: application/json

{
  "parentId": 100,
  "deptName": "Apifox测试门店{{$timestamp}}",
  "orderNum": 99,
  "leader": "测试负责人",
  "phone": "13800000000",
  "email": "test@example.com",
  "status": "0"
}
```

断言：

- 正常门店数量小于 20 时，`code = 200`
- 正常门店数量已达到 20 时，应失败，提示“有效门店数量最多为20个”

#### A03-03 停用门店改为正常

```http
PUT {{adminBaseUrl}}/system/dept
Authorization: Bearer {{adminToken}}
Content-Type: application/json

{
  "deptId": "{{deptId}}",
  "parentId": 100,
  "deptName": "Apifox测试门店",
  "orderNum": 99,
  "status": "0"
}
```

断言：

- 如果当前有效门店已满 20 个，应失败。
- 如果未满 20 个，应成功。
- 已经正常的门店仅改名称/排序，不应被 20 个限制拦截。

### A04 用户/员工管理

#### A04-01 查询用户列表

```http
GET {{adminBaseUrl}}/system/user/list?pageNum=1&pageSize=10
Authorization: Bearer {{adminToken}}
```

断言：

- `code = 200`
- 返回 `rows`、`total`

#### A04-02 新增员工用户

```http
POST {{adminBaseUrl}}/system/user
Authorization: Bearer {{adminToken}}
Content-Type: application/json

{
  "deptId": "{{deptId}}",
  "userName": "staff{{$timestamp}}",
  "nickName": "Apifox员工",
  "password": "Admin123",
  "phonenumber": "139{{$timestamp}}",
  "email": "staff{{$timestamp}}@example.com",
  "sex": "0",
  "status": "0",
  "postIds": [],
  "roleIds": []
}
```

断言：

- `code = 200`
- 新用户可在列表查到
- 操作日志记录新增用户

#### A04-03 重置员工密码

```http
PUT {{adminBaseUrl}}/system/user/resetPwd
Authorization: Bearer {{adminToken}}
Content-Type: application/json

{
  "userId": 2,
  "password": "Admin123"
}
```

断言：

- `code = 200`
- 员工使用新密码可登录后台或小程序员工端，取决于业务配置

### A05 角色与数据权限

#### A05-01 查询角色列表

```http
GET {{adminBaseUrl}}/system/role/list?pageNum=1&pageSize=10
Authorization: Bearer {{adminToken}}
```

断言：

- `code = 200`
- 返回角色列表

#### A05-02 修改角色数据权限

```http
PUT {{adminBaseUrl}}/system/role/dataScope
Authorization: Bearer {{adminToken}}
Content-Type: application/json

{
  "roleId": 2,
  "dataScope": "2",
  "deptIds": [{{deptId}}]
}
```

断言：

- `code = 200`
- 使用该角色账号查询会员/首页统计，只能看到绑定门店数据

### A06 会员管理

#### A06-01 查询会员列表

```http
GET {{adminBaseUrl}}/member/list?pageNum=1&pageSize=10&cardNo=&name=&phone=&deptId={{deptId}}
Authorization: Bearer {{adminToken}}
```

断言：

- `code = 200`
- 返回 `rows`、`total`
- 默认排序为最近更新/创建在前
- 受数据权限限制

#### A06-02 查询会员详情

```http
GET {{adminBaseUrl}}/member/{{memberId}}
Authorization: Bearer {{adminToken}}
```

断言：

- `code = 200`
- 返回会员姓名、会员编号、门店、积分等信息

#### A06-03 生成会员编号

```http
GET {{adminBaseUrl}}/member/generateCardNo?deptId={{deptId}}
Authorization: Bearer {{adminToken}}
```

断言：

- `code = 200`
- 返回会员编号
- 编号按门店前缀递增

#### A06-04 新增会员

```http
POST {{adminBaseUrl}}/member
Authorization: Bearer {{adminToken}}
Content-Type: application/json

{
  "cardNo": "APX{{$timestamp}}",
  "name": "Apifox会员",
  "phone": "138{{$timestamp}}",
  "deptId": "{{deptId}}",
  "status": 0,
  "currentPoints": 0,
  "remark": "Apifox新增"
}
```

断言：

- `code = 200`
- 会员列表可查到
- `source`、操作人等字段符合业务规则

#### A06-05 编辑会员

```http
PUT {{adminBaseUrl}}/member/{{memberId}}
Authorization: Bearer {{adminToken}}
Content-Type: application/json

{
  "name": "Apifox会员-编辑",
  "phone": "13800000001",
  "deptId": "{{deptId}}",
  "remark": "Apifox编辑"
}
```

断言：

- `code = 200`
- 详情返回更新后的字段

#### A06-06 会员编号重复新增

```http
POST {{adminBaseUrl}}/member
Authorization: Bearer {{adminToken}}
Content-Type: application/json

{
  "cardNo": "已存在会员编号",
  "name": "重复会员",
  "phone": "13800000002",
  "deptId": "{{deptId}}",
  "status": 0
}
```

断言：

- 应失败
- 不应产生重复会员数据

#### A06-07 查询会员积分流水

```http
GET {{adminBaseUrl}}/member/{{memberId}}/points?pageNum=1&pageSize=10
Authorization: Bearer {{adminToken}}
```

断言：

- `code = 200`
- 返回积分流水列表

#### A06-08 后台增加会员积分

```http
POST {{adminBaseUrl}}/member/{{memberId}}/points
Authorization: Bearer {{adminToken}}
Content-Type: application/json

{
  "points": 20,
  "reason": "Apifox测试加分",
  "remark": "Apifox"
}
```

断言：

- `code = 200`
- 会员当前积分增加 20
- 积分流水有入账记录
- 操作日志有记录

#### A06-09 导入会员 Excel

```http
POST {{adminBaseUrl}}/member/importData
Authorization: Bearer {{adminToken}}
Content-Type: multipart/form-data

file: 现有会员明细.xls
deptId: {{deptId}}
```

断言：

- `code = 200`
- 返回总数、成功数、失败数、未导入数
- 会员编号/会员卡号已存在：归入“未导入”，不算失败、不算成功
- Excel 卡类型系统不存在：归入失败，不落会员、不落会员卡、不生成订单
- 导入日志和明细日志均入库

#### A06-10 导出会员 Excel

```http
POST {{adminBaseUrl}}/member/export
Authorization: Bearer {{adminToken}}
Content-Type: application/x-www-form-urlencoded

deptId={{deptId}}&name=&phone=&cardNo=
```

断言：

- 返回 Excel 文件
- 导出当前查询条件下全量数据，不受分页影响
- 文件名格式：`门店名会员_yyyyMMdd_HHmmss.xlsx`；无门店时 `全部会员_yyyyMMdd_HHmmss.xlsx`
- 字段格式与导入 Excel 一致

### A07 会员卡类型管理

#### A07-01 查询卡类型列表

```http
GET {{adminBaseUrl}}/member/card-type/list?pageNum=1&pageSize=10
Authorization: Bearer {{adminToken}}
```

断言：

- `code = 200`
- 返回卡类型列表

#### A07-02 新增卡类型

```http
POST {{adminBaseUrl}}/member/card-type
Authorization: Bearer {{adminToken}}
Content-Type: application/json

{
  "typeName": "Apifox测试卡",
  "validDays": 30,
  "borrowLimit": 3,
  "salePrice": 99,
  "status": 0,
  "sort": 99,
  "remark": "Apifox"
}
```

断言：

- `code = 200`
- 卡类型列表可查到
- 卡类型日志有新增记录

#### A07-03 编辑卡类型

```http
PUT {{adminBaseUrl}}/member/card-type/{{cardTypeId}}
Authorization: Bearer {{adminToken}}
Content-Type: application/json

{
  "typeName": "Apifox测试卡-编辑",
  "validDays": 60,
  "borrowLimit": 5,
  "salePrice": 199,
  "status": 0,
  "sort": 98,
  "remark": "Apifox编辑"
}
```

断言：

- `code = 200`
- 卡类型日志记录变更前后数据

### A08 会员卡记录、订单、退卡

#### A08-01 查询会员卡记录

```http
GET {{adminBaseUrl}}/member/card/list?pageNum=1&pageSize=10&memberNo=&status=
Authorization: Bearer {{adminToken}}
```

断言：

- `code = 200`
- 可看到生效中、待生效、已过期、已退卡记录
- 默认排序合理，生效/待生效优先
- 受门店数据权限限制

#### A08-02 查询会员卡展示视图

```http
GET {{adminBaseUrl}}/member/card/member/{{memberId}}
Authorization: Bearer {{adminToken}}
```

断言：

- `code = 200`
- 返回当前生效卡、待生效卡、已过期卡、已退卡
- 同一会员同一时刻最多一张生效卡

#### A08-03 查询指定会员卡分页列表

```http
GET {{adminBaseUrl}}/member/card/member/{{memberId}}/list?pageNum=1&pageSize=10
Authorization: Bearer {{adminToken}}
```

断言：

- `code = 200`
- 只返回该会员卡队列

#### A08-04 查询会员卡售卡订单

```http
GET {{adminBaseUrl}}/member/card/order/list?pageNum=1&pageSize=10&memberNo=&orderStatus=
Authorization: Bearer {{adminToken}}
```

断言：

- `code = 200`
- 售卡订单可用于收入对账
- 受数据权限限制

#### A08-05 查询会员退卡订单

```http
GET {{adminBaseUrl}}/member/card/refund-order/list?pageNum=1&pageSize=10&memberNo=
Authorization: Bearer {{adminToken}}
```

断言：

- `code = 200`
- 退卡订单可用于退款对账
- 受数据权限限制

#### A08-06 后台会员卡退卡

```http
POST {{adminBaseUrl}}/member/card/{{memberCardId}}/refund
Authorization: Bearer {{adminToken}}
Content-Type: application/json

{
  "refundAmount": 99,
  "refundType": "CASH",
  "refundReason": "Apifox测试退卡"
}
```

断言：

- 付款时间 7 日内：允许退卡
- 超过付款时间 7 日：不允许退卡
- 不按生效时间判断是否可退
- 生成退卡订单
- 会员卡状态变为已退卡
- 会员卡日志、业务日志完整
- 售卡订单和退卡订单金额可对账

#### A08-07 查询会员卡日志

```http
GET {{adminBaseUrl}}/member/card/{{memberCardId}}/log
Authorization: Bearer {{adminToken}}
```

断言：

- `code = 200`
- 开卡、激活、过期、退卡均有日志

### A09 监控日志

#### A09-01 查询操作日志

```http
GET {{adminBaseUrl}}/monitor/operlog/list?pageNum=1&pageSize=10
Authorization: Bearer {{adminToken}}
```

断言：

- `code = 200`
- 后台新增、编辑、删除、导入、导出、退卡等操作有日志

#### A09-02 查询登录日志

```http
GET {{adminBaseUrl}}/monitor/logininfor/list?pageNum=1&pageSize=10
Authorization: Bearer {{adminToken}}
```

断言：

- `code = 200`
- 登录成功/失败有记录

## 五、小程序 API 测试用例

### M01 认证登录

#### M01-01 微信手机号登录-会员

```http
POST {{mpBaseUrl}}/auth/wechat-phone-login
Content-Type: application/json

{
  "code": "mock_member_code",
  "phoneCode": "mock_phone_code",
  "role": "member"
}
```

断言：

- 登录成功
- 返回 `accessToken`、`refreshToken`
- 提取会员 token 到 `memberAccessToken`
- 用户身份为会员

备注：如果测试环境使用真实微信 code，需要从小程序端获取；如果后端支持 mock，使用约定 mock code。

#### M01-02 微信手机号登录-员工

```http
POST {{mpBaseUrl}}/auth/wechat-phone-login
Content-Type: application/json

{
  "code": "mock_staff_code",
  "phoneCode": "mock_phone_code",
  "role": "staff"
}
```

断言：

- 登录成功
- 返回 `accessToken`、`refreshToken`
- 提取员工 token 到 `staffAccessToken`
- 用户身份为员工

#### M01-03 刷新 token

```http
POST {{mpBaseUrl}}/auth/refresh-token
Content-Type: application/json

{
  "refreshToken": "{{refreshToken}}"
}
```

断言：

- 刷新成功
- 返回新的 accessToken

#### M01-04 校验登录态

```http
GET {{mpBaseUrl}}/auth/session
Authorization: Bearer {{memberAccessToken}}
```

断言：

- 登录态有效
- 返回会员/员工身份标识

#### M01-05 退出登录

```http
POST {{mpBaseUrl}}/auth/logout
Authorization: Bearer {{memberAccessToken}}
```

断言：

- 退出成功
- 原 token 后续访问受保护接口失败

### M02 用户端首页与会员码

#### M02-01 用户首页

```http
GET {{mpBaseUrl}}/user/home
Authorization: Bearer {{memberAccessToken}}
```

断言：

- 返回会员基础信息
- 返回当前有效会员卡状态
- 返回真实当前积分

#### M02-02 生成动态会员码

```http
POST {{mpBaseUrl}}/user/member-code
Authorization: Bearer {{memberAccessToken}}
Content-Type: application/json

{}
```

断言：

- 返回 `memberCodeToken`
- token 归属当前登录会员
- 服务端有效期为 1 小时
- 提取 `memberCodeToken`

#### M02-03 未登录生成会员码

```http
POST {{mpBaseUrl}}/user/member-code
Content-Type: application/json

{}
```

断言：

- 失败
- 不返回 token

### M03 员工扫码与开卡

#### M03-01 员工解析会员码

```http
POST {{mpBaseUrl}}/staff/member-code/scan
Authorization: Bearer {{staffAccessToken}}
Content-Type: application/json

{
  "memberCodeToken": "{{memberCodeToken}}"
}
```

断言：

- 解析成功
- 返回会员 ID、姓名、会员编号
- token 过期或伪造时失败

#### M03-02 会员 token 调员工扫码接口

```http
POST {{mpBaseUrl}}/staff/member-code/scan
Authorization: Bearer {{memberAccessToken}}
Content-Type: application/json

{
  "memberCodeToken": "{{memberCodeToken}}"
}
```

断言：

- 应失败
- 不泄露会员敏感信息

#### M03-03 查询扫码会员概要

```http
GET {{mpBaseUrl}}/staff/members/{{memberId}}/overview
Authorization: Bearer {{staffAccessToken}}
```

断言：

- 返回会员基础信息
- 返回当前会员卡状态
- 返回借阅、积分概要

#### M03-04 员工开通/续费会员卡

```http
POST {{mpBaseUrl}}/staff/members/{{memberId}}/activate-card
Authorization: Bearer {{staffAccessToken}}
Content-Type: application/json

{
  "cardTypeId": "{{cardTypeId}}",
  "paidAmount": 365,
  "payType": "CASH",
  "memberCodeToken": "{{memberCodeToken}}",
  "remark": "Apifox员工开卡"
}
```

断言：

- 必须携带有效 `memberCodeToken`
- token 过期/伪造时不能开卡
- 生成售卡订单
- 生成会员卡记录
- 如会员当前无有效卡，新卡立即生效
- 如会员已有有效卡，新卡进入待生效队列
- 备注字段正确保存到订单/会员卡业务记录
- 日志完整

#### M03-05 按会员码购买会员卡

```http
POST {{mpBaseUrl}}/staff/member-cards/buy
Authorization: Bearer {{staffAccessToken}}
Content-Type: application/json

{
  "memberCodeToken": "{{memberCodeToken}}",
  "cardTypeId": "{{cardTypeId}}",
  "paidAmount": 365,
  "payType": "CASH",
  "remark": "Apifox扫码购卡"
}
```

断言同 M03-04。

### M04 用户端会员卡

#### M04-01 查询本人会员卡

```http
GET {{mpBaseUrl}}/user/member-cards
Authorization: Bearer {{memberAccessToken}}
```

断言：

- 返回当前生效卡
- 返回待生效卡
- 返回已过期/已退卡记录
- 同一时刻最多一张生效卡
- 待生效卡以缴费日期判断 7 日内可退，但小程序端不提供退卡操作

### M05 借阅业务

#### M05-01 员工办理借阅

```http
POST {{mpBaseUrl}}/staff/members/{{memberId}}/borrows
Authorization: Bearer {{staffAccessToken}}
Content-Type: application/json

{
  "memberCodeToken": "{{memberCodeToken}}",
  "items": [
    {
      "bookId": "{{bookId}}",
      "borrowQty": 2
    }
  ],
  "remark": "Apifox借阅"
}
```

断言：

- 会员卡有效才允许借阅
- 图书库存/可借数量足够才允许借阅
- 生成借阅单
- 每本书生成借阅明细
- 库存/可借数量扣减正确
- 借阅日志完整

#### M05-02 用户查询本人借阅记录

```http
GET {{mpBaseUrl}}/user/borrows?pageNo=1&pageSize=20
Authorization: Bearer {{memberAccessToken}}
```

断言：

- 返回每本书一条记录，而不是订单组一条记录
- 返回 `detailId`
- 分页正确

#### M05-03 用户查询借阅详情

```http
GET {{mpBaseUrl}}/user/borrows/{{detailId}}
Authorization: Bearer {{memberAccessToken}}
```

断言：

- 按 `detailId` 查询
- 只能查询本人借阅详情
- 返回借阅数量、已还数量、可还数量、借转购数量

#### M05-04 员工查询借阅详情

```http
GET {{mpBaseUrl}}/staff/borrows/{{detailId}}
Authorization: Bearer {{staffAccessToken}}
```

断言：

- 按 `detailId` 查询
- 员工身份可查看用户借阅详情
- 数据权限符合门店/角色限制

#### M05-05 员工查询指定会员借阅记录

```http
GET {{mpBaseUrl}}/staff/members/{{memberId}}/borrows?pageNo=1&pageSize=20
Authorization: Bearer {{staffAccessToken}}
```

断言：

- 返回该会员每本书一条借阅明细
- 分页正确

#### M05-06 办理部分还书

```http
POST {{mpBaseUrl}}/staff/borrow-returns
Authorization: Bearer {{staffAccessToken}}
Content-Type: application/json

{
  "items": [
    {
      "borrowDetailId": "{{detailId}}",
      "returnQty": 1
    }
  ],
  "remark": "Apifox部分还书"
}
```

断言：

- 按 `borrowDetailId` 直查，不按 memberId=null 遍历订单
- 校验 `returnQty <= 可还数量`
- 支持分次、分数量归还
- 更新借阅明细已还数量
- 更新借阅单状态
- 库存/可借数量回补正确
- 生成还书记录和日志

#### M05-07 超量还书

```http
POST {{mpBaseUrl}}/staff/borrow-returns
Authorization: Bearer {{staffAccessToken}}
Content-Type: application/json

{
  "items": [
    {
      "borrowDetailId": "{{detailId}}",
      "returnQty": 999
    }
  ],
  "remark": "Apifox超量还书"
}
```

断言：

- 应失败
- 提示无可还数量或超过可还数量
- 不生成还书记录
- 不修改库存

#### M05-08 借转购

```http
POST {{mpBaseUrl}}/staff/borrow-purchases
Authorization: Bearer {{staffAccessToken}}
Content-Type: application/json

{
  "items": [
    {
      "borrowDetailId": "{{detailId}}",
      "purchaseQty": 1,
      "salePrice": 35
    }
  ],
  "payType": "CASH",
  "remark": "Apifox丢书借转购"
}
```

断言：

- 校验 `purchaseQty <= 可转购数量`
- 生成购书订单
- 更新借阅明细借转购数量
- 借转购部分不再可还
- 订单金额可对账
- 日志完整

### M06 积分业务

#### M06-01 查询积分事项

```http
GET {{mpBaseUrl}}/staff/points-reasons
Authorization: Bearer {{staffAccessToken}}
```

断言：

- 返回可用积分事项

#### M06-02 员工增加积分

```http
POST {{mpBaseUrl}}/staff/members/{{memberId}}/points/add
Authorization: Bearer {{staffAccessToken}}
Content-Type: application/json

{
  "points": 20,
  "reasonCode": "MANUAL_ADD",
  "remark": "Apifox加分"
}
```

断言：

- 会员当前积分增加
- 生成积分入账流水
- 操作日志完整

#### M06-03 员工消耗积分

```http
POST {{mpBaseUrl}}/staff/members/{{memberId}}/points/deduct
Authorization: Bearer {{staffAccessToken}}
Content-Type: application/json

{
  "points": 10,
  "reasonCode": "MANUAL_DEDUCT",
  "remark": "Apifox扣分"
}
```

断言：

- 积分足够时扣减成功
- 积分不足时失败
- 生成积分出账流水
- 入账/出账关系可追溯

#### M06-04 用户查询本人积分记录

```http
GET {{mpBaseUrl}}/user/points-records?pageNo=1&pageSize=20
Authorization: Bearer {{memberAccessToken}}
```

断言：

- 返回本人积分记录
- 分页正确
- 数据不为空时按时间倒序

#### M06-05 用户查询积分详情

```http
GET {{mpBaseUrl}}/user/points-records/{{pointsRecordId}}
Authorization: Bearer {{memberAccessToken}}
```

断言：

- 只能查询本人积分详情
- 返回积分变动前后值、原因、操作时间

#### M06-06 员工查询全市积分列表

```http
GET {{mpBaseUrl}}/staff/points-records?pageNo=1&pageSize=20&memberId={{memberId}}
Authorization: Bearer {{staffAccessToken}}
```

断言：

- 员工可按权限查询积分记录
- 分页正确

#### M06-07 员工查询积分详情

```http
GET {{mpBaseUrl}}/staff/points-records/{{pointsRecordId}}
Authorization: Bearer {{staffAccessToken}}
```

断言：

- 返回积分详情
- 数据权限符合员工权限

### M07 账号注销

#### M07-01 查询注销资格

```http
GET {{mpBaseUrl}}/account/cancel-eligibility
Authorization: Bearer {{memberAccessToken}}
```

断言：

- 返回真实当前积分
- 返回是否存在未完成借阅
- 返回是否允许注销

#### M07-02 有未完成借阅时注销

```http
POST {{mpBaseUrl}}/account/cancel
Authorization: Bearer {{memberAccessToken}}
Content-Type: application/json

{
  "reason": "Apifox注销测试"
}
```

断言：

- 有未完成借阅时应失败
- 不注销账号

#### M07-03 满足条件注销

```http
POST {{mpBaseUrl}}/account/cancel
Authorization: Bearer {{memberAccessToken}}
Content-Type: application/json

{
  "reason": "Apifox注销测试"
}
```

断言：

- 满足条件时注销成功
- 会员状态变更
- token 失效或后续登录受限
- 记录注销日志

### M08 文件上传

#### M08-01 上传图书附件图片

```http
POST {{mpBaseUrl}}/files/book-attachment-images
Authorization: Bearer {{staffAccessToken}}
Content-Type: multipart/form-data

file: image.jpg
```

断言：

- 上传成功
- 返回文件 URL
- 非图片文件失败
- 超大文件失败

## 六、跨模块闭环测试

### B01 会员卡售卖闭环

步骤：

1. 用户登录，生成 `memberCodeToken`。
2. 员工扫码解析会员。
3. 员工开卡。
4. 查询会员卡记录。
5. 查询售卡订单。
6. 查询会员卡日志。
7. 用户查询本人会员卡。

断言：

- 开卡必须依赖有效 `memberCodeToken`。
- 售卡订单金额 = 会员卡付款金额。
- 会员卡状态符合队列规则。
- 日志可追溯操作人、门店、时间、前后状态。

### B02 多卡队列闭环

步骤：

1. 对同一会员连续购买两张卡。
2. 查询会员卡展示视图。
3. 当前卡保持生效。
4. 第二张卡为待生效。
5. 当前卡过期后触发查询或业务操作。
6. 下一张待生效卡自动生效，有效期按自身天数顺延。

断言：

- 同一时刻只有一张 `ACTIVE` 卡。
- 待生效卡按缴费时间排队。
- 不依赖定时任务也能在查询/操作时刷新状态。

### B03 退卡对账闭环

步骤：

1. 购买一张会员卡。
2. 在付款 7 日内后台退卡。
3. 查询退卡订单。
4. 查询会员卡记录。
5. 查询会员卡日志。

断言：

- 退卡按付款时间 7 日内判断。
- 不按生效时间判断。
- 小程序端无退卡接口。
- 退卡订单金额与会员卡退款金额一致。
- 退卡后不再作为有效卡统计。

### B04 借阅-部分还书-借转购闭环

步骤：

1. 员工为会员借同一种书 2 本。
2. 用户查询借阅记录，应显示该书一条明细。
3. 员工归还 1 本。
4. 员工将剩余 1 本借转购。
5. 查询借阅详情。
6. 查询购书订单和日志。

断言：

- 借阅数量 = 已还数量 + 借转购数量 + 未处理数量。
- 已还数量不能超过借阅数量。
- 借转购数量不能超过可转购数量。
- 库存、订单、日志闭环。

### B05 积分闭环

步骤：

1. 查询会员当前积分。
2. 员工增加积分。
3. 员工消耗部分积分。
4. 用户查询积分记录。
5. 员工查询积分详情。

断言：

- 当前积分 = 原积分 + 入账 - 出账。
- 积分流水前后值连续。
- 出账不能造成负积分。
- 员工端、用户端看到的数据一致。

### B06 数据权限闭环

步骤：

1. 创建两个门店 A/B。
2. A 门店下创建会员 A。
3. B 门店下创建会员 B。
4. 创建门店角色，仅授权 A。
5. 用 A 权限账号查询会员列表、会员卡记录、首页统计。

断言：

- 只能看到 A 门店数据。
- 单条详情接口不能越权访问 B 门店数据。
- 首页统计只汇总 A 门店。

## 七、Apifox 自动化建议

### 1. 登录 token 自动提取

后台登录接口后置脚本：

```javascript
const json = pm.response.json();
if (json.token) {
  pm.environment.set("adminToken", json.token);
}
```

小程序登录接口后置脚本：

```javascript
const json = pm.response.json();
const data = json.data || json;
if (data.accessToken) {
  pm.environment.set("memberAccessToken", data.accessToken);
}
if (data.refreshToken) {
  pm.environment.set("refreshToken", data.refreshToken);
}
```

生成会员码接口后置脚本：

```javascript
const json = pm.response.json();
const data = json.data || {};
if (data.memberCodeToken) {
  pm.environment.set("memberCodeToken", data.memberCodeToken);
}
```

### 2. 通用成功断言脚本

```javascript
pm.test("HTTP 200", function () {
  pm.response.to.have.status(200);
});

pm.test("业务成功", function () {
  const json = pm.response.json();
  pm.expect([0, 200]).to.include(json.code);
});
```

### 3. 列表接口断言脚本

```javascript
pm.test("列表结构正确", function () {
  const json = pm.response.json();
  pm.expect(json).to.have.property("rows");
  pm.expect(json).to.have.property("total");
});
```

### 4. 防刷/安全测试建议

| 场景 | 测试方式 | 期望 |
|---|---|---|
| 重复提交开卡 | 同一请求连续调用 2 次 | 不应生成重复订单 |
| 伪造 `memberCodeToken` | 修改 token 字符 | 开卡/扫码失败 |
| 过期 `memberCodeToken` | 等待超过有效期后使用 | 开卡/扫码失败 |
| 会员 token 调员工接口 | 用会员 token 调 `/staff/**` | 失败 |
| 员工 token 调本人接口 | 用员工 token 调 `/user/**` | 按业务身份限制 |
| 越权访问详情 | A 门店账号查 B 门店单据 | 失败 |
| 超量还书 | `returnQty > 可还数量` | 失败且数据不变 |
| 积分扣成负数 | 扣减大于当前积分 | 失败且数据不变 |

## 八、优先级建议

| 优先级 | 模块 |
|---|---|
| P0 | 登录、会员码、开卡、退卡、借阅、还书、借转购、积分增减、数据权限 |
| P1 | 会员导入导出、会员卡订单、首页统计、操作日志 |
| P2 | 系统菜单、字典、参数、通知、监控 |

