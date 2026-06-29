<template>
  <div class="app-container">
    <!-- 搜索栏 -->
    <el-form :model="queryParams" ref="queryForm" :inline="true" label-width="68px">
      <el-form-item label="姓名" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入姓名" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="queryParams.phone" placeholder="请输入手机号" clearable @keyup.enter.native="handleQuery"/>
      </el-form-item>
      <el-form-item label="门店" prop="deptId">
        <el-select v-model="queryParams.deptId" placeholder="请选择门店" clearable>
          <el-option v-for="d in deptOptions" :key="d.deptId" :label="d.deptName" :value="d.deptId"/>
        </el-select>
      </el-form-item>
      <el-form-item label="注册时间">
        <el-date-picker v-model="dateRange" type="daterange" range-separator="至"
          start-placeholder="开始日期" end-placeholder="结束日期" value-format="yyyyMMdd"/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 操作按钮 -->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="el-icon-upload2" size="mini" @click="handleImport">导入</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport">导出</el-button>
      </el-col>
    </el-row>

    <!-- 表格 -->
    <el-table v-loading="loading" :data="memberList" row-key="id" style="width:100%">
      <el-table-column label="编号" type="index" width="55" align="center"/>
      <el-table-column label="姓名" align="center" prop="name" min-width="80" :show-overflow-tooltip="true">
        <template slot-scope="scope">{{ scope.row.name || '—' }}</template>
      </el-table-column>
      <el-table-column label="会员编号" align="center" prop="cardNo" width="140"/>
      <el-table-column label="手机" align="center" prop="phone" width="130"/>
      <el-table-column label="会员类型" align="center" prop="cardTypeName" width="110"/>
      <el-table-column label="开通门店" align="center" prop="deptName" min-width="120" :show-overflow-tooltip="true"/>
      <el-table-column label="有效期" align="center" prop="validDate" width="115">
        <template slot-scope="scope">{{ scope.row.validDate || '—' }}</template>
      </el-table-column>
      <el-table-column label="借阅次数" align="center" prop="borrowCountValid" width="85"/>
      <el-table-column label="积分" align="center" prop="currentPoints" width="70"/>
      <el-table-column label="备注" align="center" prop="remark" :show-overflow-tooltip="true" min-width="130"/>
      <el-table-column label="最近操作人" align="center" prop="lastOperator" width="100"/>
      <el-table-column label="最后操作时间" align="center" prop="updatedAt" width="160">
        <template slot-scope="scope">{{ parseTime(scope.row.updatedAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="200" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-star-off" @click="handlePoints(scope.row)">积分</el-button>
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(scope.row)">查看</el-button>
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleEdit(scope.row)">修改</el-button>
          <el-dropdown size="mini" @command="(command) => handleCommand(command, scope.row)">
            <el-button size="mini" type="text" icon="el-icon-d-arrow-right">更多</el-button>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="handleDelete" icon="el-icon-delete">删除</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList"/>

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="500px" :close-on-click-modal="false" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" maxlength="50"/>
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11"/>
        </el-form-item>
        <el-form-item label="会员类型" prop="cardTypeId">
          <el-select v-model="form.cardTypeId" placeholder="请选择会员类型" @change="calcValidDate" style="width:220px">
            <el-option v-for="ct in cardTypes" :key="ct.id" :label="ct.typeName" :value="ct.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="有效期" v-if="form.validDate">
          <el-input v-model="form.validDate" :disabled="true" style="width:220px"/>
          <span v-if="form.validDateTips" style="margin-left:10px;color:#999;font-size:12px">{{ form.validDateTips }}</span>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注（单位名称）" maxlength="500"/>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button type="primary" @click="submitForm" :loading="submitting">确 定</el-button>
        <el-button @click="dialogVisible=false">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog title="会员详情" :visible.sync="detailVisible" width="650px" append-to-body>
      <el-descriptions :column="2" border v-if="detail.member">
        <el-descriptions-item label="姓名">{{ detail.member.name || '—' }}</el-descriptions-item>
        <el-descriptions-item label="电话">{{ detail.member.phone || '—' }}</el-descriptions-item>
        <el-descriptions-item label="会员卡号">{{ detail.member.cardNo }}</el-descriptions-item>
        <el-descriptions-item label="会员类型">{{ detail.member.cardTypeName }}</el-descriptions-item>
        <el-descriptions-item label="开通门店">{{ detail.member.deptName }}</el-descriptions-item>
        <el-descriptions-item label="有效期">{{ detail.member.validDate || '—' }}</el-descriptions-item>
        <el-descriptions-item label="借阅次数">{{ detail.member.borrowCountValid }}</el-descriptions-item>
        <el-descriptions-item label="当前积分">{{ detail.member.currentPoints }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.member.remark || '—' }}</el-descriptions-item>
      </el-descriptions>
      <div slot="footer">
        <el-button type="primary" @click="detailToEdit">编 辑</el-button>
        <el-button @click="detailVisible=false">关 闭</el-button>
      </div>
    </el-dialog>

    <!-- 积分管理弹窗 -->
    <el-dialog title="积分管理" :visible.sync="pointsVisible" width="950px" append-to-body>
      <p style="margin-bottom:10px">
        会员：<b>{{ pointsMember.name }}</b>（{{ pointsMember.cardNo }}）
        当前积分：<b style="color:#409EFF">{{ pointsMember.currentPoints }}</b>
      </p>
      <el-button type="primary" size="small" icon="el-icon-plus" @click="showAddPoints" style="margin-bottom:10px">添加积分</el-button>
      <el-table :data="pointsList" size="small" max-height="300">
        <el-table-column label="订单号" prop="orderNumber" width="200" :show-overflow-tooltip="true"/>
        <el-table-column label="金额" prop="amount" width="80">
          <template slot-scope="s">+{{ s.row.amount }}</template>
        </el-table-column>
        <el-table-column label="操作前" prop="orginPoints" width="80"/>
        <el-table-column label="操作后" prop="afterPoints" width="80"/>
        <el-table-column label="描述" prop="description" min-width="120" :show-overflow-tooltip="true"/>
        <el-table-column label="操作设备" prop="operationDevice" width="80"/>
        <el-table-column label="时间" prop="createdAt" width="160"/>
      </el-table>
      <div slot="footer">
        <el-button @click="pointsVisible=false">关 闭</el-button>
      </div>
    </el-dialog>

    <!-- 添加积分弹窗 -->
    <el-dialog title="添加积分" :visible.sync="addPointsVisible" width="400px" append-to-body>
      <el-form ref="addPointsForm" :model="addPointsForm" :rules="addPointsRules" label-width="80px">
        <el-form-item label="积分数额" prop="points">
          <el-input-number v-model="addPointsForm.points" :min="1" :max="99999" placeholder="请输入积分数额" style="width:100%"/>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="addPointsForm.description" type="textarea" placeholder="请输入积分变动原因" maxlength="255"/>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button type="primary" @click="submitAddPoints" :loading="addingPoints">确 定</el-button>
        <el-button @click="addPointsVisible=false">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 删除确认 -->
    <el-dialog title="删除确认" :visible.sync="deleteVisible" width="400px" append-to-body>
      <p style="text-align:center;font-size:16px;padding:20px">删除不可恢复，是否删除？</p>
      <div slot="footer">
        <el-button type="danger" @click="confirmDelete">确认删除</el-button>
        <el-button @click="deleteVisible=false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listMember, getMember, addMember, updateMember, delMember, listCardTypes, listPoints, addPoints } from '@/api/member/member'
import { listDept } from '@/api/system/dept'

export default {
  name: 'MemberList',
  data() {
    return {
      loading: false, submitting: false,
      total: 0, memberList: [],
      dateRange: [],
      queryParams: { pageNum: 1, pageSize: 10, name: null, phone: null, deptId: null },
      deptOptions: [],
      cardTypes: [],
      // Dialog
      dialogVisible: false, dialogTitle: '', isEdit: false,
      form: { id: null, name: '', phone: '', cardTypeId: null, validDate: '', validDateTips: '', remark: '', deptId: null },
      rules: {
        name: [{ required: true, message: '姓名不能为空', trigger: 'blur' }],
        phone: [{ required: true, message: '手机号不能为空', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }],
        cardTypeId: [{ required: true, message: '请选择会员类型', trigger: 'change' }]
      },
      // Detail
      detailVisible: false, detail: {},
      // Points
      pointsVisible: false, pointsMember: {}, pointsList: [],
      addPointsVisible: false, addingPoints: false,
      addPointsForm: { points: null, description: '' },
      addPointsRules: {
        points: [{ required: true, message: '请输入积分数额', trigger: 'blur' }],
        description: [{ required: true, message: '请输入描述', trigger: 'blur' }]
      },
      // Delete
      deleteVisible: false, deleteId: null
    }
  },
  created() {
    this.getList()
    this.getDeptOptions()
    listCardTypes().then(r => { this.cardTypes = r.data })
  },
  methods: {
    getList() {
      this.loading = true
      if (this.dateRange && this.dateRange.length === 2) {
        this.queryParams.params = { beginTime: this.dateRange[0], endTime: this.dateRange[1] }
      } else {
        this.queryParams.params = {}
      }
      listMember(this.queryParams).then(response => {
        this.memberList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    getDeptOptions() {
      listDept().then(r => { this.deptOptions = r.data })
    },
    handleQuery() { this.queryParams.pageNum = 1; this.getList() },
    resetQuery() {
      this.dateRange = []
      this.queryParams = { pageNum: 1, pageSize: 10, name: null, phone: null, deptId: null }
      this.handleQuery()
    },
    formatDate(date) {
      const y = date.getFullYear()
      const m = String(date.getMonth() + 1).padStart(2, '0')
      const d = String(date.getDate()).padStart(2, '0')
      return y + '-' + m + '-' + d
    },
    calcValidDate() {
      const ct = this.cardTypes.find(c => c.id === this.form.cardTypeId)
      if (!ct) {
        this.form.validDate = ''
        this.form.validDateTips = ''
        return
      }
      const today = new Date()
      if (ct.isRenewal === 0) {
        // 新卡：今天 + 有效天数
        const d = new Date(today.getFullYear(), today.getMonth(), today.getDate() + ct.validDays)
        this.form.validDate = this.formatDate(d)
        this.form.validDateTips = '（' + ct.typeName + '，' + ct.validDays + '天后到期）'
      } else {
        // 续费卡
        if (this.isEdit && this.form.validDate) {
          // 编辑模式：原有有效期 + 续费延长
          const parts = this.form.validDate.split('-')
          const d = new Date(parseInt(parts[0]), parseInt(parts[1]) - 1, parseInt(parts[2]) + ct.validDays)
          this.form.validDate = this.formatDate(d)
          this.form.validDateTips = '（' + ct.typeName + '，续费延长' + ct.validDays + '天）'
        } else {
          // 新增续费卡
          const d = new Date(today.getFullYear(), today.getMonth(), today.getDate() + ct.validDays)
          this.form.validDate = this.formatDate(d)
          this.form.validDateTips = '（' + ct.typeName + '，' + ct.validDays + '天后到期）'
        }
      }
    },
    handleAdd() {
      this.dialogTitle = '新增会员'; this.isEdit = false
      this.form = { id: null, name: '', phone: '', cardTypeId: null, validDate: '', validDateTips: '', remark: '', deptId: null }
      this.dialogVisible = true
      this.$nextTick(() => { if (this.$refs.form) this.$refs.form.clearValidate() })
    },
    handleEdit(row) {
      this.dialogTitle = '编辑会员'; this.isEdit = true
      getMember(row.id).then(r => {
        const m = r.member
        const ct = this.cardTypes.find(c => c.id === m.cardTypeId)
        let tips = ''
        if (ct && m.validDate) {
          tips = ct.isRenewal === 1 ? '（' + ct.typeName + '，修改类型将重新计算有效期）' : '（' + ct.typeName + '）'
        }
        this.form = { id: m.id, name: m.name, phone: m.phone, cardTypeId: m.cardTypeId, validDate: m.validDate || '', validDateTips: tips, remark: m.remark, deptId: m.deptId }
        this.dialogVisible = true
        this.$nextTick(() => { if (this.$refs.form) this.$refs.form.clearValidate() })
      })
    },
    handleView(row) {
      getMember(row.id).then(r => { this.detail = r; this.detailVisible = true })
    },
    detailToEdit() {
      this.detailVisible = false
      this.handleEdit({ id: this.detail.member.id })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        this.submitting = true
        const api = this.isEdit ? updateMember(this.form.id, this.form) : addMember(this.form)
        api.then(r => {
          if (r.code === 200) { this.$modal.msgSuccess(r.msg); this.dialogVisible = false; this.getList() }
          else { this.$modal.msgError(r.msg) }
          this.submitting = false
        }).catch(() => { this.submitting = false })
      })
    },
    handleCommand(command, row) {
      if (command === 'handleDelete') this.handleDelete(row)
    },
    handleDelete(row) { this.deleteId = row.id; this.deleteVisible = true },
    confirmDelete() {
      delMember(this.deleteId).then(r => {
        if (r.code === 200) { this.$modal.msgSuccess('删除成功'); this.deleteVisible = false; this.getList() }
        else { this.$modal.msgError(r.msg) }
      })
    },
    handlePoints(row) {
      this.pointsMember = row
      listPoints(row.id).then(r => {
        this.pointsList = r.data || []
        this.pointsVisible = true
      })
    },
    showAddPoints() {
      this.addPointsForm = { points: null, description: '' }
      this.addPointsVisible = true
      this.$nextTick(() => { if (this.$refs.addPointsForm) this.$refs.addPointsForm.clearValidate() })
    },
    submitAddPoints() {
      this.$refs.addPointsForm.validate(valid => {
        if (!valid) return
        this.addingPoints = true
        addPoints(this.pointsMember.id, this.addPointsForm).then(r => {
          if (r.code === 200) {
            this.$modal.msgSuccess(r.msg)
            this.addPointsVisible = false
            // Refresh points list and current points
            listPoints(this.pointsMember.id).then(res => { this.pointsList = res.data || [] })
            this.pointsMember.currentPoints = (this.pointsMember.currentPoints || 0) + this.addPointsForm.points
            this.getList()
          } else {
            this.$modal.msgError(r.msg)
          }
          this.addingPoints = false
        }).catch(() => { this.addingPoints = false })
      })
    },
    handleImport() { this.$modal.msgInfo('批量导入功能（二期开发）') },
    handleExport() {
      this.$modal.msgInfo('批量导出功能（二期开发）')
    }
  }
}
</script>