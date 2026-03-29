<template>
  <PageContainer title="车辆详情">
    <el-card v-loading="loading" shadow="never">
      <template v-if="detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="车辆编号">{{ detail.vehicleNumber || '—' }}</el-descriptions-item>
          <el-descriptions-item label="车牌号">{{ detail.licensePlate || '—' }}</el-descriptions-item>
          <el-descriptions-item label="车型ID">{{ detail.vehicleTypeId ?? '—' }}</el-descriptions-item>
          <el-descriptions-item label="所属机构">{{ detail.organId ?? '—' }}</el-descriptions-item>
          <el-descriptions-item label="载重(吨)">{{ detail.loadWeight ?? '—' }}</el-descriptions-item>
          <el-descriptions-item label="载积(m³)">{{ detail.loadVolume ?? '—' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="detail.status === 1 ? 'success' : 'info'" size="small">
              {{ detail.status === 1 ? '可用' : '停用' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </template>
      <el-empty v-else-if="!loading" description="未找到车辆" />
    </el-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import PageContainer from '@/components/PageContainer.vue'
import { vehicleApi } from '@/api/vehicle'
import type { VehicleDetail } from '@/types'

const route = useRoute()
const loading = ref(false)
const detail = ref<VehicleDetail | null>(null)

async function load() {
  const id = route.params.id as string
  if (!id) return
  loading.value = true
  try {
    detail.value = await vehicleApi.getDetail(id)
  } catch {
    detail.value = null
  } finally {
    loading.value = false
  }
}

onMounted(() => load())
watch(() => route.params.id, () => load())
</script>
