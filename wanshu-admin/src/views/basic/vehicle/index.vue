<template>
  <PageContainer title="车辆管理">
    <SearchForm v-model="searchForm" @search="handleSearch" @reset="handleReset">
      <el-form-item label="关键词">
        <el-input v-model="searchForm.keyword" placeholder="车牌号/车辆编号" clearable />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="searchForm.status" placeholder="请选择" clearable>
          <el-option label="可用" :value="1" />
          <el-option label="停用" :value="0" />
        </el-select>
      </el-form-item>
      <template #actions>
        <el-button type="primary" @click="handleAdd">新增车辆</el-button>
      </template>
    </SearchForm>

    <el-table :data="tableData" v-loading="loading">
      <el-table-column prop="vehicleNumber" label="车辆编号" width="120" />
      <el-table-column prop="licensePlate" label="车牌号" width="120" />
      <el-table-column prop="vehicleTypeId" label="车型" width="120">
        <template #default="{ row }">
          {{ vehicleTypeMap[String(row.vehicleTypeId)] || row.vehicleTypeId || '—' }}
        </template>
      </el-table-column>
      <el-table-column prop="loadWeight" label="载重(吨)" width="90" />
      <el-table-column prop="loadVolume" label="载积(m³)" width="90" />
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-switch
            :model-value="row.status === 1"
            inline-prompt
            active-text="可用"
            inactive-text="停用"
            @change="(v: string | number | boolean) => handleStatusChange(row, Boolean(v))"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="primary" @click="openBindDriverDialog(row)">绑定司机</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="车辆编号" prop="vehicleNumber">
          <el-input v-model="formData.vehicleNumber" placeholder="如 CL000001" />
        </el-form-item>
        <el-form-item label="车牌号" prop="licensePlate">
          <el-input v-model="formData.licensePlate" />
        </el-form-item>
        <el-form-item label="车型" prop="vehicleTypeId">
          <el-select v-model="formData.vehicleTypeId" placeholder="请选择车型">
            <el-option
              v-for="t in vehicleTypes"
              :key="String(t.id)"
              :label="t.typeName || ''"
              :value="t.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="载重(吨)">
          <el-input-number v-model="formData.loadWeight" :min="0" />
        </el-form-item>
        <el-form-item label="载积(m³)">
          <el-input-number v-model="formData.loadVolume" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">可用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="bindDialogVisible" title="绑定司机" width="560px" destroy-on-close @closed="onBindDialogClosed">
      <template v-if="bindVehicle">
        <p class="bind-tip">
          车辆：<strong>{{ bindVehicle.licensePlate || bindVehicle.vehicleNumber }}</strong>
        </p>
        <h4 class="bind-section">已绑定</h4>
        <el-table :data="boundDriverRows" v-loading="bindLoading" size="small" max-height="220" empty-text="暂无">
          <el-table-column prop="label" label="司机" />
          <el-table-column label="操作" width="80">
            <template #default="{ row }">
              <el-button link type="danger" size="small" @click="unbindOne(row.id)">解绑</el-button>
            </template>
          </el-table-column>
        </el-table>
        <h4 class="bind-section">添加司机</h4>
        <el-select
          v-model="selectedDriverIdsToAdd"
          multiple
          filterable
          placeholder="选择司机（可多选，已绑定不会出现）"
          style="width: 100%"
          :loading="driverListLoading"
        >
          <el-option
            v-for="d in selectableDrivers"
            :key="String(d.id)"
            :label="driverOptionLabel(d)"
            :value="String(d.id)"
          />
        </el-select>
        <div class="bind-actions">
          <el-button type="primary" :loading="bindSubmitLoading" :disabled="!selectedDriverIdsToAdd.length" @click="confirmBindDrivers">
            绑定选中司机
          </el-button>
        </div>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { vehicleApi } from '@/api/vehicle'
import { employeeApi, type EmpDriverVO } from '@/api/employee'
import type { Vehicle } from '@/types'
import PageContainer from '@/components/PageContainer.vue'
import SearchForm from '@/components/SearchForm.vue'

const loading = ref(false)
const tableData = ref<Vehicle[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const vehicleTypes = ref<any[]>([])

const vehicleTypeMap = computed(() => {
  const map: Record<string, string> = {}
  vehicleTypes.value.forEach((t) => {
    if (t.id != null) map[String(t.id)] = t.typeName || ''
  })
  return map
})

const searchForm = reactive({ keyword: '', status: undefined as number | undefined })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const formData = reactive<any>({
  id: undefined,
  vehicleNumber: '',
  licensePlate: '',
  vehicleTypeId: undefined,
  loadWeight: 0,
  loadVolume: 0,
  status: 1
})

const rules: FormRules = {
  vehicleNumber: [{ required: true, message: '请输入车辆编号', trigger: 'blur' }],
  licensePlate: [{ required: true, message: '请输入车牌号', trigger: 'blur' }],
  vehicleTypeId: [{ required: true, message: '请选择车型', trigger: 'change' }]
}

const bindDialogVisible = ref(false)
const bindVehicle = ref<Vehicle | null>(null)
const boundDriverIds = ref<string[]>([])
const bindLoading = ref(false)
const driverListLoading = ref(false)
const allDrivers = ref<EmpDriverVO[]>([])
const selectedDriverIdsToAdd = ref<string[]>([])
const bindSubmitLoading = ref(false)

const driverById = computed(() => {
  const m = new Map<string, EmpDriverVO>()
  for (const d of allDrivers.value) {
    m.set(String(d.id), d)
  }
  return m
})

const boundDriverRows = computed(() =>
  boundDriverIds.value.map((id) => ({
    id,
    label: driverOptionLabel(driverById.value.get(id)) || `司机 #${id}`
  }))
)

const selectableDrivers = computed(() => {
  const bound = new Set(boundDriverIds.value)
  return allDrivers.value.filter((d) => d.id != null && !bound.has(String(d.id)))
})

function driverOptionLabel(d?: EmpDriverVO) {
  if (!d) return ''
  const name = d.realName || d.username || ''
  const phone = d.phone ? ` ${d.phone}` : ''
  return `${name}${phone}`.trim() || String(d.id)
}

const loadVehicleTypes = async () => {
  try {
    vehicleTypes.value = await vehicleApi.getAllTypes()
  } catch (e) {
    console.error(e)
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await vehicleApi.getList({ ...searchForm, ...pagination })
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
  searchForm.status = undefined
  handleSearch()
}

const handleStatusChange = async (row: Vehicle, enabled: boolean) => {
  const next = enabled ? 1 : 0
  if (row.status === next) return
  const id = row.id != null ? String(row.id) : ''
  if (!id) return
  try {
    await vehicleApi.updateVehicleStatus(id, next)
    row.status = next
    ElMessage.success(next === 1 ? '已设为可用' : '已停用')
  } catch {
    loadData()
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增车辆'
  Object.assign(formData, {
    id: undefined,
    vehicleNumber: '',
    licensePlate: '',
    vehicleTypeId: undefined,
    loadWeight: 0,
    loadVolume: 0,
    status: 1
  })
  dialogVisible.value = true
}

const handleEdit = (row: Vehicle) => {
  dialogTitle.value = '编辑车辆'
  Object.assign(formData, {
    ...row,
    vehicleTypeId: row.vehicleTypeId,
    status: row.status ?? 1
  })
  dialogVisible.value = true
}

const handleDelete = async (row: Vehicle) => {
  await ElMessageBox.confirm('确定删除该车辆吗？', '提示', { type: 'warning' })
  if (row.id == null) return
  await vehicleApi.delete(String(row.id))
  ElMessage.success('删除成功')
  loadData()
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    const payload = { ...formData }
    if (formData.id) {
      await vehicleApi.update(String(formData.id), payload)
    } else {
      await vehicleApi.create(payload)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  })
}

const loadAllDrivers = async () => {
  driverListLoading.value = true
  try {
    const res = await employeeApi.getDriverList({ pageNum: 1, pageSize: 500 })
    allDrivers.value = res.list || []
  } catch (e) {
    console.error(e)
    allDrivers.value = []
  } finally {
    driverListLoading.value = false
  }
}

const refreshBoundIds = async () => {
  if (!bindVehicle.value?.id) {
    boundDriverIds.value = []
    return
  }
  bindLoading.value = true
  try {
    const ids = await vehicleApi.getDriverIds(String(bindVehicle.value.id))
    boundDriverIds.value = (ids || []).map((x) => String(x))
  } catch (e) {
    console.error(e)
    boundDriverIds.value = []
  } finally {
    bindLoading.value = false
  }
}

const openBindDriverDialog = async (row: Vehicle) => {
  bindVehicle.value = row
  selectedDriverIdsToAdd.value = []
  bindDialogVisible.value = true
  await loadAllDrivers()
  await refreshBoundIds()
}

const onBindDialogClosed = () => {
  bindVehicle.value = null
  boundDriverIds.value = []
  selectedDriverIdsToAdd.value = []
}

const unbindOne = async (driverId: string) => {
  if (!bindVehicle.value?.id) return
  try {
    await vehicleApi.unbindDriver(String(bindVehicle.value.id), driverId)
    ElMessage.success('已解绑')
    await refreshBoundIds()
    loadData()
  } catch (e) {
    console.error(e)
  }
}

const confirmBindDrivers = async () => {
  if (!bindVehicle.value?.id || !selectedDriverIdsToAdd.value.length) return
  bindSubmitLoading.value = true
  try {
    await vehicleApi.bindDrivers(String(bindVehicle.value.id), selectedDriverIdsToAdd.value)
    ElMessage.success('绑定成功')
    selectedDriverIdsToAdd.value = []
    await refreshBoundIds()
    loadData()
  } catch (e) {
    console.error(e)
  } finally {
    bindSubmitLoading.value = false
  }
}

onMounted(() => {
  loadVehicleTypes()
  loadData()
})
</script>

<style scoped>
.bind-tip {
  margin-bottom: 12px;
}
.bind-section {
  font-size: 14px;
  margin: 12px 0 8px;
  font-weight: 600;
}
.bind-actions {
  margin-top: 16px;
}
</style>
