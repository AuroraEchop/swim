<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { getRoles, type Role } from '../api/roles'

const model = defineModel<number | null | undefined>()

withDefaults(
  defineProps<{
    placeholder?: string
  }>(),
  {
    placeholder: '请选择角色',
  },
)

const loading = ref(false)
const roles = ref<Role[]>([])

async function loadRoles() {
  loading.value = true
  try {
    roles.value = await getRoles()
  } finally {
    loading.value = false
  }
}

onMounted(loadRoles)
</script>

<template>
  <el-select v-model="model" clearable filterable :loading="loading" :placeholder="placeholder">
    <el-option
      v-for="role in roles"
      :key="role.id"
      :label="`${role.roleName}（${role.roleCode}）`"
      :value="role.id"
    />
  </el-select>
</template>
