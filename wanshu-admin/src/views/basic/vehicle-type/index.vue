<template>
  <PageContainer title="车型管理">
    <SearchForm v-model="searchForm" @search="handleSearch" @reset="handleReset">
      <el-form-item label="关键词">
        <el-input v-model="searchForm.keyword" placeholder="车型名称/编号" clearable />
      </el-form-item>
      <template #actions>
        <el-button type="primary" @click="handleAdd">新增车型</el-button>
      </template>
    </SearchForm>

    <el-table :data="tableData" v-loading="loading">
      <el-table-column prop="typeNumber" label="车型编号" width="120" />
      <el-table-column prop="typeName" label="车型名称" />
      <el-table-column prop="loadWeight" label="载重(吨)" width="100" />
      <el-table-column prop="loadVolume" label="载积(m³)" width="100" />
      <el-table-column label="尺寸(长×宽×高)" width="180">
        <template #default="{ row }">
          {{ row.length }}×{{ row.width }}×{{ row.height }} m
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
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
        <el-form-item label="车型编号" prop="typeNumber">
          <el-input v-model="formData.typeNumber" placeholder="如 AB000001" />
        </el-form-item>
        <el-form-item label="车型名称" prop="typeName">
          <el-input v-model="formData.typeName" />
        </el-form-item>
        <el-form-item label="载重(吨)">
          <el-input-number v-model="formData.loadWeight" :min="0" />
        </el-form-item>
        <el-form-item label="载积(m³)">
          <el-input-number v-model="formData.loadVolume" :min="0" />
        </el-form-item>
        <el-form-item label="长(m)">
          <el-input-number v-model="formData.length" :min="0" :precision="1" :step="0.1" />
        </el-form-item>
        <el-form-item label="宽(m)">
          <el-input-number v-model="formData.width" :min="0" :precision="1" :step="0.1" />
        </el-form-item>
        <el-form-item label="高(m)">
          <el-input-number v-model="formData.height" :min="0" :precision="1" :step="0.1" />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { vehicleApi } from '@/api/vehicle'
import PageContainer from '@/components/PageContainer.vue'
import SearchForm from '@/components/SearchForm.vue'

const loading = ref(false)
const tableData = ref<any[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()

const searchForm = reactive({ keyword: '' })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const formData = reactive<any>({
  id: undefined, typeNumber: '', typeName: '', loadWeight: 0, loadVolume: 0,
  length: 0, width: 0, height: 0
})

const rules: FormRules = {
  typeNumber: [{ required: true, message: '请输入车型编号', trigger: 'blur' }],
  typeName: [{ required: true, message: '请输入车型名称', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await vehicleApi.getTypeList({ ...searchForm, ...pagination })
    tableData.value = res.list
    pagination.total = res.total
  } catch (e) { console.error(e) } finally { loading.value = false }
}

const handleSearch = () => { pagination.pageNum = 1; loadData() }
const handleReset = () => { searchForm.keyword = ''; handleSearch() }

const handleAdd = () => {
  dialogTitle.value = '新增车型'
  Object.assign(formData, { id: undefined, typeNumber: '', typeName: '', loadWeight: 0, loadVolume: 0, length: 0, width: 0, height: 0 })
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogTitle.value = '编辑车型'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleDelete = async (row: any) => {
  await ElMessageBox.confirm('确定删除该车型吗？', '提示', { type: 'warning' })
  await vehicleApi.deleteType(row.id)
  ElMessage.success('删除成功')
  loadData()
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    if (formData.id) {
      await vehicleApi.updateType(formData.id, formData)
    } else {
      await vehicleApi.createType(formData)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  })
}

onMounted(() => loadData())
</script>
