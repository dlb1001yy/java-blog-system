<template>
  <PageContainer title="简历管理" description="编辑个人简历信息">
    <template #action>
      <el-button type="primary" @click="handleSave">保存</el-button>
    </template>
    <div class="content-card">
      <el-form :model="form" label-width="100px">
        <el-divider content-position="left">基本信息</el-divider>
        <el-row :gutter="20">
          <el-col :span="8"><el-form-item label="姓名"><el-input v-model="form.name" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="性别"><el-radio-group v-model="form.gender"><el-radio :label="0">男</el-radio><el-radio :label="1">女</el-radio></el-radio-group></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="出生日期"><el-date-picker v-model="form.birthDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width:100%"/></el-form-item></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="8"><el-form-item label="婚姻状况"><el-select v-model="form.maritalStatus" placeholder="请选择" style="width:100%"><el-option :label="'未婚'" :value="0"/><el-option :label="'已婚'" :value="1"/><el-option :label="'离异'" :value="2"/></el-select></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="户籍"><el-input v-model="form.hukou" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="照片"><Upload v-model="form.avatar" placeholder="上传照片" /></el-form-item></el-col>
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
            <el-button type="danger" :icon="Delete" circle @click="removeSkill(i)" />
          </div>
          <el-button type="primary" plain :icon="Plus" @click="addSkill">新增技能</el-button>
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
              <el-col :span="8"><el-form-item label="结束月份"><el-date-picker v-model="work.endDate" type="month" value-format="YYYY-MM" placeholder="如 至今" style="width:100%"/></el-form-item></el-col>
            </el-row>
            <el-form-item label="工作描述"><el-input v-model="work.description" type="textarea" :rows="3" /></el-form-item>
            <div class="block-actions"><el-button type="danger" :icon="Delete" @click="removeWork(i)">删除该经历</el-button></div>
          </div>
          <el-button type="primary" plain :icon="Plus" @click="addWork">新增工作经历</el-button>
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
                <el-tag v-for="(tech, ti) in project.technologies" :key="ti" closable @close="removeTech(project, ti)" style="margin-right:6px">{{ tech }}</el-tag>
                <el-input v-model="project.techInput" placeholder="输入技术名回车添加" style="width:200px" @keyup.enter="addTech(project)" @blur="addTech(project)" />
              </div>
            </el-form-item>
            <div class="block-actions"><el-button type="danger" :icon="Delete" @click="removeProject(i)">删除该项目</el-button></div>
          </div>
          <el-button type="primary" plain :icon="Plus" @click="addProject">新增项目</el-button>
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
            <div class="block-actions"><el-button type="danger" :icon="Delete" @click="removeEducation(i)">删除该经历</el-button></div>
          </div>
          <el-button type="primary" plain :icon="Plus" @click="addEducation">新增教育经历</el-button>
        </div>

        <el-divider content-position="left">证书荣誉</el-divider>
        <div class="dynamic-list">
          <div v-for="(cert, i) in certificateList" :key="i" class="dynamic-row">
            <el-input v-model="cert.name" placeholder="证书名称" style="width:200px"/>
            <el-input v-model="cert.issuer" placeholder="颁发机构" style="width:180px; margin-left:8px"/>
            <el-date-picker v-model="cert.date" type="date" value-format="YYYY-MM-DD" placeholder="获得日期" style="margin-left:8px"/>
            <el-button type="danger" :icon="Delete" circle @click="removeCertificate(i)" style="margin-left:8px"/>
          </div>
          <el-button type="primary" plain :icon="Plus" @click="addCertificate">新增证书</el-button>
        </div>

        <el-divider content-position="left">兴趣爱好</el-divider>
        <el-form-item label="兴趣爱好"><el-input v-model="form.interests" placeholder="如 阅读、摄影、跑步" /></el-form-item>
      </el-form>
    </div>
  </PageContainer>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Delete, Plus } from '@element-plus/icons-vue'
import resumeApi from '@/api/resume'
import Upload from '@/components/Upload.vue'
import PageContainer from '@/components/PageContainer.vue'

const form = reactive({
  name: '',
  jobTitle: '',
  gender: 0,
  maritalStatus: 0,
  workYears: 0,
  expectedSalary: '',
  highestEducation: '',
  jobSearchStatus: 0,
  hukou: '',
  birthDate: '',
  phone: '',
  email: '',
  address: '',
  avatar: '',
  summary: '',
  selfEvaluation: '',
  skills: '',
  workExperience: '',
  education: '',
  projects: '',
  certificates: '',
  interests: ''
})

const skillList = ref([])
const workList = ref([])
const projectList = ref([])
const educationList = ref([])
const certificateList = ref([])

const parseJsonArray = (str) => {
  try {
    return str ? JSON.parse(str) : []
  } catch {
    return []
  }
}

const fetchResume = async () => {
  const res = await resumeApi.getResume()
  if (res.data) {
    Object.assign(form, res.data)
    skillList.value = parseJsonArray(form.skills)
    workList.value = parseJsonArray(form.workExperience)
    projectList.value = parseJsonArray(form.projects)
    educationList.value = parseJsonArray(form.education)
    certificateList.value = parseJsonArray(form.certificates)
  }
}

const handleSave = async () => {
  try {
    form.skills = JSON.stringify(skillList.value)
    form.workExperience = JSON.stringify(workList.value)
    form.projects = JSON.stringify(projectList.value)
    form.education = JSON.stringify(educationList.value)
    form.certificates = JSON.stringify(certificateList.value)
    await resumeApi.save(form)
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

const addSkill = () => {
  skillList.value.push({ name: '', level: '熟练', percent: 80 })
}
const removeSkill = (i) => {
  skillList.value.splice(i, 1)
}

const addWork = () => {
  workList.value.push({ company: '', position: '', startDate: '', endDate: '', description: '' })
}
const removeWork = (i) => {
  workList.value.splice(i, 1)
}

const addProject = () => {
  projectList.value.push({ name: '', role: '', date: '', description: '', technologies: [], techInput: '' })
}
const removeProject = (i) => {
  projectList.value.splice(i, 1)
}

const addEducation = () => {
  educationList.value.push({ school: '', major: '', degree: '本科', startDate: '', endDate: '', description: '' })
}
const removeEducation = (i) => {
  educationList.value.splice(i, 1)
}

const addCertificate = () => {
  certificateList.value.push({ name: '', issuer: '', date: '' })
}
const removeCertificate = (i) => {
  certificateList.value.splice(i, 1)
}

const addTech = (project) => {
  if (project.techInput && project.techInput.trim()) {
    project.technologies.push(project.techInput.trim())
    project.techInput = ''
  }
}
const removeTech = (project, i) => {
  project.technologies.splice(i, 1)
}

onMounted(() => fetchResume())
</script>

<style scoped>
.content-card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-color);
}

:deep(.el-input__wrapper) {
  border-radius: var(--radius-md);
  transition: box-shadow var(--transition-base);
}
:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--color-primary) inset, 0 0 0 4px rgba(99, 102, 241, 0.1);
}
:deep(.el-textarea__inner) {
  border-radius: var(--radius-md);
  transition: box-shadow var(--transition-base);
}
:deep(.el-textarea__inner:focus) {
  box-shadow: 0 0 0 1px var(--color-primary) inset, 0 0 0 4px rgba(99, 102, 241, 0.1);
}

:deep(.el-tabs__item) {
  font-size: var(--font-base);
  font-weight: 500;
}
:deep(.el-tabs__active-bar) {
  background: var(--gradient-primary);
  height: 3px;
  border-radius: var(--radius-full);
}
:deep(.el-tabs__item.is-active) {
  color: var(--color-primary);
}
:deep(.el-tabs__item:hover) {
  color: var(--color-primary-light);
}

.dynamic-list { margin-left: 100px; margin-bottom: 24px; }
.dynamic-row { display: flex; align-items: center; margin-bottom: 12px; gap: 8px; }
.dynamic-block { background: var(--bg-card); border: 1px solid var(--border-color); border-radius: var(--radius-md); padding: 16px; margin-bottom: 16px; }
.block-actions { text-align: right; margin-top: 8px; }
.tech-tags { display: flex; align-items: center; flex-wrap: wrap; gap: 6px; }
:deep(.el-divider__text) { font-weight: 600; color: var(--color-primary); }
</style>
