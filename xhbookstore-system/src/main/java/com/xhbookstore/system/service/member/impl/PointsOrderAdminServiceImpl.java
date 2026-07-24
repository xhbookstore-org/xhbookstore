package com.xhbookstore.system.service.member.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.xhbookstore.common.annotation.DataScope;
import com.xhbookstore.system.domain.member.PointsBatchAllocationRecord;
import com.xhbookstore.system.domain.member.PointsOrderRecord;
import com.xhbookstore.system.domain.member.PointsRule;
import com.xhbookstore.system.domain.member.PointsUserIntoBillDetail;
import com.xhbookstore.system.domain.member.PointsUserOutBillDetail;
import com.xhbookstore.system.mapper.member.PointsOrderAdminMapper;
import com.xhbookstore.system.mapper.member.PointsRuleMapper;
import com.xhbookstore.system.service.member.IPointsOrderAdminService;

@Service
public class PointsOrderAdminServiceImpl implements IPointsOrderAdminService {
    @Autowired private PointsOrderAdminMapper adminMapper;
    @Autowired private PointsRuleMapper ruleMapper;

    @Override
    @DataScope(deptAlias = "m", permission = "member:points:list")
    public List<PointsOrderRecord> selectList(PointsOrderRecord query) {
        return adminMapper.selectOrderList(query);
    }

    @Override
    @DataScope(deptAlias = "m", permission = "member:points:export")
    public List<PointsOrderRecord> selectExportList(PointsOrderRecord query) {
        return adminMapper.selectOrderList(query);
    }

    @Override
    @DataScope(deptAlias = "m", permission = "member:points:query")
    public Map<String, Object> selectDetail(PointsOrderRecord query) {
        PointsOrderRecord order = adminMapper.selectOrder(query);
        if (order == null) return null;

        List<PointsUserIntoBillDetail> intoBills = adminMapper.selectIntoBills(order.getOrderNumber());
        List<PointsUserOutBillDetail> outBills = adminMapper.selectOutBills(order.getOrderNumber());
        Map<Long, List<PointsBatchAllocationRecord>> allocations = adminMapper.selectAllocations(order.getOrderNumber())
                .stream().collect(Collectors.groupingBy(PointsBatchAllocationRecord::getOutBillId,
                        LinkedHashMap::new, Collectors.toList()));
        for (PointsUserOutBillDetail outBill : outBills) {
            outBill.setAllocations(allocations.getOrDefault(outBill.getId(), new ArrayList<>()));
        }

        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("order", order);
        detail.put("intoBills", intoBills);
        detail.put("outBills", outBills);
        return detail;
    }

    @Override
    public List<PointsRule> selectRuleOptions() {
        return ruleMapper.selectRuleList(null, null, null, null, null);
    }
}
