<template>
  <view :class="['message-page', isDark ? 'theme-dark' : '']">
    <!-- 留言表单卡片：对标 Web 端 MessageBoard -->
    <view class="card">
      <text class="page-title">留言板</text>
      <text class="desc">有什么想对我说的，就在这里留言吧！</text>

      <view class="form">
        <!-- 昵称（必填） -->
        <view class="form-item">
          <view class="label-row">
            <text class="label">昵称</text>
            <text class="required">*</text>
          </view>
          <input
            v-model="form.nickname"
            placeholder="请输入您的昵称"
            class="input"
            placeholder-class="placeholder"
          />
        </view>

        <!-- 邮箱（选填） -->
        <view class="form-item">
          <view class="label-row">
            <text class="label">邮箱</text>
            <text class="optional">选填</text>
          </view>
          <input
            v-model="form.email"
            type="text"
            placeholder="请输入您的邮箱"
            class="input"
            placeholder-class="placeholder"
          />
        </view>

        <!-- 内容（必填，500 字） -->
        <view class="form-item">
          <view class="label-row">
            <text class="label">内容</text>
            <text class="required">*</text>
          </view>
          <view class="textarea-wrap">
            <textarea
              v-model="form.content"
              :maxlength="500"
              placeholder="请输入留言内容..."
              class="textarea"
              placeholder-class="placeholder"
            />
            <text class="count">{{ form.content.length }}/500</text>
          </view>
        </view>

        <!-- 提交按钮 -->
        <button
          class="submit-btn"
          :loading="loading"
          :disabled="loading"
          @click="handleSubmit"
        >
          {{ loading ? '提交中...' : '提交留言' }}
        </button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import api from '@/common/api.js'
import { isDark, applyNavBarTheme } from '@/common/theme.js'

const form = ref({
  nickname: '',
  email: '',
  content: ''
})
const loading = ref(false)

onShow(() => {
  applyNavBarTheme()
})

const handleSubmit = async () => {
  if (!form.value.nickname.trim()) {
    return uni.showToast({ title: '请输入昵称', icon: 'none' })
  }
  if (!form.value.content.trim()) {
    return uni.showToast({ title: '请输入留言内容', icon: 'none' })
  }

  loading.value = true
  try {
    await api.sendMessage({
      nickname: form.value.nickname.trim(),
      email: form.value.email.trim(),
      content: form.value.content.trim()
    })
    uni.showToast({ title: '留言成功，等待审核', icon: 'success' })
    form.value.nickname = ''
    form.value.email = ''
    form.value.content = ''
  } catch (error) {
    // 网络请求错误已在 request.js 中统一处理
    console.error('Send message failed', error)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
/* 页面容器 */
.message-page {
  min-height: 100vh;
  background: var(--app-bg, #F1F5F9);
  padding: 16px 16px calc(24px + env(safe-area-inset-bottom));
}

/* 表单卡片 */
.card {
  background: var(--app-bg-card, #FFFFFF);
  border-radius: $radius-xl;
  padding: 20px;
  box-shadow: $shadow-card;
}

.page-title {
  display: block;
  font-size: 20px;
  font-weight: 600;
  color: var(--app-text, #0F172A);
}

.desc {
  display: block;
  margin-top: 8px;
  margin-bottom: 20px;
  font-size: 13px;
  color: var(--app-text-secondary, #64748B);
}

/* 表单 */
.form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* 标签行：名称 + 必填/选填标记 */
.label-row {
  display: flex;
  align-items: center;
  gap: 4px;
}

.label {
  font-size: 14px;
  font-weight: 500;
  color: var(--app-text, #0F172A);
}

/* 必填红星 */
.required {
  color: $color-danger;
  font-size: 14px;
}

/* 选填灰字 */
.optional {
  font-size: 12px;
  color: var(--app-text-tertiary, #94A3B8);
}

/* 输入框 */
.input {
  height: 44px;
  padding: 0 14px;
  background: var(--app-bg, #F1F5F9);
  border-radius: $radius-lg;
  font-size: 14px;
  color: var(--app-text, #0F172A);
}

/* 多行文本容器 */
.textarea-wrap {
  position: relative;
  background: var(--app-bg, #F1F5F9);
  border-radius: $radius-lg;
  padding: 12px 14px 26px;
}

.textarea {
  width: 100%;
  min-height: 110px;
  font-size: 14px;
  line-height: 1.6;
  color: var(--app-text, #0F172A);
}

/* 字数统计：右下角 */
.count {
  position: absolute;
  right: 14px;
  bottom: 8px;
  font-size: 11px;
  color: var(--app-text-tertiary, #94A3B8);
}

.placeholder {
  color: var(--app-text-tertiary, #94A3B8);
}

/* 提交按钮 */
.submit-btn {
  height: 46px;
  background: linear-gradient(135deg, $color-primary 0%, $color-primary-light 100%);
  color: #fff;
  border-radius: $radius-lg;
  font-size: 15px;
  font-weight: 600;
  border: none;
  margin-top: 4px;
  box-shadow: 0 8px 16px rgba(79, 70, 229, 0.3);
  transition: transform 0.1s;

  &:active {
    transform: scale(0.98);
  }

  &::after {
    border: none;
  }
}
</style>
