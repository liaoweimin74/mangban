# 盲板管理系统框架 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 搭建完整的盲板管理系统框架，包括前后端项目骨架、认证授权体系、用户管理、组织机构管理、角色权限管理、菜单管理、数据字典管理等核心功能模块。

**Architecture:** 前后端分离架构。前端 Vue 3 + TypeScript + Element Plus，后端 Spring Boot 3.5 多模块 Maven 项目（common/framework/system/admin），数据库 MySQL 8.0+，缓存 Redis 7+，认证 JWT + Spring Security。

**Tech Stack:** 前端: Vue 3, TypeScript, Vite, Element Plus, Pinia, Vue Router 4, Axios | 后端: Spring Boot 3.5, Spring Data JPA, Spring Security, JWT, MySQL 8.0+, Redis 7+

## Global Constraints

- 前端使用 Composition API + `<script setup>` 语法
- 后端使用 JDK 21+
- 所有 API 响应统一格式 `R<T>`：`{ code: number, msg: string, data: T }`
- 分页参数统一：`page`（从 1 开始）、`size`（默认 10）
- 密码使用 BCryptPasswordEncoder 加密
- 实体逻辑删除使用 `is_deleted` 字段
- 所有接口需 JWT 认证（除登录接口外）
- 前端路由使用 Hash 模式
- 文件编码统一 UTF-8

---

### Task 1: 后端父 POM 与公共模块

**Files:**
- Create: `mangban/pom.xml`
- Create: `mangban/mangban-common/pom.xml`
- Create: `mangban/mangban-common/src/main/java/com/mangban/common/constant/GlobalConstant.java`
- Create: `mangban/mangban-common/src/main/java/com/mangban/common/domain/R.java`
- Create: `mangban/mangban-common/src/main/java/com/mangban/common/domain/PageResult.java`
- Create: `mangban/mangban-common/src/main/java/com/mangban/common/exception/GlobalExceptionHandler.java`
- Create: `mangban/mangban-common/src/main/java/com/mangban/common/exception/BusinessException.java`
- Create: `mangban/mangban-common/src/main/java/com/mangban/common/util/SpringUtils.java`
- Create: `mangban/mangban-common/src/main/java/com/mangban/common/config/JacksonConfig.java`
- Create: `mangban/mangban-common/src/main/java/com/mangban/common/config/CorsConfig.java`
- Create: `mangban/mangban-common/src/main/java/com/mangban/common/config/RedisConfig.java`

**Interfaces:**
- Consumes: 无
- Produces: `R<T>` 统一响应类，`PageResult` 分页结果，`BusinessException` 业务异常，`GlobalExceptionHandler` 全局异常处理，Redis/Jackson/CORS 配置

- [ ] **Step 1: 创建父 POM 和公共模块 POM**

```xml
<!-- mangban/pom.xml -->
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.mangban</groupId>
    <artifactId>mangban</artifactId>
    <version>1.0.0</version>
    <packaging>pom</packaging>
    <modules>
        <module>mangban-common</module>
        <module>mangban-framework</module>
        <module>mangban-system</module>
        <module>mangban-admin</module>
    </modules>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.0</version>
    </parent>
    <properties>
        <java.version>21</java.version>
        <jjwt.version>0.12.6</jjwt.version>
    </properties>
    <!-- dependencies management -->
</project>
```

- [ ] **Step 2: 创建统一响应类 R 和 PageResult**

```java
// R.java - 通用响应
public class R<T> implements Serializable {
    private int code;
    private String msg;
    private T data;
    public static <T> R<T> ok(T data) { return new R<>(200, "success", data); }
    public static <T> R<T> fail(int code, String msg) { return new R<>(code, msg, null); }
}

// PageResult.java - 分页结果
public class PageResult<T> {
    private long total;
    private int page;
    private int size;
    private List<T> rows;
}
```

- [ ] **Step 3: 创建异常处理和业务异常类**
- `BusinessException` 继承 `RuntimeException`
- `GlobalExceptionHandler` 使用 `@RestControllerAdvice` 捕获 `BusinessException`、`MethodArgumentNotValidException`、未授权异常等

- [ ] **Step 4: 创建配置类**
- `JacksonConfig`：配置日期格式、时区、Long 转 String（防前端精度丢失）
- `CorsConfig`：跨域配置，允许所有来源
- `RedisConfig`：配置 RedisTemplate（Key/String 序列化）

- [ ] **Step 5: 创建常量类**
- `GlobalConstant`：定义通用常量（如 TOKEN_HEADER = "Authorization"）

- [ ] **Step 6: 验证编译**

```bash
cd mangban && mvn compile -pl mangban-common -am
```

---

### Task 2: 框架模块（Spring Security + JWT）

**Files:**
- Create: `mangban/mangban-framework/pom.xml`
- Create: `mangban/mangban-framework/src/main/java/com/mangban/framework/security/jwt/JwtTokenProvider.java`
- Create: `mangban/mangban-framework/src/main/java/com/mangban/framework/security/jwt/JwtAuthenticationFilter.java`
- Create: `mangban/mangban-framework/src/main/java/com/mangban/framework/security/config/SecurityConfig.java`
- Create: `mangban/mangban-framework/src/main/java/com/mangban/framework/security/handler/AuthenticationEntryPointImpl.java`
- Create: `mangban/mangban-framework/src/main/java/com/mangban/framework/security/handler/AccessDeniedHandlerImpl.java`
- Create: `mangban/mangban-framework/src/main/java/com/mangban/framework/security/permission/HasPermission.java`
- Create: `mangban/mangban-framework/src/main/java/com/mangban/framework/security/permission/PermissionEvaluator.java`
- Create: `mangban/mangban-framework/src/main/java/com/mangban/framework/security/domain/LoginUser.java`

**Interfaces:**
- Consumes: `R`, `BusinessException`, `RedisConfig`
- Produces: `JwtTokenProvider`（Token 生成/验证）, `SecurityConfig`（安全配置链）, `LoginUser`（用户详情）, `HasPermission`（权限注解）

- [ ] **Step 1: 创建 JWT Token 提供者**
- 实现 `createAccessToken(userId, username)`、`createRefreshToken(userId, username)`、`validateToken(token)`、`getUserIdFromToken(token)`
- Access Token 有效期 30 分钟，Refresh Token 7 天
- 使用 jjwt 库（io.jsonwebtoken）

- [ ] **Step 2: 创建 LoginUser**
- 实现 `UserDetails` 接口
- 包含字段：userId, username, password, roles, permissions, enabled

- [ ] **Step 3: 创建 JWT 认证过滤器**
- 继承 `OncePerRequestFilter`
- 从请求头提取 Token → 验证 → 设置 SecurityContext
- 跳过登录/注册/刷新接口

- [ ] **Step 4: 创建安全配置**
- 配置 `SecurityFilterChain`：禁用 CSRF、无状态 Session、配置公开路径
- 配置 `PasswordEncoder`（BCryptPasswordEncoder）
- 注册 JWT 过滤器

- [ ] **Step 5: 创建认证/授权异常处理**
- `AuthenticationEntryPointImpl`：未认证时返回 401
- `AccessDeniedHandlerImpl`：无权限时返回 403

- [ ] **Step 6: 创建权限注解**
- `@HasPermission("system:user:add")` 自定义注解
- `PermissionEvaluator` 实现权限校验逻辑

- [ ] **Step 7: 验证编译**

```bash
cd mangban && mvn compile -pl mangban-framework -am
```

---

### Task 3: 数据库设计 SQL 和系统模块实体

**Files:**
- Create: `mangban/mangban-admin/src/main/resources/schema.sql`
- Create: `mangban/mangban-system/pom.xml`
- Create: `mangban/mangban-system/src/main/java/com/mangban/system/domain/entity/SysUser.java`
- Create: `mangban/mangban-system/src/main/java/com/mangban/system/domain/entity/SysOrganization.java`
- Create: `mangban/mangban-system/src/main/java/com/mangban/system/domain/entity/SysRole.java`
- Create: `mangban/mangban-system/src/main/java/com/mangban/system/domain/entity/SysMenu.java`
- Create: `mangban/mangban-system/src/main/java/com/mangban/system/domain/entity/SysDictType.java`
- Create: `mangban/mangban-system/src/main/java/com/mangban/system/domain/entity/SysDictData.java`
- Create: `mangban/mangban-system/src/main/java/com/mangban/system/domain/entity/SysUserRole.java`
- Create: `mangban/mangban-system/src/main/java/com/mangban/system/domain/entity/SysRoleMenu.java`
- Create: `mangban/mangban-system/src/main/java/com/mangban/system/repository/*.java`（8 个 Repository 接口）

**Interfaces:**
- Consumes: `mangban-common`（基础类）
- Produces: JPA 实体和 Repository

- [ ] **Step 1: 创建 schema.sql**

```sql
-- sys_organization 组织表
CREATE TABLE sys_organization (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT DEFAULT NULL,
    org_name VARCHAR(100) NOT NULL,
    org_code VARCHAR(50) NOT NULL UNIQUE,
    sort_order INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    is_deleted TINYINT DEFAULT 0,
    created_by VARCHAR(50), created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50), updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- sys_user 用户表
CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    nickname VARCHAR(50),
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    avatar VARCHAR(255),
    org_id BIGINT,
    status TINYINT DEFAULT 1,
    is_deleted TINYINT DEFAULT 0,
    created_by VARCHAR(50), created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50), updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (org_id) REFERENCES sys_organization(id)
);

-- sys_role 角色表
CREATE TABLE sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(100) NOT NULL,
    role_code VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    status TINYINT DEFAULT 1,
    is_deleted TINYINT DEFAULT 0,
    created_by VARCHAR(50), created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50), updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- sys_menu 菜单表
CREATE TABLE sys_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    parent_id BIGINT DEFAULT NULL,
    menu_name VARCHAR(100) NOT NULL,
    menu_type TINYINT NOT NULL COMMENT '0=目录 1=菜单 2=按钮',
    path VARCHAR(200),
    component VARCHAR(255),
    permission VARCHAR(100),
    icon VARCHAR(50),
    sort_order INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    is_deleted TINYINT DEFAULT 0,
    created_by VARCHAR(50), created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50), updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- sys_role_menu 角色菜单关联表
CREATE TABLE sys_role_menu (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    FOREIGN KEY (role_id) REFERENCES sys_role(id),
    FOREIGN KEY (menu_id) REFERENCES sys_menu(id)
);

-- sys_user_role 用户角色关联表
CREATE TABLE sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES sys_user(id),
    FOREIGN KEY (role_id) REFERENCES sys_role(id)
);

-- sys_dict_type 字典类型表
CREATE TABLE sys_dict_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dict_name VARCHAR(100) NOT NULL,
    dict_code VARCHAR(50) NOT NULL UNIQUE,
    remark VARCHAR(255),
    status TINYINT DEFAULT 1,
    is_deleted TINYINT DEFAULT 0,
    created_by VARCHAR(50), created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50), updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- sys_dict_data 字典数据表
CREATE TABLE sys_dict_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dict_code VARCHAR(50) NOT NULL,
    label VARCHAR(100) NOT NULL,
    value VARCHAR(100) NOT NULL,
    sort_order INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    is_deleted TINYINT DEFAULT 0,
    created_by VARCHAR(50), created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50), updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (dict_code) REFERENCES sys_dict_type(dict_code)
);
```

- [ ] **Step 2: 创建所有 JPA 实体类**
- 使用 `@Entity`、`@Table`、`@Id`、`@GeneratedValue`、`@ManyToOne`、`@OneToMany` 等注解
- 基类提取 `BaseEntity`（id, createdBy, createdAt, updatedBy, updatedAt, isDeleted）
- `SysOrganization`：自关联 `parent_id → id`

- [ ] **Step 3: 创建所有 Repository 接口**
- 继承 `JpaRepository<T, Long>` + `JpaSpecificationExecutor<T>`
- 自定义查询方法（如 `findByUsername`、`findByParentIdOrderBySortOrder`）

- [ ] **Step 4: 验证编译**

```bash
cd mangban && mvn compile -pl mangban-system -am
```

---

### Task 4: 认证接口实现

**Files:**
- Create: `mangban/mangban-system/src/main/java/com/mangban/system/controller/AuthController.java`
- Create: `mangban/mangban-system/src/main/java/com/mangban/system/service/AuthService.java`
- Create: `mangban/mangban-system/src/main/java/com/mangban/system/service/impl/AuthServiceImpl.java`
- Create: `mangban/mangban-system/src/main/java/com/mangban/system/domain/dto/LoginRequest.java`
- Create: `mangban/mangban-system/src/main/java/com/mangban/system/domain/dto/LoginResponse.java`

**Interfaces:**
- Consumes: `SysUserRepository`, `JwtTokenProvider`, `RedisTemplate`, `PasswordEncoder`
- Produces: `POST /auth/login`, `POST /auth/logout`, `POST /auth/refresh`, `GET /auth/userinfo`, `GET /auth/menus`

- [ ] **Step 1: 创建 DTO 类**

```java
public record LoginRequest(String username, String password) {}
public record LoginResponse(String accessToken, String refreshToken, UserInfo user) {}
```

- [ ] **Step 2: 实现 AuthService**

```java
public interface AuthService {
    LoginResponse login(LoginRequest request);
    void logout(String accessToken);
    LoginResponse refreshToken(String refreshToken);
    UserInfo getCurrentUser();
    List<MenuTree> getCurrentUserMenus();
}
```

- [ ] **Step 3: 实现 AuthController**
- `POST /auth/login`：验证用户名密码 → 生成双 Token → 缓存 Refresh Token 到 Redis
- `POST /auth/logout`：从 Redis 删除 Token
- `POST /auth/refresh`：验证 Refresh Token → 重新生成双 Token
- `GET /auth/userinfo`：获取当前用户信息（含角色、权限标识）
- `GET /auth/menus`：获取当前用户菜单树

- [ ] **Step 4: 验证编译**

```bash
cd mangban && mvn compile -pl mangban-system -am
```

---

### Task 5: 用户管理后端接口

**Files:**
- Create: `mangban/mangban-system/src/main/java/com/mangban/system/controller/UserController.java`
- Create: `mangban/mangban-system/src/main/java/com/mangban/system/service/UserService.java`
- Create: `mangban/mangban-system/src/main/java/com/mangban/system/service/impl/UserServiceImpl.java`
- Create: `mangban/mangban-system/src/main/java/com/mangban/system/domain/dto/UserCreateRequest.java`
- Create: `mangban/mangban-system/src/main/java/com/mangban/system/domain/dto/UserUpdateRequest.java`
- Create: `mangban/mangban-system/src/main/java/com/mangban/system/domain/dto/UserQueryRequest.java`
- Create: `mangban/mangban-system/src/main/java/com/mangban/system/domain/vo/UserVO.java`

**Interfaces:**
- Consumes: `SysUserRepository`, `SysRoleRepository`, `SysOrganizationRepository`, `PasswordEncoder`
- Produces: `GET /users`, `POST /users`, `PUT /users/{id}`, `DELETE /users/{id}`, `PUT /users/{id}/status`, `PUT /users/{id}/reset-password`

- [ ] **Step 1: 实现 UserService**

```java
public interface UserService {
    PageResult<UserVO> list(UserQueryRequest query);
    UserVO getById(Long id);
    UserVO create(UserCreateRequest request);
    UserVO update(Long id, UserUpdateRequest request);
    void delete(Long id);
    void updateStatus(Long id, Integer status);
    void resetPassword(Long id);
}
```

- [ ] **Step 2: 实现 UserController**
- `GET /api/users`：分页查询（支持用户名、组织机构、状态筛选）
- `POST /api/users`：创建用户（默认密码 123456）
- `PUT /api/users/{id}`：修改用户
- `DELETE /api/users/{id}`：逻辑删除（检查非超级管理员）
- `PUT /api/users/{id}/status`：启用/停用
- `PUT /api/users/{id}/reset-password`：重置密码

- [ ] **Step 3: 验证编译**

---

### Task 6: 组织机构、角色、菜单、字典管理后端接口

**Files:**
- Create: `mangban/mangban-system/src/main/java/com/mangban/system/controller/OrganizationController.java`
- Create: `mangban/mangban-system/src/main/java/com/mangban/system/controller/RoleController.java`
- Create: `mangban/mangban-system/src/main/java/com/mangban/system/controller/MenuController.java`
- Create: `mangban/mangban-system/src/main/java/com/mangban/system/controller/DictTypeController.java`
- Create: `mangban/mangban-system/src/main/java/com/mangban/system/controller/DictDataController.java`
- Create: `mangban/mangban-system/src/main/java/com/mangban/system/service/OrganizationService.java` + `impl/*`
- Create: `mangban/mangban-system/src/main/java/com/mangban/system/service/RoleService.java` + `impl/*`
- Create: `mangban/mangban-system/src/main/java/com/mangban/system/service/MenuService.java` + `impl/*`
- Create: `mangban/mangban-system/src/main/java/com/mangban/system/service/DictTypeService.java` + `impl/*`
- Create: `mangban/mangban-system/src/main/java/com/mangban/system/service/DictDataService.java` + `impl/*`
- Create: DTO/VO 类（每个模块各自）

**Interfaces:**
- Consumes: 对应的 Repository
- Produces: 各模块 CRUD REST API

- [ ] **Step 1: 实现组织机构管理接口**
- `GET /api/orgs/tree`：树形查询
- `POST /api/orgs`：创建
- `PUT /api/orgs/{id}`：修改
- `DELETE /api/orgs/{id}`：删除（检查子组织和用户）

- [ ] **Step 2: 实现角色管理接口**
- `GET /api/roles`：分页查询
- `POST /api/roles`：创建
- `PUT /api/roles/{id}`：修改
- `DELETE /api/roles/{id}`：删除（检查用户绑定）
- `GET /api/roles/{id}/menus`：获取角色的菜单 ID 列表
- `PUT /api/roles/{id}/menus`：分配菜单权限

- [ ] **Step 3: 实现菜单管理接口**
- `GET /api/menus/tree`：树形查询
- `POST /api/menus`：创建
- `PUT /api/menus/{id}`：修改
- `DELETE /api/menus/{id}`：删除（检查子菜单）

- [ ] **Step 4: 实现字典类型管理接口**
- `GET /api/dict-types`：分页查询
- `POST /api/dict-types`：创建
- `PUT /api/dict-types/{id}`：修改
- `DELETE /api/dict-types/{id}`：删除（检查字典项）

- [ ] **Step 5: 实现字典数据管理接口**
- `GET /api/dict-types/{dictCode}/data`：查询字典项列表
- `POST /api/dict-types/{dictCode}/data`：创建字典项
- `PUT /api/dict-data/{id}`：修改字典项
- `DELETE /api/dict-data/{id}`：删除字典项
- 字典数据变更时清除 Redis 缓存

- [ ] **Step 6: 验证编译**

```bash
cd mangban && mvn compile -pl mangban-system -am
```

---

### Task 7: 管理后台入口模块（启动类 + 配置）

**Files:**
- Create: `mangban/mangban-admin/pom.xml`
- Create: `mangban/mangban-admin/src/main/java/com/mangban/admin/MangbanApplication.java`
- Create: `mangban/mangban-admin/src/main/resources/application.yml`
- Create: `mangban/mangban-admin/src/main/resources/application-dev.yml`
- Create: `mangban/mangban-admin/src/main/resources/application-prod.yml`

**Interfaces:**
- Consumes: 所有模块
- Produces: 可运行的 Spring Boot 应用

- [ ] **Step 1: 创建入口模块 POM**（依赖 common、framework、system）

- [ ] **Step 2: 创建启动类**

```java
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.mangban.system")
@EntityScan(basePackages = "com.mangban.system")
public class MangbanApplication {
    public static void main(String[] args) {
        SpringApplication.run(MangbanApplication.class, args);
    }
}
```

- [ ] **Step 3: 创建 application.yml**
```yaml
server:
  port: 8080
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mangban?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: root
  jpa:
    hibernate:
      ddl-auto: none
    show-sql: true
  data:
    redis:
      host: localhost
      port: 6379
```

- [ ] **Step 4: 配置多环境文件**
- `application-dev.yml`：开发环境配置
- `application-prod.yml`：生产环境配置

- [ ] **Step 5: 验证完整编译**

```bash
cd mangban && mvn compile
```

---

### Task 8: 前端项目初始化

**Files:**
- Create: `mangban-ui/package.json`
- Create: `mangban-ui/vite.config.ts`
- Create: `mangban-ui/tsconfig.json`
- Create: `mangban-ui/tsconfig.node.json`
- Create: `mangban-ui/index.html`
- Create: `mangban-ui/src/main.ts`
- Create: `mangban-ui/src/App.vue`
- Create: `mangban-ui/src/env.d.ts`
- Create: `mangban-ui/.env.development`
- Create: `mangban-ui/.env.production`
- Create: `mangban-ui/src/styles/index.scss`

- [ ] **Step 1: 初始化 package.json**

```json
{
  "name": "mangban-ui",
  "version": "1.0.0",
  "scripts": {
    "dev": "vite",
    "build": "vue-tsc && vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "vue": "^3.5",
    "vue-router": "^4.5",
    "pinia": "^2.3",
    "element-plus": "^2.9",
    "axios": "^1.7",
    "@element-plus/icons-vue": "^2.3"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.2",
    "vite": "^6.2",
    "typescript": "^5.7",
    "vue-tsc": "^2.2",
    "sass": "^1.85"
  }
}
```

- [ ] **Step 2: 创建 Vite 配置**

```ts
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: { '@': path.resolve(__dirname, 'src') }
  },
  server: {
    port: 3000,
    proxy: {
      '/api': { target: 'http://localhost:8080', changeOrigin: true }
    }
  }
})
```

- [ ] **Step 3: 创建入口文件**
- `main.ts`：创建 Vue 应用、注册 Element Plus、Pinia、Router
- `App.vue`：`<router-view />`
- `index.html`：HTML 入口
- `styles/index.scss`：全局样式重置和变量

- [ ] **Step 4: 安装依赖**

```bash
cd mangban-ui && npm install
```

---

### Task 9: 前端路由、状态管理和 HTTP 封装

**Files:**
- Create: `mangban-ui/src/router/index.ts`
- Create: `mangban-ui/src/stores/user.ts`
- Create: `mangban-ui/src/stores/app.ts`
- Create: `mangban-ui/src/stores/tagsView.ts`
- Create: `mangban-ui/src/utils/request.ts`
- Create: `mangban-ui/src/types/api.ts`
- Create: `mangban-ui/src/types/menu.ts`
- Create: `mangban-ui/src/types/user.ts`

- [ ] **Step 1: 封装 Axios（utils/request.ts）**
- 创建 axios 实例，baseURL 从环境变量读取
- 请求拦截器：注入 Authorization header
- 响应拦截器：统一错误处理，401 自动刷新 Token
- 导出 `request` 函数

- [ ] **Step 2: 配置路由**
- 公共路由：`/login`、`/404`
- 动态路由：使用 `router.addRoute()` 动态添加
- 路由守卫：检查 Token → 加载动态路由 → 跳转目标页

- [ ] **Step 3: 创建 Pinia stores**
- `userStore`：用户信息、Token、角色、权限、登录/退出/刷新
- `appStore`：侧边栏折叠状态、菜单树
- `tagsViewStore`：打开的页签列表、当前激活页签、关闭/刷新操作

- [ ] **Step 4: 创建类型定义**
- `types/api.ts`：`R<T>`、`PageResult<T>` 类型
- `types/menu.ts`：`MenuTree` 类型
- `types/user.ts`：`UserInfo`、`LoginRequest`、`LoginResponse` 类型

---

### Task 10: 前端布局与通用组件

**Files:**
- Create: `mangban-ui/src/layout/index.vue`
- Create: `mangban-ui/src/layout/Navbar.vue`
- Create: `mangban-ui/src/layout/Sidebar/index.vue`
- Create: `mangban-ui/src/layout/Sidebar/SidebarItem.vue`
- Create: `mangban-ui/src/layout/TagsView/index.vue`
- Create: `mangban-ui/src/layout/Main.vue`
- Create: `mangban-ui/src/components/SvgIcon/index.vue`
- Create: `mangban-ui/src/components/Pagination/index.vue`
- Create: `mangban-ui/src/components/SearchForm/index.vue`
- Create: `mangban-ui/src/directives/permission.ts`

- [ ] **Step 1: 实现 Layout 主布局**
- 上下结构：顶部 Navbar + 侧边栏 + TagsView + 主内容区
- Element Plus `el-container` 实现

- [ ] **Step 2: 实现 Navbar**
- 左侧：Logo + 系统名称
- 右侧：消息通知图标（占位） + 用户头像下拉菜单（个人信息、退出登录）

- [ ] **Step 3: 实现 Sidebar**
- 使用 `el-menu` 递归渲染菜单树
- 根据菜单类型（目录/菜单）渲染展开/跳转行为
- 支持侧边栏折叠

- [ ] **Step 4: 实现 TagsView**
- 水平标签栏，展示已打开页面
- 支持点击切换、关闭当前/其他/全部
- 右键菜单：刷新、关闭左侧、关闭右侧、关闭其他

- [ ] **Step 5: 实现权限指令**
- `v-permission="'system:user:add'"` 根据用户权限标识控制元素显示/隐藏

- [ ] **Step 6: 实现通用组件**
- `Pagination`：封装 `el-pagination`
- `SearchForm`：封装通用查询栏布局

---

### Task 11: 前端页面开发

**Files:**
- Create: `mangban-ui/src/views/login/index.vue`
- Create: `mangban-ui/src/views/dashboard/index.vue`
- Create: `mangban-ui/src/views/system/user/index.vue`
- Create: `mangban-ui/src/views/system/user/UserForm.vue`
- Create: `mangban-ui/src/views/system/org/index.vue`
- Create: `mangban-ui/src/views/system/org/OrgForm.vue`
- Create: `mangban-ui/src/views/system/role/index.vue`
- Create: `mangban-ui/src/views/system/role/RoleForm.vue`
- Create: `mangban-ui/src/views/system/role/MenuDialog.vue`
- Create: `mangban-ui/src/views/system/menu/index.vue`
- Create: `mangban-ui/src/views/system/menu/MenuForm.vue`
- Create: `mangban-ui/src/views/system/dict/index.vue`
- Create: `mangban-ui/src/views/system/dict/DictTypeForm.vue`
- Create: `mangban-ui/src/views/system/dict/DictDataDialog.vue`
- Create: `mangban-ui/src/views/error/404.vue`
- Create: `mangban-ui/src/api/system/user.ts`
- Create: `mangban-ui/src/api/system/org.ts`
- Create: `mangban-ui/src/api/system/role.ts`
- Create: `mangban-ui/src/api/system/menu.ts`
- Create: `mangban-ui/src/api/system/dict.ts`
- Create: `mangban-ui/src/api/auth.ts`

- [ ] **Step 1: 创建 API 层文件**
- 每个模块一个 API 文件，封装 Axios 请求
- `auth.ts`：login、logout、refresh、getUserInfo、getMenus

- [ ] **Step 2: 实现登录页面**
- 表单验证、记住密码（localStorage）、登录状态管理
- 登录成功后跳转首页，加载动态路由

- [ ] **Step 3: 实现用户管理页面**
- 列表页：查询栏（用户名、状态、组织机构）+ 表格 + 分页
- 表单弹窗：创建/编辑用户（含角色选择、组织机构树选择）
- 操作列：编辑、删除、状态切换、密码重置

- [ ] **Step 4: 实现组织机构管理页面**
- 树形展示 + 右侧表单弹窗 CRUD
- 拖拽或按钮操作

- [ ] **Step 5: 实现角色管理页面**
- 列表页 + 表单弹窗 + 分配菜单树形对话框（el-tree + 勾选）

- [ ] **Step 6: 实现菜单管理页面**
- 树形展示 + 表单弹窗 CRUD（表单中根据类型动态显示路由/组件/权限字段）

- [ ] **Step 7: 实现数据字典管理页面**
- 左右分栏：左侧字典类型列表（点击选中）→ 右侧字典项列表
- 各带独立的 CRUD 弹窗

- [ ] **Step 8: 实现 404 和首页占位**

---

### Task 12: 初始化数据与部署配置

**Files:**
- Create: `mangban/mangban-admin/src/main/resources/data.sql`
- Create: `mangban-ui/nginx.conf`
- Modify: `mangban/mangban-admin/src/main/resources/application.yml`

- [ ] **Step 1: 创建初始化 SQL 数据**
- 插入超级管理员用户（admin / 123456 BCrypt 加密）
- 插入默认菜单数据（系统管理目录 → 用户管理/角色管理/菜单管理/组织机构/字典管理菜单及按钮权限）
- 插入默认角色（超级管理员）
- 关联超级管理员用户和角色

- [ ] **Step 2: 配置 Nginx 部署**

```nginx
server {
    listen 80;
    server_name localhost;
    location / {
        root /path/to/mangban-ui/dist;
        index index.html;
        try_files $uri $uri/ /index.html;
    }
    location /api/ {
        proxy_pass http://localhost:8080;
    }
}
```

- [ ] **Step 3: 配置 application.yml 的 SQL 初始化模式**
```yaml
spring:
  sql:
    init:
      mode: always
      schema-locations: classpath:schema.sql
      data-locations: classpath:data.sql
```

---

### Task 13: 最终验证

- [ ] **Step 1: 验证后端编译**

```bash
cd mangban && mvn clean compile
```

- [ ] **Step 2: 验证前端构建**

```bash
cd mangban-ui && npm run build
```

- [ ] **Step 3: 验证项目结构完整性**
- 检查所有目录和文件是否存在
- 检查关键文件内容完整性