<template>
  <el-form ref="formRef" :model="localModel" :label-width="labelWidth || '80px'" :label-position="labelPosition" style="width: 100%">
    <!-- grid 布局 -->
    <el-row v-if="typeof layout === 'object' && 'cols' in layout" :gutter="layout.gap || 16" style="width: 100%">
      <template v-for="(field, index) in fields" :key="field.prop || index">
        <el-col :span="field.span || (24 / layout.cols)">
          <el-form-item :label="field.label" :prop="field.prop" :rules="field.rules">
            <slot v-if="field.type === 'slot'" :name="field.slotName" :value="localModel[field.prop]" :update="(v: any) => setFieldValue(field, v)" />
            <render-field v-else :field="field" :model-value="localModel[field.prop]" @update:model-value="(v: any) => setModelField(field, v)" @input-change="handleChange(field, localModel[field.prop])" />
          </el-form-item>
        </el-col>
      </template>
    </el-row>

    <!-- single/double 布局 -->
    <template v-else>
      <template v-for="(field, index) in fields" :key="field.prop || index">
        <!-- double 布局：每行两个 -->
        <el-row v-if="layout === 'double' && index % 2 === 0" :gutter="16" style="width: 100%">
          <el-col :span="field.span || 12">
            <el-form-item :label="field.label" :prop="field.prop" :rules="field.rules">
              <slot v-if="field.type === 'slot'" :name="field.slotName" :value="localModel[field.prop]" :update="(v: any) => setFieldValue(field, v)" />
              <render-field v-else :field="field" :model-value="localModel[field.prop]" @update:model-value="(v: any) => setModelField(field, v)" @input-change="handleChange(field, localModel[field.prop])" />
            </el-form-item>
          </el-col>
          <el-col v-if="fields[index + 1]" :span="fields[index + 1].span || 12">
            <el-form-item :label="fields[index + 1].label" :prop="fields[index + 1].prop" :rules="fields[index + 1].rules">
              <slot v-if="fields[index + 1].type === 'slot'" :name="fields[index + 1].slotName" :value="localModel[fields[index + 1].prop]" :update="(v: any) => setFieldValue(fields[index + 1], v)" />
              <render-field v-else :field="fields[index + 1]" :model-value="localModel[fields[index + 1].prop]" @update:model-value="(v: any) => setModelField(fields[index + 1], v)" @input-change="handleChange(fields[index + 1], localModel[fields[index + 1].prop])" />
            </el-form-item>
          </el-col>
        </el-row>
        <!-- single 布局：每行一个 -->
        <el-form-item v-else-if="layout === 'single' || (layout === 'double' && fields.length % 2 !== 0 && index === fields.length - 1)" :label="field.label" :prop="field.prop" :rules="field.rules">
          <slot v-if="field.type === 'slot'" :name="field.slotName" :value="localModel[field.prop]" :update="(v: any) => setFieldValue(field, v)" />
          <render-field v-else :field="field" :model-value="localModel[field.prop]" @update:model-value="(v: any) => setModelField(field, v)" @input-change="handleChange(field, localModel[field.prop])" />
        </el-form-item>
      </template>
    </template>
  </el-form>
</template>

<script setup lang="ts">
import { reactive, watch, ref, defineComponent, h } from 'vue'
import { ElInput, ElSelect, ElOption, ElTreeSelect, ElSwitch, ElDatePicker, ElRadioGroup, ElRadio, ElCheckboxGroup, ElCheckbox } from 'element-plus'
import type { FormField, FormBuilderProps } from './types'

// --- 内联字段渲染组件 ---
const RenderField = defineComponent({
  props: {
    field: { type: Object as () => FormField, required: true },
    modelValue: { required: true },
  },
  emits: ['update:modelValue', 'inputChange'],
  setup(props, { emit }) {
    return () => {
      const f = props.field
      const v = props.modelValue
      const onInput = (val: any) => {
        emit('update:modelValue', val)
        emit('inputChange', val)
      }
      const common = {
        modelValue: v,
        'onUpdate:modelValue': onInput,
        placeholder: f.placeholder,
        disabled: f.disabled,
        ...f.props,
      }

      switch (f.type) {
        case 'input':
          return h(ElInput, common)
        case 'textarea':
          return h(ElInput, { ...common, type: 'textarea' })
        case 'select':
          return h(ElSelect, common, () =>
            (f.options || []).map((opt) =>
              h(ElOption, { key: String(opt.value), label: opt.label, value: opt.value }),
            ),
          )
        case 'tree-select':
          return h(ElTreeSelect, {
            ...common,
            ...f.treeProps,
            checkStrictly: true,
          })
        case 'switch':
          return h(ElSwitch, common)
        case 'date-picker':
          return h(ElDatePicker, common)
        case 'radio':
          return h(
            ElRadioGroup,
            common,
            () =>
              (f.options || []).map((opt) =>
                h(ElRadio, { key: String(opt.value), value: opt.value }, () => opt.label),
              ),
          )
        case 'checkbox':
          return h(
            ElCheckboxGroup,
            common,
            () =>
              (f.options || []).map((opt) =>
                h(ElCheckbox, { key: String(opt.value), value: opt.value }, () => opt.label),
              ),
          )
        default:
          return null
      }
    }
  },
})

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
    // 先清除旧 key，再写入新值，避免残留
    const oldKeys = Object.keys(localModel)
    const newKeys = Object.keys(val)
    for (const key of oldKeys) {
      if (!(key in val)) {
        delete localModel[key]
      }
    }
    Object.assign(localModel, val)
    // 记录初始值
    for (const key of newKeys) {
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

function setModelField(field: FormField, val: any) {
  oldValues.value[field.prop] = localModel[field.prop]
  localModel[field.prop] = val
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
</script>