<template>
  <div class="app-container">
    <el-dialog v-model="dialogVisible" @open="onOpen" @close="onClose" title="Dialog Titile">
      <el-form ref="formRef" :model="formData" :rules="rules" size="default" label-width="100px">
        <el-form-item label="不良项名称" prop="field102">
          <el-input v-model="formData.field102" type="text" placeholder="请输入不良项名称" clearable
            :style="{width: '100%'}"></el-input>
        </el-form-item>
        <el-form-item label="不良项颜色" prop="field101" required>
          <el-color-picker v-model="formData.field101" size="large"></el-color-picker>
        </el-form-item>
        <el-form-item label="排序" prop="field104">
          <el-select v-model="formData.field104" placeholder="请选择排序" clearable :style="{width: '100%'}">
            <el-option v-for="(item, index) in field104Options" :key="index" :label="item.label"
              :value="item.value" :disabled="item.disabled"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="启用状态" prop="field105" required>
          <el-switch v-model="formData.field105" active-color="#148F12"></el-switch>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="close">取消</el-button>
        <el-button type="primary" @click="handelConfirm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
const {
  proxy
} = getCurrentInstance()
const formRef = ref()
const data = reactive({
  formData: {
    field102: undefined,
    field101: null,
    field104: undefined,
    field105: false,
  },
  rules: {
    field102: [{
      required: true,
      message: '请输入不良项名称',
      trigger: 'blur'
    }],
    field104: [{
      required: true,
      message: '请选择排序',
      trigger: 'change'
    }],
  }
})
const {
  formData,
  rules
} = toRefs(data)
const field104Options = ref([{
  "label": "1",
  "value": 1
}, {
  "label": "2",
  "value": 2
}, {
  "label": "3",
  "value": 3
}, {
  "label": "4",
  "value": 4
}, {
  "label": "5",
  "value": 5
}])
// 弹窗设置
const dialogVisible = defineModel()
// 弹窗确认回调
const emit = defineEmits(['confirm'])
/**
 * @name: 弹窗打开后执行
 * @description: 弹窗打开后执行方法
 * @return {*}
 */
function onOpen() {}
/**
 * @name: 弹窗关闭时执行
 * @description: 弹窗关闭方法，重置表单
 * @return {*}
 */
function onClose() {
  formRef.value.resetFields()
}
/**
 * @name: 弹窗取消
 * @description: 弹窗取消方法
 * @return {*}
 */
function close() {
  dialogVisible.value = false
}
/**
 * @name: 弹窗表单提交
 * @description: 弹窗表单提交方法
 * @return {*}
 */
function handelConfirm() {
  formRef.value.validate((valid) => {
    if (!valid) return
    // TODO 提交表单
    close()
    // 回调父级组件
    emit('confirm')
  })
}
</script>
<style>
</style>
