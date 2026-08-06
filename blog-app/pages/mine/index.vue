<template>
  <view class="mine-page">
    <!-- Hero 区：渐变背景 + 用户信息/登录入口 -->
    <view class="hero">
      <!-- 未登录：白色描边登录按钮 -->
      <view v-if="!isLogin" class="login-entry" @click="goLogin">
        点击登录
      </view>
      <!-- 已登录：头像 + 昵称 + 简介 -->
      <view v-else class="hero-user">
        <image
          class="avatar"
          :src="userInfo.avatar || '/static/default-avatar.png'"
          mode="aspectFill"
        />
        <text class="nickname">{{ userInfo.nickname || '管理员' }}</text>
        <text
          class="bio"
          v-if="userInfo.intro || userInfo.bio"
        >{{ userInfo.intro || userInfo.bio }}</text>
      </view>
    </view>

    <!-- 统计数据网格：上浮到 hero 边缘，未登录也展示公开统计 -->
    <view class="stats-card">
      <view class="stat-item">
        <text class="stat-num">{{ formatCount(stats.articleCount) }}</text>
        <text class="stat-label">文章数</text>
      </view>
      <view class="stat-item">
        <text class="stat-num">{{ formatCount(stats.viewCount) }}</text>
        <text class="stat-label">浏览数</text>
      </view>
    </view>

    <!-- 菜单列表 -->
    <view class="menu-list">
      <view class="menu-item" @click="goPage('/pages/resume/index')">
        <view class="menu-icon">
          <Icon name="document" :size="18" />
        </view>
        <text class="menu-text">我的简历</text>
        <Icon name="chevron-right" :size="18" color="#CBD5E1" />
      </view>
      <view class="menu-item" @click="showTodo">
        <view class="menu-icon">
          <Icon name="mail" :size="18" />
        </view>
        <text class="menu-text">留言反馈</text>
        <Icon name="chevron-right" :size="18" color="#CBD5E1" />
      </view>
      <view class="menu-item" @click="showTodo">
        <view class="menu-icon">
          <Icon name="user" :size="18" />
        </view>
        <text class="menu-text">关于我们</text>
        <Icon name="chevron-right" :size="18" color="#CBD5E1" />
      </view>
    </view>

    <!-- 退出登录按钮（仅登录时显示） -->
    <view class="logout-btn" v-if="isLogin" @click="handleLogout">退出登录</view>

    <TabBar current="/pages/mine/index" />
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app' // UniApp 的生命周期从这里引入
import { TOKEN_KEY, REFRESH_TOKEN_KEY } from '@/common/config.js'
import api from '@/common/api.js'
import Icon from '@/components/Icon.vue'
import TabBar from '@/components/TabBar.vue'

const userInfo = ref({})
const isLogin = ref(false)
const stats = ref({})

// 从 storage 读取 token 刷新登录态（storage 非响应式，需手动刷新）
const refreshLoginState = () => {
  isLogin.value = !!uni.getStorageSync(TOKEN_KEY)
}

// 数字格式化：>=1w 显示 1.2w，>=1k 显示 1.2k（保留两位有效数字）
const formatCount = (n) => {
  const num = Number(n) || 0
  if (num >= 10000) {
    return parseFloat((num / 10000).toPrecision(2)) + 'w'
  }
  if (num >= 1000) {
    return parseFloat((num / 1000).toPrecision(2)) + 'k'
  }
  return String(num)
}

// 每次页面显示时刷新登录态、拉取用户信息与公开统计
onShow(() => {
  refreshLoginState()
  // 已登录时拉取用户信息，错误静默处理（不弹 toast）
  if (isLogin.value) {
    api.getUserInfo()
      .then((res) => {
        userInfo.value = res.data || {}
      })
      .catch(() => {})
  }
  // 拉取站点公开统计
  api.getStats()
    .then((res) => {
      stats.value = res.data || {}
    })
    .catch(() => {})
})

const goLogin = () => {
  uni.navigateTo({ url: '/pages/mine/login' })
}

const goPage = (url) => {
  uni.navigateTo({ url })
}

// 待开发功能提示
const showTodo = () => {
  uni.showToast({ title: '功能开发中', icon: 'none' })
}

const handleLogout = () => {
  uni.showModal({
    title: '提示',
    content: '确定退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        uni.removeStorageSync(TOKEN_KEY)
        uni.removeStorageSync(REFRESH_TOKEN_KEY)
        userInfo.value = {}
        refreshLoginState() // 立即刷新视图，无需等待 onShow
        uni.showToast({ title: '已退出' })
      }
    }
  })
}
</script>

<style lang="scss" scoped>
// 页面容器
.mine-page {
  min-height: 100vh;
  background: $color-bg;
  padding-bottom: calc(56px + env(safe-area-inset-bottom) + 12px);
}

// Hero 区：靛蓝到青色渐变背景
.hero {
  background: linear-gradient(135deg, #4F46E5 0%, #06B6D4 100%);
  padding: 48px 20px 32px;
  border-radius: 0 0 24px 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

// 未登录：白色描边登录按钮
.login-entry {
  padding: 10px 24px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 999px;
  color: #fff;
  font-size: 14px;
}

// 已登录：头像 + 昵称 + 简介
.hero-user {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  border: 3px solid #fff;
}

.nickname {
  margin-top: 12px;
  font-size: 18px;
  font-weight: 700;
  color: #fff;
}

.bio {
  margin-top: 4px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
}

// 统计数据卡片：上浮到 hero 边缘
.stats-card {
  margin: -16px 16px 0;
  background: $color-bg-card;
  border-radius: 16px;
  padding: 16px 0;
  box-shadow: 0 4px 16px rgba(15, 23, 42, 0.08);
  display: grid;
  grid-template-columns: 1fr 1fr;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-num {
  font-size: 20px;
  font-weight: 700;
  color: $color-text;
}

.stat-label {
  margin-top: 4px;
  font-size: 12px;
  color: $color-text-secondary;
}

// 菜单列表
.menu-list {
  margin: 16px;
  background: $color-bg-card;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.04);
}

// 菜单项：最后一项无下边框
.menu-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid $color-divider;

  &:last-child {
    border-bottom: none;
  }
}

// 左侧图标容器
.menu-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: #EEF2FF;
  color: $color-primary;
  display: flex;
  align-items: center;
  justify-content: center;
}

// 中间文字
.menu-text {
  flex: 1;
  font-size: 14px;
  color: $color-text;
  margin-left: 12px;
}

// 退出登录按钮
.logout-btn {
  margin: 16px;
  background: $color-bg-card;
  color: $color-danger;
  border-radius: 12px;
  height: 44px;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
