package com.xhbookstore.system.mapper.member;

import java.util.List;
import com.xhbookstore.system.domain.member.PointsBatchAllocationRecord;
import com.xhbookstore.system.domain.member.PointsOrderRecord;
import com.xhbookstore.system.domain.member.PointsUserIntoBillDetail;
import com.xhbookstore.system.domain.member.PointsUserOutBillDetail;

public interface PointsOrderAdminMapper {
    List<PointsOrderRecord> selectOrderList(PointsOrderRecord query);
    PointsOrderRecord selectOrder(PointsOrderRecord query);
    List<PointsUserIntoBillDetail> selectIntoBills(String orderNumber);
    List<PointsUserOutBillDetail> selectOutBills(String orderNumber);
    List<PointsBatchAllocationRecord> selectAllocations(String orderNumber);
}
