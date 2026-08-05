<template>
  <div class="comment-list">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable>
            <el-option label="待审核" :value="0" />
            <el-option label="通过" :value="1" />
            <el-option label="拒绝" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <div class="action-bar" v-if="selectedRows.length">
        <el-button type="success" @click="handleBatchApprove">批量通过</el-button>
        <el-button type="danger" @click="handleBatchDelete">批量删除</el-button>
      </div>
      
      <el-table :data="tableData" v-loading="loading" @selection-change="handleSelectionChange" border>
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="articleTitle" label="文章" min-width="200" show-overflow-tooltip />
        <el-table-column prop="nickname" label="评论人" width="120" />
        <el-table-column prop="content" label="评论内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusMap[row.status].type" size="small">{{ statusMap[row.status].label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="评论时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status !== 1" link type="success" @click="handleApprove(row)">通过</el-button>
            <el-button v-if="row.status !== 2" link type="warning" @click="handleReject(row)">拒绝</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next, jumper"
          @current-change="fetchData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import commentApi from '@/api/comment'

const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const selectedRows = ref([])

const statusMap = {
  0: { label: '待审核', type: 'warning' },
  1: { label: '通过', type: 'success' },
  2: { label: '拒绝', type: 'danger' }
}

const searchForm = reactive({ status: null })

const fetchData = async () => {
  loading.value = true
  try {
    const res = await commentApi.getPage({
      current: currentPage.value,
      size: pageSize.value,
      ...searchForm
    })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { currentPage.value = 1; fetchData() }
const handleSelectionChange = (rows) => { selectedRows.value = rows }

const handleApprove = async (row) => {
  await commentApi.approve(row.id)
  ElMessage.success('已通过')
  fetchData()
}

const handleReject = async (row) => {
  await commentApi.reject(row.id)
  ElMessage.success('已拒绝')
  fetchData()
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该评论吗？', '提示', { type: 'warning' })
    .then(async () => {
      await commentApi.delete(row.id)
      ElMessage.success('删除成功')
      fetchData()
    }).catch(() => {})
}

const handleBatchApprove = async () => {
  const ids = selectedRows.value.map(row => row.id)
  await commentApi.batchApprove(ids)
  ElMessage.success('批量审核成功')
  fetchData()
}

const handleBatchDelete = () => {
  ElMessageBox.confirm(`确定要删除选中的 ${selectedRows.value.length} 条评论吗？`, '提示', { type: 'warning' })
    .then(async () => {
      const ids = selectedRows.value.map(row => row.id)
      await commentApi.batchDelete(ids)
      ElMessage.success('批量删除成功')
      fetchData()
    }).catch(() => {})
}

onMounted(() => fetchData())
</script>

<style scoped>
.search-card { margin-bottom: 16px; }
.action-bar { margin-bottom: 16px; }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>