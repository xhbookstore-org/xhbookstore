package com.xhbookstore.system.mapper.book;

import com.xhbookstore.system.domain.book.BookImage;
import org.apache.ibatis.annotations.Param;

public interface BookImageMapper {
    int insert(BookImage image);
    int countByBookAndType(@Param("bookId") Long bookId, @Param("imageType") Integer imageType);
}
