<template>
	<view class="my-page">
		<view class="header" :style="{'paddingTop': capsuleBottom + 'px'}">
			<view class="user-info">
				<view class="avatar-box">
					<text class="avatar-text">司</text>
				</view>
				<view class="info-text">
					<text class="name">{{ nickName || '司机' }}</text>
					<text class="phone" v-if="phone">{{ phone }}</text>
				</view>
			</view>
		</view>

		<view class="menu-list">
			<view class="menu-item" @click="handleAbout">
				<text class="menu-label">关于</text>
				<text class="menu-arrow">></text>
			</view>
			<view class="menu-item logout" @click="handleLogout">
				<text class="menu-label">退出登录</text>
			</view>
		</view>
	</view>
</template>

<script setup>
	import { ref } from 'vue'
	import { onShow, onLoad } from '@dcloudio/uni-app'
	import { logout } from '../api/login.js'

	const capsuleBottom = ref(0)
	const nickName = ref('')
	const phone = ref('')

	onLoad(() => {
		uni.getSystemInfo({
			success: (res) => {
				capsuleBottom.value = uni.getMenuButtonBoundingClientRect().bottom
			}
		})
	})

	onShow(() => {
		nickName.value = uni.getStorageSync('nickName') || ''
		phone.value = uni.getStorageSync('phone') || ''
	})

	const handleAbout = () => {
		uni.showModal({
			title: '关于',
			content: '万枢物流 司机端 v1.0.0',
			showCancel: false
		})
	}

	const handleLogout = () => {
		uni.showModal({
			title: '提示',
			content: '确定退出登录？',
			success: (res) => {
				if (res.confirm) {
					logout().catch(() => {})
					uni.removeStorageSync('token')
					uni.removeStorageSync('nickName')
					uni.removeStorageSync('phone')
					uni.removeStorageSync('avatarUrl')
					uni.reLaunch({ url: '/pages/login/index' })
				}
			}
		})
	}
</script>

<style lang="scss" scoped>
	.my-page {
		min-height: 100vh;
		background: #f3f5f9;
	}
	.header {
		background: #fff;
		padding-bottom: 40rpx;
	}
	.user-info {
		display: flex;
		align-items: center;
		padding: 40rpx 32rpx 0;
	}
	.avatar-box {
		width: 100rpx;
		height: 100rpx;
		border-radius: 50%;
		background: var(--essential-color-red);
		display: flex;
		align-items: center;
		justify-content: center;
		margin-right: 24rpx;
		.avatar-text {
			color: #fff;
			font-size: 40rpx;
			font-weight: bold;
		}
	}
	.info-text {
		display: flex;
		flex-direction: column;
		.name {
			font-size: 34rpx;
			font-weight: bold;
			color: #151515;
		}
		.phone {
			font-size: 26rpx;
			color: #999;
			margin-top: 8rpx;
		}
	}
	.menu-list {
		margin-top: 20rpx;
		background: #fff;
		border-radius: 20rpx;
		margin-left: 28rpx;
		margin-right: 28rpx;
		overflow: hidden;
	}
	.menu-item {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding: 32rpx;
		border-bottom: 1rpx solid #f0f0f0;
		&:last-child {
			border-bottom: none;
		}
	}
	.menu-label {
		font-size: 28rpx;
		color: #333;
	}
	.menu-arrow {
		font-size: 28rpx;
		color: #ccc;
	}
	.logout .menu-label {
		color: var(--essential-color-red);
	}
</style>
