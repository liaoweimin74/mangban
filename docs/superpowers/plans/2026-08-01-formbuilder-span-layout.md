# Plan: FormBuilder 多列布局 span 跨列支持

## 文件结构

| 文件 | 操作 | 职责 |
|------|------|------|
| `FormBuilder.vue` | 修改 | 模板统一 + 新增 `resolvedLayout` / `layoutRows` computed + 移除旧分支 |
| `FormBuilder.test.ts` | 修改 | 新增 span 跨列测试用例 |
| `types.ts` | 不变 | — |
| 业务页面 | 不变 | — |

## 任务

### Task 1：新增 `resolvedLayout` 和 `layoutRows` computed

**文件：** `FormBuilder.vue`

在 `<script setup>` 中现有 computed 位置（props 定义之后、`RenderField` 之后、`formRef` / `localModel` 之前）新增两个 computed：

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

const layoutRows = computed(() => {
  const cols = resolvedLayout.value.cols
  const defaultSpan = 24 / cols
  const rows: { fields: FormField[] }[] = []
  let currentRow: FormField[] = []
  let acc = 0
  for (const field of props.fields) {
    const span = field.span ?? defaultSpan
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

**注意：** 模板中原来使用 `props.layout` 和 `typeof layout === 'object' && 'cols' in layout` 的判断，全部改为使用 `resolvedLayout`。`layout` 是 props，在 `<script>` 中用 `props.layout` 访问，模板中直接用 `layout`。

**验证：** `npx vue-tsc --noEmit` 无错误

### Task 2：模板统一为 `el-row` + `v-for layoutRows`

**文件：** `FormBuilder.vue`

将整个 `<el-form>` 内的模板替换为：

```vue
<el-form ref="formRef" :model="localModel" :label-width="labelWidth || '80px'" :label-position="labelPosition" style="width: 100%">
  <el-row v-for="(row, ri) in layoutRows" :key="ri" :gutter="resolvedLayout.gap" style="width: 100%">
    <el-col v-for="field in row.fields" :key="field.prop || field.label" :span="field.span ?? (24 / resolvedLayout.cols)">
      <el-form-item :label="field.label" :prop="field.prop" :rules="field.rules">
        <slot v-if="field.type === 'slot'"
          :name="field.slotName"
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

**注意：**
- 移除原来的 `v-if="typeof layout === 'object' && 'cols' in layout"` grid 分支
- 移除原来的 `v-else` single/double 分支
- 所有 field 渲染逻辑保持不变（slot / render-field / setFieldValue / setModelField / handleChange 调用不变）
- `field.span ?? (24 / resolvedLayout.cols)` 确保未设置 span 的字段按列数均分

### Task 3：移除不再使用的变量

**文件：** `FormBuilder.vue`

检查模板中原来 `gutter` 变量（如果有引用到）是否需要移除。原来 grid 分支中：
```vue
<el-row v-if="typeof layout === 'object' && 'cols' in layout" :gutter="layout.gap || 16" ...>
```
改为使用 `resolvedLayout.gap`，不再需要直接从 `layout` 读取 gap。

不需要修改 `layout` props 的定义，也不需要修改 `FormBuilderProps` 接口。

### Task 4：新增测试

**文件：** `FormBuilder.test.ts`

新增 `describe('FormBuilder - span 字段跨列')` 块，包含以下测试：

1. **double 布局无 span 时每行 2 个字段**
   - `fields: [4 个 input]` + `layout: 'double'`
   - 断言：渲染 2 行，每行 2 个 `el-col`，每个 `el-col` 的 span 属性为 12

2. **span: 24 字段独占一行**
   - `fields: [textarea(span:24), input, input]` + `layout: 'double'`
   - 断言：第 1 行 1 个 col(span=24)，第 2 行 2 个 col(span=12)

3. **span: 16 + span: 8 混合跨列**
   - `fields: [{span:16}, {span:8}, {span:12}, {span:12}]` + `layout: 'double'`
   - 断言：第 1 行 2 个 col(span=16, span=8)，第 2 行 2 个 col(span=12, span=12)

4. **single 布局行为不变（回归）**
   - `fields: [input, input, input]` + `layout: 'single'`
   - 断言：3 行，每行 1 个 col(span=24)

5. **{ cols: 3 } 布局中 span: 24 独占一行**
   - `fields: [{span:24}, {span:8}, {span:8}, {span:8}]` + `layout: { cols: 3 }`
   - 断言：第 1 行 1 个 col(span=24)，第 2 行 3 个 col(span=8)

**断言方法：**
- 使用 `wrapper.findAll('.el-row')` 获取所有行
- 对每行使用 `row.findAll(':scope > .el-col')` 获取列
- 使用 `col.attributes('span')` 获取 span 属性值（Element Plus 的 `el-col` 渲染为带有 `span` class 和 `--el-col-span` CSS 变量）

### Task 5：运行全部测试 + 类型检查

```bash
cd mangban-ui
npx vitest run src/components/business/__tests__/FormBuilder.test.ts
npx vue-tsc --noEmit
```

### Task 6：提交

```bash
git add mangban-ui/src/components/business/FormBuilder.vue mangban-ui/src/components/business/__tests__/FormBuilder.test.ts
git commit -m "feat: FormBuilder span 字段跨列支持，统一布局渲染为 grid 模式"
```