<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Delete, Edit, MoreFilled, Plus, Refresh, Search, View } from '@element-plus/icons-vue'
import DictionarySelect from '../components/DictionarySelect.vue'
import ShipSelect from '../components/ShipSelect.vue'
import StatusTag from '../components/StatusTag.vue'
import {
  createCrewMember,
  deleteCrewMember,
  getCrewMember,
  getCrewMembers,
  updateCrewMember,
  updateCrewMemberStatus,
  type CrewMember,
  type CrewStatus,
} from '../api/crewMembers'
import { crewStatusOptions, getStatusMeta } from '../constants/status'

type DrawerMode = 'create' | 'edit' | 'view'

interface CrewForm {
  id?: number
  crewNo: string
  name: string
  gender: string
  phone: string
  certificateNo: string
  position: string
  shipId: number | null
  status: CrewStatus
  remark: string
}

const loading = ref(false)
const saving = ref(false)
const drawerVisible = ref(false)
const drawerMode = ref<DrawerMode>('create')
const formRef = ref<FormInstance>()
const crewMembers = ref<CrewMember[]>([])
const total = ref(0)

const query = reactive({
  keyword: '',
  position: '',
  shipId: null as number | null,
  status: '' as CrewStatus | '',
  page: 1,
  pageSize: 10,
})

const form = reactive<CrewForm>({
  crewNo: '',
  name: '',
  gender: '',
  phone: '',
  certificateNo: '',
  position: '',
  shipId: null,
  status: 'UNASSIGNED',
  remark: '',
})

const rules: FormRules<CrewForm> = {
  crewNo: [
    { required: true, message: '请输入船员编号', trigger: 'blur' },
    { min: 3, max: 30, message: '船员编号长度为 3 到 30 位', trigger: 'blur' },
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 30, message: '姓名长度为 2 到 30 位', trigger: 'blur' },
  ],
  certificateNo: [{ required: true, message: '请输入证件编号', trigger: 'blur' }],
  position: [{ required: true, message: '请选择岗位', trigger: 'change' }],
  status: [{ required: true, message: '请选择船员状态', trigger: 'change' }],
}

const drawerTitle = computed(() => {
  const titleMap: Record<DrawerMode, string> = {
    create: '新增船员',
    edit: '编辑船员',
    view: '船员详情',
  }
  return titleMap[drawerMode.value]
})

const isReadonly = computed(() => drawerMode.value === 'view')

function resetForm() {
  Object.assign(form, {
    id: undefined,
    crewNo: '',
    name: '',
    gender: '',
    phone: '',
    certificateNo: '',
    position: '',
    shipId: null,
    status: 'UNASSIGNED',
    remark: '',
  })
  formRef.value?.clearValidate()
}

function fillForm(member: CrewMember) {
  Object.assign(form, {
    id: member.id,
    crewNo: member.crewNo,
    name: member.name,
    gender: member.gender || '',
    phone: member.phone || '',
    certificateNo: member.certificateNo,
    position: member.position,
    shipId: member.shipId || null,
    status: member.status,
    remark: member.remark || '',
  })
}

async function loadCrewMembers() {
  loading.value = true
  try {
    const result = await getCrewMembers({
      keyword: query.keyword || undefined,
      position: query.position || undefined,
      shipId: query.shipId || undefined,
      status: query.status,
      page: query.page,
      pageSize: query.pageSize,
    })
    crewMembers.value = result.records
    total.value = result.total
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  loadCrewMembers()
}

function handleReset() {
  Object.assign(query, {
    keyword: '',
    position: '',
    shipId: null,
    status: '',
    page: 1,
    pageSize: 10,
  })
  loadCrewMembers()
}

function openCreate() {
  drawerMode.value = 'create'
  drawerVisible.value = true
  resetForm()
}

async function openDetail(row: CrewMember, mode: DrawerMode) {
  drawerMode.value = mode
  drawerVisible.value = true
  resetForm()
  const detail = await getCrewMember(row.id)
  fillForm(detail)
}

async function handleSubmit() {
  if (isReadonly.value) {
    drawerVisible.value = false
    return
  }

  await formRef.value?.validate()

  saving.value = true
  try {
    const payload = {
      name: form.name,
      gender: form.gender || undefined,
      phone: form.phone || undefined,
      certificateNo: form.certificateNo,
      position: form.position,
      shipId: form.shipId || undefined,
      status: form.status,
      remark: form.remark || undefined,
    }
    if (drawerMode.value === 'create') {
      await createCrewMember({ crewNo: form.crewNo, ...payload })
      ElMessage.success('船员新增成功')
    } else if (form.id) {
      await updateCrewMember(form.id, payload)
      ElMessage.success('船员修改成功')
    }
    drawerVisible.value = false
    await loadCrewMembers()
  } finally {
    saving.value = false
  }
}

async function handleDelete(row: CrewMember) {
  await ElMessageBox.confirm(`确认删除船员“${row.name}”吗？`, '删除确认', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    confirmButtonClass: 'el-button--danger',
  })
  await deleteCrewMember(row.id)
  ElMessage.success('船员删除成功')
  await loadCrewMembers()
}

async function handleStatusCommand(command: unknown, row: CrewMember) {
  const status = command as CrewStatus
  if (status === row.status) {
    return
  }
  const next = getStatusMeta(status).label
  await ElMessageBox.confirm(`确认将“${row.name}”状态改为“${next}”吗？`, '状态变更', {
    type: 'warning',
    confirmButtonText: '确认',
    cancelButtonText: '取消',
  })
  await updateCrewMemberStatus(row.id, status)
  ElMessage.success('状态已更新')
  await loadCrewMembers()
}

function createStatusHandler(row: CrewMember) {
  return (command: string | number | object) => handleStatusCommand(command, row)
}

onMounted(loadCrewMembers)
</script>

<template>
  <section class="module-page">
    <div class="page-heading">
      <div>
        <h2>船员管理</h2>
        <p>维护船员档案、岗位、所属船舶和在岗状态。</p>
      </div>
      <el-button type="primary" :icon="Plus" @click="openCreate">新增船员</el-button>
    </div>

    <div class="search-panel">
      <el-input
        v-model="query.keyword"
        class="search-input"
        clearable
        placeholder="姓名或证件编号"
        :prefix-icon="Search"
        @keyup.enter="handleSearch"
      />
      <DictionarySelect v-model="query.position" class="search-input" type="CREW_POSITION" placeholder="岗位" />
      <ShipSelect v-model="query.shipId" class="search-input" placeholder="所属船舶" />
      <el-select v-model="query.status" class="search-input" clearable placeholder="船员状态">
        <el-option v-for="item in crewStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
      <el-button :icon="Refresh" @click="handleReset">重置</el-button>
    </div>

    <div class="content-panel">
      <div class="table-toolbar">
        <div>
          <strong>船员列表</strong>
          <span>共 {{ total }} 条记录</span>
        </div>
      </div>

      <el-table v-loading="loading" :data="crewMembers" empty-text="暂无船员数据" table-layout="fixed">
        <el-table-column prop="crewNo" label="船员编号" min-width="130" />
        <el-table-column prop="name" label="姓名" min-width="100" />
        <el-table-column prop="gender" label="性别" width="80" />
        <el-table-column prop="position" label="岗位" width="110" />
        <el-table-column prop="shipName" label="所属船舶" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ row.shipName || '未分配' }}</template>
        </el-table-column>
        <el-table-column prop="phone" label="联系电话" min-width="140" />
        <el-table-column prop="certificateNo" label="证件编号" min-width="150" show-overflow-tooltip />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <StatusTag :status="row.status" />
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="230">
          <template #default="{ row }">
            <el-button link type="primary" :icon="View" @click="openDetail(row, 'view')">查看</el-button>
            <el-button link type="primary" :icon="Edit" @click="openDetail(row, 'edit')">编辑</el-button>
            <el-dropdown trigger="click" @command="createStatusHandler(row)">
              <el-button link type="primary" :icon="MoreFilled">状态</el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    v-for="item in crewStatusOptions"
                    :key="item.value"
                    :command="item.value"
                    :disabled="item.value === row.status"
                  >
                    {{ item.label }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button link type="danger" :icon="Delete" @click="handleDelete(row)">删除</el-button>
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
          @size-change="loadCrewMembers"
          @current-change="loadCrewMembers"
        />
      </div>
    </div>

    <el-drawer v-model="drawerVisible" :title="drawerTitle" size="560px">
      <el-form ref="formRef" :model="form" :rules="rules" :disabled="isReadonly" label-width="96px">
        <el-form-item label="船员编号" prop="crewNo">
          <el-input v-model="form.crewNo" :disabled="drawerMode !== 'create'" placeholder="例如 CREW-003" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select v-model="form.gender" clearable placeholder="请选择性别">
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
          </el-select>
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="证件编号" prop="certificateNo">
          <el-input v-model="form.certificateNo" placeholder="请输入证件编号" />
        </el-form-item>
        <el-form-item label="岗位" prop="position">
          <DictionarySelect v-model="form.position" type="CREW_POSITION" placeholder="请选择岗位" />
        </el-form-item>
        <el-form-item label="所属船舶" prop="shipId">
          <ShipSelect v-model="form.shipId" enabled-only placeholder="请选择所属船舶" />
        </el-form-item>
        <el-form-item label="船员状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择船员状态">
            <el-option v-for="item in crewStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
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
