# SearchTable 操作列图标按钮混用 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 让 SearchTable 操作列支持图标按钮与文本按钮混用，通过 `ActionButton.icon` 字段驱动，配合 tooltip 显示文字，大幅缩减操作列宽度。

**Architecture:** `ActionButton.icon` 有值时渲染为圆形图标按钮（hover tooltip 显示 label），无值时保持当前文本按钮行为。默认 CRUD 按钮（编辑/删除）自动配 icon。宽度计算改为逐按钮按类型累加。

**Tech Stack:** Vue 3 + Element Plus + Vitest + @vue/test-utils

## Global Constraints

- `ActionButton.icon` 类型从 `string` 改为 `Component`（Vue 组件类型）
- 图标按钮渲染为 `circle` + `el-tooltip`，tooltip `show-after=200`
- "更多"下拉菜单中的按钮始终以文本形式显示，不受 icon 影响
- `maxVisibleButtons` 语义不变
- 向后兼容：无 icon 的按钮行为完全不变
- 测试运行命令：`npx vitest run src/components/business/__tests__/SearchTable.test.ts`

---

### Task 1: `ActionButton.icon` 类型变更

**Files:**
- Modify: `mangban-ui/src/components/business/types.ts:41-49`

**Interfaces:**
- Produces: `ActionButton.icon?: Component`（从 `string` 改为 `Component`）

- [ ] **Step 1: 修改 `ActionButton.icon` 类型**

在 `types.ts` 顶部新增 `Component` 导入，并将 `icon` 字段类型从 `string` 改为 `Component`：

```ts
import type { Component } from 'vue'

// ... 其他 interface 不变 ...

export interface ActionButton {
  label: string
  type?: 'primary' | 'success' | 'warning' | 'danger' | 'info' | 'text'
  size?: 'small' | 'default' | 'large'
  /** Element Plus 图标组件。配置后渲染为圆形图标按钮，hover 显示 label */
  icon?: Component
  permission?: string
  confirm?: string
  onClick: (row: any) => void
}
```

- [ ] **Step 2: 验证类型不报错**

Run: `cd mangban-ui && npx vue-tsc --noEmit 2>&1 | Select-String "types.ts"`

Expected: 无 types.ts 相关的类型错误（其他文件可能有 icon 类型不匹配的警告，Task 3 会修复）

- [ ] **Step 3: Commit**

```bash
git add mangban-ui/src/components/business/types.ts
git commit -m "refactor: ActionButton.icon 类型 string -> Component"
```

---

### Task 2: 图标按钮渲染 + 默认按钮图标化 + 宽度计算（TDD）

**Files:**
- Modify: `mangban-ui/src/components/business/SearchTable.vue:96-156`（操作列模板）
- Modify: `mangban-ui/src/components/business/SearchTable.vue:197-198`（icon import）
- Modify: `mangban-ui/src/components/business/SearchTable.vue:249-256`（宽度计算）
- Modify: `mangban-ui/src/components/business/SearchTable.vue:267-289`（getDefaultActions）
- Test: `mangban-ui/src/components/business/__tests__/SearchTable.test.ts`

**Interfaces:**
- Consumes: `ActionButton.icon?: Component` from Task 1
- Produces: 图标按钮渲染（circle + tooltip）、默认按钮图标化、逐按钮宽度计算

- [ ] **Step 1: 编写失败测试 — 图标按钮渲染**

在 `SearchTable.test.ts` 的 `describe('SearchTable - 操作列')` 块之后，新增测试 describe 块：

```ts
import { Edit, Delete, Switch } from '@element-plus/icons-vue'
import type { Component } from 'vue'

describe('SearchTable - 图标按钮', () => {
  it('icon 按钮渲染为 circle button，不显示 label 文字', async () => {
    const fetchApi = vi.fn().mockResolvedValue({
      rows: [{ id: 1, name: 'test' }],
      total: 1,
    })
    const wrapper = createWrapper({
      columns: [{ prop: 'name', label: '名称' }],
      actionButtons: [
        { label: '编辑', icon: Edit as Component, onClick: () => {} },
      ],
      fetchApi,
    })
    await nextTick()
    await nextTick()
    // 图标按钮渲染为 circle，label 文字不出现在按钮文本中
    const btn = wrapper.find('.el-button.is-circle')
    expect(btn.exists()).toBe(true)
    expect(wrapper.text()).not.toContain('编辑')
  })

  it('无 icon 的按钮仍渲染为文本按钮', async () => {
    const fetchApi = vi.fn().mockResolvedValue({
      rows: [{ id: 1, name: 'test' }],
      total: 1,
    })
    const wrapper = createWrapper({
      columns: [{ prop: 'name', label: '名称' }],
      actionButtons: [
        { label: '重置密码', onClick: () => {} },
      ],
      fetchApi,
    })
    await nextTick()
    await nextTick()
    // 文本按钮不是 circle
    expect(wrapper.find('.el-button.is-circle').exists()).toBe(false)
    expect(wrapper.text()).toContain('重置密码')
  })

  it('图标按钮和文本按钮可混用', async () => {
    const fetchApi = vi.fn().mockResolvedValue({
      rows: [{ id: 1, name: 'test' }],
      total: 1,
    })
    const wrapper = createWrapper({
      columns: [{ prop: 'name', label: '名称' }],
      actionButtons: [
        { label: '编辑', icon: Edit as Component, onClick: () => {} },
        { label: '重置密码', onClick: () => {} },
      ],
      fetchApi,
    })
    await nextTick()
    await nextTick()
    expect(wrapper.find('.el-button.is-circle').exists()).toBe(true)
    expect(wrapper.text()).toContain('重置密码')
    expect(wrapper.text()).not.toContain('编辑')
  })

  it('formConfig 模式下默认编辑/删除按钮有 icon', async () => {
    const fetchApi = vi.fn().mockResolvedValue({
      rows: [{ id: 1, name: 'test' }],
      total: 1,
    })
    const wrapper = createWrapper({
      columns: [{ prop: 'name', label: '名称' }],
      formConfig: {
        fields: [{ type: 'input', label: '名称', prop: 'name' }],
        createApi: vi.fn(),
        updateApi: vi.fn(),
        deleteApi: vi.fn(),
      },
      fetchApi,
    })
    await nextTick()
    await nextTick()
    // formConfig 默认按钮应为 circle 图标按钮
    expect(wrapper.find('.el-button.is-circle').exists()).toBe(true)
    // label 不直接显示在按钮文本中
    expect(wrapper.text()).not.toContain('编辑')
  })
})
```

- [ ] **Step 2: 运行测试，确认失败**

Run: `cd mangban-ui && npx vitest run src/components/business/__tests__/SearchTable.test.ts`

Expected: 新增的 4 个图标按钮测试全部 FAIL（当前没有 circle 渲染逻辑）

- [ ] **Step 3: 实现图标按钮渲染 — 修改 import**

在 `SearchTable.vue` 的 `<script setup>` 顶部，将 import 行改为：

```ts
import { Search, Refresh, Download, Plus, ArrowDown, Edit, Delete } from '@element-plus/icons-vue'
```

- [ ] **Step 4: 实现图标按钮渲染 — 修改操作列模板**

将 `SearchTable.vue` 中操作列内联按钮区域（第 104-126 行的 `<template v-for="btn in visibleButtons">` 块）替换为四分支结构：

```html
              <template v-for="btn in visibleButtons" :key="btn.label">
                <!-- 图标 + confirm -->
                <el-popconfirm v-if="btn.icon && btn.confirm" :title="btn.confirm" @confirm="btn.onClick(row)">
                  <template #reference>
                    <el-tooltip :content="btn.label" placement="top" :show-after="200">
                      <el-button :icon="btn.icon" circle size="small" :type="btn.type" v-permission="btn.permission" />
                    </el-tooltip>
                  </template>
                </el-popconfirm>

                <!-- 图标无 confirm -->
                <el-tooltip v-else-if="btn.icon" :content="btn.label" placement="top" :show-after="200">
                  <el-button :icon="btn.icon" circle size="small" :type="btn.type" v-permission="btn.permission" @click="btn.onClick(row)" />
                </el-tooltip>

                <!-- 文本 + confirm (当前逻辑不变) -->
                <el-popconfirm v-else-if="btn.confirm" :title="btn.confirm" @confirm="btn.onClick(row)">
                  <template #reference>
                    <el-button text
                      size="small"
                      :type="btn.type"
                      :class="btn.class"
                      v-permission="btn.permission"
                    >
                      {{ btn.label }}
                    </el-button>
                  </template>
                </el-popconfirm>

                <!-- 纯文本 (当前逻辑不变) -->
                <el-button
                  v-else
                  size="small"
                  :type="btn.type || 'text'"
                  v-permission="btn.permission"
                  @click="btn.onClick(row)"
                >
                  {{ btn.label }}
                </el-button>
              </template>
```

- [ ] **Step 5: 实现默认按钮图标化 — 修改 `getDefaultActions()`**

将 `getDefaultActions()` 中的默认按钮加上 `icon`：

```ts
function getDefaultActions(): ActionButton[] {
  const btns: ActionButton[] = []
  if (props.formConfig?.updateApi) {
    btns.push({
      label: '编辑',
      icon: Edit,
      type: 'text',
      permission: props.formConfig.editPermission,
      onClick: (row) => handleEdit(row),
    })
  }
  if (props.formConfig?.deleteApi) {
    btns.push({
      label: '删除',
      icon: Delete,
      type: 'text',
      confirm: '确定删除该记录吗？',
      permission: props.formConfig.deletePermission,
      onClick: (row) => handleDelete(row),
    })
  }
  return btns
}
```

- [ ] **Step 6: 实现宽度计算 — 修改 `actionColumnWidth`**

将 `actionColumnWidth` computed 替换为逐按钮累加版本：

```ts
// 操作列宽度
const actionColumnWidth = computed(() => {
  const buttons = visibleButtons.value
  const hasMore = resolvedActionButtons.value.length > props.maxVisibleButtons

  let width = 0
  for (const btn of buttons) {
    if (btn.icon) {
      width += 32 // 圆形图标按钮 ~28px + gap 4px
    } else {
      // 文本按钮：按字数估算，保底 50px
      width += Math.max(btn.label.length * 14 + 24, 50)
    }
  }
  if (hasMore) width += 60 // "更多"下拉
  width += 24 // 左右 padding

  return Math.ceil(width) + 'px'
})
```

- [ ] **Step 7: 运行全部测试，确认通过**

Run: `cd mangban-ui && npx vitest run src/components/business/__tests__/SearchTable.test.ts`

Expected: 全部 PASS（包括原有测试和新增的 4 个图标按钮测试）

- [ ] **Step 8: 运行类型检查**

Run: `cd mangban-ui && npx vue-tsc --noEmit`

Expected: 无类型错误

- [ ] **Step 9: Commit**

```bash
git add mangban-ui/src/components/business/SearchTable.vue mangban-ui/src/components/business/__tests__/SearchTable.test.ts
git commit -m "feat: SearchTable 操作列支持图标按钮混用，默认编辑/删除图标化"
```

---

### Task 3: 现有页面适配图标按钮

**Files:**
- Modify: `mangban-ui/src/views/system/user/UserPage.vue:95-103`
- Modify: `mangban-ui/src/views/blindplate/IsolationPointManagePage.vue:157-174`

**Interfaces:**
- Consumes: `ActionButton.icon?: Component` from Task 1
- Produces: 现有页面操作按钮适配图标，获得紧凑操作列

- [ ] **Step 1: UserPage — 为"重置密码"按钮配置图标**

在 `UserPage.vue` 顶部新增图标 import：

```ts
import { Key } from '@element-plus/icons-vue'
```

修改 `actionButtons`：

```ts
const actionButtons: ActionButton[] = [
  {
    label: '重置密码',
    icon: Key,
    size: 'small',
    type: 'warning',
    confirm: '确定重置密码吗？',
    onClick: handleResetPassword,
  },
]
```

- [ ] **Step 2: IsolationPointManagePage — 为"通盲切换""占用/释放"按钮配置图标**

在 `IsolationPointManagePage.vue` 顶部新增图标 import：

```ts
import { Switch, Lock } from '@element-plus/icons-vue'
```

修改 `pointActionButtons`：

```ts
const pointActionButtons: ActionButton[] = [
  {
    label: '通盲切换', icon: Switch, size: 'small', type: 'text',
    onClick: async (row: any) => {
      const newStatus = row.status === 'OPEN' ? 'BLIND' : 'OPEN'
      await updateIsolationPointStatus(row.id, { status: newStatus })
      pointTableRef.value?.fetchList()
    },
  },
  {
    label: '占用/释放', icon: Lock, size: 'small', type: 'text',
    onClick: async (row: any) => {
      const newOccupy = row.occupyStatus === 'FREE' ? 'OCCUPIED' : 'FREE'
      await updateIsolationPointOccupy(row.id, { occupyStatus: newOccupy })
      pointTableRef.value?.fetchList()
    },
  },
]
```

- [ ] **Step 3: 运行类型检查确认无报错**

Run: `cd mangban-ui && npx vue-tsc --noEmit`

Expected: 无类型错误

- [ ] **Step 4: Commit**

```bash
git add mangban-ui/src/views/system/user/UserPage.vue mangban-ui/src/views/blindplate/IsolationPointManagePage.vue
git commit -m "feat: 现有页面操作按钮适配图标模式"
```

---

## Self-Review

**1. Spec coverage:**
- §1 类型变更 -> Task 1 ✓
- §1 渲染规则 -> Task 2 Step 4 ✓
- §2 宽度计算 -> Task 2 Step 6 ✓
- §3 默认按钮图标化 -> Task 2 Step 5 ✓
- §4 模板四分支 -> Task 2 Step 4 ✓
- §4 图标 import -> Task 2 Step 3 ✓
- 测试要点 -> Task 2 Step 1 (4 个测试覆盖图标渲染、混用、默认按钮) ✓
- 现有页面适配 -> Task 3 ✓

**2. Placeholder scan:** 无 TBD/TODO/"实现细节略"。每个 step 都有完整代码。✓

**3. Type consistency:**
- `ActionButton.icon?: Component` 在 Task 1 定义，Task 2/3 使用，类型一致 ✓
- `Edit, Delete, Switch, Lock, Key` 均为 `@element-plus/icons-vue` 导出的组件 ✓
- `actionColumnWidth` 返回 `string`，与 `el-table-column :width` 绑定一致 ✓
