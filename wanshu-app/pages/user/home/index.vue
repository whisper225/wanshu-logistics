<template>
  <view class="page-wrap">
    <!-- 顶部品牌区：深色底 + 文案 + 右侧示意 -->
    <view class="hero">
      <view class="hero-pattern" />
      <view class="hero-inner">
        <view class="hero-text">
          <text class="hero-title">寄快递 用万枢</text>
          <text class="hero-sub">高效可靠 · 值得信赖</text>
        </view>
        <view class="hero-illus" aria-hidden="true">
          <text class="hero-illus-icon">📦</text>
        </view>
      </view>
    </view>

    <!-- 主操作卡片（压住 hero 底部） -->
    <view class="action-card-wrap">
      <view class="action-card" @click="goSendForm">
        <view class="action-icon-wrap">
          <text class="action-icon">📮</text>
        </view>
        <view class="action-text">
          <text class="action-title">寄快递</text>
          <text class="action-desc">1小时上门取件</text>
        </view>
        <text class="action-arrow">›</text>
      </view>
    </view>

    <!-- 最近运单 / 空状态 -->
    <view class="section">
      <view v-if="loading" class="section-loading">
        <text>加载中...</text>
      </view>
      <view v-else-if="previewList.length === 0" class="empty">
        <view class="empty-illus">
          <view class="empty-clipboard" />
          <view class="empty-clip" />
        </view>
        <text class="empty-text">没有运单~</text>
      </view>
      <view v-else class="preview-list">
        <view
          v-for="item in previewList"
          :key="item.id"
          class="preview-item"
          @click="goTrackWithItem(item)"
        >
          <view class="preview-row">
            <text class="preview-no">{{ item.number }}</text>
            <text class="preview-status">{{ item.statusText }}</text>
          </view>
          <text class="preview-goods">{{ item.goodsName || '物品' }}</text>
        </view>
      </view>
    </view>

    <UserTabBar current="home" />
  </view>
</template>

<script lang="ts">
import { defineComponent } from '@vue/runtime-core'
import UserTabBar from '@/components/user/UserTabBar.vue'
import { listOrders } from '@/api/order'
import type { OrderItem } from '@/api/order'

export default defineComponent({
  name: 'UserHomePage',
  components: { UserTabBar },
  data() {
    return {
      loading: false,
      previewList: [] as OrderItem[],
    }
  },
  onShow() {
    this.loadPreview()
  },
  methods: {
    async loadPreview() {
      this.loading = true
      try {
        const res = await listOrders({ type: 0, pageNum: 1, pageSize: 5 })
        this.previewList = res.list || []
      } catch (_) {
        this.previewList = []
      } finally {
        this.loading = false
      }
    },
    goSendForm() {
      uni.navigateTo({ url: '/pages/user/send/index' })
    },
    goTrackWithItem(_item: OrderItem) {
      uni.reLaunch({ url: '/pages/user/track/index' })
    },
  },
})
</script>

<style scoped lang="scss">
$brand: #e53935;
$hero-bg: #1e2d4a;

.page-wrap {
  min-height: 100vh;
  background: #f5f6f8;
  padding-bottom: calc(120rpx + env(safe-area-inset-bottom));
}

.hero {
  position: relative;
  background: $hero-bg;
  padding: 48rpx 32rpx 100rpx;
  overflow: hidden;
}

.hero-pattern {
  position: absolute;
  inset: 0;
  opacity: 0.12;
  background: repeating-linear-gradient(
    -45deg,
    transparent,
    transparent 20rpx,
    rgba(255, 255, 255, 0.06) 20rpx,
    rgba(255, 255, 255, 0.06) 40rpx
  );
}

.hero-inner {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24rpx;
}

.hero-text {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.hero-title {
  font-size: 44rpx;
  font-weight: 800;
  color: #fff;
  letter-spacing: 2rpx;
  line-height: 1.3;
}

.hero-sub {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.85);
  letter-spacing: 2rpx;
}

.hero-illus {
  width: 160rpx;
  height: 160rpx;
  border-radius: 24rpx;
  background: rgba(255, 255, 255, 0.12);
  display: flex;
  align-items: center;
  justify-content: center;
}

.hero-illus-icon {
  font-size: 88rpx;
  line-height: 1;
}

.action-card-wrap {
  margin: -72rpx 24rpx 0;
  position: relative;
  z-index: 2;
}

.action-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 28rpx 28rpx;
  display: flex;
  align-items: center;
  gap: 24rpx;
  box-shadow: 0 8rpx 32rpx rgba(30, 45, 74, 0.12);
}

.action-icon-wrap {
  width: 88rpx;
  height: 88rpx;
  border-radius: 16rpx;
  background: linear-gradient(135deg, $brand 0%, #c62828 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-icon {
  font-size: 44rpx;
  line-height: 1;
}

.action-text {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8rpx;
}

.action-title {
  font-size: 34rpx;
  font-weight: 700;
  color: #1a1a1a;
}

.action-desc {
  font-size: 24rpx;
  color: #999;
}

.action-arrow {
  font-size: 40rpx;
  color: #ccc;
  font-weight: 300;
}

.section {
  margin-top: 32rpx;
  min-height: 280rpx;
}

.section-loading {
  text-align: center;
  padding: 80rpx;
  color: #bbb;
  font-size: 28rpx;
}

.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 48rpx 32rpx 80rpx;
}

.empty-illus {
  position: relative;
  width: 200rpx;
  height: 200rpx;
  margin-bottom: 24rpx;
}

.empty-clipboard {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  width: 120rpx;
  height: 140rpx;
  border-radius: 12rpx;
  background: #e8e8e8;
  border: 4rpx solid #d0d0d0;
}

.empty-clip {
  position: absolute;
  left: 50%;
  top: 28%;
  transform: translateX(-50%);
  width: 48rpx;
  height: 16rpx;
  border-radius: 4rpx 4rpx 0 0;
  background: $brand;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}

.preview-list {
  padding: 0 24rpx 24rpx;
}

.preview-item {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 16rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
}

.preview-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12rpx;
}

.preview-no {
  font-size: 26rpx;
  color: #333;
  font-family: monospace;
}

.preview-status {
  font-size: 24rpx;
  color: $brand;
}

.preview-goods {
  font-size: 24rpx;
  color: #888;
}
</style>
