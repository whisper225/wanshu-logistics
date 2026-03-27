<template>
  <PageContainer title="订单管理">
    <SearchForm v-model="searchForm" @search="handleSearch" @reset="handleReset">
      <el-form-item label="关键词">
        <el-input v-model="searchForm.keyword" placeholder="订单号/姓名/手机号" clearable />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="searchForm.status" placeholder="全部" clearable>
          <el-option v-for="(label, val) in statusMap" :key="val" :label="label" :value="Number(val)" />
        </el-select>
      </el-form-item>
    </SearchForm>

    <el-table :data="tableData" v-loading="loading">
      <el-table-column prop="orderNumber" label="订单编号" width="200" />
      <el-table-column prop="senderName" label="寄件人" width="100" />
      <el-table-column prop="senderPhone" label="寄件电话" width="120" />
      <el-table-column prop="receiverName" label="收件人" width="100" />
      <el-table-column prop="receiverPhone" label="收件电话" width="120" />
      <el-table-column prop="goodsName" label="物品名称" width="120" />
      <el-table-column prop="estimatedFee" label="估算运费" width="100" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)" size="small">
            {{ statusMap[row.status] || '未知' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleDetail(row)">详情</el-button>
          <el-button link type="primary" @click="handleEdit(row)" v-if="row.status <= 1">编辑</el-button>
          <el-button link type="danger" @click="handleCancel(row)" v-if="row.status < 5">取消</el-button>
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

    <el-dialog v-model="detailVisible" title="订单详情" width="700px">
      <el-descriptions :column="2" border v-if="currentOrder">
        <el-descriptions-item label="订单编号">{{ currentOrder.orderNumber }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentOrder.status)">{{ statusMap[currentOrder.status] }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="寄件人">{{ currentOrder.senderName }}</el-descriptions-item>
        <el-descriptions-item label="寄件电话">{{ currentOrder.senderPhone }}</el-descriptions-item>
        <el-descriptions-item label="寄件地址" :span="2">{{ currentOrder.senderAddress }}</el-descriptions-item>
        <el-descriptions-item label="收件人">{{ currentOrder.receiverName }}</el-descriptions-item>
        <el-descriptions-item label="收件电话">{{ currentOrder.receiverPhone }}</el-descriptions-item>
        <el-descriptions-item label="收件地址" :span="2">{{ currentOrder.receiverAddress }}</el-descriptions-item>
        <el-descriptions-item label="物品名称">{{ currentOrder.goodsName }}</el-descriptions-item>
        <el-descriptions-item label="物品类型">{{ currentOrder.goodsType }}</el-descriptions-item>
        <el-descriptions-item label="重量(kg)">{{ currentOrder.weight }}</el-descriptions-item>
        <el-descriptions-item label="体积(cm³)">{{ currentOrder.volume }}</el-descriptions-item>
        <el-descriptions-item label="付款方式">{{ currentOrder.paymentMethod === 1 ? '寄付' : '到付' }}</el-descriptions-item>
        <el-descriptions-item label="估算运费">{{ currentOrder.estimatedFee }} 元</el-descriptions-item>
        <el-descriptions-item label="实际运费">{{ currentOrder.actualFee }} 元</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentOrder.createdTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog v-model="editVisible" title="编辑订单" width="700px">
      <el-form :model="editForm" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="寄件人"><el-input v-model="editForm.senderName" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="寄件电话"><el-input v-model="editForm.senderPhone" /></el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="寄件地址"><el-input v-model="editForm.senderAddress" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="收件人"><el-input v-model="editForm.receiverName" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="收件电话"><el-input v-model="editForm.receiverPhone" /></el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="收件地址"><el-input v-model="editForm.receiverAddress" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="物品名称"><el-input v-model="editForm.goodsName" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="实际运费"><el-input-number v-model="editForm.actualFee" :min="0" :precision="2" /></el-form-item>
          </el-col>
        </el-row>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { businessApi } from '@/api/business'
import PageContainer from '@/components/PageContainer.vue'
import SearchForm from '@/components/SearchForm.vue'

const statusMap: Record<number, string> = {
  0: '已下单', 1: '已接单', 2: '已揽收', 3: '运输中', 4: '派送中', 5: '已签收', 6: '已拒收', 7: '已取消'
}
const getStatusType = (s: number) => {
  if (s === 5) return 'success'
  if (s === 6 || s === 7) return 'danger'
  if (s >= 2 && s <= 4) return 'warning'
  return 'info'
}

const loading = ref(false)
const tableData = ref<any[]>([])
const detailVisible = ref(false)
const editVisible = ref(false)
const currentOrder = ref<any>(null)
const editForm = reactive<any>({})
const searchForm = reactive({ keyword: '', status: undefined as number | undefined })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const loadData = async () => {
  loading.value = true
  try {
    const res = await businessApi.getOrderList({ ...searchForm, ...pagination })
    tableData.value = res.list
    pagination.total = res.total
  } catch (e) { console.error(e) } finally { loading.value = false }
}

const handleSearch = () => { pagination.pageNum = 1; loadData() }
const handleReset = () => { searchForm.keyword = ''; searchForm.status = undefined; handleSearch() }

const handleDetail = (row: any) => { currentOrder.value = row; detailVisible.value = true }

const handleEdit = (row: any) => {
  Object.assign(editForm, row)
  editVisible.value = true
}

const handleSaveEdit = async () => {
  await businessApi.updateOrder(editForm.id, editForm)
  ElMessage.success('保存成功')
  editVisible.value = false
  loadData()
}

const handleCancel = async (row: any) => {
  const { value } = await ElMessageBox.prompt('请输入取消原因', '取消订单', { type: 'warning' })
  await businessApi.cancelOrder(row.id, value)
  ElMessage.success('订单已取消')
  loadData()
}

onMounted(() => loadData())
</script>
