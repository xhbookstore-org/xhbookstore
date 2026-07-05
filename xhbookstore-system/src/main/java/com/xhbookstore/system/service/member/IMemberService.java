package com.xhbookstore.system.service.member;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import com.xhbookstore.common.core.domain.AjaxResult;
import com.xhbookstore.system.domain.member.Member;
import com.xhbookstore.system.domain.member.MemberExport;
import com.xhbookstore.system.domain.member.MemberExt;

public interface IMemberService {
    List<Member> selectMemberList(Member member);
    Member selectMemberById(Integer id);
    MemberExt selectMemberExt(Integer memberId);
    Member getByPhone(String phone);
    String generateCardNo(Long deptId);
    AjaxResult insertMember(Member member, MemberExt ext);
    AjaxResult updateMember(Member member, MemberExt ext);
    AjaxResult deleteMember(Integer id);
    List<Member> selectMemberListForExport(Member member);
    List<MemberExport> selectMemberExportList(Member member);
    AjaxResult importMembers(MultipartFile file, Long deptId, String operator) throws Exception;
}
