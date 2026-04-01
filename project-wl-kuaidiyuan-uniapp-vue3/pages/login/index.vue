<template>
	<view class="loginBox">
		<NavBar title="登录"></NavBar>
		<view class="logo-box">
			<text class="logo-text">快递员工作台</text>
		</view>

		<button class="open-dialog wx-btn" :loading="loading" @click="handleWxLogin">微信一键登录</button>

		<view class="tips-text">登录即表示同意平台服务条款与隐私政策</view>
	</view>
</template>

<script setup>
	import { ref } from 'vue'
	import { wxLogin } from '../api/login.js'
	import NavBar from '@/components/Navbar/index.vue'
	import { useStore } from 'vuex'

	const loading = ref(false)
	const store = useStore()

	const handleWxLogin = () => {
		if (loading.value) return
		loading.value = true
		uni.login({
			provider: 'weixin',
			success: (loginRes) => {
				if (!loginRes.code) {
					loading.value = false
					uni.showToast({ title: '获取登录凭证失败', icon: 'none' })
					return
				}
				wxLogin({ code: loginRes.code, role: 'courier' })
					.then((data) => {
						const token = data && data.token
						if (!token) {
							uni.showToast({ title: '登录失败：无 token', icon: 'none' })
							return
						}
						uni.setStorageSync('token', token)
						const info = data.userInfo || {}
						if (info.name) uni.setStorageSync('nickName', info.name)
						if (info.phone) uni.setStorageSync('phone', info.phone)
						if (info.avatar) uni.setStorageSync('avatarUrl', info.avatar)
						store.commit('user/setIsLoginSuccess', true)
						uni.switchTab({ url: '/pages/pickup/index' })
					})
					.catch((err) => {
						const msg = (err && err.message) || '登录失败'
						uni.showToast({ title: msg, icon: 'none', duration: 2500 })
					})
					.finally(() => {
						loading.value = false
					})
			},
			fail: () => {
				loading.value = false
				uni.showToast({ title: '微信登录不可用', icon: 'none' })
			}
		})
	}
</script>

<style lang="scss" scoped>
	.loginBox {
		min-height: 100vh;
		background: #fff;
	}
	.logo-box {
		padding: 120rpx 0 60rpx;
		text-align: center;
		.logo-text {
			font-size: 48rpx;
			font-weight: bold;
			color: var(--neutral-color-main);
		}
	}
	.wx-btn {
		margin: 40rpx 48rpx 0;
		height: 88rpx;
		line-height: 88rpx;
		background: #07c160;
		color: #fff;
		border-radius: 12rpx;
		font-size: 32rpx;
	}
	.tips-text {
		margin: 32rpx 48rpx;
		font-size: 24rpx;
		color: #999;
		text-align: center;
	}
</style>
