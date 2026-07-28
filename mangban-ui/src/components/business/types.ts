// ============================================================
// 公共业务组件类型定义
// ============================================================

// --- 查询字段 ---

export interface SearchField {
  type: 'input' | 'select' | 'tree-select' | 'date-picker' | 'date-range'
  label: string
  prop: string
  placeholder?: string
  defaultValue?: any
  options?: { label: string; value: any }[]
  treeProps?: {
    data: any[]
    props: { label: string; value: string; children?: string }
  }
  style?: string
}

// --- 表格列 ---

export interface TableColumn {
  prop?: string
  label: string
  width?: number | string
  minWidth?: number | string
  align?: 'left' | 'center' | 'right'
  fixed?: 'left' | 'right'
  formatter?: (
    row: any,
    column: TableColumn,
    cellValue: any,
    index: number,
  ) => string
  slotName?: string
}

// --- 操作按钮 ---

export interface ActionButton {
  label: string
  type?: 'primary' | 'success' | 'warning' | 'danger' | 'info' | 'text'
  size?: 'small' | 'default' | 'large'
  icon?: string
  permission?: string
  confirm?: string
  onClick: (row: any) => void
}

// --- 查询参数 ---

export interface QueryParams {
  page: number
  size: number
  [key: string]: any
}

// --- 表单布局 ---

export type FormLayout = 'single' | 'double' | { cols: number; gap?: number }

// --- 表单字段 ---

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
  label: string
  prop: string
  placeholder?: string
  rules?: any[]
  options?: { label: string; value: any }[]
  treeProps?: {
    data: any[]
    props: { label: string; value: string; children?: string }
  }
  disabled?: boolean
  span?: number
  slotName?: string
  props?: Record<string, any>
  onChange?: (
    newVal: any,
    oldVal: any,
    formData: Record<string, any>,
  ) => boolean | Promise<boolean>
}

// --- 表单 props ---

export interface FormBuilderProps {
  fields: FormField[]
  modelValue: Record<string, any>
  layout?: FormLayout
  labelWidth?: string
  labelPosition?: 'left' | 'right' | 'top'
}

// --- 表单集成配置（SearchTable 用） ---

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
  createPermission?: string
  editPermission?: string
  deletePermission?: string
  beforeCreate?: () => boolean | Promise<boolean>
  beforeEdit?: (row: T) => boolean | Promise<boolean>
  beforeDelete?: (row: T) => boolean | Promise<boolean>
  afterCreate?: (result: any) => void
  afterUpdate?: (result: any) => void
  afterDelete?: () => void
}

// --- SearchTable props ---

export interface SearchTableProps<T = any> {
  searchFields: SearchField[]
  columns: TableColumn[]
  actionButtons?: ActionButton[]
  fetchApi: (
    params: QueryParams,
  ) => Promise<{ rows: T[]; total: number }>
  defaultPageSize?: number
  pageSizes?: number[]
  showExport?: boolean
  exportLoading?: boolean
  maxVisibleButtons?: number
  formConfig?: FormConfig<T>
  /** 是否显示搜索栏，默认 true */
  showSearch?: boolean
  /** el-table 尺寸，默认 'default' */
  tableSize?: 'small' | 'default' | 'large'
}

// --- ReferencePicker props ---

export interface ReferencePickerProps<T = any> {
  modelValue: any | any[]
  valueField: string
  displayField: string
  fetchApi: (
    params: QueryParams & { keyword?: string },
  ) => Promise<{ rows: T[]; total: number }>
  columns: TableColumn[]
  mode?: 'single' | 'multiple'
  placeholder?: string
  searchPlaceholder?: string
  disabled?: boolean
  clearable?: boolean
}
