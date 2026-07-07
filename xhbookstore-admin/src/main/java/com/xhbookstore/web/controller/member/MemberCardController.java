package com.xhbookstore.web.controller.member;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.xhbookstore.common.annotation.DataScope;
import com.xhbookstore.common.annotation.Log;
import com.xhbookstore.common.core.controller.BaseController;
import com.xhbookstore.common.core.domain.AjaxResult;
import com.xhbookstore.common.core.page.TableDataInfo;
import com.xhbookstore.common.enums.BusinessType;
import com.xhbookstore.common.utils.poi.ExcelUtil;
import com.xhbookstore.system.domain.member.MemberCard;
import com.xhbookstore.system.domain.member.MemberCardExport;
import com.xhbookstore.system.domain.member.MemberCardOrder;
import com.xhbookstore.system.domain.member.MemberCardRefundOrder;
import com.xhbookstore.system.mapper.member.MemberCardBizLogMapper;
import com.xhbookstore.system.mapper.member.MemberCardMapper;
import com.xhbookstore.system.mapper.member.MemberCardOrderMapper;
import com.xhbookstore.system.mapper.member.MemberCardRefundOrderMapper;
import com.xhbookstore.system.service.member.IMemberCardService;

@RestController
@RequestMapping("/member/card")
public class MemberCardController extends BaseController {
    @Autowired private IMemberCardService memberCardService;
    @Autowired private MemberCardMapper memberCardMapper;
    @Autowired private MemberCardOrderMapper memberCardOrderMapper;
    @Autowired private MemberCardRefundOrderMapper memberCardRefundOrderMapper;
    @Autowired private MemberCardBizLogMapper memberCardBizLogMapper;

    @PreAuthorize("@ss.hasPermi('member:card:list')")
    @DataScope(deptAlias = "c")
    @GetMapping("/list")
    public TableDataInfo list(MemberCard card) {
        startPage();
        List<MemberCard> list = memberCardMapper.selectList(card);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('member:card:export')")
    @Log(title = "会员卡记录", businessType = BusinessType.EXPORT)
    @DataScope(deptAlias = "c")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MemberCard card) {
        List<MemberCardExport> list = memberCardMapper.selectExportList(card);
        ExcelUtil<MemberCardExport> util = new ExcelUtil<MemberCardExport>(MemberCardExport.class);
        util.exportExcel(response, list, "会员卡记录");
    }

    @PreAuthorize("@ss.hasAnyPermi('member:cardOrder:list,member:card:list,member:card:query')")
    @DataScope(deptAlias = "o")
    @GetMapping("/order/list")
    public TableDataInfo orderList(MemberCardOrder order) {
        startPage();
        List<MemberCardOrder> list = memberCardOrderMapper.selectList(order);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasAnyPermi('member:cardRefund:list,member:card:list,member:card:query')")
    @DataScope(deptAlias = "r")
    @GetMapping("/refund-order/list")
    public TableDataInfo refundOrderList(MemberCardRefundOrder order) {
        startPage();
        List<MemberCardRefundOrder> list = memberCardRefundOrderMapper.selectList(order);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('member:card:list')")
    @GetMapping("/member/{memberId}")
    public AjaxResult memberCards(@PathVariable Integer memberId) {
        return AjaxResult.success(memberCardService.getMemberCardView(memberId));
    }

    @PreAuthorize("@ss.hasPermi('member:card:list')")
    @GetMapping("/member/{memberId}/list")
    public TableDataInfo memberCardList(@PathVariable Integer memberId) {
        startPage();
        List<MemberCard> list = memberCardService.refreshMemberCardStatus(memberId, getUsername(), getUsername(), "ADMIN");
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('member:card:query')")
    @GetMapping("/{memberCardId}/log")
    public AjaxResult logs(@PathVariable Long memberCardId) {
        return AjaxResult.success(memberCardBizLogMapper.selectByMemberCardId(memberCardId));
    }

    @PreAuthorize("@ss.hasPermi('member:card:refund')")
    @Log(title = "会员卡退卡", businessType = BusinessType.INSERT)
    @PostMapping("/{memberCardId}/refund")
    public AjaxResult refund(@PathVariable Long memberCardId, @RequestBody Map<String, Object> body) {
        MemberCard card = memberCardMapper.selectById(memberCardId);
        if (card == null) return AjaxResult.error("会员卡不存在");
        BigDecimal refundAmount = toBigDecimal(body.get("refundAmount"));
        return memberCardService.refundCard(memberCardId, refundAmount,
                stringValue(body.get("refundType")), stringValue(body.get("reason")),
                String.valueOf(getUserId()), getUsername(), getDeptId(), stringValue(body.get("remark")));
    }

    private String stringValue(Object value) {
        return value != null ? String.valueOf(value) : null;
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null || String.valueOf(value).trim().isEmpty()) return null;
        return new BigDecimal(String.valueOf(value));
    }
}
