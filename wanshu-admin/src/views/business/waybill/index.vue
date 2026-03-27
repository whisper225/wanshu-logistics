<template>
  <PageContainer title="运单管理">
    <SearchForm v-model="searchForm" @search="handleSearch" @reset="handleReset">
      <el-form-item label="关键词">
        <el-input v-model="searchForm.keyword" placeholder="运单号/姓名" clearable />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="searchForm.status" placeholder="全部" clearable>
          <el-option v-for="(label, val) in statusMap" :key="val" :label="label" :value="Number(val)" />
        </el-select>
      </el-form-item>
    </SearchForm>

    <el-table :data="tableData" v-loading="loading">
      <el-table-column prop="waybillNumber" label="运单编号" width="200" />
      <el-table-column prop="senderName" label="寄件人" width="100" />
      <el-table-column prop="senderPhone" label="寄件电话" width="120" />
      <el-table-column prop="receiverName" label="收件人" width="100" />
      <el-table-column prop="receiverPhone" label="收件电话" width="120" />
      <el-table-column prop="goodsName" label="物品" width="100" />
      <el-table-column prop="weight" label="重量(kg)" width="90" />
      <el-table-column prop="freight" label="运费" width="80" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)" size="small">
            {{ statusMap[row.status] || '未知' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleDetail(row)">详情</el-button>
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
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

    <el-dialog v-model="detailVisible" title="运单详情" width="700px">
      <el-descriptions :column="2" border v-if="currentWaybill">
        <el-descriptions-item label="运单编号">{{ currentWaybill.waybillNumber }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentWaybill.status)">{{ statusMap[currentWaybill.status] }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="寄件人">{{ currentWaybill.senderName }}</el-descriptions-item>
        <el-descriptions-item label="寄件电话">{{ currentWaybill.senderPhone }}</el-descriptions-item>
        <el-descriptions-item label="寄件地址" :span="2">{{ currentWaybill.senderAddress }}</el-descriptions-item>
        <el-descriptions-item label="收件人">{{ currentWaybill.receiverName }}</el-descriptions-item>
        <el-descriptions-item label="收件电话">{{ currentWaybill.receiverPhone }}</el-descriptions-item>
        <el-descriptions-item label="收件地址" :span="2">{{ currentWaybill.receiverAddress }}</el-descriptions-item>
        <el-descriptions-item label="物品">{{ currentWaybill.goodsName }}</el-descriptions-item>
        <el-descriptions-item label="重量(kg)">{{ currentWaybill.weight }}</el-descriptions-item>
        <el-descriptions-item label="体积(cm³)">{{ currentWaybill.volume }}</el-descriptions-item>
        <el-descriptions-item label="运费">{{ currentWaybill.freight }} 元</el-descriptions-item>
        <el-descriptions-item label="签收时间">{{ currentWaybill.signTime }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentWaybill.createdTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog v-model="editVisible" title="编辑运单" width="600px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="物品名称"><el-input v-model="editForm.goodsName" /></el-form-item>
        <el-form-item label="重量(kg)"><el-input-number v-model="editForm.weight" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="体积(cm³)"><el-input-number v-model="editForm.volume" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="运费"><el-input-number v-model="editForm.freight" :min="0" :precision="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveEdit">保存</el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { businessApi } from '@/api/business'
import PageContainer from '@/components/PageContainer.vue'
import SearchForm from '@/components/SearchForm.vue'

const statusMap: Record<number, string> = {
  0: '待调度', 1: '已调度', 2: '待提货', 3: '在途', 4: '到达', 5: '派送中', 6: '已签收', 7: '已拒签', 8: '异常件'
}
const getStatusType = (s: number) => {
  if (s === 6) return 'success'
  if (s === 7 || s === 8) return 'danger'
  if (s >= 2 && s <= 5) return 'warning'
  return 'info'
}

const loading = ref(false)
const tableData = ref<any[]>([])
const detailVisible = ref(false)
const editVisible = ref(false)
const currentWaybill = ref<any>(null)
const editForm = reactive<any>({})
const searchForm = reactive({ keyword: '', status: undefined as number | undefined })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const loadData = async () => {
  loading.value = true
  try {
    const res = await businessApi.getWaybillList({ ...searchForm, ...pagination })
    tableData.value = res.list
    pagination.total = res.total
  } catch (e) { console.error(e) } finally { loading.value = false }
}

const handleSearch = () => { pagination.pageNum = 1; loadData() }
const handleReset = () => { searchForm.keyword = ''; searchForm.status = undefined; handleSearch() }
const handleDetail = (row: any) => { currentWaybill.value = row; detailVisible.value = true }
const handleEdit = (row: any) => { Object.assign(editForm, row); editVisible.value = true }

const handleSaveEdit = async () => {
  await businessApi.updateWaybill(editForm.id, editForm)
  ElMessage.success('保存成功')
  editVisible.value = false
  loadData()
}

onMounted(() => loadData())
</script>
