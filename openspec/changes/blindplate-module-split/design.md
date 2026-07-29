# Design: 盲板模块拆分 + 前端目录重命名

## Context

当前后端盲板业务（IsolationPoint）代码位于 `mangban-system` 模块中，包路径 `com.mangban.system.*`。前端盲板页面位于 `views/process/`，路由路径为 `/process/*`。Spring Boot 启动类 `MangbanApplication` 配置了 `@SpringBootApplication`、`@EnableJpaRepositories` 和 `@EntityScan` 扫描 `com.mangban` 基础包，因此新模块的包名必须保持 `com.mangban.*` 前缀。

## Goals / Non-Goals

**Goals:**
- 后端新建 `mangban-blindplate` Maven 子模块，包含全部 IsolationPoint 代码
- 从 `mangban-system` 中删除 IsolationPoint 相关代码
- 前端 `views/process/` → `views/blindplate/`
- 前端路由 `/process/*` → `/blindplate/*`
- 更新数据库菜单表中的路由路径

**Non-Goals:**
- 不改动盲板业务的 API 接口路径（仍为 `/api/isolation-points/*`）
- 不改动盲板业务逻辑（纯文件迁移）
- 不改动其他模块代码

## Decisions

1. **新模块包结构**：`com.mangban.blindplate.controller`、`com.mangban.blindplate.service`、`com.mangban.blindplate.domain`（含 entity/dto/vo）、`com.mangban.blindplate.repository`。遵循 `com.mangban.*` 前缀，确保 Spring 自动扫描生效。
2. **mangban-system 清理**：删除 `com.mangban.system` 下所有 IsolationPoint 相关文件（controller/Entity/Repository/Service/Impl/DTO/VO），删除后保留空包不提交。
3. **前端路由调整**：`router/index.ts` 中将 `process` 替换为 `blindplate`，同步更新路由 path 和 component 的 import 路径。
4. **菜单数据更新**：执行 SQL UPDATE 更新菜单表中 route_path 字段，`/process/` → `/blindplate/`。
5. **依赖关系**：`mangban-blindplate` 依赖 `mangban-common` 和 `mangban-framework`；`mangban-admin` 新增依赖 `mangban-blindplate`（同时保留 `mangban-system`，因为系统管理功能仍然需要）。

## Risks / Trade-offs

- **[Risk] 迁移遗漏**：可能遗漏某些引用 IsolationPoint 的文件。通过编译验证（`mvn compile`）确保无遗漏。
- **[Risk] 菜单数据不同步**：如果数据库菜单表未更新，用户点击菜单会跳转到旧路径。通过 SQL UPDATE 脚本保证同步。
- **[Risk] 前端 import 引用遗漏**：如果其他页面引用了 `views/process/` 下的组件。通过 `grep` 搜索所有引用确保无遗漏。
- **[Trade-off] 代码复制而非重构**：本次纯迁移，不修改代码逻辑。后续可在新模块中独立优化。