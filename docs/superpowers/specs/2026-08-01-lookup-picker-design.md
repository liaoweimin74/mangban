# LookupPicker 组件设计

## 目标

在表单中经常需要从其他表查找选择数据，并将选中行的多个字段带回当前表单。现有 `ReferencePicker` 只支持单值绑定（`modelValue` 存一个 `valueField` 值），无法满足多字段回填需求。本次新增 `LookupPicker` 组件，专用于「选择一行 → 填充多个表单字段」场景，并与 `FormBuilder` 深度集成。

## 方案：独立 LookupPicker 组件 + FormBuilder `lookup` 类型

### 整体架构

```
LookupPicker.vue            ← 新增，独立弹窗，处理选回多字段
FormBuilder.vue             ← 修改，新增 type: 'lookup' 分支
FormField.type              ← 修改，增加 'lookup' 枚举
ReferencePicker.vue         ← 不动，后续标记 deprecated
```

### LookupPicker Props

```typescript
interface LookupPickerProps {
  /** v-model 绑定选中行数据（整个行对象或 returnFields 映射后的子集） */
  modelValue: Record<string, any> | null

  /** 弹窗表格列定义（复用现有 TableColumn 类型） */
  columns: TableColumn[]

  /** 数据获取函数，接收分页+关键字参数 */
  fetchApi: (
    params: QueryParams & { keyword?: string },
  ) => Promise<{ rows: any[]; total: number }>

  /**
   * 字段映射：选中行的 sourceField → 表单的 targetField
   * 例：{ name: 'blindName', code: 'blindCode', spec: 'blindSpec' }
   * 选中后自动按此映射填充表单多个字段
   */
  returnFields?: Record<string, string>

  /**
   * 输入框显示字段，默认取 columns 第一个非 selection 列的 prop
   * 如 columns 第一列是 `{ prop: 'code', label: '编号' }`，则默认显示 row.code
   */
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

### LookupPicker Emits

```typescript
interface LookupPickerEmits {
  'update:modelValue': [value: Record<string, any> | null]
  /** 选中行时触发，返回完整行数据（不经过 returnFields 映射） */
  'select': [row: any]
  /** 清空时触发 */
  'clear': []
}
```

### LookupPicker Expose

```typescript
{
  openDialog: () => void   // 编程式打开弹窗
  closeDialog: () => void  // 编程式关闭弹窗
}
```

### 弹窗交互

弹窗内部实现与 `ReferencePicker` 类似但独立：

1. **输入框**：只读，点击后打开弹窗。显示 `displayField` 对应的值。右侧有搜索图标按钮和清除按钮
2. **弹窗内容**：
   - 顶部：搜索框 + 搜索按钮（`showSearch=true` 时显示）
   - 中间：`el-table`，列由 `columns` 定义；多选模式下显示复选框列
   - 底部：`el-pagination` 分页
3. **选中行为**：
   - **单选**：点击行直接选中并关闭弹窗，`modelValue` 更新为**单行对象**
   - **多选**：勾选多行后点"确定"关闭，`modelValue` 更新为**行对象数组**；点"取消"关闭不改变值
4. **多选时的 `returnFields` 行为**：`modelValue` 为数组时，`returnFields` 的每个目标字段取数组第一行的对应值（预览显示第一个选中项）
5. **清除**：点击输入框清除图标，`modelValue` 置 null（单选）或 []（多选），对应 `returnFields` 目标字段一并清空

### FormBuilder 集成

#### 类型变更

`FormField.type` 新增 `'lookup'` 枚举：

```typescript
interface FormField {
  type:
    | 'input' | 'select' | 'tree-select' | 'switch'
    | 'date-picker' | 'radio' | 'checkbox' | 'textarea'
    | 'slot'
    | 'lookup'                    // ← 新增
  // ...
  props?: Record<string, any>     // lookup 的所有专有配置都在这里
}
```

#### render-field 分支

`FormBuilder` 的 `RenderField` 组件新增 `'lookup'` 分支：

```typescript
case 'lookup':
  return h(LookupPicker, {
    modelValue: v,
    'onUpdate:modelValue': (val: Record<string, any> | null) => {
      // 1. 更新自身字段
      onInput(val)
      // 2. 批量更新表单其他字段
      if (f.props?.returnFields && val) {
        for (const [sourceField, targetField] of Object.entries(f.props.returnFields)) {
          localModel[targetField] = val[sourceField]
        }
      }
      // 3. 清空时清理目标字段
      if (!val && f.props?.returnFields) {
        for (const targetField of Object.values(f.props.returnFields)) {
          localModel[targetField] = null
        }
      }
    },
    ...f.props,
  })
```

#### 使用示例

```typescript
// 在 FormConfig.fields 中
{
  type: 'lookup',
  label: '盲板',
  prop: 'selectedBlind',
  props: {
    fetchApi: fetchBlindList,
    columns: [
      { prop: 'code', label: '编号', width: 120 },
      { prop: 'name', label: '名称', minWidth: 140 },
      { prop: 'spec', label: '规格', width: 100 },
    ],
    returnFields: {
      code: 'blindCode',
      name: 'blindName',
      spec: 'blindSpec',
    },
    displayField: 'code',
    showSearch: true,
    placeholder: '请选择盲板',
  },
},
// 自动被填充的字段
{ type: 'input', label: '盲板编号', prop: 'blindCode', disabled: true },
{ type: 'input', label: '盲板名称', prop: 'blindName', disabled: true },
{ type: 'input', label: '盲板规格', prop: 'blindSpec', disabled: true },
```

### 代码组织

```
mangban-ui/src/components/business/
├── LookupPicker.vue         ← 新增
├── ReferencePicker.vue      ← 保持不动（后续标记 deprecated）
├── FormBuilder.vue          ← 修改：render-field 新增 lookup 分支
├── types.ts                 ← 修改：FormField.type 增加 'lookup'
├── index.ts                 ← 修改：导出 LookupPicker
└── __tests__/
    ├── LookupPicker.test.ts ← 新增
    └── ReferencePicker.test.ts ← 不动
```

### 向后兼容

- 现有 `ReferencePicker` 行为完全不变，使用方无影响
- `FormBuilder` 现有类型和渲染逻辑不变，仅新增 `'lookup'` 分支
- 所有业务页面无需修改

### 变更范围

| 文件 | 变更 |
|------|------|
| `LookupPicker.vue` | 新增：独立弹窗组件，~200 行 |
| `FormBuilder.vue` | 修改：RenderField 新增 `'lookup'` case + 批量更新逻辑 |
| `types.ts` | 修改：`FormField.type` 增加 `'lookup'` |
| `index.ts` | 修改：导出 `LookupPicker` |
| `LookupPicker.test.ts` | 新增：组件测试 |

