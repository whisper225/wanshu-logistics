<template>
  <PageContainer title="回车登记">
    <el-table :data="tableData" v-loading="loading" border>
      <el-table-column prop="id" label="ID" width="160" />
      <el-table-column prop="transportTaskId" label="运输任务ID" width="120" />
      <el-table-column prop="vehicleId" label="车辆ID" width="100" />
      <el-table-column prop="driverId" label="司机ID" width="100" />
      <el-table-column prop="registerTime" label="登记时间" width="180" />
      <el-table-column prop="registerDate" label="登记日期" width="120" />
      <el-table-column prop="description" label="说明" min-width="160" show-overflow-tooltip />
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
import { dispatchApi } from '@/api/dispatch'
import PageContainer from '@/components/PageContainer.vue'

const loading = ref(false)
const tableData = ref<Record<string, unknown>[]>([])
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const loadData = async () => {
  loading.value = true
  try {
    const res = await dispatchApi.getReturnRegisterPage({ ...pagination })
    tableData.value = res.list || []
    pagination.total = res.total
  } catch (e) {
    console.error(e)
    tableData.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => loadData())
</script>
