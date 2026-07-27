## Why

当前 mangban-ui 每个业务页面（用户、角色、菜单、字典、组织等）均需重复编写搜索栏、表格、分页、弹窗表单等模板代码，导致开发效率低、界面风格不统一、维护成本高。通过抽象三个公共业务组件，新页面只需声明配置即可复用完整 CRUD 模式，减少约 60% 的重复模板代码，提升开发效率和界面一致性。

## What Changes

**SearchTable 列表页组件**
- From: 每个页面手写 el-card + el-form（搜索栏）+ el-table + el-pagination 模板，重复编写 fetchList/handleSearch/handleReset 等方法，还需额外手写弹窗表单和 CRUD 事件处理
- To: 通过声明 searchFields、columns、actionButtons 配置，组件自动渲染搜索栏、表格、分页，自动调用 fetchApi 获取数据。通过 formConfig 传入 FormBuilder 配置后，自动集成表单弹窗和 CRUD 按钮（新增/编辑/删除），actionButtons 可覆盖默认操作按钮
- Reason: 消除重复模式，提升开发效率；组合模式将典型 CRUD 页面从 210 行缩至约 50 行
- Impact: 非破坏性，新页面新用法，旧页面不变

**FormBuilder 表单组件**
- From: 每个页面手写 el-form + el-form-item 模板，逐个绑定字段
- To: 通过 fields 定义数组自动生成表单，支持 v-model 双向绑定
- Reason: 消除表单手写重复，支持布局配置
- Impact: 非破坏性

**ReferencePicker 引用查找组件**
- From: 引用字段需手写 el-select + 弹窗组合，或使用不可搜索的下拉
- To: 统一的引用查找组件，点击弹出数据选择弹窗，支持搜索查询
- Reason: 标准化引用选择交互
- Impact: 非破坏性

## Capabilities

### New Capabilities
- `search-table`: 可配置的列表页组件，包含查询筛选栏、数据表格、分页导航，支持自动数据绑定和操作列按钮
- `form-builder`: 字段定义驱动的表单组件，支持布局配置和 v-model 双向绑定
- `reference-picker`: 引用查找组件，支持搜索查询的数据选择弹窗

### Modified Capabilities

无 — 本次为纯新增组件，不修改现有功能。

## Impact

- **代码**：新增 `src/components/business/` 目录，包含 SearchTable.vue、FormBuilder.vue、ReferencePicker.vue、types.ts
- **API**：无后端 API 变更
- **依赖**：无新增 npm 依赖，全部基于现有 Element Plus + Vue3
- **团队**：前端开发人员需学习组件配置方式；后端无需配合