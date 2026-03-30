<template>
  <view class="page-wrap">
  <view class="page">
    <!-- 寄件人 -->
    <view class="card">
      <view class="card-title">
        <text class="dot sender" />
        <text>寄件人</text>
      </view>
      <view class="row-field" @click="openAddressPicker('sender')">
        <text class="field-label">姓名</text>
        <text :class="['field-value', !form.senderName && 'placeholder']">
          {{ form.senderName || '请输入姓名' }}
        </text>
      </view>
      <view class="row-field" @click="openAddressPicker('sender')">
        <text class="field-label">电话</text>
        <text :class="['field-value', !form.senderPhone && 'placeholder']">
          {{ form.senderPhone || '请输入手机号' }}
        </text>
      </view>
      <view class="row-field">
        <text class="field-label">地址</text>
        <input
          class="field-input"
          v-model="form.senderAddress"
          placeholder="请输入详细地址"
          @blur="tryEstimate"
        />
      </view>
      <view class="address-book-btn" @click="openAddressPicker('sender')">
        <text class="icon">📋</text>
        <text>从地址簿选择</text>
      </view>
    </view>

    <!-- 收件人 -->
    <view class="card">
      <view class="card-title">
        <text class="dot receiver" />
        <text>收件人</text>
      </view>
      <view class="row-field">
        <text class="field-label">姓名</text>
        <input class="field-input" v-model="form.receiverName" placeholder="请输入姓名" />
      </view>
      <view class="row-field">
        <text class="field-label">电话</text>
        <input class="field-input" v-model="form.receiverPhone" placeholder="请输入手机号" type="number" />
      </view>
      <view class="row-field">
        <text class="field-label">地址</text>
        <input class="field-input" v-model="form.receiverAddress" placeholder="请输入详细地址" @blur="tryEstimate" />
      </view>
      <view class="address-book-btn" @click="openAddressPicker('receiver')">
        <text class="icon">📋</text>
        <text>从地址簿选择</text>
      </view>
    </view>

    <!-- 上门取件时间 -->
    <view class="card">
      <view class="card-title-inline">
        <text class="title-text">上门取件时间</text>
      </view>
      <view class="time-tabs">
        <view
          v-for="t in timeTabs"
          :key="t.value"
          :class="['time-tab', form.pickupDay === t.value && 'active']"
          @click="form.pickupDay = t.value"
        >
          {{ t.label }}
        </view>
      </view>
      <view class="time-slots">
        <view
          v-for="slot in timeSlots"
          :key="slot"
          :class="['time-slot', form.pickupSlot === slot && 'active']"
          @click="form.pickupSlot = slot"
        >
          {{ slot }}
        </view>
      </view>
    </view>

    <!-- 物品信息 -->
    <view class="card">
      <view class="card-title-inline">
        <text class="title-text">物品信息</text>
      </view>
      <view class="row-field">
        <text class="field-label">物品名称</text>
        <input class="field-input" v-model="form.goodsName" placeholder="如：文件、衣物、电子产品" />
      </view>
      <view class="row-field">
        <text class="field-label">物品类型</text>
        <picker :range="goodsTypes" @change="onGoodsTypeChange">
          <text :class="['field-value', !form.goodsType && 'placeholder']">
            {{ form.goodsType || '请选择物品类型' }}
          </text>
        </picker>
      </view>
      <view class="row-field">
        <text class="field-label">预估重量</text>
        <view class="weight-row">
          <input
            class="field-input weight-input"
            v-model="form.weight"
            type="digit"
            placeholder="0.00"
            @blur="tryEstimate"
          />
          <text class="unit">kg</text>
        </view>
      </view>
      <view class="row-field">
        <text class="field-label">预估体积</text>
        <view class="weight-row">
          <input class="field-input weight-input" v-model="form.volume" type="digit" placeholder="0" />
          <text class="unit">cm³</text>
        </view>
      </view>
      <view class="forbidden-link" @click="showForbidden = true">
        <text class="icon">⚠️</text>
        <text>查看禁寄物品目录</text>
      </view>
    </view>

    <!-- 付款方式 -->
    <view class="card">
      <view class="card-title-inline">
        <text class="title-text">付款方式</text>
      </view>
      <view class="payment-options">
        <view
          v-for="p in paymentOptions"
          :key="p.value"
          :class="['payment-option', form.paymentMethod === p.value && 'active']"
          @click="form.paymentMethod = p.value"
        >
          <view :class="['radio', form.paymentMethod === p.value && 'radio-active']" />
          <text>{{ p.label }}</text>
        </view>
      </view>
    </view>

    <!-- 估算邮费 -->
    <view class="fee-bar" v-if="estimatedFee !== null">
      <text class="fee-label">估算邮费</text>
      <text class="fee-value">¥{{ estimatedFee.toFixed(2) }}</text>
      <text class="fee-note">参考价格，以揽件后为准</text>
    </view>

    <!-- 下单按钮 -->
    <view class="submit-bar">
      <view class="submit-fee">
        <text v-if="estimatedFee !== null">预计 ¥{{ estimatedFee.toFixed(2) }}</text>
        <text v-else class="fee-placeholder">填写信息自动估算邮费</text>
      </view>
      <button class="submit-btn" @click="onSubmit" :loading="submitting">立即下单</button>
    </view>

    <!-- 地址簿弹窗 -->
    <view v-if="showAddressPicker" class="overlay" @click.self="showAddressPicker = false">
      <view class="picker-panel">
        <view class="picker-header">
          <text class="picker-title">选择{{ pickerRole === 'sender' ? '寄件' : '收件' }}人</text>
          <text class="picker-close" @click="showAddressPicker = false">✕</text>
        </view>
        <view v-if="addressList.length === 0" class="picker-empty">暂无地址，请先添加地址簿</view>
        <view
          v-for="addr in addressList"
          :key="addr.id"
          class="picker-item"
          @click="selectAddress(addr)"
        >
          <view class="picker-item-top">
            <text class="picker-name">{{ addr.name }}</text>
            <text class="picker-phone">{{ addr.phone }}</text>
            <text v-if="addr.isDefault === 1" class="default-tag">默认</text>
          </view>
          <text class="picker-addr">{{ addr.address }}</text>
        </view>
        <view class="picker-add" @click="goAddAddress">
          <text>+ 添加新地址</text>
        </view>
      </view>
    </view>

    <!-- 禁寄物品弹窗 -->
    <view v-if="showForbidden" class="overlay" @click.self="showForbidden = false">
      <view class="forbidden-panel">
        <view class="picker-header">
          <text class="picker-title">禁止寄递物品目录</text>
          <text class="picker-close" @click="showForbidden = false">✕</text>
        </view>
        <view class="forbidden-list">
          <view v-for="item in forbiddenItems" :key="item" class="forbidden-item">
            <text class="forbidden-dot">•</text>
            <text>{{ item }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
  </view>
</template>

<script lang="ts">
import { defineComponent } from '@vue/runtime-core'
import { listAddress } from '@/api/user'
import type { AddressBook } from '@/api/user'
import { createOrder, estimateFreight } from '@/api/order'

const GOODS_TYPES = ['文件资料', '衣物', '电子产品', '食品', '书籍', '日用品', '化妆品', '玩具', '其他']

const FORBIDDEN_ITEMS = [
  '爆炸物、烟火制品', '压缩气体和液化气体', '易燃液体、固体', '毒害品、感染性物质',
  '放射性物质', '腐蚀品', '枪支弹药、管制刀具', '毒品及制毒物品',
  '黄色读物、盗版物品', '活体动物', '货币、有价证券', '超过物品本身价值的古玩字画',
]

export default defineComponent({
  name: 'SendPage',
  data() {
    return {
      form: {
        senderName: '',
        senderPhone: '',
        senderAddress: '',
        receiverName: '',
        receiverPhone: '',
        receiverAddress: '',
        goodsName: '',
        goodsType: '',
        weight: '',
        volume: '',
        paymentMethod: 1,
        pickupDay: 0,
        pickupSlot: '09:00-12:00',
      },
      estimatedFee: null as number | null,
      submitting: false,
      showAddressPicker: false,
      pickerRole: 'sender' as 'sender' | 'receiver',
      addressList: [] as AddressBook[],
      showForbidden: false,
      timeTabs: [
        { label: '今天', value: 0 },
        { label: '明天', value: 1 },
        { label: '后天', value: 2 },
      ],
      timeSlots: ['09:00-12:00', '12:00-15:00', '15:00-18:00', '18:00-21:00'],
      paymentOptions: [
        { label: '寄付（寄件方付款）', value: 1 },
        { label: '到付（收件方付款）', value: 2 },
      ],
      goodsTypes: GOODS_TYPES,
      forbiddenItems: FORBIDDEN_ITEMS,
    }
  },
  async mounted() {
    try {
      this.addressList = await listAddress()
      const def = this.addressList.find((a: AddressBook) => a.isDefault === 1)
      if (def) {
        this.form.senderName = def.name
        this.form.senderPhone = def.phone
        this.form.senderAddress = def.address
      }
    } catch (_) {}
  },
  methods: {
    onGoodsTypeChange(e: { detail: { value: number } }) {
      this.form.goodsType = GOODS_TYPES[e.detail.value]
    },
    async tryEstimate() {
      const w = parseFloat(this.form.weight)
      if (!isNaN(w) && w > 0) {
        try {
          const res = await estimateFreight(w, this.form.goodsType || undefined)
          this.estimatedFee = res.estimatedFee
        } catch (_) {}
      }
    },
    async openAddressPicker(role: 'sender' | 'receiver') {
      this.pickerRole = role
      if (this.addressList.length === 0) {
        try {
          this.addressList = await listAddress()
        } catch (_) {}
      }
      this.showAddressPicker = true
    },
    selectAddress(addr: AddressBook) {
      if (this.pickerRole === 'sender') {
        this.form.senderName = addr.name
        this.form.senderPhone = addr.phone
        this.form.senderAddress = addr.address
      } else {
        this.form.receiverName = addr.name
        this.form.receiverPhone = addr.phone
        this.form.receiverAddress = addr.address
      }
      this.showAddressPicker = false
    },
    goAddAddress() {
      this.showAddressPicker = false
      uni.navigateTo({ url: '/pages/user/address/index' })
    },
    buildPickupTime(): string {
      const now = new Date()
      now.setDate(now.getDate() + this.form.pickupDay)
      const dateStr = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`
      const startHour = this.form.pickupSlot.split(':')[0]
      return `${dateStr} ${startHour}:00:00`
    },
    async onSubmit() {
      if (!this.form.senderName || !this.form.senderPhone || !this.form.senderAddress) {
        uni.showToast({ title: '请完善寄件人信息', icon: 'none' })
        return
      }
      if (!this.form.receiverName || !this.form.receiverPhone || !this.form.receiverAddress) {
        uni.showToast({ title: '请完善收件人信息', icon: 'none' })
        return
      }
      if (!this.form.goodsName) {
        uni.showToast({ title: '请填写物品名称', icon: 'none' })
        return
      }
      this.submitting = true
      try {
        const order = await createOrder({
          senderName: this.form.senderName,
          senderPhone: this.form.senderPhone,
          senderAddress: this.form.senderAddress,
          receiverName: this.form.receiverName,
          receiverPhone: this.form.receiverPhone,
          receiverAddress: this.form.receiverAddress,
          goodsName: this.form.goodsName,
          goodsType: this.form.goodsType || undefined,
          weight: this.form.weight ? parseFloat(this.form.weight) : undefined,
          volume: this.form.volume ? parseFloat(this.form.volume) : undefined,
          paymentMethod: this.form.paymentMethod,
          estimatedFee: this.estimatedFee ?? undefined,
          pickupTime: this.buildPickupTime(),
        })
        uni.showModal({
          title: '下单成功',
          content: `订单号：${order.orderNumber}\n快递员将在您预约的时间上门取件`,
          showCancel: false,
          success: () => {
            uni.redirectTo({ url: '/pages/user/track/index' })
          },
        })
      } catch (e: unknown) {
        const err = e as { message?: string }
        uni.showToast({ title: err.message || '下单失败', icon: 'none' })
      } finally {
        this.submitting = false
      }
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
  padding-bottom: 160rpx;
  background: #f5f5f5;
}

.card {
  background: #fff;
  border-radius: 16rpx;
  margin: 24rpx 24rpx 0;
  padding: 24rpx;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 12rpx;
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 20rpx;
}

.card-title-inline {
  margin-bottom: 20rpx;
}

.title-text {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
}

.dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  display: block;
  &.sender { background: #e53935; }
  &.receiver { background: #52c41a; }
}

.row-field {
  display: flex;
  align-items: center;
  padding: 20rpx 0;
  border-bottom: 1rpx solid #f0f0f0;
  &:last-of-type { border-bottom: none; }
}

.field-label {
  width: 130rpx;
  font-size: 28rpx;
  color: #666;
  flex-shrink: 0;
}

.field-value {
  flex: 1;
  font-size: 28rpx;
  color: #333;
  &.placeholder { color: #bbb; }
}

.field-input {
  flex: 1;
  font-size: 28rpx;
  color: #333;
}

.weight-row {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.weight-input {
  flex: 1;
}

.unit {
  font-size: 26rpx;
  color: #999;
  width: 60rpx;
}

.address-book-btn {
  display: flex;
  align-items: center;
  gap: 8rpx;
  color: #e53935;
  font-size: 26rpx;
  margin-top: 16rpx;
  .icon { font-size: 28rpx; }
}

.forbidden-link {
  display: flex;
  align-items: center;
  gap: 8rpx;
  color: #ff4d4f;
  font-size: 26rpx;
  margin-top: 16rpx;
  .icon { font-size: 28rpx; }
}

.time-tabs {
  display: flex;
  gap: 16rpx;
  margin-bottom: 20rpx;
}

.time-tab {
  flex: 1;
  text-align: center;
  padding: 14rpx 0;
  border-radius: 8rpx;
  font-size: 28rpx;
  background: #f5f5f5;
  color: #666;
  &.active {
    background: #ffebee;
    color: #e53935;
    font-weight: 600;
  }
}

.time-slots {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
}

.time-slot {
  padding: 12rpx 20rpx;
  border-radius: 8rpx;
  font-size: 26rpx;
  background: #f5f5f5;
  color: #666;
  &.active {
    background: #ffebee;
    color: #e53935;
    font-weight: 600;
  }
}

.payment-options {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.payment-option {
  display: flex;
  align-items: center;
  gap: 16rpx;
  font-size: 28rpx;
  color: #333;
  padding: 12rpx 0;
}

.radio {
  width: 36rpx;
  height: 36rpx;
  border-radius: 50%;
  border: 2rpx solid #d9d9d9;
  &.radio-active {
    border-color: #e53935;
    background: #e53935;
    position: relative;
    &::after {
      content: '';
      position: absolute;
      width: 16rpx;
      height: 16rpx;
      background: #fff;
      border-radius: 50%;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
    }
  }
}

.fee-bar {
  margin: 24rpx 24rpx 0;
  background: #fff7e6;
  border-radius: 12rpx;
  padding: 20rpx 24rpx;
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.fee-label {
  font-size: 26rpx;
  color: #888;
}

.fee-value {
  font-size: 36rpx;
  font-weight: 700;
  color: #fa8c16;
}

.fee-note {
  font-size: 22rpx;
  color: #aaa;
  margin-left: auto;
}

.submit-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: 20rpx 32rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  display: flex;
  align-items: center;
  gap: 24rpx;
  box-shadow: 0 -2rpx 12rpx rgba(0, 0, 0, 0.06);
}

.submit-fee {
  flex: 1;
  font-size: 26rpx;
  color: #fa8c16;
  font-weight: 600;
}

.fee-placeholder {
  color: #bbb;
  font-weight: normal;
}

.submit-btn {
  background: #e53935;
  color: #fff;
  border-radius: 48rpx;
  font-size: 30rpx;
  padding: 0 48rpx;
  height: 80rpx;
  line-height: 80rpx;
  border: none;
}

.overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 100;
  display: flex;
  align-items: flex-end;
}

.picker-panel,
.forbidden-panel {
  background: #fff;
  border-radius: 24rpx 24rpx 0 0;
  width: 100%;
  max-height: 70vh;
  overflow-y: auto;
  padding-bottom: env(safe-area-inset-bottom);
}

.picker-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx 32rpx 24rpx;
  border-bottom: 1rpx solid #f0f0f0;
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

.picker-empty {
  text-align: center;
  padding: 60rpx;
  color: #999;
  font-size: 28rpx;
}

.picker-item {
  padding: 24rpx 32rpx;
  border-bottom: 1rpx solid #f5f5f5;
}

.picker-item-top {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 8rpx;
}

.picker-name {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
}

.picker-phone {
  font-size: 26rpx;
  color: #666;
}

.default-tag {
  font-size: 22rpx;
  color: #e53935;
  border: 1rpx solid #e53935;
  padding: 2rpx 8rpx;
  border-radius: 4rpx;
}

.picker-addr {
  font-size: 26rpx;
  color: #999;
}

.picker-add {
  text-align: center;
  padding: 32rpx;
  color: #e53935;
  font-size: 28rpx;
}

.forbidden-list {
  padding: 24rpx 32rpx;
}

.forbidden-item {
  display: flex;
  gap: 12rpx;
  padding: 16rpx 0;
  font-size: 28rpx;
  color: #333;
  border-bottom: 1rpx solid #f5f5f5;
}

.forbidden-dot {
  color: #ff4d4f;
  font-size: 32rpx;
  line-height: 1.4;
}
</style>
