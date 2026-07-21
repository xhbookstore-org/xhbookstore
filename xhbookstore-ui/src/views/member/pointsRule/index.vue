<template>
  <div class="app-container">
    <el-form ref="queryForm" :model="queryParams" :inline="true" label-width="76px">
      <el-form-item label="规则名称" prop="ruleName">
        <el-input v-model="queryParams.ruleName" placeholder="名称或编码" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="积分方向" prop="direction">
        <el-select v-model="queryParams.direction" placeholder="全部" clearable style="width:140px">
          <el-option label="获取积分" value="ADD" />
          <el-option label="消耗积分" value="DEDUCT" />
        </el-select>
      </el-form-item>
      <el-form-item label="开发状态" prop="implementationStatus">
        <el-select v-model="queryParams.implementationStatus" placeholder="全部" clearable style="width:140px">
          <el-option label="已有" value="EXISTING" />
          <el-option label="开发中" value="IN_PROGRESS" />
          <el-option label="暂无" value="NOT_STARTED" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">查询</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['system:pointsRule:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate()" v-hasPermi="['system:pointsRule:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete()" v-hasPermi="['system:pointsRule:remove']">删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" />
    </el-row>

    <el-alert title="规则修改只影响后续新订单；历史积分流水保留原规则快照。已有积分流水的规则不能删除，可将状态改为停用。" type="info" :closable="false" class="rule-alert" />

    <el-table v-loading="loading" :data="ruleList" border @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="45" align="center" />
      <el-table-column label="规则编码" prop="ruleCode" width="190" show-overflow-tooltip />
      <el-table-column label="规则名称" prop="ruleName" min-width="140" />
      <el-table-column label="方向" width="80" align="center">
        <template slot-scope="scope"><el-tag size="mini" :type="scope.row.direction === 'ADD' ? 'success' : 'danger'">{{ scope.row.direction === 'ADD' ? '获取' : '消耗' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="开发状态" width="90" align="center">
        <template slot-scope="scope"><el-tag size="mini" :type="implementationTag(scope.row.implementationStatus)">{{ implementationText(scope.row.implementationStatus) }}</el-tag></template>
      </el-table-column>
      <el-table-column label="触发方式" width="90" align="center">
        <template slot-scope="scope">{{ scope.row.triggerMode === 'AUTO' ? '自动' : '手动' }}</template>
      </el-table-column>
      <el-table-column label="积分规则" min-width="145"><template slot-scope="scope">{{ pointsValueText(scope.row) }}</template></el-table-column>
      <el-table-column label="冻结" width="70" align="center"><template slot-scope="scope">{{ scope.row.freezeDays ? scope.row.freezeDays + '天' : '—' }}</template></el-table-column>
      <el-table-column label="积分有效期" width="105" align="center"><template slot-scope="scope">{{ (scope.row.pointsValidDays || 360) + '天' }}</template></el-table-column>
      <el-table-column label="运行状态" width="90" align="center">
        <template slot-scope="scope"><el-tag size="mini" :type="scope.row.status === 'ENABLED' ? 'success' : 'info'">{{ ruleStatusText(scope.row.status) }}</el-tag></template>
      </el-table-column>
      <el-table-column label="备注" prop="remark" min-width="180" show-overflow-tooltip />
      <el-table-column label="最近操作人" prop="operatorName" width="100" />
      <el-table-column label="最近操作时间" width="160"><template slot-scope="scope">{{ parseTime(scope.row.operationTime || scope.row.updatedAt) || '—' }}</template></el-table-column>
      <el-table-column label="操作" width="170" fixed="right" align="center">
        <template slot-scope="scope">
          <el-button type="text" size="mini" icon="el-icon-view" @click="handleView(scope.row)" v-hasPermi="['system:pointsRule:query']">查看</el-button>
          <el-button type="text" size="mini" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:pointsRule:edit']">修改</el-button>
          <el-button type="text" size="mini" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['system:pointsRule:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="dialogTitle" :visible.sync="open" width="820px" append-to-body :close-on-click-modal="false">
      <el-form ref="form" :model="form" :rules="rules" :disabled="viewOnly" label-width="112px">
        <el-divider content-position="left">基本信息</el-divider>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="规则编码" prop="ruleCode"><el-input v-model="form.ruleCode" :disabled="!!form.id" placeholder="如 BOOK_REWARD" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="规则名称" prop="ruleName"><el-input v-model="form.ruleName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="业务场景编码" prop="sceneCode"><el-input v-model="form.sceneCode" placeholder="如 PURCHASE_BOOK" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="触发事件" prop="triggerEvent"><el-input v-model="form.triggerEvent" placeholder="如 ORDER_PAID" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="规则来源"><el-select v-model="form.ruleSource"><el-option label="系统" value="SYSTEM" /><el-option label="自定义" value="CUSTOM" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="开发状态"><el-select v-model="form.implementationStatus"><el-option label="已有" value="EXISTING" /><el-option label="开发中" value="IN_PROGRESS" /><el-option label="暂无" value="NOT_STARTED" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="运行状态"><el-select v-model="form.status"><el-option label="草稿" value="DRAFT" /><el-option label="启用" value="ENABLED" /><el-option label="停用" value="DISABLED" /><el-option label="结束" value="ENDED" /></el-select></el-form-item></el-col>
        </el-row>

        <el-divider content-position="left">积分计算</el-divider>
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="积分方向" prop="direction"><el-select v-model="form.direction"><el-option label="获取积分" value="ADD" /><el-option label="消耗积分" value="DEDUCT" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="触发方式" prop="triggerMode"><el-select v-model="form.triggerMode"><el-option label="自动" value="AUTO" /><el-option label="手动" value="MANUAL" /></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="计算方式" prop="calculationMode"><el-select v-model="form.calculationMode" @change="handleCalculationChange"><el-option label="固定积分" value="FIXED" /><el-option label="按数量" value="PER_ITEM" /><el-option label="按金额" value="PER_YUAN" /><el-option label="人工输入" value="MANUAL" /><el-option label="按原订单" value="ORIGINAL_ORDER" /></el-select></el-form-item></el-col>
          <el-col v-if="form.calculationMode === 'FIXED'" :span="8"><el-form-item label="固定积分"><el-input-number v-model="form.fixedPoints" :min="1" :max="999999" :precision="0" /></el-form-item></el-col>
          <el-col v-if="form.calculationMode === 'PER_ITEM' || form.calculationMode === 'PER_YUAN'" :span="8"><el-form-item label="单位积分"><el-input-number v-model="form.pointsPerUnit" :min="1" :max="999999" :precision="0" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="冻结天数"><el-input-number v-model="form.freezeDays" :min="0" :max="3650" :precision="0" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="积分有效期" prop="pointsValidDays"><el-input-number v-model="form.pointsValidDays" :min="1" :max="3650" :precision="0" /></el-form-item></el-col>
        </el-row>

        <el-divider content-position="left">适用策略</el-divider>
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="必须业务单号"><el-switch v-model="form.requireBizOrder" :active-value="1" :inactive-value="0" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="必须凭证"><el-switch v-model="form.requireEvidence" :active-value="1" :inactive-value="0" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="排除批量采购"><el-switch v-model="form.excludeBulkPurchase" :active-value="1" :inactive-value="0" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="启用会员日"><el-switch v-model="form.memberDayEnabled" :active-value="1" :inactive-value="0" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="允许修改积分"><el-switch v-model="form.manualPointsEditable" :active-value="1" :inactive-value="0" /></el-form-item></el-col>
          <el-col v-if="form.memberDayEnabled === 1" :span="8"><el-form-item label="会员日日期"><el-input v-model="form.memberDayDays" placeholder="[6,16,26]" /></el-form-item></el-col>
          <el-col v-if="form.memberDayEnabled === 1" :span="8"><el-form-item label="会员日倍数"><el-input-number v-model="form.memberDayMultiplier" :min="0.001" :max="99" :precision="3" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="单会员次数"><el-input-number v-model="form.memberLimit" :min="0" :precision="0" placeholder="不填不限" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="规则总次数"><el-input-number v-model="form.totalLimit" :min="0" :precision="0" placeholder="不填不限" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="单笔积分上限"><el-input-number v-model="form.maxPointsPerOrder" :min="0" :precision="0" placeholder="不填不限" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="积分预算"><el-input-number v-model="form.budgetPoints" :min="0" :precision="0" placeholder="不填不限" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="生效时间"><el-date-picker v-model="form.effectiveFrom" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" placeholder="立即生效" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="结束时间"><el-date-picker v-model="form.effectiveTo" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" placeholder="长期有效" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" :precision="0" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="3" maxlength="500" show-word-limit /></el-form-item></el-col>
        </el-row>
      </el-form>
      <div slot="footer">
        <el-button v-if="!viewOnly" type="primary" :loading="submitting" @click="submitForm">确定</el-button>
        <el-button @click="open=false">{{ viewOnly ? '关闭' : '取消' }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listPointsRule, getPointsRule, addPointsRule, updatePointsRule, delPointsRule } from '@/api/member/pointsRule'

export default {
  name: 'SystemPointsRule',
  data() {
    return {
      loading: false,
      submitting: false,
      showSearch: true,
      total: 0,
      ruleList: [],
      ids: [],
      single: true,
      multiple: true,
      open: false,
      viewOnly: false,
      dialogTitle: '',
      form: {},
      queryParams: { pageNum: 1, pageSize: 20, ruleName: null, direction: null, implementationStatus: null },
      rules: {
        ruleCode: [{ required: true, message: '请输入规则编码', trigger: 'blur' }, { pattern: /^[A-Za-z][A-Za-z0-9_]{1,63}$/, message: '须为2-64位字母、数字或下划线', trigger: 'blur' }],
        ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
        sceneCode: [{ required: true, message: '请输入业务场景编码', trigger: 'blur' }],
        triggerEvent: [{ required: true, message: '请输入触发事件', trigger: 'blur' }],
        direction: [{ required: true, message: '请选择积分方向', trigger: 'change' }],
        triggerMode: [{ required: true, message: '请选择触发方式', trigger: 'change' }],
        calculationMode: [{ required: true, message: '请选择计算方式', trigger: 'change' }]
      }
    }
  },
  created() { this.getList() },
  methods: {
    getList() {
      this.loading = true
      listPointsRule(this.queryParams).then(response => {
        this.ruleList = response.rows || []
        this.total = response.total || 0
        this.loading = false
      }).catch(() => { this.loading = false })
    },
    resetFormData() {
      this.form = {
        id: null, ruleCode: '', ruleName: '', sceneCode: '', ruleSource: 'CUSTOM',
        implementationStatus: 'NOT_STARTED', direction: 'ADD', triggerMode: 'MANUAL',
        triggerEvent: '', calculationMode: 'FIXED', fixedPoints: null, pointsPerUnit: null,
        manualPointsEditable: 0, memberDayEnabled: 0,
        memberDayDays: '[6,16,26]', memberDayMultiplier: 2, effectiveFrom: null, effectiveTo: null,
        memberLimit: null, totalLimit: null, budgetPoints: null, maxPointsPerOrder: null,
        requireBizOrder: 1, requireEvidence: 0, excludeBulkPurchase: 0,
        freezeDays: 0, pointsValidDays: 360, status: 'DRAFT', sortOrder: 0, remark: ''
      }
      this.$nextTick(() => { if (this.$refs.form) this.$refs.form.clearValidate() })
    },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    resetQuery() { this.$refs.queryForm.resetFields(); this.handleQuery() },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length !== 1
      this.multiple = selection.length === 0
    },
    handleAdd() {
      this.resetFormData()
      this.viewOnly = false
      this.dialogTitle = '新增积分规则'
      this.open = true
    },
    loadRule(id, viewOnly) {
      getPointsRule(id).then(response => {
        this.form = response.data
        this.form.pointsValidDays = this.form.pointsValidDays || 360
        this.form.memberDayDays = this.form.memberDayDays || '[6,16,26]'
        this.viewOnly = viewOnly
        this.dialogTitle = viewOnly ? '查看积分规则' : '修改积分规则'
        this.open = true
        this.$nextTick(() => { if (this.$refs.form) this.$refs.form.clearValidate() })
      })
    },
    handleView(row) { this.loadRule(row.id, true) },
    handleUpdate(row) {
      const id = row && row.id ? row.id : this.ids[0]
      this.loadRule(id, false)
    },
    handleCalculationChange(mode) {
      if (mode !== 'FIXED') this.form.fixedPoints = null
      if (mode !== 'PER_ITEM' && mode !== 'PER_YUAN') this.form.pointsPerUnit = null
      if (mode === 'MANUAL') this.form.manualPointsEditable = 1
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        this.submitting = true
        const request = this.form.id ? updatePointsRule(this.form) : addPointsRule(this.form)
        request.then(response => {
          this.$modal.msgSuccess(response.msg)
          this.open = false
          this.getList()
          this.submitting = false
        }).catch(() => { this.submitting = false })
      })
    },
    handleDelete(row) {
      const ids = row && row.id ? [row.id] : this.ids
      const names = row && row.ruleName ? row.ruleName : ids.join(',')
      this.$modal.confirm('确认删除积分规则“' + names + '”吗？已有积分流水的规则将禁止删除。').then(() => {
        return delPointsRule(ids.join(','))
      }).then(response => {
        this.$modal.msgSuccess(response.msg || '删除成功')
        this.getList()
      }).catch(() => {})
    },
    implementationText(status) { return { EXISTING: '已有', IN_PROGRESS: '开发中', NOT_STARTED: '暂无' }[status] || status || '—' },
    implementationTag(status) { return { EXISTING: 'success', IN_PROGRESS: 'warning', NOT_STARTED: 'info' }[status] || 'info' },
    ruleStatusText(status) { return { ENABLED: '启用', DRAFT: '草稿', DISABLED: '停用', ENDED: '结束' }[status] || status || '—' },
    pointsValueText(row) {
      if (row.calculationMode === 'FIXED') return row.fixedPoints ? `${row.fixedPoints} 积分` : '待配置'
      if (row.calculationMode === 'PER_ITEM') return row.pointsPerUnit ? `每本 ${Number(row.pointsPerUnit)} 积分` : '待配置'
      if (row.calculationMode === 'PER_YUAN') return row.pointsPerUnit ? `1元 = ${Number(row.pointsPerUnit)} 积分` : '待配置'
      if (row.calculationMode === 'MANUAL') return '人工输入'
      if (row.calculationMode === 'ORIGINAL_ORDER') return '按原订单实际积分'
      return '待配置'
    }
  }
}
</script>

<style scoped>
.rule-alert { margin-bottom: 16px; }
.el-select, .el-date-editor { width: 100%; }
.el-input-number { width: 100%; }
</style>
