import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '../stores/auth'

export const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'login',
    component: () => import('../views/LoginView.vue'),
    meta: { public: true, title: '登录' },
  },
  {
    path: '/',
    component: () => import('../layouts/AppLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'dashboard',
        component: () => import('../views/DashboardView.vue'),
        meta: { title: '业务工作台' },
      },
      {
        path: 'ships',
        name: 'ships',
        component: () => import('../views/ShipsView.vue'),
        meta: { title: '船舶管理', module: 'ships' },
      },
      {
        path: 'crew-members',
        name: 'crewMembers',
        component: () => import('../views/CrewMembersView.vue'),
        meta: { title: '船员管理', module: 'crew-members' },
      },
      {
        path: 'transport-orders',
        name: 'transportOrders',
        component: () => import('../views/ModulePlaceholder.vue'),
        meta: { title: '运输任务', module: 'transport-orders' },
      },
      {
        path: 'settlements',
        name: 'settlements',
        component: () => import('../views/ModulePlaceholder.vue'),
        meta: { title: '财务结算', module: 'settlements' },
      },
      {
        path: 'dictionaries',
        name: 'dictionaries',
        component: () => import('../views/ModulePlaceholder.vue'),
        meta: { title: '基础字典', module: 'dictionaries' },
      },
      {
        path: 'users',
        name: 'users',
        component: () => import('../views/ModulePlaceholder.vue'),
        meta: { title: '用户管理', module: 'users' },
      },
      {
        path: 'roles',
        name: 'roles',
        component: () => import('../views/ModulePlaceholder.vue'),
        meta: { title: '角色管理', module: 'roles' },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard',
  },
]

export const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach(async (to) => {
  const authStore = useAuthStore()
  if (to.meta.public) {
    if (authStore.isLoggedIn && to.path === '/login') {
      return '/dashboard'
    }
    return true
  }

  if (!authStore.isLoggedIn) {
    return '/login'
  }

  if (!authStore.user || !('permissions' in authStore.user)) {
    try {
      await authStore.refreshUser()
    } catch {
      return '/login'
    }
  }

  return true
})
