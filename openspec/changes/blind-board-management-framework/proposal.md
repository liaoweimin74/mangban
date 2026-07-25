## Why

当前项目仅有基础配置文件，无任何业务代码。团队需要一套完整的后台管理系统框架来支撑后续业务迭代。盲板管理系统作为企业级后台，需提供用户管理、组织机构、角色权限、数据字典等基础功能，并包含完善的认证授权体系。本次迭代一次性搭建完整框架，避免后续重复造轮子，确保前后端架构一致性和代码质量。

## What Changes

**项目初始化（前端）**
- From: 无前端项目
- To: Vue 3 + TypeScript + Vite + Element Plus 项目骨架，包含布局、路由、状态管理、HTTP 请求封装
- Reason: 前端框架是后续所有功能的基础
- Impact: 非破坏性，新增项目

**项目初始化（后端）**
- From: 无后端项目
- To: Spring Boot 3.5 多模块 Maven 项目，包含 common、framework、system、admin 四个模块
- Reason: 多模块架构清晰，便于后期扩展
- Impact: 非破坏性，新增项目

**认证授权体系**
- From: 无认证
- To: JWT + Spring Security 认证体系，包含登录、退出、Token 刷新、密码加密
- Reason: 后台管理系统必须的认证机制
- Impact: 非破坏性，新增功能

**基础功能模块**
- From: 无功能模块
- To: 用户管理、组织机构管理、角色管理、菜单管理、数据字典管理五个核心模块的 CRUD
- Reason: 后台管理系统的核心基础功能
- Impact: 非破坏性，新增功能

## Capabilities

### New Capabilities
- `user-auth`: 用户认证与授权体系，含登录、退出、Token 管理、密码加密
- `user-management`: 用户 CRUD、状态管理、密码重置、组织机构关联
- `organization-management`: 组织机构树形结构 CRUD、层级管理
- `role-management`: 角色 CRUD、角色分配菜单权限
- `menu-management`: 菜单/权限树形 CRUD、按钮权限标识管理
- `dict-management`: 数据字典类型与字典项 CRUD、Redis 缓存

### Modified Capabilities
- （无，本项目为全新创建）

## Impact

- **前端**：新增 `mangban-ui/` 目录，包含完整的前端项目代码
- **后端**：新增 `mangban/` 目录，包含四个 Maven 子模块
- **依赖**：前端需 Node.js 18+，后端需 JDK 21+、MySQL 8.0+、Redis 7+
- **API**：新增约 30+ RESTful API 接口
- **数据库**：新增 8 张核心业务表