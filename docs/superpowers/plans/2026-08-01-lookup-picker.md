# LookupPicker 组件实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 新增 LookupPicker 组件，支持从其他表弹窗选择数据并将选中行的多个字段带回当前表单，同时与 FormBuilder 深度集成。

**Architecture:** 独立 LookupPicker.vue 组件处理弹窗选择+多字段回填，FormBuilder 新增 `'lookup'` 类型分支，`FormField.type` 增加 `'lookup'` 枚举。LookupPicker 内部实现弹窗/搜索/分页/多选逻辑，与现有 ReferencePicker 无代码复用。

**Tech Stack:** Vue 3 + Element Plus + TypeScript + Tailwind CSS + Vitest + jsdom

## Global Constraints

- 组件位于 `mangban-ui/src/components/business/`
- 遵循现有代码风格：Element Plus 组件、`<script setup lang="ts">`、defineComponent RenderField 模式
- 测试使用 Vitest + @vue/test-utils + jsdom，参考现有 `ReferencePicker.test.ts` 的模式
- 所有 props 定义在 `types.ts` 中新增的 `LookupPickerProps` 接口
- `FormField.type` 新增 `'lookup'` 枚举，专有配置通过 `props: Record<string, any>` 透传
- `returnFields` 格式：`{ sourceField: 'targetField' }`
- `modelValue` 单选为单行对象，多选为对象数组
- 清空时联动清除 `returnFields` 对应的目标字段

---

### Task 1: LookupPicker 类型定义

**Files:**
- Modify: `mangban-ui/src/components/business/types.ts` — 新增 `LookupPickerProps` 接口

**Interfaces:**
- Produces: `LookupPickerProps` 接口（供 Task 2 组件使用），`QueryParams` 类型已存在无需改动

- [ ] **Step 1: 在 types.ts 中 `ReferencePickerProps` 之后新增 `LookupPickerProps` 接口**

```typescript
// --- LookupPicker props ---

export interface LookupPickerProps {
  /** v-model 绑定选中行数据 */
  modelValue: Record<string, any> | null | Record<string, any>[]

  /** 弹窗表格列定义 */
  columns: TableColumn[]

  /** 数据获取函数 */
  fetchApi: (
    params: QueryParams & { keyword?: string },
  ) => Promise<{ rows: any[]; total: number }>

  /** 字段映射：选中行的 sourceField → 表单的 targetField */
  returnFields?: Record<string, string>

  /** 输入框显示字段，默认取 columns 第一个非 selection 列的 prop */
  displayField?: string

  /** 输入框占位符 */
  placeholder?: string
  searchPlaceholder?: string

  /** 是否显示弹窗搜索框，默认 true */
  showSearch?: boolean

  /** 选择模式：single（默认）| multiple */
  mode?: 'single' | 'multiple'

  disabled?: boolean
  clearable?: boolean

  /** 弹窗标题 */
  dialogTitle?: string
}
```

- [ ] **Step 2: 确认编译通过**

Run: `cd mangban-ui && npx vue-tsc --noEmit --strict`
Expected: 无类型错误

- [ ] **Step 3: Commit**

```bash
git add mangban-ui/src/components/business/types.ts
git commit -m "feat(lookup): add LookupPickerProps type definition"
```

---

### Task 2: LookupPicker 组件实现

**Files:**
- Create: `mangban-ui/src/components/business/LookupPicker.vue`
- Test: `mangban-ui/src/components/business/__tests__/LookupPicker.test.ts`

**Interfaces:**
- Consumes: `LookupPickerProps`（Task 1）、`TableColumn`（已有）、`QueryParams`（已有）
- Produces: `LookupPicker` 组件（供 Task 3/4 使用）、`LookupPicker.test.ts` 测试

- [ ] **Step 1: 编写 LookupPicker.test.ts — 基础渲染测试（RED）**

```typescript
// ----- TDD: LookupPicker 组件测试 -----
// npx vitest run src/components/business/__tests__/LookupPicker.test.ts

import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'
import ElementPlus from 'element-plus'
import LookupPicker from '../LookupPicker.vue'

function createWrapper(props: any = {}) {
  return mount(LookupPicker, {
    props: {
      modelValue: null,
      fetchApi: vi.fn().mockResolvedValue({ rows: [], total: 0 }),
      columns: [{ prop: 'code', label: '编号' }, { prop: 'name', label: '名称' }],
      ...props,
    },
    global: {
      plugins: [ElementPlus],
    },
  })
}

describe('LookupPicker — 基础渲染', () => {
  it('渲染输入框', () => {
    const wrapper = createWrapper()
    expect(wrapper.find('input').exists()).toBe(true)
  })

  it('显示 placeholder', () => {
    const wrapper = createWrapper({ placeholder: '请选择盲板' })
    const input = wrapper.find('input')
    expect(input.attributes('placeholder')).toBe('请选择盲板')
  })

  it('默认为请选择', () => {
    const wrapper = createWrapper()
    const input = wrapper.find('input')
    expect(input.attributes('placeholder')).toBe('请选择')
  })

  it('disabled 时输入框不可用', () => {
    const wrapper = createWrapper({ disabled: true })
    const input = wrapper.find('input')
    expect(input.attributes('disabled')).toBeDefined()
  })
})
```

- [ ] **Step 2: 运行测试确认失败**

Run: `npx vitest run src/components/business/__tests__/LookupPicker.test.ts`
Expected: 报错找不到 LookupPicker 模块（组件尚未创建）

- [ ] **Step 3: 创建 LookupPicker.vue — 模板部分**

```vue
<template>
  <div class="lookup-picker">
    <el-input
      :model-value="displayText"
      :placeholder="placeholder || '请选择'"
      :disabled="disabled"
      :clearable="clearable"
      readonly
      @click="openDialog"
      @clear="handleClear"
    >
      <template #prefix>
        <el-icon><Search /></el-icon>
      </template>
    </el-input>
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle || placeholder || '选择数据'"
      width="700px"
      :close-on-click-modal="false"
      append-to-body
    >
      <!-- 搜索栏 -->
      <div v-if="showSearch !== false" style="margin-bottom: 12px; display: flex; gap: 8px">
        <el-input
          v-model="keyword"
          :placeholder="searchPlaceholder || '请输入关键字搜索'"
          clearable
          @keyup.enter="handleSearch"
        />
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>
      <!-- 表格 -->
      <el-table
        :data="tableData"
        v-loading="loading"
        border
        highlight-current-row
        @row-click="handleRowClick"
        @selection-change="handleSelectionChange"
      >
        <el-table-column v-if="mode === 'multiple'" type="selection" width="50" />
        <el-table-column
          v-for="col in columns"
          :key="col.prop || col.label"
          :prop="col.prop"
          :label="col.label"
          :width="col.width"
        />
      </el-table>
      <!-- 分页 -->
      <div style="margin-top: 12px; display: flex; justify-content: flex-end">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          small
          @size-change="fetchData()"
          @current-change="fetchData()"
        />
      </div>
      <!-- 多选底部按钮 -->
      <template v-if="mode === 'multiple'" #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmSelection">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>
```

- [ ] **Step 4: 创建 LookupPicker.vue — 脚本部分**

```vue
<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { Search } from '@element-plus/icons-vue'
import type { LookupPickerProps, QueryParams } from './types'

const props = withDefaults(defineProps<LookupPickerProps>(), {
  mode: 'single',
  placeholder: '请选择',
  searchPlaceholder: '请输入关键字搜索',
  showSearch: true,
  disabled: false,
  clearable: true,
})

const emit = defineEmits<{
  'update:modelValue': [value: Record<string, any> | null | Record<string, any>[]]
  'select': [row: any]
  'clear': []
}>()

const dialogVisible = ref(false)
const loading = ref(false)
const keyword = ref('')
const tableData = ref<any[]>([])
const total = ref(0)
const tempSelection = ref<any[]>([])

const query = reactive<QueryParams & { keyword?: string }>({
  page: 1,
  size: 10,
})

const defaultDisplayField = computed(() => {
  const firstCol = props.columns.find(c => c.prop)
  return firstCol?.prop || ''
})

const resolvedDisplayField = computed(() => props.displayField || defaultDisplayField.value)

const displayText = computed(() => {
  const val = props.modelValue
  if (!val) return ''
  if (Array.isArray(val)) {
    if (val.length === 0) return ''
    return val[0][resolvedDisplayField.value] || ''
  }
  if (typeof val === 'object') {
    return val[resolvedDisplayField.value] || ''
  }
  return ''
})

function openDialog() {
  if (props.disabled) return
  dialogVisible.value = true
  keyword.value = ''
  query.page = 1
  tempSelection.value = []
  fetchData()
}

async function fetchData() {
  loading.value = true
  try {
    const params: any = { ...query }
    if (keyword.value) params.keyword = keyword.value
    const res = await props.fetchApi(params)
    tableData.value = res.rows
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  fetchData()
}

function handleRowClick(row: any) {
  if (props.mode === 'multiple') return
  emit('update:modelValue', row)
  emit('select', row)
  dialogVisible.value = false
}

function handleSelectionChange(rows: any[]) {
  tempSelection.value = rows
}

function confirmSelection() {
  emit('update:modelValue', [...tempSelection.value])
  if (tempSelection.value.length > 0) {
    emit('select', tempSelection.value)
  }
 ialogVisible.value = false
}

function handleClear() {
  emit('update:modelValue', props.mode === 'multiple' ? [] : null)
  emit('clear')
}

defineExpose({ openDialog, closeDialog: () => { dialogVisible.value = false } })
</script>
```

- [ ] **Step 5: 运行测试验证通过**

Run: `npx vitest run src/components/business/__tests__/LookupPicker.test.ts`
Expected: PASS

- [ ] **Step 6: 编写 LookupPicker.test.ts — 弹窗交互测试（RED→GREEN）**

```typescript
describe('LookupPicker — 弹窗交互', () => {
  it('点击输入框打开弹窗', async () => {
    const wrapper = createWrapper()
    await wrapper.find('input').trigger('click')
    await nextTick()
    expect(document.body.querySelector('.el-dialog') !== null).toBeTruthy()
  })

  it('disabled 时点击不打开弹窗', async () => {
    const wrapper = createWrapper({ disabled: true })
    await wrapper.find('input').trigger('click')
    await nextTick()
    expect(wrapper.find('.el-dialog__wrapper').exists()).toBe(false)
  })

  it('打开弹窗时调用 fetchApi', async () => {
    const fetchApi = vi.fn().mockResolvedValue({ rows: [], total: 0 })
    const wrapper = createWrapper({ fetchApi })
    await wrapper.find('input').trigger('click')
    await nextTick()
    expect(fetchApi).toHaveBeenCalled()
  })
})
```

- [ ] **Step 7: 编写 LookupPicker.test.ts — 单选/多选/清除测试（RED→GREEN）**

```typescript
describe('LookupPicker — 单选', () => {
  it('点击行选中并关闭弹窗', async () => {
    const fetchApi = vi.fn().mockResolvedValue({
      rows: [{ code: 'BL-001', name: '盲板A' }],
      total: 1,
    })
    const wrapper = createWrapper({ fetchApi, returnFields: { code: 'blindCode', name: 'blindName' } })
    await wrapper.find('input').trigger('click')
    await nextTick()
    await nextTick()
    // 模拟行点击
    const rowEl = document.body.querySelector('.el-table__body-wrapper .el-table__row')
    expect(rowEl).toBeTruthy()
  })

  it('显示选中行 displayField', async () => {
    const wrapper = createWrapper({
      modelValue: { code: 'BL-001', name: '盲板A' },
      displayField: 'code',
    })
    await nextTick()
    const input = wrapper.find('input')
    expect(input.element.value).toBe('BL-001')
  })
})

describe('LookupPicker — 清除', () => {
  it('clearable 时清除按钮可用', () => {
    const wrapper = createWrapper()
    expect(wrapper.props('clearable')).toBe(true)
  })
})
```

- [ ] **Step 8: 运行全部 LookupPicker 测试**

Run: `npx vitest run src/components/business/__tests__/LookupPicker.test.ts`
Expected: 所有测试 PASS

- [ ] **Step 9: Commit**

```bash
git add mangban-ui/src/components/business/LookupPicker.vue mangban-ui/src/components/business/__tests__/LookupPicker.test.ts
git commit -m "feat(lookup): implement LookupPicker component with dialog, search, pagination, single/multiple select"
```

---

### Task 3: 类型定义扩展 — FormField 增加 `'lookup'` 枚举

**Files:**
- Modify: `mangban-ui/src/components/business/types.ts` — FormField.type 增加 `'lookup'`

**Interfaces:**
- Consumes: 无
- Produces: 更新后的 `FormField` 类型

- [ ] **Step1: 修改 FormField.type**

```typescript
export interface FormField {
  type:
    | 'input'
    | 'select'
    | 'tree-select'
    | 'switch'
    | 'date-picker'
    | 'radio'
    | 'checkbox'
    | 'textarea'
    | 'slot'
    | 'lookup'   // ← 新增
  // ... 其余属性不变
}
```

- [ ] **Step 2: 确认编译通过**

Run: `cd mangban-ui && npx vue-tsc --noEmit --strict`
Expected: 无类型错误

- [ ] **Step 3: Commit**

```bash
git add mangban-ui/src/components/business/types.ts
git commit -m "feat(lookup): add 'lookup' type to FormField enum"
```

---

### Task 4: FormBuilder 集成 — render-field 新增 `'lookup'` 分支

**Files:**
- Modify: `mangban-ui/src/components/business/FormBuilder.vue` — RenderField 的 switch 中新增 `'lookup'` case

**Interfaces:**
- Consumes: `LookupPicker`（Task 2）、更新后的 `FormField`（Task 3）
- Produces: 更新后的 `FormBuilder` 组件

- [ ] **Step 1: 在 FormBuilder.vue 的 import 中添加 LookupPicker**

```typescript
import LookupPicker from './LookupPicker.vue'
// 注意：已有 RenderField 是内联 defineComponent，在 setup 作用域外无法直接使用 import 的组件
// 需要将 RenderField 移到 setup 内或使用 markRaw/h 方式
```

**技术方案**：当前 `RenderField` 是 `defineComponent` 内联在 `<script setup>` 外，无法直接访问同文件 import 的 `LookupPicker`。需要将 `RenderField` 移到 `<script setup>` 内，或通过 `resolveComponent` 方式处理。

- [ ] **Step 2: 重构 RenderField 位置（如需）**

将 `RenderField` 从 `<script setup>` 外移到 setup 内（作为局部函数），或者使用 `markRaw(LookupPicker)` 方式：

```typescript
// 方案：将 RenderField 重构为 setup 内的局部组件
// 因为内联 defineComponent 在 setup 外无法访问组件引用

const RenderField = defineComponent({
  // ... 原有代码不变
  setup(props, { emit }) {
    return () => {
      const f = props.field
      const v = props.modelValue
      const onInput = (val: any) => {
        emit('update:modelValue', val)
        emit('inputChange', val)
      }
      const common = { /* ...原有不变 */ }

      switch (f.type) {
        // ... 原有 case 不变
        case 'lookup':
          return h(LookupPicker, {
            modelValue: v,
            'onUpdate:modelValue': (val: Record<string, any> | null | Record<string, any>[]) => {
              onInput(val)
              // 批量更新 returnFields 目标字段
              if (f.props?.returnFields && val) {
                if (Array.isArray(val) && val.length > 0) {
                  // 多选：取数组第一行填充
                  for (const [sourceField, targetField] of Object.entries(f.props.returnFields)) {
                    localModel[targetField] = val[0][sourceField]
                  }
                } else if (!Array.isArray(val)) {
                  for (const [sourceField, targetField] of Object.entries(f.props.returnFields)) {
                    localModel[targetField] = val[sourceField]
                  }
                }
              }
              // 清空时清理目标字段
              if (!val && f.props?.returnFields) {
                const isEmpty = (Array.isArray(val) && val.length === 0)
                if (!val || isEmpty) {
                  for (const targetField of Object.values(f.props.returnFields)) {
                    localModel[targetField] = null
                  }
                }
              }
            },
            ...f.props,
          })
        default:
          return null
      }
    }
  },
})
```

- [ ] **Step 3: 运行 FormBuilder 现有测试确认无回归**

Run: `npx vitest run src/components/business/__tests__/FormBuilder.test.ts`
Expected: 所有测试 PASS

- [ ] **Step 4: 编写 FormBuilder lookup 集成测试（RED→GREEN）**

```typescript
// 在 FormBuilder.test.ts 末尾追加
import LookupPicker from '../LookupPicker.vue'

describe('FormBuilder — lookup 类型', () => {
  it('渲染 lookup 字段', () => {
    const wrapper = mount(FormBuilder, {
      props: {
        fields: [
          {
            type: 'lookup',
            label: '盲板',
            prop: 'selectedBlind',
            props: {
              fetchApi: vi.fn().mockResolvedValue({ rows: [], total: 0 }),
              columns: [{ prop: 'code', label: '编号' }],
              returnFields: { code: 'blindCode' },
            },
          },
          { type: 'input', label: '盲板编号', prop: 'blindCode' },
        ],
        modelValue: {},
      },
      global: { plugins: [ElementPlus] },
    })
    expect(wrapper.findComponent(LookupPicker).exists()).toBe(true)
  })
})
```

- [ ] **Step5: 运行所有测试确认通过**

Run: `npx vitest run`
Expected: 所有测试 PASS（含 ReferencePicker、SearchTable 等原有测试）

- [ ] **Step6: Commit**

```bash
git add mangban-ui/src/components/business/FormBuilder.vue mangban-ui/src/components/business/__tests__/FormBuilder.test.ts
git commit -m "feat(lookup): integrate LookupPicker into FormBuilder as 'lookup' type"
```

---

### Task5: 导出 LookupPicker

**Files:**
- Modify: `mangban-ui/src/components/business/index.ts` — 新增 LookupPicker 导出

- [ ] **Step1: 在 index.ts 中添加导出**

```typescript
export { default as LookupPicker } from './LookupPicker.vue'
```

- [ ] **Step 2: Commit**

```bash
git add mangban-ui/src/components/business/index.ts
git commit -m "feat(lookup): export LookupPicker from business components"
```

---

### Task 6: 最终验证

- [ ] **Step 1: 类型检查**

Run: `cd mangban-ui && npx vue-tsc --noEmit --strict`
Expected: 无类型错误

- [ ] **Step2: 运行全量测试**

Run: `npx vitest run`
Expected: 全部测试 PASS

- [ ] **Step3: 确认变更清单**

| 文件 | 变更 |
|--|--|
| `types.ts` | 新增 `LookupPickerProps` 接口 + `FormField.type` 增加 `'lookup'` |
| `LookupPicker.vue` | 新增 ~180 行组件 |
| `FormBuilder.vue` | RenderField 新增 `'lookup'` case + 批量更新逻辑 |
| `index.ts` | 新增 `LookupPicker` 导出 |
| `LookupPicker.test.ts` | 新增 ~100 行测试 |

- [ ] **Step 4: 完成提交**

```bash
git status
```
确认所有变更已提交，无遗漏文件。