<template>
  <el-form :model="modelValue" :inline="true" class="search-form">
    <slot></slot>
    <el-form-item>
      <el-button type="primary" @click="$emit('search')">搜索</el-button>
      <el-button @click="$emit('reset')">重置</el-button>
      <slot name="actions"></slot>
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
defineProps<{
  modelValue: any
}>()

defineEmits<{
  search: []
  reset: []
}>()
</script>

<style scoped lang="scss">
.search-form {
  margin-bottom: 20px;
}
</style>
