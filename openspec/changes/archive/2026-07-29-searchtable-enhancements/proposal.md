# Proposal: SearchTable 搜索栏/按钮/字体/事件增强

## 动机

在使用 SearchTable 业务组件时发现四个不足：

1. **无法隐藏搜索栏**：MenuPage、OrgPage 等树形表格不需要搜索，但 SearchTable 强制渲染搜索卡片（即使 searchFields 为空）
2. **搜索按钮不简洁**：搜索/重置/导出按钮带文字，页面空间紧张时希望纯图标
3. **无法控制表格字体**：无 size 属性控制表格行高/字体大小
4. **DictPage 联动失效**：SearchTable 未转发 el-table row-click 事件，导致字典类型点击后右侧数据表格不显示

## 影响范围

| 文件 | 改动 |
|------|------|
| `types.ts` | SearchTableProps 新增 `showSearch`, `tableSize` |
| `SearchTable.vue` | 搜索卡片条件渲染、按钮纯图标、el-table :size、emit row-click |
| `SearchTable.test.ts` | 新增 showSearch/tableSize/rowClick 测试 |
| `MenuPage.vue` | 添加 `:show-search="false"` |
| `OrgPage.vue` | 添加 `:show-search="false"` |
| `DictPage.vue` | 联动修复（事件转发后自然生效，无需额外改动） |

## 破坏性变更

- 按钮去文字是视觉变更，但 Search 等图标已存在，用户不会丢失任何功能
- showSearch 默认 true，向后兼容
- tableSize 默认 'default'，向后兼容

## 与现有 change 的关系

本变更依赖 `refactor-pages-to-business-components`（当前 worktree 中已完成），应在该 change 合并后或在同一分支上修改。