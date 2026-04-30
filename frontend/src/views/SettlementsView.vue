<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Delete, Edit, Money, Plus, Refresh, Search, View } from '@element-plus/icons-vue'
import StatusTag from '../components/StatusTag.vue'
import TransportOrderSelect from '../components/TransportOrderSelect.vue'
import {
  createSettlement,
  deleteSettlement,
  getSettlement,
  getSettlements,
  updateSettlement,
  updateSettlementPayment,
  type Settlement,
} from '../api/settlements'
import type { SettlementStatus } from '../api/transportOrders'
import { settlementStatusOptions } from '../constants/status'

type DrawerMode = 'create' | 'edit' | 'view'

interface SettlementForm {
  id?: number
  settlementNo?: string
  transportOrderId: number | null
  transportOrderNo?: string
  customerName?: string
  freightAmount: number | null
  additionalFee: number | null
  discountAmount: number | null
  receivedAmount: number | null
  receivableAmount?: number
  status?: SettlementStatus
  settledAt?: string
  remark: string
}

const loading = ref(false)
const saving = ref(false)
const drawerVisible = ref(false)
const drawerMode = ref<DrawerMode>('create')
const formRef = ref<FormInstance>()
const settlements = ref<Settlement[]>([])
const total = ref(0)

const query = reactive({
  keyword: '',
  transportOrderId: null as number | null,
  customerName: '',
  status: '' as SettlementStatus | '',
  dateRange: [] as string[],
  page: 1,
  pageSize: 10,
})

const form = reactive<SettlementForm>({
  transportOrderId: null,
  freightAmount: null,
  additionalFee: 0,
  discountAmount: 0,
  receivedAmount: 0,
  remark: '',
})

const rules: FormRules<SettlementForm> = {
  transportOrderId: [{ required: true, message: '请选择运输任务', trigger: 'change' }],
  freightAmount: [{ required: true, message: '请输入运费金额', trigger: 'change' }],
}

const drawerTitle = computed(() => {
  const titleMap: Record<DrawerMode, string> = {
    create: '新增结算记录',
    edit: '编辑结算记录',
    view: '结算详情',
  }
  return titleMap[drawerMode.value]
})

const isReadonly = computed(() => drawerMode.value === 'view')

const calculatedReceivable = computed(() => {
  const freight = form.freightAmount || 0
  const additional = form.additionalFee || 0
  const discount = form.discountAmount || 0
  return Math.max(freight + additional - discount, 0)
})

function formatMoney(value?: number | null) {
  return Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function formatNow() {
  const date = new Date()
  const pad = (value: number) => String(value).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

function canDelete(row: Settlement) {
  return row.status === 'UNSETTLED'
}

function resetForm() {
  Object.assign(form, {
    id: undefined,
    settlementNo: undefined,
    transportOrderId: null,
    transportOrderNo: undefined,
    customerName: undefined,
    freightAmount: null,
    additionalFee: 0,
    discountAmount: 0,
    receivedAmount: 0,
    receivableAmount: undefined,
    status: undefined,
    settledAt: undefined,
    remark: '',
  })
  formRef.value?.clearValidate()
}

function fillForm(settlement: Settlement) {
  Object.assign(form, {
    id: settlement.id,
    settlementNo: settlement.settlementNo,
    transportOrderId: settlement.transportOrderId,
    transportOrderNo: settlement.transportOrderNo,
    customerName: settlement.customerName,
    freightAmount: Number(settlement.freightAmount),
    additionalFee: Number(settlement.additionalFee),
    discountAmount: Number(settlement.discountAmount),
    receivedAmount: Number(settlement.receivedAmount),
    receivableAmount: Number(settlement.receivableAmount),
    status: settlement.status,
    settledAt: settlement.settledAt,
    remark: settlement.remark || '',
  })
}

async function loadSettlements() {
  loading.value = true
  try {
    const result = await getSettlements({
      keyword: query.keyword || undefined,
      transportOrderId: query.transportOrderId || undefined,
      customerName: query.customerName || undefined,
      status: query.status,
      startDate: query.dateRange[0],
      endDate: query.dateRange[1],
      page: query.page,
      pageSize: query.pageSize,
    })
    settlements.value = result.records
    total.value = result.total
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  loadSettlements()
}

function handleReset() {
  Object.assign(query, {
    keyword: '',
    transportOrderId: null,
    customerName: '',
    status: '',
    dateRange: [],
    page: 1,
    pageSize: 10,
  })
  loadSettlements()
}

function openCreate() {
  drawerMode.value = 'create'
  drawerVisible.value = true
  resetForm()
}

async function openDetail(row: Settlement, mode: DrawerMode) {
  drawerMode.value = mode
  drawerVisible.value = true
  resetForm()
  const detail = await getSettlement(row.id)
  fillForm(detail)
}

function validateAmounts() {
  if (!form.freightAmount && form.freightAmount !== 0) {
    return false
  }
  if ((form.receivedAmount || 0) > calculatedReceivable.value) {
    ElMessage.warning('实收金额不能大于应收金额')
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
  if (!form.transportOrderId || !validateAmounts()) {
    return
  }

  saving.value = true
  try {
    const payload = {
      freightAmount: form.freightAmount || 0,
      additionalFee: form.additionalFee || 0,
      discountAmount: form.discountAmount || 0,
      receivedAmount: form.receivedAmount || 0,
      remark: form.remark || undefined,
    }
    if (drawerMode.value === 'create') {
      await createSettlement({ transportOrderId: form.transportOrderId, ...payload })
      ElMessage.success('结算记录新增成功')
    } else if (form.id) {
      await updateSettlement(form.id, payload)
      ElMessage.success('结算记录修改成功')
    }
    drawerVisible.value = false
    await loadSettlements()
  } finally {
    saving.value = false
  }
}

async function handlePayment(row: Settlement) {
  const { value } = await ElMessageBox.prompt('请输入本次更新后的实收金额', `登记收款：${row.settlementNo}`, {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    inputValue: String(row.receivedAmount || 0),
    inputPattern: /^\d+(\.\d{1,2})?$/,
    inputErrorMessage: '请输入有效金额，最多两位小数',
  })
  const receivedAmount = Number(value)
  if (receivedAmount > Number(row.receivableAmount)) {
    ElMessage.warning('实收金额不能大于应收金额')
    return
  }
  await updateSettlementPayment(row.id, {
    receivedAmount,
    paymentTime: formatNow(),
  })
  ElMessage.success('收款信息已更新')
  await loadSettlements()
}

async function handleDelete(row: Settlement) {
  await ElMessageBox.confirm(`确认删除结算记录“${row.settlementNo}”吗？`, '删除确认', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    confirmButtonClass: 'el-button--danger',
  })
  await deleteSettlement(row.id)
  ElMessage.success('结算记录删除成功')
  await loadSettlements()
}

onMounted(loadSettlements)
</script>

<template>
  <section class="module-page">
    <div class="page-heading">
      <div>
        <h2>财务结算</h2>
        <p>根据运输任务生成结算记录，维护应收金额、实收金额和结算状态。</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增结算</el-button>
    </div>

    <div class="search-panel">
      <el-input
        v-model="query.keyword"
        class="search-input"
        clearable
        placeholder="结算编号、任务或客户"
        :prefix-icon="Search"
        @keyup.enter="handleSearch"
      />
      <TransportOrderSelect v-model="query.transportOrderId" class="search-input" placeholder="运输任务" />
      <el-input v-model="query.customerName" class="search-input" clearable placeholder="客户名称" />
      <el-select v-model="query.status" class="search-input" clearable placeholder="结算状态">
        <el-option v-for="item in settlementStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-date-picker
        v-model="query.dateRange"
        class="date-range-input"
        type="daterange"
        value-format="YYYY-MM-DD"
        start-placeholder="创建开始"
        end-placeholder="创建结束"
      />
      <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
      <el-button :icon="Refresh" @click="handleReset">重置</el-button>
    </div>

    <div class="content-panel">
      <div class="table-toolbar">
        <div>
          <strong>结算列表</strong>
          <span>共 {{ total }} 条记录</span>
        </div>
      </div>

      <el-table v-loading="loading" :data="settlements" empty-text="暂无结算记录" table-layout="fixed">
        <el-table-column prop="settlementNo" label="结算编号" min-width="180" />
        <el-table-column prop="transportOrderNo" label="运输任务" min-width="180" />
        <el-table-column prop="customerName" label="客户" min-width="150" show-overflow-tooltip />
        <el-table-column label="应收金额" width="130" align="right">
          <template #default="{ row }">¥ {{ formatMoney(row.receivableAmount) }}</template>
        </el-table-column>
        <el-table-column label="实收金额" width="130" align="right">
          <template #default="{ row }">¥ {{ formatMoney(row.receivedAmount) }}</template>
        </el-table-column>
        <el-table-column label="结算状态" width="110">
          <template #default="{ row }">
            <StatusTag :status="row.status" />
          </template>
        </el-table-column>
        <el-table-column prop="settledAt" label="结清时间" min-width="170" />
        <el-table-column prop="createdAt" label="创建时间" min-width="170" />
        <el-table-column label="操作" fixed="right" width="250">
          <template #default="{ row }">
            <el-button link type="primary" :icon="View" @click="openDetail(row, 'view')">查看</el-button>
            <el-button link type="primary" :icon="Edit" @click="openDetail(row, 'edit')">编辑</el-button>
            <el-button link type="primary" :icon="Money" @click="handlePayment(row)">收款</el-button>
            <el-button link type="danger" :icon="Delete" :disabled="!canDelete(row)" @click="handleDelete(row)">
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
          @size-change="loadSettlements"
          @current-change="loadSettlements"
        />
      </div>
    </div>

    <el-drawer v-model="drawerVisible" :title="drawerTitle" size="600px">
      <el-form ref="formRef" :model="form" :rules="rules" :disabled="isReadonly" label-width="112px">
        <el-form-item v-if="form.settlementNo" label="结算编号">
          <el-input v-model="form.settlementNo" disabled />
        </el-form-item>
        <el-form-item label="运输任务" prop="transportOrderId">
          <TransportOrderSelect
            v-model="form.transportOrderId"
            :disabled="drawerMode !== 'create'"
            without-settlement-only
            placeholder="请选择运输任务"
          />
        </el-form-item>
        <el-form-item v-if="form.transportOrderNo" label="任务编号">
          <el-input v-model="form.transportOrderNo" disabled />
        </el-form-item>
        <el-form-item v-if="form.customerName" label="客户名称">
          <el-input v-model="form.customerName" disabled />
        </el-form-item>
        <el-form-item label="运费金额" prop="freightAmount">
          <el-input-number
            v-model="form.freightAmount"
            class="form-number"
            :min="0"
            :precision="2"
            :step="100"
            controls-position="right"
          />
        </el-form-item>
        <el-form-item label="附加费用" prop="additionalFee">
          <el-input-number
            v-model="form.additionalFee"
            class="form-number"
            :min="0"
            :precision="2"
            :step="100"
            controls-position="right"
          />
        </el-form-item>
        <el-form-item label="优惠金额" prop="discountAmount">
          <el-input-number
            v-model="form.discountAmount"
            class="form-number"
            :min="0"
            :precision="2"
            :step="100"
            controls-position="right"
          />
        </el-form-item>
        <el-form-item label="应收金额">
          <div class="amount-preview">¥ {{ formatMoney(form.receivableAmount ?? calculatedReceivable) }}</div>
        </el-form-item>
        <el-form-item label="实收金额" prop="receivedAmount">
          <el-input-number
            v-model="form.receivedAmount"
            class="form-number"
            :min="0"
            :precision="2"
            :step="100"
            controls-position="right"
          />
        </el-form-item>
        <el-form-item v-if="form.status" label="结算状态">
          <StatusTag :status="form.status" />
        </el-form-item>
        <el-form-item v-if="form.settledAt" label="结清时间">
          <el-input v-model="form.settledAt" disabled />
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
