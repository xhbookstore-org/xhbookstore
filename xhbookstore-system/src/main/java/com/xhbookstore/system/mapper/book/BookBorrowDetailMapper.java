package com.xhbookstore.system.mapper.book;

import java.util.List;
import com.xhbookstore.system.domain.book.BookBorrowDetail;

public interface BookBorrowDetailMapper {
    int insert(BookBorrowDetail detail);
    BookBorrowDetail selectById(Long id);
    BookBorrowDetail selectByIdForUpdate(Long id);
    List<BookBorrowDetail> selectByOrderId(Long borrowOrderId);
    List<BookBorrowDetail> selectByMemberId(Integer memberId);
    int updateReturnInfo(BookBorrowDetail detail);
}
