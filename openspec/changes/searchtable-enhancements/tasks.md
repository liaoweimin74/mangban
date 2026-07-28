# Tasks: SearchTable 搜索栏/按钮/字体/事件增强

## 1. types.ts — 类型扩展

- [ ] 1.1 SearchTableProps 新增 `showSearch?: boolean`
- [ ] 1.2 SearchTableProps 新增 `tableSize?: 'small' | 'default' | 'large'`

## 2. SearchTable.vue — 组件增强

- [ ] 2.1 搜索卡片添加 `v-if="showSearch"`（默认 true）
- [ ] 2.2 搜索/重置/导出按钮改为 `circle` 纯图标模式（去掉文字）
- [ ] 2.3 el-table 添加 `:size="tableSize"`（默认 'default'）
- [ ] 2.4 defineEmits 新增 `row-click` 事件
- [ ] 2.5 el-table 模板添加 `@row-click` 转发

## 3. SearchTable.test.ts — 测试扩展

- [ ] 3.1 showSearch=false 时搜索卡片不渲染
- [ ] 3.2 showSearch=true（默认）时搜索卡片渲染
- [ ] 3.3 tableSize 透传到 el-table 的 size 属性
- [ ] 3.4 row-click 事件触发时 emit 被调用

## 4. 页面适配

- [ ] 4.1 MenuPage: 添加 `:show-search="false"`
- [ ] 4.2 OrgPage: 添加 `:show-search="false"`

## 5. DictPage 联动验证

- [ ] 5.1 确认 row-click 事件转发后，字典数据表格正常联动显示

## 6. 最终验证

- [ ] 6.1 vitest 全部通过
- [ ] 6.2 vue-tsc 无新增类型错误
