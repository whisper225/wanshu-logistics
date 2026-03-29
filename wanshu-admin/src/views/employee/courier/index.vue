<template>
  <PageContainer title="快递员管理（仅查看，可维护作业范围）">
    <SearchForm v-model="searchForm" @search="handleSearch" @reset="handleReset">
      <el-form-item label="关键词">
        <el-input v-model="searchForm.keyword" placeholder="用户名/姓名/手机/工号" clearable />
      </el-form-item>
      <el-form-item label="所属营业部">
        <el-select
          v-model="searchForm.organId"
          placeholder="请选择机构"
          clearable
          filterable
          style="width: 220px"
        >
          <el-option
            v-for="o in flatOrgOptions"
            :key="o.value"
            :label="o.label"
            :value="o.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="工作状态">
        <el-select v-model="searchForm.workStatus" placeholder="请选择" clearable>
          <el-option label="上班" :value="1" />
          <el-option label="休息" :value="0" />
        </el-select>
      </el-form-item>
    </SearchForm>

    <el-table :data="tableData" v-loading="loading">
      <el-table-column prop="id" label="用户ID" width="160" show-overflow-tooltip />
      <el-table-column prop="username" label="登录账号" width="120" />
      <el-table-column prop="realName" label="姓名" width="100" />
      <el-table-column prop="phone" label="手机" width="120" />
      <el-table-column prop="employeeNo" label="工号" width="120" />
      <el-table-column prop="organName" label="所属营业部" min-width="140" show-overflow-tooltip />
      <el-table-column prop="workStatus" label="工作状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.workStatus === 1 ? 'success' : 'info'">
            {{ row.workStatus === 1 ? '上班' : '休息' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openScopeDialog(row)">作业范围</el-button>
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

    <el-dialog v-model="scopeDialogVisible" title="快递员作业范围" width="640px" destroy-on-close @closed="scopeCourierId = null">
      <el-alert type="info" show-icon :closable="false" class="mb-3">
        省 / 市 / 区 使用国标 adcode，与机构作业范围一致。
      </el-alert>
      <el-cascader
        v-model="scopeSelectedPaths"
        :options="areaOptions"
        :props="cascaderProps"
        placeholder="请选择省 / 市 / 区（可多选）"
        style="width: 100%"
        clearable
        collapse-tags
        collapse-tags-tooltip
      />
      <template #footer>
        <el-button @click="scopeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="scopeSaving" @click="saveScopes">保存</el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { regionData } from 'element-china-area-data'
import { employeeApi, type EmpCourierVO } from '@/api/employee'
import { organizationApi } from '@/api/organization'
import type { OrganScope } from '@/api/organization'
import { pathsToOrganScopes, scopesToPaths } from '@/utils/regionScope'
import PageContainer from '@/components/PageContainer.vue'
import SearchForm from '@/components/SearchForm.vue'

type TreeOrg = Record<string, unknown> & { id?: string | number; name?: string; children?: TreeOrg[] }

const loading = ref(false)
const tableData = ref<EmpCourierVO[]>([])
const flatOrgOptions = ref<{ value: string; label: string }[]>([])

const searchForm = reactive({
  keyword: '',
  organId: undefined as string | undefined,
  workStatus: undefined as number | undefined
})
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const areaOptions = ref(regionData)
const cascaderProps = {
  multiple: true,
  checkStrictly: true,
  value: 'value',
  label: 'label',
  children: 'children'
}
const scopeDialogVisible = ref(false)
const scopeCourierId = ref<string | null>(null)
const scopeSelectedPaths = ref<string[][]>([])
const scopeSaving = ref(false)

function flattenOrgTree(nodes: TreeOrg[], depth = 0): { value: string; label: string }[] {
  const out: { value: string; label: string }[] = []
  for (const n of nodes) {
    const id = n.id != null ? String(n.id) : ''
    const name = String(n.name ?? '')
    if (id) {
      out.push({ value: id, label: `${'　'.repeat(depth)}${name}` })
    }
    if (n.children?.length) {
      out.push(...flattenOrgTree(n.children, depth + 1))
    }
  }
  return out
}

const loadOrgTree = async () => {
  try {
    const tree = (await organizationApi.getTree()) as unknown as TreeOrg[]
    flatOrgOptions.value = flattenOrgTree(Array.isArray(tree) ? tree : [])
  } catch (e) {
    console.error(e)
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const params: Record<string, unknown> = { ...pagination }
    if (searchForm.keyword) params.keyword = searchForm.keyword
    if (searchForm.organId) params.organId = searchForm.organId
    if (searchForm.workStatus !== undefined) params.workStatus = searchForm.workStatus
    const res = await employeeApi.getCourierList(params as Parameters<typeof employeeApi.getCourierList>[0])
    tableData.value = res.list
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
  searchForm.organId = undefined
  searchForm.workStatus = undefined
  handleSearch()
}

const openScopeDialog = async (row: EmpCourierVO) => {
  scopeCourierId.value = String(row.id)
  scopeDialogVisible.value = true
  scopeSelectedPaths.value = []
  try {
    const scopes = await employeeApi.getCourierScopes(row.id)
    const asOrgan: OrganScope[] = (scopes || []).map((s) => ({
      provinceId: s.provinceId,
      cityId: s.cityId,
      countyId: s.countyId
    }))
    scopeSelectedPaths.value = scopesToPaths(asOrgan)
  } catch (e) {
    console.error(e)
  }
}

const saveScopes = async () => {
  if (!scopeCourierId.value) return
  scopeSaving.value = true
  try {
    const rows = pathsToOrganScopes(scopeSelectedPaths.value).map((s) => ({
      provinceId: s.provinceId,
      cityId: s.cityId,
      countyId: s.countyId
    }))
    await employeeApi.updateCourierScopes(scopeCourierId.value, rows)
    ElMessage.success('保存成功')
    scopeDialogVisible.value = false
  } catch (e) {
    console.error(e)
  } finally {
    scopeSaving.value = false
  }
}

onMounted(async () => {
  await loadOrgTree()
  loadData()
})
</script>

<style scoped>
.mb-3 {
  margin-bottom: 12px;
}
</style>
