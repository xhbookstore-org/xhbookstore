<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" label-width="84px">
      <el-form-item label="会员姓名" prop="memberName">
        <el-input v-model="queryParams.memberName" placeholder="请输入" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="手机号" prop="memberPhone">
        <el-input v-model="queryParams.memberPhone" placeholder="请输入手机号" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="会员卡号" prop="memberNo">
        <el-input v-model="queryParams.memberNo" placeholder="请输入" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="卡类型" prop="cardTypeId">
        <el-select v-model="queryParams.cardTypeId" placeholder="请输入" clearable>
          <el-option v-for="ct in cardTypes" :key="ct.id" :label="ct.typeName" :value="ct.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请输入" clearable>
          <el-option label="待生效" :value="0" />
          <el-option label="生效中" :value="1" />
          <el-option label="已过期" :value="2" />
          <el-option label="已退款" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="售卡单号" prop="saleOrderNo">
        <el-input v-model="queryParams.saleOrderNo" placeholder="请输入" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="退款单号" prop="refundOrderNo">
        <el-input v-model="queryParams.refundOrderNo" placeholder="请输入" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="门店" prop="deptId">
        <el-select v-model="queryParams.deptId" placeholder="请选择门店" clearable>
          <el-option v-for="d in deptOptions" :key="d.deptId" :label="d.deptName" :value="d.deptId" />
        </el-select>
      </el-form-item>
      <el-form-item label="操作员工" prop="createStaffName">
        <el-input v-model="queryParams.createStaffName" placeholder="请输入" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="付款时间">
        <el-date-picker
          v-model="paidRange"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          value-format="yyyy-MM-dd HH:mm:ss"
        />
      </el-form-item>
      <el-form-item label="生效时间">
        <el-date-picker
          v-model="effectiveRange"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          value-format="yyyy-MM-dd HH:mm:ss"
        />
      </el-form-item>
      <el-form-item label="到期时间">
        <el-date-picker
          v-model="expiredRange"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          value-format="yyyy-MM-dd HH:mm:ss"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
        <el-button type="warning" plain icon="el-icon-download" @click="handleExport" v-hasPermi="['member:card:export']">导出</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="cardList" row-key="id" style="width:100%">
      <el-table-column label="ID" prop="id" width="70" align="center" />
      <el-table-column label="会员" min-width="110" :show-overflow-tooltip="true">
        <template slot-scope="scope">{{ scope.row.memberName || '-' }}</template>
      </el-table-column>
      <el-table-column label="会员卡号" prop="memberNo" width="140" :show-overflow-tooltip="true" />
      <el-table-column label="手机号" prop="memberPhone" width="125" />
      <el-table-column label="卡类型" prop="cardTypeName" min-width="110" />
      <el-table-column label="状态" width="90" align="center">
        <template slot-scope="scope">
          <el-tag size="mini" :type="cardStatusTag(scope.row.status)">{{ cardStatusText(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="售卡单号" prop="saleOrderNo" width="190" :show-overflow-tooltip="true" />
      <el-table-column label="付款时间" width="160">
        <template slot-scope="scope">{{ parseTime(scope.row.paidAt) }}</template>
      </el-table-column>
      <el-table-column label="生效时间" width="160">
        <template slot-scope="scope">{{ parseTime(scope.row.effectiveAt) || '-' }}</template>
      </el-table-column>
      <el-table-column label="到期时间" width="160">
        <template slot-scope="scope">{{ parseTime(scope.row.expiredAt) || '-' }}</template>
      </el-table-column>
      <el-table-column label="实收金额" prop="saleAmount" width="95" />
      <el-table-column label="退款单号" prop="refundOrderNo" width="175" :show-overflow-tooltip="true">
        <template slot-scope="scope">{{ scope.row.refundOrderNo || '-' }}</template>
      </el-table-column>
      <el-table-column label="时间" width="160">
        <template slot-scope="scope">{{ parseTime(scope.row.refundedAt) || '-' }}</template>
      </el-table-column>
      <el-table-column label="门店" prop="deptName" min-width="110" :show-overflow-tooltip="true" />
      <el-table-column label="操作员工" prop="createStaffName" width="100" />
      <el-table-column label="操作" align="center" width="140" fixed="right">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-document" @click="handleLog(scope.row)" v-hasPermi="['member:card:query']">日志</el-button>
          <el-button
            v-if="canRefundCard(scope.row)"
            size="mini"
            type="text"
            icon="el-icon-refresh-left"
            @click="handleRefund(scope.row)"
            v-hasPermi="['member:card:refund']"
          >退卡</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog title="会员卡退卡" :visible.sync="refundVisible" width="520px" append-to-body>
      <el-descriptions v-if="refundCard.id" :column="1" border size="small" class="refund-summary">
        <el-descriptions-item label="会员">{{ refundCard.memberName || '-' }}（{{ refundCard.memberNo || '-' }}）</el-descriptions-item>
        <el-descriptions-item label="卡类型">{{ refundCard.cardTypeName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="售卡单号">{{ refundCard.saleOrderNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="付款时间">{{ parseTime(refundCard.paidAt) || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-form ref="refundForm" :model="refundForm" :rules="refundRules" label-width="90px">
        <el-form-item label="退款金额" prop="refundAmount">
          <el-input-number v-model="refundForm.refundAmount" :min="0" :precision="2" :step="1" style="width:220px" />
        </el-form-item>
        <el-form-item label="退款方式" prop="refundType">
          <el-select v-model="refundForm.refundType" style="width:220px">
            <el-option label="现金退款" value="CASH" />
            <el-option label="原路退回" value="ORIGINAL" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="原因" prop="reason">
          <el-input v-model="refundForm.reason" type="textarea" maxlength="500" show-word-limit placeholder="请输入" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button type="danger" :loading="refunding" @click="submitRefund">确认退卡</el-button>
        <el-button @click="refundVisible=false">取消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="会员卡日志" :visible.sync="logVisible" width="950px" append-to-body>
      <el-table v-loading="logLoading" :data="logList" size="small" max-height="420">
        <el-table-column label="类型" prop="logType" width="120" />
        <el-table-column label="售卡单号" prop="saleOrderNo" width="190" :show-overflow-tooltip="true" />
        <el-table-column label="退款单号" prop="refundOrderNo" width="170" :show-overflow-tooltip="true">
          <template slot-scope="scope">{{ scope.row.refundOrderNo || '-' }}</template>
        </el-table-column>
        <el-table-column label="变更字段" prop="changeFields" min-width="140" :show-overflow-tooltip="true" />
        <el-table-column label="原因" prop="reason" min-width="160" :show-overflow-tooltip="true" />
        <el-table-column label="操作人" prop="operatorName" width="100" />
        <el-table-column label="设备" prop="device" width="90" />
        <el-table-column label="时间" prop="createdAt" width="160" />
      </el-table>
      <div slot="footer">
        <el-button @click="logVisible=false">关闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listCardTypes, listMemberCards, listMemberCardLogs, refundMemberCard } from '@/api/member/member'
import { listDept } from '@/api/system/dept'

export default {
  name: 'MemberCardRecord',
  data() {
    return {
      loading: false,
      total: 0,
      cardList: [],
      cardTypes: [],
      deptOptions: [],
      paidRange: [],
      effectiveRange: [],
      expiredRange: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        memberName: null,
        memberPhone: null,
        memberNo: null,
        cardTypeId: null,
        status: null,
        saleOrderNo: null,
        refundOrderNo: null,
        deptId: null,
        createStaffName: null
      },
      refundVisible: false,
      refunding: false,
      refundCard: {},
      refundForm: { refundAmount: null, refundType: 'CASH', reason: '' },
      refundRules: {
        refundAmount: [{ required: true, message: '请填写', trigger: 'blur' }],
        refundType: [{ required: true, message: '请填写', trigger: 'change' }],
        reason: [{ required: true, message: '请填写', trigger: 'blur' }]
      },
      logVisible: false,
      logLoading: false,
      logList: []
    }
  },
  created() {
    this.getList()
    listCardTypes().then(r => { this.cardTypes = r.data || [] })
    listDept().then(r => { this.deptOptions = r.data || [] })
  },
  methods: {
    getList() {
      this.loading = true
      const params = this.buildQueryParams()
      listMemberCards(params).then(response => {
        this.cardList = response.rows || []
        this.total = response.total || 0
        this.loading = false
      }).catch(() => { this.loading = false })
    },

    buildQueryParams() {
      const params = { ...this.queryParams }
      if (this.paidRange && this.paidRange.length === 2) {
        params.beginPaidAt = this.paidRange[0]
        params.endPaidAt = this.paidRange[1]
      }
      if (this.effectiveRange && this.effectiveRange.length === 2) {
        params.beginEffectiveAt = this.effectiveRange[0]
        params.endEffectiveAt = this.effectiveRange[1]
      }
      if (this.expiredRange && this.expiredRange.length === 2) {
        params.beginExpiredAt = this.expiredRange[0]
        params.endExpiredAt = this.expiredRange[1]
      }
      return params
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.paidRange = []
      this.effectiveRange = []
      this.expiredRange = []
      this.queryParams = {
        pageNum: 1,
        pageSize: 10,
        memberName: null,
        memberPhone: null,
        memberNo: null,
        cardTypeId: null,
        status: null,
        saleOrderNo: null,
        refundOrderNo: null,
        deptId: null,
        createStaffName: null
      }
      this.handleQuery()
    },
    handleRefund(row) {
      this.refundCard = row
      this.refundForm = { refundAmount: Number(row.saleAmount || 0), refundType: 'CASH', reason: '' }
      this.refundVisible = true
      this.$nextTick(() => { if (this.$refs.refundForm) this.$refs.refundForm.clearValidate() })
    },
    submitRefund() {
      this.$refs.refundForm.validate(valid => {
        if (!valid || !this.refundCard.id) return
        this.$confirm('确认退还该会员卡并生成退款单吗？', '退卡确认', { type: 'warning' }).then(() => {
          this.refunding = true
          refundMemberCard(this.refundCard.id, this.refundForm).then(r => {
            if (r.code === 200) {
              this.$modal.msgSuccess('退卡成功')
              this.refundVisible = false
              this.getList()
            } else {
              this.$modal.msgError(r.msg)
            }
            this.refunding = false
          }).catch(() => { this.refunding = false })
        })
      })
    },
    handleLog(row) {
      this.logVisible = true
      this.logLoading = true
      listMemberCardLogs(row.id).then(r => {
        this.logList = r.data || []
        this.logLoading = false
      }).catch(() => { this.logLoading = false })
    },
    canRefundCard(card) {
      if (!card || (card.status !== 0 && card.status !== 1) || !card.paidAt) return false
      return Date.now() - new Date(card.paidAt).getTime() <= 7 * 24 * 60 * 60 * 1000
    },
    cardStatusText(status) {
      return { 0: '待生效', 1: '生效中', 2: '已过期', 3: '已退款' }[status] || '未知'
    },
    cardStatusTag(status) {
      return { 0: 'warning', 1: 'success', 2: 'info', 3: 'danger' }[status] || 'info'
    },
    formatExportTimestamp(date) {
      const pad = n => String(n).padStart(2, '0')
      return date.getFullYear() +
        pad(date.getMonth() + 1) +
        pad(date.getDate()) + '_' +
        pad(date.getHours()) +
        pad(date.getMinutes()) +
        pad(date.getSeconds())
    },
    exportConditionName() {
      const parts = []
      const dept = this.deptOptions.find(d => String(d.deptId) === String(this.queryParams.deptId))
      const cardType = this.cardTypes.find(c => String(c.id) === String(this.queryParams.cardTypeId))
      const status = this.queryParams.status !== null && this.queryParams.status !== undefined && this.queryParams.status !== ''
        ? this.cardStatusText(this.queryParams.status)
        : null
      if (dept && dept.deptName) parts.push(dept.deptName)
      if (cardType && cardType.typeName) parts.push(cardType.typeName)
      if (status) parts.push(status)
      if (this.queryParams.memberNo) parts.push(this.queryParams.memberNo)
      if (this.queryParams.memberName) parts.push(this.queryParams.memberName)
      if (this.queryParams.memberPhone) parts.push(this.queryParams.memberPhone)
      if (this.queryParams.saleOrderNo) parts.push(this.queryParams.saleOrderNo)
      if (this.queryParams.refundOrderNo) parts.push(this.queryParams.refundOrderNo)
      if (this.paidRange && this.paidRange.length === 2) parts.push('付款' + this.paidRange[0].slice(0, 10) + '至' + this.paidRange[1].slice(0, 10))
      if (this.effectiveRange && this.effectiveRange.length === 2) parts.push('生效' + this.effectiveRange[0].slice(0, 10) + '至' + this.effectiveRange[1].slice(0, 10))
      if (this.expiredRange && this.expiredRange.length === 2) parts.push('到期' + this.expiredRange[0].slice(0, 10) + '至' + this.expiredRange[1].slice(0, 10))
      return (parts.length ? parts.join('_') : '全部').replace(/[\\/:*?"<>|\s]+/g, '_')
    },
    handleExport() {
      const params = this.buildQueryParams()
      delete params.pageNum
      delete params.pageSize
      const filename = `会员卡记录_${this.exportConditionName()}_${this.formatExportTimestamp(new Date())}.xlsx`
      this.download('member/card/export', params, filename)
    }
  }
}
</script>

<style scoped>
.refund-summary {
  margin-bottom: 16px;
}
</style>
