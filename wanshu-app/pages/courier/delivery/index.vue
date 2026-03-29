<template>
  <view class="page">
    <text class="h1">派件</text>
    <text class="p">对接 /api/biz/task/delivery。</text>
    <button @click="() => uni.navigateBack({ fail: () => uni.reLaunch({ url: '/pages/login/index' }) })">返回</button>
  </view>
</template>
<script setup lang="ts">
// 占位页
</script>
<style scoped>.page{padding:32rpx}.h1{font-size:36rpx;font-weight:bold;margin-bottom:24rpx}.p{color:#666;margin-bottom:24rpx}</style>
