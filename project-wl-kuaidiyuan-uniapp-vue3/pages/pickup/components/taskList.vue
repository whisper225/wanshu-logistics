<template>
	<view class="task-list">
		<view v-if="list.length === 0 && !loading" class="empty-box">
			<text>暂无揽收任务</text>
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
			<view class="card-body" v-if="item.senderName">
				<view class="info-row">
					<text class="label">寄件人</text>
					<text class="value">{{ item.senderName }} {{ item.senderPhone }}</text>
				</view>
				<view class="info-row">
					<text class="label">地址</text>
					<text class="value">{{ item.senderAddress }}</text>
				</view>
				<view class="info-row" v-if="item.goodsName">
					<text class="label">物品</text>
					<text class="value">{{ item.goodsName }} {{ item.weight ? item.weight + 'kg' : '' }}</text>
				</view>
			</view>
			<view class="card-footer">
				<text class="time" v-if="item.assignTime">分配时间：{{ item.assignTime }}</text>
			</view>
		</view>
	</view>
</template>

<script setup>
	import { ref, watch } from 'vue'
	import { getPickupTasks } from '../../api/courier.js'

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
			const data = await getPickupTasks(props.status)
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
			url: `/subPages/pickup-detail/index?id=${item.id}`
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
	.status-1 { background: #e6f7ff; color: #1890ff; }
	.status-2 { background: #f6ffed; color: #52c41a; }
	.status-3 { background: #fff2f0; color: #ff4d4f; }
	.card-body {
		padding: 20rpx 0;
	}
	.info-row {
		display: flex;
		padding: 8rpx 0;
		font-size: 26rpx;
		line-height: 40rpx;
	}
	.label {
		color: #999;
		width: 120rpx;
		flex-shrink: 0;
	}
	.value {
		color: #333;
		flex: 1;
	}
	.card-footer {
		padding-top: 16rpx;
		.time {
			font-size: 24rpx;
			color: #bbb;
		}
	}
</style>
