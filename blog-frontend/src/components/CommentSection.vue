<template>
  <div class="comment-section card">
    <h3 class="section-title">评论区</h3>
    
    <div class="comment-form">
      <el-input v-model="form.nickname" placeholder="您的昵称" class="input-nickname" />
      <el-input v-model="form.email" placeholder="您的邮箱 (选填)" class="input-email" />
      <el-input 
        v-model="form.content" 
        type="textarea" 
        :rows="3" 
        placeholder="说点什么吧..." 
      />
      <div class="form-footer">
        <el-button type="primary" @click="submitComment">发表评论</el-button>
      </div>
    </div>

    <div class="comment-list">
      <div v-for="comment in comments" :key="comment.id" class="comment-item">
        <el-avatar :size="36">{{ comment.nickname.charAt(0) }}</el-avatar>
        <div class="comment-content">
          <div class="comment-header">
            <span class="comment-name">{{ comment.nickname }}</span>
            <span class="comment-date">{{ formatDate(comment.createTime) }}</span>
          </div>
          <p class="comment-text">{{ comment.content }}</p>
        </div>
      </div>
      <el-empty v-if="comments.length === 0" description="暂无评论，快来抢沙发吧！" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import articleApi from '@/api/article'

const props = defineProps({
  articleId: { type: [String, Number], required: true }
})

const comments = ref([])
const form = reactive({ nickname: '', email: '', content: '' })

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}

const fetchComments = async () => {
  const res = await articleApi.getComments(props.articleId)
  comments.value = res.data || []
}

const submitComment = async () => {
  if (!form.nickname.trim()) return ElMessage.warning('请输入昵称')
  if (!form.content.trim()) return ElMessage.warning('请输入评论内容')
  
  await articleApi.addComment({
    articleId: props.articleId,
    nickname: form.nickname,
    email: form.email,
    content: form.content
  })
  
  ElMessage.success('评论成功，等待审核')
  form.content = ''
  fetchComments()
}

onMounted(() => fetchComments())
</script>

<style scoped>
.section-title { margin-bottom: 20px; font-size: 18px; font-weight: 600; }
.comment-form { margin-bottom: 32px; }
.input-nickname, .input-email { margin-bottom: 12px; width: 50%; }
.form-footer { margin-top: 12px; text-align: right; }
.comment-list { display: flex; flex-direction: column; gap: 20px; }
.comment-item { display: flex; gap: 12px; }
.comment-content { flex: 1; }
.comment-header { display: flex; align-items: center; gap: 12px; margin-bottom: 4px; }
.comment-name { font-weight: 600; color: var(--text-primary); }
.comment-date { font-size: 12px; color: var(--text-secondary); }
.comment-text { color: var(--text-regular); line-height: 1.6; }
</style>