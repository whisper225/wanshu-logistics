<template>
  <view class="page">
    <text class="h1">寄快递</text>
    <text class="p">填写寄件/收件信息与物品信息（对接 biz 下单接口待接）。</text>
    <button type="primary" @click="back">返回登录</button>
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
