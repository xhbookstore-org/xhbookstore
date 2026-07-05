package com.xhbookstore.system.mapper.member;

import com.xhbookstore.system.domain.member.MemberImportLog;

public interface MemberImportLogMapper {
    int insert(MemberImportLog log);
    int updateResult(MemberImportLog log);
}
