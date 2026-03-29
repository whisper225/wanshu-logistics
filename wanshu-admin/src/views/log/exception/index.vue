<template>
  <PageContainer title="异常日志">
    <SearchForm v-model="searchForm" @search="handleSearch" @reset="handleReset">
      <el-form-item label="关键词">
        <el-input v-model="searchForm.keyword" placeholder="模块/异常类/信息/URL" clearable />
      </el-form-item>
      <el-form-item label="时间范围">
        <el-date-picker
          v-model="timeRange"
          type="datetimerange"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          value-format="YYYY-MM-DDTHH:mm:ss"
          clearable
          style="width: 100%"
        />
      </el-form-item>
    </SearchForm>

    <el-table :data="tableData" v-loading="loading">
      <el-table-column prop="module" label="模块" width="120" />
      <el-table-column prop="requestUrl" label="请求URL" min-width="180" show-overflow-tooltip />
      <el-table-column prop="exceptionName" label="异常类" width="200" show-overflow-tooltip />
      <el-table-column prop="exceptionMsg" label="异常信息" min-width="200" show-overflow-tooltip />
      <el-table-column prop="operatorName" label="操作人" width="100" />
      <el-table-column prop="operatorIp" label="IP" width="130" />
      <el-table-column prop="createdTime" label="发生时间" width="170" />
      <el-table-column label="操作" width="80" fixed="right">
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

    <el-dialog v-model="detailVisible" title="异常详情" width="760px" destroy-on-close>
      <el-descriptions v-if="currentLog" :column="1" border>
        <el-descriptions-item label="模块">{{ currentLog.module }}</el-descriptions-item>
        <el-descriptions-item label="请求URL">{{ currentLog.requestUrl }}</el-descriptions-item>
        <el-descriptions-item label="请求参数">{{ currentLog.requestParams || '—' }}</el-descriptions-item>
        <el-descriptions-item label="异常类">{{ currentLog.exceptionName }}</el-descriptions-item>
        <el-descriptions-item label="异常信息">{{ currentLog.exceptionMsg }}</el-descriptions-item>
        <el-descriptions-item label="堆栈">
          <pre class="stack-pre">{{ currentLog.stackTrace }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="操作人">{{ currentLog.operatorName }} ({{ currentLog.operatorId || '—' }})</el-descriptions-item>
        <el-descriptions-item label="IP">{{ currentLog.operatorIp }}</el-descriptions-item>
        <el-descriptions-item label="发生时间">{{ currentLog.createdTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </PageContainer>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { systemApi } from '@/api/system'
import type { ExceptionLog } from '@/types'
import PageContainer from '@/components/PageContainer.vue'
import SearchForm from '@/components/SearchForm.vue'

const loading = ref(false)
const tableData = ref<ExceptionLog[]>([])
const detailVisible = ref(false)
const currentLog = ref<ExceptionLog | null>(null)
const searchForm = reactive({ keyword: '' })
const timeRange = ref<[string, string] | null>(null)
const pagination = reactive({ pageNum: 1, pageSize: 20, total: 0 })

const loadData = async () => {
  loading.value = true
  try {
    const params: Record<string, unknown> = { ...searchForm, ...pagination }
    if (timeRange.value?.length === 2) {
      params.startTime = timeRange.value[0]
      params.endTime = timeRange.value[1]
    }
    const res = await systemApi.getExceptionLogs(params as Parameters<typeof systemApi.getExceptionLogs>[0])
    tableData.value = res.list as ExceptionLog[]
    pagination.total = res.total
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.pageNum = 1
  loadData()
}

const handleReset = () => {
  searchForm.keyword = ''
  timeRange.value = null
  handleSearch()
}

const showDetail = (row: ExceptionLog) => {
  currentLog.value = { ...row }
  detailVisible.value = true
}

onMounted(() => loadData())
</script>

<style scoped lang="scss">
.stack-pre {
  margin: 0;
  max-height: 360px;
  overflow: auto;
  font-size: 12px;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
