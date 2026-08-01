# FormBuilder 多列布局 span 跨列支持

## 目标

FormBuilder 当前支持 `single` / `double` / `{ cols: N }` 三种布局，但 `double` 模式下 `FormField.span` 不支持跨列（等于 12 的半列行为正确，等于 24 的独占一行会导致后续字段错位）。本次变更统一布局渲染逻辑，使 `span` 在所有布局模式下都能正确工作。

## 方案：统一 grid 渲染 + span 累计换行

### 类型变化

`FormLayout` 和 `FormField.span` 类型不变，`span` 含义不变：

```ts
// FormLayout — 不变，仍为联合类型
export type FormLayout = 'single' | 'double' | { cols: number; gap?: number }

// FormField.span — 不变，表示占用的 el-col 栅格数（1-24）
export interface FormField {
  // ...
  span?: number
}
```

### resolvedLayout — 旧值映射

新增 `resolvedLayout` computed，将 `single` / `double` 字符串统一映射为对象格式：

```ts
const resolvedLayout = computed(() => {
  const l = props.layout
  if (typeof l === 'object' && l !== null && 'cols' in l) {
    return { cols: l.cols, gap: l.gap ?? 16 }
  }
  if (l === 'single') return { cols: 1, gap: 0 }
  if (l === 'double') return { cols: 2, gap: 16 }
  return { cols: 1, gap: 0 }
})
```

| 旧配置 | 映射为 |
|--------|--------|
| `'single'` | `{ cols: 1, gap: 0 }` |
| `'double'` | `{ cols: 2, gap: 16 }` |
| `{ cols: 3 }` | `{ cols: 3, gap: 16 }` |
| `{ cols: 2, gap: 8 }` | `{ cols: 2, gap: 8 }` |

### layoutRows — 按 span 累计换行

新增 `layoutRows` computed，遍历 fields 按 `span` 累计值（默认 `24 / cols`）换行，**每行 span 和 ≤ 24**：

```ts
const layoutRows = computed(() => {
  const cols = resolvedLayout.value.cols
  const defaultSpan = 24 / cols
  const rows: { fields: FormField[] }[] = []
  let currentRow: FormField[] = []
  let acc = 0

  for (const field of props.fields) {
    const span = field.span ?? defaultSpan

    // 当前行已有内容且加上本字段会溢出 -> 换行
    if (acc > 0 && acc + span > 24) {
      rows.push({ fields: currentRow })
      currentRow = [field]
      acc = span
    } else {
      currentRow.push(field)
      acc += span
    }
  }
  if (currentRow.length) rows.push({ fields: currentRow })
  return rows
})
```

### 模板统一

移除原来的三套分支（grid / double / single），统一为 `el-row` + `v-for` 循环：

```vue
<el-form ref="formRef" :model="localModel" :label-width="labelWidth || '80px'" :label-position="labelPosition" style="width: 100%">
  <el-row v-for="(row, ri) in layoutRows" :key="ri" :gutter="resolvedLayout.gap" style="width: 100%">
    <el-col v-for="field in row.fields" :key="field.prop || field.label" :span="field.span ?? (24 / resolvedLayout.cols)">
      <el-form-item :label="field.label" :prop="field.prop" :rules="field.rules">
        <slot v-if="field.type === 'slot'" :name="field.slotName"
          :value="localModel[field.prop]"
          :update="(v: any) => setFieldValue(field, v)" />
        <render-field v-else
          :field="field"
          :model-value="localModel[field.prop]"
          @update:model-value="(v: any) => setModelField(field, v)"
          @input-change="handleChange(field, localModel[field.prop])" />
      </el-form-item>
    </el-col>
  </el-row>
</el-form>
```

### 行为示例

| 布局 | 字段 span | 渲染行 |
|------|-----------|--------|
| `double` | `[{span:12}, {span:12}, {span:12}, {span:12}]` | 第1行: 2个, 第2行: 2个 |
| `double` | `[{span:24}, {span:12}, {span:12}]` | 第1行: textarea, 第2行: 2个 input |
| `{ cols: 3 }` | `[{span:8}, {span:8}, {span:8}, {span:24}]` | 第1行: 3个, 第2行: 独占 |
| `double` | `[{span:8}, {span:8}, {span:8}, {span:12}, {span:12}]` | 第1行: 3个(8+8+8), 第2行: 2个(12+12) |
| `single` | `[{span:8}, {span:16}]` | 第1行: 1个(8), 第2行: 1个(16) |

## 向后兼容

- 所有现有 `single` / `double` 页面行为不变（映射为等价对象）
- `{ cols: N }` 对象模式行为不变
- 未设置 `span` 的字段自动取 `24 / cols`，与当前默认行为一致
- 组件外部暴露的 `validate()` 和 `clearValidate()` 方法不变

## 变更范围

| 文件 | 变更 |
|------|------|
| `FormBuilder.vue` | 模板统一 + 新增 `resolvedLayout` / `layoutRows` + 移除旧分支 |
| `FormBuilder.test.ts` | 新增 span 跨列测试用例 |
| `types.ts` | 不变 |
| 业务页面 | 不变（向后兼容） |

## 测试用例

1. `double` 布局无 span 时每行 2 个字段（回归）
2. `span: 24` 字段独占一行，后续字段正常换行
3. `span: 16` + `span: 8` 混合跨列占满一行
4. `single` 布局行为不变（回归）
5. `{ cols: 3 }` 布局中 `span: 24` 独占一行