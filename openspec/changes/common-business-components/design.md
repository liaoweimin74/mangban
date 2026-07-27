## Context

当前项目 mangban-ui 是一个基于 Vue3 + Element Plus + TypeScript + Pinia + Axios 的后台管理系统前端。现有页面（如 UserPage.vue）展示了典型的 CRUD 模式，但每个页面都重复编写大量模板代码：搜索栏、表格、分页、弹窗表单。这种重复降低了开发效率，也不利于界面一致性维护。

本次设计三个公共业务组件，将重复模式抽象为可配置组件，新页面只需声明配置即可生成完整功能界面。

## Goals / Non-Goals

**Goals:**
- 开发 SearchTable 组件，支持通过配置声明查询字段、显示列、操作按钮，自动绑定分页数据
- 开发 FormBuilder 组件，支持通过字段定义和布局配置快速生成表单，自动绑定数据
- 开发 ReferencePicker 组件，支持类似下拉选择的数据查找弹窗，通过搜索查询远端数据
- 所有组件提供 TypeScript 类型声明，支持泛型
- 保持与现有 Element Plus 组件风格一致
- 组件文件放置到 `src/components/business/` 目录

**Non-Goals:**
- 不改变现有页面代码，旧页面可逐步迁移，不强求一次性替换
- 不引入额外 UI 库依赖
- 不实现低代码可视化配置平台
- 不修改后端 API 接口格式
- 不实现导出功能的具体实现（只提供导出事件钩子，由调用方实现）

## Decisions

### 1. 组件采用 Props + Slots 模式，而非透传属性

- **决策**：每个组件定义明确的 props 接口，同时提供默认插槽和具名插槽供自定义
- **理由**：Props 声明式配置更清晰，类型安全；Slots 保留灵活性，自定义场景可 fallback 到手写模板
- **替代方案**：透传 $attrs — 虽灵活但类型不明确，且与 Element Plus 的属性命名可能冲突

### 2. SearchTable 使用 composition-api 封装数据请求逻辑

- **决策**：组件内部使用 `useRequest` 状态管理 loading、list、total、page、size
- **理由**：将数据请求逻辑封装在组件内部，外部只需传入 `fetchApi` 函数
- **替代方案**：外部传入 list/total/loading — 违背组件化原则，每个页面仍需重复写请求逻辑

### 3. FormBuilder 使用 v-model 双向绑定

- **决策**：通过 `modelValue` / `update:modelValue` 实现 v-model
- **理由**：与 Element Plus 的 el-form 绑定方式一致，父组件可随时获取表单数据
- **替代方案**：内部维护表单数据，通过事件抛出一致性差

### 4. ReferencePicker 使用 Teleport 渲染弹窗

- **决策**：弹窗内容使用 `<Teleport to="body">` 渲染
- **理由**：避免父组件 overflow:hidden 截断弹窗，保证弹窗在 z-index 最高层
- **替代方案**：使用 el-dialog append-to-body — 功能一致，但 Teleport 更直接

### 5. 组件文件结构

```
src/components/business/
├── SearchTable.vue       # 列表页组件
├── FormBuilder.vue       # 表单组件
├── ReferencePicker.vue   # 引用查找组件
└── types.ts              # 公共类型定义
```

## Risks / Trade-offs

| 风险 | 缓解措施 |
|------|----------|
| 组件配置复杂，学习成本高 | 提供完整示例和 JSDoc 注释；保持 props 命名直观 |
| 自定义场景需回退到手写模板 | 组件提供 slots 兜底，SearchTable 的 action slot 和 FormBuilder 的 field slot |
| 过多 props 导致组件膨胀 | 遵循单一职责，每个组件只做一件事；复杂配置拆分到子配置对象 |
| 与后端 API 响应格式强耦合 | 组件通过 `fetchApi` 函数解耦，调用方自行适配后端响应格式 |
| 组件性能问题（大列表渲染） | 必要时使用虚拟滚动（vxe-table 或 el-table-v2） |

## Usage Examples

### SearchTable — 用户列表页

改造 `UserPage.vue`，原来手写 210 行模板代码，用 SearchTable 缩减为约 80 行：

```vue
<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { SearchTable } from '@/components/business'
import { getUserList, deleteUser, updateUserStatus } from '@/api/user'
import type { SearchField, TableColumn, ActionButton } from '@/components/business'

const searchFields: SearchField[] = [
  { type: 'input', label: '用户名', prop: 'username', placeholder: '输入用户名' },
  { type: 'input', label: '昵称', prop: 'nickname', placeholder: '输入昵称' },
  { type: 'select', label: '状态', prop: 'status', placeholder: '选择状态',
    options: [{ label: '启用', value: 1 }, { label: '停用', value: 0 }] }
]

const columns: TableColumn[] = [
  { prop: 'username', label: '用户名', width: 120 },
  { prop: 'nickname', label: '昵称', width: 120 },
  { prop: 'email', label: '邮箱', minWidth: 160 },
  { prop: 'phone', label: '手机号', width: 140 },
  { prop: 'orgName', label: '组织机构', width: 140 },
  { prop: 'createdAt', label: '创建时间', width: 170 },
  { label: '状态', width: 80, align: 'center', slotName: 'status' }
]

const actionButtons: ActionButton[] = [
  { label: '编辑', type: 'primary', permission: 'system:user:edit',
    onClick: (row) => handleEdit(row) },
  { label: '删除', type: 'danger', confirm: '确定删除吗？',
    permission: 'system:user:delete',
    onClick: (row) => deleteUser(row.id).then(() => { ElMessage.success('删除成功'); /* refresh */ }) },
]

function handleEdit(row: any) { /* 打开编辑弹窗 */ }
</script>

<template>
  <SearchTable
    :search-fields="searchFields"
    :columns="columns"
    :action-buttons="actionButtons"
    :fetch-api="(params) => getUserList(params).then(r => r.data)"
  >
    <!-- 工具栏插槽 -->
    <el-button type="primary" v-permission="'system:user:add'">新增用户</el-button>
    <!-- 自定义状态列 -->
    <template #status="{ row }">
      <el-switch :model-value="row.status === 1"
        @change="updateUserStatus(row.id, row.status === 1 ? 0 : 1)" />
    </template>
  </SearchTable>
</template>
```

### FormBuilder — 用户表单

配合 `el-dialog` 使用，在新增/编辑弹窗中快速生成表单：

```vue
<script setup lang="ts">
import { ref, reactive } from 'vue'
import { FormBuilder } from '@/components/business'
import type { FormField } from '@/components/business'

const formRef = ref()
const form = reactive({ username: '', nickname: '', email: '', orgId: undefined, roleIds: [] })
const isEdit = ref(false)

const fields: FormField[] = [
  { type: 'input', label: '用户名', prop: 'username', placeholder: '请输入用户名',
    rules: [{ required: true, message: '请输入用户名' }],
    // 编辑时不显示用户名字段
    props: { disabled: true } },
  { type: 'input', label: '昵称', prop: 'nickname', placeholder: '请输入昵称',
    rules: [{ required: true, message: '请输入昵称' }] },
  { type: 'input', label: '邮箱', prop: 'email', placeholder: '请输入邮箱' },
  { type: 'input', label: '手机号', prop: 'phone', placeholder: '请输入手机号' },
  { type: 'tree-select', label: '组织机构', prop: 'orgId',
    treeProps: { data: orgTree, props: { label: 'label', value: 'id', children: 'children' } } },
  { type: 'select', label: '角色', prop: 'roleIds', options: roleOptions },
]

async function handleSubmit() {
  const valid = await formRef.value.validate()
  if (!valid) return
  // 调用 createUser / updateUser API
}
</script>

<template>
  <el-dialog :title="isEdit ? '编辑用户' : '新增用户'" width="600px">
    <FormBuilder
      ref="formRef"
      v-model="form"
      :fields="fields"
      layout="double"
      label-width="80px"
    />
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>
```

### ReferencePicker — 选择组织机构

替换手写的 `el-tree-select`，改为可搜索的引用查找：

```vue
<script setup lang="ts">
import { ref } from 'vue'
import { ReferencePicker } from '@/components/business'
import { getOrgList } from '@/api/org'

const orgId = ref<number>()
</script>

<template>
  <ReferencePicker
    v-model="orgId"
    value-field="id"
    display-field="label"
    :fetch-api="(params) => getOrgList(params).then(r => ({ rows: r.data, total: r.total }))"
    :columns="[
      { prop: 'label', label: '组织名称', minWidth: 160 },
      { prop: 'code', label: '编码', width: 120 },
    ]"
    placeholder="请选择组织机构"
    clearable
  />
</template>
```

多选模式：

```vue
<ReferencePicker
  v-model="roleIds"
  value-field="id"
  display-field="roleName"
  mode="multiple"
  :fetch-api="(params) => getRoleList(params).then(r => ({ rows: r.data.rows, total: r.data.total }))"
  :columns="[
    { prop: 'roleName', label: '角色名称', minWidth: 140 },
    { prop: 'roleCode', label: '角色编码', width: 120 },
  ]"
/>
```