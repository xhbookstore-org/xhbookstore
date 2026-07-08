<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" :inline="true" label-width="68px">
      <el-form-item label="姓名" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入姓名" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="会员编号" prop="cardNo">
        <el-input v-model="queryParams.cardNo" placeholder="请输入会员编号" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="queryParams.phone" placeholder="请输入手机号" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="门店" prop="deptId">
        <el-select v-model="queryParams.deptId" placeholder="请选择门店" clearable>
          <el-option v-for="d in deptOptions" :key="d.deptId" :label="d.deptName" :value="d.deptId" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option label="正常" :value="0" />
          <el-option label="注销" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item label="注册时间">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="yyyyMMdd"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['member:member:add']">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="info" plain icon="el-icon-upload2" size="mini" @click="handleImport" v-hasPermi="['member:member:import']">导入</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button v-if="lastImportResult" type="info" plain icon="el-icon-document" size="mini" @click="importResultVisible=true">上次导入结果</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['member:member:export']">导出</el-button>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="memberList" row-key="id" style="width:100%">
      <el-table-column label="ID" prop="id" width="70" align="center" />
      <el-table-column label="姓名" align="center" prop="name" min-width="80" :show-overflow-tooltip="true">
        <template slot-scope="scope">{{ scope.row.name || '-' }}</template>
      </el-table-column>
      <el-table-column label="会员编号" align="center" prop="cardNo" width="140" />
      <el-table-column label="手机" align="center" prop="phone" width="130" />
      <el-table-column label="会员类型" align="center" prop="cardTypeName" width="110" />
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.status === 1" type="info" size="mini">注销</el-tag>
          <el-tag v-else type="success" size="mini">正常</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="开通门店" align="center" prop="deptName" min-width="120" :show-overflow-tooltip="true" />
      <el-table-column label="有效期" align="center" prop="validDate" width="115">
        <template slot-scope="scope">{{ scope.row.validDate || '-' }}</template>
      </el-table-column>
      <el-table-column label="借阅次数" align="center" prop="borrowCountValid" width="85" />
      <el-table-column label="积分" align="center" prop="currentPoints" width="70" />
      <el-table-column label="备注" align="center" prop="remark" :show-overflow-tooltip="true" min-width="130" />
      <el-table-column label="最近操作人" align="center" prop="lastOperator" width="100" />
      <el-table-column label="最后操作时间" align="center" prop="updatedAt" width="160">
        <template slot-scope="scope">{{ parseTime(scope.row.updatedAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="300" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-star-off" @click="handlePoints(scope.row)" v-hasPermi="['member:member:points']">积分</el-button>
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(scope.row)" v-hasPermi="['member:member:query']">查看</el-button>
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleEdit(scope.row)" v-hasPermi="['member:member:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-bank-card" @click="handleMemberCards(scope.row)" v-hasPermi="['member:card:list']">会员卡</el-button>
          <el-button size="mini" type="text" icon="el-icon-bank-card" @click="handleRefundCard(scope.row)" v-hasPermi="['member:card:refund']">退卡</el-button>
          <el-dropdown size="mini" @command="(command) => handleCommand(command, scope.row)">
            <el-button size="mini" type="text" icon="el-icon-d-arrow-right">更多</el-button>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="handleDelete" icon="el-icon-delete" v-hasPermi="['member:member:remove']">删除</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="500px" :close-on-click-modal="false" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入" maxlength="50" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="会员类型" prop="cardTypeId">
          <el-select v-model="form.cardTypeId" placeholder="请选择会员类型" @change="calcValidDate" style="width:220px">
            <el-option v-for="ct in cardTypes" :key="ct.id" :label="ct.typeName" :value="ct.id" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!isEdit && form.validDate" label="有效期">
          <el-input v-model="form.validDate" :disabled="true" style="width:220px" />
          <span v-if="form.validDateTips" style="margin-left:10px;color:#999;font-size:12px">{{ form.validDateTips }}</span>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入" maxlength="500" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button type="primary" @click="submitForm" :loading="submitting">确定</el-button>
        <el-button @click="dialogVisible=false">取消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="会员详情" :visible.sync="detailVisible" width="650px" append-to-body>
      <el-descriptions :column="2" border v-if="detail.member">
        <el-descriptions-item label="姓名">{{ detail.member.name || '-' }}</el-descriptions-item>
        <el-descriptions-item label="电话">{{ detail.member.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="会员卡号">{{ detail.member.cardNo }}</el-descriptions-item>
        <el-descriptions-item label="会员类型">{{ detail.member.cardTypeName }}</el-descriptions-item>
        <el-descriptions-item label="开通门店">{{ detail.member.deptName }}</el-descriptions-item>
        <el-descriptions-item label="有效期">{{ detail.member.validDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="借阅次数">{{ detail.member.borrowCountValid }}</el-descriptions-item>
        <el-descriptions-item label="当前积分">{{ detail.member.currentPoints }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.member.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <div slot="footer">
        <el-button type="primary" @click="detailToEdit" v-hasPermi="['member:member:edit']">编辑</el-button>
        <el-button @click="detailVisible=false">关闭</el-button>
      </div>
    </el-dialog>

    <el-dialog title="积分管理" :visible.sync="pointsVisible" width="950px" append-to-body>
      <p style="margin-bottom:10px">
        会员：<b>{{ pointsMember.name }}</b>（{{ pointsMember.cardNo }}） 当前积分：<b style="color:#409EFF">{{ pointsMember.currentPoints }}</b>
      </p>
      <el-button type="primary" size="small" icon="el-icon-plus" @click="showAddPoints" style="margin-bottom:10px" v-hasPermi="['member:member:points']">添加积分</el-button>
      <el-table :data="pointsList" size="small" max-height="300">
        <el-table-column label="订单号" prop="orderNumber" width="200" :show-overflow-tooltip="true" />
        <el-table-column label="金额" prop="amount" width="80">
          <template slot-scope="s">+{{ s.row.amount }}</template>
        </el-table-column>
        <el-table-column label="操作前" prop="orginPoints" width="80" />
        <el-table-column label="操作后" prop="afterPoints" width="80" />
        <el-table-column label="描述" prop="description" min-width="120" :show-overflow-tooltip="true" />
        <el-table-column label="操作设备" prop="operationDevice" width="80" />
        <el-table-column label="时间" prop="createdAt" width="160" />
      </el-table>
      <div slot="footer">
        <el-button @click="pointsVisible=false">关闭</el-button>
      </div>
    </el-dialog>

    <el-dialog title="添加积分" :visible.sync="addPointsVisible" width="400px" append-to-body>
      <el-form ref="addPointsForm" :model="addPointsForm" :rules="addPointsRules" label-width="80px">
        <el-form-item label="积分数额" prop="points">
          <el-input-number v-model="addPointsForm.points" :min="1" :max="99999" placeholder="请输入" style="width:100%" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="addPointsForm.description" type="textarea" placeholder="请输入" maxlength="255" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button type="primary" @click="submitAddPoints" :loading="addingPoints">确定</el-button>
        <el-button @click="addPointsVisible=false">取消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="会员卡情况" :visible.sync="cardVisible" width="1050px" append-to-body>
      <div v-if="cardMember.id" class="card-member-line">
        会员：<b>{{ cardMember.name || '-' }}</b>（{{ cardMember.cardNo }}）      </div>
      <el-alert
        v-if="activeCard"
        type="success"
        :closable="false"
        class="card-active-alert"
        :title="'当前生效：' + activeCard.cardTypeName + '，到期时间：' + (parseTime(activeCard.expiredAt) || '-')"
      />
      <el-alert
        v-else
        type="warning"
        :closable="false"
        class="card-active-alert"
        title="当前没有生效中的会员卡"
      />
      <el-table v-loading="cardLoading" :data="memberCards" size="small" max-height="420">
        <el-table-column label="状态" width="90" align="center">
          <template slot-scope="scope">
            <el-tag size="mini" :type="cardStatusTag(scope.row.status)">{{ cardStatusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="卡类型" prop="cardTypeName" min-width="110" />
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
        <el-table-column label="退款单号" prop="refundOrderNo" width="170" :show-overflow-tooltip="true">
          <template slot-scope="scope">{{ scope.row.refundOrderNo || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="80" align="center">
          <template slot-scope="scope">
            <el-button
              v-if="canRefundCard(scope.row)"
              size="mini"
              type="text"
              icon="el-icon-refresh-left"
              @click="openRefundFromCard(scope.row)"
              v-hasPermi="['member:card:refund']"
            >退卡</el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>
      <div slot="footer">
        <el-button @click="cardVisible=false">关闭</el-button>
      </div>
    </el-dialog>

    <el-dialog title="会员卡退卡" :visible.sync="refundVisible" width="900px" append-to-body>
      <div v-if="refundMember.id" class="refund-member-line">
        会员：<b>{{ refundMember.name || '-' }}</b>（{{ refundMember.cardNo }}）      </div>
      <el-table v-loading="refundLoading" :data="refundCards" size="small" max-height="260" @row-click="selectRefundCard">
        <el-table-column label="选择" width="60" align="center">
          <template slot-scope="scope">
            <el-radio v-model="refundForm.memberCardId" :label="scope.row.id" :disabled="!canRefundCard(scope.row)">&nbsp;</el-radio>
          </template>
        </el-table-column>
        <el-table-column label="卡类型" prop="cardTypeName" min-width="110" />
        <el-table-column label="状态" width="90" align="center">
          <template slot-scope="scope">
            <el-tag size="mini" :type="cardStatusTag(scope.row.status)">{{ cardStatusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="付款时间" width="160">
          <template slot-scope="scope">{{ parseTime(scope.row.paidAt) }}</template>
        </el-table-column>
        <el-table-column label="生效时间" width="160">
          <template slot-scope="scope">{{ parseTime(scope.row.effectiveAt) || '-' }}</template>
        </el-table-column>
        <el-table-column label="到期时间" width="160">
          <template slot-scope="scope">{{ parseTime(scope.row.expiredAt) || '-' }}</template>
        </el-table-column>
        <el-table-column label="实收金额" prop="saleAmount" width="100" />
        <el-table-column label="可退" width="90" align="center">
          <template slot-scope="scope">
            <el-tag size="mini" :type="canRefundCard(scope.row) ? 'success' : 'info'">{{ canRefundCard(scope.row) ? '可退' : '不可退' }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-form ref="refundForm" :model="refundForm" :rules="refundRules" label-width="90px" style="margin-top:16px">
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
        <el-button type="danger" :loading="refunding" :disabled="!selectedRefundCard" @click="submitRefundCard">确认退卡</el-button>
        <el-button @click="refundVisible=false">取消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="删除确认" :visible.sync="deleteVisible" width="400px" append-to-body>
      <p style="text-align:center;font-size:16px;padding:20px">删除不可恢复，是否删除？</p>
      <div slot="footer">
        <el-button type="danger" @click="confirmDelete">确认删除</el-button>
        <el-button @click="deleteVisible=false">取消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="导入ERP会员" :visible.sync="importVisible" width="520px" :close-on-click-modal="false" append-to-body>
      <el-form ref="importForm" :model="importForm" :rules="importRules" label-width="90px">
        <el-form-item label="导入门店" prop="deptId">
          <el-select v-model="importForm.deptId" placeholder="请选择导入门店" filterable style="width:100%">
            <el-option v-for="d in deptOptions" :key="d.deptId" :label="d.deptName" :value="d.deptId" />
          </el-select>
        </el-form-item>
        <el-form-item label="Excel文件" prop="file">
          <el-upload
            ref="memberImportUpload"
            drag
            action="#"
            :auto-upload="false"
            :limit="1"
            accept=".xls,.xlsx"
            :on-change="handleImportFileChange"
            :on-remove="handleImportFileRemove"
            :on-exceed="handleImportFileExceed"
          >
            <i class="el-icon-upload"></i>
            <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
            <div class="el-upload__tip" slot="tip">仅支持 xls、xlsx；会员卡号作为ERP唯一标识。</div>
          </el-upload>
        </el-form-item>
      </el-form>
      <el-progress
        v-if="importing || importProgress > 0"
        :percentage="importProgress"
        :status="importProgress === 100 && !importing ? 'success' : undefined"
        style="margin: 10px 0 0 90px"
      />
      <div slot="footer">
        <el-button type="primary" :loading="importing" @click="submitImport">开始导入</el-button>
        <el-button @click="importVisible=false">取消</el-button>
      </div>
    </el-dialog>

    <el-dialog title="导入结果" :visible.sync="importResultVisible" width="1180px" top="5vh" append-to-body>
      <div v-if="lastImportResult" class="import-result">
        <div class="import-summary">
          <span>导入批次：{{ lastImportResult.logId || '-' }}</span>
          <span>总数：{{ lastImportResult.totalRecords || 0 }}</span>
          <span class="success">成功：{{ lastImportResult.successRecords || 0 }}</span>
          <span class="info">未导入：{{ lastImportResult.skippedRecords || importSkipped.length }}</span>
          <span class="danger">失败：{{ lastImportResult.failRecords || 0 }}</span>
          <span class="warning">提醒：{{ importWarnings.length }}</span>
        </div>
        <el-tabs v-model="importResultTab">
          <el-tab-pane :label="'未导入明细(' + importSkipped.length + ')'" name="skipped">
            <el-table :data="importSkipped" border size="small" max-height="520">
              <el-table-column label="行号" prop="rowIndex" width="80" align="center" />
              <el-table-column label="会员卡号" prop="cardNo" width="150" />
              <el-table-column label="姓名" prop="name" width="120" />
              <el-table-column label="手机" prop="phone" width="140" />
              <el-table-column label="卡类型" prop="cardTypeName" width="130" />
              <el-table-column label="原因" prop="reason" min-width="420" show-overflow-tooltip />
            </el-table>
          </el-tab-pane>
          <el-tab-pane :label="'失败明细(' + importFailures.length + ')'" name="failures">
            <el-table :data="importFailures" border size="small" max-height="520">
              <el-table-column label="行号" prop="rowIndex" width="80" align="center" />
              <el-table-column label="会员卡号" prop="cardNo" width="150" />
              <el-table-column label="姓名" prop="name" width="120" />
              <el-table-column label="手机" prop="phone" width="140" />
              <el-table-column label="卡类型" prop="cardTypeName" width="130" />
              <el-table-column label="失败原因" prop="reason" min-width="420" show-overflow-tooltip />
            </el-table>
          </el-tab-pane>
          <el-tab-pane :label="'提醒明细(' + importWarnings.length + ')'" name="warnings">
            <el-table :data="importWarnings" border size="small" max-height="520">
              <el-table-column label="行号" prop="rowIndex" width="80" align="center" />
              <el-table-column label="会员卡号" prop="cardNo" width="150" />
              <el-table-column label="姓名" prop="name" width="120" />
              <el-table-column label="手机" prop="phone" width="140" />
              <el-table-column label="卡类型" prop="cardTypeName" width="130" />
              <el-table-column label="提醒原因" prop="reason" min-width="420" show-overflow-tooltip />
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </div>
      <div slot="footer">
        <el-button @click="importResultVisible=false">关闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listMember, getMember, addMember, updateMember, delMember, listCardTypes, listPoints, addPoints, getMemberCards, refundMemberCard, importMember } from '@/api/member/member'
import { listDept } from '@/api/system/dept'

export default {
  name: 'MemberList',
  data() {
    return {
      loading: false,
      submitting: false,
      total: 0,
      memberList: [],
      dateRange: [],
      queryParams: { pageNum: 1, pageSize: 10, name: null, cardNo: null, phone: null, deptId: null, status: null },
      deptOptions: [],
      cardTypes: [],
      dialogVisible: false,
      dialogTitle: '',
      isEdit: false,
      form: { id: null, name: '', phone: '', cardTypeId: null, validDate: '', validDateTips: '', remark: '', deptId: null },
      rules: {
        name: [{ required: true, message: '姓名不能为空', trigger: 'blur' }],
        phone: [
          { required: true, message: '请填写', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请填写', trigger: 'blur' }
        ],
        cardTypeId: [{ required: true, message: '请选择会员类型', trigger: 'change' }]
      },
      detailVisible: false,
      detail: {},
      pointsVisible: false,
      pointsMember: {},
      pointsList: [],
      addPointsVisible: false,
      addingPoints: false,
      addPointsForm: { points: null, description: '' },
      addPointsRules: {
        points: [{ required: true, message: '请填写', trigger: 'blur' }],
        description: [{ required: true, message: '请填写', trigger: 'blur' }]
      },
      cardVisible: false,
      cardLoading: false,
      cardMember: {},
      memberCards: [],
      activeCard: null,
      refundVisible: false,
      refundLoading: false,
      refunding: false,
      refundMember: {},
      refundCards: [],
      selectedRefundCard: null,
      refundForm: { memberCardId: null, refundAmount: null, refundType: 'CASH', reason: '' },
      refundRules: {
        memberCardId: [{ required: true, message: '请选择要退的会员卡', trigger: 'change' }],
        refundAmount: [{ required: true, message: '请填写', trigger: 'blur' }],
        refundType: [{ required: true, message: '请填写', trigger: 'change' }],
        reason: [{ required: true, message: '请填写', trigger: 'blur' }]
      },
      importVisible: false,
      importing: false,
      importProgress: 0,
      importResultVisible: false,
      importResultTab: 'failures',
      lastImportResult: null,
      importForm: { deptId: null, file: null },
      importRules: {
        deptId: [{ required: true, message: '请选择导入门店', trigger: 'change' }],
        file: [{ required: true, message: '请选择Excel文件', trigger: 'change' }]
      },
      deleteVisible: false,
      deleteId: null
    }
  },
  computed: {
    importFailures() {
      return (this.lastImportResult && this.lastImportResult.failureRows) || []
    },
    importSkipped() {
      return (this.lastImportResult && this.lastImportResult.skippedRows) || []
    },
    importWarnings() {
      return (this.lastImportResult && this.lastImportResult.warningRows) || []
    }
  },
  created() {
    this.getList()
    this.getDeptOptions()
    listCardTypes().then(r => { this.cardTypes = r.data || [] })
  },
  methods: {
    getList() {
      this.loading = true
      this.queryParams.params = this.dateRange && this.dateRange.length === 2
        ? { beginTime: this.dateRange[0], endTime: this.dateRange[1] }
        : {}
      listMember(this.queryParams).then(response => {
        this.memberList = response.rows || []
        this.total = response.total || 0
        this.loading = false
      }).catch(() => { this.loading = false })
    },
    tableIndex(index) {
      return (this.queryParams.pageNum - 1) * this.queryParams.pageSize + index + 1
    },
    getDeptOptions() {
      listDept().then(r => { this.deptOptions = r.data || [] })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.dateRange = []
      this.queryParams = { pageNum: 1, pageSize: 10, name: null, cardNo: null, phone: null, deptId: null, status: null }
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
      const d = new Date(today.getFullYear(), today.getMonth(), today.getDate() + ct.validDays)
      this.form.validDate = this.formatDate(d)
      this.form.validDateTips = '(' + ct.typeName + ', ' + ct.validDays + '天后到期)'
    },
    handleAdd() {
      this.dialogTitle = '新增会员'
      this.isEdit = false
      this.form = { id: null, name: '', phone: '', cardTypeId: null, validDate: '', validDateTips: '', remark: '', deptId: null }
      this.dialogVisible = true
      this.$nextTick(() => { if (this.$refs.form) this.$refs.form.clearValidate() })
    },
    handleEdit(row) {
      this.dialogTitle = '编辑会员'
      this.isEdit = true
      getMember(row.id).then(r => {
        const m = r.member
        const ct = this.cardTypes.find(c => c.id === m.cardTypeId)
        const tips = ct && m.validDate ? '(' + ct.typeName + ')' : ''
        this.form = { id: m.id, name: m.name, phone: m.phone, cardTypeId: m.cardTypeId, validDate: m.validDate || '', validDateTips: tips, remark: m.remark, deptId: m.deptId }
        this.dialogVisible = true
        this.$nextTick(() => { if (this.$refs.form) this.$refs.form.clearValidate() })
      })
    },
    handleView(row) {
      getMember(row.id).then(r => {
        this.detail = r
        this.detailVisible = true
      })
    },
    detailToEdit() {
      this.detailVisible = false
      this.handleEdit({ id: this.detail.member.id })
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) return
        this.submitting = true
        const payload = { ...this.form }
        if (this.isEdit) {
          delete payload.cardTypeId
          delete payload.validDate
          delete payload.validDateTips
        }
        const api = this.isEdit ? updateMember(this.form.id, payload) : addMember(payload)
        api.then(r => {
          if (r.code === 200) {
            this.$modal.msgSuccess(r.msg)
            this.dialogVisible = false
            this.getList()
          } else {
            this.$modal.msgError(r.msg)
          }
          this.submitting = false
        }).catch(() => { this.submitting = false })
      })
    },
    handleCommand(command, row) {
      if (command === 'handleDelete') this.handleDelete(row)
    },
    handleRefundCard(row) {
      this.refundMember = row
      this.refundCards = []
      this.selectedRefundCard = null
      this.refundForm = { memberCardId: null, refundAmount: null, refundType: 'CASH', reason: '' }
      this.refundVisible = true
      this.refundLoading = true
      getMemberCards(row.id).then(r => {
        const data = r.data || {}
        this.refundCards = data.cards || []
        const first = this.refundCards.find(card => this.canRefundCard(card))
        if (first) this.selectRefundCard(first)
        this.refundLoading = false
      }).catch(() => { this.refundLoading = false })
      this.$nextTick(() => { if (this.$refs.refundForm) this.$refs.refundForm.clearValidate() })
    },
    handleMemberCards(row) {
      this.cardMember = row
      this.memberCards = []
      this.activeCard = null
      this.cardVisible = true
      this.cardLoading = true
      getMemberCards(row.id).then(r => {
        const data = r.data || {}
        this.memberCards = data.cards || []
        this.activeCard = data.activeCard || this.memberCards.find(card => card.status === 1) || null
        this.cardLoading = false
      }).catch(() => { this.cardLoading = false })
    },
    openRefundFromCard(card) {
      this.refundMember = this.cardMember
      this.refundCards = this.memberCards
      this.refundForm = { memberCardId: null, refundAmount: null, refundType: 'CASH', reason: '' }
      this.refundVisible = true
      this.selectRefundCard(card)
      this.$nextTick(() => { if (this.$refs.refundForm) this.$refs.refundForm.clearValidate() })
    },
    selectRefundCard(card) {
      if (!this.canRefundCard(card)) return
      this.selectedRefundCard = card
      this.refundForm.memberCardId = card.id
      this.refundForm.refundAmount = Number(card.saleAmount || 0)
    },
    submitRefundCard() {
      this.$refs.refundForm.validate(valid => {
        if (!valid || !this.selectedRefundCard) return
        this.$confirm('确认退还该会员卡并生成退款单吗？', '退卡确认', { type: 'warning' }).then(() => {
          this.refunding = true
          refundMemberCard(this.refundForm.memberCardId, {
            refundAmount: this.refundForm.refundAmount,
            refundType: this.refundForm.refundType,
            reason: this.refundForm.reason
          }).then(r => {
            if (r.code === 200) {
              this.$modal.msgSuccess('退卡成功')
              this.refundVisible = false
              if (this.cardVisible && this.cardMember.id) {
                this.handleMemberCards(this.cardMember)
              }
              this.getList()
            } else {
              this.$modal.msgError(r.msg)
            }
            this.refunding = false
          }).catch(() => { this.refunding = false })
        })
      })
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
    handleDelete(row) {
      this.deleteId = row.id
      this.deleteVisible = true
    },
    confirmDelete() {
      delMember(this.deleteId).then(r => {
        if (r.code === 200) {
          this.$modal.msgSuccess('删除成功')
          this.deleteVisible = false
          this.getList()
        } else {
          this.$modal.msgError(r.msg)
        }
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
    handleImport() {
      this.importForm = { deptId: this.queryParams.deptId || null, file: null }
      this.importProgress = 0
      this.importVisible = true
      this.$nextTick(() => {
        if (this.$refs.importForm) this.$refs.importForm.clearValidate()
        if (this.$refs.memberImportUpload) this.$refs.memberImportUpload.clearFiles()
      })
    },
    handleImportFileChange(file, fileList) {
      const name = (file.name || '').toLowerCase()
      if (!name.endsWith('.xls') && !name.endsWith('.xlsx')) {
        this.$modal.msgError('请选择 xls 或 xlsx 格式的Excel文件')
        fileList.splice(fileList.indexOf(file), 1)
        this.importForm.file = null
        return
      }
      this.importForm.file = file.raw
      this.$nextTick(() => {
        if (this.$refs.importForm) this.$refs.importForm.validateField('file')
      })
    },
    handleImportFileRemove() {
      this.importForm.file = null
    },
    handleImportFileExceed() {
      this.$modal.msgError('一次只能上传一个Excel文件')
    },
    submitImport() {
      this.$refs.importForm.validate(valid => {
        if (!valid) return
        const formData = new FormData()
        formData.append('file', this.importForm.file)
        formData.append('deptId', this.importForm.deptId)
        this.importing = true
        this.importProgress = 0
        importMember(formData, {
          onUploadProgress: event => {
            if (!event.total) {
              this.importProgress = Math.max(this.importProgress, 10)
              return
            }
            const percent = Math.round((event.loaded * 90) / event.total)
            this.importProgress = Math.min(95, Math.max(this.importProgress, percent))
          }
        }).then(res => {
          const data = res.data || {}
          this.importProgress = 100
          this.lastImportResult = data
          this.importResultTab = (data.failureRows || []).length > 0 ? 'failures' : ((data.skippedRows || []).length > 0 ? 'skipped' : 'warnings')
          this.importVisible = false
          this.importResultVisible = true
          this.getList()
        }).finally(() => {
          this.importing = false
        })
      })
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
    exportDeptName() {
      const dept = this.deptOptions.find(d => String(d.deptId) === String(this.queryParams.deptId))
      const baseName = dept && dept.deptName ? `${dept.deptName}会员` : '全部会员'
      return baseName.replace(/[\\/:*?"<>|\s]+/g, '_')
    },
    handleExport() {
      const params = {
        ...this.queryParams,
        params: this.dateRange && this.dateRange.length === 2
          ? { beginTime: this.dateRange[0], endTime: this.dateRange[1] }
          : {}
      }
      this.download('member/export', params, `${this.exportDeptName()}_${this.formatExportTimestamp(new Date())}.xlsx`)
    }
  }
}
</script>

<style scoped>
.refund-member-line {
  margin-bottom: 12px;
  color: #606266;
}
.card-member-line {
  margin-bottom: 10px;
  color: #606266;
}
.card-active-alert {
  margin-bottom: 12px;
}
.import-result {
  min-height: 420px;
}
.import-summary {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 12px;
  color: #303133;
}
.import-summary .success {
  color: #67c23a;
}
.import-summary .info {
  color: #409eff;
}
.import-summary .danger {
  color: #f56c6c;
}
.import-summary .warning {
  color: #e6a23c;
}
</style>
