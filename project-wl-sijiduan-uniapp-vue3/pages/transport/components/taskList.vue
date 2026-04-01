<template>
	<view class="task-list">
		<view v-if="list.length === 0 && !loading" class="empty-box">
			<text>暂无运输任务</text>
		</view>
		<view
			v-for="item in list"
			:key="item.id"
			class="task-card"
			@click="toDetail(item)"
		>
			<view class="card-header">
				<text class="task-number">{{ item.taskNumber }}</text>
				<text class="status-tag" :class="'status-' + item.status">{{ item.statusText }}</text>
			</view>
			<view class="card-body">
				<view class="route-info">
					<view class="route-point">
						<view class="dot start-dot"></view>
						<text class="route-text">{{ item.startOrganId || '起始网点' }}</text>
					</view>
					<view class="route-arrow">→</view>
					<view class="route-point">
						<view class="dot end-dot"></view>
						<text class="route-text">{{ item.endOrganId || '目标网点' }}</text>
					</view>
				</view>
				<view class="info-row" v-if="item.planDepartTime">
					<text class="label">计划出发</text>
					<text class="value">{{ item.planDepartTime }}</text>
				</view>
				<view class="info-row" v-if="item.planArriveTime">
					<text class="label">计划到达</text>
					<text class="value">{{ item.planArriveTime }}</text>
				</view>
				<view class="info-row">
					<text class="label">运单数量</text>
					<text class="value">{{ item.waybillCount || 0 }} 件</text>
				</view>
				<view class="info-row" v-if="item.loadWeight">
					<text class="label">载重</text>
					<text class="value">{{ item.loadWeight }} kg</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
	import { ref } from 'vue'
	import { getTransportTasks } from '../../api/driver.js'

	const props = defineProps({
		status: {
			type: Number,
			default: undefined
		}
	})

	const emit = defineEmits(['refresh-done'])
	const list = ref([])
	const loading = ref(false)

	const loadTasks = async () => {
		loading.value = true
		try {
			const data = await getTransportTasks(props.status)
			list.value = data || []
		} catch (e) {
			uni.showToast({ title: e.message || '加载失败', icon: 'none' })
			list.value = []
		} finally {
			loading.value = false
			emit('refresh-done')
		}
	}

	const toDetail = (item) => {
		uni.navigateTo({
			url: `/subPages/transport-detail/index?id=${item.id}`
		})
	}

	defineExpose({ loadTasks })
</script>

<style lang="scss" scoped>
	.task-list {
		padding: 0 28rpx;
	}
	.empty-box {
		padding: 200rpx 0;
		text-align: center;
		color: #999;
		font-size: 28rpx;
	}
	.task-card {
		background: #fff;
		border-radius: 20rpx;
		padding: 28rpx 32rpx;
		margin-top: 20rpx;
		box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
	}
	.card-header {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding-bottom: 20rpx;
		border-bottom: 1rpx solid #f0f0f0;
	}
	.task-number {
		font-size: 28rpx;
		font-weight: bold;
		color: #151515;
	}
	.status-tag {
		font-size: 24rpx;
		padding: 4rpx 16rpx;
		border-radius: 8rpx;
		background: #f0f0f0;
		color: #666;
	}
	.status-0 { background: #fff7e6; color: #fa8c16; }
	.status-1 { background: #e6f7ff; color: #1890ff; }
	.status-2 { background: #f6ffed; color: #52c41a; }
	.card-body {
		padding: 20rpx 0;
	}
	.route-info {
		display: flex;
		align-items: center;
		padding: 16rpx 0;
		margin-bottom: 12rpx;
	}
	.route-point {
		display: flex;
		align-items: center;
	}
	.dot {
		width: 16rpx;
		height: 16rpx;
		border-radius: 50%;
		margin-right: 12rpx;
	}
	.start-dot { background: #52c41a; }
	.end-dot { background: #E94739; }
	.route-text {
		font-size: 28rpx;
		font-weight: bold;
		color: #333;
	}
	.route-arrow {
		margin: 0 24rpx;
		color: #ccc;
		font-size: 32rpx;
	}
	.info-row {
		display: flex;
		padding: 8rpx 0;
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
</style>
