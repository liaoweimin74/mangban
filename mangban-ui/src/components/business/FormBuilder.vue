<template>
  <el-form ref="formRef" :model="localModel" :label-width="labelWidth || '80px'" :label-position="labelPosition">
    <template v-for="(field, index) in fields" :key="field.prop || index">
      <el-form-item :label="field.label" :prop="field.prop" :rules="field.rules">
        <!-- input -->
        <el-input
          v-if="field.type === 'input'"
          v-model="localModel[field.prop]"
          v-bind="field.props"
          :placeholder="field.placeholder"
          :disabled="field.disabled"
          @change="handleChange(field, localModel[field.prop])"
        />
        <!-- textarea -->
        <el-input
          v-else-if="field.type === 'textarea'"
          v-model="localModel[field.prop]"
          v-bind="field.props"
          type="textarea"
          :placeholder="field.placeholder"
          :disabled="field.disabled"
          @change="handleChange(field, localModel[field.prop])"
        />
        <!-- select -->
        <el-select
          v-else-if="field.type === 'select'"
          v-model="localModel[field.prop]"
          v-bind="field.props"
          :placeholder="field.placeholder"
          :disabled="field.disabled"
          @change="handleChange(field, localModel[field.prop])"
        >
          <el-option
            v-for="opt in field.options"
            :key="String(opt.value)"
            :label="opt.label"
            :value="opt.value"
          />
        </el-select>
        <!-- tree-select -->
        <el-tree-select
          v-else-if="field.type === 'tree-select'"
          v-model="localModel[field.prop]"
          v-bind="{ ...field.treeProps, ...field.props }"
          :placeholder="field.placeholder"
          :disabled="field.disabled"
          check-strictly
          @change="handleChange(field, localModel[field.prop])"
        />
        <!-- switch -->
        <el-switch
          v-else-if="field.type === 'switch'"
          v-model="localModel[field.prop]"
          v-bind="field.props"
          :disabled="field.disabled"
          @change="handleChange(field, localModel[field.prop])"
        />
        <!-- date-picker -->
        <el-date-picker
          v-else-if="field.type === 'date-picker'"
          v-model="localModel[field.prop]"
          v-bind="field.props"
          :placeholder="field.placeholder"
          :disabled="field.disabled"
          @change="handleChange(field, localModel[field.prop])"
        />
        <!-- radio -->
        <el-radio-group
          v-else-if="field.type === 'radio'"
          v-model="localModel[field.prop]"
          v-bind="field.props"
          :disabled="field.disabled"
          @change="handleChange(field, localModel[field.prop])"
        >
          <el-radio
            v-for="opt in field.options"
            :key="String(opt.value)"
            :value="opt.value"
          >
            {{ opt.label }}
          </el-radio>
        </el-radio-group>
        <!-- checkbox -->
        <el-checkbox-group
          v-else-if="field.type === 'checkbox'"
          v-model="localModel[field.prop]"
          v-bind="field.props"
          :disabled="field.disabled"
          @change="handleChange(field, localModel[field.prop])"
        >
          <el-checkbox
            v-for="opt in field.options"
            :key="String(opt.value)"
            :value="opt.value"
          >
            {{ opt.label }}
          </el-checkbox>
        </el-checkbox-group>
        <!-- slot -->
        <slot
          v-else-if="field.type === 'slot'"
          :name="field.slotName"
          :value="localModel[field.prop]"
          :update="(v: any) => setFieldValue(field, v)"
        />
      </el-form-item>
    </template>
  </el-form>
</template>

<script setup lang="ts">
import { reactive, watch } from 'vue'
import type { FormField, FormBuilderProps } from './types'

const props = withDefaults(defineProps<FormBuilderProps>(), {
  layout: 'single',
  labelWidth: '80px',
})

const emit = defineEmits<{
  'update:modelValue': [value: Record<string, any>]
}>()

const formRef = ref()
const localModel = reactive<Record<string, any>>({ ...props.modelValue })
const oldValues = ref<Record<string, any>>({})

// 初始化 oldValues
watch(
  () => props.modelValue,
  (val) => {
    Object.assign(localModel, val)
    // 记录初始值
    for (const key of Object.keys(val)) {
      if (!(key in oldValues.value)) {
        oldValues.value[key] = val[key]
      }
    }
  },
  { immediate: true, deep: true },
)

// 同步 localModel 变化到父组件
watch(
  localModel,
  (val) => {
    emit('update:modelValue', { ...val })
  },
  { deep: true },
)

async function handleChange(field: FormField, newVal: any) {
  if (!field.onChange) return
  const oldVal = oldValues.value[field.prop]
  const ok = await field.onChange(newVal, oldVal, { ...localModel })
  if (ok === false) {
    localModel[field.prop] = oldVal
  } else {
    oldValues.value[field.prop] = newVal
  }
}

function setFieldValue(field: FormField, val: any) {
  oldValues.value[field.prop] = localModel[field.prop]
  localModel[field.prop] = val
}

async function validate(): Promise<boolean> {
  return new Promise((resolve) => {
    formRef.value?.validate((valid: boolean) => {
      resolve(valid)
    })
  })
}

function validateField(prop: string) {
  return formRef.value?.validateField(prop)
}

function resetFields() {
  formRef.value?.resetFields()
}

function clearValidate(props?: string | string[]) {
  formRef.value?.clearValidate(props)
}

defineExpose({ validate, validateField, resetFields, clearValidate })

// ref 需要从 vue 导入
import { ref } from 'vue'
</script>