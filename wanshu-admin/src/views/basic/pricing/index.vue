<template>
  <PageContainer title="运费管理">
    <SearchForm v-model="searchForm" @search="handleSearch" @reset="handleReset">
      <el-form-item label="模板名称">
        <el-input v-model="searchForm.name" placeholder="请输入模板名称" clearable />
      </el-form-item>
      <el-form-item label="模板类型">
        <el-select v-model="searchForm.type" placeholder="请选择" clearable>
          <el-option label="同城寄" value="SAME_CITY" />
          <el-option label="省内寄" value="SAME_PROVINCE" />
          <el-option label="跨省寄" value="CROSS_PROVINCE" />
          <el-option label="经济区互寄" value="ECONOMIC_ZONE" />
        </el-select>
      </el-form-item>
      <template #actions>
        <el-button type="primary" @click="handleAdd">新增运费模板</el-button>
      </template>
    </SearchForm>

    <el-table :data="tableData" v-loading="loading">
      <el-table-column prop="name" label="模板名称" />
      <el-table-column prop="type" label="模板类型">
        <template #default="{ row }">
          <el-tag>{{ getTypeLabel(row.type) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="baseWeight" label="首重(kg)" />
      <el-table-column prop="basePrice" label="首重价格(元)" />
      <el-table-column prop="additionalWeightPrice" label="续重价格(元/kg)" />
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">
            {{ row.status === 'ACTIVE' ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">修改</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="pagination.pageNum"
      v-model:page-size="pagination.pageSize"
      :total="pagination.total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="loadData"
      @current-change="loadData"
    />

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
    >
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="140px">
        <el-form-item label="模板名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入模板名称" />
        </el-form-item>
        <el-form-item label="模板类型" prop="type">
          <el-select v-model="formData.type" placeholder="请选择">
            <el-option label="同城寄" value="SAME_CITY" />
            <el-option label="省内寄" value="SAME_PROVINCE" />
            <el-option label="跨省寄" value="CROSS_PROVINCE" />
            <el-option label="经济区互寄" value="ECONOMIC_ZONE" />
          </el-select>
        </el-form-item>
        <el-form-item label="首重(kg)" prop="baseWeight">
          <el-input-number v-model="formData.baseWeight" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="首重价格(元)" prop="basePrice">
          <el-input-number v-model="formData.basePrice" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="续重价格(元/kg)" prop="additionalWeightPrice">
          <el-input-number v-model="formData.additionalWeightPrice" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="首体积(m³)" prop="baseVolume">
          <el-input-number v-model="formData.baseVolume" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="续体积价格(元/m³)" prop="additionalVolumePrice">
          <el-input-number v-model="formData.additionalVolumePrice" :min="0" :precision="2" />
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
import { pricingApi } from '@/api/pricing'
import type { PricingTemplate } from '@/types'
import PageContainer from '@/components/PageContainer.vue'
import SearchForm from '@/components/SearchForm.vue'

const loading = ref(false)
const tableData = ref<PricingTemplate[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()

const searchForm = reactive({
  name: '',
  type: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const formData = reactive<Partial<PricingTemplate>>({
  name: '',
  type: 'SAME_CITY',
  baseWeight: 1,
  basePrice: 0,
  additionalWeightPrice: 0,
  baseVolume: 0,
  additionalVolumePrice: 0,
  status: 'ACTIVE'
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择模板类型', trigger: 'change' }],
  baseWeight: [{ required: true, message: '请输入首重', trigger: 'blur' }],
  basePrice: [{ required: true, message: '请输入首重价格', trigger: 'blur' }]
}

const getTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    'SAME_CITY': '同城寄',
    'SAME_PROVINCE': '省内寄',
    'CROSS_PROVINCE': '跨省寄',
    'ECONOMIC_ZONE': '经济区互寄'
  }
  return map[type] || type
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await pricingApi.getList({
      ...searchForm,
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    })
    tableData.value = res.list
    pagination.total = res.total
  } catch (error) {
    console.error('Load data failed:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  loadData()
}

const handleReset = () => {
  searchForm.name = ''
  searchForm.type = ''
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = '新增运费模板'
  Object.assign(formData, {
    name: '',
    type: 'SAME_CITY',
    baseWeight: 1,
    basePrice: 0,
    additionalWeightPrice: 0,
    baseVolume: 0,
    additionalVolumePrice: 0,
    status: 'ACTIVE'
  })
  dialogVisible.value = true
}

const handleEdit = (row: PricingTemplate) => {
  dialogTitle.value = '修改运费模板'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleDelete = async (row: PricingTemplate) => {
  try {
    await ElMessageBox.confirm('确定要删除该运费模板吗？', '提示', {
      type: 'warning'
    })
    await pricingApi.delete(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    console.error('Delete failed:', error)
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    try {
      if (formData.id) {
        await pricingApi.update(formData.id, formData)
      } else {
        await pricingApi.create(formData)
      }
      ElMessage.success('保存成功')
      dialogVisible.value = false
      loadData()
    } catch (error) {
      console.error('Submit failed:', error)
    }
  })
}

onMounted(() => {
  loadData()
})
</script>
