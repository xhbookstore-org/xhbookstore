package com.xhbookstore.system.service.book;

import java.util.List;
import java.util.Map;
import com.xhbookstore.common.core.domain.AjaxResult;
import com.xhbookstore.system.domain.book.BookBorrowOrder;
import com.xhbookstore.system.domain.book.BookBorrowDetail;
import com.xhbookstore.system.domain.book.BookReturnDetail;
import com.xhbookstore.system.domain.book.BookBorrowDetailImage;

public interface IBookBorrowService {
    AjaxResult createBorrowOrder(Integer memberId, List<Map<String, Object>> books, String remark,
                                  String staffId, String staffName, Long deptId);

    AjaxResult returnBook(Long borrowDetailId, String returnCondition, Integer points, String remark,
                           String staffId, String staffName, Long deptId);

    AjaxResult borrowToPurchase(List<Map<String, Object>> purchaseItems,
                                 String staffId, String staffName, Long deptId);

    List<BookBorrowOrder> selectByMemberId(Integer memberId);

    List<BookBorrowOrder> selectList(String phone, Integer status, List<Long> deptIds);

    int countTodayByDeptId(Long deptId);

    int countTodayByStaffId(String staffId);

    BookBorrowOrder selectOrderByNo(String orderNo);

    List<BookBorrowDetail> selectDetailsByOrderId(Long orderId);

    BookBorrowDetail selectDetailById(Long detailId);
    List<Map<String, Object>> selectBorrowDetailPage(String phone, Integer status, Integer memberId,
                                                     boolean borrowingOnly, int offset, int limit);
    long countBorrowDetailPage(String phone, Integer status, Integer memberId, boolean borrowingOnly);
    int countBorrowOrdersByMemberId(Integer memberId);
    int sumCurrentYearBorrowQtyByMemberId(Integer memberId);
    int sumRemainingByMemberId(Integer memberId);

    List<BookReturnDetail> selectReturnsByOrderId(Long orderId);

    List<BookBorrowDetailImage> selectImagesByDetailId(Long detailId);
}
