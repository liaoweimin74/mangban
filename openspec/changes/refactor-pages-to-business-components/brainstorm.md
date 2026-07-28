# Brainstorm: 系统列表/表单页面重构为业务组件

## 探索发现

### 当前状态

项目已有业务组件库：`src/components/business/`，包含 SearchTable、FormBuilder、ReferencePicker。UserPageEx.vue 已用业务组件重构（作为参考模板）。

需改造页面及分析：

| 页面 | 搜索 | 表格 | 分页 | 弹窗 CRUD | 特殊功能 |
|------|------|------|------|-----------|----------|
| UserPage.vue | ✅ username/nickname/orgId/status | ✅ el-table 8列 | ✅ el-pagination | ✅ 新增/编辑/删除 | 状态开关, 重置密码, 角色/组织下拉 |
| RolePage.vue | ✅ roleName/status | ✅ el-table 5列 | ✅ el-pagination | ✅ 新增/编辑/删除 | 分配菜单弹窗（树形权限） |
| MenuPage.vue | ❌ 无搜索 | ✅ el-table 树形 | ❌ 树无分页 | ✅ 新增/编辑/删除 | 树形表格, 目录/菜单/按钮 3类型 |
| DictPage.vue | ✅ 类型+数据双表格 | ✅ el-table ×2 | ✅ el-pagination ×2 | ✅ 类型+数据各CRUD | 主从双表格联动 |
| OrgPage.vue | ❌ 无搜索 | ✅ el-table 树形 | ❌ 树无分页 | ✅ 新增/编辑/删除 | 树形表格, 新增子组织 |

**排除的页面：**
- LoginPage, NotFoundPage, DashboardPage, ProfilePage（无CRUD）
- UserPageEx.vue（已重构）

## 决策

### 方案选择

**方案 A: 全部改为 SearchTable**（推荐）
- 对 UserPage, RolePage, DictPage 使用 SearchTable + FormBuilder
- 对 MenuPage, OrgPage 使用 SearchTable（树形模式，需扩展 SearchTable 支持 tree props）
- 优势：统一代码风格，减少维护成本
- 代价：SearchTable 需扩展支持树形表格（el-table 的 row-key, tree-props, default-expand-all）

**方案 B: 仅改列表页**
- 只改有搜索+分页的页面（UserPage, RolePage, DictPage）
- MenuPage, OrgPage 保持原样
- 优势：风险低
- 劣势：代码不统一

**选择方案 A**，理由：
- SearchTable 已有树形支持的基础（el-table 透传大部分属性）
- 只需添加 `treeProps` prop 配置
- 工作量：扩展 SearchTable + 改造 4 个页面（UserPage 直接用 UserPageEx 替换路由）

### 改造策略

1. **UserPage.vue** → 路由已指向 UserPageEx.vue，删除 UserPage.vue 即可
2. **RolePage.vue** → SearchTable + FormBuilder，分配菜单弹窗保留
3. **MenuPage.vue** → SearchTable 树形模式，el-tree-select 选择上级菜单
4. **DictPage.vue** → 两个 SearchTable（类型 + 数据），联动选择
5. **OrgPage.vue** → SearchTable 树形模式

### SearchTable 扩展

新增 prop: `treeProps?: { rowKey: string; children: string; defaultExpandAll: boolean }`
- 有 treeProps → 启用 el-table 树形渲染
- 无 treeProps → 普通列表

### 风险

- RolePage 分配菜单弹窗逻辑复杂，不纳入 FormBuilder，保留自定义弹窗
- DictPage 双表格联动：两个 SearchTable 实例共享选中状态
- MenuPage 的 menuType 影响表单字段显示（目录/菜单/按钮不同字段）