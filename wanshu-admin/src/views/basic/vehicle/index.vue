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
          {{ vehicleTypeMap[row.vehicleTypeId] || row.vehicleTypeId }}
        </template>
      </el-table-column>
      <el-table-column prop="loadWeight" label="载重(吨)" width="90" />
      <el-table-column prop="loadVolume" label="载积(m³)" width="90" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '可用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="primary" @click="handleBindDriver(row)">绑定司机</el-button>
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
            <el-option v-for="t in vehicleTypes" :key="t.id" :label="t.typeName" :value="t.id" />
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
  </PageContainer>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { vehicleApi } from '@/api/vehicle'
import PageContainer from '@/components/PageContainer.vue'
import SearchForm from '@/components/SearchForm.vue'

const loading = ref(false)
const tableData = ref<any[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const vehicleTypes = ref<any[]>([])

const vehicleTypeMap = computed(() => {
  const map: Record<number, string> = {}
  vehicleTypes.value.forEach(t => { map[t.id] = t.typeName })
  return map
})

const searchForm = reactive({ keyword: '', status: undefined as number | undefined })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const formData = reactive<any>({
  id: undefined, vehicleNumber: '', licensePlate: '', vehicleTypeId: undefined,
  loadWeight: 0, loadVolume: 0, status: 1
})

const rules: FormRules = {
  vehicleNumber: [{ required: true, message: '请输入车辆编号', trigger: 'blur' }],
  licensePlate: [{ required: true, message: '请输入车牌号', trigger: 'blur' }],
  vehicleTypeId: [{ required: true, message: '请选择车型', trigger: 'change' }]
}

const loadVehicleTypes = async () => {
  try { vehicleTypes.value = await vehicleApi.getAllTypes() } catch (e) { console.error(e) }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await vehicleApi.getList({ ...searchForm, ...pagination })
    tableData.value = res.list
    pagination.total = res.total
  } catch (e) { console.error(e) } finally { loading.value = false }
}

const handleSearch = () => { pagination.pageNum = 1; loadData() }
const handleReset = () => { searchForm.keyword = ''; searchForm.status = undefined; handleSearch() }

const handleAdd = () => {
  dialogTitle.value = '新增车辆'
  Object.assign(formData, { id: undefined, vehicleNumber: '', licensePlate: '', vehicleTypeId: undefined, loadWeight: 0, loadVolume: 0, status: 1 })
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogTitle.value = '编辑车辆'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleDelete = async (row: any) => {
  await ElMessageBox.confirm('确定删除该车辆吗？', '提示', { type: 'warning' })
  await vehicleApi.delete(row.id)
  ElMessage.success('删除成功')
  loadData()
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    if (formData.id) {
      await vehicleApi.update(formData.id, formData)
    } else {
      await vehicleApi.create(formData)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  })
}

const handleBindDriver = (_row: any) => {
  ElMessage.info('司机绑定功能将在员工管理模块完成后启用')
}

onMounted(() => { loadVehicleTypes(); loadData() })
</script>
