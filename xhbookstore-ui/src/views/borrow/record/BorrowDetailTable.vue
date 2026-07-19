<template>
  <el-table :data="details" border size="mini" empty-text="暂无逐册明细">
    <el-table-column label="明细ID" prop="id" width="90" align="center" />
    <el-table-column label="图书编号" prop="bookCode" min-width="130" />
    <el-table-column label="图书名称" prop="bookName" min-width="180" show-overflow-tooltip />
    <el-table-column label="数量" prop="borrowQty" width="60" align="center" />
    <el-table-column label="状态" width="90" align="center">
      <template slot-scope="scope">
        <el-tag size="mini" :type="statusTag(scope.row.borrowStatus)">{{ statusText(scope.row.borrowStatus) }}</el-tag>
      </template>
    </el-table-column>
    <el-table-column label="借书时间" prop="borrowTime" width="155" />
    <el-table-column label="还书时间" width="155">
      <template slot-scope="scope">{{ scope.row.returnTime || '—' }}</template>
    </el-table-column>
    <el-table-column label="处理时间" width="155">
      <template slot-scope="scope">{{ scope.row.handleTime || '—' }}</template>
    </el-table-column>
    <el-table-column label="最后操作人" width="105">
      <template slot-scope="scope">{{ scope.row.lastStaffName || '—' }}</template>
    </el-table-column>
    <el-table-column label="图片" min-width="150">
      <template slot-scope="scope">
        <div v-if="scope.row.images && scope.row.images.length" class="image-list">
          <el-image
            v-for="image in scope.row.images"
            :key="image.imageId"
            class="book-thumb"
            :src="image.thumbUrl || image.imageUrl"
            :preview-src-list="previewList(scope.row.images)"
            :initial-index="imageIndex(scope.row.images, image)"
            fit="cover"
          />
        </div>
        <span v-else>—</span>
      </template>
    </el-table-column>
    <el-table-column label="备注" prop="remark" min-width="130">
      <template slot-scope="scope">{{ scope.row.remark || '—' }}</template>
    </el-table-column>
  </el-table>
</template>

<script>
export default {
  name: 'BorrowDetailTable',
  props: {
    details: {
      type: Array,
      default: () => []
    }
  },
  methods: {
    statusText(status) {
      return ({ 1: '借阅中', 2: '已归还', 5: '已转购', 6: '已遗失', 7: '已取消' })[status] || '未知'
    },
    statusTag(status) {
      return ({ 1: '', 2: 'success', 5: 'warning', 6: 'danger', 7: 'info' })[status] || 'info'
    },
    previewList(images) {
      return (images || []).map(item => item.imageUrl).filter(Boolean)
    },
    imageIndex(images, current) {
      const list = this.previewList(images)
      const index = list.indexOf(current.imageUrl)
      return index < 0 ? 0 : index
    }
  }
}
</script>

<style scoped>
.image-list { display: flex; gap: 6px; flex-wrap: wrap; }
.book-thumb { width: 52px; height: 52px; border-radius: 3px; cursor: pointer; }
</style>
