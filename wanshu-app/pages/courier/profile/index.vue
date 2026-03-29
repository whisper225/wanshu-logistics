<template>
  <view class="page">
    <text class="h1">个人中心</text>
    <button type="warn" @click="onLogout">退出</button>
  </view>
</template>

<script lang="ts">
import { defineComponent } from '@vue/runtime-core'
import { logout } from '@/api/auth'

export default defineComponent({
  name: 'CourierProfile',
  methods: {
    async onLogout() {
      try {
        await logout()
      } catch (_) {
        /* ignore */
      }
      uni.removeStorageSync('token')
      uni.reLaunch({ url: '/pages/login/index' })
    },
  },
})
</script>

<style scoped>
.page {
  padding: 32rpx;
}
.h1 {
  font-size: 36rpx;
  font-weight: bold;
  margin-bottom: 24rpx;
}
</style>
