package com.xhbookstore.web.controller.member;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.xhbookstore.common.annotation.Log;
import com.xhbookstore.common.enums.BusinessType;
import com.xhbookstore.common.core.controller.BaseController;
import com.xhbookstore.common.core.domain.AjaxResult;
import com.xhbookstore.common.core.domain.entity.SysDept;
import com.xhbookstore.common.core.page.TableDataInfo;
import com.xhbookstore.common.utils.poi.ExcelUtil;
import com.xhbookstore.system.domain.member.CardType;
import com.xhbookstore.system.domain.member.Member;
import com.xhbookstore.system.domain.member.MemberExport;
import com.xhbookstore.system.domain.member.MemberExt;
import com.xhbookstore.system.domain.member.PointsOrder;
import com.xhbookstore.system.domain.member.PointsRule;
import com.xhbookstore.system.mapper.member.CardTypeMapper;
import com.xhbookstore.system.service.member.IMemberService;
import com.xhbookstore.system.service.member.IPointsService;
import com.xhbookstore.system.service.ISysDeptService;

@RestController
@RequestMapping("/member")
public class MemberController extends BaseController {

    @Autowired private IMemberService memberService;
    @Autowired private CardTypeMapper cardTypeMapper;
    @Autowired private IPointsService pointsService;
    @Autowired private ISysDeptService deptService;

    @PreAuthorize("@ss.hasPermi('member:member:list')")
    @GetMapping("/list")
    public TableDataInfo list(Member member) {
        startPage();
        List<Member> list = memberService.selectMemberList(member);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('member:member:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable Integer id) {
        Member member = memberService.selectMemberById(id);
        MemberExt ext = memberService.selectMemberExt(id);
        AjaxResult result = AjaxResult.success();
        result.put("member", member);
        result.put("ext", ext);
        return result;
    }

    @PreAuthorize("@ss.hasPermi('member:member:add')")
    @Log(title = "会员管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Member member) {
        if (member.getDeptId() == null) {
            member.setDeptId(getLoginUser().getDeptId());
        }
        return memberService.insertMember(member, null);
    }

    @PreAuthorize("@ss.hasPermi('member:member:edit')")
    @Log(title = "会员管理", businessType = BusinessType.UPDATE)
    @PutMapping(value = "/{id}")
    public AjaxResult edit(@PathVariable Integer id, @RequestBody Member member) {
        member.setId(id);
        // 会员卡身份必须通过开卡/续卡/退卡链路变更，避免资料编辑绕过订单、卡记录和日志。
        member.setCardTypeId(null);
        member.setValidDate(null);
        return memberService.updateMember(member, null);
    }

    @PreAuthorize("@ss.hasPermi('member:member:remove')")
    @Log(title = "会员管理", businessType = BusinessType.DELETE)
    @DeleteMapping(value = "/{id}")
    public AjaxResult remove(@PathVariable Integer id) {
        return memberService.deleteMember(id);
    }

    @PreAuthorize("@ss.hasPermi('member:member:add')")
    @GetMapping("/generateCardNo")
    public AjaxResult generateCardNo(@RequestParam(required = false) Long deptId) {
        if (deptId == null) {
            deptId = getLoginUser().getDeptId();
        }
        String cardNo = memberService.generateCardNo(deptId);
        return AjaxResult.success("ok", cardNo);
    }

    @PreAuthorize("@ss.hasAnyPermi('member:member:list,member:card:list,member:cardOrder:list,member:cardRefund:list')")
    @GetMapping("/cardTypes")
    public AjaxResult cardTypes() {
        // 数据源改为 util_card_type（筛选 is_del=0）
        List<CardType> list = cardTypeMapper.selectAll();
        return AjaxResult.success(list);
    }

    @PreAuthorize("@ss.hasAnyPermi('member:member:list,member:card:list,member:cardOrder:list,member:cardRefund:list')")
    @GetMapping("/depts")
    public AjaxResult memberDepts() {
        SysDept query = new SysDept();
        query.setStatus("0");
        return AjaxResult.success(deptService.selectDeptList(query));
    }

    @PreAuthorize("@ss.hasPermi('member:member:query')")
    @GetMapping("/{id}/points")
    public AjaxResult pointsList(@PathVariable Integer id) {
        List<PointsOrder> list = pointsService.selectByMemberId(id);
        return AjaxResult.success(list);
    }

    @PreAuthorize("@ss.hasPermi('member:member:points')")
    @GetMapping("/{id}/points/rules")
    public AjaxResult pointsRules(@PathVariable Integer id, @RequestParam String direction) {
        List<PointsRule> list = pointsService.selectManualFixedRules(id, direction);
        return AjaxResult.success(list);
    }

    @PreAuthorize("@ss.hasPermi('member:member:points')")
    @Log(title = "会员积分", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/points/adjust")
    public AjaxResult adjustPoints(@PathVariable Integer id, @RequestBody PointsAdjustRequest request) {
        return pointsService.adjustPointsByRule(id, request.getRuleId(), request.getPoints(), request.getDescription(),
                getUserId(), getUsername(), "PC");
    }

    @PreAuthorize("@ss.hasPermi('member:member:points')")
    @Log(title = "会员积分", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/points")
    public AjaxResult addPoints(@PathVariable Integer id, @RequestBody PointsAddRequest request) {
        return pointsService.addPoints(id, request.getPoints(), request.getDescription(),
                getLoginUser().getUsername(), "PC");
    }

    @PreAuthorize("@ss.hasPermi('member:member:import')")
    @Log(title = "会员导入", businessType = BusinessType.IMPORT)
    @PostMapping("/importData")
    public AjaxResult importData(@RequestParam("file") org.springframework.web.multipart.MultipartFile file,
                                 @RequestParam(required = false) Long deptId) throws Exception {
        if (deptId == null) {
            deptId = getLoginUser().getDeptId();
        }
        return memberService.importMembers(file, deptId, getUsername());
    }

    @PreAuthorize("@ss.hasPermi('member:member:export')")
    @Log(title = "会员管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Member member) {
        List<MemberExport> list = memberService.selectMemberExportList(member);
        ExcelUtil<MemberExport> util = new ExcelUtil<MemberExport>(MemberExport.class);
        util.exportExcel(response, list, "现有会员明细");
    }
}
