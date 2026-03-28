<template>
  <view class="page">
    <text class="h1">个人中心</text>
    <button type="warn" @click="onLogout">退出登录</button>
    <button @click="back">返回</button>
  </view>
</template>
<script setup lang="ts">
import { logout } from '@/api/auth'

async function onLogout() {
  try {
    await logout()
  } catch (_) {}
  uni.removeStorageSync('token')
  uni.reLaunch({ url: '/pages/login/index' })
}
function back() {
  uni.navigateBack({ fail: () => uni.reLaunch({ url: '/pages/login/index' }) })
}
</script>
<style scoped>
.page { padding: 32rpx; }
.h1 { font-size: 36rpx; font-weight: bold; display: block; margin-bottom: 32rpx; }
button { margin-bottom: 24rpx; }
</style>
