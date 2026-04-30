<script setup lang="ts">
import { Lock, Ship, User } from '@element-plus/icons-vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  username: 'admin',
  password: '123456',
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 30, message: '密码长度应为 6 到 30 位', trigger: 'blur' },
  ],
}

async function handleLogin() {
  const valid = await formRef.value?.validate()
  if (!valid) {
    return
  }
  loading.value = true
  try {
    await authStore.login(form.username, form.password)
    ElMessage.success('登录成功')
    await router.push('/dashboard')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="login-page">
    <section class="login-intro" aria-label="系统介绍">
      <div class="login-brand">
        <div class="login-brand-mark">
          <el-icon><Ship /></el-icon>
        </div>
        <div>
          <p>Shipping Management</p>
          <h1>航运公司管理系统</h1>
        </div>
      </div>
      <p class="login-summary">统一管理船舶、船员、运输任务与结算流程。</p>
      <div class="login-scope">
        <span>船舶档案</span>
        <span>运输任务</span>
        <span>财务结算</span>
      </div>
    </section>

    <section class="login-panel" aria-label="登录表单">
      <div class="panel-heading">
        <h2>账号登录</h2>
        <p>使用系统账号进入业务工作台</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" size="large" @keyup.enter="handleLogin">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :prefix-icon="User" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" :prefix-icon="Lock" placeholder="请输入密码" show-password type="password" />
        </el-form-item>
        <el-button class="login-submit" type="primary" :loading="loading" @click="handleLogin">登录系统</el-button>
      </el-form>
    </section>
  </main>
</template>
