package com.xhbookstore.system.service.member.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.xhbookstore.common.core.domain.AjaxResult;
import com.xhbookstore.common.utils.SecurityUtils;
import com.xhbookstore.system.domain.member.CardType;
import com.xhbookstore.system.domain.member.Member;
import com.xhbookstore.system.domain.member.MemberExt;
import com.xhbookstore.system.mapper.SysDeptMapper;
import com.xhbookstore.system.mapper.member.CardTypeMapper;
import com.xhbookstore.system.mapper.member.MemberExtMapper;
import com.xhbookstore.system.mapper.member.MemberMapper;
import com.xhbookstore.system.service.member.IMemberService;
import com.xhbookstore.common.core.domain.entity.SysDept;

@Service
public class MemberServiceImpl implements IMemberService {

    @Autowired private MemberMapper memberMapper;
    @Autowired private MemberExtMapper memberExtMapper;
    @Autowired private CardTypeMapper cardTypeMapper;
    @Autowired private SysDeptMapper deptMapper;

    @Override
    public List<Member> selectMemberList(Member member) {
        return memberMapper.selectMemberList(member);
    }

    @Override
    public Member selectMemberById(Integer id) {
        return memberMapper.selectMemberById(id);
    }

    @Override
    public MemberExt selectMemberExt(Integer memberId) {
        return memberExtMapper.selectByMemberId(memberId);
    }

    @Override
    public Member getByPhone(String phone) {
        return memberMapper.selectMemberByPhone(phone);
    }

    @Override
    public String generateCardNo(Long deptId) {
        // Get dept ERP ID as card prefix
        String prefix = String.valueOf(deptId);
        SysDept dept = deptMapper.selectDeptById(deptId);
        if (dept != null && dept.getErpDeptId() != null) {
            prefix = String.valueOf(dept.getErpDeptId());
        }

        String maxCardNo = memberMapper.selectMaxCardNoByDept(prefix);
        long seq = 1;
        if (maxCardNo != null && maxCardNo.length() >= 11) {
            String seqStr = maxCardNo.substring(prefix.length());
            try { seq = Long.parseLong(seqStr) + 1; } catch (NumberFormatException e) { /* start from 1 */ }
        }
        String seqPart = String.format("%0" + (11 - prefix.length()) + "d", seq);
        String cardNo = prefix + seqPart;
        while (memberMapper.selectMemberByCardNo(cardNo) != null) {
            seq++;
            seqPart = String.format("%0" + (11 - prefix.length()) + "d", seq);
            cardNo = prefix + seqPart;
        }
        return cardNo;
    }

    @Override
    @Transactional
    public AjaxResult insertMember(Member member, MemberExt ext) {
        // Check phone uniqueness
        if (member.getPhone() != null && memberMapper.selectMemberByPhone(member.getPhone()) != null) {
            return AjaxResult.error("该手机号已注册");
        }
        // Generate card_no
        String cardNo = generateCardNo(member.getDeptId());
        member.setCardNo(cardNo);
        member.setStatus(0);
        member.setBorrowCountValid(0);
        member.setCurrentPoints(0);
        member.setSource("manual");
        member.setSyncErp(0);
        member.setLastOperator(SecurityUtils.getUsername());

        // Calculate valid_date based on card type
        CardType cardType = cardTypeMapper.selectById(member.getCardTypeId());
        if (cardType != null && cardType.getIsRenewal() == 0) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, cardType.getValidDays());
            member.setValidDate(cal.getTime());
        }

        memberMapper.insertMember(member);

        if (ext != null) {
            ext.setMemberId(member.getId());
            ext.setJoinDate(new Date());
            ext.setTotalPoints(0);
            ext.setLevelPoints(0);
            ext.setBusinessStaffName(SecurityUtils.getUsername());
            memberExtMapper.insertMemberExt(ext);
        }

        return AjaxResult.success("新增成功");
    }

    @Override
    @Transactional
    public AjaxResult updateMember(Member member, MemberExt ext) {
        Member existing = memberMapper.selectMemberById(member.getId());
        if (existing == null) return AjaxResult.error("会员不存在");

        // Check phone uniqueness
        if (member.getPhone() != null) {
            Member phoneCheck = memberMapper.selectMemberByPhone(member.getPhone());
            if (phoneCheck != null && !phoneCheck.getId().equals(member.getId())) {
                return AjaxResult.error("该手机号已被其他会员使用");
            }
        }

        // Calculate valid_date for renewal
        CardType cardType = cardTypeMapper.selectById(member.getCardTypeId());
        if (cardType != null) {
            Calendar cal = Calendar.getInstance();
            if (cardType.getIsRenewal() == 1) {
                // Renewal: extend from original valid_date
                if (existing.getValidDate() != null) {
                    cal.setTime(existing.getValidDate());
                }
            }
            cal.add(Calendar.DAY_OF_YEAR, cardType.getValidDays());
            member.setValidDate(cal.getTime());
        }

        member.setLastOperator(SecurityUtils.getUsername());
        memberMapper.updateMember(member);

        if (ext != null) {
            ext.setMemberId(member.getId());
            MemberExt existingExt = memberExtMapper.selectByMemberId(member.getId());
            if (existingExt != null) {
                memberExtMapper.updateMemberExt(ext);
            } else {
                ext.setJoinDate(existing.getCreatedAt());
                memberExtMapper.insertMemberExt(ext);
            }
        }

        return AjaxResult.success("编辑成功");
    }

    @Override
    public AjaxResult deleteMember(Integer id) {
        memberMapper.deleteMemberById(id);
        return AjaxResult.success("删除成功");
    }

    @Override
    public List<Member> selectMemberListForExport(Member member) {
        return memberMapper.selectMemberListForExport(member);
    }
}