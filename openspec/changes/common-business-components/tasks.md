## 1. 公共类型定义

- [x] 1.1 创建 `src/components/business/types.ts`，定义 SearchTable 的 searchFields、columns、actionButtons、formConfig 等类型接口
- [x] 1.2 定义 FormBuilder 的 field definition、layout 等类型接口
- [x] 1.3 定义 ReferencePicker 的 props 类型接口

## 2. SearchTable 列表页组件

- [x] 2.1 创建 `src/components/business/SearchTable.vue`，实现搜索栏渲染逻辑（根据 searchFields 配置渲染不同字段类型）
- [x] 2.2 实现搜索/重置/导出三个按钮，靠右对齐，分别触发查询、重置、导出事件
- [x] 2.3 实现表格渲染（根据 columns 配置渲染列，支持 formatter 和自定义 slot）
- [x] 2.4 实现操作列：formConfig 存在时默认生成"编辑"/"删除"按钮，actionButtons 显式传入时覆盖默认
- [x] 2.5 实现操作列按钮折叠：maxVisibleButtons 控制直接显示的按钮数，超出部分折叠到"更多"下拉菜单
- [x] 2.5 实现分页导航（el-pagination，绑定 total/page/size，响应 size-change 和 current-change）
- [x] 2.6 实现数据获取逻辑（onMounted 自动调用 fetchApi，search/reset/pagination 变化时重新请求）
- [x] 2.7 实现 loading 状态和 toolbar slot
- [x] 2.8 实现表单弹窗集成：formConfig 存在时渲染 el-dialog + FormBuilder，管理弹窗打开/关闭/数据绑定
- [x] 2.9 实现 CRUD 方法：handleCreate（空表单）、handleEdit（getApi 获取详情）、handleDelete（确认后删除）、handleDialogSubmit（提交后刷新列表）

## 3. FormBuilder 表单组件

- [x] 3.1 创建 `src/components/business/FormBuilder.vue`，实现字段渲染引擎（根据 fields 配置渲染不同类型的 el-form-item）
- [x] 3.2 实现 v-model 双向绑定（modelValue / update:modelValue）
- [x] 3.3 实现布局配置（single / double / grid 三种布局模式）
- [x] 3.4 实现表单验证集成（支持 rules 配置，暴露 validate/validateField/resetFields 方法）
- [x] 3.5 实现字段 onChange 回调（handleUpdate 中调用 onChange，返回 false 则拒绝变更回退到旧值）

## 4. ReferencePicker 引用查找组件

- [x] 4.1 创建 `src/components/business/ReferencePicker.vue`，实现输入框点击触发弹窗
- [x] 4.2 实现弹窗内的搜索查询表格（搜索输入框 + 数据表格 + 分页）
- [x] 4.3 实现单选和多选模式（single: 点击行即选中关闭；multiple: 勾选+确认按钮）
- [x] 4.4 实现 valueField / displayField 配置，支持 v-model

## 5. 组件注册与导出

- [x] 5.1 创建 `src/components/business/index.ts` 统一导出三个组件
- [x] 5.2 确保组件可通过全局注册或按需导入使用