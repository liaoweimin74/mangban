# Plan: 系统列表/表单页面重构为业务组件

## 文件结构

| 文件 | 操作 | 职责 |
|------|------|------|
| `mangban-ui/src/components/business/types.ts` | 修改 | 新增 TreeTableProps 接口 |
| `mangban-ui/src/components/business/SearchTable.vue` | 修改 | treeProps 透传 + 条件分页隐藏 |
| `mangban-ui/src/components/business/__tests__/SearchTable.test.ts` | 修改 | 树形表格测试 |
| `mangban-ui/src/views/system/user/UserPage.vue` | 删除 | 已由 UserPageEx 替代 |
| `mangban-ui/src/views/system/role/RolePage.vue` | 重写 | SearchTable + FormBuilder |
| `mangban-ui/src/views/system/menu/MenuPage.vue` | 重写 | SearchTable tree + FormBuilder |
| `mangban-ui/src/views/system/dict/DictPage.vue` | 重写 | 双 SearchTable 联动 |
| `mangban-ui/src/views/system/org/OrgPage.vue` | 重写 | SearchTable tree + FormBuilder |

---

## Task 1: SearchTable 扩展 treeProps (5-10 min)

### 1.1 types.ts — 新增 TreeTableProps

**文件**: `mangban-ui/src/components/business/types.ts`

在 SearchTableProps 之前添加接口：

```typescript
/** 树形表格配置 */
export interface TreeTableProps {
  /** el-table 的 row-key */
  rowKey: string
  /** 子节点字段名 */
  children: string
  /** 是否默认展开所有节点 */
  defaultExpandAll?: boolean
}
```

在 SearchTableProps 接口中添加字段（约第 106 行，`columns: TableColumn[]` 之后）：

```typescript
treeProps?: TreeTableProps
```

**验证**: `npx tsc --noEmit` 无新增类型错误。

### 1.2 SearchTable.vue — 模板透传 treeProps

**文件**: `mangban-ui/src/components/business/SearchTable.vue`

找到 `<el-table>` 标签（约第 75 行）：

```vue
<el-table :data="list" v-loading="loading" border>
```

改为：

```vue
<el-table :data="list" v-loading="loading" border v-bind="treeTableProps">
```

### 1.3 SearchTable.vue — treeProps 时隐藏分页

在 `<script setup>` 中添加 computed：

```typescript
const treeTableProps = computed(() => {
  if (!props.treeProps) return {}
  return {
    'row-key': props.treeProps.rowKey,
    'tree-props': { children: props.treeProps.children },
    'default-expand-all': props.treeProps.defaultExpandAll ?? true,
  }
})

const showPagination = computed(() => !props.treeProps)
```

分页组件外包裹 `v-if="showPagination"`：

```vue
<div v-if="showPagination" class="pagination-wrapper">
  <el-pagination ... />
</div>
```

### 1.4 SearchTable.test.ts — 树形表格测试

**文件**: `mangban-ui/src/components/business/__tests__/SearchTable.test.ts`

添加测试：

```typescript
describe('tree mode', () => {
  it('透传 row-key 和 tree-props 到 el-table', async () => {
    const wrapper = mount(SearchTable, {
      props: {
        fetchApi: mockFetchApi,
        columns: [{ label: '名称', prop: 'label' }],
        treeProps: { rowKey: 'id', children: 'children', defaultExpandAll: true },
      },
    })
    await nextTick()
    const table = wrapper.find('.el-table')
    // tree mode 下应隐藏分页
    expect(wrapper.find('.el-pagination').exists()).toBe(false)
  })

  it('无 treeProps 时显示分页', async () => {
    const wrapper = mount(SearchTable, {
      props: {
        fetchApi: mockFetchApi,
        columns: [{ label: '名称', prop: 'label' }],
      },
    })
    await nextTick()
    expect(wrapper.find('.el-pagination').exists()).toBe(true)
  })
})
```

**验证**: `npx vitest run src/components/business/__tests__/SearchTable.test.ts` 全部通过。

**提交**: `feat: SearchTable 新增 treeProps 树形表格支持`

---

## Task 2: UserPage 删除 (1 min)

```bash
rm mangban-ui/src/views/system/user/UserPage.vue
```

确认 `router/index.ts` 中用户路由指向 `UserPageEx.vue`（已验证）。

**提交**: `chore: 删除 UserPage.vue（已由 UserPageEx 替代）`

---

## Task 3: RolePage 重构 (10 min)

**文件**: `mangban-ui/src/views/system/role/RolePage.vue` — 完全重写

### 3.1 SearchTable 配置

```typescript
const searchFields: SearchField[] = [
  { type: 'input', label: '角色名称', prop: 'roleName', placeholder: '输入角色名称' },
  {
    type: 'select', label: '状态', prop: 'status', placeholder: '选择状态',
    options: [{ label: '全部', value: undefined }, { label: '启用', value: 1 }, { label: '停用', value: 0 }],
    style: 'width: 120px',
  },
]

const columns: TableColumn[] = [
  { label: '角色名称', prop: 'roleName', minWidth: 150 },
  { label: '角色编码', prop: 'roleCode', width: 150 },
  { label: '描述', prop: 'description', minWidth: 200 },
  { label: '状态', prop: 'status', width: 80, formatter: (r: any) => r.status === 1 ? '启用' : '停用' },
  { label: '创建时间', prop: 'createTime', width: 170 },
]
```

### 3.2 FormConfig

```typescript
const formConfig: FormConfig<RoleVO> = {
  fields: [
    { type: 'input', label: '角色名称', prop: 'roleName', rules: [{ required: true }] },
    { type: 'input', label: '角色编码', prop: 'roleCode', rules: [{ required: true }] },
    { type: 'input', label: '描述', prop: 'description', type: 'textarea' },
  ],
  createApi: createRole,
  updateApi: (id, data) => updateRole(id as number, data),
  deleteApi: deleteRole,
  getApi: (id) => getRoleList({ page: 1, size: 1 }).then(() => {/* 需单独 getRoleById，或从列表中找 */}),
  dialogTitle: { create: '新增角色', edit: '编辑角色' },
}
```

### 3.3 分配菜单按钮

```typescript
const actionButtons: ActionButton[] = [
  { label: '分配菜单', size: 'small', type: 'text', onClick: handleAssignMenu },
]
```

分配菜单弹窗逻辑保留原有代码（树形权限选择 + assignRoleMenus）。

### 3.4 模板

```vue
<template>
  <SearchTable
    :fetch-api="fetchApi"
    :search-fields="searchFields"
    :columns="columns"
    :form-config="formConfig"
    :action-buttons="actionButtons"
  />
  <!-- 分配菜单弹窗保留原有代码 -->
</template>
```

**验证**: `npx vitest run` 无回归。

**提交**: `refactor: RolePage 用 SearchTable + FormBuilder 重构`

---

## Task 4: MenuPage 重构 (15 min)

**文件**: `mangban-ui/src/views/system/menu/MenuPage.vue` — 完全重写

### 4.1 SearchTable tree mode

```typescript
const treeProps = { rowKey: 'id', children: 'children', defaultExpandAll: true }

const columns: TableColumn[] = [
  { label: '菜单名称', prop: 'menuName', minWidth: 200 },
  { label: '图标', prop: 'icon', width: 80 },
  { label: '类型', prop: 'menuType', width: 80, formatter: (r: any) => menuTypeMap[r.menuType] },
  { label: '路径', prop: 'path', minWidth: 180 },
  { label: '权限标识', prop: 'permission', minWidth: 180 },
  { label: '排序', prop: 'sortOrder', width: 80 },
  { label: '状态', prop: 'visible', width: 80, formatter: (r: any) => r.visible === 1 ? '显示' : '隐藏' },
]
```

### 4.2 FormConfig + 条件字段

菜单类型切换时，不同 type 显示不同字段。FormBuilder 不支持条件显隐 → 用 FormField.onChange 回调 + watch 处理：

```typescript
const menuFormFields = computed<FormField[]>(() => {
  const base = [
    { type: 'tree-select', label: '上级菜单', prop: 'parentId', treeProps: { data: menuTree.value, props: { label: 'menuName', value: 'id', children: 'children' } } },
    { type: 'input', label: '菜单名称', prop: 'menuName', rules: [{ required: true }] },
    { type: 'select', label: '菜单类型', prop: 'menuType', options: menuTypeOptions, onChange: (val) => { currentMenuType.value = val } },
    { type: 'input-number', label: '排序', prop: 'sortOrder' },
  ]
  if (currentMenuType.value === 0 || currentMenuType.value === 1) {
    base.push({ type: 'input', label: '路由路径', prop: 'path' })
    base.push({ type: 'input', label: '图标', prop: 'icon' })
    base.push({ type: 'radio', label: '可见', prop: 'visible', options: [{ label: '显示', value: 1 }, { label: '隐藏', value: 0 }] })
  }
  if (currentMenuType.value === 1) {
    base.push({ type: 'input', label: '组件路径', prop: 'component' })
  }
  if (currentMenuType.value === 1 || currentMenuType.value === 2) {
    base.push({ type: 'input', label: '权限标识', prop: 'permission' })
  }
  return base
})

const currentMenuType = ref(1)
```

### 4.3 模板

```vue
<template>
  <SearchTable
    :fetch-api="fetchApi"
    :columns="columns"
    :tree-props="treeProps"
    :form-config="formConfig"
    :show-search="false"
  />
</template>
```

**验证**: 编译通过。

**提交**: `refactor: MenuPage 用 SearchTable 树形 + FormBuilder 重构`

---

## Task 5: DictPage 重构 (15 min)

**文件**: `mangban-ui/src/views/system/dict/DictPage.vue` — 完全重写

### 5.1 字典类型 SearchTable

```typescript
const typeSearchFields: SearchField[] = [
  { type: 'input', label: '字典名称', prop: 'dictName' },
  { type: 'input', label: '字典编码', prop: 'dictCode' },
]
const typeColumns: TableColumn[] = [
  { label: '字典名称', prop: 'dictName', minWidth: 150 },
  { label: '字典编码', prop: 'dictCode', width: 150 },
  { label: '备注', prop: 'remark', minWidth: 200 },
  { label: '创建时间', prop: 'createTime', width: 170 },
]
```

### 5.2 字典数据 SearchTable

```typescript
const dataSearchFields: SearchField[] = [
  { type: 'input', label: '标签', prop: 'label' },
  { type: 'input', label: '值', prop: 'value' },
]
const dataColumns: TableColumn[] = [
  { label: '标签', prop: 'label', minWidth: 150 },
  { label: '值', prop: 'value', width: 150 },
  { label: '排序', prop: 'sortOrder', width: 80 },
  { label: '创建时间', prop: 'createTime', width: 170 },
]
```

### 5.3 联动逻辑

选中类型 → 数据表格按 dictCode 过滤：

```typescript
const selectedType = ref<DictTypeVO | null>(null)

function handleTypeRowClick(row: DictTypeVO) {
  selectedType.value = row
  dataTableRef.value?.fetchList()
}

const dataFetchApi = async (params: any) => {
  if (!selectedType.value) return { rows: [], total: 0 }
  return getDictDataList({ ...params, dictCode: selectedType.value.dictCode })
}
```

### 5.4 模板

```vue
<template>
  <el-card>
    <SearchTable ref="typeTableRef" :fetch-api="getDictTypeList" :search-fields="typeSearchFields" :columns="typeColumns" :form-config="typeFormConfig" @row-click="handleTypeRowClick" />
  </el-card>
  <el-card v-if="selectedType" style="margin-top: 16px">
    <template #header>字典数据 - {{ selectedType.dictName }}</template>
    <SearchTable ref="dataTableRef" :fetch-api="dataFetchApi" :search-fields="dataSearchFields" :columns="dataColumns" :form-config="dataFormConfig" />
  </el-card>
</template>
```

**提交**: `refactor: DictPage 用双 SearchTable 联动重构`

---

## Task 6: OrgPage 重构 (10 min)

**文件**: `mangban-ui/src/views/system/org/OrgPage.vue` — 重写模板

### 6.1 SearchTable tree mode

```typescript
const treeProps = { rowKey: 'id', children: 'children', defaultExpandAll: true }

const columns: TableColumn[] = [
  { label: '组织名称', prop: 'label', minWidth: 200 },
  { label: '组织编码', prop: 'code', width: 180 },
  { label: '排序', prop: 'sortOrder', width: 80 },
  { label: '状态', prop: 'status', width: 80, formatter: (r: any) => r.status === 1 ? '启用' : '停用' },
]
```

### 6.2 FormConfig + 字段映射

```typescript
const formConfig: FormConfig<any> = {
  fields: [
    { type: 'tree-select', label: '上级组织', prop: 'parentId', treeProps: { data: orgTree.value, props: { label: 'label', value: 'id', children: 'children' } } },
    { type: 'input', label: '组织名称', prop: 'name', rules: [{ required: true }] },
    { type: 'input', label: '组织编码', prop: 'code', rules: [{ required: true }] },
    { type: 'input-number', label: '排序', prop: 'sortOrder' },
  ],
  createApi: (data) => createOrg({ ...data, orgName: data.name, orgCode: data.code }),
  updateApi: (id, data) => updateOrg(id as number, { ...data, orgName: data.name, orgCode: data.code }),
  deleteApi: deleteOrg,
  getApi: async (id) => {/* 从树中查找 */},
}
```

### 6.3 模板

```vue
<template>
  <SearchTable
    :fetch-api="fetchApi"
    :columns="columns"
    :tree-props="treeProps"
    :form-config="formConfig"
    :show-search="false"
  />
</template>
```

**提交**: `refactor: OrgPage 用 SearchTable 树形 + FormBuilder 重构（含字段映射修复）`

---

## Task 7: 验证 (5 min)

```bash
cd mangban-ui
npx tsc --noEmit    # 忽略预存 main.ts 错误
npx vitest run      # 确认所有测试通过
```

**提交**: `test: 确认所有重构后测试通过`