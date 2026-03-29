<template>
  <PageContainer title="司机详情">
    <el-card v-loading="loading" shadow="never">
      <template v-if="detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户ID">{{ detail.id ?? '—' }}</el-descriptions-item>
          <el-descriptions-item label="登录账号">{{ detail.username || '—' }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ detail.realName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="手机">{{ detail.phone || '—' }}</el-descriptions-item>
          <el-descriptions-item label="所属机构">{{ detail.organName || detail.organId || '—' }}</el-descriptions-item>
          <el-descriptions-item label="擅长车型">{{ detail.vehicleTypes || '—' }}</el-descriptions-item>
          <el-descriptions-item label="工作状态">
            <el-tag :type="detail.workStatus === 1 ? 'success' : 'info'" size="small">
              {{ detail.workStatus === 1 ? '上班' : '休息' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="驾驶证">
            <el-link v-if="detail.licenseImage" type="primary" :href="detail.licenseImage" target="_blank">查看</el-link>
            <span v-else>—</span>
          </el-descriptions-item>
        </el-descriptions>
      </template>
      <el-empty v-else-if="!loading" description="未找到司机" />
    </el-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import PageContainer from '@/components/PageContainer.vue'
import { employeeApi } from '@/api/employee'
import type { EmpDriverVO } from '@/api/employee'

const route = useRoute()
const loading = ref(false)
const detail = ref<EmpDriverVO | null>(null)

async function load() {
  const id = route.params.id as string
  if (!id) return
  loading.value = true
  try {
    detail.value = await employeeApi.getDriverDetail(id)
  } catch {
    detail.value = null
  } finally {
    loading.value = false
  }
}

onMounted(() => load())
watch(() => route.params.id, () => load())
</script>
