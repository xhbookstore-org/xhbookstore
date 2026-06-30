package com.xhbookstore.system.mapper.book;

import java.util.List;
import com.xhbookstore.system.domain.book.BookBorrowOrder;

public interface BookBorrowOrderMapper {
    int insert(BookBorrowOrder order);
    BookBorrowOrder selectById(Long id);
    BookBorrowOrder selectByOrderNo(String orderNo);
    List<BookBorrowOrder> selectByMemberId(Integer memberId);
    int updateStatus(BookBorrowOrder order);
}
