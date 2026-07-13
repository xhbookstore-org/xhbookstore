<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" label-width="96px">
      <el-form-item label="退款单号" prop="refundOrderNo">
        <el-input v-model="queryParams.refundOrderNo" placeholder="请输入退款单号" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="售卡单号" prop="saleOrderNo">
        <el-input v-model="queryParams.saleOrderNo" placeholder="请输入原售卡单号" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="会员编号" prop="memberNo">
        <el-input v-model="queryParams.memberNo" placeholder="请输入" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="会员姓名" prop="memberNameLike">
        <el-input v-model="queryParams.memberNameLike" placeholder="请输入" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="手机号" prop="memberPhone">
        <el-input v-model="queryParams.memberPhone" placeholder="请输入手机号" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="卡类型" prop="cardTypeId">
        <el-select v-model="queryParams.cardTypeId" placeholder="请输入" clearable>
          <el-option v-for="ct in cardTypes" :key="ct.id" :label="ct.typeName" :value="ct.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="退款方式" prop="refundType">
        <el-select v-model="queryParams.refundType" placeholder="请输入" clearable>
          <el-option label="现金退款" value="CASH" />
          <el-option label="原路退回" value="ORIGINAL" />
          <el-option label="其他" value="OTHER" />
        </el-select>
      </el-form-item>
      <el-form-item label="退款状态" prop="refundStatus">
        <el-select v-model="queryParams.refundStatus" placeholder="请输入" clearable>
          <el-option label="已退款" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="门店" prop="deptId">
        <el-select v-model="queryParams.deptId" placeholder="请选择门店" clearable>
          <el-option v-for="d in deptOptions" :key="d.deptId" :label="d.deptName" :value="d.deptId" />
        </el-select>
      </el-form-item>
      <el-form-item label="操作员工" prop="operatorName">
        <el-input v-model="queryParams.operatorName" placeholder="请输入" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="退款时间">
        <el-date-picker
          v-model="refundRange"
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
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="orderList" row-key="id" style="width:100%">
      <el-table-column label="ID" prop="id" width="70" align="center" />
      <el-table-column label="退款单号" prop="refundOrderNo" width="190" :show-overflow-tooltip="true" />
      <el-table-column label="售卡单号" prop="saleOrderNo" width="190" :show-overflow-tooltip="true" />
      <el-table-column label="会员编号" prop="memberNo" width="135" />
      <el-table-column label="会员姓名" prop="memberName" min-width="100" :show-overflow-tooltip="true" />
      <el-table-column label="手机号" prop="memberPhone" width="125" />
      <el-table-column label="卡类型" prop="cardTypeName" min-width="110" />
      <el-table-column label="退款金额" prop="refundAmount" width="95" align="right" />
      <el-table-column label="退款方式" width="95">
        <template slot-scope="scope">{{ refundTypeText(scope.row.refundType) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90" align="center">
        <template slot-scope="scope">
          <el-tag size="mini" type="success">{{ refundStatusText(scope.row.refundStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="时间" width="160">
        <template slot-scope="scope">{{ parseTime(scope.row.refundTime) }}</template>
      </el-table-column>
      <el-table-column label="门店" prop="deptName" min-width="110" :show-overflow-tooltip="true" />
      <el-table-column label="操作员工" prop="operatorName" width="100" />
      <el-table-column label="原因" prop="reason" min-width="160" :show-overflow-tooltip="true" />
      <el-table-column label="备注" prop="remark" min-width="150" :show-overflow-tooltip="true" />
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script>
import { listCardTypes, listMemberDepts, listMemberCardRefundOrders } from '@/api/member/member'

export default {
  name: 'MemberCardRefundOrder',
  data() {
    return {
      loading: false,
      total: 0,
      orderList: [],
      cardTypes: [],
      deptOptions: [],
      refundRange: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        refundOrderNo: null,
        saleOrderNo: null,
        memberNo: null,
        memberNameLike: null,
        memberPhone: null,
        cardTypeId: null,
        refundType: null,
        refundStatus: null,
        deptId: null,
        operatorName: null
      }
    }
  },
  created() {
    this.getList()
    listCardTypes().then(r => { this.cardTypes = r.data || [] })
    listMemberDepts().then(r => { this.deptOptions = r.data || [] })
  },
  methods: {
    getList() {
      this.loading = true
      const params = { ...this.queryParams }
      if (this.refundRange && this.refundRange.length === 2) {
        params.beginRefundTime = this.refundRange[0]
        params.endRefundTime = this.refundRange[1]
      }
      listMemberCardRefundOrders(params).then(response => {
        this.orderList = response.rows || []
        this.total = response.total || 0
        this.loading = false
      }).catch(() => { this.loading = false })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.refundRange = []
      this.queryParams = {
        pageNum: 1,
        pageSize: 10,
        refundOrderNo: null,
        saleOrderNo: null,
        memberNo: null,
        memberNameLike: null,
        memberPhone: null,
        cardTypeId: null,
        refundType: null,
        refundStatus: null,
        deptId: null,
        operatorName: null
      }
      this.handleQuery()
    },
    tableIndex(index) {
      return (this.queryParams.pageNum - 1) * this.queryParams.pageSize + index + 1
    },
    refundStatusText(status) {
      return { 1: '已退款' }[status] || '未知'
    },
    refundTypeText(type) {
      return { CASH: '现金退款', ORIGINAL: '原路退回', OTHER: '其他' }[type] || (type || '-')
    }
  }
}
</script>



