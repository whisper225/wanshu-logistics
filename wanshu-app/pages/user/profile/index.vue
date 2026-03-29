<template>
  <view class="page-wrap">
  <view class="page">
    <!-- 头像 & 基础信息 -->
    <view class="header-card">
      <view class="avatar-wrap">
        <image
          class="avatar"
          :src="displayAvatar"
          mode="aspectFill"
        />
      </view>
      <view class="user-meta">
        <text class="user-name">{{ displayName }}</text>
        <text class="user-phone">{{ displayPhone }}</text>
      </view>
    </view>

    <!-- 功能菜单 -->
    <view class="menu-card">
      <view class="menu-item" @click="goAddress">
        <view class="menu-left">
          <text class="menu-icon">📋</text>
          <text class="menu-label">我的地址簿</text>
        </view>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @click="goSend">
        <view class="menu-left">
          <text class="menu-icon">📦</text>
          <text class="menu-label">寄快递</text>
        </view>
        <text class="menu-arrow">›</text>
      </view>
      <view class="menu-item" @click="goTrack">
        <view class="menu-left">
          <text class="menu-icon">🔍</text>
          <text class="menu-label">查快递</text>
        </view>
        <text class="menu-arrow">›</text>
      </view>
    </view>

    <!-- 个人信息编辑 -->
    <view class="menu-card">
      <view class="menu-item" @click="openInfoEdit">
        <view class="menu-left">
          <text class="menu-icon">👤</text>
          <text class="menu-label">个人信息</text>
        </view>
        <view class="menu-right">
          <text class="menu-hint">性别 · 生日</text>
          <text class="menu-arrow">›</text>
        </view>
      </view>
    </view>

    <!-- 退出登录 -->
    <view class="logout-card">
      <button class="logout-btn" @click="onLogout">退出登录</button>
    </view>

    <!-- 版本号 -->
    <view class="version-tip">
      <text>万枢物流 v1.0.0</text>
    </view>

    <UserTabBar current="profile" />

    <!-- 个人信息编辑弹窗 -->
    <view v-if="showInfoEdit" class="overlay" @click.self="closeInfoEdit">
      <view class="form-panel">
        <view class="form-header">
          <text class="form-title">编辑个人信息</text>
          <text class="form-close" @click="closeInfoEdit">✕</text>
        </view>
        <view class="form-body">
          <!-- 性别 -->
          <view class="form-row">
            <text class="form-label">性别</text>
            <view class="gender-options">
              <view
                v-for="g in genderOptions"
                :key="g.value"
                :class="['gender-opt', infoForm.gender === g.value && 'active']"
                @click="selectGender(g.value)"
              >
                {{ g.label }}
              </view>
            </view>
          </view>
          <!-- 生日 -->
          <view class="form-row">
            <text class="form-label">生日</text>
            <picker mode="date" :value="infoForm.birthday || ''" @change="onBirthdayChange">
              <text :class="['picker-val', !infoForm.birthday && 'placeholder']">
                {{ infoForm.birthday || '请选择生日' }}
              </text>
            </picker>
          </view>
        </view>
        <view class="form-footer">
          <button class="form-cancel" @click="closeInfoEdit">取消</button>
          <button class="form-submit" @click="onSaveInfo" :loading="savingInfo">保存</button>
        </view>
      </view>
    </view>
  </view>
  </view>
</template>

<script lang="ts">
import { defineComponent } from '@vue/runtime-core'
import UserTabBar from '@/components/user/UserTabBar.vue'
import { getUserInfo } from '@/api/user'
import type { UserInfo } from '@/api/user'
import { logout } from '@/api/auth'

export default defineComponent({
  name: 'ProfilePage',
  components: { UserTabBar },
  data() {
    return {
      userInfo: null as UserInfo | null,
      showInfoEdit: false,
      savingInfo: false,
      defaultAvatar: 'https://api.dicebear.com/7.x/thumbs/svg?seed=wanshu',
      infoForm: {
        gender: 0,
        birthday: '',
      },
      genderOptions: [
        { label: '未知', value: 0 },
        { label: '男', value: 1 },
        { label: '女', value: 2 },
      ],
    }
  },
  computed: {
    displayAvatar(): string {
      return (this.userInfo && this.userInfo.avatar) ? this.userInfo.avatar : this.defaultAvatar
    },
    displayName(): string {
      return (this.userInfo && this.userInfo.name) ? this.userInfo.name : '万枢用户'
    },
    displayPhone(): string {
      return (this.userInfo && this.userInfo.phone) ? this.userInfo.phone : '暂未绑定手机号'
    },
  },
  mounted() {
    this.loadUserInfo()
  },
  methods: {
    async loadUserInfo() {
      try {
        this.userInfo = await getUserInfo()
        this.infoForm.gender = this.userInfo.gender ?? 0
      } catch (_) {}
    },
    onBirthdayChange(e: { detail: { value: string } }) {
      this.infoForm.birthday = e.detail.value
    },
    selectGender(val: number) {
      this.infoForm.gender = val
    },
    openInfoEdit() {
      this.showInfoEdit = true
    },
    closeInfoEdit() {
      this.showInfoEdit = false
    },
    async onSaveInfo() {
      uni.showToast({ title: '保存成功', icon: 'success' })
      this.showInfoEdit = false
    },
    goAddress() {
      uni.navigateTo({ url: '/pages/user/address/index' })
    },
    goSend() {
      uni.navigateTo({ url: '/pages/user/send/index' })
    },
    goTrack() {
      uni.navigateTo({ url: '/pages/user/track/index' })
    },
    async onLogout() {
      uni.showModal({
        title: '退出登录',
        content: '确定要退出登录吗？',
        success: async ({ confirm }: { confirm: boolean }) => {
          if (!confirm) return
          try { await logout() } catch (_) {}
          uni.removeStorageSync('token')
          uni.reLaunch({ url: '/pages/login/index' })
        },
      })
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

.header-card {
  background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
  padding: 60rpx 32rpx 40rpx;
  display: flex;
  align-items: center;
  gap: 32rpx;
}

.avatar-wrap {
  flex-shrink: 0;
}

.avatar {
  width: 128rpx;
  height: 128rpx;
  border-radius: 50%;
  border: 4rpx solid rgba(255,255,255,0.6);
  background: #e6f4ff;
}

.user-meta {
  display: flex;
  flex-direction: column;
  gap: 10rpx;
}

.user-name {
  font-size: 36rpx;
  font-weight: 700;
  color: #fff;
}

.user-phone {
  font-size: 26rpx;
  color: rgba(255,255,255,0.8);
}

.menu-card {
  background: #fff;
  border-radius: 16rpx;
  margin: 24rpx 24rpx 0;
  overflow: hidden;
}

.menu-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 32rpx 24rpx;
  border-bottom: 1rpx solid #f5f5f5;
  &:last-child { border-bottom: none; }
}

.menu-left {
  display: flex;
  align-items: center;
  gap: 20rpx;
}

.menu-icon {
  font-size: 36rpx;
}

.menu-label {
  font-size: 28rpx;
  color: #333;
}

.menu-right {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.menu-hint {
  font-size: 24rpx;
  color: #bbb;
}

.menu-arrow {
  font-size: 36rpx;
  color: #bbb;
  line-height: 1;
}

.logout-card {
  margin: 40rpx 24rpx 0;
}

.logout-btn {
  background: #fff;
  color: #ff4d4f;
  border-radius: 16rpx;
  font-size: 30rpx;
  height: 88rpx;
  line-height: 88rpx;
  border: none;
  width: 100%;
}

.version-tip {
  text-align: center;
  padding: 40rpx;
  font-size: 24rpx;
  color: #bbb;
}

.overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.5);
  z-index: 100;
  display: flex;
  align-items: flex-end;
}

.form-panel {
  background: #fff;
  border-radius: 24rpx 24rpx 0 0;
  width: 100%;
}

.form-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx 32rpx 24rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.form-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}

.form-close {
  font-size: 40rpx;
  color: #999;
}

.form-body {
  padding: 0 32rpx;
}

.form-row {
  display: flex;
  align-items: center;
  padding: 28rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
  gap: 16rpx;
  &:last-child { border-bottom: none; }
}

.form-label {
  width: 100rpx;
  font-size: 28rpx;
  color: #666;
  flex-shrink: 0;
}

.gender-options {
  display: flex;
  gap: 16rpx;
}

.gender-opt {
  padding: 12rpx 32rpx;
  border-radius: 40rpx;
  font-size: 28rpx;
  color: #666;
  background: #f5f5f5;
  &.active {
    background: #e6f4ff;
    color: #1890ff;
    font-weight: 600;
  }
}

.picker-val {
  flex: 1;
  font-size: 28rpx;
  color: #333;
  &.placeholder { color: #bbb; }
}

.form-footer {
  display: flex;
  gap: 24rpx;
  padding: 24rpx 32rpx;
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
  border-top: 1rpx solid #f0f0f0;
}

.form-cancel,
.form-submit {
  flex: 1;
  height: 80rpx;
  border-radius: 40rpx;
  font-size: 28rpx;
  border: none;
}

.form-cancel {
  background: #f5f5f5;
  color: #666;
}

.form-submit {
  background: #1890ff;
  color: #fff;
}
</style>
