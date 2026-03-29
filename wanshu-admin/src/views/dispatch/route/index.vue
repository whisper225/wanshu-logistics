<template>
  <PageContainer title="线路管理">
    <SearchForm v-model="searchForm" @search="handleSearch" @reset="handleReset">
      <el-form-item label="关键词">
        <el-input v-model="searchForm.keyword" placeholder="线路名称/编号" clearable />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="searchForm.status" placeholder="全部" clearable>
          <el-option label="启用" :value="1" />
          <el-option label="停用" :value="0" />
        </el-select>
      </el-form-item>
      <template #actions>
        <el-button type="primary" @click="openLineDialog(false)">新增线路</el-button>
        <el-button @click="openCostDialog">成本设置</el-button>
        <el-button @click="openDispatchDialog">调度配置</el-button>
      </template>
    </SearchForm>

    <el-table
      :data="tableData"
      v-loading="loading"
      row-key="id"
      @expand-change="onExpandChange"
    >
      <el-table-column type="expand" width="40">
        <template #default="{ row: lineRow }">
          <div class="trip-panel">
            <div class="trip-toolbar">
              <span class="trip-title">车次列表</span>
              <el-button type="primary" size="small" @click="openTripDialog(lineRow, null)">添加车次</el-button>
            </div>
            <el-table :data="lineRow.trips || []" size="small" border empty-text="暂无车次">
              <el-table-column prop="tripNumber" label="车次编号" width="130" />
              <el-table-column prop="tripName" label="车次名称" min-width="120" />
              <el-table-column label="发车时间" width="110">
                <template #default="{ row: t }">{{ formatDepart(t) }}</template>
              </el-table-column>
              <el-table-column label="周期" width="90">
                <template #default="{ row: t }">{{ periodLabel(t.periodType) }}</template>
              </el-table-column>
              <el-table-column label="状态" width="80">
                <template #default="{ row: t }">
                  <el-tag :type="t.status === 1 ? 'success' : 'info'" size="small">
                    {{ t.status === 1 ? '启用' : '停用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="280" fixed="right">
                <template #default="{ row: t }">
                  <el-button link type="primary" size="small" @click="openTripDialog(lineRow, t)">编辑</el-button>
                  <el-button link type="primary" size="small" @click="openArrangeDialog(t)">安排车辆</el-button>
                  <el-button link type="danger" size="small" @click="handleDeleteTrip(lineRow, t)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="lineNumber" label="线路编号" width="140" />
      <el-table-column prop="lineName" label="线路名称" min-width="140" show-overflow-tooltip />
      <el-table-column label="线路类型" width="100">
        <template #default="{ row }">{{ lineTypeLabel(row.lineType) }}</template>
      </el-table-column>
      <el-table-column prop="startOrganName" label="起始机构" width="120" show-overflow-tooltip />
      <el-table-column prop="endOrganName" label="目的地机构" width="120" show-overflow-tooltip />
      <el-table-column label="距离" width="90">
        <template #default="{ row }">{{ row.distance != null ? row.distance : '—' }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openLineDialog(true, row)">编辑</el-button>
          <el-button link type="primary" @click="openMapDialog(row)">线路地图</el-button>
          <el-button link type="danger" @click="handleDeleteLine(row)">删除</el-button>
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

    <!-- 新增/编辑线路：仅基本信息 -->
    <el-dialog v-model="lineDialogVisible" :title="lineDialogTitle" width="560px" destroy-on-close @closed="resetLineForm">
      <el-form ref="lineFormRef" :model="lineForm" :rules="lineRules" label-width="120px">
        <el-form-item label="线路编号" prop="lineNumberSuffix">
          <el-input v-model="lineForm.lineNumberSuffix" placeholder="XL 后的编码" maxlength="28" show-word-limit>
            <template #prepend>XL</template>
          </el-input>
        </el-form-item>
        <el-form-item label="线路名称" prop="lineName">
          <el-input v-model="lineForm.lineName" />
        </el-form-item>
        <el-form-item label="线路类型" prop="lineType">
          <el-select v-model="lineForm.lineType" style="width: 100%">
            <el-option label="干线" :value="1" />
            <el-option label="支线" :value="2" />
            <el-option label="接驳路线" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="起始机构" prop="startOrganId">
          <el-tree-select
            v-model="lineForm.startOrganId"
            :data="organTree"
            :props="treeProps"
            filterable
            check-strictly
            default-expand-all
            placeholder="请选择起始机构"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="目的地机构" prop="endOrganId">
          <el-tree-select
            v-model="lineForm.endOrganId"
            :data="organTree"
            :props="treeProps"
            filterable
            check-strictly
            default-expand-all
            placeholder="请选择目的地机构"
            style="width: 100%"
          />
        </el-form-item>
        <!-- 编辑时可手动修改距离和成本；新增时由后端自动计算 -->
        <template v-if="lineForm.id != null">
          <el-form-item label="距离（米）">
            <el-input-number v-model="lineForm.distance" :min="0" :precision="2" style="width: 100%" />
          </el-form-item>
          <el-form-item label="成本（元）">
            <el-input-number v-model="lineForm.cost" :min="0" :precision="2" style="width: 100%" />
          </el-form-item>
        </template>
        <el-form-item label="状态">
          <el-radio-group v-model="lineForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-alert v-if="lineForm.id == null" type="info" :closable="false" show-icon>
          距离将通过高德地图自动计算，成本依据全局成本设置估算
        </el-alert>
      </el-form>
      <template #footer>
        <el-button @click="lineDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="lineSaving" @click="submitLine">确定</el-button>
      </template>
    </el-dialog>

    <!-- 成本设置：全局，按三种路线类型（干线/支线/接驳路线）每公里成本 -->
    <el-dialog v-model="costDialogVisible" title="成本设置" width="560px" destroy-on-close>
      <el-alert type="info" :closable="false" show-icon class="mb-3">
        按<strong>路线类型</strong>设置默认每公里成本（元）：干线、支线、接驳路线（对应线路类型 1/2/3），供全局调度与测算参考。
      </el-alert>
      <el-form label-width="180px">
        <el-form-item label="干线（类型1）每公里成本">
          <el-input-number v-model="costForm.costPerKmType1" :min="0" :precision="4" style="width: 100%" />
        </el-form-item>
        <el-form-item label="支线（类型2）每公里成本">
          <el-input-number v-model="costForm.costPerKmType2" :min="0" :precision="4" style="width: 100%" />
        </el-form-item>
        <el-form-item label="接驳路线（类型3）每公里成本">
          <el-input-number v-model="costForm.costPerKmType3" :min="0" :precision="4" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="costDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="costSaving" @click="submitCost">保存</el-button>
      </template>
    </el-dialog>

    <!-- 调度配置：全局规则（dispatch_config） -->
    <el-dialog v-model="dispatchDialogVisible" title="调度配置" width="560px" destroy-on-close>
      <el-alert type="info" :closable="false" show-icon class="mb-3">
        全局调度规则，与具体线路无关，影响所有调度任务的下发与匹配策略。
      </el-alert>
      <el-form label-width="160px">
        <el-form-item label="最晚任务下发时间" required>
          <el-input-number
            v-model="globalDispatchForm.latestDispatchHour"
            :min="1"
            :max="168"
            :step="1"
            :precision="0"
          />
          <span class="form-hint">小时前，整数，最小 1</span>
        </el-form-item>
        <el-form-item label="优先匹配方式">
          <el-select v-model="globalDispatchForm.priorityFirst" style="width: 100%">
            <el-option label="转运次数最少" :value="1" />
            <el-option label="成本最低" :value="2" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dispatchDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="dispatchSaving" @click="submitDispatch">保存</el-button>
      </template>
    </el-dialog>

    <!-- 线路地图 -->
    <el-dialog v-model="mapDialogVisible" title="线路信息" width="560px" destroy-on-close>
      <div v-if="mapLine">
        <p><strong>线路：</strong>{{ mapLine.lineName }}（{{ mapLine.lineNumber }}）</p>
        <p><strong>类型：</strong>{{ lineTypeLabel(mapLine.lineType) }}</p>
        <p><strong>起始：</strong>{{ mapLine.startOrganName || mapLine.startOrganId }}</p>
        <p><strong>目的：</strong>{{ mapLine.endOrganName || mapLine.endOrganId }}</p>
        <p><strong>距离：</strong>{{ mapLine.distance != null ? mapLine.distance + ' 米' : '未计算' }}</p>
        <p><strong>成本：</strong>{{ mapLine.cost != null ? mapLine.cost + ' 元' : '未计算' }}</p>
      </div>
    </el-dialog>

    <!-- 车次 -->
    <el-dialog v-model="tripDialogVisible" :title="tripDialogTitle" width="560px" destroy-on-close @closed="tripEditLine = null">
      <el-form ref="tripFormRef" :model="tripForm" :rules="tripRules" label-width="100px">
        <el-form-item label="车次编号" prop="tripNumber">
          <el-input v-model="tripForm.tripNumber" />
        </el-form-item>
        <el-form-item label="车次名称" prop="tripName">
          <el-input v-model="tripForm.tripName" />
        </el-form-item>
        <el-form-item label="发车周期" prop="periodType">
          <el-select v-model="tripForm.periodType" style="width: 100%">
            <el-option label="天" :value="1" />
            <el-option label="周" :value="2" />
            <el-option label="月" :value="3" />
            <el-option label="一次" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="发车日" prop="departDay">
          <el-input v-model="tripForm.departDay" placeholder="周1-7 / 月1-31 / 一次填日期" />
        </el-form-item>
        <el-form-item label="发车时间" prop="departTime">
          <el-time-picker v-model="tripForm.departTime" value-format="HH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="tripForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="tripDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="tripSaving" @click="submitTrip">确定</el-button>
      </template>
    </el-dialog>

    <!-- 安排车辆 -->
    <el-dialog v-model="arrangeDialogVisible" title="安排车辆" width="820px" destroy-on-close @closed="onArrangeClose">
      <div v-if="arrangeTrip">
        <p class="mb-2">车次：{{ arrangeTrip.tripName }}（{{ arrangeTrip.tripNumber }}）· 已选 {{ arrangeAssigned.length }} / 200</p>
        <el-input
          v-model="arrangeKeyword"
          placeholder="模糊搜索车牌号"
          clearable
          class="mb-2"
          style="max-width: 280px"
          @keyup.enter="searchEligible"
        />
        <el-button type="primary" class="ml-2" :loading="eligibleLoading" @click="searchEligible">搜索可安排车辆</el-button>
        <el-table v-loading="eligibleLoading" :data="eligibleList" size="small" border class="mt-2" max-height="220">
          <el-table-column prop="licensePlate" label="车牌号" width="120" />
          <el-table-column prop="vehicleNumber" label="车辆编号" width="120" />
          <el-table-column prop="organId" label="所属机构ID" width="120" />
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="addVehicleToTrip(row)">添加</el-button>
            </template>
          </el-table-column>
        </el-table>
        <h4 class="section-h">本车次已安排</h4>
        <el-table :data="arrangeAssigned" size="small" border max-height="260">
          <el-table-column prop="licensePlate" label="车牌号" width="120">
            <template #default="{ row }">
              <el-link type="primary" @click="goVehicle(row.vehicleId)">{{ row.licensePlate }}</el-link>
            </template>
          </el-table-column>
          <el-table-column prop="vehicleNumber" label="车辆编号" width="120" />
          <el-table-column label="司机" min-width="160">
            <template #default="{ row }">
              <template v-if="driverLabels[row.vehicleId]">
                <el-link
                  v-for="(d, idx) in driverLabels[row.vehicleId].ids"
                  :key="idx"
                  type="primary"
                  class="driver-link"
                  @click="goDriver(d)"
                >
                  {{ driverLabels[row.vehicleId].names[idx] || d }}
                </el-link>
                <span v-if="!driverLabels[row.vehicleId].ids.length">—</span>
              </template>
              <span v-else>…</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="90">
            <template #default="{ row }">
              <el-button type="danger" link size="small" @click="removeTripVehicleRow(row)">移除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </PageContainer>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import {
  dispatchApi,
  type DispatchLineDto,
  type DispatchTripDto,
  type GlobalDispatchConfigDto,
  type TripVehicleRow
} from '@/api/dispatch'
import { organizationApi } from '@/api/organization'
import PageContainer from '@/components/PageContainer.vue'
import SearchForm from '@/components/SearchForm.vue'
import { vehicleApi } from '@/api/vehicle'
import { employeeApi } from '@/api/employee'
import type { Vehicle } from '@/types'

interface TreeNode {
  id: string
  name: string
  /** organType: 1=OLT 2=TLT 3=AGENCY */
  type?: number
  children?: TreeNode[]
}

interface LineRow extends DispatchLineDto {
  trips?: DispatchTripDto[]
  _tripsLoaded?: boolean
}

const router = useRouter()
const loading = ref(false)
const tableData = ref<LineRow[]>([])
const organTree = ref<TreeNode[]>([])
const treeProps = { value: 'id', label: 'name', children: 'children' }

const searchForm = reactive({ keyword: '', status: undefined as number | undefined })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 })

const lineDialogVisible = ref(false)
const lineDialogTitle = ref('')
const lineFormRef = ref<FormInstance>()
const lineSaving = ref(false)
const lineForm = reactive({
  id: undefined as number | undefined,
  lineNumberSuffix: '',
  lineName: '',
  lineType: 1,
  startOrganId: '' as string | number | undefined,
  endOrganId: '' as string | number | undefined,
  /** 仅编辑时使用 */
  distance: undefined as number | undefined,
  cost: undefined as number | undefined,
  status: 1
})

const costDialogVisible = ref(false)
const costSaving = ref(false)
const costForm = reactive({
  costPerKmType1: undefined as number | undefined,
  costPerKmType2: undefined as number | undefined,
  costPerKmType3: undefined as number | undefined
})

const dispatchDialogVisible = ref(false)
const dispatchSaving = ref(false)
const globalDispatchForm = reactive({
  latestDispatchHour: 1,
  priorityFirst: 1 as 1 | 2
})

const lineRules: FormRules = {
  lineNumberSuffix: [{ required: true, message: '请输入编号后缀', trigger: 'blur' }],
  lineName: [{ required: true, message: '请输入线路名称', trigger: 'blur' }],
  startOrganId: [
    { required: true, message: '请选择起始机构', trigger: 'change' },
    {
      trigger: 'change',
      validator: (_rule, value, callback) => {
        if (!value) return callback()
        const node = findNodeById(organTree.value, String(value))
        if (!node) return callback()
        const lt = lineForm.lineType
        const required = lt // 线路类型 1/2/3 对应机构 organType 1/2/3
        if (node.type != null && node.type !== required) {
          const typeNames: Record<number, string> = { 1: '一级转运中心', 2: '二级转运中心', 3: '营业部/网点' }
          callback(new Error(`${lineTypeLabel(lt)}起点须为${typeNames[required] ?? '对应类型机构'}`))
        } else {
          callback()
        }
      }
    }
  ],
  endOrganId: [{ required: true, message: '请选择目的地机构', trigger: 'change' }]
}

const mapDialogVisible = ref(false)
const mapLine = ref<LineRow | null>(null)

const tripDialogVisible = ref(false)
const tripDialogTitle = ref('')
const tripFormRef = ref<FormInstance>()
const tripSaving = ref(false)
const tripEditLine = ref<LineRow | null>(null)
const tripEditing = ref<DispatchTripDto | null>(null)
const tripForm = reactive({
  tripNumber: '',
  tripName: '',
  periodType: 1,
  departDay: '',
  departTime: '08:00:00',
  status: 1
})
const tripRules: FormRules = {
  tripNumber: [{ required: true, message: '请输入车次编号', trigger: 'blur' }],
  tripName: [{ required: true, message: '请输入车次名称', trigger: 'blur' }],
  periodType: [{ required: true, message: '请选择周期', trigger: 'change' }],
  departTime: [{ required: true, message: '请选择发车时间', trigger: 'change' }]
}

const arrangeDialogVisible = ref(false)
const arrangeTrip = ref<DispatchTripDto | null>(null)
const arrangeKeyword = ref('')
const eligibleList = ref<Vehicle[]>([])
const eligibleLoading = ref(false)
const arrangeAssigned = ref<TripVehicleRow[]>([])
const driverLabels = ref<Record<string, { ids: string[]; names: string[] }>>({})

function lineTypeLabel(t?: number) {
  const m: Record<number, string> = {
    1: '干线',
    2: '支线',
    3: '接驳路线'
  }
  return t != null ? m[t] ?? String(t) : '—'
}

function findNodeById(nodes: TreeNode[], id: string): TreeNode | undefined {
  for (const n of nodes) {
    if (String(n.id) === id) return n
    if (n.children) {
      const found = findNodeById(n.children, id)
      if (found) return found
    }
  }
  return undefined
}

function periodLabel(t?: number) {
  const m: Record<number, string> = { 1: '天', 2: '周', 3: '月', 4: '一次' }
  return t != null ? m[t] ?? String(t) : '—'
}

function formatDepart(t: DispatchTripDto) {
  const x = t.departTime as unknown
  if (x == null) return '—'
  if (typeof x === 'string') return x.slice(0, 8)
  if (typeof x === 'object' && x !== null && 'hour' in (x as object)) {
    const o = x as { hour?: number; minute?: number; second?: number }
    const h = String(o.hour ?? 0).padStart(2, '0')
    const m = String(o.minute ?? 0).padStart(2, '0')
    const s = String(o.second ?? 0).padStart(2, '0')
    return `${h}:${m}:${s}`
  }
  return String(x)
}

async function loadOrganTree() {
  try {
    const tree = (await organizationApi.getTree()) as unknown as TreeNode[]
    organTree.value = Array.isArray(tree) ? tree : []
  } catch {
    organTree.value = []
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await dispatchApi.getLineList({ ...searchForm, ...pagination })
    tableData.value = (res.list || []) as LineRow[]
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
  searchForm.status = undefined
  handleSearch()
}

async function onExpandChange(row: LineRow, expandedRows: LineRow[]) {
  const open = expandedRows.some((r) => String(r.id) === String(row.id))
  if (open && !row._tripsLoaded) {
    try {
      row.trips = (await dispatchApi.getLineTrips(row.id!)) as DispatchTripDto[]
      row._tripsLoaded = true
    } catch {
      row.trips = []
    }
  }
}

async function openLineDialog(edit: boolean, row?: LineRow) {
  await loadOrganTree()
  lineDialogTitle.value = edit ? '编辑线路' : '新增线路'
  if (edit && row?.id) {
    try {
      const d = await dispatchApi.getLineDetail(String(row.id))
      lineForm.id = d.id
      const num = d.lineNumber || ''
      lineForm.lineNumberSuffix = num.startsWith('XL') ? num.slice(2) : num
      lineForm.lineName = d.lineName || ''
      lineForm.lineType = d.lineType ?? 1
      lineForm.startOrganId = d.startOrganId != null ? String(d.startOrganId) : undefined
      lineForm.endOrganId = d.endOrganId != null ? String(d.endOrganId) : undefined
      lineForm.distance = d.distance != null ? Number(d.distance) : undefined
      lineForm.cost = d.cost != null ? Number(d.cost) : undefined
      lineForm.status = d.status ?? 1
    } catch {
      ElMessage.error('加载线路失败')
      return
    }
  } else {
    lineForm.id = undefined
    lineForm.lineNumberSuffix = ''
    lineForm.lineName = ''
    lineForm.lineType = 1
    lineForm.startOrganId = undefined
    lineForm.endOrganId = undefined
    lineForm.distance = undefined
    lineForm.cost = undefined
    lineForm.status = 1
  }
  lineDialogVisible.value = true
}

async function openCostDialog() {
  try {
    const g = await dispatchApi.getGlobalDispatchConfig()
    costForm.costPerKmType1 = g.costPerKmType1 != null ? Number(g.costPerKmType1) : undefined
    costForm.costPerKmType2 = g.costPerKmType2 != null ? Number(g.costPerKmType2) : undefined
    costForm.costPerKmType3 = g.costPerKmType3 != null ? Number(g.costPerKmType3) : undefined
    costDialogVisible.value = true
  } catch {
    ElMessage.error('加载全局成本配置失败')
  }
}

async function openDispatchDialog() {
  try {
    const g = await dispatchApi.getGlobalDispatchConfig()
    globalDispatchForm.latestDispatchHour = Math.max(1, Math.floor(Number(g.latestDispatchHour ?? 1)))
    globalDispatchForm.priorityFirst = (g.priorityFirst === 2 ? 2 : 1) as 1 | 2
    dispatchDialogVisible.value = true
  } catch {
    ElMessage.error('加载全局调度配置失败')
  }
}

async function submitCost() {
  costSaving.value = true
  try {
    const prev = await dispatchApi.getGlobalDispatchConfig()
    const payload: GlobalDispatchConfigDto = {
      ...prev,
      costPerKmType1: costForm.costPerKmType1,
      costPerKmType2: costForm.costPerKmType2,
      costPerKmType3: costForm.costPerKmType3
    }
    await dispatchApi.saveGlobalDispatchConfig(payload)
    ElMessage.success('保存成功')
    costDialogVisible.value = false
  } catch (e) {
    console.error(e)
  } finally {
    costSaving.value = false
  }
}

async function submitDispatch() {
  if (globalDispatchForm.latestDispatchHour < 1 || !Number.isInteger(globalDispatchForm.latestDispatchHour)) {
    ElMessage.warning('最晚任务下发时间须为不小于 1 的整数（小时）')
    return
  }
  dispatchSaving.value = true
  try {
    const prev = await dispatchApi.getGlobalDispatchConfig()
    const payload: GlobalDispatchConfigDto = {
      ...prev,
      latestDispatchHour: globalDispatchForm.latestDispatchHour,
      priorityFirst: globalDispatchForm.priorityFirst
    }
    await dispatchApi.saveGlobalDispatchConfig(payload)
    ElMessage.success('保存成功')
    dispatchDialogVisible.value = false
  } catch (e) {
    console.error(e)
  } finally {
    dispatchSaving.value = false
  }
}

function resetLineForm() {
  lineFormRef.value?.resetFields()
}

async function submitLine() {
  if (!lineFormRef.value) return
  await lineFormRef.value.validate(async (valid) => {
    if (!valid) return
    const suffix = (lineForm.lineNumberSuffix || '').trim()
    if (!suffix) {
      ElMessage.warning('请填写线路编号')
      return
    }
    lineSaving.value = true
    try {
      const basic: Partial<DispatchLineDto> = {
        lineNumber: `XL${suffix}`,
        lineName: lineForm.lineName,
        lineType: lineForm.lineType,
        startOrganId: lineForm.startOrganId,
        endOrganId: lineForm.endOrganId,
        status: lineForm.status
      }
      if (lineForm.id != null) {
        // 编辑时携带手动修改的距离和成本
        if (lineForm.distance != null) basic.distance = lineForm.distance
        if (lineForm.cost != null) basic.cost = lineForm.cost
        const prev = await dispatchApi.getLineDetail(String(lineForm.id))
        await dispatchApi.updateLine(String(lineForm.id), { ...prev, ...basic })
      } else {
        await dispatchApi.createLine(basic)
      }
      ElMessage.success('保存成功')
      lineDialogVisible.value = false
      loadData()
    } catch (e) {
      console.error(e)
    } finally {
      lineSaving.value = false
    }
  })
}

function openMapDialog(row: LineRow) {
  mapLine.value = row
  mapDialogVisible.value = true
}

async function handleDeleteLine(row: LineRow) {
  await ElMessageBox.confirm('确定删除该线路吗？若存在车次将无法删除。', '提示', { type: 'warning' })
  await dispatchApi.deleteLine(String(row.id))
  ElMessage.success('删除成功')
  loadData()
}

function openTripDialog(line: LineRow, trip: DispatchTripDto | null) {
  tripEditLine.value = line
  tripEditing.value = trip
  if (trip) {
    tripDialogTitle.value = '编辑车次'
    tripForm.tripNumber = trip.tripNumber || ''
    tripForm.tripName = trip.tripName || ''
    tripForm.periodType = trip.periodType ?? 1
    tripForm.departDay = trip.departDay || ''
    tripForm.departTime = formatDepart(trip) !== '—' ? formatDepart(trip) : '08:00:00'
    tripForm.status = trip.status ?? 1
  } else {
    tripDialogTitle.value = '添加车次'
    tripForm.tripNumber = ''
    tripForm.tripName = ''
    tripForm.periodType = 1
    tripForm.departDay = ''
    tripForm.departTime = '08:00:00'
    tripForm.status = 1
  }
  tripDialogVisible.value = true
}

async function submitTrip() {
  const lineRow = tripEditLine.value
  const parentLineId = lineRow?.id
  if (!tripFormRef.value || parentLineId == null) return
  await tripFormRef.value.validate(async (valid) => {
    if (!valid) return
    const body: Record<string, unknown> = {
      tripNumber: tripForm.tripNumber,
      tripName: tripForm.tripName,
      periodType: tripForm.periodType,
      departDay: tripForm.departDay || null,
      departTime: tripForm.departTime,
      status: tripForm.status
    }
    tripSaving.value = true
    try {
      if (tripEditing.value?.id) {
        await dispatchApi.updateTrip(tripEditing.value.id, body)
      } else {
        await dispatchApi.createTrip(parentLineId, body)
      }
      ElMessage.success('保存成功')
      tripDialogVisible.value = false
      const line = tableData.value.find((l) => l.id === parentLineId)
      if (line) {
        line._tripsLoaded = false
        line.trips = (await dispatchApi.getLineTrips(line.id!)) as DispatchTripDto[]
        line._tripsLoaded = true
      }
    } catch (e) {
      console.error(e)
    } finally {
      tripSaving.value = false
    }
  })
}

async function handleDeleteTrip(line: LineRow, trip: DispatchTripDto) {
  await ElMessageBox.confirm('确定删除该车次吗？若已安排车辆将无法删除。', '提示', { type: 'warning' })
  await dispatchApi.deleteTrip(trip.id!)
  ElMessage.success('已删除')
  line._tripsLoaded = false
  line.trips = (await dispatchApi.getLineTrips(line.id!)) as DispatchTripDto[]
  line._tripsLoaded = true
}

async function openArrangeDialog(trip: DispatchTripDto) {
  arrangeTrip.value = trip
  arrangeKeyword.value = ''
  eligibleList.value = []
  arrangeAssigned.value = []
  driverLabels.value = {}
  arrangeDialogVisible.value = true
  await refreshArrangeAssigned()
}

async function refreshArrangeAssigned() {
  if (!arrangeTrip.value?.id) return
  arrangeAssigned.value = await dispatchApi.getTripVehicles(arrangeTrip.value.id)
  await loadDriverLabelsForAssigned()
}

async function loadDriverLabelsForAssigned() {
  const map: Record<string, { ids: string[]; names: string[] }> = {}
  for (const r of arrangeAssigned.value) {
    const vid = String(r.vehicleId)
    try {
      const ids = (await vehicleApi.getDriverIds(vid)).map((x) => String(x))
      const names: string[] = []
      for (const id of ids.slice(0, 2)) {
        try {
          const d = await employeeApi.getDriverDetail(id)
          names.push(d.realName || id)
        } catch {
          names.push(id)
        }
      }
      map[vid] = { ids: ids.slice(0, 2), names }
    } catch {
      map[vid] = { ids: [], names: [] }
    }
  }
  driverLabels.value = map
}

function onArrangeClose() {
  arrangeTrip.value = null
  driverLabels.value = {}
}

async function searchEligible() {
  if (!arrangeTrip.value?.id) return
  eligibleLoading.value = true
  try {
    eligibleList.value = await dispatchApi.getEligibleTripVehicles(arrangeTrip.value.id, arrangeKeyword.value || undefined)
  } catch {
    eligibleList.value = []
  } finally {
    eligibleLoading.value = false
  }
}

async function addVehicleToTrip(v: Vehicle) {
  if (!arrangeTrip.value?.id || v.id == null) return
  if (arrangeAssigned.value.length >= 200) {
    ElMessage.warning('单车次最多 200 辆车')
    return
  }
  try {
    await dispatchApi.addTripVehicle(arrangeTrip.value.id, v.id)
    ElMessage.success('已添加')
    await refreshArrangeAssigned()
  } catch (e) {
    console.error(e)
  }
}

async function removeTripVehicleRow(row: TripVehicleRow) {
  if (!arrangeTrip.value?.id) return
  await ElMessageBox.confirm('从该车次移除该车辆？', '提示', { type: 'warning' })
  await dispatchApi.removeTripVehicle(arrangeTrip.value.id, row.vehicleId)
  ElMessage.success('已移除')
  await refreshArrangeAssigned()
}

function goVehicle(vehicleId: string) {
  router.push({ name: 'VehicleDetailPage', params: { id: vehicleId } })
}

function goDriver(id: string) {
  router.push({ name: 'DriverDetailPage', params: { id } })
}

onMounted(async () => {
  await loadOrganTree()
  loadData()
})
</script>

<style scoped>
.trip-panel {
  padding: 8px 8px 16px 40px;
  background: var(--el-fill-color-lighter);
}
.trip-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.trip-title {
  font-weight: 600;
}
.form-hint {
  margin-left: 8px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
.hint-block {
  line-height: 1.6;
}
.mb-2 {
  margin-bottom: 8px;
}
.ml-2 {
  margin-left: 8px;
}
.mt-2 {
  margin-top: 8px;
}
.section-h {
  margin: 12px 0 8px;
  font-size: 14px;
}
.map-pre {
  background: var(--el-fill-color-light);
  padding: 8px;
  border-radius: 4px;
  max-height: 240px;
  overflow: auto;
  font-size: 12px;
}
.map-poly-label {
  margin-top: 8px;
  font-weight: 600;
}
.driver-link {
  margin-right: 8px;
}
.mb-3 {
  margin-bottom: 12px;
}
</style>
