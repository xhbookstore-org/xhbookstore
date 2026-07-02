package com.xhbookstore.web.controller.member;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.xhbookstore.common.core.controller.BaseController;
import com.xhbookstore.common.core.domain.AjaxResult;
import com.xhbookstore.common.core.page.TableDataInfo;
import com.xhbookstore.common.core.domain.entity.SysDictData;
import com.xhbookstore.system.domain.member.Member;
import com.xhbookstore.system.domain.member.MemberExt;
import com.xhbookstore.system.domain.member.PointsOrder;
import com.xhbookstore.system.mapper.SysDictDataMapper;
import com.xhbookstore.system.service.member.IMemberService;
import com.xhbookstore.system.service.member.IPointsService;

@RestController
@RequestMapping("/member")
public class MemberController extends BaseController {

    @Autowired private IMemberService memberService;
    @Autowired private SysDictDataMapper dictDataMapper;
    @Autowired private IPointsService pointsService;

    @GetMapping("/list")
    public TableDataInfo list(Member member) {
        startPage();
        List<Member> list = memberService.selectMemberList(member);
        return getDataTable(list);
    }

    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable Integer id) {
        Member member = memberService.selectMemberById(id);
        MemberExt ext = memberService.selectMemberExt(id);
        AjaxResult result = AjaxResult.success();
        result.put("member", member);
        result.put("ext", ext);
        return result;
    }

    @PostMapping
    public AjaxResult add(@RequestBody Member member) {
        if (member.getDeptId() == null) {
            member.setDeptId(getLoginUser().getDeptId());
        }
        return memberService.insertMember(member, null);
    }

    @PutMapping(value = "/{id}")
    public AjaxResult edit(@PathVariable Integer id, @RequestBody Member member) {
        member.setId(id);
        return memberService.updateMember(member, null);
    }

    @DeleteMapping(value = "/{id}")
    public AjaxResult remove(@PathVariable Integer id) {
        return memberService.deleteMember(id);
    }

    @GetMapping("/generateCardNo")
    public AjaxResult generateCardNo(@RequestParam(required = false) Long deptId) {
        if (deptId == null) {
            deptId = getLoginUser().getDeptId();
        }
        String cardNo = memberService.generateCardNo(deptId);
        return AjaxResult.success("ok", cardNo);
    }

    @GetMapping("/cardTypes")
    public AjaxResult cardTypes() {
        List<SysDictData> list = dictDataMapper.selectDictDataByType("sys_member_type");
        return AjaxResult.success(list);
    }

    @GetMapping("/{id}/points")
    public AjaxResult pointsList(@PathVariable Integer id) {
        List<PointsOrder> list = pointsService.selectByMemberId(id);
        return AjaxResult.success(list);
    }

    @PostMapping("/{id}/points")
    public AjaxResult addPoints(@PathVariable Integer id, @RequestBody PointsAddRequest request) {
        return pointsService.addPoints(id, request.getPoints(), request.getDescription(),
                getLoginUser().getUsername(), "PC");
    }

    @GetMapping("/export")
    public AjaxResult export(Member member) {
        List<Member> list = memberService.selectMemberListForExport(member);
        return AjaxResult.success(list);
    }
}