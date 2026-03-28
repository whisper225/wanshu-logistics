<template>
  <view class="page">
    <text class="h1">查快递</text>
    <text class="p">运单列表与轨迹（可对接 /api/biz/waybill 与 Mongo 轨迹）。</text>
    <button @click="back">返回</button>
  </view>
</template>
<script setup lang="ts">
function back() {
  uni.navigateBack({ fail: () => uni.reLaunch({ url: '/pages/login/index' }) })
}
</script>
<style scoped>
.page { padding: 32rpx; }
.h1 { font-size: 36rpx; font-weight: bold; display: block; margin-bottom: 24rpx; }
.p { color: #666; margin-bottom: 32rpx; display: block; }
</style>
