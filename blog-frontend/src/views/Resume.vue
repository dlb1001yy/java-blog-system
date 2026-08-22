<template>
  <div class="resume-page" v-if="resume">
    <div class="container">
    <!-- 风格切换工具栏 -->
    <div class="preview-toolbar">
      <el-radio-group v-model="previewStyle">
        <el-radio-button value="modern">现代极简</el-radio-button>
        <el-radio-button value="classic">经典衬线</el-radio-button>
        <el-radio-button value="sidebar">双栏侧边栏</el-radio-button>
        <el-radio-button value="bold">粗体页眉</el-radio-button>
      </el-radio-group>
      <el-button type="primary" :icon="Printer" @click="downloadResume">打印 / 导出 PDF</el-button>
    </div>

    <div class="resume-paper">
      <ResumePreview
        :resume="resume"
        :skills="skills"
        :works="workExperience"
        :projects="projects"
        :educations="education"
        :certificates="certificates"
        :style="previewStyle"
      />
    </div>
    </div>
  </div>
  <el-empty v-else-if="expired" description="链接已失效或不存在" />
  <el-empty v-else description="暂无简历信息" />
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Printer } from '@element-plus/icons-vue'
import articleApi from '@/api/article'
import resumeApi from '@/api/resume'
import ResumePreview from '@/components/ResumePreview.vue'

const route = useRoute()
const resume = ref(null)
const isShareMode = computed(() => !!route.params.token)
const expired = ref(false)
const previewStyle = ref('modern')

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
.preview-toolbar { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px; background: #fff; border: 1px solid var(--border-color, #e4e7ed); border-radius: 10px; padding: 12px 16px; margin-bottom: 20px; }
.resume-paper { background: #fff; border-radius: 6px; box-shadow: 0 2px 16px rgba(0,0,0,.1); overflow: hidden; min-height: 500px; }
@media (max-width: 768px) { .preview-toolbar { justify-content: center; } }
</style>

<style>
/* 打印样式（非 scoped，需隐藏布局级组件） */
@media print {
  .app-header,
  .app-footer,
  .player-bar,
  .back-to-top,
  .preview-toolbar {
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