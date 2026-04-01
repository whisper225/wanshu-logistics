<template>
	<view class="detail-page">
		<NavBar title="运输详情"></NavBar>

		<view class="content" v-if="task">
			<view class="status-bar" :class="'status-bg-' + task.status">
				<text class="status-text">{{ task.statusText }}</text>
			</view>

			<view class="card">
				<view class="card-title">任务信息</view>
				<view class="info-row">
					<text class="label">任务编号</text>
					<text class="value">{{ task.taskNumber }}</text>
				</view>
				<view class="info-row">
					<text class="label">起始网点</text>
					<text class="value">{{ task.startOrganId }}</text>
				</view>
				<view class="info-row">
					<text class="label">目标网点</text>
					<text class="value">{{ task.endOrganId }}</text>
				</view>
				<view class="info-row" v-if="task.planDepartTime">
					<text class="label">计划出发</text>
					<text class="value">{{ task.planDepartTime }}</text>
				</view>
				<view class="info-row" v-if="task.actualDepartTime">
					<text class="label">实际出发</text>
					<text class="value">{{ task.actualDepartTime }}</text>
				</view>
				<view class="info-row" v-if="task.planArriveTime">
					<text class="label">计划到达</text>
					<text class="value">{{ task.planArriveTime }}</text>
				</view>
				<view class="info-row" v-if="task.actualArriveTime">
					<text class="label">实际到达</text>
					<text class="value">{{ task.actualArriveTime }}</text>
				</view>
				<view class="info-row">
					<text class="label">运单数量</text>
					<text class="value">{{ task.waybillCount || 0 }} 件</text>
				</view>
				<view class="info-row" v-if="task.loadWeight">
					<text class="label">载重</text>
					<text class="value">{{ task.loadWeight }} kg</text>
				</view>
				<view class="info-row" v-if="task.loadVolume">
					<text class="label">体积</text>
					<text class="value">{{ task.loadVolume }} m³</text>
				</view>
			</view>

			<!-- 运单列表 -->
			<view class="card" v-if="task.waybills && task.waybills.length">
				<view class="card-title">运单列表（{{ task.waybills.length }}）</view>
				<view
					class="waybill-item"
					v-for="wb in task.waybills"
					:key="wb.id"
				>
					<view class="wb-header">
						<text class="wb-number">{{ wb.waybillNumber }}</text>
						<text class="wb-status">{{ wb.status }}</text>
					</view>
					<view class="wb-row" v-if="wb.senderName">
						<text class="wb-label">寄</text>
						<text class="wb-value">{{ wb.senderName }} {{ wb.senderPhone }}</text>
					</view>
					<view class="wb-row" v-if="wb.receiverName">
						<text class="wb-label">收</text>
						<text class="wb-value">{{ wb.receiverName }} {{ wb.receiverPhone }}</text>
					</view>
					<view class="wb-row" v-if="wb.goodsName">
						<text class="wb-label">物品</text>
						<text class="wb-value">{{ wb.goodsName }} {{ wb.weight ? wb.weight + 'kg' : '' }}</text>
					</view>
				</view>
			</view>
		</view>

		<!-- 底部操作按钮 -->
		<view class="footer" v-if="task">
			<!-- 待出发 → 出库确认 -->
			<button
				v-if="task.status === 0"
				class="btn-primary"
				:loading="submitting"
				@click="handleDepart"
			>出库确认</button>

			<!-- 运输中 → 入库确认 -->
			<button
				v-if="task.status === 1"
				class="btn-primary"
				:loading="submitting"
				@click="handleArrive"
			>入库确认</button>

			<!-- 已完成 → 回车登记 -->
			<button
				v-if="task.status === 2"
				class="btn-secondary"
				@click="toReturnRegister"
			>回车登记</button>
		</view>
	</view>
</template>

<script setup>
	import { ref } from 'vue'
	import { onLoad } from '@dcloudio/uni-app'
	import NavBar from '@/components/Navbar/index.vue'
	import { getTransportDetail, confirmDeparture, confirmArrival } from '@/pages/api/driver.js'

	const task = ref(null)
	const submitting = ref(false)
	let taskId = null

	onLoad((options) => {
		taskId = options.id
		loadDetail()
	})

	const loadDetail = async () => {
		try {
			const data = await getTransportDetail(taskId)
			task.value = data
		} catch (e) {
			uni.showToast({ title: '加载失败', icon: 'none' })
		}
	}

	const handleDepart = () => {
		if (submitting.value) return
		uni.showModal({
			title: '出库确认',
			content: '确认已完成出库，开始运输？',
			success: async (res) => {
				if (!res.confirm) return
				submitting.value = true
				try {
					await confirmDeparture(taskId)
					uni.showToast({ title: '出库成功', icon: 'success' })
					loadDetail()
				} catch (e) {
					uni.showToast({ title: e.message || '操作失败', icon: 'none' })
				} finally {
					submitting.value = false
				}
			}
		})
	}

	const handleArrive = () => {
		if (submitting.value) return
		uni.showModal({
			title: '入库确认',
			content: '确认已到达目的地，完成运输？',
			success: async (res) => {
				if (!res.confirm) return
				submitting.value = true
				try {
					await confirmArrival(taskId)
					uni.showToast({ title: '入库成功', icon: 'success' })
					loadDetail()
				} catch (e) {
					uni.showToast({ title: e.message || '操作失败', icon: 'none' })
				} finally {
					submitting.value = false
				}
			}
		})
	}

	const toReturnRegister = () => {
		uni.navigateTo({
			url: `/subPages/return-register/index?taskId=${taskId}`
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
	.status-bg-0 { background: #fa8c16; }
	.status-bg-1 { background: #1890ff; }
	.status-bg-2 { background: #52c41a; }
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
	.waybill-item {
		padding: 20rpx 0;
		border-bottom: 1rpx solid #f5f5f5;
		&:last-child { border-bottom: none; }
	}
	.wb-header {
		display: flex;
		justify-content: space-between;
		margin-bottom: 12rpx;
	}
	.wb-number {
		font-size: 26rpx;
		font-weight: bold;
		color: #333;
	}
	.wb-status {
		font-size: 24rpx;
		color: #999;
	}
	.wb-row {
		display: flex;
		padding: 4rpx 0;
		font-size: 24rpx;
	}
	.wb-label {
		color: #999;
		width: 60rpx;
		flex-shrink: 0;
	}
	.wb-value {
		color: #666;
		flex: 1;
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
	.btn-primary {
		height: 88rpx;
		line-height: 88rpx;
		background: var(--essential-color-red);
		color: #fff;
		border-radius: 44rpx;
		font-size: 32rpx;
		text-align: center;
	}
	.btn-secondary {
		height: 88rpx;
		line-height: 88rpx;
		background: #fff;
		color: #333;
		border: 2rpx solid #ddd;
		border-radius: 44rpx;
		font-size: 32rpx;
		text-align: center;
	}
</style>
