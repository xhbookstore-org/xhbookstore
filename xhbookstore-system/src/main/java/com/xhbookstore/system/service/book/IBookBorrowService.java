package com.xhbookstore.system.service.book;

import java.util.List;
import java.util.Map;
import com.xhbookstore.common.core.domain.AjaxResult;
import com.xhbookstore.system.domain.book.BookBorrowOrder;
import com.xhbookstore.system.domain.book.BookBorrowDetail;
import com.xhbookstore.system.domain.book.BookReturnDetail;

public interface IBookBorrowService {
    /** 创建借书单（含明细），事务保证 */
    AjaxResult createBorrowOrder(Integer memberId, List<Map<String, Object>> books, String remark,
                                  String staffId, String staffName, Long deptId, List<String> imageUrls);

    /** 还书，事务保证原子性 */
    AjaxResult returnBook(String borrowOrderNo, List<Map<String, Object>> returnItems,
                           String staffId, String staffName, Long deptId);

    /** 查询会员借阅列表 */
    List<BookBorrowOrder> selectByMemberId(Integer memberId);

    /** 查询借书单详情 */
    BookBorrowOrder selectOrderByNo(String orderNo);

    /** 查询借书单明细 */
    List<BookBorrowDetail> selectDetailsByOrderId(Long orderId);

    /** 查询还书记录 */
    List<BookReturnDetail> selectReturnsByOrderId(Long orderId);
}
