<template>
  <view class="page">
    <!-- 搜索栏 -->
    <view class="search-bar">
      <view class="search-wrap">
        <text class="search-icon">🔍</text>
        <input
          class="search-input"
          v-model="keyword"
          placeholder="搜索姓名、电话、地址"
          @input="onSearchInput"
        />
        <text v-if="keyword" class="clear-btn" @click="clearSearch">✕</text>
      </view>
      <text v-if="multiSelect" class="cancel-select" @click="exitMultiSelect">取消</text>
    </view>

    <!-- 加载中 -->
    <view v-if="loading" class="center-tip">加载中...</view>

    <!-- 空状态 -->
    <view v-else-if="list.length === 0" class="empty">
      <text class="empty-icon">📖</text>
      <text class="empty-text">暂无地址，点击右下角添加</text>
    </view>

    <!-- 列表 -->
    <view v-else class="list">
      <view
        v-for="item in list"
        :key="item.id"
        class="address-card"
        @click="onCardClick(item)"
        @longpress="enterMultiSelect(item)"
      >
        <!-- 多选复选框 -->
        <view v-if="multiSelect" class="checkbox-wrap" @click.stop="toggleSelect(item)">
          <view :class="['checkbox', item.id != null && selectedIds.includes(item.id) && 'checked']">
            <text v-if="item.id != null && selectedIds.includes(item.id)" class="check-mark">✓</text>
          </view>
        </view>

        <view class="card-content">
          <view class="card-top">
            <text class="contact-name">{{ item.name }}</text>
            <text class="contact-phone">{{ item.phone }}</text>
            <view v-if="item.isDefault === 1" class="default-badge">默认</view>
          </view>
          <text class="card-addr">{{ item.address }}</text>

          <view class="card-actions" v-if="!multiSelect">
            <text
              v-if="item.isDefault !== 1"
              class="action-link"
              @click.stop="onSetDefault(item)"
            >设为默认</text>
            <text class="action-divider" v-if="item.isDefault !== 1">|</text>
            <text class="action-link" @click.stop="onEdit(item)">编辑</text>
            <text class="action-divider">|</text>
            <text class="action-link danger" @click.stop="onDelete(item)">删除</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 批量操作栏 -->
    <view v-if="multiSelect" class="batch-bar">
      <view class="batch-select-all" @click="toggleSelectAll">
        <view :class="['checkbox', isAllSelected && 'checked']">
          <text v-if="isAllSelected" class="check-mark">✓</text>
        </view>
        <text>全选</text>
      </view>
      <button
        class="batch-del-btn"
        :disabled="selectedIds.length === 0"
        @click="onBatchDelete"
      >删除({{ selectedIds.length }})</button>
    </view>

    <!-- 添加按钮 -->
    <view v-if="!multiSelect" class="fab" @click="onAdd">
      <text class="fab-icon">+</text>
    </view>

    <!-- 新增/编辑弹窗 -->
    <view v-if="showForm" class="overlay" @click.self="closeForm">
      <view class="form-panel">
        <view class="form-header">
          <text class="form-title">{{ editTarget ? '编辑地址' : '新增地址' }}</text>
          <text class="form-close" @click="closeForm">✕</text>
        </view>
        <scroll-view scroll-y class="form-body">
          <view class="form-row">
            <text class="form-label required">姓名</text>
            <input class="form-input" v-model="formData.name" placeholder="联系人姓名" maxlength="20" />
          </view>
          <view class="form-row">
            <text class="form-label required">电话</text>
            <input class="form-input" v-model="formData.phone" placeholder="手机号码" type="number" maxlength="11" />
          </view>
          <view class="form-row">
            <text class="form-label required">详细地址</text>
            <textarea
              class="form-textarea"
              v-model="formData.address"
              placeholder="省市区 + 详细街道地址"
              maxlength="200"
              auto-height
            />
          </view>
          <view class="form-row form-row-switch">
            <text class="form-label">设为默认地址</text>
            <switch
              :checked="formData.isDefault === 1"
              color="#1890ff"
              @change="onDefaultChange"
            />
          </view>
        </scroll-view>
        <view class="form-footer">
          <button class="form-cancel" @click="closeForm">取消</button>
          <button class="form-submit" @click="onFormSubmit" :loading="saving">保存</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script lang="ts">
import { defineComponent } from '@vue/runtime-core'
import {
  listAddress,
  createAddress,
  updateAddress,
  deleteAddress,
  deleteAddressBatch,
  setDefaultAddress,
} from '@/api/user'
import type { AddressBook } from '@/api/user'

export default defineComponent({
  name: 'AddressPage',
  data() {
    return {
      keyword: '',
      loading: false,
      list: [] as AddressBook[],
      multiSelect: false,
      selectedIds: [] as number[],
      showForm: false,
      editTarget: null as AddressBook | null,
      saving: false,
      searchTimer: null as ReturnType<typeof setTimeout> | null,
      formData: {
        name: '',
        phone: '',
        address: '',
        isDefault: 0,
      } as AddressBook,
    }
  },
  computed: {
    isAllSelected(): boolean {
      return this.list.length > 0 && this.selectedIds.length === this.list.length
    },
  },
  mounted() {
    this.loadList()
  },
  methods: {
    async loadList() {
      this.loading = true
      try {
        this.list = await listAddress(this.keyword || undefined)
      } catch (e: unknown) {
        const err = e as { message?: string }
        uni.showToast({ title: err.message || '加载失败', icon: 'none' })
      } finally {
        this.loading = false
      }
    },
    onSearchInput() {
      if (this.searchTimer) clearTimeout(this.searchTimer)
      this.searchTimer = setTimeout(() => this.loadList(), 400)
    },
    clearSearch() {
      this.keyword = ''
      this.loadList()
    },
    onCardClick(item: AddressBook) {
      if (this.multiSelect) {
        this.toggleSelect(item)
      }
    },
    enterMultiSelect(item: AddressBook) {
      this.multiSelect = true
      this.selectedIds = item.id ? [item.id] : []
    },
    exitMultiSelect() {
      this.multiSelect = false
      this.selectedIds = []
    },
    toggleSelect(item: AddressBook) {
      if (!item.id) return
      const idx = this.selectedIds.indexOf(item.id)
      if (idx >= 0) {
        this.selectedIds.splice(idx, 1)
      } else {
        this.selectedIds.push(item.id)
      }
    },
    toggleSelectAll() {
      if (this.isAllSelected) {
        this.selectedIds = []
      } else {
        this.selectedIds = this.list.map((a: AddressBook) => a.id as number).filter(Boolean)
      }
    },
    async onSetDefault(item: AddressBook) {
      if (!item.id) return
      try {
        await setDefaultAddress(item.id)
        uni.showToast({ title: '已设为默认', icon: 'success' })
        this.loadList()
      } catch (e: unknown) {
        const err = e as { message?: string }
        uni.showToast({ title: err.message || '操作失败', icon: 'none' })
      }
    },
    onAdd() {
      this.editTarget = null
      this.formData = { name: '', phone: '', address: '', isDefault: 0 }
      this.showForm = true
    },
    onEdit(item: AddressBook) {
      this.editTarget = item
      this.formData = {
        name: item.name,
        phone: item.phone,
        address: item.address,
        isDefault: item.isDefault ?? 0,
      }
      this.showForm = true
    },
    closeForm() {
      this.showForm = false
      this.editTarget = null
    },
    onDefaultChange(e: { detail: { value: boolean } }) {
      this.formData.isDefault = e.detail.value ? 1 : 0
    },
    async onFormSubmit() {
      if (!this.formData.name.trim()) {
        uni.showToast({ title: '请输入联系人姓名', icon: 'none' })
        return
      }
      if (!this.formData.phone || this.formData.phone.length < 11) {
        uni.showToast({ title: '请输入有效手机号', icon: 'none' })
        return
      }
      if (!this.formData.address.trim()) {
        uni.showToast({ title: '请输入详细地址', icon: 'none' })
        return
      }
      this.saving = true
      try {
        if (this.editTarget && this.editTarget.id) {
          await updateAddress(this.editTarget.id, { ...this.formData })
        } else {
          await createAddress({ ...this.formData })
        }
        uni.showToast({ title: '保存成功', icon: 'success' })
        this.closeForm()
        this.loadList()
      } catch (e: unknown) {
        const err = e as { message?: string }
        uni.showToast({ title: err.message || '保存失败', icon: 'none' })
      } finally {
        this.saving = false
      }
    },
    onDelete(item: AddressBook) {
      uni.showModal({
        title: '确认删除',
        content: `删除「${item.name}」的地址？`,
        success: async ({ confirm }: { confirm: boolean }) => {
          if (!confirm) return
          try {
            await deleteAddress(item.id as number)
            uni.showToast({ title: '已删除', icon: 'success' })
            this.loadList()
          } catch (e: unknown) {
            const err = e as { message?: string }
            uni.showToast({ title: err.message || '删除失败', icon: 'none' })
          }
        },
      })
    },
    async onBatchDelete() {
      if (this.selectedIds.length === 0) return
      uni.showModal({
        title: '批量删除',
        content: `确认删除选中的 ${this.selectedIds.length} 条地址？`,
        success: async ({ confirm }: { confirm: boolean }) => {
          if (!confirm) return
          try {
            await deleteAddressBatch([...this.selectedIds])
            uni.showToast({ title: '删除成功', icon: 'success' })
            this.exitMultiSelect()
            this.loadList()
          } catch (e: unknown) {
            const err = e as { message?: string }
            uni.showToast({ title: err.message || '删除失败', icon: 'none' })
          }
        },
      })
    },
  },
})
</script>

<style scoped lang="scss">
.page {
  background: #f5f5f5;
  min-height: 100vh;
  padding-bottom: 160rpx;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 16rpx;
  background: #fff;
  padding: 20rpx 24rpx;
}

.search-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  background: #f5f5f5;
  border-radius: 40rpx;
  padding: 12rpx 24rpx;
  gap: 12rpx;
}

.search-icon { font-size: 28rpx; }

.search-input {
  flex: 1;
  font-size: 28rpx;
  color: #333;
  background: transparent;
}

.clear-btn {
  color: #bbb;
  font-size: 28rpx;
}

.cancel-select {
  font-size: 28rpx;
  color: #1890ff;
}

.center-tip {
  text-align: center;
  padding: 80rpx;
  color: #bbb;
  font-size: 28rpx;
}

.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 120rpx 40rpx;
}

.empty-icon {
  font-size: 80rpx;
  margin-bottom: 24rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #bbb;
}

.list {
  padding: 16rpx 24rpx;
}

.address-card {
  background: #fff;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
  padding: 24rpx;
  display: flex;
  align-items: flex-start;
  gap: 20rpx;
}

.checkbox-wrap {
  padding-top: 4rpx;
}

.checkbox {
  width: 44rpx;
  height: 44rpx;
  border-radius: 50%;
  border: 2rpx solid #d9d9d9;
  display: flex;
  align-items: center;
  justify-content: center;
  &.checked {
    background: #1890ff;
    border-color: #1890ff;
  }
}

.check-mark {
  color: #fff;
  font-size: 24rpx;
  line-height: 1;
}

.card-content {
  flex: 1;
}

.card-top {
  display: flex;
  align-items: center;
  gap: 16rpx;
  margin-bottom: 12rpx;
}

.contact-name {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
}

.contact-phone {
  font-size: 26rpx;
  color: #666;
}

.default-badge {
  font-size: 22rpx;
  color: #1890ff;
  border: 1rpx solid #1890ff;
  padding: 2rpx 10rpx;
  border-radius: 4rpx;
}

.card-addr {
  font-size: 26rpx;
  color: #888;
  line-height: 1.6;
  margin-bottom: 16rpx;
}

.card-actions {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.action-link {
  font-size: 24rpx;
  color: #1890ff;
  &.danger { color: #ff4d4f; }
}

.action-divider {
  font-size: 24rpx;
  color: #e8e8e8;
}

.batch-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: 20rpx 32rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  display: flex;
  align-items: center;
  box-shadow: 0 -2rpx 12rpx rgba(0,0,0,0.06);
}

.batch-select-all {
  display: flex;
  align-items: center;
  gap: 12rpx;
  flex: 1;
  font-size: 28rpx;
  color: #333;
}

.batch-del-btn {
  background: #ff4d4f;
  color: #fff;
  border-radius: 40rpx;
  font-size: 28rpx;
  padding: 0 40rpx;
  height: 72rpx;
  line-height: 72rpx;
  border: none;
  &[disabled] { background: #ffa39e; }
}

.fab {
  position: fixed;
  bottom: calc(48rpx + env(safe-area-inset-bottom));
  right: 48rpx;
  width: 96rpx;
  height: 96rpx;
  background: #1890ff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8rpx 24rpx rgba(24,144,255,0.4);
}

.fab-icon {
  color: #fff;
  font-size: 56rpx;
  line-height: 1;
  margin-top: -4rpx;
}

.overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.5);
  z-index: 100;
  display: flex;
  align-items: flex-end;
}

.form-panel {
  background: #fff;
  border-radius: 24rpx 24rpx 0 0;
  width: 100%;
  max-height: 85vh;
  display: flex;
  flex-direction: column;
}

.form-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 32rpx 32rpx 24rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.form-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}

.form-close {
  font-size: 40rpx;
  color: #999;
}

.form-body {
  flex: 1;
  padding: 0 32rpx;
  overflow-y: auto;
}

.form-row {
  display: flex;
  align-items: flex-start;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f5f5f5;
  gap: 16rpx;
}

.form-row-switch {
  align-items: center;
}

.form-label {
  width: 150rpx;
  font-size: 28rpx;
  color: #333;
  flex-shrink: 0;
  padding-top: 8rpx;
  &.required::before {
    content: '*';
    color: #ff4d4f;
    margin-right: 4rpx;
  }
}

.form-input {
  flex: 1;
  font-size: 28rpx;
  color: #333;
  padding: 8rpx 0;
}

.form-textarea {
  flex: 1;
  font-size: 28rpx;
  color: #333;
  min-height: 100rpx;
  padding: 8rpx 0;
}

.form-footer {
  display: flex;
  gap: 24rpx;
  padding: 24rpx 32rpx;
  padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
  border-top: 1rpx solid #f0f0f0;
}

.form-cancel,
.form-submit {
  flex: 1;
  height: 80rpx;
  border-radius: 40rpx;
  font-size: 28rpx;
  border: none;
}

.form-cancel {
  background: #f5f5f5;
  color: #666;
}

.form-submit {
  background: #1890ff;
  color: #fff;
}
</style>
