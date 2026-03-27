<template>
  <PageContainer title="运输任务管理">
    <SearchForm v-model="searchForm" @search="handleSearch" @reset="handleReset">
      <el-form-item label="关键词">
        <el-input v-model="searchForm.keyword" placeholder="任务编号" clearable />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="searchForm.status" placeholder="全部" clearable>
          <el-option v-for="(label, val) in statusMap" :key="val" :label="label" :value="Number(val)" />
        </el-select>
      </el-form-item>
    </SearchForm>

    <el-table :data="tableData" v-loading="loading">
      <el-table-column prop="taskNumber" label="任务编号" width="220" />
      <el-table-column prop="lineId" label="线路ID" width="180" />
      <el-table-column prop="tripId" label="班次ID" width="180" />
      <el-table-column prop="vehicleId" label="车辆ID" width="180" />
      <el-table-column prop="driverId" label="司机ID" width="180" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)" size="small">
            {{ statusMap[row.status] || '未知' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="departureTime" label="发车时间" width="180" />
      <el-table-column prop="arrivalTime" label="到达时间" width="180" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleDetail(row)">详情</el-button>
          <el-button link type="primary" @click="handleUpdateStatus(row)" v-if="row.status < 3">更新状态</el-button>
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

    <el-dialog v-model="detailVisible" title="运输任务详情" width="700px">
      <el-descriptions :column="2" border v-if="currentTask">
        <el-descriptions-item label="任务编号">{{ currentTask.taskNumber }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentTask.status)">{{ statusMap[currentTask.status] }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="线路ID">{{ currentTask.lineId }}</el-descriptions-item>
        <el-descriptions-item label="班次ID">{{ currentTask.tripId }}</el-descriptions-item>
        <el-descriptions-item label="车辆ID">{{ currentTask.vehicleId }}</el-descriptions-item>
        <el-descriptions-item label="司机ID">{{ currentTask.driverId }}</el-descriptions-item>
        <el-descriptions-item label="发车时间">{{ currentTask.departureTime }}</el-descriptions-item>
        <el-descriptions-item label="到达时间">{{ currentTask.arrivalTime }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentTask.createdTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </PageContainer>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { dispatchApi } from '@/api/dispatch'
import PageContainer from '@/components/PageContainer.vue'
import SearchForm from '@/components/SearchForm.vue'

const statusMap: Record<number, string> = {
  0: '待发车', 1: '在途', 2: '到达', 3: '已回车', 4: '已取消'
}
const getStatusType = (s: number) => {
  if (s === 2 || s === 3) return 'success'
  if (s === 4) return 'danger'
  if (s === 1) return 'warning'
  return 'info'
}

const loading = ref(false)
const tableData = ref<any[]>([])
const detailVisible = ref(false)
const currentTask = ref<any>(null)
const searchForm = reactive({ keyword: '', status: undefined as number | undefined })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const loadData = async () => {
  loading.value = true
  try {
    const res = await dispatchApi.getTransportTaskList({ ...searchForm, ...pagination })
    tableData.value = res.list
    pagination.total = res.total
  } catch (e) { console.error(e) } finally { loading.value = false }
}

const handleSearch = () => { pagination.pageNum = 1; loadData() }
const handleReset = () => { searchForm.keyword = ''; searchForm.status = undefined; handleSearch() }
const handleDetail = (row: any) => { currentTask.value = row; detailVisible.value = true }

const handleUpdateStatus = async (row: any) => {
  const nextStatus = row.status + 1
  const nextLabel = statusMap[nextStatus] || '下一状态'
  await ElMessageBox.confirm(`确定将状态更新为 "${nextLabel}" 吗？`, '更新状态', { type: 'warning' })
  await dispatchApi.updateTransportTaskStatus(row.id, nextStatus)
  ElMessage.success('状态已更新')
  loadData()
}

onMounted(() => loadData())
</script>
