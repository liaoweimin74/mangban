## Design Summary

本次变更的目标是编写一套前端公共业务组件，提高开发效率和界面一致性。基于现有项目（Vue3 + Element Plus + TypeScript + Pinia + Axios）技术栈，开发三个核心业务组件：

1. **SearchTable — 列表页组件**：查询筛选栏 + 表格显示 + 分页导航
2. **FormBuilder — 表单组件**：字段定义驱动、布局灵活、自动绑定数据
3. **ReferencePicker — 引用查找组件**：类似下拉选择但弹出数据选择框，支持搜索查询

## Alternatives Considered

### 方案 A：0代码生成（完整组件化方案，本次采用）
- **做法**：将三个功能封装为独立 Vue 组件，通过 props/defineProps 定义字段、查询、显示逻辑，对外暴露勾子事件。
- **优点**：
  - 组件化开发，在现有 UserPage.vue 中可直接替换
  - 声明式配置，减少重复样板代码
  - 类型安全，支持 TypeScript 泛型
  - 与现有 Element Plus 生态无缝集成
- **缺点**：
  - 需要一定的学习成本理解组件配置
  - 灵活性不如手写模板，极端自定义场景需 fallback

### 方案 B：指令式辅助函数（轻量方案）
- **做法**：不封装组件，而是提供 useSearchTable、useFormBuilder 等 composable 函数，返回响应式数据和方法
- **优点**：轻量，不侵入模板
- **缺点**：模板仍需手写大量 el-table、el-form 标记，重复代码未消除
- **为何未采用**：用户需要的是减少模板重复，composable 无法解决模板层面的重复

### 方案 C：低代码配置平台（重型方案）
- **做法**：开发一个可视化配置平台，拖拽生成页面
- **优点**：配置化程度最高
- **缺点**：
  - 开发成本极高，与当前项目阶段不匹配
  - 过度设计
  - 维护成本高
- **为何未采用**：YAGNI — 当前需求仅需三个组件，不需要完整低代码平台

## Agreed Approach

采用 **方案 A：完整组件化方案**。

理由：
1. 用户已在 UserPage.vue 中展示了典型的 CRUD 页面模式（搜索栏 + 表格 + 分页 + 对话框），这三个组件直接覆盖该模式
2. 组件封装后，新页面只需声明配置即可，无需重复编写搜索栏、分页、弹框等模板代码
3. 组件与现有 Element Plus 组件协作，不引入额外依赖
4. 渐进式采用 — 新页面用组件，旧页面可逐步迁移

## Key Decisions

### SearchTable 组件设计
- **查询字段**：通过 `searchFields` prop 定义字段数组（类型、标签、占位符、默认值）
- **显示列**：通过 `columns` prop 定义列数组（prop、标签、宽度、格式化、自定义模板）
- **操作列**：通过 `actionButtons` prop 定义操作按钮（文本、权限标识、点击事件、确认提示）
- **数据绑定**：自动调用 `fetchApi` 获取数据，自动绑定到表格
- **搜索/重置/导出**：三个图标按钮靠右，搜索触发查询，重置清空查询条件并重新查询，导出触发 export 事件
- **分页**：内置 el-pagination，自动绑定 total/page/size
- **插槽**：表格上方预留操作栏插槽，表格下方预留扩展插槽

### FormBuilder 组件设计
- **字段定义**：通过 `fields` prop 定义字段数组（类型、标签、字段名、占位符、规则、选项）
- **布局定义**：支持 `layout` prop 定义分栏布局（单列、双列、栅格）
- **数据绑定**：通过 `modelValue` / `update:modelValue` 实现 v-model 双向绑定
- **表单验证**：集成 Element Plus 表单验证规则
- **支持类型**：input、select、tree-select、switch、date-picker、radio、checkbox、textarea、slot（自定义插槽）

### ReferencePicker 组件设计
- **交互方式**：点击输入框触发下拉弹窗，弹窗内嵌数据查询表格
- **查询能力**：弹窗内提供搜索输入框，输入条件后查询服务端数据
- **选择返回**：选中一行后关闭弹窗，将选中数据回填到输入框
- **多选/单选**：支持 single/multiple 模式
- **显示字段**：配置回填到输入框显示的字段（如 `displayField: 'nickname'`）
- **返回字段**：配置 v-model 绑定的值字段（如 `valueField: 'id'`）

## Open Questions

1. 导出功能的具体实现 — 是前端导出当前列表数据，还是调用后端导出接口？
2. 表单组件的验证规则是否需要支持异步校验？
3. ReferencePicker 的弹窗表格列是否可自定义？