<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { getShips, type Ship } from '../api/ships'

const model = defineModel<number | null | undefined>()

const props = withDefaults(
  defineProps<{
    placeholder?: string
    enabledOnly?: boolean
  }>(),
  {
    placeholder: '请选择船舶',
    enabledOnly: false,
  },
)

const loading = ref(false)
const ships = ref<Ship[]>([])

const options = computed(() => {
  if (!props.enabledOnly) {
    return ships.value
  }
  return ships.value.filter((ship) => ship.status !== 'DISABLED')
})

async function loadShips() {
  loading.value = true
  try {
    const result = await getShips({
      page: 1,
      pageSize: 100,
      status: '',
    })
    ships.value = result.records
  } finally {
    loading.value = false
  }
}

onMounted(loadShips)
</script>

<template>
  <el-select v-model="model" clearable filterable :loading="loading" :placeholder="placeholder">
    <el-option
      v-for="ship in options"
      :key="ship.id"
      :label="`${ship.shipName}（${ship.shipNo}）`"
      :value="ship.id"
    />
  </el-select>
</template>
