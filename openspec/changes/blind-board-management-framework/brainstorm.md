## Design Summary

盲板管理系统（MangBan）是一个企业级管理后台系统，用于管理组织机构、用户、角色权限及数据字典等基础功能。系统采用前后端分离架构，前端 Vue 3 + TypeScript + Element Plus，后端 Spring Boot 3.5 + JPA + MySQL + Redis。

## Alternatives Considered

### 方案 A：微前端架构（qiankun/micro-app）
- **做法**：将用户管理、组织机构、角色权限、数据字典拆分为独立子应用，通过微前端框架组合
- **优点**：各模块独立开发部署，团队可并行工作
- **缺点**：架构复杂度高，首屏加载慢，本项目前期不需要如此弹性的架构
- **为何未采用**：项目初期阶段，单体应用足够，微前端引入不必要的复杂度

### 方案 B：前后端分离 + 单体后端
- **做法**：前端 Vue 3 + Element Plus 单页应用，后端 Spring Boot 单体应用，RESTful API 通信
- **优点**：架构清晰、开发效率高、部署简单、适合中小规模项目
- **缺点**：后端模块耦合度较高，需注意代码分层
- **为何未采用**：N/A — 这是选定的方案

### 方案 C：前后端分离 + 后端微服务
- **做法**：按业务拆分为用户服务、权限服务、字典服务等独立微服务
- **优点**：独立部署、独立扩缩容、技术栈可异构
- **缺点**：分布式事务、服务发现、配置管理等基础设施成本高，过度设计
- **为何未采用**：项目初期不需要微服务，单体架构配合良好分层即可满足需求

## Agreed Approach

**方案 B：前后端分离 + 单体后端** 被选定。

原因：
1. 项目需求明确、范围清晰，属于标准的后台管理系统
2. 前后端分离架构满足现代 Web 开发最佳实践
3. Spring Boot 单体应用配合 JPA 足以支撑当前所有功能模块
4. 部署简单（前端 Nginx 静态部署 + 后端 JAR 包），运维成本低
5. 后期如有需要，可平滑演进为微服务架构

## Key Decisions

### 技术栈
| 层 | 技术选型 | 说明 |
|---|---|---|
| 前端框架 | Vue 3 + TypeScript | Composition API + `<script setup>` |
| UI 组件库 | Element Plus | 成熟的 Vue 3 组件库 |
| 前端路由 | Vue Router 4 | Hash 模式，支持嵌套路由 |
| 状态管理 | Pinia | Vue 3 官方推荐 |
| 前端构建 | Vite | 快速开发/构建 |
| HTTP 客户端 | Axios | 封装请求拦截、响应拦截、Token 刷新 |
| 后端框架 | Spring Boot 3.5 | 最新的 Spring Boot 版本 |
| ORM | Spring Data JPA + Hibernate | 对象关系映射 |
| 数据库 | MySQL 8.0+ | 关系型数据库 |
| 缓存 | Redis | 会话缓存、Token 缓存、数据字典缓存 |
| 安全 | Spring Security + JWT | 无状态认证 |
| API 文档 | SpringDoc OpenAPI | 自动生成接口文档 |

### 前端布局设计
- **整体布局**：上下结构
  - 顶部：标题栏（Logo + 系统名称 + 消息通知 + 用户信息及退出）
  - 左下：树形菜单导航（根据角色权限动态生成）
  - 右下：多页签内容区（Tabs 可关闭，每个菜单项打开一个页签）
- **列表页布局**：上部查询栏（条件输入 + 查询/重置按钮）→ 下部数据表格（分页）
- **表单页布局**：标题栏 → 表单输入区 → 底部操作栏（保存/取消按钮）

### 后端模块划分
- `mangban-common` — 公共模块（工具类、异常、常量、通用实体）
- `mangban-system` — 系统管理模块
  - 用户管理（User）：CRUD、密码管理、状态管理
  - 组织机构（Organization）：树形结构 CRUD
  - 角色管理（Role）：CRUD、角色分配菜单权限
  - 菜单管理（Menu）：树形 CRUD、按钮权限标识
  - 数据字典（Dict）：字典类型 + 字典项 CRUD
- `mangban-auth` — 认证模块（登录、退出、Token 刷新、注册）

### 数据库核心表设计
- `sys_user` — 用户表
- `sys_organization` — 组织机构表（自关联树形）
- `sys_role` — 角色表
- `sys_menu` — 菜单权限表
- `sys_role_menu` — 角色菜单关联表
- `sys_user_role` — 用户角色关联表
- `sys_dict_type` — 字典类型表
- `sys_dict_data` — 字典数据表

### 认证与权限
- 登录：用户名/密码 → JWT Token（Access Token + Refresh Token）
- 鉴权：Spring Security Filter 拦截请求，解析 JWT → 获取用户权限
- 权限模型：RBAC（基于角色的访问控制）
  - 用户 → 角色 → 菜单权限
  - 前端：根据角色菜单树动态生成路由
  - 后端：接口级权限校验（@PreAuthorize）

## Open Questions

1. 是否需要支持多租户（SaaS 模式）？当前设计按单租户处理
2. 是否需要支持 OAuth2 第三方登录？当前仅支持用户名密码登录
3. 是否需要支持国际化（i18n）？当前按中文设计
4. 是否需要支持操作日志审计？可后续迭代加入