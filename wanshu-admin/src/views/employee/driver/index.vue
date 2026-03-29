<template>
  <PageContainer title="司机管理（仅查看，可分配车辆）">
    <SearchForm v-model="searchForm" @search="handleSearch" @reset="handleReset">
      <el-form-item label="关键词">
        <el-input v-model="searchForm.keyword" placeholder="用户名/姓名/手机" clearable />
      </el-form-item>
      <el-form-item label="所属机构">
        <el-select
          v-model="searchForm.organId"
          placeholder="请选择机构"
          clearable
          filterable
          style="width: 220px"
        >
          <el-option
            v-for="o in flatOrgOptions"
            :key="o.value"
            :label="o.label"
            :value="o.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="工作状态">
        <el-select v-model="searchForm.workStatus" placeholder="请选择" clearable>
          <el-option label="上班" :value="1" />
          <el-option label="休息" :value="0" />
        </el-select>
      </el-form-item>
    </SearchForm>

    <el-table :data="tableData" v-loading="loading">
      <el-table-column prop="id" label="用户ID" width="160" show-overflow-tooltip />
      <el-table-column prop="username" label="登录账号" width="120" />
      <el-table-column prop="realName" label="姓名" width="100" />
      <el-table-column prop="phone" label="手机" width="120" />
      <el-table-column prop="organName" label="所属机构" min-width="140" show-overflow-tooltip />
      <el-table-column prop="vehicleTypes" label="擅长车型" min-width="120" show-overflow-tooltip />
      <el-table-column prop="licenseImage" label="驾驶证" min-width="100">
        <template #default="{ row }">
          <el-link v-if="row.licenseImage" type="primary" :href="row.licenseImage" target="_blank">查看</el-link>
          <span v-else>—</span>
        </template>
      </el-table-column>
      <el-table-column prop="workStatus" label="工作状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.workStatus === 1 ? 'success' : 'info'">
            {{ row.workStatus === 1 ? '上班' : '休息' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openVehicleDialog(row)">分配车辆</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="pagination.pageNum"
      v-model:page-size="pagination.pageSize"
      :total="pagination.total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="loadData"
      @current-change="loadData"
    />

    <el-dialog
      v-model="vehicleDialogVisible"
      title="分配车辆"
      width="640px"
      destroy-on-close
      @closed="vehicleDialogDriver = null"
    >
      <el-alert type="warning" show-icon :closable="false" class="mb-3">
        <template #title>绑定条件</template>
        司机信息已完善（姓名、手机、机构、擅长车型、驾驶证）；司机为<strong>上班</strong>状态（视为已排班）；仅可选择<strong>停用</strong>状态车辆（不可用/启用车辆不可绑定）。
      </el-alert>
      <div v-if="vehicleDialogDriver">
        <p class="driver-line">
          <strong>{{ vehicleDialogDriver.realName || vehicleDialogDriver.username }}</strong>
          <el-tag v-if="vehicleDialogDriver.workStatus !== 1" type="danger" size="small" class="ml-2">
            当前非上班，绑定可能失败
          </el-tag>
        </p>
        <h4 class="section-title">已绑定车辆</h4>
        <el-table :data="boundVehicles" v-loading="boundLoading" size="small" max-height="200">
          <el-table-column prop="licensePlate" label="车牌" />
          <el-table-column prop="vehicleNumber" label="车辆编号" />
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              {{ vehicleStatusLabel(row.status) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80">
            <template #default="{ row }">
              <el-button link type="danger" size="small" @click="handleUnbind(row)">解绑</el-button>
            </template>
          </el-table-column>
        </el-table>
        <h4 class="section-title mt-3">绑定停用车辆</h4>
        <el-select
          v-model="selectedVehicleId"
          placeholder="选择停用状态车辆"
          filterable
          clearable
          style="width: 100%"
          :loading="eligibleLoading"
        >
          <el-option
            v-for="v in eligibleVehicles"
            :key="String(v.id)"
            :label="`${v.licensePlate || v.vehicleNumber || ''} (${vehicleStatusLabel(v.status)})`"
            :value="String(v.id)"
          />
        </el-select>
      </div>
      <template #footer>
        <el-button @click="vehicleDialogVisible = false">关闭</el-button>
        <el-button type="primary" :loading="bindLoading" :disabled="!selectedVehicleId" @click="handleBind">
          绑定
        </el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { employeeApi, type EmpDriverVO } from '@/api/employee'
import { organizationApi } from '@/api/organization'
import { vehicleApi } from '@/api/vehicle'
import PageContainer from '@/components/PageContainer.vue'
import SearchForm from '@/components/SearchForm.vue'

type TreeOrg = Record<string, unknown> & { id?: string | number; name?: string; children?: TreeOrg[] }

/** 与后端 BaseVehicle 一致（部分字段） */
type VehicleRow = {
  id?: string | number
  vehicleNumber?: string
  licensePlate?: string
  status?: number
}

const loading = ref(false)
const tableData = ref<EmpDriverVO[]>([])
const flatOrgOptions = ref<{ value: string; label: string }[]>([])

const searchForm = reactive({
  keyword: '',
  organId: undefined as string | undefined,
  workStatus: undefined as number | undefined
})
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const vehicleDialogVisible = ref(false)
const vehicleDialogDriver = ref<EmpDriverVO | null>(null)
const boundVehicles = ref<VehicleRow[]>([])
const boundLoading = ref(false)
const eligibleVehicles = ref<VehicleRow[]>([])
const eligibleLoading = ref(false)
const selectedVehicleId = ref<string>('')
const bindLoading = ref(false)

function vehicleStatusLabel(status?: number) {
  if (status === 1) return '可用'
  if (status === 0) return '停用'
  return String(status ?? '—')
}

function flattenOrgTree(nodes: TreeOrg[], depth = 0): { value: string; label: string }[] {
  const out: { value: string; label: string }[] = []
  for (const n of nodes) {
    const id = n.id != null ? String(n.id) : ''
    const name = String(n.name ?? '')
    if (id) {
      out.push({ value: id, label: `${'　'.repeat(depth)}${name}` })
    }
    if (n.children?.length) {
      out.push(...flattenOrgTree(n.children, depth + 1))
    }
  }
  return out
}

const loadOrgTree = async () => {
  try {
    const tree = (await organizationApi.getTree()) as unknown as TreeOrg[]
    flatOrgOptions.value = flattenOrgTree(Array.isArray(tree) ? tree : [])
  } catch (e) {
    console.error(e)
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const params: Record<string, unknown> = { ...pagination }
    if (searchForm.keyword) params.keyword = searchForm.keyword
    if (searchForm.organId) params.organId = searchForm.organId
    if (searchForm.workStatus !== undefined) params.workStatus = searchForm.workStatus
    const res = await employeeApi.getDriverList(params as Parameters<typeof employeeApi.getDriverList>[0])
    tableData.value = res.list
    pagination.total = res.total
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  loadData()
}
const handleReset = () => {
  searchForm.keyword = ''
  searchForm.organId = undefined
  searchForm.workStatus = undefined
  handleSearch()
}

const loadBound = async (driverId: string | number) => {
  boundLoading.value = true
  try {
    const list = await employeeApi.getDriverBoundVehicles(driverId)
    boundVehicles.value = (list || []) as VehicleRow[]
  } catch (e) {
    console.error(e)
    boundVehicles.value = []
  } finally {
    boundLoading.value = false
  }
}

const loadEligible = async () => {
  eligibleLoading.value = true
  try {
    const res = await vehicleApi.getList({ pageNum: 1, pageSize: 500, status: 0 })
    eligibleVehicles.value = (res.list || []) as VehicleRow[]
  } catch (e) {
    console.error(e)
    eligibleVehicles.value = []
  } finally {
    eligibleLoading.value = false
  }
}

const openVehicleDialog = async (row: EmpDriverVO) => {
  vehicleDialogDriver.value = row
  selectedVehicleId.value = ''
  vehicleDialogVisible.value = true
  await Promise.all([loadBound(row.id), loadEligible()])
}

const handleUnbind = async (row: VehicleRow) => {
  if (!vehicleDialogDriver.value || row.id == null) return
  await ElMessageBox.confirm('确定解绑该车辆？', '提示', { type: 'warning' })
  await employeeApi.unbindDriverVehicle(vehicleDialogDriver.value.id, row.id)
  ElMessage.success('已解绑')
  await loadBound(vehicleDialogDriver.value.id)
  await loadEligible()
}

const handleBind = async () => {
  if (!vehicleDialogDriver.value || !selectedVehicleId.value) return
  bindLoading.value = true
  try {
    await employeeApi.bindDriverVehicle(vehicleDialogDriver.value.id, selectedVehicleId.value)
    ElMessage.success('绑定成功')
    selectedVehicleId.value = ''
    await loadBound(vehicleDialogDriver.value.id)
    await loadEligible()
  } catch (e) {
    console.error(e)
  } finally {
    bindLoading.value = false
  }
}

onMounted(async () => {
  await loadOrgTree()
  loadData()
})
</script>

<style scoped>
.mb-3 {
  margin-bottom: 12px;
}
.mt-3 {
  margin-top: 12px;
}
.section-title {
  font-size: 14px;
  margin: 8px 0;
  font-weight: 600;
}
.driver-line {
  margin-bottom: 12px;
}
.ml-2 {
  margin-left: 8px;
}
</style>
