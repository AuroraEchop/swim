<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Delete, Edit, MoreFilled, Plus, Refresh, Search, View } from '@element-plus/icons-vue'
import DictionarySelect from '../components/DictionarySelect.vue'
import ShipSelect from '../components/ShipSelect.vue'
import StatusTag from '../components/StatusTag.vue'
import {
  createTransportOrder,
  deleteTransportOrder,
  getTransportOrder,
  getTransportOrders,
  updateTransportOrder,
  updateTransportOrderStatus,
  type TransportOrder,
  type TransportStatus,
} from '../api/transportOrders'
import { getStatusMeta, transportStatusOptions } from '../constants/status'
import { useAuthStore } from '../stores/auth'

type DrawerMode = 'create' | 'edit' | 'view'

interface TransportForm {
  id?: number
  orderNo?: string
  cargoName: string
  cargoType: string
  cargoWeight: number | null
  originPort: string
  destinationPort: string
  shipId: number | null
  customerName: string
  customerPhone: string
  plannedDepartureTime: string
  plannedArrivalTime: string
  status?: TransportStatus
  settlementStatus?: string
  actualDepartureTime?: string
  actualArrivalTime?: string
  remark: string
}

const loading = ref(false)
const saving = ref(false)
const drawerVisible = ref(false)
const drawerMode = ref<DrawerMode>('create')
const formRef = ref<FormInstance>()
const orders = ref<TransportOrder[]>([])
const total = ref(0)
const authStore = useAuthStore()
const canManage = computed(() => authStore.canManage)

const query = reactive({
  keyword: '',
  shipId: null as number | null,
  originPort: '',
  destinationPort: '',
  status: '' as TransportStatus | '',
  dateRange: [] as string[],
  page: 1,
  pageSize: 10,
})

const form = reactive<TransportForm>({
  cargoName: '',
  cargoType: '',
  cargoWeight: null,
  originPort: '',
  destinationPort: '',
  shipId: null,
  customerName: '',
  customerPhone: '',
  plannedDepartureTime: '',
  plannedArrivalTime: '',
  remark: '',
})

const rules: FormRules<TransportForm> = {
  cargoName: [
    { required: true, message: '请输入货物名称', trigger: 'blur' },
    { min: 2, max: 100, message: '货物名称长度为 2 到 100 位', trigger: 'blur' },
  ],
  cargoType: [{ required: true, message: '请选择货物类型', trigger: 'change' }],
  cargoWeight: [{ required: true, message: '请输入货物重量', trigger: 'change' }],
  originPort: [{ required: true, message: '请选择起运港', trigger: 'change' }],
  destinationPort: [{ required: true, message: '请选择目的港', trigger: 'change' }],
  shipId: [{ required: true, message: '请选择运输船舶', trigger: 'change' }],
  customerName: [
    { required: true, message: '请输入客户名称', trigger: 'blur' },
    { min: 2, max: 100, message: '客户名称长度为 2 到 100 位', trigger: 'blur' },
  ],
  plannedDepartureTime: [{ required: true, message: '请选择预计出发时间', trigger: 'change' }],
  plannedArrivalTime: [{ required: true, message: '请选择预计到达时间', trigger: 'change' }],
}

const drawerTitle = computed(() => {
  const titleMap: Record<DrawerMode, string> = {
    create: '新增运输任务',
    edit: '编辑运输任务',
    view: '运输任务详情',
  }
  return titleMap[drawerMode.value]
})

const isReadonly = computed(() => drawerMode.value === 'view')

function resetForm() {
  Object.assign(form, {
    id: undefined,
    orderNo: undefined,
    cargoName: '',
    cargoType: '',
    cargoWeight: null,
    originPort: '',
    destinationPort: '',
    shipId: null,
    customerName: '',
    customerPhone: '',
    plannedDepartureTime: '',
    plannedArrivalTime: '',
    status: undefined,
    settlementStatus: undefined,
    actualDepartureTime: undefined,
    actualArrivalTime: undefined,
    remark: '',
  })
  formRef.value?.clearValidate()
}

function fillForm(order: TransportOrder) {
  Object.assign(form, {
    id: order.id,
    orderNo: order.orderNo,
    cargoName: order.cargoName,
    cargoType: order.cargoType,
    cargoWeight: Number(order.cargoWeight),
    originPort: order.originPort,
    destinationPort: order.destinationPort,
    shipId: order.shipId,
    customerName: order.customerName,
    customerPhone: order.customerPhone || '',
    plannedDepartureTime: order.plannedDepartureTime,
    plannedArrivalTime: order.plannedArrivalTime,
    status: order.status,
    settlementStatus: order.settlementStatus,
    actualDepartureTime: order.actualDepartureTime,
    actualArrivalTime: order.actualArrivalTime,
    remark: order.remark || '',
  })
}

function formatNow() {
  const date = new Date()
  const pad = (value: number) => String(value).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

function canEdit(row: TransportOrder) {
  return canManage.value && row.status !== 'ARRIVED' && row.status !== 'CANCELLED' && !row.settlementId
}

function canDelete(row: TransportOrder) {
  return canManage.value && (row.status === 'PENDING' || row.status === 'CANCELLED') && !row.settlementId
}

function nextStatusOptions(row: TransportOrder) {
  if (!canManage.value) {
    return []
  }
  if (row.status === 'PENDING') {
    return transportStatusOptions.filter((item) => item.value === 'IN_TRANSIT' || item.value === 'CANCELLED')
  }
  if (row.status === 'IN_TRANSIT') {
    return transportStatusOptions.filter((item) => item.value === 'ARRIVED')
  }
  return []
}

async function loadOrders() {
  loading.value = true
  try {
    const result = await getTransportOrders({
      keyword: query.keyword || undefined,
      shipId: query.shipId || undefined,
      originPort: query.originPort || undefined,
      destinationPort: query.destinationPort || undefined,
      status: query.status,
      startDate: query.dateRange[0],
      endDate: query.dateRange[1],
      page: query.page,
      pageSize: query.pageSize,
    })
    orders.value = result.records
    total.value = result.total
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  loadOrders()
}

function handleReset() {
  Object.assign(query, {
    keyword: '',
    shipId: null,
    originPort: '',
    destinationPort: '',
    status: '',
    dateRange: [],
    page: 1,
    pageSize: 10,
  })
  loadOrders()
}

function openCreate() {
  drawerMode.value = 'create'
  drawerVisible.value = true
  resetForm()
}

async function openDetail(row: TransportOrder, mode: DrawerMode) {
  drawerMode.value = mode
  drawerVisible.value = true
  resetForm()
  const detail = await getTransportOrder(row.id)
  fillForm(detail)
}

function validateSchedule() {
  if (form.originPort && form.destinationPort && form.originPort === form.destinationPort) {
    ElMessage.warning('起运港和目的港不能相同')
    return false
  }
  if (
    form.plannedDepartureTime &&
    form.plannedArrivalTime &&
    form.plannedArrivalTime <= form.plannedDepartureTime
  ) {
    ElMessage.warning('预计到达时间必须晚于预计出发时间')
    return false
  }
  return true
}

async function handleSubmit() {
  if (isReadonly.value) {
    drawerVisible.value = false
    return
  }

  await formRef.value?.validate()
  if (!form.cargoWeight || !form.shipId || !validateSchedule()) {
    return
  }

  saving.value = true
  try {
    const payload = {
      cargoName: form.cargoName,
      cargoType: form.cargoType,
      cargoWeight: form.cargoWeight,
      originPort: form.originPort,
      destinationPort: form.destinationPort,
      shipId: form.shipId,
      customerName: form.customerName,
      customerPhone: form.customerPhone || undefined,
      plannedDepartureTime: form.plannedDepartureTime,
      plannedArrivalTime: form.plannedArrivalTime,
      remark: form.remark || undefined,
    }
    if (drawerMode.value === 'create') {
      await createTransportOrder(payload)
      ElMessage.success('运输任务新增成功')
    } else if (form.id) {
      await updateTransportOrder(form.id, payload)
      ElMessage.success('运输任务修改成功')
    }
    drawerVisible.value = false
    await loadOrders()
  } finally {
    saving.value = false
  }
}

async function handleDelete(row: TransportOrder) {
  await ElMessageBox.confirm(`确认删除运输任务“${row.orderNo}”吗？`, '删除确认', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    confirmButtonClass: 'el-button--danger',
  })
  await deleteTransportOrder(row.id)
  ElMessage.success('运输任务删除成功')
  await loadOrders()
}

async function handleStatusCommand(command: unknown, row: TransportOrder) {
  const status = command as TransportStatus
  const next = getStatusMeta(status).label
  const payload = { status, actualDepartureTime: undefined as string | undefined, actualArrivalTime: undefined as string | undefined }
  if (status === 'IN_TRANSIT') {
    payload.actualDepartureTime = formatNow()
  }
  if (status === 'ARRIVED') {
    payload.actualArrivalTime = formatNow()
  }

  const timeHint = status === 'IN_TRANSIT' || status === 'ARRIVED' ? '，并记录当前时间' : ''
  await ElMessageBox.confirm(`确认将任务“${row.orderNo}”状态改为“${next}”${timeHint}吗？`, '状态变更', {
    type: 'warning',
    confirmButtonText: '确认',
    cancelButtonText: '取消',
  })
  await updateTransportOrderStatus(row.id, payload)
  ElMessage.success('状态已更新')
  await loadOrders()
}

function createStatusHandler(row: TransportOrder) {
  return (command: string | number | object) => handleStatusCommand(command, row)
}

onMounted(loadOrders)
</script>

<template>
  <section class="module-page">
    <div class="page-heading">
      <div>
        <h2>运输任务</h2>
        <p>创建运输任务，跟踪船舶承运、航线、计划时间和运输状态。</p>
      </div>
      <el-button v-if="canManage" type="primary" :icon="Plus" @click="openCreate">新增任务</el-button>
    </div>

    <div class="search-panel">
      <el-input
        v-model="query.keyword"
        class="search-input"
        clearable
        placeholder="任务编号、货物或客户"
        :prefix-icon="Search"
        @keyup.enter="handleSearch"
      />
      <ShipSelect v-model="query.shipId" class="search-input" placeholder="承运船舶" />
      <DictionarySelect v-model="query.originPort" class="search-input" type="PORT" placeholder="起运港" />
      <DictionarySelect v-model="query.destinationPort" class="search-input" type="PORT" placeholder="目的港" />
      <el-select v-model="query.status" class="search-input" clearable placeholder="运输状态">
        <el-option v-for="item in transportStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-date-picker
        v-model="query.dateRange"
        class="date-range-input"
        type="daterange"
        value-format="YYYY-MM-DD"
        start-placeholder="出发开始"
        end-placeholder="出发结束"
      />
      <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
      <el-button :icon="Refresh" @click="handleReset">重置</el-button>
    </div>

    <div class="content-panel">
      <div class="table-toolbar">
        <div>
          <strong>运输任务列表</strong>
          <span>共 {{ total }} 条记录</span>
        </div>
      </div>

      <el-table v-loading="loading" :data="orders" empty-text="暂无运输任务" table-layout="fixed">
        <el-table-column prop="orderNo" label="任务编号" min-width="170" />
        <el-table-column prop="cargoName" label="货物" min-width="130" show-overflow-tooltip />
        <el-table-column prop="cargoType" label="货物类型" width="120" />
        <el-table-column label="重量" width="120" align="right">
          <template #default="{ row }">{{ Number(row.cargoWeight).toFixed(2) }} 吨</template>
        </el-table-column>
        <el-table-column label="航线" min-width="170" show-overflow-tooltip>
          <template #default="{ row }">{{ row.originPort }} 至 {{ row.destinationPort }}</template>
        </el-table-column>
        <el-table-column prop="shipName" label="承运船舶" min-width="130" show-overflow-tooltip />
        <el-table-column prop="customerName" label="客户" min-width="150" show-overflow-tooltip />
        <el-table-column label="运输状态" width="110">
          <template #default="{ row }">
            <StatusTag :status="row.status" />
          </template>
        </el-table-column>
        <el-table-column label="结算状态" width="110">
          <template #default="{ row }">
            <StatusTag :status="row.settlementStatus" />
          </template>
        </el-table-column>
        <el-table-column prop="plannedDepartureTime" label="预计出发" min-width="170" />
        <el-table-column label="操作" fixed="right" width="230">
          <template #default="{ row }">
            <el-button link type="primary" :icon="View" @click="openDetail(row, 'view')">查看</el-button>
            <el-button v-if="canManage" link type="primary" :icon="Edit" :disabled="!canEdit(row)" @click="openDetail(row, 'edit')">
              编辑
            </el-button>
            <el-dropdown v-if="canManage" trigger="click" :disabled="nextStatusOptions(row).length === 0" @command="createStatusHandler(row)">
              <el-button link type="primary" :icon="MoreFilled" :disabled="nextStatusOptions(row).length === 0">
                状态
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item v-for="item in nextStatusOptions(row)" :key="item.value" :command="item.value">
                    {{ item.label }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button v-if="canManage" link type="danger" :icon="Delete" :disabled="!canDelete(row)" @click="handleDelete(row)">
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
          @size-change="loadOrders"
          @current-change="loadOrders"
        />
      </div>
    </div>

    <el-drawer v-model="drawerVisible" :title="drawerTitle" size="640px">
      <el-form ref="formRef" :model="form" :rules="rules" :disabled="isReadonly" label-width="112px">
        <el-form-item v-if="form.orderNo" label="任务编号">
          <el-input v-model="form.orderNo" disabled />
        </el-form-item>
        <el-form-item label="货物名称" prop="cargoName">
          <el-input v-model="form.cargoName" placeholder="请输入货物名称" />
        </el-form-item>
        <el-form-item label="货物类型" prop="cargoType">
          <DictionarySelect v-model="form.cargoType" type="CARGO_TYPE" placeholder="请选择货物类型" />
        </el-form-item>
        <el-form-item label="货物重量" prop="cargoWeight">
          <el-input-number
            v-model="form.cargoWeight"
            class="form-number"
            :min="0.01"
            :precision="2"
            :step="10"
            controls-position="right"
            placeholder="单位：吨"
          />
        </el-form-item>
        <el-form-item label="起运港" prop="originPort">
          <DictionarySelect v-model="form.originPort" type="PORT" placeholder="请选择起运港" />
        </el-form-item>
        <el-form-item label="目的港" prop="destinationPort">
          <DictionarySelect v-model="form.destinationPort" type="PORT" placeholder="请选择目的港" />
        </el-form-item>
        <el-form-item label="承运船舶" prop="shipId">
          <ShipSelect v-model="form.shipId" enabled-only placeholder="请选择承运船舶" />
        </el-form-item>
        <el-form-item label="客户名称" prop="customerName">
          <el-input v-model="form.customerName" placeholder="请输入客户名称" />
        </el-form-item>
        <el-form-item label="客户电话" prop="customerPhone">
          <el-input v-model="form.customerPhone" placeholder="请输入客户电话" />
        </el-form-item>
        <el-form-item label="预计出发" prop="plannedDepartureTime">
          <el-date-picker
            v-model="form.plannedDepartureTime"
            class="form-number"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="请选择预计出发时间"
          />
        </el-form-item>
        <el-form-item label="预计到达" prop="plannedArrivalTime">
          <el-date-picker
            v-model="form.plannedArrivalTime"
            class="form-number"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="请选择预计到达时间"
          />
        </el-form-item>
        <el-form-item v-if="form.status" label="运输状态">
          <StatusTag :status="form.status" />
        </el-form-item>
        <el-form-item v-if="form.settlementStatus" label="结算状态">
          <StatusTag :status="form.settlementStatus" />
        </el-form-item>
        <el-form-item v-if="form.actualDepartureTime" label="实际出发">
          <el-input v-model="form.actualDepartureTime" disabled />
        </el-form-item>
        <el-form-item v-if="form.actualArrivalTime" label="实际到达">
          <el-input v-model="form.actualArrivalTime" disabled />
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
