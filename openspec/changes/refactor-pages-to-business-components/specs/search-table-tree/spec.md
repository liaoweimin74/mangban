## ADDED Requirements

### Requirement: SearchTable SHALL support tree table rendering via treeProps prop

SearchTable 新增 `treeProps` prop，类型 `{ rowKey: string; children: string; defaultExpandAll?: boolean }`。当提供 treeProps 时，el-table 启用树形渲染，透传 row-key、tree-props、default-expand-all 属性。

#### Scenario: 树形表格渲染组织架构
- **Given** SearchTable 传入 `treeProps: { rowKey: 'id', children: 'children', defaultExpandAll: true }`
- **And** fetchApi 返回树形数据（每项含 children 数组）
- **When** 组件挂载
- **Then** 表格以树形方式展开所有节点

#### Scenario: 树形表格隐藏分页
- **Given** SearchTable 传入 `treeProps`
- **When** 组件渲染
- **Then** 不显示分页组件（树形表格无需分页）

#### Scenario: 无 treeProps 时行为不变
- **Given** SearchTable 未传入 `treeProps`
- **When** 组件渲染
- **Then** 以普通列表模式显示，带分页