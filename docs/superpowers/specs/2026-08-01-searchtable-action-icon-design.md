# SearchTable 操作列图标按钮混用设计

## 背景

SearchTable 组件操作列当前全部渲染为文本按钮。当按钮数量多且标签较长时（如"通盲切换"、"占用/释放"、"新增子节点"），操作列宽度膨胀（典型场景 230-290px），挤占数据显示空间，不美观。

## 目标

在不破坏现有 API 和页面行为的前提下，支持图标按钮与文本按钮混用，大幅缩减操作列宽度（40-50%），同时保留操作可发现性（hover tooltip 显示文字）。

## 方案：逐按钮 `icon` 字段驱动

### 核心规则

- `ActionButton.icon` 有值 -> 渲染为圆形图标按钮，hover 显示 tooltip（label 文字）
- `ActionButton.icon` 无值 -> 渲染为文本按钮（当前行为不变）
- `maxVisibleButtons` 语义不变，仍控制"前 N 个内联显示，剩余折叠到下拉"
- "更多"下拉菜单中的按钮始终以文本形式显示 `btn.label`，不受 icon 影响

### 使用示例

```ts
// 全图标化 - 最紧凑
const pointActionButtons: ActionButton[] = [
  { label: '通盲切换', icon: Switch, type: 'text', onClick: ... },
  { label: '占用/释放', icon: Lock, type: 'text', onClick: ... },
]

// 混用 - 无公认图标的按钮保持文本
const actionButtons: ActionButton[] = [
  { label: '重置密码', type: 'warning', confirm: '确定重置密码吗？', onClick: ... },
]
```

## 详细设计

### §1 类型变更与渲染逻辑

#### `ActionButton.icon` 类型变更

```ts
import type { Component } from 'vue'

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

#### 渲染规则

| 条件 | 渲染方式 |
|------|---------|
| `btn.icon` 有值 + 无 `confirm` | `<el-tooltip>` 包裹 `<el-button :icon circle>` |
| `btn.icon` 有值 + 有 `confirm` | `<el-tooltip>` 包裹 `<el-popconfirm>` 包裹 `<el-button :icon circle>` |
| `btn.icon` 无值 | 当前文本按钮逻辑，完全不变 |

tooltip 延迟 200ms 显示，避免鼠标快速划过时闪烁。

#### 模板结构（icon + confirm 组合）

```html
<el-popconfirm :title="btn.confirm" @confirm="btn.onClick(row)">
  <template #reference>
    <el-tooltip :content="btn.label" placement="top" :show-after="200">
      <el-button :icon="btn.icon" circle size="small" :type="btn.type" v-permission="btn.permission" />
    </el-tooltip>
  </template>
</el-popconfirm>
```

### §2 操作列宽度计算

#### 新宽度公式

逐按钮按类型累加，替代当前一刀切的 `visible * 70`：

```js
const actionColumnWidth = computed(() => {
  const buttons = visibleButtons.value
  const hasMore = resolvedActionButtons.value.length > props.maxVisibleButtons

  let width = 0
  for (const btn of buttons) {
    if (btn.icon) {
      width += 32  // 圆形图标按钮 ~28px + gap 4px
    } else {
      // 文本按钮：按字数估算，保底 50px
      width += Math.max(btn.label.length * 14 + 24, 50)
    }
  }
  if (hasMore) width += 60  // "更多"下拉
  width += 24  // 左右 padding

  return Math.ceil(width) + 'px'
})
```

#### 效果对比

| 页面 | 按钮配置 | 旧宽度 | 新宽度 | 节省 |
|------|---------|--------|--------|------|
| 用户管理 | 编辑(文本) + 删除(文本) + 重置密码(文本) | 230px | ~142px | 38% |
| 用户管理(全图标化) | 编辑(icon) + 删除(icon) + 重置密码(icon) | 230px | ~128px | 44% |
| 隔离点管理 | 编辑(文本) + 删除(文本) + 通盲切换(icon) + 占用/释放(icon) | 290px | ~166px | 43% |
| 隔离点管理(全图标化) | 4 个全 icon | 290px | ~152px | 48% |
| 装置层级树 | 新增子节点(文本) + 编辑(文本) + 删除(文本) | 230px | ~142px | 38% |

### §3 默认操作按钮图标化

#### `getDefaultActions()` 变更

`formConfig` 模式下自动生成的"编辑"和"删除"按钮默认配上 Element Plus 图标，使所有使用 `formConfig` 的页面自动获得紧凑的操作列：

```ts
import { Edit, Delete } from '@element-plus/icons-vue'

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

#### 向后兼容性

- 现有页面如果不改 `actionButtons`，默认按钮自动变成图标化（"编辑"✏️、"删除"🗑️），文本标签移到 tooltip
- 现有页面如果传了自定义 `actionButtons`，行为不变（按钮有没有 icon 取决于是否配了 `icon` 字段）
- 不涉及 breaking change，仅是视觉上"编辑/删除"从文本变为图标

#### 行为对照

| 场景 | 旧 | 新 |
|------|-----|-----|
| `formConfig` 模式，无自定义按钮 | "编辑" "删除" 文本按钮 | ✏️ 🗑️ 图标按钮 + tooltip |
| `formConfig` + `actionButtons: [{ label: '重置密码' }]` | "编辑" "删除" "重置密码" 三个文本 | ✏️ 🗑️ 图标 + "重置密码"文本 |
| 无 `formConfig`，只有 `actionButtons` | 纯文本 | 按 `icon` 字段混用 |
| 既无 `formConfig` 也无 `actionButtons` | 无操作列 | 无操作列（不变）|

### §4 模板实现与文件变更范围

#### 变更文件清单

| 文件 | 变更内容 |
|------|---------|
| `src/components/business/types.ts` | `ActionButton.icon` 类型 `string` -> `Component` |
| `src/components/business/SearchTable.vue` | 操作列模板重构 + 宽度计算公式 + 新增图标 import |
| `src/components/business/__tests__/SearchTable.test.ts` | 补充图标按钮渲染、宽度计算的测试用例 |

#### 模板变更细节

操作列内联按钮区域用一个 `v-for` 遍历 `visibleButtons`，内部按 `btn.icon` 和 `btn.confirm` 四分支渲染：

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
      <el-button text size="small" :type="btn.type" :class="btn.class" v-permission="btn.permission">
        {{ btn.label }}
      </el-button>
    </template>
  </el-popconfirm>

  <!-- 纯文本 (当前逻辑不变) -->
  <el-button v-else size="small" :type="btn.type || 'text'" v-permission="btn.permission" @click="btn.onClick(row)">
    {{ btn.label }}
  </el-button>
</template>
```

#### 不变的部分

- "更多"下拉菜单逻辑完全不变，始终以文本形式显示 `btn.label`
- `v-permission` 指令行为不变
- `el-table-column` 的 `fixed="right"` 不变

#### 图标 import

SearchTable.vue 顶部新增：

```ts
import { Search, Refresh, Download, Plus, ArrowDown, Edit, Delete } from '@element-plus/icons-vue'
```

`Edit`、`Delete` 仅用于 `getDefaultActions()` 内部默认按钮，不暴露给外部。

## 测试要点

- 图标按钮渲染：`icon` 有值时渲染为 circle button + tooltip
- 文本按钮渲染：`icon` 无值时渲染为 text button（不变）
- 宽度计算：图标按钮 32px，文本按钮 `max(label.length * 14 + 24, 50)`
- `maxVisibleButtons` 折叠行为不变
- "更多"下拉菜单始终文本显示
- 默认按钮图标化：`formConfig` 模式下"编辑"有 `Edit` icon，"删除"有 `Delete` icon
- 向后兼容：无 icon 的按钮行为完全不变
