<template>
  <div class="app-container">
    <el-form v-show="showSearch" ref="queryForm" :model="queryParams" :inline="true" label-width="82px">
      <el-form-item label="电话" prop="memberPhone">
        <el-input v-model.trim="queryParams.memberPhone" placeholder="会员手机号" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="姓名" prop="memberName">
        <el-input v-model.trim="queryParams.memberName" placeholder="会员姓名" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="会员卡号" prop="memberCardNo">
        <el-input v-model.trim="queryParams.memberCardNo" placeholder="会员卡号" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="借阅单号" prop="orderNo">
        <el-input v-model.trim="queryParams.orderNo" placeholder="借阅单号" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="图书编号" prop="bookCode">
        <el-input v-model.trim="queryParams.bookCode" placeholder="图书编号" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="图书名称" prop="bookName">
        <el-input v-model.trim="queryParams.bookName" placeholder="图书名称" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="订单状态" prop="borrowStatus">
        <el-select v-model="queryParams.borrowStatus" placeholder="全部" clearable style="width:150px">
          <el-option label="借阅中" :value="1" />
          <el-option label="部分处理" :value="2" />
          <el-option label="已完结" :value="3" />
          <el-option label="已取消" :value="4" />
        </el-select>
      </el-form-item>
      <el-form-item label="订单类型" prop="handlingType">
        <el-select v-model="queryParams.handlingType" placeholder="全部" clearable style="width:150px">
          <el-option label="存在借阅中" value="BORROW" />
          <el-option label="有还书记录" value="RETURN" />
          <el-option label="有借转购" value="PURCHASE" />
        </el-select>
      </el-form-item>
      <el-form-item label="门店" prop="deptId">
        <el-select v-model="queryParams.deptId" placeholder="全部门店" clearable filterable style="width:180px">
          <el-option v-for="item in deptOptions" :key="item.deptId" :label="item.deptName" :value="item.deptId" />
        </el-select>
      </el-form-item>
      <el-form-item label="操作人" prop="lastStaffName">
        <el-input v-model.trim="queryParams.lastStaffName" placeholder="最近操作人" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="借阅时间">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="yyyy-MM-dd"
          style="width:240px"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">查询</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['borrow:record:export']"
        >导出逐册明细</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-table
      class="borrow-record-table"
      v-loading="loading"
      :data="recordList"
      row-key="id"
      @expand-change="handleExpand"
    >
      <el-table-column type="expand" width="46">
        <template slot-scope="scope">
          <div class="detail-wrap" v-loading="scope.row.detailLoading">
            <borrow-detail-table :details="scope.row.details || []" />
          </div>
        </template>
      </el-table-column>
      <el-table-column label="编号" width="70" align="center">
        <template slot-scope="scope">{{ (queryParams.pageNum - 1) * queryParams.pageSize + scope.$index + 1 }}</template>
      </el-table-column>
      <el-table-column label="借阅单号" prop="orderNo" width="215" :show-overflow-tooltip="true" />
      <el-table-column label="借阅日期" prop="borrowTime" width="160" />
      <el-table-column label="姓名" prop="memberName" min-width="90" :show-overflow-tooltip="true" />
      <el-table-column label="会员卡号" prop="memberCardNo" width="140" />
      <el-table-column label="手机" prop="memberPhone" width="125" />
      <el-table-column label="会员类型" prop="cardTypeName" width="110">
        <template slot-scope="scope">{{ scope.row.cardTypeName || '—' }}</template>
      </el-table-column>
      <el-table-column label="有效期" prop="validDate" width="105">
        <template slot-scope="scope">{{ scope.row.validDate || '—' }}</template>
      </el-table-column>
      <el-table-column label="借阅册数" prop="totalBookCount" width="90" align="center" />
      <el-table-column label="最后还书时间" prop="lastReturnTime" width="160">
        <template slot-scope="scope">{{ scope.row.lastReturnTime || '—' }}</template>
      </el-table-column>
      <el-table-column label="订单状态" width="100" align="center">
        <template slot-scope="scope">
          <el-tag size="mini" :type="orderStatusTag(scope.row.borrowStatus)">{{ orderStatusText(scope.row.borrowStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="还书状态" width="100" align="center">
        <template slot-scope="scope"><el-tag size="mini" :type="returnStatusTag(scope.row.returnStatus)">{{ returnStatusText(scope.row.returnStatus) }}</el-tag></template>
      </el-table-column>
      <el-table-column label="积分" prop="points" width="80" align="right">
        <template slot-scope="scope"><span class="borrow-points">+{{ Number(scope.row.points || 0) }}</span></template>
      </el-table-column>
      <el-table-column label="备注" prop="remark" min-width="140" :show-overflow-tooltip="true">
        <template slot-scope="scope">{{ scope.row.remark || '—' }}</template>
      </el-table-column>
      <el-table-column label="最近操作人" prop="lastStaffName" width="110">
        <template slot-scope="scope">{{ scope.row.lastStaffName || '—' }}</template>
      </el-table-column>
      <el-table-column label="最近操作时间" prop="updatedAt" width="160" />
      <el-table-column label="操作" fixed="right" width="130" align="center">
        <template slot-scope="scope">
          <el-button
            type="text"
            size="mini"
            icon="el-icon-view"
            @click="handleView(scope.row)"
            v-hasPermi="['borrow:record:query']"
          >详情</el-button>
          <el-button
            type="text"
            size="mini"
            icon="el-icon-tickets"
            @click="handleDetails(scope.row)"
            v-hasPermi="['borrow:record:query']"
          >明细</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <el-dialog title="借阅记录详情" :visible.sync="detailVisible" width="1150px" append-to-body>
      <el-descriptions v-if="detailData.order" :column="4" border size="small">
        <el-descriptions-item label="借阅单号">{{ detailData.order.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="借阅时间">{{ detailData.order.borrowTime }}</el-descriptions-item>
        <el-descriptions-item label="订单状态">{{ orderStatusText(detailData.order.borrowStatus) }}</el-descriptions-item>
        <el-descriptions-item label="还书状态">{{ returnStatusText(detailData.order.returnStatus) }}</el-descriptions-item>
        <el-descriptions-item label="积分"><span class="borrow-points">+{{ Number(detailData.order.points || 0) }}</span></el-descriptions-item>
        <el-descriptions-item label="门店">{{ detailData.order.deptName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ detailData.order.memberName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="会员卡号">{{ detailData.order.memberCardNo }}</el-descriptions-item>
        <el-descriptions-item label="手机">{{ detailData.order.memberPhone || '—' }}</el-descriptions-item>
        <el-descriptions-item label="会员类型">{{ detailData.order.cardTypeName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="最近操作人">{{ detailData.order.lastStaffName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="最近操作时间">{{ detailData.order.updatedAt || '—' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailData.order.remark || '—' }}</el-descriptions-item>
      </el-descriptions>
      <div slot="footer"><el-button @click="detailVisible = false">关闭</el-button></div>
    </el-dialog>

    <el-dialog
      :title="`借阅明细${detailOrderNo ? ` - ${detailOrderNo}` : ''}`"
      :visible.sync="detailListVisible"
      width="1200px"
      append-to-body
    >
      <borrow-detail-table v-loading="detailListLoading" :details="detailListData" />
      <div slot="footer"><el-button @click="detailListVisible = false">关闭</el-button></div>
    </el-dialog>
  </div>
</template>

<script>
import { listBorrowRecords, getBorrowRecord, listBorrowDepts } from '@/api/borrow/record'
import BorrowDetailTable from './BorrowDetailTable.vue'

export default {
  name: 'BorrowRecord',
  components: { BorrowDetailTable },
  data() {
    return {
      loading: false,
      showSearch: true,
      total: 0,
      recordList: [],
      deptOptions: [],
      dateRange: [],
      detailVisible: false,
      detailData: { order: null, details: [] },
      detailListVisible: false,
      detailListLoading: false,
      detailListData: [],
      detailOrderNo: '',
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        memberPhone: undefined,
        memberName: undefined,
        memberCardNo: undefined,
        orderNo: undefined,
        bookCode: undefined,
        bookName: undefined,
        borrowStatus: undefined,
        handlingType: undefined,
        deptId: undefined,
        lastStaffName: undefined
      }
    }
  },
  created() {
    this.getDeptOptions()
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listBorrowRecords(this.addDateRange({ ...this.queryParams }, this.dateRange)).then(response => {
        this.recordList = (response.rows || []).map(row => ({ ...row, details: [], detailsLoaded: false, detailLoading: false }))
        this.total = response.total || 0
      }).finally(() => { this.loading = false })
    },
    getDeptOptions() {
      listBorrowDepts().then(response => { this.deptOptions = response.data || [] })
    },
    loadDetails(row) {
      if (row.detailsLoaded || row.detailLoading) return Promise.resolve(row.details || [])
      this.$set(row, 'detailLoading', true)
      return getBorrowRecord(row.id).then(response => {
        const data = response.data || {}
        this.$set(row, 'details', data.details || [])
        this.$set(row, 'detailsLoaded', true)
        return row.details
      }).finally(() => { this.$set(row, 'detailLoading', false) })
    },
    handleExpand(row, expandedRows) {
      if (expandedRows.some(item => item.id === row.id)) this.loadDetails(row)
    },
    handleView(row) {
      getBorrowRecord(row.id).then(response => {
        this.detailData = response.data || { order: null, details: [] }
        this.detailVisible = true
      })
    },
    handleDetails(row) {
      this.detailOrderNo = row.orderNo || ''
      this.detailListData = []
      this.detailListVisible = true
      this.detailListLoading = true
      getBorrowRecord(row.id).then(response => {
        const data = response.data || {}
        this.detailListData = data.details || []
      }).finally(() => { this.detailListLoading = false })
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
      const params = this.addDateRange({ ...this.queryParams }, this.dateRange)
      delete params.pageNum
      delete params.pageSize
      this.download('borrow/record/export', params, `借阅记录逐册明细_${this.parseTime(new Date(), '{y}{m}{d}_{h}{i}{s}')}.xlsx`)
    },
    orderStatusText(status) {
      return ({ 1: '借阅中', 2: '部分处理', 3: '已完结', 4: '已取消' })[status] || '未知'
    },
    orderStatusTag(status) {
      return ({ 1: '', 2: 'warning', 3: 'success', 4: 'info' })[status] || 'info'
    },
    returnStatusText(status) {
      return ({ NOT_RETURNED: '未还书', PARTIAL_RETURNED: '部分还书', ALL_RETURNED: '已全部还书' })[status] || '未知'
    },
    returnStatusTag(status) {
      return ({ NOT_RETURNED: 'warning', PARTIAL_RETURNED: '', ALL_RETURNED: 'success' })[status] || 'info'
    }
  }
}
</script>

<style scoped>
.detail-wrap { padding: 12px 24px; min-height: 70px; background: #fafafa; }
.detail-title { margin: 20px 0 10px; }
.borrow-points { color: #13ce66; font-weight: 600; }

/*
 * Element UI 会在固定列中复制展开行，并用空白单元格盖住主表内容。
 * 只提升主表展开单元格的层级，固定操作列在普通数据行中仍然有效。
 */
.borrow-record-table ::v-deep > .el-table__body-wrapper .el-table__expanded-cell {
  position: relative;
  z-index: 4;
}
</style>
