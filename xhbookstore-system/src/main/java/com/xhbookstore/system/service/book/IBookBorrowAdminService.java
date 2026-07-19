package com.xhbookstore.system.service.book;

import java.util.List;
import java.util.Map;
import com.xhbookstore.system.domain.book.BookBorrowDetailExport;
import com.xhbookstore.system.domain.book.BookBorrowRecord;

public interface IBookBorrowAdminService {
    List<BookBorrowRecord> selectList(BookBorrowRecord query);
    Map<String, Object> selectDetail(BookBorrowRecord query);
    List<BookBorrowDetailExport> selectExportList(BookBorrowRecord query);
}
