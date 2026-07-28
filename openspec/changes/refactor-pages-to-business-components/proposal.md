# Proposal: 系统列表/表单页面重构为业务组件

## Why

当前 RolePage, MenuPage, DictPage, OrgPage 使用原始 el-table + el-dialog 模式，大量重复代码（搜索栏、表格、分页、弹窗 CRUD）。UserPageEx 已证明 SearchTable + FormBuilder 声明式配置可将代码量减少 40%+ 且保持可维护性。统一所有 CRUD 页面到业务组件体系。

## What Changes

1. **SearchTable 扩展** — 新增 `treeProps` prop 支持树形表格（MenuPage, OrgPage）
2. **UserPage.vue 删除** — 路由已指向 UserPageEx.vue
3. **RolePage.vue 重构** — SearchTable + FormBuilder，分配菜单弹窗保留
4. **MenuPage.vue 重构** — SearchTable 树形模式 + FormBuilder + onChange 控制字段显隐
5. **DictPage.vue 重构** — 两个 SearchTable 的字典类型/数据双表格联动
6. **OrgPage.vue 重构** — SearchTable 树形模式，字段映射修正确保

## Capabilities

**New Capabilities:**
- `search-table-tree` — SearchTable 树形表格支持

**Modified Capabilities:**
- `search-table` — 扩展 treeProps prop
- `form-builder` — 确认 onChange 回调对条件字段显隐的支持

## Impact

- 修改文件：SearchTable.vue, types.ts, RolePage.vue, MenuPage.vue, DictPage.vue, OrgPage.vue
- 删除文件：UserPage.vue（路由不变）
- 无 API 变更
- 无新依赖
- 无数据库变更
