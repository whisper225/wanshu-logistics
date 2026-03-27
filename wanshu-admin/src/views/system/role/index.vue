<template>
  <PageContainer title="角色管理">
    <SearchForm v-model="searchForm" @search="handleSearch" @reset="handleReset">
      <el-form-item label="关键词">
        <el-input v-model="searchForm.keyword" placeholder="角色名称/编码" clearable />
      </el-form-item>
      <template #actions>
        <el-button type="primary" @click="handleAdd">新增角色</el-button>
      </template>
    </SearchForm>

    <el-table :data="tableData" v-loading="loading">
      <el-table-column prop="roleName" label="角色名称" />
      <el-table-column prop="roleCode" label="角色编码" />
      <el-table-column prop="description" label="描述" />
      <el-table-column prop="loginScope" label="可登录端" />
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="primary" @click="handleAssignMenus(row)">分配权限</el-button>
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
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="formData.roleName" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="formData.roleCode" :disabled="!!formData.id" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="formData.description" type="textarea" />
        </el-form-item>
        <el-form-item label="可登录端">
          <el-checkbox-group v-model="loginScopes">
            <el-checkbox label="admin" value="admin">管理端</el-checkbox>
            <el-checkbox label="courier" value="courier">快递员端</el-checkbox>
            <el-checkbox label="driver" value="driver">司机端</el-checkbox>
          </el-checkbox-group>
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

    <el-dialog v-model="menuDialogVisible" title="分配权限" width="500px">
      <el-tree
        ref="menuTreeRef"
        :data="menuTree"
        :props="{ label: 'menuName', children: 'children' }"
        show-checkbox
        node-key="id"
        :default-checked-keys="checkedMenuIds"
      />
      <template #footer>
        <el-button @click="menuDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveMenus">确定</el-button>
      </template>
    </el-dialog>
  </PageContainer>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { systemApi } from '@/api/system'
import PageContainer from '@/components/PageContainer.vue'
import SearchForm from '@/components/SearchForm.vue'

const loading = ref(false)
const tableData = ref<any[]>([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const menuDialogVisible = ref(false)
const menuTreeRef = ref()
const menuTree = ref<any[]>([])
const checkedMenuIds = ref<number[]>([])
const currentRoleId = ref<number>()
const loginScopes = ref<string[]>([])

const searchForm = reactive({ keyword: '' })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })
const formData = reactive<any>({
  id: undefined, roleName: '', roleCode: '', description: '', loginScope: '', status: 1
})

const rules: FormRules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }]
}

watch(loginScopes, (val) => { formData.loginScope = val.join(',') })

const loadData = async () => {
  loading.value = true
  try {
    const res = await systemApi.getRoleList({ ...searchForm, ...pagination })
    tableData.value = res.list
    pagination.total = res.total
  } catch (e) { console.error(e) } finally { loading.value = false }
}

const handleSearch = () => { pagination.pageNum = 1; loadData() }
const handleReset = () => { searchForm.keyword = ''; handleSearch() }

const handleAdd = () => {
  dialogTitle.value = '新增角色'
  Object.assign(formData, { id: undefined, roleName: '', roleCode: '', description: '', loginScope: '', status: 1 })
  loginScopes.value = []
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogTitle.value = '编辑角色'
  Object.assign(formData, row)
  loginScopes.value = row.loginScope ? row.loginScope.split(',') : []
  dialogVisible.value = true
}

const handleDelete = async (row: any) => {
  await ElMessageBox.confirm('确定删除该角色吗？', '提示', { type: 'warning' })
  await systemApi.deleteRole(row.id)
  ElMessage.success('删除成功')
  loadData()
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    if (formData.id) {
      await systemApi.updateRole(formData.id, formData)
    } else {
      await systemApi.createRole(formData)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  })
}

const handleAssignMenus = async (row: any) => {
  currentRoleId.value = row.id
  const [tree, ids] = await Promise.all([
    systemApi.getPermissionTree(),
    systemApi.getRolePermissions(row.id)
  ])
  menuTree.value = tree
  checkedMenuIds.value = ids || []
  menuDialogVisible.value = true
}

const handleSaveMenus = async () => {
  if (!currentRoleId.value || !menuTreeRef.value) return
  const checkedKeys = menuTreeRef.value.getCheckedKeys()
  const halfCheckedKeys = menuTreeRef.value.getHalfCheckedKeys()
  await systemApi.assignPermissions(currentRoleId.value, [...checkedKeys, ...halfCheckedKeys])
  ElMessage.success('权限分配成功')
  menuDialogVisible.value = false
}

onMounted(() => loadData())
</script>
