<template>
  <!-- 注册页：多层径向渐变 mesh 背景 + 玻璃拟态卡片（风格对齐登录页） -->
  <view :class="['register-container', isDark ? 'theme-dark' : '']">
    <view class="register-main">
      <view class="register-card">
        <!-- 头部：标题 + 副标题 -->
        <view class="header">
          <text class="title">创建账号</text>
          <text class="subtitle">加入 Java码农笔记</text>
        </view>

        <!-- 表单 -->
        <view class="form">
          <!-- 用户名 -->
          <view class="input-item">
            <Icon name="user" :size="18" color="#94A3B8" class="input-icon" />
            <input
              v-model="form.username"
              placeholder="请输入用户名"
              class="input"
              placeholder-class="placeholder"
            />
          </view>

          <!-- 邮箱 -->
          <view class="input-item">
            <Icon name="mail" :size="18" color="#94A3B8" class="input-icon" />
            <input
              v-model="form.email"
              type="text"
              placeholder="请输入邮箱"
              class="input"
              placeholder-class="placeholder"
            />
          </view>

          <!-- 密码（内联锁 SVG，避免修改 Icon.vue） -->
          <view class="input-item">
            <view class="input-icon lock-icon">
              <svg
                viewBox="0 0 24 24"
                width="18"
                height="18"
                fill="none"
                stroke="#94A3B8"
                stroke-width="1.8"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
                <path d="M7 11V7a5 5 0 0 1 10 0v4" />
              </svg>
            </view>
            <input
              v-model="form.password"
              type="password"
              placeholder="密码（8位以上，含字母和数字）"
              class="input"
              placeholder-class="placeholder"
            />
          </view>

          <!-- 确认密码 -->
          <view class="input-item">
            <view class="input-icon lock-icon">
              <svg
                viewBox="0 0 24 24"
                width="18"
                height="18"
                fill="none"
                stroke="#94A3B8"
                stroke-width="1.8"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
                <path d="M7 11V7a5 5 0 0 1 10 0v4" />
              </svg>
            </view>
            <input
              v-model="form.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              class="input"
              placeholder-class="placeholder"
            />
          </view>

          <!-- 注册按钮 -->
          <button
            class="register-btn"
            :loading="loading"
            :disabled="loading"
            @click="handleRegister"
          >
            {{ loading ? '注册中...' : '注 册' }}
          </button>

          <!-- 底部登录引导 -->
          <view class="tips">
            <text class="tip-text">已有账号？</text>
            <text class="tip-link" @click="goLogin">去登录</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import api from '@/common/api.js'
import { isDark, applyNavBarTheme } from '@/common/theme.js'
import Icon from '@/components/Icon.vue'

// 邮箱格式校验正则
const EMAIL_RE = /^[\w.+-]+@[\w-]+(\.[\w-]+)+$/
// 密码强度：至少 8 位且同时包含字母与数字（与前端 Web 端一致）
const PASSWORD_RE = /^(?=.*[A-Za-z])(?=.*\d).{8,}$/

const form = ref({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})
const loading = ref(false)

onShow(() => {
  applyNavBarTheme()
})

// 返回登录页（登录页在页面栈中有上一页，普通返回即可）
const goLogin = () => {
  uni.navigateBack({
    fail: () => uni.redirectTo({ url: '/subpkg/pages/mine/login' })
  })
}

const handleRegister = async () => {
  const { username, email, password, confirmPassword } = form.value

  if (!username || !username.trim()) {
    return uni.showToast({ title: '请输入用户名', icon: 'none' })
  }
  if (!EMAIL_RE.test(email)) {
    return uni.showToast({ title: '邮箱格式不正确', icon: 'none' })
  }
  if (!PASSWORD_RE.test(password)) {
    return uni.showToast({ title: '密码需8位以上且包含字母和数字', icon: 'none' })
  }
  if (password !== confirmPassword) {
    return uni.showToast({ title: '两次输入的密码不一致', icon: 'none' })
  }

  loading.value = true
  try {
    await api.register({ username: username.trim(), email, password, confirmPassword })
    uni.showToast({ title: '注册成功，请登录', icon: 'success' })
    // 1s 后跳登录页并预填用户名；redirectTo 关闭注册页避免栈中残留
    setTimeout(() => {
      uni.redirectTo({
        url: '/subpkg/pages/mine/login?username=' + encodeURIComponent(username.trim()),
        fail: () => uni.navigateBack()
      })
    }, 1000)
  } catch (error) {
    // 网络请求错误已在 request.js 中统一处理
    console.error('Register failed', error)
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
// 注册页容器：多层径向渐变 mesh 背景（与登录页一致）
.register-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #1E1B4B;
  background-image:
    radial-gradient(at 0% 0%, $color-primary 0%, transparent 50%),
    radial-gradient(at 100% 0%, $color-secondary 0%, transparent 50%),
    radial-gradient(at 50% 100%, $color-accent 0%, transparent 50%);
}

// 卡片区域：垂直居中
.register-main {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

// 玻璃拟态注册卡片
.register-card {
  width: 100%;
  max-width: 360px;
  background: var(--app-bg-card, #FFFFFF);
  -webkit-backdrop-filter: blur(20px);
  backdrop-filter: blur(20px);
  border: 1px solid var(--app-border, #E2E8F0);
  border-radius: 24px;
  padding: 40px 28px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
}

// 头部
.header {
  text-align: center;
  margin-bottom: 32px;
}

.title {
  display: block;
  font-size: 26px;
  font-weight: 700;
  color: var(--app-text, #0F172A);
}

.subtitle {
  display: block;
  margin-top: 6px;
  font-size: 13px;
  color: var(--app-text-secondary, #64748B);
}

// 表单
.form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

// 输入项容器
.input-item {
  display: flex;
  align-items: center;
  background: var(--app-bg, #F1F5F9);
  border-radius: $radius-lg;
  padding: 0 14px;
  height: 48px;
  border: 2px solid transparent;
  transition: all 0.2s;

  &:focus-within {
    background: var(--app-bg-card, #FFFFFF);
    border-color: $color-primary;
    box-shadow: 0 0 0 4px rgba(79, 70, 229, 0.1);
  }
}

// 左侧图标
.input-icon {
  margin-right: 10px;
}

.lock-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
}

.input {
  flex: 1;
  font-size: 15px;
  color: var(--app-text, #0F172A);
  height: 100%;
}

.placeholder {
  color: var(--app-text-tertiary, #94A3B8);
}

// 注册按钮
.register-btn {
  height: 48px;
  background: linear-gradient(135deg, $color-primary 0%, $color-primary-light 100%);
  color: #fff;
  border-radius: $radius-lg;
  font-size: 16px;
  font-weight: 600;
  border: none;
  margin-top: 8px;
  box-shadow: 0 8px 16px rgba(79, 70, 229, 0.3);
  transition: transform 0.1s;

  &:active {
    transform: scale(0.98);
  }

  // 移除 uni-app button 默认边框
  &::after {
    border: none;
  }
}

// 底部登录引导
.tips {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 20px;
}

.tip-text {
  font-size: 12px;
  color: var(--app-text-secondary, #64748B);
}

.tip-link {
  font-size: 12px;
  color: $color-primary;
  font-weight: 500;
}
</style>
