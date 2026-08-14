<template>
  <div class="login-page">
    <!-- 左侧品牌区 -->
    <div class="brand-side">
      <div class="brand-content">
        <div class="brand-logo">
          <span class="logo-dot"></span>
          <span class="logo-text">Java 博客</span>
        </div>
        <h1 class="brand-title">现代化博客内容管理平台</h1>
        <p class="brand-desc">高效写作 · 智能管理 · 数据驱动<br/>让每一篇好内容被看见</p>
        <ul class="brand-features">
          <li><span class="feature-icon">✦</span> 可视化数据看板</li>
          <li><span class="feature-icon">✦</span> Markdown 沉浸式写作</li>
          <li><span class="feature-icon">✦</span> 评论与留言一键管理</li>
        </ul>
        <div class="brand-footer">© 2026 Java Blog Admin</div>
      </div>
      <div class="brand-glow"></div>
    </div>

    <!-- 右侧登录区 -->
    <div class="form-side">
      <div class="login-box">
        <div class="login-header">
          <h2>欢迎回来 👋</h2>
          <p>请登录您的管理员账号</p>
        </div>
        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
          @keyup.enter="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              :prefix-icon="User"
              size="large"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              :prefix-icon="Lock"
              size="large"
              show-password
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              class="login-btn"
              :loading="loading"
              @click="handleLogin"
            >
              登 录
            </el-button>
          </el-form-item>
        </el-form>
        <div class="login-tips">
          <p>默认账号: admin</p>
          <p>默认密码: admin123</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref()
const loading = ref(false)

const loginForm = reactive({
  username: 'admin',
  password: 'admin123'
})

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      await userStore.login(loginForm)
      ElMessage.success('登录成功')
      router.push('/dashboard')
    } catch (error) {
      ElMessage.error('登录失败，请检查用户名和密码')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-page {
  display: flex;
  min-height: 100vh;
  background: var(--bg-subtle);
}

/* 左侧品牌区 */
.brand-side {
  flex: 1;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-8);
  overflow: hidden;
  background: linear-gradient(135deg, #047857 0%, #059669 40%, #14B8A6 100%);
  color: #fff;
}

.brand-glow {
  position: absolute;
  pointer-events: none;
  top: -100px;
  right: -100px;
  width: 400px;
  height: 400px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.15), transparent 70%);
}

.brand-glow::before {
  content: '';
  position: absolute;
  bottom: -150px;
  left: -100px;
  width: 500px;
  height: 500px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.15), transparent 70%);
}

.brand-content {
  position: relative;
  z-index: 1;
  max-width: 420px;
  width: 100%;
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-8);
}

.logo-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #fff;
  box-shadow: 0 0 20px rgba(255, 255, 255, 0.8);
}

.logo-text {
  font-size: var(--font-xl);
  font-weight: 700;
  letter-spacing: 1px;
}

.brand-title {
  font-size: var(--font-3xl);
  font-weight: 800;
  line-height: 1.3;
  margin-bottom: var(--space-4);
  color: #fff;
}

.brand-desc {
  font-size: var(--font-md);
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: var(--space-8);
}

.brand-features {
  list-style: none;
  padding: 0;
  margin-bottom: var(--space-8);
}

.brand-features li {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-2) 0;
  font-size: var(--font-base);
  color: rgba(255, 255, 255, 0.9);
}

.feature-icon {
  color: #fff;
  font-size: var(--font-md);
}

.brand-footer {
  font-size: var(--font-xs);
  color: rgba(255, 255, 255, 0.5);
}

/* 右侧登录区 */
.form-side {
  width: 520px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-8);
}

.login-box {
  width: 100%;
  max-width: 400px;
  padding: var(--space-8);
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-xl);
  border: 1px solid rgba(255, 255, 255, 0.6);
}

.login-header {
  text-align: center;
  margin-bottom: var(--space-6);
}

.login-header h2 {
  font-size: var(--font-2xl);
  color: var(--text-primary);
  margin-bottom: var(--space-2);
  font-weight: 700;
}

.login-header p {
  color: var(--text-secondary);
  font-size: var(--font-base);
}

.login-form {
  margin-bottom: var(--space-4);
}

:deep(.el-input__wrapper) {
  border-radius: var(--radius-md);
  padding: 4px 12px;
  box-shadow: 0 0 0 1px var(--border-color) inset;
  transition: all var(--transition-base);
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--color-primary-light) inset;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--color-primary) inset, 0 0 0 4px rgba(5, 150, 105, 0.1);
}

:deep(.el-input--large .el-input__wrapper) {
  padding: 6px 12px;
}

.login-btn {
  width: 100%;
  height: 44px;
  border-radius: var(--radius-md);
  font-size: var(--font-md);
  font-weight: 600;
  background: var(--gradient-primary);
  border: none;
  box-shadow: var(--shadow-primary);
  transition: all var(--transition-base);
}

:deep(.login-btn:hover) {
  transform: translateY(-2px);
  box-shadow: 0 12px 24px -6px rgba(5, 150, 105, 0.5);
}

:deep(.login-btn:active) {
  transform: translateY(0);
}

.login-tips {
  text-align: center;
  color: var(--text-secondary);
  font-size: var(--font-xs);
  line-height: 1.8;
  margin-top: var(--space-4);
  padding: var(--space-3);
  background: var(--bg-subtle);
  border-radius: var(--radius-md);
}

/* 响应式 */
@media (max-width: 768px) {
  .brand-side {
    display: none;
  }

  .form-side {
    width: 100%;
  }

  .login-box {
    max-width: none;
    padding: var(--space-5);
  }
}
</style>
