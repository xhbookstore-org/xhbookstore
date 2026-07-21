<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :model="queryParams" :inline="true" label-width="84px">
      <el-form-item label="手机号" prop="memberPhone">
        <el-input v-model.trim="queryParams.memberPhone" placeholder="会员手机号" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="姓名" prop="memberName">
        <el-input v-model.trim="queryParams.memberName" placeholder="会员姓名" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="会员编号" prop="memberNo">
        <el-input v-model.trim="queryParams.memberNo" placeholder="会员编号" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="积分单号" prop="orderNumber">
        <el-input v-model.trim="queryParams.orderNumber" placeholder="积分订单号" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="积分规则" prop="ruleId">
        <el-select v-model="queryParams.ruleId" placeholder="全部规则" clearable filterable style="width:190px">
          <el-option v-for="item in ruleOptions" :key="item.id" :label="item.ruleName" :value="item.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="积分方向" prop="direction">
        <el-select v-model="queryParams.direction" placeholder="全部" clearable style="width:130px">
          <el-option label="增加积分" value="ADD" />
          <el-option label="扣减积分" value="DEDUCT" />
        </el-select>
      </el-form-item>
      <el-form-item label="操作类型" prop="operationKind">
        <el-select v-model="queryParams.operationKind" placeholder="全部" clearable style="width:130px">
          <el-option label="正常入账/消费" value="NORMAL" />
          <el-option label="冲销" value="REVERSAL" />
          <el-option label="过期" value="EXPIRATION" />
        </el-select>
      </el-form-item>
      <el-form-item label="可用状态" prop="availabilityStatus">
        <el-select v-model="queryParams.availabilityStatus" placeholder="全部" clearable style="width:130px">
          <el-option label="可用" value="AVAILABLE" />
          <el-option label="冻结中" value="FROZEN" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>
      </el-form-item>
      <el-form-item label="订单状态" prop="orderStatus">
        <el-select v-model="queryParams.orderStatus" placeholder="全部" clearable style="width:130px">
          <el-option label="成功" value="SUCCESS" />
          <el-option label="已冲销" value="REVERSED" />
          <el-option label="失败" value="FAILED" />
        </el-select>
      </el-form-item>
      <el-form-item label="操作时间">
        <el-date-picker
          v-model="dateRange"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          value-format="yyyy-MM-dd HH:mm:ss"
          style="width:350px"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">查询</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['member:points:export']">导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table v-loading="loading" :data="orderList" class="points-order-table">
      <el-table-column label="编号" width="70" align="center">
        <template slot-scope="scope">{{ (queryParams.pageNum - 1) * queryParams.pageSize + scope.$index + 1 }}</template>
      </el-table-column>
      <el-table-column label="积分订单号" prop="orderNumber" width="215" :show-overflow-tooltip="true" />
      <el-table-column label="姓名" prop="memberName" min-width="95" :show-overflow-tooltip="true" />
      <el-table-column label="会员编号" prop="memberNo" width="140" :show-overflow-tooltip="true" />
      <el-table-column label="手机号" prop="memberPhone" width="125" />
      <el-table-column label="积分规则" prop="ruleName" min-width="130" :show-overflow-tooltip="true" />
      <el-table-column label="积分变化" width="95" align="right">
        <template slot-scope="scope">
          <span :class="scope.row.amount >= 0 ? 'points-add' : 'points-deduct'">{{ signedPoints(scope.row.amount) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作前" prop="orginPoints" width="85" align="right" />
      <el-table-column label="操作后" prop="afterPoints" width="85" align="right" />
      <el-table-column label="可用状态" width="95" align="center">
        <template slot-scope="scope">
          <el-tag size="mini" :type="availabilityTag(scope.row.availabilityStatus)">{{ availabilityText(scope.row.availabilityStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="订单状态" width="90" align="center">
        <template slot-scope="scope">{{ orderStatusText(scope.row.orderStatus) }}</template>
      </el-table-column>
      <el-table-column label="业务单号" prop="businessOrderNo" min-width="155" :show-overflow-tooltip="true">
        <template slot-scope="scope">{{ scope.row.businessOrderNo || '—' }}</template>
      </el-table-column>
      <el-table-column label="门店" prop="deptName" min-width="105" :show-overflow-tooltip="true" />
      <el-table-column label="操作人" prop="operatorName" width="105" />
      <el-table-column label="操作时间" prop="operationTime" width="160" />
      <el-table-column label="操作" fixed="right" width="90" align="center">
        <template slot-scope="scope">
          <el-button type="text" size="mini" icon="el-icon-view" @click="handleDetail(scope.row)" v-hasPermi="['member:points:query']">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-drawer title="积分流水详情" :visible.sync="detailVisible" size="72%" append-to-body>
      <div class="points-detail" v-loading="detailLoading">
        <el-descriptions v-if="detail.order" :column="4" border size="small">
          <el-descriptions-item label="积分订单号" :span="2">{{ detail.order.orderNumber }}</el-descriptions-item>
          <el-descriptions-item label="积分变化"><span :class="detail.order.amount >= 0 ? 'points-add' : 'points-deduct'">{{ signedPoints(detail.order.amount) }}</span></el-descriptions-item>
          <el-descriptions-item label="订单状态">{{ orderStatusText(detail.order.orderStatus) }}</el-descriptions-item>
          <el-descriptions-item label="会员">{{ detail.order.memberName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="会员编号">{{ detail.order.memberNo || '—' }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ detail.order.memberPhone || '—' }}</el-descriptions-item>
          <el-descriptions-item label="门店">{{ detail.order.deptName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="规则编码">{{ detail.order.ruleCode || '—' }}</el-descriptions-item>
          <el-descriptions-item label="规则名称">{{ detail.order.ruleName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="触发方式">{{ triggerModeText(detail.order.triggerMode) }}</el-descriptions-item>
          <el-descriptions-item label="操作类型">{{ operationKindText(detail.order.operationKind) }}</el-descriptions-item>
          <el-descriptions-item label="操作前积分">{{ valueOrDash(detail.order.orginPoints) }}</el-descriptions-item>
          <el-descriptions-item label="操作后积分">{{ valueOrDash(detail.order.afterPoints) }}</el-descriptions-item>
          <el-descriptions-item label="冻结前积分">{{ valueOrDash(detail.order.beforeFrozenPoints) }}</el-descriptions-item>
          <el-descriptions-item label="冻结后积分">{{ valueOrDash(detail.order.afterFrozenPoints) }}</el-descriptions-item>
          <el-descriptions-item label="可用状态">{{ availabilityText(detail.order.availabilityStatus) }}</el-descriptions-item>
          <el-descriptions-item label="预计可用时间">{{ detail.order.availableAt || '—' }}</el-descriptions-item>
          <el-descriptions-item label="实际解冻时间">{{ detail.order.unfrozenAt || '—' }}</el-descriptions-item>
          <el-descriptions-item label="操作时间">{{ detail.order.operationTime || '—' }}</el-descriptions-item>
          <el-descriptions-item label="业务类型">{{ detail.order.businessType || '—' }}</el-descriptions-item>
          <el-descriptions-item label="业务单号">{{ detail.order.businessOrderNo || '—' }}</el-descriptions-item>
          <el-descriptions-item label="原积分单号">{{ detail.order.originalOrderNo || '—' }}</el-descriptions-item>
          <el-descriptions-item label="操作人">{{ detail.order.operatorName || 'SYSTEM' }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="4">{{ detail.order.description || '—' }}</el-descriptions-item>
        </el-descriptions>

        <div class="detail-title">入账批次</div>
        <el-table :data="detail.intoBills" border size="mini" empty-text="本流水未形成入账批次">
          <el-table-column label="批次ID" prop="id" width="90" />
          <el-table-column label="原始积分" prop="points" width="90" align="right" />
          <el-table-column label="已核销" width="90" align="right">
            <template slot-scope="scope">{{ scope.row.points - scope.row.remainingPoints }}</template>
          </el-table-column>
          <el-table-column label="剩余积分" prop="remainingPoints" width="95" align="right" />
          <el-table-column label="批次状态" width="100"><template slot-scope="scope">{{ billStatusText(scope.row.billStatus) }}</template></el-table-column>
          <el-table-column label="生效时间" prop="createdAt" width="160" />
          <el-table-column label="到期时间" prop="expiredTime" width="160"><template slot-scope="scope">{{ scope.row.expiredTime || '—' }}</template></el-table-column>
          <el-table-column label="说明" prop="description" min-width="150" :show-overflow-tooltip="true" />
        </el-table>

        <div class="detail-title">出账与核销</div>
        <el-table :data="detail.outBills" border size="mini" empty-text="本流水未形成出账记录">
          <el-table-column type="expand" width="44">
            <template slot-scope="scope">
              <el-table :data="scope.row.allocations || []" border size="mini" empty-text="暂无批次核销明细">
                <el-table-column label="入账批次ID" prop="intoBillId" width="110" />
                <el-table-column label="入账订单号" prop="intoOrderNo" min-width="180" />
                <el-table-column label="积分来源" prop="sourceDescription" min-width="150" />
                <el-table-column label="本次核销" prop="points" width="95" align="right" />
                <el-table-column label="批次剩余" prop="batchRemainingPoints" width="95" align="right" />
                <el-table-column label="生效时间" prop="effectiveTime" width="160" />
                <el-table-column label="到期时间" prop="expiredTime" width="160" />
              </el-table>
            </template>
          </el-table-column>
          <el-table-column label="出账ID" prop="id" width="90" />
          <el-table-column label="出账积分" prop="points" width="95" align="right" />
          <el-table-column label="核销状态" width="100"><template slot-scope="scope">{{ billStatusText(scope.row.billStatus) }}</template></el-table-column>
          <el-table-column label="创建时间" prop="createdAt" width="160" />
          <el-table-column label="说明" prop="description" min-width="180" :show-overflow-tooltip="true" />
        </el-table>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import { getPointsOrder, listPointsOrderRules, listPointsOrders } from '@/api/member/pointsOrder'

export default {
  name: 'MemberPointsOrder',
  data() {
    return {
      loading: false,
      showSearch: true,
      total: 0,
      orderList: [],
      ruleOptions: [],
      dateRange: [],
      detailVisible: false,
      detailLoading: false,
      detail: { order: null, intoBills: [], outBills: [] },
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        memberPhone: undefined,
        memberName: undefined,
        memberNo: undefined,
        orderNumber: undefined,
        ruleId: undefined,
        direction: undefined,
        operationKind: undefined,
        availabilityStatus: undefined,
        orderStatus: undefined
      }
    }
  },
  created() {
    this.getRuleOptions()
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      const params = { ...this.queryParams }
      if (this.dateRange && this.dateRange.length === 2) {
        params.beginTime = this.dateRange[0]
        params.endTime = this.dateRange[1]
      }
      listPointsOrders(params).then(response => {
        this.orderList = response.rows || []
        this.total = response.total || 0
      }).finally(() => { this.loading = false })
    },
    getRuleOptions() {
      listPointsOrderRules().then(response => { this.ruleOptions = response.data || [] })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.dateRange = []
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleExport() {
      const params = { ...this.queryParams }
      if (this.dateRange && this.dateRange.length === 2) {
        params.beginTime = this.dateRange[0]
        params.endTime = this.dateRange[1]
      }
      delete params.pageNum
      delete params.pageSize
      this.download('member/points/order/export', params, `积分流水_${this.parseTime(new Date(), '{y}{m}{d}_{h}{i}{s}')}.xlsx`)
    },
    handleDetail(row) {
      this.detailVisible = true
      this.detailLoading = true
      this.detail = { order: null, intoBills: [], outBills: [] }
      getPointsOrder(row.id).then(response => {
        this.detail = response.data || this.detail
      }).finally(() => { this.detailLoading = false })
    },
    signedPoints(value) {
      const number = Number(value || 0)
      return number > 0 ? `+${number}` : String(number)
    },
    valueOrDash(value) {
      return value === null || value === undefined ? '—' : value
    },
    availabilityText(value) {
      return { AVAILABLE: '可用', FROZEN: '冻结中', CANCELLED: '已取消' }[value] || (value || '—')
    },
    availabilityTag(value) {
      return { AVAILABLE: 'success', FROZEN: 'warning', CANCELLED: 'info' }[value] || ''
    },
    orderStatusText(value) {
      return { SUCCESS: '成功', REVERSED: '已冲销', FAILED: '失败' }[value] || (value || '—')
    },
    operationKindText(value) {
      return { NORMAL: '正常入账/消费', REVERSAL: '冲销', EXPIRATION: '过期' }[value] || (value || '—')
    },
    triggerModeText(value) {
      return { AUTO: '自动', MANUAL: '手动' }[value] || (value || '—')
    },
    billStatusText(value) {
      return { 0: '未核销', 1: '部分核销', 2: '全部核销', 3: '已过期', 4: '已冲销' }[value] || '未知'
    }
  }
}
</script>

<style scoped>
.points-order-table .points-add,
.points-detail .points-add { color: #13ce66; font-weight: 600; }
.points-order-table .points-deduct,
.points-detail .points-deduct { color: #f56c6c; font-weight: 600; }
.points-detail { padding: 0 22px 30px; }
.detail-title { margin: 24px 0 12px; padding-left: 10px; border-left: 3px solid #409eff; font-size: 15px; font-weight: 600; }
</style>
