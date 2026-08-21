<template>
  <div class="about-site">
    <div class="container">
      <!-- 上方区块：关于我（复用 About.vue 的内容与样式） -->
      <div class="card">
        <h1 class="page-title">关于本站</h1>
        <div class="content markdown-body">
          <p>欢迎来到 <strong>Java码农笔记</strong>！</p>
          <p>这是一个专注于Java技术分享的个人博客。在这里，我会记录自己在Java学习之路上的心得体会，分享Spring、数据库、前端技术以及DevOps等方面的知识。</p>
          <h2>联系方式</h2>
          <ul>
            <li>邮箱: admin@javalog.com</li>
            <li>GitHub: github.com/javalog</li>
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
import { ElMessage } from 'element-plus'
import articleApi from '@/api/article'

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
</style>
