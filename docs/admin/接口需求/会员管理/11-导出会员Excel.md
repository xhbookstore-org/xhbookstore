# 导出会员 Excel

## 基本信息

| 项 | 内容 |
|---|---|
| 接口 | `POST /member/export` |
| 权限 | `member:member:export` |
| 日志 | `@Log(title = "会员管理", businessType = EXPORT)` |
| 数据权限 | `@DataScope(deptAlias = "m")` |
| 返回类型 | Excel 文件流 |

## 入参

| 字段 | 位置 | 类型 | 必填 | 说明 |
|---|---|---|---|---|
| status | form/query | int | 否 | 会员状态 |
| name | form/query | string | 否 | 姓名，模糊查询 |
| cardNo | form/query | string | 否 | 会员编号，模糊查询 |
| phone | form/query | string | 否 | 手机号，模糊查询 |
| deptId | form/query | long | 否 | 门店 ID |
| params[beginTime] | form/query | string | 否 | 创建开始日期 |
| params[endTime] | form/query | string | 否 | 创建结束日期 |

## 完整请求报文

```http
POST /member/export HTTP/1.1
Host: 152.136.127.168
Authorization: Bearer <token>
Content-Type: application/x-www-form-urlencoded

deptId=103&cardNo=65000000001&name=张三&params[beginTime]=2026-07-01&params[endTime]=2026-07-31
```

## 出参

| 内容 | 说明 |
|---|---|
| Content-Type | `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet` |
| 文件内容 | Excel 文件 |
| 导出范围 | 符合查询条件和数据权限的全量数据，不按分页截断 |

## Excel 字段

导出字段按导入 Excel 格式，包括会员编号、姓名、卡类型、级别、折扣、累计积分、当前积分、等级积分、注销标记、挂失标记、手机号、消费金额、消费次数、入会日期、性别、年龄、备注、单位电话、有效期、微信、微博、上级积分比例、上级姓名、业务员等。

## 备注

- 前端下载文件名规则：选择门店时为“门店名会员_yyyyMMddHHmmss.xlsx”，未选择门店时为“全部会员_yyyyMMddHHmmss.xlsx”。
