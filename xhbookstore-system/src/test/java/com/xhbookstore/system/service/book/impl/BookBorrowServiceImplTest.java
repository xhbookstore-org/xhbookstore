package com.xhbookstore.system.service.book.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.xhbookstore.system.domain.book.BookBorrowDetail;
import com.xhbookstore.system.domain.book.BookBorrowDetailImage;
import com.xhbookstore.system.domain.book.BookBorrowOrder;
import com.xhbookstore.system.domain.member.Member;
import com.xhbookstore.system.mapper.book.BookBorrowDetailImageMapper;
import com.xhbookstore.system.mapper.book.BookBorrowDetailMapper;
import com.xhbookstore.system.mapper.book.BookBorrowLogMapper;
import com.xhbookstore.system.mapper.book.BookBorrowOrderMapper;
import com.xhbookstore.system.mapper.book.BookPurchaseLogMapper;
import com.xhbookstore.system.mapper.book.BookPurchaseOrderMapper;
import com.xhbookstore.system.mapper.book.BookReturnDetailMapper;
import com.xhbookstore.system.mapper.book.BookReturnLogMapper;
import com.xhbookstore.system.mapper.member.MemberMapper;
import com.xhbookstore.system.service.book.BookBorrowException;
import com.xhbookstore.system.service.member.IPointsService;

@ExtendWith(MockitoExtension.class)
class BookBorrowServiceImplTest {

    @Mock private BookBorrowOrderMapper orderMapper;
    @Mock private BookBorrowDetailMapper detailMapper;
    @Mock private BookReturnDetailMapper returnMapper;
    @Mock private BookBorrowDetailImageMapper detailImageMapper;
    @Mock private MemberMapper memberMapper;
    @Mock private BookPurchaseOrderMapper purchaseOrderMapper;
    @Mock private BookBorrowLogMapper borrowLogMapper;
    @Mock private BookReturnLogMapper returnLogMapper;
    @Mock private BookPurchaseLogMapper purchaseLogMapper;
    @Mock private IPointsService pointsService;

    @InjectMocks private BookBorrowServiceImpl service;

    @Test
    void createBorrowOrderCreatesOneDetailPerCopyAndBindsImages() {
        Member member = member(1);
        when(memberMapper.selectMemberById(1)).thenReturn(member);
        when(detailImageMapper.selectByImageIdForUpdate("IMG-1")).thenReturn(tempImage("IMG-1", 1, "9"));
        when(detailImageMapper.selectByImageIdForUpdate("IMG-2")).thenReturn(tempImage("IMG-2", 1, "9"));
        when(detailImageMapper.selectByImageIdForUpdate("IMG-3")).thenReturn(tempImage("IMG-3", 1, "9"));
        when(detailImageMapper.selectByImageIdForUpdate("IMG-4")).thenReturn(tempImage("IMG-4", 1, "9"));
        when(detailImageMapper.selectByImageIdForUpdate("IMG-5")).thenReturn(tempImage("IMG-5", 1, "9"));
        when(detailImageMapper.selectByImageIdForUpdate("IMG-6")).thenReturn(tempImage("IMG-6", 1, "9"));
        when(detailImageMapper.bindToDetail(eq("IMG-1"), eq(1), any(), any(), any(), eq(1))).thenReturn(1);
        when(detailImageMapper.bindToDetail(eq("IMG-2"), eq(1), any(), any(), any(), eq(2))).thenReturn(1);
        when(detailImageMapper.bindToDetail(eq("IMG-3"), eq(1), any(), any(), any(), eq(3))).thenReturn(1);
        when(detailImageMapper.bindToDetail(eq("IMG-4"), eq(1), any(), any(), any(), eq(1))).thenReturn(1);
        when(detailImageMapper.bindToDetail(eq("IMG-5"), eq(1), any(), any(), any(), eq(2))).thenReturn(1);
        when(detailImageMapper.bindToDetail(eq("IMG-6"), eq(1), any(), any(), any(), eq(3))).thenReturn(1);
        when(pointsService.grantBorrowPoints(eq(1), any(), eq(2), eq(3L)))
                .thenReturn(Map.of("status", "SUCCESS", "points", 20));
        doAnswer(invocation -> {
            ((BookBorrowOrder) invocation.getArgument(0)).setId(100L);
            return 1;
        }).when(orderMapper).insert(any());
        AtomicLong detailIds = new AtomicLong(200);
        doAnswer(invocation -> {
            ((BookBorrowDetail) invocation.getArgument(0)).setId(detailIds.getAndIncrement());
            return 1;
        }).when(detailMapper).insert(any());

        service.createBorrowOrder(1, List.of(
                Map.of("bookCode", "A-001", "bookName", "同名书", "imageIds", List.of("IMG-1", "IMG-2", "IMG-3")),
                Map.of("imageIds", List.of("IMG-4", "IMG-5", "IMG-6"))),
                "remark", "9", "张店员", 3L);

        ArgumentCaptor<BookBorrowDetail> details = ArgumentCaptor.forClass(BookBorrowDetail.class);
        verify(detailMapper, org.mockito.Mockito.times(2)).insert(details.capture());
        assertThat(details.getAllValues()).extracting(BookBorrowDetail::getBookCode)
                .containsExactly("A-001", null);
        assertThat(details.getAllValues()).extracting(BookBorrowDetail::getBookName)
                .containsExactly("同名书", "借阅图书");
        assertThat(details.getAllValues()).extracting(BookBorrowDetail::getBorrowQty)
                .containsOnly(1);
        verify(detailImageMapper).bindToDetail(eq("IMG-1"), eq(1), eq(200L), eq(100L), any(), eq(1));
        verify(pointsService).grantBorrowPoints(eq(1), any(), eq(2), eq(3L));
    }

    @Test
    void createBorrowOrderRequiresExactlyThreeImagesBeforeWriting() {
        when(memberMapper.selectMemberById(1)).thenReturn(member(1));
        Map<String, Object> book = Map.of(
                "bookCode", "A-001", "bookName", "书",
                "imageIds", List.of("1", "2", "3", "4"));

        assertThatThrownBy(() -> service.createBorrowOrder(1, List.of(book), null, "9", "staff", 1L))
                .isInstanceOf(BookBorrowException.class)
                .hasMessageContaining("exactly 3 images");

        verify(orderMapper, never()).insert(any());
        verify(detailMapper, never()).insert(any());
    }

    @Test
    void returnBookDoesNotWriteWhenSecondDetailIsAlreadySettled() {
        when(detailMapper.selectByIdForUpdate(1L)).thenReturn(detail(1L, 10L, "DY1", 1, 1, 0));

        assertThatThrownBy(() -> service.returnBook(1L, "intact", null, null, "1", "staff", 1L))
                .isInstanceOf(BookBorrowException.class)
                .hasMessageContaining("already been settled");

        verify(returnMapper, never()).insert(any());
        verify(detailMapper, never()).updateReturnInfo(any());
    }

    @Test
    void borrowToPurchaseRequiresPriceBeforeWriting() {
        BookBorrowDetail detail = detail(1L, 10L, "DY1", 1, 0, 0);
        when(detailMapper.selectByIdForUpdate(1L)).thenReturn(detail);
        when(orderMapper.selectByOrderNo("DY1")).thenReturn(order(10L, "DY1"));

        assertThatThrownBy(() -> service.borrowToPurchase(
                List.of(Map.of("borrowDetailId", 1L, "purchaseQty", 1)), "1", "staff", 1L))
                .isInstanceOf(BookBorrowException.class)
                .hasMessageContaining("Unit price is required");

        verify(purchaseOrderMapper, never()).insert(any());
        verify(detailMapper, never()).updatePurchaseInfo(any());
    }

    @Test
    void borrowToPurchaseAcceptsZeroPriceWithoutBookMasterData() {
        BookBorrowDetail detail = detail(1L, 10L, "DY1", 1, 0, 0);
        BookBorrowOrder order = order(10L, "DY1");
        order.setMemberId(1);
        order.setMemberCardNo("CARD");
        when(detailMapper.selectByIdForUpdate(1L)).thenReturn(detail);
        when(detailMapper.selectById(1L)).thenReturn(detail(1L, 10L, "DY1", 1, 0, 1));
        when(orderMapper.selectByOrderNo("DY1")).thenReturn(order);
        when(detailMapper.selectByOrderId(10L)).thenReturn(List.of(detail(1L, 10L, "DY1", 1, 0, 1)));

        service.borrowToPurchase(List.of(Map.of(
                "borrowDetailId", 1L, "purchaseQty", 1, "unitPrice", BigDecimal.ZERO)),
                "1", "staff", 1L);

        verify(purchaseOrderMapper).insert(any());
        verify(detailMapper).updatePurchaseInfo(any());
    }

    private Member member(int id) {
        Member member = new Member();
        member.setId(id);
        member.setCardNo("CARD-" + id);
        return member;
    }

    private BookBorrowDetailImage tempImage(String imageId, int memberId, String staffId) {
        BookBorrowDetailImage image = new BookBorrowDetailImage();
        image.setImageId(imageId);
        image.setMemberId(memberId);
        image.setBindStatus("TEMP");
        image.setCreateStaffId(staffId);
        return image;
    }

    private BookBorrowOrder order(Long id, String orderNo) {
        BookBorrowOrder order = new BookBorrowOrder();
        order.setId(id);
        order.setOrderNo(orderNo);
        return order;
    }

    private BookBorrowDetail detail(Long id, Long orderId, String orderNo,
                                    int borrowQty, int returnedQty, int purchaseQty) {
        BookBorrowDetail detail = new BookBorrowDetail();
        detail.setId(id);
        detail.setBorrowOrderId(orderId);
        detail.setBorrowOrderNo(orderNo);
        detail.setBookCode("BOOK-" + id);
        detail.setBookName("book-" + id);
        detail.setBorrowQty(borrowQty);
        detail.setReturnedQty(returnedQty);
        detail.setPurchaseQty(purchaseQty);
        return detail;
    }
}
