<template>
  <!-- 登录页：多层径向渐变 mesh 背景 + 玻璃拟态卡片 -->
  <view :class="['login-container', isDark ? 'theme-dark' : '']">
    <!-- 顶部自定义导航栏：卡片底色，与登录卡片风格呼应 -->
    <NavBar title="登录" />
    <!-- 卡片区域：占满剩余空间并居中 -->
    <view class="login-main">
      <view class="login-card">
      <!-- 头部：标题 + 副标题 -->
      <view class="header">
        <text class="title">欢迎回来</text>
        <text class="subtitle">Java码农笔记</text>
      </view>

      <!-- 表单 -->
      <view class="form">
        <!-- 用户名输入项 -->
        <view class="input-item">
          <Icon name="user" :size="18" color="#94A3B8" class="input-icon" />
          <input
            v-model="form.username"
            placeholder="请输入用户名"
            class="input"
            placeholder-class="placeholder"
          />
        </view>

        <!-- 密码输入项（内联锁 SVG，避免修改 Icon.vue） -->
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
            placeholder="请输入密码"
            class="input"
            placeholder-class="placeholder"
          />
        </view>

        <!-- 验证码输入项：输入框 + 点击图片刷新 -->
        <view class="input-item">
          <input
            v-model="form.captchaCode"
            placeholder="请输入验证码"
            class="input"
            placeholder-class="placeholder"
          />
          <image
            class="captcha-image"
            :src="captchaImage"
            mode="aspectFill"
            @click="refreshCaptcha"
          />
        </view>

        <!-- 登录按钮 -->
        <button
          class="login-btn"
          :loading="loading"
          :disabled="loading"
          @click="handleLogin"
        >
          {{ loading ? '登录中...' : '登 录' }}
        </button>

        <!-- 默认账号提示 -->
        <view class="tips">
          <text class="tip-text">默认账号: admin / admin123</text>
        </view>
      </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { TOKEN_KEY, REFRESH_TOKEN_KEY } from '@/common/config.js'
import api, { getCaptcha } from '@/common/api.js'
import { isDark } from '@/common/theme.js'
import Icon from '@/components/Icon.vue'
import NavBar from '@/components/NavBar.vue'

const form = ref({
  username: 'admin',
  password: 'admin123',
  captchaId: '',
  captchaCode: ''
})
const loading = ref(false)
const captchaImage = ref('')

// 获取图形验证码（captchaId 一次性消费，失败后需刷新）
const refreshCaptcha = async () => {
  try {
    const res = await getCaptcha()
    form.value.captchaId = res.data.captchaId
    captchaImage.value = res.data.image
    form.value.captchaCode = ''
  } catch (e) {
    // 网络请求错误已在 request.js 中统一处理
  }
}

onLoad(() => {
  refreshCaptcha()
})

const handleLogin = async () => {
  if (!form.value.username || !form.value.password) {
    return uni.showToast({ title: '请输入账号和密码', icon: 'none' })
  }
  if (!form.value.captchaCode) {
    return uni.showToast({ title: '请输入验证码', icon: 'none' })
  }

  loading.value = true
  try {
    const res = await api.login(form.value)

    // 保存 Token
    uni.setStorageSync(TOKEN_KEY, res.data.accessToken)
    uni.setStorageSync(REFRESH_TOKEN_KEY, res.data.refreshToken)

    uni.showToast({ title: '登录成功', icon: 'success' })

    // 登录成功后的跳转逻辑
    setTimeout(() => {
      const pages = getCurrentPages()
      // 如果有上一页，返回上一页；否则跳转到首页
      if (pages.length > 1) {
        uni.navigateBack()
      } else {
        // 无原生 tabBar，首页用 reLaunch 打开（与自定义 TabBar 切换方式一致）
        uni.reLaunch({ url: '/pages/index/index' })
      }
    }, 1000)

  } catch (error) {
    // 网络请求错误已在 request.js 中统一处理，此处可留空
    console.error('Login failed', error)
    // 验证码已被一次性消费，失败后刷新
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
// 登录页容器：多层径向渐变 mesh 背景，底色深靛蓝让 mesh 更突出；顶部导航 + 下方内容区
.login-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #1E1B4B;
  background-image:
    radial-gradient(at 0% 0%, $color-primary 0%, transparent 50%),
    radial-gradient(at 100% 0%, $color-secondary 0%, transparent 50%),
    radial-gradient(at 50% 100%, $color-accent 0%, transparent 50%);
}

// 卡片区域：占满导航栏以外的剩余空间并居中
.login-main {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

// 玻璃拟态登录卡片：背景用主题卡片色（高不透明度），确保暗色下可读
.login-card {
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

  // 聚焦时高亮边框 + 柔光
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

// 图形验证码图片
.captcha-image {
  width: 260rpx;
  height: 80rpx;
  margin-left: 10px;
  border-radius: $radius-lg;
}

// 登录按钮
.login-btn {
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

// 默认账号提示
.tips {
  text-align: center;
  margin-top: 20px;
}

.tip-text {
  font-size: 12px;
  color: var(--app-text-secondary, #64748B);
}
</style>
