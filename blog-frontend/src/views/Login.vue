<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <template #header>
        <div class="auth-title">登录</div>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="0">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            show-password
            :prefix-icon="Lock"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item prop="captchaCode">
          <div class="captcha-row">
            <el-input v-model="form.captchaCode" placeholder="验证码" :prefix-icon="Key" @keyup.enter="handleLogin" />
            <img v-if="captcha.image" :src="captcha.image" class="captcha-img" title="点击刷新" @click="loadCaptcha" />
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="auth-btn" :loading="loading" @click="handleLogin">
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="auth-footer">
        没有账号？<el-link type="primary" @click="$router.push('/register')">去注册</el-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Key } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import authApi from '@/api/auth'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)
const captcha = reactive({ captchaId: '', image: '' })
const form = reactive({
  username: route.query.username || '',
  password: '',
  captchaCode: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captchaCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

const loadCaptcha = async () => {
  try {
    const res = await authApi.getCaptcha()
    captcha.captchaId = res.data?.captchaId || ''
    captcha.image = res.data?.image || ''
  } catch {
    /* 错误已由拦截器提示 */
  }
}

const handleLogin = () => {
  formRef.value.validate(async valid => {
    if (!valid) return
    loading.value = true
    try {
      const res = await authApi.login({
        username: form.username,
        password: form.password,
        captchaId: captcha.captchaId,
        captchaCode: form.captchaCode
      })
      // 后端返回 { accessToken, refreshToken }，refreshToken 已由 HTTP-only Cookie 下发
      userStore.setUser(res.data || {})
      ElMessage.success('登录成功')
      router.push(route.query.redirect || '/')
    } finally {
      loading.value = false
      // 验证码一次性消费，失败后需刷新
      form.captchaCode = ''
      loadCaptcha()
    }
  })
}

onMounted(() => {
  loadCaptcha()
})
</script>

<style scoped>
.auth-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 70vh;
}

.auth-card {
  width: 400px;
}

.auth-title {
  text-align: center;
  font-size: 20px;
  font-weight: bold;
}

.auth-btn {
  width: 100%;
}

.captcha-row {
  display: flex;
  gap: 10px;
  width: 100%;
}

.captcha-row .el-input {
  flex: 1;
}

.captcha-img {
  height: 40px;
  width: 130px;
  border-radius: 4px;
  cursor: pointer;
  border: 1px solid var(--border-color);
  flex-shrink: 0;
}

.auth-footer {
  text-align: center;
  font-size: 14px;
  color: #909399;
}
</style>
