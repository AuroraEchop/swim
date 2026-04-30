import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { getCurrentUser, login as loginApi, logout as logoutApi } from '../api/auth'
import type { CurrentUser, LoginUser } from '../types/auth'

const tokenKey = 'shipping_token'
const userKey = 'shipping_user'

function readStoredUser(): CurrentUser | LoginUser | null {
  const raw = localStorage.getItem(userKey)
  if (!raw) {
    return null
  }
  try {
    return JSON.parse(raw) as CurrentUser | LoginUser
  } catch {
    return null
  }
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem(tokenKey) || '')
  const user = ref<CurrentUser | LoginUser | null>(readStoredUser())
  const isLoggedIn = computed(() => Boolean(token.value))
  const isAdmin = computed(() => user.value?.roleCode === 'ADMIN')
  const canManage = computed(() => isAdmin.value)

  async function login(username: string, password: string) {
    const result = await loginApi({ username, password })
    token.value = result.loginToken
    user.value = result.user
    localStorage.setItem(tokenKey, result.loginToken)
    localStorage.setItem(userKey, JSON.stringify(result.user))
  }

  async function refreshUser() {
    if (!token.value) {
      return
    }
    const current = await getCurrentUser()
    user.value = current
    localStorage.setItem(userKey, JSON.stringify(current))
  }

  async function logout() {
    try {
      await logoutApi()
    } finally {
      token.value = ''
      user.value = null
      localStorage.removeItem(tokenKey)
      localStorage.removeItem(userKey)
    }
  }

  return {
    token,
    user,
    isLoggedIn,
    isAdmin,
    canManage,
    login,
    refreshUser,
    logout,
  }
})
