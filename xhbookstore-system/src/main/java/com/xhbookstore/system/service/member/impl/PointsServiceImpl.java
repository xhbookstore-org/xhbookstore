package com.xhbookstore.system.service.member.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.xhbookstore.common.core.domain.AjaxResult;
import com.xhbookstore.system.domain.member.Member;
import com.xhbookstore.system.domain.member.PointsOrder;
import com.xhbookstore.system.domain.member.PointsUserIntoBillDetail;
import com.xhbookstore.system.mapper.member.MemberMapper;
import com.xhbookstore.system.mapper.member.PointsOrderMapper;
import com.xhbookstore.system.mapper.member.PointsUserIntoBillDetailMapper;
import com.xhbookstore.system.service.member.IPointsService;

/**
 * 积分/书城币管理服务实现
 *
 * 添加积分流程（悲观锁 + 事务保证原子性）：
 * 1. SELECT ... FOR UPDATE 锁定会员行
 * 2. 记录操作前积分
 * 3. 插入订单表 xhbs_points_order
 * 4. 插入入账单表 xhbs_points_user_into_bill_detail
 * 5. 更新会员积分余额
 * 6. 事务提交，释放行锁
 *
 * 并发安全：同一会员并发加积分时，后者等待前者事务提交后才能读取最新积分
 */
@Service
public class PointsServiceImpl implements IPointsService {

    @Autowired private PointsOrderMapper pointsOrderMapper;
    @Autowired private PointsUserIntoBillDetailMapper intoBillMapper;
    @Autowired private MemberMapper memberMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult addPoints(Integer memberId, Integer points, String description, String operator, String operationDevice) {
        // 1. 悲观锁查询会员信息，防止并发修改
        Member member = memberMapper.selectMemberByIdForUpdate(memberId);
        if (member == null) {
            return AjaxResult.error("会员不存在");
        }

        // 2. 记录操作前积分（锁内读取，保证准确性）
        int oldPoints = (member.getCurrentPoints() != null ? member.getCurrentPoints() : 0);
        int newPoints = oldPoints + points;

        // 3. 生成订单号: IN + yyyyMMddHHmmss + 6位随机数
        String orderNumber = generateOrderNumber("IN");

        // 4. 插入订单表
        PointsOrder order = new PointsOrder();
        order.setOrderNumber(orderNumber);
        order.setOpenId(member.getCardNo() != null ? member.getCardNo() : "");
        order.setCardNo(member.getCardNo() != null ? member.getCardNo() : "");
        order.setMemberId(memberId);
        order.setAppOrderNumber("");
        order.setAmount(points);
        order.setDescription(description != null ? description : "管理员添加积分");
        order.setType(1);
        order.setCustomArgs("operator:" + operator);
        order.setCompletedTime(new Date());
        order.setClientIp("127.0.0.1");
        order.setItemId("");
        order.setItemName("");
        order.setPrice(points);
        order.setDiscountedPrice(points);
        order.setOperationDevice(operationDevice != null ? operationDevice : "PC");
        order.setIsDel(0);
        order.setAmountType(2);
        order.setAppId("");
        order.setOrginPoints(oldPoints);
        order.setAfterPoints(newPoints);
        pointsOrderMapper.insertPointsOrder(order);

        // 5. 插入入账单表
        PointsUserIntoBillDetail intoBill = new PointsUserIntoBillDetail();
        intoBill.setMemberId(memberId);
        intoBill.setPoints(points);
        intoBill.setRemainingPoints(points);
        intoBill.setDescription(description != null ? description : "管理员添加积分");
        intoBill.setOrderNoSrc(orderNumber);
        intoBill.setOrderNoTarget("");
        intoBill.setActivityKey("");
        intoBill.setActivityName("");
        intoBill.setEventKey("");
        intoBill.setEventName("");
        intoBill.setAccountType(0);
        intoBill.setBillStatus(0);
        intoBill.setIsWhiteOrder(0);
        intoBill.setExpiredTime(null);
        intoBill.setExpiredTimestamp(0L);
        intoBill.setIsDel(0);
        intoBillMapper.insertIntoBill(intoBill);

        // 6. 更新会员积分余额
        Member updateMember = new Member();
        updateMember.setId(memberId);
        updateMember.setCurrentPoints(newPoints);
        updateMember.setLastOperator(operator);
        memberMapper.updateMember(updateMember);

        return AjaxResult.success("积分添加成功，订单号：" + orderNumber);
    }

    /**
     * 生成订单号
     * 格式: 前缀 + yyyyMMddHHmmss + 6位随机数字
     */
    private String generateOrderNumber(String prefix) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String dateStr = sdf.format(new Date());
        int random = (int) (Math.random() * 900000) + 100000;
        return prefix + dateStr + random;
    }

    @Override
    public List<PointsOrder> selectByMemberId(Integer memberId) {
        return pointsOrderMapper.selectByMemberId(memberId);
    }
}
