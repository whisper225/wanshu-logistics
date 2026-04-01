<template>
	<view class="delivery-page">
		<view class="header">
			<view class="header-title" :style="{'paddingTop': capsuleBottom + 'px'}">
				<text class="title-text">派件任务</text>
			</view>
			<view class="tab-bar">
				<view
					v-for="(tab, idx) in tabs"
					:key="tab.value"
					class="tab-item"
					:class="{ active: currentTab === idx }"
					@click="switchTab(idx)"
				>
					<text>{{ tab.label }}</text>
					<text class="tab-count" v-if="tabCounts[idx]">({{ tabCounts[idx] }})</text>
					<view class="tab-line" v-if="currentTab === idx"></view>
				</view>
			</view>
		</view>
		<view class="list-wrap" :style="{'paddingTop': listPadTop + 'px'}">
			<TaskList ref="taskListRef" :status="tabs[currentTab].value" @refresh-done="onRefreshDone" />
		</view>
	</view>
</template>

<script setup>
	import { ref, computed } from 'vue'
	import { onShow, onLoad, onPullDownRefresh } from '@dcloudio/uni-app'
	import TaskList from './components/taskList.vue'

	const tabs = [
		{ label: '全部', value: undefined },
		{ label: '已分配', value: 1 },
		{ label: '已签收', value: 2 },
		{ label: '已拒收', value: 3 }
	]
	const currentTab = ref(0)
	const tabCounts = ref([0, 0, 0, 0])
	const taskListRef = ref()
	const capsuleBottom = ref(0)
	const listPadTop = computed(() => capsuleBottom.value + 90)

	onLoad(() => {
		uni.getSystemInfo({
			success: (res) => {
				capsuleBottom.value = uni.getMenuButtonBoundingClientRect().bottom
			}
		})
	})

	onShow(() => {
		taskListRef.value && taskListRef.value.loadTasks()
	})

	onPullDownRefresh(() => {
		taskListRef.value && taskListRef.value.loadTasks()
	})

	const switchTab = (idx) => {
		currentTab.value = idx
		taskListRef.value && taskListRef.value.loadTasks()
	}

	const onRefreshDone = () => {
		uni.stopPullDownRefresh()
	}
</script>

<style lang="scss" scoped>
	.delivery-page {
		min-height: 100vh;
		background: #f3f5f9;
	}
	.header {
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		z-index: 10;
		background: #fff;
	}
	.header-title {
		text-align: center;
		padding-bottom: 20rpx;
		.title-text {
			font-size: 36rpx;
			font-weight: bold;
		}
	}
	.tab-bar {
		display: flex;
		padding: 0 28rpx 20rpx;
	}
	.tab-item {
		margin-right: 60rpx;
		font-size: 28rpx;
		color: #888;
		position: relative;
		padding-bottom: 16rpx;
		&.active {
			color: #151515;
			font-weight: bold;
		}
		.tab-count {
			margin-left: 4rpx;
		}
	}
	.tab-line {
		position: absolute;
		bottom: 0;
		left: 0;
		right: 0;
		height: 6rpx;
		background: linear-gradient(210deg, #f25c4d 25%, #e52d21 100%);
		border-radius: 6rpx;
	}
	.list-wrap {
		padding-bottom: 20rpx;
	}
</style>
