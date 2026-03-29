<template>
  <view class="tab-bar">
    <view
      v-for="tab in tabs"
      :key="tab.path"
      :class="['tab-item', current === tab.key && 'active']"
      @click="switchTab(tab)"
    >
      <text class="tab-icon">{{ tab.icon }}</text>
      <text class="tab-label">{{ tab.label }}</text>
    </view>
  </view>
</template>

<script lang="ts">
import { defineComponent } from '@vue/runtime-core'

const TABS = [
  { key: 'send',    label: '寄快递', icon: '📦', path: '/pages/user/send/index' },
  { key: 'track',   label: '查快递', icon: '🔍', path: '/pages/user/track/index' },
  { key: 'profile', label: '我的',   icon: '👤', path: '/pages/user/profile/index' },
]

export default defineComponent({
  name: 'UserTabBar',
  props: {
    current: {
      type: String as () => 'send' | 'track' | 'profile',
      required: true,
    },
  },
  data() {
    return { tabs: TABS }
  },
  methods: {
    switchTab(tab: typeof TABS[0]) {
      if (tab.key === this.current) return
      uni.reLaunch({ url: tab.path })
    },
  },
})
</script>

<style scoped lang="scss">
.tab-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: calc(100rpx + env(safe-area-inset-bottom));
  padding-bottom: env(safe-area-inset-bottom);
  background: #fff;
  display: flex;
  border-top: 1rpx solid #f0f0f0;
  box-shadow: 0 -2rpx 12rpx rgba(0, 0, 0, 0.06);
  z-index: 99;
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6rpx;
  padding-top: 12rpx;

  .tab-icon {
    font-size: 40rpx;
    line-height: 1;
    filter: grayscale(1) opacity(0.5);
  }

  .tab-label {
    font-size: 22rpx;
    color: #999;
  }

  &.active {
    .tab-icon { filter: none; }
    .tab-label { color: #1890ff; font-weight: 600; }
  }
}
</style>
