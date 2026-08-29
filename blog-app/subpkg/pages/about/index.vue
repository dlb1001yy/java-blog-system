<template>
  <view :class="['about-page', isDark ? 'theme-dark' : '']">
    <!-- ① 站点介绍卡 -->
    <view class="card">
      <text class="card-title">关于本站</text>
      <view class="intro">
        <view class="para">
          <text class="strong">欢迎来到 Java码农笔记！</text>
        </view>
        <view class="para">
          <text class="txt">这是一个 Java 技术学习分享一体化平台，集</text>
          <text class="strong">博客、面试刷题、在线考试、音乐、简历</text>
          <text class="txt">五大模块于一体。在这里，我会记录自己在 Java 学习之路上的心得体会，分享 Spring、数据库、前端技术以及 DevOps 等方面的知识。</text>
        </view>
        <view class="para">
          <text class="txt">平台覆盖「学、练、测、展」的完整学习闭环：通过博客输入知识，借助面试题库练习巩固，再以在线考试检验掌握程度，并支持在线制作简历一键生成分享链接，学有余力时还能在音乐模块放松身心。</text>
        </view>
      </view>

      <!-- ② 功能模块网格（可点击跳转） -->
      <text class="section-title">功能模块</text>
      <view class="module-grid">
        <view
          v-for="m in modules"
          :key="m.path"
          class="module-item"
          @click="goPage(m.path)"
        >
          <view class="module-head">
            <view class="module-icon">
              <Icon :name="m.icon" :size="18" />
            </view>
            <text class="module-name">{{ m.name }}</text>
          </view>
          <text class="module-desc">{{ m.desc }}</text>
        </view>
      </view>

      <!-- ③ 技术栈 -->
      <text class="section-title">技术栈</text>
      <view class="tech-section">
        <view v-for="g in techStack" :key="g.label" class="tech-row">
          <text class="tech-label">{{ g.label }}</text>
          <view class="tech-tags">
            <text v-for="t in g.tags" :key="t" class="tech-tag">{{ t }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- ④ 联系方式卡 -->
    <view class="card">
      <text class="card-title">联系方式</text>
      <view class="contact-item" @click="copyText(CONTACT.email)">
        <view class="contact-icon">
          <Icon name="mail" :size="18" />
        </view>
        <view class="contact-main">
          <text class="contact-label">邮箱</text>
          <text class="contact-value">{{ CONTACT.email }}</text>
        </view>
        <text class="contact-action">复制</text>
      </view>
      <view class="contact-item" @click="openGitee">
        <view class="contact-icon">
          <Icon name="location" :size="18" />
        </view>
        <view class="contact-main">
          <text class="contact-label">源码仓库（Gitee）</text>
          <text class="contact-value link">{{ CONTACT.gitee }}</text>
        </view>
        <text class="contact-action">复制</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { onShow } from '@dcloudio/uni-app'
import { isDark, applyNavBarTheme } from '@/common/theme.js'
import Icon from '@/components/Icon.vue'

// 联系方式（文案取自 Web 端 AboutSite.vue）
const CONTACT = {
  email: '1310471544@qq.com',
  gitee: 'https://gitee.com/dlbyy/java-blog-system.git'
}

// 功能模块（与 Web 端 AboutSite.vue 口径一致，路径换成 uni-app 页面）
const modules = [
  {
    name: '个人博客',
    desc: '记录学习笔记与技术心得，支持分类、标签、归档与全文搜索',
    path: '/subpkg-article/pages/list',
    icon: 'document'
  },
  {
    name: '在线考试',
    desc: '模拟真实考试环境，自动判卷，支持成绩查询',
    path: '/subpkg-study/pages/exam/index',
    icon: 'trophy'
  },
  {
    name: '面试刷题',
    desc: '精选面试题库，助力求职准备',
    path: '/subpkg-study/pages/interview/index',
    icon: 'book'
  },
  {
    name: '音乐放松',
    desc: '学习之余聆听音乐放松身心',
    path: '/subpkg-music/pages/index',
    icon: 'music'
  }
]

// 技术栈三组（具体技术名取自 Web 端 AboutSite.vue）
const techStack = [
  {
    label: '前端',
    tags: ['Vue 3', 'Element Plus', 'Pinia', 'Vue Router', 'Axios', 'uni-app']
  },
  {
    label: '后端',
    tags: ['Spring Boot 3', 'Spring Security', 'MyBatis-Plus', 'MySQL', 'Redis', 'Elasticsearch', 'MinIO / OSS']
  },
  {
    label: '部署',
    tags: ['Docker', 'Nginx', 'Prometheus']
  }
]

onShow(() => {
  applyNavBarTheme()
})

// 跳转站内功能模块页
const goPage = (url) => {
  uni.navigateTo({ url })
}

// 复制文本到剪贴板并提示
const copyText = (text) => {
  uni.setClipboardData({
    data: text,
    success: () => {
      uni.showToast({ title: '已复制', icon: 'success' })
    }
  })
}

// Gitee 仓库：H5 新开窗口，其余端（App/小程序）复制链接后手动打开
const openGitee = () => {
  // #ifdef H5
  window.open(CONTACT.gitee, '_blank')
  return
  // #endif
  // #ifndef H5
  copyText(CONTACT.gitee)
  // #endif
}
</script>

<style lang="scss" scoped>
/* 页面容器 */
.about-page {
  min-height: 100vh;
  background: var(--app-bg, #F1F5F9);
  padding: 16px 16px calc(24px + env(safe-area-inset-bottom));
}

/* 通用卡片 */
.card {
  background: var(--app-bg-card, #FFFFFF);
  border-radius: $radius-xl;
  padding: 20px;
  box-shadow: $shadow-card;
  margin-bottom: 16px;
}

.card-title {
  display: block;
  font-size: 18px;
  font-weight: 700;
  color: var(--app-text, #0F172A);
  margin-bottom: 14px;
}

/* 站点介绍段落 */
.intro {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.para {
  font-size: 14px;
  line-height: 1.8;
}

.txt {
  font-size: 14px;
  line-height: 1.8;
  color: var(--app-text, #0F172A);
}

.strong {
  font-size: 14px;
  line-height: 1.8;
  font-weight: 600;
  color: var(--app-text, #0F172A);
}

/* 区块小标题 */
.section-title {
  display: block;
  margin: 22px 0 12px;
  font-size: 16px;
  font-weight: 600;
  color: var(--app-text, #0F172A);
}

/* 功能模块网格：两列 */
.module-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.module-item {
  padding: 14px;
  border: 1px solid var(--app-border, #E2E8F0);
  border-radius: $radius-lg;
  transition: transform 0.15s ease, opacity 0.15s ease;

  &:active {
    transform: scale(0.97);
    opacity: 0.9;
  }
}

.module-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

/* 模块图标容器：低透明度主色底 */
.module-icon {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  background: rgba(79, 70, 229, 0.12);
  color: $color-primary;
  display: flex;
  align-items: center;
  justify-content: center;
}

.module-name {
  font-size: 14px;
  font-weight: 600;
  color: $color-primary;
}

.module-desc {
  font-size: 12px;
  line-height: 1.6;
  color: var(--app-text-secondary, #64748B);
}

/* 技术栈 */
.tech-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.tech-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.tech-label {
  flex-shrink: 0;
  width: 36px;
  font-size: 13px;
  font-weight: 600;
  color: var(--app-text, #0F172A);
  line-height: 24px;
}

.tech-tags {
  flex: 1;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tech-tag {
  padding: 3px 10px;
  font-size: 12px;
  color: var(--app-text, #0F172A);
  background: var(--app-bg, #F1F5F9);
  border-radius: $radius-sm;
}

/* 联系方式条目 */
.contact-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid var(--app-divider, #F1F5F9);
  transition: transform 0.15s ease, opacity 0.15s ease;

  &:active {
    transform: scale(0.98);
    opacity: 0.9;
  }

  &:last-child {
    border-bottom: none;
    padding-bottom: 0;
  }

  &:first-of-type {
    padding-top: 0;
  }
}

.contact-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: rgba(79, 70, 229, 0.12);
  color: $color-primary;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
}

.contact-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.contact-label {
  font-size: 13px;
  font-weight: 500;
  color: var(--app-text, #0F172A);
}

.contact-value {
  font-size: 12px;
  color: var(--app-text-secondary, #64748B);
  word-break: break-all;
}

.contact-value.link {
  color: $color-primary;
}

.contact-action {
  flex-shrink: 0;
  margin-left: 8px;
  font-size: 12px;
  color: var(--app-text-tertiary, #94A3B8);
}
</style>
