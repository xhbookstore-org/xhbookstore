package com.xhbookstore.system.mapper.book;

import java.util.List;
import com.xhbookstore.system.domain.book.BookBorrowDetailImage;
import org.apache.ibatis.annotations.Param;

public interface BookBorrowDetailImageMapper {
    int insert(BookBorrowDetailImage image);
    BookBorrowDetailImage selectByImageIdForUpdate(@Param("imageId") String imageId);
    int bindToDetail(@Param("imageId") String imageId,
                     @Param("memberId") Integer memberId,
                     @Param("borrowDetailId") Long borrowDetailId,
                     @Param("borrowOrderId") Long borrowOrderId,
                     @Param("borrowOrderNo") String borrowOrderNo,
                     @Param("sortOrder") Integer sortOrder);
    int countByDetail(@Param("borrowDetailId") Long borrowDetailId);
    int maxSortByDetail(@Param("borrowDetailId") Long borrowDetailId);
    List<BookBorrowDetailImage> selectByDetailId(@Param("borrowDetailId") Long borrowDetailId);
}
