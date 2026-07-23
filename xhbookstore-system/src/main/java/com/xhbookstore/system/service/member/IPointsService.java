package com.xhbookstore.system.service.member;

import java.util.List;
import java.util.Map;
import com.xhbookstore.common.core.domain.AjaxResult;
import com.xhbookstore.system.domain.member.PointsOrder;
import com.xhbookstore.system.domain.member.PointsRule;

/**
 * 积分/书城币管理服务接口
 */
public interface IPointsService {

    /**
     * 给用户添加积分
     * @param memberId  会员ID
     * @param points    积分数额
     * @param description 描述信息
     * @param operator  操作人
     * @param operationDevice 操作设备
     * @return 操作结果
     */
    AjaxResult addPoints(Integer memberId, Integer points, String description, String operator, String operationDevice);

    /**
     * 扣减用户积分（悲观锁+事务）
     */
    AjaxResult deductPoints(Integer memberId, Integer points, String description, String operator, String operationDevice);

    /** 查询会员当前可人工选择且已配置积分值的规则。 */
    List<PointsRule> selectManualFixedRules(Integer memberId, String direction);

    /** 按积分规则执行人工加分或扣分；仅规则允许时接受员工录入基础积分。 */
    AjaxResult adjustPointsByRule(Integer memberId, Long ruleId, Integer requestedPoints, String description,
                                  Long operatorUserId, String operator, String operationDevice);

    /** 根据 BORROW_BOOK 规则按逐册明细数发放积分，同一借阅单全局幂等。 */
    Map<String, Object> grantBorrowPoints(Integer memberId, String borrowOrderNo, int detailCount, Long deptId);

    /**
     * 借阅图书破损赔付或转购完成后，按 PURCHASE_BOOK 规则自动处理积分。
     * paidAmount 为员工录入的实付金额（允许 0），同一还书单幂等。
     */
    Map<String, Object> settleBorrowReturnPoints(Integer memberId, String returnOrderNo, int paidAmount,
                                                 Long deptId, String operator);

    /**
     * 查询会员积分订单列表
     * @param memberId 会员ID
     * @return 积分订单列表
     */
    List<PointsOrder> selectByMemberId(Integer memberId);

    List<PointsOrder> selectPage(String phone, Integer memberId, String direction, int offset, int limit);

    long countPage(String phone, Integer memberId, String direction);

    int sumYearEarned(Integer memberId);

    PointsOrder selectByOrderNumber(String orderNumber);
}
