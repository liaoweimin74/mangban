## 1. 后端项目初始化

- [ ] 1.1 创建 mangban 父 POM 项目（多模块 Maven 结构）
- [ ] 1.2 创建 mangban-common 公共模块（通用工具类、异常处理、统一返回结果）
- [ ] 1.3 创建 mangban-framework 框架模块（Spring Security、JWT、Redis 配置）
- [ ] 1.4 创建 mangban-system 系统管理模块（实体、Repository、Service、Controller）
- [ ] 1.5 创建 mangban-admin 管理后台入口模块（启动类、配置文件、全局异常处理）

## 2. 数据库设计与 JPA 实体

- [ ] 2.1 创建数据库表结构 SQL 脚本（sys_user、sys_organization、sys_role、sys_menu、sys_role_menu、sys_user_role、sys_dict_type、sys_dict_data）
- [ ] 2.2 创建 JPA 实体类及对应 Repository
- [ ] 2.3 创建实体间关联关系映射（@ManyToMany、@OneToMany、@ManyToOne）

## 3. 认证与安全框架

- [ ] 3.1 实现 JWT Token 工具类（生成、验证、解析 Access Token 和 Refresh Token）
- [ ] 3.2 实现 Spring Security 配置（SecurityFilterChain、PasswordEncoder、CORS）
- [ ] 3.3 实现 JWT 认证过滤器（JwtAuthenticationFilter）
- [ ] 3.4 实现认证入口点（AuthenticationEntryPoint）和访问拒绝处理（AccessDeniedHandler）
- [ ] 3.5 实现自定义权限注解（@HasPermission）和权限评估器
- [ ] 3.6 实现登录接口（/auth/login）和退出接口（/auth/logout）
- [ ] 3.7 实现 Token 刷新接口（/auth/refresh）
- [ ] 3.8 实现获取当前用户信息和菜单树接口

## 4. 用户管理模块

- [ ] 4.1 实现用户分页查询接口（支持多条件筛选）
- [ ] 4.2 实现用户创建、修改、删除接口
- [ ] 4.3 实现用户状态启用/停用接口
- [ ] 4.4 实现用户密码重置接口

## 5. 组织机构管理模块

- [ ] 5.1 实现组织机构树形查询接口
- [ ] 5.2 实现组织机构创建、修改、删除接口（含子组织/用户检查）

## 6. 角色管理模块

- [ ] 6.1 实现角色分页查询接口
- [ ] 6.2 实现角色创建、修改、删除接口
- [ ] 6.3 实现角色分配菜单权限接口

## 7. 菜单管理模块

- [ ] 7.1 实现菜单树形查询接口
- [ ] 7.2 实现菜单创建、修改、删除接口（含子菜单检查）

## 8. 数据字典管理模块

- [ ] 8.1 实现字典类型分页查询、创建、修改、删除接口
- [ ] 8.2 实现字典项列表查询、创建、修改、删除接口
- [ ] 8.3 实现字典数据 Redis 缓存机制

## 9. 前端项目初始化

- [ ] 9.1 使用 Vite 创建 Vue 3 + TypeScript 项目
- [ ] 9.2 集成 Element Plus 组件库
- [ ] 9.3 配置 Vue Router 4（路由守卫、动态路由）
- [ ] 9.4 配置 Pinia 状态管理（用户状态、应用状态、标签页状态）
- [ ] 9.5 封装 Axios HTTP 请求（请求/响应拦截器、Token 刷新）
- [ ] 9.6 配置 Vite 构建环境（开发/生产环境变量）

## 10. 前端布局与框架

- [ ] 10.1 实现 Layout 布局组件（顶部导航栏、侧边栏、多页签、主内容区）
- [ ] 10.2 实现左侧树形菜单导航组件（动态渲染、递归组件）
- [ ] 10.3 实现顶部导航栏（Logo、消息通知图标、用户头像/下拉菜单）
- [ ] 10.4 实现多页签组件（TagsView，可关闭、刷新、记录路由）
- [ ] 10.5 实现路由守卫（检查 Token、加载动态路由、权限验证）
- [ ] 10.6 实现权限指令（v-permission）和权限 Hook

## 11. 前端页面开发

- [ ] 11.1 实现登录页面（表单验证、记住密码、登录状态管理）
- [ ] 11.2 实现用户管理页面（列表页 + 表单页，支持 CRUD、状态切换、密码重置）
- [ ] 11.3 实现组织机构管理页面（树形展示 + 表单弹窗 CRUD）
- [ ] 11.4 实现角色管理页面（列表页 + 表单弹窗 + 分配菜单权限树形对话框）
- [ ] 11.5 实现菜单管理页面（树形展示 + 表单弹窗 CRUD）
- [ ] 11.6 实现数据字典管理页面（字典类型列表 + 字典项列表，双列表联动）
- [ ] 11.7 实现 404 错误页面和首页仪表盘占位

## 12. 配置与部署

- [ ] 12.1 配置 Spring Boot 多环境配置文件（dev/prod）
- [ ] 12.2 配置数据库初始化脚本（schema.sql）
- [ ] 12.3 配置 Nginx 部署示例（前端路由转发）
- [ ] 12.4 配置 Redis 连接信息