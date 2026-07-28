# Verify: 系统列表/表单页面重构为业务组件

## 验证清单

> ⚠️ 此文件在 apply 阶段完成后填写。当前为占位模板。

### Pre-Check

- [ ] 实现提交数 > 0
- [ ] tasks.md 中有已完成的任务

### 功能验证

- [ ] RolePage: 搜索/列表/新增/编辑/删除/分配菜单 正常
- [ ] MenuPage: 树形表格显示/菜单类型切换表单/新增/编辑/删除 正常
- [ ] DictPage: 字典类型 CRUD + 字典数据联动 正常
- [ ] OrgPage: 树形表格/新增子组织/字段映射 正常
- [ ] UserPage.vue 已删除，路由指向 UserPageEx

### 组件验证

- [ ] SearchTable treeProps 树形渲染正常
- [ ] SearchTable tree mode 下分页隐藏
- [ ] SearchTable 无 treeProps 时行为不变

### 测试验证

- [ ] vitest 所有测试通过
- [ ] tsc --noEmit 无新增类型错误（忽略预存错误）
