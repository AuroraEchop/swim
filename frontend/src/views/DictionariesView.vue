<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Delete, Edit, Plus, Refresh, Search } from '@element-plus/icons-vue'
import {
  createDictionaryItem,
  deleteDictionaryItem,
  getDictionaryItems,
  updateDictionaryItem,
  type DictionaryItem,
} from '../api/dictionaries'

type DrawerMode = 'create' | 'edit'

interface DictionaryTypeMeta {
  label: string
  value: string
  description: string
}

interface DictionaryForm {
  id?: number
  dictType: string
  label: string
  value: string
  sort: number
  enabled: boolean
  remark: string
}

const dictionaryTypes: DictionaryTypeMeta[] = [
  { label: '船舶类型', value: 'SHIP_TYPE', description: '用于船舶管理中的船舶类型下拉' },
  { label: '货物类型', value: 'CARGO_TYPE', description: '用于运输任务中的货物类型下拉' },
  { label: '港口信息', value: 'PORT', description: '用于船舶所属港口和运输航线选择' },
  { label: '船员岗位', value: 'CREW_POSITION', description: '用于船员管理中的岗位选择' },
]

const activeType = ref('SHIP_TYPE')
const keyword = ref('')
const loading = ref(false)
const saving = ref(false)
const drawerVisible = ref(false)
const drawerMode = ref<DrawerMode>('create')
const formRef = ref<FormInstance>()
const items = ref<DictionaryItem[]>([])

const form = reactive<DictionaryForm>({
  dictType: 'SHIP_TYPE',
  label: '',
  value: '',
  sort: 0,
  enabled: true,
  remark: '',
})

const rules: FormRules<DictionaryForm> = {
  label: [{ required: true, message: '请输入显示名称', trigger: 'blur' }],
  value: [{ required: true, message: '请输入字典值', trigger: 'blur' }],
}

const currentTypeMeta = computed(() => {
  return dictionaryTypes.find((item) => item.value === activeType.value) || dictionaryTypes[0]
})

const drawerTitle = computed(() => (drawerMode.value === 'create' ? '新增字典项' : '编辑字典项'))

const filteredItems = computed(() => {
  const word = keyword.value.trim().toLowerCase()
  if (!word) {
    return items.value
  }
  return items.value.filter((item) => {
    return (
      item.label.toLowerCase().includes(word) ||
      item.value.toLowerCase().includes(word) ||
      (item.remark || '').toLowerCase().includes(word)
    )
  })
})

function resetForm() {
  Object.assign(form, {
    id: undefined,
    dictType: activeType.value,
    label: '',
    value: '',
    sort: 0,
    enabled: true,
    remark: '',
  })
  formRef.value?.clearValidate()
}

function fillForm(item: DictionaryItem) {
  Object.assign(form, {
    id: item.id,
    dictType: item.dictType,
    label: item.label,
    value: item.value,
    sort: item.sort || 0,
    enabled: Boolean(item.enabled),
    remark: item.remark || '',
  })
}

async function loadItems() {
  loading.value = true
  try {
    items.value = await getDictionaryItems(activeType.value)
  } finally {
    loading.value = false
  }
}

function handleTabChange() {
  keyword.value = ''
  loadItems()
}

function openCreate() {
  drawerMode.value = 'create'
  drawerVisible.value = true
  resetForm()
}

function openEdit(item: DictionaryItem) {
  drawerMode.value = 'edit'
  drawerVisible.value = true
  resetForm()
  fillForm(item)
}

async function handleSubmit() {
  await formRef.value?.validate()
  saving.value = true
  try {
    const payload = {
      label: form.label,
      value: form.value,
      sort: form.sort,
      enabled: form.enabled,
      remark: form.remark || undefined,
    }
    if (drawerMode.value === 'create') {
      await createDictionaryItem({ dictType: activeType.value, ...payload })
      ElMessage.success('字典项新增成功')
    } else if (form.id) {
      await updateDictionaryItem(form.id, payload)
      ElMessage.success('字典项修改成功')
    }
    drawerVisible.value = false
    await loadItems()
  } finally {
    saving.value = false
  }
}

async function handleDelete(item: DictionaryItem) {
  await ElMessageBox.confirm(`确认删除字典项“${item.label}”吗？`, '删除确认', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    confirmButtonClass: 'el-button--danger',
  })
  await deleteDictionaryItem(item.id)
  ElMessage.success('字典项删除成功')
  await loadItems()
}

onMounted(loadItems)
</script>

<template>
  <section class="module-page">
    <div class="page-heading">
      <div>
        <h2>基础字典</h2>
        <p>维护船舶类型、货物类型、港口和船员岗位等基础下拉数据。</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增字典项</el-button>
    </div>

    <div class="content-panel">
      <el-tabs v-model="activeType" class="dictionary-tabs" @tab-change="handleTabChange">
        <el-tab-pane v-for="type in dictionaryTypes" :key="type.value" :label="type.label" :name="type.value" />
      </el-tabs>

      <div class="table-toolbar">
        <div>
          <strong>{{ currentTypeMeta.label }}</strong>
          <span>{{ currentTypeMeta.description }}</span>
        </div>
        <div class="toolbar-actions">
          <el-input
            v-model="keyword"
            class="search-input"
            clearable
            placeholder="显示名称、字典值或备注"
            :prefix-icon="Search"
          />
          <el-button :icon="Refresh" @click="loadItems">刷新</el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="filteredItems" empty-text="暂无字典项" table-layout="fixed">
        <el-table-column prop="label" label="显示名称" min-width="140" />
        <el-table-column prop="value" label="字典值" min-width="160" show-overflow-tooltip />
        <el-table-column prop="sort" label="排序号" width="100" />
        <el-table-column label="启用状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'" effect="light" round>
              {{ row.enabled ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="180" show-overflow-tooltip />
        <el-table-column prop="updatedAt" label="更新时间" min-width="170" />
        <el-table-column label="操作" fixed="right" width="150">
          <template #default="{ row }">
            <el-button link type="primary" :icon="Edit" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-drawer v-model="drawerVisible" :title="drawerTitle" size="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-form-item label="字典类型">
          <el-input :model-value="currentTypeMeta.label" disabled />
        </el-form-item>
        <el-form-item label="显示名称" prop="label">
          <el-input v-model="form.label" maxlength="50" placeholder="请输入显示名称" show-word-limit />
        </el-form-item>
        <el-form-item label="字典值" prop="value">
          <el-input v-model="form.value" maxlength="50" placeholder="请输入字典值" show-word-limit />
        </el-form-item>
        <el-form-item label="排序号" prop="sort">
          <el-input-number v-model="form.sort" class="form-number" :min="0" :step="1" controls-position="right" />
        </el-form-item>
        <el-form-item label="启用状态" prop="enabled">
          <el-switch v-model="form.enabled" active-text="启用" inactive-text="停用" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" :rows="4" maxlength="255" show-word-limit type="textarea" />
        </el-form-item>
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
