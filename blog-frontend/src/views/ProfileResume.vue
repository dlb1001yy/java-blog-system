<template>
  <div class="profile-resume-page">
    <div class="container">
      <div class="card">
        <h1 class="page-title">我的简历</h1>
        <p class="page-desc">
          <!-- 简历展示页暂时屏蔽 -->
          <el-tag :type="auditStatusTagType" style="margin-left:12px">{{ auditStatusText }}</el-tag>
          <span v-if="auditStatus === 2 && auditRemark" class="audit-remark">拒绝原因：{{ auditRemark }}</span>
        </p>

        <el-form :model="form" label-width="100px">
          <el-divider content-position="left">基本信息</el-divider>
          <el-row :gutter="20">
            <el-col :span="8"><el-form-item label="姓名"><el-input v-model="form.name" placeholder="姓名" /></el-form-item></el-col>
            <el-col :span="8"><el-form-item label="性别"><el-radio-group v-model="form.gender"><el-radio :value="0">男</el-radio><el-radio :value="1">女</el-radio></el-radio-group></el-form-item></el-col>
            <el-col :span="8"><el-form-item label="出生日期"><el-date-picker v-model="form.birthDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width:100%"/></el-form-item></el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="8"><el-form-item label="婚姻状况"><el-select v-model="form.maritalStatus" placeholder="请选择" style="width:100%"><el-option label="未婚" :value="0"/><el-option label="已婚" :value="1"/><el-option label="离异" :value="2"/></el-select></el-form-item></el-col>
            <el-col :span="8"><el-form-item label="户籍"><el-input v-model="form.hukou" /></el-form-item></el-col>
            <el-col :span="8"><el-form-item label="照片链接"><el-input v-model="form.avatar" placeholder="头像图片地址" /></el-form-item></el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="8"><el-form-item label="电话"><el-input v-model="form.phone" /></el-form-item></el-col>
            <el-col :span="8"><el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item></el-col>
            <el-col :span="8"><el-form-item label="现居地址"><el-input v-model="form.address" /></el-form-item></el-col>
          </el-row>

          <el-divider content-position="left">求职意向</el-divider>
          <el-row :gutter="20">
            <el-col :span="8"><el-form-item label="求职岗位"><el-input v-model="form.jobTitle" /></el-form-item></el-col>
            <el-col :span="8"><el-form-item label="求职状态"><el-select v-model="form.jobSearchStatus" placeholder="请选择" style="width:100%"><el-option label="离职-随时到岗" :value="0"/><el-option label="在职-暂不流动" :value="1"/><el-option label="在职-考虑机会" :value="2"/></el-select></el-form-item></el-col>
            <el-col :span="8"><el-form-item label="期望薪资"><el-input v-model="form.expectedSalary" placeholder="如 20-30K" /></el-form-item></el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="8"><el-form-item label="工作年限"><el-input-number v-model="form.workYears" :min="0" :max="50" style="width:100%"/></el-form-item></el-col>
            <el-col :span="8"><el-form-item label="最高学历"><el-select v-model="form.highestEducation" placeholder="请选择" style="width:100%"><el-option label="高中" value="高中"/><el-option label="大专" value="大专"/><el-option label="本科" value="本科"/><el-option label="硕士" value="硕士"/><el-option label="博士" value="博士"/></el-select></el-form-item></el-col>
          </el-row>

          <el-divider content-position="left">个人简介</el-divider>
          <el-form-item label="简介"><el-input v-model="form.summary" type="textarea" :rows="4" /></el-form-item>

          <el-divider content-position="left">自我评价</el-divider>
          <el-form-item label="自我评价"><el-input v-model="form.selfEvaluation" type="textarea" :rows="4" /></el-form-item>

          <el-divider content-position="left">技能特长</el-divider>
          <div class="dynamic-list">
            <div v-for="(skill, i) in skillList" :key="i" class="dynamic-row">
              <el-input v-model="skill.name" placeholder="技能名" style="width:180px"/>
              <el-select v-model="skill.level" style="width:100px"><el-option label="精通" value="精通"/><el-option label="熟练" value="熟练"/><el-option label="掌握" value="掌握"/><el-option label="了解" value="了解"/></el-select>
              <el-slider v-model="skill.percent" style="flex:1; margin:0 16px" />
              <el-button type="danger" :icon="Delete" circle @click="skillList.splice(i, 1)" />
            </div>
            <el-button type="primary" plain :icon="Plus" @click="skillList.push({ name: '', level: '熟练', percent: 80 })">新增技能</el-button>
          </div>

          <el-divider content-position="left">工作经历</el-divider>
          <div class="dynamic-list">
            <div v-for="(work, i) in workList" :key="i" class="dynamic-block">
              <el-row :gutter="20">
                <el-col :span="8"><el-form-item label="公司"><el-input v-model="work.company" /></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="职位"><el-input v-model="work.position" /></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="开始月份"><el-date-picker v-model="work.startDate" type="month" value-format="YYYY-MM" placeholder="如 2020-01" style="width:100%"/></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="8"><el-form-item label="结束月份"><el-date-picker v-model="work.endDate" type="month" value-format="YYYY-MM" placeholder="至今可不填" style="width:100%"/></el-form-item></el-col>
              </el-row>
              <el-form-item label="工作描述"><el-input v-model="work.description" type="textarea" :rows="3" /></el-form-item>
              <div class="block-actions"><el-button type="danger" :icon="Delete" @click="workList.splice(i, 1)">删除该经历</el-button></div>
            </div>
            <el-button type="primary" plain :icon="Plus" @click="workList.push({ company: '', position: '', startDate: '', endDate: '', description: '' })">新增工作经历</el-button>
          </div>

          <el-divider content-position="left">项目经验</el-divider>
          <div class="dynamic-list">
            <div v-for="(project, i) in projectList" :key="i" class="dynamic-block">
              <el-row :gutter="20">
                <el-col :span="8"><el-form-item label="项目名"><el-input v-model="project.name" /></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="担任角色"><el-input v-model="project.role" /></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="项目时间"><el-input v-model="project.date" placeholder="如 2023-01" /></el-form-item></el-col>
              </el-row>
              <el-form-item label="项目描述"><el-input v-model="project.description" type="textarea" :rows="3" /></el-form-item>
              <el-form-item label="技术栈">
                <div class="tech-tags">
                  <el-tag v-for="(tech, ti) in project.technologies" :key="ti" closable @close="project.technologies.splice(ti, 1)" style="margin-right:6px">{{ tech }}</el-tag>
                  <el-input v-model="project.techInput" placeholder="输入技术名回车添加" style="width:200px" @keyup.enter="addTech(project)" />
                </div>
              </el-form-item>
              <div class="block-actions"><el-button type="danger" :icon="Delete" @click="projectList.splice(i, 1)">删除该项目</el-button></div>
            </div>
            <el-button type="primary" plain :icon="Plus" @click="projectList.push({ name: '', role: '', date: '', description: '', technologies: [], techInput: '' })">新增项目</el-button>
          </div>

          <el-divider content-position="left">教育背景</el-divider>
          <div class="dynamic-list">
            <div v-for="(edu, i) in educationList" :key="i" class="dynamic-block">
              <el-row :gutter="20">
                <el-col :span="8"><el-form-item label="学校"><el-input v-model="edu.school" /></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="专业"><el-input v-model="edu.major" /></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="学历"><el-select v-model="edu.degree" style="width:100%"><el-option label="大专" value="大专"/><el-option label="本科" value="本科"/><el-option label="硕士" value="硕士"/><el-option label="博士" value="博士"/></el-select></el-form-item></el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="8"><el-form-item label="开始月份"><el-date-picker v-model="edu.startDate" type="month" value-format="YYYY-MM" style="width:100%"/></el-form-item></el-col>
                <el-col :span="8"><el-form-item label="结束月份"><el-date-picker v-model="edu.endDate" type="month" value-format="YYYY-MM" style="width:100%"/></el-form-item></el-col>
              </el-row>
              <el-form-item label="描述"><el-input v-model="edu.description" type="textarea" :rows="2" /></el-form-item>
              <div class="block-actions"><el-button type="danger" :icon="Delete" @click="educationList.splice(i, 1)">删除该经历</el-button></div>
            </div>
            <el-button type="primary" plain :icon="Plus" @click="educationList.push({ school: '', major: '', degree: '本科', startDate: '', endDate: '', description: '' })">新增教育经历</el-button>
          </div>

          <el-divider content-position="left">证书荣誉</el-divider>
          <div class="dynamic-list">
            <div v-for="(cert, i) in certificateList" :key="i" class="dynamic-row">
              <el-input v-model="cert.name" placeholder="证书名称" style="width:200px"/>
              <el-input v-model="cert.issuer" placeholder="颁发机构" style="width:180px; margin-left:8px"/>
              <el-date-picker v-model="cert.date" type="date" value-format="YYYY-MM-DD" placeholder="获得日期" style="margin-left:8px"/>
              <el-button type="danger" :icon="Delete" circle @click="certificateList.splice(i, 1)" style="margin-left:8px"/>
            </div>
            <el-button type="primary" plain :icon="Plus" @click="certificateList.push({ name: '', issuer: '', date: '' })">新增证书</el-button>
          </div>

          <el-divider content-position="left">兴趣爱好</el-divider>
          <el-form-item label="兴趣爱好"><el-input v-model="form.interests" placeholder="如 阅读、摄影、跑步" /></el-form-item>

          <div class="save-actions">
            <el-button type="primary" size="large" :loading="saving" @click="handleSave">保存简历</el-button>
          </div>
        </el-form>
      </div>

      <!-- 分享链接 -->
      <div class="card share-card">
        <h2 class="share-title">分享链接</h2>
        <div class="share-form">
          <el-select v-model="shareDuration" style="width:160px">
            <el-option v-for="opt in durationOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
          <el-input-number v-if="shareDuration === -1" v-model="customDays" :min="1" :max="3650" placeholder="天数" style="margin-left:12px" />
          <span v-if="shareDuration === -1" class="custom-days-text">天</span>
          <el-tooltip :disabled="auditStatus === 1" content="简历审核通过后可分享" placement="top">
            <span>
              <el-button type="primary" :disabled="auditStatus !== 1" :loading="sharing" style="margin-left:12px" @click="handleCreateShare">生成分享链接</el-button>
            </span>
          </el-tooltip>
        </div>

        <h3 class="share-subtitle">我的分享</h3>
        <el-table :data="shareList" size="small" empty-text="暂无分享记录">
          <el-table-column label="链接" min-width="320">
            <template #default="{ row }">
              <div class="share-link-cell">
                <span class="share-link-text">{{ shareUrl(row.shareToken) }}</span>
                <el-button link type="primary" size="small" @click="copyLink(row.shareToken)">复制</el-button>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="170" />
          <el-table-column label="过期时间" width="170">
            <template #default="{ row }">
              <el-tag v-if="isExpired(row)" type="danger" size="small">已过期</el-tag>
              <span v-else-if="!row.expireTime">永久</span>
              <span v-else>{{ row.expireTime }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="90">
            <template #default="{ row }">
              <el-button link type="danger" size="small" @click="handleRevoke(row)">撤销</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Plus } from '@element-plus/icons-vue'
import resumeApi from '@/api/resume'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)

const form = reactive({
  name: '',
  jobTitle: '',
  gender: 0,
  maritalStatus: null,
  workYears: null,
  expectedSalary: '',
  highestEducation: '',
  jobSearchStatus: null,
  hukou: '',
  birthDate: '',
  phone: '',
  email: '',
  address: '',
  avatar: '',
  summary: '',
  selfEvaluation: '',
  interests: ''
})

const skillList = ref([])
const workList = ref([])
const projectList = ref([])
const educationList = ref([])
const certificateList = ref([])
const saving = ref(false)

// 审核状态：0待审核 1已通过 2已拒绝
const auditStatus = ref(null)
const auditRemark = ref('')
const auditStatusText = computed(() => {
  const map = { 0: '待审核', 1: '已通过', 2: '已拒绝' }
  return map[auditStatus.value] ?? '待审核'
})
const auditStatusTagType = computed(() => {
  const map = { 0: 'warning', 1: 'success', 2: 'danger' }
  return map[auditStatus.value] ?? 'warning'
})

// 分享相关
const durationOptions = [
  { label: '永久', value: 0 },
  { label: '30分钟', value: 30 },
  { label: '1天', value: 1440 },
  { label: '7天', value: 10080 },
  { label: '1个月', value: 43200 },
  { label: '3个月', value: 129600 },
  { label: '1年', value: 525600 },
  { label: '自定义天数', value: -1 }
]
const shareDuration = ref(0)
const customDays = ref(7)
const shareList = ref([])
const sharing = ref(false)

const shareUrl = (token) => `${location.origin}/blog/resume/share/${token}`
const isExpired = (row) => !!row.expireTime && new Date(row.expireTime).getTime() < Date.now()

const copyLink = async (token) => {
  try {
    await navigator.clipboard.writeText(shareUrl(token))
    ElMessage.success('已复制')
  } catch {
    ElMessage.error('复制失败，请手动复制')
  }
}

const fetchShares = async () => {
  try {
    const res = await resumeApi.getMyShares()
    shareList.value = res.data || []
  } catch {
    // 错误提示由请求拦截器统一处理
  }
}

const handleCreateShare = async () => {
  // 计算过期分钟数：-1 自定义天数；0 表示永久（null）
  const expireMinutes = shareDuration.value === -1
    ? customDays.value * 1440
    : shareDuration.value === 0 ? null : shareDuration.value
  sharing.value = true
  try {
    const res = await resumeApi.createShare({ expireMinutes })
    ElMessage.success('分享链接生成成功')
    if (res.data) {
      shareList.value.unshift(res.data)
    } else {
      fetchShares()
    }
  } catch {
    // 错误提示由请求拦截器统一处理
  } finally {
    sharing.value = false
  }
}

const handleRevoke = async (row) => {
  try {
    await ElMessageBox.confirm('撤销后该分享链接将立即失效，确定撤销吗？', '提示', { type: 'warning' })
  } catch {
    return
  }
  try {
    await resumeApi.revokeShare(row.id)
    ElMessage.success('已撤销')
    fetchShares()
  } catch {
    // 错误提示由请求拦截器统一处理
  }
}

const parseJsonArray = (str) => {
  try {
    return str ? JSON.parse(str) : []
  } catch {
    return []
  }
}

const fetchResume = async () => {
  try {
    const res = await resumeApi.getMyResume()
    if (res.data) {
      Object.assign(form, res.data)
      auditStatus.value = res.data.status ?? 0
      auditRemark.value = res.data.auditRemark || ''
      skillList.value = parseJsonArray(res.data.skills)
      workList.value = parseJsonArray(res.data.workExperience)
      projectList.value = parseJsonArray(res.data.projects)
      educationList.value = parseJsonArray(res.data.education)
      certificateList.value = parseJsonArray(res.data.certificates)
    }
  } catch {
    // 未保存过简历，保持空表单
  }
}

const addTech = (project) => {
  if (project.techInput && project.techInput.trim()) {
    project.technologies.push(project.techInput.trim())
    project.techInput = ''
  }
}

const handleSave = async () => {
  if (!form.name) {
    ElMessage.warning('请填写姓名')
    return
  }
  saving.value = true
  try {
    await resumeApi.saveMyResume({
      ...form,
      skills: JSON.stringify(skillList.value),
      workExperience: JSON.stringify(workList.value),
      projects: JSON.stringify(projectList.value),
      education: JSON.stringify(educationList.value),
      certificates: JSON.stringify(certificateList.value)
    })
    ElMessage.success('保存成功')
  } catch {
    // 错误提示由请求拦截器统一处理
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    router.push('/')
    return
  }
  fetchResume()
  fetchShares()
})
</script>

<style scoped>
.page-title {
  font-size: 26px;
  margin-bottom: 8px;
}

.page-desc {
  color: var(--text-secondary);
  font-size: 14px;
  margin-bottom: 24px;
}

.view-link {
  color: var(--primary-color);
}

.audit-remark {
  margin-left: 10px;
  color: var(--el-color-danger);
  font-size: 13px;
}

.share-card {
  margin-top: 24px;
}

.share-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 16px;
}

.share-form {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.custom-days-text {
  margin-left: 6px;
  color: var(--text-secondary);
}

.share-subtitle {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
}

.share-link-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.share-link-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--text-secondary);
  font-size: 13px;
}

.dynamic-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 20px;
}

.dynamic-row {
  display: flex;
  align-items: center;
}

.dynamic-block {
  padding: 16px;
  background: #f9fafb;
  border-radius: var(--radius-sm);
  border-left: 4px solid var(--primary-color);
}

.block-actions {
  text-align: right;
}

.tech-tags {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.save-actions {
  text-align: center;
  margin-top: 24px;
}

@media (max-width: 768px) {
  .dynamic-row {
    flex-wrap: wrap;
    gap: 8px;
  }
}
</style>
