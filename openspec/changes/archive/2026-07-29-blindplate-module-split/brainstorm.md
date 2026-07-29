# Brainstorm: 盲板模块拆分 + 前端目录重命名

## Design Summary

后端将盲板业务（IsolationPoint）从 mangban-system 模块分离为独立模块 mangban-blindplate；前端将 `views/process/` 目录重命名为 `views/blindplate/`，路由路径同步更新。

## Alternatives Considered

### 方案 A：新建独立后端模块 mangban-blindplate（Agreed）
- **做法**：创建 `mangban-blindplate` Maven 子模块，将 mangban-system 中所有 IsolationPoint 相关代码（controller/service/repository/entity/dto/vo）迁移过去，并调整 pom.xml 依赖关系
- **优点**：
  - 模块职责清晰，system 不再耦合盲板业务
  - 独立模块便于独立部署和版本管理
  - 符合单一职责原则
- **缺点**：
  - 需要调整 mangban-admin 的 pom 依赖
  - 迁移过程需确保 import 路径和 Spring 扫描范围正确

### 方案 B：在 mangban-system 内拆包
- **做法**：不创建新模块，仅在 mangban-system 内将 IsolationPoint 相关类移到 `com.mangban.system.blindplate` 子包
- **优点**：改动最小，无需调整 pom.xml
- **缺点**：模块边界不清晰，system 仍然包含盲板代码，长期维护仍然混乱

### 方案 C：将盲板独立为微服务
- **做法**：创建独立 Spring Boot 应用，通过 REST API 与其他模块通信
- **优点**：完全解耦，独立扩缩容
- **缺点**：过度设计，当前规模不需要微服务架构，引入网络通信开销

## Agreed Approach

采用方案 A：
1. 后端：新建 `mangban-blindplate` 模块，从 `mangban-system` 迁移所有 IsolationPoint 相关代码
2. 前端：`views/process/` → `views/blindplate/`，路由 `/process/*` → `/blindplate/*`

## Key Decisions

1. **模块命名**：使用 `mangban-blindplate`（而非 `mangban-isolation-point`），与前端目录 `blindplate` 保持一致
2. **mangban-system 清理**：迁移后删除 mangban-system 中 IsolationPoint 相关文件和空包
3. **前端路由**：`/process/isolation-points` → `/blindplate/isolation-points`，`/process/isolation-points/status` → `/blindplate/isolation-points/status`
4. **依赖关系**：`mangban-blindplate` 依赖 `mangban-common` 和 `mangban-framework`；`mangban-admin` 新增依赖 `mangban-blindplate`
5. **菜单数据**：更新数据库中菜单表的路由路径（/process → /blindplate）

## Open Questions

无 — 需求明确