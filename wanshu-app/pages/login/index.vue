<template>
  <view class="page">
    <!-- 顶部 Hero -->
    <view class="hero">
      <view class="logo-wrap">
        <text class="logo-icon">🚚</text>
        <text class="logo-text">万枢物流</text>
      </view>
      <text class="logo-sub">高效 · 安全 · 便捷</text>
    </view>

    <!-- 三端切换 Tab -->
    <view class="role-tabs">
      <view
        v-for="tab in roleTabs"
        :key="tab.role"
        :class="['role-tab', activeRole === tab.role ? 'role-tab-active' : '']"
        @click="switchRole(tab.role)"
      >
        <text class="role-tab-icon">{{ tab.icon }}</text>
        <text class="role-tab-label">{{ tab.label }}</text>
      </view>
    </view>

    <!-- 当前端介绍 -->
    <view class="role-desc-card">
      <text class="role-desc-title">{{ currentTitle }}</text>
      <text class="role-desc-sub">{{ currentDesc }}</text>
    </view>

    <!-- 微信登录按钮 -->
    <view class="login-box">
      <button
        class="wx-login-btn"
        :loading="loading"
        @click="onWxLogin"
      >
        <text class="wx-login-text">微信一键登录</text>
      </button>
      <text class="login-tip">登录即代表同意《用户协议》和《隐私政策》</text>
    </view>

    <!-- 底部直达（调试用） -->
    <view class="footer-links">
      <text class="footer-link" @click="goNext('user')">用户端</text>
      <text class="footer-divider">|</text>
      <text class="footer-link" @click="goNext('courier')">快递员端</text>
      <text class="footer-divider">|</text>
      <text class="footer-link" @click="goNext('driver')">司机端</text>
    </view>
  </view>
</template>

<script lang="ts">
import { defineComponent } from '@vue/runtime-core'
import { wxLogin } from '@/api/auth'

type UserRole = 'user' | 'courier' | 'driver'

const ROLE_TABS = [
  {
    role: 'user' as UserRole,
    icon: '📦',
    label: '用户端',
    title: '寄快递 · 查快递',
    desc: '下单寄件、实时跟踪运单状态、管理地址簿',
    home: '/pages/user/send/index',
  },
  {
    role: 'courier' as UserRole,
    icon: '🛵',
    label: '快递员端',
    title: '取件 · 派件',
    desc: '查看取件任务、完成派件作业、查看历史记录',
    home: '/pages/courier/pickup/index',
  },
  {
    role: 'driver' as UserRole,
    icon: '🚛',
    label: '司机端',
    title: '提货 · 运输 · 交付',
    desc: '接收运输任务、在途跟踪、完成货物交付',
    home: '/pages/driver/pickup/index',
  },
]

export default defineComponent({
  name: 'LoginPage',
  data() {
    return {
      roleTabs: ROLE_TABS,
      activeRole: 'user' as UserRole,
      loading: false,
    }
  },
  computed: {
    currentTab(): typeof ROLE_TABS[0] {
      return this.roleTabs.find((t) => t.role === this.activeRole) || ROLE_TABS[0]
    },
    currentTitle(): string {
      return this.currentTab.title
    },
    currentDesc(): string {
      return this.currentTab.desc
    },
  },
  methods: {
    switchRole(role: UserRole) {
      this.activeRole = role
    },
    onWxLogin() {
      if (this.loading) return
      this.loading = true
      const role = this.activeRole
      const home = this.currentTab.home

      uni.login({
        provider: 'weixin',
        success: async (loginRes: { code: string }) => {
          try {
            const data = await wxLogin(loginRes.code, role)
            uni.setStorageSync('token', data.token)
            uni.setStorageSync('role', role)
            uni.showToast({ title: '登录成功', icon: 'success' })
            setTimeout(() => uni.reLaunch({ url: home }), 600)
          } catch (e: unknown) {
            const err = e as { message?: string }
            uni.showToast({ title: err.message || '登录失败', icon: 'none' })
          } finally {
            this.loading = false
          }
        },
        fail: () => {
          uni.showToast({ title: '获取微信授权失败', icon: 'none' })
          this.loading = false
        },
      })
    },
    goNext(role: UserRole) {
      const tab = this.roleTabs.find((t) => t.role === role)
      if (tab) uni.reLaunch({ url: tab.home })
    },
  },
})
</script>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  background: linear-gradient(160deg, #e8f4ff 0%, #f5f5f5 50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-bottom: 60rpx;
}

/* Hero */
.hero {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 100rpx 0 60rpx;
}

.logo-wrap {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 16rpx;
}

.logo-icon {
  font-size: 64rpx;
}

.logo-text {
  font-size: 52rpx;
  font-weight: 800;
  color: #1890ff;
  letter-spacing: 4rpx;
}

.logo-sub {
  font-size: 26rpx;
  color: #999;
  letter-spacing: 6rpx;
}

/* Role Tabs */
.role-tabs {
  display: flex;
  background: #fff;
  border-radius: 20rpx;
  padding: 8rpx;
  margin: 0 40rpx 32rpx;
  width: calc(100% - 80rpx);
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.06);
  box-sizing: border-box;
}

.role-tab {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  padding: 20rpx 0;
  border-radius: 14rpx;
}

.role-tab-active {
  background: #1890ff;
}

.role-tab-active .role-tab-label {
  color: #fff;
  font-weight: 700;
}

.role-tab-icon {
  font-size: 36rpx;
}

.role-tab-label {
  font-size: 24rpx;
  color: #666;
}

/* Desc Card */
.role-desc-card {
  background: #fff;
  border-radius: 20rpx;
  padding: 32rpx 40rpx;
  margin: 0 40rpx 48rpx;
  width: calc(100% - 80rpx);
  box-sizing: border-box;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.06);
  min-height: 120rpx;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 12rpx;
}

.role-desc-title {
  font-size: 32rpx;
  font-weight: 700;
  color: #1890ff;
  display: block;
}

.role-desc-sub {
  font-size: 26rpx;
  color: #888;
  line-height: 1.6;
  display: block;
}

/* Login */
.login-box {
  width: calc(100% - 80rpx);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24rpx;
}

.wx-login-btn {
  width: 100%;
  height: 96rpx;
  background: #07c160;
  color: #fff;
  border-radius: 48rpx;
  font-size: 32rpx;
  font-weight: 600;
  border: none;
  box-shadow: 0 8rpx 24rpx rgba(7, 193, 96, 0.35);
}

.wx-login-text {
  font-size: 32rpx;
}

.login-tip {
  font-size: 22rpx;
  color: #bbb;
  text-align: center;
  display: block;
}

/* Footer */
.footer-links {
  display: flex;
  align-items: center;
  gap: 20rpx;
  margin-top: 60rpx;
}

.footer-link {
  font-size: 24rpx;
  color: #1890ff;
}

.footer-divider {
  font-size: 24rpx;
  color: #e0e0e0;
}
</style>
