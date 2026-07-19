package com.xhbookstore.system.service.book.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.xhbookstore.common.annotation.DataScope;
import com.xhbookstore.system.domain.book.BookBorrowAdminDetail;
import com.xhbookstore.system.domain.book.BookBorrowDetailExport;
import com.xhbookstore.system.domain.book.BookBorrowRecord;
import com.xhbookstore.system.mapper.book.BookBorrowAdminMapper;
import com.xhbookstore.system.mapper.book.BookBorrowDetailImageMapper;
import com.xhbookstore.system.service.book.IBookBorrowAdminService;

@Service
public class BookBorrowAdminServiceImpl implements IBookBorrowAdminService {
    @Autowired private BookBorrowAdminMapper adminMapper;
    @Autowired private BookBorrowDetailImageMapper imageMapper;

    @Override
    @DataScope(deptAlias = "o", permission = "borrow:record:list")
    public List<BookBorrowRecord> selectList(BookBorrowRecord query) {
        return adminMapper.selectOrderList(query);
    }

    @Override
    @DataScope(deptAlias = "o", permission = "borrow:record:query")
    public Map<String, Object> selectDetail(BookBorrowRecord query) {
        BookBorrowRecord order = adminMapper.selectOrder(query);
        if (order == null) return null;
        List<BookBorrowAdminDetail> details = adminMapper.selectDetails(order.getId());
        for (BookBorrowAdminDetail detail : details) {
            detail.setImages(imageMapper.selectByDetailId(detail.getId()));
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("order", order);
        result.put("details", details);
        return result;
    }

    @Override
    @DataScope(deptAlias = "o", permission = "borrow:record:export")
    public List<BookBorrowDetailExport> selectExportList(BookBorrowRecord query) {
        return adminMapper.selectExportList(query);
    }
}
