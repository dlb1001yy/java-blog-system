<template>
  <div class="message-board">
    <div class="container">
      <div class="card">
        <h2 class="page-title">留言板</h2>
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

const form = reactive({ nickname: '', email: '', content: '' })

const submitMessage = async () => {
  if (!form.nickname.trim()) return ElMessage.warning('请输入昵称')
  if (!form.content.trim()) return ElMessage.warning('请输入留言内容')
  await articleApi.addMessage(form)
  ElMessage.success('留言成功，等待审核')
  form.nickname = ''; form.email = ''; form.content = ''
}
</script>

<style scoped>
.page-title { margin-bottom: 12px; font-size: 20px; font-weight: 600; }
.desc { color: var(--text-secondary); margin-bottom: 24px; }
.message-form { max-width: 800px; }
</style>