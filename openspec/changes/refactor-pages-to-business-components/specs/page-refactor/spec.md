## ADDED Requirements

### Requirement: RolePage SHALL use SearchTable + FormBuilder for CRUD operations

RolePage 用 SearchTable 替代原始 el-table 搜索/表格/分页模式，FormBuilder 替代新增/编辑弹窗。分配菜单弹窗保留为自定义弹窗。

#### Scenario: 角色列表搜索
- **Given** RolePage 使用 SearchTable
- **When** 用户输入角色名并点击搜索
- **Then** SearchTable 调用 getRoleList 并刷新表格

#### Scenario: 新增/编辑角色
- **Given** RolePage 使用 FormBuilder
- **When** 用户点击新增或编辑
- **Then** FormBuilder 弹窗显示 roleName, roleCode, description 字段

#### Scenario: 分配菜单
- **Given** RolePage 的操作列
- **When** 用户点击分配菜单
- **Then** 显示自定义树形权限选择弹窗

### Requirement: MenuPage SHALL use SearchTable tree mode + FormBuilder for CRUD

MenuPage 用 SearchTable 树形模式 + FormBuilder。menuType 切换控制字段显隐。

#### Scenario: 树形表格渲染
- **Given** MenuPage 使用 SearchTable treeProps
- **When** 组件挂载
- **Then** 表格以树形方式显示菜单层级

#### Scenario: 新增目录/菜单/按钮
- **Given** MenuPage 表单
- **When** 用户选择 menuType 为「目录」
- **Then** 显示 parentId, menuName, menuType, path, icon, sortOrder, visible 字段

#### Scenario: 新增按钮类型
- **Given** MenuPage 表单
- **When** 用户选择 menuType 为「按钮」
- **Then** 显示 parentId, menuName, menuType, permission, sortOrder 字段（隐藏 path, component, icon）

### Requirement: DictPage SHALL use dual SearchTable for type and data management

DictPage 用两个 SearchTable 实例：上方字典类型、下方字典数据。选中类型联动数据表格。

#### Scenario: 字典类型 CRUD
- **Given** DictPage 上部 SearchTable
- **When** 用户点击类型行
- **Then** 下部数据表格根据选中类型的 dictCode 加载对应数据

#### Scenario: 字典数据 CRUD
- **Given** DictPage 下部 SearchTable
- **When** 用户新增/编辑字典数据
- **Then** 数据表单自动填充当前选中类型的 dictCode

### Requirement: OrgPage SHALL use SearchTable tree mode + FormBuilder

OrgPage 用 SearchTable 树形模式。表单字段映射 name→orgName, code→orgCode。

#### Scenario: 树形组织架构
- **Given** OrgPage 使用 SearchTable treeProps
- **When** 组件挂载
- **Then** 表格树形显示组织层级

#### Scenario: 新增子组织字段映射
- **Given** OrgPage 新增表单
- **When** 用户提交
- **Then** handleSubmit 将 form.name 映射为 orgName, form.code 映射为 orgCode 发送给后端

### Requirement: UserPage SHALL be removed in favor of UserPageEx

UserPage.vue 删除，路由已指向 UserPageEx.vue（已用 SearchTable + FormBuilder 重构）。