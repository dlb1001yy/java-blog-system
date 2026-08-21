<template>
  <div class="resume-page" v-if="resume">
    <div class="container">
    <!-- 简历头部 -->
    <div class="card resume-header">
      <div class="header-left">
        <el-avatar :size="100" :src="resume.avatar" class="avatar">
          {{ resume.name?.charAt(0) }}
        </el-avatar>
      </div>
      <div class="header-info">
        <h1>{{ resume.name }}</h1>
        <p class="job-title">{{ resume.jobTitle }}</p>
        <div class="contact-info">
          <span><el-icon><Phone /></el-icon> {{ resume.phone }}</span>
          <span><el-icon><Message /></el-icon> {{ resume.email }}</span>
          <span><el-icon><Location /></el-icon> {{ resume.address }}</span>
        </div>
        <div class="basic-info">
          <span v-if="genderText">{{ genderText }}</span>
          <span v-if="age">{{ age }}</span>
          <span v-if="maritalText">{{ maritalText }}</span>
          <span v-if="resume.workYears != null">{{ resume.workYears }}年经验</span>
          <span v-if="resume.highestEducation">{{ resume.highestEducation }}</span>
          <span v-if="jobSearchText">{{ jobSearchText }}</span>
          <span v-if="resume.expectedSalary">{{ resume.expectedSalary }}</span>
          <span v-if="resume.hukou">户籍: {{ resume.hukou }}</span>
        </div>
      </div>
    </div>

    <!-- 个人简介 -->
    <div class="card">
      <h2 class="section-title">
        <el-icon><User /></el-icon>
        个人简介
      </h2>
      <p class="summary-text" v-html="resume.summary"></p>
    </div>

    <!-- 自我评价 -->
    <div class="card" v-if="resume.selfEvaluation">
      <h2 class="section-title"><el-icon><EditPen /></el-icon>自我评价</h2>
      <p class="summary-text" v-html="resume.selfEvaluation"></p>
    </div>

    <!-- 技能特长 -->
    <div class="card">
      <h2 class="section-title">
        <el-icon><Cpu /></el-icon>
        技能特长
      </h2>
      <div class="skills-grid">
        <div class="skill-item" v-for="skill in skills" :key="skill.name">
          <div class="skill-header">
            <span class="skill-name">{{ skill.name }}</span>
            <span class="skill-level">{{ skill.level }}</span>
          </div>
          <el-progress 
            :percentage="skill.percent" 
            :color="skill.color"
            :stroke-width="8"
          />
        </div>
      </div>
    </div>

    <!-- 工作经历 -->
    <div class="card">
      <h2 class="section-title">
        <el-icon><Briefcase /></el-icon>
        工作经历
      </h2>
      <div class="timeline">
        <div class="timeline-item" v-for="(work, index) in workExperience" :key="index">
          <div class="timeline-dot"></div>
          <div class="timeline-content">
            <div class="timeline-header">
              <h3>{{ work.company }}</h3>
              <span class="timeline-date">{{ work.startDate }} - {{ work.endDate }}</span>
            </div>
            <p class="timeline-position">{{ work.position }}</p>
            <p class="summary-text" v-html="work.description"></p>
          </div>
        </div>
      </div>
    </div>

    <!-- 项目经验 -->
    <div class="card">
      <h2 class="section-title">
        <el-icon><Folder /></el-icon>
        项目经验
      </h2>
      <div class="projects-list">
        <div class="project-item" v-for="(project, index) in projects" :key="index">
          <div class="project-header">
            <h3>{{ project.name }}</h3>
            <span class="project-date">{{ project.date }}</span>
          </div>
          <p class="project-role">{{ project.role }}</p>
          <p class="summary-text" v-html="project.description"></p>
          <div class="project-tech" v-if="project.technologies">
            <el-tag 
              v-for="tech in project.technologies" 
              :key="tech"
              size="small"
              type="info"
              effect="plain"
            >
              {{ tech }}
            </el-tag>
          </div>
        </div>
      </div>
    </div>

    <!-- 教育背景 -->
    <div class="card">
      <h2 class="section-title">
        <el-icon><Reading /></el-icon>
        教育背景
      </h2>
      <div class="timeline">
        <div class="timeline-item" v-for="(edu, index) in education" :key="index">
          <div class="timeline-dot"></div>
          <div class="timeline-content">
            <div class="timeline-header">
              <h3>{{ edu.school }}</h3>
              <span class="timeline-date">{{ edu.startDate }} - {{ edu.endDate }}</span>
            </div>
            <p class="timeline-position">{{ edu.major }} ({{ edu.degree }})</p>
            <p class="timeline-desc" v-if="edu.description">{{ edu.description }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 证书荣誉 -->
    <div class="card" v-if="certificates.length">
      <h2 class="section-title"><el-icon><Medal /></el-icon>证书荣誉</h2>
      <div class="timeline">
        <div class="timeline-item" v-for="(cert, index) in certificates" :key="index">
          <div class="timeline-dot"></div>
          <div class="timeline-content">
            <div class="timeline-header">
              <h3>{{ cert.name }}</h3>
              <span class="timeline-date">{{ cert.date }}</span>
            </div>
            <p class="timeline-position" v-if="cert.issuer">{{ cert.issuer }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 兴趣爱好 -->
    <div class="card" v-if="resume.interests">
      <h2 class="section-title"><el-icon><Star /></el-icon>兴趣爱好</h2>
      <p class="summary-text">{{ resume.interests }}</p>
    </div>

    <!-- 打印/导出按钮 -->
    <div class="download-section">
      <el-button type="primary" size="large" :icon="Printer" @click="downloadResume">
        打印 / 导出 PDF
      </el-button>
    </div>
    </div>
  </div>
  <el-empty v-else-if="expired" description="链接已失效或不存在" />
  <el-empty v-else description="暂无简历信息" />
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import {
  Phone, Message, Location, User, Cpu, Briefcase,
  Folder, Reading, Download, EditPen, Medal, Star, Printer
} from '@element-plus/icons-vue'
import articleApi from '@/api/article'
import resumeApi from '@/api/resume'

const route = useRoute()
const resume = ref(null)
const isShareMode = computed(() => !!route.params.token)
const expired = ref(false)

const skills = computed(() => {
  if (!resume.value?.skills) return []
  try {
    return JSON.parse(resume.value.skills)
  } catch {
    return []
  }
})

const workExperience = computed(() => {
  if (!resume.value?.workExperience) return []
  try {
    return JSON.parse(resume.value.workExperience)
  } catch {
    return []
  }
})

const projects = computed(() => {
  if (!resume.value?.projects) return []
  try {
    return JSON.parse(resume.value.projects)
  } catch {
    return []
  }
})

const education = computed(() => {
  if (!resume.value?.education) return []
  try {
    return JSON.parse(resume.value.education)
  } catch {
    return []
  }
})

const certificates = computed(() => {
  if (!resume.value?.certificates) return []
  try { return JSON.parse(resume.value.certificates) } catch { return [] }
})
const genderText = computed(() => {
  const g = resume.value?.gender
  return g === 0 ? '男' : g === 1 ? '女' : ''
})
const maritalText = computed(() => {
  const m = resume.value?.maritalStatus
  return m === 0 ? '未婚' : m === 1 ? '已婚' : m === 2 ? '离异' : ''
})
const jobSearchText = computed(() => {
  const s = resume.value?.jobSearchStatus
  return s === 0 ? '离职-随时到岗' : s === 1 ? '在职-暂不流动' : s === 2 ? '在职-考虑机会' : ''
})
const age = computed(() => {
  if (!resume.value?.birthDate) return ''
  const birth = new Date(resume.value.birthDate)
  const now = new Date()
  let a = now.getFullYear() - birth.getFullYear()
  const m = now.getMonth() - birth.getMonth()
  if (m < 0 || (m === 0 && now.getDate() < birth.getDate())) a--
  return a > 0 ? a + '岁' : ''
})

const fetchResume = async () => {
  // 分享 token 模式（匿名访问）
  if (isShareMode.value) {
    try {
      const res = await resumeApi.getResumeByToken(route.params.token)
      resume.value = res.data || null
      expired.value = !resume.value
    } catch {
      expired.value = true
    }
    return
  }
  try {
    const res = route.params.userId
      ? await articleApi.getResumeByUserId(route.params.userId)
      : await articleApi.getResume()
    resume.value = res.data || null
  } catch {
    resume.value = null
  }
}

const downloadResume = () => {
  // 触发PDF下载
  window.print()
}

onMounted(() => {
  fetchResume()
})
</script>

<style scoped>
.resume-header {
  display: flex;
  align-items: center;
  gap: 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.header-info h1 {
  font-size: 32px;
  margin-bottom: 8px;
}

.job-title {
  font-size: 18px;
  opacity: 0.9;
  margin-bottom: 16px;
}

.contact-info {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}

.contact-info span {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  opacity: 0.9;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 24px;
  padding-bottom: 12px;
  border-bottom: 2px solid var(--primary-color);
}

.summary-text {
  font-size: 15px;
  line-height: 1.8;
  color: var(--text-regular);
  /* 新增这一行，让文本中的换行符生效 */
  white-space: pre-line; 
}

.skills-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.skill-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.skill-name {
  font-weight: 500;
  font-size: 14px;
}

.skill-level {
  font-size: 12px;
  color: var(--text-secondary);
}

.timeline {
  position: relative;
  padding-left: 24px;
}

.timeline::before {
  content: '';
  position: absolute;
  left: 6px;
  top: 8px;
  bottom: 8px;
  width: 2px;
  background: var(--border-color);
}

.timeline-item {
  position: relative;
  margin-bottom: 24px;
}

.timeline-dot {
  position: absolute;
  left: -24px;
  top: 6px;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: var(--primary-color);
  border: 3px solid #fff;
  box-shadow: 0 0 0 2px var(--primary-color);
}

.timeline-content {
  padding-left: 12px;
}

.timeline-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.timeline-header h3 {
  font-size: 16px;
  font-weight: 600;
}

.timeline-date {
  font-size: 13px;
  color: var(--text-secondary);
}

.timeline-position {
  color: var(--primary-color);
  font-size: 14px;
  margin-bottom: 8px;
  font-weight: 500;
}

.timeline-desc {
  font-size: 14px;
  color: var(--text-regular);
  line-height: 1.6;
}

.projects-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.project-item {
  padding: 16px;
  background: #f9fafb;
  border-radius: var(--radius-sm);
  border-left: 4px solid var(--primary-color);
}

.project-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.project-header h3 {
  font-size: 16px;
  font-weight: 600;
}

.project-date {
  font-size: 13px;
  color: var(--text-secondary);
}

.project-role {
  color: var(--primary-color);
  font-size: 14px;
  margin-bottom: 8px;
  font-weight: 500;
}

.project-desc {
  font-size: 14px;
  color: var(--text-regular);
  line-height: 1.6;
  margin-bottom: 12px;
}

.project-tech {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.download-section {
  text-align: center;
  margin-top: 32px;
}

@media (max-width: 768px) {
  .resume-header {
    flex-direction: column;
    text-align: center;
  }
  
  .contact-info {
    justify-content: center;
  }
  
  .skills-grid {
    grid-template-columns: 1fr;
  }
  
  .timeline-header {
    flex-direction: column;
    align-items: flex-start;
  }
}

.basic-info {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 12px;
}
.basic-info span {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  font-size: 13px;
}
</style>

<style>
/* 打印样式（非 scoped，需隐藏布局级组件） */
@media print {
  .app-header,
  .app-footer,
  .player-bar,
  .back-to-top,
  .download-section {
    display: none !important;
  }

  body,
  #app .main-content {
    padding: 0 !important;
    margin: 0 !important;
    background: #fff !important;
  }

  .resume-page .container {
    width: 100% !important;
    max-width: 100% !important;
    margin: 0 !important;
    padding: 0 !important;
  }

  .resume-page .card {
    box-shadow: none !important;
    border: none !important;
    break-inside: avoid;
    page-break-inside: avoid;
  }
}
</style>