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
            <p class="timeline-desc">{{ work.description }}</p>
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
          <p class="project-desc">{{ project.description }}</p>
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

    <!-- 下载按钮 -->
    <div class="download-section">
      <el-button type="primary" size="large" :icon="Download" @click="downloadResume">
        下载PDF简历
      </el-button>
    </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import {
  Phone, Message, Location, User, Cpu, Briefcase,
  Folder, Reading, Download
} from '@element-plus/icons-vue'
import articleApi from '@/api/article'

// 默认示例数据（后端无数据时展示）
const defaultResume = {
  name: '张三',
  jobTitle: 'Java开发工程师',
  phone: '138-0000-0000',
  email: 'zhangsan@example.com',
  address: '北京市朝阳区',
  avatar: '',
  summary: '5年Java开发经验，熟练掌握Spring Boot、微服务架构、MySQL、Redis等技术栈。具备良好的编码习惯和团队协作能力，善于解决技术难题。',
  skills: JSON.stringify([
    { name: 'Java', level: '精通', percent: 90, color: '#67C23A' },
    { name: 'Spring Boot', level: '精通', percent: 85, color: '#409EFF' },
    { name: 'MySQL', level: '熟练', percent: 80, color: '#E6A23C' },
    { name: 'Redis', level: '熟练', percent: 75, color: '#F56C6C' },
    { name: 'Vue.js', level: '熟悉', percent: 70, color: '#909399' }
  ]),
  workExperience: JSON.stringify([
    {
      company: 'ABC科技有限公司',
      position: '高级Java开发工程师',
      startDate: '2021-03',
      endDate: '至今',
      description: '负责核心业务系统的架构设计和开发，优化系统性能，带领小组完成多个重要项目。'
    },
    {
      company: 'XYZ互联网公司',
      position: 'Java开发工程师',
      startDate: '2019-07',
      endDate: '2021-02',
      description: '参与电商平台的开发与维护，负责订单模块、支付模块的功能迭代。'
    }
  ]),
  projects: JSON.stringify([
    {
      name: '企业级博客系统',
      role: '核心开发',
      date: '2023-01',
      description: '基于Spring Boot + Vue.js开发的全栈博客系统，支持文章管理、评论、标签分类等功能。',
      technologies: ['Spring Boot', 'Vue.js', 'MySQL', 'Redis', 'MyBatis-Plus']
    }
  ]),
  education: JSON.stringify([
    {
      school: '某某大学',
      major: '计算机科学与技术',
      degree: '本科',
      startDate: '2015-09',
      endDate: '2019-06',
      description: ''
    }
  ])
}

const resume = ref(null)

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

const fetchResume = async () => {
  try {
    const res = await articleApi.getResume()
    // 如果后端返回空数据，使用示例数据
    resume.value = res.data || defaultResume
  } catch {
    // 请求失败也使用示例数据
    resume.value = defaultResume
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
</style>