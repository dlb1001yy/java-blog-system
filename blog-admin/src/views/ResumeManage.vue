<template>
  <PageContainer title="用户简历管理" description="审核前台用户简历">
    <div class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="用户名/姓名" clearable style="width: 200px" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="待审核" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已拒绝" :value="2" />
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
        <el-table-column prop="userName" label="用户名" width="120" show-overflow-tooltip />
        <el-table-column prop="name" label="姓名" width="100" show-overflow-tooltip />
        <el-table-column prop="jobTitle" label="求职岗位" min-width="140" show-overflow-tooltip />
        <el-table-column label="审核状态" width="100">
          <template #default="{ row }">
            <el-tooltip v-if="row.status === 2 && row.auditRemark" :content="row.auditRemark" placement="top">
              <el-tag type="danger" size="small">已拒绝</el-tag>
            </el-tooltip>
            <el-tag v-else :type="statusType[row.status] || 'info'" size="small">
              {{ statusMap[row.status] || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="170" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleDetail(row)">详情</el-button>
            <el-button v-if="row.status !== 1" link type="success" @click="handleApprove(row)">通过</el-button>
            <el-button v-if="row.status !== 2" link type="danger" @click="handleReject(row)">拒绝</el-button>
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

    <!-- 详情抽屉 -->
    <el-drawer v-model="detailVisible" title="简历详情" size="45%">
      <div v-loading="detailLoading" class="detail-body">
        <template v-if="detail">
          <el-divider content-position="left">基本信息</el-divider>
          <div class="info-grid">
            <div class="info-item"><span class="info-label">姓名</span><span>{{ detail.name || '-' }}</span></div>
            <div class="info-item"><span class="info-label">性别</span><span>{{ detail.gender === 1 ? '女' : '男' }}</span></div>
            <div class="info-item"><span class="info-label">出生日期</span><span>{{ detail.birthDate || '-' }}</span></div>
            <div class="info-item"><span class="info-label">电话</span><span>{{ detail.phone || '-' }}</span></div>
            <div class="info-item"><span class="info-label">邮箱</span><span>{{ detail.email || '-' }}</span></div>
            <div class="info-item"><span class="info-label">现居地址</span><span>{{ detail.address || '-' }}</span></div>
            <div class="info-item"><span class="info-label">求职岗位</span><span>{{ detail.jobTitle || '-' }}</span></div>
            <div class="info-item"><span class="info-label">期望薪资</span><span>{{ detail.expectedSalary || '-' }}</span></div>
            <div class="info-item"><span class="info-label">最高学历</span><span>{{ detail.highestEducation || '-' }}</span></div>
            <div class="info-item"><span class="info-label">工作年限</span><span>{{ detail.workYears != null ? detail.workYears + ' 年' : '-' }}</span></div>
          </div>

          <template v-if="detail.selfEvaluation">
            <el-divider content-position="left">自我评价</el-divider>
            <p class="section-text">{{ detail.selfEvaluation }}</p>
          </template>
          <template v-if="detail.summary">
            <el-divider content-position="left">个人简介</el-divider>
            <p class="section-text">{{ detail.summary }}</p>
          </template>

          <template v-if="detailData.skills.length">
            <el-divider content-position="left">技能特长</el-divider>
            <div v-for="(item, i) in detailData.skills" :key="'sk' + i" class="list-item">
              {{ item.name }}<template v-if="item.level">（{{ item.level }}）</template>
            </div>
          </template>

          <template v-if="detailData.work.length">
            <el-divider content-position="left">工作经历</el-divider>
            <div v-for="(item, i) in detailData.work" :key="'wk' + i" class="list-item">
              <div class="item-title">{{ item.company || '-' }}<template v-if="item.position"> · {{ item.position }}</template></div>
              <div v-if="item.startDate || item.endDate" class="item-sub">{{ item.startDate || '?' }} ~ {{ item.endDate || '至今' }}</div>
              <div v-if="item.description" class="item-sub">{{ item.description }}</div>
            </div>
          </template>

          <template v-if="detailData.projects.length">
            <el-divider content-position="left">项目经验</el-divider>
            <div v-for="(item, i) in detailData.projects" :key="'pj' + i" class="list-item">
              <div class="item-title">{{ item.name || '-' }}<template v-if="item.role"> · {{ item.role }}</template></div>
              <div v-if="item.date" class="item-sub">{{ item.date }}</div>
              <div v-if="item.description" class="item-sub">{{ item.description }}</div>
            </div>
          </template>

          <template v-if="detailData.education.length">
            <el-divider content-position="left">教育背景</el-divider>
            <div v-for="(item, i) in detailData.education" :key="'ed' + i" class="list-item">
              <div class="item-title">{{ item.school || '-' }}<template v-if="item.major"> · {{ item.major }}</template><template v-if="item.degree"> · {{ item.degree }}</template></div>
              <div v-if="item.startDate || item.endDate" class="item-sub">{{ item.startDate || '?' }} ~ {{ item.endDate || '至今' }}</div>
            </div>
          </template>

          <template v-if="detailData.certificates.length">
            <el-divider content-position="left">证书荣誉</el-divider>
            <div v-for="(item, i) in detailData.certificates" :key="'ct' + i" class="list-item">
              {{ item.name }}<template v-if="item.issuer">（{{ item.issuer }}）</template><template v-if="item.date"> · {{ item.date }}</template>
            </div>
          </template>
        </template>
      </div>
    </el-drawer>

    <!-- 拒绝弹窗 -->
    <el-dialog v-model="rejectVisible" title="拒绝原因" width="440px">
      <el-input v-model="rejectRemark" type="textarea" :rows="4" placeholder="请输入拒绝原因" />
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleRejectSubmit">确定</el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import resumeApi from '@/api/resume'
import PageContainer from '@/components/PageContainer.vue'

const loading = ref(false)
const saving = ref(false)
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const statusMap = { 0: '待审核', 1: '已通过', 2: '已拒绝' }
const statusType = { 0: 'warning', 1: 'success', 2: 'danger' }

const searchForm = reactive({ keyword: '', status: null })

const fetchData = async () => {
  loading.value = true
  try {
    const res = await resumeApi.getPage({
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

// 详情
const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref(null)
const detailData = ref({ skills: [], work: [], projects: [], education: [], certificates: [] })

const parseJsonArray = (str) => {
  try {
    const arr = str ? JSON.parse(str) : []
    return Array.isArray(arr) ? arr : []
  } catch {
    return []
  }
}

const handleDetail = async (row) => {
  detailVisible.value = true
  detailLoading.value = true
  try {
    const res = await resumeApi.getDetail(row.id)
    detail.value = res.data
    detailData.value = {
      skills: parseJsonArray(detail.value.skills),
      work: parseJsonArray(detail.value.workExperience),
      projects: parseJsonArray(detail.value.projects),
      education: parseJsonArray(detail.value.education),
      certificates: parseJsonArray(detail.value.certificates)
    }
  } finally {
    detailLoading.value = false
  }
}

// 通过
const handleApprove = (row) => {
  ElMessageBox.confirm(`确定通过「${row.name || row.userName}」的简历审核吗？`, '提示', { type: 'warning' })
    .then(async () => {
      await resumeApi.audit(row.id, 1, '')
      ElMessage.success('已通过')
      fetchData()
    }).catch(() => {})
}

// 拒绝
const rejectVisible = ref(false)
const rejectRemark = ref('')
const rejectId = ref(null)

const handleReject = (row) => {
  rejectId.value = row.id
  rejectRemark.value = ''
  rejectVisible.value = true
}

const handleRejectSubmit = async () => {
  if (!rejectRemark.value.trim()) {
    ElMessage.warning('请填写拒绝原因')
    return
  }
  saving.value = true
  try {
    await resumeApi.audit(rejectId.value, 2, rejectRemark.value.trim())
    ElMessage.success('已拒绝')
    rejectVisible.value = false
    fetchData()
  } finally {
    saving.value = false
  }
}

onMounted(() => fetchData())
</script>

<style scoped>
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

.detail-body { min-height: 200px; }
.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-3) var(--space-5);
}
.info-item { display: flex; font-size: 14px; }
.info-label { color: var(--text-secondary, #909399); width: 80px; flex-shrink: 0; }
.section-text { font-size: 14px; line-height: 1.8; white-space: pre-wrap; margin: 0; }
.list-item {
  padding: var(--space-2) 0;
  border-bottom: 1px dashed var(--border-color);
  font-size: 14px;
  line-height: 1.6;
}
.list-item:last-child { border-bottom: none; }
.item-title { font-weight: 600; }
.item-sub { color: var(--text-secondary, #909399); font-size: 13px; margin-top: 2px; white-space: pre-wrap; }
:deep(.el-divider__text) { font-weight: 600; color: var(--color-primary); }
</style>
