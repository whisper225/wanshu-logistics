<template>
  <div class="organ-list-panel">
    <SearchForm v-model="searchForm" @search="handleSearch" @reset="handleReset">
      <el-form-item label="关键词">
        <el-input v-model="searchForm.keyword" placeholder="机构名称 / 负责人 / 电话" clearable />
      </el-form-item>
      <template #actions>
        <el-button type="primary" @click="formDialogRef?.openCreateTop()">新增顶级机构</el-button>
      </template>
    </SearchForm>

    <el-table :data="tableData" v-loading="loading" border stripe>
      <el-table-column prop="organName" label="机构名称" min-width="160" show-overflow-tooltip />
      <el-table-column label="类型" width="120">
        <template #default="{ row }">
          <el-tag size="small">{{ organTypeLabel(row.organType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="parentName" label="上级机构" width="140" show-overflow-tooltip />
      <el-table-column prop="managerName" label="负责人" width="100" />
      <el-table-column prop="managerPhone" label="负责人电话" width="120" />
      <el-table-column prop="address" label="详细地址" min-width="180" show-overflow-tooltip />
      <el-table-column label="状态" width="88">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="72" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="formDialogRef?.openCreateChild(row.id)">新增下级</el-button>
          <el-button link type="primary" @click="formDialogRef?.openEdit(row)">编辑</el-button>
          <el-button
            link
            type="danger"
            :disabled="row.hasChildren === true"
            @click="handleDelete(row)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="pagination.pageNum"
      v-model:page-size="pagination.pageSize"
      :total="pagination.total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      class="pager"
      @size-change="loadData"
      @current-change="loadData"
    />

    <OrganFormDialog ref="formDialogRef" @success="onFormSuccess" />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { organizationApi, type OrganTableRow } from '@/api/organization'
import { organTypeLabel } from '@/utils/organization'
import SearchForm from '@/components/SearchForm.vue'
import OrganFormDialog from '@/components/organization/OrganFormDialog.vue'

const emit = defineEmits<{
  success: []
}>()

const loading = ref(false)
const tableData = ref<OrganTableRow[]>([])
const formDialogRef = ref<InstanceType<typeof OrganFormDialog>>()

const searchForm = reactive({ keyword: '' })
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

async function loadData() {
  loading.value = true
  try {
    const res = await organizationApi.getPage({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      keyword: searchForm.keyword || undefined
    })
    tableData.value = res.list || []
    pagination.total = res.total ?? 0
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.pageNum = 1
  loadData()
}

function handleReset() {
  searchForm.keyword = ''
  pagination.pageNum = 1
  loadData()
}

function onFormSuccess() {
  loadData()
  emit('success')
}

async function handleDelete(row: OrganTableRow) {
  if (row.hasChildren === true) {
    ElMessage.warning('请先删除或调整下级机构后再删除本机构')
    return
  }
  try {
    await ElMessageBox.confirm(
      `确定删除机构「${row.organName}」？若存在下级机构将无法删除。`,
      '删除确认',
      { type: 'warning' }
    )
    await organizationApi.delete(String(row.id))
    ElMessage.success('已删除')
    loadData()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

onMounted(() => {
  loadData()
})

defineExpose({ loadData })
</script>

<style scoped lang="scss">
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
