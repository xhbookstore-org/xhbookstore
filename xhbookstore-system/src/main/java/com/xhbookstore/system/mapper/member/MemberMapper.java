package com.xhbookstore.system.mapper.member;

import java.util.List;
import com.xhbookstore.system.domain.member.Member;

public interface MemberMapper {
    List<Member> selectMemberList(Member member);
    Member selectMemberById(Integer id);
    Member selectMemberByIdForUpdate(Integer id);
    Member selectMemberByPhone(String phone);
    Member selectMemberByCardNo(String cardNo);
    int insertMember(Member member);
    int updateMember(Member member);
    int deleteMemberById(Integer id);
    String selectMaxCardNoByDept(String deptPrefix);
    List<Member> selectMemberListForExport(Member member);
}