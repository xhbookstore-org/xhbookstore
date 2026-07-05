package com.xhbookstore.system.mapper.member;

import com.xhbookstore.system.domain.member.MemberImportDetail;

public interface MemberImportDetailMapper {
    int insert(MemberImportDetail detail);
    int updateResult(MemberImportDetail detail);
}
