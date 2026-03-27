<template>
  <el-card class="page-container">
    <template #header v-if="$slots.header || title">
      <div class="page-header">
        <slot name="header">
          <span class="page-title">{{ title }}</span>
        </slot>
      </div>
    </template>
    <slot></slot>
  </el-card>
</template>

<script setup lang="ts">
defineProps<{
  title?: string
}>()
</script>

<style scoped lang="scss">
.page-container {
  .page-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    
    .page-title {
      font-size: 16px;
      font-weight: bold;
    }
  }
}
</style>
