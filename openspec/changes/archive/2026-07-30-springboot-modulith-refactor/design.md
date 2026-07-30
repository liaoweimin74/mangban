## Context

**项目背景**：石化厂盲板与工艺隔离完整性管理系统，后端 Spring Boot 3.5.0 + Java 21 + Maven 多模块，前端 Vue 3 + Element Plus。

**当前架构**：5 个 Maven 模块——
- `common`：通用工具（统一返回 R、异常处理、配置）
- `framework`：安全框架（JWT、Spring Security）
- `system`：系统管理（用户、角色、菜单、字典、位置）
- `blindplate`：核心业务（隔离点、盲板作业）
- `admin`：启动入口

**当前问题**：
1. 调试需编译多个 jar 包，运行调试不方便
2. `blindplate` 跨模块直接访问 `system` 的 `SysLocationRepository`，模块边界模糊
3. 未来可能拆分为微服务，但当前没有边界约束

**决策依据**：用户确认 SysLocation（工厂→装置→单元位置层级）是盲板业务的基础主数据，应归属于 `blindplate` 模块，而非 `system`。

## Goals / Non-Goals

**Goals:**
- 消除 `blindplate` 对 `system` 的跨模块直接 Repository 访问
- 引入 Spring Modulith 编译期模块边界验证
- 实现 `mvn spring-boot:run` 一键启动调试
- 为未来微服务拆分建立模块间 API 契约

**Non-Goals:**
- 不改变现有 Maven 多模块结构（5 个 module 保持不变）
- 不引入 Modulith 事件总线（当前业务规模不需要）
- 不引入 Modulith 的 Testing/Documentation 特性
- 不重构 `common` 和 `framework` 模块（标记为 OPEN）
- 不改变前端代码

## Decisions

### D1: SysLocation 迁移至 blindplate 模块

`SysLocation`（工厂→装置→单元树形层级）的本质是隔离点管理的基础主数据：
- 隔离点挂在装置/单元下
- 组态监控按装置/区域展示
- 风险等级按隔离点判定

它不属于组织架构数据（用户/角色/部门），因此不应放在 `system`。

**迁移范围**：Entity、Repository、Service、Controller、DTO、VO 共 7 个文件，整体从 `com.mangban.system` 包迁移至 `com.mangban.blindplate` 包。

### D2: 保留 Maven 多模块，Modulith 边界基于 package

Maven 模块是**物理构建单元**，Modulith 模块是**逻辑边界**。两者不冲突：
- Maven 模块决定编译顺序和依赖传递
- Modulith 编译期验证 package 访问权限

`common` 和 `framework` 标记为 `OPEN`（允许任意访问），`system` 和 `blindplate` 使用 named interface 模式。

### D3: 使用 `@ApplicationModule` 标注模块边界

在每个模块的根 package 上使用 `@ApplicationModule` 注解：
- `com.mangban.common` → OPEN
- `com.mangban.framework` → OPEN
- `com.mangban.system` → named interface `system.api`
- `com.mangban.blindplate` → named interface `blindplate.api`

### D4: 不引入 Modulith 事件总线

当前模块间通信量小，业务逻辑主要是同步 CRUD，引入事件总线会增加复杂度。保留未来按需引入的能力。

## Risks / Trade-offs

| 风险 | 缓解措施 |
|------|---------|
| SysLocation 迁移后 system 模块的测试可能受影响 | 迁移后运行完整测试套件验证 |
| Modulith 版本与 Spring Boot 3.5.0 兼容性 | 使用 spring-modulith-bom 管理版本，选用兼容版本 1.4.x |
| 团队对 Modulith 不熟悉 | Modulith API 简洁，学习成本低；文档完善 |
| 迁移后 git 历史丢失 | 迁移是文件移动+包名修改，git 可追踪；如需保留历史可用 `git mv` |
| 未来 Modulith 项目活跃度 | Spring 官方项目，长期维护有保障 |

## Migration Plan

### 第一阶段：迁移 SysLocation 到 blindplate

涉及文件（7 个 system 源文件 → 7 个 blindplate 目标文件）：

| 源文件 | 目标包 |
|--------|--------|
| `system.domain.entity.SysLocation` | `blindplate.domain.entity.SysLocation` |
| `system.repository.SysLocationRepository` | `blindplate.repository.SysLocationRepository` |
| `system.service.LocationService` | `blindplate.service.LocationService` |
| `system.service.impl.LocationServiceImpl` | `blindplate.service.impl.LocationServiceImpl` |
| `system.controller.LocationController` | `blindplate.controller.LocationController` |
| `system.domain.dto.LocationCreateRequest` | `blindplate.domain.dto.LocationCreateRequest` |
| `system.domain.dto.LocationUpdateRequest` | `blindplate.domain.dto.LocationUpdateRequest` |
| `system.domain.vo.LocationTreeNode` | `blindplate.domain.vo.LocationTreeNode` |

同时修改 `IsolationPointServiceImpl` 中的 import 引用（从 `com.mangban.system.*` 改为 `com.mangban.blindplate.*`）。

**验证**：`mvn compile` 通过，`mvn test` 通过。

### 第二阶段：引入 Spring Modulith

1. 根 `pom.xml` 添加 `spring-modulith-bom` 依赖管理
2. `admin/pom.xml` 添加 `spring-modulith-starter-core` 依赖
3. 在 `com.mangban` 根 package 添加 `package-info.java` 标注模块边界：

```java
@ApplicationModule(displayName = "核心业务模块",
    allowedDependencies = {"common", "framework", "system"})
package com.mangban.blindplate;

import org.springframework.modulith.ApplicationModule;
```

```java
@ApplicationModule(displayName = "系统管理模块",
    allowedDependencies = {"common", "framework"})
package com.mangban.system;

import org.springframework.modulith.ApplicationModule;
```

```java
@ApplicationModule(displayName = "基础设施",
    type = ApplicationModule.Type.OPEN)
package com.mangban.common;

import org.springframework.modulith.ApplicationModule;
```

```java
@ApplicationModule(displayName = "安全框架",
    type = ApplicationModule.Type.OPEN)
package com.mangban.framework;

import org.springframework.modulith.ApplicationModule;
```

**验证**：`mvn compile` 通过，Modulith 编译期验证无违规报告。

### 第三阶段（可选）：添加 Modulith 测试

```java
@ApplicationModuleTest
class BlindplateModuleTest {
    @Test
    void verifyModuleStructure() {
        // Modulith 自动验证模块边界
    }
}
```

## Open Questions

无。设计已确认。