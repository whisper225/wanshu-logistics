<template>
  <el-dialog
    v-model="visible"
    :title="dialogTitle"
    width="620px"
    destroy-on-close
    @closed="onDialogClosed"
  >
    <el-form ref="formRef" :model="formData" :rules="rules" label-width="120px">
      <el-form-item
        v-if="formData.parentId != null && String(formData.parentId) !== '0'"
        label="上级机构ID"
      >
        <el-input :model-value="String(formData.parentId)" disabled />
      </el-form-item>
      <el-form-item label="机构名称" prop="organName">
        <el-input v-model="formData.organName" maxlength="100" show-word-limit />
      </el-form-item>
      <el-form-item label="机构类型" prop="organType">
        <el-select v-model="formData.organType" placeholder="请选择" style="width: 100%">
          <el-option label="一级转运中心 (OLT)" :value="1" />
          <el-option label="二级分拣中心 (TLT)" :value="2" />
          <el-option label="营业部 (AGENCY)" :value="3" />
        </el-select>
      </el-form-item>
      <el-form-item label="省 / 市 / 区 ID">
        <el-input-number v-model="formData.provinceId" :controls="false" placeholder="省" class="id-inline" />
        <el-input-number v-model="formData.cityId" :controls="false" placeholder="市" class="id-inline" />
        <el-input-number v-model="formData.countyId" :controls="false" placeholder="区" class="id-inline" />
      </el-form-item>
      <el-form-item label="详细地址">
        <el-input v-model="addressStr" type="textarea" :rows="2" maxlength="500" show-word-limit />
      </el-form-item>
      <el-form-item label="经度">
        <el-input-number v-model="formData.longitude" :precision="6" :step="0.000001" style="width: 100%" />
      </el-form-item>
      <el-form-item label="纬度">
        <el-input-number v-model="formData.latitude" :precision="6" :step="0.000001" style="width: 100%" />
      </el-form-item>
      <el-form-item label="负责人">
        <el-input v-model="formData.managerName" maxlength="20" />
      </el-form-item>
      <el-form-item label="负责人电话">
        <el-input v-model="formData.managerPhone" maxlength="20" />
      </el-form-item>
      <el-form-item label="对接人">
        <el-input v-model="formData.contactName" maxlength="20" />
      </el-form-item>
      <el-form-item label="对接人电话">
        <el-input v-model="formData.contactPhone" maxlength="20" />
      </el-form-item>
      <el-form-item label="排序">
        <el-input-number v-model="formData.sortOrder" :min="0" :max="999999" />
      </el-form-item>
      <el-form-item label="状态">
        <el-radio-group v-model="formData.status">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">停用</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { organizationApi, type BaseOrganPayload, type OrganTableRow } from '@/api/organization'
import { emptyOrganForm, toApiParentId } from '@/utils/organization'

const emit = defineEmits<{
  success: []
}>()

const visible = ref(false)
const submitting = ref(false)
const dialogMode = ref<'create-top' | 'create-child' | 'edit'>('create-top')
const formRef = ref<FormInstance>()
const formData = reactive<BaseOrganPayload>(emptyOrganForm(0))

const addressStr = computed({
  get: () => formData.address ?? '',
  set: (v: string) => {
    formData.address = v
  }
})

const dialogTitle = computed(() => {
  if (dialogMode.value === 'edit') return '编辑机构'
  if (dialogMode.value === 'create-child') return '新增下级机构'
  return '新增顶级机构'
})

const rules: FormRules = {
  organName: [{ required: true, message: '请输入机构名称', trigger: 'blur' }],
  organType: [{ required: true, message: '请选择机构类型', trigger: 'change' }]
}

function onDialogClosed() {
  formRef.value?.resetFields()
}

function openCreateTop() {
  dialogMode.value = 'create-top'
  Object.assign(formData, emptyOrganForm(0))
  delete (formData as { id?: string }).id
  visible.value = true
}

function openCreateChild(parentId: string | number) {
  dialogMode.value = 'create-child'
  Object.assign(formData, emptyOrganForm(String(parentId)))
  delete (formData as { id?: string }).id
  visible.value = true
}

async function openEdit(row: OrganTableRow) {
  dialogMode.value = 'edit'
  try {
    const detail = await organizationApi.getDetail(String(row.id))
    Object.assign(formData, {
      ...emptyOrganForm(0),
      ...detail,
      organName: detail.organName ?? '',
      address: detail.address ?? ''
    })
    visible.value = true
  } catch {
    /* request 拦截器已提示 */
  }
}

async function openEditById(id: string) {
  dialogMode.value = 'edit'
  try {
    const detail = await organizationApi.getDetail(id)
    Object.assign(formData, {
      ...emptyOrganForm(0),
      ...detail,
      organName: detail.organName ?? '',
      address: detail.address ?? ''
    })
    visible.value = true
  } catch {
    /* request 拦截器已提示 */
  }
}

async function handleSubmit() {
  await formRef.value?.validate().catch(() => Promise.reject())
  submitting.value = true
  try {
    const payload: BaseOrganPayload = {
      parentId: toApiParentId(formData.parentId),
      organName: formData.organName.trim(),
      organType: formData.organType,
      provinceId: formData.provinceId ?? null,
      cityId: formData.cityId ?? null,
      countyId: formData.countyId ?? null,
      address: formData.address?.trim() || null,
      longitude: formData.longitude ?? null,
      latitude: formData.latitude ?? null,
      managerName: formData.managerName || null,
      managerPhone: formData.managerPhone || null,
      contactName: formData.contactName || null,
      contactPhone: formData.contactPhone || null,
      sortOrder: formData.sortOrder ?? 0,
      status: formData.status ?? 1
    }

    if (dialogMode.value === 'edit' && formData.id != null) {
      await organizationApi.update(String(formData.id), { ...payload, id: formData.id })
      ElMessage.success('保存成功')
    } else {
      await organizationApi.create(payload)
      ElMessage.success('新增成功')
    }
    visible.value = false
    emit('success')
  } finally {
    submitting.value = false
  }
}

defineExpose({
  openCreateTop,
  openCreateChild,
  openEdit,
  openEditById
})
</script>

<style scoped lang="scss">
.id-inline {
  width: 30%;
  margin-right: 8px;
}
</style>
