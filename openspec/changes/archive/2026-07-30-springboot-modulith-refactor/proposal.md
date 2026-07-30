## Why

本项目当前使用 Maven 多模块架构（5 个模块），存在以下问题：
1. **调试不便**：每次修改需编译多个 jar 包才能运行
2. **模块边界模糊**：`blindplate` 业务模块直接访问 `system` 模块的 Repository，缺乏约束
3. **未来拆分风险**：计划拆分为微服务，但当前没有模块间 API 契约

引入 Spring Modulith 可以在不改变 Maven 模块结构的前提下，通过 package 级别的编译期验证强制模块边界，同时保持单一部署单元实现一键启动调试，为未来微服务拆分打下基础。

## What Changes

### 第一阶段：迁移 SysLocation 到 blindplate
- 将 `SysLocation`（工厂→装置→单元位置层级）从 `system` 模块整体迁移至 `blindplate` 模块
- 涉及 7 个文件：Entity、Repository、Service、Controller、DTO、VO
- 消除 `blindplate` 对 `system` 的唯一跨模块依赖

### 第二阶段：引入 Spring Modulith
- 添加 `spring-modulith-bom` 和 `spring-modulith-starter-core` 依赖
- 用 `@ApplicationModule` 标注各模块的 package 边界
- `common` 和 `framework` 标记为 OPEN 模块
- `system` 和 `blindplate` 使用 named interface 模式

## Capabilities

### New Capabilities
- `springboot-modulith-modularization` — Spring Modulith 模块化架构

## Impact

**代码影响**：
- `system` 模块：移除 7 个文件（SysLocation 相关）
- `blindplate` 模块：新增 7 个文件（SysLocation 迁移过来），修改 1 个文件（IsolationPointServiceImpl 的 import）
- 根 pom.xml：添加 Modulith BOM 依赖管理
- admin/pom.xml：添加 spring-modulith-starter-core 依赖
- 各模块根 package：新增 package-info.java 标注模块边界

**依赖影响**：
- `blindplate` 不再依赖 `system` 的内部实现
- `system` 职责更纯粹（仅保留用户/角色/菜单/字典）

**技术栈**：Spring Modulith 1.4.x（兼容 Spring Boot 3.5.0）