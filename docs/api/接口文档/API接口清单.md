# API 接口清单

> 小程序 API 技术文档索引。基础路径：`/api/mp/v1`

## 认证模块

| 方法 | 路径 | 说明 | 详细文档 |
|---|---|---|---|
| POST | `/auth/wechat-phone-login` | 微信手机号登录 | [auth/01-微信手机号登录.md](auth/01-微信手机号登录.md) |
| POST | `/auth/refresh-token` | 刷新 Token | [auth/02-刷新Token.md](auth/02-刷新Token.md) |
| GET | `/auth/session` | 校验登录态 | [auth/03-校验登录态.md](auth/03-校验登录态.md) |
| POST | `/auth/logout` | 退出登录 | [auth/04-退出登录.md](auth/04-退出登录.md) |

## 用户端

| 方法 | 路径 | 说明 | 详细文档 |
|---|---|---|---|
| GET | `/user/home` | 用户首页 | [user/01-用户首页.md](user/01-用户首页.md) |
| POST | `/user/member-code` | 生成动态会员码 | [user/02-生成动态会员码.md](user/02-生成动态会员码.md) |
| GET | `/user/member-cards` | 查询本人会员卡 | [user/07-查询本人会员卡.md](user/07-查询本人会员卡.md) |
| GET | `/user/borrows` | 本人借阅记录，每本书一条明细 | [user/03-查询本人借阅记录.md](user/03-查询本人借阅记录.md) |
| GET | `/user/borrows/{detailId}` | 查询本人借阅详情，按借阅明细 ID 查询 | [user/04-查询借阅详情.md](user/04-查询借阅详情.md) |
| GET | `/user/points-records` | 本人积分记录 | [user/05-查询本人积分记录.md](user/05-查询本人积分记录.md) |
| GET | `/user/points-records/{pointsRecordId}` | 积分详情 | [user/06-查询积分详情.md](user/06-查询积分详情.md) |

## 员工端

| 方法 | 路径 | 说明 | 详细文档 |
|---|---|---|---|
| GET | `/staff/home` | 员工首页 | [staff/01-员工首页.md](staff/01-员工首页.md) |
| POST | `/staff/member-code/scan` | 解析会员码 | [staff/02-解析会员码.md](staff/02-解析会员码.md) |
| GET | `/staff/members/{memberId}/overview` | 查询扫码会员概要 | [staff/03-查询扫码会员概要.md](staff/03-查询扫码会员概要.md) |
| GET | `/staff/borrows` | 全市借阅列表 | [staff/04-查询全市借阅列表.md](staff/04-查询全市借阅列表.md) |
| GET | `/staff/borrows/{detailId}` | 员工侧借阅详情，按借阅明细 ID 查询 | [staff/05-查询员工侧借阅详情.md](staff/05-查询员工侧借阅详情.md) |
| POST | `/staff/borrow-returns` | 办理还书 | [staff/06-办理还书.md](staff/06-办理还书.md) |
| GET | `/staff/members/{memberId}/borrows` | 指定会员借阅记录 | [staff/07-查询指定会员借阅记录.md](staff/07-查询指定会员借阅记录.md) |
| POST | `/staff/members/{memberId}/borrows` | 办理借阅 | [staff/08-办理借阅.md](staff/08-办理借阅.md) |
| GET | `/staff/points-reasons` | 查询积分事项 | [staff/09-查询积分事项.md](staff/09-查询积分事项.md) |
| POST | `/staff/members/{memberId}/points/add` | 增加积分 | [staff/10-增加积分.md](staff/10-增加积分.md) |
| POST | `/staff/members/{memberId}/points/deduct` | 消耗积分 | [staff/11-消耗积分.md](staff/11-消耗积分.md) |
| GET | `/staff/points-records` | 全市积分列表 | [staff/12-查询全市积分列表.md](staff/12-查询全市积分列表.md) |
| GET | `/staff/points-records/{pointsRecordId}` | 积分详情 | [staff/13-查询积分详情.md](staff/13-查询积分详情.md) |
| POST | `/staff/members/{memberId}/activate-card` | 开通续费会员卡 | [staff/14-开通续费会员卡.md](staff/14-开通续费会员卡.md) |
| GET | `/staff/card-types` | 卡类型列表 | [staff/15-卡类型列表.md](staff/15-卡类型列表.md) |
| POST | `/staff/member-cards/buy` | 按会员码购买会员卡 | [staff/16-按会员码购买会员卡.md](staff/16-按会员码购买会员卡.md) |

## 账号管理

| 方法 | 路径 | 说明 | 详细文档 |
|---|---|---|---|
| GET | `/account/cancel-eligibility` | 注销资格查询 | [account/01-查询注销前置状态.md](account/01-查询注销前置状态.md) |
| POST | `/account/cancel` | 注销账号 | [account/02-注销账号.md](account/02-注销账号.md) |

## 文件上传

| 方法 | 路径 | 说明 | 详细文档 |
|---|---|---|---|
| POST | `/files/book-attachment-images` | 上传图书图片 | [files/01-上传图书附件图片.md](files/01-上传图书附件图片.md) |

## 通用响应格式

```json
{
  "code": 0,
  "message": "操作成功",
  "data": {},
  "requestId": "req_xxx"
}
```
