package com.xhbookstore.system.mapper.book;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.xhbookstore.system.domain.book.BookBorrowOrder;

public interface BookBorrowOrderMapper {
    int insert(BookBorrowOrder order);
    BookBorrowOrder selectById(Long id);
    BookBorrowOrder selectByOrderNo(String orderNo);
    List<BookBorrowOrder> selectByMemberId(Integer memberId);
    List<BookBorrowOrder> selectList(@Param("phone") String phone, @Param("status") Integer status,
                                      @Param("deptIds") List<Long> deptIds);
    int countTodayByDeptId(@Param("deptId") Long deptId);
    int countTodayByStaffId(@Param("staffId") String staffId);
    int countByMemberId(@Param("memberId") Integer memberId);
    int sumCurrentYearBorrowQtyByMemberId(@Param("memberId") Integer memberId);
    int updateStatus(BookBorrowOrder order);
}
