# Task 1-7: Backend Implementation Brief

## Goal
实现完整的 Spring Boot 3.5 后端，包含多模块 Maven 项目、认证授权、用户管理、组织机构、角色权限、菜单管理、数据字典等功能。

## Architecture
多模块 Maven 项目结构：
- `mangban/pom.xml` - 父 POM
- `mangban/mangban-common/` - 公共模块（工具类、异常、统一响应）
- `mangban/mangban-framework/` - 框架模块（Spring Security + JWT + Redis）
- `mangban/mangban-system/` - 系统管理模块（实体、Service、Controller）
- `mangban/mangban-admin/` - 入口模块（启动类、配置）

## Files to Create

### 父 POM
- `mangban/pom.xml` - 父 POM，Spring Boot 3.5.0 parent，模块声明 common/framework/system/admin

### 公共模块 (mangban-common)
- `mangban/mangban-common/pom.xml`
- `mangban/mangban-common/src/main/java/com/mangban/common/constant/GlobalConstant.java`
- `mangban/mangban-common/src/main/java/com/mangban/common/domain/R.java` - 统一响应 {code, msg, data}
- `mangban/mangban-common/src/main/java/com/mangban/common/domain/PageResult.java` - 分页 {total, page, size, rows}
- `mangban/mangban-common/src/main/java/com/mangban/common/exception/GlobalExceptionHandler.java` - @RestControllerAdvice
- `mangban/mangban-common/src/main/java/com/mangban/common/exception/BusinessException.java`
- `mangban/mangban-common/src/main/java/com/mangban/common/util/SpringUtils.java`
- `mangban/mangban-common/src/main/java/com/mangban/common/config/JacksonConfig.java` - 日期格式、Long转String
- `mangban/mangban-common/src/main/java/com/mangban/common/config/CorsConfig.java`
- `mangban/mangban-common/src/main/java/com/mangban/common/config/RedisConfig.java`

### 框架模块 (mangban-framework)
- `mangban/mangban-framework/pom.xml` - 依赖 spring-boot-starter-security, jjwt 0.12.6
- `mangban/mangban-framework/src/main/java/com/mangban/framework/security/jwt/JwtTokenProvider.java` - JWT 生成/验证，Access Token 30min, Refresh Token 7d
- `mangban/mangban-framework/src/main/java/com/mangban/framework/security/jwt/JwtAuthenticationFilter.java` - OncePerRequestFilter
- `mangban/mangban-framework/src/main/java/com/mangban/framework/security/config/SecurityConfig.java` - SecurityFilterChain, PasswordEncoder, 公开路径 /auth/**
- `mangban/mangban-framework/src/main/java/com/mangban/framework/security/handler/AuthenticationEntryPointImpl.java` - 401
- `mangban/mangban-framework/src/main/java/com/mangban/framework/security/handler/AccessDeniedHandlerImpl.java` - 403
- `mangban/mangban-framework/src/main/java/com/mangban/framework/security/permission/HasPermission.java` - @HasPermission 注解
- `mangban/mangban-framework/src/main/java/com/mangban/framework/security/permission/PermissionEvaluator.java` - 权限校验
- `mangban/mangban-framework/src/main/java/com/mangban/framework/security/domain/LoginUser.java` - 实现 UserDetails
- `mangban/mangban-framework/src/main/java/com/mangban/framework/redis/RedisCache.java` - Redis 工具类

### 系统模块 (mangban-system)

**实体 (8个表):**
- `SysUser.java` - id, username, nickname, password, email, phone, avatar, orgId, status, isDeleted, BaseEntity
- `SysOrganization.java` - id, parentId, orgName, orgCode, sortOrder, status, isDeleted, BaseEntity, children
- `SysRole.java` - id, roleName, roleCode, description, status, isDeleted, BaseEntity
- `SysMenu.java` - id, parentId, menuName, menuType(0目录1菜单2按钮), path, component, permission, icon, sortOrder, status, isDeleted, BaseEntity, children
- `SysDictType.java` - id, dictName, dictCode, remark, status, isDeleted, BaseEntity
- `SysDictData.java` - id, dictCode, label, value, sortOrder, status, isDeleted, BaseEntity
- `SysUserRole.java` - id, userId, roleId
- `SysRoleMenu.java` - id, roleId, menuId

**Repository (每个实体一个):**
继承 JpaRepository + JpaSpecificationExecutor

**DTO/VO:**
- LoginRequest, LoginResponse, UserInfo, MenuTree
- UserCreateRequest, UserUpdateRequest, UserQueryRequest, UserVO
- 各模块的 CRUD DTO

**Service:**
- AuthService: login, logout, refreshToken, getCurrentUser, getCurrentUserMenus
- UserService: list, getById, create, update, delete, updateStatus, resetPassword
- OrganizationService: tree, create, update, delete
- RoleService: list, create, update, delete, getRoleMenus, assignMenus
- MenuService: tree, create, update, delete
- DictTypeService: list, create, update, delete
- DictDataService: list, create, update, delete

**Controller:**
- AuthController: POST /auth/login, /auth/logout, /auth/refresh, GET /auth/userinfo, /auth/menus
- UserController: GET/POST /users, PUT/DELETE /users/{id}, PUT /users/{id}/status, /users/{id}/reset-password
- OrganizationController: GET /orgs/tree, POST /orgs, PUT/DELETE /orgs/{id}
- RoleController: GET/POST /roles, PUT/DELETE /roles/{id}, GET/PUT /roles/{id}/menus
- MenuController: GET /menus/tree, POST /menus, PUT/DELETE /menus/{id}
- DictTypeController: GET/POST /dict-types, PUT/DELETE /dict-types/{id}
- DictDataController: GET /dict-types/{dictCode}/data, POST, PUT/DELETE /dict-data/{id}

### 管理后台入口 (mangban-admin)
- `mangban/mangban-admin/pom.xml` - 依赖所有模块
- `MangbanApplication.java` - @SpringBootApplication, @EnableJpaRepositories, @EntityScan
- `application.yml` - 数据源、JPA、Redis
- `application-dev.yml`
- `application-prod.yml`
- `schema.sql` - 8张表 DDL
- `data.sql` - 初始化 admin/123456 用户、默认菜单、超级管理员角色

## Database Schema
参见 plan.md 中的 Task 3 Step 1 SQL 定义。

## Constraints
- Java 21+, Spring Boot 3.5.0, Spring Data JPA, Spring Security, jjwt 0.12.6
- 所有 Controller 路径前缀 /api/（如 /api/auth/login, /api/users）
- 统一响应 R<T>: {code: 200, msg: "success", data: ...}
- 分页: page从1开始, size默认10
- 密码 BCrypt 加密
- 所有实体逻辑删除 is_deleted
- JPA: ddl-auto=none, 用 schema.sql 初始化
- 登录接口 /api/auth/login 公开，其他接口需 JWT 认证