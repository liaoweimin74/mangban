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
- 不强制 SearchTable 必须使用 FormBuilder — 两者可独立使用，也可组合使用

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

### 5. 表单字段支持 onChange 回调，可在值变更前拦截或拒绝

- **决策**：`FormField` 定义增加 `onChange?: (newVal: any, oldVal: any, formData: Record<string, any>) => boolean | Promise<boolean>` 回调
- **理由**：业务场景中常有字段联动（如选择组织后自动填充编码、选择部门后校验权限），需要同步或异步校验后决定是否接受变更
- **行为**：onChange 返回 `false` 或 `Promise<false>` 时，字段值回退到旧值；返回 `true` 或 `undefined` 时正常更新
- **替代方案**：外部 watch 监听 modelValue 变化后手动回滚 — 时序难以控制，可能触发多余请求

### 6. SearchTable 与 FormBuilder 组合：通过 formConfig 集成表单弹窗

- **决策**：SearchTable 增加 `formConfig` prop，传入 `FormBuilder` 的字段定义、API 配置后，组件内部自动管理弹窗状态和 CRUD 流程
- **默认操作列**：当 `formConfig` 存在时，操作列默认显示"新增"（工具栏）、"编辑"、"删除"三个按钮，分别对应 createApi/updateApi/deleteApi
- **自定义覆盖**：如果同时传入 `actionButtons`，则优先使用 `actionButtons`；如果 `actionButtons` 传空数组 `[]`，则隐藏操作列
- **理由**：消除每个 CRUD 页面写弹窗模板 + 表单模板 + CRUD 事件处理的大量重复代码，典型页面从 200+ 行缩至 50 行
- **替代方案**：外部自行组合 SearchTable + el-dialog + FormBuilder — 灵活但每个页面重复编写弹窗逻辑，违背组件化初衷

### 7. 操作列按钮折叠：超过 maxVisibleButtons 时收起为"更多"下拉

- **决策**：SearchTable 增加 `maxVisibleButtons` prop（默认 3），操作列中超过该数量的按钮自动折叠到 `el-dropdown` 下拉菜单中，最后显示一个向下的三角图标
- **理由**：操作列按钮数量不可控（编辑/删除/重置密码/授权/导出等），全部平铺在小列宽内会溢出或换行，折叠方案不依赖 CSS 计算，稳定可靠
- **行为**：前 N 个按钮直接显示，剩余按钮放在 el-dropdown 中，点击下拉展开；不改变按钮的 onClick、confirm 等行为逻辑
- **替代方案**：CSS 自动溢出隐藏 — 兼容性差，无法处理 el-popconfirm 的交互

### 8. 组件文件结构

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

### SearchTable + FormBuilder — 用户管理页（组合模式）

`formConfig` 传入后，SearchTable 自动集成表单弹窗和 CRUD 按钮，页面缩减到极致：

```vue
<script setup lang="ts">
import { SearchTable } from '@/components/business'
import { getUserList, createUser, updateUser, deleteUser } from '@/api/user'
import { getOrgTree } from '@/api/org'
import type { SearchField, TableColumn, FormField } from '@/components/business'

const searchFields: SearchField[] = [
  { type: 'input', label: '用户名', prop: 'username', placeholder: '输入用户名' },
  { type: 'select', label: '状态', prop: 'status', placeholder: '选择状态',
    options: [{ label: '启用', value: 1 }, { label: '停用', value: 0 }] }
]

const columns: TableColumn[] = [
  { prop: 'username', label: '用户名', width: 120 },
  { prop: 'nickname', label: '昵称', width: 120 },
  { prop: 'email', label: '邮箱', minWidth: 160 },
  { prop: 'orgName', label: '组织机构', width: 140 },
  { label: '状态', width: 80, slotName: 'status' }
]

const formFields: FormField[] = [
  { type: 'input', label: '用户名', prop: 'username', placeholder: '请输入用户名',
    rules: [{ required: true, message: '请输入用户名' }],
    props: { disabled: true } }, // 编辑时禁用
  { type: 'input', label: '昵称', prop: 'nickname', placeholder: '请输入昵称',
    rules: [{ required: true, message: '请输入昵称' }] },
  { type: 'input', label: '邮箱', prop: 'email', placeholder: '请输入邮箱' },
  { type: 'tree-select', label: '组织机构', prop: 'orgId',
    treeProps: { data: orgTree, props: { label: 'label', value: 'id' } } },
  { type: 'select', label: '角色', prop: 'roleIds', options: roleOptions },
]
</script>

<template>
  <SearchTable
    :search-fields="searchFields"
    :columns="columns"
    :fetch-api="(params) => getUserList(params).then(r => r.data)"
    :form-config="{
      fields: formFields,
      createApi: (data) => createUser(data),
      updateApi: (id, data) => updateUser(id, data),
      deleteApi: (id) => deleteUser(id),
      layout: 'double',
      labelWidth: '80px',
      dialogWidth: '600px'
    }"
  >
    <!-- 自定义状态列 -->
    <template #status="{ row }">
      <el-switch :model-value="row.status === 1"
        @change="updateUserStatus(row.id, row.status === 1 ? 0 : 1)" />
    </template>
  </SearchTable>
</template>
```

以上代码约 50 行，对比原来的 UserPage.vue 约 210 行，缩减约 75%。SearchTable 自动处理：
- 工具栏"新增用户"按钮 → 打开表单弹窗（空表单）
- 操作列"编辑"按钮 → 调用 getApi 获取详情 → 填充表单弹窗
- 操作列"删除"按钮 → 确认弹窗 → 调用 deleteApi → 刷新列表
- 表单提交 → 调用 createApi / updateApi → 关闭弹窗 → 刷新列表

操作列按钮较多时自动折叠（默认最多显示 3 个，超出部分收起在"更多"下拉中）：

```vue
<SearchTable
  :search-fields="searchFields"
  :columns="columns"
  :fetch-api="(p) => getUserList(p).then(r => r.data)"
  :form-config="formConfig"
  :action-buttons="[
    { label: '编辑', type: 'primary', onClick: (r) => handleEdit(r) },
    { label: '重置密码', type: 'warning', onClick: (r) => handleResetPwd(r) },
    { label: '分配角色', onClick: (r) => handleAssignRole(r) },
    { label: '删除', type: 'danger', confirm: '确定删除？', onClick: (r) => handleDelete(r) },
  ]"
  :max-visible-buttons="2"
/>
<!-- 结果：编辑、重置密码 直接显示，分配角色、删除 折叠到"更多▼"下拉中 -->
```

### SearchTable — 纯列表（独立使用）

如果只需要列表不绑定表单，仍可使用独立的 SearchTable：

```vue
<SearchTable
  :search-fields="searchFields"
  :columns="columns"
  :action-buttons="actionButtons"
  :fetch-api="(params) => getUserList(params).then(r => r.data)"
>
  <!-- 工具栏插槽 -->
  <el-button type="primary">新增用户</el-button>
  <!-- 自定义状态列 -->
  <template #status="{ row }">
    <el-switch :model-value="row.status === 1"
      @change="updateUserStatus(row.id, row.status === 1 ? 0 : 1)" />
  </template>
</SearchTable>
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
  // 联动：选择组织后自动校验该组织是否允许新用户注册
  { type: 'tree-select', label: '所属组织', prop: 'orgId',
    treeProps: { data: orgTree, props: { label: 'label', value: 'id' } },
    onChange: async (newVal, oldVal, formData) => {
      if (!newVal) return true
      const res = await checkOrgAvailable(newVal)
      if (!res.available) {
        ElMessage.warning('该组织暂不接受新用户')
        return false // 拒绝变更，值回退
      }
      return true
    }
  },
]
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