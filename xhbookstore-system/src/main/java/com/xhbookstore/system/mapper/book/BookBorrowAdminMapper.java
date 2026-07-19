package com.xhbookstore.system.mapper.book;

import java.util.List;
import com.xhbookstore.system.domain.book.BookBorrowAdminDetail;
import com.xhbookstore.system.domain.book.BookBorrowDetailExport;
import com.xhbookstore.system.domain.book.BookBorrowRecord;

public interface BookBorrowAdminMapper {
    List<BookBorrowRecord> selectOrderList(BookBorrowRecord query);
    BookBorrowRecord selectOrder(BookBorrowRecord query);
    List<BookBorrowAdminDetail> selectDetails(Long borrowOrderId);
    List<BookBorrowDetailExport> selectExportList(BookBorrowRecord query);
}
