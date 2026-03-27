<template>
  <PageContainer title="派件作业管理">
    <SearchForm v-model="searchForm" @search="handleSearch" @reset="handleReset">
      <el-form-item label="关键词">
        <el-input v-model="searchForm.keyword" placeholder="任务单号" clearable />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="searchForm.status" placeholder="全部" clearable>
          <el-option label="待分配" :value="0" />
          <el-option label="已分配" :value="1" />
          <el-option label="派送中" :value="2" />
          <el-option label="已签收" :value="3" />
          <el-option label="已拒收" :value="4" />
          <el-option label="已取消" :value="5" />
        </el-select>
      </el-form-item>
    </SearchForm>

    <el-table :data="tableData" v-loading="loading">
      <el-table-column prop="taskNumber" label="任务单号" width="220" />
      <el-table-column prop="waybillId" label="运单ID" width="180" />
      <el-table-column prop="courierId" label="快递员ID" width="180" />
      <el-table-column prop="assignTime" label="分配时间" width="180" />
      <el-table-column prop="deliveryTime" label="派件时间" width="180" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="deliveryStatusType(row.status)" size="small">
            {{ deliveryStatusMap[row.status] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleAssign(row)" v-if="row.status === 0">分配快递员</el-button>
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
  </PageContainer>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { dispatchApi } from '@/api/dispatch'
import PageContainer from '@/components/PageContainer.vue'
import SearchForm from '@/components/SearchForm.vue'

const deliveryStatusMap: Record<number, string> = {
  0: '待分配', 1: '已分配', 2: '派送中', 3: '已签收', 4: '已拒收', 5: '已取消'
}
const deliveryStatusType = (s: number) => {
  if (s === 3) return 'success'
  if (s === 4 || s === 5) return 'danger'
  if (s === 2) return 'warning'
  return 'info'
}

const loading = ref(false)
const tableData = ref<any[]>([])
const searchForm = reactive({ keyword: '', status: undefined as number | undefined })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const loadData = async () => {
  loading.value = true
  try {
    const res = await dispatchApi.getDeliveryTaskList({ ...searchForm, ...pagination })
    tableData.value = res.list
    pagination.total = res.total
  } catch (e) { console.error(e) } finally { loading.value = false }
}

const handleSearch = () => { pagination.pageNum = 1; loadData() }
const handleReset = () => { searchForm.keyword = ''; searchForm.status = undefined; handleSearch() }

const handleAssign = async (row: any) => {
  const { value } = await ElMessageBox.prompt('请输入快递员ID', '分配快递员')
  await dispatchApi.assignDeliveryTask(row.id, Number(value))
  ElMessage.success('分配成功')
  loadData()
}

onMounted(() => loadData())
</script>
