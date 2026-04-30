<script setup lang="ts">
import { Money, Refresh, Ship, Tickets, TrendCharts, UserFilled } from '@element-plus/icons-vue'
import { computed, onMounted, ref } from 'vue'
import { getDashboardSummary, getRecentTransportOrders, type DashboardSummary, type RecentTransportOrder } from '../api/dashboard'

const loading = ref(false)
const summary = ref<DashboardSummary>({
  shipCount: 0,
  crewCount: 0,
  pendingTransportCount: 0,
  inTransitCount: 0,
  unsettledCount: 0,
  totalReceivableAmount: 0,
  totalReceivedAmount: 0,
})
const recentOrders = ref<RecentTransportOrder[]>([])

const collectionRate = computed(() => {
  if (!summary.value.totalReceivableAmount) {
    return '0.0%'
  }
  return `${((summary.value.totalReceivedAmount / summary.value.totalReceivableAmount) * 100).toFixed(1)}%`
})

const metrics = computed(() => [
  { label: '船舶数量', value: summary.value.shipCount, icon: Ship },
  { label: '船员数量', value: summary.value.crewCount, icon: UserFilled },
  { label: '待出发任务', value: summary.value.pendingTransportCount, icon: Tickets },
  { label: '运输中任务', value: summary.value.inTransitCount, icon: TrendCharts },
  { label: '未结清记录', value: summary.value.unsettledCount, icon: Money },
])

function formatMoney(value: number) {
  return value.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

async function loadDashboard() {
  loading.value = true
  try {
    const [summaryData, orders] = await Promise.all([getDashboardSummary(), getRecentTransportOrders(5)])
    summary.value = summaryData
    recentOrders.value = orders
  } finally {
    loading.value = false
  }
}

onMounted(loadDashboard)
</script>

<template>
  <div class="dashboard" v-loading="loading">
    <section class="dashboard-summary">
      <div v-for="item in metrics" :key="item.label" class="metric-block">
        <div class="metric-icon">
          <el-icon><component :is="item.icon" /></el-icon>
        </div>
        <div>
          <p>{{ item.label }}</p>
          <strong>{{ item.value }}</strong>
        </div>
      </div>
    </section>

    <section class="amount-panel">
      <div>
        <p>应收总金额</p>
        <strong>¥ {{ formatMoney(summary.totalReceivableAmount) }}</strong>
      </div>
      <div>
        <p>实收总金额</p>
        <strong>¥ {{ formatMoney(summary.totalReceivedAmount) }}</strong>
      </div>
      <div>
        <p>回款比例</p>
        <strong>{{ collectionRate }}</strong>
      </div>
      <el-button :icon="Refresh" @click="loadDashboard">刷新数据</el-button>
    </section>

    <section class="content-panel">
      <div class="panel-title">
        <div>
          <h2>近期运输任务</h2>
          <p>按预计出发时间展示最近任务</p>
        </div>
        <RouterLink to="/transport-orders">查看全部</RouterLink>
      </div>

      <el-table :data="recentOrders" empty-text="暂无运输任务" table-layout="fixed">
        <el-table-column prop="orderNo" label="任务编号" min-width="170" />
        <el-table-column prop="cargoName" label="货物" min-width="140" />
        <el-table-column prop="shipName" label="船舶" min-width="130" />
        <el-table-column label="航线" min-width="180">
          <template #default="{ row }">{{ row.originPort }} 至 {{ row.destinationPort }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110" />
        <el-table-column prop="plannedDepartureTime" label="预计出发" min-width="170" />
      </el-table>
    </section>
  </div>
</template>
