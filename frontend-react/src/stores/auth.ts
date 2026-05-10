import { create } from 'zustand'
import type { LoginUser, CurrentUser } from '../types/auth'
import * as authApi from '../api/auth'

interface AuthState {
  token: string | null
  user: CurrentUser | LoginUser | null
  isLoggedIn: boolean
  isAdmin: boolean
  canManage: boolean
  login: (username: string, password: string) => Promise<void>
  refreshUser: () => Promise<void>
  logout: () => Promise<void>
}

export const useAuthStore = create<AuthState>((set, get) => ({
  token: localStorage.getItem('shipping_token'),
  user: JSON.parse(localStorage.getItem('shipping_user') || 'null'),

  get isLoggedIn() {
    return Boolean(get().token)
  },
  get isAdmin() {
    return get().user?.roleCode === 'ADMIN'
  },
  get canManage() {
    return get().user?.roleCode === 'ADMIN'
  },

  login: async (username, password) => {
    const res = await authApi.login(username, password)
    localStorage.setItem('shipping_token', res.loginToken)
    localStorage.setItem('shipping_user', JSON.stringify(res.user))
    set({ token: res.loginToken, user: res.user })
  },

  refreshUser: async () => {
    const user = await authApi.getCurrentUser()
    localStorage.setItem('shipping_user', JSON.stringify(user))
    set({ user })
  },

  logout: async () => {
    try {
      await authApi.logout()
    } finally {
      localStorage.removeItem('shipping_token')
      localStorage.removeItem('shipping_user')
      set({ token: null, user: null })
    }
  },
}))
