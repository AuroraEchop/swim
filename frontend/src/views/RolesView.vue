<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Delete, Edit, Plus, Refresh, Search } from '@element-plus/icons-vue'
import { createRole, deleteRole, getRoles, updateRole, type Role } from '../api/roles'

type DrawerMode = 'create' | 'edit'

interface RoleForm {
  id?: number
  roleName: string
  roleCode: string
  description: string
  builtIn?: boolean
}

const loading = ref(false)
const saving = ref(false)
const drawerVisible = ref(false)
const drawerMode = ref<DrawerMode>('create')
const formRef = ref<FormInstance>()
const roles = ref<Role[]>([])
const keyword = ref('')

const form = reactive<RoleForm>({
  roleName: '',
  roleCode: '',
  description: '',
})

const rules: FormRules<RoleForm> = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
}

const drawerTitle = computed(() => (drawerMode.value === 'create' ? '新增角色' : '编辑角色'))

const filteredRoles = computed(() => {
  const word = keyword.value.trim().toLowerCase()
  if (!word) {
    return roles.value
  }
  return roles.value.filter((role) => {
    return (
      role.roleName.toLowerCase().includes(word) ||
      role.roleCode.toLowerCase().includes(word) ||
      (role.description || '').toLowerCase().includes(word)
    )
  })
})

function resetForm() {
  Object.assign(form, {
    id: undefined,
    roleName: '',
    roleCode: '',
    description: '',
    builtIn: undefined,
  })
  formRef.value?.clearValidate()
}

function fillForm(role: Role) {
  Object.assign(form, {
    id: role.id,
    roleName: role.roleName,
    roleCode: role.roleCode,
    description: role.description || '',
    builtIn: role.builtIn,
  })
}

async function loadRoles() {
  loading.value = true
  try {
    roles.value = await getRoles()
  } finally {
    loading.value = false
  }
}

function openCreate() {
  drawerMode.value = 'create'
  drawerVisible.value = true
  resetForm()
}

function openEdit(role: Role) {
  drawerMode.value = 'edit'
  drawerVisible.value = true
  resetForm()
  fillForm(role)
}

async function handleSubmit() {
  await formRef.value?.validate()
  saving.value = true
  try {
    if (drawerMode.value === 'create') {
      await createRole({
        roleName: form.roleName,
        roleCode: form.roleCode,
        description: form.description || undefined,
      })
      ElMessage.success('角色新增成功')
    } else if (form.id) {
      await updateRole(form.id, {
        roleName: form.roleName,
        description: form.description || undefined,
      })
      ElMessage.success('角色修改成功')
    }
    drawerVisible.value = false
    await loadRoles()
  } finally {
    saving.value = false
  }
}

async function handleDelete(role: Role) {
  await ElMessageBox.confirm(`确认删除角色“${role.roleName}”吗？`, '删除确认', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    confirmButtonClass: 'el-button--danger',
  })
  await deleteRole(role.id)
  ElMessage.success('角色删除成功')
  await loadRoles()
}

onMounted(loadRoles)
</script>

<template>
  <section class="module-page">
    <div class="page-heading">
      <div>
        <h2>角色管理</h2>
        <p>维护系统角色基础信息。权限字段作为课程设计预留，不做复杂权限树。</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增角色</el-button>
    </div>

    <div class="content-panel">
      <div class="table-toolbar">
        <div>
          <strong>角色列表</strong>
          <span>共 {{ filteredRoles.length }} 条记录</span>
        </div>
        <div class="toolbar-actions">
          <el-input v-model="keyword" class="search-input" clearable placeholder="角色名称、编码或说明" :prefix-icon="Search" />
          <el-button :icon="Refresh" @click="loadRoles">刷新</el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="filteredRoles" empty-text="暂无角色数据" table-layout="fixed">
        <el-table-column prop="roleName" label="角色名称" min-width="140" />
        <el-table-column prop="roleCode" label="角色编码" min-width="140" />
        <el-table-column prop="description" label="角色说明" min-width="220" show-overflow-tooltip />
        <el-table-column label="内置角色" width="110">
          <template #default="{ row }">
            <el-tag :type="row.builtIn ? 'success' : 'info'" effect="light" round>
              {{ row.builtIn ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="150">
          <template #default="{ row }">
            <el-button link type="primary" :icon="Edit" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" :icon="Delete" :disabled="row.builtIn" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-drawer v-model="drawerVisible" :title="drawerTitle" size="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" maxlength="30" placeholder="请输入角色名称" show-word-limit />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input
            v-model="form.roleCode"
            :disabled="drawerMode === 'edit'"
            maxlength="30"
            placeholder="例如 VIEWER"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="角色说明" prop="description">
          <el-input v-model="form.description" :rows="4" maxlength="255" show-word-limit type="textarea" />
        </el-form-item>
        <p class="course-note form-note">
          课程设计简化：权限标识可作为说明预留，当前后端不持久化权限明细。
        </p>
      </el-form>

      <template #footer>
        <div class="drawer-footer">
          <el-button @click="drawerVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="handleSubmit">保存</el-button>
        </div>
      </template>
    </el-drawer>
  </section>
</template>
