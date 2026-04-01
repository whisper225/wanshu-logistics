<template>
	<view class="NavBar" :style="{'height':all}">
		<view class="title" :style="{'paddingTop':capsuleTop+'px',height:capsuleHeight+'px'}">
			{{title}}
			<image :src='src' @click="handleTo" class="navbar-image"></image>
		</view>
	</view>
</template>

<script setup>
	import { ref } from 'vue';
	import { onLoad } from '@dcloudio/uni-app';

	const props = defineProps({
		title: {
			type: String,
			default: '',
		},
		handleToLink: {
			type: Function
		},
		src: {
			type: String,
			default: '../../static/goBack.png',
		}
	})

	let capsuleTop = ref()
	let capsuleBottom = ref()
	let all = ref()
	let capsuleHeight = ref()

	onLoad(() => {
		uni.getSystemInfo({
			success: (res) => {
				const deviceNavHeight = res.statusBarHeight
				capsuleTop.value = uni.getMenuButtonBoundingClientRect().top
				capsuleBottom.value = uni.getMenuButtonBoundingClientRect().bottom
				all.value = (capsuleTop.value + capsuleBottom.value - deviceNavHeight) + 'px'
				capsuleHeight.value = uni.getMenuButtonBoundingClientRect().height
			}
		})
	})

	const handleTo = () => {
		if (props.handleToLink) {
			props.handleToLink()
		} else {
			uni.navigateBack()
		}
	}
</script>

<style src="./index.scss" lang="scss" scoped></style>
