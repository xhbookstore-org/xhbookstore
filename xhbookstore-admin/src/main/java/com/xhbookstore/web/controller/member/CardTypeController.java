package com.xhbookstore.web.controller.member;

import java.util.*;
import com.alibaba.fastjson2.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.xhbookstore.common.annotation.Log;
import com.xhbookstore.common.enums.BusinessType;
import com.xhbookstore.common.core.controller.BaseController;
import com.xhbookstore.common.core.domain.AjaxResult;
import com.xhbookstore.common.core.page.TableDataInfo;
import com.xhbookstore.common.utils.SecurityUtils;
import com.xhbookstore.system.domain.member.CardType;
import com.xhbookstore.system.mapper.member.CardTypeMapper;
import com.xhbookstore.system.mapper.member.CardTypeLogMapper;

/**
 * 会员卡类型管理 — util_card_type CRUD + util_card_type_log 操作日志
 */
@RestController
@RequestMapping("/member/card-type")
public class CardTypeController extends BaseController {

    @Autowired private CardTypeMapper cardTypeMapper;
    @Autowired private CardTypeLogMapper cardTypeLogMapper;

    /** 列表 */
    @PreAuthorize("@ss.hasPermi('member:cardType:list')")
    @GetMapping("/list")
    public TableDataInfo list() {
        List<CardType> list = cardTypeMapper.selectAll();
        return getDataTable(list);
    }

    /** 详情 */
    @PreAuthorize("@ss.hasPermi('member:cardType:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Integer id) {
        CardType ct = cardTypeMapper.selectById(id);
        return ct != null ? AjaxResult.success(ct) : AjaxResult.error("卡类型不存在");
    }

    /** 新增 */
    @PreAuthorize("@ss.hasPermi('member:cardType:add')")
    @Log(title = "卡类型管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CardType ct) {
        ct.setCreateBy(SecurityUtils.getUsername());
        cardTypeMapper.insert(ct);

        // 写日志
        writeLog(ct.getId(), "CREATE", null, ct, "新增卡类型");
        return AjaxResult.success(ct);
    }

    /** 编辑 — 记录变更字段到日志 */
    @PreAuthorize("@ss.hasPermi('member:cardType:edit')")
    @Log(title = "卡类型管理", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}")
    public AjaxResult edit(@PathVariable Integer id, @RequestBody CardType ct) {
        CardType before = cardTypeMapper.selectById(id);
        if (before == null) return AjaxResult.error("卡类型不存在");

        ct.setId(id);
        ct.setUpdateBy(SecurityUtils.getUsername());
        cardTypeMapper.update(ct);

        CardType after = cardTypeMapper.selectById(id);
        // 生成变更摘要 — 对比所有业务字段
        StringBuilder sb = new StringBuilder();
        if (!Objects.equals(before.getTypeName(), after.getTypeName()))   sb.append("名称:").append(before.getTypeName()).append("→").append(after.getTypeName()).append(";");
        if (!Objects.equals(before.getPrice(), after.getPrice()))         sb.append("售价:").append(before.getPrice()).append("→").append(after.getPrice()).append(";");
        if (!Objects.equals(before.getValidDays(), after.getValidDays())) sb.append("天数:").append(before.getValidDays()).append("→").append(after.getValidDays()).append(";");
        if (!Objects.equals(before.getBorrowLimit(), after.getBorrowLimit())) sb.append("借阅上限:").append(before.getBorrowLimit()).append("→").append(after.getBorrowLimit()).append(";");
        if (!Objects.equals(before.getDiscount(), after.getDiscount()))   sb.append("折扣:").append(before.getDiscount()).append("→").append(after.getDiscount()).append(";");
        if (!Objects.equals(before.getIsRenewal(), after.getIsRenewal())) sb.append("续费:").append(before.getIsRenewal()).append("→").append(after.getIsRenewal()).append(";");
        if (!Objects.equals(before.getDescription(), after.getDescription())) sb.append("说明:").append(before.getDescription()).append("→").append(after.getDescription()).append(";");
        if (!Objects.equals(before.getSort(), after.getSort()))           sb.append("排序:").append(before.getSort()).append("→").append(after.getSort()).append(";");
        if (!Objects.equals(before.getStatus(), after.getStatus()))       sb.append("状态:").append(before.getStatus()).append("→").append(after.getStatus()).append(";");
        String remark = sb.length() > 0 ? sb.toString() : "无变更";
        writeLog(id, "UPDATE", before, after, remark);
        return AjaxResult.success(after);
    }

    /** 删除（逻辑删，is_del=1） */
    @PreAuthorize("@ss.hasPermi('member:cardType:remove')")
    @Log(title = "卡类型管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Integer id) {
        CardType before = cardTypeMapper.selectById(id);
        if (before == null) return AjaxResult.error("卡类型不存在");

        cardTypeMapper.deleteById(id);

        CardType after = cardTypeMapper.selectById(id);
        writeLog(id, "DELETE", before, after, "删除卡类型");
        return AjaxResult.success();
    }

    /** 操作日志 */
    @PreAuthorize("@ss.hasPermi('member:cardType:query')")
    @GetMapping("/{id}/log")
    public AjaxResult log(@PathVariable Integer id) {
        List<Map<String, Object>> logs = cardTypeLogMapper.selectByCardTypeId(id);
        return AjaxResult.success(logs);
    }

    /** 写操作日志到 util_card_type_log */
    private void writeLog(Integer cardTypeId, String operationType, CardType before, CardType after, String remark) {
        Map<String, Object> log = new HashMap<>();
        log.put("cardTypeId", cardTypeId);
        log.put("operationType", operationType);
        log.put("beforeData", before != null ? JSON.toJSONString(before) : null);
        log.put("afterData", after != null ? JSON.toJSONString(after) : null);
        log.put("operatorId", String.valueOf(SecurityUtils.getUserId()));
        log.put("operator", SecurityUtils.getUsername());
        log.put("remark", remark);
        cardTypeLogMapper.insert(log);
    }
}
