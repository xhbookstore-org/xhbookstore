package com.xhbookstore.system.service.member;

import java.util.List;
import com.xhbookstore.common.core.domain.AjaxResult;
import com.xhbookstore.system.domain.member.PointsOrder;

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
