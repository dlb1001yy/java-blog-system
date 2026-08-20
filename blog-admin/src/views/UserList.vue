<template>
  <PageContainer title="用户管理" description="前台用户管理">
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-label">用户总数</div>
        <div class="stat-value">{{ stats.total }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">活跃用户（近30天）</div>
        <div class="stat-value">{{ stats.active }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">角色分布</div>
        <div class="stat-roles">
          <el-tag v-for="(count, role) in stats.roles" :key="role" size="small"
                  :type="role === 'admin' ? 'danger' : 'primary'">
            {{ roleMap[role] || role }}: {{ count }}
          </el-tag>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-label">本月新增</div>
        <div class="stat-value">{{ stats.monthNew }}</div>
      </div>
    </div>

    <div class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="用户名/昵称/邮箱" clearable style="width: 200px" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="searchForm.role" placeholder="请选择" clearable style="width: 140px">
            <el-option label="管理员" value="admin" />
            <el-option label="普通用户" value="user" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择" clearable style="width: 140px">
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-card">
      <el-table :data="tableData" v-loading="loading" :border="false" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column label="用户" min-width="200">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="32" :src="row.avatar" icon="UserFilled" />
              <div class="user-info">
                <span class="nickname">{{ row.nickname || row.username }}</span>
                <span class="username">{{ row.username }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column prop="role" label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="row.role === 'admin' ? 'danger' : 'primary'" size="small">
              {{ roleMap[row.role] || row.role }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="warning" @click="handleResetPassword(row)">重置密码</el-button>
            <el-button v-if="row.status === 1" link type="danger" @click="handleToggleEnable(row, false)">禁用</el-button>
            <el-button v-else link type="success" @click="handleToggleEnable(row, true)">启用</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next, jumper"
          @current-change="fetchData"
        />
      </div>
    </div>

    <el-dialog v-model="editVisible" title="编辑用户" width="480px">
      <el-form :model="editForm" label-width="90px">
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" />
        </el-form-item>
        <el-form-item label="头像URL">
          <el-input v-model="editForm.avatar" placeholder="https://" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="editForm.role" style="width: 100%">
            <el-option label="管理员" value="admin" />
            <el-option label="普通用户" value="user" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="editForm.status" style="width: 100%">
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="resetVisible" title="重置密码" width="440px">
      <el-form label-width="90px">
        <el-form-item label="新密码">
          <el-input v-model="resetPasswordValue" type="password" show-password placeholder="留空则自动生成随机密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleResetSubmit">确定</el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import userManageApi from '@/api/userManage'
import PageContainer from '@/components/PageContainer.vue'

const loading = ref(false)
const saving = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const stats = ref({ total: 0, active: 0, roles: {}, monthNew: 0 })

const roleMap = { admin: '管理员', user: '用户' }

const searchForm = reactive({ keyword: '', role: null, status: null })

const fetchStats = async () => {
  const res = await userManageApi.getStats()
  stats.value = res.data
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await userManageApi.getList({
      page: currentPage.value,
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

// 编辑
const editVisible = ref(false)
const editForm = reactive({ id: null, nickname: '', email: '', avatar: '', role: 'user', status: 1 })

const handleEdit = (row) => {
  editForm.id = row.id
  editForm.nickname = row.nickname
  editForm.email = row.email
  editForm.avatar = row.avatar
  editForm.role = row.role
  editForm.status = row.status
  editVisible.value = true
}

const handleSave = async () => {
  saving.value = true
  try {
    await userManageApi.update(editForm.id, {
      nickname: editForm.nickname,
      email: editForm.email,
      avatar: editForm.avatar,
      role: editForm.role,
      status: editForm.status
    })
    ElMessage.success('更新成功')
    editVisible.value = false
    fetchData()
    fetchStats()
  } finally {
    saving.value = false
  }
}

// 重置密码
const resetVisible = ref(false)
const resetPasswordValue = ref('')
const resetUserId = ref(null)

const handleResetPassword = (row) => {
  resetUserId.value = row.id
  resetPasswordValue.value = ''
  resetVisible.value = true
}

const handleResetSubmit = async () => {
  saving.value = true
  try {
    const res = await userManageApi.resetPassword(resetUserId.value, resetPasswordValue.value)
    resetVisible.value = false
    const newPassword = res.data
    ElMessageBox.alert(`新密码：${newPassword}`, '重置成功', {
      confirmButtonText: '复制',
      type: 'success'
    }).then(() => {
      navigator.clipboard?.writeText(newPassword)
      ElMessage.success('已复制到剪贴板')
    }).catch(() => {})
  } finally {
    saving.value = false
  }
}

// 启用/禁用
const handleToggleEnable = (row, enable) => {
  const action = enable ? '启用' : '禁用'
  ElMessageBox.confirm(`确定要${action}用户「${row.nickname || row.username}」吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await userManageApi.enable(row.id, enable)
      ElMessage.success(`已${action}`)
      fetchData()
      fetchStats()
    }).catch(() => {})
}

onMounted(() => { fetchData(); fetchStats() })
</script>

<style scoped>
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-4);
  margin-bottom: var(--space-4);
}

.stat-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-4) var(--space-5);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
}

.stat-label { font-size: 13px; color: var(--text-secondary, #909399); margin-bottom: var(--space-2); }
.stat-value { font-size: 24px; font-weight: 700; }
.stat-roles { display: flex; flex-wrap: wrap; gap: var(--space-2); align-items: center; min-height: 24px; }

.search-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-5);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
  margin-bottom: var(--space-4);
}
:deep(.el-form--inline .el-form-item) { margin-bottom: 0; }

.table-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-5);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
}

.user-cell { display: flex; align-items: center; gap: var(--space-3); }
.user-info { display: flex; flex-direction: column; line-height: 1.4; }
.user-info .nickname { font-weight: 600; }
.user-info .username { font-size: 12px; color: var(--text-secondary, #909399); }

:deep(.el-table) { border-radius: var(--radius-md); }
:deep(.el-table th.el-table__cell) { background: var(--bg-subtle); color: var(--text-regular); font-weight: 600; }
:deep(.el-table tr) { transition: background var(--transition-base); }
:deep(.el-table__row:hover > td.el-table__cell) { background: var(--el-color-primary-light-9) !important; }
:deep(.el-table .el-table__cell) { border-bottom: 1px solid var(--border-color); }
:deep(.el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell) { background: var(--bg-subtle); }

.pagination-wrap { display: flex; justify-content: flex-end; margin-top: var(--space-5); }

:deep(.el-dialog) { border-radius: var(--radius-lg); overflow: hidden; }
:deep(.el-dialog__header) { padding: var(--space-4) var(--space-5); border-bottom: 1px solid var(--border-color); margin-right: 0; }
:deep(.el-dialog__body) { padding: var(--space-5); }
:deep(.el-dialog__footer) { padding: var(--space-4) var(--space-5); border-top: 1px solid var(--border-color); }
</style>
