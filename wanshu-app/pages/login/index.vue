<template>
  <view class="page">
    <view class="logo">万枢物流</view>

    <view class="section">
      <text class="label">用户端（微信演示）</text>
      <button type="primary" @click="onWx">模拟 wx.login 后登录</button>
      <text class="tip">需后端配置 wanshu.auth.wx-mock-user-id</text>
    </view>

    <view class="section">
      <text class="label">司机端（短信）</text>
      <input v-model="phone" placeholder="手机号" type="number" class="input" />
      <view class="row">
        <input v-model="smsCode" placeholder="验证码" class="input flex" />
        <button size="mini" @click="onSendSms">获取验证码</button>
      </view>
      <button type="primary" @click="onSmsLogin">短信登录</button>
    </view>

    <view class="section">
      <text class="label">快递员 / 通用账号</text>
      <input v-model="account" placeholder="账号" class="input" />
      <input v-model="password" password placeholder="密码" class="input" />
      <button type="default" @click="onPwdLogin">密码登录（无图形验证码）</button>
    </view>

    <view class="links">
      <text @click="goUser">进入用户分包</text>
      <text @click="goCourier">快递员端</text>
      <text @click="goDriver">司机端</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import * as auth from '@/api/auth'

const phone = ref('')
const smsCode = ref('')
const account = ref('')
const password = ref('')

function saveToken(token: string) {
  uni.setStorageSync('token', token)
}

async function onWx() {
  try {
    const data = await auth.wxLogin('demo-code-' + Date.now())
    saveToken(data.token)
    uni.showToast({ title: '登录成功', icon: 'success' })
  } catch (e: any) {
    uni.showToast({ title: e.message || '失败', icon: 'none' })
  }
}

async function onSendSms() {
  try {
    await auth.sendSms(phone.value)
    uni.showToast({ title: '已发送(见 Redis)', icon: 'none' })
  } catch (e: any) {
    uni.showToast({ title: e.message || '失败', icon: 'none' })
  }
}

async function onSmsLogin() {
  try {
    const data = await auth.smsLogin(phone.value, smsCode.value)
    saveToken(data.token)
    uni.showToast({ title: '登录成功', icon: 'success' })
  } catch (e: any) {
    uni.showToast({ title: e.message || '失败', icon: 'none' })
  }
}

async function onPwdLogin() {
  try {
    const data = await auth.passwordLogin(account.value, password.value)
    saveToken(data.token)
    uni.showToast({ title: '登录成功', icon: 'success' })
  } catch (e: any) {
    uni.showToast({ title: e.message || '失败', icon: 'none' })
  }
}

function goUser() {
  uni.navigateTo({ url: '/pages/user/send/index' })
}
function goCourier() {
  uni.navigateTo({ url: '/pages/courier/pickup/index' })
}
function goDriver() {
  uni.navigateTo({ url: '/pages/driver/pickup/index' })
}
</script>

<style scoped lang="scss">
.page {
  padding: 32rpx;
}
.logo {
  font-size: 40rpx;
  font-weight: bold;
  text-align: center;
  margin: 48rpx 0;
}
.section {
  background: #fff;
  border-radius: 12rpx;
  padding: 24rpx;
  margin-bottom: 24rpx;
}
.label {
  display: block;
  margin-bottom: 16rpx;
  font-size: 28rpx;
  color: #666;
}
.input {
  border: 1px solid #eee;
  padding: 16rpx;
  margin-bottom: 16rpx;
  border-radius: 8rpx;
}
.row {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 16rpx;
}
.flex {
  flex: 1;
}
.tip {
  font-size: 22rpx;
  color: #999;
  display: block;
  margin-top: 12rpx;
}
.links {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  color: #1890ff;
  font-size: 28rpx;
}
button {
  margin-top: 12rpx;
}
</style>
