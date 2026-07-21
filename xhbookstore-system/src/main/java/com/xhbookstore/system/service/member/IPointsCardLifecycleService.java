package com.xhbookstore.system.service.member;

import java.util.Map;
import com.xhbookstore.system.domain.member.Member;
import com.xhbookstore.system.domain.member.MemberCard;

/** 会员卡购卡积分的冻结、解冻和退卡冲销生命周期。 */
public interface IPointsCardLifecycleService {
    Map<String, Object> grantFrozenCardPoints(Member member, MemberCard card, boolean renewal);
    Map<String, Integer> processPending(int limit);
}
