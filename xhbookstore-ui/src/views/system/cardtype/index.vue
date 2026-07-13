<template>
  <div class="app-container">
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd"
          v-hasPermi="['member:cardType:add']">新增</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="list" border>
      <el-table-column label="ID" prop="id" width="70" align="center"/>
      <el-table-column label="名称" prop="typeName" width="120"/>
      <el-table-column label="售价" prop="price" width="80" align="center">
        <template slot-scope="scope">{{ scope.row.price ? '¥' + scope.row.price : '免费' }}</template>
      </el-table-column>
      <el-table-column label="有效期(天)" prop="validDays" width="100" align="center"/>
      <el-table-column label="借阅上限" prop="borrowLimit" width="90" align="center"/>
      <el-table-column label="折扣" prop="discount" width="70" align="center">
        <template slot-scope="scope">{{ scope.row.discount && scope.row.discount < 1 ? (scope.row.discount * 10).toFixed(0) + '折' : '-' }}</template>
      </el-table-column>
      <el-table-column label="续费" prop="isRenewal" width="60" align="center">
        <template slot-scope="scope">{{ scope.row.isRenewal == 1 ? '是' : '否' }}</template>
      </el-table-column>
      <el-table-column label="排序" prop="sort" width="60" align="center"/>
      <el-table-column label="状态" width="70" align="center">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.sys_normal_disable" :value="String(scope.row.status)"/>
        </template>
      </el-table-column>
      <el-table-column label="说明" prop="description" :show-overflow-tooltip="true" min-width="150"/>
      <el-table-column label="操作" align="center" width="200" fixed="right">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)"
            v-hasPermi="['member:cardType:edit']">修改</el-button>
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleLog(scope.row)" v-hasPermi="['member:cardType:query']">日志</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)"
            v-hasPermi="['member:cardType:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="title" :visible.sync="open" width="560px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="卡类型名称" prop="typeName">
          <el-input v-model="form.typeName" placeholder="如：年卡、半年卡"/>
        </el-form-item>
        <el-form-item label="售价(元)" prop="price">
          <el-input-number v-model="form.price" :precision="2" :min="0" placeholder="0.00" style="width:200px"/>
        </el-form-item>
        <el-form-item label="有效天数" prop="validDays">
          <el-input-number v-model="form.validDays" :min="0" placeholder="365" style="width:200px"/>
        </el-form-item>
        <el-form-item label="借阅上限" prop="borrowLimit">
          <el-input-number v-model="form.borrowLimit" :min="0" placeholder="10" style="width:200px"/>
        </el-form-item>
        <el-form-item label="购书折扣" prop="discount">
          <el-input-number v-model="form.discount" :precision="2" :min="0" :max="1" :step="0.05" placeholder="0.85" style="width:200px"/>
        </el-form-item>
        <el-form-item label="续费卡" prop="isRenewal">
          <el-switch v-model="form.isRenewal" :active-value="1" :inactive-value="0"/>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" style="width:200px"/>
        </el-form-item>
        <el-form-item label="说明" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="卡类型说明"/>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="0">正常</el-radio>
            <el-radio :label="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 操作日志弹窗 -->
    <el-dialog title="操作日志" :visible.sync="logOpen" width="900px" append-to-body>
      <el-table :data="logList" border max-height="400">
        <el-table-column label="操作" width="70" align="center">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.operation_type=='CREATE'" type="success">新增</el-tag>
            <el-tag v-else-if="scope.row.operation_type=='UPDATE'" type="warning">修改</el-tag>
            <el-tag v-else-if="scope.row.operation_type=='DELETE'" type="danger">删除</el-tag>
            <span v-else>{{ scope.row.operation_type }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作人" prop="operator" width="90"/>
        <el-table-column label="变更内容" prop="remark" :show-overflow-tooltip="true" min-width="280"/>
        <el-table-column label="操作时间" prop="created_at" width="160"/>
      </el-table>
    </el-dialog>
  </div>
</template>

<script>
import { listCardType, getCardType, addCardType, updateCardType, delCardType, listCardTypeLog } from "@/api/system/cardtype";

export default {
  name: "CardType",
  dicts: ['sys_normal_disable'],
  data() {
    return {
      loading: false,
      showSearch: false,
      list: [],
      logList: [],
      open: false,
      logOpen: false,
      title: "",
      form: {},
      rules: {
        typeName: [{ required: true, message: "卡类型名称不能为空", trigger: "blur" }],
        validDays: [{ required: true, message: "有效天数不能为空", trigger: "blur" }]
      }
    };
  },
  created() { this.getList(); },
  methods: {
    getList() {
      this.loading = true;
      listCardType().then(res => {
        this.list = (res.rows || []).slice().sort((a, b) => Number(b.id) - Number(a.id));
        this.loading = false;
      });
    },
    reset() { this.form = { isRenewal: 0, status: 0, price: 0, validDays: 0, borrowLimit: 0, discount: 1.00, sort: 0 }; },
    handleAdd() { this.reset(); this.open = true; this.title = "新增卡类型"; },
    handleUpdate(row) {
      getCardType(row.id).then(res => {
        const ct = res.data || res;
        this.form = {
          id: ct.id, typeName: ct.typeName, price: ct.price, validDays: ct.validDays,
          borrowLimit: ct.borrowLimit, discount: ct.discount, isRenewal: ct.isRenewal,
          description: ct.description, sort: ct.sort,
          status: ct.status != null ? Number(ct.status) : 0
        };
        this.open = true;
        this.title = "修改卡类型";
      }).catch(() => { this.$modal.msgError("加载卡类型失败"); });
    },
    submitForm() {
      this.$refs.form.validate(valid => {
        if (!valid) return;
        if (this.form.id) {
          updateCardType(this.form.id, this.form).then(() => { this.$modal.msgSuccess("修改成功"); this.open = false; this.getList(); });
        } else {
          addCardType(this.form).then(() => { this.$modal.msgSuccess("新增成功"); this.open = false; this.getList(); });
        }
      });
    },
    handleDelete(row) {
      this.$modal.confirm('确认删除卡类型「' + row.typeName + '」？').then(() => {
        delCardType(row.id).then(() => { this.$modal.msgSuccess("删除成功"); this.getList(); });
      });
    },
    handleLog(row) {
      listCardTypeLog(row.id).then(res => { this.logList = res.data; this.logOpen = true; });
    },
    cancel() { this.open = false; }
  }
};
</script>
