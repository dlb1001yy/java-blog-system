<template>
  <div class="article-list">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="标题">
          <el-input v-model="searchForm.title" placeholder="请输入文章标题" clearable />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="searchForm.type" placeholder="请选择" clearable>
            <el-option label="原创" :value="0" />
            <el-option label="转载" :value="1" />
            <el-option label="翻译" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.isPublish" placeholder="请选择" clearable>
            <el-option label="草稿" :value="0" />
            <el-option label="已发布" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作栏 -->
    <div class="action-bar">
      <el-button type="primary" :icon="Plus" @click="$router.push('/article/create')">
        写文章
      </el-button>
      <el-button type="danger" :icon="Delete" :disabled="!selectedRows.length" @click="handleBatchDelete">
        批量删除
      </el-button>
    </div>

    <!-- 表格 -->
    <el-card>
      <el-table
        :data="tableData"
        v-loading="loading"
        @selection-change="handleSelectionChange"
        border
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="120" />
        <el-table-column prop="type" label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="typeMap[row.type].type" size="small">{{ typeMap[row.type].label }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="viewCount" label="浏览量" width="90" />
        <el-table-column prop="likeCount" label="点赞数" width="90" />
        <el-table-column prop="isPublish" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.isPublish ? 'success' : 'info'" size="small">
              {{ row.isPublish ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isTop" label="置顶" width="80">
          <template #default="{ row }">
            <el-tag :type="row.isTop ? 'danger' : 'info'" size="small">
              {{ row.isTop ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link :type="row.isPublish ? 'warning' : 'success'" @click="handleTogglePublish(row)">
              {{ row.isPublish ? '下架' : '发布' }}
            </el-button>
            <el-button link :type="row.isTop ? 'info' : 'danger'" @click="handleToggleTop(row)">
              {{ row.isTop ? '取消置顶' : '置顶' }}
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import articleApi from '@/api/article'

const router = useRouter()

const loading = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const selectedRows = ref([])

const typeMap = {
  0: { label: '原创', type: 'primary' },
  1: { label: '转载', type: 'warning' },
  2: { label: '翻译', type: 'success' }
}

const searchForm = reactive({
  title: '',
  type: null,
  isPublish: null
})

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      current: currentPage.value,
      size: pageSize.value,
      ...searchForm
    }
    const res = await articleApi.getPage(params)
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchData()
}

const handleReset = () => {
  searchForm.title = ''
  searchForm.type = null
  searchForm.isPublish = null
  handleSearch()
}

const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

const handleEdit = (row) => {
  router.push(`/article/edit/${row.id}`)
}

const handleTogglePublish = async (row) => {
  await articleApi.togglePublish(row.id)
  ElMessage.success('操作成功')
  fetchData()
}

const handleToggleTop = async (row) => {
  await articleApi.toggleTop(row.id)
  ElMessage.success('操作成功')
  fetchData()
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该文章吗？', '提示', { type: 'warning' })
    .then(async () => {
      await articleApi.delete(row.id)
      ElMessage.success('删除成功')
      fetchData()
    }).catch(() => {})
}

const handleBatchDelete = () => {
  ElMessageBox.confirm(`确定要删除选中的 ${selectedRows.value.length} 篇文章吗？`, '提示', { type: 'warning' })
    .then(async () => {
      const ids = selectedRows.value.map(row => row.id)
      await articleApi.batchDelete(ids)
      ElMessage.success('批量删除成功')
      fetchData()
    }).catch(() => {})
}

const handleSizeChange = () => fetchData()
const handleCurrentChange = () => fetchData()

onMounted(() => fetchData())
</script>

<style scoped>
.search-card { margin-bottom: 16px; }
.action-bar { margin-bottom: 16px; }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>