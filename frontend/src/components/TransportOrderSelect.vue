<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { getTransportOrders, type TransportOrder } from '../api/transportOrders'

const model = defineModel<number | null | undefined>()

const props = withDefaults(
  defineProps<{
    placeholder?: string
    withoutSettlementOnly?: boolean
  }>(),
  {
    placeholder: '请选择运输任务',
    withoutSettlementOnly: false,
  },
)

const loading = ref(false)
const orders = ref<TransportOrder[]>([])

const options = computed(() => {
  if (!props.withoutSettlementOnly) {
    return orders.value
  }
  return orders.value.filter((order) => !order.settlementId)
})

async function loadOrders() {
  loading.value = true
  try {
    const result = await getTransportOrders({
      page: 1,
      pageSize: 100,
      status: '',
    })
    orders.value = result.records
  } finally {
    loading.value = false
  }
}

onMounted(loadOrders)
</script>

<template>
  <el-select v-model="model" clearable filterable :loading="loading" :placeholder="placeholder">
    <el-option
      v-for="order in options"
      :key="order.id"
      :label="`${order.orderNo}（${order.customerName}）`"
      :value="order.id"
    />
  </el-select>
</template>
