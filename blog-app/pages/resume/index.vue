<template>
  <view class="resume-page" v-if="resume">
    <!-- Hero 卡片：渐变背景 + 头像 + 姓名 + 联系方式 -->
    <view class="hero-card">
      <view class="avatar">
        <image v-if="avatarUrl" :src="avatarUrl" class="avatar-img" mode="aspectFill" />
        <text v-else class="avatar-letter">{{ resume.name?.charAt(0) }}</text>
      </view>
      <text class="name">{{ resume.name }}</text>
      <text class="job-title" v-if="resume.jobTitle">{{ resume.jobTitle }}</text>
      <view class="contact">
        <view class="contact-item" v-if="resume.phone">
          <Icon name="phone" :size="14" color="rgba(255,255,255,0.9)" />
          <text>{{ resume.phone }}</text>
        </view>
        <view class="contact-item" v-if="resume.email">
          <Icon name="mail" :size="14" />
          <text>{{ resume.email }}</text>
        </view>
        <view class="contact-item" v-if="resume.address">
          <Icon name="location" :size="14" />
          <text>{{ resume.address }}</text>
        </view>
      </view>
    </view>

    <!-- 基本信息 -->
    <view class="section" v-if="genderText || age || maritalText || resume.workYears != null || resume.highestEducation || jobSearchText || resume.expectedSalary || resume.hukou">
      <view class="section-title">
        <Icon name="user" :size="18" :color="colors.primary" />
        <text>基本信息</text>
      </view>
      <view class="info-grid">
        <view class="info-item" v-if="genderText"><text class="info-label">性别</text><text class="info-value">{{ genderText }}</text></view>
        <view class="info-item" v-if="age"><text class="info-label">年龄</text><text class="info-value">{{ age }}</text></view>
        <view class="info-item" v-if="maritalText"><text class="info-label">婚姻</text><text class="info-value">{{ maritalText }}</text></view>
        <view class="info-item" v-if="resume.workYears != null"><text class="info-label">工作年限</text><text class="info-value">{{ resume.workYears }}年</text></view>
        <view class="info-item" v-if="resume.highestEducation"><text class="info-label">最高学历</text><text class="info-value">{{ resume.highestEducation }}</text></view>
        <view class="info-item" v-if="jobSearchText"><text class="info-label">求职状态</text><text class="info-value">{{ jobSearchText }}</text></view>
        <view class="info-item" v-if="resume.expectedSalary"><text class="info-label">期望薪资</text><text class="info-value">{{ resume.expectedSalary }}</text></view>
        <view class="info-item" v-if="resume.hukou"><text class="info-label">户籍</text><text class="info-value">{{ resume.hukou }}</text></view>
      </view>
    </view>

    <!-- 个人简介 -->
    <view class="section" v-if="resume.summary">
      <view class="section-title">
        <Icon name="user" :size="18" :color="colors.primary" />
        <text>个人简介</text>
      </view>
      <text class="section-content">{{ resume.summary }}</text>
    </view>

    <!-- 自我评价 -->
    <view class="section" v-if="resume.selfEvaluation">
      <view class="section-title">
        <Icon name="edit" :size="18" :color="colors.primary" />
        <text>自我评价</text>
      </view>
      <text class="section-content">{{ resume.selfEvaluation }}</text>
    </view>

    <!-- 技能特长（彩色标签云） -->
    <view class="section" v-if="skills.length">
      <view class="section-title">
        <Icon name="edit" :size="18" :color="colors.primary" />
        <text>技能特长</text>
      </view>
      <view class="skills-cloud">
        <view
          class="skill-tag"
          :class="getSkillColor(skill.level)"
          v-for="skill in skills"
          :key="skill.name"
        >
          {{ skill.name }}
        </view>
      </view>
    </view>

    <!-- 工作经历（卡片化时间线） -->
    <view class="section" v-if="workExperience.length">
      <view class="section-title">
        <Icon name="document" :size="18" :color="colors.primary" />
        <text>工作经历</text>
      </view>
      <view class="timeline">
        <view class="timeline-item" v-for="(work, index) in workExperience" :key="index">
          <text class="timeline-title">{{ work.company }}</text>
          <view class="timeline-date">
            <Icon name="clock" :size="12" :color="colors.textTertiary" />
            <text>{{ work.startDate }} - {{ work.endDate }}</text>
          </view>
          <text class="timeline-sub" v-if="work.position">{{ work.position }}</text>
          <text class="timeline-desc" v-if="work.description">{{ work.description }}</text>
        </view>
      </view>
    </view>

    <!-- 项目经验（卡片网格） -->
    <view class="section" v-if="projects.length">
      <view class="section-title">
        <Icon name="folder" :size="18" :color="colors.primary" />
        <text>项目经验</text>
      </view>
      <view class="projects-list">
        <view class="project-item" v-for="(project, index) in projects" :key="index">
          <view class="project-header">
            <text class="project-name">{{ project.name }}</text>
            <text class="project-date" v-if="project.date">{{ project.date }}</text>
          </view>
          <text class="project-role" v-if="project.role">{{ project.role }}</text>
          <text class="project-desc" v-if="project.description">{{ project.description }}</text>
          <view class="project-tags" v-if="project.technologies && project.technologies.length">
            <text class="tag" v-for="tech in project.technologies" :key="tech">{{ tech }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 教育背景（卡片化时间线） -->
    <view class="section" v-if="education.length">
      <view class="section-title">
        <Icon name="document" :size="18" :color="colors.primary" />
        <text>教育背景</text>
      </view>
      <view class="timeline">
        <view class="timeline-item" v-for="(edu, index) in education" :key="index">
          <text class="timeline-title">{{ edu.school }}</text>
          <view class="timeline-date">
            <Icon name="clock" :size="12" :color="colors.textTertiary" />
            <text>{{ edu.startDate }} - {{ edu.endDate }}</text>
          </view>
          <text class="timeline-sub" v-if="edu.major">{{ edu.major }}<text v-if="edu.degree"> ({{ edu.degree }})</text></text>
          <text class="timeline-desc" v-if="edu.description">{{ edu.description }}</text>
        </view>
      </view>
    </view>

    <!-- 证书荣誉 -->
    <view class="section" v-if="certificates.length">
      <view class="section-title">
        <Icon name="document" :size="18" :color="colors.primary" />
        <text>证书荣誉</text>
      </view>
      <view class="timeline">
        <view class="timeline-item" v-for="(cert, index) in certificates" :key="index">
          <text class="timeline-title">{{ cert.name }}</text>
          <view class="timeline-date" v-if="cert.date">
            <Icon name="clock" :size="12" :color="colors.textTertiary" />
            <text>{{ cert.date }}</text>
          </view>
          <text class="timeline-sub" v-if="cert.issuer">{{ cert.issuer }}</text>
        </view>
      </view>
    </view>

    <!-- 兴趣爱好 -->
    <view class="section" v-if="resume.interests">
      <view class="section-title">
        <Icon name="edit" :size="18" :color="colors.primary" />
        <text>兴趣爱好</text>
      </view>
      <text class="section-content">{{ resume.interests }}</text>
    </view>

    <TabBar current="/pages/resume/index" />
  </view>
  <view v-else class="empty-state"><text>暂无简历信息</text></view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app' // UniApp 的生命周期从这里引入
import api from '@/common/api.js'
import { BASE_URL } from '@/common/config.js'
import { colors } from '@/common/theme.js'
import TabBar from '@/components/TabBar.vue'
import Icon from '@/components/Icon.vue'

const resume = ref(null)

// 头像完整 URL：avatar 形如 "/api/uploads/xxx.jpg"，需拼接 host
const avatarUrl = computed(() => {
  const a = resume.value?.avatar
  if (!a) return ''
  if (/^https?:\/\//i.test(a)) return a
  return BASE_URL.replace(/\/api$/, '') + a
})

// 使用 computed 进行数据解析
const skills = computed(() => {
  if (!resume.value?.skills) return []
  try { return JSON.parse(resume.value.skills) } catch { return [] }
})

const workExperience = computed(() => {
  if (!resume.value?.workExperience) return []
  try { return JSON.parse(resume.value.workExperience) } catch { return [] }
})

const projects = computed(() => {
  if (!resume.value?.projects) return []
  try { return JSON.parse(resume.value.projects) } catch { return [] }
})

const education = computed(() => {
  if (!resume.value?.education) return []
  try { return JSON.parse(resume.value.education) } catch { return [] }
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

// 根据 level 文案返回技能标签颜色类名
// 精通/熟练 → 主色，掌握/良好 → 辅色，其他 → 强调色
const getSkillColor = (level) => {
  const l = (level || '').toString()
  if (l.includes('精通') || l.includes('熟练')) return 'skill-tag--primary'
  if (l.includes('掌握') || l.includes('良好')) return 'skill-tag--secondary'
  return 'skill-tag--accent'
}

// onLoad 直接使用，无需导入
onLoad(async () => {
  try {
    const res = await api.getResume()
    resume.value = res.data || null
  } catch (e) {
    uni.showToast({ title: '简历加载失败', icon: 'none' })
  }
})
</script>

<style lang="scss" scoped>
.resume-page {
  background: $color-bg;
  min-height: 100vh;
  padding-bottom: calc(56px + env(safe-area-inset-bottom) + 12px);
}

/* === Hero 卡片 === */
.hero-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 32px 20px 24px;
  background: linear-gradient(135deg, $color-primary 0%, $color-secondary 100%);
  border-radius: 0 0 24px 24px;
  color: #fff;

  .avatar {
    width: 80px;
    height: 80px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.25);
    border: 3px solid #fff;
    box-shadow: 0 4px 16px rgba(15, 23, 42, 0.15);
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
    flex-shrink: 0;

    .avatar-img {
      width: 100%;
      height: 100%;
      border-radius: 50%;
    }

    .avatar-letter {
      font-size: 32px;
      font-weight: 700;
      color: #fff;
    }
  }

  .name {
    margin-top: 12px;
    font-size: 24px;
    font-weight: 700;
    color: #fff;
  }

  .job-title {
    margin-top: 4px;
    font-size: 14px;
    color: rgba(255, 255, 255, 0.85);
  }

  .contact {
    margin-top: 12px;
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
    gap: 8px;

    .contact-item {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      font-size: 12px;
      color: rgba(255, 255, 255, 0.9);
    }
  }
}

/* === 通用卡片 === */
.section {
  background: $color-bg-card;
  margin: 12px 16px;
  border-radius: $radius-xl;
  padding: 20px 16px;
  box-shadow: $shadow-card;

  .section-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;
    color: $color-text;
    margin-bottom: 16px;
    padding-bottom: 12px;
    border-bottom: 2px solid $color-primary;
  }

  .section-content {
    font-size: 14px;
    color: #475569;
    line-height: 1.8;
  }
}

/* === 技能标签云 === */
.skills-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;

  .skill-tag {
    padding: 6px 14px;
    border-radius: $radius-full;
    font-size: 13px;

    &.skill-tag--primary {
      background: #EEF2FF;
      color: $color-primary;
    }

    &.skill-tag--secondary {
      background: #ECFEFF;
      color: $color-secondary;
    }

    &.skill-tag--accent {
      background: #F3E8FF;
      color: $color-accent;
    }
  }
}

/* === 工作经历 / 教育背景（卡片化时间线） === */
.timeline {
  .timeline-item {
    background: #F8FAFC;
    border-radius: $radius-lg;
    padding: 16px;
    margin-bottom: 12px;
    border-left: 3px solid $color-primary;

    &:last-child {
      margin-bottom: 0;
    }

    .timeline-title {
      display: block;
      font-size: 15px;
      font-weight: 600;
      color: $color-text;
    }

    .timeline-date {
      display: flex;
      align-items: center;
      gap: 4px;
      margin-top: 4px;
      font-size: 12px;
      color: $color-text-tertiary;
    }

    .timeline-sub {
      display: block;
      margin-top: 4px;
      font-size: 13px;
      font-weight: 500;
      color: $color-primary;
    }

    .timeline-desc {
      display: block;
      margin-top: 8px;
      font-size: 13px;
      color: #475569;
      line-height: 1.6;
    }
  }
}

/* === 项目经验卡片 === */
.projects-list {
  .project-item {
    background: $color-bg-card;
    border: 1px solid $color-border;
    border-radius: $radius-lg;
    padding: 16px;
    margin-bottom: 12px;

    &:last-child {
      margin-bottom: 0;
    }

    .project-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 8px;

      .project-name {
        flex: 1;
        font-size: 15px;
        font-weight: 600;
        color: $color-text;
      }

      .project-date {
        font-size: 12px;
        color: $color-text-tertiary;
        flex-shrink: 0;
      }
    }

    .project-role {
      display: block;
      margin-top: 4px;
      font-size: 13px;
      font-weight: 500;
      color: $color-primary;
    }

    .project-desc {
      display: block;
      margin-top: 8px;
      font-size: 13px;
      color: #475569;
      line-height: 1.6;
    }

    .project-tags {
      display: flex;
      flex-wrap: wrap;
      gap: 6px;
      margin-top: 12px;

      .tag {
        background: #EEF2FF;
        color: $color-primary;
        padding: 3px 10px;
        border-radius: $radius-full;
        font-size: 11px;
      }
    }
  }
}

/* === 空状态 === */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  color: $color-text-tertiary;
  font-size: 14px;
}

/* === 基本信息 grid === */
.info-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;

  .info-item {
    display: flex;
    flex-direction: column;
    gap: 2px;
    background: #F8FAFC;
    border-radius: $radius-md;
    padding: 8px 12px;
    min-width: 80px;

    .info-label {
      font-size: 11px;
      color: $color-text-tertiary;
    }

    .info-value {
      font-size: 13px;
      font-weight: 500;
      color: $color-text;
    }
  }
}
</style>
