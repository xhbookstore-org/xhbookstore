package com.xhbookstore.system.service.book.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import com.alibaba.fastjson2.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.xhbookstore.common.core.domain.AjaxResult;
import com.xhbookstore.system.domain.member.Member;
import com.xhbookstore.system.domain.book.*;
import com.xhbookstore.system.mapper.member.MemberMapper;
import com.xhbookstore.system.mapper.book.*;
import com.xhbookstore.system.service.book.IBookBorrowService;

@Service
public class BookBorrowServiceImpl implements IBookBorrowService {

    @Autowired private BookBorrowOrderMapper orderMapper;
    @Autowired private BookBorrowDetailMapper detailMapper;
    @Autowired private BookReturnDetailMapper returnMapper;
    @Autowired private BookBorrowDetailImageMapper detailImageMapper;
    @Autowired private MemberMapper memberMapper;
    @Autowired private BookInfoMapper bookInfoMapper;
    @Autowired private BookPurchaseOrderMapper purchaseOrderMapper;
    @Autowired private BookBorrowLogMapper borrowLogMapper;
    @Autowired private BookReturnLogMapper returnLogMapper;
    @Autowired private BookPurchaseLogMapper purchaseLogMapper;
    @Autowired private BookInfoHistoryMapper bookInfoHistoryMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult createBorrowOrder(Integer memberId, List<Map<String, Object>> books,
                                         String remark, String staffId, String staffName, Long deptId,
                                         List<String> imageUrls) {
        Member member = memberMapper.selectMemberById(memberId);
        if (member == null) return AjaxResult.error("Member not found");
        if (books == null || books.isEmpty()) return AjaxResult.error("At least one book is required");

        String traceId = newTraceId();
        String orderNo = generateOrderNo("DY");
        int totalQty = 0;
        List<BookBorrowDetail> details = new ArrayList<>();

        for (Map<String, Object> bookItem : books) {
            Long bookId = toLong(bookItem.get("bookId"));
            int qty = toInt(bookItem.get("borrowQty"), 1);
            if (bookId == null) return AjaxResult.error("Book is required");
            if (qty <= 0) return AjaxResult.error("Borrow quantity must be greater than 0");

            BookInfo beforeBook = bookInfoMapper.selectByIdForUpdate(bookId);
            if (beforeBook == null) return AjaxResult.error("Book not found: " + bookId);
            if (safeInt(beforeBook.getLendableQty()) < qty) {
                return AjaxResult.error("Not enough lendable stock: " + beforeBook.getBookName());
            }
            if (bookInfoMapper.decreaseLendableQty(bookId, qty) != 1) {
                return AjaxResult.error("Not enough lendable stock: " + beforeBook.getBookName());
            }
            BookInfo afterBook = bookInfoMapper.selectById(bookId);
            writeBookHistory(bookId, 2, beforeBook, afterBook, "lendable_qty",
                    "BORROW_DECREASE_LENDABLE", orderNo, 1, staffId, staffName);

            BookBorrowDetail detail = new BookBorrowDetail();
            detail.setBorrowOrderNo(orderNo);
            detail.setMemberId(memberId);
            detail.setBookId(bookId);
            detail.setBookName(strOrDefault(bookItem.get("bookName"), beforeBook.getBookName()));
            detail.setBorrowQty(qty);
            detail.setRemark((String) bookItem.get("remark"));
            detail.setFirstStaffId(staffId);
            detail.setFirstStaffName(staffName);
            details.add(detail);
            totalQty += qty;
        }

        BookBorrowOrder order = new BookBorrowOrder();
        order.setOrderNo(orderNo);
        order.setMemberId(memberId);
        order.setMemberCardNo(member.getCardNo());
        order.setMemberName(member.getName());
        order.setMemberPhone(member.getPhone());
        order.setTotalBookCount(totalQty);
        order.setBorrowStatus(1);
        order.setRemark(remark);
        order.setDeptId(deptId);
        order.setFirstStaffId(staffId);
        order.setFirstStaffName(staffName);
        orderMapper.insert(order);
        writeBorrowLog(traceId, order, null, null, order, "", 0, 1,
                "book_borrow_order", staffId, staffName);

        for (BookBorrowDetail detail : details) {
            detail.setBorrowOrderId(order.getId());
            detail.setBorrowOrderNo(orderNo);
            detailMapper.insert(detail);
            writeBorrowLog(traceId, order, detail.getId(), null, detail, "", 0, 4,
                    "book_borrow_detail", staffId, staffName);
        }

        if (imageUrls != null && !imageUrls.isEmpty()) {
            int sort = 1;
            for (String url : imageUrls) {
                if (url == null || url.trim().isEmpty()) continue;
                BookBorrowDetailImage img = new BookBorrowDetailImage();
                img.setBorrowOrderId(order.getId());
                img.setBorrowOrderNo(orderNo);
                img.setImageUrl(url.trim());
                img.setImageType(1);
                img.setSortOrder(sort++);
                img.setCreateStaffId(staffId);
                img.setCreateStaffName(staffName);
                detailImageMapper.insert(img);
            }
        }

        Map<String, Object> data = new HashMap<>();
        data.put("orderNo", orderNo);
        data.put("totalBookCount", totalQty);
        data.put("detailCount", details.size());
        data.put("imageCount", imageUrls != null ? imageUrls.size() : 0);
        data.put("traceId", traceId);
        return AjaxResult.success("Borrow created", data);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult returnBook(String borrowOrderNo, List<Map<String, Object>> returnItems,
                                  String staffId, String staffName, Long deptId) {
        BookBorrowOrder order = orderMapper.selectByOrderNo(borrowOrderNo);
        if (order == null) return AjaxResult.error("Borrow order not found");
        if (returnItems == null || returnItems.isEmpty()) return AjaxResult.error("Return items are required");

        String traceId = newTraceId();
        int totalReturned = 0;
        List<String> returnOrderNos = new ArrayList<>();

        for (Map<String, Object> item : returnItems) {
            Long detailId = toLong(item.get("borrowDetailId"));
            int returnQty = toInt(item.get("returnQty"), 0);
            int returnType = toInt(item.get("returnType"), 1);
            String itemRemark = (String) item.get("remark");
            if (detailId == null) return AjaxResult.error("Borrow detail id is required");
            if (returnQty <= 0) return AjaxResult.error("Return quantity must be greater than 0");

            BookBorrowDetail beforeDetail = detailMapper.selectByIdForUpdate(detailId);
            if (beforeDetail == null || !order.getId().equals(beforeDetail.getBorrowOrderId())) {
                return AjaxResult.error("Borrow detail not found: " + detailId);
            }
            if (returnQty > remainingQty(beforeDetail)) {
                return AjaxResult.error("Return quantity exceeds remaining quantity: " + beforeDetail.getBookName());
            }

            BookInfo beforeBook = null;
            if (beforeDetail.getBookId() != null) {
                beforeBook = bookInfoMapper.selectByIdForUpdate(beforeDetail.getBookId());
            }

            String returnOrderNo = generateOrderNo("HS");
            BookReturnDetail rd = new BookReturnDetail();
            rd.setReturnOrderNo(returnOrderNo);
            rd.setBorrowOrderId(order.getId());
            rd.setBorrowOrderNo(borrowOrderNo);
            rd.setBorrowDetailId(detailId);
            rd.setMemberId(order.getMemberId());
            rd.setBookId(beforeDetail.getBookId());
            rd.setBookName(beforeDetail.getBookName());
            rd.setReturnQty(returnQty);
            rd.setReturnType(returnType);
            rd.setRemark(itemRemark);
            rd.setDeptId(deptId);
            rd.setStaffId(staffId);
            rd.setStaffName(staffName);
            returnMapper.insert(rd);

            BookBorrowDetail afterDetail = cloneDetail(beforeDetail);
            afterDetail.setReturnedQty(safeInt(beforeDetail.getReturnedQty()) + returnQty);
            afterDetail.setLastStaffId(staffId);
            afterDetail.setLastStaffName(staffName);
            applyDetailStatus(afterDetail);
            detailMapper.updateReturnInfo(afterDetail);
            BookBorrowDetail savedDetail = detailMapper.selectById(detailId);

            if (beforeDetail.getBookId() != null) {
                bookInfoMapper.increaseLendableQty(beforeDetail.getBookId(), returnQty);
                BookInfo afterBook = bookInfoMapper.selectById(beforeDetail.getBookId());
                writeBookHistory(beforeDetail.getBookId(), 2, beforeBook, afterBook, "lendable_qty",
                        "RETURN_INCREASE_LENDABLE", returnOrderNo, 2, staffId, staffName);
            }

            Map<String, Object> beforeData = new LinkedHashMap<>();
            beforeData.put("borrowDetail", beforeDetail);
            Map<String, Object> afterData = new LinkedHashMap<>();
            afterData.put("returnDetail", rd);
            afterData.put("borrowDetail", savedDetail);
            writeReturnLog(traceId, rd, beforeData, afterData, "return_qty,returned_qty,borrow_status", staffId, staffName);
            writeBorrowLog(traceId, order, detailId, beforeDetail, savedDetail, returnOrderNo, 1, 5,
                    "returned_qty,borrow_status,return_all_time", staffId, staffName);

            returnOrderNos.add(returnOrderNo);
            totalReturned += returnQty;
        }

        BookBorrowOrder beforeOrder = cloneOrder(order);
        BookBorrowOrder updatedOrder = refreshOrderStatus(order, staffId, staffName);
        if (safeInt(updatedOrder.getIsFinished()) == 1 && safeInt(beforeOrder.getIsFinished()) != 1) {
            writeBorrowLog(traceId, updatedOrder, null, beforeOrder, updatedOrder, "", 0, 3,
                    "borrow_status,is_finished,return_all_time", staffId, staffName);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("returnOrderNos", returnOrderNos);
        data.put("totalReturned", totalReturned);
        data.put("traceId", traceId);
        return AjaxResult.success("Return completed", data);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult borrowToPurchase(List<Map<String, Object>> purchaseItems,
                                       String staffId, String staffName, Long deptId) {
        if (purchaseItems == null || purchaseItems.isEmpty()) return AjaxResult.error("Purchase items are required");

        String traceId = newTraceId();
        List<String> purchaseOrderNos = new ArrayList<>();
        Set<String> affectedOrderNos = new LinkedHashSet<>();
        int totalPurchased = 0;
        BigDecimal totalPaid = BigDecimal.ZERO;

        for (Map<String, Object> item : purchaseItems) {
            Long detailId = toLong(item.get("borrowDetailId"));
            int purchaseQty = toInt(item.get("purchaseQty"), 0);
            if (detailId == null) return AjaxResult.error("Borrow detail id is required");
            if (purchaseQty <= 0) return AjaxResult.error("Purchase quantity must be greater than 0");

            BookBorrowDetail beforeDetail = detailMapper.selectByIdForUpdate(detailId);
            if (beforeDetail == null) return AjaxResult.error("Borrow detail not found: " + detailId);
            BookBorrowOrder order = orderMapper.selectByOrderNo(beforeDetail.getBorrowOrderNo());
            if (order == null) return AjaxResult.error("Borrow order not found: " + beforeDetail.getBorrowOrderNo());
            if (purchaseQty > remainingQty(beforeDetail)) {
                return AjaxResult.error("Purchase quantity exceeds remaining quantity: " + beforeDetail.getBookName());
            }

            BookInfo beforeBook = null;
            if (beforeDetail.getBookId() != null) {
                beforeBook = bookInfoMapper.selectByIdForUpdate(beforeDetail.getBookId());
                if (beforeBook == null) return AjaxResult.error("Book not found: " + beforeDetail.getBookId());
            }

            BigDecimal unitPrice = toBigDecimal(item.get("unitPrice"));
            if (unitPrice == null && beforeBook != null) unitPrice = firstNonNull(beforeBook.getSalePrice(), beforeBook.getPrice());
            if (unitPrice == null) unitPrice = BigDecimal.ZERO;
            BigDecimal receivableAmount = toBigDecimal(item.get("receivableAmount"));
            if (receivableAmount == null) receivableAmount = unitPrice.multiply(BigDecimal.valueOf(purchaseQty));
            BigDecimal paidAmount = toBigDecimal(item.get("paidAmount"));
            if (paidAmount == null) paidAmount = receivableAmount;
            BigDecimal discountPrice = toBigDecimal(item.get("discountPrice"));
            if (discountPrice == null) discountPrice = BigDecimal.ZERO;
            int paymentType = toInt(item.get("paymentType"), 1);
            int pointsDeduct = toInt(item.get("pointsDeduct"), 0);
            String purchaseOrderNo = generateOrderNo("JSDD");

            Map<String, Object> purchaseOrder = new LinkedHashMap<>();
            purchaseOrder.put("orderNo", purchaseOrderNo);
            purchaseOrder.put("borrowOrderId", order.getId());
            purchaseOrder.put("borrowOrderNo", order.getOrderNo());
            purchaseOrder.put("borrowDetailId", detailId);
            purchaseOrder.put("memberId", order.getMemberId());
            purchaseOrder.put("memberCardNo", order.getMemberCardNo());
            purchaseOrder.put("memberName", order.getMemberName());
            purchaseOrder.put("orderType", 2);
            purchaseOrder.put("paymentType", paymentType);
            purchaseOrder.put("bookId", beforeDetail.getBookId());
            purchaseOrder.put("bookName", beforeDetail.getBookName());
            purchaseOrder.put("qty", purchaseQty);
            purchaseOrder.put("unitPrice", unitPrice);
            purchaseOrder.put("discountPrice", discountPrice);
            purchaseOrder.put("receivableAmount", receivableAmount);
            purchaseOrder.put("paidAmount", paidAmount);
            purchaseOrder.put("pointsDeduct", pointsDeduct);
            purchaseOrder.put("orderStatus", 1);
            purchaseOrder.put("remark", item.get("remark"));
            purchaseOrder.put("deptId", deptId != null ? deptId : order.getDeptId());
            purchaseOrder.put("staffId", staffId);
            purchaseOrder.put("staffName", staffName);
            purchaseOrderMapper.insert(purchaseOrder);

            BookBorrowDetail afterDetail = cloneDetail(beforeDetail);
            afterDetail.setPurchaseQty(safeInt(beforeDetail.getPurchaseQty()) + purchaseQty);
            afterDetail.setPurchaseOrderNo(appendOrderNo(beforeDetail.getPurchaseOrderNo(), purchaseOrderNo));
            afterDetail.setLastStaffId(staffId);
            afterDetail.setLastStaffName(staffName);
            applyDetailStatus(afterDetail);
            detailMapper.updatePurchaseInfo(afterDetail);
            BookBorrowDetail savedDetail = detailMapper.selectById(detailId);

            if (beforeDetail.getBookId() != null) {
                if (bookInfoMapper.decreaseStockQty(beforeDetail.getBookId(), purchaseQty) != 1) {
                    return AjaxResult.error("Not enough stock for purchase conversion: " + beforeDetail.getBookName());
                }
                BookInfo afterBook = bookInfoMapper.selectById(beforeDetail.getBookId());
                writeBookHistory(beforeDetail.getBookId(), 2, beforeBook, afterBook, "stock_qty",
                        "BORROW_TO_PURCHASE_DECREASE_STOCK", purchaseOrderNo, 3, staffId, staffName);
            }

            Map<String, Object> beforeData = new LinkedHashMap<>();
            beforeData.put("borrowDetail", beforeDetail);
            Map<String, Object> afterData = new LinkedHashMap<>();
            afterData.put("purchaseOrder", purchaseOrder);
            afterData.put("borrowDetail", savedDetail);
            writePurchaseLog(traceId, purchaseOrder, beforeData, afterData,
                    "purchase_qty,purchase_order_no,borrow_status,stock_qty", staffId, staffName);
            writeBorrowLog(traceId, order, detailId, beforeDetail, savedDetail, purchaseOrderNo, 2, 6,
                    "purchase_qty,purchase_order_no,borrow_status,return_all_time", staffId, staffName);

            BookBorrowOrder beforeOrder = cloneOrder(order);
            BookBorrowOrder updatedOrder = refreshOrderStatus(order, staffId, staffName);
            if (safeInt(updatedOrder.getIsFinished()) == 1 && safeInt(beforeOrder.getIsFinished()) != 1) {
                writeBorrowLog(traceId, updatedOrder, null, beforeOrder, updatedOrder, purchaseOrderNo, 2, 3,
                        "borrow_status,is_finished,return_all_time", staffId, staffName);
            }

            affectedOrderNos.add(order.getOrderNo());
            purchaseOrderNos.add(purchaseOrderNo);
            totalPurchased += purchaseQty;
            totalPaid = totalPaid.add(paidAmount);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("purchaseOrderNos", purchaseOrderNos);
        data.put("borrowOrderNos", affectedOrderNos);
        data.put("totalPurchased", totalPurchased);
        data.put("totalPaid", totalPaid);
        data.put("traceId", traceId);
        return AjaxResult.success("Purchase conversion completed", data);
    }

    @Override
    public List<BookBorrowOrder> selectByMemberId(Integer memberId) {
        return orderMapper.selectByMemberId(memberId);
    }

    @Override
    public BookBorrowOrder selectOrderByNo(String orderNo) {
        return orderMapper.selectByOrderNo(orderNo);
    }

    @Override
    public List<BookBorrowDetail> selectDetailsByOrderId(Long orderId) {
        return detailMapper.selectByOrderId(orderId);
    }

    @Override
    public BookBorrowDetail selectDetailById(Long detailId) {
        return detailMapper.selectById(detailId);
    }

    @Override
    public List<BookReturnDetail> selectReturnsByOrderId(Long orderId) {
        return returnMapper.selectByBorrowOrderId(orderId);
    }

    private BookBorrowOrder refreshOrderStatus(BookBorrowOrder order, String staffId, String staffName) {
        List<BookBorrowDetail> details = detailMapper.selectByOrderId(order.getId());
        boolean allDone = true;
        boolean anyDone = false;
        for (BookBorrowDetail d : details) {
            int settled = safeInt(d.getReturnedQty()) + safeInt(d.getPurchaseQty());
            if (settled <= 0) allDone = false;
            if (settled > 0) anyDone = true;
            if (settled < safeInt(d.getBorrowQty())) allDone = false;
        }
        BookBorrowOrder update = cloneOrder(order);
        update.setBorrowStatus(allDone ? 2 : (anyDone ? 3 : 1));
        update.setIsFinished(allDone ? 1 : 0);
        update.setReturnAllTime(allDone ? new Date() : null);
        update.setLastStaffId(staffId);
        update.setLastStaffName(staffName);
        orderMapper.updateStatus(update);
        return orderMapper.selectByOrderNo(order.getOrderNo());
    }

    private void applyDetailStatus(BookBorrowDetail detail) {
        int borrowQty = safeInt(detail.getBorrowQty());
        int returnedQty = safeInt(detail.getReturnedQty());
        int purchaseQty = safeInt(detail.getPurchaseQty());
        int settledQty = returnedQty + purchaseQty;
        if (settledQty >= borrowQty) {
            detail.setBorrowStatus(purchaseQty > 0 ? 5 : 2);
            detail.setReturnAllTime(new Date());
        } else {
            detail.setBorrowStatus(purchaseQty > 0 ? 4 : 3);
            detail.setReturnAllTime(null);
        }
    }

    private int remainingQty(BookBorrowDetail detail) {
        return safeInt(detail.getBorrowQty()) - safeInt(detail.getReturnedQty()) - safeInt(detail.getPurchaseQty());
    }

    private BookBorrowDetail cloneDetail(BookBorrowDetail src) {
        BookBorrowDetail d = new BookBorrowDetail();
        d.setId(src.getId());
        d.setBorrowOrderId(src.getBorrowOrderId());
        d.setBorrowOrderNo(src.getBorrowOrderNo());
        d.setMemberId(src.getMemberId());
        d.setBookId(src.getBookId());
        d.setBookName(src.getBookName());
        d.setBorrowQty(src.getBorrowQty());
        d.setReturnedQty(src.getReturnedQty());
        d.setPurchaseQty(src.getPurchaseQty());
        d.setBorrowStatus(src.getBorrowStatus());
        d.setBorrowTime(src.getBorrowTime());
        d.setReturnAllTime(src.getReturnAllTime());
        d.setPurchaseOrderNo(src.getPurchaseOrderNo());
        d.setRemark(src.getRemark());
        d.setFirstStaffId(src.getFirstStaffId());
        d.setFirstStaffName(src.getFirstStaffName());
        d.setLastStaffId(src.getLastStaffId());
        d.setLastStaffName(src.getLastStaffName());
        d.setCreatedAt(src.getCreatedAt());
        d.setUpdatedAt(src.getUpdatedAt());
        d.setIsDel(src.getIsDel());
        return d;
    }

    private BookBorrowOrder cloneOrder(BookBorrowOrder src) {
        BookBorrowOrder o = new BookBorrowOrder();
        o.setId(src.getId());
        o.setOrderNo(src.getOrderNo());
        o.setMemberId(src.getMemberId());
        o.setMemberCardNo(src.getMemberCardNo());
        o.setMemberName(src.getMemberName());
        o.setMemberPhone(src.getMemberPhone());
        o.setTotalBookCount(src.getTotalBookCount());
        o.setIsFinished(src.getIsFinished());
        o.setBorrowStatus(src.getBorrowStatus());
        o.setBorrowTime(src.getBorrowTime());
        o.setReturnAllTime(src.getReturnAllTime());
        o.setExpectedReturnTime(src.getExpectedReturnTime());
        o.setRemark(src.getRemark());
        o.setDeptId(src.getDeptId());
        o.setFirstStaffId(src.getFirstStaffId());
        o.setFirstStaffName(src.getFirstStaffName());
        o.setLastStaffId(src.getLastStaffId());
        o.setLastStaffName(src.getLastStaffName());
        o.setCreatedAt(src.getCreatedAt());
        o.setUpdatedAt(src.getUpdatedAt());
        o.setIsDel(src.getIsDel());
        return o;
    }

    private void writeBorrowLog(String traceId, BookBorrowOrder order, Long detailId, Object beforeData,
                                Object afterData, String sourceOrderNo, int sourceType, int logType,
                                String changeFields, String staffId, String staffName) {
        Map<String, Object> log = new HashMap<>();
        log.put("traceId", traceId);
        log.put("borrowOrderId", order.getId());
        log.put("borrowOrderNo", order.getOrderNo());
        log.put("borrowDetailId", detailId);
        log.put("sourceOrderNo", sourceOrderNo);
        log.put("sourceType", sourceType);
        log.put("logType", logType);
        log.put("beforeData", toJson(beforeData));
        log.put("afterData", toJson(afterData));
        log.put("changeFields", changeFields);
        log.put("staffId", staffId);
        log.put("staffName", staffName);
        log.put("clientIp", "");
        borrowLogMapper.insert(log);
    }

    private void writeReturnLog(String traceId, BookReturnDetail rd, Object beforeData, Object afterData,
                                String changeFields, String staffId, String staffName) {
        Map<String, Object> log = new HashMap<>();
        log.put("traceId", traceId);
        log.put("returnDetailId", rd.getId());
        log.put("returnOrderNo", rd.getReturnOrderNo());
        log.put("borrowOrderId", rd.getBorrowOrderId());
        log.put("borrowOrderNo", rd.getBorrowOrderNo());
        log.put("borrowDetailId", rd.getBorrowDetailId());
        log.put("logType", 1);
        log.put("beforeData", toJson(beforeData));
        log.put("afterData", toJson(afterData));
        log.put("changeFields", changeFields);
        log.put("staffId", staffId);
        log.put("staffName", staffName);
        log.put("clientIp", "");
        returnLogMapper.insert(log);
    }

    private void writePurchaseLog(String traceId, Map<String, Object> purchaseOrder, Object beforeData, Object afterData,
                                  String changeFields, String staffId, String staffName) {
        Map<String, Object> log = new HashMap<>();
        log.put("traceId", traceId);
        log.put("purchaseOrderId", purchaseOrder.get("id"));
        log.put("purchaseOrderNo", purchaseOrder.get("orderNo"));
        log.put("borrowOrderId", purchaseOrder.get("borrowOrderId"));
        log.put("borrowDetailId", purchaseOrder.get("borrowDetailId"));
        log.put("logType", 1);
        log.put("beforeData", toJson(beforeData));
        log.put("afterData", toJson(afterData));
        log.put("changeFields", changeFields);
        log.put("staffId", staffId);
        log.put("staffName", staffName);
        log.put("clientIp", "");
        purchaseLogMapper.insert(log);
    }

    private void writeBookHistory(Long bookId, int changeType, Object beforeData, Object afterData,
                                  String changeFields, String reason, String sourceOrderNo,
                                  int sourceType, String staffId, String staffName) {
        Map<String, Object> history = new HashMap<>();
        history.put("bookId", bookId);
        history.put("changeType", changeType);
        history.put("beforeData", toJson(beforeData));
        history.put("afterData", toJson(afterData));
        history.put("changeFields", changeFields);
        history.put("changeReason", reason);
        history.put("sourceOrderNo", sourceOrderNo);
        history.put("sourceType", sourceType);
        history.put("staffId", staffId);
        history.put("staffName", staffName);
        bookInfoHistoryMapper.insert(history);
    }

    private String toJson(Object data) {
        return data == null ? null : JSON.toJSONString(data);
    }

    private String appendOrderNo(String oldValue, String orderNo) {
        if (oldValue == null || oldValue.trim().isEmpty()) return orderNo;
        return oldValue + "," + orderNo;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private int toInt(Object value, int defaultValue) {
        if (value == null) return defaultValue;
        return Integer.parseInt(value.toString());
    }

    private Long toLong(Object value) {
        if (value == null || value.toString().trim().isEmpty()) return null;
        return Long.parseLong(value.toString());
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null || value.toString().trim().isEmpty()) return null;
        return new BigDecimal(value.toString());
    }

    private BigDecimal firstNonNull(BigDecimal first, BigDecimal second) {
        return first != null ? first : second;
    }

    private String strOrDefault(Object value, String defaultValue) {
        if (value == null || value.toString().trim().isEmpty()) return defaultValue;
        return value.toString();
    }

    private String newTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String generateOrderNo(String prefix) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String dateStr = sdf.format(new Date());
        int random = (int)(Math.random() * 900000) + 100000;
        return prefix + dateStr + random;
    }
}