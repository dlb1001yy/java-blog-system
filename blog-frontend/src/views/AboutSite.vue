<template>
  <div class="about-site">
    <div class="container">
      <!-- 上方区块：关于本站 -->
      <div class="card">
        <h1 class="page-title">关于本站</h1>
        <div class="content markdown-body">
          <p>欢迎来到 <strong>Java码农笔记</strong>！</p>
          <p>这是一个 Java 技术学习分享一体化平台，集<strong>博客、面试刷题、在线考试、音乐、简历</strong>五大模块于一体。在这里，我会记录自己在 Java 学习之路上的心得体会，分享 Spring、数据库、前端技术以及 DevOps 等方面的知识。</p>
          <p>平台覆盖「学、练、测、展」的完整学习闭环：通过博客输入知识，借助面试题库练习巩固，再以在线考试检验掌握程度，并支持在线制作简历一键生成分享链接，学有余力时还能在音乐模块放松身心。</p>

          <h2>功能模块</h2>
          <div class="module-list">
            <div v-for="m in modules" :key="m.path" class="module-item" @click="router.push(m.path)">
              <div class="module-name">{{ m.name }}</div>
              <div class="module-desc">{{ m.desc }}</div>
            </div>
          </div>

          <h2>技术栈</h2>
          <div class="tech-section">
            <div class="tech-row">
              <span class="tech-label">前端</span>
              <div class="tech-tags">
                <span class="tech-tag">Vue 3</span><span class="tech-tag">Element Plus</span><span class="tech-tag">Pinia</span><span class="tech-tag">Vue Router</span><span class="tech-tag">Axios</span>
              </div>
            </div>
            <div class="tech-row">
              <span class="tech-label">后端</span>
              <div class="tech-tags">
                <span class="tech-tag">Spring Boot 3</span><span class="tech-tag">Spring Security</span><span class="tech-tag">MyBatis-Plus</span><span class="tech-tag">MySQL</span><span class="tech-tag">Redis</span><span class="tech-tag">Elasticsearch</span><span class="tech-tag">MinIO / OSS</span>
              </div>
            </div>
            <div class="tech-row">
              <span class="tech-label">部署</span>
              <div class="tech-tags">
                <span class="tech-tag">Docker</span><span class="tech-tag">Nginx</span><span class="tech-tag">Prometheus</span>
              </div>
            </div>
          </div>

          <h2>联系方式</h2>
          <ul class="contact-list">
            <li>邮箱：1310471544@qq.com</li>
            <li>源码仓库：<a href="https://gitee.com/dlbyy/java-blog-system.git" target="_blank" rel="noopener">https://gitee.com/dlbyy/java-blog-system.git</a></li>
          </ul>
        </div>
      </div>

      <!-- 下方区块：留言板（复用 MessageBoard.vue 的发布表单逻辑） -->
      <div class="card">
        <h2 class="section-title">留言板</h2>
        <p class="desc">有什么想对我说的，就在这里留言吧！</p>
        <el-form :model="form" class="message-form">
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="昵称">
                <el-input v-model="form.nickname" placeholder="请输入您的昵称" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="邮箱">
                <el-input v-model="form.email" placeholder="请输入您的邮箱 (选填)" />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="内容">
            <el-input v-model="form.content" type="textarea" :rows="5" placeholder="请输入留言内容..." />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="submitMessage">提交留言</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import articleApi from '@/api/article'

const router = useRouter()

// 功能模块介绍（与首页 modules 口径一致）
const modules = [
  { name: '个人博客', desc: '记录学习笔记与技术心得，支持分类、标签、归档与全文搜索', path: '/articles' },
  { name: '面试刷题', desc: '精选面试题库，助力求职准备', path: '/interview' },
  { name: '在线考试', desc: '模拟真实考试环境，自动判卷，支持成绩查询', path: '/exam' },
  { name: '音乐放松', desc: '学习之余聆听音乐放松身心', path: '/music' },
  { name: '我的简历', desc: '在线制作个人简历，一键生成分享链接', path: '/profile/resume' }
]

// 留言表单（与 MessageBoard.vue 保持一致）
const form = reactive({ nickname: '', email: '', content: '' })

// 提交留言
const submitMessage = async () => {
  if (!form.nickname.trim()) return ElMessage.warning('请输入昵称')
  if (!form.content.trim()) return ElMessage.warning('请输入留言内容')
  await articleApi.addMessage(form)
  ElMessage.success('留言成功，等待审核')
  form.nickname = ''; form.email = ''; form.content = ''
}
</script>

<style scoped>
.page-title { margin-bottom: 24px; font-size: 24px; font-weight: 700; }
.section-title { margin-bottom: 12px; font-size: 20px; font-weight: 600; }
.content { font-size: 15px; line-height: 1.8; color: var(--text-regular); }
.content h2 { margin: 24px 0 12px; font-size: 18px; }
.content ul { padding-left: 20px; }
.desc { color: var(--text-secondary); margin-bottom: 24px; }
.message-form { max-width: 800px; }
.about-site .card + .card { margin-top: 20px; }

/* 功能模块网格 */
.module-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 12px;
}
.module-item {
  padding: 14px 16px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.25s;
}
.module-item:hover {
  border-color: var(--primary-color);
  background: rgba(64, 158, 255, 0.06);
  transform: translateY(-2px);
}
.module-name { font-size: 15px; font-weight: 600; color: var(--primary-color); margin-bottom: 6px; }
.module-desc { font-size: 13px; line-height: 1.6; color: var(--text-secondary); }

/* 技术栈 */
.tech-section { display: flex; flex-direction: column; gap: 10px; }
.tech-row { display: flex; align-items: flex-start; gap: 12px; }
.tech-label {
  flex-shrink: 0;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-regular);
  line-height: 26px;
}
.tech-tags { display: flex; flex-wrap: wrap; gap: 8px; }
.tech-tag {
  padding: 3px 10px;
  font-size: 12px;
  color: var(--text-regular);
  background: var(--bg-color);
  border-radius: 4px;
}

/* 联系方式 */
.contact-list a { color: var(--primary-color); text-decoration: none; }
.contact-list a:hover { text-decoration: underline; }

@media (max-width: 768px) {
  .module-list { grid-template-columns: 1fr; }
  .tech-row { flex-direction: column; gap: 6px; }
}
</style>
