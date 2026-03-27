<template>
  <PageContainer title="机构管理">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>机构列表</span>
              <el-input
                v-model="searchKeyword"
                placeholder="搜索机构"
                size="small"
                style="width: 200px"
                clearable
                @input="handleSearch"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </div>
          </template>
          <el-tree
            :data="organizationTree"
            :props="{ label: 'name', children: 'children' }"
            :expand-on-click-node="false"
            :filter-node-method="filterNode"
            ref="treeRef"
            @node-click="handleNodeClick"
            highlight-current
          >
            <template #default="{ node, data }">
              <span class="tree-node">
                <span>{{ node.label }}</span>
                <el-tag size="small" type="info">{{ getOrganizationTypeLabel(data.type) }}</el-tag>
              </span>
            </template>
          </el-tree>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card v-if="currentOrg">
          <template #header>
            <div class="card-header">
              <span>机构信息</span>
              <el-button v-if="!isEditing" type="primary" size="small" @click="handleEdit">编辑</el-button>
              <div v-else>
                <el-button type="primary" size="small" @click="handleSave">保存</el-button>
                <el-button size="small" @click="handleCancel">取消</el-button>
              </div>
            </div>
          </template>
          <el-form :model="formData" label-width="120px" :disabled="!isEditing">
            <el-form-item label="机构名称">
              <el-input v-model="formData.name" />
            </el-form-item>
            <el-form-item label="机构类型">
              <el-select v-model="formData.type" placeholder="请选择">
                <el-option label="总公司" value="HEADQUARTERS" />
                <el-option label="分公司" value="BRANCH" />
                <el-option label="一级转运中心" value="PRIMARY_HUB" />
                <el-option label="二级分拣中心" value="SECONDARY_HUB" />
                <el-option label="营业部" value="STATION" />
              </el-select>
            </el-form-item>
            <el-form-item label="机构地址">
              <el-cascader
                v-model="addressValue"
                :options="addressOptions"
                placeholder="请选择省市区"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="详细地址">
              <el-input v-model="formData.address.detail" />
            </el-form-item>
            <el-form-item label="机构负责人">
              <el-input v-model="formData.manager" maxlength="5" />
            </el-form-item>
            <el-form-item label="负责人电话">
              <el-input v-model="formData.managerPhone" maxlength="11" />
            </el-form-item>
            <el-form-item label="机构对接人">
              <el-input v-model="formData.contact" maxlength="5" />
            </el-form-item>
            <el-form-item label="对接人电话">
              <el-input v-model="formData.contactPhone" maxlength="11" />
            </el-form-item>
            <el-form-item label="经度">
              <el-input-number v-model="formData.longitude" :precision="6" :min="-180" :max="180" />
              <el-radio-group v-model="formData.longitudeDirection" style="margin-left: 10px">
                <el-radio label="E">东经</el-radio>
                <el-radio label="W">西经</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="纬度">
              <el-input-number v-model="formData.latitude" :precision="6" :min="-90" :max="90" />
              <el-radio-group v-model="formData.latitudeDirection" style="margin-left: 10px">
                <el-radio label="N">北纬</el-radio>
                <el-radio label="S">南纬</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-form>
          
          <el-divider />
          
          <div class="employee-section">
            <h4>员工信息</h4>
            <el-table :data="employees" style="width: 100%">
              <el-table-column prop="name" label="姓名" />
              <el-table-column prop="phone" label="电话" />
              <el-table-column prop="role" label="角色" />
              <el-table-column prop="createdAt" label="加入时间" />
            </el-table>
          </div>
        </el-card>
        <el-empty v-else description="请选择机构" />
      </el-col>
    </el-row>
  </PageContainer>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { organizationApi } from '@/api/organization'
import { getOrganizationTypeLabel } from '@/utils'
import type { Organization } from '@/types'
import PageContainer from '@/components/PageContainer.vue'

const searchKeyword = ref('')
const organizationTree = ref<Organization[]>([])
const currentOrg = ref<Organization | null>(null)
const isEditing = ref(false)
const treeRef = ref()
const employees = ref<any[]>([])
const addressValue = ref<string[]>([])
const addressOptions = ref<any[]>([])

const formData = reactive<Partial<Organization>>({
  name: '',
  type: 'STATION',
  address: {
    province: '',
    city: '',
    district: '',
    detail: ''
  },
  manager: '',
  managerPhone: '',
  contact: '',
  contactPhone: '',
  latitude: 0,
  latitudeDirection: 'N',
  longitude: 0,
  longitudeDirection: 'E',
  status: 'ACTIVE'
})

const loadOrganizations = async () => {
  try {
    organizationTree.value = await organizationApi.getTree({ keyword: searchKeyword.value })
  } catch (error) {
    console.error('Load organizations failed:', error)
  }
}

const handleSearch = () => {
  if (treeRef.value) {
    treeRef.value.filter(searchKeyword.value)
  }
}

const filterNode = (value: string, data: Organization) => {
  if (!value) return true
  return data.name.includes(value)
}

const handleNodeClick = async (data: Organization) => {
  currentOrg.value = data
  Object.assign(formData, data)
  addressValue.value = [data.address.province, data.address.city, data.address.district]
  isEditing.value = false
  
  try {
    const res = await organizationApi.getEmployees(data.id)
    employees.value = res.list
  } catch (error) {
    console.error('Load employees failed:', error)
  }
}

const handleEdit = () => {
  isEditing.value = true
}

const handleSave = async () => {
  if (!currentOrg.value) return
  
  try {
    await organizationApi.update(currentOrg.value.id, formData)
    ElMessage.success('保存成功')
    isEditing.value = false
    loadOrganizations()
  } catch (error) {
    console.error('Save failed:', error)
  }
}

const handleCancel = () => {
  if (currentOrg.value) {
    Object.assign(formData, currentOrg.value)
  }
  isEditing.value = false
}

watch(addressValue, (val) => {
  if (val && val.length === 3) {
    formData.address = {
      province: val[0],
      city: val[1],
      district: val[2],
      detail: formData.address?.detail || ''
    }
  }
})

onMounted(() => {
  loadOrganizations()
})
</script>

<style scoped lang="scss">
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.tree-node {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex: 1;
  padding-right: 10px;
}

.employee-section {
  margin-top: 20px;
  
  h4 {
    margin-bottom: 15px;
  }
}
</style>
