<template>
  <view class="page">
    <text class="h1">个人中心</text>
    <button type="warn" @click="out">退出</button>
  </view>
</template>
<script setup lang="ts">
import { logout } from '@/api/auth'
async function out() {
  try { await logout() } catch (_) {}
  uni.removeStorageSync('token')
  uni.reLaunch({ url: '/pages/login/index' })
}
</script>
<style scoped>.page{padding:32rpx}.h1{font-size:36rpx;font-weight:bold;margin-bottom:24rpx}</style>
