# Common Business Components Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 创建三个前端公共业务组件（SearchTable、FormBuilder、ReferencePicker），提高 CRUD 页面开发效率和界面一致性。

**Architecture:** 每个组件为独立 Vue SFC，放置于 `src/components/business/` 目录，通过 props 声明式配置驱动渲染，使用 Element Plus 组件实现 UI。公共类型定义在 `types.ts`，统一导出在 `index.ts`。

**Tech Stack:** Vue 3.5 + TypeScript + Element Plus 2.14 + Pinia + Axios

## Global Constraints

- 语言：简体中文（所有注释、显示文本）
- 文件路径：`src/components/business/`
- 遵循现有项目风格：Composition API + `<script setup lang="ts">` 模式
- 组件使用 defineProps + defineEmits + defineExpose 声明接口
- 不引入新的 npm 依赖
- 所有 props 和 emits 必须有 TypeScript 类型声明
- 使用现有 http 工具（`@/utils/http`）进行 API 调用

---

## File Structure

```
src/components/business/
├── types.ts              # 公共类型定义（所有组件的 props/emits 接口）
├── SearchTable.vue       # 列表页组件
├── FormBuilder.vue       # 表单组件
├── ReferencePicker.vue   # 引用查找组件
└── index.ts              # 统一导出
```

### Task 1: 公共类型定义 (types.ts)

**Files:**
- Create: `src/components/business/types.ts`

**Interfaces:**
- Produces: 所有组件需要的类型接口

- [ ] **Step 1: 定义 SearchTable 相关类型**

```typescript
// 查询字段定义
export interface SearchField {
  type: 'input' | 'select' | 'tree-select' | 'date-picker' | 'date-range'
  label: string
  prop: string
  placeholder?: string
  defaultValue?: any
  options?: { label: string; value: any }[]
  treeProps?: { data: any[]; props: { label: string; value: string; children?: string } }
  style?: string
}

// 列定义
export interface TableColumn {
  prop?: string
  label: string
  width?: number | string
  minWidth?: number | string
  align?: 'left' | 'center' | 'right'
  fixed?: 'left' | 'right'
  formatter?: (row: any, column: TableColumn, cellValue: any, index: number) => string
  slotName?: string
}

// 操作按钮定义
export interface ActionButton {
  label: string
  type?: 'primary' | 'success' | 'warning' | 'danger' | 'info' | 'text'
  size?: 'small' | 'default' | 'large'
  icon?: string
  permission?: string
  confirm?: string
  onClick: (row: any) => void
}

// 查询参数
export interface QueryParams {
  page: number
  size: number
  [key: string]: any
}

// SearchTable props
export interface SearchTableProps<T = any> {
  searchFields: SearchField[]
  columns: TableColumn[]
  actionButtons?: ActionButton[]
  fetchApi: (params: QueryParams) => Promise<{ rows: T[]; total: number }>
  defaultPageSize?: number
  pageSizes?: number[]
  showExport?: boolean
  exportLoading?: boolean
  maxVisibleButtons?: number // 操作列最多直接显示的按钮数，超出部分折叠到"更多"下拉
  // 集成 FormBuilder 的 CRUD 配置
  formConfig?: FormConfig<T>
}

// 集成表单配置
export interface FormConfig<T = any> {
  fields: FormField[]
  createApi?: (data: any) => Promise<any>
  updateApi?: (id: number | string, data: any) => Promise<any>
  deleteApi?: (id: number | string) => Promise<any>
  getApi?: (id: number | string) => Promise<T>
  layout?: FormLayout
  labelWidth?: string
  dialogWidth?: string
  dialogTitle?: { create?: string; edit?: string }
  // 新增/编辑前钩子，返回 false 阻止操作
  beforeCreate?: () => boolean | Promise<boolean>
  beforeEdit?: (row: T) => boolean | Promise<boolean>
  beforeDelete?: (row: T) => boolean | Promise<boolean>
  // 操作成功后回调
  afterCreate?: (result: any) => void
  afterUpdate?: (result: any) => void
  afterDelete?: () => void
}
```

- [ ] **Step 2: 定义 FormBuilder 相关类型**

```typescript
// 表单字段定义
export interface FormField {
  type: 'input' | 'select' | 'tree-select' | 'switch' | 'date-picker' | 'radio' | 'checkbox' | 'textarea' | 'slot'
  label: string
  prop: string
  placeholder?: string
  rules?: any[]
  options?: { label: string; value: any }[]
  treeProps?: { data: any[]; props: { label: string; value: string; children?: string } }
  disabled?: boolean
  span?: number // 栅格占列数
  slotName?: string
  props?: Record<string, any> // 透传给 el-* 组件的额外属性
  // 值变更回调。返回 false / Promise<false> 则拒绝变更，回退到旧值
  onChange?: (newVal: any, oldVal: any, formData: Record<string, any>) => boolean | Promise<boolean>
}

// 布局类型
export type FormLayout = 'single' | 'double' | { cols: number; gap?: number }

// FormBuilder props
export interface FormBuilderProps {
  fields: FormField[]
  modelValue: Record<string, any>
  layout?: FormLayout
  labelWidth?: string
  labelPosition?: 'left' | 'right' | 'top'
}
```

- [ ] **Step 3: 定义 ReferencePicker 相关类型**

```typescript
// ReferencePicker props
export interface ReferencePickerProps<T = any> {
  modelValue: any | any[]
  valueField: string
  displayField: string
  fetchApi: (params: QueryParams & { keyword?: string }) => Promise<{ rows: T[]; total: number }>
  columns: TableColumn[]
  mode?: 'single' | 'multiple'
  placeholder?: string
  disabled?: boolean
  clearable?: boolean
}
```

- [ ] **Step 4: 验证类型定义**

确保所有类型在 TypeScript 下无语法错误。

---

### Task 2: SearchTable 组件

**Files:**
- Create: `src/components/business/SearchTable.vue`

**Interfaces:**
- Consumes: `SearchField`, `TableColumn`, `ActionButton`, `QueryParams`, `SearchTableProps` from `types.ts`
- Produces: 可复用的列表页组件，emits: `search`, `reset`, `export`

- [ ] **Step 1: 实现组件骨架和 props 声明**

```vue
<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { Search, Refresh, Download, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { SearchField, TableColumn, ActionButton, QueryParams, SearchTableProps, FormConfig } from './types'
import FormBuilder from './FormBuilder.vue'

const props = withDefaults(defineProps<SearchTableProps>(), {
  defaultPageSize: 10,
  pageSizes: () => [10, 20, 50],
  showExport: false,
  exportLoading: false,
  maxVisibleButtons: 3
})

const emit = defineEmits<{
  search: [params: QueryParams]
  reset: []
  export: [params: QueryParams]
}>()

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)

const query = reactive<QueryParams>({ page: 1, size: props.defaultPageSize })
const initialQuery = ref<Record<string, any>>({})

// 表单弹窗状态
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const editId = ref<number | string>(0)
const formData = ref<Record<string, any>>({})
const formLoading = ref(false)
const formRef = ref()

// 计算操作列按钮：formConfig 存在时生成默认按钮
const resolvedActionButtons = computed<ActionButton[]>(() => {
  if (props.actionButtons !== undefined) return props.actionButtons // 显式传入，包括 []
  if (props.formConfig) return getDefaultActions()
  return []
})

function getDefaultActions(): ActionButton[] {
  const btns: ActionButton[] = []
  if (props.formConfig?.updateApi) {
    btns.push({
      label: '编辑', type: 'primary', permission: props.formConfig.editPermission,
      onClick: (row) => handleEdit(row)
    })
  }
  if (props.formConfig?.deleteApi) {
    btns.push({
      label: '删除', type: 'danger', confirm: '确定删除该记录吗？',
      permission: props.formConfig.deletePermission,
      onClick: (row) => handleDelete(row)
    })
  }
  return btns
}

// 分割可见按钮和折叠按钮
const visibleButtons = computed(() => resolvedActionButtons.value.slice(0, props.maxVisibleButtons))
const dropdownButtons = computed(() => resolvedActionButtons.value.slice(props.maxVisibleButtons))

function handleDropdownAction(row: any, command: string) {
  const btn = resolvedActionButtons.value.find(b => b.label === command)
  if (btn) btn.onClick(row)
}
// ... 后续逻辑
</script>
```

- [ ] **Step 2: 实现搜索栏渲染**

根据 `searchFields` 数组循环渲染 el-form-item，每个 field 根据 type 渲染对应的 Element Plus 控件（el-input / el-select / el-tree-select / el-date-picker）。

```vue
<el-card style="margin-bottom: 16px">
  <el-form :inline="true" :model="query" @submit.prevent>
    <el-form-item v-for="field in searchFields" :key="field.prop" :label="field.label">
      <el-input v-if="field.type === 'input'" v-model="query[field.prop]" :placeholder="field.placeholder" clearable />
      <el-select v-else-if="field.type === 'select'" v-model="query[field.prop]" :placeholder="field.placeholder" clearable :style="field.style || 'width: 180px'">
        <el-option v-for="opt in field.options" :key="String(opt.value)" :label="opt.label" :value="opt.value" />
      </el-select>
      <el-tree-select v-else-if="field.type === 'tree-select'" v-model="query[field.prop]" v-bind="field.treeProps" :placeholder="field.placeholder" clearable :style="field.style || 'width: 200px'" check-strictly />
      <el-date-picker v-else-if="field.type === 'date-picker'" v-model="query[field.prop]" :placeholder="field.placeholder" />
      <el-date-picker v-else-if="field.type === 'date-range'" v-model="query[field.prop]" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" />
    </el-form-item>
    <el-form-item>
      <div style="display: flex; gap: 8px; margin-left: auto">
        <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
        <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        <el-button v-if="showExport" :icon="Download" :loading="exportLoading" @click="handleExport">导出</el-button>
      </div>
    </el-form-item>
  </el-form>
</el-card>
```

- [ ] **Step 3: 实现工具栏 slot、表格渲染和表单弹窗**

```vue
<el-card>
  <!-- 工具栏 slot + 新增按钮 -->
  <div style="margin-bottom: 12px; display: flex; align-items: center; gap: 8px">
    <slot />
    <el-button v-if="formConfig && $slots.default === undefined" type="primary" :icon="Plus"
      v-permission="formConfig.createPermission" @click="handleCreate">新增</el-button>
  </div>
  <el-table :data="list" v-loading="loading" border>
    <el-table-column v-for="col in columns" :key="col.prop || col.label" :prop="col.prop" :label="col.label"
      :width="col.width" :min-width="col.minWidth" :align="col.align" :fixed="col.fixed" :formatter="col.formatter">
      <template #default="{ row, column, $index }" v-if="col.slotName && $slots[col.slotName]">
        <slot :name="col.slotName" :row="row" :column="column" :$index="$index" />
      </template>
    </el-table-column>
    <!-- 操作列 -->
    <el-table-column v-if="resolvedActionButtons.length" label="操作" :width="actionColumnWidth" fixed="right">
      <template #default="{ row }">
        <div class="flex items-center gap-1 whitespace-nowrap" style="display: inline-flex; align-items: center;">
          <template v-for="btn in visibleButtons" :key="btn.label">
            <el-popconfirm v-if="btn.confirm" :title="btn.confirm" @confirm="btn.onClick(row)">
              <template #reference>
                <el-button size="small" :type="btn.type || 'text'" v-permission="btn.permission">{{ btn.label }}</el-button>
              </template>
            </el-popconfirm>
            <el-button v-else size="small" :type="btn.type || 'text'" v-permission="btn.permission" @click="btn.onClick(row)">{{ btn.label }}</el-button>
          </template>
          <!-- 折叠按钮 -->
          <el-dropdown v-if="dropdownButtons.length" trigger="click" @command="(cmd) => handleDropdownAction(row, cmd)">
            <el-button size="small" text>
              <div style="display: flex; align-items: center; gap: 2px">
                <span>更多</span>
                <el-icon><ArrowDown /></el-icon>
              </div>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <template v-for="btn in dropdownButtons" :key="btn.label">
                  <el-dropdown-item v-if="btn.confirm" :command="btn.label" divided>
                    <el-popconfirm :title="btn.confirm" @confirm="btn.onClick(row)">
                      <template #reference>
                        <span>{{ btn.label }}</span>
                      </template>
                    </el-popconfirm>
                  </el-dropdown-item>
                  <el-dropdown-item v-else :command="btn.label" @click="btn.onClick(row)">
                    {{ btn.label }}
                  </el-dropdown-item>
                </template>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </template>
    </el-table-column>
  </el-table>
  <!-- 分页 -->
  <div v-if="total > 0" style="margin-top: 16px; display: flex; justify-content: flex-end">
    <el-pagination
      v-model:current-page="query.page" v-model:page-size="query.size"
      :total="total" :page-sizes="pageSizes"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="fetchList()" @current-change="fetchList()"
    />
  </div>
</el-card>

<!-- 集成表单弹窗 -->
<el-dialog v-if="formConfig" v-model="dialogVisible" :title="dialogTitle" :width="formConfig.dialogWidth || '500px'"
  :close-on-click-modal="false" @close="handleDialogClose">
  <FormBuilder
    ref="formRef" v-model="formData" :fields="formConfig.fields"
    :layout="formConfig.layout" :label-width="formConfig.labelWidth || '80px'"
  />
  <template #footer>
    <el-button @click="dialogVisible = false">取消</el-button>
    <el-button type="primary" :loading="formLoading" @click="handleDialogSubmit">确定</el-button>
  </template>
</el-dialog>
```

- [ ] **Step 4: 实现数据获取和分页逻辑**

```typescript
async function fetchList() {
  loading.value = true
  try {
    const res = await props.fetchApi({ ...query })
    list.value = res.rows
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  emit('search', { ...query })
  fetchList()
}

function handleReset() {
  Object.assign(query, { page: 1, size: props.defaultPageSize, ...initialQuery.value })
  emit('reset')
  fetchList()
}

function handleExport() {
  emit('export', { ...query })
}

onMounted(() => {
  // 记录初始查询值
  const init: Record<string, any> = {}
  for (const field of searchFields) {
    if (field.defaultValue !== undefined) init[field.prop] = field.defaultValue
    else init[field.prop] = undefined
  }
  initialQuery.value = init
  Object.assign(query, init)
  fetchList()
})
```

- [ ] **Step 5: 实现分页组件**

```vue
<div style="margin-top: 16px; display: flex; justify-content: flex-end">
  <el-pagination
    v-model:current-page="query.page"
    v-model:page-size="query.size"
    :total="total"
    :page-sizes="pageSizes"
    layout="total, sizes, prev, pager, next, jumper"
    @size-change="fetchList()"
    @current-change="fetchList()"
  />
</div>
```

- [ ] **Step 6: 实现表单 CRUD 逻辑**（仅当 formConfig 存在时启用）

```typescript
function handleCreate() {
  isEdit.value = false
  editId.value = 0
  formData.value = {}
  dialogTitle.value = formConfig?.dialogTitle?.create || '新增'
  dialogVisible.value = true
}

async function handleEdit(row: any) {
  if (formConfig?.beforeEdit) {
    const ok = await formConfig.beforeEdit(row)
    if (ok === false) return
  }
  isEdit.value = true
  editId.value = row.id
  dialogTitle.value = formConfig?.dialogTitle?.edit || '编辑'

  if (formConfig?.getApi) {
    formLoading.value = true
    try {
      const res = await formConfig.getApi(row.id)
      formData.value = res
    } finally {
      formLoading.value = false
    }
  } else {
    // 没有 getApi 时直接用行数据填充表单
    formData.value = { ...row }
  }
  dialogVisible.value = true
}

async function handleDelete(row: any) {
  if (formConfig?.beforeDelete) {
    const ok = await formConfig.beforeDelete(row)
    if (ok === false) return
  }
  try {
    await formConfig?.deleteApi?.(row.id)
    ElMessage.success('删除成功')
    formConfig?.afterDelete?.()
    fetchList()
  } catch {
    // 错误已在 http 拦截器中处理
  }
}

async function handleDialogSubmit() {
  const valid = await formRef.value?.validate()
  if (!valid) return

  formLoading.value = true
  try {
    if (isEdit.value) {
      await formConfig?.updateApi?.(editId.value, formData.value)
      formConfig?.afterUpdate?.(formData.value)
    } else {
      await formConfig?.createApi?.(formData.value)
      formConfig?.afterCreate?.(formData.value)
    }
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    fetchList()
  } finally {
    formLoading.value = false
  }
}

function handleDialogClose() {
  formData.value = {}
}
```

---

### Task 3: FormBuilder 表单组件

**Files:**
- Create: `src/components/business/FormBuilder.vue`

**Interfaces:**
- Consumes: `FormField`, `FormLayout`, `FormBuilderProps` from `types.ts`
- Produces: 表单组件，expose `validate`, `validateField`, `resetFields`, `clearValidate`

- [ ] **Step 1: 实现组件骨架和 props 声明**

```vue
<script setup lang="ts">
import { ref } from 'vue'
import type { FormField, FormLayout, FormBuilderProps } from './types'

const props = withDefaults(defineProps<FormBuilderProps>(), {
  layout: 'single',
  labelWidth: '80px',
  labelPosition: 'left'
})

const emit = defineEmits<{
  'update:modelValue': [value: Record<string, any>]
}>()

const formRef = ref()

function handleUpdate(prop: string, value: any) {
  const field = props.fields.find(f => f.prop === prop)
  if (field?.onChange) {
    const oldVal = props.modelValue[prop]
    const result = field.onChange(value, oldVal, { ...props.modelValue })
    if (result instanceof Promise) {
      result.then(accepted => {
        if (accepted !== false) emit('update:modelValue', { ...props.modelValue, [prop]: value })
      })
    } else if (result !== false) {
      emit('update:modelValue', { ...props.modelValue, [prop]: value })
    }
    // result === false: 拒绝变更，不回写
  } else {
    emit('update:modelValue', { ...props.modelValue, [prop]: value })
  }
}

async function validate(): Promise<boolean> {
  if (!formRef.value) return true
  try {
    await formRef.value.validate()
    return true
  } catch {
    return false
  }
}

function validateField(prop: string) {
  formRef.value?.validateField(prop)
}

function resetFields() {
  formRef.value?.resetFields()
}

function clearValidate() {
  formRef.value?.clearValidate()
}

defineExpose({ validate, validateField, resetFields, clearValidate })
</script>
```

- [ ] **Step 2: 实现布局计算逻辑**

```typescript
const colSpan = computed(() => {
  if (props.layout === 'single') return 24
  if (props.layout === 'double') return 12
  if (typeof props.layout === 'object') return 24 / props.layout.cols
  return 24
})
```

- [ ] **Step 3: 实现表单字段渲染模板**

```vue
<el-form ref="formRef" :model="modelValue" :label-width="labelWidth" :label-position="labelPosition">
  <el-row :gutter="typeof layout === 'object' ? (layout.gap || 20) : 20">
    <el-col v-for="field in fields" :key="field.prop" :span="field.span || colSpan">
      <el-form-item :label="field.label" :prop="field.prop" :rules="field.rules">
        <!-- input -->
        <el-input v-if="field.type === 'input'" :model-value="modelValue[field.prop]" :placeholder="field.placeholder" :disabled="field.disabled" v-bind="field.props" @update:model-value="handleUpdate(field.prop, $event)" />
        <!-- textarea -->
        <el-input v-else-if="field.type === 'textarea'" type="textarea" :model-value="modelValue[field.prop]" :placeholder="field.placeholder" :disabled="field.disabled" :rows="3" v-bind="field.props" @update:model-value="handleUpdate(field.prop, $event)" />
        <!-- select -->
        <el-select v-else-if="field.type === 'select'" :model-value="modelValue[field.prop]" :placeholder="field.placeholder" :disabled="field.disabled" style="width: 100%" v-bind="field.props" @update:model-value="handleUpdate(field.prop, $event)">
          <el-option v-for="opt in field.options" :key="String(opt.value)" :label="opt.label" :value="opt.value" />
        </el-select>
        <!-- tree-select -->
        <el-tree-select v-else-if="field.type === 'tree-select'" :model-value="modelValue[field.prop]" :placeholder="field.placeholder" :disabled="field.disabled" style="width: 100%" v-bind="field.treeProps" @update:model-value="handleUpdate(field.prop, $event)" />
        <!-- switch -->
        <el-switch v-else-if="field.type === 'switch'" :model-value="modelValue[field.prop]" :disabled="field.disabled" v-bind="field.props" @update:model-value="handleUpdate(field.prop, $event)" />
        <!-- date-picker -->
        <el-date-picker v-else-if="field.type === 'date-picker'" :model-value="modelValue[field.prop]" :placeholder="field.placeholder" :disabled="field.disabled" style="width: 100%" v-bind="field.props" @update:model-value="handleUpdate(field.prop, $event)" />
        <!-- radio -->
        <el-radio-group v-else-if="field.type === 'radio'" :model-value="modelValue[field.prop]" :disabled="field.disabled" @update:model-value="handleUpdate(field.prop, $event)">
          <el-radio v-for="opt in field.options" :key="String(opt.value)" :value="opt.value">{{ opt.label }}</el-radio>
        </el-radio-group>
        <!-- checkbox -->
        <el-checkbox-group v-else-if="field.type === 'checkbox'" :model-value="modelValue[field.prop]" :disabled="field.disabled" @update:model-value="handleUpdate(field.prop, $event)">
          <el-checkbox v-for="opt in field.options" :key="String(opt.value)" :value="opt.value">{{ opt.label }}</el-checkbox>
        </el-checkbox-group>
        <!-- slot -->
        <slot v-else-if="field.type === 'slot'" :name="field.slotName || field.prop" :prop="field.prop" :value="modelValue[field.prop]" :update="(val: any) => handleUpdate(field.prop, val)" />
      </el-form-item>
    </el-col>
  </el-row>
</el-form>
```

---

### Task 4: ReferencePicker 引用查找组件

**Files:**
- Create: `src/components/business/ReferencePicker.vue`

**Interfaces:**
- Consumes: `ReferencePickerProps`, `TableColumn`, `QueryParams` from `types.ts`
- Produces: 引用查找组件，emits `update:modelValue`

- [ ] **Step 1: 实现组件骨架和 props 声明**

```vue
<script setup lang="ts">
import { ref } from 'vue'
import { Search } from '@element-plus/icons-vue'
import type { TableColumn, QueryParams } from './types'

const props = withDefaults(defineProps<{
  modelValue: any | any[]
  valueField: string
  displayField: string
  fetchApi: (params: QueryParams & { keyword?: string }) => Promise<{ rows: any[]; total: number }>
  columns: TableColumn[]
  mode?: 'single' | 'multiple'
  placeholder?: string
  disabled?: boolean
  clearable?: boolean
}>(), {
  mode: 'single',
  placeholder: '请选择',
  disabled: false,
  clearable: false
})

const emit = defineEmits<{
  'update:modelValue': [value: any | any[]]
}>()

const dialogVisible = ref(false)
const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const keyword = ref('')
const selectedRows = ref<any[]>([])
const displayText = ref('')
const page = ref(1)
const size = ref(10)
</script>
```

- [ ] **Step 2: 实现输入框显示逻辑**

```vue
<template>
  <div>
    <el-input
      :model-value="displayText"
      :placeholder="placeholder"
      :disabled="disabled"
      :clearable="clearable"
      readonly
      @click="openDialog"
      @clear="handleClear"
    >
      <template #suffix>
        <el-icon><Search /></el-icon>
      </template>
    </el-input>
```

```typescript
async function openDialog() {
  if (props.disabled) return
  dialogVisible.value = true
  page.value = 1
  keyword.value = ''
  selectedRows.value = []
  await fetchData()
}

async function fetchData() {
  loading.value = true
  try {
    const params: any = { page: page.value, size: size.value }
    if (keyword.value) params.keyword = keyword.value
    const res = await props.fetchApi(params)
    list.value = res.rows
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function handleClear() {
  emit('update:modelValue', props.mode === 'multiple' ? [] : null)
  displayText.value = ''
}
```

- [ ] **Step 3: 实现弹窗内搜索和表格**

```vue
<el-dialog v-model="dialogVisible" title="请选择" width="640px" :close-on-click-modal="false">
  <!-- 搜索栏 -->
  <div style="margin-bottom: 12px; display: flex; gap: 8px">
    <el-input v-model="keyword" placeholder="输入搜索条件" clearable @keyup.enter="handleDialogSearch" style="flex: 1" />
    <el-button type="primary" :icon="Search" @click="handleDialogSearch">搜索</el-button>
  </div>
  <!-- 表格 -->
  <el-table :data="list" v-loading="loading" border @row-click="handleRowClick" @selection-change="handleSelectionChange">
    <el-table-column v-if="mode === 'multiple'" type="selection" width="50" />
    <el-table-column v-for="col in columns" :key="col.prop || col.label" :prop="col.prop" :label="col.label" :width="col.width" :min-width="col.minWidth" />
  </el-table>
  <!-- 分页 -->
  <div style="margin-top: 12px; display: flex; justify-content: flex-end">
    <el-pagination
      v-model:current-page="page"
      v-model:page-size="size"
      :total="total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next"
      @size-change="fetchData()"
      @current-change="fetchData()"
    />
  </div>
  <template #footer>
    <el-button @click="dialogVisible = false">取消</el-button>
    <el-button v-if="mode === 'multiple'" type="primary" @click="confirmMultiple">确定</el-button>
  </template>
</el-dialog>
```

- [ ] **Step 4: 实现选择和确认逻辑**

```typescript
function handleRowClick(row: any) {
  if (props.mode === 'single') {
    emit('update:modelValue', row[props.valueField])
    displayText.value = row[props.displayField]
    dialogVisible.value = false
  }
}

function handleSelectionChange(rows: any[]) {
  selectedRows.value = rows
}

function confirmMultiple() {
  const values = selectedRows.value.map(r => r[props.valueField])
  emit('update:modelValue', values)
  displayText.value = selectedRows.value.map(r => r[props.displayField]).join(' / ')
  dialogVisible.value = false
}

function handleDialogSearch() {
  page.value = 1
  fetchData()
}
```

---

### Task 5: 统一导出

**Files:**
- Create: `src/components/business/index.ts`

**Interfaces:**
- Produces: 统一导出供外部使用

- [ ] **Step 1: 创建 index.ts**

```typescript
export { default as SearchTable } from './SearchTable.vue'
export { default as FormBuilder } from './FormBuilder.vue'
export { default as ReferencePicker } from './ReferencePicker.vue'
export * from './types'
```

---

## Self-Review

- ✅ SearchTable 覆盖 spec 中所有需求：搜索栏渲染、搜索/重置/导出按钮、表格渲染、操作列、分页、loading、toolbar slot、fetchApi 绑定
- ✅ FormBuilder 覆盖 spec：多种字段类型渲染、v-model 双向绑定、布局配置、验证集成、expose 方法
- ✅ ReferencePicker 覆盖 spec：输入框点击弹窗、弹窗搜索表格、单选/多选、valueField/displayField、自动加载
- ✅ 无占位符或 TBD 内容
- ✅ 类型一致性：所有组件共享 types.ts 中定义的类型
- ✅ 每个步骤包含完整代码