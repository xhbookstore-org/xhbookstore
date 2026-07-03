package com.xhbookstore.system.mapper.book;

import java.util.Map;

public interface BookPurchaseLogMapper {
    int insert(Map<String, Object> log);
}