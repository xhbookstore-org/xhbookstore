package com.xhbookstore.system.mapper.member;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.xhbookstore.system.domain.member.MemberCard;
import com.xhbookstore.system.domain.member.MemberCardExport;

public interface MemberCardMapper {
    int insert(MemberCard card);
    MemberCard selectById(Long id);
    MemberCard selectBySaleOrderNo(String saleOrderNo);
    MemberCard selectByIdForUpdate(Long id);
    MemberCard selectActiveByMemberIdForUpdate(Integer memberId);
    MemberCard selectNextPendingByMemberIdForUpdate(Integer memberId);
    List<MemberCard> selectPendingByMemberIdForUpdate(Integer memberId);
    List<MemberCard> selectByMemberId(Integer memberId);
    List<MemberCard> selectList(MemberCard card);
    List<MemberCardExport> selectExportList(MemberCard card);
    int activate(@Param("id") Long id, @Param("effectiveAt") java.util.Date effectiveAt,
                 @Param("expiredAt") java.util.Date expiredAt);
    int updateSchedule(@Param("id") Long id, @Param("effectiveAt") java.util.Date effectiveAt,
                       @Param("expiredAt") java.util.Date expiredAt);
    int expire(@Param("id") Long id);
    int refund(@Param("id") Long id, @Param("refundOrderNo") String refundOrderNo);
}
