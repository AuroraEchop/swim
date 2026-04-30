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
  Ship,
  Tickets,
  User,
  UserFilled,
} from '@element-plus/icons-vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { updatePassword } from '../api/auth'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const collapsed = ref(false)
const passwordDialogVisible = ref(false)
const passwordSaving = ref(false)
const passwordFormRef = ref<FormInstance>()
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const passwordRules: FormRules<typeof passwordForm> = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 30, message: '新密码长度为 6 到 30 位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的新密码不一致'))
          return
        }
        callback()
      },
      trigger: 'blur',
    },
  ],
}

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

function openPasswordDialog() {
  Object.assign(passwordForm, {
    oldPassword: '',
    newPassword: '',
    confirmPassword: '',
  })
  passwordDialogVisible.value = true
  passwordFormRef.value?.clearValidate()
}

async function handlePasswordSubmit() {
  await passwordFormRef.value?.validate()
  if (passwordForm.oldPassword === passwordForm.newPassword) {
    ElMessage.warning('新密码不能与旧密码相同')
    return
  }
  passwordSaving.value = true
  try {
    await updatePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
    })
    ElMessage.success('密码修改成功')
    passwordDialogVisible.value = false
  } finally {
    passwordSaving.value = false
  }
}

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

        <div v-if="authStore.canManage" class="menu-group">
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
                <el-dropdown-item :icon="Document" @click="openPasswordDialog">修改密码</el-dropdown-item>
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

    <el-dialog v-model="passwordDialogVisible" title="修改密码" width="420px">
      <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="96px">
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="drawer-footer">
          <el-button @click="passwordDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="passwordSaving" @click="handlePasswordSubmit">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
