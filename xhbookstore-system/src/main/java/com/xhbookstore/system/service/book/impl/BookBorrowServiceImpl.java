package com.xhbookstore.system.service.book.impl;

import java.text.SimpleDateFormat;
import java.util.*;
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
    @Autowired private MemberMapper memberMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult createBorrowOrder(Integer memberId, List<Map<String, Object>> books,
                                         String remark, String staffId, String staffName, Long deptId) {
        Member member = memberMapper.selectMemberById(memberId);
        if (member == null) return AjaxResult.error("会员不存在");
        if (books == null || books.isEmpty()) return AjaxResult.error("至少需要一本图书");

        // 生成借书单号
        String orderNo = generateOrderNo("DY");

        int totalQty = 0;
        List<BookBorrowDetail> details = new ArrayList<>();
        for (Map<String, Object> book : books) {
            String bookName = (String) book.get("bookName");
            Object qtyObj = book.get("borrowQty");
            Object bookIdObj = book.get("bookId");
            int qty = qtyObj != null ? Integer.parseInt(qtyObj.toString()) : 1;
            totalQty += qty;

            BookBorrowDetail detail = new BookBorrowDetail();
            detail.setBorrowOrderNo(orderNo);
            detail.setMemberId(memberId);
            detail.setBookId(bookIdObj != null ? Long.parseLong(bookIdObj.toString()) : null);
            detail.setBookName(bookName);
            detail.setBorrowQty(qty);
            detail.setFirstStaffId(staffId);
            detail.setFirstStaffName(staffName);
            details.add(detail);
        }

        // 插入借书单
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

        // 插入明细
        for (BookBorrowDetail detail : details) {
            detail.setBorrowOrderId(order.getId());
            detail.setBorrowOrderNo(orderNo);
            detailMapper.insert(detail);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("orderNo", orderNo);
        data.put("totalBookCount", totalQty);
        data.put("detailCount", details.size());
        return AjaxResult.success("借书成功", data);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult returnBook(String borrowOrderNo, List<Map<String, Object>> returnItems,
                                  String staffId, String staffName, Long deptId) {
        BookBorrowOrder order = orderMapper.selectByOrderNo(borrowOrderNo);
        if (order == null) return AjaxResult.error("借书单不存在");
        if (returnItems == null || returnItems.isEmpty()) return AjaxResult.error("请选择要还的图书");

        List<BookBorrowDetail> details = detailMapper.selectByOrderId(order.getId());
        int totalReturned = 0;
        List<String> returnOrderNos = new ArrayList<>();

        for (Map<String, Object> item : returnItems) {
            Long detailId = Long.parseLong(item.get("borrowDetailId").toString());
            int returnQty = Integer.parseInt(item.get("returnQty").toString());
            int returnType = item.get("returnType") != null ? Integer.parseInt(item.get("returnType").toString()) : 1;
            String itemRemark = (String) item.get("remark");

            BookBorrowDetail detail = details.stream().filter(d -> d.getId().equals(detailId)).findFirst().orElse(null);
            if (detail == null) return AjaxResult.error("借书明细不存在: " + detailId);
            if (detail.getBorrowQty() - detail.getReturnedQty() - detail.getPurchaseQty() < returnQty) {
                return AjaxResult.error("还书数量超过可还数量: " + detail.getBookName());
            }

            // 生成还书单号
            String returnOrderNo = generateOrderNo("HS");
            returnOrderNos.add(returnOrderNo);

            // 插入还书记录
            BookReturnDetail rd = new BookReturnDetail();
            rd.setReturnOrderNo(returnOrderNo);
            rd.setBorrowOrderId(order.getId());
            rd.setBorrowOrderNo(borrowOrderNo);
            rd.setBorrowDetailId(detailId);
            rd.setMemberId(order.getMemberId());
            rd.setBookId(detail.getBookId());
            rd.setBookName(detail.getBookName());
            rd.setReturnQty(returnQty);
            rd.setReturnType(returnType);
            rd.setRemark(itemRemark);
            rd.setDeptId(deptId);
            rd.setStaffId(staffId);
            rd.setStaffName(staffName);
            returnMapper.insert(rd);

            // 更新借书明细
            int newReturned = detail.getReturnedQty() + returnQty;
            detail.setReturnedQty(newReturned);
            detail.setLastStaffId(staffId);
            detail.setLastStaffName(staffName);
            if (newReturned + detail.getPurchaseQty() >= detail.getBorrowQty()) {
                detail.setBorrowStatus(detail.getPurchaseQty() > 0 ? 5 : 2);
                detail.setReturnAllTime(new Date());
            } else {
                detail.setBorrowStatus(detail.getPurchaseQty() > 0 ? 4 : 3);
            }
            detailMapper.updateReturnInfo(detail);
            totalReturned += returnQty;
        }

        // 更新借书单状态
        boolean allDone = details.stream().allMatch(d -> {
            List<BookReturnDetail> returns = returnMapper.selectByBorrowDetailId(d.getId());
            int totalRet = returns.stream().mapToInt(BookReturnDetail::getReturnQty).sum();
            return totalRet + d.getPurchaseQty() >= d.getBorrowQty();
        });
        order.setBorrowStatus(allDone ? 2 : 3);
        order.setIsFinished(allDone ? 1 : 0);
        if (allDone) order.setReturnAllTime(new Date());
        order.setLastStaffId(staffId);
        order.setLastStaffName(staffName);
        orderMapper.updateStatus(order);

        Map<String, Object> data = new HashMap<>();
        data.put("returnOrderNos", returnOrderNos);
        data.put("totalReturned", totalReturned);
        return AjaxResult.success("还书成功", data);
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
    public List<BookReturnDetail> selectReturnsByOrderId(Long orderId) {
        return returnMapper.selectByBorrowOrderId(orderId);
    }

    private String generateOrderNo(String prefix) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String dateStr = sdf.format(new Date());
        int random = (int)(Math.random() * 900000) + 100000;
        return prefix + dateStr + random;
    }
}
