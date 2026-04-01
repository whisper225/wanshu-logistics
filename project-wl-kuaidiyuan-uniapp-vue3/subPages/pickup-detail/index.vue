<template>
	<view class="detail-page">
		<NavBar title="揽收详情"></NavBar>

		<view class="content" v-if="task">
			<view class="status-bar">
				<text class="status-text">{{ task.statusText }}</text>
			</view>

			<view class="card">
				<view class="card-title">任务信息</view>
				<view class="info-row">
					<text class="label">任务编号</text>
					<text class="value">{{ task.taskNumber }}</text>
				</view>
				<view class="info-row" v-if="task.assignTime">
					<text class="label">分配时间</text>
					<text class="value">{{ task.assignTime }}</text>
				</view>
			</view>

			<view class="card" v-if="task.senderName">
				<view class="card-title">寄件人信息</view>
				<view class="info-row">
					<text class="label">姓名</text>
					<text class="value">{{ task.senderName }}</text>
				</view>
				<view class="info-row">
					<text class="label">电话</text>
					<text class="value link" @click="callPhone(task.senderPhone)">{{ task.senderPhone }}</text>
				</view>
				<view class="info-row">
					<text class="label">地址</text>
					<text class="value">{{ task.senderAddress }}</text>
				</view>
			</view>

			<view class="card" v-if="task.goodsName">
				<view class="card-title">物品信息</view>
				<view class="info-row">
					<text class="label">物品名称</text>
					<text class="value">{{ task.goodsName }}</text>
				</view>
				<view class="info-row" v-if="task.weight">
					<text class="label">重量</text>
					<text class="value">{{ task.weight }}kg</text>
				</view>
			</view>

			<!-- 备注输入（仅已分配状态可操作） -->
			<view class="card" v-if="task.status === 1">
				<view class="card-title">揽收备注</view>
				<textarea
					class="remark-input"
					v-model="remark"
					placeholder="请输入揽收备注（选填）"
					:maxlength="200"
				/>
			</view>
		</view>

		<!-- 底部操作按钮 -->
		<view class="footer" v-if="task && task.status === 1">
			<button class="btn-confirm" :loading="submitting" @click="handleComplete">确认揽收</button>
		</view>
	</view>
</template>

<script setup>
	import { ref } from 'vue'
	import { onLoad } from '@dcloudio/uni-app'
	import NavBar from '@/components/Navbar/index.vue'
	import { getPickupTasks, completePickup } from '@/pages/api/courier.js'

	const task = ref(null)
	const remark = ref('')
	const submitting = ref(false)
	let taskId = null

	onLoad((options) => {
		taskId = options.id
		loadDetail()
	})

	const loadDetail = async () => {
		try {
			const list = await getPickupTasks()
			task.value = (list || []).find(item => String(item.id) === String(taskId)) || null
		} catch (e) {
			uni.showToast({ title: '加载失败', icon: 'none' })
		}
	}

	const callPhone = (phone) => {
		if (phone) uni.makePhoneCall({ phoneNumber: phone })
	}

	const handleComplete = async () => {
		if (submitting.value) return
		uni.showModal({
			title: '确认揽收',
			content: '确定已完成揽收？',
			success: async (res) => {
				if (!res.confirm) return
				submitting.value = true
				try {
					await completePickup(taskId, remark.value || undefined)
					uni.showToast({ title: '揽收成功', icon: 'success' })
					setTimeout(() => uni.navigateBack(), 1500)
				} catch (e) {
					uni.showToast({ title: e.message || '操作失败', icon: 'none' })
				} finally {
					submitting.value = false
				}
			}
		})
	}
</script>

<style lang="scss" scoped>
	.detail-page {
		min-height: 100vh;
		background: #f3f5f9;
		padding-bottom: 160rpx;
	}
	.content {
		padding: 20rpx 28rpx;
	}
	.status-bar {
		background: var(--essential-color-red);
		padding: 24rpx 32rpx;
		border-radius: 20rpx;
		margin-bottom: 20rpx;
		.status-text {
			color: #fff;
			font-size: 32rpx;
			font-weight: bold;
		}
	}
	.card {
		background: #fff;
		border-radius: 20rpx;
		padding: 28rpx 32rpx;
		margin-bottom: 20rpx;
	}
	.card-title {
		font-size: 30rpx;
		font-weight: bold;
		color: #151515;
		padding-bottom: 20rpx;
		border-bottom: 1rpx solid #f0f0f0;
		margin-bottom: 16rpx;
	}
	.info-row {
		display: flex;
		padding: 12rpx 0;
		font-size: 26rpx;
		line-height: 40rpx;
	}
	.label {
		color: #999;
		width: 140rpx;
		flex-shrink: 0;
	}
	.value {
		color: #333;
		flex: 1;
	}
	.link {
		color: #1890ff;
	}
	.remark-input {
		width: 100%;
		height: 200rpx;
		font-size: 26rpx;
		padding: 16rpx;
		box-sizing: border-box;
		background: #f9f9f9;
		border-radius: 12rpx;
	}
	.footer {
		position: fixed;
		bottom: 0;
		left: 0;
		right: 0;
		padding: 20rpx 48rpx;
		padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
		background: #fff;
		box-shadow: 0 -4rpx 16rpx rgba(0,0,0,0.05);
	}
	.btn-confirm {
		height: 88rpx;
		line-height: 88rpx;
		background: var(--essential-color-red);
		color: #fff;
		border-radius: 44rpx;
		font-size: 32rpx;
		text-align: center;
	}
</style>
