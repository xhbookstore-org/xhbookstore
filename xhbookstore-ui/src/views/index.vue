<template>
  <div class="dashboard-page">
    <div class="dashboard-header">
      <div>
        <div class="dashboard-title">会员数据看板</div>
        <div class="dashboard-subtitle">
          {{ overview.scopeName || '当前数据权限' }}
          <span v-if="overview.refreshedAt"> · 更新于 {{ overview.refreshedAt }}</span>
        </div>
      </div>
      <el-button type="primary" icon="el-icon-refresh" size="small" :loading="refreshing" @click="handleRefresh" v-hasPermi="['dashboard:member:refresh']">
        刷新统计
      </el-button>
    </div>

    <el-alert
      v-if="overview.missingDeptCount"
      class="dashboard-alert"
      type="warning"
      :closable="false"
      :title="`有 ${overview.missingDeptCount} 个可见部门暂无缓存，已按现有缓存展示`"
    />

    <div class="stat-grid">
      <div v-for="item in memberCards" :key="item.label" class="stat-card">
        <div class="stat-label">{{ item.label }}</div>
        <div class="stat-value">{{ formatNumber(item.value) }}</div>
        <div class="stat-note">{{ item.note }}</div>
      </div>
    </div>

    <el-row :gutter="16" class="section-row">
      <el-col :xs="24" :lg="12">
        <div class="panel">
          <div class="panel-title">借阅卡会员结构</div>
          <el-table :data="cardTypeRows" size="small" border>
            <el-table-column prop="name" label="类型" min-width="110" />
            <el-table-column prop="total" label="合计" align="right" width="110">
              <template slot-scope="scope">{{ formatNumber(scope.row.total) }}</template>
            </el-table-column>
            <el-table-column prop="unit" label="单位会员" align="right" width="120">
              <template slot-scope="scope">{{ formatNumber(scope.row.unit) }}</template>
            </el-table-column>
            <el-table-column prop="natural" label="自然会员" align="right" width="120">
              <template slot-scope="scope">{{ formatNumber(scope.row.natural) }}</template>
            </el-table-column>
          </el-table>
        </div>
      </el-col>
      <el-col :xs="24" :lg="12">
        <div class="panel">
          <div class="panel-title">小程序使用数据</div>
          <div class="usage-grid">
            <div v-for="item in usageCards" :key="item.label" class="usage-item">
              <div class="usage-label">{{ item.label }}</div>
              <div class="usage-value">{{ formatNumber(item.value) }}</div>
            </div>
          </div>
          <div class="usage-note">{{ overview.loginStatsScopeNote }}</div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { getMemberDashboardOverview, refreshMemberDashboard } from '@/api/dashboard'

export default {
  name: 'Index',
  data() {
    return {
      loading: false,
      refreshing: false,
      overview: {
        stats: {},
        loginStats: {}
      }
    }
  },
  computed: {
    stats() {
      return this.overview.stats || {}
    },
    loginStats() {
      return this.overview.loginStats || {}
    },
    memberCards() {
      return [
        { label: '累计会员', value: this.stats.totalMembers, note: '借阅卡会员 + 普通会员' },
        { label: '借阅卡会员', value: this.stats.borrowCardMembers, note: '当前有效借阅卡' },
        { label: '普通会员', value: this.stats.normalMembers, note: '未持有效借阅卡' },
        { label: '单位借阅卡会员', value: this.stats.unitBorrowCardMembers, note: '会员备注不为空' },
        { label: '自然借阅卡会员', value: this.stats.naturalBorrowCardMembers, note: '会员备注为空' },
        { label: '可见部门', value: this.overview.visibleDeptCount, note: '按登录人数据权限汇总' }
      ]
    },
    cardTypeRows() {
      return [
        {
          name: '尊享会员',
          total: this.stats.yearCardMembers,
          unit: this.stats.unitYearCardMembers,
          natural: this.stats.naturalYearCardMembers
        },
        {
          name: '畅享会员',
          total: this.stats.halfYearCardMembers,
          unit: this.stats.unitHalfYearCardMembers,
          natural: this.stats.naturalHalfYearCardMembers
        }
      ]
    },
    usageCards() {
      return [
        { label: '用户登录总量', value: this.loginStats.totalLoginCount },
        { label: '会员码展示总量', value: this.stats.totalMemberCodeShowCount },
        { label: '年用户登录量', value: this.loginStats.yearLoginCount },
        { label: '年会员码展示量', value: this.stats.yearMemberCodeShowCount },
        { label: '月用户登录量', value: this.loginStats.monthLoginCount },
        { label: '月会员码展示量', value: this.stats.monthMemberCodeShowCount },
        { label: '昨日用户登录量', value: this.loginStats.yesterdayLoginCount },
        { label: '昨日会员码展示量', value: this.stats.yesterdayMemberCodeShowCount }
      ]
    }
  },
  created() {
    this.getOverview()
  },
  methods: {
    getOverview() {
      this.loading = true
      getMemberDashboardOverview().then(res => {
        this.overview = res.data || { stats: {}, loginStats: {} }
      }).finally(() => {
        this.loading = false
      })
    },
    handleRefresh() {
      this.refreshing = true
      refreshMemberDashboard().then(() => {
        this.$message.success('统计已刷新')
        this.getOverview()
      }).finally(() => {
        this.refreshing = false
      })
    },
    formatNumber(value) {
      const number = Number(value || 0)
      return number.toLocaleString()
    }
  }
}
</script>

<style lang="scss" scoped>
.dashboard-page {
  min-height: calc(100vh - 84px);
  padding: 20px;
  background: #f5f7fb;
}

.dashboard-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.dashboard-title {
  color: #1f2937;
  font-size: 24px;
  font-weight: 700;
  line-height: 32px;
}

.dashboard-subtitle {
  margin-top: 4px;
  color: #6b7280;
  font-size: 13px;
}

.dashboard-alert {
  margin-bottom: 16px;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 12px;
}

.stat-card,
.panel {
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: #fff;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
}

.stat-card {
  min-height: 118px;
  padding: 16px;
}

.stat-label,
.usage-label {
  color: #6b7280;
  font-size: 13px;
}

.stat-value {
  margin-top: 14px;
  color: #111827;
  font-size: 28px;
  font-weight: 700;
  line-height: 34px;
}

.stat-note {
  margin-top: 10px;
  color: #9ca3af;
  font-size: 12px;
}

.section-row {
  margin-top: 16px;
}

.panel {
  min-height: 302px;
  padding: 16px;
}

.panel-title {
  margin-bottom: 14px;
  color: #1f2937;
  font-size: 16px;
  font-weight: 700;
}

.usage-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.usage-item {
  min-height: 70px;
  padding: 12px;
  border: 1px solid #edf0f5;
  border-radius: 6px;
  background: #fafbfc;
}

.usage-value {
  margin-top: 8px;
  color: #111827;
  font-size: 22px;
  font-weight: 700;
}

.usage-note {
  margin-top: 14px;
  color: #9ca3af;
  font-size: 12px;
  line-height: 20px;
}

@media (max-width: 1200px) {
  .stat-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .dashboard-page {
    padding: 12px;
  }

  .dashboard-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .stat-grid,
  .usage-grid {
    grid-template-columns: 1fr;
  }
}
</style>
