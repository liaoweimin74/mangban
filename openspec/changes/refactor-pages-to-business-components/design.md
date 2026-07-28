# Design: 系统列表/表单页面重构为业务组件

## Context

mangban-ui 已有业务组件库（SearchTable, FormBuilder, ReferencePicker），UserPageEx.vue 已验证声明式 CRUD 可行。现有 4 个页面（RolePage, MenuPage, DictPage, OrgPage）仍使用原始 el-table + el-dialog 模式，需统一改造。UserPage.vue 由 UserPageEx 替代，删除原文件。

**约束：**
- 不新建页面，在原页面上直接修改
- 路由不变
- 保留现有 API 接口不变
- 不修改业务组件库的核心逻辑（仅扩展）

## Goals / Non-Goals

**Goals:**
- 用 SearchTable + FormBuilder 重构 4 个 CRUD 页面
- SearchTable 扩展树形表格支持（MenuPage, OrgPage）
- UserPage.vue → 删除，路由已指向 UserPageEx.vue

**Non-Goals:**
- 不改 LoginPage, NotFoundPage, DashboardPage, ProfilePage
- 不改 RolePage 分配菜单功能（保留自定义弹窗）
- 不修改后端 API

## Decisions

### D1: SearchTable 扩展 treeProps 支持树形表格

**选择**: 新增 `treeProps?: { rowKey: string; children: string; defaultExpandAll?: boolean }` prop

**替代方案**: 为树形页面保留原始 el-table — 放弃，因为统一风格收益高于扩展成本

**实现**: SearchTable 模板中 `v-bind="treeProps"` 透传到 el-table。无 treeProps 时不影响现有行为。

### D2: UserPage.vue 处理

**选择**: 直接删除 UserPage.vue（路由已指向 UserPageEx）

### D3: RolePage 分配菜单弹窗

**选择**: 表单部分用 FormBuilder，分配菜单弹窗保留自定义（树形权限分配逻辑复杂，不通用）。actionButtons 中添加「分配菜单」按钮。

### D4: DictPage 双 SearchTable

**选择**: 两个 SearchTable 实例：上面的「字典类型」和下面的「字典数据」。手动管理 selectedType 状态联动。不尝试用 FormBuilder 合并两个表单。

### D5: MenuPage 表单条件字段

**选择**: menuType 切换时（目录/菜单/按钮），字段变化由 `FormField.onChange` 回调触发 form 字段变化。或分为三个独立的 FormConfig（但字段逻辑复杂，保留条件显隐由 watch 处理）。

### D6: OrgPage 表单修复

已有 BUG 修复（字段映射 name→orgName）。只需将模板改为 SearchTable。

## SearchTable 扩展设计

```typescript
// types.ts 新增
export interface TreeTableProps {
  rowKey: string
  children: string
  defaultExpandAll?: boolean
}

// SearchTableProps 新增字段
treeProps?: TreeTableProps
```

SearchTable 模板变更：
```vue
<el-table
  :data="list"
  v-loading="loading"
  border
  v-bind="treeProps"
>
```

## 页面改造 Map

| 页面 | SearchTable | FormBuilder | 特殊处理 |
|------|-------------|-------------|----------|
| UserPage.vue | 删除文件 | — | 路由已指向 UserPageEx |
| RolePage.vue | searchFields: roleName, status | fields: roleName, roleCode, description | actionButtons: 分配菜单（自定义弹窗） |
| MenuPage.vue | treeProps: rowKey='id', children='children' | fields: parentId, menuName, menuType, path, component, icon, permission, sortOrder, visible | menuType onChange 显隐字段 |
| DictPage.vue | ×2: 字典类型 + 字典数据 | ×2: 类型表单 + 数据表单 | selectedType 联动 |
| OrgPage.vue | treeProps: rowKey='id', children='children' | fields: parentId, name(orgName映射), code(orgCode映射), sortOrder | BUG修复：字段映射 |

## Risks / Trade-offs

- [Risk] MenuPage 条件字段显隐在 FormBuilder 中不支持 → Mitigation: 用 `FormField.onChange` 回调 + watch 手动控制字段显隐，或在 FormBuilder 的 fields 动态改变
- [Risk] SearchTable treeProps + 分页冲突 → treeProps 页面应隐藏分页
- [Trade-off] 分配菜单功能不通用化 → 保留自定义弹窗，不增加 FormBuilder 复杂度

## Migration Plan

1. 扩展 SearchTable 添加 treeProps 支持
2. 扩展 FormBuilder 确认 onChange 回调传递正确
3. 逐个页面改造：RolePage → MenuPage → DictPage → OrgPage
4. 删除 UserPage.vue
5. 运行测试验证无回归
