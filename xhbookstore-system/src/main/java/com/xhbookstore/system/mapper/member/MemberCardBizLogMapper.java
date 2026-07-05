package com.xhbookstore.system.mapper.member;

import java.util.List;
import com.xhbookstore.system.domain.member.MemberCardBizLog;

public interface MemberCardBizLogMapper {
    int insert(MemberCardBizLog log);
    List<MemberCardBizLog> selectByMemberCardId(Long memberCardId);
}
