<template>
  <PageContainer title="快递员管理">
    <SearchForm v-model="searchForm" @search="handleSearch" @reset="handleReset">
      <el-form-item label="所属营业部">
        <el-input v-model="searchForm.organId" placeholder="营业部ID" clearable />
      </el-form-item>
      <el-form-item label="工作状态">
        <el-select v-model="searchForm.workStatus" placeholder="请选择" clearable>
          <el-option label="上班" :value="1" />
          <el-option label="休息" :value="0" />
        </el-select>
      </el-form-item>
      <template #actions>
        <el-button type="primary" @click="handleAdd">新增快递员</el-button>
      </template>
    </SearchForm>

    <el-table :data="tableData" v-loading="loading">
      <el-table-column prop="id" label="快递员ID" width="180" />
      <el-table-column prop="employeeNo" label="工号" width="120" />
      <el-table-column prop="organId" label="所属营业部" width="180" />
      <el-table-column prop="idCard" label="身份证号" width="180" />
      <el-table-column prop="workStatus" label="工作状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.workStatus === 1 ? 'success' : 'info'">
            {{ row.workStatus === 1 ? '上班' : '休息' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="primary" @click="handleToggleStatus(row)">
            {{ row.workStatus === 1 ? '设为休息' : '设为上班' }}
          </el-button>
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
        <el-form-item label="工号" prop="employeeNo">
          <el-input v-model="formData.employeeNo" />
        </el-form-item>
        <el-form-item label="所属营业部" prop="organId">
          <el-input v-model="formData.organId" placeholder="营业部ID" />
        </el-form-item>
        <el-form-item label="身份证号">
          <el-input v-model="formData.idCard" />
        </el-form-item>
        <el-form-item label="工作状态">
          <el-radio-group v-model="formData.workStatus">
            <el-radio :value="1">上班</el-radio>
            <el-radio :value="0">休息</el-radio>
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { employeeApi } from '@/api/employee'
import PageContainer from '@/components/PageContainer.vue'
import SearchForm from '@/components/SearchForm.vue'

const loading = ref(false)
const tableData = ref<any[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()

const searchForm = reactive({ organId: '', workStatus: undefined as number | undefined })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const formData = reactive<any>({
  id: undefined, employeeNo: '', organId: '', idCard: '', workStatus: 1
})

const rules: FormRules = {
  employeeNo: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  organId: [{ required: true, message: '请选择营业部', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const params: any = { ...pagination }
    if (searchForm.organId) params.organId = searchForm.organId
    if (searchForm.workStatus !== undefined) params.workStatus = searchForm.workStatus
    const res = await employeeApi.getCourierList(params)
    tableData.value = res.list
    pagination.total = res.total
  } catch (e) { console.error(e) } finally { loading.value = false }
}

const handleSearch = () => { pagination.pageNum = 1; loadData() }
const handleReset = () => { searchForm.organId = ''; searchForm.workStatus = undefined; handleSearch() }

const handleAdd = () => {
  dialogTitle.value = '新增快递员'
  Object.assign(formData, { id: undefined, employeeNo: '', organId: '', idCard: '', workStatus: 1 })
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogTitle.value = '编辑快递员'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleDelete = async (row: any) => {
  await ElMessageBox.confirm('确定删除该快递员吗？', '提示', { type: 'warning' })
  await employeeApi.deleteCourier(row.id)
  ElMessage.success('删除成功')
  loadData()
}

const handleToggleStatus = async (row: any) => {
  const newStatus = row.workStatus === 1 ? 0 : 1
  await employeeApi.updateCourier(row.id, { workStatus: newStatus })
  ElMessage.success('状态更新成功')
  loadData()
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    if (formData.id) {
      await employeeApi.updateCourier(formData.id, formData)
    } else {
      await employeeApi.createCourier(formData)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  })
}

onMounted(() => loadData())
</script>
