<template>
  <PageContainer title="用户管理">
    <SearchForm v-model="searchForm" @search="handleSearch" @reset="handleReset">
      <el-form-item label="关键词">
        <el-input v-model="searchForm.keyword" placeholder="用户名/姓名/手机号" clearable />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="searchForm.status" placeholder="请选择" clearable>
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
      </el-form-item>
      <template #actions>
        <el-button type="primary" @click="handleAdd">新增用户</el-button>
      </template>
    </SearchForm>

    <el-table :data="tableData" v-loading="loading">
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="realName" label="姓名" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column label="所属机构" min-width="160" show-overflow-tooltip>
        <template #default="{ row }">
          {{ organLabel(row.organId) }}
        </template>
      </el-table-column>
      <el-table-column prop="gender" label="性别">
        <template #default="{ row }">
          {{ row.gender === 1 ? '男' : row.gender === 2 ? '女' : '未知' }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="lastLoginTime" label="最后登录时间" width="180" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="primary" @click="handleAssignRoles(row)">分配角色</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="formData" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="formData.username" :disabled="!!formData.id" />
        </el-form-item>
        <el-form-item label="密码" :prop="formData.id ? '' : 'password'">
          <el-input v-model="formData.password" type="password" show-password
            :placeholder="formData.id ? '不修改请留空' : '请输入密码'" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="formData.realName" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="formData.phone" />
        </el-form-item>
        <el-form-item label="所属机构">
          <el-select
            v-model="formData.organId"
            placeholder="请选择机构"
            clearable
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="o in flatOrgOptions"
              :key="o.value"
              :label="o.label"
              :value="o.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="formData.gender">
            <el-radio :value="0">未知</el-radio>
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="roleDialogVisible" title="分配角色" width="500px">
      <el-checkbox-group v-model="selectedRoleIds">
        <el-checkbox v-for="role in allRoles" :key="role.id" :value="role.id" :label="role.id">
          {{ role.roleName }}（{{ role.roleCode }}）
        </el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveRoles">确定</el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { systemApi } from '@/api/system'
import { organizationApi } from '@/api/organization'
import PageContainer from '@/components/PageContainer.vue'
import SearchForm from '@/components/SearchForm.vue'

type TreeOrg = Record<string, unknown> & { id?: string | number; name?: string; children?: TreeOrg[] }

const loading = ref(false)
const tableData = ref<any[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const roleDialogVisible = ref(false)
const allRoles = ref<any[]>([])
const selectedRoleIds = ref<number[]>([])
const currentUserId = ref<number>()

const searchForm = reactive({ keyword: '', status: undefined as number | undefined })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const flatOrgOptions = ref<{ value: string; label: string }[]>([])

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

function organLabel(organId: string | number | undefined | null) {
  if (organId === undefined || organId === null || organId === '') return '—'
  const sid = String(organId)
  const hit = flatOrgOptions.value.find((o) => o.value === sid)
  if (!hit) return sid
  return hit.label.replace(/^[\s　]+/, '')
}

const loadOrgTree = async () => {
  try {
    const tree = (await organizationApi.getTree()) as unknown as TreeOrg[]
    flatOrgOptions.value = flattenOrgTree(Array.isArray(tree) ? tree : [])
  } catch (e) {
    console.error(e)
  }
}

const formData = reactive<any>({
  id: undefined,
  username: '',
  password: '',
  realName: '',
  phone: '',
  organId: undefined as string | undefined,
  gender: 0,
  status: 1
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await systemApi.getUserList({ ...searchForm, ...pagination })
    tableData.value = res.list
    pagination.total = res.total
  } catch (e) { console.error(e) } finally { loading.value = false }
}

const handleSearch = () => { pagination.pageNum = 1; loadData() }
const handleReset = () => { searchForm.keyword = ''; searchForm.status = undefined; handleSearch() }

const handleAdd = () => {
  dialogTitle.value = '新增用户'
  Object.assign(formData, {
    id: undefined,
    username: '',
    password: '',
    realName: '',
    phone: '',
    organId: undefined,
    gender: 0,
    status: 1
  })
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogTitle.value = '编辑用户'
  Object.assign(formData, {
    ...row,
    password: '',
    organId: row.organId != null && row.organId !== '' ? String(row.organId) : undefined
  })
  dialogVisible.value = true
}

const handleDelete = async (row: any) => {
  await ElMessageBox.confirm('确定删除该用户吗？', '提示', { type: 'warning' })
  await systemApi.deleteUser(row.id)
  ElMessage.success('删除成功')
  loadData()
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    const payload = { ...formData }
    if (payload.organId === '' || payload.organId === undefined) {
      payload.organId = null
    }
    if (formData.id) {
      await systemApi.updateUser(formData.id, payload)
    } else {
      await systemApi.createUser(payload)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  })
}

const handleAssignRoles = async (row: any) => {
  currentUserId.value = row.id
  const [roles, userRoles] = await Promise.all([
    systemApi.getRoleList({ pageNum: 1, pageSize: 100 }),
    systemApi.getUserRoles(row.id)
  ])
  allRoles.value = roles.list || roles
  selectedRoleIds.value = userRoles || []
  roleDialogVisible.value = true
}

const handleSaveRoles = async () => {
  if (!currentUserId.value) return
  await systemApi.assignRoles(currentUserId.value, selectedRoleIds.value)
  ElMessage.success('角色分配成功')
  roleDialogVisible.value = false
}

onMounted(async () => {
  await loadOrgTree()
  loadData()
})
</script>
