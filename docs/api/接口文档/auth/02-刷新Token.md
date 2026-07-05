# 刷新 Token

## 基本信息

| 项 | 内容 |
|---|---|
| 接口 | `POST /api/mp/v1/auth/refresh-token` |
| 认证 | 不需要 accessToken，需要合法 refreshToken |
| Content-Type | `application/json;charset=UTF-8` |
| 对应代码 | `AuthController.refreshToken()` |

## 入参

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| refreshToken | string | 是 | 登录接口返回的刷新令牌 |

## 完整请求报文

```http
POST /api/mp/v1/auth/refresh-token HTTP/1.1
Host: 152.136.127.168
Content-Type: application/json;charset=UTF-8

{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9.refresh"
}
```

## 出参

| 字段 | 类型 | 说明 |
|---|---|---|
| data.accessToken | string | 新 accessToken |
| data.expiresIn | long | 有效秒数 |

## 完整响应报文

```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9.new_access",
    "expiresIn": 7200
  },
  "requestId": "req_20260705_110010_001"
}
```

## 异常响应

```json
{
  "code": 401,
  "message": "refreshToken已过期",
  "data": null,
  "requestId": "req_20260705_110010_002"
}
```
