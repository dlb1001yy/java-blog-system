import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))

  const setUser = (data) => {
    token.value = data.accessToken
    userInfo.value = data
    localStorage.setItem('token', data.accessToken)
    localStorage.setItem('userInfo', JSON.stringify(data))
  }

  const logout = () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  return { token, userInfo, setUser, logout }
})