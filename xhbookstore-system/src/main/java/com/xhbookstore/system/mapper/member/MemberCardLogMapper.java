package com.xhbookstore.system.mapper.member;

import java.util.List;
import com.xhbookstore.system.domain.member.MemberCardLog;

public interface MemberCardLogMapper {
    int insert(MemberCardLog log);
    List<MemberCardLog> selectByMemberId(Integer memberId);
}
