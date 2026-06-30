package com.xhbookstore.system.mapper.book;

import java.util.List;
import com.xhbookstore.system.domain.book.BookReturnDetail;

public interface BookReturnDetailMapper {
    int insert(BookReturnDetail detail);
    List<BookReturnDetail> selectByBorrowDetailId(Long borrowDetailId);
    List<BookReturnDetail> selectByBorrowOrderId(Long borrowOrderId);
    String selectMaxReturnOrderNo(String prefix);
}
