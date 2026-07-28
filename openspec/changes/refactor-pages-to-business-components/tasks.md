# Tasks: 系统列表/表单页面重构为业务组件

## 1. SearchTable 扩展 treeProps

- [ ] 1.1 types.ts: 新增 TreeTableProps 接口 + SearchTableProps 加 treeProps 字段
- [ ] 1.2 SearchTable.vue: 模板中 el-table 透传 treeProps（v-bind="treeProps"）
- [ ] 1.3 SearchTable.vue: treeProps 存在时隐藏分页组件
- [ ] 1.4 SearchTable.test.ts: 新增树形表格测试（验证 row-key, tree-props 透传, 分页隐藏）
- [ ] 1.5 运行测试确认无回归

## 2. UserPage 删除

- [ ] 2.1 删除 UserPage.vue 文件
- [ ] 2.2 确认 router/index.ts 已指向 UserPageEx.vue

## 3. RolePage 重构

- [ ] 3.1 用 SearchTable 替代搜索/表格/分页（searchFields: roleName, status）
- [ ] 3.2 用 FormBuilder (formConfig) 替代新增/编辑弹窗（fields: roleName, roleCode, description）
- [ ] 3.3 actionButtons 添加「分配菜单」按钮（保留自定义弹窗逻辑）
- [ ] 3.4 删除原始模板代码

## 4. MenuPage 重构

- [ ] 4.1 用 SearchTable treeProps 替代树形表格（rowKey='id', children='children', defaultExpandAll）
- [ ] 4.2 用 FormBuilder (formConfig) 替代新增/编辑弹窗
- [ ] 4.3 处理 menuType onChange 控制字段显隐（目录/菜单/按钮不同字段）
- [ ] 4.4 删除原始模板代码

## 5. DictPage 重构

- [ ] 5.1 上部字典类型：SearchTable (searchFields: dictName, dictCode)
- [ ] 5.2 上部 FormBuilder (formConfig) 替代类型新增/编辑弹窗
- [ ] 5.3 下部字典数据：SearchTable (searchFields: label, value)
- [ ] 5.4 下部 FormBuilder (formConfig) 替代数据新增/编辑弹窗
- [ ] 5.5 实现 selectedType 联动：选中类型时数据表格根据 dictCode 加载
- [ ] 5.6 删除原始模板代码

## 6. OrgPage 重构

- [ ] 6.1 用 SearchTable treeProps 替代树形表格
- [ ] 6.2 用 FormBuilder 替代新增/编辑弹窗（字段映射 name→orgName, code→orgCode）
- [ ] 6.3 删除原始模板代码

## 7. 验证

- [ ] 7.1 运行 vitest 确认所有测试通过
- [ ] 7.2 运行 tsc --noEmit 确认类型正确（忽略预存错误）
- [ ] 7.3 确认所有路由正常
