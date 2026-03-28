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
                @input="handleTreeFilter"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
            </div>
          </template>
          <el-tree
            ref="treeRef"
            :data="organizationTree"
            :props="{ label: 'name', children: 'children' }"
            :expand-on-click-node="false"
            :filter-node-method="filterNode"
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
            <el-alert type="info" show-icon :closable="false" class="scope-tip">
              省 / 市 / 区 ID 与库表约定为<strong>国标 adcode</strong>（与级联及地图一致）。
            </el-alert>
            <el-form :disabled="!isEditing">
              <el-form-item label="作业范围">
                <el-cascader
                  v-model="selectedAreas"
                  :options="areaOptions"
                  :props="cascaderProps"
                  placeholder="请选择省 / 市 / 区（可多选）"
                  style="width: 100%"
                  clearable
                  collapse-tags
                  collapse-tags-tooltip
                />
              </el-form-item>
            </el-form>

            <el-divider />

            <div class="area-map">
              <h4>业务范围查看</h4>
              <p v-if="!amapKey" class="map-hint">
                未配置环境变量 <code>VITE_AMAP_KEY</code>，地图仅显示占位。请使用<strong> JS API（Web 端）</strong>Key。
              </p>
              <p v-else-if="!amapSecurityCode" class="map-hint">
                已配置 Key 但未配置 <code>VITE_AMAP_SECURITY_CODE</code>。2021-12 后申请的 Key 必须在控制台复制<strong>安全密钥</strong>并写入该变量，否则地图瓦片常为空白；修改 .env 后需<strong>重启</strong> dev 服务。
              </p>
              <div v-if="amapKey" ref="mapContainerRef" class="map-canvas" />
              <div v-else class="map-placeholder">
                <span>地图区域（需高德 Key）</span>
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
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { regionData } from 'element-china-area-data'
import { organizationApi, type OrganScope } from '@/api/organization'
import PageContainer from '@/components/PageContainer.vue'
import { pathsToOrganScopes, scopesToPaths } from '@/utils/regionScope'
import { mountScopePolygons, type AmapScopeMapHandle } from '@/utils/amapScopeMap'

type TreeOrg = Record<string, unknown> & { id?: string; name?: string; children?: TreeOrg[] }

const searchKeyword = ref('')
const treeRef = ref()
const organizationTree = ref<TreeOrg[]>([])
const currentOrg = ref<TreeOrg | null>(null)
const isEditing = ref(false)
/** 级联多选路径（国标 adcode 字符串） */
const selectedAreas = ref<string[][]>([])
const areaOptions = ref(regionData)
const loadedScopes = ref<OrganScope[]>([])
const editSnapshot = ref<string[][]>([])
const mapContainerRef = ref<HTMLElement>()

const amapKey = (import.meta.env.VITE_AMAP_KEY as string | undefined)?.trim()
/** 与 Key 配套的安全密钥（控制台「安全密钥」）；2021-12 后申请的 Key 不配则地图常为空白 */
const amapSecurityCode = (import.meta.env.VITE_AMAP_SECURITY_CODE as string | undefined)?.trim()

const cascaderProps = {
  multiple: true,
  checkStrictly: true,
  value: 'value',
  label: 'label',
  children: 'children',
}

let mapHandle: AmapScopeMapHandle | null = null

/** 地图上展示的 scopes：查看态用已保存数据；编辑态用当前级联选择预览 */
const mapScopes = computed((): OrganScope[] => {
  if (!currentOrg.value) return []
  if (isEditing.value) return pathsToOrganScopes(selectedAreas.value)
  return loadedScopes.value
})

const loadOrganizations = async () => {
  try {
    organizationTree.value = (await organizationApi.getTree({
      keyword: searchKeyword.value || undefined,
    })) as unknown as TreeOrg[]
  } catch (error) {
    console.error('Load organizations failed:', error)
  }
}

function handleTreeFilter() {
  treeRef.value?.filter(searchKeyword.value)
}

const filterNode = (value: string, data: TreeOrg) => {
  if (!value) return true
  return String(data.name ?? '').includes(value)
}

const handleNodeClick = async (data: TreeOrg) => {
  currentOrg.value = data
  isEditing.value = false
  const id = String(data.id ?? '')
  if (!id) return
  try {
    const scopes = (await organizationApi.getScopes(id)) as OrganScope[]
    loadedScopes.value = Array.isArray(scopes) ? scopes : []
    selectedAreas.value = scopesToPaths(loadedScopes.value)
  } catch (error) {
    console.error('Load service area failed:', error)
    loadedScopes.value = []
    selectedAreas.value = []
  }
}

const handleEdit = () => {
  editSnapshot.value = JSON.parse(JSON.stringify(selectedAreas.value)) as string[][]
  isEditing.value = true
}

const handleSave = async () => {
  if (!currentOrg.value) return
  const id = String(currentOrg.value.id ?? '')
  if (!id) return
  try {
    const scopes = pathsToOrganScopes(selectedAreas.value).map((s) => ({
      provinceId: s.provinceId,
      cityId: s.cityId,
      countyId: s.countyId,
    }))
    await organizationApi.updateScopes(id, scopes)
    ElMessage.success('保存成功')
    isEditing.value = false
    const reloaded = (await organizationApi.getScopes(id)) as OrganScope[]
    loadedScopes.value = Array.isArray(reloaded) ? reloaded : []
    selectedAreas.value = scopesToPaths(loadedScopes.value)
  } catch (error) {
    console.error('Save failed:', error)
  }
}

const handleCancel = () => {
  selectedAreas.value = JSON.parse(JSON.stringify(editSnapshot.value)) as string[][]
  isEditing.value = false
}

async function refreshMap() {
  await nextTick()
  mapHandle?.destroy()
  mapHandle = null
  const el = mapContainerRef.value
  if (!el || !amapKey || !currentOrg.value) return
  try {
    mapHandle = await mountScopePolygons(el, amapKey, mapScopes.value, undefined, amapSecurityCode)
  } catch (e) {
    console.error('Map render failed:', e)
  }
}

watch(
  () => [currentOrg.value?.id, loadedScopes.value, isEditing.value, selectedAreas.value] as const,
  () => {
    if (amapKey) void refreshMap()
  },
  { deep: true, flush: 'post' }
)

onMounted(() => {
  loadOrganizations()
})

onUnmounted(() => {
  mapHandle?.destroy()
  mapHandle = null
})
</script>

<style scoped lang="scss">
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.scope-tip {
  margin-bottom: 16px;
}

.service-area-content {
  .area-map {
    h4 {
      margin-bottom: 12px;
    }
  }
}

.map-hint {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  margin-bottom: 8px;
  line-height: 1.5;
}

.map-canvas {
  height: 400px;
  width: 100%;
  border-radius: 4px;
  overflow: hidden;
  border: 1px solid var(--el-border-color-lighter);
}

.map-placeholder {
  height: 400px;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  color: #999;
  font-size: 14px;
}
</style>
