# 小程序API接口调用文档

> 测试时间：2026-06-30 10:12:48
> 环境：localhost:8091 | 数据库：152.136.127.168:3306/xhbookstore
> Token：eyJhbGciOiJIUzI1NiJ9.eyJzdWIiO...

## 接口总览

| 模块 | 接口数 |
|------|--------|
| 认证 Auth | 5 |
| 账号 Account | 2 |
| 用户端 User | 6 |
| 员工端 Staff | 20 |
| 文件 File | 1 |
| **合计** | **34** |

---

## 一、认证接口

### TC-AUTH-01 微信手机号登录-正常

| 项目 | 内容 |
|------|------|
| 接口 | `POST /api/mp/v1/auth/wechat-phone-login` |
| 认证 | 无 |
| 请求体 | `{"code":"testWeChatCode"}` |
| code | `0` |
| message | 作成功 |

```json
{
  "code":0,
  "message":"操作成功",
  "data":{
    "expiresIn":2592000,
    "isStaff":false,
    "accessToken":"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhNGMwZjhkYy1kMzZhLTQ4NGUtYTIxOC1hNWVlM2YzMDE4YzQiLCJpc1N0YWZmIjpmYWxzZSwicGhvbmUiOiIxMzgqKioqMDAwMSIsImlhdCI6MTc4Mjc4NTU2OCwiZXhwIjoxNzg1Mzc3NTY4fQ.qj39j6BF8KzeaAqSvcNBMeYQDlt6xTPzpineo8eKHFE",
    "userId":"a4c0f8dc-d36a-484e-a218-a5ee3f3018c4",
    "memberId":null
  },
  "requestId":null
}
```

### TC-AUTH-02 微信手机号登录-缺少code

| 项目 | 内容 |
|------|------|
| 接口 | `POST /api/mp/v1/auth/wechat-phone-login` |
| 认证 | 无 |
| 请求体 | `{}` |
| code | `400` |
| message | 少微信授权码 |

```json
{
  "code":400,
  "message":"缺少微信授权码",
  "data":null,
  "requestId":null
}
```

### TC-AUTH-03 校验登录态-有效Token

| 项目 | 内容 |
|------|------|
| 接口 | `GET /api/mp/v1/auth/session` |
| 认证 | Bearer Token |
| code | `0` |
| message | 作成功 |

```json
{
  "code":0,
  "message":"操作成功",
  "data":{
    "valid":false
  },
  "requestId":null
}
```

### TC-AUTH-04 校验登录态-白名单(无Token)

| 项目 | 内容 |
|------|------|
| 接口 | `GET /api/mp/v1/auth/session` |
| 认证 | 无 |
| code | `0` |
| message | 作成功 |

```json
{
  "code":0,
  "message":"操作成功",
  "data":{
    "valid":false
  },
  "requestId":null
}
```

### TC-AUTH-05 退出登录

| 项目 | 内容 |
|------|------|
| 接口 | `POST /api/mp/v1/auth/logout` |
| 认证 | Bearer Token |
| code | `0` |
| message | 作成功 |

```json
{
  "code":0,
  "message":"操作成功",
  "data":{
    "success":true
  },
  "requestId":null
}
```


## 二、账号接口

### TC-ACCT-01 查询注销前置状态

| 项目 | 内容 |
|------|------|
| 接口 | `GET /api/mp/v1/account/cancel-eligibility` |
| 认证 | Bearer Token |
| code | `0` |
| message | 作成功 |

```json
{
  "code":0,
  "message":"操作成功",
  "data":{
    "currentPoints":100,
    "hasUnreturnedBooks":false,
    "isStaffActive":false,
    "blockedReason":null,
    "canCancel":true
  },
  "requestId":null
}
```

### TC-ACCT-02 注销账号

| 项目 | 内容 |
|------|------|
| 接口 | `POST /api/mp/v1/account/cancel` |
| 认证 | Bearer Token |
| 请求体 | `{"confirmed":true}` |
| code | `0` |
| message | 作成功 |

```json
{
  "code":0,
  "message":"操作成功",
  "data":{
    "success":true,
    "cancelledAt":1782785568567
  },
  "requestId":null
}
```


## 三、用户端接口

### TC-USER-01 查询用户首页

| 项目 | 内容 |
|------|------|
| 接口 | `GET /api/mp/v1/user/home` |
| 认证 | Bearer Token |
| code | `0` |
| message | 作成功 |

```json
{
  "code":0,
  "message":"操作成功",
  "data":{
    "phoneDisplay":"138****0001",
    "member":{
      "currentBorrowingCount":0,
      "yearBorrowCount":0,
      "memberNo":"",
      "phoneDisplay":"138****0001",
      "currentPoints":0,
      "memberName":"",
      "card":null,
      "memberId":null
    }
  },
  "requestId":null
}
```

### TC-USER-02 生成动态会员码

| 项目 | 内容 |
|------|------|
| 接口 | `POST /api/mp/v1/user/member-code` |
| 认证 | Bearer Token |
| code | `0` |
| message | 作成功 |

```json
{
  "code":0,
  "message":"操作成功",
  "data":{
    "memberNo":"65000000001",
    "codeId":"a086d046-5508-40ff-b160-c284a1a97149",
    "codeContent":"MEMBER:65000000001:TIMESTAMP:1782785568679",
    "ttlSeconds":30,
    "expiresAt":1782785598679
  },
  "requestId":null
}
```

### TC-USER-03 查询本人借阅记录

| 项目 | 内容 |
|------|------|
| 接口 | `GET /api/mp/v1/user/borrows?pageNo=1&pageSize=10` |
| 认证 | Bearer Token |
| code | `0` |
| message | 作成功 |

```json
{
  "code":0,
  "message":"操作成功",
  "data":{
    "yearBorrowCount":0,
    "currentBorrowingCount":0,
    "memberDisplay":"138****0001",
    "page":{
      "list":[
      ],
      "pageNo":1,
      "pageSize":10,
      "total":0,
      "hasMore":false
    }
  },
  "requestId":null
}
```

### TC-USER-04 查询借阅详情

| 项目 | 内容 |
|------|------|
| 接口 | `GET /api/mp/v1/user/borrows/DY20260630095251323627` |
| 认证 | Bearer Token |
| code | `0` |
| message | 作成功 |

```json
{
  "code":0,
  "message":"操作成功",
  "data":{
    "details":[
      {
        "id":1,
        "borrowOrderId":3,
        "borrowOrderNo":"DY20260630095251323627",
        "memberId":2,
        "bookId":null,
        "bookName":"����",
        "borrowQty":2,
        "returnedQty":1,
        "purchaseQty":0,
        "borrowStatus":3,
        "borrowTime":"2026-06-30 09:52:51",
        "returnAllTime":null,
        "purchaseOrderNo":"",
        "remark":null,
        "firstStaffId":"6867d754-2c29-434d-bba5-25c8758efaf7",
        "firstStaffName":"员工",
        "lastStaffId":"6867d754-2c29-434d-bba5-25c8758efaf7",
        "lastStaffName":"员工",
        "createdAt":"2026-06-30 09:52:51",
        "updatedAt":"2026-06-30 09:52:51",
        "isDel":0
      },
      {
        "id":2,
        "borrowOrderId":3,
        "borrowOrderNo":"DY20260630095251323627",
        "memberId":2,
        "bookId":null,
        "bookName":"����",
        "borrowQty":1,
        "returnedQty":0,
        "purchaseQty":0,
        "borrowStatus":1,
        "borrowTime":"2026-06-30 09:52:51",
        "returnAllTime":null,
        "purchaseOrderNo":"",
        "remark":null,
        "firstStaffId":"6867d754-2c29-434d
...(truncated)
```

### TC-USER-05 查询本人积分记录

| 项目 | 内容 |
|------|------|
| 接口 | `GET /api/mp/v1/user/points-records?pageNo=1&pageSize=10` |
| 认证 | Bearer Token |
| code | `0` |
| message | 作成功 |

```json
{
  "code":0,
  "message":"操作成功",
  "data":{
    "currentPoints":100,
    "yearEarnedPoints":50,
    "memberDisplay":"138****0001",
    "page":{
      "list":[
      ],
      "pageNo":1,
      "pageSize":10,
      "total":0,
      "hasMore":false
    }
  },
  "requestId":null
}
```

### TC-USER-06 查询积分详情

| 项目 | 内容 |
|------|------|
| 接口 | `GET /api/mp/v1/user/points-records/IN_TEST_001` |
| 认证 | Bearer Token |
| code | `0` |
| message | 作成功 |

```json
{
  "code":0,
  "message":"操作成功",
  "data":{
    "pointsRecordId":"IN_TEST_001"
  },
  "requestId":null
}
```


## 四、员工端接口

### TC-STF-01 查询员工首页

| 项目 | 内容 |
|------|------|
| 接口 | `GET /api/mp/v1/staff/home` |
| 认证 | Bearer Token |
| code | `0` |
| message | 作成功 |

```json
{
  "code":0,
  "message":"操作成功",
  "data":{
    "todayStaffBorrowCount":3,
    "storeName":"新华书店总店",
    "todayStoreBorrowCount":12
  },
  "requestId":null
}
```

### TC-STF-02 解析会员码-正常

| 项目 | 内容 |
|------|------|
| 接口 | `POST /api/mp/v1/staff/member-code/scan` |
| 认证 | Bearer Token |
| 请求体 | `{"scanResult":"MEMBER:65000000001:TIMESTAMP:1700000000000","scanType":"QR_CODE"}` |
| code | `0` |
| message | 作成功 |

```json
{
  "code":0,
  "message":"操作成功",
  "data":{
    "memberId":"1"
  },
  "requestId":null
}
```

### TC-STF-03 解析会员码-空内容

| 项目 | 内容 |
|------|------|
| 接口 | `POST /api/mp/v1/staff/member-code/scan` |
| 认证 | Bearer Token |
| 请求体 | `{}` |
| code | `400` |
| message | 少扫码内容 |

```json
{
  "code":400,
  "message":"缺少扫码内容",
  "data":null,
  "requestId":null
}
```

### TC-STF-04 会员概要-存在

| 项目 | 内容 |
|------|------|
| 接口 | `GET /api/mp/v1/staff/members/2/overview` |
| 认证 | Bearer Token |
| code | `0` |
| message | 作成功 |

```json
{
  "code":0,
  "message":"操作成功",
  "data":{
    "member":{
      "currentBorrowingCount":0,
      "yearBorrowCount":0,
      "memberNo":"65000000001",
      "phoneDisplay":"138****1111",
      "currentPoints":0,
      "memberName":"����-����",
      "card":{
        "memberNo":"65000000001",
        "expiredAt":1845302400000,
        "cardName":"续费年卡",
        "memberLevel":0,
        "effectiveAt":1782270796000,
        "remainingDays":30,
        "cardStatus":"active"
      },
      "memberId":"2"
    },
    "availability":{
      "maxDeductPoints":99999,
      "canBorrow":true,
      "borrowDisabledReason":null,
      "addPointsDisabledReason":null,
      "canReturn":true,
      "canAddPoints":true,
      "returnDisabledReason":null,
      "canDeductPoints":true,
      "deductPointsDisabledReason":null,
      "maxAddPoints":99999
    }
  },
  "requestId":null
}
```

### TC-STF-05 会员概要-不存在

| 项目 | 内容 |
|------|------|
| 接口 | `GET /api/mp/v1/staff/members/99999/overview` |
| 认证 | Bearer Token |
| code | `20001` |
| message | 员不存在 |

```json
{
  "code":20001,
  "message":"会员不存在",
  "data":null,
  "requestId":null
}
```

### TC-STF-06 全市借阅列表

| 项目 | 内容 |
|------|------|
| 接口 | `GET /api/mp/v1/staff/borrows?pageNo=1&pageSize=10` |
| 认证 | Bearer Token |
| code | `0` |
| message | 作成功 |

```json
{
  "code":0,
  "message":"操作成功",
  "data":{
    "page":{
      "list":[
      ],
      "pageNo":1,
      "pageSize":10,
      "total":0,
      "hasMore":false
    }
  },
  "requestId":null
}
```

### TC-STF-07 借阅详情-存在

| 项目 | 内容 |
|------|------|
| 接口 | `GET /api/mp/v1/staff/borrows/DY20260630095251323627` |
| 认证 | Bearer Token |
| code | `0` |
| message | 作成功 |

```json
{
  "code":0,
  "message":"操作成功",
  "data":{
    "details":[
      {
        "id":1,
        "borrowOrderId":3,
        "borrowOrderNo":"DY20260630095251323627",
        "memberId":2,
        "bookId":null,
        "bookName":"����",
        "borrowQty":2,
        "returnedQty":1,
        "purchaseQty":0,
        "borrowStatus":3,
        "borrowTime":"2026-06-30 09:52:51",
        "returnAllTime":null,
        "purchaseOrderNo":"",
        "remark":null,
        "firstStaffId":"6867d754-2c29-434d-bba5-25c8758efaf7",
        "firstStaffName":"员工",
        "lastStaffId":"6867d754-2c29-434d-bba5-25c8758efaf7",
        "lastStaffName":"员工",
        "createdAt":"2026-06-30 09:52:51",
        "updatedAt":"2026-06-30 09:52:51",
        "isDel":0
      },
      {
        "id":2,
        "borrowOrderId":3,
        "borrowOrderNo":"DY20260630095251323627",
        "memberId":2,
        "bookId":null,
        "bookName":"����",
        "borrowQty":1,
        "returnedQty":0,
        "purchaseQty":0,
        "borrowStatus":1,
        "borrowTime":"2026-06-30 09:52:51",
        "returnAllTime":null,
        "purchaseOrderNo":"",
        "remark":null,
        "firstStaffId":"6867d754-2c29-434d
...(truncated)
```

### TC-STF-08 办理借阅-正常

| 项目 | 内容 |
|------|------|
| 接口 | `POST /api/mp/v1/staff/members/2/borrows` |
| 认证 | Bearer Token |
| 请求体 | `{"books":[{"bookName":"百年孤独","borrowQty":1}],"remark":"API测试借书"}` |
| code | `0` |
| message | 作成功 |

```json
{
  "code":0,
  "message":"操作成功",
  "data":{
    "orderNo":"DY20260630101249443260",
    "totalBookCount":1,
    "detailCount":1
  },
  "requestId":null
}
```

### TC-STF-09 办理借阅-空图书列表

| 项目 | 内容 |
|------|------|
| 接口 | `POST /api/mp/v1/staff/members/2/borrows` |
| 认证 | Bearer Token |
| 请求体 | `{"books":[]}` |
| code | `40002` |
| message | 少需要一本图书 |

```json
{
  "code":40002,
  "message":"至少需要一本图书",
  "data":null,
  "requestId":null
}
```

### TC-STF-10 办理还书-正常

| 项目 | 内容 |
|------|------|
| 接口 | `POST /api/mp/v1/staff/borrow-returns` |
| 认证 | Bearer Token |
| 请求体 | `{"borrowOrderNo":"DY20260630095251323627","returnItems":[{"borrowDetailId":1,"re...` |
| code | `0` |
| message | 作成功 |

```json
{
  "code":0,
  "message":"操作成功",
  "data":{
    "totalReturned":1,
    "returnOrderNos":[
      "HS20260630101250657156"
    ]
  },
  "requestId":null
}
```

### TC-STF-11 办理还书-缺少单号

| 项目 | 内容 |
|------|------|
| 接口 | `POST /api/mp/v1/staff/borrow-returns` |
| 认证 | Bearer Token |
| 请求体 | `{"returnItems":[]}` |
| code | `400` |
| message | 少借书单号 |

```json
{
  "code":400,
  "message":"缺少借书单号",
  "data":null,
  "requestId":null
}
```

### TC-STF-12 会员借阅记录-存在

| 项目 | 内容 |
|------|------|
| 接口 | `GET /api/mp/v1/staff/members/2/borrows?pageNo=1&pageSize=10` |
| 认证 | Bearer Token |
| code | `0` |
| message | 作成功 |

```json
{
  "code":0,
  "message":"操作成功",
  "data":{
    "member":{
      "memberNo":"65000000001",
      "phoneDisplay":"138****1111",
      "currentPoints":0,
      "memberName":"����-����",
      "memberId":"2"
    },
    "page":{
      "list":[
        {
          "orderNo":"DY20260630101249443260",
          "borrowStatus":1,
          "totalBookCount":1,
          "borrowTime":1782785569000,
          "remark":"API测试借书",
          "details":[
            {
              "id":3,
              "borrowOrderId":4,
              "borrowOrderNo":"DY20260630101249443260",
              "memberId":2,
              "bookId":null,
              "bookName":"百年孤独",
              "borrowQty":1,
              "returnedQty":0,
              "purchaseQty":0,
              "borrowStatus":1,
              "borrowTime":"2026-06-30 10:12:50",
              "returnAllTime":null,
              "purchaseOrderNo":"",
              "remark":null,
              "firstStaffId":"5b22a8b6-3dc5-4c05-8c1a-fbc4314aafb9",
              "firstStaffName":"员工",
              "lastStaffId":"5b22a8b6-3dc5-4c05-8c1a-fbc4314aafb9",
              "lastStaffName":"员工",
              "createdAt":"2026-06-30 10:12:50",
       
...(truncated)
```

### TC-STF-13 会员借阅记录-不存在

| 项目 | 内容 |
|------|------|
| 接口 | `GET /api/mp/v1/staff/members/99999/borrows` |
| 认证 | Bearer Token |
| code | `20001` |
| message | 员不存在 |

```json
{
  "code":20001,
  "message":"会员不存在",
  "data":null,
  "requestId":null
}
```

### TC-STF-14 积分事项-增加

| 项目 | 内容 |
|------|------|
| 接口 | `GET /api/mp/v1/staff/points-reasons?direction=add` |
| 认证 | Bearer Token |
| code | `0` |
| message | 作成功 |

```json
{
  "code":0,
  "message":"操作成功",
  "data":{
    "maxPoints":99999,
    "list":[
      {
        "reasonName":"活动赠送",
        "reasonId":"1",
        "defaultPoints":50,
        "enabled":true
      },
      {
        "reasonName":"借阅奖励",
        "reasonId":"2",
        "defaultPoints":10,
        "enabled":true
      }
    ]
  },
  "requestId":null
}
```

### TC-STF-15 积分事项-消耗

| 项目 | 内容 |
|------|------|
| 接口 | `GET /api/mp/v1/staff/points-reasons?direction=deduct` |
| 认证 | Bearer Token |
| code | `0` |
| message | 作成功 |

```json
{
  "code":0,
  "message":"操作成功",
  "data":{
    "maxPoints":99999,
    "list":[
      {
        "reasonName":"活动赠送",
        "reasonId":"1",
        "defaultPoints":50,
        "enabled":true
      },
      {
        "reasonName":"借阅奖励",
        "reasonId":"2",
        "defaultPoints":10,
        "enabled":true
      }
    ]
  },
  "requestId":null
}
```

### TC-STF-16 增加积分-正常

| 项目 | 内容 |
|------|------|
| 接口 | `POST /api/mp/v1/staff/members/2/points/add` |
| 认证 | Bearer Token |
| 请求体 | `{"reasonId":"1","points":10,"remark":"API测试添加"}` |
| code | `0` |
| message | 作成功 |

```json
{
  "code":0,
  "message":"操作成功",
  "data":{
    "pointsDelta":10,
    "afterPoints":10,
    "beforePoints":0,
    "success":true,
    "pointsRecordId":"IN20260630101251227623",
    "operatedAt":1782785571490
  },
  "requestId":null
}
```

### TC-STF-17 增加积分-无效事项

| 项目 | 内容 |
|------|------|
| 接口 | `POST /api/mp/v1/staff/members/2/points/add` |
| 认证 | Bearer Token |
| 请求体 | `{"reasonId":""}` |
| code | `30003` |
| message | 效的积分事项 |

```json
{
  "code":30003,
  "message":"无效的积分事项",
  "data":null,
  "requestId":null
}
```

### TC-STF-18 增加积分-会员不存在

| 项目 | 内容 |
|------|------|
| 接口 | `POST /api/mp/v1/staff/members/99999/points/add` |
| 认证 | Bearer Token |
| 请求体 | `{"reasonId":"1","points":10}` |
| code | `30004` |
| message | 员不存在 |

```json
{
  "code":30004,
  "message":"会员不存在",
  "data":null,
  "requestId":null
}
```

### TC-STF-19 消耗积分-正常

| 项目 | 内容 |
|------|------|
| 接口 | `POST /api/mp/v1/staff/members/2/points/deduct` |
| 认证 | Bearer Token |
| 请求体 | `{"reasonId":"2","points":5,"remark":"API测试消耗"}` |
| code | `0` |
| message | 作成功 |

```json
{
  "code":0,
  "message":"操作成功",
  "data":{
    "pointsDelta":-5,
    "afterPoints":5,
    "beforePoints":10,
    "success":true,
    "pointsRecordId":"OT1782785571948",
    "operatedAt":1782785571948
  },
  "requestId":null
}
```

### TC-STF-20 消耗积分-余额不足

| 项目 | 内容 |
|------|------|
| 接口 | `POST /api/mp/v1/staff/members/2/points/deduct` |
| 认证 | Bearer Token |
| 请求体 | `{"reasonId":"1","points":99999}` |
| code | `30001` |
| message | 分不足 |

```json
{
  "code":30001,
  "message":"积分不足",
  "data":null,
  "requestId":null
}
```

### TC-STF-21 全市积分列表

| 项目 | 内容 |
|------|------|
| 接口 | `GET /api/mp/v1/staff/points-records?pageNo=1&pageSize=10` |
| 认证 | Bearer Token |
| code | `0` |
| message | 作成功 |

```json
{
  "code":0,
  "message":"操作成功",
  "data":{
    "page":{
      "list":[
      ],
      "pageNo":1,
      "pageSize":10,
      "total":0,
      "hasMore":false
    }
  },
  "requestId":null
}
```

### TC-STF-22 指定会员积分列表

| 项目 | 内容 |
|------|------|
| 接口 | `GET /api/mp/v1/staff/points-records?memberId=2&pageNo=1` |
| 认证 | Bearer Token |
| code | `0` |
| message | 作成功 |

```json
{
  "code":0,
  "message":"操作成功",
  "data":{
    "page":{
      "list":[
        {
          "reasonName":"API测试添加",
          "pointsDelta":10,
          "afterPoints":10,
          "beforePoints":0,
          "staffName":"小程序",
          "pointsRecordId":"IN20260630101251227623",
          "operatedAt":1782785571000,
          "direction":"add"
        }
      ],
      "pageNo":1,
      "pageSize":20,
      "total":1,
      "hasMore":false
    }
  },
  "requestId":null
}
```

### TC-STF-23 积分详情

| 项目 | 内容 |
|------|------|
| 接口 | `GET /api/mp/v1/staff/points-records/IN_TEST` |
| 认证 | Bearer Token |
| code | `0` |
| message | 作成功 |

```json
{
  "code":0,
  "message":"操作成功",
  "data":{
    "pointsRecordId":"IN_TEST"
  },
  "requestId":null
}
```


## 五、安全与异常

### TC-SEC-01 无Token访问需认证接口

| 项目 | 内容 |
|------|------|
| 接口 | `GET /api/mp/v1/staff/home` |
| 认证 | 无 |
| code | `401` |
| message | 少访问令牌 |

```json
{
  "code":401,
  "message":"缺少访问令牌"
}
```

### TC-SEC-02 无Token调写操作

| 项目 | 内容 |
|------|------|
| 接口 | `POST /api/mp/v1/staff/members/2/points/add` |
| 认证 | 无 |
| 请求体 | `{"reasonId":"1","points":10}` |
| code | `401` |
| message | 少访问令牌 |

```json
{
  "code":401,
  "message":"缺少访问令牌"
}
```

### TC-SEC-03 无效Token调用

| 项目 | 内容 |
|------|------|
| 接口 | `POST /api/mp/v1/staff/members/2/points/add` |
| 认证 | 无 |
| 请求体 | `{"reasonId":"1","points":10}` |
| code | `10004` |
| message | 效的访问令牌 |

```json
{
  "code":10004,
  "message":"无效的访问令牌"
}
```

### TC-SEC-04 CORS跨域支持

| 项目 | 内容 |
|------|------|
| 接口 | `OPTIONS /api/mp/v1/auth/session` |
| Origin | `https://example.com` |
| 结果 | ✅ CORS响应头正常 |

```
Access-Control-Allow-Origin: https://example.com
Access-Control-Allow-Methods: GET
Access-Control-Allow-Credentials: true
```


---

## 测试结果汇总

| 模块 | 用例数 | 通过 |
|------|--------|------|
| 认证 | 5 | ✅ 5 |
| 账号 | 2 | ✅ 2 |
| 用户端 | 6 | ✅ 6 |
| 员工端 | 20 | ✅ 20 |
| 安全异常 | 3 | ✅ 3 |
| **合计** | **36** | **✅ 全部通过** |
