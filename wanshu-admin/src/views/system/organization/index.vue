<template>
  <PageContainer title="组织管理">
    <el-alert type="info" show-icon :closable="false" style="margin-bottom: 20px">
      组织管理功能请前往「基础数据管理 > 机构管理」进行操作。
    </el-alert>
    <el-button type="primary" @click="$router.push('/basic/organization')">前往机构管理</el-button>
  </PageContainer>
</template>

<script setup lang="ts">
import PageContainer from '@/components/PageContainer.vue'
</script>
