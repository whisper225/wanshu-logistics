<template>
	<view class="register-page">
		<NavBar title="回车登记"></NavBar>

		<view class="content">
			<view class="card">
				<view class="card-title">情况说明</view>
				<textarea
					class="desc-input"
					v-model="description"
					placeholder="请输入运输情况说明（选填）"
					:maxlength="500"
				/>
			</view>

			<view class="card">
				<view class="card-title">现场照片</view>
				<view class="image-upload">
					<view class="image-list">
						<view
							class="image-item"
							v-for="(img, idx) in imageList"
							:key="idx"
						>
							<image :src="img" mode="aspectFill" class="preview-img" />
							<view class="remove-btn" @click="removeImage(idx)">×</view>
						</view>
						<view class="add-btn" @click="chooseImage" v-if="imageList.length < 9">
							<text class="add-icon">+</text>
							<text class="add-text">添加照片</text>
						</view>
					</view>
				</view>
			</view>
		</view>

		<view class="footer">
			<button class="btn-submit" :loading="submitting" @click="handleSubmit">提交登记</button>
		</view>
	</view>
</template>

<script setup>
	import { ref } from 'vue'
	import { onLoad } from '@dcloudio/uni-app'
	import NavBar from '@/components/Navbar/index.vue'
	import { createReturnRegister } from '@/pages/api/driver.js'

	const description = ref('')
	const imageList = ref([])
	const submitting = ref(false)
	let taskId = null

	onLoad((options) => {
		taskId = options.taskId
	})

	const chooseImage = () => {
		uni.chooseImage({
			count: 9 - imageList.value.length,
			sizeType: ['compressed'],
			sourceType: ['album', 'camera'],
			success: (res) => {
				imageList.value = [...imageList.value, ...res.tempFilePaths]
			}
		})
	}

	const removeImage = (idx) => {
		imageList.value.splice(idx, 1)
	}

	const handleSubmit = async () => {
		if (submitting.value) return
		submitting.value = true
		try {
			const images = imageList.value.length ? imageList.value.join(',') : undefined
			await createReturnRegister(taskId, description.value || undefined, images)
			uni.showToast({ title: '登记成功', icon: 'success' })
			setTimeout(() => uni.navigateBack(), 1500)
		} catch (e) {
			uni.showToast({ title: e.message || '提交失败', icon: 'none' })
		} finally {
			submitting.value = false
		}
	}
</script>

<style lang="scss" scoped>
	.register-page {
		min-height: 100vh;
		background: #f3f5f9;
		padding-bottom: 160rpx;
	}
	.content {
		padding: 20rpx 28rpx;
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
	.desc-input {
		width: 100%;
		height: 240rpx;
		font-size: 26rpx;
		padding: 16rpx;
		box-sizing: border-box;
		background: #f9f9f9;
		border-radius: 12rpx;
	}
	.image-list {
		display: flex;
		flex-wrap: wrap;
		gap: 16rpx;
	}
	.image-item {
		position: relative;
		width: 200rpx;
		height: 200rpx;
	}
	.preview-img {
		width: 200rpx;
		height: 200rpx;
		border-radius: 12rpx;
	}
	.remove-btn {
		position: absolute;
		top: -12rpx;
		right: -12rpx;
		width: 40rpx;
		height: 40rpx;
		line-height: 36rpx;
		text-align: center;
		background: rgba(0,0,0,0.5);
		color: #fff;
		border-radius: 50%;
		font-size: 28rpx;
	}
	.add-btn {
		width: 200rpx;
		height: 200rpx;
		border: 2rpx dashed #ddd;
		border-radius: 12rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
	}
	.add-icon {
		font-size: 48rpx;
		color: #ccc;
	}
	.add-text {
		font-size: 22rpx;
		color: #999;
		margin-top: 8rpx;
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
	.btn-submit {
		height: 88rpx;
		line-height: 88rpx;
		background: var(--essential-color-red);
		color: #fff;
		border-radius: 44rpx;
		font-size: 32rpx;
		text-align: center;
	}
</style>
