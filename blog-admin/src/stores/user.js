import { defineStore } from 'pinia'
import { ref } from 'vue'
import authApi from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('admin_token') || '')
  const username = ref(localStorage.getItem('admin_username') || '')
  const userInfo = ref({})

  const login = async (loginForm) => {
    const res = await authApi.login(loginForm)
    // 注意：refresh token 由后端通过 HTTP-only Cookie 下发，前端不存储
    token.value = res.data.accessToken
    username.value = res.data.username
    localStorage.setItem('admin_token', res.data.accessToken)
    localStorage.setItem('admin_username', res.data.username)
    return res
  }

  // 使用 Cookie 中的 refresh token 刷新 access token（供 request 拦截器调用）
  const refreshToken = async () => {
    const res = await authApi.refresh()
    token.value = res.data.accessToken
    localStorage.setItem('admin_token', res.data.accessToken)
    return res.data.accessToken
  }

  const logout = () => {
    token.value = ''
    username.value = ''
    userInfo.value = {}
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_username')
  }

  const getUserInfo = async () => {
    if (!token.value) return
    const res = await authApi.getUserInfo()
    userInfo.value = res.data
    return res
  }

  return {
    token,
    username,
    userInfo,
    login,
    logout,
    refreshToken,
    getUserInfo
  }
})
