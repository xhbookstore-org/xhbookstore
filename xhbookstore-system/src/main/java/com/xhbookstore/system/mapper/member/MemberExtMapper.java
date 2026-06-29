package com.xhbookstore.system.mapper.member;

import com.xhbookstore.system.domain.member.MemberExt;

public interface MemberExtMapper {
    MemberExt selectByMemberId(Integer memberId);
    int insertMemberExt(MemberExt ext);
    int updateMemberExt(MemberExt ext);
    int deleteByMemberId(Integer memberId);
}