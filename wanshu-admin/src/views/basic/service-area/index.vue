<template>
  <PageContainer title="机构作业范围管理">
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
            @node-click="handleNodeClick"
            highlight-current
          />
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card v-if="currentOrg">
          <template #header>
            <div class="card-header">
              <span>作业范围 - {{ currentOrg.name }}</span>
              <el-button v-if="!isEditing" type="primary" size="small" @click="handleEdit">编辑</el-button>
              <div v-else>
                <el-button type="primary" size="small" @click="handleSave">保存</el-button>
                <el-button size="small" @click="handleCancel">取消</el-button>
              </div>
            </div>
          </template>
          <div class="service-area-content">
            <el-form :disabled="!isEditing">
              <el-form-item label="作业范围">
                <el-cascader
                  v-model="selectedAreas"
                  :options="areaOptions"
                  :props="cascaderProps"
                  placeholder="请选择省市区"
                  style="width: 100%"
                  clearable
                />
              </el-form-item>
            </el-form>
            
            <el-divider />
            
            <div class="area-map">
              <h4>业务范围查看</h4>
              <div ref="mapRef" style="height: 400px; background: #f5f5f5; display: flex; align-items: center; justify-content: center;">
                <span style="color: #999;">地图区域展示</span>
              </div>
            </div>
          </div>
        </el-card>
        <el-empty v-else description="请选择机构" />
      </el-col>
    </el-row>
  </PageContainer>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { organizationApi } from '@/api/organization'
import type { Organization, ServiceArea } from '@/types'
import PageContainer from '@/components/PageContainer.vue'

const searchKeyword = ref('')
const organizationTree = ref<Organization[]>([])
const currentOrg = ref<Organization | null>(null)
const isEditing = ref(false)
const selectedAreas = ref<string[]>([])
const areaOptions = ref<any[]>([])
const mapRef = ref<HTMLElement>()

const cascaderProps = {
  multiple: true,
  checkStrictly: true,
  value: 'code',
  label: 'name',
  children: 'children'
}

const loadOrganizations = async () => {
  try {
    organizationTree.value = await organizationApi.getTree()
  } catch (error) {
    console.error('Load organizations failed:', error)
  }
}

const handleNodeClick = async (data: Organization) => {
  currentOrg.value = data
  isEditing.value = false
  
  try {
    const serviceArea = await organizationApi.getServiceArea(data.id)
    selectedAreas.value = []
  } catch (error) {
    console.error('Load service area failed:', error)
  }
}

const handleEdit = () => {
  isEditing.value = true
}

const handleSave = async () => {
  if (!currentOrg.value) return
  
  try {
    await organizationApi.updateServiceArea(currentOrg.value.id, {
      organizationId: currentOrg.value.id,
      areas: []
    })
    ElMessage.success('保存成功')
    isEditing.value = false
  } catch (error) {
    console.error('Save failed:', error)
  }
}

const handleCancel = () => {
  isEditing.value = false
}

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

.service-area-content {
  .area-map {
    h4 {
      margin-bottom: 15px;
    }
  }
}
</style>
