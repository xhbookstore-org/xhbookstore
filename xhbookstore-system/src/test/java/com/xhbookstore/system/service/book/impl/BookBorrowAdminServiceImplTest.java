package com.xhbookstore.system.service.book.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.xhbookstore.system.domain.book.BookBorrowAdminDetail;
import com.xhbookstore.system.domain.book.BookBorrowDetailImage;
import com.xhbookstore.system.domain.book.BookBorrowRecord;
import com.xhbookstore.system.mapper.book.BookBorrowAdminMapper;
import com.xhbookstore.system.mapper.book.BookBorrowDetailImageMapper;

@ExtendWith(MockitoExtension.class)
class BookBorrowAdminServiceImplTest {
    @Mock private BookBorrowAdminMapper adminMapper;
    @Mock private BookBorrowDetailImageMapper imageMapper;
    @InjectMocks private BookBorrowAdminServiceImpl service;

    @Test
    void detailReturnsAllCopyRowsWithTheirOwnImages() {
        BookBorrowRecord query = new BookBorrowRecord();
        query.setId(10L);
        BookBorrowRecord order = new BookBorrowRecord();
        order.setId(10L);
        BookBorrowAdminDetail first = detail(101L, "A-001");
        BookBorrowAdminDetail second = detail(102L, "A-002");
        BookBorrowDetailImage firstImage = new BookBorrowDetailImage();
        firstImage.setImageId("IMG-1");
        when(adminMapper.selectOrder(query)).thenReturn(order);
        when(adminMapper.selectDetails(10L)).thenReturn(List.of(first, second));
        when(imageMapper.selectByDetailId(101L)).thenReturn(List.of(firstImage));
        when(imageMapper.selectByDetailId(102L)).thenReturn(List.of());

        Map<String, Object> result = service.selectDetail(query);

        assertThat(result.get("order")).isSameAs(order);
        assertThat(first.getImages()).extracting(BookBorrowDetailImage::getImageId).containsExactly("IMG-1");
        assertThat(second.getImages()).isEmpty();
    }

    @Test
    void detailDoesNotLoadChildrenWhenOrderIsOutsideDataScope() {
        BookBorrowRecord query = new BookBorrowRecord();
        query.setId(10L);
        when(adminMapper.selectOrder(query)).thenReturn(null);

        assertThat(service.selectDetail(query)).isNull();
        verify(adminMapper, never()).selectDetails(10L);
        verify(imageMapper, never()).selectByDetailId(101L);
    }

    private BookBorrowAdminDetail detail(Long id, String bookCode) {
        BookBorrowAdminDetail detail = new BookBorrowAdminDetail();
        detail.setId(id);
        detail.setBookCode(bookCode);
        detail.setBorrowQty(1);
        return detail;
    }
}
