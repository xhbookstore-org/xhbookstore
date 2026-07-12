package com.xhbookstore.system.mapper.book;

import com.xhbookstore.system.domain.book.BookBorrowDetailImage;
import org.apache.ibatis.annotations.Param;

public interface BookBorrowDetailImageMapper {
    int insert(BookBorrowDetailImage image);
    int countByDetailAndType(@Param("borrowDetailId") Long borrowDetailId,
                             @Param("imageType") Integer imageType);
}
