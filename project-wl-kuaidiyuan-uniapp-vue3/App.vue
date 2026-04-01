<script setup>
import { onLaunch } from '@dcloudio/uni-app'

onLaunch(() => {
  const token = uni.getStorageSync('token')
  if (!token) {
    setTimeout(() => {
      uni.reLaunch({ url: '/pages/login/index' })
    }, 100)
  }
})
</script>

<style lang="scss">
@import '@/uni_modules/uni-scss/index.scss';
/* #ifndef APP-NVUE */
@import '@/styles/common.scss';
page {
	background-color: #f5f5f5;
}
/* #endif */
</style>
