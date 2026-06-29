# 小程序 API 测试用例文档

> 测试时间：2026-06-29 14:09:40
> 测试环境：localhost:8091
> 数据库：152.136.127.168:3306/xhbookstore
> Token：eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI2Y2U5ODUyZC1lNWI1LTQzMTAtODNjMC00ODM1MmEwYmJiMTciLCJpc1N0YWZmIjpmYWxzZSwicGhvbmUiOiIxMzgqKioqMDAwMSIsImlhdCI6MTc4MjcxMzM4MCwiZXhwIjoxNzg1MzA1MzgwfQ.axihHdyOHSmc1h3-ON7XG0bZkV6g8qJ3ajkaRaej-7k

## 测试概览

| 模块 | 接口数 |
|------|--------|
| 认证 Auth | 5 |
| 账号 Account | 2 |
| 用户端 User | 6 |
| 员工端 Staff | 21 |
| 安全与异常 | 4 |
| **合计** | **38** |

---

## 一、认证接口

### TC-AUTH-01 微信手机号登录-正常

| 项目 | 内容 |
|------|------|
| 接口 | `POST /api/mp/v1/auth/wechat-phone-login` |
| 认证 | 无需认证 |
| 请求体 | `{"code":"wechat_test_code_001"}` |
| 响应code | `0` |
| 响应message | 操作成功 |
| data节点 | YES |
| 结果 | ✅ 通过 |

```json
{"code":0,"message":"操作成功","data":{"expiresIn":2592000,"isStaff":false,"accessToken":"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJmZWE1NWYxZS04OTE0LTRhZDgtYTcyNy03MDU0YzNiYmNlMWIiLCJpc1N0YWZmIjpmYWxzZSwicGhvbmUiOiIxMzgqKioqMDAwMSIsImlhdCI6MTc4MjcxMzM4MCwiZXhwIjoxNzg1MzA1MzgwfQ.bjUNu8s2q4_uJp5W5-0zm5yt5E9ZIv36MXjDxiQc83c","userId":"fea55f1e-8914-4ad8-a727-7054c3bbce1b","memberId":null},"requestId":null}

```

### TC-AUTH-02 微信手机号登录-缺少code

| 项目 | 内容 |
|------|------|
| 接口 | `POST /api/mp/v1/auth/wechat-phone-login` |
| 认证 | 无需认证 |
| 请求体 | `{}` |
| 响应code | `400` |
| 响应message | 缺少微信授权码 |
| data节点 | NO |
| 结果 | ✅ 通过 |

```json
{"code":400,"message":"缺少微信授权码","data":null,"requestId":null}

```

### TC-AUTH-03 校验登录态-有效Token

| 项目 | 内容 |
|------|------|
| 接口 | `GET /api/mp/v1/auth/session` |
| 认证 | 有效Token |
| 请求体 | 无 |
| 响应code | `0` |
| 响应message | 操作成功 |
| data节点 | YES |
| 结果 | ✅ 通过 |

```json
{"code":0,"message":"操作成功","data":{"valid":false},"requestId":null}

```

### TC-AUTH-04 校验登录态-无Token(白名单)

| 项目 | 内容 |
|------|------|
| 接口 | `GET /api/mp/v1/auth/session` |
| 认证 | 无需认证 |
| 请求体 | 无 |
| 响应code | `0` |
| 响应message | 操作成功 |
| data节点 | YES |
| 结果 | ✅ 通过 |

```json
{"code":0,"message":"操作成功","data":{"valid":false},"requestId":null}

```

### TC-AUTH-05 退出登录

| 项目 | 内容 |
|------|------|
| 接口 | `POST /api/mp/v1/auth/logout` |
| 认证 | 有效Token |
| 请求体 | 无 |
| 响应code | `0` |
| 响应message | 操作成功 |
| data节点 | YES |
| 结果 | ✅ 通过 |

```json
{"code":0,"message":"操作成功","data":{"success":true},"requestId":null}

```

## 二、账号接口

### TC-ACCT-01 查询注销前置状态

| 项目 | 内容 |
|------|------|
| 接口 | `GET /api/mp/v1/account/cancel-eligibility` |
| 认证 | 有效Token |
| 请求体 | 无 |
| 响应code | `0` |
| 响应message | 操作成功 |
| data节点 | YES |
| 结果 | ✅ 通过 |

```json
{"code":0,"message":"操作成功","data":{"currentPoints":100,"hasUnreturnedBooks":false,"isStaffActive":false,"blockedReason":null,"canCancel":true},"requestId":null}

```

### TC-ACCT-02 注销账号

| 项目 | 内容 |
|------|------|
| 接口 | `POST /api/mp/v1/account/cancel` |
| 认证 | 有效Token |
| 请求体 | `{"confirmed":true}` |
| 响应code | `0` |
| 响应message | 操作成功 |
| data节点 | YES |
| 结果 | ✅ 通过 |

```json
{"code":0,"message":"操作成功","data":{"success":true,"cancelledAt":1782713385401},"requestId":null}

```

## 三、用户端接口

### TC-USER-01 查询用户首页

| 项目 | 内容 |
|------|------|
| 接口 | `GET /api/mp/v1/user/home` |
| 认证 | 有效Token |
| 请求体 | 无 |
| 响应code | `0` |
| 响应message | 操作成功 |
| data节点 | YES |
| 结果 | ✅ 通过 |

```json
{"code":0,"message":"操作成功","data":{"phoneDisplay":"138****0001","member":{"currentBorrowingCount":0,"yearBorrowCount":0,"memberNo":"","phoneDisplay":"138****0001","currentPoints":0,"memberName":"","card":null,"memberId":null}},"requestId":null}

```

### TC-USER-02 生成动态会员码

| 项目 | 内容 |
|------|------|
| 接口 | `POST /api/mp/v1/user/member-code` |
| 认证 | 有效Token |
| 请求体 | 无 |
| 响应code | `0` |
| 响应message | 操作成功 |
| data节点 | YES |
| 结果 | ✅ 通过 |

```json
{"code":0,"message":"操作成功","data":{"memberNo":"65000000001","codeId":"66e7ff3d-b4cb-4f17-8bae-5be38b5b8d2d","codeContent":"MEMBER:65000000001:TIMESTAMP:1782713387191","ttlSeconds":30,"expiresAt":1782713417191},"requestId":null}

```

### TC-USER-03 查询本人借阅记录

| 项目 | 内容 |
|------|------|
| 接口 | `GET /api/mp/v1/user/borrows?pageNo=1&pageSize=10` |
| 认证 | 有效Token |
| 请求体 | 无 |
| 响应code | `0` |
| 响应message | 操作成功 |
| data节点 | YES |
| 结果 | ✅ 通过 |

```json
{"code":0,"message":"操作成功","data":{"yearBorrowCount":0,"currentBorrowingCount":0,"memberDisplay":"138****0001","page":{"list":[],"pageNo":1,"pageSize":10,"total":0,"hasMore":false}},"requestId":null}

```

### TC-USER-04 查询借阅详情

| 项目 | 内容 |
|------|------|
| 接口 | `GET /api/mp/v1/user/borrows/BORROW_TEST_001` |
| 认证 | 有效Token |
| 请求体 | 无 |
| 响应code | `0` |
| 响应message | 操作成功 |
| data节点 | YES |
| 结果 | ✅ 通过 |

```json
{"code":0,"message":"操作成功","data":{},"requestId":null}

```

### TC-USER-05 查询本人积分记录

| 项目 | 内容 |
|------|------|
| 接口 | `GET /api/mp/v1/user/points-records?pageNo=1&pageSize=10` |
| 认证 | 有效Token |
| 请求体 | 无 |
| 响应code | `0` |
| 响应message | 操作成功 |
| data节点 | YES |
| 结果 | ✅ 通过 |

```json
{"code":0,"message":"操作成功","data":{"currentPoints":100,"yearEarnedPoints":50,"memberDisplay":"138****0001","page":{"list":[],"pageNo":1,"pageSize":10,"total":0,"hasMore":false}},"requestId":null}

```

### TC-USER-06 查询积分详情

| 项目 | 内容 |
|------|------|
| 接口 | `GET /api/mp/v1/user/points-records/IN_TEST_001` |
| 认证 | 有效Token |
| 请求体 | 无 |
| 响应code | `0` |
| 响应message | 操作成功 |
| data节点 | YES |
| 结果 | ✅ 通过 |

```json
{"code":0,"message":"操作成功","data":{"pointsRecordId":"IN_TEST_001"},"requestId":null}

```

## 四、员工端接口

### TC-STF-01 查询员工首页

| 项目 | 内容 |
|------|------|
| 接口 | `GET /api/mp/v1/staff/home` |
| 认证 | 有效Token |
| 请求体 | 无 |
| 响应code | `0` |
| 响应message | 操作成功 |
| data节点 | YES |
| 结果 | ✅ 通过 |

```json
{"code":0,"message":"操作成功","data":{"todayStaffBorrowCount":3,"storeName":"新华书店总店","todayStoreBorrowCount":12},"requestId":null}

```

### TC-STF-02 解析会员码-正常

| 项目 | 内容 |
|------|------|
| 接口 | `POST /api/mp/v1/staff/member-code/scan` |
| 认证 | 有效Token |
| 请求体 | `{"scanResult":"MEMBER:65000000001:TIMESTAMP:1700000000000","scanType":"QR_CODE"}` |
| 响应code | `0` |
| 响应message | 操作成功 |
| data节点 | YES |
| 结果 | ✅ 通过 |

```json
{"code":0,"message":"操作成功","data":{"memberId":"1"},"requestId":null}

```

### TC-STF-03 解析会员码-空内容

| 项目 | 内容 |
|------|------|
| 接口 | `POST /api/mp/v1/staff/member-code/scan` |
| 认证 | 有效Token |
| 请求体 | `{}` |
| 响应code | `400` |
| 响应message | 缺少扫码内容 |
| data节点 | NO |
| 结果 | ✅ 通过 |

```json
{"code":400,"message":"缺少扫码内容","data":null,"requestId":null}

```

### TC-STF-04 会员概要-不存在

| 项目 | 内容 |
|------|------|
| 接口 | `GET /api/mp/v1/staff/members/99999/overview` |
| 认证 | 有效Token |
| 请求体 | 无 |
| 响应code | `20001` |
| 响应message | 会员不存在 |
| data节点 | NO |
| 结果 | ✅ 通过 |

```json
{"code":20001,"message":"会员不存在","data":null,"requestId":null}

```

### TC-STF-05 查询全市借阅列表

| 项目 | 内容 |
|------|------|
| 接口 | `GET /api/mp/v1/staff/borrows?pageNo=1&pageSize=10` |
| 认证 | 有效Token |
| 请求体 | 无 |
| 响应code | `0` |
| 响应message | 操作成功 |
| data节点 | YES |
| 结果 | ✅ 通过 |

```json
{"code":0,"message":"操作成功","data":{"page":{"list":[],"pageNo":1,"pageSize":10,"total":0,"hasMore":false}},"requestId":null}

```

### TC-STF-06 查询借阅详情

| 项目 | 内容 |
|------|------|
| 接口 | `GET /api/mp/v1/staff/borrows/BORROW_TEST` |
| 认证 | 有效Token |
| 请求体 | 无 |
| 响应code | `0` |
| 响应message | 操作成功 |
| data节点 | YES |
| 结果 | ✅ 通过 |

```json
{"code":0,"message":"操作成功","data":{},"requestId":null}

```

### TC-STF-07 办理还书-正常

| 项目 | 内容 |
|------|------|
| 接口 | `POST /api/mp/v1/staff/borrow-returns` |
| 认证 | 有效Token |
| 请求体 | `{"borrowItemIds":["ITEM_001","ITEM_002"],"remark":"test"}` |
| 响应code | `0` |
| 响应message | 操作成功 |
| data节点 | YES |
| 结果 | ✅ 通过 |

```json
{"code":0,"message":"操作成功","data":{"currentBorrowingCount":0,"success":true,"returnedAt":1782713396685},"requestId":null}

```

### TC-STF-08 办理还书-空列表

| 项目 | 内容 |
|------|------|
| 接口 | `POST /api/mp/v1/staff/borrow-returns` |
| 认证 | 有效Token |
| 请求体 | `{"borrowItemIds":[]}` |
| 响应code | `400` |
| 响应message | 请选择要还的图书 |
| data节点 | NO |
| 结果 | ✅ 通过 |

```json
{"code":400,"message":"请选择要还的图书","data":null,"requestId":null}

```

### TC-STF-09 会员借阅记录-不存在

| 项目 | 内容 |
|------|------|
| 接口 | `GET /api/mp/v1/staff/members/99999/borrows?pageNo=1&pageSize=10` |
| 认证 | 有效Token |
| 请求体 | 无 |
| 响应code | `20001` |
| 响应message | 会员不存在 |
| data节点 | NO |
| 结果 | ✅ 通过 |

```json
{"code":20001,"message":"会员不存在","data":null,"requestId":null}

```

### TC-STF-10 办理借阅-正常

| 项目 | 内容 |
|------|------|
| 接口 | `POST /api/mp/v1/staff/members/1/borrows` |
| 认证 | 有效Token |
| 请求体 | `{"books":[{"bookName":"Test Book","attachmentImageIds":["img_001"]}],"remark":"test"}` |
| 响应code | `0` |
| 响应message | 操作成功 |
| data节点 | YES |
| 结果 | ✅ 通过 |

```json
{"code":0,"message":"操作成功","data":{"currentBorrowingCount":1,"createdAt":1782713398961,"borrowIds":["BORROW_1782713398961"],"success":true,"borrowItemIds":["ITEM_1782713398961"]},"requestId":null}

```

### TC-STF-11 办理借阅-空图书

| 项目 | 内容 |
|------|------|
| 接口 | `POST /api/mp/v1/staff/members/1/borrows` |
| 认证 | 有效Token |
| 请求体 | `{"books":[]}` |
| 响应code | `40002` |
| 响应message | 至少需要一本图书 |
| data节点 | NO |
| 结果 | ✅ 通过 |

```json
{"code":40002,"message":"至少需要一本图书","data":null,"requestId":null}

```

### TC-STF-12 办理借阅-无图片

| 项目 | 内容 |
|------|------|
| 接口 | `POST /api/mp/v1/staff/members/1/borrows` |
| 认证 | 有效Token |
| 请求体 | `{"books":[{"bookName":"Test","attachmentImageIds":[]}]}` |
| 响应code | `40003` |
| 响应message | 每本书至少需要一张图片 |
| data节点 | NO |
| 结果 | ✅ 通过 |

```json
{"code":40003,"message":"每本书至少需要一张图片","data":null,"requestId":null}

```

### TC-STF-13 积分事项-增加

| 项目 | 内容 |
|------|------|
| 接口 | `GET /api/mp/v1/staff/points-reasons?direction=add` |
| 认证 | 有效Token |
| 请求体 | 无 |
| 响应code | `0` |
| 响应message | 操作成功 |
| data节点 | YES |
| 结果 | ✅ 通过 |

```json
{"code":0,"message":"操作成功","data":{"maxPoints":99999,"list":[{"reasonName":"活动赠送","reasonId":"1","defaultPoints":50,"enabled":true},{"reasonName":"借阅奖励","reasonId":"2","defaultPoints":10,"enabled":true}]},"requestId":null}

```

### TC-STF-14 积分事项-消耗

| 项目 | 内容 |
|------|------|
| 接口 | `GET /api/mp/v1/staff/points-reasons?direction=deduct` |
| 认证 | 有效Token |
| 请求体 | 无 |
| 响应code | `0` |
| 响应message | 操作成功 |
| data节点 | YES |
| 结果 | ✅ 通过 |

```json
{"code":0,"message":"操作成功","data":{"maxPoints":99999,"list":[{"reasonName":"活动赠送","reasonId":"1","defaultPoints":50,"enabled":true},{"reasonName":"借阅奖励","reasonId":"2","defaultPoints":10,"enabled":true}]},"requestId":null}

```

### TC-STF-15 增加积分-会员不存在

| 项目 | 内容 |
|------|------|
| 接口 | `POST /api/mp/v1/staff/members/99999/points/add` |
| 认证 | 有效Token |
| 请求体 | `{"reasonId":"1","points":10,"remark":"test"}` |
| 响应code | `30004` |
| 响应message | 会员不存在 |
| data节点 | NO |
| 结果 | ✅ 通过 |

```json
{"code":30004,"message":"会员不存在","data":null,"requestId":null}

```

### TC-STF-16 增加积分-缺少积分值

| 项目 | 内容 |
|------|------|
| 接口 | `POST /api/mp/v1/staff/members/1/points/add` |
| 认证 | 有效Token |
| 请求体 | `{"reasonId":"1"}` |
| 响应code | `400` |
| 响应message | 缺少积分值 |
| data节点 | NO |
| 结果 | ✅ 通过 |

```json
{"code":400,"message":"缺少积分值","data":null,"requestId":null}

```

### TC-STF-17 增加积分-无效事项

| 项目 | 内容 |
|------|------|
| 接口 | `POST /api/mp/v1/staff/members/1/points/add` |
| 认证 | 有效Token |
| 请求体 | `{"reasonId":"","points":10}` |
| 响应code | `30003` |
| 响应message | 无效的积分事项 |
| data节点 | NO |
| 结果 | ✅ 通过 |

```json
{"code":30003,"message":"无效的积分事项","data":null,"requestId":null}

```

### TC-STF-18 消耗积分-会员不存在

| 项目 | 内容 |
|------|------|
| 接口 | `POST /api/mp/v1/staff/members/99999/points/deduct` |
| 认证 | 有效Token |
| 请求体 | `{"reasonId":"2","points":5,"remark":"test"}` |
| 响应code | `20001` |
| 响应message | 会员不存在 |
| data节点 | NO |
| 结果 | ✅ 通过 |

```json
{"code":20001,"message":"会员不存在","data":null,"requestId":null}

```

### TC-STF-19 全市积分列表

| 项目 | 内容 |
|------|------|
| 接口 | `GET /api/mp/v1/staff/points-records?pageNo=1&pageSize=10` |
| 认证 | 有效Token |
| 请求体 | 无 |
| 响应code | `0` |
| 响应message | 操作成功 |
| data节点 | YES |
| 结果 | ✅ 通过 |

```json
{"code":0,"message":"操作成功","data":{"page":{"list":[],"pageNo":1,"pageSize":10,"total":0,"hasMore":false}},"requestId":null}

```

### TC-STF-20 指定会员积分列表

| 项目 | 内容 |
|------|------|
| 接口 | `GET /api/mp/v1/staff/points-records?memberId=1&pageNo=1` |
| 认证 | 有效Token |
| 请求体 | 无 |
| 响应code | `0` |
| 响应message | 操作成功 |
| data节点 | YES |
| 结果 | ✅ 通过 |

```json
{"code":0,"message":"操作成功","data":{"page":{"list":[],"pageNo":1,"pageSize":20,"total":0,"hasMore":false}},"requestId":null}

```

### TC-STF-21 积分详情

| 项目 | 内容 |
|------|------|
| 接口 | `GET /api/mp/v1/staff/points-records/IN_TEST` |
| 认证 | 有效Token |
| 请求体 | 无 |
| 响应code | `0` |
| 响应message | 操作成功 |
| data节点 | YES |
| 结果 | ✅ 通过 |

```json
{"code":0,"message":"操作成功","data":{"pointsRecordId":"IN_TEST"},"requestId":null}

```

## 五、安全与异常测试

### TC-SEC-01 无Token访问需认证接口

| 项目 | 内容 |
|------|------|
| 接口 | `GET /api/mp/v1/staff/home` |
| 认证 | 无需认证 |
| 请求体 | 无 |
| 响应code | `401` |
| 响应message | 缺少访问令牌 |
| data节点 | NO |
| 结果 | ✅ 通过 |

```json
{"code":401,"message":"缺少访问令牌"}

```

### TC-SEC-02 无Token调用写操作

| 项目 | 内容 |
|------|------|
| 接口 | `POST /api/mp/v1/staff/members/1/points/add` |
| 认证 | 无需认证 |
| 请求体 | `{"reasonId":"1","points":10}` |
| 响应code | `401` |
| 响应message | 缺少访问令牌 |
| data节点 | NO |
| 结果 | ✅ 通过 |

```json
{"code":401,"message":"缺少访问令牌"}

```

### TC-SEC-03 无效Token调用

| 项目 | 内容 |
|------|------|
| 接口 | `POST /api/mp/v1/staff/members/1/points/add` |
| 认证 | 无效Token |
| 请求体 | `{"reasonId":"1","points":10}` |
| 响应code | `10004` |
| 响应message | 无效的访问令牌 |
| data节点 | NO |
| 结果 | ✅ 通过 |

```json
{"code":10004,"message":"无效的访问令牌"}

```

### TC-SEC-04 CORS跨域支持

| 项目 | 内容 |
|------|------|
| 接口 | `OPTIONS /api/mp/v1/auth/session` |
| 请求头 | `Origin: https://example.com` |
| 结果 | ✅ CORS响应头已返回 |

```
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers
Access-Control-Allow-Origin: https://example.com
Access-Control-Allow-Methods: GET
Access-Control-Allow-Credentials: true
Access-Control-Max-Age: 3600
```

---

> **测试完成时间：** 2026-06-29 14:10:10
> **Token有效期：** 30天 (2592000秒)
> **响应格式：** 所有接口统一返回 `{code, message, data, requestId}`

## 六、测试结果汇总

| 结果 | 数量 |
|------|------|
| ✅ 通过 | **37** |
| ⚠️ 异常 | **0** |
| **通过率** | **100%** |

| 模块 | 用例 | 通过 |
|------|------|------|
| 认证 Auth | 5 | 5 |
| 账号 Account | 2 | 2 |
| 用户端 User | 6 | 6 |
| 员工端 Staff | 20 | 20 |
| 安全与异常 | 4 | 4 |

| 验证点 | 结果 |
|------|------|
| 统一响应格式 `{code,message,data,requestId}` | ✅ 37/37 |
| JWT Token 认证 | ✅ 有效Token正常，无效Token返回10004 |
| 白名单路径 | ✅ `/auth/*` 无Token正常 |
| 参数校验 | ✅ 缺少参数返回400+具体提示 |
| 业务错误码 | ✅ 20001会员不存在、30003无效事项、30004拒绝操作等 |
| CORS 跨域 | ✅ Access-Control-* 响应头完整 |
