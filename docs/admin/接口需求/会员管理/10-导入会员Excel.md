# 导入会员 Excel

## 基本信息

| 项 | 内容 |
|---|---|
| 接口 | `POST /member/importData` |
| 权限 | `member:member:import` |
| 日志 | `@Log(title = "会员导入", businessType = IMPORT)` |
| 请求类型 | `multipart/form-data` |
| 返回类型 | `AjaxResult` |

## 入参

| 字段 | 位置 | 类型 | 必填 | 说明 |
|---|---|---|---|---|
| file | form-data | file | 是 | Excel 文件，格式按现有会员明细模板 |
| deptId | form-data | long | 否 | 导入门店；为空使用当前登录人门店 |

## 完整请求报文

```http
POST /member/importData HTTP/1.1
Host: 152.136.127.168
Authorization: Bearer <token>
Content-Type: multipart/form-data; boundary=----WebKitFormBoundaryDemo

------WebKitFormBoundaryDemo
Content-Disposition: form-data; name="deptId"

103
------WebKitFormBoundaryDemo
Content-Disposition: form-data; name="file"; filename="现有会员明细.xls"
Content-Type: application/vnd.ms-excel

<binary>
------WebKitFormBoundaryDemo--
```

## 出参

| 字段 | 类型 | 说明 |
|---|---|---|
| code | int | 状态码 |
| msg | string | 导入提示 |
| importLogId | int | 导入日志 ID |
| totalCount | int | 读取总行数 |
| successCount | int | 成功导入数 |
| failureCount | int | 失败数 |
| skippedCount | int | 未导入数 |
| failureRows | array | 失败明细 |
| skippedRows | array | 未导入明细 |
| warningRows | array | 导入成功但有提醒的明细 |

## 完整响应报文

```json
{
  "code": 200,
  "msg": "导入完成",
  "importLogId": 12,
  "totalCount": 99,
  "successCount": 80,
  "failureCount": 3,
  "skippedCount": 16,
  "failureRows": [
    {
      "rowIndex": 8,
      "name": "王五",
      "cardNo": "65000000030",
      "reason": "会员卡类型不存在：360卡"
    }
  ],
  "skippedRows": [
    {
      "rowIndex": 9,
      "name": "赵六",
      "cardNo": "65000000031",
      "reason": "未导入：会员卡号已存在，不再重新导入"
    }
  ],
  "warningRows": []
}
```

## 业务说明

- 当前登录人作为操作人写入导入日志。
- 新增会员写入来源字段，导入来源为 ERP 导入。
- 会员编号/会员卡号已存在时归为“未导入”，不算成功，也不算失败。
- Excel 中卡类型在系统中不存在时不入库，并写入结果明细。
- 导入过程写入 `member_import_log` 和 `member_import_detail`。
