## MODIFIED Requirements

### Requirement: SearchTable 应支持 treeProps prop 配置

SearchTableProps 新增 `treeProps?: TreeTableProps` 字段：

```typescript
export interface TreeTableProps {
  rowKey: string
  children: string
  defaultExpandAll?: boolean
}
```

当 treeProps 存在时：
- el-table 透传 `row-key`, `tree-props`, `default-expand-all`
- 隐藏分页组件
- 不调用 `fetchApi` 的分页参数（tree 数据通常全量返回）