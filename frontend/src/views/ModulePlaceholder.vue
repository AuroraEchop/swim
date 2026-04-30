<script setup lang="ts">
import { Plus, Search } from '@element-plus/icons-vue'
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const title = computed(() => String(route.meta.title || '业务模块'))

const moduleHints: Record<string, string[]> = {
  ships: ['船舶编号', '船名', '船舶类型', '载重量', '所属港口', '状态'],
  'crew-members': ['船员编号', '姓名', '岗位', '所属船舶', '联系方式', '状态'],
  'transport-orders': ['任务编号', '货物名称', '航线', '承运船舶', '运输状态', '结算状态'],
  settlements: ['结算编号', '运输任务', '客户', '应收金额', '实收金额', '结算状态'],
  dictionaries: ['字典类型', '显示名称', '字典值', '排序号', '启用状态', '备注'],
  users: ['用户名', '真实姓名', '联系电话', '邮箱', '角色', '状态'],
  roles: ['角色名称', '角色编码', '角色说明', '内置角色', '操作'],
}

const fields = computed(() => moduleHints[String(route.meta.module)] || [])
</script>

<template>
  <section class="module-page">
    <div class="page-heading">
      <div>
        <h2>{{ title }}</h2>
        <p>页面框架已按正式后台风格搭建，下一步接入对应业务接口。</p>
      </div>
      <el-button type="primary" :icon="Plus">新增</el-button>
    </div>

    <div class="search-panel">
      <el-input class="search-input" placeholder="请输入关键词" :prefix-icon="Search" />
      <el-button type="primary">查询</el-button>
      <el-button>重置</el-button>
    </div>

    <div class="content-panel">
      <el-table :data="[]" empty-text="暂无业务数据">
        <el-table-column v-for="field in fields" :key="field" :label="field" min-width="140" />
        <el-table-column label="操作" fixed="right" width="180">
          <template #default>
            <el-button link type="primary">查看</el-button>
            <el-button link type="primary">编辑</el-button>
            <el-button link type="danger">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </section>
</template>
