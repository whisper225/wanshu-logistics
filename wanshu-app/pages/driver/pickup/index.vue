<template>
  <view class="page">
    <text class="h1">提货</text>
    <text class="p">对接 dispatch 运输任务。</text>
    <button @click="back">返回登录</button>
  </view>
</template>
<script setup lang="ts">
function back() { uni.reLaunch({ url: '/pages/login/index' }) }
</script>
<style scoped>.page{padding:32rpx}.h1{font-size:36rpx;font-weight:bold;margin-bottom:24rpx}.p{color:#666;margin-bottom:24rpx}</style>
