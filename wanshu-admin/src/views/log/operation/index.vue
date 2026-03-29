<template>
  <PageContainer title="操作日志">
    <SearchForm v-model="searchForm" @search="handleSearch" @reset="handleReset">
      <el-form-item label="关键词">
        <el-input v-model="searchForm.keyword" placeholder="模块/操作/操作人/URL" clearable />
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
      <el-table-column prop="module" label="模块" width="120" show-overflow-tooltip />
      <el-table-column prop="operation" label="操作" min-width="140" show-overflow-tooltip />
      <el-table-column prop="requestUrl" label="请求URL" min-width="200" show-overflow-tooltip />
      <el-table-column label="方法" width="160" show-overflow-tooltip>
        <template #default="{ row }">
          <span class="mono">{{ shortMethod(row.method) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="operatorName" label="操作人" width="100" />
      <el-table-column prop="operatorIp" label="IP" width="130" />
      <el-table-column prop="costTime" label="耗时(ms)" width="90" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdTime" label="操作时间" width="170" />
      <el-table-column label="操作" width="80" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row)">详情</el-button>
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

    <el-dialog v-model="detailVisible" title="操作日志详情" width="720px" destroy-on-close>
      <el-descriptions v-if="current" :column="1" border>
        <el-descriptions-item label="模块">{{ current.module }}</el-descriptions-item>
        <el-descriptions-item label="操作">{{ current.operation }}</el-descriptions-item>
        <el-descriptions-item label="方法">{{ current.method }}</el-descriptions-item>
        <el-descriptions-item label="请求URL">{{ current.requestUrl }}</el-descriptions-item>
        <el-descriptions-item label="请求参数">
          <pre class="param-pre">{{ current.requestParams || '—' }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="操作人">{{ current.operatorName }} ({{ current.operatorId || '—' }})</el-descriptions-item>
        <el-descriptions-item label="IP">{{ current.operatorIp }}</el-descriptions-item>
        <el-descriptions-item label="耗时">{{ current.costTime }} ms</el-descriptions-item>
        <el-descriptions-item label="状态">{{ current.status === 1 ? '成功' : '失败' }}</el-descriptions-item>
        <el-descriptions-item label="时间">{{ current.createdTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </PageContainer>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { systemApi } from '@/api/system'
import type { OperationLog } from '@/types'
import PageContainer from '@/components/PageContainer.vue'
import SearchForm from '@/components/SearchForm.vue'

const loading = ref(false)
const tableData = ref<OperationLog[]>([])
const searchForm = reactive({ keyword: '' })
const timeRange = ref<[string, string] | null>(null)
const pagination = reactive({ pageNum: 1, pageSize: 20, total: 0 })

const detailVisible = ref(false)
const current = ref<OperationLog | null>(null)

const shortMethod = (m?: string) => {
  if (!m) return '—'
  const parts = m.split('.')
  return parts.length > 1 ? parts[parts.length - 2] + '.' + parts[parts.length - 1] : m
}

const loadData = async () => {
  loading.value = true
  try {
    const params: Record<string, unknown> = { ...searchForm, ...pagination }
    if (timeRange.value?.length === 2) {
      params.startTime = timeRange.value[0]
      params.endTime = timeRange.value[1]
    }
    const res = await systemApi.getOperationLogs(params as Parameters<typeof systemApi.getOperationLogs>[0])
    tableData.value = res.list as OperationLog[]
    pagination.total = res.total
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

/** 列表接口已返回完整字段；直接用行数据避免雪花 ID 经 JSON number 丢精度导致详情 404/5002 */
const openDetail = (row: OperationLog) => {
  current.value = { ...row }
  detailVisible.value = true
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

onMounted(() => loadData())
</script>

<style scoped lang="scss">
.mono {
  font-family: ui-monospace, monospace;
  font-size: 12px;
}
.param-pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 12px;
  max-height: 240px;
  overflow: auto;
}
</style>
