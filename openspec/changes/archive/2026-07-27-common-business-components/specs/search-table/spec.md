## ADDED Requirements

### Requirement:SearchTable 应在顶部渲染查询筛选栏
SearchTable 根据 `searchFields` prop 定义在顶部渲染查询筛选栏，每个字段类型渲染对应的 Element Plus 表单控件�?
#### 场景：渲染文本输入框
- **给定** SearchTable �?`searchFields` 包含 `{ type: 'input', label: '用户�?, prop: 'username' }`
- **�?* 组件挂载�?- **那么** 查询栏应显示一�?el-form-item（标�?用户�?）和一�?el-input（绑定到 query.username�?
#### 场景：渲染下拉选择�?- **给定** SearchTable �?`searchFields` 包含 `{ type: 'select', label: '状�?, prop: 'status', options: [{ label: '启用', value: 1 }, { label: '停用', value: 0 }] }`
- **�?* 组件挂载�?- **那么** 查询栏应显示一�?el-select 并包含给出的选项

#### 场景：渲染树形选择�?- **给定** SearchTable �?`searchFields` 包含 `{ type: 'tree-select', label: '组织机构', prop: 'orgId', treeProps: { data: [], props: { label: 'label', value: 'id' } } }`
- **�?* 组件挂载�?- **那么** 查询栏应显示一�?el-tree-select 并包含给出的 treeProps

---

### Requirement:SearchTable 应在筛选栏右侧提供搜索、重置、导出按�?SearchTable 应渲染搜索按钮（el-button type=primary，图�?Search）、重置按钮（el-button，图�?Refresh）、导出按钮（el-button，图�?Download），靠右对齐�?
#### 场景：搜索按钮触发查�?- **给定** SearchTable �?fetchApi prop
- **�?* 用户点击搜索按钮�?- **那么** SearchTable 应将 page 设为 1，并使用当前查询参数调用 fetchApi

#### 场景：重置按钮清空筛选条件并重新查询
- **给定** SearchTable 的查询字段已填入内容
- **�?* 用户点击重置按钮�?- **那么** SearchTable 应清空所有查询字段值，�?page 设为 1，并调用 fetchApi

#### 场景：导出按钮触发导出事�?- **给定** SearchTable �?showExport �?true
- **�?* 用户点击导出按钮�?- **那么** SearchTable �?emit 一�?'export' 事件，携带当前查询参�?
---

### Requirement:SearchTable 应在中间区域渲染数据表格
SearchTable 根据 `columns` prop 定义渲染 el-table。每列支�?prop 绑定、自定义标签、宽度、格式化函数和自定义模板插槽�?
#### 场景：渲染简单文本列
- **给定** SearchTable �?`columns` 包含 `[{ prop: 'username', label: '用户�?, width: 120 }, { prop: 'email', label: '邮箱', minWidth: 160 }]`
- **�?* 表格渲染数据�?- **那么** 每行应在对应列显示该 prop 的�?
#### 场景：渲染操作列按钮
- **给定** SearchTable �?`actionButtons` 包含 `[{ label: '编辑', type: 'primary', permission: 'system:user:edit', onClick: (row) => handleEdit(row) }]`
- **�?* 表格渲染�?- **那么** 最后一列应为操作列，每行包含操作按�?
#### 场景：通过插槽自定义列模板
- **给定** SearchTable 的列配置中某列有 `slotName: 'status'`
- **�?* 父组件提�?`<template #status="{ row }">` 内容�?- **那么** 该列应渲染插槽内容而非原始 prop �?
---

### Requirement:SearchTable 应在底部渲染分页导航
SearchTable 应渲�?el-pagination，显示总条数、每页条数选择器（10/20/50）、上一�?下一页按钮和页码按钮�?
#### 场景：切换页码触发重新查�?- **给定** SearchTable �?fetchApi 返回 total=100
- **�?* 用户点击�?2 页时
- **那么** SearchTable 应将 page 设为 2，并使用更新后的 page 参数调用 fetchApi

#### 场景：切换每页条数触发重新查�?- **给定** SearchTable 的默�?pageSize=10
- **�?* 用户从每页条数下拉框选择 20 �?- **那么** SearchTable 应将 size 设为 20，page 设为 1，并调用 fetchApi

---

### Requirement:SearchTable 应支持加载状�?SearchTable �?fetchApi 执行期间应显�?el-table �?v-loading 加载遮罩�?
#### 场景：加载中显示遮罩
- **给定** SearchTable �?fetchApi 需�?500ms 才能完成
- **�?* 组件正在请求数据�?- **那么** 表格应显示加载遮�?
---

### Requirement:SearchTable 应在表格上方提供工具栏插�?SearchTable 在筛选栏和表格之间提供一个默认插槽，用于放置工具栏按钮（�?新增用户"）�?
#### 场景：工具栏插槽渲染自定义内�?- **给定** SearchTable 的模板中使用�?`<template #default><el-button>新增用户</el-button></template>`
- **�?* 组件渲染�?- **那么** 工具栏区域应包含"新增用户"按钮

---

### Requirement:SearchTable 应接�?fetchApi 作为必需的函�?prop
SearchTable 要求 `fetchApi` prop 是一个函�?`(params: QueryParams) => Promise<{ rows: T[]; total: number }>`。组件应在挂载时及搜�?重置/分页变化时自动调用它�?
#### 场景：挂载时自动获取数据
- **给定** SearchTable 有有效的 fetchApi
- **�?* 组件挂载�?- **那么** fetchApi 应被调用，默认参数为 page=1, size=10

#### 场景：fetchApi 接收到正确的参数
- **给定** SearchTable 包含查询字段�?fetchApi
- **�?* 用户填写用户名并点击搜索�?- **那么** fetchApi 应被调用，参数包�?`{ page: 1, size: 10, username: 'admin' }`

---

### Requirement:SearchTable 应支�?formConfig prop 以集�?FormBuilder �?CRUD 功能
SearchTable 应接�?`formConfig` prop。当存在时，SearchTable 应在工具栏渲�?新增"按钮，在操作列渲�?编辑"/"删除"按钮。组件应内部管理弹窗的打开/关闭状态、表单数据绑定和 CRUD API 调用�?
#### 场景：formConfig 渲染默认操作按钮
- **给定** SearchTable �?`formConfig` 包含 `{ fields: [...], createApi, updateApi, deleteApi }`
- **�?* 组件渲染�?- **那么** 工具栏应包含"新增"按钮，每行应包含"编辑"�?删除"按钮

#### 场景：新增按钮打开空表单弹�?- **给定** SearchTable �?formConfig
- **�?* 用户点击"新增"按钮�?- **那么** 应打开一个标题为"新增"的弹窗，表单为空

#### 场景：编辑按钮获取详情并打开表单弹窗
- **给定** SearchTable �?formConfig 包含 `getApi: (id) => fetchUser(id)`
- **�?* 用户点击某行�?编辑"按钮�?- **那么** getApi 应以该行 id 被调用，然后打开弹窗，表单填充获取到的数�?
#### 场景：删除按钮显示确认弹窗并调用 deleteApi
- **给定** SearchTable �?formConfig 包含 `deleteApi: (id) => deleteUser(id)`
- **�?* 用户点击某行�?删除"按钮并确认时
- **那么** deleteApi 应以该行 id 被调用，列表刷新

#### 场景：表单提交触发创建或更新
- **给定** SearchTable �?formConfig 包含 `createApi` �?`updateApi`
- **�?* 用户在弹窗中填写表单并点�?确定"�?- **那么** 新增模式下以表单数据调用 createApi，编辑模式下�?id 和表单数据调�?updateApi，弹窗关闭，列表刷新

#### 场景：actionButtons 覆盖 formConfig 默认按钮
- **给定** SearchTable 同时�?`formConfig` �?`actionButtons`
- **�?* 组件渲染�?- **那么** 操作列应渲染 `actionButtons` 而非默认�?编辑"/"删除"按钮

#### 场景：空 actionButtons 隐藏操作�?- **给定** SearchTable �?`formConfig` �?`actionButtons: []`
- **�?* 组件渲染�?- **那么** 不应渲染操作�?
#### 场景：无 formConfig 且无 actionButtons 时不渲染操作�?- **给定** SearchTable 没有 `formConfig` 也没�?`actionButtons`
- **�?* 组件渲染�?- **那么** 不应渲染操作�?
---

### Requirement:SearchTable 应将超出 maxVisibleButtons 的操作按钮折叠到"更多"下拉菜单
SearchTable 应接�?`maxVisibleButtons` prop（默�?3）。前 N 个按钮直接显示在操作列中，剩余按钮隐藏在 el-dropdown 中，通过一个带向下箭头�?更多"按钮触发�?
#### 场景：按钮数在限制内时直接显�?- **给定** SearchTable �?`maxVisibleButtons: 3` 且有 3 个操作按�?- **�?* 表格渲染�?- **那么** 所�?3 个按钮应直接显示在操作列中，无下拉菜�?
#### 场景：按钮数超出限制时折叠到下拉
- **给定** SearchTable �?`maxVisibleButtons: 2` 且有 4 个操作按�?- **�?* 表格渲染�?- **那么** �?2 个按钮应直接显示，同时出现带向下箭头�?更多"按钮，点击后下拉展开剩余 2 个按�?
#### 场景：下拉菜单中的按钮点击触发正确操�?- **给定** SearchTable 有一个被折叠的按钮，标签�?重置密码"，绑定了 onClick 处理函数
- **�?* 用户在下拉菜单中点击"重置密码"�?- **那么** onClick 处理函数应被调用，并传入正确的行数据