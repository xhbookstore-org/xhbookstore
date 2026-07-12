package com.xhbookstore.system.mapper.book;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.xhbookstore.system.domain.book.BookBorrowDetail;

public interface BookBorrowDetailMapper {
    int insert(BookBorrowDetail detail);
    BookBorrowDetail selectById(Long id);
    BookBorrowDetail selectByIdForUpdate(Long id);
    List<BookBorrowDetail> selectByOrderId(Long borrowOrderId);
    List<BookBorrowDetail> selectByMemberId(Integer memberId);
    int updateReturnInfo(BookBorrowDetail detail);
    int updatePurchaseInfo(BookBorrowDetail detail);
    List<Map<String, Object>> selectPage(@Param("phone") String phone,
                                         @Param("status") Integer status,
                                         @Param("memberId") Integer memberId,
                                         @Param("borrowingOnly") boolean borrowingOnly,
                                         @Param("offset") int offset,
                                         @Param("limit") int limit);
    long countPage(@Param("phone") String phone,
                   @Param("status") Integer status,
                   @Param("memberId") Integer memberId,
                   @Param("borrowingOnly") boolean borrowingOnly);
    int sumRemainingByMemberId(@Param("memberId") Integer memberId);
}
