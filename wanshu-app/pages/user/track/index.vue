<template>
  <view class="page-wrap">
  <view class="page">
    <!-- 搜索栏 -->
    <view class="search-bar">
      <view class="search-input-wrap">
        <text class="search-icon">🔍</text>
        <input
          class="search-input"
          v-model="keyword"
          placeholder="搜索运/订单号、姓名、电话"
          @confirm="onSearch"
          @input="onSearchInput"
        />
        <text v-if="keyword" class="search-clear" @click="clearSearch">✕</text>
      </view>
    </view>

    <!-- Tab 切换 -->
    <view class="tab-bar">
      <view
        v-for="tab in tabs"
        :key="tab.type"
        :class="['tab-item', activeType === tab.type && 'active']"
        @click="switchTab(tab.type)"
      >
        <text>{{ tab.label }}</text>
        <view v-if="activeType === tab.type" class="tab-line" />
      </view>
    </view>

    <!-- 加载中 -->
    <view v-if="loading" class="center-tip">
      <text>加载中...</text>
    </view>

    <!-- 空状态 -->
    <view v-else-if="list.length === 0" class="empty">
      <text class="empty-icon">📦</text>
      <text class="empty-text">暂无{{ activeType === 0 ? '寄件' : '收件' }}记录</text>
      <button v-if="activeType === 0" class="empty-btn" @click="goSend">立即寄快递</button>
    </view>

    <!-- 订单列表 -->
    <view v-else class="list">
      <view
        v-for="item in list"
        :key="item.id"
        class="order-card"
        @click="goDetail(item)"
      >
        <view class="order-header">
          <view class="order-number-wrap">
            <text class="order-number">{{ item.number }}</text>
            <text class="copy-btn" @click.stop="copyNumber(item.number)">复制</text>
          </view>
          <text :class="['status-badge', getStatusClass(item.status)]">{{ item.statusText }}</text>
        </view>

        <view class="order-route">
          <view class="route-person">
            <text class="person-name">{{ item.senderName }}</text>
            <text class="person-phone">{{ maskPhone(item.senderPhone) }}</text>
          </view>
          <view class="route-arrow">
            <text class="arrow-line">————</text>
            <text class="arrow-head">▶</text>
          </view>
          <view class="route-person">
            <text class="person-name">{{ item.receiverName }}</text>
            <text class="person-phone">{{ maskPhone(item.receiverPhone) }}</text>
          </view>
        </view>

        <view class="order-goods">
          <text class="goods-name">{{ item.goodsName || '未填写物品名称' }}</text>
          <text v-if="item.estimatedFee" class="goods-fee">¥{{ item.estimatedFee }}</text>
        </view>

        <view class="order-footer">
          <text class="order-time">{{ formatTime(item.createdTime) }}</text>
          <view class="action-btns">
            <button
              v-if="item.status === 0"
              class="action-btn warn"
              size="mini"
              @click.stop="onCancel(item)"
            >取消寄件</button>
            <button
              v-if="[5, 6, 7].includes(item.status)"
              class="action-btn"
              size="mini"
              @click.stop="onDelete(item)"
            >删除</button>
          </view>
        </view>
      </view>
    </view>

    <!-- 订单详情弹窗 -->
    <view v-if="showDetail && currentItem" class="overlay" @click.self="showDetail = false">
      <view class="detail-panel">
        <view class="picker-header">
          <text class="picker-title">订单详情</text>
          <text class="picker-close" @click="showDetail = false">✕</text>
        </view>
        <view class="detail-body">
          <view class="detail-section">
            <view class="detail-row">
              <text class="detail-label">订单号</text>
              <view class="detail-value-row">
                <text class="detail-value mono">{{ currentItem.number }}</text>
                <text class="copy-link" @click="copyNumber(currentItem.number)">复制</text>
              </view>
            </view>
            <view class="detail-row">
              <text class="detail-label">状态</text>
              <text :class="['detail-value', 'status-text', getStatusClass(currentItem.status)]">
                {{ currentItem.statusText }}
              </text>
            </view>
          </view>

          <view class="detail-section" v-if="orderDetail">
            <view class="section-title">寄件信息</view>
            <view class="detail-row">
              <text class="detail-label">姓名</text>
              <text class="detail-value">{{ orderDetail.senderName }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">电话</text>
              <text class="detail-value">{{ orderDetail.senderPhone }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">地址</text>
              <text class="detail-value">{{ orderDetail.senderAddress }}</text>
            </view>

            <view class="section-title">收件信息</view>
            <view class="detail-row">
              <text class="detail-label">姓名</text>
              <text class="detail-value">{{ orderDetail.receiverName }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">电话</text>
              <text class="detail-value">{{ orderDetail.receiverPhone }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">地址</text>
              <text class="detail-value">{{ orderDetail.receiverAddress }}</text>
            </view>

            <view class="section-title">物品信息</view>
            <view class="detail-row">
              <text class="detail-label">物品名称</text>
              <text class="detail-value">{{ orderDetail.goodsName }}</text>
            </view>
            <view class="detail-row" v-if="orderDetail.weight">
              <text class="detail-label">重量</text>
              <text class="detail-value">{{ orderDetail.weight }} kg</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">付款方式</text>
              <text class="detail-value">{{ orderDetail.paymentMethod === 1 ? '寄付' : '到付' }}</text>
            </view>
            <view class="detail-row" v-if="orderDetail.estimatedFee">
              <text class="detail-label">估算运费</text>
              <text class="detail-value fee">¥{{ orderDetail.estimatedFee }}</text>
            </view>
            <view class="detail-row" v-if="orderDetail.pickupTime">
              <text class="detail-label">取件时间</text>
              <text class="detail-value">{{ orderDetail.pickupTime }}</text>
            </view>
            <view class="detail-row" v-if="orderDetail.cancelReason">
              <text class="detail-label">取消原因</text>
              <text class="detail-value">{{ orderDetail.cancelReason }}</text>
            </view>
          </view>

          <!-- 运单（电子存根）信息 -->
          <view class="detail-section" v-if="waybillInfo">
            <view class="section-title">运单信息（电子存根）</view>
            <view class="detail-row">
              <text class="detail-label">运单号</text>
              <view class="detail-value-row">
                <text class="detail-value mono">{{ waybillInfo.waybillNumber }}</text>
                <text class="copy-link" @click="copyNumber(waybillInfo.waybillNumber)">复制</text>
              </view>
            </view>
            <view class="detail-row" v-if="waybillInfo.freight">
              <text class="detail-label">实际运费</text>
              <text class="detail-value fee">¥{{ waybillInfo.freight }}</text>
            </view>
            <view class="detail-row" v-if="waybillInfo.signTime">
              <text class="detail-label">签收时间</text>
              <text class="detail-value">{{ waybillInfo.signTime }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <UserTabBar current="track" />

    <!-- 取消原因弹窗 -->
    <view v-if="showCancelModal" class="overlay" @click.self="showCancelModal = false">
      <view class="cancel-modal">
        <text class="cancel-title">取消寄件</text>
        <textarea
          class="cancel-input"
          v-model="cancelReason"
          placeholder="请输入取消原因（可选）"
          maxlength="200"
        />
        <view class="cancel-btns">
          <button class="cancel-btn-no" @click="showCancelModal = false">取消</button>
          <button class="cancel-btn-yes" @click="confirmCancel" :loading="cancelling">确认取消</button>
        </view>
      </view>
    </view>
  </view>
  </view>
</template>

<script lang="ts">
import { defineComponent } from '@vue/runtime-core'
import UserTabBar from '@/components/user/UserTabBar.vue'
import { listOrders, getOrderDetail, getOrderWaybill, cancelOrder, deleteOrder } from '@/api/order'
import type { OrderItem, OrderDetail, WaybillDetail } from '@/api/order'

export default defineComponent({
  name: 'TrackPage',
  components: { UserTabBar },
  data() {
    return {
      keyword: '',
      activeType: 0,
      loading: false,
      list: [] as OrderItem[],
      showDetail: false,
      currentItem: null as OrderItem | null,
      orderDetail: null as OrderDetail | null,
      waybillInfo: null as WaybillDetail | null,
      showCancelModal: false,
      cancelTarget: null as OrderItem | null,
      cancelReason: '',
      cancelling: false,
      searchTimer: null as ReturnType<typeof setTimeout> | null,
      tabs: [
        { label: '寄件', type: 0 },
        { label: '收件', type: 1 },
      ],
    }
  },
  mounted() {
    this.switchTab(0)
  },
  methods: {
    async loadList() {
      this.loading = true
      try {
        const res = await listOrders({
          keyword: this.keyword || undefined,
          type: this.activeType,
        })
        this.list = res.list
      } catch (e: unknown) {
        const err = e as { message?: string }
        uni.showToast({ title: err.message || '加载失败', icon: 'none' })
      } finally {
        this.loading = false
      }
    },
    switchTab(type: number) {
      this.activeType = type
      this.list = []
      this.loadList()
    },
    onSearchInput() {
      if (this.searchTimer) clearTimeout(this.searchTimer)
      this.searchTimer = setTimeout(() => this.loadList(), 500)
    },
    onSearch() {
      this.loadList()
    },
    clearSearch() {
      this.keyword = ''
      this.loadList()
    },
    async goDetail(item: OrderItem) {
      this.currentItem = item
      this.orderDetail = null
      this.waybillInfo = null
      this.showDetail = true
      try {
        this.orderDetail = await getOrderDetail(item.id)
        if ([2, 3, 4, 5, 6].includes(item.status)) {
          this.waybillInfo = await getOrderWaybill(item.id)
        }
      } catch (_) {}
    },
    copyNumber(num: string) {
      uni.setClipboardData({ data: num })
    },
    onCancel(item: OrderItem) {
      this.cancelTarget = item
      this.cancelReason = ''
      this.showCancelModal = true
    },
    async confirmCancel() {
      if (!this.cancelTarget) return
      this.cancelling = true
      try {
        await cancelOrder(this.cancelTarget.id, this.cancelReason || undefined)
        uni.showToast({ title: '取消成功', icon: 'success' })
        this.showCancelModal = false
        this.loadList()
      } catch (e: unknown) {
        const err = e as { message?: string }
        uni.showToast({ title: err.message || '取消失败', icon: 'none' })
      } finally {
        this.cancelling = false
      }
    },
    async onDelete(item: OrderItem) {
      uni.showModal({
        title: '确认删除',
        content: '删除后不可恢复，是否继续？',
        success: async ({ confirm }: { confirm: boolean }) => {
          if (!confirm) return
          try {
            await deleteOrder(item.id)
            uni.showToast({ title: '已删除', icon: 'success' })
            this.loadList()
          } catch (e: unknown) {
            const err = e as { message?: string }
            uni.showToast({ title: err.message || '删除失败', icon: 'none' })
          }
        },
      })
    },
    goSend() {
      uni.navigateTo({ url: '/pages/user/send/index' })
    },
    maskPhone(phone: string) {
      if (!phone || phone.length < 7) return phone
      return phone.slice(0, 3) + '****' + phone.slice(-4)
    },
    formatTime(t: string) {
      if (!t) return ''
      return t.replace('T', ' ').slice(0, 16)
    },
    getStatusClass(status: number) {
      if ([0, 1].includes(status)) return 'status-pending'
      if ([2, 3, 4].includes(status)) return 'status-active'
      if (status === 5) return 'status-done'
      if ([6, 7].includes(status)) return 'status-cancel'
      return ''
    },
  },
})
</script>

<style scoped lang="scss">
.page-wrap {
  min-height: 100vh;
  background: #f5f5f5;
}

.page {
  background: #f5f5f5;
  min-height: 100vh;
  padding-bottom: 120rpx;
}

.search-bar {
  background: #fff;
  padding: 20rpx 24rpx;
}

.search-input-wrap {
  display: flex;
  align-items: center;
  background: #f5f5f5;
  border-radius: 40rpx;
  padding: 12rpx 24rpx;
  gap: 12rpx;
}

.search-icon {
  font-size: 28rpx;
}

.search-input {
  flex: 1;
  font-size: 28rpx;
  color: #333;
  background: transparent;
}

.search-clear {
  color: #bbb;
  font-size: 28rpx;
}

.tab-bar {
  display: flex;
  background: #fff;
  border-bottom: 1rpx solid #f0f0f0;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 24rpx 0 20rpx;
  font-size: 28rpx;
  color: #666;
  position: relative;
  &.active {
    color: #1890ff;
    font-weight: 600;
  }
}

.tab-line {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 40rpx;
  height: 4rpx;
  background: #1890ff;
  border-radius: 2rpx;
}

.center-tip {
  text-align: center;
  padding: 80rpx;
  color: #bbb;
  font-size: 28rpx;
}

.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 40rpx;
}

.empty-icon {
  font-size: 80rpx;
  margin-bottom: 24rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #bbb;
  margin-bottom: 32rpx;
}

.empty-btn {
  background: #1890ff;
  color: #fff;
  border-radius: 48rpx;
  font-size: 28rpx;
  padding: 0 48rpx;
  border: none;
}

.list {
  padding: 16rpx 24rpx;
}

.order-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  margin-bottom: 20rpx;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
}

.order-number-wrap {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.order-number {
  font-size: 26rpx;
  color: #333;
  font-family: monospace;
}

.copy-btn {
  font-size: 22rpx;
  color: #1890ff;
  border: 1rpx solid #1890ff;
  padding: 2rpx 10rpx;
  border-radius: 4rpx;
}

.status-badge {
  font-size: 24rpx;
  padding: 4rpx 14rpx;
  border-radius: 20rpx;
  &.status-pending { background: #fff7e6; color: #fa8c16; }
  &.status-active { background: #e6f4ff; color: #1890ff; }
  &.status-done { background: #f6ffed; color: #52c41a; }
  &.status-cancel { background: #fff1f0; color: #ff4d4f; }
}

.order-route {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;
}

.route-person {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.person-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
}

.person-phone {
  font-size: 24rpx;
  color: #999;
}

.route-arrow {
  display: flex;
  align-items: center;
  color: #bbb;
  font-size: 24rpx;
  padding: 0 8rpx;
}

.order-goods {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12rpx 0;
  border-top: 1rpx solid #f5f5f5;
  border-bottom: 1rpx solid #f5f5f5;
}

.goods-name {
  font-size: 26rpx;
  color: #666;
}

.goods-fee {
  font-size: 28rpx;
  font-weight: 600;
  color: #fa8c16;
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 16rpx;
}

.order-time {
  font-size: 24rpx;
  color: #bbb;
}

.action-btns {
  display: flex;
  gap: 12rpx;
}

.action-btn {
  font-size: 22rpx;
  padding: 0 16rpx;
  height: 48rpx;
  line-height: 48rpx;
  border-radius: 8rpx;
  border: 1rpx solid #d9d9d9;
  background: #fff;
  color: #666;
  &.warn {
    border-color: #ff4d4f;
    color: #ff4d4f;
  }
}

.overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 100;
  display: flex;
  align-items: flex-end;
}

.detail-panel {
  background: #fff;
  border-radius: 24rpx 24rpx 0 0;
  width: 100%;
  max-height: 85vh;
  overflow-y: auto;
  padding-bottom: env(safe-area-inset-bottom);
}

.picker-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx 32rpx 24rpx;
  border-bottom: 1rpx solid #f0f0f0;
  position: sticky;
  top: 0;
  background: #fff;
}

.picker-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}

.picker-close {
  font-size: 40rpx;
  color: #999;
  line-height: 1;
}

.detail-body {
  padding: 0 32rpx 32rpx;
}

.detail-section {
  border-bottom: 1rpx solid #f0f0f0;
  padding: 20rpx 0;
  &:last-child { border-bottom: none; }
}

.section-title {
  font-size: 26rpx;
  color: #999;
  margin: 16rpx 0 8rpx;
  font-weight: 600;
}

.detail-row {
  display: flex;
  align-items: flex-start;
  padding: 12rpx 0;
  gap: 16rpx;
}

.detail-label {
  width: 140rpx;
  font-size: 26rpx;
  color: #999;
  flex-shrink: 0;
}

.detail-value {
  flex: 1;
  font-size: 28rpx;
  color: #333;
  &.mono { font-family: monospace; }
  &.fee { color: #fa8c16; font-weight: 600; }
  &.status-pending { color: #fa8c16; }
  &.status-active { color: #1890ff; }
  &.status-done { color: #52c41a; }
  &.status-cancel { color: #ff4d4f; }
}

.detail-value-row {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.copy-link {
  font-size: 22rpx;
  color: #1890ff;
}

.cancel-modal {
  background: #fff;
  border-radius: 24rpx 24rpx 0 0;
  width: 100%;
  padding: 32rpx 32rpx calc(32rpx + env(safe-area-inset-bottom));
}

.cancel-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
  display: block;
  margin-bottom: 24rpx;
}

.cancel-input {
  width: 100%;
  height: 160rpx;
  border: 1rpx solid #e8e8e8;
  border-radius: 12rpx;
  padding: 16rpx;
  font-size: 28rpx;
  color: #333;
  box-sizing: border-box;
  margin-bottom: 24rpx;
}

.cancel-btns {
  display: flex;
  gap: 24rpx;
}

.cancel-btn-no,
.cancel-btn-yes {
  flex: 1;
  height: 80rpx;
  border-radius: 40rpx;
  font-size: 28rpx;
  border: none;
}

.cancel-btn-no {
  background: #f5f5f5;
  color: #666;
}

.cancel-btn-yes {
  background: #ff4d4f;
  color: #fff;
}
</style>
