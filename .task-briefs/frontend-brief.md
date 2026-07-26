# Task 8-12: Frontend Implementation Brief

## Goal
实现完整的 Vue 3 + TypeScript + Element Plus 前端，包含项目骨架、布局、路由、状态管理、HTTP封装、登录页、系统管理各功能页面。

## Architecture
标准 Vite + Vue 3 + TS 项目结构：
- `mangban-ui/` 根目录
- src 目录按功能组织

## Files to Create

### 项目配置文件
- `mangban-ui/package.json` - 依赖 vue, vue-router, pinia, element-plus, axios, @element-plus/icons-vue
- `mangban-ui/vite.config.ts` - proxy /api -> localhost:8080, alias @ -> src
- `mangban-ui/tsconfig.json`
- `mangban-ui/tsconfig.node.json`
- `mangban-ui/index.html`
- `mangban-ui/.env.development` - VITE_API_BASE_URL=/api
- `mangban-ui/.env.production`

### 入口
- `mangban-ui/src/main.ts` - createApp, use ElementPlus, use Pinia, use Router
- `mangban-ui/src/App.vue` - <router-view />
- `mangban-ui/src/env.d.ts`

### 样式
- `mangban-ui/src/styles/index.scss` - 全局样式重置、CSS变量

### 类型定义
- `mangban-ui/src/types/api.ts` - R<T>, PageResult<T>
- `mangban-ui/src/types/menu.ts` - MenuTree
- `mangban-ui/src/types/user.ts` - UserInfo, LoginRequest, LoginResponse

### HTTP 封装
- `mangban-ui/src/utils/request.ts` - Axios 实例，请求拦截器(注入Token)，响应拦截器(401自动刷新)

### API 层
- `mangban-ui/src/api/auth.ts` - login, logout, refresh, getUserInfo, getMenus
- `mangban-ui/src/api/system/user.ts`
- `mangban-ui/src/api/system/org.ts`
- `mangban-ui/src/api/system/role.ts`
- `mangban-ui/src/api/system/menu.ts`
- `mangban-ui/src/api/system/dict.ts`

### 状态管理 (Pinia)
- `mangban-ui/src/stores/user.ts` - 用户信息、Token、登录/退出/刷新、权限
- `mangban-ui/src/stores/app.ts` - 侧边栏折叠、菜单树
- `mangban-ui/src/stores/tagsView.ts` - 页签列表、激活、关闭/刷新

### 路由
- `mangban-ui/src/router/index.ts` - 公共路由(/login, /404), 动态路由守卫

### 布局组件
- `mangban-ui/src/layout/index.vue` - el-container 上下左右布局
- `mangban-ui/src/layout/Navbar.vue` - Logo + 系统名称 + 消息通知图标 + 用户下拉菜单(个人信息, 退出)
- `mangban-ui/src/layout/Sidebar/index.vue` - el-menu 递归渲染
- `mangban-ui/src/layout/Sidebar/SidebarItem.vue` - 递归子菜单
- `mangban-ui/src/layout/TagsView/index.vue` - 多页签，可关闭，右键菜单
- `mangban-ui/src/layout/Main.vue` - <router-view /> 内容区

### 通用组件
- `mangban-ui/src/components/SvgIcon/index.vue`
- `mangban-ui/src/components/Pagination/index.vue` - 封装 el-pagination
- `mangban-ui/src/components/SearchForm/index.vue` - 通用查询栏

### 权限指令
- `mangban-ui/src/directives/permission.ts` - v-permission

### 页面
- `mangban-ui/src/views/login/index.vue` - 登录表单，记住密码
- `mangban-ui/src/views/dashboard/index.vue` - 首页占位
- `mangban-ui/src/views/system/user/index.vue` - 用户列表(查询栏+表格+分页)
- `mangban-ui/src/views/system/user/UserForm.vue` - 用户表单弹窗
- `mangban-ui/src/views/system/org/index.vue` - 组织机构树+弹窗CRUD
- `mangban-ui/src/views/system/org/OrgForm.vue`
- `mangban-ui/src/views/system/role/index.vue` - 角色列表+弹窗
- `mangban-ui/src/views/system/role/RoleForm.vue`
- `mangban-ui/src/views/system/role/MenuDialog.vue` - 菜单树勾选对话框
- `mangban-ui/src/views/system/menu/index.vue` - 菜单树+弹窗
- `mangban-ui/src/views/system/menu/MenuForm.vue` - 根据类型动态显示字段
- `mangban-ui/src/views/system/dict/index.vue` - 字典类型列表+字典项列表(左右分栏)
- `mangban-ui/src/views/system/dict/DictTypeForm.vue`
- `mangban-ui/src/views/system/dict/DictDataDialog.vue`
- `mangban-ui/src/views/error/404.vue`

### 部署
- `mangban-ui/nginx.conf` - 单页应用路由转发 + API代理

## Layout Design
- 上下结构: 顶部Navbar + (左侧Sidebar + 右侧TagsView+Main)
- el-container 实现
- Navbar: 左Logo+系统名, 右消息通知+用户头像下拉
- Sidebar: el-menu 递归渲染菜单树
- TagsView: 水平标签, 点击切换/关闭

## List Page Pattern
```
<SearchForm> -- 查询条件 + 查询/重置按钮
<el-table> -- 数据表格
<Pagination> -- 分页
```

## Form Page Pattern
```
<el-dialog>
  标题
  <el-form> -- 表单输入
  底部: 保存/取消按钮
</el-dialog>
```

## Constraints
- Composition API + <script setup>
- Hash 模式路由
- 所有 API 路径前缀 /api/
- 动态路由: 登录后从 /auth/menus 获取菜单树动态生成路由
- v-permission 指令控制按钮级显示
- 401 自动刷新 Token, 刷新失败跳转登录