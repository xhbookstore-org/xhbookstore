package com.xhbookstore.system.service.member;

import com.xhbookstore.system.domain.member.Member;
import com.xhbookstore.system.domain.member.MemberCodeTokenInfo;

public interface IMemberCodeTokenService {
    MemberCodeTokenInfo createToken(Integer memberId, String scene, int ttlSeconds);
    Member verifyToken(String tokenOrContent, String scene);
    Member consumeToken(String tokenOrContent, String scene);
}
