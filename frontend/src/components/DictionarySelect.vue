<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { getDictionaryItems, type DictionaryItem } from '../api/dictionaries'

const model = defineModel<string | null | undefined>()

const props = withDefaults(
  defineProps<{
    type: string
    placeholder?: string
    optionValue?: 'label' | 'value'
    enabledOnly?: boolean
  }>(),
  {
    placeholder: '请选择',
    optionValue: 'label',
    enabledOnly: true,
  },
)

const loading = ref(false)
const options = ref<DictionaryItem[]>([])

const visibleOptions = computed(() => {
  if (!props.enabledOnly) {
    return options.value
  }
  return options.value.filter((item) => item.enabled)
})

async function loadOptions() {
  loading.value = true
  try {
    options.value = await getDictionaryItems(props.type)
  } finally {
    loading.value = false
  }
}

onMounted(loadOptions)
</script>

<template>
  <el-select v-model="model" clearable filterable :loading="loading" :placeholder="placeholder">
    <el-option
      v-for="item in visibleOptions"
      :key="item.id"
      :label="item.label"
      :value="item[optionValue]"
    />
  </el-select>
</template>
