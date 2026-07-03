package com.xhbookstore.system.mapper.book;

import org.apache.ibatis.annotations.Param;
import com.xhbookstore.system.domain.book.BookInfo;

public interface BookInfoMapper {
    BookInfo selectById(Long id);
    BookInfo selectByIdForUpdate(Long id);
    int decreaseLendableQty(@Param("id") Long id, @Param("qty") Integer qty);
    int increaseLendableQty(@Param("id") Long id, @Param("qty") Integer qty);
}