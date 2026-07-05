<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" label-width="84px">
      <el-form-item label="订单号" prop="orderNo">
        <el-input v-model="queryParams.orderNo" placeholder="请输入订单号" clearable @keyup.enter.native="handleQuery" />
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
      <el-form-item label="支付方式" prop="paymentType">
        <el-select v-model="queryParams.paymentType" placeholder="请选择支付方式" clearable>
          <el-option label="员工录入" value="STAFF_INPUT" />
          <el-option label="ERP导入" value="ERP_IMPORT" />
          <el-option label="现金" value="CASH" />
          <el-option label="其他" value="OTHER" />
        </el-select>
      </el-form-item>
      <el-form-item label="订单状态" prop="orderStatus">
        <el-select v-model="queryParams.orderStatus" placeholder="请输入" clearable>
          <el-option label="已支付" :value="1" />
          <el-option label="已取消" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="门店" prop="deptId">
        <el-select v-model="queryParams.deptId" placeholder="请选择门店" clearable>
          <el-option v-for="d in deptOptions" :key="d.deptId" :label="d.deptName" :value="d.deptId" />
        </el-select>
      </el-form-item>
      <el-form-item label="操作员工" prop="createStaffName">
        <el-input v-model="queryParams.createStaffName" placeholder="请输入" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="支付时间">
        <el-date-picker
          v-model="payRange"
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
      <el-table-column label="订单号" prop="orderNo" width="190" :show-overflow-tooltip="true" />
      <el-table-column label="会员编号" prop="memberNo" width="135" />
      <el-table-column label="会员姓名" prop="memberName" min-width="100" :show-overflow-tooltip="true" />
      <el-table-column label="手机号" prop="memberPhone" width="125" />
      <el-table-column label="卡类型" prop="cardTypeName" min-width="110" />
      <el-table-column label="应收金额" prop="receivableAmount" width="95" align="right" />
      <el-table-column label="实收金额" prop="paidAmount" width="95" align="right" />
      <el-table-column label="支付方式" width="95">
        <template slot-scope="scope">{{ paymentTypeText(scope.row.paymentType) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90" align="center">
        <template slot-scope="scope">
          <el-tag size="mini" :type="scope.row.orderStatus === 1 ? 'success' : 'info'">{{ orderStatusText(scope.row.orderStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="支付时间" width="160">
        <template slot-scope="scope">{{ parseTime(scope.row.payTime) || '-' }}</template>
      </el-table-column>
      <el-table-column label="会员卡ID" prop="memberCardId" width="90">
        <template slot-scope="scope">{{ scope.row.memberCardId || '-' }}</template>
      </el-table-column>
      <el-table-column label="门店" prop="deptName" min-width="110" :show-overflow-tooltip="true" />
      <el-table-column label="操作员工" prop="createStaffName" width="100" />
      <el-table-column label="鍒涘缓时间" width="160">
        <template slot-scope="scope">{{ parseTime(scope.row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="备注" prop="remark" min-width="150" :show-overflow-tooltip="true" />
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
  </div>
</template>

<script>
import { listCardTypes, listMemberCardOrders } from '@/api/member/member'
import { listDept } from '@/api/system/dept'

export default {
  name: 'MemberCardOrder',
  data() {
    return {
      loading: false,
      total: 0,
      orderList: [],
      cardTypes: [],
      deptOptions: [],
      payRange: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        orderNo: null,
        memberNo: null,
        memberNameLike: null,
        memberPhone: null,
        cardTypeId: null,
        paymentType: null,
        orderStatus: null,
        deptId: null,
        createStaffName: null
      }
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
      const params = { ...this.queryParams }
      if (this.payRange && this.payRange.length === 2) {
        params.beginPayTime = this.payRange[0]
        params.endPayTime = this.payRange[1]
      }
      listMemberCardOrders(params).then(response => {
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
      this.payRange = []
      this.queryParams = {
        pageNum: 1,
        pageSize: 10,
        orderNo: null,
        memberNo: null,
        memberNameLike: null,
        memberPhone: null,
        cardTypeId: null,
        paymentType: null,
        orderStatus: null,
        deptId: null,
        createStaffName: null
      }
      this.handleQuery()
    },
    tableIndex(index) {
      return (this.queryParams.pageNum - 1) * this.queryParams.pageSize + index + 1
    },
    orderStatusText(status) {
      return { 1: '已支付', 2: '已取消' }[status] || '未知'
    },
    paymentTypeText(type) {
      return { STAFF_INPUT: '员工录入', ERP_IMPORT: 'ERP导入', CASH: '现金', OTHER: '其他' }[type] || (type || '-')
    }
  }
}
</script>



