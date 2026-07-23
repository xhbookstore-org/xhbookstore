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
import com.xhbookstore.system.service.book.BookBorrowException;
import com.xhbookstore.system.service.member.IPointsService;

@Service
public class BookBorrowServiceImpl implements IBookBorrowService {

    private static final int MAX_IMAGES_PER_DETAIL = 3;

    @Autowired private BookBorrowOrderMapper orderMapper;
    @Autowired private BookBorrowDetailMapper detailMapper;
    @Autowired private BookReturnDetailMapper returnMapper;
    @Autowired private BookBorrowDetailImageMapper detailImageMapper;
    @Autowired private MemberMapper memberMapper;
    @Autowired private BookPurchaseOrderMapper purchaseOrderMapper;
    @Autowired private BookBorrowLogMapper borrowLogMapper;
    @Autowired private BookReturnLogMapper returnLogMapper;
    @Autowired private BookPurchaseLogMapper purchaseLogMapper;
    @Autowired private IPointsService pointsService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult createBorrowOrder(Integer memberId, List<Map<String, Object>> books,
                                         String remark, String staffId, String staffName, Long deptId) {
        Member member = memberMapper.selectMemberById(memberId);
        if (member == null) throw new BookBorrowException("Member not found");
        if (books == null || books.isEmpty()) throw new BookBorrowException("At least one book is required");

        String traceId = newTraceId();
        String orderNo = generateOrderNo("DY");
        List<BorrowBookInput> inputs = new ArrayList<>();
        Set<String> requestImageIds = new HashSet<>();

        for (Map<String, Object> bookItem : books) {
            String bookCode = optionalText(bookItem.get("bookCode"), 64);
            String bookName = optionalText(firstNonBlankObject(bookItem.get("bookName"), bookItem.get("bookTitle")), 200);
            if (bookName == null) bookName = "借阅图书";
            int qty = toInt(bookItem.get("borrowQty"), 1);
            if (qty != 1) {
                throw new BookBorrowException("Each book item must represent exactly one copy");
            }
            List<String> imageIds = stringList(bookItem.get("imageIds"));
            if (imageIds.size() != MAX_IMAGES_PER_DETAIL) {
                throw new BookBorrowException("Each borrowed book must have exactly 3 images");
            }
            for (String imageId : imageIds) {
                if (!requestImageIds.add(imageId)) {
                    throw new BookBorrowException("Duplicate image id: " + imageId);
                }
                BookBorrowDetailImage image = detailImageMapper.selectByImageIdForUpdate(imageId);
                if (image == null || image.getBorrowDetailId() != null || !"TEMP".equals(image.getBindStatus())) {
                    throw new BookBorrowException("Image is unavailable: " + imageId);
                }
                if (!Objects.equals(image.getMemberId(), memberId)
                        || !Objects.equals(image.getCreateStaffId(), staffId)) {
                    throw new BookBorrowException("Image does not belong to this member or operator: " + imageId);
                }
            }
            inputs.add(new BorrowBookInput(bookCode, bookName, text(bookItem.get("remark")), imageIds));
        }

        BookBorrowOrder order = new BookBorrowOrder();
        order.setOrderNo(orderNo);
        order.setMemberId(memberId);
        order.setMemberCardNo(member.getCardNo());
        order.setMemberName(member.getName());
        order.setMemberPhone(member.getPhone());
        order.setMemberCardTypeName(member.getCardTypeName());
        order.setMemberValidDate(member.getValidDate());
        order.setTotalBookCount(inputs.size());
        order.setBorrowStatus(1);
        order.setRemark(remark);
        order.setDeptId(deptId);
        order.setFirstStaffId(staffId);
        order.setFirstStaffName(staffName);
        orderMapper.insert(order);
        writeBorrowLog(traceId, order, null, null, order, "", 0, 1,
                "book_borrow_order", staffId, staffName);

        List<Map<String, Object>> detailResults = new ArrayList<>();
        int totalImages = 0;
        for (BorrowBookInput input : inputs) {
            BookBorrowDetail detail = new BookBorrowDetail();
            detail.setBorrowOrderId(order.getId());
            detail.setBorrowOrderNo(orderNo);
            detail.setMemberId(memberId);
            detail.setBookCode(input.bookCode());
            detail.setBookName(input.bookName());
            detail.setBorrowQty(1);
            detail.setRemark(input.remark());
            detail.setFirstStaffId(staffId);
            detail.setFirstStaffName(staffName);
            detailMapper.insert(detail);
            writeBorrowLog(traceId, order, detail.getId(), null, detail, "", 0, 4,
                    "book_borrow_detail", staffId, staffName);
            int sort = 1;
            for (String imageId : input.imageIds()) {
                if (detailImageMapper.bindToDetail(imageId, memberId, detail.getId(), order.getId(), orderNo, sort++) != 1) {
                    throw new BookBorrowException("Failed to bind image: " + imageId);
                }
            }
            totalImages += input.imageIds().size();
            Map<String, Object> detailResult = new LinkedHashMap<>();
            detailResult.put("borrowDetailId", detail.getId());
            detailResult.put("bookCode", detail.getBookCode());
            detailResult.put("bookName", detail.getBookName());
            detailResult.put("imageCount", input.imageIds().size());
            detailResults.add(detailResult);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("orderNo", orderNo);
        data.put("borrowOrderId", order.getId());
        data.put("totalBookCount", inputs.size());
        data.put("detailCount", inputs.size());
        data.put("imageCount", totalImages);
        data.put("details", detailResults);
        data.put("points", pointsService.grantBorrowPoints(memberId, orderNo, inputs.size(), deptId));
        data.put("traceId", traceId);
        return AjaxResult.success("Borrow created", data);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult returnBook(Long borrowDetailId, String returnCondition, Integer points, String remark,
                                  String staffId, String staffName, Long deptId) {
        if (borrowDetailId == null) throw new BookBorrowException("Borrow detail id is required");
        ReturnCondition condition = ReturnCondition.parse(returnCondition);
        if (condition.requiresPoints() && points == null) {
            throw new BookBorrowException("Points are required for " + condition.value());
        }
        if (!condition.requiresPoints()) points = null;
        if (points != null && points < 0) throw new BookBorrowException("Points cannot be negative");

        BookBorrowDetail beforeDetail = detailMapper.selectByIdForUpdate(borrowDetailId);
        if (beforeDetail == null) throw new BookBorrowException("Borrow detail not found: " + borrowDetailId);
        if (remainingQty(beforeDetail) != 1) {
            throw new BookBorrowException("Borrow detail has already been settled: " + borrowDetailId);
        }
        BookBorrowOrder order = orderMapper.selectByOrderNo(beforeDetail.getBorrowOrderNo());
        if (order == null || !Objects.equals(order.getId(), beforeDetail.getBorrowOrderId())) {
            throw new BookBorrowException("Borrow order not found for detail: " + borrowDetailId);
        }

        String traceId = newTraceId();
        String returnOrderNo = generateOrderNo("HS");
        BookReturnDetail rd = new BookReturnDetail();
        rd.setReturnOrderNo(returnOrderNo);
        rd.setBorrowOrderId(order.getId());
        rd.setBorrowOrderNo(order.getOrderNo());
        rd.setBorrowDetailId(borrowDetailId);
        rd.setMemberId(order.getMemberId());
        rd.setBookId(beforeDetail.getBookId());
        rd.setBookCode(beforeDetail.getBookCode());
        rd.setBookName(beforeDetail.getBookName());
        rd.setReturnQty(1);
        rd.setReturnType(condition.type());
        rd.setReturnCondition(condition.value());
        rd.setPoints(points);
        rd.setRemark(buildReturnRemark(condition, points, remark));
        rd.setDeptId(deptId);
        rd.setStaffId(staffId);
        rd.setStaffName(staffName);
        returnMapper.insert(rd);

        BookBorrowDetail afterDetail = cloneDetail(beforeDetail);
        if (condition == ReturnCondition.PURCHASE) {
            afterDetail.setPurchaseQty(safeInt(beforeDetail.getPurchaseQty()) + 1);
            afterDetail.setPurchaseOrderNo(appendOrderNo(beforeDetail.getPurchaseOrderNo(), returnOrderNo));
        } else {
            afterDetail.setReturnedQty(safeInt(beforeDetail.getReturnedQty()) + 1);
        }
        afterDetail.setLastStaffId(staffId);
        afterDetail.setLastStaffName(staffName);
        applyDetailStatus(afterDetail);
        if (condition == ReturnCondition.PURCHASE) detailMapper.updatePurchaseInfo(afterDetail);
        else detailMapper.updateReturnInfo(afterDetail);
        BookBorrowDetail savedDetail = detailMapper.selectById(borrowDetailId);

        Map<String, Object> beforeData = new LinkedHashMap<>();
        beforeData.put("borrowDetail", beforeDetail);
        Map<String, Object> afterData = new LinkedHashMap<>();
        afterData.put("returnDetail", rd);
        afterData.put("borrowDetail", savedDetail);
        writeReturnLog(traceId, rd, beforeData, afterData,
                condition == ReturnCondition.PURCHASE ? "purchase_qty,borrow_status,return_condition,points" : "return_qty,returned_qty,borrow_status,return_condition,points",
                staffId, staffName);
        writeBorrowLog(traceId, order, borrowDetailId, beforeDetail, savedDetail, returnOrderNo,
                condition == ReturnCondition.PURCHASE ? 2 : 1, 5,
                condition == ReturnCondition.PURCHASE ? "purchase_qty,borrow_status,return_all_time" : "returned_qty,borrow_status,return_all_time",
                staffId, staffName);

        BookBorrowOrder beforeOrder = cloneOrder(order);
        BookBorrowOrder updatedOrder = refreshOrderStatus(order, staffId, staffName);
        if (safeInt(updatedOrder.getIsFinished()) == 1 && safeInt(beforeOrder.getIsFinished()) != 1) {
            writeBorrowLog(traceId, updatedOrder, null, beforeOrder, updatedOrder, returnOrderNo, 0, 3,
                    "borrow_status,is_finished,return_all_time", staffId, staffName);
        }

        Map<String, Object> pointsResult = condition.requiresPoints()
                ? pointsService.settleBorrowReturnPoints(order.getMemberId(), returnOrderNo, points, deptId, staffName)
                : Collections.singletonMap("status", "NOT_APPLICABLE");
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("returnOrderNo", returnOrderNo);
        data.put("borrowDetailId", borrowDetailId);
        data.put("returnCondition", condition.value());
        data.put("points", points);
        data.put("pointsResult", pointsResult);
        data.put("traceId", traceId);
        return AjaxResult.success("Return completed", data);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult borrowToPurchase(List<Map<String, Object>> purchaseItems,
                                       String staffId, String staffName, Long deptId) {
        if (purchaseItems == null || purchaseItems.isEmpty()) throw new BookBorrowException("Purchase items are required");

        Set<Long> purchaseDetailIds = new HashSet<>();
        for (Map<String, Object> item : purchaseItems) {
            Long detailId = toLong(item.get("borrowDetailId"));
            int purchaseQty = toInt(item.get("purchaseQty"), 1);
            if (detailId == null) throw new BookBorrowException("Borrow detail id is required");
            if (purchaseQty != 1) throw new BookBorrowException("Each borrow detail must be purchased as one copy");
            if (!purchaseDetailIds.add(detailId)) throw new BookBorrowException("Duplicate borrow detail: " + detailId);

            BookBorrowDetail detail = detailMapper.selectByIdForUpdate(detailId);
            if (detail == null) throw new BookBorrowException("Borrow detail not found: " + detailId);
            if (orderMapper.selectByOrderNo(detail.getBorrowOrderNo()) == null) {
                throw new BookBorrowException("Borrow order not found: " + detail.getBorrowOrderNo());
            }
            if (purchaseQty > remainingQty(detail)) {
                throw new BookBorrowException("Purchase quantity exceeds remaining quantity: " + detail.getBookName());
            }
            BigDecimal unitPrice = toBigDecimal(item.get("unitPrice"));
            if (unitPrice == null || unitPrice.signum() < 0) {
                throw new BookBorrowException("Unit price is required and cannot be negative");
            }
        }

        String traceId = newTraceId();
        List<String> purchaseOrderNos = new ArrayList<>();
        Set<String> affectedOrderNos = new LinkedHashSet<>();
        int totalPurchased = 0;
        BigDecimal totalPaid = BigDecimal.ZERO;

        for (Map<String, Object> item : purchaseItems) {
            Long detailId = toLong(item.get("borrowDetailId"));
            int purchaseQty = toInt(item.get("purchaseQty"), 1);
            if (detailId == null) throw new BookBorrowException("Borrow detail id is required");
            if (purchaseQty != 1) throw new BookBorrowException("Each borrow detail must be purchased as one copy");

            BookBorrowDetail beforeDetail = detailMapper.selectByIdForUpdate(detailId);
            if (beforeDetail == null) throw new BookBorrowException("Borrow detail not found: " + detailId);
            BookBorrowOrder order = orderMapper.selectByOrderNo(beforeDetail.getBorrowOrderNo());
            if (order == null) throw new BookBorrowException("Borrow order not found: " + beforeDetail.getBorrowOrderNo());
            if (purchaseQty > remainingQty(beforeDetail)) {
                throw new BookBorrowException("Purchase quantity exceeds remaining quantity: " + beforeDetail.getBookName());
            }

            BigDecimal unitPrice = toBigDecimal(item.get("unitPrice"));
            if (unitPrice == null || unitPrice.signum() < 0) {
                throw new BookBorrowException("Unit price is required and cannot be negative");
            }
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
            purchaseOrder.put("bookCode", beforeDetail.getBookCode());
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

            Map<String, Object> beforeData = new LinkedHashMap<>();
            beforeData.put("borrowDetail", beforeDetail);
            Map<String, Object> afterData = new LinkedHashMap<>();
            afterData.put("purchaseOrder", purchaseOrder);
            afterData.put("borrowDetail", savedDetail);
            writePurchaseLog(traceId, purchaseOrder, beforeData, afterData,
                    "purchase_qty,purchase_order_no,borrow_status", staffId, staffName);
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
    public List<BookBorrowOrder> selectList(String phone, Integer status, List<Long> deptIds) {
        return orderMapper.selectList(phone, status, deptIds);
    }

    @Override
    public int countTodayByDeptId(Long deptId) {
        return orderMapper.countTodayByDeptId(deptId);
    }

    @Override
    public int countTodayByStaffId(String staffId) {
        return orderMapper.countTodayByStaffId(staffId);
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
    public List<Map<String, Object>> selectBorrowDetailPage(String phone, Integer status, Integer memberId,
                                                            boolean borrowingOnly, int offset, int limit) {
        return detailMapper.selectPage(phone, status, memberId, borrowingOnly, offset, limit);
    }

    @Override
    public long countBorrowDetailPage(String phone, Integer status, Integer memberId, boolean borrowingOnly) {
        return detailMapper.countPage(phone, status, memberId, borrowingOnly);
    }

    @Override
    public int countBorrowOrdersByMemberId(Integer memberId) {
        return orderMapper.countByMemberId(memberId);
    }

    @Override
    public int sumRemainingByMemberId(Integer memberId) {
        return detailMapper.sumRemainingByMemberId(memberId);
    }

    @Override
    public List<BookReturnDetail> selectReturnsByOrderId(Long orderId) {
        return returnMapper.selectByBorrowOrderId(orderId);
    }

    @Override
    public List<BookBorrowDetailImage> selectImagesByDetailId(Long detailId) {
        return detailImageMapper.selectByDetailId(detailId);
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
        update.setBorrowStatus(allDone ? 3 : (anyDone ? 2 : 1));
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
            detail.setBorrowStatus(1);
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
        d.setBookCode(src.getBookCode());
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
        o.setMemberCardTypeName(src.getMemberCardTypeName());
        o.setMemberValidDate(src.getMemberValidDate());
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

    private String requiredText(Object value, String message, int maxLength) {
        String result = text(value);
        if (result == null) throw new BookBorrowException(message);
        if (result.length() > maxLength) throw new BookBorrowException(message + " (max " + maxLength + ")");
        return result;
    }

    private String optionalText(Object value, int maxLength) {
        String result = text(value);
        if (result != null && result.length() > maxLength) {
            throw new BookBorrowException("Text exceeds max length " + maxLength);
        }
        return result;
    }

    private String buildReturnRemark(ReturnCondition condition, Integer points, String remark) {
        StringBuilder result = new StringBuilder("还书状态：").append(condition.label());
        if (points != null) result.append("；积分金额：").append(points);
        String userRemark = text(remark);
        if (userRemark != null) result.append("；").append(userRemark);
        if (result.length() > 500) throw new BookBorrowException("Return remark exceeds max length 500");
        return result.toString();
    }

    private String text(Object value) {
        if (value == null) return null;
        String result = value.toString().trim();
        return result.isEmpty() ? null : result;
    }

    private Object firstNonBlankObject(Object first, Object second) {
        return first != null && !first.toString().isBlank() ? first : second;
    }

    private List<String> stringList(Object value) {
        if (value == null) return Collections.emptyList();
        if (!(value instanceof Collection<?> values)) {
            throw new BookBorrowException("imageIds must be an array");
        }
        List<String> result = new ArrayList<>();
        for (Object item : values) {
            String imageId = requiredText(item, "Image id is required", 64);
            result.add(imageId);
        }
        return result;
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

    private record BorrowBookInput(String bookCode, String bookName, String remark, List<String> imageIds) {
    }

    private enum ReturnCondition {
        INTACT("intact", "无损", 1, false),
        SLIGHT_WEAR("slight_wear", "轻微磨损", 2, false),
        DAMAGED("damaged", "破损", 3, true),
        PURCHASE("purchase", "购买", 4, true);

        private final String value;
        private final String label;
        private final int type;
        private final boolean requiresPoints;

        ReturnCondition(String value, String label, int type, boolean requiresPoints) {
            this.value = value;
            this.label = label;
            this.type = type;
            this.requiresPoints = requiresPoints;
        }

        static ReturnCondition parse(String value) {
            if (value != null) {
                for (ReturnCondition condition : values()) {
                    if (condition.value.equals(value.trim())) return condition;
                }
            }
            throw new BookBorrowException("returnCondition must be one of intact, slight_wear, damaged, purchase");
        }

        String value() { return value; }
        String label() { return label; }
        int type() { return type; }
        boolean requiresPoints() { return requiresPoints; }
    }
}
