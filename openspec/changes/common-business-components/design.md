## Context

当前项目 mangban-ui 是一个基于 Vue3 + Element Plus + TypeScript + Pinia + Axios 的后台管理系统前端。现有页面（如 UserPage.vue）展示了典型的 CRUD 模式，但每个页面都重复编写大量模板代码：搜索栏、表格、分页、弹窗表单。这种重复降低了开发效率，也不利于界面一致性维护。

本次设计三个公共业务组件，将重复模式抽象为可配置组件，新页面只需声明配置即可生成完整功能界面。

## Goals / Non-Goals

**Goals:**
- 开发 SearchTable 组件，支持通过配置声明查询字段、显示列、操作按钮，自动绑定分页数据
- 开发 FormBuilder 组件，支持通过字段定义和布局配置快速生成表单，自动绑定数据
- 开发 ReferencePicker 组件，支持类似下拉选择的数据查找弹窗，通过搜索查询远端数据
- 所有组件提供 TypeScript 类型声明，支持泛型
- 保持与现有 Element Plus 组件风格一致
- 组件文件放置到 `src/components/business/` 目录

**Non-Goals:**
- 不改变现有页面代码，旧页面可逐步迁移，不强求一次性替换
- 不引入额外 UI 库依赖
- 不实现低代码可视化配置平台
- 不修改后端 API 接口格式
- 不实现导出功能的具体实现（只提供导出事件钩子，由调用方实现）

## Decisions

### 1. 组件采用 Props + Slots 模式，而非透传属性

- **决策**：每个组件定义明确的 props 接口，同时提供默认插槽和具名插槽供自定义
- **理由**：Props 声明式配置更清晰，类型安全；Slots 保留灵活性，自定义场景可 fallback 到手写模板
- **替代方案**：透传 $attrs — 虽灵活但类型不明确，且与 Element Plus 的属性命名可能冲突

### 2. SearchTable 使用 composition-api 封装数据请求逻辑

- **决策**：组件内部使用 `useRequest` 状态管理 loading、list、total、page、size
- **理由**：将数据请求逻辑封装在组件内部，外部只需传入 `fetchApi` 函数
- **替代方案**：外部传入 list/total/loading — 违背组件化原则，每个页面仍需重复写请求逻辑

### 3. FormBuilder 使用 v-model 双向绑定

- **决策**：通过 `modelValue` / `update:modelValue` 实现 v-model
- **理由**：与 Element Plus 的 el-form 绑定方式一致，父组件可随时获取表单数据
- **替代方案**：内部维护表单数据，通过事件抛出一致性差

### 4. ReferencePicker 使用 Teleport 渲染弹窗

- **决策**：弹窗内容使用 `<Teleport to="body">` 渲染
- **理由**：避免父组件 overflow:hidden 截断弹窗，保证弹窗在 z-index 最高层
- **替代方案**：使用 el-dialog append-to-body — 功能一致，但 Teleport 更直接

### 5. 组件文件结构

```
src/components/business/
├── SearchTable.vue       # 列表页组件
├── FormBuilder.vue       # 表单组件
├── ReferencePicker.vue   # 引用查找组件
└── types.ts              # 公共类型定义
```

## Risks / Trade-offs

| 风险 | 缓解措施 |
|------|----------|
| 组件配置复杂，学习成本高 | 提供完整示例和 JSDoc 注释；保持 props 命名直观 |
| 自定义场景需回退到手写模板 | 组件提供 slots 兜底，SearchTable 的 action slot 和 FormBuilder 的 field slot |
| 过多 props 导致组件膨胀 | 遵循单一职责，每个组件只做一件事；复杂配置拆分到子配置对象 |
| 与后端 API 响应格式强耦合 | 组件通过 `fetchApi` 函数解耦，调用方自行适配后端响应格式 |
| 组件性能问题（大列表渲染） | 必要时使用虚拟滚动（vxe-table 或 el-table-v2）