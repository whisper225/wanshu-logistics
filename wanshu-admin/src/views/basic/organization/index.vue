<template>
  <PageContainer title="机构管理">
    <el-alert type="info" show-icon :closable="false" class="tip">
      机构数据保存在 MySQL，保存后会同步到 Neo4j。树状视图便于浏览层级；「列表维护」支持分页与批量检索。
    </el-alert>

    <el-tabs v-model="activeTab" class="organ-tabs" @tab-change="onTabChange">
      <el-tab-pane label="树状视图" name="tree">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-card>
              <template #header>
                <div class="card-header">
                  <span>机构树</span>
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
              <div class="tree-actions">
                <el-button type="primary" size="small" @click="organDialogRef?.openCreateTop()">新增顶级机构</el-button>
              </div>
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
                    <el-tag size="small" type="info">{{ organTypeLabel(Number(data.type)) }}</el-tag>
                  </span>
                </template>
              </el-tree>
            </el-card>
          </el-col>
          <el-col :span="16">
            <el-card v-if="detailView">
              <template #header>
                <div class="card-header">
                  <span>机构详情</span>
                  <div class="header-actions">
                    <el-button type="primary" size="small" @click="organDialogRef?.openEditById(String(detailView!.id!))">
                      编辑
                    </el-button>
                    <el-button
                      type="primary"
                      size="small"
                      plain
                      @click="organDialogRef?.openCreateChild(String(detailView!.id!))"
                    >
                      新增下级
                    </el-button>
                    <el-button
                      type="danger"
                      size="small"
                      :disabled="detailView?.hasChildren === true"
                      @click="handleDeleteCurrent"
                    >
                      删除
                    </el-button>
                  </div>
                </div>
              </template>
              <el-descriptions :column="1" border>
                <el-descriptions-item label="机构名称">{{ detailView.organName }}</el-descriptions-item>
                <el-descriptions-item label="类型">{{ organTypeLabel(detailView.organType) }}</el-descriptions-item>
                <el-descriptions-item label="上级机构ID">
                  {{ detailView.parentId == null || Number(detailView.parentId) === 0 ? '—' : String(detailView.parentId) }}
                </el-descriptions-item>
                <el-descriptions-item label="省/市/区 ID">
                  {{ [detailView.provinceId, detailView.cityId, detailView.countyId].filter(Boolean).join(' / ') || '—' }}
                </el-descriptions-item>
                <el-descriptions-item label="详细地址">{{ detailView.address || '—' }}</el-descriptions-item>
                <el-descriptions-item label="经纬度">
                  {{ detailView.longitude ?? '—' }}, {{ detailView.latitude ?? '—' }}
                </el-descriptions-item>
                <el-descriptions-item label="负责人">{{ detailView.managerName || '—' }}</el-descriptions-item>
                <el-descriptions-item label="负责人电话">{{ detailView.managerPhone || '—' }}</el-descriptions-item>
                <el-descriptions-item label="对接人">{{ detailView.contactName || '—' }}</el-descriptions-item>
                <el-descriptions-item label="对接人电话">{{ detailView.contactPhone || '—' }}</el-descriptions-item>
                <el-descriptions-item label="排序">{{ detailView.sortOrder ?? 0 }}</el-descriptions-item>
                <el-descriptions-item label="状态">
                  <el-tag :type="detailView.status === 1 ? 'success' : 'info'" size="small">
                    {{ detailView.status === 1 ? '启用' : '停用' }}
                  </el-tag>
                </el-descriptions-item>
              </el-descriptions>

              <el-divider />
              <div class="employee-section">
                <h4>员工信息</h4>
                <el-empty description="暂未对接员工列表" :image-size="64" />
              </div>
            </el-card>
            <el-empty v-else description="请在左侧选择机构" />
          </el-col>
        </el-row>
      </el-tab-pane>

      <el-tab-pane label="列表维护" name="list" lazy>
        <OrganListPanel ref="listPanelRef" @success="onOrganFormSuccess" />
      </el-tab-pane>
    </el-tabs>

    <OrganFormDialog ref="organDialogRef" @success="onOrganFormSuccess" />
  </PageContainer>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { organizationApi, type BaseOrganPayload } from '@/api/organization'
import { organTypeLabel } from '@/utils/organization'
import PageContainer from '@/components/PageContainer.vue'
import OrganFormDialog from '@/components/organization/OrganFormDialog.vue'
import OrganListPanel from '@/components/organization/OrganListPanel.vue'

const route = useRoute()
const router = useRouter()

const searchKeyword = ref('')
/** 与 GET /base/organ/tree 一致，type 为后端数字 1/2/3 */
const organizationTree = ref<Record<string, unknown>[]>([])
const treeRef = ref()
const detailView = ref<BaseOrganPayload | null>(null)
const organDialogRef = ref<InstanceType<typeof OrganFormDialog>>()
const listPanelRef = ref<InstanceType<typeof OrganListPanel>>()

const activeTab = ref<'tree' | 'list'>('tree')

async function loadOrganizations() {
  try {
    organizationTree.value = await organizationApi.getTree({ keyword: searchKeyword.value })
  } catch (error) {
    console.error('Load organizations failed:', error)
  }
}

function handleTreeFilter() {
  if (treeRef.value) {
    treeRef.value.filter(searchKeyword.value)
  }
}

const filterNode = (value: string, data: Record<string, unknown>) => {
  if (!value) return true
  return String(data.name ?? '').includes(value)
}

const handleNodeClick = async (data: Record<string, unknown>) => {
  try {
    detailView.value = await organizationApi.getDetail(String(data.id))
  } catch {
    detailView.value = null
  }
}

async function handleDeleteCurrent() {
  if (!detailView.value?.id) return
  if (detailView.value.hasChildren === true) {
    ElMessage.warning('请先删除或调整下级机构后再删除本机构')
    return
  }
  try {
    await ElMessageBox.confirm(
      `确定删除机构「${detailView.value.organName}」？若存在下级机构将无法删除。`,
      '删除确认',
      { type: 'warning' }
    )
    await organizationApi.delete(String(detailView.value.id))
    ElMessage.success('已删除')
    detailView.value = null
    await loadOrganizations()
    listPanelRef.value?.loadData()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

async function onOrganFormSuccess() {
  await loadOrganizations()
  listPanelRef.value?.loadData()
  if (detailView.value?.id) {
    try {
      detailView.value = await organizationApi.getDetail(String(detailView.value.id))
    } catch {
      detailView.value = null
    }
  }
}

function onTabChange(name: string | number) {
  const v = name === 'list' ? 'list' : 'tree'
  router.replace({ path: '/basic/organization', query: v === 'list' ? { view: 'list' } : {} })
}

onMounted(() => {
  if (route.query.view === 'list') {
    activeTab.value = 'list'
  }
  loadOrganizations()
})

watch(
  () => route.query.view,
  (v) => {
    if (v === 'list') activeTab.value = 'list'
    else if (v === undefined || v === 'tree') activeTab.value = 'tree'
  }
)
</script>

<style scoped lang="scss">
.tip {
  margin-bottom: 16px;
}

.organ-tabs {
  margin-top: 8px;
}

.tree-actions {
  margin-bottom: 12px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 8px;
}

.header-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tree-node {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex: 1;
  padding-right: 10px;
}

.employee-section {
  margin-top: 12px;

  h4 {
    margin-bottom: 12px;
  }
}
</style>
