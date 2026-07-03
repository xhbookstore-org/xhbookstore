package com.xhbookstore.system.mapper.book;

import java.util.Map;

public interface BookBorrowLogMapper {
    int insert(Map<String, Object> log);
}