<template>
  <PageContainer title="运费管理">
    <el-alert type="info" show-icon :closable="false" class="tip">
      普快计费：计费重量=max(实重, 体积重)，体积重=体积(cm³)/轻抛系数；同城/省内/跨省每种模板仅允许一条；经济区互寄可多选经济区（同一经济区仅绑定一个模板）。试算见接口说明。
    </el-alert>
    <SearchForm v-model="searchForm" @search="handleSearch" @reset="handleReset">
      <el-form-item label="模板名称">
        <el-input v-model="searchForm.name" placeholder="请输入模板名称" clearable />
      </el-form-item>
      <el-form-item label="模板类型">
        <el-select v-model="searchForm.type" placeholder="请选择" clearable>
          <el-option label="同城寄" value="1" />
          <el-option label="省内寄" value="2" />
          <el-option label="跨省寄" value="3" />
          <el-option label="经济区互寄" value="4" />
        </el-select>
      </el-form-item>
      <template #actions>
        <el-button type="primary" @click="handleAdd">新增运费模板</el-button>
      </template>
    </SearchForm>

    <el-table :data="tableData" v-loading="loading">
      <el-table-column prop="templateName" label="模板名称" min-width="140" />
      <el-table-column label="模板类型" width="120">
        <template #default="{ row }">
          <el-tag>{{ typeLabel(row.templateType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="firstWeight" label="首重(kg)" width="90" />
      <el-table-column prop="firstWeightPrice" label="首重价(元)" width="100" />
      <el-table-column prop="extraWeight" label="续重单位(kg)" width="110" />
      <el-table-column prop="extraWeightPrice" label="续重价(元/单位)" width="130" />
      <el-table-column prop="lightThrowRatio" label="轻抛系数" width="100">
        <template #default="{ row }">
          {{ row.lightThrowRatio ?? '默认' }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="640px" destroy-on-close>
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="150px">
        <el-form-item label="模板名称" prop="templateName">
          <el-input v-model="formData.templateName" placeholder="请输入模板名称" />
        </el-form-item>
        <el-form-item label="模板类型" prop="templateType">
          <el-select v-model="formData.templateType" placeholder="请选择" style="width: 100%">
            <el-option label="同城寄" :value="1" />
            <el-option label="省内寄" :value="2" />
            <el-option label="跨省寄" :value="3" />
            <el-option label="经济区互寄" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="formData.templateType === 4" label="经济区" prop="economicZoneIds">
          <el-select
            v-model="formData.economicZoneIds"
            multiple
            filterable
            placeholder="可多选，共用一个模板"
            style="width: 100%"
          >
            <el-option
              v-for="z in economicZones"
              :key="z.id"
              :label="`${z.zoneName}（轻抛${z.lightThrowRatio ?? '—'}）`"
              :value="z.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="首重(kg)" prop="firstWeight">
          <el-input-number v-model="formData.firstWeight" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="首重价格(元)" prop="firstWeightPrice">
          <el-input-number v-model="formData.firstWeightPrice" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="续重单位(kg)" prop="extraWeight">
          <el-input-number v-model="formData.extraWeight" :min="0.01" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="续重单价(元/单位)" prop="extraWeightPrice">
          <el-input-number v-model="formData.extraWeightPrice" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="轻抛系数(cm³/kg)" prop="lightThrowRatio">
          <el-input-number
            v-model="formData.lightThrowRatio"
            :min="1"
            :precision="0"
            placeholder="留空则按类型/经济区默认"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">启用</el-radio>
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { pricingApi } from '@/api/pricing'
import type { EconomicZoneItem, PricingTemplate } from '@/types'
import PageContainer from '@/components/PageContainer.vue'
import SearchForm from '@/components/SearchForm.vue'

const loading = ref(false)
const tableData = ref<PricingTemplate[]>([])
const economicZones = ref<EconomicZoneItem[]>([])
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

const defaultForm = (): Partial<PricingTemplate> => ({
  templateName: '',
  templateType: 1,
  firstWeight: 1,
  firstWeightPrice: 0,
  extraWeight: 1,
  extraWeightPrice: 0,
  lightThrowRatio: undefined,
  status: 1,
  economicZoneIds: []
})

const formData = reactive<Partial<PricingTemplate>>(defaultForm())

const rules: FormRules = {
  templateName: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
  templateType: [{ required: true, message: '请选择模板类型', trigger: 'change' }],
  firstWeight: [{ required: true, message: '请输入首重', trigger: 'blur' }],
  firstWeightPrice: [{ required: true, message: '请输入首重价格', trigger: 'blur' }]
}

const typeLabel = (t?: number) => {
  const m: Record<number, string> = { 1: '同城寄', 2: '省内寄', 3: '跨省寄', 4: '经济区互寄' }
  return t != null ? m[t] ?? String(t) : '—'
}

const loadEconomicZones = async () => {
  try {
    economicZones.value = await pricingApi.getEconomicZones()
  } catch {
    economicZones.value = []
  }
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
  Object.assign(formData, defaultForm(), { id: undefined })
  dialogVisible.value = true
}

const handleEdit = async (row: PricingTemplate) => {
  dialogTitle.value = '修改运费模板'
  try {
    const detail = await pricingApi.getDetail(String(row.id))
    const zoneIds = detail.economicZoneIds?.map((id) => String(id)) ?? []
    Object.assign(formData, {
      ...detail,
      economicZoneIds: zoneIds,
      id: detail.id != null ? String(detail.id) : undefined
    })
    dialogVisible.value = true
  } catch (e) {
    console.error(e)
  }
}

const handleDelete = async (row: PricingTemplate) => {
  try {
    await ElMessageBox.confirm('确定要删除该运费模板吗？', '提示', {
      type: 'warning'
    })
    await pricingApi.delete(String(row.id))
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

    if (formData.templateType === 4 && (!formData.economicZoneIds || formData.economicZoneIds.length === 0)) {
      ElMessage.warning('经济区互寄请至少选择一个经济区')
      return
    }

    const payload: Record<string, unknown> = {
      templateName: formData.templateName,
      templateType: formData.templateType,
      firstWeight: formData.firstWeight,
      firstWeightPrice: formData.firstWeightPrice,
      extraWeight: formData.extraWeight,
      extraWeightPrice: formData.extraWeightPrice,
      status: formData.status ?? 1
    }
    if (formData.lightThrowRatio != null && !Number.isNaN(Number(formData.lightThrowRatio))) {
      payload.lightThrowRatio = Number(formData.lightThrowRatio)
    }
    if (formData.templateType === 4 && formData.economicZoneIds?.length) {
      payload.economicZoneIds = formData.economicZoneIds.map((id) => Number(id))
    }

    try {
      if (formData.id) {
        await pricingApi.update(String(formData.id), payload as Partial<PricingTemplate>)
      } else {
        await pricingApi.create(payload as Partial<PricingTemplate>)
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
  loadEconomicZones()
  loadData()
})
</script>

<style scoped lang="scss">
.tip {
  margin-bottom: 16px;
}
</style>
