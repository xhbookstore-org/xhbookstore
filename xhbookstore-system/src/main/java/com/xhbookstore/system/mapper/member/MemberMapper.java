package com.xhbookstore.system.mapper.member;

import java.util.List;
import java.util.Date;
import org.apache.ibatis.annotations.Param;
import com.xhbookstore.system.domain.member.Member;
import com.xhbookstore.system.domain.member.MemberExport;

public interface MemberMapper {
    List<Member> selectMemberList(Member member);
    Member selectMemberById(Integer id);
    Member selectMemberByIdForUpdate(Integer id);
    Member selectMemberByPhone(String phone);
    Member selectMemberByCardNo(String cardNo);
    Integer selectLevelIdByName(String levelName);
    int insertMember(Member member);
    int updateMember(Member member);
    int deleteMemberById(Integer id);
    String selectMaxCardNoByDept(String deptPrefix);
    List<Member> selectMemberListForExport(Member member);
    List<MemberExport> selectMemberExportList(Member member);
    int updateCurrentCard(@Param("id") Integer id, @Param("cardTypeId") Integer cardTypeId,
                          @Param("validDate") Date validDate, @Param("status") Integer status,
                          @Param("lastOperator") String lastOperator);
}
