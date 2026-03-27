<template>
  <PageContainer title="线路管理">
    <SearchForm v-model="searchForm" @search="handleSearch" @reset="handleReset">
      <el-form-item label="关键词">
        <el-input v-model="searchForm.keyword" placeholder="线路名称/编号" clearable />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="searchForm.status" placeholder="全部" clearable>
          <el-option label="启用" :value="1" />
          <el-option label="停用" :value="0" />
        </el-select>
      </el-form-item>
      <template #actions>
        <el-button type="primary" @click="handleAdd">新增线路</el-button>
      </template>
    </SearchForm>

    <el-table :data="tableData" v-loading="loading">
      <el-table-column prop="lineNumber" label="线路编号" width="140" />
      <el-table-column prop="lineName" label="线路名称" />
      <el-table-column prop="lineType" label="线路类型" width="100">
        <template #default="{ row }">
          {{ row.lineType === 1 ? '干线' : row.lineType === 2 ? '支线' : '接驳' }}
        </template>
      </el-table-column>
      <el-table-column prop="startOrganName" label="起始机构" width="140" />
      <el-table-column prop="endOrganName" label="目的机构" width="140" />
      <el-table-column prop="distance" label="距离(km)" width="100" />
      <el-table-column prop="estimatedTime" label="预计时长(h)" width="110" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="primary" @click="handleTrips(row)">班次管理</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="线路编号" prop="lineNumber">
          <el-input v-model="formData.lineNumber" />
        </el-form-item>
        <el-form-item label="线路名称" prop="lineName">
          <el-input v-model="formData.lineName" />
        </el-form-item>
        <el-form-item label="线路类型">
          <el-select v-model="formData.lineType">
            <el-option label="干线" :value="1" />
            <el-option label="支线" :value="2" />
            <el-option label="接驳" :value="3" />
          </el-select>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="起始机构ID"><el-input v-model="formData.startOrganId" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="目的机构ID"><el-input v-model="formData.endOrganId" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="距离(km)"><el-input-number v-model="formData.distance" :min="0" :precision="1" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预计时长(h)"><el-input-number v-model="formData.estimatedTime" :min="0" :precision="1" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="状态">
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

    <el-dialog v-model="tripDialogVisible" title="班次管理" width="700px">
      <div style="margin-bottom: 10px; text-align: right">
        <el-button type="primary" size="small" @click="handleAddTrip">新增班次</el-button>
      </div>
      <el-table :data="tripList" size="small">
        <el-table-column prop="tripNumber" label="班次编号" width="120" />
        <el-table-column prop="departureTime" label="发车时间" width="100" />
        <el-table-column prop="vehicleId" label="车辆ID" width="120" />
        <el-table-column prop="driverId" label="司机ID" width="120" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button link type="danger" size="small" @click="handleDeleteTrip(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </PageContainer>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { dispatchApi } from '@/api/dispatch'
import PageContainer from '@/components/PageContainer.vue'
import SearchForm from '@/components/SearchForm.vue'

const loading = ref(false)
const tableData = ref<any[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const tripDialogVisible = ref(false)
const tripList = ref<any[]>([])
const currentLineId = ref<number>()

const searchForm = reactive({ keyword: '', status: undefined as number | undefined })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const formData = reactive<any>({
  id: undefined, lineNumber: '', lineName: '', lineType: 1,
  startOrganId: '', endOrganId: '', distance: 0, estimatedTime: 0, status: 1
})

const rules: FormRules = {
  lineNumber: [{ required: true, message: '请输入线路编号', trigger: 'blur' }],
  lineName: [{ required: true, message: '请输入线路名称', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await dispatchApi.getLineList({ ...searchForm, ...pagination })
    tableData.value = res.list
    pagination.total = res.total
  } catch (e) { console.error(e) } finally { loading.value = false }
}

const handleSearch = () => { pagination.pageNum = 1; loadData() }
const handleReset = () => { searchForm.keyword = ''; searchForm.status = undefined; handleSearch() }

const handleAdd = () => {
  dialogTitle.value = '新增线路'
  Object.assign(formData, { id: undefined, lineNumber: '', lineName: '', lineType: 1, startOrganId: '', endOrganId: '', distance: 0, estimatedTime: 0, status: 1 })
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogTitle.value = '编辑线路'
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleDelete = async (row: any) => {
  await ElMessageBox.confirm('确定删除该线路吗？', '提示', { type: 'warning' })
  await dispatchApi.deleteLine(row.id)
  ElMessage.success('删除成功')
  loadData()
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    if (formData.id) {
      await dispatchApi.updateLine(formData.id, formData)
    } else {
      await dispatchApi.createLine(formData)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  })
}

const handleTrips = async (row: any) => {
  currentLineId.value = row.id
  try {
    tripList.value = await dispatchApi.getLineTrips(row.id)
  } catch { tripList.value = [] }
  tripDialogVisible.value = true
}

const handleAddTrip = () => { ElMessage.info('新增班次功能开发中') }

const handleDeleteTrip = async (row: any) => {
  await ElMessageBox.confirm('确定删除该班次吗？', '提示', { type: 'warning' })
  await dispatchApi.deleteTrip(row.id)
  ElMessage.success('删除成功')
  if (currentLineId.value) {
    tripList.value = await dispatchApi.getLineTrips(currentLineId.value)
  }
}

onMounted(() => loadData())
</script>
