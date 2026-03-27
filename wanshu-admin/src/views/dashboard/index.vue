<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card class="org-card">
          <template #header>
            <div class="card-header">
              <span>机构概述</span>
            </div>
          </template>
          <div class="org-info" v-if="stats">
            <div class="info-item">
              <div class="label">机构名称</div>
              <div class="value">{{ stats.organizationInfo.name }}</div>
            </div>
            <div class="info-item">
              <div class="label">机构类型</div>
              <div class="value">{{ getOrganizationTypeLabel(stats.organizationInfo.type) }}</div>
            </div>
            <div class="info-item">
              <div class="label">下属机构</div>
              <div class="value">{{ stats.organizationInfo.subOrganizationCount }}</div>
            </div>
            <div class="info-item">
              <div class="label">员工数量</div>
              <div class="value">{{ stats.organizationInfo.employeeCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #1890ff">
              <el-icon><Money /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">今日订单金额</div>
              <div class="stat-value">¥{{ stats?.todayData.orderAmount || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #52c41a">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">今日订单数量</div>
              <div class="stat-value">{{ stats?.todayData.orderCount || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #faad14">
              <el-icon><Van /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">运输任务</div>
              <div class="stat-value">{{ stats?.todayData.transportTaskCount || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>取件任务</span>
            </div>
          </template>
          <div class="task-stats" v-if="stats">
            <div class="task-item">
              <span>待分配</span>
              <el-tag type="warning">{{ stats.pickupTasks.unassigned }}</el-tag>
            </div>
            <div class="task-item">
              <span>已分配未取件</span>
              <el-tag type="info">{{ stats.pickupTasks.assigned }}</el-tag>
            </div>
            <div class="task-item">
              <span>已完成</span>
              <el-tag type="success">{{ stats.pickupTasks.completed }}</el-tag>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>运输任务</span>
            </div>
          </template>
          <div class="task-stats" v-if="stats">
            <div class="task-item">
              <span>待提货</span>
              <el-tag type="warning">{{ stats.transportTasks.pending }}</el-tag>
            </div>
            <div class="task-item">
              <span>在途</span>
              <el-tag type="info">{{ stats.transportTasks.inProgress }}</el-tag>
            </div>
            <div class="task-item">
              <span>已完成</span>
              <el-tag type="success">{{ stats.transportTasks.completed }}</el-tag>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>派件任务</span>
            </div>
          </template>
          <div class="task-stats" v-if="stats">
            <div class="task-item">
              <span>待分配</span>
              <el-tag type="warning">{{ stats.deliveryTasks.unassigned }}</el-tag>
            </div>
            <div class="task-item">
              <span>待派件</span>
              <el-tag type="info">{{ stats.deliveryTasks.assigned }}</el-tag>
            </div>
            <div class="task-item">
              <span>已完成</span>
              <el-tag type="success">{{ stats.deliveryTasks.completed }}</el-tag>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="mt-20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>近七日订单趋势</span>
            </div>
          </template>
          <div ref="chartRef" style="height: 300px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>全国订单分布</span>
            </div>
          </template>
          <div ref="mapRef" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { systemApi } from '@/api/system'
import { getOrganizationTypeLabel } from '@/utils'
import type { DashboardStats } from '@/types'

const stats = ref<DashboardStats>()
const chartRef = ref<HTMLElement>()
const mapRef = ref<HTMLElement>()
let chartInstance: echarts.ECharts | null = null
let mapInstance: echarts.ECharts | null = null

const loadStats = async () => {
  try {
    stats.value = await systemApi.getDashboardStats()
    initCharts()
  } catch (error) {
    console.error('Load stats failed:', error)
  }
}

const initCharts = () => {
  if (!stats.value) return
  
  if (chartRef.value) {
    chartInstance = echarts.init(chartRef.value)
    chartInstance.setOption({
      tooltip: {
        trigger: 'axis'
      },
      xAxis: {
        type: 'category',
        data: stats.value.recentOrders.dates
      },
      yAxis: {
        type: 'value'
      },
      series: [{
        data: stats.value.recentOrders.counts,
        type: 'bar',
        itemStyle: {
          color: '#1890ff'
        }
      }]
    })
  }
  
  if (mapRef.value) {
    mapInstance = echarts.init(mapRef.value)
    mapInstance.setOption({
      tooltip: {
        trigger: 'item'
      },
      visualMap: {
        min: 0,
        max: Math.max(...stats.value.orderDistribution.map(d => d.count)),
        text: ['高', '低'],
        realtime: false,
        calculable: true,
        inRange: {
          color: ['#e0f3f8', '#abd9e9', '#74add1', '#4575b4', '#313695']
        }
      },
      series: [{
        name: '订单数量',
        type: 'map',
        map: 'china',
        roam: true,
        data: stats.value.orderDistribution.map(d => ({
          name: d.province,
          value: d.count
        }))
      }]
    })
  }
}

onMounted(() => {
  loadStats()
  
  window.addEventListener('resize', () => {
    chartInstance?.resize()
    mapInstance?.resize()
  })
})

onUnmounted(() => {
  chartInstance?.dispose()
  mapInstance?.dispose()
})
</script>

<style scoped lang="scss">
.dashboard {
  .mt-20 {
    margin-top: 20px;
  }
  
  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-weight: bold;
  }
  
  .org-card {
    .org-info {
      display: flex;
      gap: 40px;
      
      .info-item {
        flex: 1;
        
        .label {
          font-size: 14px;
          color: #999;
          margin-bottom: 8px;
        }
        
        .value {
          font-size: 20px;
          font-weight: bold;
          color: #333;
        }
      }
    }
  }
  
  .stat-card {
    .stat-content {
      display: flex;
      align-items: center;
      gap: 20px;
      
      .stat-icon {
        width: 60px;
        height: 60px;
        border-radius: 10px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
        font-size: 28px;
      }
      
      .stat-info {
        flex: 1;
        
        .stat-label {
          font-size: 14px;
          color: #999;
          margin-bottom: 8px;
        }
        
        .stat-value {
          font-size: 24px;
          font-weight: bold;
          color: #333;
        }
      }
    }
  }
  
  .task-stats {
    .task-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 12px 0;
      border-bottom: 1px solid #f0f0f0;
      
      &:last-child {
        border-bottom: none;
      }
      
      span {
        font-size: 14px;
        color: #666;
      }
    }
  }
}
</style>
