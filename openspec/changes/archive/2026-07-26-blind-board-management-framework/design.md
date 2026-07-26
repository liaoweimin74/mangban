## Context

盲板管理系统（MangBan）是一个面向企业内部的后台管理系统，需提供用户管理、组织机构管理、角色权限管理、数据字典等基础功能模块。系统采用前后端分离架构，前端 Vue 3 + TypeScript + Element Plus，后端 Spring Boot 3.5 + JPA + MySQL + Redis。

当前项目处于初始阶段，仅有基础的项目配置文件。本次迭代目标为搭建完整的框架代码，包括前后端项目结构、认证授权体系、基础功能模块的 CRUD 骨架。

## Goals / Non-Goals

**Goals:**
- 搭建前端 Vue 3 + TypeScript + Element Plus 项目骨架（布局、路由、状态管理、HTTP 请求封装）
- 搭建后端 Spring Boot 3.5 + JPA + MySQL + Redis 项目骨架（多模块 Maven 项目结构）
- 实现完整的用户认证体系（登录、退出、Token 管理、密码加密）
- 实现用户管理功能（CRUD、状态管理、密码重置）
- 实现组织机构管理功能（树形结构 CRUD）
- 实现角色管理功能（CRUD、角色-菜单权限分配）
- 实现菜单管理功能（树形 CRUD、按钮权限标识）
- 实现数据字典管理功能（字典类型 + 字典项 CRUD）
- 实现 RBAC 权限控制（后端接口级 + 前端路由级）

**Non-Goals:**
- 操作日志审计（后续迭代添加）
- 消息通知模块（仅保留前端 UI 占位，后端暂不实现）
- 文件上传/下载功能
- 国际化（i18n）支持
- 多租户 SaaS 支持
- OAuth2 / 第三方登录
- 单元测试 / 集成测试（后续迭代添加）

## Decisions

### 1. 前端项目结构（Vue 3 + Vite + TypeScript + Element Plus）

```
mangban-ui/
├── src/
│   ├── api/              # API 接口层（按模块拆分）
│   ├── assets/           # 静态资源（图片、样式）
│   ├── components/       # 通用组件
│   │   ├── Breadcrumb/
│   │   ├── PageHeader/
│   │   ├── Pagination/
│   │   ├── SearchForm/
│   │   └── SvgIcon/
│   ├── composables/      # 组合式函数
│   ├── directives/       # 自定义指令（权限指令 v-permission）
│   ├── hooks/            # 自定义 Hook
│   ├── icons/            # SVG 图标
│   ├── layout/           # 布局组件
│   │   ├── Sidebar/      # 侧边栏
│   │   ├── Navbar/       # 顶部导航栏
│   │   ├── TagsView/     # 多页签
│   │   └── Main/         # 主内容区
│   ├── mock/             # Mock 数据（开发用）
│   ├── router/           # 路由配置
│   ├── stores/           # Pinia 状态管理
│   ├── styles/           # 全局样式
│   ├── types/            # TypeScript 类型定义
│   ├── utils/            # 工具函数
│   └── views/            # 页面视图
│       ├── dashboard/    # 首页
│       ├── login/        # 登录
│       ├── system/       # 系统管理
│       │   ├── user/     # 用户管理
│       │   ├── role/     # 角色管理
│       │   ├── menu/     # 菜单管理
│       │   ├── org/      # 组织机构
│       │   └── dict/     # 数据字典
│       ├── profile/      # 个人中心
│       └── error/        # 错误页面
├── .env.development      # 开发环境变量
├── .env.production       # 生产环境变量
├── vite.config.ts        # Vite 配置
├── tsconfig.json         # TypeScript 配置
└── package.json
```

**决策理由**：按功能模块组织代码，API 层与视图层分离，复用组件集中管理，符合 Vue 3 最佳实践。

### 2. 后端项目结构（Spring Boot 多模块）

```
mangban/
├── pom.xml                          # 父 POM
├── mangban-common/                  # 公共模块
│   ├── src/main/java/.../
│   │   ├── config/                  # 通用配置（Jackson、CORS、Redis）
│   │   ├── constant/                # 常量
│   │   ├── domain/                  # 通用返回结果类
│   │   ├── exception/               # 全局异常处理
│   │   ├── util/                    # 工具类
│   │   └── web/                     # 通用 Controller 基类
│   └── pom.xml
├── mangban-framework/               # 框架模块
│   ├── src/main/java/.../
│   │   ├── config/                  # 安全配置、JPA 配置
│   │   ├── security/                # Spring Security + JWT
│   │   │   ├── auth/                # 认证过滤器
│   │   │   ├── handler/             # 认证/授权异常处理
│   │   │   ├── jwt/                 # JWT 工具类
│   │   │   └── permission/          # 权限注解
│   │   └── redis/                   # Redis 工具类
│   └── pom.xml
├── mangban-system/                  # 系统管理模块
│   ├── src/main/java/.../
│   │   ├── controller/              # REST 控制器
│   │   ├── domain/                  # 实体类 + DTO
│   │   ├── repository/              # JPA Repository
│   │   ├── service/                 # 业务逻辑
│   │   │   ├── impl/                # 实现类
│   │   │   └── mapstruct/           # MapStruct 转换
│   │   └── mapper/                  # 数据映射
│   └── pom.xml
├── mangban-admin/                   # 管理后台入口模块
│   ├── src/main/java/.../
│   │   ├── MangbanApplication.java  # 启动类
│   │   └── controller/              # 系统级 Controller
│   ├── src/main/resources/
│   │   ├── application.yml          # 主配置
│   │   ├── application-dev.yml      # 开发配置
│   │   └── application-prod.yml     # 生产配置
│   └── pom.xml
└── .mvn/                            # Maven 配置
```

**决策理由**：多模块 Maven 项目结构清晰，公共模块独立复用，框架模块封装安全逻辑，系统模块聚焦业务，入口模块负责启动和配置。

### 3. 认证方案：JWT + Spring Security

- **Access Token**：有效期 30 分钟，存于前端内存/请求头
- **Refresh Token**：有效期 7 天，存于 Redis + 前端
- **密码加密**：BCryptPasswordEncoder
- **Token 刷新**：Axios 拦截器自动检测 401 并调用刷新接口
- **无状态**：服务端不存储 Session，所有状态通过 Token 传递

### 4. 权限模型：RBAC

```
用户 (User) ──< 用户角色关联 (UserRole) >── 角色 (Role) ──< 角色菜单关联 (RoleMenu) >── 菜单 (Menu)
```

- 菜单表包含：目录、菜单、按钮三种类型
- 按钮权限通过 `@PreAuthorize("hasPermission('system:user:add')")` 注解控制
- 前端通过 `v-permission` 指令控制按钮级显示

### 5. 数据字典设计方案

- 字典类型表（sys_dict_type）：存储字典分类（如：sex、status）
- 字典数据表（sys_dict_data）：存储具体字典项（key-value）
- 后端提供缓存接口，首次查询后缓存到 Redis
- 前端提供 dict 标签组件，通过字典类型自动加载下拉选项

### 6. 前端路由与权限

- 路由表分为：公共路由（login、404）和动态路由（需要权限的页面）
- 用户登录后，后端返回该用户的菜单树（含路由 path、组件路径、权限标识）
- 前端根据菜单树动态生成 Vue Router 路由
- 侧边栏菜单根据角色菜单树动态渲染
- 多页签（TagsView）记录用户已打开的页面，支持关闭

### 7. 前端 HTTP 请求封装

- Axios 实例封装 baseURL、超时时间
- 请求拦截器：自动注入 Token
- 响应拦截器：统一错误处理、401 自动刷新 Token
- 所有 API 调用使用 async/await，返回统一格式 `R<T>`

## Risks / Trade-offs

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| JPA 懒加载导致 N+1 查询 | 性能下降 | 使用 `@EntityGraph` 或 `JOIN FETCH` 明确指定加载策略 |
| JWT Token 无法主动吊销 | 安全问题 | 短有效期 Access Token + Redis 黑名单机制 |
| 前端动态路由刷新后丢失 | 页面空白 | 路由守卫中检查 Token 并重新加载路由表 |
| 多模块间实体依赖 | 编译耦合 | 基础实体定义在 common 模块，业务实体在 system 模块 |
| 首次加载菜单/权限数据量大 | 首屏缓慢 | 按需加载、懒加载子菜单、异步路由组件 |
| 多页签过多导致内存占用 | 浏览器卡顿 | 限制最大页签数（建议 10-15），关闭后销毁组件实例 |