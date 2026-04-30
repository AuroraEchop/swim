<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Delete, Edit, MoreFilled, Plus, Refresh, Search, View } from '@element-plus/icons-vue'
import DictionarySelect from '../components/DictionarySelect.vue'
import StatusTag from '../components/StatusTag.vue'
import {
  createShip,
  deleteShip,
  getShip,
  getShips,
  updateShip,
  updateShipStatus,
  type Ship,
  type ShipStatus,
} from '../api/ships'
import { getStatusMeta, shipStatusOptions } from '../constants/status'
import { useAuthStore } from '../stores/auth'

type DrawerMode = 'create' | 'edit' | 'view'

interface ShipForm {
  id?: number
  shipNo: string
  shipName: string
  shipType: string
  loadCapacity: number | null
  homePort: string
  status: ShipStatus
  remark: string
}

const loading = ref(false)
const saving = ref(false)
const drawerVisible = ref(false)
const drawerMode = ref<DrawerMode>('create')
const formRef = ref<FormInstance>()
const ships = ref<Ship[]>([])
const total = ref(0)
const authStore = useAuthStore()
const canManage = computed(() => authStore.canManage)

const query = reactive({
  keyword: '',
  type: '',
  homePort: '',
  status: '' as ShipStatus | '',
  page: 1,
  pageSize: 10,
})

const form = reactive<ShipForm>({
  shipNo: '',
  shipName: '',
  shipType: '',
  loadCapacity: null,
  homePort: '',
  status: 'IDLE',
  remark: '',
})

const rules: FormRules<ShipForm> = {
  shipNo: [
    { required: true, message: '请输入船舶编号', trigger: 'blur' },
    { min: 3, max: 30, message: '船舶编号长度为 3 到 30 位', trigger: 'blur' },
  ],
  shipName: [
    { required: true, message: '请输入船名', trigger: 'blur' },
    { min: 2, max: 50, message: '船名长度为 2 到 50 位', trigger: 'blur' },
  ],
  shipType: [{ required: true, message: '请选择船舶类型', trigger: 'change' }],
  loadCapacity: [{ required: true, message: '请输入载重量', trigger: 'change' }],
  status: [{ required: true, message: '请选择船舶状态', trigger: 'change' }],
}

const drawerTitle = computed(() => {
  const titleMap: Record<DrawerMode, string> = {
    create: '新增船舶',
    edit: '编辑船舶',
    view: '船舶详情',
  }
  return titleMap[drawerMode.value]
})

const isReadonly = computed(() => drawerMode.value === 'view')

function resetForm() {
  Object.assign(form, {
    id: undefined,
    shipNo: '',
    shipName: '',
    shipType: '',
    loadCapacity: null,
    homePort: '',
    status: 'IDLE',
    remark: '',
  })
  formRef.value?.clearValidate()
}

function fillForm(ship: Ship) {
  Object.assign(form, {
    id: ship.id,
    shipNo: ship.shipNo,
    shipName: ship.shipName,
    shipType: ship.shipType,
    loadCapacity: Number(ship.loadCapacity),
    homePort: ship.homePort || '',
    status: ship.status,
    remark: ship.remark || '',
  })
}

async function loadShips() {
  loading.value = true
  try {
    const result = await getShips({
      keyword: query.keyword || undefined,
      type: query.type || undefined,
      homePort: query.homePort || undefined,
      status: query.status,
      page: query.page,
      pageSize: query.pageSize,
    })
    ships.value = result.records
    total.value = result.total
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  loadShips()
}

function handleReset() {
  Object.assign(query, {
    keyword: '',
    type: '',
    homePort: '',
    status: '',
    page: 1,
    pageSize: 10,
  })
  loadShips()
}

function openCreate() {
  drawerMode.value = 'create'
  drawerVisible.value = true
  resetForm()
}

async function openDetail(row: Ship, mode: DrawerMode) {
  drawerMode.value = mode
  drawerVisible.value = true
  resetForm()
  const detail = await getShip(row.id)
  fillForm(detail)
}

async function handleSubmit() {
  if (isReadonly.value) {
    drawerVisible.value = false
    return
  }

  await formRef.value?.validate()
  if (!form.loadCapacity) {
    return
  }

  saving.value = true
  try {
    const payload = {
      shipName: form.shipName,
      shipType: form.shipType,
      loadCapacity: form.loadCapacity,
      homePort: form.homePort || undefined,
      status: form.status,
      remark: form.remark || undefined,
    }
    if (drawerMode.value === 'create') {
      await createShip({ shipNo: form.shipNo, ...payload })
      ElMessage.success('船舶新增成功')
    } else if (form.id) {
      await updateShip(form.id, payload)
      ElMessage.success('船舶修改成功')
    }
    drawerVisible.value = false
    await loadShips()
  } finally {
    saving.value = false
  }
}

async function handleDelete(row: Ship) {
  await ElMessageBox.confirm(`确认删除船舶“${row.shipName}”吗？`, '删除确认', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    confirmButtonClass: 'el-button--danger',
  })
  await deleteShip(row.id)
  ElMessage.success('船舶删除成功')
  await loadShips()
}

async function handleStatusCommand(command: unknown, row: Ship) {
  const status = command as ShipStatus
  if (status === row.status) {
    return
  }
  const next = getStatusMeta(status).label
  await ElMessageBox.confirm(`确认将“${row.shipName}”状态改为“${next}”吗？`, '状态变更', {
    type: 'warning',
    confirmButtonText: '确认',
    cancelButtonText: '取消',
  })
  await updateShipStatus(row.id, status)
  ElMessage.success('状态已更新')
  await loadShips()
}

function createStatusHandler(row: Ship) {
  return (command: string | number | object) => handleStatusCommand(command, row)
}

onMounted(loadShips)
</script>

<template>
  <section class="module-page">
    <div class="page-heading">
      <div>
        <h2>船舶管理</h2>
        <p>维护船舶档案、载重量、所属港口和当前业务状态。</p>
      </div>
      <el-button v-if="canManage" type="primary" :icon="Plus" @click="openCreate">新增船舶</el-button>
    </div>

    <div class="search-panel">
      <el-input
        v-model="query.keyword"
        class="search-input"
        clearable
        placeholder="船名或船舶编号"
        :prefix-icon="Search"
        @keyup.enter="handleSearch"
      />
      <DictionarySelect v-model="query.type" class="search-input" type="SHIP_TYPE" placeholder="船舶类型" />
      <DictionarySelect v-model="query.homePort" class="search-input" type="PORT" placeholder="所属港口" />
      <el-select v-model="query.status" class="search-input" clearable placeholder="船舶状态">
        <el-option v-for="item in shipStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
      <el-button :icon="Refresh" @click="handleReset">重置</el-button>
    </div>

    <div class="content-panel">
      <div class="table-toolbar">
        <div>
          <strong>船舶列表</strong>
          <span>共 {{ total }} 条记录</span>
        </div>
      </div>

      <el-table v-loading="loading" :data="ships" empty-text="暂无船舶数据" table-layout="fixed">
        <el-table-column prop="shipNo" label="船舶编号" min-width="130" />
        <el-table-column prop="shipName" label="船名" min-width="140" show-overflow-tooltip />
        <el-table-column prop="shipType" label="船舶类型" width="120" />
        <el-table-column label="载重量" width="130" align="right">
          <template #default="{ row }">{{ Number(row.loadCapacity).toFixed(2) }} 吨</template>
        </el-table-column>
        <el-table-column prop="homePort" label="所属港口" width="120" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <StatusTag :status="row.status" />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" min-width="170" />
        <el-table-column label="操作" fixed="right" width="230">
          <template #default="{ row }">
            <el-button link type="primary" :icon="View" @click="openDetail(row, 'view')">查看</el-button>
            <el-button v-if="canManage" link type="primary" :icon="Edit" @click="openDetail(row, 'edit')">编辑</el-button>
            <el-dropdown v-if="canManage" trigger="click" @command="createStatusHandler(row)">
              <el-button link type="primary" :icon="MoreFilled">状态</el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    v-for="item in shipStatusOptions"
                    :key="item.value"
                    :command="item.value"
                    :disabled="item.value === row.status"
                  >
                    {{ item.label }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button v-if="canManage" link type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
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
          @size-change="loadShips"
          @current-change="loadShips"
        />
      </div>
    </div>

    <el-drawer v-model="drawerVisible" :title="drawerTitle" size="560px">
      <el-form ref="formRef" :model="form" :rules="rules" :disabled="isReadonly" label-width="96px">
        <el-form-item label="船舶编号" prop="shipNo">
          <el-input v-model="form.shipNo" :disabled="drawerMode !== 'create'" placeholder="例如 SHIP-003" />
        </el-form-item>
        <el-form-item label="船名" prop="shipName">
          <el-input v-model="form.shipName" placeholder="请输入船名" />
        </el-form-item>
        <el-form-item label="船舶类型" prop="shipType">
          <DictionarySelect v-model="form.shipType" type="SHIP_TYPE" placeholder="请选择船舶类型" />
        </el-form-item>
        <el-form-item label="载重量" prop="loadCapacity">
          <el-input-number
            v-model="form.loadCapacity"
            class="form-number"
            :min="0.01"
            :precision="2"
            :step="100"
            controls-position="right"
            placeholder="单位：吨"
          />
        </el-form-item>
        <el-form-item label="所属港口" prop="homePort">
          <DictionarySelect v-model="form.homePort" type="PORT" placeholder="请选择所属港口" />
        </el-form-item>
        <el-form-item label="船舶状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择船舶状态">
            <el-option v-for="item in shipStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" :rows="4" maxlength="255" show-word-limit type="textarea" />
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
