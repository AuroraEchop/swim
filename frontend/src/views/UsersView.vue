<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Delete, Edit, Plus, Refresh, Search, View } from '@element-plus/icons-vue'
import RoleSelect from '../components/RoleSelect.vue'
import StatusTag from '../components/StatusTag.vue'
import { createUser, deleteUser, getUser, getUsers, updateUser, type User, type UserStatus } from '../api/users'
import { userStatusOptions } from '../constants/status'
import { useAuthStore } from '../stores/auth'

type DrawerMode = 'create' | 'edit' | 'view'

interface UserForm {
  id?: number
  username: string
  password: string
  realName: string
  phone: string
  email: string
  roleId: number | null
  roleName?: string
  roleCode?: string
  status: UserStatus
}

const loading = ref(false)
const saving = ref(false)
const drawerVisible = ref(false)
const drawerMode = ref<DrawerMode>('create')
const formRef = ref<FormInstance>()
const users = ref<User[]>([])
const total = ref(0)
const authStore = useAuthStore()
const canManage = computed(() => authStore.canManage)

const query = reactive({
  username: '',
  realName: '',
  roleId: null as number | null,
  status: '' as UserStatus | '',
  page: 1,
  pageSize: 10,
})

const form = reactive<UserForm>({
  username: '',
  password: '123456',
  realName: '',
  phone: '',
  email: '',
  roleId: null,
  status: 'ENABLED',
})

const rules = computed<FormRules<UserForm>>(() => ({
  username: [
    { required: drawerMode.value === 'create', message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 30, message: '用户名长度为 3 到 30 位', trigger: 'blur' },
  ],
  password: [
    { required: drawerMode.value === 'create', message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 30, message: '密码长度为 6 到 30 位', trigger: 'blur' },
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 30, message: '真实姓名长度为 2 到 30 位', trigger: 'blur' },
  ],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  roleId: [{ required: true, message: '请选择角色', trigger: 'change' }],
  status: [{ required: true, message: '请选择用户状态', trigger: 'change' }],
}))

const drawerTitle = computed(() => {
  const titleMap: Record<DrawerMode, string> = {
    create: '新增用户',
    edit: '编辑用户',
    view: '用户详情',
  }
  return titleMap[drawerMode.value]
})

const isReadonly = computed(() => drawerMode.value === 'view')

function resetForm() {
  Object.assign(form, {
    id: undefined,
    username: '',
    password: '123456',
    realName: '',
    phone: '',
    email: '',
    roleId: null,
    roleName: undefined,
    roleCode: undefined,
    status: 'ENABLED',
  })
  formRef.value?.clearValidate()
}

function fillForm(user: User) {
  Object.assign(form, {
    id: user.id,
    username: user.username,
    password: '',
    realName: user.realName,
    phone: user.phone || '',
    email: user.email || '',
    roleId: user.roleId,
    roleName: user.roleName,
    roleCode: user.roleCode,
    status: user.status,
  })
}

async function loadUsers() {
  loading.value = true
  try {
    const result = await getUsers({
      username: query.username || undefined,
      realName: query.realName || undefined,
      roleId: query.roleId || undefined,
      status: query.status,
      page: query.page,
      pageSize: query.pageSize,
    })
    users.value = result.records
    total.value = result.total
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  loadUsers()
}

function handleReset() {
  Object.assign(query, {
    username: '',
    realName: '',
    roleId: null,
    status: '',
    page: 1,
    pageSize: 10,
  })
  loadUsers()
}

function openCreate() {
  drawerMode.value = 'create'
  drawerVisible.value = true
  resetForm()
}

async function openDetail(row: User, mode: DrawerMode) {
  drawerMode.value = mode
  drawerVisible.value = true
  resetForm()
  const detail = await getUser(row.id)
  fillForm(detail)
}

async function handleSubmit() {
  if (isReadonly.value) {
    drawerVisible.value = false
    return
  }

  await formRef.value?.validate()
  if (!form.roleId) {
    return
  }

  saving.value = true
  try {
    const payload = {
      realName: form.realName,
      phone: form.phone || undefined,
      email: form.email || undefined,
      roleId: form.roleId,
      status: form.status,
    }
    if (drawerMode.value === 'create') {
      await createUser({
        username: form.username,
        password: form.password,
        ...payload,
      })
      ElMessage.success('用户新增成功')
    } else if (form.id) {
      await updateUser(form.id, payload)
      ElMessage.success('用户修改成功')
    }
    drawerVisible.value = false
    await loadUsers()
  } finally {
    saving.value = false
  }
}

async function handleDelete(row: User) {
  await ElMessageBox.confirm(`确认删除用户“${row.username}”吗？`, '删除确认', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    confirmButtonClass: 'el-button--danger',
  })
  await deleteUser(row.id)
  ElMessage.success('用户删除成功')
  await loadUsers()
}

onMounted(loadUsers)
</script>

<template>
  <section class="module-page">
    <div class="page-heading">
      <div>
        <h2>用户管理</h2>
        <p>维护系统登录用户、角色归属和账号启停状态。</p>
      </div>
      <el-button v-if="canManage" type="primary" :icon="Plus" @click="openCreate">新增用户</el-button>
    </div>

    <div class="search-panel">
      <el-input
        v-model="query.username"
        class="search-input"
        clearable
        placeholder="用户名"
        :prefix-icon="Search"
        @keyup.enter="handleSearch"
      />
      <el-input v-model="query.realName" class="search-input" clearable placeholder="真实姓名" />
      <RoleSelect v-model="query.roleId" class="search-input" placeholder="角色" />
      <el-select v-model="query.status" class="search-input" clearable placeholder="用户状态">
        <el-option v-for="item in userStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
      <el-button :icon="Refresh" @click="handleReset">重置</el-button>
    </div>

    <div class="content-panel">
      <div class="table-toolbar">
        <div>
          <strong>用户列表</strong>
          <span>共 {{ total }} 条记录</span>
        </div>
      </div>

      <el-table v-loading="loading" :data="users" empty-text="暂无用户数据" table-layout="fixed">
        <el-table-column prop="username" label="用户名" min-width="130" />
        <el-table-column prop="realName" label="真实姓名" min-width="120" />
        <el-table-column prop="phone" label="联系电话" min-width="140" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column prop="roleName" label="角色" min-width="120" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <StatusTag :status="row.status" />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" min-width="170" />
        <el-table-column label="操作" fixed="right" width="180">
          <template #default="{ row }">
            <el-button link type="primary" :icon="View" @click="openDetail(row, 'view')">查看</el-button>
            <el-button v-if="canManage" link type="primary" :icon="Edit" @click="openDetail(row, 'edit')">编辑</el-button>
            <el-button v-if="canManage" link type="danger" :icon="Delete" :disabled="row.username === 'admin'" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-row">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          background
          layout="total, sizes, prev, pager, next, jumper"
          :page-sizes="[10, 20, 50]"
          :total="total"
          @size-change="loadUsers"
          @current-change="loadUsers"
        />
      </div>
    </div>

    <el-drawer v-model="drawerVisible" :title="drawerTitle" size="560px">
      <el-form ref="formRef" :model="form" :rules="rules" :disabled="isReadonly" label-width="96px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="drawerMode !== 'create'" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item v-if="drawerMode === 'create'" label="密码" prop="password">
          <el-input v-model="form.password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="角色" prop="roleId">
          <RoleSelect v-model="form.roleId" placeholder="请选择角色" />
        </el-form-item>
        <el-form-item label="用户状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择用户状态">
            <el-option v-for="item in userStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="drawer-footer">
          <el-button @click="drawerVisible = false">{{ isReadonly ? '关闭' : '取消' }}</el-button>
          <el-button v-if="!isReadonly" type="primary" :loading="saving" @click="handleSubmit">保存</el-button>
        </div>
      </template>
    </el-drawer>
  </section>
</template>
