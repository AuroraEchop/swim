<script setup lang="ts">
import { computed } from 'vue'
import { getStatusMeta } from '../constants/status'

const props = defineProps<{
  status?: string
}>()

const meta = computed(() => getStatusMeta(props.status))
</script>

<template>
  <el-tag :type="meta.type" effect="light" round>
    {{ meta.label }}
  </el-tag>
</template>
