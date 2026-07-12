package com.xhbookstore.system.service.book.impl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

import com.xhbookstore.system.domain.book.BookBorrowDetail;
import com.xhbookstore.system.domain.book.BookBorrowOrder;
import com.xhbookstore.system.domain.book.BookInfo;
import com.xhbookstore.system.domain.member.Member;
import com.xhbookstore.system.mapper.book.BookBorrowDetailImageMapper;
import com.xhbookstore.system.mapper.book.BookBorrowDetailMapper;
import com.xhbookstore.system.mapper.book.BookBorrowLogMapper;
import com.xhbookstore.system.mapper.book.BookBorrowOrderMapper;
import com.xhbookstore.system.mapper.book.BookInfoHistoryMapper;
import com.xhbookstore.system.mapper.book.BookInfoMapper;
import com.xhbookstore.system.mapper.book.BookPurchaseLogMapper;
import com.xhbookstore.system.mapper.book.BookPurchaseOrderMapper;
import com.xhbookstore.system.mapper.book.BookReturnDetailMapper;
import com.xhbookstore.system.mapper.book.BookReturnLogMapper;
import com.xhbookstore.system.mapper.member.MemberMapper;
import com.xhbookstore.system.service.book.BookBorrowException;

@ExtendWith(MockitoExtension.class)
class BookBorrowServiceImplTest {

    @Mock private BookBorrowOrderMapper orderMapper;
    @Mock private BookBorrowDetailMapper detailMapper;
    @Mock private BookReturnDetailMapper returnMapper;
    @Mock private BookBorrowDetailImageMapper detailImageMapper;
    @Mock private MemberMapper memberMapper;
    @Mock private BookInfoMapper bookInfoMapper;
    @Mock private BookPurchaseOrderMapper purchaseOrderMapper;
    @Mock private BookBorrowLogMapper borrowLogMapper;
    @Mock private BookReturnLogMapper returnLogMapper;
    @Mock private BookPurchaseLogMapper purchaseLogMapper;
    @Mock private BookInfoHistoryMapper bookInfoHistoryMapper;

    @InjectMocks private BookBorrowServiceImpl service;

    @Test
    void createBorrowOrderDoesNotWriteWhenSecondBookFailsValidation() {
        Member member = new Member();
        member.setId(1);
        when(memberMapper.selectMemberById(1)).thenReturn(member);
        when(bookInfoMapper.selectByIdForUpdate(1L)).thenReturn(book(1L, "first", 5, 5));
        when(bookInfoMapper.selectByIdForUpdate(2L)).thenReturn(book(2L, "second", 0, 5));

        List<Map<String, Object>> books = List.of(
                Map.of("bookId", 1L, "borrowQty", 1),
                Map.of("bookId", 2L, "borrowQty", 1));

        assertThatThrownBy(() -> service.createBorrowOrder(1, books, null, "1", "staff", 1L, null))
                .isInstanceOf(BookBorrowException.class)
                .hasMessageContaining("Not enough lendable stock");

        verify(bookInfoMapper, never()).decreaseLendableQty(any(), any());
        verify(orderMapper, never()).insert(any());
        verify(detailMapper, never()).insert(any());
    }

    @Test
    void returnBookDoesNotWriteWhenSecondDetailFailsValidation() {
        BookBorrowOrder order = order(10L, "DY1");
        when(orderMapper.selectByOrderNo("DY1")).thenReturn(order);
        when(detailMapper.selectByIdForUpdate(1L)).thenReturn(detail(1L, 10L, "DY1", 101L, 2, 0, 0));
        when(detailMapper.selectByIdForUpdate(2L)).thenReturn(detail(2L, 10L, "DY1", 102L, 1, 1, 0));
        when(bookInfoMapper.selectByIdForUpdate(101L)).thenReturn(book(101L, "first", 1, 1));

        List<Map<String, Object>> items = List.of(
                Map.of("borrowDetailId", 1L, "returnQty", 1),
                Map.of("borrowDetailId", 2L, "returnQty", 1));

        assertThatThrownBy(() -> service.returnBook("DY1", items, "1", "staff", 1L))
                .isInstanceOf(BookBorrowException.class)
                .hasMessageContaining("exceeds remaining quantity");

        verify(returnMapper, never()).insert(any());
        verify(detailMapper, never()).updateReturnInfo(any());
        verify(bookInfoMapper, never()).increaseLendableQty(any(), any());
    }

    @Test
    void borrowToPurchaseDoesNotWriteWhenSecondBookHasInsufficientStock() {
        BookBorrowOrder order = order(10L, "DY1");
        BookBorrowDetail first = detail(1L, 10L, "DY1", 101L, 1, 0, 0);
        BookBorrowDetail second = detail(2L, 10L, "DY1", 102L, 1, 0, 0);
        when(detailMapper.selectByIdForUpdate(1L)).thenReturn(first);
        when(detailMapper.selectByIdForUpdate(2L)).thenReturn(second);
        when(orderMapper.selectByOrderNo("DY1")).thenReturn(order);
        when(bookInfoMapper.selectByIdForUpdate(101L)).thenReturn(book(101L, "first", 1, 2));
        when(bookInfoMapper.selectByIdForUpdate(102L)).thenReturn(book(102L, "second", 1, 0));

        List<Map<String, Object>> items = List.of(
                Map.of("borrowDetailId", 1L, "purchaseQty", 1),
                Map.of("borrowDetailId", 2L, "purchaseQty", 1));

        assertThatThrownBy(() -> service.borrowToPurchase(items, "1", "staff", 1L))
                .isInstanceOf(BookBorrowException.class)
                .hasMessageContaining("Not enough stock");

        verify(purchaseOrderMapper, never()).insert(any());
        verify(detailMapper, never()).updatePurchaseInfo(any());
        verify(bookInfoMapper, never()).decreaseStockQty(any(), any());
    }

    private BookInfo book(Long id, String name, int lendableQty, int stockQty) {
        BookInfo book = new BookInfo();
        book.setId(id);
        book.setBookName(name);
        book.setLendableQty(lendableQty);
        book.setStockQty(stockQty);
        return book;
    }

    private BookBorrowOrder order(Long id, String orderNo) {
        BookBorrowOrder order = new BookBorrowOrder();
        order.setId(id);
        order.setOrderNo(orderNo);
        return order;
    }

    private BookBorrowDetail detail(Long id, Long orderId, String orderNo, Long bookId,
                                    int borrowQty, int returnedQty, int purchaseQty) {
        BookBorrowDetail detail = new BookBorrowDetail();
        detail.setId(id);
        detail.setBorrowOrderId(orderId);
        detail.setBorrowOrderNo(orderNo);
        detail.setBookId(bookId);
        detail.setBookName("book-" + bookId);
        detail.setBorrowQty(borrowQty);
        detail.setReturnedQty(returnedQty);
        detail.setPurchaseQty(purchaseQty);
        return detail;
    }
}
