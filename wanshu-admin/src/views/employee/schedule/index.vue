<template>
  <PageContainer title="排班管理">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card>
          <template #header>筛选条件</template>
          <el-form label-width="80px">
            <el-form-item label="员工类型">
              <el-radio-group v-model="employeeType" @change="loadData">
                <el-radio value="courier">快递员</el-radio>
                <el-radio value="driver">司机</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="日期">
              <el-date-picker v-model="selectedDate" type="date" placeholder="选择日期"
                value-format="YYYY-MM-DD" @change="loadData" style="width: 100%" />
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
      <el-col :span="18">
        <el-card>
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center">
              <span>{{ employeeType === 'courier' ? '快递员' : '司机' }}排班列表</span>
              <el-button type="primary" size="small" @click="handleBatchSchedule">批量排班</el-button>
            </div>
          </template>
          <el-table :data="tableData" v-loading="loading">
            <el-table-column prop="id" label="员工ID" width="180" />
            <el-table-column prop="organId" label="所属机构" width="180" />
            <el-table-column prop="workStatus" label="当前状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.workStatus === 1 ? 'success' : 'info'">
                  {{ row.workStatus === 1 ? '上班' : '休息' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button link type="primary" @click="handleToggle(row)">
                  {{ row.workStatus === 1 ? '排休' : '排班' }}
                </el-button>
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
        </el-card>
      </el-col>
    </el-row>
  </PageContainer>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { employeeApi } from '@/api/employee'
import PageContainer from '@/components/PageContainer.vue'

const loading = ref(false)
const tableData = ref<any[]>([])
const employeeType = ref('courier')
const selectedDate = ref('')
const pagination = reactive({ pageNum: 1, pageSize: 20, total: 0 })

const loadData = async () => {
  loading.value = true
  try {
    const params: any = { ...pagination }
    let res
    if (employeeType.value === 'courier') {
      res = await employeeApi.getCourierList(params)
    } else {
      res = await employeeApi.getDriverList(params)
    }
    tableData.value = res.list
    pagination.total = res.total
  } catch (e) { console.error(e) } finally { loading.value = false }
}

const handleToggle = async (row: any) => {
  const newStatus = row.workStatus === 1 ? 0 : 1
  try {
    if (employeeType.value === 'courier') {
      await employeeApi.updateCourier(row.id, { workStatus: newStatus })
    } else {
      await employeeApi.updateDriver(row.id, { workStatus: newStatus })
    }
    ElMessage.success('操作成功')
    loadData()
  } catch (e) { console.error(e) }
}

const handleBatchSchedule = () => {
  ElMessage.info('批量排班功能开发中')
}

onMounted(() => loadData())
</script>
