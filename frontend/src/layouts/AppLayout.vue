<script setup lang="ts">
import {
  Avatar,
  Collection,
  Compass,
  DataLine,
  Document,
  Fold,
  Money,
  Refresh,
  Setting,
  Ship,
  Tickets,
  User,
  UserFilled,
} from '@element-plus/icons-vue'
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const collapsed = ref(false)

const currentTitle = computed(() => String(route.meta.title || '业务工作台'))

const businessMenus = [
  { path: '/dashboard', title: '首页概览', icon: DataLine },
  { path: '/ships', title: '船舶管理', icon: Ship },
  { path: '/crew-members', title: '船员管理', icon: UserFilled },
  { path: '/transport-orders', title: '运输任务', icon: Compass },
  { path: '/settlements', title: '财务结算', icon: Money },
]

const systemMenus = [
  { path: '/dictionaries', title: '基础字典', icon: Collection },
  { path: '/users', title: '用户管理', icon: User },
  { path: '/roles', title: '角色管理', icon: Tickets },
]

async function handleLogout() {
  await authStore.logout()
  await router.push('/login')
}
</script>

<template>
  <div class="app-shell" :class="{ 'is-collapsed': collapsed }">
    <aside class="app-sidebar">
      <div class="brand">
        <div class="brand-mark">
          <el-icon><Ship /></el-icon>
        </div>
        <div v-if="!collapsed" class="brand-copy">
          <strong>航运公司</strong>
          <span>管理系统</span>
        </div>
      </div>

      <el-scrollbar class="menu-scroll">
        <div class="menu-group">
          <p v-if="!collapsed" class="menu-group-title">业务工作台</p>
          <RouterLink v-for="item in businessMenus" :key="item.path" :to="item.path" class="menu-item">
            <el-icon><component :is="item.icon" /></el-icon>
            <span v-if="!collapsed">{{ item.title }}</span>
          </RouterLink>
        </div>

        <div class="menu-group">
          <p v-if="!collapsed" class="menu-group-title">系统配置</p>
          <RouterLink v-for="item in systemMenus" :key="item.path" :to="item.path" class="menu-item">
            <el-icon><component :is="item.icon" /></el-icon>
            <span v-if="!collapsed">{{ item.title }}</span>
          </RouterLink>
        </div>
      </el-scrollbar>
    </aside>

    <section class="app-main">
      <header class="app-header">
        <div class="header-left">
          <el-button :icon="Fold" text circle aria-label="折叠菜单" @click="collapsed = !collapsed" />
          <div>
            <h1>{{ currentTitle }}</h1>
            <p>航运业务数据维护与流程管理</p>
          </div>
        </div>

        <div class="header-actions">
          <el-tooltip content="刷新当前页面">
            <el-button :icon="Refresh" text circle aria-label="刷新当前页面" @click="router.go(0)" />
          </el-tooltip>
          <el-dropdown>
            <button class="user-menu" type="button">
              <el-icon><Avatar /></el-icon>
              <span>{{ authStore.user?.realName || '未命名用户' }}</span>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item :icon="Document">修改密码</el-dropdown-item>
                <el-dropdown-item :icon="Setting">个人信息</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="page-body">
        <RouterView />
      </main>
    </section>
  </div>
</template>
