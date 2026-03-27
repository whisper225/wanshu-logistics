<template>
  <PageContainer title="异常日志">
    <SearchForm v-model="searchForm" @search="handleSearch" @reset="handleReset">
      <el-form-item label="关键词">
        <el-input v-model="searchForm.keyword" placeholder="模块/异常类名" clearable />
      </el-form-item>
    </SearchForm>

    <el-table :data="tableData" v-loading="loading">
      <el-table-column prop="module" label="异常模块" width="120" />
      <el-table-column prop="requestUrl" label="请求URL" show-overflow-tooltip />
      <el-table-column prop="exceptionName" label="异常类名" width="180" show-overflow-tooltip />
      <el-table-column prop="exceptionMsg" label="异常信息" show-overflow-tooltip />
      <el-table-column prop="operatorName" label="操作人" width="100" />
      <el-table-column prop="createdTime" label="发生时间" width="180" />
      <el-table-column label="操作" width="80">
        <template #default="{ row }">
          <el-button link type="primary" @click="showDetail(row)">详情</el-button>
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

    <el-dialog v-model="detailVisible" title="异常详情" width="700px">
      <el-descriptions :column="1" border v-if="currentLog">
        <el-descriptions-item label="模块">{{ currentLog.module }}</el-descriptions-item>
        <el-descriptions-item label="请求URL">{{ currentLog.requestUrl }}</el-descriptions-item>
        <el-descriptions-item label="请求参数">{{ currentLog.requestParams }}</el-descriptions-item>
        <el-descriptions-item label="异常类名">{{ currentLog.exceptionName }}</el-descriptions-item>
        <el-descriptions-item label="异常信息">{{ currentLog.exceptionMsg }}</el-descriptions-item>
        <el-descriptions-item label="堆栈信息">
          <pre style="max-height: 300px; overflow: auto; font-size: 12px;">{{ currentLog.stackTrace }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="操作人">{{ currentLog.operatorName }}</el-descriptions-item>
        <el-descriptions-item label="发生时间">{{ currentLog.createdTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </PageContainer>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { systemApi } from '@/api/system'
import PageContainer from '@/components/PageContainer.vue'
import SearchForm from '@/components/SearchForm.vue'

const loading = ref(false)
const tableData = ref<any[]>([])
const detailVisible = ref(false)
const currentLog = ref<any>(null)
const searchForm = reactive({ keyword: '' })
const pagination = reactive({ pageNum: 1, pageSize: 20, total: 0 })

const loadData = async () => {
  loading.value = true
  try {
    const res = await systemApi.getExceptionLogs({ ...searchForm, ...pagination })
    tableData.value = res.list
    pagination.total = res.total
  } catch (e) { console.error(e) } finally { loading.value = false }
}

const handleSearch = () => { pagination.pageNum = 1; loadData() }
const handleReset = () => { searchForm.keyword = ''; handleSearch() }
const showDetail = (row: any) => { currentLog.value = row; detailVisible.value = true }

onMounted(() => loadData())
</script>
